/*
 * Copyright 2015 Eduardo Duarte
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

package com.edduarte.vokter.rest.resources.v1;

import com.edduarte.vokter.job.JobManager;
import com.edduarte.vokter.persistence.Session;
import com.edduarte.vokter.rest.model.CommonResponse;
import com.edduarte.vokter.rest.model.v1.AddRequest;
import com.edduarte.vokter.rest.model.v1.CancelRequest;
import com.edduarte.vokter.util.CORSUtils;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import org.apache.commons.validator.routines.UrlValidator;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * REST Resource for calls on path "/vokter/v1/".
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.4.1
 * @since 1.0.0
 */
@Path("/v1/")
@Api(tags = {"v1"})
@SwaggerDefinition(info = @Info(
        title = "API v1",
        description = "Vokter REST API service version 1, which is " +
                "kept active for backwards-compatibility purposes.",
        version = "1"
))
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RestResource {

    private final JobManager jobManager;


    public RestResource(JobManager jobManager) {
        this.jobManager = jobManager;
    }


    /**
     * Options method for "subscribe" to enable Access-Control-Allow-Origin
     * for all origin endpoints, opening the API to client services using CORS
     * forcefully, like frontends using AngularJS's regular $http calls.
     */
    @OPTIONS
    @Path("subscribe")
    @ApiOperation(value = "", hidden = true)
    public Response watchOptions(
            @HeaderParam("Access-Control-Request-Headers") String acrHeader) {
        return CORSUtils.getOptionsWithCORS(acrHeader);
    }


    @POST
    @Path("subscribe")
    @ApiOperation(
            value = "Add a job",
            notes = "Adds a new monitoring job to Vokter. The document will " +
                    "be checked periodically, and if the provided keywords " +
                    "are matched, the clientUrl will receive a notification " +
                    "as a POST request.",
            nickname = "add"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "The monitoring job was created successfully.",
                    response = String.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "The provided data is invalid.",
                    response = CommonResponse.class
            ),
            @ApiResponse(
                    code = 409,
                    message = "The request conflicts with a currently " +
                            "active watch job, since the provided document " +
                            "URL is already being watched and notified to " +
                            "the provided client URL.",
                    response = CommonResponse.class
            ),
            @ApiResponse(
                    code = 415,
                    message = "The request body has an invalid format."
            )})
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response watch(AddRequest addRequest) {
        String[] schemes = {"http", "https"};
        UrlValidator urlValidator = new UrlValidator(schemes, UrlValidator.ALLOW_LOCAL_URLS);

        String documentUrl = addRequest.getDocumentUrl();
        if (documentUrl == null || documentUrl.isEmpty() ||
                !urlValidator.isValid(documentUrl)) {
            CommonResponse responseBody = CommonResponse.invalidDocumentUrl();
            return CORSUtils.getResponseBuilderWithCORS(400)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(responseBody)
                    .build();
        }

        String clientUrl = addRequest.getClientUrl();
        if (clientUrl == null || clientUrl.isEmpty() ||
                !urlValidator.isValid(clientUrl)) {
            CommonResponse responseBody = CommonResponse.invalidClientUrl();
            return CORSUtils.getResponseBuilderWithCORS(400)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(responseBody)
                    .build();
        }

        List<String> keywords = addRequest.getKeywords();
        if (keywords != null) {
            for (Iterator<String> it = keywords.iterator(); it.hasNext(); ) {
                String k = it.next();
                if (k == null || k.isEmpty()) {
                    it.remove();
                }
            }
        }

        if (keywords == null || keywords.isEmpty()) {
            CommonResponse responseBody = CommonResponse.emptyKeywords();
            return CORSUtils.getResponseBuilderWithCORS(400)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(responseBody)
                    .build();
        }

        if (addRequest.getIgnoreAdded() &&
                addRequest.getIgnoreRemoved()) {
            CommonResponse responseBody = CommonResponse.emptyDifferenceActions();
            return CORSUtils.getResponseBuilderWithCORS(400)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(responseBody)
                    .build();
        }

        com.edduarte.vokter.rest.model.v2.AddRequest r =
                new com.edduarte.vokter.rest.model.v2.AddRequest(addRequest);

        Session session = jobManager.createJob(
                r.getDocumentUrl(), r.getDocumentContentType(),
                r.getClientUrl(), r.getClientContentType(),
                r.getKeywords(), r.getEvents(),
                r.filterStopwords(), r.enableStemming(), r.ignoreCase(),
                r.getSnippetOffset(), r.getInterval()
        );
        if (session != null) {
            CommonResponse responseBody = CommonResponse.ok();
            return CORSUtils.getResponseBuilderWithCORS(200)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(responseBody)
                    .build();
        } else {
            CommonResponse responseBody = CommonResponse.alreadyExists();
            return CORSUtils.getResponseBuilderWithCORS(409)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(responseBody)
                    .build();
        }
    }


    /**
     * Options method for "cancel" to enable Access-Control-Allow-Origin
     * for all origin endpoints, opening the API to client services using CORS
     * forcefully, like frontends using AngularJS's regular $http calls.
     */
    @OPTIONS
    @Path("cancel")
    @ApiOperation(value = "", hidden = true)
    public Response cancelOptions(
            @HeaderParam("Access-Control-Request-Headers") String acrHeader) {
        return CORSUtils.getOptionsWithCORS(acrHeader);
    }


    @POST
    @Path("cancel")
    @ApiOperation(
            value = "Cancel a job",
            notes = "Cancels a monitoring job on Vokter. If the job canceled " +
                    "is the last job attached to the specified document url, " +
                    "then all document snapshots and detected differences " +
                    "for that document are cleared from Vokter.",
            response = CommonResponse.class,
            nickname = "cancel"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "The monitoring job was successfully canceled.",
                    response = CommonResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "The provided data is invalid.",
                    response = CommonResponse.class
            ),
            @ApiResponse(
                    code = 415,
                    message = "The request body has an invalid format."
            ),
            @ApiResponse(
                    code = 404,
                    message = "The specified job to cancel does not exist.",
                    response = CommonResponse.class
            )})
    public Response cancel(
            @ApiParam(value = "The job data, including the pair of document " +
                    "url to client url that identifies the job.", required = true)
                    CancelRequest cancelRequest) throws ExecutionException {

        boolean wasDeleted = jobManager.cancelJob(
                cancelRequest.getDocumentUrl(),
                cancelRequest.getDocumentContentType(),
                cancelRequest.getClientUrl(),
                cancelRequest.getClientContentType()
        );
        if (wasDeleted) {
            CommonResponse responseBody = CommonResponse.ok();
            return CORSUtils.getResponseBuilderWithCORS(200)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(responseBody)
                    .build();
        } else {
            CommonResponse responseBody = CommonResponse.notExists();
            return CORSUtils.getResponseBuilderWithCORS(404)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(responseBody)
                    .build();
        }
    }
}