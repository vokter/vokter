package com.edduarte.vokter.swagger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Eduardo Duarte
 * @author Federico Recio
 */
@Path(Constants.SWAGGER_PATH)
@Produces(MediaType.TEXT_HTML)
public class SwaggerResource {

    private final String urlPattern;


    public SwaggerResource(String urlPattern) {
        this.urlPattern = urlPattern;
    }


    @GET
    public SwaggerView get() {
        return new SwaggerView(urlPattern);
    }
}
