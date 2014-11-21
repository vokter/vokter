package argus.filter;

import it.unimi.dsi.lang.MutableString;

/**
 * Utility filter implementation that concatenates two filter classes.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public class AndFilter implements Filter {

    private final Filter f1;
    private final Filter f2;

    private AndFilter(Filter f1, Filter f2) {
        this.f1 = f1;
        this.f2 = f2;
    }

    public static AndFilter of(Filter f1, Filter f2) {
        return new AndFilter(f1, f2);
    }

    @Override
    public void filter(MutableString documentContent) {
        f1.filter(documentContent);
        f2.filter(documentContent);
    }
}
