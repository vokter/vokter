package argus;

import argus.diff.DifferenceDetector;
import argus.diff.Difference;
import argus.document.Document;
import argus.document.DocumentBuilder;
import argus.document.DocumentCollection;
import argus.job.JobManager;
import argus.job.JobManagerHandler;
import argus.keyword.Keyword;
import argus.keyword.KeywordBuilder;
import argus.parser.GeniaParser;
import argus.parser.Parser;
import argus.parser.ParserPool;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.ProtectionDomain;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class Context implements LifeCycle.Listener, JobManagerHandler {

    private static final Logger logger = LoggerFactory.getLogger(Context.class);

    private static final String DOCUMENTS_DB = "argus_documents_db";
    private static final String OCCURRENCES_DB = "argus_occurrences_db";
    private static final String DIFFERENCES_DB = "argus_differences_db";

    private static final Context instance;

    static {
        Context aux;
        try {
            aux = new Context();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            aux = null;
        }
        instance = aux;
    }

    /**
     * A manager for all Quartz jobs, handling scheduling and persistence of
     * asynchronous processes for document differences detection and
     * keyword-difference matching.
     */
    private final JobManager jobManager;
    /**
     * A parser-pool that contains a set number of parsers. When the last parser
     * from the pool is removed, future parsing workers will be locked until
     * a used parser is placed back in the pool.
     */
    private final ParserPool parserPool;
    /**
     * The client for the used MongoDB database.
     */
    private MongoClient mongoClient;
    /**
     * The MongoDB database for fetched documents.
     */
    private DB documentsDB;
    /**
     * The MongoDB database for parsed occurrences for each document.
     */
    private DB occurrencesDB;
    /**
     * The MongoDB database for detected differences between snapshots for each
     * document.
     */
    private DB differencesDB;
    /**
     * The collection instance that will contain all imported and processed corpus
     * during the context's execution.
     */
    private DocumentCollection collection;

    /**
     * Flag that locks server shutdown until it is properly initialized.
     */
    private boolean initialized;

    /**
     * Flag that sets the QueryProcessor usage of
     * stopword filtering.
     */
    private boolean isStoppingEnabled = true;

    /**
     * Flag that sets the QueryProcessor usage of
     * a porter stemmer.
     */
    private boolean isStemmingEnabled = true;

    /**
     * Flag that sets the QueryProcessor matching
     * of equal occurrences with different casing.
     */
    private boolean ignoreCase = true;


    private Context() throws Exception {
        super();
        initialized = false;
        jobManager = JobManager.create("argus_job_manager", this);
        parserPool = new ParserPool();
    }

    public static Context getInstance() {
        return instance;
    }

    /**
     * Indexes the specified document and detects differences between an older
     * snapshot and the new one. Once differences are collected, saves the resulting
     * index of all occurrences of the new snapshot for future query and comparison
     * jobs.
     */
    @Override
    public boolean detectDifferences(String url) {

        // create a new document snapshot for the provided url
        DocumentBuilder builder = DocumentBuilder.fromUrl(url);
        if (isStoppingEnabled) {
            builder.withStopwords();
        }
        if (isStemmingEnabled) {
            builder.withStemming();
        }
        if (ignoreCase) {
            builder.ignoreCase();
        }
        Document newDocument = builder.build(occurrencesDB, parserPool);
        if (newDocument == null) {
            // A problem occurred during processing, mostly during the fetching phase.
            // This could happen if the page was unavailable at the time.
            return false;
        }

        // check if there is a older document in the collection
        Document oldDocument = collection.get(url);

        if (oldDocument != null) {
            // there was already a document for this url on the collection, so
            // detect differences between them and add them to the differences
            // database
            DifferenceDetector detector = new DifferenceDetector(oldDocument, newDocument, parserPool);
            List<Difference> results = detector.call();

            removeExistingDifferences(url);
            DBCollection diffColl = differencesDB.getCollection(url);

            BulkWriteOperation bulkOp = diffColl.initializeUnorderedBulkOperation();
            results.forEach(bulkOp::insert);
            bulkOp.execute();
            bulkOp = null;
            diffColl = null;
        }

        //replace the old document in the collection with the new one
        collection.remove(url);
        collection.add(newDocument);

        return true;
    }

    /**
     * Collects the existing differences that were stored in the database.
     */
    @Override
    public List<Difference> getExistingDifferences(String url) {
        DBCollection diffColl = differencesDB.getCollection(url);
        Iterable<DBObject> cursor = diffColl.find();
        return StreamSupport.stream(cursor.spliterator(), true)
                .map(Difference::new)
                .collect(Collectors.toList());
    }

    /**
     * Removes existing differences for the specified url
     */
    @Override
    public void removeExistingDifferences(String url) {
        DBCollection diffColl = differencesDB.getCollection(url);
        diffColl.drop();
    }

    /**
     * Process and build keyword objects based on this context configuration
     */
    @Override
    public Keyword buildKeyword(String keywordInput) {
        KeywordBuilder builder = KeywordBuilder.fromText(keywordInput);
        if (isStoppingEnabled) {
            builder.withStopwords();
        }
        if (isStemmingEnabled) {
            builder.withStemming();
        }
        if (ignoreCase) {
            builder.ignoreCase();
        }
        return builder.build(parserPool);
    }


    public void setStopwordsEnabled(boolean isStoppingEnabled) {
        this.isStoppingEnabled = isStoppingEnabled;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public boolean isStoppingEnabled() {
        return isStoppingEnabled;
    }

    public boolean isStemmingEnabled() {
        return isStemmingEnabled;
    }

    public void setStemmingEnabled(boolean isStemmingEnabled) {
        this.isStemmingEnabled = isStemmingEnabled;
    }

    public boolean isIgnoringCase() {
        return ignoreCase;
    }

    /**
     * Starts this REST context at the specified port, using the specified number
     * of threads and wrapping the specified collection and stopwords for queries.
     */
    public void start(final int port,
                      final int maxThreads,
                      final String dbHost,
                      final int dbPort) throws Exception {
        if (initialized) {
            return;
        }

        // Set JSP to always use Standard JavaC
        System.setProperty("org.apache.jasper.compiler.disablejsr199", "false");

        mongoClient = new MongoClient(dbHost, dbPort);

        documentsDB = mongoClient.getDB(DOCUMENTS_DB);
        occurrencesDB = mongoClient.getDB(OCCURRENCES_DB);
        differencesDB = mongoClient.getDB(DIFFERENCES_DB);

        collection = new DocumentCollection(
                "argus_production_collection",
                documentsDB,
                occurrencesDB
        );

        logger.info("Starting jobs...");
        jobManager.initialize(maxThreads);

        logger.info("Starting parsers...");
        for (int i = 1; i < maxThreads; i++) {
            Parser p = new GeniaParser();
            parserPool.place(p);
        }

        logger.info("Starting server...");
        Server server = new Server();
        server.setStopAtShutdown(true);

        // Increase server thread pool
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMaxThreads(maxThreads);
        server.setThreadPool(threadPool);


        // Ensure a non-blocking connector (NIO) is used.
        Connector connector = new SelectChannelConnector();
        connector.setPort(port);
        connector.setMaxIdleTime(30000);
        server.setConnectors(new Connector[]{connector});


        // Retrieve the jar file.
        ProtectionDomain protectionDomain = Context.class.getProtectionDomain();
        String jarFilePath = protectionDomain.getCodeSource().getLocation().toExternalForm();


        // Associate the web context (which includes all files in the 'resources'
        // folder to the jar file.
        WebAppContext context = new WebAppContext(jarFilePath, "/");
        context.setServer(server);


        // Add the handlers for the jar context file.
        HandlerList handlers = new HandlerList();
        handlers.addHandler(context);
        server.setHandler(handlers);
        server.addLifeCycleListener(this);

        server.start();

        logger.info("Server started at 'localhost:" + port + "'");
        logger.info("Press Ctrl+C to shutdown the server...");
        initialized = true;

        server.join();
    }

    @Override
    public void lifeCycleStarting(LifeCycle lifeCycle) {
    }

    @Override
    public void lifeCycleStarted(LifeCycle lifeCycle) {
    }

    @Override
    public void lifeCycleFailure(LifeCycle lifeCycle, Throwable throwable) {
    }

    @Override
    public void lifeCycleStopping(LifeCycle lifeCycle) {
    }

    @Override
    public void lifeCycleStopped(LifeCycle lifeCycle) {
        jobManager.stop();
        parserPool.clear();
        mongoClient.close();
        initialized = false;
    }
}
