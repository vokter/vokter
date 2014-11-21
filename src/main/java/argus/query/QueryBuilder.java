package argus.query;

import argus.stemmer.Stemmer;
import it.unimi.dsi.lang.MutableString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Builder class that loads an input text and processes this into a
 * {@link argus.query.Query} structure.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public final class QueryBuilder {

    private static final Logger logger = LoggerFactory.getLogger(QueryBuilder.class);

    private MutableString queryInput;
    private int slop;
    private Set<MutableString> stopwords;
    private Class<? extends Stemmer> stemmerClass;
    private boolean ignoreCase = false;


    private QueryBuilder() {
    }

    public static QueryBuilder newBuilder() {
        return new QueryBuilder();
    }

    public QueryBuilder withText(final MutableString queryInput) {
        this.queryInput = queryInput;
        return this;
    }

    public QueryBuilder withSlop(final int slop) {
        this.slop = slop;
        return this;
    }

    public QueryBuilder withStopwords(final Set<MutableString> stopwords) {
        this.stopwords = stopwords;
        return this;
    }


    public QueryBuilder withStemmer(final Class<? extends Stemmer> stemmerClass) {
        this.stemmerClass = stemmerClass;
        return this;
    }


    public QueryBuilder ignoreCase() {
        this.ignoreCase = true;
        return this;
    }

    public Query build() {
        LinkedHashSet<MutableString> queryTexts = new LinkedHashSet<>();

        new QueryPipeline(

                // the textual input of the search
                queryInput,

                // general structure that holds the separated tokens of the search
                queryTexts,

                // flag that forces every found token to be
                // lower case, matching, for example, the words
                // 'be' and 'Be' as the same token when searching
                ignoreCase,

                // the set of stopwords that will be filtered during tokenization
                stopwords,

                // the stemmer class that will be used to stem the detected tokens
                stemmerClass

        ).run();

        // instantiate the Query object
        return new Query(queryTexts, slop);
    }
}
