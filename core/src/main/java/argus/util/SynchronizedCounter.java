package argus.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A class that generates and returns a unique id from an incrementer, meaning that
 * every newly generated id will be the previously generated id + 1.
 * Note: This method differs from a simple static integer counter by being
 * thread-safe.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public final class SynchronizedCounter {
    private final AtomicLong sNextGeneratedId;

    public SynchronizedCounter() {
        sNextGeneratedId = new AtomicLong(1);
    }

    public SynchronizedCounter(int startAt) {
        sNextGeneratedId = new AtomicLong(startAt);
    }

    public long getAndIncrement() {
        for (; ; ) {
            final long result = sNextGeneratedId.get();
            long newValue = result + 1;

            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    public void reset() {
        sNextGeneratedId.set(1);
    }
}
