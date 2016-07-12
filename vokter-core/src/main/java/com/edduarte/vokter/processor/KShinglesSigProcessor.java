package com.edduarte.vokter.processor;

import com.edduarte.vokter.hash.HashMethod;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Processor class to retrieve shingles of length k.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class KShinglesSigProcessor implements Processor<List<String>, int[]> {

    private final HashMethod hash;

    private final int sigSize;


    public KShinglesSigProcessor(HashMethod hash, int sigSize) {
        this.hash = hash;
        this.sigSize = sigSize;
    }


    @Override
    public Callable<int[]> process(List<String> shingles) {
        return new SignatureCallable(shingles, hash, sigSize);
    }


    private class SignatureCallable implements Callable<int[]> {

        private final List<String> shingles;

        private final HashMethod hash;

        private final int sigSize;


        private SignatureCallable(List<String> shingles,
                                  HashMethod hash,
                                  int sigSize) {
            this.shingles = shingles;
            this.hash = hash;
            this.sigSize = sigSize;
        }


        @Override
        public int[] call() {
            int[] sig = new int[sigSize];

            for (int i = 0; i < sigSize; i++) {
                sig[i] = Integer.MAX_VALUE;
            }

            Collections.sort(shingles);

            for (final String s : shingles) {
                byte[] bytes = s.getBytes(Charset.forName("UTF-8"));
                int[] hash = this.hash.getHashFunction()
                        .hash(bytes, Integer.MAX_VALUE, sigSize);
                for (int i = 0; i < sigSize; i++) {
                    sig[i] = Math.min(sig[i], hash[i]);
                }
            }

            return sig;
        }
    }
}
