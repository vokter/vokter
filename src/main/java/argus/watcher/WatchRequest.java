package argus.watcher;

import argus.index.Document;
import org.codehaus.jackson.annotate.JsonProperty;

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
    private String responseUrl;

    @JsonProperty
    private String documentUrl;

    @JsonProperty
    private Set<String> keywords;


    public String getDocumentUrl() {
        return documentUrl;
    }


    public String getResponseUrl() {
        return responseUrl;
    }


    public Set<String> getKeywords() {
        return keywords;
    }
}
