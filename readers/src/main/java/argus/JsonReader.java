package argus;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import it.unimi.dsi.lang.MutableString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * A reader class that supports reading documents in the JSON format.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 2.0
 */
public class JsonReader implements argus.reader.Reader {

    @Override
    public MutableString readDocumentContents(InputStream documentStream) throws IOException {
        Gson jsonParser = new Gson();

        MutableString sb = new MutableString();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(documentStream))) {
            for (int c; (c = reader.read()) != -1; ) {
                sb.append((char) c);
            }
        }

        Map data = jsonParser.fromJson(sb.toString(), Map.class);
        sb.delete(0, sb.length());
        data.forEach((key, value) -> {
            sb.append(key);
            sb.append(' ');
            sb.append(value);
            sb.append(' ');
        });

        return sb.compact();
    }

    @Override
    public ImmutableSet<String> getSupportedContentTypes() {
        return ImmutableSet.of("application/json");
    }
}
