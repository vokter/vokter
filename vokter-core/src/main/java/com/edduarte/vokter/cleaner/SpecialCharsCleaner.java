/*
 * Copyright 2015 Eduardo Duarte
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

package com.edduarte.vokter.cleaner;

import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.lang.MutableString;

/**
 * Simple cleaner class that filters some common special, non-informative
 * characters. The filtered characters are replaced by whitespaces (optimizing
 * SimpleParser results).
 * <p>
 * As a rule, this cleaner will only clear characters that do not provide much
 * information on their own, like quotation-marks and bullet-points, for
 * example. This cleaner, however, will NOT clean characters that provide
 * mathematical information, like ½, π, µ and φ.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class SpecialCharsCleaner extends Cleaner {

    /**
     * The characters to evaluate and clean from the provided document text.
     */
    private static final CharList CHARS_TO_FILTER = new CharArrayList(new char[]{
            '{', '}', '[', ']', '(', ')', '*', '/', '^', '~', '<', '>',
            '_', '…', '–', '−', '.', ',', '!', '?', '@', '#', '&', '+', '-', '=',
            '/', ':', ';', '\\', '|', '\"', '\'', '”', '“', '„', '«',
            '»', '’', '‘', '′', '⏐', '•', '↔', '►', '', '', '', '', '',
            '', '', '●', '®', '¶', '♦', '→', '·', '·', '▪', '○', '', '',
            '', '', '†', '', '║', '▲', '´', '\n', '\r'});

    private static final char DEFAULT_REPLACEMENTS = ' ';

    private final char replacement;


    public SpecialCharsCleaner() {
        this.replacement = DEFAULT_REPLACEMENTS;
    }


    public SpecialCharsCleaner(char replacement) {
        this.replacement = replacement;
    }


    @Override
    protected void setup(MutableString s) {
    }


    @Override
    protected boolean clean(MutableString s, int i, char last, char curr) {
        if (CHARS_TO_FILTER.contains(curr)) s.setCharAt(i, replacement);
        return false;
    }
}
