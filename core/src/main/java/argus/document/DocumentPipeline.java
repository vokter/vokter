package argus.document;

import argus.cleaner.AndCleaner;
import argus.cleaner.Cleaner;
import argus.cleaner.DiacriticCleaner;
import argus.cleaner.SpecialCharsCleaner;
import argus.langdetect.LanguageDetector;
import argus.langdetect.LanguageDetectorException;
import argus.langdetect.LanguageDetectorFactory;
import argus.reader.Reader;
import argus.stemmer.Stemmer;
import argus.stopper.SnowballStopwordsLoader;
import argus.stopper.StopwordsLoader;
import argus.term.Term;
import argus.tokenizer.Tokenizer;
import argus.util.DynamicClassScanner;
import it.unimi.dsi.lang.MutableString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A processing pipeline that reads, filters and tokenizes a content stream,
 * specifically a document. Every detected token is stored with a group
 * of common occurrences between different documents by using the provided
 * concurrent map structures.
 * <p/>
 * This class was named Pipeline in the previous assignment.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 2.0
 */
public class DocumentPipeline implements Callable<Document> {

    private static final Logger logger = LoggerFactory.getLogger(DocumentPipeline.class);

    private final AtomicReference<Document> documentAtom;
    private final ConcurrentMap<MutableString, Term> documentTerms;
    private final DocumentInput documentInput;
    private final boolean isStoppingEnabled;
    private final boolean isStemmingEnabled;
    private final boolean ignoreCase;


    public DocumentPipeline(final AtomicReference<Document> documentAtom,
                            final ConcurrentMap<MutableString, Term> documentTerms,
                            final DocumentInput documentInput,
                            final boolean isStoppingEnabled,
                            final boolean isStemmingEnabled,
                            final boolean ignoreCase) {
        this.documentAtom = documentAtom;
        this.documentTerms = documentTerms;
        this.documentInput = documentInput;
        this.isStoppingEnabled = isStoppingEnabled;
        this.isStemmingEnabled = isStemmingEnabled;
        this.ignoreCase = ignoreCase;
    }


    @Override
    public Document call() {
        try {
            InputStream documentStream = documentInput.getStream();
            String url = documentInput.getUrl();


            // reads and parses contents from input content stream
            Class<? extends Reader> readerClass = DynamicClassScanner
                    .getCompatibleReader(documentInput.getContentType());
            Reader reader = readerClass.newInstance();
            MutableString content = reader.readDocumentContents(documentStream);
            reader = null;


            // creates a document that represents this pipeline processing result.
            // The contents are copied to this object so that it keeps them in its
            // original form, without any transformations that come from filtering
            // or stemming.
            Document document = new Document(url, content.copy());
            documentAtom.lazySet(document);


            // filters the contents by cleaning characters of whole strings
            // according to each cleaner's implementation
            Cleaner cleaner = AndCleaner.of(new SpecialCharsCleaner(), new DiacriticCleaner());
            cleaner.clean(content);
            cleaner = null;


            Tokenizer tokenizer = new Tokenizer();

            // infers the document language using a Bayesian detection model
            LanguageDetector langDetector = LanguageDetectorFactory.create();
            langDetector.append(content);
            String language = langDetector.detect();
            System.out.println(content);
            System.out.println("language detected: " + language);


            // sets the tokenizer's stopper according to the detected language
            // if the detected language is not supported, stopping is ignored
            if (isStoppingEnabled) {
                StopwordsLoader stopwordsLoader = new SnowballStopwordsLoader();
                stopwordsLoader.load(language);
                tokenizer.enableStopwords(stopwordsLoader);
            }

            // sets the tokenizer's stemmer according to the detected language
            // if the detected language is not supported, stemming is ignored
            if (isStemmingEnabled) {
                Class<? extends Stemmer> stemmerClass = DynamicClassScanner.getCompatibleStemmer(language);
                if (stemmerClass != null) {
                    Stemmer stemmer = stemmerClass.newInstance();
                    tokenizer.enableStemming(stemmer);
                }
            }

            // sets the tokenizer to lower every word-case
            tokenizer.setIgnoreCase(ignoreCase);

            // detects tokens from the document and loads them into separate
            // objects in memory
            List<Tokenizer.Result> results = tokenizer.tokenize(content);
            tokenizer = null;

            // converts tokenizer results into Term objects
            for (Tokenizer.Result r : results) {
                MutableString termText = r.text;

                Term term = new Term(termText);
                Term existingTerm = documentTerms.putIfAbsent(termText, term);
                if (existingTerm != null) {
                    term = existingTerm;
                }
//                Term term = documentTerms.get(termText);
//                if (term == null) {
//                    term = new Term(termText);
//                    documentTerms.put(termText, term);
//                }

                // adds the detected occurrence (by the Tokenizer) as a document
                term.addOccurrence(r.count, r.start, r.end - 1);
            }

            // unloads / garbage collects retained file streams
            documentInput.destroy();
            documentStream.close();
            documentStream = null;
            content.delete(0, content.length());
            content = null;

            return document;

        } catch (IOException | ReflectiveOperationException | LanguageDetectorException ex) {
            throw new RuntimeException(ex);
        }
    }
}
