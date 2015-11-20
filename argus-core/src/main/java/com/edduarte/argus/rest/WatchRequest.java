/*
 * Copyright 2014 Ed Duarte
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

    @JsonProperty
    private final String documentUrl;

    @JsonProperty
    private final String responseUrl;

    @JsonProperty
    private final List<String> keywords;

    @JsonProperty
    private final int interval;


    public WatchRequest(final String documentUrl,
                        final String responseUrl,
                        final List<String> keywords,
                        final int interval) {
        this.documentUrl = documentUrl;
        this.responseUrl = responseUrl;
        this.keywords = keywords;
        this.interval = interval;
    }


    public String getRequestUrl() {
        return documentUrl;
    }


    public String getResponseUrl() {
        return responseUrl;
    }


    public List<String> getKeywords() {
        return keywords;
    }


    public int getInterval() {
        return interval;
    }
}
