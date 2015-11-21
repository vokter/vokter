/*
 * Copyright 2015 Ed Duarte
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.edduarte.argus;

import com.edduarte.argus.diff.Difference;
import com.edduarte.argus.diff.DifferenceDetector;
import com.edduarte.argus.document.Document;
import com.edduarte.argus.document.DocumentBuilder;
import com.edduarte.argus.document.DocumentCollection;
import com.edduarte.argus.job.JobManager;
import com.edduarte.argus.job.JobManagerHandler;
import com.edduarte.argus.keyword.Keyword;
import com.edduarte.argus.keyword.KeywordBuilder;
import com.edduarte.argus.parser.Parser;
import com.edduarte.argus.parser.ParserPool;
import com.edduarte.argus.parser.SimpleParser;
import com.edduarte.argus.rest.WatchRequest;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
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


/**
 * @author Ed Duarte (<a href="mailto:ed@edduarte.com">ed@edduarte.com</a>)
 * @version 1.3.0
 * @since 1.0.0
 */
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
     * A parser-pool that contains a set number of parsers. When the last
     * parser from the pool is removed, future parsing workers will be locked
     * until a used parser is placed back in the pool.
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
     * The MongoDB database for detected differences between snapshots for
     * each document.
     */
    private DB differencesDB;


    /**
     * The Bayesian detection model that allows detection of language of input
     * text.
     */
    private LanguageDetector langDetector;

    /**
     * The collection instance that will contain all imported and processed
     * corpus during the context's execution.
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
        jobManager = JobManager.create("argus_job_manager", 420, this);
        parserPool = new ParserPool();
    }


    public static Context getInstance() {
        return instance;
    }


    public boolean createJob(final WatchRequest request) {
        return jobManager.createJob(request);
    }


    public boolean cancelJob(String requestUrl, final String responseUrl) {
        return jobManager.cancelMatchingJob(requestUrl, responseUrl);
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
        DocumentBuilder builder = DocumentBuilder
                .fromUrl(url)
                .withLanguageDetector(langDetector);

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

            if (!results.isEmpty()) {
                BulkWriteOperation bulkOp = diffColl.initializeUnorderedBulkOperation();
                results.forEach(bulkOp::insert);
                bulkOp.execute();
                bulkOp = null;
                diffColl = null;
            }
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
        KeywordBuilder builder = KeywordBuilder
                .fromText(keywordInput)
                .withLanguageDetector(langDetector);

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

        List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();
        langDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .withProfiles(languageProfiles)
                .build();

        collection = new DocumentCollection(
                "argus_production_collection",
                documentsDB,
                occurrencesDB
        );

        logger.info("Starting jobs...");
        jobManager.initialize();

        logger.info("Starting parsers...");
        for (int i = 1; i < maxThreads; i++) {
            Parser p = new SimpleParser();
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
