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

package com.edduarte.vokter.diff;

import com.edduarte.vokter.document.Document;
import com.edduarte.vokter.document.DocumentBuilder;
import com.edduarte.vokter.parser.ParserPool;
import com.edduarte.vokter.parser.SimpleParser;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class DifferenceDetectorTest {

    private static final Logger logger = LoggerFactory.getLogger(DifferenceDetectorTest.class);

    private static MongoClient mongoClient;

    private static DB occurrencesDB;

    private static ParserPool parserPool;


    @BeforeClass
    public static void setUp() throws IOException, InterruptedException {
        mongoClient = new MongoClient("localhost", 27017);
        occurrencesDB = mongoClient.getDB("test_terms_db");
        parserPool = new ParserPool();
        parserPool.place(new SimpleParser());
    }


    @AfterClass
    public static void close() {
        occurrencesDB.dropDatabase();
        mongoClient.close();
    }


    @Test
    public void testSimple() {
        String url = "http://www.bbc.com/news/uk/";
        String type = "text/html";
        String oldSnapshot = "is the of the 100-eyed giant in Greek mythology.";
        String newSnapshot = "Vokter Panoptes is the name of the 100-eyed giant in Norse mythology.";

        Document oldSnapshotDoc = DocumentBuilder
                .fromString(url, oldSnapshot, type)
                .ignoreCase()
                .withStopwords()
                .withStemming()
                .build(occurrencesDB, parserPool);

        Document newSnapshotDoc = DocumentBuilder
                .fromString(url, newSnapshot, type)
                .ignoreCase()
                .withStopwords()
                .withStemming()
                .build(occurrencesDB, parserPool);

        DifferenceDetector comparison = new DifferenceDetector(
                oldSnapshotDoc,
                newSnapshotDoc,
                parserPool
        );
        List<Difference> diffList = comparison.call();
        assertEquals(5, diffList.size());
    }


    @Test
    public void testBBCNews() throws IOException {
        String url = "http://www.bbc.com/news/uk/";
        String type = "text/html";
        InputStream oldStream = getClass().getResourceAsStream("bbc_news_8_12_2014_11_00.html");
        InputStream newStream = getClass().getResourceAsStream("bbc_news_8_12_2014_13_00.html");
        String oldSnapshot = IOUtils.toString(oldStream);
        String newSnapshot = IOUtils.toString(newStream);

        Document oldSnapshotDoc = DocumentBuilder
                .fromString(url, oldSnapshot, type)
                .ignoreCase()
                .withStopwords()
                .withStemming()
                .build(occurrencesDB, parserPool);

        Document newSnapshotDoc = DocumentBuilder
                .fromString(url, newSnapshot, type)
                .ignoreCase()
                .withStopwords()
                .withStemming()
                .build(occurrencesDB, parserPool);

        DifferenceDetector comparison = new DifferenceDetector(
                oldSnapshotDoc,
                newSnapshotDoc,
                parserPool
        );
        List<Difference> diffList = comparison.call();
        assertEquals(352, diffList.size());
    }
}

