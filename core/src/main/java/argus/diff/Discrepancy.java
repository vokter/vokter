package argus.diff;

import argus.term.Snippet;

/**
 * TODO
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class Discrepancy {

    public static enum Status {
        ADDED,
        MODIFIED,
        REMOVED
    }

    private final Status status;
    private final Snippet before;
    private final Snippet after;

    Discrepancy(Status status, Snippet before, Snippet after) {
        this.status = status;
        this.before = before;
        this.after = after;
    }
}
