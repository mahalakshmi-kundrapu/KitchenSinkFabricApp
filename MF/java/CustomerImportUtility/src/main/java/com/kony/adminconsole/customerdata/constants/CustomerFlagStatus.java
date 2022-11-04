/**
 * 
 */
package com.kony.adminconsole.customerdata.constants;

import java.util.ArrayList;

/**
 * @author Aditya Mankal
 * @version 1.0
 * 
 *          Enumeration of CustomerFlagStatus values
 */
public enum CustomerFlagStatus {
    SID_DEFAULTER("Defaulter"), SID_FRAUDDETECTED("Fraud Detected"), SID_HIGHRISK("High Risk");
    private String alias;

    private CustomerFlagStatus(String alias) {
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
        for (CustomerFlagStatus customerFlagStatus : CustomerFlagStatus.values()) {
            if (customerFlagStatus.getAlias().equals(alias)) {
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
        for (CustomerFlagStatus customerFlagStatus : CustomerFlagStatus.values()) {
            valuesList.add(customerFlagStatus.getAlias());
        }
        return valuesList;
    }
}
