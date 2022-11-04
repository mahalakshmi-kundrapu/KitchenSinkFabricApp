package com.kony.adminconsole.commons.handler;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.konylabs.middleware.api.ConfigurableParametersHelper;
import com.konylabs.middleware.api.ServicesManager;
import com.konylabs.middleware.api.ServicesManagerHelper;
import com.konylabs.middleware.controller.DataControllerRequest;

/**
 * Handler Class to fetch the Environment Configuration Values
 * 
 * @author Aditya Mankal
 *
 */
public class EnvironmentConfigurationsHandler {

    private static final Logger LOG = Logger.getLogger(EnvironmentConfigurationsHandler.class);

    /**
     * Returns the value associated with the provided server property name
     * 
     * @param propertyName
     * @param requestInstance
     * @return associated value or null if either no value exists or on any exception
     */
    public static String getValue(String propertyName, DataControllerRequest requestInstance) {
        try {
            ServicesManager servicesManager = requestInstance != null ? requestInstance.getServicesManager()
                    : ServicesManagerHelper.getServicesManager((HttpServletRequest) null);
            return getValue(propertyName, servicesManager);
        } catch (Exception e) {
            LOG.error("Error occured while accessing Services Manager", e);
        }
        return null;
    }

    /**
     * Returns the value associated with the provided server property name
     * 
     * @param propertyName
     * @param requestInstance
     * @return associated value or null if either no value exists or on any exception
     */
    public static String getValue(String propertyName, HttpServletRequest requestInstance) {
        try {
            return getValue(propertyName, ServicesManagerHelper.getServicesManager(requestInstance));
        } catch (Exception e) {
            LOG.error("Error occured while accessing Services Manager", e);
        }
        return null;
    }

    /**
     * Returns the value associated with the provided server property name
     * 
     * @param propertyName
     * @param servicesManager
     * @return
     */
    public static String getValue(String propertyName, ServicesManager servicesManager) {
        try {
            ConfigurableParametersHelper configurableParametersHelper = servicesManager
                    .getConfigurableParametersHelper();
            String value = configurableParametersHelper.getServerProperty(propertyName);
            return value;
        } catch (Exception e) {
            LOG.error("Error occured while fetching environment configuration parameter. Attempted server property:"
                    + propertyName, e);
        }
        return null;
    }

    /**
     * Returns the value associated with the provided client app property name
     * 
     * @param propertyName
     * @param requestInstance
     * @return associated value or null if either no value exists or on any exception
     */
    public static String getClientAppPropertyValue(String propertyName, DataControllerRequest requestInstance) {
        try {
            return getClientAppPropertyValue(propertyName, requestInstance.getServicesManager());
        } catch (Exception e) {
            LOG.error("Error occured while accessing Services Manager", e);
        }
        return null;
    }

    /**
     * Returns the value associated with the provided client app property name
     * 
     * @param propertyName
     * @param requestInstance
     * @return associated value or null if either no value exists or on any exception
     */
    public static String getClientAppPropertyValue(String propertyName, HttpServletRequest requestInstance) {
        try {
            return getClientAppPropertyValue(propertyName, ServicesManagerHelper.getServicesManager(requestInstance));
        } catch (Exception e) {
            LOG.error("Error occured while accessing Services Manager", e);
        }
        return null;
    }

    /**
     * Returns the value associated with the provided client app property name
     * 
     * @param propertyName
     * @param servicesManager
     * @return
     */
    public static String getClientAppPropertyValue(String propertyName, ServicesManager servicesManager) {
        try {
            ConfigurableParametersHelper configurableParametersHelper = servicesManager
                    .getConfigurableParametersHelper();
            String value = configurableParametersHelper.getClientAppProperty(propertyName);
            return value;
        } catch (Exception e) {
            LOG.error("Error occured while fetching environment configuration parameter. Attempted client app property:"
                    + propertyName, e);
        }
        return null;
    }
}
