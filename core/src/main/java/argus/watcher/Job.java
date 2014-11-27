package argus.watcher;

import argus.Context;
import argus.comparison.Discrepancy;
import argus.document.Document;
import argus.document.DocumentBuilder;
import argus.document.DocumentCollection;
import argus.comparison.DocumentComparison;
import argus.util.SynchronizedCounter;

import java.util.List;

/**
 * TODO
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class Job implements Runnable {

    private DocumentCollection collection;
    private WatchRequest request;
    private final SynchronizedCounter numSuccessfulExecs;
    private final SynchronizedCounter numUnsuccessfulExecs;

    public Job(final DocumentCollection collection,
               final WatchRequest request) {
        this.collection = collection;
        this.request = request;
        this.numSuccessfulExecs = new SynchronizedCounter();
        this.numUnsuccessfulExecs = new SynchronizedCounter();
    }

    @Override
    public void run() {

        String documentUrl = request.getDocumentUrl();

        Document oldSnapshot = collection.getDocumentForId(documentUrl);
        if (oldSnapshot == null) {
            long currentUnsuccessful = numUnsuccessfulExecs.getAndIncrement();
            if (currentUnsuccessful == 10) {
                Context.getInstance().cancelScheduledJob(documentUrl);
            }
            return;
        }

        Document currentSnapshot = DocumentBuilder.fromUrl(documentUrl)
                .withStopwords()
                .withStemming()
                .ignoreCase()
                .build();

        DocumentComparison comparator = new DocumentComparison();
        List<Discrepancy> discrepanciesFound = comparator.compare(oldSnapshot, currentSnapshot);



        documentUrl = null;
        oldSnapshot = null;
        currentSnapshot = null;
        comparator = null;

        numSuccessfulExecs.getAndIncrement();
        numUnsuccessfulExecs.reset();
    }

}
