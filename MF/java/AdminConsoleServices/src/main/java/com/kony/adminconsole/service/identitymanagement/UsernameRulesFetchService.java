package com.kony.adminconsole.service.identitymanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
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
 * Service to fetch username rules from the back end
 * 
 * @author Mohit Khosla (KH2356)
 */

public class UsernameRulesFetchService implements JavaService2 {

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

        try {

            boolean isCustomer = true;

            String usernameRuleForCustomer = requestInstance.getParameter("usernameRuleForCustomer");
            if (usernameRuleForCustomer != null && usernameRuleForCustomer.equals("false")) {
                isCustomer = false;
            }

            // ** Reading from 'usernamerules' table **
            Map<String, String> usernameRulesTableMap = new HashMap<>();
            usernameRulesTableMap.put(ODataQueryConstants.FILTER, "IsCustomer eq " + (isCustomer ? 1 : 0));

            String readUsernameRulesResponse = Executor.invokeService(ServiceURLEnum.USERNAMERULES_READ,
                    usernameRulesTableMap, null, requestInstance);
            JSONObject readUsernameRulesResponseJSON = CommonUtilities.getStringAsJSONObject(readUsernameRulesResponse);

            if (readUsernameRulesResponseJSON != null && readUsernameRulesResponseJSON.has(FabricConstants.OPSTATUS)
                    && readUsernameRulesResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readUsernameRulesResponseJSON.has(USERNAME_RULES)) {

                JSONObject usernameRulesResponse = readUsernameRulesResponseJSON.getJSONArray(USERNAME_RULES)
                        .getJSONObject(0);

                Record usernameRulesRecord = new Record();
                usernameRulesRecord.setId(USERNAME_RULES);

                if (StringUtils.isNotBlank(usernameRulesResponse.optString(MIN_LENGTH))
                        && StringUtils.isNotBlank(usernameRulesResponse.optString(MAX_LENGTH))
                        && StringUtils.isNotBlank(usernameRulesResponse.optString(SYMBOLS_ALLOWED))
                        && StringUtils.isNotBlank(usernameRulesResponse.optString(SUPPORTED_SYMBOLS))) {

                    usernameRulesRecord.addParam(
                            new Param(MIN_LENGTH, usernameRulesResponse.getString(MIN_LENGTH), FabricConstants.INT));
                    usernameRulesRecord.addParam(
                            new Param(MAX_LENGTH, usernameRulesResponse.getString(MAX_LENGTH), FabricConstants.INT));
                    usernameRulesRecord.addParam(new Param(SYMBOLS_ALLOWED,
                            usernameRulesResponse.getString(SYMBOLS_ALLOWED), FabricConstants.BOOLEAN));
                    usernameRulesRecord.addParam(new Param(SUPPORTED_SYMBOLS,
                            usernameRulesResponse.getString(SUPPORTED_SYMBOLS), FabricConstants.STRING));
                } else {
                    throw new ApplicationException(ErrorCodeEnum.ERR_21051);
                }

                result.addRecord(usernameRulesRecord);
            } else {
                ErrorCodeEnum.ERR_21051.setErrorCode(result);
            }
        } catch (ApplicationException ae) {
            ae.getErrorCodeEnum().setErrorCode(result);
            LOG.error("ApplicationException occured in UsernameRulesFetchService. Error: ", ae);
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            LOG.error("Exception occured in UsernameRulesFetchService. Error: ", e);
        }

        return result;
    }
}
