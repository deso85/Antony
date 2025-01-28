package bot.antony.commands.antcheck.client;

import java.util.List;

import bot.antony.commands.antcheck.client.dto.*;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v2")
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@Consumes(MediaType.APPLICATION_JSON)
public interface AntCheckClient {

	public static String BASE_URL = "https://antcheck.info";

	@GET
	@Path("/ecommerce/currencies")
	public List<Currency> getCurrencies(@QueryParam("limit") String limit);

	@GET
	@Path("/ecommerce/products")
	public List<Product> getProducts(@QueryParam("limit") String limit);

	@GET
	@Path("/ecommerce/shops")
	public List<Shop> getShops(@QueryParam("limit") String limit);
	
	@GET
	@Path("/ecommerce/shops")
	public List<Shop> getShopsById(@QueryParam("id") Integer id);
	
	@GET
	@Path("/ecommerce/shops")
	public List<Shop> getShopsByName(@QueryParam("name") String name, @QueryParam("limit") String limit);

	@GET
	@Path("/ants/species")
	public List<Specie> getSpecies(@QueryParam("limit") String limit);

	@GET
	@Path("/ecommerce/variants")
	public List<Variant> getVariants(@QueryParam("limit") String limit);

}
