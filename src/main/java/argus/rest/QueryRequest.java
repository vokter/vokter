package argus.rest;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Wrapper class of a JSON request for queries performed on the index.jsp page.
 * This request is consumed by the 'search' method in the RESTResource class.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public class QueryRequest {

    @JsonProperty
    public String query;

    @JsonProperty
    public int slop;
}
