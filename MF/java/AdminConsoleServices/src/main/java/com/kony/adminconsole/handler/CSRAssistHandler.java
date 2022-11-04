/**
 * 
 */
package com.kony.adminconsole.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.dataobject.Result;

/**
 * Handler to manage CSR Assist Requests
 * 
 * @author Aditya Mankal
 * 
 */
public class CSRAssistHandler {

    private static final String IS_CONSUMED_PARAM_DEFAULT_VALUE = "0";
    private static final String IS_CONSUMED_PARAM_FALSE_VALUE_NUMERIC = "0";
    private static final String IS_CONSUMED_PARAM_FALSE_VALUE_TEXT = "false";
    private static final long CSR_ASSIST_GRANT_TOKEN_VALIDITY_IN_MINS = 60;

    private String errorMessage = null;

    public String fetchLastGeneratedCSRAssitActiveGrantToken(String customerId, String loggedInCSRId,
            DataControllerRequest requestInstance) {
        String authorizationToken = null;
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "userId eq '" + loggedInCSRId + "' and customerId eq '"
                + customerId + "' and (isConsumed eq '" + IS_CONSUMED_PARAM_FALSE_VALUE_NUMERIC + "' or isConsumed eq '"
                + IS_CONSUMED_PARAM_FALSE_VALUE_TEXT + "') and (softdeleteflag eq '0' or softdeleteflag eq 'false')");
        postParametersMap.put(ODataQueryConstants.SELECT, "id,createdts");
        String readCSRAssistGrantResponse = Executor.invokeService(ServiceURLEnum.CSRASSISTGRANT_READ,
                postParametersMap, null, requestInstance);
        JSONObject readCSRAssistGrantResponseJSON = CommonUtilities.getStringAsJSONObject(readCSRAssistGrantResponse);
        if (readCSRAssistGrantResponseJSON != null && readCSRAssistGrantResponseJSON.has(FabricConstants.OPSTATUS)
                && readCSRAssistGrantResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readCSRAssistGrantResponseJSON.has("csrassistgrant")) {
            JSONArray csrAssistGrantRecords = readCSRAssistGrantResponseJSON.getJSONArray("csrassistgrant");
            if (csrAssistGrantRecords.length() > 0) {
                if (csrAssistGrantRecords.get(0) instanceof JSONObject) {
                    JSONObject currentRecordJSONObject = csrAssistGrantRecords.getJSONObject(0);
                    if (currentRecordJSONObject.has("id")) {
                        authorizationToken = currentRecordJSONObject.optString("id");
                        String createdts = currentRecordJSONObject.optString("createdts");
                        if (!hasCSRAssistGrantTokenExpired(createdts)) {
                            updateLastRetrievedTimeStamp(authorizationToken, loggedInCSRId, requestInstance);
                            return authorizationToken;
                        }

                    }
                }
            }
        }
        return null;
    }

    public String generateCSRAssistGrantToken(String customerId, String customerType,
            UserDetailsBean userDetailsBeanInstance, String appId, DataControllerRequest requestInstance) {

        // Generate Authorization Token and store at persistent storage level
        String authorizationToken = null;
        authorizationToken = fetchLastGeneratedCSRAssitActiveGrantToken(customerId, userDetailsBeanInstance.getUserId(),
                requestInstance);

        if (StringUtils.isBlank(authorizationToken)) {
            String currentTimestamp = CommonUtilities.getISOFormattedLocalTimestamp();
            authorizationToken = CommonUtilities.getNewId().toString();
            Map<String, String> postParametersMap = new HashMap<String, String>();
            postParametersMap.put("id", authorizationToken);
            postParametersMap.put("userId", userDetailsBeanInstance.getUserId());
            postParametersMap.put("userRoleId", userDetailsBeanInstance.getRoleName());
            postParametersMap.put("userName",
                    userDetailsBeanInstance.getFirstName() + " " + userDetailsBeanInstance.getLastName());
            postParametersMap.put("internalUserName", userDetailsBeanInstance.getUserName());
            postParametersMap.put("customerId", customerId);
            postParametersMap.put("CustomerType", customerType);
            postParametersMap.put("isConsumed", IS_CONSUMED_PARAM_DEFAULT_VALUE);
            postParametersMap.put("appId", appId);
            postParametersMap.put("createdby", userDetailsBeanInstance.getUserId());
            postParametersMap.put("modifiedby", userDetailsBeanInstance.getUserId());
            postParametersMap.put("lastretrievedts", currentTimestamp);
            postParametersMap.put("createdts", currentTimestamp);
            postParametersMap.put("lastmodifiedts", currentTimestamp);
            postParametersMap.put("synctimestamp", currentTimestamp);
            postParametersMap.put("softdeleteflag", "0");

            String insertGrantTokenResponse = Executor.invokeService(ServiceURLEnum.CSRASSISTGRANT_CREATE,
                    postParametersMap, null, requestInstance);

            JSONObject insertGrantTokenResponseJSON = CommonUtilities.getStringAsJSONObject(insertGrantTokenResponse);
            if (!(insertGrantTokenResponseJSON != null && insertGrantTokenResponseJSON.has(FabricConstants.OPSTATUS)
                    && insertGrantTokenResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && insertGrantTokenResponseJSON.has("csrassistgrant"))) {
                errorMessage = insertGrantTokenResponse;
                return null;
            }
        }

        return authorizationToken;
    }

    public boolean validateCSRAssistGrantToken(String csrAssistGrantToken, String appId,
            DataControllerRequest requestInstance, Result processedResult) {
        boolean validationResponse = true;

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER,
                "id eq '" + csrAssistGrantToken + "' and appId eq '" + appId + "'");
        String readCSRAssistGrantResponse = Executor.invokeService(ServiceURLEnum.CSRASSISTGRANT_READ,
                postParametersMap, null, requestInstance);
        JSONObject readCSRAssistGrantResponseJSON = CommonUtilities.getStringAsJSONObject(readCSRAssistGrantResponse);

        if (readCSRAssistGrantResponseJSON != null && readCSRAssistGrantResponseJSON.has(FabricConstants.OPSTATUS)
                && readCSRAssistGrantResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readCSRAssistGrantResponseJSON.has("csrassistgrant")) {
            JSONArray csrAssistGrantRecords = readCSRAssistGrantResponseJSON.getJSONArray("csrassistgrant");
            if (csrAssistGrantRecords.length() > 0) {
                String createdts = "", isConsumed = "", softdeleteflag = "";
                JSONObject csrAssistGrantRecord = csrAssistGrantRecords.getJSONObject(0);

                isConsumed = csrAssistGrantRecord.optString("isConsumed");
                createdts = csrAssistGrantRecord.optString("createdts");
                requestInstance.setAttribute("UserId", csrAssistGrantRecord.optString("userId"));
                requestInstance.setAttribute("adminRole", csrAssistGrantRecord.optString("userRoleId"));
                requestInstance.setAttribute("adminName", csrAssistGrantRecord.optString("userName"));
                requestInstance.setAttribute("adminUserName", csrAssistGrantRecord.optString("internalUserName"));
                requestInstance.setAttribute("CustomerId", csrAssistGrantRecord.optString("customerId"));
                requestInstance.setAttribute("CustomerType", csrAssistGrantRecord.optString("CustomerType"));
                softdeleteflag = csrAssistGrantRecord.optString("softdeleteflag");

                if (softdeleteflag.equalsIgnoreCase("true") || softdeleteflag.equalsIgnoreCase("1")) {
                    ErrorCodeEnum.ERR_20948.setErrorCode(processedResult);
                    validationResponse = false;
                }

                if (isConsumed.equalsIgnoreCase("true") || isConsumed.equalsIgnoreCase("1")) {
                    ErrorCodeEnum.ERR_20949.setErrorCode(processedResult);
                    validationResponse = false;
                }

                if (hasCSRAssistGrantTokenExpired(createdts)) {
                    ErrorCodeEnum.ERR_20950.setErrorCode(processedResult);
                    validationResponse = false;
                }

            } else {
                ErrorCodeEnum.ERR_20934.setErrorCode(processedResult);
                validationResponse = false;
            }

        } else {
            ErrorCodeEnum.ERR_20934.setErrorCode(processedResult);
            validationResponse = false;
        }

        return validationResponse;
    }

    public void updateTokenRetrievedAndConsumedTimeStamp(String csrAssistGrantToken,
            DataControllerRequest requestInstance, String authToken) {
        String currentTimestamp = CommonUtilities.getISOFormattedLocalTimestamp();
        Map<String, String> postParameterMap = new HashMap<String, String>();
        postParameterMap.put("id", csrAssistGrantToken);
        postParameterMap.put("isConsumed", "true");
        postParameterMap.put("lastretrievedts", currentTimestamp);
        postParameterMap.put("tokenconsumedts", currentTimestamp);
        postParameterMap.put("modifiedts", currentTimestamp);
        Executor.invokeService(ServiceURLEnum.CSRASSISTGRANT_UPDATE, postParameterMap, null, requestInstance);
    }

    public void updateTokenConsumedTimeStamp(String csrAssistGrantToken, String loggedInCSRId,
            DataControllerRequest requestInstance, String authToken) {
        String currentTimestamp = CommonUtilities.getISOFormattedLocalTimestamp();
        Map<String, String> postParameterMap = new HashMap<String, String>();
        postParameterMap.put("id", csrAssistGrantToken);
        postParameterMap.put("tokenconsumedts", currentTimestamp);
        postParameterMap.put("modifiedby", loggedInCSRId);
        postParameterMap.put("modifiedts", currentTimestamp);
        Executor.invokeService(ServiceURLEnum.CSRASSISTGRANT_UPDATE, postParameterMap, null, requestInstance);
    }

    public void updateLastRetrievedTimeStamp(String csrAssistGrantToken, String loggedInCSRId,
            DataControllerRequest requestInstance) {
        String currentTimestamp = CommonUtilities.getISOFormattedLocalTimestamp();
        Map<String, String> postParameterMap = new HashMap<String, String>();
        postParameterMap.put("id", csrAssistGrantToken);
        postParameterMap.put("lastretrievedts", currentTimestamp);
        postParameterMap.put("modifiedby", loggedInCSRId);
        postParameterMap.put("modifiedts", currentTimestamp);
        Executor.invokeService(ServiceURLEnum.CSRASSISTGRANT_UPDATE, postParameterMap, null, requestInstance);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    boolean hasCSRAssistGrantTokenExpired(String tokenCreatedTimestamp) {
        long timeDiffBetweenNowAndCSRAssistGrantTokenCreation_minutes = CommonUtilities
                .getTimeElapsedFromTimestampToNowInMinutes(tokenCreatedTimestamp, "yyyy-MM-dd HH:mm:ss");
        if (timeDiffBetweenNowAndCSRAssistGrantTokenCreation_minutes > CSR_ASSIST_GRANT_TOKEN_VALIDITY_IN_MINS) {
            return true;
        }
        return false;
    }
}
