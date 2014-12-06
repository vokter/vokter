package argus.langdetect;

import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static argus.langdetect.LanguageDetectorException.ErrorCode;

/**
 * Language Detector Factory Class
 * <p/>
 * This class manages an initialization and constructions of {@link LanguageDetector}.
 * <p/>
 * Before using language detection library,
 * load profiles with {@link LanguageDetectorFactory#loadProfile(String)} method
 * and set initialization parameters.
 * <p/>
 * When the language detection,
 * construct Detector instance via {@link LanguageDetectorFactory#create()}.
 * See also {@link LanguageDetector}'s sample code.
 * <p/>
 * <ul>
 * <li>4x faster improvement based on Elmer Garduno's code. Thanks!</li>
 * </ul>
 *
 * @author Nakatani Shuyo
 * @see LanguageDetector
 */
public class LanguageDetectorFactory {

    private static LanguageDetectorFactory instance_ = new LanguageDetectorFactory();

    Map<String, double[]> wordLangProbMap;
    List<String> langList;
    Long seed = null;

    private LanguageDetectorFactory() {
        wordLangProbMap = new HashMap<>();
        langList = new ArrayList<>();
    }

    /**
     * Load profiles from specified directory.
     * This method must be called once before language detection.
     *
     * @param profileDirectory profile directory path
     * @throws LanguageDetectorException Can't open profiles(error code = {@link ErrorCode#FileLoadError})
     *                                   or profile's format is wrong (error code = {@link ErrorCode#FormatError})
     */
    public static void loadProfile(String profileDirectory) throws LanguageDetectorException {
        loadProfile(new File(profileDirectory));
    }

    /**
     * Load profiles from specified directory.
     * This method must be called once before language detection.
     *
     * @param profileDirectory profile directory path
     * @throws LanguageDetectorException Can't open profiles(error code = {@link ErrorCode#FileLoadError})
     *                                   or profile's format is wrong (error code = {@link ErrorCode#FormatError})
     */
    public static void loadProfile(File profileDirectory) throws LanguageDetectorException {
        File[] listFiles = profileDirectory.listFiles();
        if (listFiles == null)
            throw new LanguageDetectorException(ErrorCode.NeedLoadProfileError, "Not found profile: " + profileDirectory);

        int langsize = listFiles.length, index = 0;
        for (File file : listFiles) {
            if (file.getName().startsWith(".") || !file.isFile()) continue;
            FileInputStream is = null;
            try {
                is = new FileInputStream(file);
                LanguageProfile profile = JSON.decode(is, LanguageProfile.class);
                addProfile(profile, index, langsize);
                ++index;
            } catch (JSONException e) {
                throw new LanguageDetectorException(ErrorCode.FormatError, "profile format error in '" + file.getName() + "'");
            } catch (IOException e) {
                throw new LanguageDetectorException(ErrorCode.FileLoadError, "can't open '" + file.getName() + "'");
            } finally {
                try {
                    if (is != null) is.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Load profiles from specified directory.
     * This method must be called once before language detection.
     *
     * @throws LanguageDetectorException Can't open profiles(error code = {@link ErrorCode#FileLoadError})
     *                                   or profile's format is wrong (error code = {@link ErrorCode#FormatError})
     */
    public static void loadProfile(List<String> json_profiles) throws LanguageDetectorException {
        int index = 0;
        int langSize = json_profiles.size();
        if (langSize < 2)
            throw new LanguageDetectorException(ErrorCode.NeedLoadProfileError, "Need more than 2 profiles");

        for (String json : json_profiles) {
            try {
                LanguageProfile profile = JSON.decode(json, LanguageProfile.class);
                addProfile(profile, index, langSize);
                index++;
            } catch (JSONException e) {
                throw new LanguageDetectorException(ErrorCode.FormatError, "profile format error");
            }
        }
    }

    /**
     * @param profile
     * @param langsize
     * @param index
     * @throws LanguageDetectorException
     */
    public static void addProfile(LanguageProfile profile, int index, int langsize) throws LanguageDetectorException {
        String lang = profile.name;
        if (instance_.langList.contains(lang)) {
            throw new LanguageDetectorException(ErrorCode.DuplicateLangError, "duplicate the same language profile");
        }
        instance_.langList.add(lang);
        for (String word : profile.freq.keySet()) {
            if (!instance_.wordLangProbMap.containsKey(word)) {
                instance_.wordLangProbMap.put(word, new double[langsize]);
            }
            int length = word.length();
            if (length >= 1 && length <= 3) {
                double prob = profile.freq.get(word).doubleValue() / profile.n_words[length - 1];
                instance_.wordLangProbMap.get(word)[index] = prob;
            }
        }
    }

    /**
     * Clear loaded language profiles (reinitialization to be available)
     */
    public static void clear() {
        instance_.langList.clear();
        instance_.wordLangProbMap.clear();
    }

    /**
     * Construct Detector instance
     *
     * @return Detector instance
     * @throws LanguageDetectorException
     */
    public static LanguageDetector create() throws LanguageDetectorException {
        if (instance_.langList.size() == 0) {
            throw new LanguageDetectorException(ErrorCode.NeedLoadProfileError, "need to load profiles");
        }
        return new LanguageDetector(instance_);
    }

    /**
     * Construct Detector instance with smoothing parameter
     *
     * @param alpha smoothing parameter (default value = 0.5)
     * @return Detector instance
     * @throws LanguageDetectorException
     */
    public static LanguageDetector create(double alpha) throws LanguageDetectorException {
        LanguageDetector detector = create();
        detector.setAlpha(alpha);
        return detector;
    }

    public static void setSeed(long seed) {
        instance_.seed = seed;
    }

    public static final List<String> getLangList() {
        return Collections.unmodifiableList(instance_.langList);
    }
}
