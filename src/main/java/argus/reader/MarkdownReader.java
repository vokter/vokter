package argus.reader;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.lang.MutableString;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Eduardo Duarte (<a href="mailto:eduarte@ubiwhere.com">eduarte@ubiwhere.com</a>)
 * @version 1.0
 */
public class MarkdownReader implements Reader {

    @Override
    public MutableString readDocumentContents(InputStream documentStream) throws IOException {
        return null;
    }

    @Override
    public ImmutableSet<String> getSupportedContentTypes() {
        return ImmutableSet.of("md", "markdown");
    }
}
