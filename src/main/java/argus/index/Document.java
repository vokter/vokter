package argus.index;

import argus.util.SynchronizedCounter;
import it.unimi.dsi.lang.MutableString;

import java.io.Serializable;

/**
 * Simple structure that holds a document path and content and associates it with
 * an id.
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

    private final int id;

    private final String path;

    private final MutableString content;


    public Document(String path, MutableString content) {
        this.id = counter.getAndIncrement();
        this.path = path;
        this.content = content;
    }


    public String getPath() {
        return path;
    }


    public MutableString getContent() {
        return content;
    }


    public int getId() {
        return id;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Document document = (Document) o;
        return id == document.id;
    }


    @Override
    public int hashCode() {
        return id;
    }


    @Override
    public String toString() {
        return path;
    }
}

