package argus.query.vectormodel;

import argus.index.Collection;
import argus.index.Term;
import argus.query.Query;

import java.util.Set;
import java.util.stream.Stream;

/**
 * A query-to-terms pair that represents a vector in the vector space model, where
 * queries are single points and where terms are axes of the space.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public class QueryVector {

    private final Collection parentCollection;
    private final Query point;
    private final Set<Term> axes;

    public QueryVector(final Collection parentCollection,
                       final Query query,
                       final Set<Term> terms) {
        this.parentCollection = parentCollection;
        this.point = query;
        this.axes = terms;
    }


    public Query getQuery() {
        return point;
    }


    public Set<Term> getTerms() {
        return axes;
    }


    public Stream<Axe> getWeightedAxes() {
        int N = parentCollection.getN();

        final double vectorValue = Math.sqrt(
                axes.stream()
                        .mapToDouble(t -> Math.pow(t.getInverseDocumentFrequency(N), 2))
                        .sum()
        );

        return axes.stream().map(t -> {
            double nlize = t.getInverseDocumentFrequency(N) / vectorValue;
            return new Axe(point, t, nlize);
        });
    }


    /**
     * Represents a weighted axe (a term with a nlize value) that starts at this
     * vector's point (a query).
     */
    public final static class Axe {

        private final Query query;
        private final Term term;
        private final double nlize;

        public Axe(final Query query,
                   final Term term,
                   final double nlize) {
            this.query = query;
            this.term = term;
            this.nlize = nlize;
        }

        public Query getQuery() {
            return query;
        }

        public Term getTerm() {
            return term;
        }

        public double getNlize() {
            return nlize;
        }
    }
}
