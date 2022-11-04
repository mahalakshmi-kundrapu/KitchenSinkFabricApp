/**
 * 
 */
package com.kony.adminconsole.customerdata.constants;

import java.util.ArrayList;

/**
 * @author Aditya Mankal
 * @version 1.0
 * 
 *          Enumeration of CustomerStatus values
 */
public enum CustomerStatus {
    SID_CUS_ACTIVE("Customer Active"), SID_CUS_LOCKED("Customer Locked"), SID_CUS_SUSPENDED("Customer Suspended");

    private String alias;

    private CustomerStatus(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    /**
     * Method to validate if the provided alias matches any of the Enumeration Values
     * 
     * @param alias
     * @return boolean
     */
    public static boolean validateAlias(String alias) {
        for (CustomerStatus customerStatus : CustomerStatus.values()) {
            if (customerStatus.getAlias().equals(alias)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to get all the Alias Values as a list
     * 
     * @return ArrayList<String>
     */
    public static ArrayList<String> getAllValues() {
        ArrayList<String> valuesList = new ArrayList<String>();
        for (CustomerStatus customerStatus : CustomerStatus.values()) {
            valuesList.add(customerStatus.getAlias());
        }
        return valuesList;
    }

}
