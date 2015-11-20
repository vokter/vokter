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

package com.edduarte.argus.reader;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.lang.MutableString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A reader class that supports reading documents in plain-text format.
 *
 * @author Ed Duarte (<a href="mailto:ed@edduarte.com">ed@edduarte.com</a>)
 * @version 2.0.0
 * @since 1.0.0
 */
public class PlainTextReader implements argus.reader.Reader {

    @Override
    public MutableString readDocumentContents(InputStream documentStream) throws IOException {
        MutableString sb = new MutableString();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(documentStream))) {
            for (int c; (c = reader.read()) != -1; ) {
                sb.append((char) c);
            }
        }
        return sb.compact();
    }

    @Override
    public ImmutableSet<String> getSupportedContentTypes() {
        return ImmutableSet.of("text/plain");
    }
}
