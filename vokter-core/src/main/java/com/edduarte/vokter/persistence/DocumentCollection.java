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
     * Adds the specified document to the local database.
     */
    void add(Document d);


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


    public static class Params {

        private final String url;

        private final String contentType;


        private Params(String url, String contentType) {
            this.url = url;
            if (contentType == null) {
                this.contentType = "";
            } else {
                this.contentType = contentType;
            }
        }


        public static Params of(String url, String contentType) {
            return new Params(url, contentType);
        }


        public String getUrl() {
            return url;
        }


        public String getContentType() {
            return contentType;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Params that = (Params) o;
            return this.url.equals(that.url) &&
                    this.contentType.equals(that.contentType);

        }


        @Override
        public int hashCode() {
            int result = url.hashCode();
            result = 31 * result + contentType.hashCode();
            return result;
        }
    }


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
