/*
 * Copyright 2015 Ed Duarte
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

import java.io.IOException;
import java.io.InputStream;

/**
 * Indexing module that reads an {@link java.io.InputStream} of a document in a
 * specific format / extension, and provides the most optimized state of its textual
 * content according to that extension.
 *
 * @author Ed Duarte (<a href="mailto:ed@edduarte.com">ed@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public interface Reader {

    MutableString readDocumentContents(InputStream documentStream) throws IOException;

    ImmutableSet<String> getSupportedContentTypes();
}
