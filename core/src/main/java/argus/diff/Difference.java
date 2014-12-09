package argus.diff;

import argus.keyword.Keyword;
import argus.keyword.KeywordSerializer;
import com.google.gson.GsonBuilder;

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
}
