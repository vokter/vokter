package argus.reader;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.lang.MutableString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import java.io.IOException;
import java.io.InputStream;

import static org.apache.commons.lang3.StringUtils.replace;

/**
 * A reader class that supports reading documents in the XML format.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 2.0
 */
public class MarkupReader implements Reader {

    @Override
    public MutableString readDocumentContents(InputStream documentStream) throws IOException {
        LineIterator it = IOUtils.lineIterator(documentStream, "UTF-8");
        MutableString sb = new MutableString();

        while (it.hasNext()) {
            String processedLine = it.next();

            processedLine = replace(processedLine, "</.*?>", "");
            processedLine = replace(processedLine, "<.*?>", "");
            processedLine = replace(processedLine, "<.*?/>", "");

            processedLine = processedLine.trim();

            sb.append(processedLine);

            if (it.hasNext()) {
                sb.append(" ");
            }
        }

        return sb.compact();
    }

    @Override
    public ImmutableSet<String> getSupportedExtensions() {
        return ImmutableSet.of("html", "htm", "dhtml", "xhtml", "xml", "xsl", "xss", "atom", "rss", "asp", "aspx", "mspx", "jsp", "jspx", "php", "phtml", "rhtml");
    }
}
