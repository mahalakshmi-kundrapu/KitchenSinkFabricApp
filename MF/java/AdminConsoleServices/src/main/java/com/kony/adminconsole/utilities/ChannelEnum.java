package com.kony.adminconsole.utilities;

import java.util.ArrayList;
import java.util.List;

public enum ChannelEnum {

    MOBILE("Mobile"), EMAIL("Email");

    private String channelAlias;

    private ChannelEnum(String channelAlias) {
        this.channelAlias = channelAlias;
    }

    public String getChannelAlias() {
        return this.channelAlias;
    }

    public static List<String> getAllChannelAliases() {
        List<String> aliases = new ArrayList<>();
        for (ChannelEnum name : ChannelEnum.values()) {
            aliases.add(name.getChannelAlias());
        }
        return aliases;
    }
}
