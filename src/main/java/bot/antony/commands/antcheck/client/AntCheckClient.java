package bot.antony.commands.antcheck.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/v1")
//@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
//@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.TEXT_HTML + "; charset=UTF-8")
@Consumes(MediaType.TEXT_HTML)
public interface AntCheckClient {

	public static String BASE_URL = "https://old.antcheck.info";

	@GET
	@Path("/species/")
	public Response getSpecies(@QueryParam("name") String name);

	@GET
	@Path("/variants/")
	public Response getVariants(@QueryParam("id") String id, @QueryParam("groupby") String groupBy);

	@GET
	@Path("/shops/")
	public Response getShops(@QueryParam("id") String id);

}
