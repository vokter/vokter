package pt.ua.adis.project.web.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.adis.project.web.server.Server;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * REST Resource for calls on path "/manage/".
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
@Path("/")
public class MainResource {

    private static final Logger logger = LoggerFactory.getLogger(MainResource.class);

    @GET
    @Path("list")
    @Produces("application/json")
    public Response listOperations() {
        List<String> strings = new ArrayList<>();
        return Response.ok(strings).build();
    }

    @POST
    @Path("subscribe/{name}/{num1}/{num2}")
    @Consumes("application/json")
    @Produces("application/json")
    public String calculate(@PathParam("name") String name,
                            @PathParam("num1") String num1,
                            @PathParam("num2") String num2)
            throws ExecutionException, InterruptedException {

        Future<String> future = Server.getInstance().getExecutor().submit(new Callable<String>() {

            @Override
            public String call() throws Exception {
                return null;
            }
        });

        return future.get();
    }
}
