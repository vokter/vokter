package argus.diff;

/**
 * A structure that set the action that was performed on a text for it to trigger a
 * difference.
 * The following example ...
 * {Diff(Status.deleted, "Hello"), Diff(Status.inserted, "Goodbye"),
 * Diff(Status.nothing, " world.")}
 * ... means that the newer document snapshot erased "Hello", wrote "Goodbye" and
 * kept " world.".
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public enum DifferenceAction {
    inserted, deleted, nothing
}
