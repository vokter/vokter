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

import com.edduarte.vokter.model.mongodb.Document;
import com.edduarte.vokter.model.mongodb.Diff;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class DiffDetector implements Callable<List<Diff>> {

    private static final Logger logger = LoggerFactory.getLogger(DiffDetector.class);

    private final Document oldSnapshot;

    private final Document newSnapshot;


    public DiffDetector(final Document oldSnapshot,
                        final Document newSnapshot) {
        this.oldSnapshot = oldSnapshot;
        this.newSnapshot = newSnapshot;
    }


    @Override
    public List<Diff> call() {
        Stopwatch sw = Stopwatch.createStarted();

        DiffMatchPatch dmp = new DiffMatchPatch();

        String original = oldSnapshot.getText();
        String revision = newSnapshot.getText();

        // TODO: use LSH to determine a similarity index. If distance is above
        // 0.4, the documents are different enough and a more computational
        // intensive task (analysing token by token differences).

        LinkedList<DiffMatchPatch.Diff> diffs = dmp.diff_main(original, revision);
        dmp.diff_cleanupSemantic(diffs);

//        int insertedCountOffset = 0, deletedCountOffset = 0;
        List<Diff> retrievedDiffs = diffs.parallelStream()
                .filter(diff -> !diff.getOperation().equals(DiffEvent.nothing))
                .map(diff -> new Diff(
                        diff.getOperation(),
                        diff.getText(),
                        diff.getStartIndex()
                )).collect(Collectors.toList());

        //            String diffText = diff.text;
//            List<Parser.Result> results = parser.parse(new MutableString(diffText));
//            for (Parser.Result result : results) {
//                String snippet;
//                String occurrenceText = result.text.toString();
//                switch (diff.action) {
//                    case inserted: {
//                        int wordNum = insertedCountOffset++;
//                        snippet = getSnippet(newSnapshot, occurrenceText, wordNum);
//                        break;
//                    }
//                    case deleted: {
//                        int wordNum = deletedCountOffset++;
//                        snippet = getSnippet(oldSnapshot, occurrenceText, wordNum);
//                        break;
//                    }
//                    default: {
//                        insertedCountOffset++;
//                        deletedCountOffset++;
//                        continue;
//                    }
//                }
//            }
//            results.clear();
//            results = null;

//        try {
//            parserPool.place(parser);
//        } catch (InterruptedException ex) {
//            logger.error(ex.getMessage(), ex);
//            return null;
//        }

//        ListIterator<MatchedDiff> it = retrievedDiffs.listIterator();
//        int i = 1;
//        while (it.hasNext() && i < retrievedDiffs.size()) {
//            MatchedDiff d1 = it.next();
//            MatchedDiff d2 = retrievedDiffs.get(i);
//
//            if (d1.status == d2.status &&
//                    d1.keyword.equals(d2.keyword) &&
//                    d1.endIndex + SNIPPET_INDEX_OFFSET >= d2.startIndex - SNIPPET_INDEX_OFFSET) {
////                d2.startIndex = d1.startIndex;
//                it.remove();
//
//            } else {
//                i++;
//            }
//        }

        sw.stop();
        logger.info("Completed difference detection for document '{}' in {}",
                newSnapshot.getUrl(), sw.toString());
        return retrievedDiffs;
    }
}
