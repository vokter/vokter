package argus.index;

import argus.filter.AndFilter;
import argus.filter.DiacriticFilter;
import argus.filter.Filter;
import argus.filter.SpecialCharsFilter;
import argus.reader.Reader;
import argus.stemmer.Stemmer;
import argus.tokenizer.Tokenizer;
import gnu.trove.map.TIntObjectMap;
import it.unimi.dsi.lang.MutableString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

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
public class DocumentPipeline implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DocumentPipeline.class);

    private final TIntObjectMap<Document> documents;
    private final ConcurrentMap<MutableString, Term> tokens;
    private final boolean ignoreCase;
    private final DocumentInput documentInput;
    private final Class<? extends Reader> readerClass;
    private final Set<MutableString> stopwords;
    private final Class<? extends Stemmer> stemmerClass;


    public DocumentPipeline(final TIntObjectMap<Document> documents,
                            final ConcurrentMap<MutableString, Term> tokens,
                            final boolean ignoreCase,
                            final DocumentInput documentInput,
                            final Class<? extends Reader> readerClass,
                            final Set<MutableString> stopwords,
                            final Class<? extends Stemmer> stemmerClass) {
        this.documents = documents;
        this.tokens = tokens;
        this.ignoreCase = ignoreCase;
        this.documentInput = documentInput;
        this.readerClass = readerClass;
        this.stopwords = stopwords;
        this.stemmerClass = stemmerClass;
    }


    @Override
    public void run() {
        try {
            Reader reader = readerClass.newInstance();

            boolean isStopwordEnabled = stopwords != null;

            Filter filter = AndFilter.of(new SpecialCharsFilter(), new DiacriticFilter());

            Tokenizer tokenizer = new Tokenizer();

            if (isStopwordEnabled) {
                tokenizer.enableStopwords(stopwords);
            }

            if (stemmerClass != null) {
                Stemmer stemmer = stemmerClass.newInstance();
                tokenizer.enableStemming(stemmer);
            }

            if (ignoreCase) {
                tokenizer.ignoreCase();
            }

            InputStream documentStream = documentInput.getStream();

            Path path = documentInput.getPath();
            String documentFilename;
            if (path.getNameCount() == 1) {
                documentFilename = path.getName(path.getNameCount() - 1).toString();
            } else {
                documentFilename = path.getName(path.getNameCount() - 2).toString()
                        + "_" + path.getName(path.getNameCount() - 1).toString();
            }

            MutableString content = reader.readDocumentContents(documentStream);

            // creates a document that represents this pipeline processing result.
            // The contents are copied to this object so that it keeps them in its
            // original form, without any transformations that come from filtering
            // or stemming.
            Document document = new Document(documentFilename, content.copy());
            int docId = document.getId();
            documents.put(docId, document);

            // filters the contents by cleaning characters of whole strings
            // according to each filter's implementation
            filter.filter(content);

            // detects tokens from documents and loads them into memory (saving
            // them in Token objects)
            List<Tokenizer.Result> results = tokenizer.tokenize(content);


            // converts tokenizer results into Token object in the batch builder
            for (Tokenizer.Result r : results) {

                MutableString tokenText = r.text;

                Term tokenObject = tokens.get(tokenText);
                if (tokenObject == null) {
                    tokenObject = Term.of(tokenText);
                    tokens.put(tokenText, tokenObject);
                }

                // adds the detected occurrence (by the Tokenizer) as a document
                tokenObject.addOccurrence(docId, r.count, r.start, r.end - 1);
            }

            // unloads / garbage collects retained file streams
            documentInput.destroy();
            documentStream.close();
            documentStream = null;
            content.delete(0, content.length());
            content = null;
            reader = null;
            filter = null;
            tokenizer = null;

        } catch (IOException | ReflectiveOperationException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
}
