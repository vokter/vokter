package argus.diff;

import argus.document.Document;
import argus.document.DocumentBuilder;
import argus.job.Job;
import argus.keyword.Keyword;
import argus.keyword.KeywordBuilder;
import argus.keyword.KeywordSerializer;
import argus.parser.GeniaParser;
import argus.parser.ParserPool;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gson.GsonBuilder;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.apache.tools.ant.filters.StringInputStream;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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
    private static DB termsDatabase;
    private static ParserPool parserPool;


    @BeforeClass
    public static void setUp() throws IOException, InterruptedException {
        mongoClient = new MongoClient("localhost", 27017);
        termsDatabase = mongoClient.getDB("terms_db");
        parserPool = new ParserPool();
        parserPool.place(new GeniaParser());
    }

    @Test
    public void testSimple() {
        String url = "http://www.bbc.com/news/uk/";
        String type = "text/html";
        InputStream oldSnapshot = new StringInputStream("is the of the 100-eyed giant in Greek mythology.");
        InputStream newSnapshot = new StringInputStream("Argus Panoptes is the name of the 100-eyed giant in Norse mythology.");
        List<String> words = Lists.newArrayList(
                "the greek",
                "argus panoptes"
        );
        List<Keyword> keywords = words
                .stream()
                .map(string -> KeywordBuilder.fromText(string)
                        .ignoreCase()
                        .withStopwords()
                        .withStemming()
                        .build(parserPool))
                .collect(Collectors.toList());

        Job job = new Job(url, keywords, 10, url);

        Document oldSnapshotDoc = DocumentBuilder
                .fromStream(url, oldSnapshot, type)
                .ignoreCase()
                .withStopwords()
                .withStemming()
                .build(termsDatabase, parserPool);

        Document newSnapshotDoc = DocumentBuilder
                .fromStream(url, newSnapshot, type)
                .ignoreCase()
                .withStopwords()
                .withStemming()
                .build(termsDatabase, parserPool);

        DifferenceDetector comparison = new DifferenceDetector(
                termsDatabase,
                oldSnapshotDoc,
                newSnapshotDoc,
                Lists.newArrayList(job)
        );
        Multimap<Job, Difference> diffList = comparison.call();
        assertEquals(2, diffList.size());

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Keyword.class, new KeywordSerializer());
        String diffJson = gsonBuilder.create().toJson(diffList.values());
    }


    @Test
    public void testBBCNews() {
        String url = "http://www.bbc.com/news/uk/";
        String type = "text/html";
        InputStream oldSnapshot = getClass().getResourceAsStream("bbc_news_8_12_2014_11_00.html");
        InputStream newSnapshot = getClass().getResourceAsStream("bbc_news_8_12_2014_13_00.html");
        List<String> words = Lists.newArrayList(
                "last updated",
                "House of Commons",
                "Shrien Dewani"
        );
        List<Keyword> keywords = words
                .stream()
                .map(string -> KeywordBuilder.fromText(string)
                        .ignoreCase()
                        .withStopwords()
                        .withStemming()
                        .build(parserPool))
                .collect(Collectors.toList());

        Job job = new Job(url, keywords, 10, url);

        Document oldSnapshotDoc = DocumentBuilder
                .fromStream(url, oldSnapshot, type)
                .ignoreCase()
                .withStopwords()
                .withStemming()
                .build(termsDatabase, parserPool);

        Document newSnapshotDoc = DocumentBuilder
                .fromStream(url, newSnapshot, type)
                .ignoreCase()
                .withStopwords()
                .withStemming()
                .build(termsDatabase, parserPool);


        DifferenceDetector comparison = new DifferenceDetector(
                termsDatabase,
                oldSnapshotDoc,
                newSnapshotDoc,
                Lists.newArrayList(job)
        );
        Multimap<Job, Difference> diffList = comparison.call();
        assertEquals(7, diffList.size());
    }


    @AfterClass
    public static void close() {
        termsDatabase.dropDatabase();
        mongoClient.close();
    }
}

