/*
 * Copyright 2015 Eduardo Duarte
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.edduarte.vokter.rest.model.v2;

import com.edduarte.vokter.diff.DiffEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.ws.rs.core.MediaType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.edduarte.vokter.diff.DiffEvent.*;

/**
 * Model class of a JSON request for page watching.
 * This request is consumed by the 'watch' method in the RestResource class.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.3
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty(required = true)
    private String documentUrl;

    @JsonProperty
    private String documentContentType;

    @JsonProperty(required = true)
    private String clientUrl;

    @JsonProperty
    private String clientContentType;

    @JsonProperty(required = true)
    private List<String> keywords;

    /**
     * Interval in seconds that is used to trigger a detection job, since
     * matching jobs occur immediately after a detection job ends. That means
     * that if the combination of documentUrl with documentContentType is being
     * watched by two clients, one with interval 200 and another with interval
     * 70, then the same diff detection job will occur every 70 seconds and
     * every 200 seconds, calling matching jobs if differences are found. In
     * other words, the client that specified a 200 second interval may actually
     * receive differences at a 70 second interval, economizing detection calls
     * (instead of duplicating them) and discovering potential differences at a
     * faster pace.
     */
    @JsonProperty
    private int interval;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<DiffEvent> events;

    @JsonProperty
    private boolean filterStopwords;

    @JsonProperty
    private boolean enableStemming;

    @JsonProperty
    private boolean ignoreCase;

    @JsonProperty
    private int snippetOffset;


    // default variables
    public AddRequest() {
        this.documentContentType = MediaType.TEXT_HTML;
        this.clientContentType = MediaType.APPLICATION_JSON;
        this.interval = 600;
        this.events = Arrays.asList(inserted, deleted);
        this.filterStopwords = true;
        this.enableStemming = true;
        this.ignoreCase = true;
        this.snippetOffset = 50;
    }


    /**
     * Used for add default values to a v1 rest request.
     */
    public AddRequest(com.edduarte.vokter.rest.model.v1.AddRequest r) {
        this();

        this.documentUrl = r.getDocumentUrl();
        this.clientUrl = r.getClientUrl();
        this.keywords = r.getKeywords();

        this.events = new ArrayList<>();
        if (r.getIgnoreAdded()) {
            events.add(DiffEvent.deleted);
        } else if (r.getIgnoreRemoved()) {
            events.add(DiffEvent.inserted);
        } else {
            events.add(DiffEvent.inserted);
            events.add(DiffEvent.deleted);
        }
    }


    /**
     * Used for testing only.
     */
    public AddRequest(final String documentUrl,
                      final String clientUrl,
                      final List<String> keywords,
                      final int interval) {
        this();
        this.documentUrl = documentUrl;
        this.clientUrl = clientUrl;
        this.keywords = keywords;
        this.interval = interval;
    }


    public String getDocumentUrl() {
        return documentUrl;
    }


    public String getDocumentContentType() {
        return documentContentType;
    }


    public String getClientUrl() {
        return clientUrl;
    }


    public String getClientContentType() {
        return clientContentType;
    }


    public List<String> getKeywords() {
        return keywords;
    }


    public int getInterval() {
        return interval;
    }


    public List<DiffEvent> getEvents() {
        return events;
    }


    public int getSnippetOffset() {
        return snippetOffset;
    }


    public boolean filterStopwords() {
        return filterStopwords;
    }


    public boolean enableStemming() {
        return enableStemming;
    }


    public boolean ignoreCase() {
        return ignoreCase;
    }
}
