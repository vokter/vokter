package argus.keyword;

import com.aliasi.util.Pair;
import com.mongodb.BasicDBObject;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Keyword represents a sequence of texts that should match a difference detected
 * between two snapshots of a Document.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public final class Keyword extends BasicDBObject {

    final String originalInput;

    /**
     * The set of texts that compose this search, stored in the same order as
     * written by the user (linked hash set implementation).
     */
    private final Collection<String> texts;


    Keyword(final String originalInput, final Collection<String> texts) {
        this.originalInput = originalInput;
        this.texts = texts;
    }


    /**
     * Returns a lazy access to all texts that compose this query.
     */
    public Stream<String> textStream() {
        return texts.stream();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Keyword keyword = (Keyword) o;
        return originalInput.equals(keyword.originalInput);
    }

    @Override
    public int hashCode() {
        return originalInput.hashCode();
    }

    @Override
    public String toString() {
        String fullQuery = textStream().collect(Collectors.joining(" "));
        return "'" + fullQuery + "'";
    }
}
