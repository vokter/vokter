package argus.stopper;

import it.unimi.dsi.lang.MutableString;

/**
 * Indexing module that checks if the received textual state of a term corresponds
 * to a stopword.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 2.0
 */
public interface StopwordsLoader {

    void load(String language);

    void destroy();

    boolean isStopword(MutableString termText);
}
