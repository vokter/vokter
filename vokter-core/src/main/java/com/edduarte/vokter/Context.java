/*
 * Copyright 2015 Eduardo Duarte
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

package com.edduarte.vokter;

import com.edduarte.vokter.diff.DiffDetector;
import com.edduarte.vokter.diff.DiffEvent;
import com.edduarte.vokter.diff.DiffMatcher;
import com.edduarte.vokter.diff.Match;
import com.edduarte.vokter.document.DocumentBuilder;
import com.edduarte.vokter.document.DocumentCollection;
import com.edduarte.vokter.document.DocumentPair;
import com.edduarte.vokter.job.JobManager;
import com.edduarte.vokter.job.JobManagerHandler;
import com.edduarte.vokter.keyword.KeywordBuilder;
import com.edduarte.vokter.model.mongodb.Diff;
import com.edduarte.vokter.model.mongodb.Document;
import com.edduarte.vokter.model.mongodb.Keyword;
import com.edduarte.vokter.model.mongodb.Session;
import com.edduarte.vokter.parser.Parser;
import com.edduarte.vokter.parser.ParserPool;
import com.edduarte.vokter.parser.SimpleParser;
import com.edduarte.vokter.util.Constants;
import com.mongodb.BasicDBObject;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class Context implements LifeCycle.Listener, JobManagerHandler {

    private static final Logger logger = LoggerFactory.getLogger(Context.class);

    private static final String DOCUMENTS_DB = "vokter_documents_db";

    private static final String SESSIONS_DB = "vokter_sessions_db";

    private static final String SESSIONS_COLLECTION = "vokter_sessions_collection";

    private static final String DIFFERENCES_DB = "vokter_differences_db";

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
     * The MongoDB database for client url to token pairings.
     */
    private DB sessionsDB;

    /**
     * The MongoDB database for detected differences between snapshots for
     * each document.
     */
    private DB differencesDB;

    /**
     * The Bayesian detection model that allows language detection.
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
     * Flag that sets difference detection jobs to perform stopword filtering.
     */
    private boolean isStoppingEnabled = false;

    /**
     * Flag that sets difference detection jobs to perform stemming.
     */
    private boolean isStemmingEnabled = false;

    /**
     * Flag that sets difference detection jobs to of equal occurrences with different casing.
     */
    private boolean ignoreCase = true;


    private Context() throws Exception {
        super();
        initialized = false;
        jobManager = JobManager.create("vokter_job_manager", this);
        parserPool = new ParserPool();
    }


    public static Context getInstance() {
        return instance;
    }


    private static String getDiffCollectionName(String url, String contentType) {
        return url + "|" + contentType;
    }


    @Override
    public Session createOrGetSession(String clientUrl, String clientContentType) {
        DBCollection collection = documentsDB.getCollection(SESSIONS_COLLECTION);

        DBObject obj = collection.findOne(
                new BasicDBObject(Session.CLIENT_URL, clientUrl)
                        .append(Session.CLIENT_CONTENT_TYPE, clientContentType));
        if (obj == null) {
            // there is no session for this client, so create one
            String token = Constants.bytesToHex(Constants.generateRandomBytes());
            Session s = new Session(clientUrl, clientContentType, token);
            collection.insert(s);
            return s;
        } else {
            return new Session((BasicDBObject) obj);
        }
    }


    @Override
    public void removeSession(String clientUrl, String clientContentType) {
        DBCollection collection = documentsDB.getCollection(SESSIONS_COLLECTION);
        DBObject obj = collection.findOne(
                new BasicDBObject(Session.CLIENT_URL, clientUrl)
                        .append(Session.CLIENT_CONTENT_TYPE, clientContentType));
        if (obj != null) {
            Session s = new Session((BasicDBObject) obj);
            collection.remove(s);
        }
    }


    @Override
    public Session validateToken(String clientUrl, String clientContentType, String token) {
        DBCollection collection = documentsDB.getCollection(SESSIONS_COLLECTION);

        DBObject obj = collection.findOne(
                new BasicDBObject(Session.CLIENT_URL, clientUrl)
                        .append(Session.CLIENT_CONTENT_TYPE, clientContentType)
                        .append(Session.TOKEN, token));
        if (obj == null) {
            return null;
        } else {
            return new Session((BasicDBObject) obj);
        }
    }


    public Session createJob(
            String documentUrl, String documentContentType,
            String clientUrl, String clientContentType,
            List<String> keywords, List<DiffEvent> events,
            boolean filterStopwords, boolean enableStemming, boolean ignoreCase,
            int snippetOffset, int interval) {
        boolean created = jobManager.createJob(
                documentUrl, documentContentType,
                clientUrl, clientContentType,
                keywords, events,
                filterStopwords, enableStemming, ignoreCase,
                snippetOffset, interval
        );
        if (created) {
            return createOrGetSession(clientUrl, clientContentType);
        } else {
            return null;
        }
    }


    public boolean cancelJob(String documentUrl, String documentContentType,
                             String clientUrl, String clientContentType) {
        return jobManager.cancelJob(
                documentUrl, documentContentType,
                clientUrl, clientContentType
        );
    }


    /**
     * Indexes the specified document and detects differences between an older
     * snapshot and the new one. Once differences are collected, saves the resulting
     * index of all occurrences of the new snapshot for future query and comparison
     * jobs.
     */
    @Override
    public DetectResult detectDifferences(String url, String contentType) {

        // create a new document snapshot for the provided url
//        DocumentBuilder builder = DocumentBuilder
//                .fromUrl(url)
//                .withLanguageDetector(langDetector);

//        if (isStoppingEnabled) {
//            builder.withStopwords();
//        }
//        if (isStemmingEnabled) {
//            builder.withStemming();
//        }
//        if (ignoreCase) {
//            builder.ignoreCase();
//        }
        Document newDocument = DocumentBuilder
                .fromUrl(url, contentType)
                .build();
        logger.info("{}", newDocument);
        if (newDocument == null) {
            // A problem occurred during processing, mostly during the fetching phase.
            // This could happen if the page was unavailable at the time.
            return new DetectResult(false, false);
        }

        boolean hasNewDiffs = false;

        // check if there is a older document in the collection
        DocumentPair pair = collection.get(url, contentType);

        if (pair != null) {
            // there was already a document for this url on the collection, so
            // detect differences between them and add them to the differences
            // database
            Document oldDocument = pair.latest();
            logger.info("{}", oldDocument);
            DiffDetector detector = new DiffDetector(oldDocument, newDocument);
            List<Diff> results = detector.call();
            hasNewDiffs = !results.isEmpty();

            removeExistingDifferences(url, contentType);
            DBCollection diffColl = differencesDB.getCollection(
                    getDiffCollectionName(url, contentType));

            if (hasNewDiffs) {
                BulkWriteOperation bulkOp = diffColl.initializeUnorderedBulkOperation();
                results.forEach(bulkOp::insert);
                bulkOp.execute();
                bulkOp = null;
                diffColl = null;
            }
        }

        // remove the oldest document with this url and content type and add the
        // new one to the collection
        collection.add(newDocument);

        return new DetectResult(true, hasNewDiffs);
    }


    @Override
    public Set<Match> matchDifferences(
            String documentUrl, String documentContentType,
            List<Keyword> keywords,
            boolean filterStopwords, boolean enableStemming, boolean ignoreCase,
            boolean ignoreAdded, boolean ignoreRemoved,
            int snippetOffset) {
        DBCollection diffColl = differencesDB.getCollection(
                getDiffCollectionName(documentUrl, documentContentType));
        Iterable<DBObject> cursor = diffColl.find();
        List<Diff> diffs = StreamSupport.stream(cursor.spliterator(), true)
                .map(Diff::new)
                .collect(Collectors.toList());

        DocumentPair pair = collection.get(documentUrl, documentContentType);
        if (pair != null) {
            String oldText = pair.oldest().getText();
            String newText = pair.latest().getText();
            DiffMatcher matcher = new DiffMatcher(
                    oldText, newText,
                    keywords, diffs, parserPool, langDetector,
                    filterStopwords, enableStemming, ignoreCase,
                    ignoreAdded, ignoreRemoved,
                    snippetOffset
            );
            try {
                return matcher.call();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return Collections.emptySet();
    }


    /**
     * Removes existing differences for the specified url
     */
    @Override
    public void removeExistingDifferences(String documentUrl, String documentContentType) {
        DBCollection diffColl = differencesDB.getCollection(
                getDiffCollectionName(documentUrl, documentContentType));
        diffColl.drop();
    }


    /**
     * Process and build keyword objects based on this context configuration
     */
    @Override
    public Keyword buildKeyword(String keywordInput, boolean isStoppingEnabled,
                                boolean isStemmingEnabled, boolean ignoreCase) {
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
        sessionsDB = mongoClient.getDB(SESSIONS_DB);
        differencesDB = mongoClient.getDB(DIFFERENCES_DB);

        List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();
        langDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .withProfiles(languageProfiles)
                .build();

        collection = new DocumentCollection(
                "vokter_production_collection",
                documentsDB
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
