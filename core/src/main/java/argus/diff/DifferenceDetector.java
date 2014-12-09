package argus.diff;

import argus.document.Document;
import argus.keyword.Keyword;
import argus.term.Term;
import com.aliasi.util.Pair;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import it.unimi.dsi.lang.MutableString;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * TODO
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class DifferenceDetector implements Callable<List<Difference>> {
    private static final int SNIPPET_INDEX_OFFSET = 50;

    private final DB termDatabase;
    private final Document oldSnapshot;
    private final Document newSnapshot;
    private final List<Keyword> keywords;

    public DifferenceDetector(final DB termDatabase,
                              final Document oldSnapshot,
                              final Document newSnapshot,
                              final List<Keyword> keywords) {
        this.termDatabase = termDatabase;
        this.oldSnapshot = oldSnapshot;
        this.newSnapshot = newSnapshot;
        this.keywords = keywords;
    }

    @Override
    public List<Difference> call() {
        DiffMatchPatch dmp = new DiffMatchPatch();

        String original = getProcessedContent(oldSnapshot, termDatabase);
        String revision = getProcessedContent(newSnapshot, termDatabase);

        LinkedList<DiffMatchPatch.Diff> diffs = dmp.diff_main(original, revision);
        dmp.diff_cleanupSemantic(diffs);

        int count = 0, offset = 0;
        List<DetectedDiff> retrievedDiffs = new ArrayList<>();
        for (DiffMatchPatch.Diff diff : diffs) {
            String diffText = diff.text;

            // check if at least ONE of keywords has ALL of its texts contained in
            // the diff text
            Optional<Pair<Keyword, Stream<String>>> op = keywords.stream()
                    .map(k -> new Pair<>(k, k.textStream()))
                    .filter(p -> p.b().allMatch(diffText::contains))
                    .findAny();
            boolean loop = true;
            int startIndex = 0, endIndex;
            do {
                endIndex = diffText.indexOf(' ', startIndex);

                if (endIndex < 0) {
                    // is the last word, add it as a token and stop the loop
                    endIndex = diffText.length();
                    loop = false;
                }

                if (startIndex == endIndex) {
                    // is empty or the first character in the text is a space, so skip it
                    startIndex++;
                    continue;
                }

                final String diffTermText = diffText.substring(startIndex, endIndex);

                // if after trimming the string is empty, then there
                // is no valuable token to collect
                if (diffTermText.isEmpty()) {
                    startIndex = endIndex + 1;
                    continue;
                }

                if (op.isPresent()) {
                    Keyword keyword = op.get().a();
                    retrievedDiffs.add(new DetectedDiff(
                            diff.status,
                            keyword,
                            count,
                            startIndex + offset,
                            endIndex + offset,
                            diffTermText
                    ));
                }


                count++;
                startIndex = endIndex + 1;
            } while (loop);

            offset = offset + endIndex;
        }

        ListIterator<DetectedDiff> it = retrievedDiffs.listIterator();
        int i = 1;
        while (it.hasNext() && i < retrievedDiffs.size()) {
            DetectedDiff d1 = it.next();
            DetectedDiff d2 = retrievedDiffs.get(i);

            if (d1.status == d2.status &&
                    d1.keyword.equals(d2.keyword) &&
                    d1.endIndex + SNIPPET_INDEX_OFFSET >= d2.startIndex - SNIPPET_INDEX_OFFSET) {
//                d2.startIndex = d1.startIndex;
                it.remove();

            } else {
                i++;
            }
        }

        return retrievedDiffs
                .parallelStream()
                .unordered()
                .map(diff -> {
                    switch (diff.status) {
                        case INSERTED: {
                            String snippet = getSnippet(newSnapshot, termDatabase, diff);
                            return new Difference(diff.status, diff.keyword, snippet);
                        }
                        case DELETED: {
                            String snippet = getSnippet(oldSnapshot, termDatabase, diff);
                            return new Difference(diff.status, diff.keyword, snippet);
                        }
                        default:
                            return null;
                    }
                })
                .filter(diff -> diff != null)
                .collect(Collectors.toList());
    }


    /**
     * Converts a cluster of terms associated with a document into a String, where
     * each term is separated by a whitespace.
     */
    @SuppressWarnings("unchecked")
    private static String getProcessedContent(Document d, DB termsDB) {
        DBCollection coll = termsDB.getCollection(d.getTermCollectionName());

        DBCursor cursor = coll.find();
        return StreamSupport.stream(cursor.spliterator(), false)
                .map(Term::new)
                .map(Term::toString)
                .collect(Collectors.joining(" "));
    }


    private static String getSnippet(Document d, DB termsDB, DetectedDiff diff) {
        Term term = d.getTerm(termsDB, diff.termText, diff.wordCount);
        String originalContent = d.getOriginalContent();

        int snippetStart = term.getStartIndex() - SNIPPET_INDEX_OFFSET;
        if (snippetStart < 0) {
            snippetStart = 0;
        }
        int snippetEnd = term.getEndIndex() + SNIPPET_INDEX_OFFSET;
        if (snippetEnd > originalContent.length()) {
            snippetEnd = originalContent.length();
        }
        return originalContent.substring(snippetStart, snippetEnd);
    }

    private static class DetectedDiff {

        /**
         * The status of this difference.
         */
        private final DifferenceStatus status;

        /**
         * The keyword contained within this difference.
         */
        private final Keyword keyword;

        /**
         * Number of words until the term of this difference appears in the document.
         */
        private final int wordCount;

        /**
         * The character-based index when this difference starts on the document.
         */
        public int startIndex;

        /**
         * The character-based index when this difference ends on the document.
         */
        public int endIndex;

        /**
         * The text of the term contained within this difference.
         */
        private final String termText;

        private DetectedDiff(final DifferenceStatus status,
                             final Keyword keyword,
                             final int wordCount,
                             final int startIndex,
                             final int endIndex,
                             final String termText) {
            this.status = status;
            this.keyword = keyword;
            this.wordCount = wordCount;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.termText = termText;
        }
    }
}
