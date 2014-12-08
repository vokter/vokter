package argus.diff;

import argus.document.Document;
import argus.document.DocumentBuilder;
import argus.parser.GeniaParser;
import argus.parser.ParserPool;
import argus.query.QueryBuilder;
import com.google.common.collect.Lists;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
    public void test() {
        String url = "http://www.bbc.com/news/uk/";
        String type = "text/html";
        InputStream oldSnapshot = getClass().getResourceAsStream("bbc_news_8_12_2014_11_00.html");
        InputStream newSnapshot = getClass().getResourceAsStream("bbc_news_8_12_2014_11_45.html");
        List<String> keywords = Lists.newArrayList(
                "Last updated",
                "House of Commons",
                "Shrien Dewani"
        );

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

        keywords.stream()
                .map(k -> QueryBuilder.fromText(k)
                        .ignoreCase()
                        .withStopwords()
                        .withStemming()
                        .build(parserPool))
                .forEach(query -> {
                    DifferenceDetector comparison = new DifferenceDetector(
                            termsDatabase,
                            oldSnapshotDoc,
                            newSnapshotDoc,
                            query
                    );
                    List<Discrepancy> discrepancyList = comparison.call();
//                    logger.info(discrepancyList.toString());
                });

    }


    @AfterClass
    public static void close() {
        termsDatabase.dropDatabase();
        mongoClient.close();
    }
}

