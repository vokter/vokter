package argus.query;

import argus.document.Document;
import argus.parser.Parser;
import argus.parser.ParserPool;
import argus.stemmer.Stemmer;
import com.google.common.base.Stopwatch;
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

    private final MutableString queryInput;
    private int slop;
    private boolean isStoppingEnabled;
    private boolean isStemmingEnabled;
    private boolean ignoreCase;


    private QueryBuilder(final MutableString queryInput) {
        this.queryInput = queryInput;
        this.isStoppingEnabled = false;
        this.isStemmingEnabled = false;
        this.ignoreCase = false;
    }

    public static QueryBuilder fromText(final String queryInput) {
        return new QueryBuilder(new MutableString(queryInput));
    }

    public QueryBuilder withSlop(final int slop) {
        this.slop = slop;
        return this;
    }

    public QueryBuilder withStopwords() {
        this.isStoppingEnabled = true;
        return this;
    }


    public QueryBuilder withStemming() {
        this.isStemmingEnabled = true;
        return this;
    }


    public QueryBuilder ignoreCase() {
        this.ignoreCase = true;
        return this;
    }

    public Query build(ParserPool parserPool) {

        // step 3) Takes a parser from the parser-pool.
        Parser parser;
        try {
            parser = parserPool.take();
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }

        QueryPipeline pipeline = new QueryPipeline(

                // the textual input of the search
                queryInput,

                // the parser that will be used for query parsing and term
                // detection
                parser,

                // the set of stopwords that will be filtered during tokenization
                isStoppingEnabled,

                // the stemmer class that will be used to stem the detected tokens
                isStemmingEnabled,

                // flag that forces every found token to be
                // lower case, matching, for example, the words
                // 'be' and 'Be' as the same token when searching
                ignoreCase
        );

        // step 5) Process the document asynchronously.
        Stopwatch sw = Stopwatch.createStarted();
        Query aux;
        try {
            aux = pipeline.call();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
        sw.stop();
        logger.info("Query processor elapsed time: " + sw.toString());
        sw = null;
        final Query query = aux;

        // step 6) Place the parser back in the parser-pool.
        try {
            parserPool.place(parser);
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }

        return query;
    }
}
