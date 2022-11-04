package com.kony.adminconsole.utilities;

import java.util.ArrayList;
import java.util.List;

public enum WireTransferTypesEnum {

    DOMESTIC("Domestic"), INTERNATIONAL("International");

    private String typeAlias;

    private WireTransferTypesEnum(String typeAlias) {
        this.typeAlias = typeAlias;
    }

    public String getTypeAlias() {
        return this.typeAlias;
    }

    public static List<String> getAllTypeAliases() {
        List<String> aliases = new ArrayList<>();
        for (WireTransferTypesEnum name : WireTransferTypesEnum.values()) {
            aliases.add(name.getTypeAlias());
        }
        return aliases;
    }
}
