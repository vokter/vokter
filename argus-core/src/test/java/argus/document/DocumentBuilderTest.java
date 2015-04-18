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
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
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
