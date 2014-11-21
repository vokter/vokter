package argus.util;

import argus.reader.Reader;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A static class that reads implementations of the Reader interface, groupped by
 * supported extension.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public class ReaderScanner {

    private static final String PROJECT_PACKAGE = "argus";

    private static final Logger logger = LoggerFactory.getLogger(ReaderScanner.class);


    /**
     * The implemented reader classes (in the 'readers' package) that will be used
     * to read supported files from the document collection.
     */
    private static final Map<String, Class<? extends Reader>> existingReaderClasses;

    static {
        // Loads implemented reader classes (in the 'readers' package) to read
        // supported files from the corpus during indexing.
        Reflections.log = null;
        Reflections r = new Reflections(PROJECT_PACKAGE);
        existingReaderClasses = r
                .getSubTypesOf(Reader.class)
                .stream()
                .filter(c -> !Modifier.isAbstract(c.getModifiers())
                        && !Modifier.isInterface(c.getModifiers()))
                .collect(Collectors.toMap(readerClass -> {
                    try {
                        Reader reader = readerClass.newInstance();
                        String ext = reader.getSupportedExtension();
                        reader = null;
                        return ext;

                    } catch (InstantiationException | IllegalAccessException e) {
                        logger.error("There was a problem detecting the currently implemented Reader classes.", e);
                    }
                    return null;
                }, aClass -> aClass));
    }

    /**
     * Verifies if this server supports reading the specified file extension.
     */
    public static boolean supportsExtension(String ext) {
        return existingReaderClasses.containsKey(ext);
    }

    /**
     * Returns an implementation of the Reader interface that is capable of
     * reading the specified file extension.
     */
    public static Class<? extends Reader> getCompatibleReader(String ext) {
        return existingReaderClasses.get(ext);
    }
}
