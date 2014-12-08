package argus.term;

/**
 * A representation of a term with a textual snippet collected from the
 * original document.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public final class Snippet extends Term {
    private static final int INDEX_OFFSET = 20;

    /**
     * Used for context-aware comparisons, when word count is the same.
     */
    private final String snippetText;

    public Snippet(String originalContent, Term term) {
        super(term);
        int snippetStart = term.getStartIndex() - INDEX_OFFSET;
        if (snippetStart < 0) {
            snippetStart = 0;
        }
        int snippetEnd = term.getEndIndex() + 1 + INDEX_OFFSET;
        if (snippetEnd > originalContent.length()) {
            snippetEnd = originalContent.length();
        }
        this.snippetText = originalContent.substring(snippetStart, snippetEnd);
    }


    public String getSnippet() {
        return snippetText;
    }
}
