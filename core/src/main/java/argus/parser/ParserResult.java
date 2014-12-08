package argus.parser;

import it.unimi.dsi.lang.MutableString;

/**
 * Represents a parsing result, providing access to a token's phrase
 * position, start position, end position and text.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class ParserResult {

    public final int count;
    public final int start;
    public final int end;
    public final MutableString text;

    ParserResult(final int count,
                 final int start,
                 final int end,
                 final MutableString text) {
        this.count = count;
        this.start = start;
        this.end = end;
        this.text = text;
    }
}
