package argus.reader;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.lang.MutableString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A reader class that supports reading documents in plain-text format.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 2.0
 */
public class PlainTextReader implements Reader {

    @Override
    public MutableString readDocumentContents(InputStream documentStream) throws IOException {
        MutableString sb = new MutableString();
        try (BufferedReader rdr = new BufferedReader(new InputStreamReader(documentStream))) {
            for (int c; (c = rdr.read()) != -1; ) {
                sb.append((char) c);
            }
        }
        return sb.compact();
    }

    @Override
    public ImmutableSet<String> getSupportedExtensions() {
        return ImmutableSet.of("txt");
    }
}
