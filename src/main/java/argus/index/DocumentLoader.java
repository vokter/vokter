package argus.index;

import argus.util.Util;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.esotericsoftware.kryo.serializers.DeflateSerializer;
import it.unimi.dsi.lang.MutableString;
import org.apache.commons.io.FileDeleteStrategy;
import org.cache2k.CacheSource;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.*;

/**
 * A cache loader that reads local document files separated by first-character of
 * tokens. This cache loader is used by a LoadingCache, instantiated at the
 * CollectionBuilder, to manage temporary in-memory tokens.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public final class DocumentLoader implements CacheSource<Long, Document> {

    private static final String DATABASE_NAME = "doc_db";

    private File parentDocumentsFolder;

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
        parentDocumentsFolder = new File(Util.INSTALL_DIR, DATABASE_NAME);
        if (parentDocumentsFolder.exists() && !parentDocumentsFolder.isDirectory()) {
            try {
                FileDeleteStrategy.FORCE.delete(parentDocumentsFolder);
            } catch (IOException e) {
                parentDocumentsFolder.delete();
            }
        }
        parentDocumentsFolder.mkdirs();
    }


    @Override
    @ParametersAreNonnullByDefault
    public Document get(Long documentId) throws Exception {
        File documentFile = new File(parentDocumentsFolder, Long.toString(documentId));

        if (documentFile.exists() && !documentFile.isDirectory()) {
            try (InputStream inputStream = new FileInputStream(documentFile);
                 Input in = new Input(inputStream)) {
                return kryo.readObject(in, Document.class);
            }
        }

        // if this point is reached, then there are no stored
        // documents with the specified id
        return null;
    }

    @ParametersAreNonnullByDefault
    public void write(Document document) {
        File documentFile = new File(parentDocumentsFolder, Long.toString(document.getId()));

        try (OutputStream outputStream = new FileOutputStream(documentFile);
             Output out = new Output(outputStream)) {
            kryo.writeObject(out, document);

        } catch (IOException ex) {
            System.err.println("Error while writing document with id '" + document.getId() + "'.");
            ex.printStackTrace();
        }
    }
}
