package com.kony.adminconsole.core.config;

import java.io.Serializable;

import com.kony.adminconsole.commons.handler.EnvironmentConfigurationsHandler;
import com.konylabs.middleware.controller.DataControllerRequest;

/**
 * Class used to fetch the Run Time Configuration Parameter values.
 * 
 * @author Aditya Mankal
 *
 */
public enum EnvironmentConfiguration implements Serializable {

    AC_HOST_URL, AC_DBP_SERVICES_URL, AC_LOG_SERVICES_URL, AC_DBP_AUTH_URL, AC_FABRIC_LOGIN_USERNAME,
    AC_FABRIC_LOGIN_PASSWORD, AC_DBP_APP_KEY, AC_DBP_APP_SECRET, AC_DBP_SHARED_SECRET, AC_EMAIL_TEMPLATE_LOGO_URL,
    AC_CSR_ASSIST_OLB_HOST_URL, AC_CSR_ASSIST_CL_HOST_URL, AC_KMS_URL, AC_ENCRYPTION_KEY, AC_INTERNAL_API_ACCESS_TOKEN,
    AC_LOG_SERVICES_API_ACCESS_TOKEN, DBX_SCHEMA_NAME, AC_OKTA_AUTHORIZATION_KEY, AC_SERVICE_INVOKE_METHOD,
    AC_APPID_TO_APP_MAPPING, AC_NUMVERIFY_API_KEY;

    public String getValue(DataControllerRequest requestInstance) {
        return EnvironmentConfigurationsHandler.getValue(this.name(), requestInstance);
    }

}
