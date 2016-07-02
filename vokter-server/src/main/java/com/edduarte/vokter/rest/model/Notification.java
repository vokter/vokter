package com.edduarte.vokter.rest.model;

import com.edduarte.vokter.diff.DiffEvent;
import com.edduarte.vokter.diff.Match;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class Notification {

    @JsonProperty(required = true)
    private final Status status;


    @JsonProperty(required = true)
    private final String url;


    @JsonProperty
    private final String contentType;


    @JsonProperty(required = true)
    private final Set<MatchWrapper> diffs;


    private Notification(Status status,
                         String url,
                         String contentType,
                         Set<Match> diffs) {
        this.status = status;
        this.url = url;
        this.contentType = contentType;
        this.diffs = diffs.parallelStream()
                .map(MatchWrapper::new)
                .collect(Collectors.toSet());
    }


    public static Notification ok(String documentUrl,
                                  String contentType,
                                  Set<Match> diffs) {
        return new Notification(Status.ok, documentUrl, contentType, diffs);
    }


    public static Notification timeout(String documentUrl,
                                       String contentType) {
        return new Notification(
                Status.timeout,
                documentUrl,
                contentType,
                Collections.emptySet()
        );
    }


    public Status getStatus() {
        return status;
    }


    public String getUrl() {
        return url;
    }


    public String getContentType() {
        return contentType;
    }


    public Set<MatchWrapper> getDiffs() {
        return diffs;
    }


    public enum Status {
        ok, timeout
    }


    public static class MatchWrapper {

        @JsonProperty(required = true)
        private DiffEvent event;

        @JsonProperty(required = true)
        private String keyword;

        @JsonProperty(required = true)
        private String text;

        @JsonProperty(required = true)
        private String snippet;


        public MatchWrapper(Match match) {
            this.event = match.getEvent();
            this.keyword = match.getKeyword().getOriginalInput();
            this.text = match.getText();
            this.snippet = match.getSnippet();
        }


        public DiffEvent getEvent() {
            return event;
        }


        public String getKeyword() {
            return keyword;
        }


        public String getText() {
            return text;
        }


        public String getSnippet() {
            return snippet;
        }
    }
}
