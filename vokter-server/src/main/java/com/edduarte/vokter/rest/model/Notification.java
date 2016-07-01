package com.edduarte.vokter.rest.model;

import com.edduarte.vokter.diff.Match;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Set;

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
    private final Set<Match> diffs;


    private Notification(Status status,
                         String url,
                         String contentType,
                         Set<Match> diffs) {
        this.status = status;
        this.url = url;
        this.contentType = contentType;
        this.diffs = diffs;
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


    public Set<Match> getDiffs() {
        return diffs;
    }


    public enum Status {
        ok, timeout
    }
}
