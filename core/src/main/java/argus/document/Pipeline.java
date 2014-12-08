package argus.document;

import argus.cleaner.AndCleaner;
import argus.cleaner.Cleaner;
import argus.cleaner.DiacriticCleaner;
import argus.cleaner.SpecialCharsCleaner;
import argus.langdetect.LanguageDetector;
import argus.langdetect.LanguageDetectorFactory;
import argus.parser.Parser;
import argus.parser.ParserResult;
import argus.reader.Reader;
import argus.stemmer.Stemmer;
import argus.stopper.SnowballStopwords;
import argus.stopper.Stopwords;
import argus.term.Term;
import argus.util.Constants;
import argus.util.PluginLoader;
import com.mongodb.DB;
import it.unimi.dsi.lang.MutableString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A processing pipeline that reads, filters and tokenizes a content stream,
 * specifically a document. Every detected token is stored with a group
 * of common occurrences between different documents by using the provided
 * concurrent map structures.
 * <p>
 * This class was named Pipeline in the previous assignment.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 2.0
 */
public class Pipeline implements Callable<Document> {

    private static final Logger logger = LoggerFactory.getLogger(Pipeline.class);

    private final DB termsDatabase;
    private final DocumentInput documentInput;
    private final Parser parser;
    private final boolean isStoppingEnabled;
    private final boolean isStemmingEnabled;
    private final boolean ignoreCase;


    public Pipeline(final DB termsDatabase,
                    final DocumentInput documentInput,
                    final Parser parser,
                    final boolean isStoppingEnabled,
                    final boolean isStemmingEnabled,
                    final boolean ignoreCase) {
        this.termsDatabase = termsDatabase;
        this.documentInput = documentInput;
        this.parser = parser;
        this.isStoppingEnabled = isStoppingEnabled;
        this.isStemmingEnabled = isStemmingEnabled;
        this.ignoreCase = ignoreCase;
    }


    @Override
    public Document call() throws Exception {
        InputStream documentStream = documentInput.getStream();
        String url = documentInput.getUrl();


        // create a temporary in-memory term structure, which will be saved to
        // local files after indexing
        final ConcurrentMap<MutableString, Term> terms = new ConcurrentHashMap<>();


        // reads and parses contents from input content stream
        Class<? extends Reader> readerClass = PluginLoader
                .getCompatibleReader(documentInput.getContentType());
        Reader reader = readerClass.newInstance();
        MutableString content = reader.readDocumentContents(documentStream);
        reader = null;
        documentStream.close();
        documentStream = null;
        documentInput.destroy();


        // creates a document that represents this pipeline processing result.
        // The contents are copied to this object so that it keeps them in its
        // original form, without any transformations that come from filtering
        // or stemming.
        Document document = new Document(url, content.copy());


        // filters the contents by cleaning characters of whole strings
        // according to each cleaner's implementation
        Cleaner cleaner = AndCleaner.of(new SpecialCharsCleaner(), new DiacriticCleaner());
        cleaner.clean(content);
        cleaner = null;


        // infers the document language using a Bayesian detection model
        LanguageDetectorFactory.loadProfile(Constants.LANGUAGE_PROFILES_DIR);
        LanguageDetector langDetector = LanguageDetectorFactory.create();
        langDetector.append(content);
        String languageCode = langDetector.detect();
        langDetector = null;
        LanguageDetectorFactory.clear();


        // sets the parser's stopper according to the detected language
        // if the detected language is not supported, stopping is ignored
        Stopwords stopwords = null;
        if (isStoppingEnabled) {
            stopwords = new SnowballStopwords();
            stopwords.load(languageCode);
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
        List<ParserResult> results = parser.parse(content, stopwords, stemmer, ignoreCase);

        content.delete(0, content.length());
        content = null;

        if (stopwords != null) {
            stopwords.destroy();
        }

        if (stemmer != null) {
            stemmer = null;
        }


        // converts parser results into Term objects
        for (ParserResult r : results) {
            MutableString termText = r.text;

            Term term = new Term(termText);
            Term existingTerm = terms.putIfAbsent(termText, term);
            if (existingTerm != null) {
                term = existingTerm;
            }

            // adds the detected occurrence (by the Tokenizer) as a document
            term.addOccurrence(r.count, r.start, r.end - 1);
        }
        results.clear();
        results = null;


        // calculate the normalization factor (n'lize) for each term in the document
        // NOTE: for the calculations above: wt(t, d) = (1 + log10(tf(t, d))) * idf(t)

        // VectorValue(t) = √ ∑ idf(t)²
        final double vectorValue = Math.sqrt(terms
                        .values()
                        .parallelStream()
                        .mapToDouble(t -> Math.pow(t.getLogFrequencyWeight(), 2))
                        .sum()
        );
        terms.forEach((text, term) -> {

            // wt(t, d) = 1 + log10(tf(t, d))
            double wt = term.getLogFrequencyWeight();

            // nlize(t, d) = wt(t, d) / VectorValue(t)
            double nlize = wt / vectorValue;

            term.addNormalizedWeight(nlize);
        });


        // Uncomment below to print the top10 index
        logger.info("Vocabulary size: " + terms.size());
        terms.values()
                .stream()
                .sorted((o1, o2) -> Integer.compare(o2.getTermFrequency(), o1.getTermFrequency()))
                .limit(10)
                .forEach(term -> logger.info(term.toString() + " " +
                                term.getTermFrequency() + " " +
                                term.getNormalizedWeight()
                ));


        // adds the terms to the document object
        document.addMultipleTerms(termsDatabase, terms.values());

        return document;
    }
}
