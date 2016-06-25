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

package com.edduarte.vokter.model.v1.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

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
public class SubscribeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    // mandatory field
    @JsonProperty(required = true)
    private String documentUrl;

    // mandatory field
    @JsonProperty(required = true)
    private String clientUrl;

    /**
     * Deprecated and replaced by 'clientUrl'. This attribute was kept for
     * backwards-compatibility purposes, since this is only used if the
     * subscribe request was sent using 'receiverUrl' and NOT 'clientUrl' (as
     * documented in versions earlier than 1.3.3). For versions 1.3.3 and
     * upwards, this field is not mandatory unless the clientUrl field is empty.
     */
    @Deprecated
    @JsonProperty
    private String receiverUrl; // mandatory field

    @JsonProperty(required = true)
    private List<String> keywords; // mandatory field

    /**
     * The interval value is no longer used. Before deprecation, the intervals
     * for difference-matching jobs could be set, but difference-detection jobs
     * would still occur independently of difference-matching so it can
     * accommodate to all matching jobs for the same document. This means that
     * detection jobs needed to use an internal interval (420 seconds), and if
     * matching jobs were configured to run more frequently than that interval,
     * it would look for matches on the same detected differences two times or
     * more.
     * For versions 2.0.0 and above, matching jobs occur immediately after a
     * detection job ends.
     */
    @Deprecated
    @JsonProperty(defaultValue = "600")
    private int interval;

    @JsonProperty(defaultValue = "false")
    private boolean ignoreAdded;

    @JsonProperty(defaultValue = "false")
    private boolean ignoreRemoved;


    public SubscribeRequest() {
        this.interval = 600;
        this.ignoreAdded = false;
        this.ignoreRemoved = false;
    }


    /**
     * Used for testing only.
     */
    public SubscribeRequest(final String documentUrl,
                            final String clientUrl,
                            final List<String> keywords,
                            final int interval,
                            final boolean ignoreAdded,
                            final boolean ignoreRemoved) {
        this.documentUrl = documentUrl;
        this.clientUrl = clientUrl;
        this.keywords = keywords;
        this.interval = interval;
        this.ignoreAdded = ignoreAdded;
        this.ignoreRemoved = ignoreRemoved;
    }


    public String getDocumentUrl() {
        return documentUrl;
    }


    public String getClientUrl() {
        return clientUrl != null ? clientUrl : receiverUrl;
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


    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }


    public void setClientUrl(String clientUrl) {
        this.clientUrl = clientUrl;
    }


    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }


    public void setInterval(int interval) {
        this.interval = interval;
    }


    public void setIgnoreAdded(boolean ignoreAdded) {
        this.ignoreAdded = ignoreAdded;
    }


    public void setIgnoreRemoved(boolean ignoreRemoved) {
        this.ignoreRemoved = ignoreRemoved;
    }
}
