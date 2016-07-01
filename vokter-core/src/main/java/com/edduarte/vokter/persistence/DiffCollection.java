package com.edduarte.vokter.persistence;

import com.edduarte.vokter.diff.DiffDetector;

import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public interface DiffCollection {

    /**
     * Adds the specified differences to the collection, associating them with
     * the specified document url and document content type.
     */
    void addDifferences(String documentUrl, String documentContentType,
                        List<DiffDetector.Result> diffs);

    /**
     * Gets all differences in the collection associated with the specified
     * document url and document content type.
     */
    List<Diff> getDifferences(String documentUrl, String documentContentType);

    /**
     * Removes all differences in the collection associated with the specified
     * document url and document content type.
     */
    void removeDifferences(String documentUrl, String documentContentType);

}
