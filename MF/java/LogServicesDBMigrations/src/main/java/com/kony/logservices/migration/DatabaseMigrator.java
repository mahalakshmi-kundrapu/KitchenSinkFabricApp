package com.kony.logservices.migration;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.flywaydb.core.Flyway;

/**
 * Main class performs the Admin Console database migrations
 * 
 * @author Venkateswara Rao Alla, Aditya Mankal
 *
 */

public class DatabaseMigrator {

    private static String PROP_USERNAME = "db.user";
    private static String PROP_PASSWORD = "db.password";
    private static String PROP_HOST = "db.host";
    private static String PROP_PORT = "db.port";
    private static String PROP_CONN_PROPS = "db.conn.props";
    private static String PROP_NAME = "db.name";
    private static String PROP_DB_TYPE = "db.type";

    public static void main(String[] args) {
        try {
            // redirecting the standard ouput and error to a result file
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss");
            PrintStream stdout = new PrintStream(
                    new File("migration_result_" + dateFormat.format(new Date()) + ".log"));
            System.setOut(stdout);
            System.setErr(stdout);

            // validate the provided database details
            String dbUser = System.getProperty(PROP_USERNAME);
            String dbPassword = System.getProperty(PROP_PASSWORD);
            String dbHost = System.getProperty(PROP_HOST);
            String dbPortProp = System.getProperty(PROP_PORT);
            Integer dbPort = null;
            String dbConnProps = System.getProperty(PROP_CONN_PROPS);
            String dbName = System.getProperty(PROP_NAME);
            String dbTypeProp = System.getProperty(PROP_DB_TYPE);
            DBTypes dbType = null;

            if (dbUser == null || (dbUser = dbUser.trim()).isEmpty()) {
                throw new RuntimeException("System property -D" + PROP_USERNAME + " is required");
            }

            if (dbPassword == null || (dbPassword = dbPassword.trim()).isEmpty()) {
                throw new RuntimeException("System property -D" + PROP_PASSWORD + " is required");
            }

            if (dbHost == null || (dbHost = dbHost.trim()).isEmpty()) {
                throw new RuntimeException("System property -D" + PROP_HOST + " is required");
            }

            if (dbPortProp == null || (dbPortProp = dbPortProp.trim()).isEmpty()) {
                throw new RuntimeException("System property -D" + PROP_PORT + " is required");
            }

            try {
                dbPort = Integer.parseInt(dbPortProp);
            } catch (NumberFormatException nfe) {
                throw new RuntimeException("System property -D" + PROP_PORT + " provided must be a number");
            }

            if (dbName == null || (dbName = dbName.trim()).isEmpty()) {
                throw new RuntimeException("System property -D" + PROP_NAME + " is required");
            }

            if (dbTypeProp == null || (dbTypeProp = dbTypeProp.trim()).isEmpty()) {
                System.out.println("System property -D" + PROP_DB_TYPE + " is not provided and defaults to \""
                        + DBTypes.MYSQL.name() + "\"");
                dbTypeProp = DBTypes.MYSQL.name();
            }

            try {
                dbType = DBTypes.valueOf(dbTypeProp.toUpperCase());
            } catch (IllegalArgumentException iae) {
                throw new RuntimeException("System property -D" + PROP_DB_TYPE
                        + " provided must be one of following supported databases: " + Arrays.asList(DBTypes.values()));
            }

            // Flyway work starts here
            migrate(dbType, dbUser, dbPassword, dbHost, dbPort, dbConnProps, dbName,
                    "classpath:dbscripts/" + dbType.name().toLowerCase());

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void migrate(DBTypes dbType, String dbUser, String dbPassword, String dbHost, int dbPort,
            String connectionPropsStr, String dbName, String dbScriptsLocation) throws Exception {
        Flyway flyway = new Flyway();
        flyway.setDataSource(dbType.replaceInJDBCURL(dbHost, dbPort, connectionPropsStr, dbName), dbUser, dbPassword);
        flyway.setSchemas(dbName);
        flyway.setBaselineOnMigrate(true);
        flyway.setBaselineDescription("Baseline to 4.0 version as this schema already exists");
        flyway.setBaselineVersionAsString("4.0");
        flyway.setLocations(dbScriptsLocation);
        flyway.migrate();
    }

}
