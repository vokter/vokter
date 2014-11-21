package argus.index;

import java.io.Serializable;
import java.util.Comparator;

/**
 * An occurrence represents a position within a document where a term occurs.
 * This occurrence is implemented to allow phrase queries, using a word-based
 * counter, and to allow snippet printing, using character-based counters.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 2.0
 */
public final class Occurrence implements Comparable<Occurrence>, Serializable {
    private static final long serialVersionUID = 1L;

    private final Term parentTerm;
    private final int phrasePosition;
    private final int startPosition;
    private final int endPosition;


    Occurrence(Term parentTerm, int phrasePosition, int startPosition, int endPosition) {
        this.parentTerm = parentTerm;
        this.phrasePosition = phrasePosition;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }


    /**
     * Returns the word-based position of this occurrence in the parent document.
     */
    public int getPhrasePosition() {
        return phrasePosition;
    }


    /**
     * Returns the starting, character-based index of this occurrence in the parent
     * document.
     */
    public int getStartPosition() {
        return startPosition;
    }


    /**
     * Returns the ending, character-based index of this occurrence in the parent
     * document.
     */
    public int getEndPosition() {
        return endPosition;
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
        return this.parentTerm.equals(that.parentTerm) &&
                this.phrasePosition == that.phrasePosition &&
                this.startPosition == that.startPosition &&
                this.endPosition == that.endPosition;
    }


    /**
     * Returns the hashcode of this occurrence.
     */
    @Override
    public int hashCode() {
        int result = parentTerm.hashCode();
        result = 31 * result + phrasePosition;
        result = 31 * result + startPosition;
        result = 31 * result + endPosition;
        return result;
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


    @Override
    public String toString() {
        return parentTerm.toString();
    }


    /**
     * Comparator used to sort a set of {@link Occurrence} objects.
     */
    public static class OccurrenceComparator implements Comparator<Occurrence> {

        /**
         * Compares two occurrences considering their positions in the sentence,
         * comparing the start and end pointer.
         *
         * @param a1 1st occurrence to be compared.
         * @param a2 2nd occurrence to be compared.
         * @return <code>1</code> if the 1st occurrence appears latter in the
         * sentence, and <code>-1</code> if the 2nd occurrence appears latter
         * in the sentence.
         */
        @Override
        public int compare(final Occurrence a1, final Occurrence a2) {

            if (a1.getStartPosition() > a2.getStartPosition()) {
                return 1;
            }
            if (a1.getStartPosition() < a2.getStartPosition()) {
                return -1;
            }

            if (a1.getEndPosition() > a2.getEndPosition()) {
                return 1;
            }
            if (a1.getEndPosition() < a2.getEndPosition()) {
                return -1;
            }
            return 0;
        }
    }
}