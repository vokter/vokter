package com.edduarte.vokter.model.v2;

import com.edduarte.vokter.diff.DifferenceEvent;
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
    private DifferenceEvent event;

    /**
     * The keyword contained within this difference.
     */
    @JsonProperty(required = true)
    private Keyword keyword;

    /**
     * The text that represents this difference, or in other words, the text
     * that was either added or removed from the document.
     */
    @JsonProperty(required = true)
    private String snippet;


    public Match(final DifferenceEvent event,
                 final Keyword keyword,
                 final String snippet) {
        this.event = event;
        this.keyword = keyword;
        this.snippet = snippet;
    }


    public DifferenceEvent getEvent() {
        return event;
    }


    public Keyword getKeyword() {
        return keyword;
    }


    public String getSnippet() {
        return snippet;
    }


    @Override
    public String toString() {
        return "{" +
                "event=" + event +
                ", keyword=" + keyword +
                ", snippet='" + snippet + '\'' +
                '}';
    }
}
