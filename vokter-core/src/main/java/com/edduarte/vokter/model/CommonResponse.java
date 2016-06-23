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

package com.edduarte.vokter.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.4.1
 * @since 1.0.0
 */
public class CommonResponse {

    @JsonProperty
    private final int code;

    @JsonProperty
    private final String message;


    private CommonResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }


    public static CommonResponse ok() {
        return new CommonResponse(0, "");
    }


    public static CommonResponse invalidDocumentUrl() {
        return new CommonResponse(1, "The provided document URL is invalid.");
    }


    public static CommonResponse invalidClientUrl() {
        return new CommonResponse(2, "The provided client URL is invalid.");
    }


    public static CommonResponse emptyKeywords() {
        return new CommonResponse(3, "You need to provide at least one valid keyword.");
    }


    public static CommonResponse emptyDifferenceActions() {
        return new CommonResponse(4, "At least one difference action " +
                "('added' or 'removed') must not be ignored.");
    }


    public static CommonResponse alreadyExists() {
        return new CommonResponse(5, "The request conflicts with a " +
                "currently active watch job, since the provided document " +
                "URL is already being watched and notified to the provided " +
                "client URL.");
    }


    public static CommonResponse invalidFormat() {
        return new CommonResponse(6, "The request body has an invalid format.");
    }


    public static CommonResponse notExists() {
        return new CommonResponse(7, "The specified job to cancel does not exist.");
    }


    public static CommonResponse other(int code, String message) {
        return new CommonResponse(code, message);
    }


    public int getCode() {
        return code;
    }


    public String getMessage() {
        return message;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommonResponse that = (CommonResponse) o;
        return code == that.code && message.equals(that.message);
    }


    @Override
    public int hashCode() {
        int result = code;
        result = 31 * result + message.hashCode();
        return result;
    }
}
