package bot.antony.commands.antcheck.client;

import java.util.List;

import bot.antony.commands.antcheck.client.dto.Offer;
import bot.antony.commands.antcheck.client.dto.Shop;
import bot.antony.commands.antcheck.client.dto.Specie;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/api/antcheck")
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@Consumes(MediaType.APPLICATION_JSON)
public interface AntCheckClient {

	public static String BASE_URL = "https://antcheck.info";
	
	@GET
	@Path("/species")
	public List<Specie> getSpecies();
	
	@GET
	@Path("/species")
	public List<Specie> getSpeciesByGenus(@QueryParam("genus") String genus);
	
	@GET
	@Path("/species")
	public List<Specie> getSpeciesBySpecies(@QueryParam("species") String species);
	
	@GET
	@Path("/species")
	public List<Specie> getSpeciesByGenusAndSpecies(@QueryParam("genus") String genus, @QueryParam("species") String species);

	@GET
	@Path("/offers")
	public List<Offer> getOffers();
	
	@GET
	@Path("/offers")
	public List<Offer> getOffersByShopId(@QueryParam("shopid") Integer shopid);
	
	@GET
	@Path("/offers")
	public List<Offer> getOffersBySpeciesId(@QueryParam("speciesid") Integer speciesid);
	
	@GET
	@Path("/offers")
	public List<Offer> getOffersByShopIdAndSpeciesId(@QueryParam("shopid") Integer shopid, @QueryParam("speciesid") Integer speciesid);

	@GET
	@Path("/shops")
	public List<Shop> getShops();
	
	@GET
	@Path("/shops")
	public List<Shop> getShopsById(@QueryParam("shopid") Integer shopid);
	
	@GET
	@Path("/shops")
	public List<Shop> getShopsByName(@QueryParam("shopname") String shopname);
	
	@GET
	@Path("/shops")
	public List<Shop> getShopsByIdAndName(@QueryParam("shopid") Integer shopid, @QueryParam("shopname") String shopname);

}
