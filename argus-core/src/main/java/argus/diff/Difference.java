package argus.diff;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.io.Serializable;

/**
 * A difference represents a addition or a removal of an occurrence from a document.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class Difference extends BasicDBObject implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String DIFF_ACTION = "diff_action";
    public static final String OCCURRENCE_TEXT = "occurrence_text";
    public static final String SNIPPET = "snippet";

    public Difference(final DifferenceAction action,
                      final String occurrenceText,
                      final String snippet) {
        super(DIFF_ACTION, action.toString());
        append(OCCURRENCE_TEXT, occurrenceText);
        append(SNIPPET, snippet);
    }

    public Difference(DBObject mongoObject) {
        super(mongoObject.toMap());
    }


    /**
     * Returns the status of this difference.
     */
    public DifferenceAction getAction() {
        String action = getString(DIFF_ACTION);
        return DifferenceAction.valueOf(action);
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
