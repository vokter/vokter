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

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author Ed Duarte (<a href="mailto:ed@edduarte.com">ed@edduarte.com</a>)
 * @version 1.4.1
 * @since 1.0.0
 */
public class CancelRequest {

    @JsonProperty
    private String documentUrl; // mandatory field

    @JsonProperty
    private String clientUrl; // mandatory field

    @JsonProperty
    @Deprecated
    /**
     * Deprecated and replaced by 'clientUrl'. This attribute was kept for
     * backwards-compatibility purposes, since this is only used if the
     * subscribe request was sent using 'responseUrl' and NOT 'clientUrl' (as
     * documented in versions earlier than 1.3.3).
     */
    private String responseUrl; // mandatory field


    public String getDocumentUrl() {
        return documentUrl;
    }


    public String getClientUrl() {
        return clientUrl != null ? clientUrl : responseUrl;
    }
}
