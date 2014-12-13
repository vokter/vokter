package argus.diff;

import argus.document.Document;
import argus.document.DocumentBuilder;
import argus.parser.GeniaParser;
import argus.parser.ParserPool;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.filters.StringInputStream;
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
 * TODO
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
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
        parserPool.place(new GeniaParser());
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
        String newSnapshot = "Argus Panoptes is the name of the 100-eyed giant in Norse mythology.";

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

