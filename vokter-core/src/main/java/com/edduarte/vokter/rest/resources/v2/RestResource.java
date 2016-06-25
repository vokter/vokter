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

package com.edduarte.vokter.rest.resources.v2;

import com.edduarte.vokter.Context;
import com.edduarte.vokter.diff.DifferenceEvent;
import com.edduarte.vokter.model.Session;
import com.edduarte.vokter.model.v1.rest.SubscribeRequest;
import com.edduarte.vokter.model.v2.rest.CancelRequest;
import com.edduarte.vokter.model.CommonResponse;
import com.edduarte.vokter.model.v2.rest.AddRequest;
import com.edduarte.vokter.util.CORSUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import org.apache.commons.validator.routines.UrlValidator;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * REST Resource for calls on path "/vokter/v2/".
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.4.1
 * @since 1.0.0
 */
@Path("/v2/")
@Api(tags = {"Version 2"})
@SwaggerDefinition(info = @Info(
        title = "Version 2",
        description = "Vokter REST API service latest version.",
        version = "2"
))
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RestResource {

    /**
     * Options method to enable Access-Control-Allow-Origin for all origin
     * endpoints, opening the API to client services using CORS like AngularJS
     * frontends.
     */
    @OPTIONS
    @ApiOperation(value = "", hidden = true)
    public Response options(
            @HeaderParam("Access-Control-Request-Headers") String acrHeader) {
        return CORSUtils.getOptionsWithCORS(acrHeader);
    }


    @GET
    @ApiOperation(
            value = "Get an example request body",
            notes = "Returns an example request body that can be sent as " +
                    "payload in POST to start a monitoring job.",
            response = AddRequest.class,
            nickname = "example"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "The example request body was returned successfully."
            )})
    public Response exampleRequest() {
        AddRequest requestBody = new AddRequest(
                "http://www.example.com",
                "http://your.site/client-rest-api",
                Arrays.asList("argus", "argus panoptes"),
                Arrays.asList(DifferenceEvent.inserted, DifferenceEvent.deleted)
        );
        return Response.status(200)
                .type(MediaType.APPLICATION_JSON)
                .entity(requestBody)
                .build();
    }


    @POST
    @ApiOperation(
            value = "Add a monitoring job",
            notes = "Adds a new monitoring job to Vokter. The page will be " +
                    "checked periodically, and if the provided keywords are " +
                    "matched, the clientUrl will receive a notification as " +
                    "a POST request.",
            response = CommonResponse.class,
            nickname = "add"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "The monitoring job was created successfully."
            ),
            @ApiResponse(
                    code = 400,
                    message = "The provided data is invalid."
            ),
            @ApiResponse(
                    code = 409,
                    message = "The request conflicts with a currently " +
                            "active watch job, since the provided document " +
                            "URL is already being watched and notified to " +
                            "the provided client URL."
            ),
            @ApiResponse(
                    code = 415,
                    message = "The request body has an invalid format."
            )})
    public Response watch(
            @ApiParam(value = "The job data, including the document url to " +
                    "monitor and the keywords to watch for.", required = true)
                    AddRequest addRequest) {

        String[] schemes = {"http", "https"};
        UrlValidator urlValidator = new UrlValidator(schemes, UrlValidator.ALLOW_LOCAL_URLS);

        String documentUrl = addRequest.getDocumentUrl();
        if (documentUrl == null || documentUrl.isEmpty() ||
                !urlValidator.isValid(documentUrl)) {
            CommonResponse responseBody = CommonResponse.invalidDocumentUrl();
            return Response.status(400)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(responseBody)
                    .build();
        }

        String clientUrl = addRequest.getClientUrl();
        if (clientUrl == null || clientUrl.isEmpty() ||
                !urlValidator.isValid(clientUrl)) {
            CommonResponse responseBody = CommonResponse.invalidClientUrl();
            return Response.status(400)
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
            return Response.status(400)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(responseBody)
                    .build();
        }

        if (addRequest.getEvents().isEmpty()) {
            CommonResponse responseBody = CommonResponse.emptyDifferenceActions();
            return Response.status(400)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(responseBody)
                    .build();
        }

        Context context = Context.getInstance();
        SubscribeRequest r = new SubscribeRequest(
                addRequest.getDocumentUrl(),
                addRequest.getClientUrl(),
                addRequest.getKeywords(),
                600,
                false,
                false
        );
        boolean created = context.createJob(r);
        if (created) {
            CommonResponse responseBody = CommonResponse.ok();
            return Response.status(200)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(responseBody)
                    .build();
        } else {
            CommonResponse responseBody = CommonResponse.alreadyExists();
            return Response.status(409)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(responseBody)
                    .build();
        }
    }


    @DELETE
    @ApiOperation(
            value = "Cancel a monitoring job",
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
                    message = "The monitoring job was successfully canceled."
            ),
            @ApiResponse(
                    code = 400,
                    message = "The provided data is invalid."
            ),
            @ApiResponse(
                    code = 401,
                    message = "Authentication token is invalid or has expired."
            ),
            @ApiResponse(
                    code = 415,
                    message = "The request body has an invalid format."
            ),
            @ApiResponse(
                    code = 404,
                    message = "The specified job to cancel does not exist."
            )})
    public Response cancel(
            @ApiParam(value = "Token that authenticates the client url.",
                    required = true)
            @HeaderParam("Authorization") String token,
            @ApiParam(value = "The job data, including the pair of document " +
                    "url to client url that identifies the job.", required = true)
                    CancelRequest cancelRequest) throws ExecutionException {

//        Session session = validateToken(token);
        Session session = new Session(cancelRequest.getClientUrl(), token);
        if (session == null ||
                !session.getClientUrl().equals(cancelRequest.getClientUrl())) {
            return CORSUtils.getResponseBuilderWithCORS(401)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(CommonResponse.unauthorized())
                    .build();
        }

        Context context = Context.getInstance();
        boolean wasDeleted = context.cancelJob(
                cancelRequest.getDocumentUrl(),
                cancelRequest.getClientUrl()
        );
        if (wasDeleted) {
            CommonResponse responseBody = CommonResponse.ok();
            return Response.status(200)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(responseBody)
                    .build();
        } else {
            CommonResponse responseBody = CommonResponse.notExists();
            return Response.status(404)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(responseBody)
                    .build();
        }
    }


    private static Session validateToken(String token) {
        if (token == null) {
            return null;
        }

        return null;
    }
}