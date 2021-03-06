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

package com.edduarte.vokter.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 0.3.1
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CancelRequest {

    @JsonProperty(required = true)
    private String documentUrl; // mandatory field

    // mandatory field
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


    public String getDocumentUrl() {
        return documentUrl;
    }


    public String getClientUrl() {
        return clientUrl != null ? clientUrl : responseUrl;
    }
}
