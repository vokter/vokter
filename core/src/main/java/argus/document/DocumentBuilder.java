package argus.document;

import argus.term.Term;
import argus.util.DynamicClassLoader;
import com.google.common.base.Stopwatch;
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
import java.text.DecimalFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
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
    public Document build() {

        sw.reset();
        sw.start();


        // step 1) create a temporary in-memory document structure, which will be
        //         saved to local files after indexing
        AtomicReference<Document> documentAtom = new AtomicReference<>();


        // step 2) Create a temporary in-memory term structure, which will be
        //         saved to local files after indexing
        ConcurrentMap<MutableString, Term> documentTerms = new ConcurrentHashMap<>();


        // step 3) Perform a lazy loading of the document, by obtaining its url,
        // content stream and content type.
        DocumentInput input = documentLazySupplier.get();


        // step x) Checks if the input document is supported by the server
        boolean isSupported = DynamicClassLoader.getCompatibleReader(input.getContentType()) != null;
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


        // step 4) Build a processing instruction to be executed.
        //         A pipeline instantiates a new object for each of the
        //         required modules, improving performance of parallel jobs.
        DocumentPipeline pipeline = new DocumentPipeline(

                // general structure that holds the collected documentAtom
                documentAtom,

                // general structure that holds the created tokens
                documentTerms,

                // the input document info, including its path and InputStream
                input,

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


        // step 5) Process each documentAtom in parallel to asynchronously obtain
        //         the index from each document
        Stopwatch sw2 = Stopwatch.createStarted();
        Document builtDocument = pipeline.call();
        sw2.stop();
        logger.info("Document processor elapsed time: " + sw2.toString());
        sw2 = null;


        // step 7) calculate the normalization factor (n'lize) for each term in
        //         the document.
        // NOTE: for the calculations above: wt(t, d) = (1 + log10(tf(t, d))) * idf(t)

        // VectorValue(t) = √ ∑ idf(t)²
        final double vectorValue = Math.sqrt(documentTerms
                        .values()
                        .parallelStream()
                        .mapToDouble(t -> Math.pow(t.getLogFrequencyWeight(), 2))
                        .sum()
        );

        documentTerms.forEach((text, term) -> {

            // wt(t, d) = 1 + log10(tf(t, d))
            double wt = term.getLogFrequencyWeight();

            // nlize(t, d) = wt(t, d) / VectorValue(t)
            double nlize = wt / vectorValue;

            term.addNormalizedWeight(nlize);
        });


        // Uncomment below to print the top10 index
        logger.info("Vocabulary size: " + documentTerms.size());
        documentTerms.values()
                .stream()
                .sorted((o1, o2) -> Integer.compare(o2.getTermFrequency(), o1.getTermFrequency()))
                .limit(10)
                .forEach(term -> {
                    logger.info(term.toString() + " " + term.getTermFrequency() + " " + term.getNormalizedWeight());
                });


//        // step 8) sort and group collected terms by its first character
//        Map<Character, List<Map.Entry<MutableString, Term>>> groupedTokens = documentTerms
//                .entrySet()
//                .stream()
//                .collect(groupingBy(e -> e.getKey().charAt(0)));
//
//
//        // step 9) command the current VM to delete the index folder once the
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
//        // step 13) create local files (by document id) that contain the
//        //          serialized documentAtom
//        DocumentLoader documentLoader = new DocumentLoader();
//        documentAtom.valueCollection().stream().forEach(documentLoader::write);
//
//
//        // step 14) create a cache loader for the documentAtom local files created above
//        Cache<Long, Document> documentsCache = CacheBuilder
//                .newCache(Long.class, Document.class)
//                .expiryDuration(20, TimeUnit.SECONDS)
//                .maxSize(600)
//                .source(documentLoader)
//                .build();
//        documentsCache.clear();


        // step 15) instantiate the Collection object, which represents the core
        //          access to the above mentioned persistence and cache mechanisms.
        sw.stop();
        logger.info("Elapsed building time: " + sw.toString());

//        String indexDiskSize = fileSizeToString(folderSize(parentIndexFolder));
//        logger.info("Terms Disk size: " + indexDiskSize);
//
//        String documentDiskSize = fileSizeToString(folderSize(parentDocumentsFolder));
//        logger.info("Document Disk size: " + documentDiskSize);

        return builtDocument;
    }
}