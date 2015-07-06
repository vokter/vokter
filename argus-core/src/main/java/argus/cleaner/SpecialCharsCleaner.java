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

package argus.cleaner;

import it.unimi.dsi.lang.MutableString;

/**
 * Simple cleaner class that filters some common special, non-informative
 * characters. The filtered characters are replaced by whitespaces (optimizing
 * the Tokenizer results).
 * <p>
 * As a rule, this clean will only clear characters that do not provide much
 * information on their own, like quotation-marks and bullet-points, for example.
 * This clean, however, will NOT clean characters that provide mathematical
 * information, like ½, π, µ and φ.
 *
 * @author Ed Duarte (<a href="mailto:edmiguelduarte@gmail.com">edmiguelduarte@gmail.com</a>)
 * @version 2.0.0
 * @since 1.0.0
 */
public class SpecialCharsCleaner implements Cleaner {

    /**
     * The characters to evaluate and clean from the provided document text.
     */
    private static final char[] CHARS_TO_FILTER = {
            '{', '}', '[', ']', '(', ')', '*', '/', '^', '~', '<', '>',
            '_', '…', '–', '−', '.', ',', '!', '?', '@', '#', '&', '+', '-', '=',
            '/', ':', ';', '\\', '|', '\"', '\'', '”', '“', '„', '«',
            '»', '’', '‘', '′', '⏐', '•', '↔', '►', '', '', '', '', '',
            '', '', '●', '®', '¶', '♦', '→', '·', '·', '▪', '○', '', '',
            '', '', '†', '', '║', '▲', '´', '\n'};

    private static final char[] DEFAULT_REPLACEMENTS;

    static {
        int numOfChars = CHARS_TO_FILTER.length;
        DEFAULT_REPLACEMENTS = new char[numOfChars];
        for (int i = 0; i < numOfChars; i++) {
            DEFAULT_REPLACEMENTS[i] = ' ';
        }
    }

    private final char[] replacements;


    public SpecialCharsCleaner() {
        this.replacements = DEFAULT_REPLACEMENTS;
    }


    public SpecialCharsCleaner(char replacement) {
        int numOfChars = CHARS_TO_FILTER.length;
        this.replacements = new char[numOfChars];
        for (int i = 0; i < numOfChars; i++) {
            this.replacements[i] = replacement;
        }
    }


    @Override
    public void clean(MutableString documentContents) {
        // the replace implementation of mutable strings use 'indexOf' instead
        // of 'split' to perform character lookup, since char-by-char matching
        // is considerably faster than regex-pattern matching
        documentContents.replace(CHARS_TO_FILTER, replacements);
    }
}
