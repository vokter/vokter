package com.edduarte.vokter.similarity;

import com.edduarte.vokter.hash.HashMethod;
import com.edduarte.vokter.processor.BandsProcessor;
import com.edduarte.vokter.processor.KShinglesSigProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.edduarte.vokter.Constants.MAX_THREADS;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class LSHStringSimilarity
        extends LSHSimilarity
        implements StringSimilarity {

    private static final Logger logger = LoggerFactory.getLogger(LSHStringSimilarity.class);

    private final JaccardStringSimilarity jaccard;

    private final KShinglesSigProcessor sigp;

    private final BandsProcessor bandp;

    private final ExecutorService exec;


    /**
     * Instantiates a Similarity class for strings using the LSH algorithm.
     *
     * @param b    the number of bands
     * @param r    the number of rows
     * @param hash the hash method to use when hashing shingles to signatures
     * @param k    the length k of the shingles to generate
     */
    public LSHStringSimilarity(int b, int r, HashMethod hash, int k) {
        this(Executors.newFixedThreadPool(MAX_THREADS), b, r, 0.5, hash, k);
    }


    /**
     * Instantiates a Similarity class for strings using the LSH algorithm.
     *
     * @param b    the number of bands
     * @param r    the number of rows
     * @param s    the threshold (value between 0.0 and 1.0) that balances the
     *             trade-off between the number of false positives and false
     *             negatives. A sensible threshold is 0.5, so we have a equal
     *             number of false positives and false negatives.
     * @param hash the hash method to use when hashing shingles to signatures
     * @param k    the length k of the shingles to generate
     */
    public LSHStringSimilarity(int b, int r, double s, HashMethod hash, int k) {
        this(Executors.newFixedThreadPool(MAX_THREADS), b, r, s, hash, k);
    }


    /**
     * Instantiates a Similarity class for strings using the LSH algorithm.
     *
     * @param exec the executor that will receive the concurrent signature and
     *             band processing tasks
     * @param b    the number of bands
     * @param r    the number of rows
     * @param s    the threshold (value between 0.0 and 1.0) that balances the
     *             trade-off between the number of false positives and false
     *             negatives. A sensible threshold is 0.5, so we have a equal
     *             number of false positives and false negatives.
     * @param hash the hash method to use when hashing shingles to signatures
     * @param k    the length k of the shingles to generate
     */
    public LSHStringSimilarity(ExecutorService exec, int b, int r, double s,
                               HashMethod hash, int k) {
        // signature size is determined by a threshold S
        int R = (int) Math.ceil(Math.log(1.0 / b) / Math.log(s)) + 1;
        int signatureSize = R * b;

        this.jaccard = new JaccardStringSimilarity(exec, k);
        this.sigp = new KShinglesSigProcessor(hash, signatureSize);
        this.bandp = new BandsProcessor(b, r);
        this.exec = exec;
    }


    @Override
    public double stringSimilarity(String s1, String s2) {
        return isCandidatePair(s1, s2) ?
                jaccard.stringSimilarity(s1, s2) : 0;
    }


    public boolean isCandidatePair(String s1, String s2) {
        JaccardStringSimilarity.ShinglePair pair =
                jaccard.getShingles(s1, s2);
        try {
            Future<int[]> signatureFuture1 = exec
                    .submit(sigp.process(pair.shingles1));
            Future<int[]> signatureFuture2 = exec
                    .submit(sigp.process(pair.shingles2));

            int[] signature1 = signatureFuture1.get();
            int[] signature2 = signatureFuture2.get();

            Future<int[]> bandsFuture1 = exec
                    .submit(bandp.process(signature1));
            Future<int[]> bandsFuture2 = exec
                    .submit(bandp.process(signature2));

            int[] bands1 = bandsFuture1.get();
            int[] bands2 = bandsFuture2.get();

            return isCandidatePair(bands1, bands2);

        } catch (ExecutionException | InterruptedException ex) {
            String m = "There was a problem processing set signatures.";
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
