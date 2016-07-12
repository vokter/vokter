package com.edduarte.vokter.hash;

import java.util.Arrays;

/**
 * Different types of hash functions that can be used.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public enum HashMethod {

    /**
     * Generates hash values using the Java Random Number Generator (RNG) which is a Linear Congruential Generator
     * (LCG), implementing the following formula: <br> <code>number_i+1 = (a * number_i + countingBits) mod
     * size</code><br> <br> The RNG is initialized using the value to be hashed.
     */
    RNG(HashProvider::hashRNG),

    /**
     * Generates hash values using the Carter Wegman function (<a href="http://en.wikipedia.org/wiki/Universal_hashing">Wikipedia</a>),
     * which is a universal hashing function. It thus has optimal guarantees for the uniformity of generated hash
     * values. On the downside, the performance is not optimal, as arithmetic operations on large numbers have to be
     * performed.
     */
    CarterWegman(HashProvider::hashCarterWegman),

    /**
     * Generates hash values using a Cyclic Redundancy Check (CRC32). CRC is designed as a checksum for data
     * integrity not as hash function but exhibits very good uniformity and is relatively fast.
     */
    CRC32(HashProvider::hashCRC),

    /**
     * Generates hash values using the Adler32 Checksum algorithm. Adler32 is comparable to CRC32 but is faster at
     * the cost of a less uniform distribution of hash values.
     */
    Adler32(HashProvider::hashAdler),

    /**
     * Generates hash values using the Murmur 2 hash, see: https://code.google.com/p/smhasher/wiki/MurmurHash2
     * <p>
     * Murmur 2 is very fast. However, there is a flaw that affects the uniformity of some input values (for
     * instance increasing integers as strings).
     */
    Murmur2(HashProvider::hashMurmur2),

    /**
     * Generates hash values using the Murmur 3 hash, see: https://code.google.com/p/smhasher/wiki/MurmurHash3
     * <p>
     * Its uniformity is comparable to that of cryptographic hash functions but considerably faster.
     */
    Murmur3(HashProvider::hashMurmur3),

    /**
     * Uses a the Murmur 3 hash in combination with a performance optimization described by Kirsch and Mitzenmacher,
     * see: http://www.eecs.harvard.edu/~kirsch/pubs/bbbf/esa06.pdf - hash values are generated through the scheme
     * h_i = (h1 + i*h2) mod m <p> Though this method is asymptotically optimal our experiements revealed that
     * real-world performance is not as good as pure Murmur 3 hashes or cryptographic hash functions, in particular
     * for random words.</p>
     */
    Murmur3KirschMitzenmacher(HashProvider::hashCassandra),

    /**
     * Uses the Fowler–Noll–Vo (FNV) hash function to generate a hash values. It is superior to the standard
     * implementation in {@link Arrays} and can be easily implemented in most languages. Hashing then uses the very
     * simple Linear Congruential Generator scheme and the Java initialization constants. This method is intended to
     * be employed if the bloom filter has to be used in a language which doesn't support any of the other hash
     * functions. This hash function can then easily be implemented.
     */
    FNVWithLCG(HashProvider::hashSimpleLCG),

    /**
     * Generates a hash value using MD2. MD2 is rather slow an not as evenely distributed as other cryptographic
     * hash functions
     */
    MD2((bytes, m, k) -> HashProvider.hashCrypt(bytes, m, k, "MD2")),

    /**
     * Generates a hash value using the cryptographic MD5 hash function. It is fast and has good guarantees for the
     * uniformity of generated hash values, as the hash functions are designed for cryptographic use.
     */
    MD5((bytes, m, k) -> HashProvider.hashCrypt(bytes, m, k, "MD5")),

    /**
     * Generates a hash value using the cryptographic SHA1 hash function. It is fast but uniformity of hash values
     * is better for the second generation of SHA (256,384,512).
     */
    SHA1((bytes, m, k) -> HashProvider.hashCrypt(bytes, m, k, "SHA-1")),

    /**
     * Generates a hash value using the cryptographic SHA-256 hash function. It is fast and has good guarantees for
     * the uniformity of generated hash values, as the hash functions are designed for cryptographic use.
     */
    SHA256((bytes, m, k) -> HashProvider.hashCrypt(bytes, m, k, "SHA-256")),

    /**
     * Generates a hash value using the cryptographic SHA-384 hash function. It is fast and has good guarantees for
     * the uniformity of generated hash values, as the hash functions are designed for cryptographic use.
     */
    SHA384((bytes, m, k) -> HashProvider.hashCrypt(bytes, m, k, "SHA-384")),

    /**
     * Generates a hash value using the cryptographic SHA-512 hash function. It is fast and has good guarantees for
     * the uniformity of generated hash values, as the hash functions are designed for cryptographic use.
     */
    SHA512((bytes, m, k) -> HashProvider.hashCrypt(bytes, m, k, "SHA-512"));

    private HashFunction hashFunction;


    private HashMethod(HashFunction hashFunction) {
        this.hashFunction = hashFunction;
    }


    public HashFunction getHashFunction() {
        return hashFunction;
    }
}
