package argus.query.vectormodel;

import argus.index.Document;
import argus.index.Term;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * This class corresponds to a group of {@link MergedAxe} pointing to the
 * same document, hence, holding a sum of all the scores from different
 * MergedAxes as the final score that ranks the
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public class MergedAxeGroup {


    private final Document document;
    private final Set<Term> terms;
    private final double scoreSum;


    private MergedAxeGroup(Document document, Set<Term> terms, double scoreSum) {
        this.document = document;
        this.terms = terms;
        this.scoreSum = scoreSum;
    }


    public static MergedAxeGroup group(Document document, List<MergedAxe> axes) {

        double scoreSum = axes.stream()
                .mapToDouble(MergedAxe::getAxeScore).sum();

        Set<Term> terms = axes.stream()
                .map(MergedAxe::getTerm)
                .collect(toSet());

        return new MergedAxeGroup(document, terms, scoreSum);
    }


    public Document getDocument() {
        return document;
    }


    public Set<Term> getTerms() {
        return terms;
    }


    public double getScoreSum() {
        return scoreSum;
    }
}
