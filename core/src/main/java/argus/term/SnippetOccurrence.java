package argus.term;

import it.unimi.dsi.lang.MutableString;

/**
 * An occurrence representation with a textual snippet collected from the
 * original document.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public final class SnippetOccurrence extends Occurrence {
    private static final int INDEX_OFFSET = 50;

    /**
     * Used for context-aware comparisons, when word count is the same.
     */
    private final MutableString snippetText;

    public SnippetOccurrence(MutableString originalContent, Occurrence occurrence) {
        super(occurrence);
        int snippetStart = occurrence.getStartIndex() - INDEX_OFFSET;
        if (snippetStart < 0) {
            snippetStart = 0;
        }
        int snippetEnd = occurrence.getEndIndex() + 1 + INDEX_OFFSET;
        if (snippetEnd > originalContent.length()) {
            snippetEnd = originalContent.length();
        }
        this.snippetText = originalContent.substring(snippetStart, snippetEnd);
    }


    public MutableString getSnippet() {
        return snippetText;
    }
}
