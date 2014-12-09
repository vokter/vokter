package argus.job;

import argus.document.DocumentCollection;
import argus.parser.ParserPool;
import argus.util.SynchronizedCounter;

/**
 * TODO
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class JobExecution implements Runnable {

    private static final long FAULT_TOLERANCE = 10;

    private final DocumentCollection collection;
    private final JobRequest request;
    private final ParserPool parserPool;
    private final SynchronizedCounter faultCount;

    public JobExecution(final DocumentCollection collection,
                        final ParserPool parserPool,
                        final JobRequest request) {
        this.collection = collection;
        this.parserPool = parserPool;
        this.request = request;
        this.faultCount = new SynchronizedCounter();
    }

    @Override
    public void run() {
//
//        String documentUrl = request.getDocumentUrl();
//
//        Document oldSnapshot = collection.getDocumentForId(documentUrl);
//        if (oldSnapshot == null) {
            long currentUnsuccessful = faultCount.getAndIncrement();
            if (currentUnsuccessful == FAULT_TOLERANCE) {
//                Context.getInstance().cancelScheduledJob(documentUrl);
            }
//            return;
//        }
//
//        Document currentSnapshot = DocumentBuilder.fromUrl(documentUrl)
//                .withStopwords()
//                .withStemming()
//                .ignoreCase()
//                .build();
//
//        DocumentComparison comparator = new DocumentComparison();
//        List<Discrepancy> discrepanciesFound = comparator.compare(oldSnapshot, currentSnapshot);
//
//
//        documentUrl = null;
//        oldSnapshot = null;
//        currentSnapshot = null;
//        comparator = null;
//
        faultCount.reset();
    }
}
