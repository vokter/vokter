package argus.job;

import argus.diff.Difference;
import argus.diff.DifferenceMatcher;
import argus.keyword.Keyword;
import com.google.gson.Gson;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.UnableToInterruptJobException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
@PersistJobDataAfterExecution
public class MatchingJob implements InterruptableJob {

    public static final String PARENT_JOB_MANAGER = "parent_job_manager";
    public final static String REQUEST_URL = "request_url";
    public final static String KEYWORDS = "keywords";
    public final static String HAS_NEW_DIFFS = "has_new_diffs";

    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        JobKey key = context.getJobDetail().getKey();
        String responseUrl = key.getName();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        String managerName = dataMap.getString(PARENT_JOB_MANAGER);
        JobManager manager = JobManager.get(managerName);
        if (manager == null) {
            return;
        }

        String requestUrl = dataMap.getString(REQUEST_URL);

        List<String> keywords = new Gson().fromJson(dataMap.getString(KEYWORDS), ArrayList.class);
        boolean hasNewDifferences = dataMap.getBoolean(HAS_NEW_DIFFS);

        if (hasNewDifferences) {
            dataMap.put(HAS_NEW_DIFFS, false);

            // build keywords
            List<Keyword> kws = keywords.stream()
                    .map(manager::callBuildKeyword)
                    .collect(Collectors.toList());

            // match them
            List<Difference> diffs = manager.callGetDiffsImpl(requestUrl);
            DifferenceMatcher matcher = new DifferenceMatcher(kws, diffs);
            Set<DifferenceMatcher.Result> results = matcher.call();
            if (!results.isEmpty()) {
                manager.responseOk(requestUrl, responseUrl, results);
            }
        }
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
    }
}
