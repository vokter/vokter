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

import it.unimi.dsi.lang.MutableString;

/**
 * Processes char by char textual content from the received textual content. A
 * cleaner should analyze a character and either replace it or remove it. The
 * main idea behind cleaners is that they can be coupled together using
 * AndCleaner, but the cleaning algorithm is done on a single-pass. Regardless
 * of how many cleaners were coupled, all cleaning logic is running at O(n).
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 2.0.0
 * @since 1.0.0
 */
public abstract class Cleaner {


    public final void clean(MutableString s) {
        setup(s);
        // cleaner implementation use char lookup since char-by-char matching is
        // considerably faster than regex-pattern matching
        char last = ' ';
        int i = 0;
        while (i < s.length()) {
            char curr = s.charAt(i);
            boolean shouldDelete = clean(s, i, last, curr);
            if (shouldDelete) {
                s.deleteCharAt(i);
            } else {
                // assume that the current character might have been updated, so
                // update the current character reference
                last = s.charAt(i);
                i++;
            }
        }
    }


    protected abstract void setup(MutableString s);


    /**
     * Cleans the current char, replacing it or deleting it. If it was deleted,
     * returns true.
     */
    protected abstract boolean clean(MutableString s, int i, char last, char curr);

}
