package argus.util;

import argus.reader.Reader;
import argus.stemmer.Stemmer;
import argus.stopper.StopwordsLoader;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A static class that reads implementations of the Reader interface, grouped by
 * supported extension, and of the Stemmer interface, grouped by supported language.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public class DynamicClassScanner {

    private static final String PROJECT_PACKAGE = "argus";

    private static final Logger logger = LoggerFactory.getLogger(DynamicClassScanner.class);


    /**
     * The implemented reader classes (in the 'readers' package) that will be used
     * to read supported documents after being fetched.
     */
    private static final Map<String, Class<? extends Reader>> existingReaderClasses;

    /**
     * The implemented stemmer classes (in the 'stemmer' package) that will be used
     * to stem supported documents after being read.
     */
    private static final Map<String, Class<? extends Stemmer>> existingStemmerClasses;

    static {
        // Loads implemented reader classes (in the 'readers' package) to read
        // supported files from the corpus during indexing.
        Reflections.log = null;
        Reflections r = new Reflections(PROJECT_PACKAGE);

        existingReaderClasses = new HashMap<>();
        r.getSubTypesOf(Reader.class)
                .stream()
                .filter(c -> !Modifier.isAbstract(c.getModifiers())
                        && !Modifier.isInterface(c.getModifiers()))
                .forEach(readerClass -> {
                    try {
                        Reader reader = readerClass.newInstance();
                        Set<String> extensions = reader.getSupportedContentTypes();
                        reader = null;
                        for (String ext : extensions) {
                            existingReaderClasses.put(ext, readerClass);
                        }
                    } catch (InstantiationException | IllegalAccessException e) {
                        logger.error("There was a problem detecting the currently implemented Reader classes.", e);
                    }
                });

        existingStemmerClasses = new HashMap<>();
        r.getSubTypesOf(Stemmer.class)
                .stream()
                .filter(c -> !Modifier.isAbstract(c.getModifiers())
                        && !Modifier.isInterface(c.getModifiers()))
                .forEach(stemmerClass -> {
                    try {
                        Stemmer stemmer = stemmerClass.newInstance();
                        String supportedLanguage = stemmer.getSupportedLanguage();
                        stemmer = null;
                        existingStemmerClasses.put(supportedLanguage, stemmerClass);
                    } catch (InstantiationException | IllegalAccessException e) {
                        logger.error("There was a problem detecting the currently implemented Stemmer classes.", e);
                    }
                });
    }

    /**
     * Verifies if this server supports reading the specified content type.
     */
    public static boolean hasCompatibleReader(String contentType) {
        return existingReaderClasses.containsKey(contentType);
    }

    /**
     * Returns an implementation of the Reader interface that is capable of
     * reading the specified content type.
     */
    public static Class<? extends Reader> getCompatibleReader(String contentType) {
        return existingReaderClasses.get(contentType);
    }

    /**
     * Verifies if this server supports stemming the specified language.
     */
    public static boolean hasCompatibleStemmer(String language) {
        return existingStemmerClasses.containsKey(language);
    }

    /**
     * Returns an implementation of the Stemmer interface that is capable of
     * stemming the specified language.
     */
    public static Class<? extends Stemmer> getCompatibleStemmer(String language) {
        return existingStemmerClasses.get(language);
    }
}
