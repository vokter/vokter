package argus.rest;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class CancelRequest {

    @JsonProperty
    public String documentUrl;

    @JsonProperty
    public String responseUrl;
}
