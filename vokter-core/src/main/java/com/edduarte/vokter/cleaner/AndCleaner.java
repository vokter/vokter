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
 * Utility Cleaner implementation that concatenates two Cleaner classes.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class AndCleaner implements Cleaner {

    private final Cleaner f1;

    private final Cleaner f2;


    private AndCleaner(Cleaner f1, Cleaner f2) {
        this.f1 = f1;
        this.f2 = f2;
    }


    public static AndCleaner of(Cleaner f1, Cleaner f2) {
        return new AndCleaner(f1, f2);
    }


    @Override
    public void clean(MutableString documentContent) {
        f1.clean(documentContent);
        f2.clean(documentContent);
    }
}
