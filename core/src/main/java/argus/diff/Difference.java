package argus.diff;

import argus.keyword.Keyword;

/**
 * TODO
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class Difference {

    /**
     * The status of this difference.
     */
    public final DifferenceStatus status;

    /**
     * The keyword contained within this difference.
     */
    public final Keyword keyword;

    /**
     * The text that represents this difference, or in other words, the text that
     * was either added or removed from the document.
     */
    public final String snippet;


    Difference(final DifferenceStatus status,
               final Keyword keyword,
               final String snippet) {
        this.status = status;
        this.keyword = keyword;
        this.snippet = snippet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Difference that = (Difference) o;
        return this.keyword.equals(that.keyword) &&
                this.snippet.equals(that.snippet) &&
                this.status == that.status;
    }

    @Override
    public int hashCode() {
        int result = status.hashCode();
        result = 31 * result + keyword.hashCode();
        result = 31 * result + snippet.hashCode();
        return result;
    }
}
