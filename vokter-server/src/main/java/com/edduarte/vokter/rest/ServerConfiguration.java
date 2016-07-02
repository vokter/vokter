package com.edduarte.vokter.rest;

import com.edduarte.vokter.swagger.SwaggerBundleConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class ServerConfiguration extends Configuration {

    @JsonProperty("swagger")
    private SwaggerBundleConfig config;

    public ServerConfiguration() {
        config = new SwaggerBundleConfig();
        config.setResourcePackage("com.edduarte.vokter.rest.resources");
        config.setTitle("Vokter REST API");
        config.setDescription("Vokter is a high-performance, scalable web " +
                "service that manages a high number of concurrent jobs, " +
                "triggering notifications when specified keywords " +
                "were either added or removed from a web document. " +
                "This service implements a information retrieval " +
                "system that fetches, indexes and performs queries " +
                "over web documents on a periodic basis. It optionally " +
                "supports multi-language stop-word filtering, to ignore " +
                "changes in common words with no important significance, " +
                "and stemming to detect changes in lexically derived words.");
        config.setVersion("2.0.0");
        config.setContact("hello@edduarte.com");
        config.setLicense("Apache License, Version 2.0");
        config.setLicenseUrl("http://www.apache.org/licenses/LICENSE-2.0");
    }


    public SwaggerBundleConfig getConfig() {
        return config;
    }
}