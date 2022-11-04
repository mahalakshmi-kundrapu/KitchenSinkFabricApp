package com.kony.adminconsole.service.identitymanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.EventEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ModuleNameEnum;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to update password rules at the back end
 * 
 * @author Mohit Khosla (KH2356)
 */

public class PasswordLockoutSettingsUpdateService implements JavaService2 {

    public static final String PASSWORD_LOCKOUT_SETTINGS = "passwordlockoutsettings";
    public static final String PASSWORD_VALIDITY = "passwordValidity";
    public static final String PASSWORD_EXPIRY_WARNING_REQUIRED = "passwordExpiryWarningRequired";
    public static final String PASSWORD_EXPIRY_WARNING_THRESHOLD = "passwordExpiryWarningThreshold";
    public static final String PASSWORD_HISTORY_COUNT = "passwordHistoryCount";
    public static final String ACCOUNT_LOCKOUT_THRESHOLD = "accountLockoutThreshold";
    public static final String ACCOUNT_LOCKOUT_TIME = "accountLockoutTime";
    public static final String RECOVERY_EMAIL_LINK_VALIDITY = "recoveryEmailLinkValidity";

    private static final Logger LOG = Logger.getLogger(PasswordLockoutSettingsFetchService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        String userId = LoggedInUserHandler.getUserDetails(requestInstance).getUserId();

        try {

            Map<String, String> postParametersMap = new HashMap<>();
            postParametersMap.put("id", "PLOCKID1");

            String passwordValidity = requestInstance.getParameter(PASSWORD_VALIDITY);
            if (passwordValidity != null && !passwordValidity.equals("")) {
                if (!StringUtils.isNumeric(passwordValidity)) {
                    ErrorCodeEnum.ERR_21155.setErrorCode(result);
                    return result;
                } else if (Integer.parseInt(passwordValidity) < 1 || Integer.parseInt(passwordValidity) > 999) {
                    ErrorCodeEnum.ERR_21156.setErrorCode(result);
                    return result;
                } else {
                    postParametersMap.put(PASSWORD_VALIDITY, passwordValidity);
                }
            }

            String passwordExpiryWarningRequired = requestInstance.getParameter(PASSWORD_EXPIRY_WARNING_REQUIRED);
            if (passwordExpiryWarningRequired != null && !passwordExpiryWarningRequired.equals("")) {
                if (!(passwordExpiryWarningRequired.equals("true") || passwordExpiryWarningRequired.equals("false"))) {
                    ErrorCodeEnum.ERR_21157.setErrorCode(result);
                    return result;
                } else {
                    postParametersMap.put(PASSWORD_EXPIRY_WARNING_REQUIRED, passwordExpiryWarningRequired);
                }
            }

            if (passwordExpiryWarningRequired != null && passwordExpiryWarningRequired.equals("true")) {
                String passwordExpiryWarningThreshold = requestInstance.getParameter(PASSWORD_EXPIRY_WARNING_THRESHOLD);
                if (passwordExpiryWarningThreshold != null && !passwordExpiryWarningThreshold.equals("")) {
                    if (!StringUtils.isNumeric(passwordExpiryWarningThreshold)) {
                        ErrorCodeEnum.ERR_21158.setErrorCode(result);
                        return result;
                    } else if (Integer.parseInt(passwordExpiryWarningThreshold) < 1
                            || Integer.parseInt(passwordExpiryWarningThreshold) > 99) {
                        ErrorCodeEnum.ERR_21159.setErrorCode(result);
                        return result;
                    } else {
                        postParametersMap.put(PASSWORD_EXPIRY_WARNING_THRESHOLD, passwordExpiryWarningThreshold);
                    }
                }
            } else {
                postParametersMap.put(PASSWORD_EXPIRY_WARNING_THRESHOLD, "-1");
            }

            String passwordHistoryCount = requestInstance.getParameter(PASSWORD_HISTORY_COUNT);
            if (passwordHistoryCount != null && !passwordHistoryCount.equals("")) {
                if (!StringUtils.isNumeric(passwordHistoryCount)) {
                    ErrorCodeEnum.ERR_21160.setErrorCode(result);
                    return result;
                } else if (Integer.parseInt(passwordHistoryCount) < 1 || Integer.parseInt(passwordHistoryCount) > 9) {
                    ErrorCodeEnum.ERR_21161.setErrorCode(result);
                    return result;
                } else {
                    postParametersMap.put(PASSWORD_HISTORY_COUNT, passwordHistoryCount);
                }
            }

            String accountLockoutThreshold = requestInstance.getParameter(ACCOUNT_LOCKOUT_THRESHOLD);
            if (accountLockoutThreshold != null && !accountLockoutThreshold.equals("")) {
                if (!StringUtils.isNumeric(accountLockoutThreshold)) {
                    ErrorCodeEnum.ERR_21162.setErrorCode(result);
                    return result;
                } else if (Integer.parseInt(accountLockoutThreshold) < 1
                        || Integer.parseInt(accountLockoutThreshold) > 99) {
                    ErrorCodeEnum.ERR_21163.setErrorCode(result);
                    return result;
                } else {
                    postParametersMap.put(ACCOUNT_LOCKOUT_THRESHOLD, accountLockoutThreshold);
                }
            }

            String accountLockoutTime = requestInstance.getParameter(ACCOUNT_LOCKOUT_TIME);
            if (accountLockoutTime != null && !accountLockoutTime.equals("")) {
                if (!StringUtils.isNumeric(accountLockoutTime)) {
                    ErrorCodeEnum.ERR_21164.setErrorCode(result);
                    return result;
                } else if (Integer.parseInt(accountLockoutTime) < 1 || Integer.parseInt(accountLockoutTime) > 9999) {
                    ErrorCodeEnum.ERR_21165.setErrorCode(result);
                    return result;
                } else {
                    postParametersMap.put(ACCOUNT_LOCKOUT_TIME, accountLockoutTime);
                }
            }

            String recoveryEmailLinkValidity = requestInstance.getParameter(RECOVERY_EMAIL_LINK_VALIDITY);
            if (recoveryEmailLinkValidity != null && !recoveryEmailLinkValidity.equals("")) {
                if (!StringUtils.isNumeric(recoveryEmailLinkValidity)) {
                    ErrorCodeEnum.ERR_21166.setErrorCode(result);
                    return result;
                } else if (Integer.parseInt(recoveryEmailLinkValidity) < 1
                        || Integer.parseInt(recoveryEmailLinkValidity) > 9999) {
                    ErrorCodeEnum.ERR_21167.setErrorCode(result);
                    return result;
                } else {
                    postParametersMap.put(RECOVERY_EMAIL_LINK_VALIDITY, recoveryEmailLinkValidity);
                }
            }

            postParametersMap.put("modifiedby", userId);

            String updatePasswordLockoutSettingsResponse = Executor.invokeService(
                    ServiceURLEnum.PASSWORDLOCKOUTSETTINGS_UPDATE, postParametersMap, null, requestInstance);

            JSONObject updatePasswordLockoutSettingsResponseJSON = CommonUtilities
                    .getStringAsJSONObject(updatePasswordLockoutSettingsResponse);
            if (updatePasswordLockoutSettingsResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PASSWORDSETTINGS, EventEnum.UPDATE,
                        ActivityStatusEnum.SUCCESSFUL, "Password lockout settings update successful");
            } else {
                ErrorCodeEnum.ERR_21153.setErrorCode(result);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PASSWORDSETTINGS, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED, "Password lockout settings update failed");
            }
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            LOG.error("Exception occured in PasswordLockoutSettingsUpdateService. Error: ", e);
        }

        return result;
    }
}
