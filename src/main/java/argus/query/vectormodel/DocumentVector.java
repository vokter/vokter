package argus.query.vectormodel;

import argus.document.DocumentCollection;
import argus.document.Document;
import argus.term.Term;

import java.util.Set;
import java.util.stream.Stream;

/**
 * A document-to-terms pair that represents a vector in the vector space model,
 * where documents are single points and where terms are axes of the space.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public class DocumentVector {

    private final DocumentCollection parentCollection;
    private final Document point;
    private final Set<Term> axes;


    public DocumentVector(final DocumentCollection parentCollection,
                          final Document document,
                          final Set<Term> terms) {
        this.parentCollection = parentCollection;
        this.point = document;
        this.axes = terms;
    }


    public Document getDocument() {
        return point;
    }


    public Stream<Axe> getWeightedAxes() {
        return axes.stream().map(t -> {
            // the nlize value is already stored in the index, so no calculations
            // are required (optimizing query speeds)
            double nlize = t.getNormalizedWeight();
            return new Axe(point, t, nlize);
        });
    }


    /**
     * Represents a weighted axe (a term with a nlize value) that starts at this
     * vector's point (a document).
     */
    public final static class Axe {

        private final Document document;
        private final Term term;
        private final double nlize;

        public Axe(final Document document,
                   final Term term,
                   final double nlize) {
            this.document = document;
            this.term = term;
            this.nlize = nlize;
        }

        public Document getDocument() {
            return document;
        }

        public Term getTerm() {
            return term;
        }

        public double getNlize() {
            return nlize;
        }
    }
}
