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

package com.edduarte.vokter.parser;

import com.edduarte.vokter.stemmer.Stemmer;
import com.edduarte.vokter.stopper.Stopper;
import it.unimi.dsi.lang.MutableString;

import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public interface Parser extends AutoCloseable {

    /**
     * Parses the specified text and obtains parsed results.
     */
    default List<Result> parse(MutableString text) {
        return parse(text, null, null, false);
    }

    /**
     * Parses the specified text by using the specified stopwords and stemmer, and
     * obtains parsed results.
     */
    List<Result> parse(final MutableString text,
                       final Stopper stopper,
                       final Stemmer stemmer,
                       final boolean ignoreCase);

    @Override
    void close();


    /**
     * Represents a parsing result, providing access to a token's phrase
     * position, start position, end position and text.
     */
    public static class Result {

        public int wordNum;

        public int start;

        public int end;

        public MutableString text;


        Result(final int wordNum,
               final int start,
               final int end,
               final MutableString text) {
            this.wordNum = wordNum;
            this.start = start;
            this.end = end;
            this.text = text;
        }
    }

}
