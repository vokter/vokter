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

package com.edduarte.argus.document;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.io.Serializable;
import java.util.Comparator;

/**
 * An occurrence represents a position within a document where a occurrence occurs.
 * This occurrence is implemented to allow phrase queries, using a word-based
 * counter, and to allow snippet printing, using character-based counters.
 *
 * @author Ed Duarte (<a href="mailto:ed@edduarte.com">ed@edduarte.com</a>)
 * @version 1.3.0
 * @since 1.0.0
 */
public class Occurrence extends BasicDBObject implements Comparable<Occurrence>, Serializable {
    public static final String TEXT = "text";

    public static final String WORD_COUNT = "word_count";

    public static final String START_INDEX = "start_index";

    public static final String END_INDEX = "end_index";

    private static final long serialVersionUID = 1L;


    public Occurrence(String text, int wordCount, int startIndex, int endIndex) {
        super(TEXT, text);
        append(WORD_COUNT, wordCount);
        append(START_INDEX, startIndex);
        append(END_INDEX, endIndex);
    }


    public Occurrence(BasicDBObject mongoObject) {
        super(mongoObject);
    }


    public Occurrence(DBObject mongoObject) {
        super(mongoObject.toMap());
    }


    /**
     * Returns the word-based position of this occurrence in the parent document.
     */
    public int getWordCount() {
        return getInt(WORD_COUNT);
    }


    /**
     * Returns the starting, character-based index of this occurrence in the parent
     * document.
     */
    public int getStartIndex() {
        return getInt(START_INDEX);
    }


    /**
     * Returns the ending, character-based index of this occurrence in the parent
     * document.
     */
    public int getEndIndex() {
        return getInt(END_INDEX);
    }


    /**
     * Returns the text that representsoccurrences term.
     */
    @Override
    public String toString() {
        return getString(TEXT);
    }


    /**
     * Verifies if the specified occurrence is contained in this one.
     *
     * @param a the occurrence to test if it is contained in this one
     * @return <code>true</code> if the specified occurrence is contained
     * in this one, and <code>false</code> in case otherwise.
     */
    public boolean contains(Occurrence a) {

        if (this.equals(a)) {
            return false;
        }

        return this.getStartIndex() <= a.getStartIndex()
                && this.getEndIndex() >= a.getEndIndex();
    }


    /**
     * Verifies if the specified occurrence is intersected with this one.
     *
     * @param a the occurrence that should be intersected with this one.
     * @return <code>true</code> if the specified occurrence is intersected
     * in this one, and <code>false</code> in case otherwise.
     */
    public boolean intersects(Occurrence a) {

        if (this.nests(a) || a.nests(this)) {
            return false;
        }

        if (this.getStartIndex() >= a.getStartIndex() &&
                this.getStartIndex() <= a.getEndIndex() && this.getEndIndex() > a.getEndIndex()) {
            return true;
        }

        if (this.getEndIndex() >= a.getStartIndex() &&
                this.getEndIndex() <= a.getEndIndex() && this.getStartIndex() < a.getStartIndex()) {
            return true;
        }

        if (a.getStartIndex() >= this.getStartIndex() &&
                a.getStartIndex() <= this.getEndIndex() && a.getEndIndex() > this.getEndIndex()) {
            return true;
        }

        if (a.getEndIndex() >= this.getStartIndex() &&
                a.getEndIndex() <= this.getEndIndex() && a.getStartIndex() < this.getStartIndex()) {
            return true;
        }

        return false;
    }


    /**
     * Verifies if the specified occurrence is nested in this one.
     *
     * @param a the occurrence that should be nested in this one.
     * @return <code>true</code> if the specified occurrence is nested
     * in this one, and <code>false</code> otherwise.
     */
    public boolean nests(Occurrence a) {

        if (this.equals(a)) {
            return false;
        }

        return (this.getStartIndex() >= a.getStartIndex())
                && (this.getStartIndex() <= a.getEndIndex())
                && (this.getEndIndex() >= a.getStartIndex())
                && (this.getEndIndex() <= a.getEndIndex());
    }


    /**
     * Compares this occurrence with the specified one in tokens of their positions.
     *
     * @param o the other occurrence to compare with this
     * @return the result of the comparison
     */
    @Override
    public int compareTo(Occurrence o) {
        return new OccurrenceComparator().compare(this, o);
    }


    /**
     * Checks the equality between this occurrence and the specified one.
     *
     * @param o the occurrence to be compared with.
     * @return <code>true</code> if the two annotations are equal, and
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Occurrence that = (Occurrence) o;
        return this.getWordCount() == that.getWordCount() &&
                this.getStartIndex() == that.getStartIndex() &&
                this.getEndIndex() == that.getEndIndex();
    }


    /**
     * Override the hashCode method to consider all the internal variables.
     *
     * @return unique number for each occurrence
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.getWordCount();
        hash = 79 * hash + this.getStartIndex();
        hash = 79 * hash + this.getEndIndex();
        return hash;
    }


    /**
     * Comparator used to sort a set of {@link Occurrence} objects.
     */
    public static class OccurrenceComparator implements Comparator<Occurrence> {

        /**
         * Compares two occurrences considering their positions in the sentence,
         * comparing the initialize and end pointer.
         *
         * @param a1 1st occurrence to be compared.
         * @param a2 2nd occurrence to be compared.
         * @return <code>1</code> if the 1st occurrence appears latter in the
         * sentence, and <code>-1</code> if the 2nd occurrence appears latter
         * in the sentence.
         */
        @Override
        public int compare(final Occurrence a1, final Occurrence a2) {

            if (a1.getStartIndex() > a2.getStartIndex()) {
                return 1;
            }
            if (a1.getStartIndex() < a2.getStartIndex()) {
                return -1;
            }

            if (a1.getEndIndex() > a2.getEndIndex()) {
                return 1;
            }
            if (a1.getEndIndex() < a2.getEndIndex()) {
                return -1;
            }
            return 0;
        }
    }
}