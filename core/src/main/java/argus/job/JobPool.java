package argus.job;

import argus.document.DocumentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * TODO
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class JobPool {

    private static final Logger logger = LoggerFactory.getLogger(JobPool.class);
    private final Map<String, ScheduledFuture<?>> watchExecutions;
    private ScheduledExecutorService executor;

    public JobPool() {
        watchExecutions = new ConcurrentHashMap<>();
    }

    public void initialize(int maxThreads) {
        executor = Executors.newScheduledThreadPool(maxThreads);
    }

    public void scheduleJob(String documentUrl, final String responseUrl) throws IllegalArgumentException {
        // if document url already exists, make a comparison immediately and
        // respond to the other response urls, then add the new responseUrl
        DocumentBuilder.fromUrl(documentUrl);

        ScheduledFuture<?> handle = executor.scheduleWithFixedDelay(() -> {


        }, 10, 10, TimeUnit.MINUTES);
        watchExecutions.put(documentUrl, handle);
    }

    public void cancelJob(String documentUrl, final String responseUrl) {
        ScheduledFuture<?> handle = watchExecutions.get(documentUrl);
        if (handle != null) {
            handle.cancel(true);
            watchExecutions.remove(documentUrl);
        }
    }

    public void clear() {
        watchExecutions.values().stream()
                .filter(handle -> handle != null)
                .forEach(handle -> handle.cancel(true));
        watchExecutions.clear();
        if (!executor.isShutdown()) {
            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }
}
