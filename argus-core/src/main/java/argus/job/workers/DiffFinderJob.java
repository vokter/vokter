package argus.job.workers;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

/**
 * TODO
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class DiffFinderJob implements Job {

    public static final String DOCUMENT_URL = "document_url";
    public static final String RESPONSE_URL = "response_url";

    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        JobKey key = context.getJobDetail().getKey();
//        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
//
//        String documentUrl = dataMap.getString(DOCUMENT_URL);
//        String responseUrl = dataMap.getString(RESPONSE_URL);
//
//
        System.out.println("DiffFinderJob: " + key.getGroup() + " " + key.getName());

    }
}
