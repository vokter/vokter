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

import java.text.Normalizer;

/**
 * Cleaner class that converts diacritic words into their non-diacritic form.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class DiacriticCleaner implements Cleaner {

    @Override
    public void clean(MutableString documentContents) {
        String string = Normalizer.normalize(documentContents.toString(), Normalizer.Form.NFD);
        documentContents.delete(0, documentContents.length());
        for (char c : string.toCharArray()) {
            if (c <= '\u007F') {
                documentContents.append(c);
            }
        }
    }
}
