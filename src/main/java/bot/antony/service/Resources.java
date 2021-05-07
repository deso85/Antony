package bot.antony.service;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;

import bot.antony.db.EntityManagerBridge;

public class Resources {

	
	@Produces
	//@RequestScoped
	public EntityManager getEntityManager(EntityManagerBridge emb) {
		return emb.getEntityManager();
	}
	
}