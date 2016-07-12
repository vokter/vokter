package com.edduarte.vokter.hash;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class HashProvider {
    private static final int seed32 = 89478583;


    /**
     * @param a the byte array to be hashed
     * @return the 32 bit integer hash value
     */
    private static int hashBytes(byte a[]) {
        // 32 bit FNV constants. Using longs as Java does not support unsigned
        // data types.
        final long FNV_PRIME = 16777619;
        final long FNV_OFFSET_BASIS = 2166136261l;

        if (a == null)
            return 0;

        long result = FNV_OFFSET_BASIS;
        for (byte element : a) {
            result = (result * FNV_PRIME) & 0xFFFFFFFF;
            result ^= element;
        }

        return (int) result;
    }


    /**
     * @param value the value to be hashed
     * @param m     integer output range [1,size]
     * @param k     number of hashes to be computed
     * @return array with <i>hashes</i> integer hash positions in the range <i>[0,size)</i>
     */
    public static int[] hashCarterWegman(byte[] value, int m, int k) {
        int[] positions = new int[k];
        BigInteger prime32 = BigInteger.valueOf(4294967279l);
        BigInteger prime64 = BigInteger.valueOf(53200200938189l);
        BigInteger prime128 = new BigInteger("21213943449988109084994671");
        Random r = new Random(seed32);
        //BigInteger.valueOf(hashBytes(value)
        BigInteger v = new BigInteger(value.length > 0 ? value : new byte[1]);

        for (int i = 0; i < k; i++) {
            BigInteger a = BigInteger.valueOf(r.nextLong());
            BigInteger b = BigInteger.valueOf(r.nextLong());
            positions[i] = a.multiply(v).add(b).mod(prime64)
                    .mod(BigInteger.valueOf(m)).intValue();
        }
        return positions;
    }


    /**
     * @param value the value to be hashed
     * @param m     integer output range [1,size]
     * @param k     number of hashes to be computed
     * @return array with <i>hashes</i> integer hash positions in the range <i>[0,size)</i>
     */
    public static int[] hashRNG(byte[] value, int m, int k) {
        int[] positions = new int[k];
        Random r = new Random(hashBytes(value));
        for (int i = 0; i < k; i++) {
            positions[i] = r.nextInt(m);
        }
        return positions;
    }


    /**
     * @param value the value to be hashed
     * @param m     integer output range [1,size]
     * @param k     number of hashes to be computed
     * @return array with <i>hashes</i> integer hash positions in the range <i>[0,size)</i>
     */
    public static int[] hashCRC(byte[] value, int m, int k) {
        return hashChecksum(value, new CRC32(), m, k);
    }


    /**
     * @param value the value to be hashed
     * @param m     integer output range [1,size]
     * @param k     number of hashes to be computed
     * @return array with <i>hashes</i> integer hash positions in the range <i>[0,size)</i>
     */
    public static int[] hashAdler(byte[] value, int m, int k) {
        return hashChecksum(value, new Adler32(), m, k);
    }


    public static int[] hashChecksum(byte[] value, Checksum cs, int m, int k) {
        int[] positions = new int[k];
        int hashes = 0;
        int salt = 0;
        while (hashes < k) {
            cs.reset();
            cs.update(value, 0, value.length);
            // Modify the data to be checksummed by adding the number of already
            // calculated hashes, the loop counter and
            // a static seed
            cs.update(hashes + salt++ + seed32);
            int hash = rejectionSample((int) cs.getValue(), m);
            if (hash != -1) {
                positions[hashes++] = hash;
            }
        }
        return positions;
    }


    /**
     * @param value the value to be hashed
     * @param m     integer output range [1,size]
     * @param k     number of hashes to be computed
     * @return array with <i>hashes</i> integer hash positions in the range <i>[0,size)</i>
     */
    public static int[] hashSimpleLCG(byte[] value, int m, int k) {
        // Java constants
        final long multiplier = 0x5DEECE66DL;
        final long addend = 0xBL;
        final long mask = (1L << 48) - 1;

        // Generate int from byte Array using the FNV hash
        int reduced = Math.abs(hashBytes(value));
        // Make number positive
        // Handle the special case: smallest negative number is itself as the
        // absolute value
        if (reduced == Integer.MIN_VALUE)
            reduced = 42;

        // Calculate hashes numbers iteratively
        int[] positions = new int[k];
        long seed = reduced;
        for (int i = 0; i < k; i++) {
            // LCG formula: x_i+1 = (multiplier * x_i + addend) mod mask
            seed = (seed * multiplier + addend) & mask;
            positions[i] = (int) (seed >>> (48 - 30)) % m;
        }
        return positions;
    }


    public static int[] hashMurmur3(byte[] value, int m, int k) {
        return rejectionSample(HashProvider::murmur3, value, m, k);
    }


    public static int[] hashCassandra(byte[] value, int m, int k) {
        int[] result = new int[k];
        int hash1 = murmur3(0, value);
        int hash2 = murmur3((int) hash1, value);
        for (int i = 0; i < k; i++) {
            result[i] = Math.abs((hash1 + i * hash2) % m);
        }
        return result;
    }


    public static int murmur3(int seed, byte[] bytes) {
        int h1 = seed; //Standard in Guava
        int c1 = 0xcc9e2d51;
        int c2 = 0x1b873593;
        int len = bytes.length;
        int i = 0;

        while (len >= 4) {
            //process()
            int k1 = bytes[i + 0] & 0xFF;
            k1 |= (bytes[i + 1] & 0xFF) << 8;
            k1 |= (bytes[i + 2] & 0xFF) << 16;
            k1 |= (bytes[i + 3] & 0xFF) << 24;

            k1 *= c1;
            k1 = Integer.rotateLeft(k1, 15);
            k1 *= c2;

            h1 ^= k1;
            h1 = Integer.rotateLeft(h1, 13);
            h1 = h1 * 5 + 0xe6546b64;

            len -= 4;
            i += 4;
        }


        if (len > 0) {
            //processingRemaining()
            int k1 = 0;
            switch (len) {
                case 3:
                    k1 ^= (bytes[i + 2] & 0xFF) << 16;
                    // fall through
                case 2:
                    k1 ^= (bytes[i + 1] & 0xFF) << 8;
                    // fall through
                case 1:
                    k1 ^= (bytes[i] & 0xFF);
                    // fall through
                default:
                    k1 *= c1;
                    k1 = Integer.rotateLeft(k1, 15);
                    k1 *= c2;
                    h1 ^= k1;
            }
            i += len;
        }

        //makeHash()
        h1 ^= i;

        h1 ^= h1 >>> 16;
        h1 *= 0x85ebca6b;
        h1 ^= h1 >>> 13;
        h1 *= 0xc2b2ae35;
        h1 ^= h1 >>> 16;

        return h1;
    }


    // Code taken from:
    // http://dmy999.com/article/50/murmurhash-2-java-port by Derekt
    // Young (Public Domain)
    // as the Hadoop implementation by Andrzej Bialecki is buggy
    public static int[] hashMurmur2(byte[] value, int em, int ka) {
        int[] positions = new int[ka];

        int hashes = 0;
        int lastHash = 0;
        byte[] data = value.clone();
        while (hashes < ka) {


            for (int i = 0; i < value.length; i++) {
                if (data[i] == 127) {
                    data[i] = 0;
                    continue;
                } else {
                    data[i]++;
                    break;
                }
            }

            // 'size' and 'r' are mixing constants generated offline.
            // They're not really 'magic', they just happen to work well.
            int m = 0x5bd1e995;
            int r = 24;

            // Initialize the hash to a 'random' value
            int len = data.length;
            int h = seed32 ^ len;

            int i = 0;
            while (len >= 4) {
                int k = data[i + 0] & 0xFF;
                k |= (data[i + 1] & 0xFF) << 8;
                k |= (data[i + 2] & 0xFF) << 16;
                k |= (data[i + 3] & 0xFF) << 24;

                k *= m;
                k ^= k >>> r;
                k *= m;

                h *= m;
                h ^= k;

                i += 4;
                len -= 4;
            }

            switch (len) {
                case 3:
                    h ^= (data[i + 2] & 0xFF) << 16;
                case 2:
                    h ^= (data[i + 1] & 0xFF) << 8;
                case 1:
                    h ^= (data[i + 0] & 0xFF);
                    h *= m;
            }

            h ^= h >>> 13;
            h *= m;
            h ^= h >>> 15;

            lastHash = rejectionSample(h, em);
            if (lastHash != -1) {
                positions[hashes++] = lastHash;
            }
        }
        return positions;
    }


    /**
     * Performs rejection sampling on a random 32bit Java int (sampled from Integer.MIN_VALUE to Integer.MAX_VALUE).
     *
     * @param random int
     * @param m      integer output range [1,size]
     * @return the number down-sampled to interval [0, size]. Or -1 if it has to be rejected.
     */
    public static int rejectionSample(int random, int m) {
        random = Math.abs(random);
        if (random > (2147483647 - 2147483647 % m)
                || random == Integer.MIN_VALUE)
            return -1;
        else
            return random % m;
    }


    public static int[] rejectionSample(BiFunction<Integer, byte[], Integer> hashFunction, byte[] value, int m, int k) {
        int[] hashes = new int[k];
        int seed = 0;
        int pos = 0;
        while (pos < k) {
            seed = hashFunction.apply(seed, value);
            int hash = rejectionSample(seed, m);
            if (hash != -1) {
                hashes[pos++] = hash;
            }
        }
        return hashes;
    }


    /**
     * @param value  the value to be hashed
     * @param m      integer output range [1,size]
     * @param k      number of hashes to be computed
     * @param method the hash method name used by {@link MessageDigest#getInstance(String)}
     * @return array with <i>hashes</i> integer hash positions in the range <i>[0,size)</i>
     */
    public static int[] hashCrypt(byte[] value, int m, int k, String method) {
        //MessageDigest is not thread-safe --> use new instance
        MessageDigest cryptHash = null;
        try {
            cryptHash = MessageDigest.getInstance(method);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        int[] positions = new int[k];

        int computedHashes = 0;
        // Add salt to the hash deterministically in order to generate different
        // hashes for each round
        // Alternative: use pseudorandom sequence
        Random r = new Random(seed32);
        byte[] digest = new byte[0];
        while (computedHashes < k) {
            // byte[] saltBytes =
            // ByteBuffer.allocate(4).putInt(r.nextInt()).array();
            cryptHash.update(digest);
            digest = cryptHash.digest(value);
            BitSet hashed = BitSet.valueOf(digest);

            // Convert the hash to numbers in the range [0,size)
            // Size of the BloomFilter rounded to the next power of two
            int filterSize = 32 - Integer.numberOfLeadingZeros(m);
            // Computed hash bits
            int hashBits = digest.length * 8;
            // Split the hash value according to the size of the Bloomfilter --> higher performance than just doing modulo
            for (int split = 0; split < (hashBits / filterSize)
                    && computedHashes < k; split++) {
                int from = split * filterSize;
                int to = (split + 1) * filterSize;
                BitSet hashSlice = hashed.get(from, to);
                // Bitset to Int
                long[] longHash = hashSlice.toLongArray();
                int intHash = longHash.length > 0 ? (int) longHash[0] : 0;
                // Only use the position if it's in [0,size); Called rejection sampling
                if (intHash < m) {
                    positions[computedHashes] = intHash;
                    computedHashes++;
                }
            }
        }

        return positions;
    }
}
