package com.kony.adminconsole.preprocessor;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.kony.adminconsole.core.config.EnvironmentConfiguration;
import com.konylabs.middleware.common.DataPreProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

/**
 * Preprocessor to attach the Fabric account username and password to the Input Map by fetching it from Run Time
 * Configurations
 * 
 * @author Aditya Mankal
 *
 */
public class AuthKMSServicePreProcessor implements DataPreProcessor2 {

    private static final Logger LOG = Logger.getLogger(AuthKMSServicePreProcessor.class);

    private static final String USER_ID_KEY = "userid";
    private static final String PASSWORD_KEY = "password";

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean execute(HashMap inputMap, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance, Result serviceResult) throws Exception {
        try {
            inputMap.put(USER_ID_KEY, EnvironmentConfiguration.AC_FABRIC_LOGIN_USERNAME.getValue(requestInstance));
            inputMap.put(PASSWORD_KEY, EnvironmentConfiguration.AC_FABRIC_LOGIN_PASSWORD.getValue(requestInstance));
            LOG.debug("Fabric username and password succesfully set in the inputMap.");
            return true;
        } catch (Exception e) {
            LOG.error("Error occured while fetching Fabric username and Fabric password from Server Configurations");
            LOG.error(e);
            return false;
        }

    }

}
