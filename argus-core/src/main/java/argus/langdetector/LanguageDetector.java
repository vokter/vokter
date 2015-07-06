/*
 * Copyright 2014 Ed Duarte
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

package argus.langdetector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.lang.Character.UnicodeBlock;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import static argus.langdetector.LanguageDetectorException.ErrorCode;

/**
 * {@link LanguageDetector} class is to detect language from specified text.
 * Its instance is able to be constructed via the factory class
 * {@link LanguageDetectorFactory}.
 * <p>
 * After appending a target text to the {@link LanguageDetector} instance with
 * {@link #append(java.io.Reader)} or {@link #append(CharSequence)},
 * the detector provides the language detection results for target text via {@link #detect()} or {@link #getProbabilities()}.
 * {@link #detect()} method returns a single language name which has the highest probability.
 * {@link #getProbabilities()} methods returns a list of multiple languages and their probabilities.
 * <p>
 * <ul>
 * <li>4x faster improvement based on Elmer Garduno's code. Thanks!</li>
 * </ul>
 *
 * @author Nakatani Shuyo
 * @see LanguageDetectorFactory
 */
public class LanguageDetector {

    private static final Logger logger = LoggerFactory.getLogger(LanguageDetector.class);

    private static final double ALPHA_DEFAULT = 0.5;

    private static final double ALPHA_WIDTH = 0.05;

    private static final int ITERATION_LIMIT = 1000;

    private static final double PROB_THRESHOLD = 0.1;

    private static final double CONV_THRESHOLD = 0.99999;

    private static final int BASE_FREQ = 10000;

    private static final String UNKNOWN_LANG = "unknown";

    private static final Pattern URL_REGEX = Pattern.compile("https?://[-_.?&~;+=/#0-9A-Za-z]{1,2076}");

    private static final Pattern MAIL_REGEX = Pattern.compile("[-_.0-9A-Za-z]{1,64}@[-_0-9A-Za-z]{1,255}[-_.0-9A-Za-z]{1,255}");

    private final Map<String, double[]> wordLangProbMap;

    private final List<String> langList;

    private StringBuffer text;

    private double[] langprob = null;

    private double alpha = ALPHA_DEFAULT;

    private int n_trial = 7;

    private int max_text_length = 10000;

    private double[] priorMap = null;

    private boolean verbose = false;

    private Long seed = null;


    /**
     * Constructor.
     * Detector instance can be constructed via {@link LanguageDetectorFactory#create()}.
     *
     * @param factory {@link LanguageDetectorFactory} instance (only DetectorFactory inside)
     */
    public LanguageDetector(LanguageDetectorFactory factory) {
        this.wordLangProbMap = factory.wordLangProbMap;
        this.langList = factory.langList;
        this.text = new StringBuffer();
        this.seed = factory.seed;
    }


    /**
     * normalize probabilities and check convergence by the maximum probability
     *
     * @return maximum of probabilities
     */
    static private double normalizeProb(double[] prob) {
        double maxp = 0, sump = 0;
        for (int i = 0; i < prob.length; ++i) sump += prob[i];
        for (int i = 0; i < prob.length; ++i) {
            double p = prob[i] / sump;
            if (maxp < p) maxp = p;
            prob[i] = p;
        }
        return maxp;
    }


    /**
     * unicode encoding (for verbose mode)
     */
    static private String unicodeEncode(String word) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < word.length(); ++i) {
            char ch = word.charAt(i);
            if (ch >= '\u0080') {
                String st = Integer.toHexString(0x10000 + (int) ch);
                while (st.length() < 4) st = "0" + st;
                buf.append("\\u").append(st.subSequence(1, 5));
            } else {
                buf.append(ch);
            }
        }
        return buf.toString();
    }


    /**
     * Set Verbose Mode(use for debug).
     */
    public void setVerbose() {
        this.verbose = true;
    }


    /**
     * Set smoothing parameter.
     * The default value is 0.5(i.e. Expected Likelihood Estimate).
     *
     * @param alpha the smoothing parameter
     */
    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }


    /**
     * Set prior information about language probabilities.
     *
     * @param priorMap the priorMap to set
     * @throws LanguageDetectorException
     */
    public void setPriorMap(HashMap<String, Double> priorMap) throws LanguageDetectorException {
        this.priorMap = new double[langList.size()];
        double sump = 0;
        for (int i = 0; i < this.priorMap.length; ++i) {
            String lang = langList.get(i);
            if (priorMap.containsKey(lang)) {
                double p = priorMap.get(lang);
                if (p < 0)
                    throw new LanguageDetectorException(ErrorCode.InitParamError,
                            "Prior probability must be non-negative.");
                this.priorMap[i] = p;
                sump += p;
            }
        }
        if (sump <= 0)
            throw new LanguageDetectorException(ErrorCode.InitParamError,
                    "More one of prior probability must be non-zero.");
        for (int i = 0; i < this.priorMap.length; ++i) {
            this.priorMap[i] /= sump;
        }
    }


    /**
     * Specify max size of target text to use for language detection.
     * The default value is 10000(10KB).
     *
     * @param max_text_length the max_text_length to set
     */
    public void setMaxTextLength(int max_text_length) {
        this.max_text_length = max_text_length;
    }


    /**
     * Append the target text for language detection.
     * This method read the text from specified input reader.
     * If the total size of target text exceeds the limit size specified by {@link LanguageDetector#setMaxTextLength(int)},
     * the rest is cut down.
     *
     * @param reader the input reader (BufferedReader as usual)
     * @throws java.io.IOException Can't read the reader.
     */
    public void append(Reader reader) throws IOException {
        char[] buf = new char[max_text_length / 2];
        while (text.length() < max_text_length && reader.ready()) {
            int length = reader.read(buf);
            append(new String(buf, 0, length));
        }
    }


    /**
     * Append the target text for language detection.
     * If the total size of target text exceeds the limit size specified by {@link LanguageDetector#setMaxTextLength(int)},
     * the rest is cut down.
     *
     * @param text the target text to append
     */
    public void append(CharSequence text) {
        text = URL_REGEX.matcher(text).replaceAll(" ");
        text = MAIL_REGEX.matcher(text).replaceAll(" ");
        text = NGram.normalize_vi(text);
        char pre = 0;
        for (int i = 0; i < text.length() && i < max_text_length; ++i) {
            char c = text.charAt(i);
            if (c != ' ' || pre != ' ') this.text.append(c);
            pre = c;
        }
    }


    /**
     * Cleaning text to detect
     * (eliminate URL, e-mail address and Latin sentence if it is not written in Latin alphabet)
     */
    private void cleaningText() {
        int latinCount = 0, nonLatinCount = 0;
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (c <= 'z' && c >= 'A') {
                ++latinCount;
            } else if (c >= '\u0300' &&
                    UnicodeBlock.of(c) != UnicodeBlock.LATIN_EXTENDED_ADDITIONAL) {
                ++nonLatinCount;
            }
        }
        if (latinCount * 2 < nonLatinCount) {
            StringBuffer textWithoutLatin = new StringBuffer();
            for (int i = 0; i < text.length(); ++i) {
                char c = text.charAt(i);
                if (c > 'z' || c < 'A') textWithoutLatin.append(c);
            }
            text = textWithoutLatin;
        }
    }


    /**
     * Detect language of the target text and return the language name which has the highest probability.
     *
     * @return detected language name which has most probability.
     * @throws LanguageDetectorException code = ErrorCode.CantDetectError : Can't detect because of no valid features in text
     */
    public String detect() throws LanguageDetectorException {
        List<Language> probabilities = getProbabilities();
        if (probabilities.size() > 0) {
            return probabilities.get(0).lang;
        }
        return UNKNOWN_LANG;
    }


    /**
     * Get language candidates which have high probabilities
     *
     * @return possible languages list (whose probabilities are over PROB_THRESHOLD, ordered by probabilities descendently
     * @throws LanguageDetectorException code = ErrorCode.CantDetectError : Can't detect because of no valid features in text
     */
    public List<Language> getProbabilities() throws LanguageDetectorException {
        if (langprob == null) {
            detectBlock();
        }
        return sortProbability(langprob);
    }


    private void detectBlock() throws LanguageDetectorException {
        cleaningText();
        List<String> ngrams = extractNGrams();
        if (ngrams.size() == 0) {
            throw new LanguageDetectorException(ErrorCode.CantDetectError, "no features in text");
        }

        langprob = new double[langList.size()];

        Random rand = new Random();
        if (seed != null) rand.setSeed(seed);
        for (int t = 0; t < n_trial; ++t) {
            double[] prob = initProbability();
            double alpha = this.alpha + rand.nextGaussian() * ALPHA_WIDTH;

            for (int i = 0; ; ++i) {
                int r = rand.nextInt(ngrams.size());
                updateLangProb(prob, ngrams.get(r), alpha);
                if (i % 5 == 0) {
                    if (normalizeProb(prob) > CONV_THRESHOLD || i >= ITERATION_LIMIT) {
                        break;
                    }
                    if (verbose) {
                        logger.info("> " + sortProbability(prob));
                    }
                }
            }
            for (int j = 0; j < langprob.length; ++j)
                langprob[j] += prob[j] / n_trial;
            if (verbose) {
                logger.info("==> " + sortProbability(prob));
            }
        }
    }


    /**
     * Initialize the map of language probabilities.
     * If there is the specified prior map, use it as initial map.
     *
     * @return initialized map of language probabilities
     */
    private double[] initProbability() {
        double[] prob = new double[langList.size()];
        if (priorMap != null) {
            for (int i = 0; i < prob.length; ++i) prob[i] = priorMap[i];
        } else {
            for (int i = 0; i < prob.length; ++i)
                prob[i] = 1.0 / langList.size();
        }
        return prob;
    }


    /**
     * Extract n-grams from target text
     *
     * @return n-grams list
     */
    private List<String> extractNGrams() {
        List<String> list = new ArrayList<>();
        NGram ngram = new NGram();
        for (int i = 0; i < text.length(); ++i) {
            ngram.addChar(text.charAt(i));
            for (int n = 1; n <= NGram.N_GRAM; ++n) {
                String w = ngram.get(n);
                if (w != null && wordLangProbMap.containsKey(w)) list.add(w);
            }
        }
        return list;
    }


    /**
     * update language probabilities with N-gram string(N=1,2,3)
     *
     * @param word N-gram string
     */
    private boolean updateLangProb(double[] prob, String word, double alpha) {
        if (word == null || !wordLangProbMap.containsKey(word)) return false;

        double[] langProbMap = wordLangProbMap.get(word);
        if (verbose) {
            logger.info(word + "(" + unicodeEncode(word) + "):" + wordProbToString(langProbMap));
        }

        double weight = alpha / BASE_FREQ;
        for (int i = 0; i < prob.length; ++i) {
            prob[i] *= weight + langProbMap[i];
        }
        return true;
    }


    private String wordProbToString(double[] prob) {
        Formatter formatter = new Formatter();
        for (int j = 0; j < prob.length; ++j) {
            double p = prob[j];
            if (p >= 0.00001) {
                formatter.format(" %s:%.5f", langList.get(j), p);
            }
        }
        String string = formatter.toString();
        formatter.close();
        return string;
    }


    /**
     * @return language candidates order by probabilities descendently
     */
    private List<Language> sortProbability(double[] prob) {
        List<Language> list = new ArrayList<>();
        for (int j = 0; j < prob.length; ++j) {
            double p = prob[j];
            if (p > PROB_THRESHOLD) {
                for (int i = 0; i <= list.size(); ++i) {
                    if (i == list.size() || list.get(i).prob < p) {
                        list.add(i, new Language(langList.get(j), p));
                        break;
                    }
                }
            }
        }
        return list;
    }

}
