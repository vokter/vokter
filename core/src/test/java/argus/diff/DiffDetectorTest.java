package argus.diff;

import argus.document.Document;
import argus.document.DocumentBuilder;
import argus.job.workers.MatchWorker;
import argus.keyword.Keyword;
import argus.keyword.KeywordBuilder;
import argus.keyword.KeywordSerializer;
import argus.parser.GeniaParser;
import argus.parser.ParserPool;
import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.apache.tools.ant.filters.StringInputStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * TODO
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class DiffDetectorTest {

    private static final Logger logger = LoggerFactory.getLogger(DiffDetectorTest.class);

    private static MongoClient mongoClient;
    private static DB occurrencesDB;
    private static ParserPool parserPool;


    @BeforeClass
    public static void setUp() throws IOException, InterruptedException {
        mongoClient = new MongoClient("localhost", 27017);
        occurrencesDB = mongoClient.getDB("terms_db");
        parserPool = new ParserPool();
        parserPool.place(new GeniaParser());
    }

    @Test
    public void testSimple() {
        String url = "http://www.bbc.com/news/uk/";
        String type = "text/html";
        InputStream oldSnapshot = new StringInputStream("is the of the 100-eyed giant in Greek mythology.");
        InputStream newSnapshot = new StringInputStream("Argus Panoptes is the name of the 100-eyed giant in Norse mythology.");

        Document oldSnapshotDoc = DocumentBuilder
                .fromStream(url, oldSnapshot, type)
                .ignoreCase()
                .withStopwords()
                .withStemming()
                .build(occurrencesDB, parserPool);

        Document newSnapshotDoc = DocumentBuilder
                .fromStream(url, newSnapshot, type)
                .ignoreCase()
                .withStopwords()
                .withStemming()
                .build(occurrencesDB, parserPool);

        DiffDetector comparison = new DiffDetector(
                oldSnapshotDoc,
                newSnapshotDoc,
                parserPool
        );
        List<DiffDetector.Result> diffList = comparison.call();
        assertEquals(5, diffList.size());
    }

    @Test
    public void testBBCNews() {
        String url = "http://www.bbc.com/news/uk/";
        String type = "text/html";
        InputStream oldSnapshot = getClass().getResourceAsStream("bbc_news_8_12_2014_11_00.html");
        InputStream newSnapshot = getClass().getResourceAsStream("bbc_news_8_12_2014_13_00.html");

        Document oldSnapshotDoc = DocumentBuilder
                .fromStream(url, oldSnapshot, type)
                .ignoreCase()
                .withStopwords()
                .withStemming()
                .build(occurrencesDB, parserPool);

        Document newSnapshotDoc = DocumentBuilder
                .fromStream(url, newSnapshot, type)
                .ignoreCase()
                .withStopwords()
                .withStemming()
                .build(occurrencesDB, parserPool);

        DiffDetector comparison = new DiffDetector(
                oldSnapshotDoc,
                newSnapshotDoc,
                parserPool
        );
        List<DiffDetector.Result> diffList = comparison.call();
        assertEquals(352, diffList.size());
    }

    @AfterClass
    public static void close() {
        occurrencesDB.dropDatabase();
        mongoClient.close();
    }
}

