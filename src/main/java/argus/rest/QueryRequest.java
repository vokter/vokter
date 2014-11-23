package argus.rest;

import org.codehaus.jackson.annotate.JsonProperty;


public class QueryRequest {

    @JsonProperty
    public String query;

    @JsonProperty
    public int slop;
}
