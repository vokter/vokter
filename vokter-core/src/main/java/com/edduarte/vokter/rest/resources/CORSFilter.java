package com.edduarte.vokter.rest.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
@Provider
public class CORSFilter implements ContainerResponseFilter {

    private static final Logger logger = LoggerFactory.getLogger(CORSFilter.class);


    @Override
    public void filter(ContainerRequestContext request,
                       ContainerResponseContext response) throws IOException {
        MultivaluedMap<String, Object> headers = response.getHeaders();

        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            headers.putSingle("Access-Control-Max-Age", 1728000);
            headers.putSingle("Content-Length", 0);
            headers.putSingle("Access-Control-Allow-Headers",
                    "Origin,Accept,Key,DNT,X-Mx-ReqToken,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization");
            headers.putSingle("Access-Control-Allow-Origin", "*");
            headers.putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");

        } else if (request.getMethod().equalsIgnoreCase("GET") ||
                request.getMethod().equalsIgnoreCase("POST")) {
//            headers.putSingle("Access-Control-Allow-Credentials", "true");
            headers.putSingle("Access-Control-Allow-Headers",
                    "Origin,Accept,Key,DNT,X-Mx-ReqToken,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization");
            headers.putSingle("Access-Control-Allow-Origin", "*");
            headers.putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        }
    }
}