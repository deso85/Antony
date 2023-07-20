package bot.antony.commands.antcheck;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.core.type.TypeReference;

import bot.antony.Antony;
import bot.antony.commands.antcheck.client.AntCheckClient;
import bot.antony.commands.antcheck.client.dto.Offer;
import bot.antony.commands.antcheck.client.dto.Shop;
import bot.antony.commands.antcheck.client.dto.Specie;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;

/**
 * Controller for antcheck.info API
 */
public class AntcheckController {
	private String subdir = "antcheck" + File.separator;
	private String backupdir = subdir + "backup" + File.separator;
	private String offersFileName = "offers.json";
	private String shopsFileName = "shops.json";
	private String blShopsFileName = "shops.blacklisted.json";
	private String speciesFileName = "species.json";
	private LocalDateTime lastUpdatedDateTime = LocalDateTime.now();
	private LocalDateTime nextUpdateDateTime = LocalDateTime.now().minusDays(1);
	private DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
	private DateTimeFormatter backupName = DateTimeFormatter.ofPattern("yyyy.MM.dd");
	private AntCheckClient antCheckClient = Utils.getAntCheckClient();
	private List<Offer> offers = new ArrayList<Offer>();
	private List<Shop> shops = new ArrayList<Shop>();
	private List<Specie> species = new ArrayList<Specie>();
	private List<Shop> blShops = new ArrayList<Shop>();
	private boolean isRunning = false;
	private Thread updateThread;

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public AntcheckController() {
		loadData();
		Antony.getLogger().info("Created antcheck controller.");
		
		File directory = new File(Antony.getDataPath() + backupdir);
		if (!directory.exists()){
			directory.mkdirs();
	    }
	}
	
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	public void updateData() {
		if(nextUpdateDateTime.isBefore(LocalDateTime.now())) {
			
			if(updateThread != null && updateThread.isAlive()) {
				updateThread.interrupt();
				Antony.getLogger().error("[Antcheck Controller] Update Thread interrupted due to a timeout.");
			}
			updateThread = new Thread() {
				public void run() {
					try {
						Antony.getLogger().info("[Antcheck Controller] Updating Offers");
						updateOffers();
						Thread.sleep(5000); //5sec
						Antony.getLogger().info("[Antcheck Controller] Updating Shops");
						updateShops();
						Thread.sleep(5000); //5sec
						Antony.getLogger().info("[Antcheck Controller] Updating Species");
						updateSpecies();
						Antony.getLogger().info("[Antcheck Controller] Backing Up Data");
						backupData();
						lastUpdatedDateTime = LocalDateTime.now();
						nextUpdateDateTime = LocalDateTime.now().plusMinutes(60).truncatedTo(ChronoUnit.HOURS);
						Antony.getLogger().info("[Antcheck Controller] Updated Data. Next update: " + nextUpdateDateTime.format(dtFormatter));
						
					} catch (InterruptedException e) {
						Antony.getLogger().error("Wasn't able to put Thread asleep.", e);
					}
				}
			};
			updateThread.start();
		}
	}
	
	public boolean updateOffers() {
		List<Offer> updatedOffers = antCheckClient.getOffers();
		if(updatedOffers != null && updatedOffers.size() > 0) {
			offers = updatedOffers;
			saveOffers();
			return true;
		}
		return false;
	}
	
	public boolean updateShops() {
		List<Shop> updatedShops = antCheckClient.getShops();
		if(updatedShops != null && updatedShops.size() > 0) {
			shops = updatedShops;
			saveShops();
			return true;
		}
		return false;
	}

	public boolean updateSpecies() {
		List<Specie> updatedSpecies = antCheckClient.getSpecies();
		if(updatedSpecies != null && updatedSpecies.size() > 0) {
			species = updatedSpecies;
			saveSpecies();
			return true;
		}
		return false;
	}
	
	private void loadData() {
		loadOffers();
		loadShops();
		loadSpecies();
		loadBlShops();
	}
	
	private void saveData() {
		saveOffers();
		saveShops();
		saveSpecies();
		saveBlShops();
	}
	
	private void backupData() {
		try {
			String offersFile = Antony.getDataPath() + subdir + offersFileName;
			String shopsFile = Antony.getDataPath() + subdir + shopsFileName;
			String speciesFile = Antony.getDataPath() + subdir + speciesFileName;
			final List<String> srcFiles = Arrays.asList(offersFile, shopsFile, speciesFile);
			
			FileOutputStream fos;
			fos = new FileOutputStream(Antony.getDataPath() + backupdir + LocalDateTime.now().format(backupName) + ".zip");
			ZipOutputStream zipOut = new ZipOutputStream(fos);
			
			for (String srcFile : srcFiles) {
			    File fileToZip = new File(srcFile);
			    FileInputStream fis = new FileInputStream(fileToZip);
			    ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
			    zipOut.putNextEntry(zipEntry);
			
			    byte[] bytes = new byte[1024];
			    int length;
			    while((length = fis.read(bytes)) >= 0) {
			        zipOut.write(bytes, 0, length);
			    }
			    fis.close();
			}
			zipOut.close();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void loadOffers() {
		offers = (List<Offer>) Utils.loadJSONData(subdir, offersFileName, new TypeReference<List<Offer>>(){}, offers);
	}
	
	private void saveOffers() {
		Utils.saveJSONData(subdir, offersFileName, offers);
	}
	
	@SuppressWarnings("unchecked")
	private void loadShops() {
		shops = (List<Shop>) Utils.loadJSONData(subdir, shopsFileName, new TypeReference<List<Shop>>(){}, shops);
	}
	
	private void saveShops() {
		Utils.saveJSONData(subdir, shopsFileName, shops);
	}
	
	@SuppressWarnings("unchecked")
	private void loadSpecies() {
		species = (List<Specie>) Utils.loadJSONData(subdir, speciesFileName, new TypeReference<List<Specie>>(){}, species);
	}
	
	private void saveSpecies() {
		Utils.saveJSONData(subdir, speciesFileName, species);
	}
	
	@SuppressWarnings("unchecked")
	private void loadBlShops() {
		blShops = (List<Shop>) Utils.loadJSONData(subdir, blShopsFileName, new TypeReference<List<Shop>>(){}, blShops);
	}
	
	private void saveBlShops() {
		Utils.saveJSONData(subdir, blShopsFileName, blShops);
	}
	
	public List<Shop> addBlShop(int id) {
		List<Shop> result = antCheckClient.getShopsById(id);
		blShops.addAll(antCheckClient.getShopsById(id));
		saveBlShops();
		return result;
	}
	
	public List<Shop> addBlShop(String name) {
		List<Shop> result = antCheckClient.getShopsByName(name);
		blShops.addAll(antCheckClient.getShopsByName(name));
		saveBlShops();
		return result;
	}
	
	public List<Shop> removeBlShop(int id) {
		List<Shop> result = antCheckClient.getShopsById(id);
		blShops.removeAll(result);
		saveBlShops();
		return result;
	}
	
	public List<Shop> removeBlShop(String name) {
		List<Shop> result = antCheckClient.getShopsByName(name);
		blShops.removeAll(result);
		saveBlShops();
		return result;
	}
	
	public List<Specie> getAntsByGenus(String genus) {
		return species.stream()
				.filter(specie -> specie.getGenus().equalsIgnoreCase(genus))
				.collect(Collectors.toList());
	}
	
	public List<Specie> getAntsBySpecies(String specieName) {
		return species.stream()
				.filter(specie -> specie.getSpecies().equalsIgnoreCase(specieName))
				.collect(Collectors.toList());
	}
	
	public List<Specie> getAntsByGenusAndSpecies(String genus, String specieName) {
		return species.stream()
				.filter(specie -> specie.getGenus().equalsIgnoreCase(genus))
				.filter(specie -> specie.getSpecies().equalsIgnoreCase(specieName))
				.collect(Collectors.toList());
	}
	
	public List<Specie> getAntsLikeGenus(String genus) {
		return species.stream()
				.filter(specie -> specie.getGenus().toLowerCase().contains(genus.toLowerCase()))
				.collect(Collectors.toList());
	}
	
	public List<Specie> getAntsLikeSpecies(String specieName) {
		return species.stream()
				.filter(specie -> specie.getSpecies().toLowerCase().contains(specieName.toLowerCase()))
				.collect(Collectors.toList());
	}
	
	public List<Specie> getAntsLikeGenusAndSpecies(String genus, String specieName) {
		return species.stream()
				.filter(specie -> specie.getGenus().toLowerCase().contains(genus.toLowerCase()))
				.filter(specie -> specie.getSpecies().toLowerCase().contains(specieName.toLowerCase()))
				.collect(Collectors.toList());
	}
	
	public List<Specie> getAntsConcatGenusAndSpecies(String genus, String specieName) {
		return species.stream()
				.filter(specie -> specie.getGenus().toLowerCase().endsWith(genus.toLowerCase()))
				.filter(specie -> specie.getSpecies().toLowerCase().startsWith(specieName.toLowerCase()))
				.collect(Collectors.toList());
	}
	
	public List<Specie> getAntsByGenusOrSpecies(String name) {
		List<Specie> returnList = new ArrayList<Specie>();
		returnList.addAll(getAntsByGenus(name));
		returnList.addAll(getAntsBySpecies(name));
		return returnList;
	}
	
	public List<Specie> getAntsLikeGenusOrSpecies(String name) {
		List<Specie> returnList = new ArrayList<Specie>();
		returnList.addAll(getAntsLikeGenus(name));
		returnList.addAll(getAntsLikeSpecies(name));
		return returnList;
	}
	
	public List<Specie> findAnt(String genus, String specieName) {
		List<Specie> specieList = new ArrayList<Specie>();
		
		//exact match
		specieList = getAntsByGenusAndSpecies(genus, specieName);
		Antony.getLogger().debug("[Antcheck Controller] Found " + specieList.size() + " ant(s) by exact search with genus (" + genus + ") and species name (" + specieName + ")");
		if(specieList.size() != 1) {
			//genus and species name concatenated found
			specieList = getAntsConcatGenusAndSpecies(genus, specieName);
			Antony.getLogger().debug("[Antcheck Controller] Found " + specieList.size() + " ant(s) by concatenated search with genus (" + genus + ") and species name (" + specieName + ")");
			if(specieList.size() != 1) {
				//genus and species name like found
				specieList = getAntsLikeGenusAndSpecies(genus, specieName);
				Antony.getLogger().debug("[Antcheck Controller] Found " + specieList.size() + " ant(s) by like search with genus (" + genus + ") and species name (" + specieName + ")");
			}
		}
		
		return specieList;
	}
	
	public List<Specie> findAnt(String name) {
		List<Specie> specieList = new ArrayList<Specie>();
		
		//exact match for genus or specie name
		specieList = getAntsByGenusOrSpecies(name);
		Antony.getLogger().debug("[Antcheck Controller] Found " + specieList.size() + " ant(s) by exact search with genus (" + name + ") or species name (" + name + ")");
		if(specieList.size() != 1) {
			//genus and species name like found
			specieList = getAntsLikeGenusOrSpecies(name);
			Antony.getLogger().debug("[Antcheck Controller] Found " + specieList.size() + " ant(s) by like search with genus (" + name + ") or species name (" + name + ")");
		}
		
		return specieList;
	}
	
	public List<Offer> getOffersForAnt(Specie ant) {
		List<Offer> offerList = new ArrayList<Offer>();
		
		offerList = getOffers().stream()
				.filter(offer -> offer.getSpeciesid().equals(ant.getId()))
				.collect(Collectors.toList());
		
		return offerList;
	}
	
	public List<Offer> getOffersForAntWithoutBlShops(Specie ant) {
		List<Offer> offerList = getOffersForAnt(ant);
		
		for(Shop shop : getBlShops()) {
			offerList.removeIf(offer -> offer.getShopid().equals(shop.getId()));
		}
		
		return offerList;
	}
	
	public List<Shop> getShopsByOffers(List<Offer> offers){
		Set<Shop> shops = new HashSet<>();
		
		//Get all shops
		for(Offer offer : offers) {
			if(getShopById(offer.getShopid()) != null) {
				shops.add(getShopById(offer.getShopid()));
			}
		}
		
		return new ArrayList<Shop>(shops);
	}
	
	public Shop getShopById(int id) {
		//because of data inconsistency
		if(shops.stream()
			.filter(shop -> shop.getId().equals(id))
			.collect(Collectors.toList()).isEmpty()) {
			return null;
		}
		
		return shops.stream()
			.filter(shop -> shop.getId().equals(id))
			.collect(Collectors.toList()).get(0);
	}
	
	public List<Shop> removeBlacklistedShops(List<Shop> shops) {
		shops.removeAll(blShops);
		return shops;
	}
	
	public void run(JDA jda) {
		if(!isRunning) {
			isRunning = true;
			Antony.getLogger().info("[Antcheck Controller] Starting Runner");
			Thread timerThread = new Thread() {
				public void run() {
					while(jda.getPresence().getStatus() == OnlineStatus.ONLINE) {
						try {
							updateData();
							Thread.sleep(60000); //1min
						} catch (InterruptedException e) {
							Antony.getLogger().error("Wasn't able to put Thread asleep.", e);
						}
					}
					isRunning = false;
					Antony.getLogger().info("[Antcheck Controller] Stopping Runner");
				}
			};
			timerThread.start();
		}
	}

	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	public List<Offer> getOffers() {
		return offers;
	}
	
	public List<Shop> getShops() {
		return shops;
	}

	public List<Specie> getSpecies() {
		return species;
	}

	public List<Shop> getBlShops() {
		return blShops;
	}

	public LocalDateTime getLastUpdatedDateTime() {
		return lastUpdatedDateTime;
	}
	
}