package bot.antony.service;

import javax.inject.Inject;

import bot.antony.db.dao.ConfigurationDAOInterface;

public class AntonyService implements AntonyServiceInterface {
	
	@Inject
	private ConfigurationDAOInterface configurationDao;
	
	@Override
	public void init() {
		
	}
	
}