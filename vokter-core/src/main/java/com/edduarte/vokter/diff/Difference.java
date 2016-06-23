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

package com.edduarte.vokter.diff;

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

    public static final String OCCURRENCE_TEXT = "occurrence_text";

    public static final String SNIPPET = "snippet";

    private static final long serialVersionUID = 1L;


    public Difference(final DifferenceEvent action,
                      final String occurrenceText,
                      final String snippet) {
        super(DIFF_EVENT, action.toString());
        append(OCCURRENCE_TEXT, occurrenceText);
        append(SNIPPET, snippet);
    }


    public Difference(DBObject mongoObject) {
        super(mongoObject.toMap());
    }


    /**
     * Returns the status of this difference.
     */
    public DifferenceEvent getAction() {
        String action = getString(DIFF_EVENT);
        return DifferenceEvent.valueOf(action);
    }


    /**
     * Returns the text of the occurrence contained within this difference.
     */
    public String getOccurrenceText() {
        return getString(OCCURRENCE_TEXT);
    }


    /**
     * Returns the snippet of this difference in the original document (non-processed).
     */
    public String getSnippet() {
        return getString(SNIPPET);
    }

}
