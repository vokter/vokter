/*
 * Copyright 2014 Ed Duarte
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
import com.edduarte.argus.rest.RestResponse;
import com.edduarte.argus.rest.WatchRequest;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.ExecutionException;

/**
 * REST Resource for calls on path "/rest/".
 *
 * @author Ed Duarte (<a href="mailto:ed@edduarte.com">ed@edduarte.com</a>)
 * @version 1.3.0
 * @since 1.0.0
 */
@Path("/")
public class RestResource {

    private static final Logger logger = LoggerFactory.getLogger(RestResource.class);


    @GET
    @Path("example")
    @Produces(MediaType.APPLICATION_JSON)
    public String test() {
        WatchRequest watchRequest = new WatchRequest(
                "www.example.com/url/to/watch",
                "http://your.site/async-response-receiver",
                Lists.newArrayList("single-word-keyword", "multiple word keyword"),
                600
        );
        return new Gson().toJson(watchRequest);
    }


    @POST
    @Path("testResponse")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response response(String responseJSON) {
        System.out.println("received Argus response: " + responseJSON);
        return Response.ok().build();
    }


    @POST
    @Path("watch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String watch(String watchRequestJson) {
        try {
            WatchRequest watchRequest = new Gson()
                    .fromJson(watchRequestJson, WatchRequest.class);

            Context context = Context.getInstance();
            boolean created = context.createJob(watchRequest);
            if (created) {
                RestResponse response = new RestResponse(RestResponse.Code.ok, "");
                return response.toString();
            } else {
                RestResponse response = new RestResponse(RestResponse.Code.error, "" +
                        "The request conflicts with a currently active watch " +
                        "job, since the provided document url is already being " +
                        "watched and notified to the provided response url!");
                return response.toString();
            }

        } catch (JsonSyntaxException ex) {
            // the job-request json had an invalid format
            RestResponse response = new RestResponse(RestResponse.Code.error, "" +
                    "The request has an invalid format. Must provide a message " +
                    "with the following format:\n" +
                    "{\"documentUrl\": <url-to-watch>, " +
                    "\"keywords\": <keywords-to-watch-for>, " +
                    "\"interval\": <interval-in-seconds>, " +
                    "\"responseUrl: <url-to-send-async-responses-to>}");
            return response.toString();
        }
    }


    @POST
    @Path("cancel")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String cancel(String cancelRequestJson) throws ExecutionException {
        try {
            CancelRequest cancelRequest = new Gson()
                    .fromJson(cancelRequestJson, CancelRequest.class);

            Context context = Context.getInstance();
            boolean wasDeleted = context.cancelJob(cancelRequest.documentUrl, cancelRequest.responseUrl);
            RestResponse response;
            if (wasDeleted) {
                response = new RestResponse(RestResponse.Code.ok, "");
            } else {
                response = new RestResponse(RestResponse.Code.error, "The job to cancel does not exist!");
            }
            return response.toString();

        } catch (JsonSyntaxException ex) {
            // the cancel-request json had an invalid format
            RestResponse response = new RestResponse(RestResponse.Code.error, "" +
                    "The request has an invalid format. Must provide a message " +
                    "with the following format:\n" +
                    "{\"documentUrl\": <url-to-cancel>, " +
                    "\"responseUrl: <url-to-cancel>}");
            return response.toString();
        }
    }
}