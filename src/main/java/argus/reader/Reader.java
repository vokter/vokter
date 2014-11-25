package argus.reader;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.lang.MutableString;

import java.io.IOException;
import java.io.InputStream;

/**
 * Indexing module that reads an {@link java.io.InputStream} of a document in a
 * specific format / extension, and provides the most optimized state of its textual
 * content according to that extension.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 2.0
 */
public interface Reader {

    MutableString readDocumentContents(InputStream documentStream) throws IOException;

    ImmutableSet<String> getSupportedContentTypes();
}
