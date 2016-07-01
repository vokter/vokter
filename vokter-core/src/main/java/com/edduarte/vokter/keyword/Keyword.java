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

package com.edduarte.vokter.keyword;

import com.fasterxml.jackson.annotation.JsonValue;
import it.unimi.dsi.lang.MutableString;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Keyword represents a set of texts that should match a difference
 * detected between two snapshots of a document.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public final class Keyword {

    private final String originalInput;

    /**
     * The set of texts that compose this search, stored in the same order as
     * written by the user (linked hash set implementation).
     */
    private final Collection<MutableString> texts;


    public Keyword(final String originalInput, final Collection<MutableString> texts) {
        this.originalInput = originalInput;
        this.texts = texts;
    }


    @JsonValue
    public String getOriginalInput() {
        return originalInput;
    }


    /**
     * Returns a lazy access to all texts that compose this query.
     */
    public Stream<MutableString> textStream() {
        return texts.stream();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Keyword keyword = (Keyword) o;
        return originalInput.equals(keyword.originalInput);
    }


    @Override
    public int hashCode() {
        return originalInput.hashCode();
    }


    @Override
    public String toString() {
        String fullQuery = textStream().collect(Collectors.joining(" "));
        return fullQuery;
    }
}
