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

import com.edduarte.argus.diff.Difference;
import com.edduarte.argus.diff.DifferenceMatcher;
import com.edduarte.argus.keyword.Keyword;
import com.edduarte.argus.keyword.KeywordSerializer;
import com.edduarte.argus.rest.SubscribeRequest;
import com.edduarte.argus.util.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.quartz.*;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.simpl.SimpleThreadPool;
import org.quartz.spi.JobStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.*;

/**
 * @author Ed Duarte (<a href="mailto:ed@edduarte.com">ed@edduarte.com</a>)
 * @version 1.3.3
 * @since 1.0.0
 */
public class JobManager {

    private static final Logger logger = LoggerFactory.getLogger(JobManager.class);

    private static final String SCHEDULER_NAME = "Argus_Scheduler";

    private static final Map<String, JobManager> activeManagers = new HashMap<>();

    private final String managerName;

    private final JobManagerHandler handler;

    private final int detectionInterval;

    private Scheduler scheduler;


    private JobManager(final String managerName,
                       int detectionInterval,
                       final JobManagerHandler handler) {
        this.managerName = managerName;
        this.handler = handler;
        this.detectionInterval = detectionInterval;
    }


    public static JobManager create(final String managerName,
                                    final int detectionInterval,
                                    final JobManagerHandler handler) {
        JobManager existingManager = get(managerName);
        if (existingManager != null) {
            existingManager.stop();
        }

        JobManager newManager = new JobManager(managerName, detectionInterval, handler);
        activeManagers.put(managerName, newManager);
        return newManager;
    }


    public static JobManager get(final String managerName) {
        return activeManagers.get(managerName);
    }


    public void initialize() throws SchedulerException {
        StdSchedulerFactory factory = new StdSchedulerFactory();
        scheduler = factory.getScheduler();
        factory = null;
        scheduler.start();
    }


    public void initialize(JobStore jobStore, int maxThreads) throws SchedulerException {
        DirectSchedulerFactory factory = DirectSchedulerFactory.getInstance();
        factory.createScheduler(
                SCHEDULER_NAME,
                Constants.bytesToHex(Constants.generateRandomBytes()),
                new SimpleThreadPool(maxThreads, 5),
                jobStore
        );
        scheduler = factory.getScheduler(SCHEDULER_NAME);
        factory = null;
        scheduler.start();
    }


    public boolean createJob(final SubscribeRequest request) {

        String documentUrl = request.getDocumentUrl();
        String clientUrl = request.getClientUrl();

        try {
            // attempt creating a new DiffDetectorJob
            JobDetail detectionJob = JobBuilder.newJob(DetectionJob.class)
                    .withIdentity(documentUrl, "detection" + documentUrl)
                    .usingJobData(DetectionJob.PARENT_JOB_MANAGER, managerName)
                    .usingJobData(DetectionJob.FAULT_COUNTER, 0)
                    .build();

            Trigger detectionTrigger = TriggerBuilder.newTrigger()
                    .withIdentity(documentUrl, "detection" + documentUrl)
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(detectionInterval)
                            .repeatForever())
                    .build();

            try {
                scheduler.scheduleJob(detectionJob, detectionTrigger);
                logger.info("Started detection job for '{}'.", documentUrl);
            } catch (ObjectAlreadyExistsException ignored) {
                // there is already a job monitoring the request url, so ignore this
            }

            String keywordJson = new Gson().toJson(request.getKeywords());

            JobDetail matchingJob = JobBuilder.newJob(MatchingJob.class)
                    .withIdentity(clientUrl, "matching" + documentUrl)
                    .usingJobData(MatchingJob.PARENT_JOB_MANAGER, managerName)
                    .usingJobData(MatchingJob.REQUEST_URL, documentUrl)
                    .usingJobData(MatchingJob.KEYWORDS, keywordJson)
                    .usingJobData(MatchingJob.IGNORE_ADDED, request.getIgnoreAdded())
                    .usingJobData(MatchingJob.IGNORE_REMOVED, request.getIgnoreRemoved())
                    .usingJobData(MatchingJob.HAS_NEW_DIFFS, false)
                    .build();

            GregorianCalendar cal = new GregorianCalendar();
            cal.add(Calendar.SECOND, request.getInterval());

            Trigger matchingTrigger = TriggerBuilder.newTrigger()
                    .withIdentity(clientUrl, "matching" + documentUrl)
                    .startAt(cal.getTime())
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(request.getInterval())
                            .repeatForever())
                    .build();

            try {
                scheduler.scheduleJob(matchingJob, matchingTrigger);
            } catch (ObjectAlreadyExistsException ex) {
                return false;
            }

        } catch (SchedulerException ex) {
            logger.error(ex.getMessage(), ex);
        }

        return true;
    }


    void timeoutDetectionJob(String documentUrl) {
        try {
            Set<JobKey> keys = scheduler.getJobKeys(GroupMatcher.groupEquals("matching" + documentUrl));
            for (JobKey k : keys) {
                String clientUrl = k.getName();
                sendTimeoutResponse(documentUrl, clientUrl);
                scheduler.interrupt(k);
                scheduler.deleteJob(k);
            }

            JobKey detectJobKey = new JobKey(documentUrl, "detection" + documentUrl);
            scheduler.interrupt(detectJobKey);
            scheduler.deleteJob(detectJobKey);
            handler.removeExistingDifferences(documentUrl);
            logger.info("Timed-out detection job for '{}'.", documentUrl);
        } catch (SchedulerException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }


    public boolean cancelMatchingJob(String documentUrl, final String clientUrl) {
        JobKey matchingJobKey = new JobKey(clientUrl, "matching" + documentUrl);
        return cancelMatchingJobAux(documentUrl, matchingJobKey);
    }

    private boolean cancelMatchingJobAux(String documentUrl, JobKey jobKey) {
        try {
            scheduler.interrupt(jobKey);
            boolean wasDeleted = scheduler.deleteJob(jobKey);

            if (wasDeleted) {
                // check if there are more match jobs for the same request url
                Set<JobKey> keys = scheduler.getJobKeys(GroupMatcher.groupEquals("matching" + documentUrl));
                if (keys.isEmpty()) {
                    // no more matching jobs for this document! interrupt the
                    // DiffDetectJob
                    JobKey detectJobKey = new JobKey(documentUrl, "detection" + documentUrl);
                    scheduler.interrupt(detectJobKey);
                    scheduler.deleteJob(detectJobKey);
                    handler.removeExistingDifferences(documentUrl);
                    logger.info("Canceled detection job for '{}'.", documentUrl);
                }
            }

            return wasDeleted;

        } catch (SchedulerException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return false;
    }


    public void stop() {
        try {
            scheduler.shutdown();
        } catch (SchedulerException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }


    final boolean callDetectDiffImpl(String documentUrl) {
        boolean wasSuccessful = handler.detectDifferences(documentUrl);

        // notify all matching jobs of that url that there are new differences to match
        if (wasSuccessful) {
            try {
                Set<JobKey> keys = scheduler.getJobKeys(GroupMatcher.groupEquals("matching" + documentUrl));
                for (JobKey k : keys) {
                    JobDetail jobDetail = scheduler.getJobDetail(k);
                    List<? extends Trigger> triggerList = scheduler.getTriggersOfJob(k);
                    if (!triggerList.isEmpty()) {
                        Trigger trigger = triggerList.get(0);

                        // update job to say that he has new diffs
                        JobDataMap dataMap = jobDetail.getJobDataMap();
                        dataMap.put(MatchingJob.HAS_NEW_DIFFS, true);

                        attemptRefreshJob(jobDetail, trigger, 10);

                    } else {
                        // invalid state, where there is still an active job
                        // without triggers
                        // unschedule it!
                        cancelMatchingJobAux(documentUrl, k);
                    }
                }
            } catch (SchedulerException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

        return wasSuccessful;
    }


    private void attemptRefreshJob(JobDetail jobDetail, Trigger trigger, int tries) {
        if (tries > 0) {
            try {
                scheduler.unscheduleJob(trigger.getKey());
                scheduler.scheduleJob(jobDetail, trigger);
            } catch (SchedulerException ignored) {
                // could not refresh job because the it did not unschedule properly
                int newCount = tries - 1;
                logger.info("Error while refreshing job, attempting " + newCount + " more times.");
                attemptRefreshJob(jobDetail, trigger, newCount);
            }
        }
    }


    final List<Difference> callGetDiffsImpl(String url) {
        return handler.getExistingDifferences(url);
    }


    final Keyword callBuildKeyword(String keywordInput) {
        return handler.buildKeyword(keywordInput);
    }


    final boolean responseOk(final String documentUrl,
                             final String clientUrl,
                             final Set<DifferenceMatcher.Result> diffs) {
        Map<String, Object> jsonResponseMap = new LinkedHashMap<>();
        jsonResponseMap.put("status", "ok");
        jsonResponseMap.put("url", documentUrl);
        jsonResponseMap.put("diffs", diffs);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Keyword.class, new KeywordSerializer());
        String input = gsonBuilder.create().toJson(jsonResponseMap);
        return sendResponse(clientUrl, input);
    }


    final boolean sendTimeoutResponse(final String documentUrl,
                                      final String clientUrl) {
        Map<String, Object> jsonResponseMap = new LinkedHashMap<>();
        jsonResponseMap.put("status", "timeout");
        jsonResponseMap.put("url", documentUrl);
        Set<DifferenceMatcher.Result> diffs = Collections.emptySet();
        jsonResponseMap.put("diffs", diffs);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Keyword.class, new KeywordSerializer());
        String input = gsonBuilder.create().toJson(jsonResponseMap);
        return sendResponse(clientUrl, input);
    }


    private boolean sendResponse(final String clientUrl, final String input) {
        try {
            URL targetUrl = new URL(clientUrl);

            HttpURLConnection httpConnection = (HttpURLConnection) targetUrl.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Content-Type", "application/json");

            OutputStream outputStream = httpConnection.getOutputStream();
            outputStream.write(input.getBytes());
            outputStream.flush();

            int responseCode = httpConnection.getResponseCode();
            httpConnection.disconnect();
            return responseCode == 200;

        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return false;
    }
}
