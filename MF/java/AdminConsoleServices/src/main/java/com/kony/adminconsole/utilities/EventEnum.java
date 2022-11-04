package com.kony.adminconsole.utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Enum that holds the all events done by an admin.
 * 
 * 
 *
 */

public enum EventEnum {

    CREATE("Create"), UPDATE("Update"), DELETE("Delete"), LOGIN("Login"), SEARCH("Search"),
    DOWNLOADFILE("Download File"), COMMUNICATION("Communication");

    private String eventNameAlias;

    private EventEnum(String eventNameAlias) {
        this.eventNameAlias = eventNameAlias;
    }

    public String getEventNameAlias() {
        return this.eventNameAlias;
    }

    public static List<String> getAllEventAliases() {
        List<String> aliases = new ArrayList<>();
        for (EventEnum name : EventEnum.values()) {
            aliases.add(name.getEventNameAlias());
        }
        return aliases;
    }

    public static List<String> getCustomerEventAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add(EventEnum.CREATE.getEventNameAlias());
        aliases.add(EventEnum.UPDATE.getEventNameAlias());
        aliases.add(EventEnum.DELETE.getEventNameAlias());
        aliases.add(EventEnum.COMMUNICATION.getEventNameAlias());
        return aliases;
    }

}
