package argus.query;

import argus.cleaner.AndCleaner;
import argus.cleaner.Cleaner;
import argus.cleaner.DiacriticCleaner;
import argus.cleaner.SpecialCharsCleaner;
import argus.langdetect.LanguageDetector;
import argus.langdetect.LanguageDetectorFactory;
import argus.parser.Parser;
import argus.parser.ParserResult;
import argus.stemmer.Stemmer;
import argus.stopper.FileStopwords;
import argus.stopper.Stopwords;
import argus.util.Constants;
import argus.util.PluginLoader;
import it.unimi.dsi.lang.MutableString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * A processing pipeline that reads, filters and tokenizes a text input,
 * specifically a query.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public class QueryPipeline implements Callable<Query> {

    private static final Logger logger = LoggerFactory.getLogger(QueryPipeline.class);

    private final MutableString queryInput;
    private final Parser parser;
    private final boolean isStoppingEnabled;
    private final boolean isStemmingEnabled;
    private final boolean ignoreCase;


    public QueryPipeline(final MutableString queryInput,
                         final Parser parser,
                         final boolean isStoppingEnabled,
                         final boolean isStemmingEnabled,
                         final boolean ignoreCase) {
        this.queryInput = queryInput;
        this.parser = parser;
        this.isStoppingEnabled = isStoppingEnabled;
        this.isStemmingEnabled = isStemmingEnabled;
        this.ignoreCase = ignoreCase;
    }


    @Override
    public Query call() throws Exception {

        // create a temporary in-memory term structure
        final Set<MutableString> terms = new LinkedHashSet<>();


        // filters the contents by cleaning characters of whole strings
        // according to each cleaner's implementation
        Cleaner cleaner = AndCleaner.of(new SpecialCharsCleaner(), new DiacriticCleaner());
        cleaner.clean(queryInput);
        cleaner = null;


        // infers the document language using a Bayesian detection model
        LanguageDetectorFactory.loadProfile(Constants.LANGUAGE_PROFILES_DIR);
        LanguageDetector langDetector = LanguageDetectorFactory.create();
        langDetector.append(queryInput);
        String languageCode = langDetector.detect();
        langDetector = null;
        LanguageDetectorFactory.clear();


        // sets the parser's stopper according to the detected language
        // if the detected language is not supported, stopping is ignored
        Stopwords stopwords = null;
        if (isStoppingEnabled) {
            stopwords = new FileStopwords(languageCode);
        }


        // sets the parser's stemmer according to the detected language
        // if the detected language is not supported, stemming is ignored
        Stemmer stemmer = null;
        if (isStemmingEnabled) {
            Class<? extends Stemmer> stemmerClass = PluginLoader.getCompatibleStemmer(languageCode);
            if (stemmerClass != null) {
                stemmer = stemmerClass.newInstance();
            }
        }


        // detects tokens from the document and loads them into separate
        // objects in memory
        List<ParserResult> results = parser.parse(queryInput, stopwords, stemmer, ignoreCase);
        queryInput.delete(0, queryInput.length());

        if (stopwords != null) {
            stopwords.destroy();
        }

        if (stemmer != null) {
            stemmer = null;
        }


        // converts parser results into Term objects
        for (ParserResult r : results) {
            MutableString termText = r.text;
            terms.add(termText);
        }
        results.clear();
        results = null;


        // adds the terms to the document object
        Query query = new Query(terms, 0);

        return query;
    }
}
