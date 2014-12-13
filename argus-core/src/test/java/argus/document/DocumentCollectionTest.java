package argus.document;

import argus.parser.GeniaParser;
import argus.parser.ParserPool;
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
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
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
        parserPool.place(new GeniaParser());
        parserPool.place(new GeniaParser());
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
        assertNull(collection.get("http://en.wikipedia.org/wiki/Argus_Panoptes"));

        // testing add
        Document d = DocumentBuilder
                .fromUrl("http://en.wikipedia.org/wiki/Argus_Panoptes")
                .ignoreCase()
                .withStopwords()
                .withStemming()
                .build(occurrencesDB, parserPool);
        collection.add(d);
        assertNotNull(collection.get("http://en.wikipedia.org/wiki/Argus_Panoptes"));

        // testing remove
        collection.remove("http://en.wikipedia.org/wiki/Argus_Panoptes");
        assertNull(collection.get("http://en.wikipedia.org/wiki/Argus_Panoptes"));
    }
}
