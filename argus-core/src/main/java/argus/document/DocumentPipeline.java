package argus.document;

import argus.cleaner.AndCleaner;
import argus.cleaner.Cleaner;
import argus.cleaner.DiacriticCleaner;
import argus.cleaner.SpecialCharsCleaner;
import argus.langdetector.LanguageDetector;
import argus.langdetector.LanguageDetectorFactory;
import argus.parser.Parser;
import argus.reader.Reader;
import argus.stemmer.Stemmer;
import argus.stopper.FileStopwords;
import argus.stopper.Stopwords;
import argus.util.Constants;
import argus.util.PluginLoader;
import com.mongodb.DB;
import it.unimi.dsi.lang.MutableString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * A processing pipeline that reads, filters and tokenizes a content stream,
 * specifically a document. Every detected token is stored with a group
 * of common occurrences between different documents by using the provided
 * concurrent map structures.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 2.0
 */
public class DocumentPipeline implements Callable<Document> {

    private static final Logger logger = LoggerFactory.getLogger(DocumentPipeline.class);

    private final DB occurrencesDB;
    private final DocumentInput documentInput;
    private final Parser parser;
    private final boolean isStoppingEnabled;
    private final boolean isStemmingEnabled;
    private final boolean ignoreCase;


    public DocumentPipeline(final DB occurrencesDB,
                            final DocumentInput documentInput,
                            final Parser parser,
                            final boolean isStoppingEnabled,
                            final boolean isStemmingEnabled,
                            final boolean ignoreCase) {
        this.occurrencesDB = occurrencesDB;
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

        // reads and parses contents from input content stream
        Class<? extends Reader> readerClass = PluginLoader
                .getCompatibleReader(documentInput.getContentType());
        Reader reader = readerClass.newInstance();
        MutableString content = reader.readDocumentContents(documentStream);
        reader = null;
        documentStream.close();
        documentStream = null;
        documentInput.destroy();


        // filters the contents by cleaning characters of whole strings
        // according to each cleaner's implementation
        Cleaner cleaner = AndCleaner.of(new SpecialCharsCleaner(), new DiacriticCleaner());
        cleaner.clean(content);
        cleaner = null;


        String temp = content.toString();
        temp = temp.replaceAll(" +", " ");
        temp = temp.trim();
        content.replace(0, content.length(), temp);


        // creates a document that represents this pipeline processing result.
        // The contents are copied to this object so that it keeps them in its
        // original form, without any transformations that come from cleaning,
        // stopping or stemming.
        Document document = new Document(occurrencesDB, url, content.toString());


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

        if (stopwords != null) {
            stopwords.destroy();
            stopwords = null;
        }

        if (stemmer != null) {
            stemmer = null;
        }


//        // calculate the normalization factor (n'lize) for each term in the document
//        // NOTE: for the calculations above: wt(t, d) = (1 + log10(tf(t, d))) * idf(t)
//
//        // VectorValue(t) = √ ∑ idf(t)²
//        final double vectorValue = Math.sqrt(terms
//                        .values()
//                        .parallelStream()
//                        .mapToDouble(t -> Math.pow(t.getLogFrequencyWeight(), 2))
//                        .sum()
//        );
//        terms.forEach((text, term) -> {
//
//            // wt(t, d) = 1 + log10(tf(t, d))
//            double wt = term.getLogFrequencyWeight();
//
//            // nlize(t, d) = wt(t, d) / VectorValue(t)
//            double nlize = wt / vectorValue;
//
//            term.addNormalizedWeight(nlize);
//        });


//        // Uncomment below to print the top10 index
//        logger.info("Vocabulary size: " + results.size());
//        results.stream().forEach(r -> logger.info(r.text.toString()));


        content.delete(0, content.length());
        content = null;


        // create a database collection for this document terms and converts
        // parser results into Term objects

        Stream<Occurrence> termStream = results.stream()
                .map(r -> new Occurrence(r.text.toString(), r.wordNum, r.start, r.end - 1));
        document.addOccurrences(termStream);

        results.clear();
        results = null;

        return document;
    }
}
