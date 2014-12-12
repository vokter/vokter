package argus.job.workers;

import argus.keyword.Keyword;
import argus.util.Constants;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;
import java.util.stream.Stream;

/**
 * TODO
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class MatchWorker implements Job {

    private final String id;

    private final String documentUrl;

    private final List<Keyword> keywords;

    private final int interval;

    private final String responseUrl;

    public MatchWorker(final String documentUrl,
                       final List<Keyword> keywords,
                       final int interval,
                       final String responseUrl) {
        this.id = Constants.bytesToHex(Constants.generateRandomBytes());
        this.documentUrl = documentUrl;
        this.keywords = keywords;
        this.interval = interval;
        this.responseUrl = responseUrl;
    }

    public String getId() {
        return id;
    }


    public String getDocumentUrl() {
        return documentUrl;
    }

    public Stream<Keyword> keywordStream() {
        return keywords.stream();
    }

    public int getInterval() {
        return interval;
    }


    public String getResponseUrl() {
        return responseUrl;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

    }
}
