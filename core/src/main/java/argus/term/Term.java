package argus.term;

import com.mongodb.BasicDBObject;
import it.unimi.dsi.lang.MutableString;

import java.util.ArrayList;


/**
 * A Term represents the most basic information unit, corresponding to a
 * indexable and searchable field of text.
 * <p>
 * This class was named Token in the previous assignment.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 2.0
 */
public final class Term extends BasicDBObject {

    public static final String TEXT = "text";
    public static final String WEIGHT = "weight";
    public static final String OCCURRENCES = "occurrences";


    /**
     * Constructor used by the DocumentPipeline, when the specified text
     * corresponds to a new, unique term in the parent document.
     */
    public Term(final MutableString text) {
        super(TEXT, text.toString());
        append(WEIGHT, 1);
        append(OCCURRENCES, new ArrayList<>());
    }


    /**
     * Constructor used by the TermLoader, when obtaining a term from a query to
     * the MongoDB term collection.
     */
    public Term(final BasicDBObject mongoObject) {
        super(mongoObject);
    }


    /**
     * Sets this term as occurring in the specified document at the specified
     * phrase pointer and at the specified starting and ending positions.
     */
    public void addOccurrence(int position, int start, int end) {
        ArrayList occurrencesArray = (ArrayList) get(OCCURRENCES);
        occurrencesArray.add(new Occurrence(position, start, end));
    }


    /**
     * Sets this term normalized weight factor.
     */
    public void addNormalizedWeight(double nlize) {
        put(WEIGHT, nlize);
    }


    /**
     * Returns this term normalized weight factor.
     */
    public double getNormalizedWeight() {
        return (double) get(WEIGHT);
    }


    /**
     * Returns the term frequency (tf) of this term t in the document d, which is
     * the number of occurrences of t in d.
     */
    public int getTermFrequency() {
        ArrayList occurrencesArray = (ArrayList) get(OCCURRENCES);
        return occurrencesArray.size();
    }


    /**
     * Returns this term weight based on the log tf weighting scheme.
     */
    public double getLogFrequencyWeight() {
        double tf = getTermFrequency();

        if (tf > 0) {
            return 1.0 + Math.log10(tf);

        } else {
            return 0;
        }
    }


    /**
     * Returns the text that represents this term.
     */
    @Override
    public String toString() {
        return getString(TEXT);
    }

}

