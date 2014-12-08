package argus.stopper;

import it.unimi.dsi.lang.MutableString;

/**
 * Loader module that checks if the received textual state of a term corresponds
 * to a stopword.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 2.0
 */
public interface Stopwords {

    boolean isStopword(MutableString termText);

    void destroy();
}
