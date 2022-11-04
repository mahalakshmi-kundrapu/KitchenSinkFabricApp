package com.kony.service.definer.dao.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kony.service.definer.dao.ConfigurationDAO;
import com.kony.service.definer.dto.Configuration;
import com.kony.service.util.FileUtilities;
import com.kony.service.util.JSONUtilities;

/**
 * JSON Storage DAO Implementation of {@link Configuration}
 *
 * @author Aditya Mankal
 */
public class ConfigurationDAOImpl implements ConfigurationDAO {

	public static final String CONFIGURATION_FILE_PATH = "server/Configuration.json";

	private static final Logger LOG = LogManager.getLogger(ConfigurationDAOImpl.class);

	private static ConfigurationDAOImpl configurationDAOImpl;

	private ConfigurationDAOImpl() {
		// Private Constructor
	}

	public static ConfigurationDAOImpl getInstance() {

		if (configurationDAOImpl == null) {
			configurationDAOImpl = new ConfigurationDAOImpl();
		}
		return configurationDAOImpl;

	}

	@Override
	public Configuration getConfiguration() {
		try {
			String configurationFileContents = FileUtilities.readFileFromClassPathToString(CONFIGURATION_FILE_PATH);
			Configuration configuration = JSONUtilities.parse(configurationFileContents, Configuration.class);
			return configuration;
		} catch (Exception e) {
			LOG.error("Exception in constructing Configuration Instance. Exception:", e);
			throw new RuntimeException("Exception in constructing Configuration Instance. Exception", e);
		}
	}

}
