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
    default List<ParserResult> parse(MutableString text) {
        return parse(text, null, null, false);
    }

    /**
     * Parses the specified text by using the specified stopwords and stemmer, and
     * obtains parsed results.
     */
    List<ParserResult> parse(final MutableString text,
                             final Stopwords stopwords,
                             final Stemmer stemmer,
                             final boolean ignoreCase);

    @Override
    void close();
}
