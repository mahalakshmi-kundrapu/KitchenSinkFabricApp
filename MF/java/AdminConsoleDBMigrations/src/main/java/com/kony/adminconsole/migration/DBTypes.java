package com.kony.adminconsole.migration;

public enum DBTypes {

    MYSQL("jdbc:mysql://$HOSTNAME$:$PORT$?"), MSSQL("jdbc:sqlserver://$HOSTNAME$:$PORT$;databaseName=$DB_NAME$;"),
    ORACLE("jdbc:oracle:thin:@$HOSTNAME$:$PORT$") {
        @Override
        public String replaceInJDBCURL(String dbHost, int dbPort, String connectionPropsStr, String dbName) {
            throw new UnsupportedOperationException("Database Migration to '" + this.name() + "' is not yet supported");
        }
    };

    private String jdbcURL;

    private DBTypes(String jdbcURL) {
        this.jdbcURL = jdbcURL;
    }

    public String replaceInJDBCURL(String dbHost, int dbPort, String connectionPropsStr, String dbName) {
        String jdbcURL = this.jdbcURL.replace("$HOSTNAME$", dbHost).replace("$PORT$", String.valueOf(dbPort))
                .replace("$DB_NAME$", dbName);
        if (connectionPropsStr != null && !connectionPropsStr.isEmpty()) {
            jdbcURL = jdbcURL.concat(connectionPropsStr);
        }
        return jdbcURL;
    }

}