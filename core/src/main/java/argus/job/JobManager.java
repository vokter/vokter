package argus.job;

import argus.document.DocumentBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class JobManager {

    private static final Logger logger = LoggerFactory.getLogger(JobManager.class);
    private final Scheduler scheduler;

    public JobManager() throws SchedulerException {
        scheduler = StdSchedulerFactory.getDefaultScheduler();
    }

    public void initialize(int maxThreads) {
        try {
            scheduler.start();
        } catch (SchedulerException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public void scheduleJob(final String documentUrl,
                            final long interval,
                            final String responseUrl) throws IllegalArgumentException {
        // if document url already exists, make a comparison immediately and
        // respond to the other response urls, then add the new responseUrl
        DocumentBuilder.fromUrl(documentUrl);


    }

    public void cancelJob(String documentUrl, final String responseUrl) {

    }

    public void stop() {
        try {
            scheduler.shutdown();
        } catch (SchedulerException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
}
