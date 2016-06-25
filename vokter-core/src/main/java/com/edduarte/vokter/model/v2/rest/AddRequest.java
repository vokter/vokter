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

package com.edduarte.vokter.model.v2.rest;

import com.edduarte.vokter.diff.DifferenceEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import static com.edduarte.vokter.diff.DifferenceEvent.*;

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

    // mandatory field
    @JsonProperty(required = true)
    private String documentUrl;

    // mandatory field
    @JsonProperty(required = true)
    private String clientUrl;

    // mandatory field
    @JsonProperty(required = true)
    private List<String> keywords;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<DifferenceEvent> events;

    @JsonProperty(defaultValue = "50")
    private int snippetOffset;


    public AddRequest() {
        this.events = Arrays.asList(inserted, deleted);
        this.snippetOffset = 50;
    }


    /**
     * Used for testing only.
     */
    public AddRequest(final String documentUrl,
                      final String clientUrl,
                      final List<String> keywords,
                      final List<DifferenceEvent> events) {
        this.documentUrl = documentUrl;
        this.clientUrl = clientUrl;
        this.keywords = keywords;
        this.events = events;
    }


    public String getDocumentUrl() {
        return documentUrl;
    }


    public String getClientUrl() {
        return clientUrl;
    }


    public List<String> getKeywords() {
        return keywords;
    }


    public List<DifferenceEvent> getEvents() {
        return events;
    }


    public int getSnippetOffset() {
        return snippetOffset;
    }
}
