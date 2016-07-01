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

package com.edduarte.vokter.persistence.mongodb;

import com.edduarte.vokter.persistence.Document;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.mongodb.BasicDBObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Simple structure that holds a document current snapshot and associates
 * it with an url.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public final class MongoDocument extends BasicDBObject
        implements Document, Serializable {

    public static final String URL = "url";

    public static final String DATE = "date";

    public static final String TEXT = "text";

    public static final String CONTENT_TYPE = "content_type";

    public static final String SHINGLES = "shingles";

    public static final String SHINGLE_LENGTH = "shingle_length";

    public static final String BANDS = "bands";

    private static final long serialVersionUID = 1L;


    public MongoDocument(String url, Date date, String contentType, String text,
                         List<String> shingles, int k, int[] bands) {
        super();
        if (contentType == null) {
            contentType = "";
        }
        append(URL, url);
        append(DATE, date);
        append(CONTENT_TYPE, contentType);
        append(TEXT, text);
        append(SHINGLES, shingles);
        append(SHINGLE_LENGTH, k);
        List<Integer> list = new ArrayList<>();
        int size = bands.length;
        for (int i = 0; i < size; i++) {
            list.add(bands[i]);
        }
        append(BANDS, list);
    }


    public MongoDocument(BasicDBObject dbObject) {
        super(dbObject);
    }


    public String getUrl() {
        return getString(URL);
    }


    public Date getDate() {
        return getDate(DATE);
    }


    public String getContentType() {
        return getString(CONTENT_TYPE);
    }


    public String getText() {
        return getString(TEXT);
    }


    public List<String> getShingles() {
        return (ArrayList<String>) get(SHINGLES);
    }


    public int getShingleLength() {
        return getInt(SHINGLE_LENGTH);
    }


    public int[] getBands() {
        List<Integer> list = (ArrayList<Integer>) get(BANDS);

        int[] bands = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            bands[i] = list.get(i);
        }
        return bands;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MongoDocument that = (MongoDocument) o;
        return this.getUrl().equals(that.getUrl()) &&
                this.getContentType().equals(that.getContentType()) &&
                this.getDate().equals(that.getDate());
    }


    @Override
    public int hashCode() {
        return getUrl().hashCode() *
                getContentType().hashCode() *
                getDate().hashCode();
    }


    @Override
    public String toString() {
        ISO8601DateFormat formatter = new ISO8601DateFormat();
        String dateString = formatter.format(getDate());
        return "{'" + getUrl() + "' collected at " + dateString + "}";
    }


    @Override
    public Document clone() {
        return new MongoDocument(
                getUrl(), getDate(), getContentType(), getText(),
                getShingles(), getShingleLength(), getBands()
        );
    }
}

