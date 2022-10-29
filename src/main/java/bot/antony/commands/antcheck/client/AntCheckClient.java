package bot.antony.commands.antcheck.client;

import java.util.List;

import bot.antony.commands.antcheck.client.dto.Shop;
import bot.antony.commands.antcheck.client.dto.Specie;
import bot.antony.commands.antcheck.client.dto.Variant;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@Consumes(MediaType.APPLICATION_JSON)
//@Produces(MediaType.TEXT_HTML + "; charset=UTF-8")
//@Consumes(MediaType.TEXT_HTML)
public interface AntCheckClient {

	public static String BASE_URL = "https://old.antcheck.info";

	@GET
	@Path("/species/")
	public List<Specie> getSpecies(@QueryParam("name") String name);

	@GET
	@Path("/variants/")
	public List<Variant> getVariants(@QueryParam("id") String id, @QueryParam("groupby") String groupBy);

	@GET
	@Path("/shops/")
	public List<Shop> getShops(@QueryParam("id") String id);

}
