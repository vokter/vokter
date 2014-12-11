package argus.job;

import argus.keyword.Keyword;
import com.aliasi.util.Pair;
import com.mongodb.BasicDBObject;

import java.util.List;
import java.util.stream.Stream;

/**
 * TODO
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class Job extends BasicDBObject {

    private String documentUrl;

    private List<Keyword> keywords;

    private int interval;

    private String responseUrl;

    public Job(String documentUrl, List<Keyword> keywords, int interval, String responseUrl) {
        this.documentUrl = documentUrl;
        this.keywords = keywords;
        this.interval = interval;
        this.responseUrl = responseUrl;
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
}
