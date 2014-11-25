package argus.stemmer;

import it.unimi.dsi.lang.MutableString;

/**
 * Indexing module that performs stemming over the received textual content.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 2.0
 */
public interface Stemmer {

    void stem(MutableString termText);

    String getSupportedLanguage();
}
