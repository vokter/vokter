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

package com.edduarte.vokter.job;

import com.edduarte.vokter.diff.DiffDetector;
import com.edduarte.vokter.diff.DiffMatcher;
import com.edduarte.vokter.diff.Match;
import com.edduarte.vokter.document.DocumentBuilder;
import com.edduarte.vokter.keyword.Keyword;
import com.edduarte.vokter.keyword.KeywordBuilder;
import com.edduarte.vokter.parser.ParserPool;
import com.edduarte.vokter.parser.SimpleParser;
import com.edduarte.vokter.persistence.Diff;
import com.edduarte.vokter.persistence.Document;
import com.edduarte.vokter.persistence.DocumentCollection;
import com.edduarte.vokter.persistence.Session;
import com.edduarte.vokter.persistence.mongodb.MongoDiff;
import com.edduarte.vokter.persistence.mongodb.MongoDocument;
import com.edduarte.vokter.persistence.mongodb.MongoDocumentCollection;
import com.edduarte.vokter.persistence.mongodb.MongoSession;
import com.google.common.collect.Lists;
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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class JobManagerTest {

    private static final Logger logger = LoggerFactory.getLogger(JobManagerTest.class);

    private static MongoClient mongoClient;

    private static DB documentsDB;

    private static DocumentCollection collection;

    private static DB differencesDB;

    private static DB jobsDB;

    private static ParserPool parserPool;

    private static LanguageDetector langDetector;

    private AtomicReference<String> newestTestDocument;


    @BeforeClass
    public static void setUp() throws IOException, InterruptedException {
        mongoClient = new MongoClient("localhost", 27017);
        jobsDB = mongoClient.getDB("vokter_jobs");
        jobsDB.dropDatabase();
        documentsDB = mongoClient.getDB("test_documents_db");
        documentsDB.dropDatabase();
        collection = new MongoDocumentCollection(
                "test_vokter_collection",
                documentsDB
        );
        differencesDB = mongoClient.getDB("test_differences_db");
        differencesDB.dropDatabase();
        parserPool = new ParserPool();
        parserPool.place(new SimpleParser());
        parserPool.place(new SimpleParser());
        parserPool.place(new SimpleParser());
        parserPool.place(new SimpleParser());
        parserPool.place(new SimpleParser());
        List<LanguageProfile> languageProfiles =
                new LanguageProfileReader().readAllBuiltIn();
        langDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .withProfiles(languageProfiles)
                .build();
    }


    @AfterClass
    public static void close() {
        collection.destroy();
        jobsDB.dropDatabase();
        documentsDB.dropDatabase();
        differencesDB.dropDatabase();
        parserPool.clear();
        mongoClient.close();
    }


    private static String getDiffCollectionName(String url, String contentType) {
        return url + "|" + contentType;
    }


    @Test
    public void testSimple() throws Exception {
        newestTestDocument = new AtomicReference<>("Argus Panoptes is " +
                "the name of the 100-eyed giant in Greek mythology.");
        JobManager manager = JobManager.create("test_vokter_manager", new JobManagerHandler() {

            @Override
            public DetectResult detectDifferences(String url, String contentType) {

                // check if there is a older document in the collection
                DocumentCollection.Pair pair = collection.get(url, contentType);

                if (pair != null) {
                    Document oldDocument = pair.latest();
                    Document newDocument = DocumentBuilder
                            .fromString(url, newestTestDocument.get(),
                                    MediaType.TEXT_PLAIN)
                            .withStopwords()
                            .withShingleLength(oldDocument.getShingleLength())
                            .build(langDetector, MongoDocument.class);
                    if (newDocument == null) {
                        // A problem occurred during processing, mostly during
                        // the fetching phase.
                        // This could happen if the page was unavailable at the
                        // time.
                        return new DetectResult(false, false);
                    }

                    DiffDetector detector = new DiffDetector(
                            oldDocument,
                            newDocument,
                            MongoDiff.class
                    );
                    List<Diff> results = detector.call();
                    boolean hasNewDiffs = !results.isEmpty();

                    removeExistingDifferences(url, contentType);
                    if (hasNewDiffs) {
                        DBCollection diffColl = differencesDB.getCollection(
                                getDiffCollectionName(url, contentType));
                        BulkWriteOperation bulkOp =
                                diffColl.initializeUnorderedBulkOperation();
                        results.parallelStream()
                                .map(d -> (MongoDiff) d)
                                .forEach(bulkOp::insert);
                        bulkOp.execute();
                        bulkOp = null;
                        diffColl = null;
                    }

                    // remove the oldest document with this url and content type
                    // and add the new one to the collection
                    collection.add(newDocument);

                    return new DetectResult(true, hasNewDiffs);

                } else {
                    // this is a new document, so process it and add to the
                    // collection
                    Document newDocument = DocumentBuilder
                            .fromString(url, newestTestDocument.get(),
                                    MediaType.TEXT_PLAIN)
                            .withStopwords()
                            .build(langDetector, MongoDocument.class);
                    collection.add(newDocument);

                    return new DetectResult(true, false);
                }
            }


            @Override
            public Set<Match> matchDifferences(
                    String documentUrl, String documentContentType,
                    List<Keyword> keywords,
                    boolean filterStopwords, boolean enableStemming, boolean ignoreCase,
                    boolean ignoreAdded, boolean ignoreRemoved,
                    int snippetOffset) {

                // check diffs stored on the database
                DBCollection diffColl = differencesDB.getCollection(
                        getDiffCollectionName(documentUrl, documentContentType));
                long count = diffColl.count();
                if (count <= 0) {
                    return Collections.emptySet();
                }

                Iterable<DBObject> cursor = diffColl.find();
                List<Diff> diffs = StreamSupport.stream(cursor.spliterator(), true)
                        .map(MongoDiff::new)
                        .collect(Collectors.toList());

                DocumentCollection.Pair pair = collection.get(documentUrl, documentContentType);
                if (pair == null) {
                    return Collections.emptySet();
                }

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

                return Collections.emptySet();
            }


            @Override
            public void removeExistingDifferences(String documentUrl, String documentContentType) {
                DBCollection diffColl = differencesDB.getCollection(
                        getDiffCollectionName(documentUrl, documentContentType));
                diffColl.drop();
            }


            @Override
            public Keyword buildKeyword(String keywordInput, boolean isStoppingEnabled, boolean isStemmingEnabled, boolean ignoreCase) {
                return KeywordBuilder.fromText(keywordInput)
                        .withStopwords()
                        .withStemming()
                        .ignoreCase()
                        .build(parserPool);
            }


            @Override
            public boolean sendNotificationToClient(String documentUrl, String documentContentType, String clientUrl, String clientContentType, Set<Match> diffs) {
                // do nothing
                return true;
            }


            @Override
            public boolean sendTimeoutToClient(String documentUrl, String documentContentType, String clientUrl, String clientContentType) {
                // do nothing
                return true;
            }


            @Override
            public void removeSession(String clientUrl, String clientContentType) {
            }


            @Override
            public Session validateToken(String clientUrl, String clientContentType, String token) {
                return new MongoSession(clientUrl, clientContentType, token);
            }
        });
        manager.initialize();

        boolean wasCreated = manager.createJob(
                "http://example.com",
                MediaType.TEXT_PLAIN,
                "https://www.google.com",
                MediaType.APPLICATION_JSON,
                Lists.newArrayList("the greek", "argus panoptes"),
                7
        );
        assertTrue(wasCreated);
        // jobs run every 5 seconds, so force the test to wait 15 seconds to
        // ensure that a detection job is performed and finished
        // the result should be no differences detected
        Thread.sleep(15000);


        newestTestDocument.set("is the of the 100-eyed giant in Greek mythology.");
        // this time, the result should be differences detected
        Thread.sleep(12000); // TODO: when this is set to 9 seconds, adding a
        // new job for the same document (as done below) prompts the same
        // detection twice without the diffs from the first trigger being
        // stored, which leads to matching for the same job happening twice,
        // which leads to notifying the client twice

        wasCreated = manager.createJob(
                "http://example.com",
                MediaType.TEXT_PLAIN,
                "https://www.google.com",
                MediaType.APPLICATION_JSON,
                Lists.newArrayList("argus"),
                12
        );
        assertFalse(wasCreated);
        wasCreated = manager.createJob(
                "http://example.com",
                MediaType.TEXT_PLAIN,
                "https://www.google.pt",
                MediaType.APPLICATION_JSON,
                Lists.newArrayList("greek"),
                19
        );
        assertTrue(wasCreated);
        // wait 20 seconds to ensure that the second job (every 19 seconds)
        // doesn't find any differences, and then wait another 20 seconds.
        // The first job, which runs every 5 seconds, should detect the
        // difference first, and matching for both the first request and the
        // second request should occur
        Thread.sleep(20000);
        newestTestDocument.set("is the of the 100-eyed giant in Norse mythology.");
        Thread.sleep(20000);


        manager.cancelJob(
                "http://example.com",
                MediaType.TEXT_PLAIN,
                "https://www.google.com",
                MediaType.APPLICATION_JSON
        );
        manager.cancelJob(
                "http://example.com",
                MediaType.TEXT_PLAIN,
                "https://www.google.pt",
                MediaType.APPLICATION_JSON
        );
        // wait 5 seconds to ensure that the 2 existing jobs were canceled
        Thread.sleep(5000);

        wasCreated = manager.createJob(
                "http://example.com",
                MediaType.TEXT_PLAIN,
                "https://www.google.com",
                MediaType.APPLICATION_JSON,
                Lists.newArrayList("the greek", "argus panoptes"),
                5
        );
        assertTrue(wasCreated);
        // wait 15 seconds to ensure that the new existing jobs is performed and
        // finished
        // the result should be no differences detected
        Thread.sleep(15000);


        manager.stop();
    }

}

