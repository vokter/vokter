package argus.index;

import argus.reader.Reader;
import argus.stemmer.Stemmer;
import argus.util.ReaderScanner;
import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import gnu.trove.TCollections;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import it.unimi.dsi.lang.MutableString;
import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

/**
 * Builder class that loads documents streams and indexes them into a
 * {@link Collection} structure.
 * <p/>
 * This class is a merge of the CorpusLoader class and the Processor classes from
 * the previous assignment.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public final class CollectionBuilder {

    private static final Logger logger = LoggerFactory.getLogger(CollectionBuilder.class);

    private final Stopwatch sw;
    private final Supplier<Stream<Pair<String, Stream<DocumentInput>>>> corpusLazySupplier;

    private Set<MutableString> stopwords;
    private Class<? extends Stemmer> stemmerClass;
    private boolean ignoreCase = false;


    private CollectionBuilder(
            Supplier<Stream<Pair<String, Stream<DocumentInput>>>> corpusLazySupplier) {
        this.sw = Stopwatch.createUnstarted();
        this.corpusLazySupplier = corpusLazySupplier;
    }


    private static Stream<Path> filesInDir(Path dir) {
        try {
            return Files.list(dir).flatMap(path -> path.toFile().isDirectory()
                            ?
                            filesInDir(path)
                            :
                            Collections.singletonList(path).stream()
            );

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    private static String getExtension(Path path) {
        return FilenameUtils.getExtension(path.toString()).toLowerCase();
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


    /**
     * Instantiates a loader that collects all documents contained within a
     * specified local directory, load InputStreams for each one and groups these
     * by extension.
     *
     * @param rootDir the root directory of the documents
     * @return the corups loader instance
     */
    public static CollectionBuilder fromDir(Path rootDir) {
        return new CollectionBuilder(() -> {

            // Collects lazily all existing sub files in the root directory
            final Stream<Path> subFiles = filesInDir(rootDir);

            // Groups the streams by extension and filters out files that contain
            // unsupported extensions
            Stream<Map.Entry<String, List<Path>>> filteredStream = subFiles
                    .filter(path -> !path.toFile().isDirectory())
                    .collect(groupingBy(CollectionBuilder::getExtension))
                    .entrySet()
                    .stream()
                    .filter(e -> ReaderScanner.supportsExtension(e.getKey()));

            // Opens input streams for each collected file
            return filteredStream.map(e -> Pair.of(e.getKey(), e.getValue()
                    .stream()
                    .map(path -> {
                        try {
                            InputStream is = FileUtils.openInputStream(path.toFile());
                            return new DocumentInput(path, is);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    })));
        });
    }


    /**
     * Instantiates a loader that collects all documents from the specified
     * list of name-to-stream pairings and groups these by extension.
     *
     * @param nameStreamList the list with each document's filename and inputstream
     * @return the loaded documents documents grouped by extension.
     */
    public static CollectionBuilder fromStreams(List<Pair<String, InputStream>> nameStreamList) {
        return new CollectionBuilder(() -> {

            // Collects lazily all existing streams
            final Stream<DocumentInput> dataStream = nameStreamList
                    .stream()
                    .map(p -> new DocumentInput(Paths.get(p.getLeft()), p.getValue()));

            // Groups the streams by extension and filters out files that contain
            // unsupported extensions
            return dataStream
                    .collect(groupingBy(l -> getExtension(l.getPath())))
                    .entrySet()
                    .stream()
                    .map(e -> Pair.of(e.getKey(), e.getValue().stream()))
                    .filter(e -> ReaderScanner.supportsExtension(e.getKey()));
        });
    }


    public CollectionBuilder withStopwords(final Set<MutableString> stopwords) {
        this.stopwords = stopwords;
        return this;
    }


    public CollectionBuilder withStemmer(final Class<? extends Stemmer> stemmerClass) {
        this.stemmerClass = stemmerClass;
        return this;
    }


    public CollectionBuilder ignoreCase() {
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
    public Collection buildInFolders(final File parentIndexFolder, final File parentDocumentsFolder) {
        if (!parentIndexFolder.exists() || !parentIndexFolder.isDirectory()) {
            throw new RuntimeException("Specified index folder is invalid!");
        }

        if (!parentDocumentsFolder.exists() || !parentDocumentsFolder.isDirectory()) {
            throw new RuntimeException("Specified documents folder is invalid!");
        }

        sw.reset();
        sw.start();
        logger.info("Started corpus indexing...");


        // step 1) create a temporary in-memory document structure, which will be
        //         saved to local files after indexing
        TIntObjectMap<Document> documents = TCollections.synchronizedMap(new TIntObjectHashMap<>());


        // step 2) Create a temporary in-memory term structure, which will be
        //         saved to local files after indexing
        ConcurrentMap<MutableString, Term> terms = new ConcurrentHashMap<>();


        // step 3) Perform a lazy loading of the documents InputStreams and returns
        //         the loaded documents grouped by extension
        Stream<Pair<String, Stream<DocumentInput>>> corpusInputs = corpusLazySupplier.get();


        // step 4) Build processing instructions to be executed in parallel.
        //         Each one of these processes instantiate a new object of the
        //         required modules and use these in their own processing pipeline,
        //         improving performance of parallel jobs.
        List<DocumentPipeline> pipelines = new ArrayList<>();
        corpusInputs.forEach(pair -> {
            String extension = pair.getKey();
            Stream<DocumentInput> loadedFileStream = pair.getValue();
            Class<? extends Reader> readerClass = ReaderScanner.getCompatibleReader(extension);

            loadedFileStream.forEach(inputDocument -> {
                DocumentPipeline p = new DocumentPipeline(

                        // general structure that holds the collected documents
                        documents,

                        // general structure that holds the created tokens
                        terms,

                        // flag that forces every found token to be
                        // lower case, matching, for example, the words
                        // 'be' and 'Be' as the same token
                        ignoreCase,

                        // the input document info, including its path and InputStream
                        inputDocument,

                        // the reader class that supports reading the InputDocument above
                        readerClass,

                        // the set of stopwords that will be filtered during tokenization
                        stopwords,

                        // the stemmer class that will be used to stem the detected tokens
                        stemmerClass

                );
                pipelines.add(p);
            });
        });


        // step 5) Process each documents in parallel to asynchronously obtain
        //         the index from each document
        Stopwatch sw2 = Stopwatch.createStarted();
        pipelines.parallelStream().forEach(DocumentPipeline::run);
        sw2.stop();
        logger.info("Indexing processor time: " + sw2.toString());
        sw2 = null;


        int N = documents.size();


        // step 6) obtain a map where each key is an ID of a document and each
        //         value is a stream of terms that said document contains
        // NOTE: Because Java 8 lacks map-reduction during 'collect', the map has
        //       to contain "Entry<Integer, Term>" values instead of "Term" values.
        //       This has to be implicitly handled in the next step.
        Map<Integer, List<Pair<Integer, Term>>> map = terms
                .values()
                .stream()
                .flatMap(term -> term
                        .getOccurringDocuments()
                        .stream()
                        .map(docId -> Pair.of(docId, term)))
                .collect(groupingBy(Pair::getKey));


        // step 7) for each document in the above map, calculate the normalization
        //         factor (n'lize)
        map.forEach((docId, docTerms) -> {

            // for the calculations above: wt(t, d) = (1 + log10(tf(t, d))) * idf(t)

            // VectorValue(t) = √ ∑ idf(t)²
            final double vectorValue = Math.sqrt(
                    docTerms.parallelStream()
                            .mapToDouble(p -> {
                                Term t = p.getValue();
                                return Math.pow(t.getTfIdfWeight(docId, N), 2);
                            })
                            .sum()
            );

            docTerms.forEach(p -> {
                Term t = p.getValue();

                // wt(t, d) = 1 + log10(tf(t, d))
                double wt = t.getTfIdfWeight(docId, N);

                // nlize(t, d) = wt(t, d) / VectorValue(t)
                double nlize = wt / vectorValue;

                t.addNormalizedWeight(docId, nlize);
            });
        });


        // Uncomment below to print the top10 index
//        logger.info("Vocabulary size: " + terms.size());
//        terms.values()
//                .stream()
//                .sorted((o1, o2) -> Integer.compare(o2.getCollectionFrequency(), o1.getCollectionFrequency()))
//                .limit(10)
//                .forEach(term -> {
//                    logger.info(term.toString() + " " + term.getCollectionFrequency() + " " + term.getDocumentFrequency());
//                });


        // step 8) sort and group collected terms by its first character
        Map<Character, List<Map.Entry<MutableString, Term>>> groupedTokens = terms
                .entrySet()
                .stream()
                .collect(groupingBy(e -> e.getKey().charAt(0)));


        // step 9) command the current VM to delete the index folder once the
        //         application is closed
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Deleting temporary term database.");
            try {
                FileDeleteStrategy.FORCE.delete(parentIndexFolder);
            } catch (IOException ex) {
                parentIndexFolder.deleteOnExit();
            }
        }));


        // step 10) create local files that contain the index for every found token,
        //          grouped by first character
        TermLoader termLoader = new TermLoader(parentIndexFolder);
        groupedTokens
                .entrySet()
                .parallelStream()
                .unordered()
                .forEach(entriesForChar -> {
                    Character c = entriesForChar.getKey();
                    Set<Term> termsToWrite = entriesForChar
                            .getValue()
                            .stream()
                            .map(Map.Entry::getValue)
                            .collect(toSet());
                    termLoader.write(c, termsToWrite);
                });


        // step 11) create a cache loader for the index local files created above

        // NOTE: Only 10 cached terms are allowed, reducing heap memory size
        //       consumption when the index has more than 10 frequent terms on
        //       every query.
        LoadingCache<String, Term> termsCache = CacheBuilder
                .newBuilder()
                .expireAfterAccess(20, TimeUnit.SECONDS)
                .maximumSize(10)
                .build(termLoader);
        termsCache.invalidateAll();


        // step 12) command the current VM to delete the documents folder once the
        //          application is closed
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Deleting temporary term database.");
            try {
                FileDeleteStrategy.FORCE.delete(parentDocumentsFolder);
            } catch (IOException ex) {
                parentDocumentsFolder.deleteOnExit();
            }
        }));


        // step 13) create local files (by document id) that contain the
        //          serialized documents
        DocumentLoader documentLoader = new DocumentLoader(parentDocumentsFolder);
        documents.valueCollection().stream().forEach(documentLoader::write);


        // step 14) create a cache loader for the documents local files created above
        LoadingCache<Integer, Document> documentsCache = CacheBuilder
                .newBuilder()
                .expireAfterAccess(20, TimeUnit.SECONDS)
                .build(documentLoader);
        documentsCache.invalidateAll();


        // step 15) instantiate the Collection object, which represents the core
        //          access to the above mentioned persistence and cache mechanisms.
        sw.stop();
        logger.info("Elapsed indexing time: " + sw.toString());

        String indexDiskSize = fileSizeToString(folderSize(parentIndexFolder));
        logger.info("Index Disk size: " + indexDiskSize);

        String documentDiskSize = fileSizeToString(folderSize(parentDocumentsFolder));
        logger.info("Documents Disk size: " + documentDiskSize);

        return new Collection(documentsCache, termsCache, N);
    }
}