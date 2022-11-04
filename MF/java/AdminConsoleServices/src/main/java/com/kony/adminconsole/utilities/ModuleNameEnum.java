package com.kony.adminconsole.utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Enum that holds the all the Modules for Admin Console.
 * 
 * 
 *
 */

public enum ModuleNameEnum {

    LOGIN("Login"), USERS("Internal Users Management"), ROLES("Role Management"), PERMISSIONS("Permissions Management"),
    FAQS("Frequently Asked Questions"), PRIVACYPOLICIES("Privacy Policies"), CREDENTIALPOLICIES("Credential Policies"),
    TANDC("Terms and Conditions"), OUTAGEMESSAGE("Service Outage Message"),
    LOCATIONS("Branch and ATM Location Management"), CUSTOMERCARE("Customer Care Information"),
    SERVICES("Service Management"), CUSTOMERS("Customer Management"), CUSTOMERROLES("Customer Roles"),
    ALERTS("Alert Management"), MESSAGES("Customer Requests and Messages"), PERIODICLIMITS("Periodic Limits"),
    /* SECURITYIMAGES("Security Images"), */
    SECURITYQUESTIONS("Security Questions"), CONFIGURATIONSFRAMEWORK("Configurations Framework"), COMPANY("Company"),
    BUSINESSCONFIGURATION("Business Configuration"), PASSWORDSETTINGS("Password Settings"),
    // DECISIONMANAGEMENT("Decision Management"),
    LEADMANAGEMENT("Lead Management"), MFACONFIGURATIONS("MFA Configurations"), MFASCENARIOS("MFA Scenarios"),
    CAMPAIGN("Campaign Management"), TRAVELNOTIFICATION("Travel Notification");

    private String moduleNameAlias;

    private ModuleNameEnum(String moduleNameAlias) {
        this.moduleNameAlias = moduleNameAlias;
    }

    public String getModuleNameAlias() {
        return this.moduleNameAlias;
    }

    public static List<String> getAllModuleAliases() {
        List<String> aliases = new ArrayList<>();
        for (ModuleNameEnum name : ModuleNameEnum.values()) {
            aliases.add(name.getModuleNameAlias());
        }
        return aliases;
    }
}
