package argus.diff;

import argus.job.MatchingJob;
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
public class DifferenceMatcher implements Callable<Set<DifferenceMatcher.Result>> {

    private static final Logger logger = LoggerFactory.getLogger(DifferenceMatcher.class);

    private final List<Keyword> keywords;
    private final List<Difference> differences;

    public DifferenceMatcher(final List<Keyword> keywords,
                             final List<Difference> differences) {
        this.keywords = keywords;
        this.differences = differences;
    }

    @Override
    public Set<DifferenceMatcher.Result> call() {
        Stopwatch sw = Stopwatch.createStarted();

        Set<Result> matchedDiffs = new ConcurrentHashSet<>();

        DifferenceAction lastAction = DifferenceAction.nothing;
        BloomFilter<String> lastBloomFilter = null;
        for (Difference r : differences) {
            if (lastAction == DifferenceAction.nothing || r.getAction() != lastAction) {
                // reset the bloom filter being used
                lastBloomFilter = BloomFilter
                        .create((from, into) -> into.putUnencodedChars(from), 10);
                lastAction = r.getAction();
            }
            BloomFilter<String> bloomFilter = lastBloomFilter;
            bloomFilter.put(r.getOccurrenceText());

            // check if AT LEAST ONE of the keywords has ALL of its texts
            // contained in the diff text
            keywords.parallelStream()
                    .unordered()
                    .filter(kw -> kw.textStream().allMatch(bloomFilter::mightContain))
                    .map(kw -> new Pair<>(r, kw))
                    .filter((pair) -> pair.b().textStream().anyMatch(pair.a().getOccurrenceText()::equals))
                    .map((pair) -> {
                        Difference diff = pair.a();
                        Keyword keyword = pair.b();
                        switch (diff.getAction()) {
                            case inserted:
                                return new Result(diff.getAction(), keyword, diff.getSnippet());
                            case deleted:
                                return new Result(diff.getAction(), keyword, diff.getSnippet());
                        }
                        return null;
                    })
                    .filter(diff -> diff != null)
                    .forEach(matchedDiffs::add);
        }

        sw.stop();
        logger.info("Completed difference matching for keywords '{}' in {}",
                keywords.toString(), sw.toString());
        return matchedDiffs;
    }

    public static class Result {

        /**
         * The action of this difference.
         */
        public final DifferenceAction action;

        /**
         * The keyword contained within this difference.
         */
        public final Keyword keyword;

        /**
         * The text that represents this difference, or in other words, the text that
         * was either added or removed from the document.
         */
        public final String snippet;

        protected Result(final DifferenceAction action,
                         final Keyword keyword,
                         final String snippet) {
            this.action = action;
            this.keyword = keyword;
            this.snippet = snippet;
        }
    }

}
