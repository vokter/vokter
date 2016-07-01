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

import com.edduarte.vokter.parser.ParserPool;
import com.edduarte.vokter.parser.SimpleParser;
import com.edduarte.vokter.persistence.DiffCollection;
import com.edduarte.vokter.persistence.DocumentCollection;
import com.edduarte.vokter.persistence.Session;
import com.edduarte.vokter.persistence.SessionCollection;
import com.edduarte.vokter.persistence.mongodb.MongoDiffCollection;
import com.edduarte.vokter.persistence.mongodb.MongoDocumentCollection;
import com.edduarte.vokter.persistence.mongodb.MongoSessionCollection;
import com.google.common.collect.Lists;
import com.mongodb.DB;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class RestNotificationHandlerTest {

    private static final Logger logger = LoggerFactory.getLogger(RestNotificationHandlerTest.class);

    private static MongoClient mongoClient;

    private static DB db;

    private static DocumentCollection documentCollection;

    private static DiffCollection diffCollection;

    private static SessionCollection sessionCollection;

    private static DB jobsDB;

    private static ParserPool parserPool;

    private static LanguageDetector langDetector;


    @BeforeClass
    public static void setUp() throws IOException, InterruptedException {
        mongoClient = new MongoClient("localhost", 27017);
        jobsDB = mongoClient.getDB("vokter_jobs");
        jobsDB.dropDatabase();
        db = mongoClient.getDB("vokter_test");
        db.dropDatabase();
        documentCollection = new MongoDocumentCollection(db);
        diffCollection = new MongoDiffCollection(db);
        sessionCollection = new MongoSessionCollection(db);
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
        documentCollection.destroy();
        jobsDB.dropDatabase();
        db.dropDatabase();
        parserPool.clear();
        mongoClient.close();
    }


    @Test
    public void testSimple() throws Exception {
        AtomicReference<String> newestTestDocument = new AtomicReference<>(
                "Argus Panoptes is the name of the 100-eyed giant in Greek mythology.");
        JobManager manager = JobManager.create(
                "test_vokter_manager",
                documentCollection,
                diffCollection,
                sessionCollection,
                parserPool,
                langDetector,
                false,
                false,
                new RestNotificationHandler()
        );
        manager.initialize();

        Session createdSession = manager.createJob(
                "http://example.com",
                MediaType.TEXT_PLAIN,
                "https://www.google.com",
                MediaType.APPLICATION_JSON,
                Lists.newArrayList("the greek", "argus panoptes"),
                7
        );
        assertNotNull(createdSession);
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

        createdSession = manager.createJob(
                "http://example.com",
                MediaType.TEXT_PLAIN,
                "https://www.google.com",
                MediaType.APPLICATION_JSON,
                Lists.newArrayList("argus"),
                12
        );
        assertNull(createdSession);
        createdSession = manager.createJob(
                "http://example.com",
                MediaType.TEXT_PLAIN,
                "https://www.google.pt",
                MediaType.APPLICATION_JSON,
                Lists.newArrayList("greek"),
                19
        );
        assertNotNull(createdSession);
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

        createdSession = manager.createJob(
                "http://example.com",
                MediaType.TEXT_PLAIN,
                "https://www.google.com",
                MediaType.APPLICATION_JSON,
                Lists.newArrayList("the greek", "argus panoptes"),
                5
        );
        assertNotNull(createdSession);
        // wait 15 seconds to ensure that the new existing jobs is performed and
        // finished
        // the result should be no differences detected
        Thread.sleep(15000);


        manager.stop();
    }

}

