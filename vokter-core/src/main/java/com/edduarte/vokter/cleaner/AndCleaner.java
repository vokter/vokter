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

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.lang.MutableString;

import java.util.Arrays;
import java.util.List;

/**
 * Utility Cleaner implementation that concatenates two Cleaner classes into a
 * single reference.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class AndCleaner extends Cleaner {

    private final List<Cleaner> cleanerList;


    private AndCleaner(List<Cleaner> cleanerList) {
        this.cleanerList = cleanerList;
    }


    public static AndCleaner of(Cleaner f1, Cleaner f2) {
        return new AndCleaner(Arrays.asList(f1, f2));
    }


    public static AndCleaner of(Cleaner... f) {
        return new AndCleaner(Arrays.asList(f));
    }


    public static AndCleaner of(List<Cleaner> f) {
        return new AndCleaner(ImmutableList.copyOf(f));
    }


    @Override
    protected void setup(MutableString s) {
        for (Cleaner c : cleanerList) {
            c.setup(s);
        }
    }


    @Override
    protected boolean clean(MutableString s, int i, char last, char curr) {
        for (Cleaner c : cleanerList) {
            boolean shouldDelete = c.clean(s, i, last, curr);
            if (shouldDelete) {
                // if one cleaner asks the character to be deleted, don't pass
                // this character through the remaining cleaners
                return true;
            } else {
                // assume that the current character might have been updated,
                // so update the current character reference
                curr = s.charAt(i);
            }
        }
        return false;
    }
}
