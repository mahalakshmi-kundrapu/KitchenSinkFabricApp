/**
 * 
 */
package com.kony.adminconsole.utilities;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Aditya Mankal
 * 
 * 
 *         Enum holding the possible values of Status
 *
 */
public enum StatusEnum {

    SID_NEW, SID_ACTIVE, SID_INACTIVE, SID_ARCHIVED, SID_CANCELLED, SID_SUSPENDED, SID_CUS_ACTIVE, SID_CUS_LOCKED,
    SID_CUS_SUSPENDED, SID_DELETED, SID_OPEN, SID_INPROGRESS, SID_RESOLVED, SID_APP_FAILED, SID_SUBSCRIBED,
    SID_UNSUBSCRIBED, SID_EVENT_SUCCESS, SID_EVENT_FAILURE, SID_TANDC_DRAFT, SID_TANDC_ARCHIVED, SID_TANDC_ACTIVE,
    SID_FEATURE_ACTIVE, SID_FEATURE_INACTIVE;

    public static List<String> getStatusAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add(ActivityStatusEnum.POSTED.getStatusAlias());
        aliases.add(ActivityStatusEnum.PENDING.getStatusAlias());
        aliases.add(ActivityStatusEnum.FAILED.getStatusAlias());
        return aliases;
    }

    public static boolean isValidStatusCode(String statusCode) {

        StatusEnum values[] = StatusEnum.values();
        for (StatusEnum statusEnum : values) {
            if (StringUtils.equals(statusCode, statusEnum.name())) {
                return true;
            }
        }
        return false;
    }

}
