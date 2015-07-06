/*
 * Copyright 2014 Ed Duarte
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

package argus.document;

import argus.parser.ParserPool;
import argus.parser.SimpleParser;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Ed Duarte (<a href="mailto:edmiguelduarte@gmail.com">edmiguelduarte@gmail.com</a>)
 * @version 2.0.0
 * @since 1.0.0
 */
public class DocumentBuilderTest {

    private static final Logger logger = LoggerFactory.getLogger(DocumentBuilderTest.class);

    private static MongoClient mongoClient;

    private static ParserPool parserPool;


    @BeforeClass
    public static void setUp() throws IOException, InterruptedException {
        mongoClient = new MongoClient("localhost", 27017);
        parserPool = new ParserPool();
        parserPool.place(new SimpleParser());
    }


    // without stopwords and without stemming


    @AfterClass
    public static void close() {
        parserPool.clear();
        mongoClient.close();
    }


    @Test
    public void testHTMLNoStopNoStem() {
        DB occurrencesDB = mongoClient.getDB("test_terms_db");

        DocumentBuilder
                .fromUrl("http://en.wikipedia.org/wiki/Argus_Panoptes")
                .ignoreCase()
                .build(occurrencesDB, parserPool);
        occurrencesDB.dropDatabase();
    }


    @Test
    public void testXMLNoStopNoStem() {
        DB occurrencesDB = mongoClient.getDB("test_terms_db");

        DocumentBuilder
                .fromUrl("http://en.wikipedia.org/wiki/Special:Export/Argus_Panoptes")
                .ignoreCase()
                .build(occurrencesDB, parserPool);
        occurrencesDB.dropDatabase();
    }


    // with stopwords and without stemming


    @Test
    public void testJSONNoStopNoStem() {
        DB occurrencesDB = mongoClient.getDB("test_terms_db");

        DocumentBuilder
                .fromUrl("http://en.wikipedia.org/w/api.php?format=json&action=query&titles=Argus_Panoptes&prop=revisions&rvprop=content")
                .ignoreCase()
                .build(occurrencesDB, parserPool);
        occurrencesDB.dropDatabase();
    }


    @Test
    public void testHTMLStopNoStem() {
        DB occurrencesDB = mongoClient.getDB("test_terms_db");

        DocumentBuilder
                .fromUrl("http://en.wikipedia.org/wiki/Argus_Panoptes")
                .ignoreCase()
                .withStopwords()
                .build(occurrencesDB, parserPool);
        occurrencesDB.dropDatabase();
    }


    @Test
    public void testXMLStopNoStem() {
        DB occurrencesDB = mongoClient.getDB("test_terms_db");

        DocumentBuilder
                .fromUrl("http://en.wikipedia.org/wiki/Special:Export/Argus_Panoptes")
                .ignoreCase()
                .withStopwords()
                .build(occurrencesDB, parserPool);
        occurrencesDB.dropDatabase();
    }


    // with stopwords and with stemming


    @Test
    public void testJSONStopNoStem() {
        DB occurrencesDB = mongoClient.getDB("test_terms_db");

        DocumentBuilder
                .fromUrl("http://en.wikipedia.org/w/api.php?format=json&action=query&titles=Argus_Panoptes&prop=revisions&rvprop=content")
                .ignoreCase()
                .withStopwords()
                .build(occurrencesDB, parserPool);
        occurrencesDB.dropDatabase();
    }


    @Test
    public void testHTMLStopStem() {
        DB occurrencesDB = mongoClient.getDB("test_terms_db");

        DocumentBuilder
                .fromUrl("http://en.wikipedia.org/wiki/Argus_Panoptes")
                .ignoreCase()
                .withStopwords()
                .withStemming()
                .build(occurrencesDB, parserPool);
        occurrencesDB.dropDatabase();
    }


    @Test
    public void testXMLStopStem() {
        DB occurrencesDB = mongoClient.getDB("test_terms_db");

        DocumentBuilder
                .fromUrl("http://en.wikipedia.org/wiki/Special:Export/Argus_Panoptes")
                .ignoreCase()
                .withStopwords()
                .withStemming()
                .build(occurrencesDB, parserPool);
        occurrencesDB.dropDatabase();
    }


    @Test
    public void testJSONStopStem() {
        DB occurrencesDB = mongoClient.getDB("test_terms_db");

        DocumentBuilder
                .fromUrl("http://en.wikipedia.org/w/api.php?format=json&action=query&titles=Argus_Panoptes&prop=revisions&rvprop=content")
                .ignoreCase()
                .withStopwords()
                .withStemming()
                .build(occurrencesDB, parserPool);
        occurrencesDB.dropDatabase();
    }
}
