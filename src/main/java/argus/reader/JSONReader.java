package argus.reader;

import com.google.common.collect.ImmutableSet;
import com.json.parsers.JSONParser;
import com.json.parsers.JsonParserFactory;
import it.unimi.dsi.lang.MutableString;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A reader class that supports reading documents in the JSON format.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 2.0
 */
public class JSONReader implements Reader {

    @Override
    public MutableString readDocumentContents(InputStream documentStream) throws IOException {
        JSONParser jsonParser = JsonParserFactory.getInstance().newJsonParser();

        MutableString sb = new MutableString();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(documentStream))) {
            for (int c; (c = reader.read()) != -1; ) {
                sb.append((char) c);
            }
        }

        Map data = jsonParser.parseJson(sb.toString());
        sb.delete(0, sb.length());
        data.forEach((key, value) -> {
            sb.append(key);
            sb.append(value);
        });

        return sb.compact();
    }

    @Override
    public ImmutableSet<String> getSupportedContentTypes() {
        return ImmutableSet.of("application/json");
    }
}
