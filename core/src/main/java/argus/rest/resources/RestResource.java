package argus.rest.resources;

import argus.Context;
import argus.watcher.JobRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.lang.MutableString;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * REST Resource for calls on path "/web/".
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
@Path("/")
public class RestResource {

    private static final Logger logger = LoggerFactory.getLogger(RestResource.class);


    @POST
    @Path("search")
    @Consumes("application/json")
    @Produces(MediaType.TEXT_HTML)
    public Response search(String searchRequestJSON) throws ExecutionException {
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


    @POST
    @Path("save-settings")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response saveSettings(FormDataMultiPart formParams) {

        Map<String, List<FormDataBodyPart>> fieldsByName = formParams.getFields();
        boolean isStemmingEnabled = false;
        boolean isStoppingEnabled = false;
        boolean ignoreCase = false;

        for (List<FormDataBodyPart> fields : fieldsByName.values()) {
            for (FormDataBodyPart field : fields) {
                String fieldName = field.getName();

                switch (fieldName) {

                    case "isStoppingEnabled":
                        isStoppingEnabled = true;
                        break;

                    case "isStemmingEnabled":
                        isStemmingEnabled = true;
                        break;

                    case "ignoreCase":
                        ignoreCase = true;
                        break;
                }
            }
        }

        Context context = Context.getInstance();
        context.setStopwordsEnabled(isStoppingEnabled);
        context.setStemmingEnabled(isStemmingEnabled);
        context.setIgnoreCase(ignoreCase);

        return refreshIndexPage();
    }


//    @POST
//    @Path("upload-stop")
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    @Produces(MediaType.TEXT_HTML)
//    public Response uploadStopwords(
//            @FormDataParam("stopwordFile") InputStream fileStream,
//            @FormDataParam("stopwordFile") FormDataContentDisposition fileDetail) {
//
//        String fileName = fileDetail.getFileName();
//
//        if (!fileName.isEmpty()) {
//            StopwordFileLoader loader = new StopwordFileLoader();
//            Set<MutableString> stopwords = loader.load(fileStream);
//            loader = null;
//
//            Context.getInstance().setStopwords(stopwords);
//        }
//
//        return refreshIndexPage();
//    }


    private Response refreshIndexPage() {
        URI uri = UriBuilder.fromUri("../index.jsp").build();
        return Response.temporaryRedirect(uri).build();
    }
}