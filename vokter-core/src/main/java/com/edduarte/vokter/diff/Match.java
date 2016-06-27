package com.edduarte.vokter.diff;

import com.edduarte.vokter.model.mongodb.Keyword;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class Match {

    /**
     * The event of this difference ('added', 'removed' or 'nothing').
     */
    @JsonProperty(required = true)
    private DiffEvent event;

    /**
     * The keyword contained within this difference.
     */
    @JsonProperty(required = true)
    private Keyword keyword;

    /**
     * The text that contains the keyword above that was either added or
     * removed from the document.
     */
    @JsonProperty(required = true)
    private String text;

    /**
     * The text that contains the difference text above along with surrounding
     * text that contextualizes where this difference occurred.
     */
    @JsonProperty(required = true)
    private String snippet;


    public Match(final DiffEvent event,
                 final Keyword keyword,
                 final String text,
                 final String snippet) {
        this.event = event;
        this.keyword = keyword;
        this.text = text;
        this.snippet = snippet;
    }


    public DiffEvent getEvent() {
        return event;
    }


    public Keyword getKeyword() {
        return keyword;
    }


    public String getText() {
        return text;
    }


    public String getSnippet() {
        return snippet;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Match match = (Match) o;

        return event == match.event &&
                keyword.equals(match.keyword) &&
                text.equals(match.text);

    }


    @Override
    public int hashCode() {
        int result = event.hashCode();
        result = 31 * result + keyword.hashCode();
        result = 31 * result + text.hashCode();
        return result;
    }


    @Override
    public String toString() {
        return "{" +
                "event=" + event +
                ", keyword=" + keyword +
                ", text='" + text + '\'' +
                ", snippet='" + snippet + '\'' +
                '}';
    }
}
