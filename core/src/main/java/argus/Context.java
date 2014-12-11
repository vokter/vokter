package argus;

import argus.document.Document;
import argus.document.DocumentBuilder;
import argus.document.DocumentCollection;
import argus.job.JobPool;
import argus.parser.GeniaParser;
import argus.parser.Parser;
import argus.parser.ParserPool;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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

    private static final String TERMS_DB = "argus_terms_db";
    private static final String DOCUMENTS_DB = "argus_documents_db";

    private static final Context instance = new Context();
    private final JobPool jobPool;
    private final ParserPool parserPool;
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
     * of equal terms with different casing.
     */
    private boolean ignoreCase = true;
    private MongoClient mongoClient;
    private DB documentsDatabase;
    private DB termsDatabase;


    private Context() {
        super();
        initialized = false;
        jobPool = new JobPool();
        parserPool = new ParserPool();
    }


    public static Context getInstance() {
        return instance;
    }

    public static void main(String[] args) {

        installUncaughtExceptionHandler();

        CommandLineParser parser = new GnuParser();
        Options options = new Options();

        options.addOption("t", "threads", true, "Number of max threads to be used "
                + "for computation and indexing processes. By default, this will "
                + "be the number of available cores.");

        options.addOption("port", "port", true, "Server port.");

        options.addOption("nocase", "ignore-case", false, "Ignores differentiation "
                + "between equal words with different casing.");

        options.addOption("stop", "stopwords", false, "Perform stopword filtering.");

        options.addOption("stem", "stemming", false, "Perform stemming.");

        options.addOption("h", "help", false, "Shows this help prompt.");


        CommandLine commandLine;
        try {
            // Parse the program arguments
            commandLine = parser.parse(options, args);
        } catch (ParseException ex) {
            logger.error("There was a problem processing the input arguments.");
            logger.error(ex.getMessage(), ex);
            return;
        }


        if (commandLine.hasOption("h")) {
            String usage = "[-h] [-port] [-t] [-nocase] [-stop] [-stem]";
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar argus-core.jar " + usage, options);
            return;
        }


        // Get threads
        int maxThreads = Runtime.getRuntime().availableProcessors() - 1;
        maxThreads = maxThreads > 0 ? maxThreads : 1;
        if (commandLine.hasOption('t')) {
            String threadsText = commandLine.getOptionValue('t');
            maxThreads = Integer.parseInt(threadsText);
            if (maxThreads <= 0 || maxThreads > 32) {
                logger.error("Invalid number of threads. Must be a number between 1 and 32.");
                return;
            }
        }


        // Get port
        int port = 8080;
        if (commandLine.hasOption("port")) {
            String portString = commandLine.getOptionValue("port");
            port = Integer.parseInt(portString);
        }


        // Set JSP to always use Standard JavaC
        System.setProperty("org.apache.jasper.compiler.disablejsr199", "false");


        boolean isStoppingEnabled = false;
        boolean isStemmingEnabled = false;
        boolean isIgnoringCase = false;

        if (commandLine.hasOption("stop")) {
            isStoppingEnabled = true;
        }

        if (commandLine.hasOption("stem")) {
            isStemmingEnabled = true;
        }

        if (commandLine.hasOption("nocase")) {
            isIgnoringCase = true;
        }


        try {
            Context context = new Context();
            context.setStopwordsEnabled(isStoppingEnabled);
            context.setStemmingEnabled(isStemmingEnabled);
            context.setIgnoreCase(isIgnoringCase);

            context.initialize(port, maxThreads);

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.info("Shutting down the server...");
            System.exit(1);
        }
    }

    private static void installUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            if (e instanceof ThreadDeath) {
                logger.warn("Ignoring uncaught ThreadDead exception.");
                return;
            }
            logger.error("Uncaught exception on cli thread, aborting.", e);
            System.exit(0);
        });
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

        Document d = builder.build(termsDatabase, parserPool);
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
    public void initialize(int port, int maxThreads) throws Exception {
        if (initialized) {
            return;
        }

        mongoClient = new MongoClient("localhost", 27017);

        documentsDatabase = mongoClient.getDB(DOCUMENTS_DB);
        termsDatabase = mongoClient.getDB(TERMS_DB);

        collection = new DocumentCollection(
                "argus_production_collection",
                documentsDatabase,
                termsDatabase
        );

        jobPool.initialize(maxThreads);

        logger.info("Starting parsers...");
        for (int i = 1; i < maxThreads; i++) {
            Parser p = new GeniaParser();
            parserPool.place(p);
        }


        logger.info("Starting server...");
        // Start a Jetty server with some sensible defaults
        Server server = new Server();
        server.setStopAtShutdown(true);


        // Increases thread pool
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMaxThreads(maxThreads);
        server.setThreadPool(threadPool);


        // Ensures a non-blocking connector (NIO) is used.
        Connector connector = new SelectChannelConnector();
        connector.setPort(port);
        connector.setMaxIdleTime(30000);
        server.setConnectors(new Connector[]{connector});


        // Retrieves the jar file.
        ProtectionDomain protectionDomain = Context.class.getProtectionDomain();
        String jarFilePath = protectionDomain.getCodeSource().getLocation().toExternalForm();


        // Associates the web context (which includes all files in the 'resources'
        // folder to the jar file.
        WebAppContext context = new WebAppContext(jarFilePath, "/");
        context.setServer(server);


        // Adds the handlers for the jar context file.
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
        jobPool.clear();
        parserPool.clear();
        initialized = false;
    }

    @Override
    public void lifeCycleStopped(LifeCycle lifeCycle) {
    }
}
