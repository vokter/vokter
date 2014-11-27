package argus.query;

import argus.stemmer.Stemmer;
import it.unimi.dsi.lang.MutableString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * A processing pipeline that reads, filters and tokenizes a text input,
 * specifically a query. Every detected token is stored in the provided
 * concurrent map structures, to avoid duplicate term production.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public class QueryPipeline implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(QueryPipeline.class);

    private final MutableString queryInput;
    private final Set<MutableString> queryTexts;
    private final boolean ignoreCase;
    private final Set<MutableString> stopwords;
    private final Class<? extends Stemmer> stemmerClass;


    public QueryPipeline(final MutableString queryInput,
                         final Set<MutableString> queryTexts,
                         final boolean ignoreCase,
                         final Set<MutableString> stopwords,
                         final Class<? extends Stemmer> stemmerClass) {
        this.queryInput = queryInput;
        this.queryTexts = queryTexts;
        this.ignoreCase = ignoreCase;
        this.stopwords = stopwords;
        this.stemmerClass = stemmerClass;
    }


    @Override
    public void run() {
//        try {
//            boolean isStopwordEnabled = stopwords != null;
//
//            Cleaner cleaner = AndCleaner.of(new SpecialCharsCleaner(), new DiacriticCleaner());
//
//            Tokenizer tokenizer = new Tokenizer();
//
//            if (isStopwordEnabled) {
//                tokenizer.setupStopwords(stopwords);
//            }
//
//            if (stemmerClass != null) {
//                Stemmer stemmer = stemmerClass.newInstance();
//                tokenizer.setupStemming(stemmer);
//            }
//
//            if (ignoreCase) {
//                tokenizer.setupIgnoreCase();
//            }
//
//            // filters irrelevant characters from the search
//            cleaner.clean(queryInput);
//
//
//            // tokenizes the input text
//            List<Tokenizer.Result> results = tokenizer.parse(queryInput);
//
//
//            // add the tokenizer results as search terms
//            for (Tokenizer.Result r : results) {
//                MutableString tokenText = r.text;
//                queryTexts.add(tokenText);
//            }
//
//
//            // unloads / garbage collects retained file streams
//            queryInput.delete(0, queryInput.length());
//            cleaner = null;
//            tokenizer = null;
//
//        } catch (ReflectiveOperationException ex) {
//            logger.error(ex.getMessage(), ex);
//        }
    }
}
