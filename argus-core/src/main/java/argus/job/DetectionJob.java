package argus.job;

import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.UnableToInterruptJobException;

/**
 * TODO
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
@PersistJobDataAfterExecution
public class DetectionJob implements InterruptableJob {

    public static final String PARENT_JOB_MANAGER = "parent_job_manager";
    public static final String FAULT_COUNTER = "fault_counter";

    private static final long FAULT_TOLERANCE = 10;

    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        JobKey key = context.getJobDetail().getKey();
        String documentUrl = key.getName();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        String managerName = dataMap.getString(PARENT_JOB_MANAGER);
        JobManager manager = JobManager.get(managerName);
        if (manager == null) {
            return;
        }
        boolean wasSuccessful = manager.callDetectDiffImpl(documentUrl);

        int faultCounter = (int) dataMap.getOrDefault(FAULT_COUNTER, 0);
        faultCounter = wasSuccessful ? 0 : (faultCounter + 1);
        dataMap.put(FAULT_COUNTER, faultCounter);

        if (faultCounter >= FAULT_TOLERANCE) {
            // exceeded fault tolerance, so cancel this job and notify matcher jobs
            manager.timeoutDetectionJob(documentUrl);
        }
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
    }
}
