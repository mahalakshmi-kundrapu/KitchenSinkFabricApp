package com.kony.logservices.util;

import com.kony.adminconsole.commons.handler.EnvironmentConfigurationsHandler;
import com.konylabs.middleware.controller.DataControllerRequest;

public enum EnvironmentConfiguration {

    LOG_DATASOURCE_JDBC_URL, LOG_DATASOURCE_USERNAME, LOG_DATASOURCE_PASSWORD, LOG_DATASOURCE_MAX_POOL_SIZE,
    LOG_ARCHIVE_DATASOURCE_JDBC_URL, LOG_ARCHIVE_DATASOURCE_USERNAME, LOG_ARCHIVE_DATASOURCE_PASSWORD,
    LOG_ARCHIVE_DATASOURCE_MAX_POOL_SIZE, AC_LOG_SERVICES_API_ACCESS_TOKEN, LOG_RETENTION_PERIOD_IN_MONTHS;

    public String getValue(DataControllerRequest requestInstance) {
        return EnvironmentConfigurationsHandler.getValue(this.name(), requestInstance);
    }

}
