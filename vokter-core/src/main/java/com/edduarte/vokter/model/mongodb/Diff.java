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

package com.edduarte.vokter.model.mongodb;

import com.edduarte.vokter.diff.DiffEvent;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.io.Serializable;

/**
 * A difference represents a addition or a removal of an occurrence from a document.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class Diff extends BasicDBObject implements Serializable {

    public static final String DIFF_EVENT = "diff_event";

    public static final String TEXT = "text";

    public static final String START_INDEX = "start_index";

    public static final String END_INDEX = "end_index";

    private static final long serialVersionUID = 1L;


    public Diff(final DiffEvent action,
                final String occurrenceText,
                final int startIndex) {
        super(DIFF_EVENT, action.toString());
        append(TEXT, occurrenceText);
        append(START_INDEX, startIndex);
        append(END_INDEX, startIndex + occurrenceText.length());
    }


    public Diff(DBObject mongoObject) {
        super(mongoObject.toMap());
    }


    /**
     * Returns the status of this difference.
     */
    public DiffEvent getEvent() {
        String event = getString(DIFF_EVENT);
        return DiffEvent.valueOf(event);
    }


    /**
     * Returns the text of the occurrence contained within this difference.
     */
    public String getText() {
        return getString(TEXT);
    }


    public int getStartIndex() {
        return getInt(START_INDEX);
    }


    public int getEndIndex() {
        return getInt(END_INDEX);
    }
}
