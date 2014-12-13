package argus.diff;

import argus.document.Document;
import argus.document.Occurrence;
import argus.parser.Parser;
import argus.parser.ParserPool;
import com.google.common.base.Stopwatch;
import it.unimi.dsi.lang.MutableString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * TODO
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class DiffFinder implements Callable<List<DiffFinder.Result>> {

    private static final Logger logger = LoggerFactory.getLogger(DiffFinder.class);
    private static final int SNIPPET_INDEX_OFFSET = 50;

    private final Document oldSnapshot;
    private final Document newSnapshot;
    private final ParserPool parserPool;

    public DiffFinder(final Document oldSnapshot,
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
    public List<DiffFinder.Result> call() {
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
        List<Result> retrievedDiffs = new ArrayList<>();
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

                retrievedDiffs.add(new Result(
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

    public static class Result {

        /**
         * The status of this difference.
         */
        public final DiffAction action;

        /**
         * The text of the occurrence contained within this difference.
         */
        public final String occurrenceText;

        /**
         * The snippet of this difference in the original document (non-processed).
         */
        public final String snippet;

        protected Result(final DiffAction action,
                         final String occurrenceText,
                         final String snippet) {
            this.action = action;
            this.occurrenceText = occurrenceText;
            this.snippet = snippet;
        }
    }
}
