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

package com.edduarte.vokter.job;

import com.edduarte.vokter.diff.Difference;
import com.edduarte.vokter.diff.DifferenceDetector;
import com.edduarte.vokter.document.Document;
import com.edduarte.vokter.document.DocumentBuilder;
import com.edduarte.vokter.document.DocumentCollection;
import com.edduarte.vokter.keyword.Keyword;
import com.edduarte.vokter.keyword.KeywordBuilder;
import com.edduarte.vokter.parser.ParserPool;
import com.edduarte.vokter.parser.SimpleParser;
import com.edduarte.vokter.rest.SubscribeRequest;
import com.google.common.collect.Lists;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
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

    private static DB occurrencesDB;

    private static DB differencesDB;

    private static DB jobsDB;

    private static ParserPool parserPool;

    private static DocumentCollection collection;

    private AtomicReference<String> testDocuments;


    @BeforeClass
    public static void setUp() throws IOException, InterruptedException {
        mongoClient = new MongoClient("localhost", 27017);
        jobsDB = mongoClient.getDB("vokter_jobs");
        jobsDB.dropDatabase();
        documentsDB = mongoClient.getDB("test_documents_db");
        documentsDB.dropDatabase();
        occurrencesDB = mongoClient.getDB("test_terms_db");
        occurrencesDB.dropDatabase();
        differencesDB = mongoClient.getDB("test_differences_db");
        differencesDB.dropDatabase();
        jobsDB = mongoClient.getDB("test_jobs_db");
        jobsDB.dropDatabase();
        parserPool = new ParserPool();
        parserPool.place(new SimpleParser());
        collection = new DocumentCollection(
                "test_vokter_collection",
                documentsDB,
                occurrencesDB
        );
    }


    @AfterClass
    public static void close() {
        collection.destroy();
        jobsDB.dropDatabase();
        documentsDB.dropDatabase();
        occurrencesDB.dropDatabase();
        differencesDB.dropDatabase();
        parserPool.clear();
        mongoClient.close();
    }


    @Test
    public void testSimple() throws Exception {
        JobManager manager = JobManager.create("test_vokter_manager", 12, new JobManagerHandler() {
            @Override
            public boolean detectDifferences(String url) {

                // create a new document snapshot for the provided url
                Document newDocument = DocumentBuilder
                        .fromString(url, testDocuments.get(), "text/html")
                        .withStopwords()
                        .withStemming()
                        .ignoreCase()
                        .build(occurrencesDB, parserPool);
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
                    if (!results.isEmpty()) {
                        DBCollection diffColl = differencesDB.getCollection(url);
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


            @Override
            public List<Difference> getExistingDifferences(String url) {
                DBCollection diffColl = differencesDB.getCollection(url);
                diffColl.count();
                Iterable<DBObject> cursor = diffColl.find();
                return StreamSupport.stream(cursor.spliterator(), true)
                        .map(Difference::new)
                        .collect(Collectors.toList());
            }


            @Override
            public void removeExistingDifferences(String url) {
                DBCollection diffColl = differencesDB.getCollection(url);
                diffColl.drop();
            }


            @Override
            public Keyword buildKeyword(String keywordInput) {
                return KeywordBuilder.fromText(keywordInput)
                        .withStopwords()
                        .withStemming()
                        .ignoreCase()
                        .build(parserPool);
            }
        });
        testDocuments = new AtomicReference<>("Argus Panoptes is the name of the 100-eyed giant in Norse mythology.");
        manager.initialize();


        boolean wasCreated = manager.createJob(new SubscribeRequest(
                "testRequestUrl",
                "https://www.google.com",
                Lists.newArrayList("the greek", "argus panoptes"),
                10,
                false,
                false
        ));
        assertTrue(wasCreated);
        Thread.sleep(20000);


        testDocuments.lazySet("is the of the 100-eyed giant in Greek mythology.");
        Thread.sleep(20000);


        wasCreated = manager.createJob(new SubscribeRequest(
                "testRequestUrl",
                "https://www.google.com",
                Lists.newArrayList("argus"),
                15,
                false,
                false
        ));
        assertFalse(wasCreated);
        wasCreated = manager.createJob(new SubscribeRequest(
                "testRequestUrl",
                "https://www.google.pt",
                Lists.newArrayList("argus"),
                15,
                false,
                false
        ));
        assertTrue(wasCreated);
        Thread.sleep(30000);


        manager.cancelMatchingJob("testRequestUrl", "https://www.google.com");
        manager.cancelMatchingJob("testRequestUrl", "https://www.google.pt");
        Thread.sleep(30000);


        wasCreated = manager.createJob(new SubscribeRequest(
                "testRequestUrl",
                "https://www.google.com",
                Lists.newArrayList("the greek", "argus panoptes"),
                10,
                false,
                false
        ));
        assertTrue(wasCreated);
        Thread.sleep(30000);


        manager.stop();
    }

}

