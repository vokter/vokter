package argus.diff;

import argus.document.Document;
import argus.job.Job;
import argus.keyword.Keyword;
import argus.parser.Parser;
import argus.parser.ParserPool;
import argus.parser.ParserResult;
import argus.term.Term;
import com.aliasi.util.Pair;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import it.unimi.dsi.lang.MutableString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * TODO
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class DifferenceDetector implements Callable<Multimap<Job, Difference>> {

    private static final Logger logger = LoggerFactory.getLogger(DifferenceDetector.class);
    private static final int SNIPPET_INDEX_OFFSET = 50;

    private final DB termDatabase;
    private final Document oldSnapshot;
    private final Document newSnapshot;
    private final List<Job> jobs;
    private final ParserPool parserPool;

    public DifferenceDetector(final DB termDatabase,
                              final Document oldSnapshot,
                              final Document newSnapshot,
                              final List<Job> jobs,
                              final ParserPool parserPool) {
        this.termDatabase = termDatabase;
        this.oldSnapshot = oldSnapshot;
        this.newSnapshot = newSnapshot;
        this.jobs = jobs;
        this.parserPool = parserPool;
    }

    @Override
    public Multimap<Job, Difference> call() {
        DiffMatchPatch dmp = new DiffMatchPatch();

        String original = getProcessedContent(oldSnapshot, termDatabase);
        String revision = getProcessedContent(newSnapshot, termDatabase);

        LinkedList<DiffMatchPatch.Diff> diffs = dmp.diff_main(original, revision);
        dmp.diff_cleanupSemantic(diffs);

        List<Pair<Job, Keyword>> keywords = jobs.stream()
                .flatMap(Job::keywordStream)
                .collect(Collectors.toList());

        Parser parser;
        try {
            parser = parserPool.take();
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
        int insertedCountOffset = 0, deletedCountOffset = 0;
        List<MatchedDiff> retrievedDiffs = new ArrayList<>();
        for (DiffMatchPatch.Diff diff : diffs) {
            BloomFilter<String> bloomFilter = BloomFilter
                    .create((from, into) -> into.putUnencodedChars(from), 10);
            String diffText = diff.text;

            List<ParserResult> results = parser.parse(new MutableString(diffText));
            for (Iterator<ParserResult> it = results.iterator(); it.hasNext(); ) {
                ParserResult result = it.next();

                switch (diff.status) {
                    case inserted:
                        result.wordNum = insertedCountOffset++;
                        break;

                    case deleted:
                        result.wordNum = deletedCountOffset++;
                        break;

                    case none:
                        insertedCountOffset++;
                        deletedCountOffset++;
                        continue;
                }


                // check if at least ONE of the keywords has ALL of its texts contained in
                // the diff text
                String diffTermText = result.text.toString();
                bloomFilter.put(diffTermText);

                List<Pair<Job, Keyword>> matchedKeywords = keywords
                        .stream()
                        .filter(p -> p.b().textStream().allMatch(bloomFilter::mightContain))
                        .collect(Collectors.toList());

                for (Pair<Job, Keyword> p : matchedKeywords) {
                    if (p.b().textStream().anyMatch(diffTermText::equals)) {
                        retrievedDiffs.add(new MatchedDiff(
                                diff.status,
                                p.a(),
                                p.b(),
                                result.wordNum,
                                diffTermText
                        ));
                    }
                }
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

        Multimap<Job, Difference> result = Multimaps.synchronizedSetMultimap(
                HashMultimap.create()
        );

        retrievedDiffs
                .stream()
                .unordered()
                .map(diff -> {
                    switch (diff.status) {
                        case inserted: {
                            String snippet = getSnippet(newSnapshot, termDatabase, diff);
                            if (snippet != null) {
                                return new Pair<>(diff.job, new Difference(diff.status, diff.keyword, snippet));
                            }
                            break;
                        }
                        case deleted: {
                            String snippet = getSnippet(oldSnapshot, termDatabase, diff);
                            if (snippet != null) {
                                return new Pair<>(diff.job, new Difference(diff.status, diff.keyword, snippet));
                            }
                            break;
                        }
                    }
                    return null;
                })
                .filter(p -> p != null)
                .forEach(p -> result.put(p.a(), p.b()));
        retrievedDiffs.clear();
        retrievedDiffs = null;

        return result;
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


    private static String getSnippet(Document d, DB termsDB, MatchedDiff diff) {
        Term term = d.getTerm(termsDB, diff.termText, diff.wordCount);
        if (term == null) {
            return null;
        }
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

    private static class MatchedDiff {

        /**
         * The status of this difference.
         */
        private final DifferenceStatus status;

        /**
         * The job that triggered notification for this difference.
         */
        private final Job job;

        /**
         * The keyword that triggered notification for this difference.
         */
        private final Keyword keyword;

        /**
         * Number of words until the term of this difference appears in the document.
         */
        private final int wordCount;

        /**
         * The text of the term contained within this difference.
         */
        private final String termText;

        private MatchedDiff(final DifferenceStatus status,
                            final Job job,
                            final Keyword keyword,
                            final int wordCount,
                            final String termText) {
            this.status = status;
            this.job = job;
            this.keyword = keyword;
            this.wordCount = wordCount;
            this.termText = termText;
        }
    }
}
