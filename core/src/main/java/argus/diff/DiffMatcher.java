package argus.diff;

import argus.document.Document;
import argus.job.Job;
import argus.keyword.Keyword;
import argus.term.Term;
import com.aliasi.util.Pair;
import com.google.common.base.Stopwatch;
import com.google.common.hash.BloomFilter;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TODO
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class DiffMatcher implements Callable<Set<DiffMatcher.Result>> {

    private static final Logger logger = LoggerFactory.getLogger(DiffMatcher.class);

    private final Job job;
    private final List<DiffDetector.Result> differences;
    private final Stopwatch sw;

    public DiffMatcher(final Job job,
                       final List<DiffDetector.Result> differences) {
        this.job = job;
        this.differences = differences;
        this.sw = Stopwatch.createUnstarted();
    }

    @Override
    public Set<DiffMatcher.Result> call() {
        sw.start();
        Set<Pair<DiffDetector.Result, Keyword>> matchedDiffs = new ConcurrentHashSet<>();

        DiffAction lastAction = DiffAction.nothing;
        BloomFilter<String> lastBloomFilter = null;
        for (DiffDetector.Result r : differences) {
            if (lastAction == DiffAction.nothing || r.action != lastAction) {
                // reset the bloom filter being used
                lastBloomFilter = BloomFilter
                        .create((from, into) -> into.putUnencodedChars(from), 10);
                lastAction = r.action;
            }
            BloomFilter<String> bloomFilter = lastBloomFilter;
            bloomFilter.put(r.termText);

            // check if at least ONE of the keywords has ALL of its texts contained in
            // the diff text
            job.keywordStream()
                    .parallel()
                    .unordered()
                    .filter(kw -> kw.textStream().allMatch(bloomFilter::mightContain))
                    .map(kw -> new Pair<>(r, kw))
                    .forEach((pair) -> {
                        if (pair.b().textStream().anyMatch(pair.a().termText::equals)) {
                            matchedDiffs.add(pair);
                        }
                    });
        }

        Set<Result> results = matchedDiffs
                .parallelStream()
                .unordered()
                .map(pair -> {
                    DiffDetector.Result diff = pair.a();
                    Keyword keyword = pair.b();
                    switch (diff.action) {
                        case inserted:
                            return new Result(diff.action, keyword, diff.snippet);
                        case deleted:
                            return new Result(diff.action, keyword, diff.snippet);
                    }
                    return null;
                })
                .filter(p -> p != null)
                .collect(Collectors.toSet());

        sw.stop();
        logger.info("Difference matching elapsed time: " + sw.toString());
        return results;
    }

    public static class Result {

        /**
         * The status of this difference.
         */
        public final DiffAction status;

        /**
         * The keyword contained within this difference.
         */
        public final Keyword keyword;

        /**
         * The text that represents this difference, or in other words, the text that
         * was either added or removed from the document.
         */
        public final String snippet;


        protected Result(final DiffAction status,
                         final Keyword keyword,
                         final String snippet) {
            this.status = status;
            this.keyword = keyword;
            this.snippet = snippet;
        }
    }

}
