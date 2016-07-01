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

import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
@PersistJobDataAfterExecution
public class DiffDetectorJob implements InterruptableJob {

    private static final Logger logger =
            LoggerFactory.getLogger(DiffDetectorJob.class);

    public static final String PARENT_JOB_MANAGER = "parent_job_manager";

    public static final String FAULT_COUNTER = "fault_counter";

    public static final String URL = "url";

    public static final String CONTENT_TYPE = "content_type";

    private static final long FAULT_TOLERANCE = 10;


    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        String documentUrl = dataMap.getString(URL);
        String contentType = dataMap.getString(CONTENT_TYPE);
        String managerName = dataMap.getString(PARENT_JOB_MANAGER);
        JobManager manager = JobManager.get(managerName);
        if (manager == null) {
            return;
        }
        boolean wasSuccessful = manager
                .callDetectDiffImpl(documentUrl, contentType);

        int faultCounter = (int) dataMap.getOrDefault(FAULT_COUNTER, 0);
        faultCounter = wasSuccessful ? 0 : (faultCounter + 1);
        dataMap.put(FAULT_COUNTER, faultCounter);

        if (faultCounter >= FAULT_TOLERANCE) {
            // exceeded fault tolerance, so cancel this job and notify clients
            manager.timeoutDetectionJob(documentUrl, contentType);
        }
    }


    @Override
    public void interrupt() throws UnableToInterruptJobException {
    }
}
