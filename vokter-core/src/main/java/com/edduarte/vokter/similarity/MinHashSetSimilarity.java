package com.edduarte.vokter.similarity;

import com.edduarte.vokter.processor.SetSigProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.edduarte.vokter.Constants.MAX_THREADS;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class MinHashSetSimilarity
        extends MinHashSimilarity
        implements SetSimilarity {

    private static final Logger logger =
            LoggerFactory.getLogger(MinHashSetSimilarity.class);

    private final SetSigProcessor p;

    private final ExecutorService exec;


    /**
     * Instantiates a Similarity class for number sets using the MinHashing
     * algorithm.
     *
     * @param n       the total number of unique elements in both sets
     * @param sigSize the length of the signature array to be generated
     */
    public MinHashSetSimilarity(int n, int sigSize) {
        this(Executors.newFixedThreadPool(MAX_THREADS), n, sigSize);
    }


    /**
     * Instantiates a Similarity class for number sets using the MinHashing
     * algorithm.
     *
     * @param exec    the executor that will receive the concurrent shingle
     *                processing tasks
     * @param n       the total number of unique elements in both sets
     * @param sigSize the length of the signature array to be generated
     */
    public MinHashSetSimilarity(ExecutorService exec, int n, int sigSize) {
        this.exec = exec;
        this.p = new SetSigProcessor(n, sigSize);
    }


    @Override
    public double setSimilarity(Collection<? extends Number> c1,
                                Collection<? extends Number> c2) {
        Future<int[]> signatureFuture1 = exec.submit(p.process(c1));
        Future<int[]> signatureFuture2 = exec.submit(p.process(c2));

        try {
            int[] signature1 = signatureFuture1.get();
            int[] signature2 = signatureFuture2.get();

            return signatureSimilarity(signature1, signature2);

        } catch (ExecutionException | InterruptedException ex) {
            String m = "There was a problem processing set signatures.";
            logger.error(m, ex);
            throw new RuntimeException(m, ex);
        }
    }
}
