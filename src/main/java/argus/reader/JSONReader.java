package argus.reader;

import it.unimi.dsi.lang.MutableString;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
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
        JsonFactory factory = new JsonFactory();

        ObjectMapper mapper = new ObjectMapper(factory);
        JsonNode rootNode = mapper.readTree(documentStream);
        MutableString sb = new MutableString();

        for (Iterator<Map.Entry<String, JsonNode>> it = rootNode.getFields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> field = it.next();
            sb.append(field.getValue().getTextValue());
        }

        return sb.compact();
    }

    @Override
    public String getSupportedExtension() {
        return "json";
    }
}
