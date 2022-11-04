package com.kony.adminconsole.loans.utils;

import org.apache.log4j.Logger;

import com.konylabs.middleware.api.ConfigurableParametersHelper;
import com.konylabs.middleware.api.ServicesManager;
import com.konylabs.middleware.api.processor.manager.FabricRequestManager;
import com.konylabs.middleware.controller.DataControllerRequest;

/**
 * Handler Class to fetch the Environment Configuration Values
 * 
 * @author Manoj Dasari - KH2117
 *
 */
public class EnvironmentConfigurationsHandler {

	private static final Logger LOG = Logger.getLogger(EnvironmentConfigurationsHandler.class);

	public static String getValue(String key, DataControllerRequest requestInstance) {
		try {
			ServicesManager serviceManager = requestInstance.getServicesManager();
			ConfigurableParametersHelper configurableParametersHelper = serviceManager
					.getConfigurableParametersHelper();
			String requiredURL = configurableParametersHelper.getServerProperty(key);
			return requiredURL;
		} catch (Exception e) {
			LOG.error("Error occured while fetching environment configuration parameter. Attempted Key:" + key, e);
		}
		return null;
	}
	
	public static String getValue(String key, FabricRequestManager fabricRequestManager) {
		try {
			ConfigurableParametersHelper configurableParametersHelper = fabricRequestManager.getServicesManager().getConfigurableParametersHelper();
			String requiredURL = configurableParametersHelper.getServerProperty(key);
			if(requiredURL==null) {
				return "null";
			}
			return requiredURL;
		} catch (Exception e) {
			LOG.error("Error occured while fetching environment configuration parameter. Attempted Key:" + key, e);
		}
		return null;
	}
}
