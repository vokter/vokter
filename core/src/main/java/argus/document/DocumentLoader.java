package argus.document;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DeflateSerializer;
import it.unimi.dsi.lang.MutableString;
import org.apache.commons.io.FileDeleteStrategy;
import org.cache2k.CacheSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;

import static argus.util.Constants.DOCUMENTS_DIR;

/**
 * A cache loader that reads local document files separated by first-character of
 * tokens. This cache loader is used by a LoadingCache, instantiated at the
 * CollectionBuilder, to manage temporary in-memory tokens.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public final class DocumentLoader implements CacheSource<String, Document> {

    private static final Logger logger = LoggerFactory.getLogger(DocumentLoader.class);

    /**
     * For document serialization, the third-party library Kryo is used
     * due to its optimization speeds and String compression capabilities.
     */
    private static final Kryo kryo = new Kryo();

    static {
        // registers an implementation on how to write the document content
        kryo.register(MutableString.class, new DeflateSerializer(new Serializer<MutableString>() {
            @Override
            public void write(Kryo kryo, Output output, MutableString o) {
                output.writeString(o);
            }

            @Override
            public MutableString read(Kryo kryo, Input input, Class aClass) {
                return new MutableString(input.readString());
            }
        }));
    }


    DocumentLoader() {
        if (DOCUMENTS_DIR.exists() && !DOCUMENTS_DIR.isDirectory()) {
            try {
                FileDeleteStrategy.FORCE.delete(DOCUMENTS_DIR);
            } catch (IOException e) {
                DOCUMENTS_DIR.delete();
            }
        }
        DOCUMENTS_DIR.mkdirs();
    }


    @Override
    public Document get(String documentUrl) {
        String documentFilename;
        try {
            documentFilename = URLEncoder.encode(documentUrl, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            logger.error("Error while reading document with url '" + documentUrl + "'.", ex);
            return null;
        }

        File documentFile = new File(DOCUMENTS_DIR, documentFilename);

        if (documentFile.exists() && !documentFile.isDirectory()) {
            try (InputStream inputStream = new FileInputStream(documentFile);
                 Input in = new Input(inputStream)) {
                return kryo.readObject(in, Document.class);
            } catch (IOException ex) {
                logger.error("Error while reading document with url '" + documentUrl + "'.", ex);
            }
        }

        // if this point is reached, then there are no stored
        // documents with the specified id
        return null;
    }

    public void write(Document document) {
        String documentUrl = document.getUrl();
        String documentFilename;
        try {
            documentFilename = URLEncoder.encode(documentUrl, "UTF-8");

        } catch (UnsupportedEncodingException ex) {
            logger.error("Error while writing document with url '" + documentUrl + "'.", ex);
            return;
        }

        File documentFile = new File(DOCUMENTS_DIR, documentFilename);

        try (OutputStream outputStream = new FileOutputStream(documentFile);
             Output out = new Output(outputStream)) {
            kryo.writeObject(out, document);

        } catch (IOException ex) {
            logger.error("Error while writing document with url '" + documentUrl + "'.", ex);
        }
    }
}
