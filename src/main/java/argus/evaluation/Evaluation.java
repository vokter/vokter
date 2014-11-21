package argus.evaluation;

import argus.index.Collection;
import argus.index.Document;
import argus.query.Query;
import argus.query.QueryBuilder;
import argus.query.QueryResult;
import com.google.common.base.Stopwatch;
import gnu.trove.list.array.TDoubleArrayList;
import it.unimi.dsi.lang.MutableString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Module that performs precision and recall evaluation of queries over a specified
 * collection and using the specified evaluation parameters.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public class Evaluation {

    private static final Logger logger = LoggerFactory.getLogger(Evaluation.class);

    private final Collection collection;
    private final QueryBuilder queryBuilder;
    private final List<EvaluationParam> evaluationParams;

    public Evaluation(final Collection collection,
                      final QueryBuilder queryBuilder,
                      final List<EvaluationParam> evaluationParams) {
        this.collection = collection;
        this.queryBuilder = queryBuilder;
        this.evaluationParams = evaluationParams;
    }

    public void evaluate() {

        Stopwatch sw = Stopwatch.createStarted();

        TDoubleArrayList averages = new TDoubleArrayList();
        for (EvaluationParam ev : evaluationParams) {
            queryBuilder.withText(new MutableString(ev.getText()));
            queryBuilder.withSlop(ev.getSlop());


            logger.info("");
            Query query = queryBuilder.build();
            QueryResult result = collection.search(query);

            List<String> retrieved = result.getMatchedTerms()
                    .keySet()
                    .stream()
                    .map(Document::getPath)
                    .limit(500) // return only the first 500 documents, as requested
                    .collect(toList());
            List<String> relevant = ev.getExpectedDocuments().stream().limit(500).collect(Collectors.toList());

            long tp = retrieved.stream().filter(relevant::contains).count();
            long fp = retrieved.stream().filter(d -> !relevant.contains(d)).count();
            long fn = relevant.stream().filter(d -> !retrieved.contains(d)).count();

            logger.info("Q:" + ev.getId() + ":\"" + ev.getText() + "\"~" + ev.getSlop());
            logger.info("\t true positive = " + tp);
            logger.info("\t false positive = " + fp);
            logger.info("\t false negative = " + fn);

            double precision = (double) tp / ((double) tp + (double) fp);
            double recall = (double) tp / ((double) tp + (double) fn);
            double fmeasure = (2 * recall * precision) / (recall + precision);

            logger.info("\t precision = " + precision);
            logger.info("\t recall = " + recall);
            logger.info("\t F measure = " + fmeasure);

            double tps = 0;
            TDoubleArrayList matchedRelevant = new TDoubleArrayList();
            int count = 1;
            for (String docName : retrieved) {
                if (relevant.contains(docName)) {
                    // same document at same position for both lists
                    tps++;
                    double averagePrecision = tps / count;
                    matchedRelevant.add(averagePrecision);
                }
                count++;
            }
            double averageRankingPrecision = matchedRelevant.sum() / matchedRelevant.size();
            averages.add(averageRankingPrecision);

            logger.info("\t average ranking precision = " + averageRankingPrecision);
        }

        sw.stop();
        double map = averages.sum() / averages.size();

        logger.info("");
        logger.info("Evaluation elapsed time: " + sw.toString());
        logger.info("Mean Average Precision (MAP): " + map);
        logger.info("");

    }
}
