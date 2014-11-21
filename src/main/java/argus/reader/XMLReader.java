package argus.reader;

import it.unimi.dsi.lang.MutableString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * A reader class that supports reading documents in the XML format.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 2.0
 */
public class XMLReader implements Reader {

    @Override
    public MutableString readDocumentContents(InputStream documentStream) throws IOException {
        LineIterator it = IOUtils.lineIterator(documentStream, "UTF-8");
        MutableString sb = new MutableString();

        while (it.hasNext()) {
            String processedLine = it.next();

            processedLine = StringUtils.replace(processedLine, "</.*?>", "");
            processedLine = StringUtils.replace(processedLine, "<.*?>", "");
            processedLine = StringUtils.replace(processedLine, "<.*?/>", "");

            processedLine = processedLine.trim();

            sb.append(processedLine);

            if (it.hasNext()) {
                sb.append(" ");
            }
        }

        return sb.compact();
    }

    @Override
    public String getSupportedExtension() {
        return "xml";
    }
}
