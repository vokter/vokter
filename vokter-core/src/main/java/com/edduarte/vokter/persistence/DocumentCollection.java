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
    Pair get(String url, String contentType);


    void destroy();


    public static class Pair {

        private final Document mA;

        private final Document mB;


        private Pair(Document a, Document b) {
            this.mA = a;
            this.mB = b;
        }


        public static Pair of(Document a, Document b) {
            return new Pair(a, b);
        }


        public Document oldest() {
            return this.mA;
        }


        public Document latest() {
            return this.mB;
        }


        public String toString() {
            return "(" + this.oldest() + "," + this.latest() + ")";
        }


        public boolean equals(Object that) {
            if (!(that instanceof Pair)) {
                return false;
            } else {
                Pair thatPair = (Pair) that;
                return this.mA.equals(thatPair.mA) && this.mB.equals(thatPair.mB);
            }
        }


        public int hashCode() {
            return 31 * this.mA.hashCode() + this.mB.hashCode();
        }
    }
}
