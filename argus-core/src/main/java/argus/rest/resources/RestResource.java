package argus.rest.resources;

import argus.job.JobRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.lang.MutableString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.concurrent.ExecutionException;

/**
 * REST Resource for calls on path "/rest/".
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
@Singleton
@Path("/")
public class RestResource {

    private static final Logger logger = LoggerFactory.getLogger(RestResource.class);

    @GET
    @Path("test")
    public String test() {
        return "test";
    }

    @POST
    @Path("testResponse")
    @Consumes("application/json")
    public Response response(String responseJSON) throws ExecutionException {
        System.out.println(responseJSON);
        return Response.ok("Yep").build();
    }

    @POST
    @Path("watch")
    @Consumes("application/json")
    @Produces("application/json")
    public Response watch(String searchRequestJSON) throws ExecutionException {
        try {
            JobRequest jobRequest = new Gson().fromJson(searchRequestJSON, JobRequest.class);

            MutableString queryText = new MutableString(jobRequest.getRequestUrl());
            int slop = jobRequest.getInterval();

            logger.info(queryText + " " + slop + " " + jobRequest.getKeywords().toString());


            StringBuilder sb = new StringBuilder();

            return Response.ok(sb.toString()).build();

        } catch (JsonSyntaxException ex) {
            return Response.ok("<div class=\"span3\" style=\"padding-left:25px;\">"
                    + "<br/><br/>The inserted search query is invalid.</div>")
                    .build();
        }
    }


    private Response refreshIndexPage() {
        URI uri = UriBuilder.fromUri("../index.jsp").build();
        return Response.temporaryRedirect(uri).build();
    }
}