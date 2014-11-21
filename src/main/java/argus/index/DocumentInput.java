package argus.index;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * A class that represents a document being lazily collected, containing
 * data that will be later compose a Document object.
 * This object is converted into a Document in the collection in the
 * DocumentPipeline class, during the indexing process.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public class DocumentInput {

    private Path filePath;
    private InputStream fileStream;

    public DocumentInput(Path filePath, InputStream fileStream) {
        this.filePath = filePath;
        this.fileStream = fileStream;
    }

    public Path getPath() {
        return filePath;
    }

    public InputStream getStream() {
        return fileStream;
    }

    public void destroy() {
        filePath = null;
        fileStream = null;
    }

    @Override
    public String toString() {
        return filePath.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DocumentInput that = (DocumentInput) o;
        return filePath.equals(that.filePath);
    }

    @Override
    public int hashCode() {
        return filePath.hashCode();
    }
}
