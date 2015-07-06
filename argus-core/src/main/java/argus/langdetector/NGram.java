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

import java.lang.Character.UnicodeBlock;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cut out N-gram from text.
 *
 * @author Nakatani Shuyo
 */
class NGram {

    public final static int N_GRAM = 3;

    public static final HashMap<Character, Character> cjk_map = new HashMap<>();

    /**
     * CJK Kanji Normalization Mapping
     */
    private static final String[] CJK_CLASS = {
            LanguageDetectorMessages.getString("NGram.KANJI_1_0"),
            LanguageDetectorMessages.getString("NGram.KANJI_1_2"),
            LanguageDetectorMessages.getString("NGram.KANJI_1_4"),
            LanguageDetectorMessages.getString("NGram.KANJI_1_8"),
            LanguageDetectorMessages.getString("NGram.KANJI_1_11"),
            LanguageDetectorMessages.getString("NGram.KANJI_1_12"),
            LanguageDetectorMessages.getString("NGram.KANJI_1_13"),
            LanguageDetectorMessages.getString("NGram.KANJI_1_14"),
            LanguageDetectorMessages.getString("NGram.KANJI_1_16"),
            LanguageDetectorMessages.getString("NGram.KANJI_1_18"),
            LanguageDetectorMessages.getString("NGram.KANJI_1_22"),
            LanguageDetectorMessages.getString("NGram.KANJI_1_27"),
            LanguageDetectorMessages.getString("NGram.KANJI_1_29"),
            LanguageDetectorMessages.getString("NGram.KANJI_1_31"),
            LanguageDetectorMessages.getString("NGram.KANJI_1_35"),
            LanguageDetectorMessages.getString("NGram.KANJI_2_0"),
            LanguageDetectorMessages.getString("NGram.KANJI_2_1"),
            LanguageDetectorMessages.getString("NGram.KANJI_2_4"),
            LanguageDetectorMessages.getString("NGram.KANJI_2_9"),
            LanguageDetectorMessages.getString("NGram.KANJI_2_10"),
            LanguageDetectorMessages.getString("NGram.KANJI_2_11"),
            LanguageDetectorMessages.getString("NGram.KANJI_2_12"),
            LanguageDetectorMessages.getString("NGram.KANJI_2_13"),
            LanguageDetectorMessages.getString("NGram.KANJI_2_15"),
            LanguageDetectorMessages.getString("NGram.KANJI_2_16"),
            LanguageDetectorMessages.getString("NGram.KANJI_2_18"),
            LanguageDetectorMessages.getString("NGram.KANJI_2_21"),
            LanguageDetectorMessages.getString("NGram.KANJI_2_22"),
            LanguageDetectorMessages.getString("NGram.KANJI_2_23"),
            LanguageDetectorMessages.getString("NGram.KANJI_2_28"),
            LanguageDetectorMessages.getString("NGram.KANJI_2_29"),
            LanguageDetectorMessages.getString("NGram.KANJI_2_30"),
            LanguageDetectorMessages.getString("NGram.KANJI_2_31"),
            LanguageDetectorMessages.getString("NGram.KANJI_2_32"),
            LanguageDetectorMessages.getString("NGram.KANJI_2_35"),
            LanguageDetectorMessages.getString("NGram.KANJI_2_36"),
            LanguageDetectorMessages.getString("NGram.KANJI_2_37"),
            LanguageDetectorMessages.getString("NGram.KANJI_2_38"),
            LanguageDetectorMessages.getString("NGram.KANJI_3_1"),
            LanguageDetectorMessages.getString("NGram.KANJI_3_2"),
            LanguageDetectorMessages.getString("NGram.KANJI_3_3"),
            LanguageDetectorMessages.getString("NGram.KANJI_3_4"),
            LanguageDetectorMessages.getString("NGram.KANJI_3_5"),
            LanguageDetectorMessages.getString("NGram.KANJI_3_8"),
            LanguageDetectorMessages.getString("NGram.KANJI_3_9"),
            LanguageDetectorMessages.getString("NGram.KANJI_3_11"),
            LanguageDetectorMessages.getString("NGram.KANJI_3_12"),
            LanguageDetectorMessages.getString("NGram.KANJI_3_13"),
            LanguageDetectorMessages.getString("NGram.KANJI_3_15"),
            LanguageDetectorMessages.getString("NGram.KANJI_3_16"),
            LanguageDetectorMessages.getString("NGram.KANJI_3_18"),
            LanguageDetectorMessages.getString("NGram.KANJI_3_19"),
            LanguageDetectorMessages.getString("NGram.KANJI_3_22"),
            LanguageDetectorMessages.getString("NGram.KANJI_3_23"),
            LanguageDetectorMessages.getString("NGram.KANJI_3_27"),
            LanguageDetectorMessages.getString("NGram.KANJI_3_29"),
            LanguageDetectorMessages.getString("NGram.KANJI_3_30"),
            LanguageDetectorMessages.getString("NGram.KANJI_3_31"),
            LanguageDetectorMessages.getString("NGram.KANJI_3_32"),
            LanguageDetectorMessages.getString("NGram.KANJI_3_35"),
            LanguageDetectorMessages.getString("NGram.KANJI_3_36"),
            LanguageDetectorMessages.getString("NGram.KANJI_3_37"),
            LanguageDetectorMessages.getString("NGram.KANJI_3_38"),
            LanguageDetectorMessages.getString("NGram.KANJI_4_0"),
            LanguageDetectorMessages.getString("NGram.KANJI_4_9"),
            LanguageDetectorMessages.getString("NGram.KANJI_4_10"),
            LanguageDetectorMessages.getString("NGram.KANJI_4_16"),
            LanguageDetectorMessages.getString("NGram.KANJI_4_17"),
            LanguageDetectorMessages.getString("NGram.KANJI_4_18"),
            LanguageDetectorMessages.getString("NGram.KANJI_4_22"),
            LanguageDetectorMessages.getString("NGram.KANJI_4_24"),
            LanguageDetectorMessages.getString("NGram.KANJI_4_28"),
            LanguageDetectorMessages.getString("NGram.KANJI_4_34"),
            LanguageDetectorMessages.getString("NGram.KANJI_4_39"),
            LanguageDetectorMessages.getString("NGram.KANJI_5_10"),
            LanguageDetectorMessages.getString("NGram.KANJI_5_11"),
            LanguageDetectorMessages.getString("NGram.KANJI_5_12"),
            LanguageDetectorMessages.getString("NGram.KANJI_5_13"),
            LanguageDetectorMessages.getString("NGram.KANJI_5_14"),
            LanguageDetectorMessages.getString("NGram.KANJI_5_18"),
            LanguageDetectorMessages.getString("NGram.KANJI_5_26"),
            LanguageDetectorMessages.getString("NGram.KANJI_5_29"),
            LanguageDetectorMessages.getString("NGram.KANJI_5_34"),
            LanguageDetectorMessages.getString("NGram.KANJI_5_39"),
            LanguageDetectorMessages.getString("NGram.KANJI_6_0"),
            LanguageDetectorMessages.getString("NGram.KANJI_6_3"),
            LanguageDetectorMessages.getString("NGram.KANJI_6_9"),
            LanguageDetectorMessages.getString("NGram.KANJI_6_10"),
            LanguageDetectorMessages.getString("NGram.KANJI_6_11"),
            LanguageDetectorMessages.getString("NGram.KANJI_6_12"),
            LanguageDetectorMessages.getString("NGram.KANJI_6_16"),
            LanguageDetectorMessages.getString("NGram.KANJI_6_18"),
            LanguageDetectorMessages.getString("NGram.KANJI_6_20"),
            LanguageDetectorMessages.getString("NGram.KANJI_6_21"),
            LanguageDetectorMessages.getString("NGram.KANJI_6_22"),
            LanguageDetectorMessages.getString("NGram.KANJI_6_23"),
            LanguageDetectorMessages.getString("NGram.KANJI_6_25"),
            LanguageDetectorMessages.getString("NGram.KANJI_6_28"),
            LanguageDetectorMessages.getString("NGram.KANJI_6_29"),
            LanguageDetectorMessages.getString("NGram.KANJI_6_30"),
            LanguageDetectorMessages.getString("NGram.KANJI_6_32"),
            LanguageDetectorMessages.getString("NGram.KANJI_6_34"),
            LanguageDetectorMessages.getString("NGram.KANJI_6_35"),
            LanguageDetectorMessages.getString("NGram.KANJI_6_37"),
            LanguageDetectorMessages.getString("NGram.KANJI_6_39"),
            LanguageDetectorMessages.getString("NGram.KANJI_7_0"),
            LanguageDetectorMessages.getString("NGram.KANJI_7_3"),
            LanguageDetectorMessages.getString("NGram.KANJI_7_6"),
            LanguageDetectorMessages.getString("NGram.KANJI_7_7"),
            LanguageDetectorMessages.getString("NGram.KANJI_7_9"),
            LanguageDetectorMessages.getString("NGram.KANJI_7_11"),
            LanguageDetectorMessages.getString("NGram.KANJI_7_12"),
            LanguageDetectorMessages.getString("NGram.KANJI_7_13"),
            LanguageDetectorMessages.getString("NGram.KANJI_7_16"),
            LanguageDetectorMessages.getString("NGram.KANJI_7_18"),
            LanguageDetectorMessages.getString("NGram.KANJI_7_19"),
            LanguageDetectorMessages.getString("NGram.KANJI_7_20"),
            LanguageDetectorMessages.getString("NGram.KANJI_7_21"),
            LanguageDetectorMessages.getString("NGram.KANJI_7_23"),
            LanguageDetectorMessages.getString("NGram.KANJI_7_25"),
            LanguageDetectorMessages.getString("NGram.KANJI_7_28"),
            LanguageDetectorMessages.getString("NGram.KANJI_7_29"),
            LanguageDetectorMessages.getString("NGram.KANJI_7_32"),
            LanguageDetectorMessages.getString("NGram.KANJI_7_33"),
            LanguageDetectorMessages.getString("NGram.KANJI_7_35"),
            LanguageDetectorMessages.getString("NGram.KANJI_7_37"),
    };

    private static final String LATIN1_EXCLUDED = LanguageDetectorMessages.getString("NGram.LATIN1_EXCLUDE");

    private static final String[] NORMALIZED_VI_CHARS = {
            LanguageDetectorMessages.getString("NORMALIZED_VI_CHARS_0300"),
            LanguageDetectorMessages.getString("NORMALIZED_VI_CHARS_0301"),
            LanguageDetectorMessages.getString("NORMALIZED_VI_CHARS_0303"),
            LanguageDetectorMessages.getString("NORMALIZED_VI_CHARS_0309"),
            LanguageDetectorMessages.getString("NORMALIZED_VI_CHARS_0323")};

    private static final String TO_NORMALIZE_VI_CHARS = LanguageDetectorMessages.getString("TO_NORMALIZE_VI_CHARS");

    private static final String DMARK_CLASS = LanguageDetectorMessages.getString("DMARK_CLASS");

    private static final Pattern ALPHABET_WITH_DMARK = Pattern.compile("([" + TO_NORMALIZE_VI_CHARS + "])(["
            + DMARK_CLASS + "])");

    static {
        for (String cjk_list : CJK_CLASS) {
            char representative = cjk_list.charAt(0);
            for (int i = 0; i < cjk_list.length(); ++i) {
                cjk_map.put(cjk_list.charAt(i), representative);
            }
        }
    }

    private StringBuffer grams_;

    private boolean capitalword_;


    /**
     * Constructor.
     */
    public NGram() {
        grams_ = new StringBuffer(" ");
        capitalword_ = false;
    }


    /**
     * Character Normalization
     *
     * @param ch
     * @return Normalized character
     */
    static public char normalize(char ch) {
        UnicodeBlock block = UnicodeBlock.of(ch);
        if (block == UnicodeBlock.BASIC_LATIN) {
            if (ch < 'A' || (ch < 'a' && ch > 'Z') || ch > 'z') ch = ' ';
        } else if (block == UnicodeBlock.LATIN_1_SUPPLEMENT) {
            if (LATIN1_EXCLUDED.indexOf(ch) >= 0) ch = ' ';
        } else if (block == UnicodeBlock.LATIN_EXTENDED_B) {
            // normalization for Romanian
            if (ch == '\u0219')
                ch = '\u015f';  // Small S with comma below => with cedilla
            if (ch == '\u021b')
                ch = '\u0163';  // Small T with comma below => with cedilla
        } else if (block == UnicodeBlock.GENERAL_PUNCTUATION) {
            ch = ' ';
        } else if (block == UnicodeBlock.ARABIC) {
            if (ch == '\u06cc') ch = '\u064a';  // Farsi yeh => Arabic yeh
        } else if (block == UnicodeBlock.LATIN_EXTENDED_ADDITIONAL) {
            if (ch >= '\u1ea0') ch = '\u1ec3';
        } else if (block == UnicodeBlock.HIRAGANA) {
            ch = '\u3042';
        } else if (block == UnicodeBlock.KATAKANA) {
            ch = '\u30a2';
        } else if (block == UnicodeBlock.BOPOMOFO || block == UnicodeBlock.BOPOMOFO_EXTENDED) {
            ch = '\u3105';
        } else if (block == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
            if (cjk_map.containsKey(ch)) ch = cjk_map.get(ch);
        } else if (block == UnicodeBlock.HANGUL_SYLLABLES) {
            ch = '\uac00';
        }
        return ch;
    }


    /**
     * Normalizer for Vietnamese.
     * Normalize Alphabet + Diacritical Mark(U+03xx) into U+1Exx .
     *
     * @param text
     * @return normalized text
     */
    public static CharSequence normalize_vi(CharSequence text) {
        Matcher m = ALPHABET_WITH_DMARK.matcher(text);
        StringBuffer buf = new StringBuffer();
        while (m.find()) {
            int alphabet = TO_NORMALIZE_VI_CHARS.indexOf(m.group(1));
            int dmark = DMARK_CLASS.indexOf(m.group(2)); // Diacritical Mark
            m.appendReplacement(buf, NORMALIZED_VI_CHARS[dmark].substring(alphabet, alphabet + 1));
        }
        if (buf.length() == 0)
            return text;
        m.appendTail(buf);
        return buf.toString();
    }


    /**
     * Append a character into ngram buffer.
     *
     * @param ch
     */
    public void addChar(char ch) {
        ch = normalize(ch);
        char lastchar = grams_.charAt(grams_.length() - 1);
        if (lastchar == ' ') {
            grams_ = new StringBuffer(" ");
            capitalword_ = false;
            if (ch == ' ') return;
        } else if (grams_.length() >= N_GRAM) {
            grams_.deleteCharAt(0);
        }
        grams_.append(ch);

        if (Character.isUpperCase(ch)) {
            if (Character.isUpperCase(lastchar)) capitalword_ = true;
        } else {
            capitalword_ = false;
        }
    }


    /**
     * Get n-Gram
     *
     * @param n length of n-gram
     * @return n-Gram String (null if it is invalid)
     */
    public String get(int n) {
        if (capitalword_) return null;
        int len = grams_.length();
        if (n < 1 || n > 3 || len < n) return null;
        if (n == 1) {
            char ch = grams_.charAt(len - 1);
            if (ch == ' ') return null;
            return Character.toString(ch);
        } else {
            return grams_.substring(len - n, len);
        }
    }

}
