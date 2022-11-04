package com.kony.adminconsole.utilities;

import java.util.ArrayList;
import java.util.List;

public enum ActivityStatusEnum {

    POSTED("Posted"), PENDING("Pending"), FAILED("Failed"), SUCCESSFUL("Successful");

    private String statusAlias;

    private ActivityStatusEnum(String statusAlias) {
        this.statusAlias = statusAlias;
    }

    public String getStatusAlias() {
        return this.statusAlias;
    }

    public static List<String> getTransactionStatusAliases() {

        List<String> aliases = new ArrayList<>();
        aliases.add(ActivityStatusEnum.POSTED.getStatusAlias());
        aliases.add(ActivityStatusEnum.PENDING.getStatusAlias());
        aliases.add(ActivityStatusEnum.FAILED.getStatusAlias());
        return aliases;
    }

    public static List<String> getAdminStatusAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add(ActivityStatusEnum.SUCCESSFUL.getStatusAlias());
        aliases.add(ActivityStatusEnum.FAILED.getStatusAlias());
        return aliases;
    }

}
