package argus.rest;

import com.google.gson.Gson;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class RestResponse {

    @JsonProperty
    private final Code code;

    @JsonProperty
    private final String message;

    public RestResponse(Code code, String message) {
        this.code = code;
        this.message = message;
    }

    public static enum Code {
        ok, error
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
