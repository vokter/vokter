package argus.term;

import it.unimi.dsi.lang.MutableString;

/**
 * TODO
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class TermSnippet {
    private static final int TERM_SNIPPET_OFFSET = 100;

    private int wordCount;
    private int start;
    private int end;

    /**
     * Used for context-aware comparisons, when word count is the same.
     */
    private final MutableString snippetText;

    public TermSnippet(MutableString originalContent, Occurrence occurrence) {
        int snippetStart = occurrence.getStartIndex() - TERM_SNIPPET_OFFSET;
        if (snippetStart < 0) {
            snippetStart = 0;
        }
        int snippetEnd = occurrence.getEndIndex() + 1 + TERM_SNIPPET_OFFSET;
        if (snippetEnd > originalContent.length()) {
            snippetEnd = originalContent.length();
        }
        this.wordCount = occurrence.getWordCount();
        this.start = occurrence.getStartIndex();
        this.end = occurrence.getEndIndex();
        this.snippetText = originalContent.substring(snippetStart, snippetEnd);
    }


    public int getWordCount() {
        return wordCount;
    }


    public int getStart() {
        return start;
    }


    public int getEnd() {
        return end;
    }


    public MutableString getSnippetText() {
        return snippetText;
    }
}
