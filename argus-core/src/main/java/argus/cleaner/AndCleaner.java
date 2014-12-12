package argus.cleaner;

import it.unimi.dsi.lang.MutableString;

/**
 * Utility Cleaner implementation that concatenates two Cleaner classes.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public class AndCleaner implements Cleaner {

    private final Cleaner f1;
    private final Cleaner f2;

    private AndCleaner(Cleaner f1, Cleaner f2) {
        this.f1 = f1;
        this.f2 = f2;
    }

    public static AndCleaner of(Cleaner f1, Cleaner f2) {
        return new AndCleaner(f1, f2);
    }

    @Override
    public void clean(MutableString documentContent) {
        f1.clean(documentContent);
        f2.clean(documentContent);
    }
}
