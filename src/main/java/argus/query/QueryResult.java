package argus.query;

import argus.index.Document;
import argus.index.Term;
import com.google.common.collect.LinkedHashMultimap;

/**
 * This class is used as the output for using the 'search(Query)' method of the
 * Collection class, containing a set of sorted documents, each document being
 * mapped with the query terms that each contains.
 * In addition, the total elapsed time is also returned to the user for a better
 * user-experience.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public class QueryResult {

    private final LinkedHashMultimap<Document, Term> matchedDocumentToTerms;
    private final String elapsedTime;


    public QueryResult(LinkedHashMultimap<Document, Term> matchedDocumentToTerms, String elapsedTime) {
        this.matchedDocumentToTerms = matchedDocumentToTerms;
        this.elapsedTime = elapsedTime;
    }


    public LinkedHashMultimap<Document, Term> getMatchedTerms() {
        return matchedDocumentToTerms;
    }


    public String getElapsedTime() {
        return elapsedTime;
    }
}
