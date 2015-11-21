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

package com.edduarte.argus.rest.resources;

import com.edduarte.argus.Context;
import com.edduarte.argus.keyword.Keyword;
import com.edduarte.argus.rest.CancelRequest;
import com.edduarte.argus.rest.RestResponse;
import com.edduarte.argus.rest.WatchRequest;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * REST Resource for calls on path "/rest/v1/".
 *
 * @author Ed Duarte (<a href="mailto:ed@edduarte.com">ed@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
@Path("/v1/")
public class RestV1Resource {

    @GET
    @Path("exampleRequest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response exampleRequest() {
        WatchRequest requestBody = new WatchRequest(
                "http://www.example.com",
                "http://your.site/client-rest-api",
                Lists.newArrayList("argus", "panoptes", "argus panoptes"),
                600,
                false,
                false
        );
        return Response.status(200)
                .type(MediaType.APPLICATION_JSON)
                .entity(requestBody.toString())
                .build();
    }


    @GET
    @Path("exampleResponse")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response exampleResponse() {
        RestResponse responseBody = new RestResponse(RestResponse.Code.ok, "");
        return Response.status(200)
                .type(MediaType.APPLICATION_JSON)
                .entity(responseBody.toString())
                .build();
    }


    @POST
    @Path("watch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response watch(String watchRequestJson) {
        try {
            WatchRequest watchRequest = new Gson().fromJson(
                    watchRequestJson,
                    WatchRequest.class
            );
            String[] schemes = {"http","https"};
            UrlValidator urlValidator = new UrlValidator(schemes);

            String docUrl = watchRequest.getDocumentUrl();
            if (docUrl == null || docUrl.isEmpty() ||
                    !urlValidator.isValid(docUrl)) {
                RestResponse responseBody = new RestResponse(
                        RestResponse.Code.error,
                        "The provided document Url is invalid!");
                return Response.status(400)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(responseBody.toString())
                        .build();
            }

            String rcvUrl = watchRequest.getReceiverUrl();
            if (rcvUrl == null || rcvUrl.isEmpty() ||
                    !urlValidator.isValid(rcvUrl)) {
                RestResponse responseBody = new RestResponse(
                        RestResponse.Code.error,
                        "The provided receiver Url is invalid!");
                return Response.status(400)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(responseBody.toString())
                        .build();
            }

            List<String> keywords = watchRequest.getKeywords();
            if (keywords != null) {
                for (Iterator<String> it = keywords.iterator(); it.hasNext(); ) {
                    String k = it.next();
                    if (k == null || k.isEmpty()) {
                        it.remove();
                    }
                }
            }

            if (keywords == null || keywords.isEmpty()) {
                RestResponse responseBody = new RestResponse(
                        RestResponse.Code.error,
                        "You need to provide at least one valid keyword!");
                return Response.status(400)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(responseBody.toString())
                        .build();
            }

            if (!watchRequest.getIgnoreAdded() &&
                    !watchRequest.getIgnoreRemoved()) {
                RestResponse responseBody = new RestResponse(
                        RestResponse.Code.error,
                        "At least one difference action ('added' or " +
                                "'removed') must not be ignored!"
                );
                return Response.status(400)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(responseBody.toString())
                        .build();
            }

            Context context = Context.getInstance();
            boolean created = context.createJob(watchRequest);
            if (created) {
                RestResponse responseBody = new RestResponse(
                        RestResponse.Code.ok,
                        ""
                );
                return Response.status(200)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(responseBody.toString())
                        .build();
            } else {
                RestResponse responseBody = new RestResponse(
                        RestResponse.Code.error,
                        "The request conflicts with a currently active watch " +
                                "job, since the provided document url is " +
                                "already being watched and notified to the " +
                                "provided response url!"
                );
                return Response.status(400)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(responseBody.toString())
                        .build();
            }

        } catch (JsonSyntaxException ex) {
            // the job-request json had an invalid format
            RestResponse responseBody = new RestResponse(
                    RestResponse.Code.error,
                    "The request has an invalid format. Must provide a " +
                            "message with the following format:\n" +
                            "{\"documentUrl\": <url-to-watch>, " +
                            "\"keywords\": <keywords-to-watch-for>, " +
                            "\"interval\": <interval-in-seconds>, " +
                            "\"responseUrl: <url-to-send-async-responses-to>}"
            );
            return Response.status(400)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(responseBody.toString())
                    .build();
        }
    }


    @POST
    @Path("cancel")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancel(String cancelRequestJson) throws ExecutionException {
        try {
            CancelRequest cancelRequest = new Gson()
                    .fromJson(cancelRequestJson, CancelRequest.class);

            Context context = Context.getInstance();
            boolean wasDeleted = context.cancelJob(
                    cancelRequest.documentUrl,
                    cancelRequest.responseUrl
            );
            if (wasDeleted) {
                RestResponse responseBody = new RestResponse(
                        RestResponse.Code.ok,
                        ""
                );
                return Response.status(200)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(responseBody.toString())
                        .build();
            } else {
                RestResponse responseBody = new RestResponse(
                        RestResponse.Code.error,
                        "The job to cancel does not exist!"
                );
                return Response.status(404)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(responseBody.toString())
                        .build();
            }

        } catch (JsonSyntaxException ex) {
            // the cancel-request json had an invalid format
            RestResponse responseBody = new RestResponse(
                    RestResponse.Code.error,
                    "The request has an invalid format. Must provide a " +
                            "message with the following format:\n" +
                            "{\"documentUrl\": <url-to-cancel>, " +
                            "\"responseUrl: <url-to-cancel>}"
            );
            return Response.status(400)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(responseBody.toString())
                    .build();
        }
    }
}