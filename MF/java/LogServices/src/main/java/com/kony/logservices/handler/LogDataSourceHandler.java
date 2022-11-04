package com.kony.logservices.handler;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.sql2o.Sql2o;

import com.kony.logservices.util.EnvironmentConfiguration;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Datasource handler that maintains the log databases
 * 
 * @author Venkateswara Rao Alla
 *
 */

public class LogDataSourceHandler {

    private static final Logger LOG = Logger.getLogger(LogDataSourceHandler.class);

    private static HikariDataSource LOG_DATASOURCE;
    private static HikariDataSource LOG_ARCHIVE_DATASOURCE;
    private static Sql2o LOG_SQL2O;
    private static Sql2o LOG_ARCHIVE_SQL2O;
    private static final InheritableThreadLocal<DataControllerRequest> THREAD_LOCAL_DCR =
            new InheritableThreadLocal<>(); // used
                                            // to
                                            // hold
                                            // DCR
                                            // in
                                            // this
                                            // current
                                            // thread
                                            // context

    private static synchronized void createLogDataSource() throws Exception {
        if (LOG_DATASOURCE == null || LOG_DATASOURCE.isClosed()) {
            HikariConfig config = new HikariConfig(getLogDatasourceProps());
            LOG_DATASOURCE = new HikariDataSource(config);
            LOG_SQL2O = new Sql2o(LOG_DATASOURCE);
        }
    }

    private static synchronized void createLogArchiveDataSource() throws Exception {
        if (LOG_ARCHIVE_DATASOURCE == null || LOG_ARCHIVE_DATASOURCE.isClosed()) {
            HikariConfig config = new HikariConfig(getLogArchiveDatasourceProps());
            LOG_ARCHIVE_DATASOURCE = new HikariDataSource(config);
            LOG_ARCHIVE_SQL2O = new Sql2o(LOG_ARCHIVE_DATASOURCE);
        }
    }

    public static Sql2o getLogSql2oInstance() {
        if (LOG_SQL2O == null || LOG_DATASOURCE == null || LOG_DATASOURCE.isClosed()) {
            try {
                createLogDataSource();
            } catch (Exception e) {
                LOG.error("Failed creating DBP log datasource", e);
                closeLogDataSource(); // closing DBP log datasource if created
            }
        }
        return LOG_SQL2O;
    }

    public static Sql2o getLogArchiveSql2oInstance() {
        if (LOG_ARCHIVE_SQL2O == null || LOG_ARCHIVE_DATASOURCE == null || LOG_ARCHIVE_DATASOURCE.isClosed()) {
            try {
                createLogArchiveDataSource();
            } catch (Exception e) {
                LOG.error("Failed creating DBP archive log datasource", e);
                closeLogArchiveDataSource(); // closing DBP archive log datasource if created
            }
        }
        return LOG_ARCHIVE_SQL2O;
    }

    public static synchronized void closeLogDataSource() {
        try {
            if (LOG_DATASOURCE != null) {
                LOG.error("[Informational] Releasing DBP log database resources");
                LOG_DATASOURCE.close();
            }
        } catch (Exception e) {
            LOG.error("Failed to release resources of DBP log datasource", e);
        }
    }

    public static synchronized void closeLogArchiveDataSource() {
        try {
            if (LOG_ARCHIVE_DATASOURCE != null) {
                LOG.error("[Informational] Releasing DBP archive log database resources");
                LOG_ARCHIVE_DATASOURCE.close();
            }
        } catch (Exception e) {
            LOG.error("Failed to release resources of DBP archive log datasource", e);
        }
    }

    private static Properties getLogDatasourceProps() {

        DataControllerRequest requestInstance = getRequest();

        Properties properties = new Properties();

        properties.put("jdbcUrl", EnvironmentConfiguration.LOG_DATASOURCE_JDBC_URL.getValue(requestInstance));
        properties.put("username", EnvironmentConfiguration.LOG_DATASOURCE_USERNAME.getValue(requestInstance));
        properties.put("password", EnvironmentConfiguration.LOG_DATASOURCE_PASSWORD.getValue(requestInstance));
        properties.put("maximumPoolSize",
                EnvironmentConfiguration.LOG_DATASOURCE_MAX_POOL_SIZE.getValue(requestInstance));

        return properties;
    }

    private static Properties getLogArchiveDatasourceProps() {

        DataControllerRequest requestInstance = getRequest();

        Properties properties = new Properties();

        properties.put("jdbcUrl", EnvironmentConfiguration.LOG_ARCHIVE_DATASOURCE_JDBC_URL.getValue(requestInstance));
        properties.put("username", EnvironmentConfiguration.LOG_ARCHIVE_DATASOURCE_USERNAME.getValue(requestInstance));
        properties.put("password", EnvironmentConfiguration.LOG_ARCHIVE_DATASOURCE_PASSWORD.getValue(requestInstance));
        properties.put("maximumPoolSize",
                EnvironmentConfiguration.LOG_ARCHIVE_DATASOURCE_MAX_POOL_SIZE.getValue(requestInstance));

        return properties;
    }

    public static DataControllerRequest getRequest() {
        return THREAD_LOCAL_DCR.get();
    }

    public static void setRequest(DataControllerRequest dataControllerRequest) {
        THREAD_LOCAL_DCR.set(dataControllerRequest);
    }

    public static void removeRequest() {
        THREAD_LOCAL_DCR.remove();
    }

}
