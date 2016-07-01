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

import com.edduarte.vokter.diff.DiffEvent;
import com.edduarte.vokter.diff.Match;
import com.edduarte.vokter.document.DocumentBuilder;
import com.edduarte.vokter.keyword.Keyword;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobPersistenceException;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.listeners.JobChainingJobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.edduarte.vokter.diff.DiffEvent.deleted;
import static com.edduarte.vokter.diff.DiffEvent.inserted;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.3
 * @since 1.0.0
 */
public class JobManager {

    private static final Logger logger = LoggerFactory.getLogger(JobManager.class);

    private static final Map<String, JobManager> activeManagers = new HashMap<>();

    private final String managerName;

    private final JobManagerHandler handler;

    private Scheduler scheduler;


    private JobManager(final String managerName,
                       final JobManagerHandler handler) {
        this.managerName = managerName;
        this.handler = handler;
    }


    public static JobManager create(final String managerName,
                                    final JobManagerHandler handler) {
        JobManager existingManager = get(managerName);
        if (existingManager != null) {
            existingManager.stop();
        }

        JobManager newManager = new JobManager(
                managerName,
                handler
        );
        activeManagers.put(managerName, newManager);
        return newManager;
    }


    public static JobManager get(final String managerName) {
        return activeManagers.get(managerName);
    }


    private static String detectJobGroup(String documentUrl,
                                         String documentContentType) {
        return "detect|" + documentUrl + "|" + documentContentType;
    }


    private static String matchJobName(String clientUrl,
                                       String clientContentType) {
        return clientUrl + "|" + clientContentType;
    }


    private static String matchJobGroup(String documentUrl,
                                        String documentContentType) {
        return "match|" + documentUrl + "|" + documentContentType;
    }


    private static String chainName(String documentUrl,
                                    String documentContentType,
                                    String clientUrl,
                                    String clientContentType) {
        return "chain|" +
                documentUrl + "|" + documentContentType + "|" +
                clientUrl + "|" + clientContentType;
    }


    private static JobKey detectJobKey(String documentUrl,
                                       String documentContentType) {
        return new JobKey(
                documentContentType,
                detectJobGroup(documentUrl, documentContentType)
        );
    }


    private static TriggerKey detectTriggerKey(String documentUrl,
                                               String documentContentType,
                                               String clientUrl,
                                               String clientContentType) {
        return new TriggerKey(
                clientUrl + "|" + clientContentType,
                detectJobGroup(documentUrl, documentContentType)
        );
    }


    private static JobKey matchJobKey(String documentUrl,
                                      String documentContentType,
                                      String clientUrl,
                                      String clientContentType) {
        return new JobKey(
                matchJobName(clientUrl, clientContentType),
                matchJobGroup(documentUrl, documentContentType)
        );
    }


    public void initialize() throws SchedulerException {
        StdSchedulerFactory factory = new StdSchedulerFactory();
        scheduler = factory.getScheduler();
        factory = null;
        scheduler.start();
    }


    /**
     * Simplified API
     */
    public boolean createJob(String documentUrl, String clientUrl,
                             List<String> keywords, int interval) {
        return createJob(
                documentUrl, MediaType.TEXT_HTML,
                clientUrl, MediaType.APPLICATION_JSON,
                keywords, Arrays.asList(inserted, deleted),
                true, true, true,
                50, interval
        );
    }


    /**
     * Used for testing only.
     */
    boolean createJob(String documentUrl, String documentContentType,
                      String clientUrl, String clientContentType,
                      List<String> keywords, int interval) {
        return createJob(
                documentUrl, documentContentType,
                clientUrl, clientContentType,
                keywords, Arrays.asList(inserted, deleted),
                true, true, true,
                50, interval
        );
    }


    public boolean createJob(
            String documentUrl, String documentContentType,
            String clientUrl, String clientContentType,
            List<String> keywords, List<DiffEvent> events,
            boolean filterStopwords, boolean enableStemming, boolean ignoreCase,
            int snippetOffset, int interval) {

        // test if the document is readable
        boolean isReadable = DocumentBuilder
                .fromUrl(documentUrl, documentContentType)
                .isReadable();
        if (!isReadable) {
            return false;
        }

        try {
            // attempt to create a new detection job
            JobKey detectJKey = detectJobKey(documentUrl, documentContentType);
            if (scheduler.getJobDetail(detectJKey) == null) {
                JobDetail detectionJob = JobBuilder.newJob(DiffDetectorJob.class)
                        .withIdentity(detectJKey)
                        .usingJobData(DiffDetectorJob.PARENT_JOB_MANAGER, managerName)
                        .usingJobData(DiffDetectorJob.FAULT_COUNTER, 0)
                        .usingJobData(DiffDetectorJob.URL, documentUrl)
                        .usingJobData(DiffDetectorJob.CONTENT_TYPE, documentContentType)
                        .storeDurably(true)
                        .build();

                try {
                    scheduler.addJob(detectionJob, false);
                    logger.info("Started detection job for document '{}' ({}).",
                            documentUrl, documentContentType);
                } catch (ObjectAlreadyExistsException ignored) {
                    // there is already a job monitoring the specified document, so
                    // ignore this
                    logger.warn("Detection job for document '{}' ({}) already exists!",
                            documentUrl, documentContentType);
                }
            } else {
                logger.warn("Detection job for document '{}' ({}) already exists!",
                        documentUrl, documentContentType);
            }

            ObjectMapper mapper = new ObjectMapper();
            String keywordJson = mapper.writeValueAsString(keywords);
            String eventsJson = mapper.writeValueAsString(events);

            // attempt to create a new matching job, chained to execute after
            // the detection job
            JobKey matchJKey = matchJobKey(
                    documentUrl, documentContentType,
                    clientUrl, clientContentType
            );
            if (scheduler.getJobDetail(matchJKey) != null) {
                logger.warn("Matching job " +
                                "for document '{}' ({}) " +
                                "to client '{}' ({}) already exists!.",
                        documentUrl, documentContentType,
                        clientUrl, clientContentType);
                return false;
            }
            JobDetail matchingJob = JobBuilder.newJob(DiffMatcherJob.class)
                    .withIdentity(matchJKey)
                    .usingJobData(DiffMatcherJob.PARENT_JOB_MANAGER, managerName)
                    .usingJobData(DiffMatcherJob.DOCUMENT_URL, documentUrl)
                    .usingJobData(DiffMatcherJob.DOCUMENT_CONTENT_TYPE, documentContentType)
                    .usingJobData(DiffMatcherJob.CLIENT_URL, clientUrl)
                    .usingJobData(DiffMatcherJob.CLIENT_CONTENT_TYPE, clientContentType)
                    .usingJobData(DiffMatcherJob.KEYWORDS, keywordJson)
                    .usingJobData(DiffMatcherJob.EVENTS, eventsJson)
                    .usingJobData(DiffMatcherJob.FILTER_STOPWORDS, filterStopwords)
                    .usingJobData(DiffMatcherJob.ENABLE_STEMMING, enableStemming)
                    .usingJobData(DiffMatcherJob.IGNORE_CASE, ignoreCase)
                    .usingJobData(DiffMatcherJob.SNIPPET_OFFSET, snippetOffset)
                    .usingJobData(DiffMatcherJob.HAS_NEW_DIFFS, false)
                    .storeDurably(true)
                    .build();

//            GregorianCalendar cal = new GregorianCalendar();
//            cal.add(Calendar.SECOND, request.getInterval());
//
//            Trigger matchingTrigger = TriggerBuilder.newTrigger()
//                    .withIdentity(matchTKey)
//                    .startAt(cal.getTime())
//                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
//                            .withIntervalInSeconds(request.getInterval())
//                            .repeatForever())
//                    .build();

            try {
                scheduler.addJob(matchingJob, false);
                logger.info("Started matching job " +
                                "for document '{}' ({}) " +
                                "to client '{}' ({}).",
                        documentUrl, documentContentType,
                        clientUrl, clientContentType);
            } catch (ObjectAlreadyExistsException ex) {
                // there is already a matching job for the specified document,
                // so return false which should be interpreted as "not created /
                // already exists"
                logger.warn("Matching job " +
                                "for document '{}' ({}) " +
                                "to client '{}' ({}) already exists!.",
                        documentUrl, documentContentType,
                        clientUrl, clientContentType);
                return false;
            }


            // attempt to create a new detection trigger that uses the interval
            // specified by the client
            TriggerKey detectTKey = detectTriggerKey(
                    documentUrl, documentContentType,
                    clientUrl, clientContentType
            );
            Trigger detectionTrigger = TriggerBuilder.newTrigger()
                    .forJob(detectJKey)
                    .withIdentity(detectTKey)
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(interval)
                            .repeatForever())
                    .build();

            try {
                scheduler.scheduleJob(detectionTrigger);
                logger.info("Started detection trigger " +
                                "for document '{}' ({}) " +
                                "to client '{}' ({}), with interval {}.",
                        documentUrl, documentContentType,
                        clientUrl, clientContentType,
                        interval);
            } catch (ObjectAlreadyExistsException ignored) {
                // there is already trigger for the specified document and for
                // the specified client, so ignore this
            }

            JobChainingJobListener chain = new JobChainingJobListener(chainName(
                    documentUrl, documentContentType,
                    clientUrl, clientContentType
            ));
            chain.addJobChainLink(detectJKey, matchJKey);
            scheduler.getListenerManager().addJobListener(chain);

        } catch (SchedulerException | JsonProcessingException ex) {
            logger.error(ex.getMessage(), ex);
            return false;
        }

        return true;
    }


    void timeoutDetectionJob(String documentUrl, String documentContentType) {
        try {
            Set<JobKey> keys = scheduler.getJobKeys(
                    GroupMatcher.groupEquals(
                            matchJobGroup(documentUrl, documentContentType)));
            for (JobKey k : keys) {
                JobDetail detail = scheduler.getJobDetail(k);
                JobDataMap m = detail.getJobDataMap();
                String clientUrl = m.getString(DiffMatcherJob.CLIENT_URL);
                String clientContentType =
                        m.getString(DiffMatcherJob.CLIENT_CONTENT_TYPE);

                scheduler.getListenerManager().removeJobListener(chainName(
                        documentUrl, documentContentType,
                        clientUrl, clientContentType
                ));
                sendTimeoutToClient(
                        documentUrl, documentContentType,
                        clientUrl, clientContentType
                );
                scheduler.interrupt(k);
                scheduler.deleteJob(k);
                logger.info("Timed out matching job " +
                                "for document '{}' ({}) " +
                                "to client '{}' ({}).",
                        documentUrl, documentContentType,
                        clientUrl, clientContentType);
            }

            JobKey detectJobKey = detectJobKey(documentUrl, documentContentType);
            scheduler.interrupt(detectJobKey);
            scheduler.deleteJob(detectJobKey);
            handler.removeExistingDifferences(documentUrl, documentContentType);
            logger.info("Timed out detection job for document '{}' ({}).",
                    documentUrl, documentContentType);
        } catch (SchedulerException | JsonProcessingException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }


    /**
     * Simplified API
     */
    public boolean cancelJob(String documentUrl, String clientUrl) {
        return cancelJob(documentUrl, MediaType.TEXT_HTML,
                clientUrl, MediaType.APPLICATION_JSON);
    }


    public boolean cancelJob(String documentUrl,
                             String documentContentType,
                             String clientUrl,
                             String clientContentType) {
        JobKey jobKey = matchJobKey(
                documentUrl, documentContentType,
                clientUrl, clientContentType
        );

        try {
            boolean wasDeleted;
            try {
                scheduler.interrupt(jobKey);
                wasDeleted = scheduler.deleteJob(jobKey);
                if (wasDeleted) {
                    scheduler.getListenerManager().removeJobListener(chainName(
                            documentUrl, documentContentType,
                            clientUrl, clientContentType
                    ));
                    logger.info("Canceled matching job " +
                                    "for document '{}' ({}) " +
                                    "to client '{}' ({}).",
                            documentUrl, documentContentType,
                            clientUrl, clientContentType
                    );
                }
            } catch (JobPersistenceException ex) {
                // job was already deleted
                wasDeleted = true;
            }

            if (wasDeleted) {
                // check if there are more match jobs for the same document
                Set<JobKey> keys = scheduler.getJobKeys(GroupMatcher.groupEquals(
                        matchJobGroup(documentUrl, documentContentType)
                ));
                if (keys.isEmpty()) {
                    // no more matching jobs for this document! interrupt the
                    // detection job
                    JobKey detectJobKey =
                            detectJobKey(documentUrl, documentContentType);
                    scheduler.interrupt(detectJobKey);
                    scheduler.deleteJob(detectJobKey);
                    handler.removeExistingDifferences(
                            documentUrl, documentContentType);
                    logger.info("Canceled detection job for document '{}' ({}).",
                            documentUrl, documentContentType);
                }

                // check if there are more match jobs for the same client
                keys = scheduler.getJobKeys(GroupMatcher.anyGroup());
                boolean hasMatchingJob = keys.parallelStream()
                        .anyMatch(k -> k.getName().equals(
                                matchJobName(clientUrl, clientContentType)));

                if (!hasMatchingJob) {
                    // no more match jobs for this client, so remove the session
                    handler.removeSession(clientUrl, clientContentType);
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


    final boolean callDetectDiffImpl(String documentUrl,
                                     String documentContentType) {

        JobManagerHandler.DetectResult result =
                handler.detectDifferences(documentUrl, documentContentType);

        // notify all matching jobs of that url that there are new differences
        // to match
        if (result.wasSuccessful() && result.hasNewDiffs()) {
            try {
                Set<JobKey> keys = scheduler.getJobKeys(GroupMatcher.groupEquals(
                        matchJobGroup(documentUrl, documentContentType)
                ));
                for (JobKey k : keys) {
                    JobDetail detail = scheduler.getJobDetail(k);

                    // update job to say that he has new diffs
                    attemptRefreshMatchingJob(detail, 10);
                }
            } catch (SchedulerException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

        return result.wasSuccessful();
    }


    private void attemptRefreshMatchingJob(JobDetail detail, int attemptsLeft) {
        if (attemptsLeft > 0) {
            try {
                JobDataMap m = detail.getJobDataMap();
                JobDetail matchingJob = JobBuilder.newJob(DiffMatcherJob.class)
                        .withIdentity(detail.getKey())
                        .usingJobData(DiffMatcherJob.PARENT_JOB_MANAGER,
                                managerName)
                        .usingJobData(DiffMatcherJob.DOCUMENT_URL,
                                m.getString(DiffMatcherJob.DOCUMENT_URL))
                        .usingJobData(DiffMatcherJob.DOCUMENT_CONTENT_TYPE,
                                m.getString(DiffMatcherJob.DOCUMENT_CONTENT_TYPE))
                        .usingJobData(DiffMatcherJob.CLIENT_URL,
                                m.getString(DiffMatcherJob.CLIENT_URL))
                        .usingJobData(DiffMatcherJob.CLIENT_CONTENT_TYPE,
                                m.getString(DiffMatcherJob.CLIENT_CONTENT_TYPE))
                        .usingJobData(DiffMatcherJob.KEYWORDS,
                                m.getString(DiffMatcherJob.KEYWORDS))
                        .usingJobData(DiffMatcherJob.EVENTS,
                                m.getString(DiffMatcherJob.EVENTS))
                        .usingJobData(DiffMatcherJob.FILTER_STOPWORDS,
                                m.getBoolean(DiffMatcherJob.FILTER_STOPWORDS))
                        .usingJobData(DiffMatcherJob.ENABLE_STEMMING,
                                m.getBoolean(DiffMatcherJob.ENABLE_STEMMING))
                        .usingJobData(DiffMatcherJob.IGNORE_CASE,
                                m.getBoolean(DiffMatcherJob.IGNORE_CASE))
                        .usingJobData(DiffMatcherJob.SNIPPET_OFFSET,
                                m.getInt(DiffMatcherJob.SNIPPET_OFFSET))
                        .usingJobData(DiffMatcherJob.HAS_NEW_DIFFS, true)
                        .storeDurably(true)
                        .build();

                scheduler.addJob(matchingJob, true);

            } catch (SchedulerException ignored) {
                // could not refresh job because the it did not unschedule
                // properly
                logger.info(ignored.toString());
                int newCount = attemptsLeft - 1;
                logger.info("Error while refreshing job, attempting "
                        + newCount + " more times.");
                attemptRefreshMatchingJob(detail, newCount);
            }
        }
    }


    final Set<Match> callGetMatchesImpl(String documentUrl,
                                        String documentContentType,
                                        List<Keyword> keywords,
                                        boolean filterStopwords,
                                        boolean enableStemming,
                                        boolean ignoreCase,
                                        boolean ignoreAdded,
                                        boolean ignoreRemoved,
                                        int snippetOffset) {
        return handler.matchDifferences(
                documentUrl,
                documentContentType,
                keywords,
                filterStopwords,
                enableStemming,
                ignoreCase,
                ignoreAdded,
                ignoreRemoved,
                snippetOffset
        );
    }


    final Keyword callBuildKeyword(String keywordInput,
                                   boolean isStoppingEnabled,
                                   boolean isStemmingEnabled,
                                   boolean ignoreCase) {
        return handler.buildKeyword(
                keywordInput, isStoppingEnabled, isStemmingEnabled, ignoreCase);
    }


    final boolean sendTimeoutToClient(final String documentUrl,
                                      final String documentContentType,
                                      final String clientUrl,
                                      final String clientContentType)
            throws JsonProcessingException {
        return handler.sendTimeoutToClient(
                documentUrl, documentContentType,
                clientUrl, clientContentType
        );
    }


    public boolean sendNotificationToClient(String documentUrl, String documentContentType,
                                            String clientUrl, String clientContentType,
                                            Set<Match> results) {
        return handler.sendNotificationToClient(
                documentUrl, documentContentType,
                clientUrl, clientContentType,
                results
        );
    }
}
