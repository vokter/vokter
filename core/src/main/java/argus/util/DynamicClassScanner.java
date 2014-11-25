package argus.util;

import argus.Main;
import argus.reader.Reader;
import argus.stemmer.Stemmer;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

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
    private static final Map<String, File> existingReaderClasses = new HashMap<>();

    /**
     * The implemented stemmer classes (in the 'stemmer' package) that will be used
     * to stem supported documents after being read.
     */
    private static final Map<String, File> existingStemmerClasses = new HashMap<>();

    static {
        // Loads implemented reader classes (in the 'readers' package) to read
        // supported files from the corpus during indexing.
//        Reflections.log = null;
//        Reflections r = new Reflections(PROJECT_PACKAGE);
//
//        existingReaderClasses = new HashMap<>();
//        r.getSubTypesOf(Reader.class)
//                .stream()
//                .filter(c -> !Modifier.isAbstract(c.getModifiers())
//                        && !Modifier.isInterface(c.getModifiers()))
//                .forEach(readerClass -> {
//                    try {
//                        Reader reader = readerClass.newInstance();
//                        Set<String> extensions = reader.getSupportedContentTypes();
//                        reader = null;
//                        for (String ext : extensions) {
//                            existingReaderClasses.put(ext, readerClass);
//                        }
//                    } catch (InstantiationException | IllegalAccessException e) {
//                        logger.error("There was a problem detecting the currently implemented Reader classes.", e);
//                    }
//                });
//
//        existingStemmerClasses = new HashMap<>();
//        r.getSubTypesOf(Stemmer.class)
//                .stream()
//                .filter(c -> !Modifier.isAbstract(c.getModifiers())
//                        && !Modifier.isInterface(c.getModifiers()))
//                .forEach(stemmerClass -> {
//                    try {
//                        Stemmer stemmer = stemmerClass.newInstance();
//                        String supportedLanguage = stemmer.getSupportedLanguage();
//                        stemmer = null;
//                        existingStemmerClasses.put(supportedLanguage, stemmerClass);
//                    } catch (InstantiationException | IllegalAccessException e) {
//                        logger.error("There was a problem detecting the currently implemented Stemmer classes.", e);
//                    }
//                });
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

        File pluginFile = existingReaderClasses.get(contentType);
        if (pluginFile == null) {
            return null;
        }
        try {
            Class loadedClass = PluginLoader.loadPlugin(pluginFile);
            if (Reader.class.isAssignableFrom(loadedClass)) {
                return loadedClass;
            }
        } catch (IOException | ClassNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return null;
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
        return null;
    }


    private static class PluginLoader extends ClassLoader {

        private PluginLoader(ClassLoader parent) {
            super(parent);
        }

        public static Class loadPlugin(File pluginFile)
                throws ClassNotFoundException, IOException {

            PluginLoader loader = new PluginLoader(Main.class.getClassLoader());

            String url = "file:" + pluginFile.getAbsolutePath();
            URL myUrl = new URL(url);
            URLConnection connection = myUrl.openConnection();
            InputStream input = connection.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int data = input.read();

            while (data != -1) {
                buffer.write(data);
                data = input.read();
            }

            input.close();

            byte[] classData = buffer.toByteArray();

            String s = pluginFile.getName();
            Class loadedClass = loader.defineClass(
                    "argus." + s.substring(0, s.lastIndexOf(".")),
                    classData,
                    0,
                    classData.length
            );

            loader.clearAssertionStatus();
            loader = null;
            System.gc();

            return loadedClass;
        }
    }
}
