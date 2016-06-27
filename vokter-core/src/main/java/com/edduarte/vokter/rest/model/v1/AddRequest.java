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

package com.edduarte.vokter.rest.model.v1;

import com.edduarte.vokter.diff.DiffEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.ws.rs.core.MediaType;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import static com.edduarte.vokter.diff.DiffEvent.deleted;
import static com.edduarte.vokter.diff.DiffEvent.inserted;

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

    /**
     * Deprecated and replaced by 'clientUrl'. This attribute was kept for
     * backwards-compatibility purposes, since this is only used if the
     * subscribe request was sent using 'receiverUrl' and NOT 'clientUrl' (as
     * documented in versions earlier than 1.3.3). For versions 1.3.3 and
     * upwards, this field is not mandatory unless the clientUrl field is empty.
     */
    @Deprecated
    @JsonProperty
    private String receiverUrl;

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
     *
     * For versions of Vokter below 2.0.0, diff-detection jobs and diff-matching
     * would have independent intervals. This means that detection jobs needed
     * to use an internal interval (420 seconds), and if matching jobs were
     * configured to run more frequently than that interval, it would look for
     * matches on the same detected differences two times or more.
     */
    @JsonProperty
    private int interval;

    @JsonProperty
    private boolean ignoreAdded;

    @JsonProperty
    private boolean ignoreRemoved;

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
        this.ignoreAdded = false;
        this.ignoreRemoved = false;
        this.filterStopwords = true;
        this.enableStemming = true;
        this.ignoreCase = true;
        this.snippetOffset = 50;
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


//    public AddRequest(final String documentUrl,
//                      final String documentContentType,
//                      final String clientUrl,
//                      final String clientContentType,
//                      final List<String> keywords,
//                      final int interval,
//                      final boolean ignoreAdded,
//                      final boolean ignoreRemoved,
//                      boolean filterStopwords,
//                      boolean enableStemming,
//                      boolean ignoreCase,
//                      int snippetOffset) {
//        this.documentUrl = documentUrl;
//        this.documentContentType = documentContentType;
//        this.clientUrl = clientUrl;
//        this.clientContentType = clientContentType;
//        this.keywords = keywords;
//        this.interval = interval;
//        this.ignoreAdded = ignoreAdded;
//        this.ignoreRemoved = ignoreRemoved;
//        this.filterStopwords = filterStopwords;
//        this.enableStemming = enableStemming;
//        this.ignoreCase = ignoreCase;
//        this.snippetOffset = snippetOffset;
//    }


    public String getDocumentUrl() {
        return documentUrl;
    }


    public String getDocumentContentType() {
        return documentContentType;
    }


    public String getClientUrl() {
        return clientUrl != null ? clientUrl : receiverUrl;
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


    public boolean getIgnoreAdded() {
        return ignoreAdded;
    }


    public boolean getIgnoreRemoved() {
        return ignoreRemoved;
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


    public int getSnippetOffset() {
        return snippetOffset;
    }
}
