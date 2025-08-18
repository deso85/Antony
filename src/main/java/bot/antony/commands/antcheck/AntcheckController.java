package bot.antony.commands.antcheck;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import bot.antony.commands.antcheck.client.dto.*;
import bot.antony.commands.antcheck.client.dto.Currency;
import com.fasterxml.jackson.core.type.TypeReference;

import bot.antony.Antony;
import bot.antony.commands.antcheck.client.AntCheckClient;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;

/**
 * Controller for antcheck.info API
 */
public class AntcheckController {
	private final String subdir = "antcheck" + File.separator;
	private final String backupdir = subdir + "backup" + File.separator;
	private final String currenciesFileName = "currencies.json";
	private final String productsFileName = "products.json";
	private final String shopsFileName = "shops.json";
	private final String blShopsFileName = "shops.blacklisted.json";
	private final String speciesFileName = "species.json";
	private final String variantsFileName = "variants.json";
	private LocalDateTime lastUpdatedDateTime = LocalDateTime.now();
	private LocalDateTime nextUpdateDateTime = LocalDateTime.now().minusDays(1);
	private final DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
	private final DateTimeFormatter backupName = DateTimeFormatter.ofPattern("yyyy.MM.dd");
	private final AntCheckClient antCheckClient = Utils.getAntCheckClient();
	private List<Currency> currencies = new ArrayList<Currency>();
	private List<Product> products = new ArrayList<Product>();
	private List<Shop> shops = new ArrayList<Shop>();
	private List<Specie> species = new ArrayList<Specie>();
	private List<Variant> variants = new ArrayList<Variant>();
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
						Antony.getLogger().info("[Antcheck Controller] Updating Currencies");
						updateCurrencies();
						Thread.sleep(5000); //5sec
						Antony.getLogger().info("[Antcheck Controller] Updating Products");
						updateProducts();
						Thread.sleep(5000); //5sec
						Antony.getLogger().info("[Antcheck Controller] Updating Shops");
						updateShops();
						Thread.sleep(5000); //5sec
						Antony.getLogger().info("[Antcheck Controller] Updating Species");
						updateSpecies();
						Thread.sleep(5000); //5sec
						Antony.getLogger().info("[Antcheck Controller] Updating Variants");
						updateVariants();
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
	
	public boolean updateCurrencies() {
		List<Currency> updatedCurrencies = antCheckClient.getCurrencies("-1");
		if(updatedCurrencies != null && !updatedCurrencies.isEmpty()) {
			currencies = updatedCurrencies;
			saveCurrencies();
			return true;
		}
		return false;
	}

	public boolean updateProducts() {
		List<Product> updatedProducts = antCheckClient.getProducts("-1");
		if(updatedProducts != null && !updatedProducts.isEmpty()) {
			products = updatedProducts;
			saveProducts();
			return true;
		}
		return false;
	}
	
	public boolean updateShops() {
		List<Shop> updatedShops = antCheckClient.getShops("-1");
		if(updatedShops != null && !updatedShops.isEmpty()) {
			shops = updatedShops;
			saveShops();
			return true;
		}
		return false;
	}

	public boolean updateSpecies() {
		List<Specie> updatedSpecies = antCheckClient.getSpecies("-1");
		if(updatedSpecies != null && !updatedSpecies.isEmpty()) {
			species = updatedSpecies;
			saveSpecies();
			return true;
		}
		return false;
	}

	public boolean updateVariants() {
		List<Variant> updatedVariants = antCheckClient.getVariants("-1");
		if(updatedVariants != null && !updatedVariants.isEmpty()) {
			variants = updatedVariants;
			saveVariants();
			return true;
		}
		return false;
	}

	private void loadData() {
		loadCurrencies();
		loadProducts();
		loadShops();
		loadSpecies();
		loadBlShops();
		loadVariants();
	}
	
	private void saveData() {
		saveCurrencies();
		saveProducts();
		saveShops();
		saveSpecies();
		saveBlShops();
		saveVariants();
	}
	
	private void backupData() {
		try {
			String currenciesFile = Antony.getDataPath() + subdir + currenciesFileName;
			String productsFile = Antony.getDataPath() + subdir + productsFileName;
			String shopsFile = Antony.getDataPath() + subdir + shopsFileName;
			String speciesFile = Antony.getDataPath() + subdir + speciesFileName;
			String variantsFile = Antony.getDataPath() + subdir + variantsFileName;
			final List<String> srcFiles = Arrays.asList(currenciesFile, productsFile, shopsFile, speciesFile, variantsFile);
			
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
	private void loadCurrencies() {
		currencies = (List<Currency>) Utils.loadJSONData(subdir, currenciesFileName, new TypeReference<List<Currency>>(){}, currencies);
	}
	
	private void saveCurrencies() {
		Utils.saveJSONData(subdir, currenciesFileName, currencies);
	}

	@SuppressWarnings("unchecked")
	private void loadProducts() {
		products = (List<Product>) Utils.loadJSONData(subdir, productsFileName, new TypeReference<List<Product>>(){}, products);
	}

	private void saveProducts() {
		Utils.saveJSONData(subdir, productsFileName, products);
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
	private void loadVariants() {
		variants = (List<Variant>) Utils.loadJSONData(subdir, variantsFileName, new TypeReference<List<Variant>>(){}, variants);
	}

	private void saveVariants() {
		Utils.saveJSONData(subdir, variantsFileName, variants);
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
		List<Shop> result = antCheckClient.getShopsByName(name, "-1");
		blShops.addAll(antCheckClient.getShopsByName(name, "-1"));
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
		List<Shop> result = antCheckClient.getShopsByName(name, "-1");
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
	
	public Shop getShopById(int id) {
		//because of data inconsistency
		if(shops.stream().noneMatch(shop -> shop.getId().equals(id))) {
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
	
	public List<Shop> getNonBlacklistedShops() {
		return removeBlacklistedShops(shops);
	}

	public List<Product> getAntProducts() {
		return products.stream()
				.filter(product -> product.getProduct_type().equalsIgnoreCase("ants"))
				.collect(Collectors.toList());
	}

	public List<Product> getAvailableAntProducts(Specie ant) {
		return products.stream()
				.filter(product -> product.getProduct_type().equalsIgnoreCase("ants"))
				.filter(product -> Objects.equals(product.getType_id(), ant.getId()))
				.filter(Product::isIn_stock)
				.filter(Product::isIs_active)
				.collect(Collectors.toList());
	}

    public List<Product> getAvailableProductsByShop(Shop shop) {
        return getAntProducts().stream()
                .filter(Product::isIn_stock)
                .filter(Product::isIs_active)
                .filter(p -> Objects.equals(p.getShop_id(), shop.getId()))
                .collect(Collectors.toList());
    }

    public List<Product> getFilteredAvailableAntProducts(Specie ant) {
		List<Product> products = getAvailableAntProducts(ant);
		List<Shop> shops = getNonBLOnlineShops();
		return getProductsFilteredByShops(products, shops);
	}

	public List<Product> getProductsFilteredByShops(List<Product> products, List<Shop> shops) {
		List<Product> newList = new ArrayList<>();

		for(Product product : products) {
			for(Shop shop : shops) {
				if(product.getShop_id().equals(shop.getId())) {
					newList.add(product);
				}
			}
		}

		return newList;
	}

	public List<Variant> getAvailableProductVariants(Product product) {
		return getAvailableVariants().stream()
				.filter(variant -> variant.getProduct_id().equals(product.getId()))
				.collect(Collectors.toList());
	}

	public List<Variant> getAvailableVariants() {
		return getVariants().stream()
				.filter(Variant::isIn_stock)
				.filter(Variant::isIs_active)
				.collect(Collectors.toList());
	}

	public List<Shop> getOnlineShops() {
		return shops.stream()
				.filter(Shop::isOnline)
				.collect(Collectors.toList());
	}

	public List<Shop> getNonBLOnlineShops() {
		return removeBlacklistedShops(getOnlineShops());
	}

	public List<Shop> getShopsFromProducts(List<Product> products) {
		Set<Shop> shops = new HashSet<>();

		for(Product product : products) {
			shops.add(getShopById(product.getShop_id()));
		}

		return new ArrayList<>(shops);
	}

	public Currency getCurrency(String iso) {
		return getCurrencies().stream()
				.filter(currency -> currency.getIso().equals(iso))
				.collect(Collectors.toList()).get(0);
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
	public List<Currency> getCurrencies() {
		return currencies;
	}

	public List<Product> getProducts() {
		return products;
	}

	public List<Variant> getVariants() {
		return variants;
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