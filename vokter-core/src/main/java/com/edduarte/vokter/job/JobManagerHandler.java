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

import com.edduarte.vokter.model.mongodb.Difference;
import com.edduarte.vokter.model.mongodb.Keyword;

import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
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
