package com.edduarte.vokter.swagger;

import io.dropwizard.Configuration;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.server.ServerFactory;
import io.dropwizard.server.SimpleServerFactory;

/**
 * Wrapper around Dropwizard configuration and the bundle's config that
 * simplifies getting some information from them.
 *
 * @author Eduardo Duarte
 * @author Federico Recio
 * @author Flemming Frandsen
 */
public class ConfigurationHelper {

    private final Configuration configuration;
    private final SwaggerBundleConfig swaggerBundleConfig;

    public ConfigurationHelper(Configuration configuration,
                               SwaggerBundleConfig swaggerBundleConfig) {
        this.configuration = configuration;
        this.swaggerBundleConfig = swaggerBundleConfig;
    }

    public String getJerseyRootPath() {
        // if the user explicitly defined a path to prefix requests use it
        // instead of derive it
        if (swaggerBundleConfig.getUriPrefix() != null) {
            return swaggerBundleConfig.getUriPrefix();
        }

        String rootPath;

        ServerFactory serverFactory = configuration.getServerFactory();

        if (serverFactory instanceof SimpleServerFactory) {
            rootPath = ((SimpleServerFactory) serverFactory).getJerseyRootPath();
        } else {
            rootPath = ((DefaultServerFactory) serverFactory).getJerseyRootPath();
        }

        return stripUrlSlashes(rootPath);
    }

    public String getUrlPattern() {
        // if the user explicitly defined a path to prefix requests use it
        // instead of derive it
        if (swaggerBundleConfig.getUriPrefix() != null) {
            return swaggerBundleConfig.getUriPrefix();
        }

        final String applicationContextPath = getApplicationContextPath();
        final String rootPath = getJerseyRootPath();

        String urlPattern;

        if (rootPath.equals("/") && applicationContextPath.equals("/")) {
            urlPattern = "/";
        } else if (rootPath.equals("/") && !applicationContextPath.equals("/")) {
            urlPattern = applicationContextPath;
        } else if (!rootPath.equals("/") && applicationContextPath.equals("/")) {
            urlPattern = rootPath;
        } else {
            urlPattern = applicationContextPath + rootPath;
        }

        return urlPattern;
    }

    public String getSwaggerUriPath() {
        final String jerseyRootPath = getJerseyRootPath();
        String uriPathPrefix = jerseyRootPath.equals("/") ? "" : jerseyRootPath;
        return uriPathPrefix + Constants.SWAGGER_URI_PATH;
    }

    private String getApplicationContextPath() {
        String applicationContextPath;

        ServerFactory serverFactory = configuration.getServerFactory();

        if (serverFactory instanceof SimpleServerFactory) {
            applicationContextPath = ((SimpleServerFactory) serverFactory)
                    .getApplicationContextPath();
        } else {
            applicationContextPath = ((DefaultServerFactory) serverFactory)
                    .getApplicationContextPath();
        }

        return stripUrlSlashes(applicationContextPath);
    }

    private String stripUrlSlashes(String urlToStrip) {
        if (urlToStrip.endsWith("/*")) {
            urlToStrip = urlToStrip.substring(0, urlToStrip.length() - 1);
        }

        if (urlToStrip.length() > 1 && urlToStrip.endsWith("/")) {
            urlToStrip = urlToStrip.substring(0, urlToStrip.length() - 1);
        }

        return urlToStrip;
    }
}
