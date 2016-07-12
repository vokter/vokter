package com.edduarte.vokter.hash;

/**
 * An interface which can be implemented to provide custom hash functions.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public interface HashFunction {

    /**
     * Computes hash values.
     *
     * @param value the byte[] representation of the element to be hashed
     * @param m     integer output range [1,size]
     * @param k     number of hashes to be computed
     * @return int array of hashes hash values
     */
    int[] hash(byte[] value, int m, int k);
}
