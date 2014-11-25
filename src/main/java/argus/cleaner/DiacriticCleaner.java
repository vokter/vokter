package argus.cleaner;

import it.unimi.dsi.lang.MutableString;

import java.text.Normalizer;

import static java.text.Normalizer.Form.*;

/**
 * Cleaner class that converts diacritic words into their non-diacritic form.
 * This is mostly usable for
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public class DiacriticCleaner implements Cleaner {

    @Override
    public void clean(MutableString documentContents) {
        String string = Normalizer.normalize(documentContents.toString(), Normalizer.Form.NFD);
        documentContents.delete(0, documentContents.length());
        for (char c : string.toCharArray()) {
            if (c <= '\u007F'){
                documentContents.append(c);
            }
        }
    }
}