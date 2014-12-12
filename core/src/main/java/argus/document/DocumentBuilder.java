package argus.document;

import argus.parser.Parser;
import argus.parser.ParserPool;
import argus.util.PluginLoader;
import com.google.common.base.Stopwatch;
import com.mongodb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Supplier;

/**
 * Builder class that loads documents streams and indexes them into a
 * {@link DocumentCollection} structure.
 * <p>
 * This class is a merge of the CorpusLoader class and the Processor classes from
 * the previous assignment.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public final class DocumentBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DocumentBuilder.class);

    /**
     * The low-footprint loader of the document, using a lazy stream.
     */
    private final Supplier<DocumentInput> documentLazySupplier;

    /**
     * Flag that sets usage of stopword filtering.
     */
    private boolean isStoppingEnabled = false;

    /**
     * Flag that sets usage of a porter stemmer.
     */
    private boolean isStemmingEnabled = false;

    /**
     * Flag that sets matching of equal occurrences with different casing.
     */
    private boolean ignoreCase = false;

    private DocumentBuilder(final Supplier<DocumentInput> documentLazySupplier) {
        this.documentLazySupplier = documentLazySupplier;
    }

    /**
     * Instantiates a loader that collects a document from a
     * specified web url, by fetching the content as a InputStream and the content
     * format.
     */
    public static DocumentBuilder fromUrl(final String url) {
        return new DocumentBuilder(() -> {
            try {
                URL urlToFetch = new URL(url);

                HttpURLConnection.setFollowRedirects(true);
                HttpURLConnection connection = (HttpURLConnection) urlToFetch.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                InputStream contentStream = new BufferedInputStream(connection.getInputStream());
                ContentType contentType = new ContentType(connection.getContentType());
                return new DocumentInput(url, contentStream, contentType.getBaseType());

            } catch (IOException | ParseException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    /**
     * Instantiates a loader that collects a document from a
     * specified input stream. This constructor is mostly used for testing.
     */
    public static DocumentBuilder fromStream(final String url,
                                             final InputStream stream,
                                             final String type) {
        return new DocumentBuilder(() -> {
            try {
                ContentType contentType = new ContentType(type);
                return new DocumentInput(url, stream, contentType.getBaseType());

            } catch (ParseException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    public DocumentBuilder withStopwords() {
        this.isStoppingEnabled = true;
        return this;
    }

    public DocumentBuilder withStemming() {
        this.isStemmingEnabled = true;
        return this;
    }

    public DocumentBuilder ignoreCase() {
        this.ignoreCase = true;
        return this;
    }

    /**
     * Indexes the documents specified in the factory method and adds the index
     * files into the specified folder.
     * <p>
     * This method will perform all tasks associated with reading a corpus,
     * processing and indexing it, writing the results to disk persistence and
     * building cached systems that provide synchronous access to documents and
     * tokens.
     * <p>
     * The most recently accessed tokens and documents are kept in memory for 20
     * seconds before being destroyed. If a token and a document are not in cache,
     * the relevant data is read and parsed from the local files.
     *
     * @return the built index of the documents specified in the factory method
     */
    public Document build(DB occurrencesDB, ParserPool parserPool) {
        Stopwatch sw = Stopwatch.createStarted();

        // step 1) Perform a lazy loading of the document, by obtaining its url,
        // content stream and content type.
        DocumentInput input = documentLazySupplier.get();


        // step 2) Checks if the input document is supported by the server
        boolean isSupported = PluginLoader.getCompatibleReader(input.getContentType()) != null;
        if (!isSupported) {
            logger.info("Ignored processing document '{}': No compatible readers available for content-type '{}'.",
                    input.getUrl(),
                    input.getContentType()
            );
            return null;
        }
        logger.info("Started processing document '{}' with content-type '{}'...",
                input.getUrl(),
                input.getContentType()
        );


        // step 3) Takes a parser from the parser-pool.
        Parser parser;
        try {
            parser = parserPool.take();
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }


        // step 4) Build a processing instruction to be executed.
        //         A pipeline instantiates a new object for each of the
        //         required modules, improving performance of parallel jobs.
        DocumentPipeline pipeline = new DocumentPipeline(

                // general structure that holds the created occurrences
                occurrencesDB,

                // the input document info, including its path and InputStream
                input,

                // parser that will be used for document parsing and occurrence
                // detection
                parser,

                // flag that sets that stopwords will be filtered during
                // tokenization
                isStoppingEnabled,

                // flag that sets that every found occurrence during tokenization will
                // be stemmer
                isStemmingEnabled,

                // flag that forces every found token to be lower case, matching,
                // for example, the words 'be' and 'Be' as the same token
                ignoreCase
        );


        // step 5) Process the document asynchronously.
        Document document;
        try {
            document = pipeline.call();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }


        // step 6) Place the parser back in the parser-pool.
        try {
            parserPool.place(parser);
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }

        sw.stop();
        logger.info("Completed fetching document '{}' in {}",
                document.getUrl(), sw.toString());

        return document;
    }
}