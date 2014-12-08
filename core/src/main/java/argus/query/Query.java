package argus.query;

import it.unimi.dsi.lang.MutableString;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A Query represents a sequence of terms that should match a set of documents
 * within a collection.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public final class Query {

    /**
     * The set of terms that compose this search, stored in the same order as
     * written by the user (linked hash set implementation).
     */
    private final Collection<MutableString> texts;

    /**
     * The number of other words permitted between words in query phrase. If zero,
     * then this query is an exact phrase search.
     */
    private final int slop;


    Query(final Collection<MutableString> texts, final int slop) {
        this.texts = texts;
        this.slop = slop;
    }


    /**
     * Returns a lazy access to all texts that compose this query.
     */
    public Stream<MutableString> textStream() {
        return texts.stream();
    }


    /**
     * Returns the maximum allowed distance between the query terms in matched
     * documents.
     */
    public int getSlop() {
        return slop;
    }


    @Override
    public String toString() {
        Optional<MutableString> fullQuery = textStream()
                .reduce((t1, t2) -> t1.append(" ").append(t2));

        if (fullQuery.isPresent()) {
            return "'" + fullQuery.get().toString() + "', with slop " + slop;

        } else {
            return "'', with slop " + slop;
        }
    }
}
