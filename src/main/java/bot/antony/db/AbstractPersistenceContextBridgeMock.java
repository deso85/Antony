package bot.antony.db;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.hibernate.jpa.HibernatePersistenceProvider;

import com.google.common.collect.ImmutableMap;

public abstract class AbstractPersistenceContextBridgeMock implements EntityManagerBridge {
	protected InitialContext context;
	protected EntityManager em;

	public AbstractPersistenceContextBridgeMock() {
	}

	protected abstract Map<String, Object> getSessionFactoryProperties();

	public EntityManager getEntityManager() {
		if (this.em == null) {
			EntityManagerFactory entityManagerFactory = (new HibernatePersistenceProvider())
					.createContainerEntityManagerFactory(archiverPersistenceUnitInfo(),
							ImmutableMap.builder().putAll(this.getSessionFactoryProperties()).build());
			this.em = entityManagerFactory.createEntityManager();
		}

		return this.em;
	}

	protected static PersistenceUnitInfo archiverPersistenceUnitInfo() {
		return new PersistenceUnitInfo() {

			public String getPersistenceUnitName() {
				return "ApplicationPersistenceUnit";
			}

			public String getPersistenceProviderClassName() {
				return "org.hibernate.jpa.HibernatePersistenceProvider";
			}

			public PersistenceUnitTransactionType getTransactionType() {
				return PersistenceUnitTransactionType.RESOURCE_LOCAL;
			}

			public DataSource getJtaDataSource() {
				return null;
			}

			public DataSource getNonJtaDataSource() {
				return null;
			}

			public List<String> getMappingFileNames() {
				return Collections.emptyList();
			}

			public List<URL> getJarFileUrls() {
				try {
					return Collections.list(this.getClass().getClassLoader().getResources(""));
				} catch (IOException var2) {
					throw new UncheckedIOException(var2);
				}
			}

			public URL getPersistenceUnitRootUrl() {
				return null;
			}

			public List<String> getManagedClassNames() {
				// return Collections.emptyList();
				return Arrays.asList("bot.antony.db.ConfigurationEntity");
			}

			public boolean excludeUnlistedClasses() {
				return false;
			}

			public SharedCacheMode getSharedCacheMode() {
				return null;
			}

			public ValidationMode getValidationMode() {
				return null;
			}

			public Properties getProperties() {
				return new Properties();
			}

			public String getPersistenceXMLSchemaVersion() {
				return null;
			}

			public ClassLoader getClassLoader() {
				return null;
			}

			public void addTransformer(ClassTransformer transformer) {
			}

			public ClassLoader getNewTempClassLoader() {
				return null;
			}
		};
	}
}
