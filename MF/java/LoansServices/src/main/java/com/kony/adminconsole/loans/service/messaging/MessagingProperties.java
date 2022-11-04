package com.kony.adminconsole.loans.service.messaging;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;


public class MessagingProperties {
	
	private static final Logger LOG = Logger.getLogger(MessagingProperties.class);
	private static Properties messagingProperties = new Properties();
	
	static {
		try {
			InputStream inputStream = MessagingProperties.class.getClassLoader().getResourceAsStream("LoansMessagingConfig.properties");
			messagingProperties.load(inputStream);
		} catch (FileNotFoundException e) {
			LOG.info("error while reading properties file", e);
		} catch (IOException e) {
			LOG.info("error while reading properties file", e);
		}
	}
	
	private MessagingProperties(){}
	
	public static String getValueForProperty(String property) {
		if(messagingProperties.containsKey(property)){
			return messagingProperties.getProperty(property);
		}
		return "";
	}
}