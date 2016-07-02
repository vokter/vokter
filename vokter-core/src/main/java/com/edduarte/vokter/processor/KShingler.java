package com.edduarte.vokter.processor;

import com.edduarte.vokter.processor.Processor;
import com.edduarte.vokter.stopper.Stopper;
import it.unimi.dsi.lang.MutableString;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Processor class to retrieve shingles of length k.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class KShingler implements Processor<CharSequence, List<String>> {

    /**
     * K value, generating shingles with length k
     */
    private final int k;

    private final Stopper stopper;


    public KShingler(int k) {
        this(k, null);
    }


    public KShingler(int k, Stopper stopper) {
        this.k = k;
        this.stopper = stopper;
    }


    @Override
    public Callable<List<String>> process(CharSequence s) {
        return new ShingleCallable(k, stopper, s);
    }


    private static class ShingleCallable implements Callable<List<String>> {

        private final int k;

        private final Stopper stopper;

        private final CharSequence text;


        private ShingleCallable(int k, Stopper stopper, CharSequence text) {
            this.k = k;
            this.stopper = stopper;
            this.text = text;
        }


        @Override
        public List<String> call() {

            List<String> shingles = new ArrayList<>();

            for (int i = 0; i < (text.length() - k + 1); i++) {
                MutableString s = new MutableString(text.subSequence(i, i + k));

                if (stopper != null && stopper.isStopword(s)) {
                    // shingle matches a stopword, so skip it
                    continue;
                }

                shingles.add(s.toString());
            }

            return shingles;
        }
    }
}
