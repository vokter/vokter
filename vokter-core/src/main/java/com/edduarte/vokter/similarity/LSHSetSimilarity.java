package com.edduarte.vokter.similarity;

import com.edduarte.vokter.processor.BandsProcessor;
import com.edduarte.vokter.processor.SetSigProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
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
public class LSHSetSimilarity
        extends LSHSimilarity
        implements SetSimilarity {

    private static final Logger logger = LoggerFactory.getLogger(LSHSetSimilarity.class);

    private final JaccardSetSimilarity jaccard;

    private final SetSigProcessor sigp;

    private final BandsProcessor bandp;

    private final ExecutorService exec;


    /**
     * Instantiates a Similarity class for number sets using the LSH algorithm.
     *
     * @param n the total number of unique elements in both sets
     * @param b the number of bands
     * @param r the number of rows
     */
    public LSHSetSimilarity(int n, int b, int r) {
        this(Executors.newFixedThreadPool(MAX_THREADS), n, b, r, 0.5);
    }


    /**
     * Instantiates a Similarity class for number sets using the LSH algorithm.
     *
     * @param n the total number of unique elements in both sets
     * @param b the number of bands
     * @param r the number of rows
     * @param s the threshold (value between 0.0 and 1.0) that balances the
     *          trade-off between the number of false positives and false
     *          negatives. A sensible threshold is 0.5, so we have a equal
     *          number of false positives and false negatives.
     */
    public LSHSetSimilarity(int n, int b, int r, double s) {
        this(Executors.newFixedThreadPool(MAX_THREADS), n, b, r, s);
    }


    /**
     * Instantiates a Similarity class for number sets using the LSH algorithm.
     *
     * @param exec the executor that will receive the concurrent signature and
     *             band processing tasks
     * @param n    the total number of unique elements in both sets
     * @param b    the number of bands
     * @param r    the number of rows
     * @param s    the threshold (value between 0.0 and 1.0) that balances the
     *             trade-off between the number of false positives and false
     *             negatives. A sensible threshold is 0.5, so we have a equal
     *             number of false positives and false negatives.
     */
    public LSHSetSimilarity(ExecutorService exec, int n, int b, int r, double s) {
        // signature size is determined by a threshold S
        int R = (int) Math.ceil(Math.log(1.0 / b) / Math.log(s)) + 1;
        int sigSize = R * b;
        this.jaccard = new JaccardSetSimilarity();
        this.sigp = new SetSigProcessor(n, sigSize);
        this.bandp = new BandsProcessor(b, r);
        this.exec = exec;
    }


    @Override
    public double setSimilarity(Collection<? extends Number> c1,
                                Collection<? extends Number> c2) {
        return isCandidatePair(c1, c2) ?
                jaccard.setSimilarity(c1, c2) : 0;
    }


    public boolean isCandidatePair(Collection<? extends Number> c1,
                                   Collection<? extends Number> c2) {
        try {
            Future<int[]> signatureFuture1 = exec.submit(sigp.process(c1));
            Future<int[]> signatureFuture2 = exec.submit(sigp.process(c2));

            int[] signature1 = signatureFuture1.get();
            int[] signature2 = signatureFuture2.get();

            Future<int[]> bandsFuture1 = exec.submit(bandp.process(signature1));
            Future<int[]> bandsFuture2 = exec.submit(bandp.process(signature2));

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
