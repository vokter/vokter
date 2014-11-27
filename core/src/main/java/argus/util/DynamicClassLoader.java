package argus.util;

import argus.Context;
import argus.reader.Reader;
import argus.stemmer.Stemmer;
import it.unimi.dsi.lang.MutableString;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.tuple.Pair;
import org.cache2k.Cache;
import org.cache2k.CacheBuilder;
import org.cache2k.CacheSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * A static class that reads implementations of the Reader interface, grouped by
 * supported extension, and of the Stemmer interface, grouped by supported language.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public class DynamicClassLoader {

    private static final Logger logger = LoggerFactory.getLogger(DynamicClassLoader.class);


    /**
     * The implemented reader classes (in the 'readers' package) that will be used
     * to read supported documents after being fetched.
     */
    private static final Cache<String, Class> existingReaderClasses;
    /**
     * The implemented stemmer classes (in the 'stemmer' package) that will be used
     * to stem supported documents after being read.
     */
    private static final Cache<String, Class> existingStemmerClasses;

    static {
        existingReaderClasses = CacheBuilder.newCache(String.class, Class.class)
                .name("ReaderClassesCache")
                .expiryDuration(5, TimeUnit.SECONDS)
                .source(new ReaderSource())
                .build();

        existingStemmerClasses = CacheBuilder.newCache(String.class, Class.class)
                .name("StemmerClassesCache")
                .expiryDuration(5, TimeUnit.SECONDS)
                .source(new StemmerSource())
                .build();

    }

    private static Stream<Path> filesInDir(Path dir) {
        try {
            return Files.list(dir).flatMap(path -> path.toFile().isDirectory() ?
                    filesInDir(path) :
                    Collections.singletonList(path).stream());

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Returns an implementation of the Reader interface that is capable of
     * reading the specified content type.
     */
    public static Class<? extends Reader> getCompatibleReader(String contentType) {
        return existingReaderClasses.get(contentType);
    }

    /**
     * Returns an implementation of the Stemmer interface that is capable of
     * stemming the specified language.
     */
    public static Class<? extends Stemmer> getCompatibleStemmer(String language) {
        return existingStemmerClasses.get(language);
    }


    private static class PluginLoader extends ClassLoader {

        private PluginLoader(ClassLoader parent) {
            super(parent);
        }

        public static Class loadPlugin(Path pluginFile)
                throws ClassNotFoundException, IOException {

            PluginLoader loader = new PluginLoader(Context.class.getClassLoader());

            String url = "file:" + pluginFile.toAbsolutePath().toString();
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

            MutableString s = new MutableString(pluginFile.getFileName().toString());
            try {
                Class loadedClass = loader.defineClass(
                        "argus." + s.replace(".class", "").toString(),
                        classData,
                        0,
                        classData.length
                );

                loader.clearAssertionStatus();
                loader = null;
                System.gc();

                return loadedClass;

            } catch (NoClassDefFoundError ex) {
                return null;
            }
        }
    }

    private static class ReaderSource implements CacheSource<String, Class> {

        @Override
        public Class<? extends Reader> get(String contentType) throws Throwable {
            final AtomicReference<Class<? extends Reader>> toReturn = new AtomicReference<>();

            // lazily collects all existing reader classes
            Stream<Path> readerFiles = filesInDir(Constants.READER_CLASSES_DIR.toPath());
            Stream<Pair<String, Class<? extends Reader>>> stream = readerFiles.flatMap(path -> {
                try {
                    Class<? extends Reader> readerClass = loadReader(path);
                    if (readerClass != null) {
                        Reader reader = readerClass.newInstance();
                        return reader
                                .getSupportedContentTypes()
                                .parallelStream()
                                .map(type -> Pair.of(type, readerClass));
                    }
                } catch (ReflectiveOperationException ex) {
                    logger.error(ex.getMessage(), ex);
                }
                return Stream.empty();
            });
            stream.forEach(pair -> {
                existingReaderClasses.putIfAbsent(pair.getKey(), pair.getValue());
                if (pair.getKey().equalsIgnoreCase(contentType)) {
                    toReturn.set(pair.getValue());
                }
            });

            return toReturn.get();
        }

        private Class<? extends Reader> loadReader(Path pluginFile) {
            if (pluginFile != null) {
                try {
                    Class loadedClass = PluginLoader.loadPlugin(pluginFile);
                    if (loadedClass != null &&
                            argus.reader.Reader.class.isAssignableFrom(loadedClass) &&
                            !Modifier.isAbstract(loadedClass.getModifiers()) &&
                            !Modifier.isInterface(loadedClass.getModifiers())) {
                        return loadedClass;
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
            return null;
        }
    }

    private static class StemmerSource implements CacheSource<String, Class> {

        @Override
        public Class<? extends Stemmer> get(String contentType) throws Throwable {
            final AtomicReference<Class<? extends Stemmer>> toReturn = new AtomicReference<>();

            // lazily collects all existing reader classes
            Stream<Path> stemmerFiles = filesInDir(Constants.STEMMER_CLASSES_DIR.toPath());
            Stream<Pair<String, Class<? extends Stemmer>>> stream = stemmerFiles.map(path -> {
                try {
                    Class<? extends Stemmer> stemmerClass = loadStemmer(path);
                    if (stemmerClass != null) {
                        Stemmer stemmer = stemmerClass.newInstance();
                        return Pair.of(stemmer.getSupportedLanguage(), stemmerClass);
                    }
                } catch (ReflectiveOperationException ex) {
                    logger.error(ex.getMessage(), ex);
                }
                return null;
            });
            stream.filter(pair -> pair != null).forEach(pair -> {
                existingStemmerClasses.putIfAbsent(pair.getKey(), pair.getValue());
                if (pair.getKey().equalsIgnoreCase(contentType)) {
                    toReturn.set(pair.getValue());
                }
            });

            return toReturn.get();
        }

        private Class<? extends Stemmer> loadStemmer(Path pluginFile) {
            if (pluginFile != null) {
                try {
                    Class loadedClass = PluginLoader.loadPlugin(pluginFile);
                    if (loadedClass != null &&
                            Stemmer.class.isAssignableFrom(loadedClass) &&
                            !Modifier.isAbstract(loadedClass.getModifiers()) &&
                            !Modifier.isInterface(loadedClass.getModifiers())) {
                        return loadedClass;
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
            return null;
        }
    }

}
