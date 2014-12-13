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
import javax.ws.rs.core.MediaType;
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
    @Path("watch")
    @Consumes("application/json")
    @Produces(MediaType.TEXT_HTML)
    public Response watch(String searchRequestJSON) throws ExecutionException {
        try {
            JobRequest jobRequest = new Gson().fromJson(searchRequestJSON, JobRequest.class);

            MutableString queryText = new MutableString(jobRequest.getDocumentUrl());
            int slop = jobRequest.getInterval();

            logger.info(queryText + " " + slop + " " + jobRequest.getKeywords().toString());


            StringBuilder sb = new StringBuilder();
//            QueryResult results = Context
//                    .getInstance()
//                    .searchCollection(queryText, slop);
//
//            Multimap<Document, Term> matchedTerms = results.getMatchedTerms();
//            Set<Document> sortedDocuments = matchedTerms.keySet();
//            sb.append("<div class=\"span3\" style=\"padding-left:25px;\">");
//            sb.append("<br/><br/>Obtained ");
//            sb.append(sortedDocuments.size());
//            sb.append(" results in ");
//            sb.append(results.getElapsedTime());
//            sb.append("</div>");
//
//            if (matchedTerms.size() != 0) {
//                int count = 1;
//                for (Document d : sortedDocuments) {
//                    sb.append("<div class=\"result page-header\">");
//                    sb.append("<div class=\"span5\" style=\"padding-left:15px;\">");
//                    sb.append("<h4>");
//                    sb.append(count++);
//                    sb.append(". ");
//                    sb.append(d.getUrl());
//                    sb.append("</h4>");
//                    for (Term t : matchedTerms.get(d)) {
//                        sb.append("<p>");
//                        sb.append(t.getSummaryForDocument(d, 2));
//                        sb.append("</p>");
//                    }
//                    sb.append("</div></div>");
//                }
//                sb.append("<div class=\"span3\" style=\"padding:25px;\">");
//                sb.append("<a href=\"#\" id=\"load\">See more results</a>");
//                sb.append("</div>");
//
//            }

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