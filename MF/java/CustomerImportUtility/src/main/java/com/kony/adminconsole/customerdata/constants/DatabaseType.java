/**
 * 
 */
package com.kony.adminconsole.customerdata.constants;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Aditya Mankal
 * @version 1.0
 *
 *          Enumeration holding the DatabaseType values
 */
public enum DatabaseType {

    MYSQL("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "START TRANSACTION;", "COMMIT;"),
    ORACLE("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "", "COMMIT;"),
    MSSQL("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "BEGIN TRANSACTION;", "COMMIT;");

    private String timestampFormat;
    private String dateFormat;
    private String startTransactionStatement;
    private String closeTransactionStatement;

    private DatabaseType(String timestampFormat, String dateFormat, String startTransactionStatement,
            String closeTransactionStatement) {
        this.timestampFormat = timestampFormat;
        this.dateFormat = dateFormat;
        this.startTransactionStatement = startTransactionStatement;
        this.closeTransactionStatement = closeTransactionStatement;
    }

    /**
     * Method to parse the timestamp value into Database compliant format
     *
     * @param epochTimeinMilliseconds
     * @return String
     */
    public String parseTimestamp(long timestampInMillis) {
        Date dateObject = new Date(timestampInMillis);
        SimpleDateFormat dateFormatter = new SimpleDateFormat(this.timestampFormat);
        return dateFormatter.format(dateObject);
    }

    /**
     * Method to parse the Date value into Database compliant format
     *
     * @param epochTimeinMilliseconds
     * @return String
     */
    public String parseDate(long timestampInMillis) {
        Date dateObject = new Date(timestampInMillis);
        SimpleDateFormat dateFormatter = new SimpleDateFormat(this.dateFormat);
        return dateFormatter.format(dateObject);
    }

    /**
     * Method to get the current timestamp
     * 
     * @return
     */
    public String getCurrentTimestamp() {
        return parseTimestamp(new Date().getTime());
    }

    public String getTimestampFormat() {
        return timestampFormat;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public String getStartTransactionStatement() {
        return startTransactionStatement;
    }

    public String getCloseTransactionStatement() {
        return closeTransactionStatement;
    }

}
