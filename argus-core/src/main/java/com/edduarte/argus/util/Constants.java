/*
 * Copyright 2015 Ed Duarte
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.edduarte.argus.util;

import com.edduarte.argus.Context;
import org.apache.commons.io.FileDeleteStrategy;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.text.DecimalFormat;

/**
 * @author Ed Duarte (<a href="mailto:ed@edduarte.com">ed@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class Constants {

    public static final String PROJECT_DIR = System.getProperty("user.dir")
            + File.separator
            + "src"
            + File.separator
            + "main";

    public static final File INSTALL_DIR = new File(
            Context.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath())
            .getAbsoluteFile()
            .getParentFile();

    public static final File LANGUAGE_PROFILES_DIR =
            new File(INSTALL_DIR, "argus-lang-profiles");

    public static final File READER_CLASSES_DIR =
            new File(INSTALL_DIR, "argus-readers");

    public static final File STEMMER_CLASSES_DIR =
            new File(INSTALL_DIR, "argus-stemmers");

    public static final File STOPWORDS_DIR =
            new File(INSTALL_DIR, "argus-stopwords");

    public static final File PARSER_DIR =
            new File(INSTALL_DIR, "argus-parser");

    private static final SecureRandom random = new SecureRandom();

    private static final char[] hexArray = "0123456789abcdef".toCharArray();


    /**
     * Generates and returns a 128-bit random hash.
     */
    public static byte[] generateRandomBytes() {
        byte[] bytes = new byte[8];
        random.nextBytes(bytes);
        return bytes;
    }


    /**
     * Converts a byte-hash into hexadecimal format.
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    public static void deleteFile(File fileToDelete) {
        try {
            FileDeleteStrategy.FORCE.delete(fileToDelete);
        } catch (IOException e) {
            fileToDelete.delete();
        }
    }


    public static long folderSize(File directory) {
        long length = 0;
        File[] subFiles = directory.listFiles();
        if (subFiles != null) {
            for (File f : subFiles) {
                length += f.isFile() ? f.length() : folderSize(f);
            }
        }
        return length;
    }


    public static String fileSizeToString(long size) {
        if (size <= 0) {
            return "0 kb";
        }

        final String[] units = new String[]{"bytes", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


    public static long difference(long n1, long n2) {
        long result = n1 - n2;
        return result >= 0 ? result : -result;
    }


    public static int difference(int n1, int n2) {
        int result = n1 - n2;
        return result >= 0 ? result : -result;
    }
}
