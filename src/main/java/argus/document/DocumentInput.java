package argus.document;

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

    private String url;
    private InputStream contentStream;
    private String contentType;


    public DocumentInput(String url, InputStream contentStream, String contentType) {
        this.url = url;
        this.contentStream = contentStream;
        this.contentType = contentType;
    }


    public String getUrl() {
        return url;
    }


    public InputStream getStream() {
        return contentStream;
    }


    public String getContentType() {
        return contentType;
    }


    public void destroy() {
        url = null;
        contentStream = null;
        contentType = null;
    }


    @Override
    public String toString() {
        return url;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DocumentInput that = (DocumentInput) o;
        return url.equals(that.url);
    }


    @Override
    public int hashCode() {
        return url.hashCode();
    }
}
