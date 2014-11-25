package argus.langdetect;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import argus.langdetect.util.LangProfile;
import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;

import static argus.langdetect.LanguageDetectorException.*;

/**
 * Language Detector Factory Class
 * 
 * This class manages an initialization and constructions of {@link LanguageDetector}.
 * 
 * Before using language detection library, 
 * load profiles with {@link LanguageDetectorFactory#loadProfile(String)} method
 * and set initialization parameters.
 * 
 * When the language detection,
 * construct Detector instance via {@link LanguageDetectorFactory#create()}.
 * See also {@link LanguageDetector}'s sample code.
 * 
 * <ul>
 * <li>4x faster improvement based on Elmer Garduno's code. Thanks!</li>
 * </ul>
 * 
 * @see LanguageDetector
 * @author Nakatani Shuyo
 */
public class LanguageDetectorFactory {
    public HashMap<String, double[]> wordLangProbMap;
    public ArrayList<String> langlist;
    public Long seed = null;
    private LanguageDetectorFactory() {
        wordLangProbMap = new HashMap<String, double[]>();
        langlist = new ArrayList<String>();
    }
    static private LanguageDetectorFactory instance_ = new LanguageDetectorFactory();

    /**
     * Load profiles from specified directory.
     * This method must be called once before language detection.
     *  
     * @param profileDirectory profile directory path
     * @throws LanguageDetectorException  Can't open profiles(error code = {@link ErrorCode#FileLoadError})
     *                              or profile's format is wrong (error code = {@link ErrorCode#FormatError})
     */
    public static void loadProfile(String profileDirectory) throws LanguageDetectorException {
        loadProfile(new File(profileDirectory));
    }

    /**
     * Load profiles from specified directory.
     * This method must be called once before language detection.
     *  
     * @param profileDirectory profile directory path
     * @throws LanguageDetectorException  Can't open profiles(error code = {@link ErrorCode#FileLoadError})
     *                              or profile's format is wrong (error code = {@link ErrorCode#FormatError})
     */
    public static void loadProfile(File profileDirectory) throws LanguageDetectorException {
        File[] listFiles = profileDirectory.listFiles();
        if (listFiles == null)
            throw new LanguageDetectorException(ErrorCode.NeedLoadProfileError, "Not found profile: " + profileDirectory);
            
        int langsize = listFiles.length, index = 0;
        for (File file: listFiles) {
            if (file.getName().startsWith(".") || !file.isFile()) continue;
            FileInputStream is = null;
            try {
                is = new FileInputStream(file);
                LangProfile profile = JSON.decode(is, LangProfile.class);
                addProfile(profile, index, langsize);
                ++index;
            } catch (JSONException e) {
                throw new LanguageDetectorException(ErrorCode.FormatError, "profile format error in '" + file.getName() + "'");
            } catch (IOException e) {
                throw new LanguageDetectorException(ErrorCode.FileLoadError, "can't open '" + file.getName() + "'");
            } finally {
                try {
                    if (is!=null) is.close();
                } catch (IOException e) {}
            }
        }
    }

    /**
     * Load profiles from specified directory.
     * This method must be called once before language detection.
     *  
     * @throws LanguageDetectorException  Can't open profiles(error code = {@link ErrorCode#FileLoadError})
     *                              or profile's format is wrong (error code = {@link ErrorCode#FormatError})
     */
    public static void loadProfile(List<String> json_profiles) throws LanguageDetectorException {
        int index = 0;
        int langsize = json_profiles.size();
        if (langsize < 2)
            throw new LanguageDetectorException(ErrorCode.NeedLoadProfileError, "Need more than 2 profiles");
            
        for (String json: json_profiles) {
            try {
                LangProfile profile = JSON.decode(json, LangProfile.class);
                addProfile(profile, index, langsize);
                ++index;
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
    public static void addProfile(LangProfile profile, int index, int langsize) throws LanguageDetectorException {
        String lang = profile.name;
        if (instance_.langlist.contains(lang)) {
            throw new LanguageDetectorException(ErrorCode.DuplicateLangError, "duplicate the same language profile");
        }
        instance_.langlist.add(lang);
        for (String word: profile.freq.keySet()) {
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
    static public void clear() {
        instance_.langlist.clear();
        instance_.wordLangProbMap.clear();
    }

    /**
     * Construct Detector instance
     * 
     * @return Detector instance
     * @throws LanguageDetectorException
     */
    static public LanguageDetector create() throws LanguageDetectorException {
        return createDetector();
    }

    /**
     * Construct Detector instance with smoothing parameter 
     * 
     * @param alpha smoothing parameter (default value = 0.5)
     * @return Detector instance
     * @throws LanguageDetectorException
     */
    public static LanguageDetector create(double alpha) throws LanguageDetectorException {
        LanguageDetector detector = createDetector();
        detector.setAlpha(alpha);
        return detector;
    }

    static private LanguageDetector createDetector() throws LanguageDetectorException {
        if (instance_.langlist.size() == 0) {
            throw new LanguageDetectorException(ErrorCode.NeedLoadProfileError, "need to load profiles");
        }
        return new LanguageDetector(instance_);
    }
    
    public static void setSeed(long seed) {
        instance_.seed = seed;
    }
    
    public static final List<String> getLangList() {
        return Collections.unmodifiableList(instance_.langlist);
    }
}
