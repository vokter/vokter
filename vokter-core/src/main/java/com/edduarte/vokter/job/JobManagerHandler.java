/*
 * Copyright 2015 Eduardo Duarte
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.edduarte.vokter.job;

import com.edduarte.vokter.diff.Match;
import com.edduarte.vokter.model.mongodb.Keyword;
import com.edduarte.vokter.model.mongodb.Session;

import java.util.List;
import java.util.Set;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public interface JobManagerHandler {

    /**
     * Indexes the specified document and detects differences between an older
     * snapshot and the new one with the specified content-type. Once
     * differences are collected, saves the resulting index of all occurrences
     * of the new snapshot for future query and comparison jobs.
     */
    DetectResult detectDifferences(String url, String contentType);

    /**
     * Matches the existing differences that were stored in the database with
     * the provided keywords
     */
    Set<Match> matchDifferences(String documentUrl, String documentContentType,
                                List<Keyword> keywords,
                                boolean filterStopwords, boolean enableStemming,
                                boolean ignoreCase,
                                boolean ignoreAdded, boolean ignoreRemoved,
                                int snippetOffset);

    /**
     * Removes existing differences for the specified documentUrl with the
     * specified content-type
     */
    void removeExistingDifferences(String documentUrl,
                                   String documentContentType);

    /**
     * Process and build keyword objects based on this context configuration
     */
    Keyword buildKeyword(String keywordInput, boolean isStoppingEnabled,
                         boolean isStemmingEnabled, boolean ignoreCase);

    /**
     * Get a session for the pair (clientUrl, clientContentType), and if one
     * does not exist, create one
     */
    Session createOrGetSession(String clientUrl, String clientContentType);

    /**
     * Remove a session for the pair (clientUrl, clientContentType)
     */
    void removeSession(String clientUrl, String clientContentType);

    /**
     * Gets a session if the pair (clientUrl, clientContentType) matches with
     * the provided token. If not, return null
     */
    Session validateToken(String clientUrl, String clientContentType, String token);

    public static class DetectResult {

        private final boolean wasSuccessful;

        private final boolean hasNewDiffs;


        public DetectResult(boolean wasSuccessful, boolean hasNewDiffs) {
            this.wasSuccessful = wasSuccessful;
            this.hasNewDiffs = hasNewDiffs;
        }


        public boolean wasSuccessful() {
            return wasSuccessful;
        }


        public boolean hasNewDiffs() {
            return hasNewDiffs;
        }
    }
}
