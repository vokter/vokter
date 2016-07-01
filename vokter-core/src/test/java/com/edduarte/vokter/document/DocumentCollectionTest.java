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

    private static DocumentCollection collection;

    private static LanguageDetector langDetector;


    @BeforeClass
    public static void setUp() throws IOException, InterruptedException {
        mongoClient = new MongoClient("localhost", 27017);
        documentsDB = mongoClient.getDB("test_documents_db");
        collection = new DocumentCollection("test_collection", documentsDB);
        List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();
        langDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .withProfiles(languageProfiles)
                .build();
    }


    @AfterClass
    public static void close() {
        collection.destroy();
        documentsDB.dropDatabase();
        mongoClient.close();
    }


    @Test
    public void test() {
        String url = "https://en.wikipedia.org/wiki/Argus_Panoptes";

        assertNull(collection.get(url, MediaType.TEXT_HTML));

        // testing add
        Document d = DocumentBuilder
                .fromUrl(url, null)
                .build(langDetector);
        collection.add(d);
        DocumentPair pair = collection.get(url, MediaType.TEXT_HTML);
        assertNotNull(pair);

        // testing remove
        collection.remove(url, MediaType.TEXT_HTML);
        assertNull(collection.get(url, MediaType.TEXT_HTML));
    }
}
