package argus.parser;

import argus.stemmer.Stemmer;
import argus.stopper.Stopwords;
import it.unimi.dsi.lang.MutableString;

import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public interface Parser extends AutoCloseable {

    /**
     * Parses the specified text and obtains parsed results.
     */
    default List<Result> parse(MutableString text) {
        return parse(text, null, null, false);
    }

    /**
     * Parses the specified text by using the specified stopwords and stemmer, and
     * obtains parsed results.
     */
    List<Result> parse(final MutableString text,
                             final Stopwords stopwords,
                             final Stemmer stemmer,
                             final boolean ignoreCase);

    @Override
    void close();


    /**
     * Represents a parsing result, providing access to a token's phrase
     * position, start position, end position and text.
     */
    public static class Result {

        public int wordNum;
        public int start;
        public int end;
        public MutableString text;

        Result(final int wordNum,
               final int start,
               final int end,
               final MutableString text) {
            this.wordNum = wordNum;
            this.start = start;
            this.end = end;
            this.text = text;
        }
    }

}
