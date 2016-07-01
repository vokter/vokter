package com.edduarte.vokter.similarity;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class SetSimilarityTest {

    private static final Logger logger = LoggerFactory.getLogger(SetSimilarityTest.class);

    private static Collection<Integer> c1;

    private static Collection<Integer> c2;

    private static Collection<Integer> c3;

    private static Collection<Integer> c4;


    @BeforeClass
    public static void setUp() throws IOException, InterruptedException {
        c1 = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8);
        c2 = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8);
        c3 = Arrays.asList(-1, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        c4 = Arrays.asList(18, 1, 2, 3, 4, 10, 6, 7, 8);
    }


    @Test
    public void jaccardTest() {
        // for jaccard indexes, difference between expected and actual must be
        // exact
        JaccardSetSimilarity s = Similarity.ofSets().jaccard();
        assertEquals(1.0, s.setSimilarity(c1, c2), 0);
        assertEquals(0.7272727272727273, s.setSimilarity(c1, c3), 0);
        assertEquals(0.6363636363636364, s.setSimilarity(c1, c4), 0);
    }


    @Test
    public void minHashTest() {
        // for min-hash indexes, which generates signatures for universal hashes
        // using random coefficients, we need a looser delta
        MinHashSetSimilarity s = Similarity.ofSets().minHashing(c1.size(), 200);
        assertEquals(1.0, s.setSimilarity(c1, c2), 0);
        assertEquals(0.495, s.setSimilarity(c1, c3), 0.2);
        assertEquals(0.94, s.setSimilarity(c1, c4), 0.2);
    }


    @Test
    public void lshTest() {
        // for lsh indexes, which determined candidate pairs but produces
        // jaccard indexes, similarity values can be either the exact index from
        // "jaccardTest" or 0.
        LSHSetSimilarity s = Similarity.ofSets().lsh(c1.size(), 20, 5);

        assertEquals(1.0, s.setSimilarity(c1, c2), 0);
        // because the two sets have the exact same elements, LSH bands will be
        // exactly the same, so this test will always assume these are candidate
        // pairs and return the jaccard index
        // in other words, no need for a try/catch to test if it's 0 instead

        double index = s.setSimilarity(c1, c3);
        try {
            assertEquals(0.7272727272727273, index, 0);
        } catch (AssertionError error) {
            // maybe this was not considered to be a candidate pair, so test if
            // it's 0
            assertEquals(0, index, 0);
        }

        index = s.setSimilarity(c1, c4);
        try {
            assertEquals(0.6363636363636364, index, 0);
        } catch (AssertionError error) {
            // maybe this was not considered to be a candidate pair, so test if
            // it's 0
            assertEquals(0, index, 0);
        }
    }
}
