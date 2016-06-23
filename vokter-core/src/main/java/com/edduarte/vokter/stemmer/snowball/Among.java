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

package com.edduarte.vokter.stemmer.snowball;

import java.lang.reflect.Method;

public class Among {

    public final int s_size; /* search string */

    public final char[] s; /* search string */

    public final int substring_i; /* index to longest matching substring */

    public final int result; /* result of the lookup */

    public final Method method; /* method to use if substring matches */

    public final SnowballStemmer methodobject; /* object to invoke method on */


    public Among(String s, int substring_i, int result,
                 String methodname, SnowballStemmer methodobject) {

        this.s_size = s.length();
        this.s = s.toCharArray();
        this.substring_i = substring_i;
        this.result = result;
        this.methodobject = methodobject;
        if (methodname.length() == 0) {
            this.method = null;
        } else {
            try {
                this.method = methodobject.getClass().
                        getDeclaredMethod(methodname, new Class[0]);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
