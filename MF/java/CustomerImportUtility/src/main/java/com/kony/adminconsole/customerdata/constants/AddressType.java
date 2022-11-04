/**
 * 
 */
package com.kony.adminconsole.customerdata.constants;

import java.util.ArrayList;

/**
 * @author Aditya Mankal
 * @version 1.0
 * 
 *          Enumeration of AddressType values
 */
public enum AddressType {
    ADR_TYPE_HOME("Home"), ADR_TYPE_OTHER("Other"), ADR_TYPE_WORK("Office");
    private String alias;

    private AddressType(String alias) {
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
        for (AddressType addressType : AddressType.values()) {
            if (addressType.getAlias().equals(alias)) {
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
        for (AddressType addresssType : AddressType.values()) {
            valuesList.add(addresssType.getAlias());
        }
        return valuesList;
    }

}
