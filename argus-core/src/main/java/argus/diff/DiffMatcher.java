package argus.diff;

import argus.job.workers.DiffMatcherJob;
import argus.keyword.Keyword;
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

/**
 * TODO
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class DiffMatcher implements Callable<Set<DiffMatcher.Result>> {

    private static final Logger logger = LoggerFactory.getLogger(DiffMatcher.class);

    private final DiffMatcherJob worker;
    private final List<DiffFinder.Result> differences;

    public DiffMatcher(final DiffMatcherJob worker,
                       final List<DiffFinder.Result> differences) {
        this.worker = worker;
        this.differences = differences;
    }

    @Override
    public Set<DiffMatcher.Result> call() {
        Stopwatch sw = Stopwatch.createStarted();

        Set<Pair<DiffFinder.Result, Keyword>> matchedDiffs = new ConcurrentHashSet<>();

        DiffAction lastAction = DiffAction.nothing;
        BloomFilter<String> lastBloomFilter = null;
        for (DiffFinder.Result r : differences) {
            if (lastAction == DiffAction.nothing || r.action != lastAction) {
                // reset the bloom filter being used
                lastBloomFilter = BloomFilter
                        .create((from, into) -> into.putUnencodedChars(from), 10);
                lastAction = r.action;
            }
            BloomFilter<String> bloomFilter = lastBloomFilter;
            bloomFilter.put(r.occurrenceText);

            // check if at least ONE of the keywords has ALL of its texts contained in
            // the diff text
            worker.keywordStream()
                    .parallel()
                    .unordered()
                    .filter(kw -> kw.textStream().allMatch(bloomFilter::mightContain))
                    .map(kw -> new Pair<>(r, kw))
                    .forEach((pair) -> {
                        if (pair.b().textStream()
                                .anyMatch(pair.a().occurrenceText::equals)) {
                            matchedDiffs.add(pair);
                        }
                    });
        }

        Set<Result> results = matchedDiffs
                .parallelStream()
                .unordered()
                .map(pair -> {
                    DiffFinder.Result diff = pair.a();
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
        logger.info("Completed difference matching for job '{}' in {}",
                worker.getResponseUrl(), sw.toString());
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
