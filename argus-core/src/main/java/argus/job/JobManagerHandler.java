package argus.job;

import argus.diff.Difference;
import argus.keyword.Keyword;

import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public interface JobManagerHandler {

    /**
     * Indexes the specified document and detects differences between an older
     * snapshot and the new one. Once differences are collected, saves the resulting
     * index of all occurrences of the new snapshot for future query and comparison
     * jobs.
     */
    boolean detectDifferences(String url);

    /**
     * Collects the existing differences that were stored in the database.
     */
    List<Difference> getExistingDifferences(String url);

    /**
     * Removes existing differences for the specified url
     */
    void removeExistingDifferences(String url);

    /**
     * Process and build keyword objects based on this context configuration
     */
    Keyword buildKeyword(String keywordInput);
}
