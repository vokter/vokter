package argus.document;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
* @version 1.0
*/
public class EMEAPlainEvaluation {

    private static final Logger logger = LoggerFactory.getLogger(EMEAPlainEvaluation.class);

    @Test
    public void test() {
//        String corpusDir = Main.class.getResource("EMEA_plain").getPath();
//
//        // loads the default evaluation file.
//        InputStream evaluationFileStream = Main.class.getResourceAsStream("evaluate.txt");
//        EvaluationFileLoader evLoader = new EvaluationFileLoader();
//        List<EvaluationParam> evaluationParams = evLoader.load(evaluationFileStream, false);
//        evLoader = null;
//
//        // loads the default stopwords file
//        InputStream stopwordsFileStream = Main.class.getResourceAsStream("stopwords.txt");
//        StopwordFileLoader stopLoader = new StopwordFileLoader();
//        Set<MutableString> loadedStopwords = stopLoader.load(stopwordsFileStream);
//        stopLoader = null;
//
//        DocumentCollectionBuilder cb = DocumentCollectionBuilder.fromDir(Paths.get(corpusDir));
//        QueryBuilder qb = QueryBuilder.newBuilder();
//
//        cb.withStopwords(loadedStopwords);
//        qb.withStopwords(loadedStopwords);
//
//        cb.withStemmer(PortugueseStemmer.class);
//        qb.withStemmer(PortugueseStemmer.class);
//
//        cb.ignoreCase();
//        qb.ignoreCase();
//
//        File indexFolder = new File(Constants.INSTALL_DIR, "index");
//        indexFolder.mkdirs();
//        File documentsFolder = new File(Constants.INSTALL_DIR, "documents");
//        documentsFolder.mkdirs();
//
//        // index EMEA plain text corpus
//        DocumentCollection collection = cb.buildInFolders(indexFolder, documentsFolder);
//
//        // startServer evaluation
//        Stopwatch sw = Stopwatch.createStarted();
//
//        TDoubleArrayList averages = new TDoubleArrayList();
//        for (EvaluationParam ev : evaluationParams) {
//            qb.withText(new MutableString(ev.getText()));
//            qb.withSlop(ev.getSlop());
//
//
//            logger.info("");
//            Query query = qb.build();
//            QueryResult result = collection.search(query);
//
//            List<String> retrieved = result.getMatchedTerms()
//                    .keySet()
//                    .stream()
//                    .map(Document::getUrl)
//                    .limit(500) // return only the first 500 documents, as requested
//                    .collect(toList());
//            List<String> relevant = ev.getExpectedDocuments().stream().limit(500).collect(toList());
//
//            long tp = retrieved.stream().filter(relevant::contains).count();
//            long fp = retrieved.stream().filter(d -> !relevant.contains(d)).count();
//            long fn = relevant.stream().filter(d -> !retrieved.contains(d)).count();
//
//            logger.info("Q:" + ev.getId() + ":\"" + ev.getText() + "\"~" + ev.getSlop());
//            logger.info("\t true positive = " + tp);
//            logger.info("\t false positive = " + fp);
//            logger.info("\t false negative = " + fn);
//
//            double precision = (double) tp / ((double) tp + (double) fp);
//            double recall = (double) tp / ((double) tp + (double) fn);
//            double fmeasure = (2 * recall * precision) / (recall + precision);
//
//            logger.info("\t precision = " + precision);
//            logger.info("\t recall = " + recall);
//            logger.info("\t F measure = " + fmeasure);
//
//            double tps = 0;
//            TDoubleArrayList matchedRelevant = new TDoubleArrayList();
//            int count = 1;
//            for (String docName : retrieved) {
//                if (relevant.contains(docName)) {
//                    // same document at same position for both lists
//                    tps++;
//                    double averagePrecision = tps / count;
//                    matchedRelevant.add(averagePrecision);
//                }
//                count++;
//            }
//            double averageRankingPrecision = matchedRelevant.sum() / matchedRelevant.size();
//            averages.add(averageRankingPrecision);
//
//            logger.info("\t average ranking precision = " + averageRankingPrecision);
//        }
//
//        sw.stop();
//        double map = averages.sum() / averages.size();
//
//        logger.info("");
//        logger.info("Evaluation elapsed time: " + sw.toString());
//        logger.info("Mean Average Precision (MAP): " + map);
//        logger.info("");
    }
}
