package argus.job;

import argus.diff.Difference;
import argus.diff.DifferenceDetector;
import argus.document.Document;
import argus.document.DocumentBuilder;
import argus.document.DocumentCollection;
import argus.keyword.Keyword;
import argus.keyword.KeywordBuilder;
import argus.parser.ParserPool;
import argus.parser.SimpleParser;
import argus.rest.WatchRequest;
import com.google.common.collect.Lists;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class JobManagerTest {

    private static final Logger logger = LoggerFactory.getLogger(JobManagerTest.class);

    private static MongoClient mongoClient;
    private static DB documentsDB;
    private static DB occurrencesDB;
    private static DB differencesDB;
    private static DB jobsDB;
    private static ParserPool parserPool;
    private static DocumentCollection collection;

    private AtomicReference<String> testDocuments;


    @BeforeClass
    public static void setUp() throws IOException, InterruptedException {
        mongoClient = new MongoClient("localhost", 27017);
        jobsDB = mongoClient.getDB("argus_jobs");
        jobsDB.dropDatabase();
        documentsDB = mongoClient.getDB("test_documents_db");
        occurrencesDB = mongoClient.getDB("test_terms_db");
        differencesDB = mongoClient.getDB("text_differences_db");
        jobsDB = mongoClient.getDB("test_jobs_db");
        parserPool = new ParserPool();
        parserPool.place(new SimpleParser());
        collection = new DocumentCollection(
                "test_argus_collection",
                documentsDB,
                occurrencesDB
        );
    }

    @AfterClass
    public static void close() {
        collection.destroy();
        jobsDB.dropDatabase();
        documentsDB.dropDatabase();
        occurrencesDB.dropDatabase();
        differencesDB.dropDatabase();
        parserPool.clear();
        mongoClient.close();
    }

    @Test
    public void testSimple() throws Exception {
        JobManager manager = JobManager.create("test_argus_manager", 12, new JobManagerHandler() {
            @Override
            public boolean detectDifferences(String url) {

                // create a new document snapshot for the provided url
                Document newDocument = DocumentBuilder
                        .fromString(url, testDocuments.get(), "text/html")
                        .withStopwords()
                        .withStemming()
                        .ignoreCase()
                        .build(occurrencesDB, parserPool);
                if (newDocument == null) {
                    // A problem occurred during processing, mostly during the fetching phase.
                    // This could happen if the page was unavailable at the time.
                    return false;
                }

                // check if there is a older document in the collection
                Document oldDocument = collection.get(url);

                if (oldDocument != null) {
                    // there was already a document for this url on the collection, so
                    // detect differences between them and add them to the differences
                    // database
                    DifferenceDetector detector = new DifferenceDetector(oldDocument, newDocument, parserPool);
                    List<Difference> results = detector.call();

                    removeExistingDifferences(url);
                    if (!results.isEmpty()) {
                        DBCollection diffColl = differencesDB.getCollection(url);
                        BulkWriteOperation bulkOp = diffColl.initializeUnorderedBulkOperation();
                        results.forEach(bulkOp::insert);
                        bulkOp.execute();
                        bulkOp = null;
                        diffColl = null;
                    }
                }

                //replace the old document in the collection with the new one
                collection.remove(url);
                collection.add(newDocument);

                return true;
            }

            @Override
            public List<Difference> getExistingDifferences(String url) {
                DBCollection diffColl = differencesDB.getCollection(url);
                diffColl.count();
                Iterable<DBObject> cursor = diffColl.find();
                return StreamSupport.stream(cursor.spliterator(), true)
                        .map(Difference::new)
                        .collect(Collectors.toList());
            }

            @Override
            public void removeExistingDifferences(String url) {
                DBCollection diffColl = differencesDB.getCollection(url);
                diffColl.drop();
            }

            @Override
            public Keyword buildKeyword(String keywordInput) {
                return KeywordBuilder.fromText(keywordInput)
                        .withStopwords()
                        .withStemming()
                        .ignoreCase()
                        .build(parserPool);
            }
        });
        testDocuments = new AtomicReference<>("Argus Panoptes is the name of the 100-eyed giant in Norse mythology.");
        manager.initialize();


        boolean wasCreated = manager.createJob(new WatchRequest(
                "testRequestUrl",
                "http://www.google.com",
                Lists.newArrayList("the greek", "argus panoptes"),
                10
        ));
        assertTrue(wasCreated);
        Thread.sleep(20000);


        testDocuments.lazySet("is the of the 100-eyed giant in Greek mythology.");
        System.out.println("document changed");
        Thread.sleep(20000);


        wasCreated = manager.createJob(new WatchRequest(
                "testRequestUrl",
                "http://www.google.com",
                Lists.newArrayList("argus"),
                15));
        assertFalse(wasCreated);
        wasCreated = manager.createJob(new WatchRequest(
                "testRequestUrl",
                "http://www.google.pt",
                Lists.newArrayList(
                        "argus"
                ),
                15));
        assertTrue(wasCreated);
        System.out.println("added new job");
        Thread.sleep(30000);


        manager.cancelMatchingJob("testRequestUrl", "http://www.google.com");
        manager.cancelMatchingJob("testRequestUrl", "http://www.google.pt");
        System.out.println("canceled all jobs");
        Thread.sleep(30000);


        wasCreated = manager.createJob(new WatchRequest(
                "testRequestUrl",
                "http://www.google.com",
                Lists.newArrayList("the greek", "argus panoptes"),
                10
        ));
        assertTrue(wasCreated);
        System.out.println("added old job");
        Thread.sleep(30000);


        manager.stop();
    }

}

