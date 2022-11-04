/**
 * 
 */
package com.kony.adminconsole.customerdata.constants;

import java.util.ArrayList;

/**
 * @author Aditya Mankal
 * @version 1.0
 * 
 *          Enumeration of EmployementStatus values
 */
public enum EmployementStatus {
    SID_EMPLOYED("Employed"), SID_RETIRED("Retired"), SID_STUDENT("Student"), SID_UNEMPLOYED("Unemployed");

    private String alias;

    private EmployementStatus(String alias) {
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
        for (EmployementStatus employementStatus : EmployementStatus.values()) {
            if (employementStatus.getAlias().equals(alias)) {
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
        for (EmployementStatus employementStatus : EmployementStatus.values()) {
            valuesList.add(employementStatus.getAlias());
        }
        return valuesList;
    }
}
