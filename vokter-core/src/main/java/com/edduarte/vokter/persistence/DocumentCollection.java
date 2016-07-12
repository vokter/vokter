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

package com.edduarte.vokter.persistence;

import com.optimaize.langdetect.LanguageDetector;

/**
 * A DocumentCollection represents the widest information unit, and has direct
 * access to every collected document and term.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public interface DocumentCollection {

    /**
     * Add a new document to the collection.
     */
    Document addNewDocument(String documentUrl, String documentContentType,
                            LanguageDetector langDetector,
                            boolean filterStopwords, boolean ignoreCase);

    /**
     * Add a new snapshot of a previously stored document to the collection.
     */
    Document addNewSnapshot(Document oldDocument, LanguageDetector langDetector,
                            boolean filterStopwords, boolean ignoreCase);


    /**
     * Removes the specified document from the local database.
     */
    void remove(String url, String contentType);


    /**
     * Converts the specified document url and content type into a pair of
     * document object, the url and the contentType stored
     */
    Snapshots get(String url, String contentType);


    void invalidateCache();


    public static class Snapshots {

        private final Document oldest;

        private final Document newest;


        private Snapshots(Document oldest, Document newest) {
            this.oldest = oldest;
            this.newest = newest;
        }


        public static Snapshots of(Document a, Document b) {
            return new Snapshots(a, b);
        }


        public Document oldest() {
            return this.oldest;
        }


        public Document latest() {
            return this.newest;
        }


        public String toString() {
            return "(" + this.oldest() + "," + this.latest() + ")";
        }


        public boolean equals(Object o) {
            if (!(o instanceof Snapshots)) {
                return false;
            } else {
                Snapshots that = (Snapshots) o;
                return this.oldest.equals(that.oldest) && this.newest.equals(that.newest);
            }
        }


        public int hashCode() {
            return 31 * this.oldest.hashCode() + this.newest.hashCode();
        }
    }
}
