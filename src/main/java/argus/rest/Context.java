package argus.rest;

import argus.Main;
import argus.index.Collection;
import argus.index.CollectionBuilder;
import argus.query.Query;
import argus.query.QueryBuilder;
import argus.query.QueryResult;
import argus.stemmer.PortugueseStemmer;
import it.unimi.dsi.lang.MutableString;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.Set;


public class Context
        extends org.eclipse.jetty.server.Server
        implements LifeCycle.Listener {

    private static final Logger logger = LoggerFactory.getLogger(Context.class);

    private static Context instance;


    /**
     * This context's stopwords, which are used on queries.
     */
    private Set<MutableString> loadedStopwords;


    /**
     * The index instance that will contain all processed and
     * imported corpus during the context's execution.
     */
    private Collection collection;


    /**
     * Flag that locks context join and shutdown until it is
     * properly initialized.
     */
    private boolean initialized;


    /**
     * The port where the context is loaded to.
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


    public void setStopwordsEnabled(boolean isStoppingEnabled) {
        this.isStoppingEnabled = isStoppingEnabled;
    }


    public void setStemmingEnabled(boolean isStemmingEnabled) {
        this.isStemmingEnabled = isStemmingEnabled;
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


    public boolean isIgnoringCase() {
        return ignoreCase;
    }


    public void setStopwords(Set<MutableString> stopwords) {
        this.loadedStopwords = stopwords;
    }

    /**
     * Starts this REST context at the specified port, using the specified number
     * of threads and wrapping the specified collection and stopwords for queries.
     */
    public void start(int port,
                      int maxThreads,
                      Set<MutableString> loadedStopwords) throws Exception {
        if (initialized) {
            return;
        }

        logger.info("Starting server...");

        this.port = port;
        this.loadedStopwords = loadedStopwords;

        // Start a Jetty server with some sensible defaults
        setStopAtShutdown(true);


        // Increases thread pool
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMaxThreads(maxThreads);
        setThreadPool(threadPool);


        // Ensures a non-blocking connector (NIO) is used.
        Connector connector = new SelectChannelConnector();
        connector.setPort(port);
        connector.setMaxIdleTime(30000);
        setConnectors(new Connector[]{connector});


        // Retrieves the jar file.
        ProtectionDomain protectionDomain = Main.class.getProtectionDomain();
        String jarFilePath = protectionDomain.getCodeSource().getLocation().toExternalForm();


        // Associates the web context (which includes all files in the 'resources'
        // folder to the jar file.
        WebAppContext context = new WebAppContext(jarFilePath, "/");
        context.setServer(this);


        // Adds the handlers for the jar context file.
        HandlerList handlers = new HandlerList();
        handlers.addHandler(context);
        setHandler(handlers);
        addLifeCycleListener(this);

        super.start();
        initialized = true;
    }

    @Override
    public void join() throws InterruptedException {
        if (!initialized) {
            throw new RuntimeException("Server was not initialized!");
        }
        logger.info("Server started at 'localhost:" + port + "'");
        logger.info("Press Ctrl+C to shutdown the server...");
        super.join();
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
    }


    /**
     * Indexes the specified directory's documents and saves the resulting index
     * of all occurrences into the specified folder, separated by first-character
     * in different files, and the resulting documents into the specified folder,
     * separated by id.
     */
    public void createCollectionFromDir(String directoryPath, File outputIndexFolder, File outputDocumentsFolder) {
        CollectionBuilder builder = CollectionBuilder.fromDir(Paths.get(directoryPath));

        if (isStoppingEnabled) {
            builder.withStopwords(loadedStopwords);
        }

        if (isStemmingEnabled) {
            builder.withStemmer(PortugueseStemmer.class);
        }

        if (ignoreCase) {
            builder.ignoreCase();
        }

        this.collection = builder.buildInFolders(outputIndexFolder, outputDocumentsFolder);
    }


    /**
     * Processes the specified string as a search and searches this context
     * collection.
     */
    public QueryResult searchCollection(MutableString queryText, int slop) {

        QueryBuilder builder = QueryBuilder.newBuilder();
        builder.withText(queryText);
        builder.withSlop(slop);

        if (isStoppingEnabled) {
            builder.withStopwords(loadedStopwords);
        }

        if (isStemmingEnabled) {
            builder.withStemmer(PortugueseStemmer.class);
        }

        if (ignoreCase) {
            builder.ignoreCase();
        }

        Query query = builder.build();

        return collection.search(query);
    }
}
