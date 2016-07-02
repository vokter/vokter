package com.edduarte.vokter.rest;

import com.codahale.metrics.health.HealthCheck;
import com.edduarte.vokter.job.JobManager;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class JobManagerHealthCheck extends HealthCheck {

    private final JobManager jobManager;


    public JobManagerHealthCheck(JobManager jobManager) {
        this.jobManager = jobManager;
    }


    @Override
    protected Result check() throws Exception {
        if (jobManager != null) {
            return Result.healthy();
        } else {
            return Result.unhealthy("Job manager was not properly initialized");
        }
    }
}
