package com.edduarte.vokter.similarity;


import com.edduarte.vokter.processor.similarity.KShinglesSigProcessor;
import com.edduarte.vokter.similarity.HashProvider.HashMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.edduarte.vokter.util.Constants.MAX_THREADS;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class MinHashStringSimilarity
        extends MinHashSimilarity
        implements StringSimilarity {

    private static final Logger logger =
            LoggerFactory.getLogger(MinHashStringSimilarity.class);

    private final ExecutorService exec;

    private final JaccardStringSimilarity jaccard;

    private final KShinglesSigProcessor p;


    /**
     * Instantiates a Similarity class for strings using the MinHashing
     * algorithm.
     *
     * @param sigSize the length of the signature array to be generated
     * @param hash    the hash method to use when hashing shingles to signatures
     * @param k       the length k of the shingles to generate
     */
    public MinHashStringSimilarity(int sigSize, HashMethod hash, int k) {
        this(Executors.newFixedThreadPool(MAX_THREADS), sigSize, hash, k);
    }


    /**
     * Instantiates a Similarity class for strings using the MinHashing
     * algorithm.
     *
     * @param exec    the executor that will receive the concurrent shingle
     *                processing tasks
     * @param sigSize the length of the signature array to be generated
     * @param hash    the hash method to use when hashing shingles to signatures
     * @param k       the length k of the shingles to generate
     */
    public MinHashStringSimilarity(ExecutorService exec, int sigSize,
                                   HashMethod hash, int k) {
        this.jaccard = new JaccardStringSimilarity(exec, k);
        this.p = new KShinglesSigProcessor(hash, sigSize);
        this.exec = exec;
    }


    @Override
    public double stringSimilarity(String s1, String s2) {
        JaccardStringSimilarity.ShinglePair p = jaccard.getShingles(s1, s2);
        int[][] signatures = getSignatures(p.shingles1, p.shingles2);
        return signatureSimilarity(signatures[0], signatures[1]);
    }


    int[][] getSignatures(List<String> shingles1, List<String> shingles2) {
        Future<int[]> signatureFuture1 = exec.submit(p.process(shingles1));
        Future<int[]> signatureFuture2 = exec.submit(p.process(shingles2));

        try {
            int[] signature1 = signatureFuture1.get();
            int[] signature2 = signatureFuture2.get();
            int signatureSize = signature1.length;
            int[][] result = new int[2][signatureSize];
            result[0] = signature1;
            result[1] = signature2;
            return result;

        } catch (ExecutionException | InterruptedException ex) {
            String m = "There was a problem processing shingle signatures.";
            logger.error(m, ex);
            throw new RuntimeException(m, ex);
        }
    }


    public void close() {
        exec.shutdown();
        try {
            exec.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            String m = "There was a problem executing the processing tasks.";
            logger.error(m, ex);
            throw new RuntimeException(m, ex);
        }
    }
}
