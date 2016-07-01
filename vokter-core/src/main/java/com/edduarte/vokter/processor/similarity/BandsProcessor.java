package com.edduarte.vokter.processor.similarity;


import com.edduarte.vokter.processor.Processor;

import java.util.concurrent.Callable;

/**
 * Processor class to retrieve shingles of length k.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class BandsProcessor implements Processor<int[], int[]> {

    private final int b;

    private final int r;


    public BandsProcessor(int b, int r) {
        this.b = b;
        this.r = r;
    }


    @Override
    public Callable<int[]> process(int[] sig) {
        return new BandsCallable(sig, b, r);
    }


    private static class BandsCallable implements Callable<int[]> {

        private static final int LARGE_PRIME = 433494437;

        private final int[] sig;

        private final int b;

        private final int r;


        private BandsCallable(int[] sig, int b, int r) {
            this.sig = sig;
            this.b = b;
            this.r = r;
        }


        @Override
        public int[] call() throws Exception {
            int sigSize = sig.length;
            int[] res = new int[b];
            int buckets = sigSize / b;

            for (int i = 0; i < sigSize; i++) {
                int band = Math.min(i / buckets, b - 1);
                res[band] = (int) ((res[band] + (long) sig[i] * LARGE_PRIME) % r);
            }

            return res;
        }
    }
}
