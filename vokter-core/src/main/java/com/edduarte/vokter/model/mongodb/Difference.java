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

import com.edduarte.vokter.diff.DifferenceEvent;
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
public class Difference extends BasicDBObject implements Serializable {

    public static final String DIFF_EVENT = "diff_event";

    public static final String TEXT = "text";

    public static final String START_INDEX = "start_index";

    private static final long serialVersionUID = 1L;


    public Difference(final DifferenceEvent action,
                      final String occurrenceText,
                      final int startIndex) {
        super(DIFF_EVENT, action.toString());
        append(TEXT, occurrenceText);
        append(START_INDEX, startIndex);
    }


    public Difference(DBObject mongoObject) {
        super(mongoObject.toMap());
    }


    /**
     * Returns the status of this difference.
     */
    public DifferenceEvent getEvent() {
        String event = getString(DIFF_EVENT);
        return DifferenceEvent.valueOf(event);
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
}
