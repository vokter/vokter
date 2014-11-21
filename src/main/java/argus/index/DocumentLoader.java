package argus.index;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.cache.CacheLoader;
import it.unimi.dsi.lang.MutableString;

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
public final class DocumentLoader extends CacheLoader<Long, Document> {

    private File parentDocumentsFolder;

    /**
     * For document serialization, the third-party library Kryo is used
     * due to its optimization speeds and String compression capabilities.
     */
    private static final Kryo kryo = new Kryo();

    static {
        // registers an implementation on how to write the document content
        kryo.register(MutableString.class, new Serializer<MutableString>() {
            @Override
            public void write(Kryo kryo, Output output, MutableString o) {
                output.writeString(o);
            }

            @Override
            public MutableString read(Kryo kryo, Input input, Class aClass) {
                return new MutableString(input.readString());
            }
        });
    }


    DocumentLoader(File parentDocumentsFolder) {
        this.parentDocumentsFolder = parentDocumentsFolder;
    }

    @Override
    @ParametersAreNonnullByDefault
    public Document load(Long documentId) throws Exception {
        File documentFile = new File(parentDocumentsFolder, Long.toString(documentId));

        if (documentFile.exists() && !documentFile.isDirectory()) {
            InputStream inputStream = new FileInputStream(documentFile);
            Input in = new Input(inputStream);
            Document result = kryo.readObject(in, Document.class);
            in.close();
            return result;
        }

        // if this point is reached, then there are no stored
        // documents with the specified id
        return null;
    }

    @ParametersAreNonnullByDefault
    public void write(Document document) {
        File documentFile = new File(parentDocumentsFolder, Long.toString(document.getId()));

        try {
            OutputStream outputStream = new FileOutputStream(documentFile);
            Output out = new Output(outputStream);
            kryo.writeObject(out, document);
            out.close();

        } catch (IOException ex) {
            System.err.println("Error while writing document with id '" + document.getId() + "'.");
            ex.printStackTrace();
        }
    }
}
