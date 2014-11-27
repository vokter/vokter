package argus;

import argus.document.Document;
import argus.document.DocumentCollection;
import com.mongodb.MongoClient;
import org.apache.commons.cli.*;
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
import java.util.Map;
import java.util.concurrent.*;


public class Context
        implements LifeCycle.Listener {

    private static final Logger logger = LoggerFactory.getLogger(Context.class);

    private static Context instance;


    private Server server;

    private MongoClient mongoClient;

    private ScheduledExecutorService executor;

    private Map<String, ScheduledFuture<?>> watchExecutions = new ConcurrentHashMap<>();


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
     * The port where the server is loaded to.
     */
    private int port;


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


    private Context() {
        super();
        initialized = false;
    }


    public static Context getInstance() {
        if (instance == null) {
            instance = new Context();
        }
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
            Context context = Context.getInstance();
            context.setStopwordsEnabled(isStoppingEnabled);
            context.setStemmingEnabled(isStemmingEnabled);
            context.setIgnoreCase(isIgnoringCase);

            context.startServer(port, maxThreads);

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

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    /**
     * Starts this REST context at the specified port, using the specified number
     * of threads and wrapping the specified collection and stopwords for queries.
     */
    public void startServer(int port,
                            int maxThreads) throws Exception {
        if (initialized) {
            return;
        }

        logger.info("Starting server...");

        this.port = port;

        server = new Server();

        mongoClient = new MongoClient("localhost", 27017);

        executor = Executors.newScheduledThreadPool(maxThreads);


        // Start a Jetty server with some sensible defaults
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
        initialized = true;

        logger.info("Server started at 'localhost:" + port + "'");
        logger.info("Press Ctrl+C to shutdown the server...");

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


//    /**
//     * Indexes the specified directory's documents and saves the resulting index
//     * of all occurrences into the specified folder, separated by first-character
//     * in different files, and the resulting documents into the specified folder,
//     * separated by id.
//     */
//    public void createCollectionFromDir(String directoryPath, File outputIndexFolder, File outputDocumentsFolder) {
//        DocumentCollectionBuilder builder = DocumentCollectionBuilder.fromDir(Paths.get(directoryPath));
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
//        this.collection = builder.buildInFolders(outputIndexFolder, outputDocumentsFolder);
//    }
//
//
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
    public void lifeCycleStopping(LifeCycle lifeCycle) {
        mongoClient.close();
        watchExecutions.values().stream()
                .filter(handle -> handle != null)
                .forEach(handle -> handle.cancel(true));
        watchExecutions.clear();
        if (!executor.isShutdown()) {
            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

    }

    @Override
    public void lifeCycleStopped(LifeCycle lifeCycle) {
    }

    public void scheduleJob(final Document document) {

        ScheduledFuture<?> handle =
                executor.scheduleWithFixedDelay(() -> {


                }, 10, 10, TimeUnit.MINUTES);
        watchExecutions.put(document.getUrl(), handle);
    }

    public void cancelScheduledJob(String documentUrl) {
        ScheduledFuture<?> handle = watchExecutions.get(documentUrl);
        if (handle != null) {
            handle.cancel(true);
            watchExecutions.remove(documentUrl);
        }
    }
}
