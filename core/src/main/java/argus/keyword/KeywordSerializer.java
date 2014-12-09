package argus.keyword;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * TODO
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class KeywordSerializer implements JsonSerializer<Keyword> {

    @Override
    public JsonElement serialize(final Keyword src,
                                 final Type typeOfSrc,
                                 final JsonSerializationContext context) {
        return new JsonPrimitive(src.originalInput);
    }
}
