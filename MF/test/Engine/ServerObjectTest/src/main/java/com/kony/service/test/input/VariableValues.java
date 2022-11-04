package com.kony.service.test.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kony.service.driver.TestExecutionCLI;

/**
 * Class to read the Property File containg the property values
 *
 * @author Aditya Mankal
 */
public class VariableValues {

	private static final Logger LOG = LogManager.getLogger(VariableValues.class);

	public static final Properties CONFIG_PROPS = loadConfigProps();

	private static Properties loadConfigProps() {

		// Validate Property File Path
		if (StringUtils.isNotBlank(TestExecutionCLI.TEST_VARIABLE_VALUES_PROPERTY_FILE_PATH)) {
			File propertyFile = new File(TestExecutionCLI.TEST_VARIABLE_VALUES_PROPERTY_FILE_PATH);

			if (propertyFile != null && propertyFile.exists() && propertyFile.isFile() && propertyFile.canRead()) {
				Properties properties = new Properties();
				try (InputStream inputStream = new FileInputStream(TestExecutionCLI.TEST_VARIABLE_VALUES_PROPERTY_FILE_PATH)) {
					LOG.debug("Loading Variable Values from Property File..");
					properties.load(inputStream);
					Enumeration<?> e = properties.propertyNames();
					while (e.hasMoreElements()) {
						String key = (String) e.nextElement();
						String value = properties.getProperty(key);
						properties.put(key.trim(), value.trim());
					}
					LOG.debug("Loaded Variable Values from Property File.");
				} catch (Exception e) {
					LOG.error("Exception in reading Variable Values Property File", e);
				}
				return properties;

			}
		}
		return null;
	}

	/**
	 * Method to get the list of Property Names
	 * 
	 * @return list of Property Names
	 */
	public static Set<String> getPropertyNames() {
		return CONFIG_PROPS == null ? new HashSet<String>() : CONFIG_PROPS.stringPropertyNames();
	}

	/**
	 * Method to get the value of a property
	 * 
	 * @param key
	 * @return Property Value
	 */
	public static String getValue(String key) {
		return CONFIG_PROPS == null ? StringUtils.EMPTY : CONFIG_PROPS.getProperty(key);
	}
}
