package com.kony.adminconsole.utilities;

import java.util.ArrayList;
import java.util.List;

public enum AccountTypeEnum {

    CHECKINGS("Checking"), SAVINGS("Savings"), CURRENT("Current");

    private String accountTypeAlias;

    private AccountTypeEnum(String accountTypeAlias) {
        this.accountTypeAlias = accountTypeAlias;
    }

    public String getAccountTypeAlias() {
        return this.accountTypeAlias;
    }

    public static List<String> getAllAccountTypeAliases() {
        List<String> aliases = new ArrayList<>();
        for (AccountTypeEnum name : AccountTypeEnum.values()) {
            aliases.add(name.getAccountTypeAlias());
        }
        return aliases;
    }
}
