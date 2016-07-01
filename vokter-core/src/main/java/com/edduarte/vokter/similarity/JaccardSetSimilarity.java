package com.edduarte.vokter.similarity;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class JaccardSetSimilarity implements SetSimilarity {

    private static final Logger logger =
            LoggerFactory.getLogger(JaccardSetSimilarity.class);


    /**
     * Instantiates a Similarity class for number sets using the Jaccard
     * algorithm.
     */
    public JaccardSetSimilarity() {
    }


    public double setSimilarity(Collection<? extends Number> c1,
                                Collection<? extends Number> c2) {
        Set<? extends Number> intersectionSet = c1.stream()
                .filter(c2::contains)
                .collect(Collectors.toSet());
        Set<Number> unionSet = Sets.newHashSet(c1);
        unionSet.addAll(c2);
        return (double) intersectionSet.size() / (double) unionSet.size();
    }
}
