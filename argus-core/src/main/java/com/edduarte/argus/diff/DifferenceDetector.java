/*
 * Copyright 2015 Ed Duarte
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

package com.edduarte.argus.diff;

import com.edduarte.argus.document.Document;
import com.edduarte.argus.document.Occurrence;
import com.edduarte.argus.parser.Parser;
import com.edduarte.argus.parser.ParserPool;
import com.google.common.base.Stopwatch;
import it.unimi.dsi.lang.MutableString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Ed Duarte (<a href="mailto:ed@edduarte.com">ed@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class DifferenceDetector implements Callable<List<Difference>> {

    private static final Logger logger = LoggerFactory.getLogger(DifferenceDetector.class);

    private static final int SNIPPET_INDEX_OFFSET = 50;

    private final Document oldSnapshot;

    private final Document newSnapshot;

    private final ParserPool parserPool;


    public DifferenceDetector(final Document oldSnapshot,
                              final Document newSnapshot,
                              final ParserPool parserPool) {
        this.oldSnapshot = oldSnapshot;
        this.newSnapshot = newSnapshot;
        this.parserPool = parserPool;
    }


    private static String getSnippet(Document d, String occurrenceText, int wordCount) {
        Occurrence occurrence = d.getOccurrence(occurrenceText, wordCount);
        if (occurrence == null) {
            return "";
        }
        String originalContent = d.getOriginalContent();

        int snippetStart = occurrence.getStartIndex() - SNIPPET_INDEX_OFFSET;
        if (snippetStart < 0) {
            snippetStart = 0;
        }
        int snippetEnd = occurrence.getEndIndex() + SNIPPET_INDEX_OFFSET;
        if (snippetEnd > originalContent.length()) {
            snippetEnd = originalContent.length();
        }
        return originalContent.substring(snippetStart, snippetEnd);
    }


    @Override
    public List<Difference> call() {
        Stopwatch sw = Stopwatch.createStarted();

        DiffMatchPatch dmp = new DiffMatchPatch();

        String original = oldSnapshot.getProcessedContent();
        String revision = newSnapshot.getProcessedContent();

        LinkedList<DiffMatchPatch.Diff> diffs = dmp.diff_main(original, revision);
        dmp.diff_cleanupSemantic(diffs);

        Parser parser;
        try {
            parser = parserPool.take();
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }

        int insertedCountOffset = 0, deletedCountOffset = 0;
        List<Difference> retrievedDiffs = new ArrayList<>();
        for (DiffMatchPatch.Diff diff : diffs) {
            String diffText = diff.text;

            List<Parser.Result> results = parser.parse(new MutableString(diffText));
            for (Parser.Result result : results) {
                String snippet;
                String occurrenceText = result.text.toString();
                switch (diff.action) {
                    case inserted: {
                        int wordNum = insertedCountOffset++;
                        snippet = getSnippet(newSnapshot, occurrenceText, wordNum);
                        break;
                    }
                    case deleted: {
                        int wordNum = deletedCountOffset++;
                        snippet = getSnippet(oldSnapshot, occurrenceText, wordNum);
                        break;
                    }
                    default: {
                        insertedCountOffset++;
                        deletedCountOffset++;
                        continue;
                    }
                }

                retrievedDiffs.add(new Difference(
                        diff.action,
                        result.text.toString(),
                        snippet
                ));
            }
            results.clear();
            results = null;
        }

        try {
            parserPool.place(parser);
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }

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
