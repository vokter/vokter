package argus.query.vectormodel;

import argus.document.Document;
import argus.query.Query;
import argus.term.Term;

/**
 * A merge result of a DocumentVector-Axe and a QueryVector-Axe with the
 * same term, containing a score. This score is used to evaluate document ranking.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public final class MergedAxe {

    private final Document document;
    private final Query query;
    private final Term term;
    private final double score;


    private MergedAxe(final Document document,
                      final Query query,
                      final Term term,
                      final double score) {
        this.document = document;
        this.query = query;
        this.term = term;
        this.score = score;
    }

    public static MergedAxe mergeOf(DocumentVector.Axe docAxe, QueryVector.Axe queryAxe) {
        if (!docAxe.getTerm().equals(queryAxe.getTerm())) {
            throw new RuntimeException("The two axes to be merged must contain the same term!");
        }

        double score = queryAxe.getNlize() * docAxe.getNlize();

        return new MergedAxe(
                docAxe.getDocument(),
                queryAxe.getQuery(),
                docAxe.getTerm(),
                score
        );
    }

    public Document getDocument() {
        return document;
    }

    public Query getQuery() {
        return query;
    }

    public Term getTerm() {
        return term;
    }

    public double getAxeScore() {
        return score;
    }
}
