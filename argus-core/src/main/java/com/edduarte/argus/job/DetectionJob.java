/*
 * Copyright 2015 Ed Duarte
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

package com.edduarte.argus.job;

import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.UnableToInterruptJobException;

/**
 * @author Ed Duarte (<a href="mailto:ed@edduarte.com">ed@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
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
