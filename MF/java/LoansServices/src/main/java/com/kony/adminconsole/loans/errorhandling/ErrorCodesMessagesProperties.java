package com.kony.adminconsole.loans.errorhandling;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ErrorCodesMessagesProperties {
	
	private static final Logger LOGGER = Logger.getLogger(ErrorCodesMessagesProperties.class);
	private static Properties errorCodesMessagesProperties = new Properties();
	/**
	 * Name of the Properties file that this class needs to read from
	 */
	private static final String PROP_FILE_NAME = "ErrorCodesMessages.properties";
	
	/**
	 * Static block to initialize the property object and load the properties
	 */
	static {
		try {
			InputStream inputStream = ErrorCodesMessagesProperties.class.getClassLoader().getResourceAsStream(PROP_FILE_NAME);
			errorCodesMessagesProperties.load(inputStream);
		} catch (FileNotFoundException e) {
			LOGGER.info("Error while reading properties file", e);
		} catch (IOException e) {
			LOGGER.info("Error while reading properties file", e);
		}
	}
	
	private ErrorCodesMessagesProperties(){}
	
	public static String getValueForProperty(String property) {
		String propertyValue=null;
		if(errorCodesMessagesProperties.containsKey(property)){
			propertyValue=errorCodesMessagesProperties.getProperty(property);
		}
		return propertyValue;
	}
}