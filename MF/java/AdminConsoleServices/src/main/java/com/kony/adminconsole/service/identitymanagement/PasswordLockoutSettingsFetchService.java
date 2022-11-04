package com.kony.adminconsole.service.identitymanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to fetch password lockout settings from the back end
 * 
 * @author Mohit Khosla
 */

public class PasswordLockoutSettingsFetchService implements JavaService2 {

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

        try {

            // ** Reading from 'passwordlockoutsettings' table **
            Map<String, String> passwordRulesTableMap = new HashMap<>();

            String readPasswordRulesResponse = Executor.invokeService(ServiceURLEnum.PASSWORDLOCKOUTSETTINGS_READ,
                    passwordRulesTableMap, null, requestInstance);
            JSONObject readPasswordRulesResponseJSON = CommonUtilities.getStringAsJSONObject(readPasswordRulesResponse);

            if (readPasswordRulesResponseJSON != null && readPasswordRulesResponseJSON.has(FabricConstants.OPSTATUS)
                    && readPasswordRulesResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readPasswordRulesResponseJSON.has(PASSWORD_LOCKOUT_SETTINGS)) {

                JSONObject passwordRulesResponse = readPasswordRulesResponseJSON.getJSONArray(PASSWORD_LOCKOUT_SETTINGS)
                        .getJSONObject(0);

                Record passwordRulesRecord = new Record();
                passwordRulesRecord.setId(PASSWORD_LOCKOUT_SETTINGS);

                if (StringUtils.isNotBlank(passwordRulesResponse.optString(PASSWORD_VALIDITY))
                        && StringUtils.isNotBlank(passwordRulesResponse.optString(PASSWORD_EXPIRY_WARNING_REQUIRED))
                        && StringUtils.isNotBlank(passwordRulesResponse.optString(PASSWORD_EXPIRY_WARNING_THRESHOLD))
                        && StringUtils.isNotBlank(passwordRulesResponse.optString(PASSWORD_HISTORY_COUNT))
                        && StringUtils.isNotBlank(passwordRulesResponse.optString(ACCOUNT_LOCKOUT_THRESHOLD))
                        && StringUtils.isNotBlank(passwordRulesResponse.optString(ACCOUNT_LOCKOUT_TIME))
                        && StringUtils.isNotBlank(passwordRulesResponse.optString(RECOVERY_EMAIL_LINK_VALIDITY))) {

                    passwordRulesRecord.addParam(new Param(PASSWORD_VALIDITY,
                            passwordRulesResponse.getString(PASSWORD_VALIDITY), FabricConstants.INT));
                    passwordRulesRecord.addParam(new Param(PASSWORD_EXPIRY_WARNING_REQUIRED,
                            passwordRulesResponse.getString(PASSWORD_EXPIRY_WARNING_REQUIRED),
                            FabricConstants.BOOLEAN));
                    passwordRulesRecord.addParam(new Param(PASSWORD_EXPIRY_WARNING_THRESHOLD,
                            passwordRulesResponse.getString(PASSWORD_EXPIRY_WARNING_THRESHOLD), FabricConstants.INT));
                    passwordRulesRecord.addParam(new Param(PASSWORD_HISTORY_COUNT,
                            passwordRulesResponse.getString(PASSWORD_HISTORY_COUNT), FabricConstants.INT));
                    passwordRulesRecord.addParam(new Param(ACCOUNT_LOCKOUT_THRESHOLD,
                            passwordRulesResponse.getString(ACCOUNT_LOCKOUT_THRESHOLD), FabricConstants.INT));
                    passwordRulesRecord.addParam(new Param(ACCOUNT_LOCKOUT_TIME,
                            passwordRulesResponse.getString(ACCOUNT_LOCKOUT_TIME), FabricConstants.INT));
                    passwordRulesRecord.addParam(new Param(RECOVERY_EMAIL_LINK_VALIDITY,
                            passwordRulesResponse.getString(RECOVERY_EMAIL_LINK_VALIDITY), FabricConstants.INT));
                } else {
                    throw new ApplicationException(ErrorCodeEnum.ERR_21151);
                }

                result.addRecord(passwordRulesRecord);
            } else {
                ErrorCodeEnum.ERR_21151.setErrorCode(result);
            }
        } catch (ApplicationException ae) {
            ae.getErrorCodeEnum().setErrorCode(result);
            LOG.error("ApplicationException occured in PasswordLockoutSettingsFetchService. Error: ", ae);
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            LOG.error("Exception occured in PasswordLockoutSettingsFetchService. Error: ", e);
        }

        return result;
    }
}
