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
import com.edduarte.vokter.keyword.Keyword;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
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
public class DiffMatcherJob implements InterruptableJob {

    public static final String PARENT_JOB_MANAGER = "parent_job_manager";

    public static final String HAS_NEW_DIFFS = "has_new_diffs";

    public final static String DOCUMENT_URL = "document_url";

    public static final String DOCUMENT_CONTENT_TYPE = "document_content_type";

    public static final String CLIENT_URL = "client_url";

    public static final String CLIENT_CONTENT_TYPE = "client_content_type";

    public static final String CLIENT_TOKEN = "client_token";

    public final static String KEYWORDS = "keywords";

    public final static String EVENTS = "events";

    public final static String FILTER_STOPWORDS = "filter_stopwords";

    public final static String ENABLE_STEMMING = "enable_stemming";

    public final static String IGNORE_CASE = "ignore_case";

    public final static String SNIPPET_OFFSET = "snippet_offset";

    private static final Logger logger =
            LoggerFactory.getLogger(DiffMatcherJob.class);


    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        String managerName = dataMap.getString(PARENT_JOB_MANAGER);
        JobManager manager = JobManager.get(managerName);
        if (manager == null) {
            return;
        }

        String documentUrl = dataMap.getString(DOCUMENT_URL);
        String documentContentType = dataMap.getString(DOCUMENT_CONTENT_TYPE);
        String clientUrl = dataMap.getString(CLIENT_URL);
        String clientContentType = dataMap.getString(CLIENT_CONTENT_TYPE);
        String clientToken = dataMap.getString(CLIENT_TOKEN);

        try {
            ObjectMapper mapper = new ObjectMapper();

            boolean hasNewDifferences = dataMap.getBoolean(HAS_NEW_DIFFS);
            if (hasNewDifferences) {
                dataMap.put(HAS_NEW_DIFFS, false);

                List<String> keywords = mapper.readValue(
                        dataMap.getString(KEYWORDS), ArrayList.class);
                List<DiffEvent> events = mapper.readValue(
                        dataMap.getString(EVENTS), ArrayList.class);
                boolean ignoreAdded = !events.contains(DiffEvent.inserted);
                boolean ignoreRemoved = !events.contains(DiffEvent.deleted);
                boolean filterStopwords = dataMap.getBoolean(FILTER_STOPWORDS);
                boolean enableStemming = dataMap.getBoolean(ENABLE_STEMMING);
                boolean ignoreCase = dataMap.getBoolean(IGNORE_CASE);
                int snippetOffset = dataMap.getInt(SNIPPET_OFFSET);

                // build keywords
                List<Keyword> kws = keywords.stream()
                        .map((keywordInput) -> manager.callBuildKeyword(
                                keywordInput,
                                filterStopwords,
                                enableStemming,
                                ignoreCase
                        ))
                        .collect(Collectors.toList());

                // match them
                Set<Match> results = manager.callGetMatchesImpl(
                        documentUrl, documentContentType,
                        kws, filterStopwords, enableStemming, ignoreCase,
                        ignoreAdded, ignoreRemoved, snippetOffset);
                if (!results.isEmpty()) {
                    boolean wasSuccessful = manager.sendNotificationToClient(
                            documentUrl, documentContentType,
                            clientUrl, clientContentType, clientToken,
                            results
                    );
                    // TODO: Add fault tolerance so that, if failed 10 times,
                    // cancel this matching job and send a timeout to the client
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
