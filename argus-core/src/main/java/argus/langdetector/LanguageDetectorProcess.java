package argus.langdetector;

import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * LangDetect Command Line Interface
 * <p>
 * This is a command line interface of Language Detection Library "LangDetect".
 *
 * @author Nakatani Shuyo
 */
public class LanguageDetectorProcess {

    private static final Logger logger = LoggerFactory.getLogger(LanguageDetectorProcess.class);
    /**
     * smoothing default parameter (ELE)
     */
    private static final double DEFAULT_ALPHA = 0.5;

    /**
     * for Command line easy parser
     */
    private Map<String, String> opt_with_value = new HashMap<>();
    private Map<String, String> values = new HashMap<>();
    private Set<String> opt_without_value = new HashSet<>();
    private List<String> arg_list = new ArrayList<>();

    /**
     * Command Line Interface
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        LanguageDetectorProcess process = new LanguageDetectorProcess();
        process.addOpt("-d", "directory", "./");
        process.addOpt("-a", "alpha", "" + DEFAULT_ALPHA);
        process.addOpt("-s", "seed", null);
        process.addOpt("-l", "lang", null);
        process.parse(args);

        if (process.hasOpt("--genprofile")) {
            process.generateProfile();
        } else if (process.hasOpt("--genprofile-text")) {
            process.generateProfileFromText();
        } else if (process.hasOpt("--detectlang")) {
            process.detectLang();
        } else if (process.hasOpt("--batchtest")) {
            process.batchTest();
        }
    }

    /**
     * Command line easy parser
     *
     * @param args command line arguments
     */
    private void parse(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            if (opt_with_value.containsKey(args[i])) {
                String key = opt_with_value.get(args[i]);
                values.put(key, args[i + 1]);
                ++i;
            } else if (args[i].startsWith("-")) {
                opt_without_value.add(args[i]);
            } else {
                arg_list.add(args[i]);
            }
        }
    }

    private void addOpt(String opt, String key, String value) {
        opt_with_value.put(opt, key);
        values.put(key, value);
    }

    private String get(String key) {
        return values.get(key);
    }

    private Long getLong(String key) {
        String value = values.get(key);
        if (value == null) return null;
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private double getDouble(String key, double defaultValue) {
        try {
            return Double.valueOf(values.get(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean hasOpt(String opt) {
        return opt_without_value.contains(opt);
    }

    /**
     * File search (easy glob)
     *
     * @param directory directory path
     * @param pattern   searching file pattern with regular representation
     * @return matched file
     */
    private File searchFile(File directory, String pattern) {
        for (File file : directory.listFiles()) {
            if (file.getName().matches(pattern)) return file;
        }
        return null;
    }

    /**
     * load profiles
     *
     * @return false if load success
     */
    private boolean loadProfile() {
        String profileDirectory = get("directory") + "/";
        try {
            LanguageDetectorFactory.loadProfile(profileDirectory);
            Long seed = getLong("seed");
            if (seed != null) {
                LanguageDetectorFactory.setSeed(seed);
            }
            return false;
        } catch (LanguageDetectorException e) {
            logger.error("ERROR: " + e.getMessage());
            return true;
        }
    }

    /**
     * Generate Language Profile from Wikipedia Abstract Database File
     * <p>
     * <pre>
     * usage: --genprofile -d [abstracts directory] [language names]
     * </pre>
     */
    public void generateProfile() {
        File directory = new File(get("directory"));
        for (String lang : arg_list) {
            File file = searchFile(directory, lang + "wiki-.*-abstract\\.xml.*");
            if (file == null) {
                logger.error("Not Found abstract xml : lang = " + lang);
                continue;
            }

            FileOutputStream os = null;
            try {
                LanguageProfile profile = LanguageProfileFactory.loadFromWikipediaAbstract(lang, file);
                profile.omitLessFreq();

                File profile_path = new File(get("directory") + "/profiles/" + lang);
                os = new FileOutputStream(profile_path);
                JSON.encode(profile, os);
            } catch (JSONException | IOException | LanguageDetectorException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (os != null) os.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * Generate Language Profile from Text File
     * <p>
     * <pre>
     * usage: --genprofile-text -l [language code] [text file path]
     * </pre>
     */
    private void generateProfileFromText() {
        if (arg_list.size() != 1) {
            logger.error("Need to specify text file path");
            return;
        }
        File file = new File(arg_list.get(0));
        if (!file.exists()) {
            logger.error("Need to specify existing text file path");
            return;
        }

        String lang = get("lang");
        if (lang == null) {
            logger.error("Need to specify langage code(-l)");
            return;
        }

        FileOutputStream os = null;
        try {
            LanguageProfile profile = LanguageProfileFactory.loadFromText(lang, file);
            profile.omitLessFreq();

            File profile_path = new File(lang);
            os = new FileOutputStream(profile_path);
            JSON.encode(profile, os);
        } catch (JSONException | LanguageDetectorException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) os.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Language detection test for each file (--detectlang option)
     * <p>
     * <pre>
     * usage: --detectlang -d [profile directory] -a [alpha] -s [seed] [test file(s)]
     * </pre>
     */
    public void detectLang() {
        if (loadProfile()) return;
        for (String filename : arg_list) {
            BufferedReader is = null;
            try {
                is = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "utf-8"));

                LanguageDetector detector = LanguageDetectorFactory.create(getDouble("alpha", DEFAULT_ALPHA));
                if (hasOpt("--debug")) detector.setVerbose();
                detector.append(is);
                logger.info(filename + ":" + detector.getProbabilities());
            } catch (IOException | LanguageDetectorException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) is.close();
                } catch (IOException ignored) {
                }
            }

        }
    }

    /**
     * Batch Test of Language Detection (--batchtest option)
     * <p>
     * <pre>
     * usage: --batchtest -d [profile directory] -a [alpha] -s [seed] [test data(s)]
     * </pre>
     * <p>
     * The format of test data(s):
     * <pre>
     *   [correct language name]\t[text body for test]\n
     * </pre>
     */
    public void batchTest() {
        if (loadProfile()) return;
        HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
        for (String filename : arg_list) {
            BufferedReader is = null;
            try {
                is = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "utf-8"));
                while (is.ready()) {
                    String line = is.readLine();
                    int idx = line.indexOf('\t');
                    if (idx <= 0) continue;
                    String correctLang = line.substring(0, idx);
                    String text = line.substring(idx + 1);

                    LanguageDetector detector = LanguageDetectorFactory.create(getDouble("alpha", DEFAULT_ALPHA));
                    detector.append(text);
                    String lang = "";
                    try {
                        lang = detector.detect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!result.containsKey(correctLang))
                        result.put(correctLang, new ArrayList<String>());
                    result.get(correctLang).add(lang);
                    if (hasOpt("--debug")) {
                        logger.info(correctLang + "," + lang + "," + (text.length() > 100 ? text.substring(0, 100) : text));
                    }
                }

            } catch (IOException | LanguageDetectorException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) is.close();
                } catch (IOException ignored) {
                }
            }

            List<String> langlist = new ArrayList<>(result.keySet());
            Collections.sort(langlist);

            int totalCount = 0, totalCorrect = 0;
            for (String lang : langlist) {
                Map<String, Integer> resultCount = new HashMap<>();
                int count = 0;
                List<String> list = result.get(lang);
                for (String detectedLang : list) {
                    ++count;
                    if (resultCount.containsKey(detectedLang)) {
                        resultCount.put(detectedLang, resultCount.get(detectedLang) + 1);
                    } else {
                        resultCount.put(detectedLang, 1);
                    }
                }
                int correct = resultCount.containsKey(lang) ? resultCount.get(lang) : 0;
                double rate = correct / (double) count;
                logger.info(String.format("%s (%d/%d=%.2f): %s", lang, correct, count, rate, resultCount));
                totalCorrect += correct;
                totalCount += count;
            }
            logger.info(String.format("total: %d/%d = %.3f", totalCorrect, totalCount, totalCorrect / (double) totalCount));

        }

    }

}
