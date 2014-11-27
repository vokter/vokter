package argus.watcher;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;
import java.util.Set;

/**
 * Wrapper class of a JSON request for page watching performed on the index.jsp page.
 * This request is consumed by the 'search' method in the RESTResource class.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public class WatchRequest {

    @JsonProperty
    private String documentUrl;

    @JsonProperty
    private List<String> keywords;

    @JsonProperty
    private int interval;

    @JsonProperty
    private String responseUrl;


    public String getDocumentUrl() {
        return documentUrl;
    }


    public List<String> getKeywords() {
        return keywords;
    }


    public int getInterval() {
        return interval;
    }


    public String getResponseUrl() {
        return responseUrl;
    }
}
