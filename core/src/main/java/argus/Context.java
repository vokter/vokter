package argus;

import argus.document.Document;
import argus.document.DocumentBuilder;
import argus.document.DocumentCollection;
import argus.job.JobManager;
import argus.parser.GeniaParser;
import argus.parser.Parser;
import argus.parser.ParserPool;
import com.mongodb.DB;
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


public class Context implements LifeCycle.Listener {

    private static final Logger logger = LoggerFactory.getLogger(Context.class);

    private static final String DOCUMENTS_DB = "argus_documents_db";
    private static final String OCCURRENCES_DB = "argus_occurrences_db";

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
        jobManager = new JobManager();
        parserPool = new ParserPool();
    }


    public static Context getInstance() {
        return instance;
    }

    /**
     * Indexes the specified document and saves the resulting index
     * of all occurrences for future query and comparison.
     */
    public void addDocumentFromUrl(String url) {
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

        Document d = builder.build(occurrencesDB, parserPool);
        this.collection.add(d);
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

        collection = new DocumentCollection(
                "argus_production_collection",
                documentsDB,
                occurrencesDB
        );

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


//    /**
//     * Processes the specified string as a search and searches this context
//     * collection.
//     */
//    public QueryResult searchCollection(MutableString queryText, int slop) {
//
//        QueryBuilder builder = QueryBuilder.newBuilder();
//        builder.withText(queryText);
//        builder.withSlop(slop);
//
//        if (isStoppingEnabled) {
//            builder.withStopwords(loadedStopwords);
//        }
//
//        if (isStemmingEnabled) {
//            builder.withStemmer(PortugueseStemmer.class);
//        }
//
//        if (ignoreCase) {
//            builder.ignoreCase();
//        }
//
//        Query query = builder.build();
//
//        return collection.search(query);
//    }

    @Override
    public void lifeCycleStarted(LifeCycle lifeCycle) {
    }

    @Override
    public void lifeCycleFailure(LifeCycle lifeCycle, Throwable throwable) {
    }

    @Override
    public void lifeCycleStopping(LifeCycle lifeCycle) {
        mongoClient.close();
        jobManager.stop();
        parserPool.clear();
        initialized = false;
    }

    @Override
    public void lifeCycleStopped(LifeCycle lifeCycle) {
    }
}
