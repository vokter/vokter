package argus.document;

import argus.util.Constants;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DeflateSerializer;
import it.unimi.dsi.lang.MutableString;
import org.cache2k.CacheSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
        if (Constants.DOCUMENTS_DIR.exists() && !Constants.DOCUMENTS_DIR.isDirectory()) {
            Constants.deleteFile(Constants.DOCUMENTS_DIR);
        }
        Constants.DOCUMENTS_DIR.mkdirs();
    }

    private static String urlToFilename(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");

        } catch (UnsupportedEncodingException ex) {
            logger.error("Error while writing document with url '" + url + "'.", ex);
            return null;
        }
    }

    public void write(Document document) {
        String documentUrl = document.getUrl();
        String documentFilename = urlToFilename(documentUrl);
        if (documentFilename == null) {
            return;
        }

        File documentFile = new File(Constants.DOCUMENTS_DIR, documentFilename);
        try {
            if (documentFile.exists()) {
                Constants.deleteFile(documentFile);
            }
            OutputStream outputStream = new FileOutputStream(documentFile);
            Output out = new Output(outputStream);
            kryo.writeObject(out, document);
            out.close();
            outputStream.close();

        } catch (IOException ex) {
            logger.error("Error while writing document with url '" + documentUrl + "'.", ex);
        }

        logger.info("Added a new document for url " + documentUrl);
        String diskSize = Constants.fileSizeToString(Constants.folderSize(Constants.DOCUMENTS_DIR));
        logger.info("Current disk size: " + diskSize);
    }

    public void delete(String documentUrl) {
        String documentFilename = urlToFilename(documentUrl);
        if (documentFilename == null) {
            return;
        }
        File documentFile = new File(Constants.DOCUMENTS_DIR, documentFilename);
        if (documentFile.exists() && !documentFile.isDirectory()) {
            Constants.deleteFile(documentFile);
        }
    }

    @Override
    public Document get(String documentUrl) {
        String documentFilename = urlToFilename(documentUrl);
        if (documentFilename == null) {
            return null;
        }
        File documentFile = new File(Constants.DOCUMENTS_DIR, documentFilename);
        if (documentFile.exists() && !documentFile.isDirectory()) {
            try (InputStream inputStream = new FileInputStream(documentFile);
                 Input in = new Input(inputStream)) {
                return kryo.readObject(in, Document.class);
            } catch (IOException ex) {
                logger.error("Error while reading document with url '" + documentUrl + "'.", ex);
            }
        }
        // if this point is reached, then there are no stored
        // documents with the specified url
        return null;
    }
}
