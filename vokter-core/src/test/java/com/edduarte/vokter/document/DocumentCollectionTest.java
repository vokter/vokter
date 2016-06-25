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

package com.edduarte.vokter.document;

import com.edduarte.vokter.model.mongodb.Document;
import com.edduarte.vokter.parser.ParserPool;
import com.edduarte.vokter.parser.SimpleParser;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class DocumentCollectionTest {

    private static final Logger logger = LoggerFactory.getLogger(DocumentCollectionTest.class);

    private static MongoClient mongoClient;

    private static DB documentsDB;

    private static DB occurrencesDB;

    private static ParserPool parserPool;

    private static DocumentCollection collection;


    @BeforeClass
    public static void setUp() throws IOException, InterruptedException {
        mongoClient = new MongoClient("localhost", 27017);
        documentsDB = mongoClient.getDB("test_documents_db");
        occurrencesDB = mongoClient.getDB("test_terms_db");
        parserPool = new ParserPool();
        parserPool.place(new SimpleParser());
        parserPool.place(new SimpleParser());
        collection = new DocumentCollection("test_collection", documentsDB, occurrencesDB);
    }


    @AfterClass
    public static void close() {
        collection.destroy();
        documentsDB.dropDatabase();
        occurrencesDB.dropDatabase();
        mongoClient.close();
    }


    @Test
    public void test() {
        String url = "https://en.wikipedia.org/wiki/Argus_Panoptes";
        
        assertNull(collection.get(url));

        // testing add
        Document d = DocumentBuilder
                .fromUrl(url)
                .ignoreCase()
                .withStopwords()
                .withStemming()
                .build(occurrencesDB, parserPool);
        collection.add(d);
        assertNotNull(collection.get(url));

        // testing remove
        collection.remove(url);
        assertNull(collection.get(url));
    }
}
