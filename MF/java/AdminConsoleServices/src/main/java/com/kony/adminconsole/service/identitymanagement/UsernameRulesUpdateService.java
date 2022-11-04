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
 * Service to update username rules at the back end
 * 
 * @author Mohit Khosla (KH2356)
 */

public class UsernameRulesUpdateService implements JavaService2 {

    public static final String USERNAME_RULES = "usernamerules";
    public static final String MIN_LENGTH = "minLength";
    public static final String MAX_LENGTH = "maxLength";
    public static final String SYMBOLS_ALLOWED = "symbolsAllowed";
    public static final String SUPPORTED_SYMBOLS = "supportedSymbols";

    private static final Logger LOG = Logger.getLogger(UsernameRulesFetchService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        String userId = LoggedInUserHandler.getUserDetails(requestInstance).getUserId();

        try {

            Map<String, String> postParametersMap = new HashMap<>();
            postParametersMap.put("id", "URULEID1");

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

            String symbolsAllowed = requestInstance.getParameter(SYMBOLS_ALLOWED);
            if (symbolsAllowed != null && !symbolsAllowed.equals("")) {
                if (!(symbolsAllowed.equals("true") || symbolsAllowed.equals("false"))) {
                    ErrorCodeEnum.ERR_21059.setErrorCode(result);
                    return result;
                } else {
                    postParametersMap.put(SYMBOLS_ALLOWED, symbolsAllowed);
                }
            } else {
                ErrorCodeEnum.ERR_21063.setErrorCode(result);
                return result;
            }

            postParametersMap.put("modifiedby", userId);

            String updateUsernameRulesResponse = Executor.invokeService(ServiceURLEnum.USERNAMERULES_UPDATE,
                    postParametersMap, null, requestInstance);

            JSONObject updateUsernameRulesResponseJSON = CommonUtilities
                    .getStringAsJSONObject(updateUsernameRulesResponse);
            if (updateUsernameRulesResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CREDENTIALPOLICIES, EventEnum.UPDATE,
                        ActivityStatusEnum.SUCCESSFUL, "Username rules update successful");
            } else {
                ErrorCodeEnum.ERR_21053.setErrorCode(result);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CREDENTIALPOLICIES, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED, "Username rules update failed");
            }
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            LOG.error("Exception occured in UsernameRulesUpdateService. Error: ", e);
        }

        return result;
    }
}
