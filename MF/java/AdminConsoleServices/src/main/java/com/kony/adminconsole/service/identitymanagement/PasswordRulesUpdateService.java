package com.kony.adminconsole.service.identitymanagement;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

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

public class PasswordRulesUpdateService implements JavaService2 {

    public static final String PASSWORD_RULES = "passwordrules";
    public static final String MIN_LENGTH = "minLength";
    public static final String MAX_LENGTH = "maxLength";
    public static final String ATLEAST_ONE_LOWER_CASE = "atleastOneLowerCase";
    public static final String ATLEAST_ONE_UPPER_CASE = "atleastOneUpperCase";
    public static final String ATLEAST_ONE_NUMBER = "atleastOneNumber";
    public static final String ATLEAST_ONE_SYMBOL = "atleastOneSymbol";
    public static final String CHAR_REPEAT_COUNT = "charRepeatCount";
    public static final String BOOLEAN_TRUE = "true";
    public static final String BOOLEAN_FALSE = "false";
    public static final Pattern NUMBER_PATTERN = Pattern.compile("^[0-9-]*$");

    private static final Logger LOG = Logger.getLogger(PasswordRulesFetchService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        String userId = LoggedInUserHandler.getUserDetails(requestInstance).getUserId();

        try {

            Map<String, String> postParametersMap = new HashMap<>();
            postParametersMap.put("id", "PRULEID1");

            String minLength = requestInstance.getParameter(MIN_LENGTH);
            if (minLength != null && !minLength.equals("")) {
                if (!StringUtils.isNumeric(minLength)) {
                    ErrorCodeEnum.ERR_21055.setErrorCode(result);
                    return result;
                } else if (Integer.parseInt(minLength) < 8 || Integer.parseInt(minLength) > 64) {
                    ErrorCodeEnum.ERR_21056.setErrorCode(result);
                    return result;
                } else {
                    postParametersMap.put(MIN_LENGTH, minLength);
                }
            } else {
                ErrorCodeEnum.ERR_21061.setErrorCode(result);
                return result;
            }

            String maxLength = requestInstance.getParameter(MAX_LENGTH);
            if (maxLength != null && !maxLength.equals("")) {
                if (!StringUtils.isNumeric(maxLength)) {
                    ErrorCodeEnum.ERR_21057.setErrorCode(result);
                    return result;
                } else if (Integer.parseInt(maxLength) < 8 || Integer.parseInt(maxLength) > 64) {
                    ErrorCodeEnum.ERR_21058.setErrorCode(result);
                    return result;
                } else if (Integer.parseInt(minLength) > Integer.parseInt(maxLength)) {
                    ErrorCodeEnum.ERR_21060.setErrorCode(result);
                    return result;
                } else {
                    postParametersMap.put(MAX_LENGTH, maxLength);
                }
            } else {
                ErrorCodeEnum.ERR_21062.setErrorCode(result);
                return result;
            }

            String atleastOneLowerCase = requestInstance.getParameter(ATLEAST_ONE_LOWER_CASE);
            if (atleastOneLowerCase != null && !atleastOneLowerCase.equals("")) {
                if (!(atleastOneLowerCase.equals(BOOLEAN_TRUE) || atleastOneLowerCase.equals(BOOLEAN_FALSE))) {
                    ErrorCodeEnum.ERR_21075.setErrorCode(result);
                    return result;
                } else {
                    postParametersMap.put(ATLEAST_ONE_LOWER_CASE, atleastOneLowerCase);
                }
            } else {
                ErrorCodeEnum.ERR_21081.setErrorCode(result);
                return result;
            }

            String atleastOneUpperCase = requestInstance.getParameter(ATLEAST_ONE_UPPER_CASE);
            if (atleastOneUpperCase != null && !atleastOneUpperCase.equals("")) {
                if (!(atleastOneUpperCase.equals(BOOLEAN_TRUE) || atleastOneUpperCase.equals(BOOLEAN_FALSE))) {
                    ErrorCodeEnum.ERR_21076.setErrorCode(result);
                    return result;
                } else {
                    postParametersMap.put(ATLEAST_ONE_UPPER_CASE, atleastOneUpperCase);
                }
            } else {
                ErrorCodeEnum.ERR_21082.setErrorCode(result);
                return result;
            }

            String atleastOneNumber = requestInstance.getParameter(ATLEAST_ONE_NUMBER);
            if (atleastOneNumber != null && !atleastOneNumber.equals("")) {
                if (!(atleastOneNumber.equals(BOOLEAN_TRUE) || atleastOneNumber.equals(BOOLEAN_FALSE))) {
                    ErrorCodeEnum.ERR_21077.setErrorCode(result);
                    return result;
                } else {
                    postParametersMap.put(ATLEAST_ONE_NUMBER, atleastOneNumber);
                }
            } else {
                ErrorCodeEnum.ERR_21083.setErrorCode(result);
                return result;
            }

            String atleastOneSymbol = requestInstance.getParameter(ATLEAST_ONE_SYMBOL);
            if (atleastOneSymbol != null && !atleastOneSymbol.equals("")) {
                if (!(atleastOneSymbol.equals(BOOLEAN_TRUE) || atleastOneSymbol.equals(BOOLEAN_FALSE))) {
                    ErrorCodeEnum.ERR_21078.setErrorCode(result);
                    return result;
                } else {
                    postParametersMap.put(ATLEAST_ONE_SYMBOL, atleastOneSymbol);
                }
            } else {
                ErrorCodeEnum.ERR_21084.setErrorCode(result);
                return result;
            }

            String charRepeatCount = requestInstance.getParameter(CHAR_REPEAT_COUNT);
            if (charRepeatCount != null && !charRepeatCount.equals("")) {
                if (!NUMBER_PATTERN.matcher(charRepeatCount).matches()) {
                    ErrorCodeEnum.ERR_21079.setErrorCode(result);
                    return result;
                } else if (Integer.parseInt(charRepeatCount) < -1 || Integer.parseInt(charRepeatCount) > 9) {
                    ErrorCodeEnum.ERR_21080.setErrorCode(result);
                    return result;
                } else {
                    postParametersMap.put(CHAR_REPEAT_COUNT, charRepeatCount);
                }
            } else {
                ErrorCodeEnum.ERR_21085.setErrorCode(result);
                return result;
            }

            postParametersMap.put("modifiedby", userId);

            String updatePasswordRulesResponse = Executor.invokeService(ServiceURLEnum.PASSWORDRULES_UPDATE,
                    postParametersMap, null, requestInstance);

            JSONObject updatePasswordRulesResponseJSON = CommonUtilities
                    .getStringAsJSONObject(updatePasswordRulesResponse);
            if (updatePasswordRulesResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CREDENTIALPOLICIES, EventEnum.UPDATE,
                        ActivityStatusEnum.SUCCESSFUL, "Password rules update successful");
            } else {
                ErrorCodeEnum.ERR_21073.setErrorCode(result);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CREDENTIALPOLICIES, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED, "Password rules update failed");
            }
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            LOG.error("Exception occured in PasswordRulesUpdateService. Error: ", e);
        }

        return result;
    }
}
