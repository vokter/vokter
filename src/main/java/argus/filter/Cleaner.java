package argus.filter;

import it.unimi.dsi.lang.MutableString;

/**
 * Indexing module that cleans textual content from the received textual content.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 2.0
 */
public interface Cleaner {

    void clean(MutableString documentContent);
}
