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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.ws.rs.core.MediaType;

/**
 * Model class of a JSON request to cancel a page monitoring.
 * This request is consumed by the 'cancel' method in the RestResource class.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.4.1
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CancelRequest {

    @JsonProperty(required = true)
    private String documentUrl;

    @JsonProperty
    private String documentContentType;

    @JsonProperty(required = true)
    private String clientUrl;

    /**
     * Deprecated and replaced by 'clientUrl'. This attribute was kept for
     * backwards-compatibility purposes, since this is only used if the
     * subscribe request was sent using 'responseUrl' and NOT 'clientUrl' (as
     * documented in versions earlier than 1.3.3). For versions 1.3.3 and
     * upwards, this field is not mandatory unless the clientUrl field is empty.
     */
    @Deprecated
    @JsonProperty
    private String responseUrl;

    @JsonProperty
    private String clientContentType;


    // default variables
    public CancelRequest() {
        this.documentContentType = null;
        this.clientContentType = MediaType.APPLICATION_JSON;
    }


    public String getDocumentUrl() {
        return documentUrl;
    }


    public String getDocumentContentType() {
        return documentContentType;
    }


    public String getClientUrl() {
        return clientUrl != null ? clientUrl : responseUrl;
    }


    public String getClientContentType() {
        return clientContentType;
    }
}
