/*
 * Copyright 2015 Ed Duarte
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

package com.edduarte.argus.rest;

import com.google.gson.Gson;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Wrapper class of a JSON request for page watching.
 * This request is consumed by the 'watch' method in the RESTResource class.
 *
 * @author Ed Duarte (<a href="mailto:ed@edduarte.com">ed@edduarte.com</a>)
 * @version 1.3.0
 * @since 1.0.0
 */
public class WatchRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_INTERVAL = 600;

    private static final boolean DEFAULT_IGNORE_ADDED = false;

    private static final boolean DEFAULT_IGNORE_REMOVED = false;

    @JsonProperty
    private String documentUrl; // mandatory fields

    @JsonProperty
    private String receiverUrl; // mandatory fields

    @JsonProperty
    private List<String> keywords; // mandatory fields

    @JsonProperty
    private int interval;

    @JsonProperty
    private boolean ignoreAdded;

    @JsonProperty
    private boolean ignoreRemoved;

    /**
     * Used by GSON.
     */
    public WatchRequest() {
        this.interval = DEFAULT_INTERVAL;
        this.ignoreAdded = DEFAULT_IGNORE_ADDED;
        this.ignoreRemoved = DEFAULT_IGNORE_REMOVED;
    }


    /**
     * Used for testing only.
     */
    public WatchRequest(final String documentUrl,
                        final String receiverUrl,
                        final List<String> keywords,
                        final int interval,
                        final boolean ignoreAdded,
                        final boolean ignoreRemoved) {
        this.documentUrl = documentUrl;
        this.receiverUrl = receiverUrl;
        this.keywords = keywords;
        this.interval = interval;
        this.ignoreAdded = ignoreAdded;
        this.ignoreRemoved = ignoreRemoved;
    }


    public String getDocumentUrl() {
        return documentUrl;
    }


    public String getReceiverUrl() {
        return receiverUrl;
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


    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
