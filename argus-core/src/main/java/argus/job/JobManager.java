package argus.job;

import argus.document.Document;
import argus.document.DocumentCollection;
import argus.job.workers.DiffFinderJob;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.simpl.RAMJobStore;
import org.quartz.simpl.SimpleThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

import static argus.util.Constants.bytesToHex;
import static argus.util.Constants.generateRandomBytes;

/**
 * TODO
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class JobManager {

    private static final Logger logger = LoggerFactory.getLogger(JobManager.class);

    private static final String SCHEDULER_NAME = "Argus_Scheduler";
    private final DocumentCollection collection;
    private final Function<String, Document> buildImpl;
    private Scheduler scheduler;

    public JobManager(final DocumentCollection collection,
                      final Function<String, Document> buildImpl) {
        this.collection = collection;
        this.buildImpl = buildImpl;
    }

    public void initialize(int maxThreads) throws SchedulerException {
        DirectSchedulerFactory factory = DirectSchedulerFactory.getInstance();
        factory.createScheduler(
                SCHEDULER_NAME,
                bytesToHex(generateRandomBytes()),
                new SimpleThreadPool(maxThreads, 5),
                new RAMJobStore()
        );
        scheduler = factory.getScheduler(SCHEDULER_NAME);
        factory = null;
        scheduler.start();
    }

    public void createJob(final String documentUrl,
                          final String responseUrl,
                          final long interval) {

        // searches for "documentUrl" in the collection


        // if document url already exists, make a comparison immediately and
        // respond to the new responseUrl
        Document document = buildImpl.apply(documentUrl);

        JobDetail job = JobBuilder.newJob(DiffFinderJob.class)
                .withIdentity(responseUrl, documentUrl)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(responseUrl, documentUrl)
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
//                        .withIntervalInMinutes(7)
                        .withIntervalInSeconds(7)
                        .repeatForever())
                .build();

        try {
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException ex) {
            logger.error(ex.getMessage(), ex);
        }
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
