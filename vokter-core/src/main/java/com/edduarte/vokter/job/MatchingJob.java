/*
 * Copyright 2015 Eduardo Duarte
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.edduarte.vokter.job;

import com.edduarte.vokter.model.mongodb.Difference;
import com.edduarte.vokter.diff.DifferenceMatcher;
import com.edduarte.vokter.model.mongodb.Keyword;
import com.edduarte.vokter.model.v2.Match;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
@PersistJobDataAfterExecution
public class MatchingJob implements InterruptableJob {

    private static final Logger logger = LoggerFactory.getLogger(MatchingJob.class);

    public static final String PARENT_JOB_MANAGER = "parent_job_manager";

    public final static String REQUEST_URL = "request_url";

    public final static String KEYWORDS = "keywords";

    public final static String HAS_NEW_DIFFS = "has_new_diffs";

    public final static String IGNORE_ADDED = "ignore_added";

    public final static String IGNORE_REMOVED = "ignore_removed";


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

        try {
            ObjectMapper mapper = new ObjectMapper();
            List<String> keywords = mapper.readValue(dataMap.getString(KEYWORDS), ArrayList.class);
            boolean hasNewDifferences = dataMap.getBoolean(HAS_NEW_DIFFS);
            boolean ignoreAdded = dataMap.getBoolean(IGNORE_ADDED);
            boolean ignoreRemoved = dataMap.getBoolean(IGNORE_REMOVED);

            if (hasNewDifferences) {
                dataMap.put(HAS_NEW_DIFFS, false);

                // build keywords
                List<Keyword> kws = keywords.stream()
                        .map(manager::callBuildKeyword)
                        .collect(Collectors.toList());

                // match them
                List<Difference> diffs = manager.callGetDiffsImpl(requestUrl);
                DifferenceMatcher matcher = new DifferenceMatcher(kws, diffs, ignoreAdded, ignoreRemoved);
                Set<Match> results = matcher.call();
                if (!results.isEmpty()) {
                    manager.responseOk(requestUrl, responseUrl, results);
                }
            }
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }


    @Override
    public void interrupt() throws UnableToInterruptJobException {
    }
}
