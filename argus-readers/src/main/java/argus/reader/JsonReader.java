package argus.reader;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.lang.MutableString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A reader class that supports reading documents in the JSON format.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 2.0
 */
public class JsonReader implements argus.reader.Reader {

    private final Gson jsonParser;

    public JsonReader() {
        jsonParser = new Gson();
    }

    @Override
    public MutableString readDocumentContents(InputStream documentStream) throws IOException {
        MutableString sb = new MutableString();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(documentStream))) {
            for (int c; (c = reader.read()) != -1; ) {
                sb.append((char) c);
            }
        }

        JsonElement fullText = jsonParser.fromJson(sb.toString(), JsonElement.class);
        sb.delete(0, sb.length());
        readRecursive(fullText, sb);

        return sb.compact();
    }

    private void readRecursive(JsonElement root, MutableString collector) {
        if (root.isJsonObject()) {
            JsonObject object = (JsonObject) root;
            object.entrySet().forEach(e -> {
                collector.append(e.getKey());
                collector.append(' ');
                readRecursive(e.getValue(), collector);
                collector.append(' ');
            });
        } else if (root.isJsonArray()) {
            JsonArray array = (JsonArray) root;
            array.forEach(child -> {
                readRecursive(child, collector);
                collector.append(' ');
            });
        } else if (root.isJsonPrimitive()) {
            JsonPrimitive primitive = (JsonPrimitive) root;
            collector.append(primitive.getAsString());
            collector.append(' ');
        }
    }

    @Override
    public ImmutableSet<String> getSupportedContentTypes() {
        return ImmutableSet.of("application/json");
    }
}
