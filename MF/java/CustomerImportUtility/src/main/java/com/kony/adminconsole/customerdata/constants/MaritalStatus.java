/**
 * 
 */
package com.kony.adminconsole.customerdata.constants;

import java.util.ArrayList;

/**
 * @author Aditya Mankal
 * @version 1.0
 * 
 *          Enumeration of MaritalStatus values
 */
public enum MaritalStatus {
    SID_DIVORCED("Divorced"), SID_MARRIED("Married"), SID_SINGLE("Single"), SID_WIDOWED("Widowed");

    private String alias;

    private MaritalStatus(String alias) {
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
        for (MaritalStatus maritalStatus : MaritalStatus.values()) {
            if (maritalStatus.getAlias().equals(alias)) {
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
    public static ArrayList<String> getAllAliasValues() {
        ArrayList<String> valuesList = new ArrayList<String>();
        for (MaritalStatus maritalStatus : MaritalStatus.values()) {
            valuesList.add(maritalStatus.getAlias());
        }
        return valuesList;
    }
}
