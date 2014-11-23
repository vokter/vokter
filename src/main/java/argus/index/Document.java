package argus.index;

import argus.util.SynchronizedCounter;
import it.unimi.dsi.lang.MutableString;

import java.io.Serializable;

/**
 * Simple structure that holds a document current snapshot and associates
 * it with an url.
 * <p/>
 * The id is obtained by using a synchronized counter, which in turn will ensure
 * that different Document objects being created in concurrency will always have
 * different IDs.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public final class Document implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final SynchronizedCounter counter = new SynchronizedCounter();

    private final long id;

    private final String url;

    private final MutableString contentSnapshot;



    public Document(String url, MutableString contentSnapshot) {
        this.id = counter.getAndIncrement();
        this.url = url;
        this.contentSnapshot = contentSnapshot;
    }


    public String getUrl() {
        return url;
    }


    public MutableString getContentSnapshot() {
        return contentSnapshot;
    }


    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Document document = (Document) o;
        return url.equalsIgnoreCase(document.url);
    }


    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }


    @Override
    public String toString() {
        return url;
    }
}

