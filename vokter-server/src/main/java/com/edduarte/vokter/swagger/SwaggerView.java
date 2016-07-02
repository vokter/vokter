package com.edduarte.vokter.swagger;

import com.google.common.base.Charsets;
import io.dropwizard.views.View;

/**
 * Serves the content of Swagger's index page which has been "templatized" to
 * support replacing the directory in which Swagger's static content is located
 * (i.e. JS files) and the path with which requests to resources need to be
 * prefixed.
 *
 * @author Eduardo Duarte
 * @author Federico Recio
 */
public class SwaggerView extends View {

    private final String swaggerAssetsPath;
    private final String contextPath;

    protected SwaggerView(String urlPattern) {
        super("index.ftl", Charsets.UTF_8);

        if (urlPattern.equals("/")) {
            swaggerAssetsPath = Constants.SWAGGER_URI_PATH;
        } else {
            swaggerAssetsPath = urlPattern + Constants.SWAGGER_URI_PATH;
        }

        if (urlPattern.equals("/")) {
            contextPath = "";
        } else {
            contextPath = urlPattern;
        }
    }

    /**
     * Returns the path with which all requests for Swagger's static content need to be prefixed
     */
    @SuppressWarnings("unused")
    public String getSwaggerAssetsPath() {
        return swaggerAssetsPath;
    }

    /**
     * Returns the path with with which all requests made by Swagger's UI to Resources need to be prefixed
     */
    @SuppressWarnings("unused")
    public String getContextPath() {
        return contextPath;
    }
}
