package argus.evaluation;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a query to be evaluated, using the contained input text and slop
 * factor. Evaluation is performed by analysing the precision, recall and
 * ranking precision of the query results with the contained relevant documents.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public class EvaluationParam {

    private final String id;
    private final String text;
    private final int slop;
    private final List<String> expectedDocuments;

    public EvaluationParam(final String id,
                           final String text,
                           final int slop,
                           final List<String> expectedDocuments) {
        this.id = id;
        this.text = text;
        this.slop = slop;
        this.expectedDocuments = expectedDocuments;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public int getSlop() {
        return slop;
    }

    public List<String> getExpectedDocuments() {
        return new ArrayList<>(expectedDocuments); // create new list to avoid modifications
    }
}
