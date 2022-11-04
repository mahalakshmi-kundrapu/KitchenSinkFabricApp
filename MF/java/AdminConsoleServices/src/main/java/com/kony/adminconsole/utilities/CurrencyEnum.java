package com.kony.adminconsole.utilities;

import java.util.ArrayList;
import java.util.List;

public enum CurrencyEnum {

    USD("USD"), EUR("EUR"), AUD("AUD");

    private String currencyAlias;

    private CurrencyEnum(String currencyAlias) {
        this.currencyAlias = currencyAlias;
    }

    public String getCurrencyAlias() {
        return this.currencyAlias;
    }

    public static List<String> getAllCurrencyAliases() {
        List<String> aliases = new ArrayList<>();
        for (CurrencyEnum name : CurrencyEnum.values()) {
            aliases.add(name.getCurrencyAlias());
        }
        return aliases;
    }

}
