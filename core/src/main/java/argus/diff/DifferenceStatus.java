package argus.diff;

/**
 * The data structure representing a diff is a Linked list of Diff objects:
 * {Diff(Status.deleted, "Hello"), Diff(Status.inserted, "Goodbye"),
 * Diff(Status.none, " world.")}
 * which means: delete "Hello", add "Goodbye" and keep " world."
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public enum DifferenceStatus {
    inserted, deleted, none
}
