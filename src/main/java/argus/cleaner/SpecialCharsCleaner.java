package argus.cleaner;

import it.unimi.dsi.lang.MutableString;

/**
 * Simple cleaner class that filters some common special, non-informative
 * characters. The filtered characters are replaced by whitespaces (optimizing
 * the Tokenizer results).
 * <p/>
 * As a rule, this clean will only clear characters that do not provide much
 * information on their own, like quotation-marks and bullet-points, for example.
 * This clean, however, will NOT clean characters that provide mathematical
 * information, like ½, π, µ and φ.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 2.0
 */
public class SpecialCharsCleaner implements Cleaner {

    /**
     * The characters to evaluate and clean from the provided document text.
     */
    private static final char[] CHARS_TO_FILTER = {
            '{', '}', '[', ']', '(', ')', '*', '/', '^', '~', '<', '>',
            '_', '…', '–', '−', '.', ',', '!', '?', '@', '#', '&', '+', '-', '=',
            '/', ':', ';', '\\', '|', '\"', '\'', '”', '“', '„', '«',
            '»', '’', '‘', '′', '⏐', '•', '↔', '►', '', '', '', '', '',
            '', '', '●', '®', '¶', '♦', '→', '·', '·', '▪', '○', '', '',
            '', '', '†', '', '║', '▲', '´', '\n'};

    private static final char[] DEFAULT_REPLACEMENTS;

    static {
        int numOfChars = CHARS_TO_FILTER.length;
        DEFAULT_REPLACEMENTS = new char[numOfChars];
        for (int i = 0; i < numOfChars; i++) {
            DEFAULT_REPLACEMENTS[i] = ' ';
        }
    }

    private final char[] replacements;

    public SpecialCharsCleaner() {
        this.replacements = DEFAULT_REPLACEMENTS;
    }

    public SpecialCharsCleaner(char replacement) {
        int numOfChars = CHARS_TO_FILTER.length;
        this.replacements = new char[numOfChars];
        for (int i = 0; i < numOfChars; i++) {
            this.replacements[i] = replacement;
        }
    }

    @Override
    public void clean(MutableString documentContents) {
        // the replace implementation of mutable strings use 'indexOf' instead
        // of 'split' to perform character lookup, since char-by-char matching
        // is considerably faster than regex-pattern matching
        documentContents.replace(CHARS_TO_FILTER, replacements);
    }
}
