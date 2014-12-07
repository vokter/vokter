package argus.document;

import argus.Context;
import argus.parser.Parser;
import argus.parser.ParserPool;
import argus.term.Term;
import argus.util.PluginLoader;
import com.google.common.base.Stopwatch;
import com.mongodb.*;
import it.unimi.dsi.lang.MutableString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * Builder class that loads documents streams and indexes them into a
 * {@link DocumentCollection} structure.
 * <p/>
 * This class is a merge of the CorpusLoader class and the Processor classes from
 * the previous assignment.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public final class DocumentBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DocumentBuilder.class);

    private final Stopwatch sw;
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
     * Flag that sets matching of equal terms with different casing.
     */
    private boolean ignoreCase = false;


    private DocumentBuilder(final Supplier<DocumentInput> documentLazySupplier) {
        this.sw = Stopwatch.createUnstarted();
        this.documentLazySupplier = documentLazySupplier;
    }


    /**
     * Instantiates a loader that collects a document from a
     * specified web url, by fetching the content as a InputStream and the content
     * format.
     *
     * @param url the root directory of the documents
     * @return the corups loader instance
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
                String contentType = connection.getContentType();
                ContentType type = new ContentType(contentType);
                return new DocumentInput(url, contentStream, type.getBaseType());

            } catch (IOException | ParseException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private static long folderSize(File directory) {
        long length = 0;
        File[] subFiles = directory.listFiles();
        if (subFiles != null) {
            for (File f : subFiles) {
                length += f.isFile() ? f.length() : folderSize(f);
            }
        }
        return length;
    }

    private static String fileSizeToString(long size) {
        if (size <= 0) {
            return "0 kb";
        }

        final String[] units = new String[]{"bytes", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
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
     * <p/>
     * This method will perform all tasks associated with reading a corpus,
     * processing and indexing it, writing the results to disk persistence and
     * building cached systems that provide synchronous access to documents and
     * tokens.
     * <p/>
     * The most recently accessed tokens and documents are kept in memory for 20
     * seconds before being destroyed. If a token and a document are not in cache,
     * the relevant data is read and parsed from the local files.
     *
     * @return the built index of the documents specified in the factory method
     */
    public Document build(DB termsDatabase, ParserPool parserPool) {

        sw.reset();
        sw.start();


        // step 2) Perform a lazy loading of the document, by obtaining its url,
        // content stream and content type.
        DocumentInput input = documentLazySupplier.get();


        // step 3) Checks if the input document is supported by the server
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


        // step 4) Takes a parser from the context parser-pool.
        Parser parser = null;
        try {
            parser = parserPool.take();
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }


        // step 5) Build a processing instruction to be executed.
        //         A pipeline instantiates a new object for each of the
        //         required modules, improving performance of parallel jobs.
        Pipeline pipeline = new Pipeline(

                // general structure that holds the created terms
                termsDatabase,

                // the input document info, including its path and InputStream
                input,

                // parser that will be used for document parsing and term
                // detection
                parser,

                // flag that sets that stopwords will be filtered during
                // tokenization
                isStoppingEnabled,

                // flag that sets that every found term during tokenization will
                // be stemmer
                isStemmingEnabled,

                // flag that forces every found token to be lower case, matching,
                // for example, the words 'be' and 'Be' as the same token
                ignoreCase
        );


        // step 6) Process the document asynchronously.
        Stopwatch sw2 = Stopwatch.createStarted();
        Document aux;
        try {
            aux = pipeline.call();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
        sw2.stop();
        logger.info("Document processor elapsed time: " + sw2.toString());
        sw2 = null;
        final Document document = aux;

        // step 7) Place the used parser back in the context parser-pool.
        try {
            parserPool.place(parser);
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }


        // step 8) calculate the normalization factor (n'lize) for each term in
        //         the document.
        // NOTE: for the calculations above: wt(t, d) = (1 + log10(tf(t, d))) * idf(t)
        DBCursor cursor = document.termCollection.find();
        try {
            int i = 0;
            while(cursor.hasNext() && i < 10) {
                Term term = new Term((BasicDBObject) cursor.next());
                logger.info("{}", term.toString());
                i++;
            }
        } finally {
            cursor.close();
        }

//        // VectorValue(t) = √ ∑ idf(t)²
//        document.termCollection.mapReduce();
//        final double vectorValue = Math.sqrt(terms
//                        .values()
//                        .parallelStream()
//                        .mapToDouble(t -> Math.pow(t.getLogFrequencyWeight(), 2))
//                        .sum()
//        );
//
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


        // Uncomment below to print the top10 index
//        logger.info("Vocabulary size: " + documentTerms.size());
//        documentTerms.values()
//                .stream()
//                .sorted((o1, o2) -> Integer.compare(o2.getTermFrequency(), o1.getTermFrequency()))
//                .limit(10)
//                .forEach(term -> {
//                    logger.info(term.toString() + " " + term.getTermFrequency() + " " + term.getNormalizedWeight());
//                });



//        // step 9) sort and group collected terms by its first character
//        Map<Character, List<Map.Entry<MutableString, Term>>> groupedTokens = documentTerms
//                .entrySet()
//                .stream()
//                .collect(groupingBy(e -> e.getKey().charAt(0)));
//
//
//        // step 10) command the current VM to delete the index folder once the
//        //         application is closed
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            logger.info("Deleting temporary term database.");
//            try {
//                FileDeleteStrategy.FORCE.delete(parentIndexFolder);
//            } catch (IOException ex) {
//                parentIndexFolder.deleteOnExit();
//            }
//        }));
//
//
//        // step 11) create local files (by document id) that contain the
//        //          serialized documentAtom
//        DocumentLoader documentLoader = new DocumentLoader();
//        documentAtom.valueCollection().stream().forEach(documentLoader::write);
//
//
//        // step 12) create a cache loader for the documentAtom local files created above
//        Cache<Long, Document> documentsCache = CacheBuilder
//                .newCache(Long.class, Document.class)
//                .expiryDuration(20, TimeUnit.SECONDS)
//                .maxSize(600)
//                .source(documentLoader)
//                .build();
//        documentsCache.clear();


        // step 13) instantiate the Collection object, which represents the core
        //          access to the above mentioned persistence and cache mechanisms.
        sw.stop();
        logger.info("Elapsed building time: " + sw.toString());

//        String indexDiskSize = fileSizeToString(folderSize(parentIndexFolder));
//        logger.info("Terms Disk size: " + indexDiskSize);
//
//        String documentDiskSize = fileSizeToString(folderSize(parentDocumentsFolder));
//        logger.info("Document Disk size: " + documentDiskSize);

        return document;
    }
}