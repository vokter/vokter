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

    public int wordNum;
    public int start;
    public int end;
    public MutableString text;

    ParserResult(final int wordNum,
                 final int start,
                 final int end,
                 final MutableString text) {
        this.wordNum = wordNum;
        this.start = start;
        this.end = end;
        this.text = text;
    }

    @Override
    public String toString() {
        return "ParserResult{" +
                "wordNum=" + wordNum +
                ", text=" + text.toString() +
                '}';
    }
}
