package com.edduarte.vokter.similarity;

import orestes.bloomfilter.HashProvider;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

import static orestes.bloomfilter.HashProvider.HashMethod.Murmur3;
import static org.junit.Assert.assertEquals;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class StringSimilarityTest {

    private static final Logger logger = LoggerFactory.getLogger(StringSimilarityTest.class);

    private static String s1;

    private static String s2;

    private static String s3;

    private static String s4;


    @BeforeClass
    public static void setUp() throws IOException, InterruptedException {

        s1 = "is the of the 100-eyed giant in Greek mythology.";

        // no differences, index should be 1.0 for all algorithms
        s2 = "is the of the 100-eyed giant in Greek mythology.";

        // differences are not negligible
        s3 = "Argus Panoptes is the name of the 100-eyed giant in Norse mythology.";

        // difference is negligible, so for the most part is should NOT be seen
        // as a candidate pair
        s4 = "is the of the 100-eyed giant in Greek mythology .";
    }


    @Test
    public void jaccardTest() {
        // for jaccard indexes, difference between expected and actual must be
        // exact
        JaccardStringSimilarity s = Similarity.ofStrings().jaccard(3);
        assertEquals(1.0, s.stringSimilarity(s1, s2), 0);
        assertEquals(0.6825396825396826, s.stringSimilarity(s1, s3), 0);
        assertEquals(0.9772727272727273, s.stringSimilarity(s1, s4), 0);
    }


    @Test
    public void minHashTest() {
        // for min-hash indexes, which generates signatures for universal hashes
        // using random coefficients, we need a looser delta
        MinHashStringSimilarity s = Similarity.ofStrings().minHashing(200, Murmur3, 3);
        assertEquals(1.0, s.stringSimilarity(s1, s2), 0);
        assertEquals(0.535, s.stringSimilarity(s1, s3), 0.2);
        assertEquals(0.925, s.stringSimilarity(s1, s4), 0.2);
    }


    @Test
    public void lshTest() {
        // for lsh indexes, which determined candidate pairs but produces
        // jaccard indexes, similarity values can be either the exact index from
        // "jaccardTest" or 0.
        LSHStringSimilarity s = Similarity.ofStrings().lsh(20, 5, Murmur3, 3);

        assertEquals(1.0, s.stringSimilarity(s1, s2), 0);
        // because the two sets have the exact same elements, LSH bands will be
        // exactly the same, so this test will always assume these are candidate
        // pairs and return the jaccard index
        // in other words, no need for a try/catch to test if it's 0 instead

        double index = s.stringSimilarity(s1, s3);
        try {
            assertEquals(0.6825396825396826, index, 0);
        } catch (AssertionError error) {
            // maybe this was not considered to be a candidate pair, so test if
            // it's 0
            assertEquals(0, index, 0);
        }

        index = s.stringSimilarity(s1, s4);
        try {
            assertEquals(0.9772727272727273, index, 0);
        } catch (AssertionError error) {
            // maybe this was not considered to be a candidate pair, so test if
            // it's 0
            assertEquals(0, index, 0);
        }
    }
}
