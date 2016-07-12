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

package com.edduarte.vokter.diff;

import com.edduarte.vokter.persistence.Document;
import com.edduarte.vokter.similarity.JaccardStringSimilarity;
import com.edduarte.vokter.similarity.LSHSimilarity;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class DiffDetector implements Callable<List<DiffDetector.Result>> {

    private static final Logger logger = LoggerFactory.getLogger(DiffDetector.class);

    private final Document oldSnapshot;

    private final Document newSnapshot;


    public DiffDetector(final Document oldSnapshot,
                        final Document newSnapshot) {
        this.oldSnapshot = oldSnapshot;
        this.newSnapshot = newSnapshot;
    }


    @Override
    public List<Result> call() {
        Stopwatch sw = Stopwatch.createStarted();

        int[] oldBands = oldSnapshot.getBands();
        int[] newBands = newSnapshot.getBands();

        // use LSH band comparisson to determine a similarity index before going
        // for a more computational intensive task. If similarity is below 0.95,
        // the documents are different enough, so send them to DiffMatchPatch
        boolean isCandidatePair = LSHSimilarity.isCandidatePair(oldBands, newBands);
        if (isCandidatePair) {
            // documents might be similar, lets confirm with Jaccard
            List<String> oldShingles = oldSnapshot.getShingles();
            List<String> newShingles = newSnapshot.getShingles();
            double similarityIndex = JaccardStringSimilarity
                    .shingleSimilarity(oldShingles, newShingles);
            if (similarityIndex >= 0.95) {
                // documents are very similar, so just assume there are no
                // relevant differences
                return Collections.emptyList();
            }
        }


        String original = oldSnapshot.getText();
        String revision = newSnapshot.getText();

        DiffMatchPatch dmp = new DiffMatchPatch();
        LinkedList<DiffMatchPatch.Diff> diffs = dmp.diff_main(original, revision);
        dmp.diff_cleanupSemantic(diffs);

        List<Result> retrievedDiffs = diffs.parallelStream()
                .filter(diff -> !diff.getOperation().equals(DiffEvent.nothing))
                .map(Result::new)
                .filter(diff -> diff != null)
                .collect(Collectors.toList());

        sw.stop();
        logger.info("Completed difference detection for document '{}' in {}",
                newSnapshot.getUrl(), sw.toString());
        return retrievedDiffs;
    }


    public static class Result {

        private final DiffEvent event;

        private final String text;

        private final int startIndex;


        private Result(DiffMatchPatch.Diff diff) {
            this.event = diff.action;
            this.text = diff.text;
            this.startIndex = diff.startIndex;
        }


        public DiffEvent getEvent() {
            return event;
        }


        public String getText() {
            return text;
        }


        public int getStartIndex() {
            return startIndex;
        }


        @Override
        public String toString() {
            return "Result{" +
                    "event=" + event +
                    ", text='" + text +
                    "\'}";
        }
    }
}
