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

package com.edduarte.argus.client;

import com.google.gson.Gson;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Resource for calls on path "/rest/".
 *
 * @author Ed Duarte (<a href="mailto:ed@edduarte.com">ed@edduarte.com</a>)
 * @version 1.4.1
 * @since 1.0.0
 */
@Path("/")
public class ClientResource {

    /**
     * WARNING: If did not deploy this client in the default host, change the
     * following 4 Strings accordingly.
     */
    private static final String CLIENT_PROTOCOL = "http:";
    private static final String CLIENT_HOSTNAME = "localhost";
    private static final String CLIENT_PORT = "8080";
    private static final String CLIENT_WAR_NAME = ""; // write the war name if its deployed on jetty, or leave this empty if its deployed on tomcat

    /**
     * WARNING: If did not deploy Argus server in its default host, change the
     * following 4 Strings accordingly.
    */
    private static final String SERVER_PROTOCOL = "http:";
    private static final String SERVER_HOSTNAME = "localhost";
    private static final String SERVER_PORT = "9000";
    private static final String SERVER_WAR_NAME = ""; // write the war name if its deployed on jetty, or leave this empty if its deployed on tomcat






    private static final String CLIENT_URL;
    static {
        StringBuilder aux = new StringBuilder()
                .append(CLIENT_PROTOCOL)
                .append("//")
                .append(CLIENT_HOSTNAME)
                .append(":")
                .append(CLIENT_PORT)
                .append("/")
                .append(CLIENT_WAR_NAME);
        if (aux.charAt(aux.length() - 1) != '/') {
            aux.append("/");
        }
        aux.append("rest/notification/");
        CLIENT_URL = aux.toString();
    }

    private static final String SERVER_URL;
    static {
        StringBuilder aux = new StringBuilder()
                .append(SERVER_PROTOCOL)
                .append("//")
                .append(SERVER_HOSTNAME)
                .append(":")
                .append(SERVER_PORT)
                .append("/")
                .append(SERVER_WAR_NAME);
        if (aux.charAt(aux.length() - 1) != '/') {
            aux.append("/");
        }
        aux.append("argus/v1/");
        SERVER_URL = aux.toString();
    }


    /**
     * This is the required REST POST method that needs to be implemented by
     * the client in order to receive notifications from Argus.
     */
    @POST
    @Path("notification")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response notification(String notificationBody) {
        // print notification in console
        System.out.println("Received notification:" + notificationBody);

        // always return a successful response to Argus' notifications
        return Response.status(200)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }


    /**
     * Usage:
     * http://localhost:8080/rest/watch?url=ENCODED_URL&keywords=KEYWORDS_SEPARATED_WITH_COMMAS
     *
     * Example (enter this in the address bar of your browser):
     * http://localhost:8080/rest/watch?url=http%3A%2F%2Fbbc.com&keywords=a,the
     */
    @GET
    @Path("watch")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response watch(@QueryParam("url") String url,
                          @QueryParam("keywords") String keywords) {

        // build a request JSON object with all three mandatory fields
        Map<String, Object> requestJson = new HashMap<>();
        requestJson.put("documentUrl", url);
        requestJson.put("clientUrl", CLIENT_URL);
        requestJson.put("keywords", Arrays.asList(keywords.split(",")));
        String input = new Gson().toJson(requestJson);

        // send the above JSON body to /argus/v1/subscribe/
        ResponseBody responseBody = sendRequest(SERVER_URL + "subscribe/", input);
        return parseResponse(url, "watch", responseBody);
    }


    /**
     * Usage:
     * http://localhost:8080/rest/cancel?url=ENCODED_URL
     *
     * Example (enter this in the address bar of your browser):
     * http://localhost:8080/rest/cancel?url=http%3A%2F%2Fbbc.com
     */
    @GET
    @Path("cancel")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cancel(@QueryParam("url") String url) {

        // build a request JSON object with all two mandatory fields
        Map<String, Object> requestJson = new HashMap<>();
        requestJson.put("documentUrl", url);
        requestJson.put("clientUrl", CLIENT_URL);
        String input = new Gson().toJson(requestJson);

        // send the above JSON body to /argus/v1/cancel/
        ResponseBody responseBody = sendRequest(SERVER_URL + "cancel/", input);
        return parseResponse(url, "cancel", responseBody);
    }


    private ResponseBody sendRequest(final String serverUrl, final String input) {
        try {
            URL targetUrl = new URL(serverUrl);

            HttpURLConnection c = (HttpURLConnection) targetUrl.openConnection();
            c.setDoOutput(true);
            c.setRequestMethod("POST");
            c.setRequestProperty("Content-Type", "application/json");

            OutputStream outputStream = c.getOutputStream();
            outputStream.write(input.getBytes());
            outputStream.flush();

            InputStream is;
            try {
                is = c.getInputStream();
            } catch (IOException ex) {
                is = c.getErrorStream();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();
            c.disconnect();

            return ResponseBody.fromJson(sb.toString());

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }


    private Response parseResponse(String documentUrl, String action, ResponseBody responseBody) {
        if (responseBody != null) {

            StringBuilder messageToUser = new StringBuilder();
            if (responseBody.getCode() == 0) {
                // response code is 0
                // tell the user that the watch request was successful
                messageToUser.append("<b>The request to ")
                        .append(action)
                        .append(" the url <u>")
                        .append(documentUrl)
                        .append("</u> was successful!</b>");

                return Response.status(200)
                        .type(MediaType.TEXT_HTML)
                        .entity(messageToUser.toString())
                        .build();
            } else {
                // tell the user that the watch request was NOT successful
                messageToUser.append("<b>The request to ")
                        .append(action)
                        .append(" the url <u>")
                        .append(documentUrl)
                        .append("</u> was NOT successful!<br/>Error message:</b>")
                        .append(responseBody.getMessage());

                return Response.status(400)
                        .type(MediaType.TEXT_HTML)
                        .entity(messageToUser.toString())
                        .build();
            }
        } else {
            return Response.status(500)
                    .type(MediaType.TEXT_HTML)
                    .entity("<b>Error while requesting the server.</b>")
                    .build();
        }
    }
}