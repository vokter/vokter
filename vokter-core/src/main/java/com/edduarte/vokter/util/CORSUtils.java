package com.edduarte.vokter.util;

import javax.ws.rs.core.Response;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class CORSUtils {

    private CORSUtils() {
    }


    public static Response.ResponseBuilder getResponseBuilderWithCORS(int statusCode) {
        return Response.status(statusCode)
//                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Headers",
                        "Origin,Accept,Key,DNT,X-Mx-ReqToken,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization")
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    }


    public static Response getOptionsWithCORS(String accessControlHeader) {
        return Response.ok()
//                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Headers", accessControlHeader != null ?
                        accessControlHeader :
                        "Origin,Accept,Key,DNT,X-Mx-ReqToken,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization")
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                .build();
    }
}
