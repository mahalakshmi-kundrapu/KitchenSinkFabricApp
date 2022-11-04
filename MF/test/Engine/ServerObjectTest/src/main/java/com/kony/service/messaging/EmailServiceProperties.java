package com.kony.service.messaging;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Property Reader for the email-messaging-service.properties file
 *
 * @author Aditya Mankal
 */
public class EmailServiceProperties {

	public static final Properties CONFIG_PROPS = loadConfigProps();
	public static final String SMTP_HOST = getValue("mail.smtp.host");
	public static final String SMTP_USERNAME = getValue("mail.smtp.username");
	public static final String SMTP_PASSWORD = getValue("mail.smtp.password");
	public static final String SMTP_SENDERID = getValue("mail.smtp.senderid");
	public static final String SMTP_PORT = getValue("mail.smtp.port");
	public static final String SMTP_SOCKET_FACTORY_CLASS = getValue("mail.smtp.socketFactory.class");
	private static final Logger LOG = LogManager.getLogger(EmailServiceProperties.class);

	private static Properties loadConfigProps() {
		Properties properties = new Properties();
		try (InputStream inputStream = EmailServiceProperties.class.getClassLoader()
				.getResourceAsStream("email-messaging-service.properties");) {
			properties.load(inputStream);

			Enumeration<?> e = properties.propertyNames();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String value = properties.getProperty(key);
				properties.put(key.trim(), value.trim());//
			}
		} catch (Exception e) {
			LOG.error(e);
		}
		return properties;
	}

	public static String getValue(String key) {
		return CONFIG_PROPS.getProperty(key);
	}

}
