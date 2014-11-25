package argus.util;

import argus.Main;

import java.io.File;

/**
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public class Constants {

    public static final String PROJECT_DIR = System.getProperty("user.dir")
            + File.separator
            + "src"
            + File.separator
            + "main";

    public static final File INSTALL_DIR = new File(
            Main.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath())
            .getAbsoluteFile()
            .getParentFile();

    public static final File LANGUAGE_PROFILES_DIR =
            new File(INSTALL_DIR, "lang-profiles");

    public static final File READER_CLASSES_DIR =
            new File(INSTALL_DIR, "readers");

    public static final File STEMMER_CLASSES_DIR =
            new File(INSTALL_DIR, "stemmers");

    public static final File STOPWORDS_DIR =
            new File(INSTALL_DIR, "stopwords");

    public static final File DOCUMENTS_DIR =
            new File(INSTALL_DIR, "documents");

    public static final File TERMS_DIR =
            new File(INSTALL_DIR, "mongodb");

    public static long difference(long n1, long n2) {
        long result = n1 - n2;
        return result >= 0 ? result : -result;
    }

    public static int difference(int n1, int n2) {
        int result = n1 - n2;
        return result >= 0 ? result : -result;
    }
}
