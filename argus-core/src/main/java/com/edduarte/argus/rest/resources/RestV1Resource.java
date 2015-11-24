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
import com.edduarte.argus.rest.CancelRequest;
import com.edduarte.argus.rest.ResponseBody;
import com.edduarte.argus.rest.SubscribeRequest;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.validator.routines.UrlValidator;

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
 * REST Resource for calls on path "/argus/v1/".
 *
 * @author Ed Duarte (<a href="mailto:ed@edduarte.com">ed@edduarte.com</a>)
 * @version 1.4.1
 * @since 1.0.0
 */
@Path("/v1/")
public class RestV1Resource {

    @GET
    @Path("exampleRequest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response exampleRequest() {
        SubscribeRequest requestBody = new SubscribeRequest(
                "http://www.example.com",
                "http://your.site/client-rest-api",
                Lists.newArrayList("argus", "argus panoptes"),
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
        ResponseBody responseBody = new ResponseBody(0, "");
        return Response.status(200)
                .type(MediaType.APPLICATION_JSON)
                .entity(responseBody.toString())
                .build();
    }


    @POST
    @Path("subscribe")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response watch(String watchRequestJson) {
        try {
            SubscribeRequest subscribeRequest = new Gson().fromJson(
                    watchRequestJson,
                    SubscribeRequest.class
            );
            String[] schemes = {"http", "https"};
            UrlValidator urlValidator = new UrlValidator(schemes, UrlValidator.ALLOW_LOCAL_URLS);

            String documentUrl = subscribeRequest.getDocumentUrl();
            if (documentUrl == null || documentUrl.isEmpty() ||
                    !urlValidator.isValid(documentUrl)) {
                ResponseBody responseBody = new ResponseBody(1,
                        "The provided document URL is invalid.");
                return Response.status(400)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(responseBody.toString())
                        .build();
            }

            String clientUrl = subscribeRequest.getClientUrl();
            if (clientUrl == null || clientUrl.isEmpty() ||
                    !urlValidator.isValid(clientUrl)) {
                ResponseBody responseBody = new ResponseBody(2,
                        "The provided client URL is invalid.");
                return Response.status(400)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(responseBody.toString())
                        .build();
            }

            List<String> keywords = subscribeRequest.getKeywords();
            if (keywords != null) {
                for (Iterator<String> it = keywords.iterator(); it.hasNext(); ) {
                    String k = it.next();
                    if (k == null || k.isEmpty()) {
                        it.remove();
                    }
                }
            }

            if (keywords == null || keywords.isEmpty()) {
                ResponseBody responseBody = new ResponseBody(3,
                        "You need to provide at least one valid keyword.");
                return Response.status(400)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(responseBody.toString())
                        .build();
            }

            if (subscribeRequest.getIgnoreAdded() &&
                    subscribeRequest.getIgnoreRemoved()) {
                ResponseBody responseBody = new ResponseBody(4,
                        "At least one difference action (added or " +
                                "removed) must not be ignored.");
                return Response.status(400)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(responseBody.toString())
                        .build();
            }

            Context context = Context.getInstance();
            boolean created = context.createJob(subscribeRequest);
            if (created) {
                ResponseBody responseBody = new ResponseBody(0,
                        ""
                );
                return Response.status(200)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(responseBody.toString())
                        .build();
            } else {
                ResponseBody responseBody = new ResponseBody(5,
                        "The request conflicts with a currently active watch " +
                                "job, since the provided document URL is " +
                                "already being watched and notified to the " +
                                "provided client URL.");
                return Response.status(400)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(responseBody.toString())
                        .build();
            }

        } catch (JsonSyntaxException ex) {
            // the job-request json had an invalid format
            ResponseBody responseBody = new ResponseBody(6,
                    "The request has an invalid format.");
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
                    cancelRequest.getDocumentUrl(),
                    cancelRequest.getClientUrl()
            );
            if (wasDeleted) {
                ResponseBody responseBody = new ResponseBody(0, "");
                return Response.status(200)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(responseBody.toString())
                        .build();
            } else {
                ResponseBody responseBody = new ResponseBody(7,
                        "The specified job to cancel does not exist.");
                return Response.status(404)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(responseBody.toString())
                        .build();
            }

        } catch (JsonSyntaxException ex) {
            // the cancel-request json had an invalid format
            ResponseBody responseBody = new ResponseBody(6,
                    "The request has an invalid format.");
            return Response.status(400)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(responseBody.toString())
                    .build();
        }
    }
}