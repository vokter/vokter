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

package com.edduarte.vokter.stopper;

/**
 * Loader module that checks if the received textual state of a term corresponds
 * to a stopword.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public interface Stopper extends
        java.util.function.Predicate<CharSequence>,
        com.google.common.base.Predicate<CharSequence> {

    @Override
    default boolean test(CharSequence input) {
        return isStopword(input);
    }

    @Override
    default boolean apply(CharSequence input) {
        return isStopword(input);
    }

    boolean isStopword(CharSequence termText);

    boolean isEmpty();

    void destroy();
}
