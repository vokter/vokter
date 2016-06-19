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

package com.edduarte.vokter.document;

import java.io.InputStream;

/**
 * A class that represents a document being lazily collected, containing
 * data that will be later compose a Document object.
 * This object is converted into a Document in the collection in the
 * DocumentPipeline class, during the indexing process.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class DocumentInput {

    private String url;

    private InputStream contentStream;

    private String contentType;


    public DocumentInput(String url, InputStream contentStream, String contentType) {
        this.url = url;
        this.contentStream = contentStream;
        this.contentType = contentType;
    }


    public String getUrl() {
        return url;
    }


    public InputStream getStream() {
        return contentStream;
    }


    public String getContentType() {
        return contentType;
    }


    public void destroy() {
        url = null;
        contentStream = null;
        contentType = null;
    }


    @Override
    public String toString() {
        return url;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DocumentInput that = (DocumentInput) o;
        return url.equals(that.url);
    }


    @Override
    public int hashCode() {
        return url.hashCode();
    }
}
