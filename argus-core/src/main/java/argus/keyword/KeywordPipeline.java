package argus.keyword;

import argus.cleaner.AndCleaner;
import argus.cleaner.Cleaner;
import argus.cleaner.DiacriticCleaner;
import argus.cleaner.SpecialCharsCleaner;
import argus.langdetector.LanguageDetector;
import argus.langdetector.LanguageDetectorFactory;
import argus.parser.Parser;
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
public class KeywordPipeline implements Callable<Keyword> {

    private static final Logger logger = LoggerFactory.getLogger(KeywordPipeline.class);

    private final String queryInput;
    private final Parser parser;
    private final boolean isStoppingEnabled;
    private final boolean isStemmingEnabled;
    private final boolean ignoreCase;


    public KeywordPipeline(final String queryInput,
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
    public Keyword call() throws Exception {

        final MutableString content = new MutableString(queryInput);

        // filters the contents by cleaning characters of whole strings
        // according to each cleaner's implementation
        Cleaner cleaner = AndCleaner.of(new SpecialCharsCleaner(), new DiacriticCleaner());
        cleaner.clean(content);
        cleaner = null;


        // infers the document language using a Bayesian detection model
        // FIX-ME if two threads run this, the first language detector will not
        // have loaded profiles
        if (LanguageDetectorFactory.getLangList().isEmpty()) {
            LanguageDetectorFactory.loadProfile(Constants.LANGUAGE_PROFILES_DIR);
        }
        LanguageDetector langDetector = LanguageDetectorFactory.create();
        langDetector.append(content);
        String languageCode = langDetector.detect();
        langDetector = null;
        LanguageDetectorFactory.clear();


        // sets the parser's stopper according to the detected language
        // if the detected language is not supported, stopping is ignored
        Stopwords stopwords = null;
        if (isStoppingEnabled) {
            stopwords = new FileStopwords(languageCode);
            if (stopwords.isEmpty()) {
                // if no compatible stopwords were found, use the english stopwords
                stopwords = new FileStopwords("en");
            }
        }


        // sets the parser's stemmer according to the detected language
        // if the detected language is not supported, stemming is ignored
        Stemmer stemmer = null;
        if (isStemmingEnabled) {
            Class<? extends Stemmer> stemmerClass = PluginLoader.getCompatibleStemmer(languageCode);
            if (stemmerClass != null) {
                stemmer = stemmerClass.newInstance();
            } else {
                // if no compatible stemmers were found, use the english stemmer
                stemmerClass = PluginLoader.getCompatibleStemmer("en");
                if (stemmerClass != null) {
                    stemmer = stemmerClass.newInstance();
                }
            }
        }


        // detects tokens from the document and loads them into separate
        // objects in memory
        List<Parser.Result> results = parser.parse(content, stopwords, stemmer, ignoreCase);
        content.delete(0, content.length());

        if (stopwords != null) {
            stopwords.destroy();
        }

        if (stemmer != null) {
            stemmer = null;
        }


        // create a temporary in-memory term structure and converts parser results
        // into Term objects
        final Set<String> terms = new LinkedHashSet<>();
        for (Parser.Result r : results) {
            MutableString termText = r.text;
            terms.add(termText.toString());
        }
        results.clear();
        results = null;


        // adds the terms to the keyword object
        return new Keyword(queryInput, terms);
    }
}
