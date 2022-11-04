package com.kony.adminconsole.loans.utils;

import org.apache.log4j.Logger;

import com.konylabs.middleware.common.URLProvider;
import com.konylabs.middleware.controller.DataControllerRequest;

/**
 * Common URL provider class to resolve the relevant URL as per the requested
 * service. Resolves the URL by fetching values from the Run Time Configurations
 * 
 * @author Manoj Dasari - KH2117
 *
 */
public class ServiceURLProvider implements URLProvider {

	private static final Logger LOG = Logger.getLogger(ServiceURLProvider.class);
	private static final String KEY_ENDS_MARKER = "_$_";

	@Override
	public String execute(String operationURL, DataControllerRequest requestInstance) {

		try {
			String targetURL = null;
			String propertyKey = operationURL.substring(
					operationURL.indexOf(KEY_ENDS_MARKER) + KEY_ENDS_MARKER.length(),
					operationURL.lastIndexOf(KEY_ENDS_MARKER));
			String baseURL = operationURL.substring(0,
					operationURL.lastIndexOf(KEY_ENDS_MARKER) + KEY_ENDS_MARKER.length());
			targetURL = operationURL.replace(baseURL,
					EnvironmentConfigurationsHandler.getValue(propertyKey, requestInstance));
			LOG.debug("URL resolved from server configurations :" + targetURL);
			return targetURL;
		} catch (Exception e) {
			LOG.error("Error occured while resolving operation URL. Attempted Operation URL:" + operationURL);
			LOG.error(e);
		}
		return null;
	}

}
