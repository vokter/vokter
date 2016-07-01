package com.edduarte.vokter.similarity;

import orestes.bloomfilter.HashProvider.HashMethod;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Similarity {

    public static StringSimilarityFactory ofStrings() {
        return new StringSimilarityFactory();
    }

    public static SetSimilarityFactory ofSets() {
        return new SetSimilarityFactory();
    }

    public static class StringSimilarityFactory {

        public final JaccardStringSimilarity jaccard(int shingleLength) {
            return new JaccardStringSimilarity(shingleLength);
        }


        public final MinHashStringSimilarity minHashing(int signatureSize,
                                                        HashMethod hashMethod,
                                                        int shingleLength) {
            return new MinHashStringSimilarity(
                    signatureSize, hashMethod, shingleLength);
        }


        public final LSHStringSimilarity lsh(int bandCount, int rowCount,
                                             HashMethod hashMethod,
                                             int shingleLength) {
            return new LSHStringSimilarity(
                    bandCount, rowCount, hashMethod, shingleLength);
        }
    }


    public static class SetSimilarityFactory {

        public final JaccardSetSimilarity jaccard() {
            return new JaccardSetSimilarity();
        }


        public final MinHashSetSimilarity minHashing(int n, int signatureSize) {
            return new MinHashSetSimilarity(n, signatureSize);
        }


        public final LSHSetSimilarity lsh(int n, int bandCount, int rowCount) {
            return new LSHSetSimilarity(n, bandCount, rowCount);
        }
    }
}
