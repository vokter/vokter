package argus.rest;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Wrapper class of a JSON request for page watching.
 * This request is consumed by the 'watch' method in the RESTResource class.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public class WatchRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private final String documentUrl;

    @JsonProperty
    private final String responseUrl;

    @JsonProperty
    private final List<String> keywords;

    @JsonProperty
    private final int interval;

    public WatchRequest(final String documentUrl,
                        final String responseUrl,
                        final List<String> keywords,
                        final int interval) {
        this.documentUrl = documentUrl;
        this.responseUrl = responseUrl;
        this.keywords = keywords;
        this.interval = interval;
    }

    public String getRequestUrl() {
        return documentUrl;
    }

    public String getResponseUrl() {
        return responseUrl;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public int getInterval() {
        return interval;
    }
}
