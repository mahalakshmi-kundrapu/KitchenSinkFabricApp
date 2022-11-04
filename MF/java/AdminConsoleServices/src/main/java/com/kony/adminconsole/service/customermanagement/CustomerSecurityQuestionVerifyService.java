package com.kony.adminconsole.service.customermanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.crypto.BCrypt;
import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class CustomerSecurityQuestionVerifyService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CustomerSecurityQuestionVerifyService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        Param statusParam;

        String customerId = null;
        String userName = null;
        String securityQuestions = null;

        JSONArray inputSecurityQuestions = null;
        int securityQuestionsArrayLengthFromClient = 0;
        CustomerHandler customerHandler = new CustomerHandler();

        try {

            // Input Customer username
            userName = requestInstance.getParameter("userName");
            if (StringUtils.isBlank(userName)) {
                ErrorCodeEnum.ERR_20612.setErrorCode(result);
                return result;
            }

            // Input Security Questions
            securityQuestions = requestInstance.getParameter("securityQuestions");
            if (StringUtils.isBlank(securityQuestions)) {
                ErrorCodeEnum.ERR_20601.setErrorCode(result);
                return result;
            }
            inputSecurityQuestions = CommonUtilities.getStringAsJSONArray(securityQuestions);
            if (inputSecurityQuestions == null || inputSecurityQuestions.length() == 0) {
                ErrorCodeEnum.ERR_20601.setErrorCode(result);
                return result;
            }
            securityQuestionsArrayLengthFromClient = inputSecurityQuestions.length();

            // Resolve Customer-ID
            customerId = customerHandler.getCustomerId(userName, requestInstance);
            if (StringUtils.isBlank(customerId)) {
                ErrorCodeEnum.ERR_20613.setErrorCode(result);
                return result;
            }

            // Fetch configured Security Questions
            JSONObject customerSecurityQuestionResponseJSON = customerHandler.getSecurityQuestionsForGivenCustomer(
                    customerId, CommonUtilities.getAuthToken(requestInstance), requestInstance);

            // Verify if Security Questions have been fetched successfully
            if (customerSecurityQuestionResponseJSON == null
                    || !customerSecurityQuestionResponseJSON.has(FabricConstants.OPSTATUS)
                    || customerSecurityQuestionResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                    || !customerSecurityQuestionResponseJSON.has("customersecurityquestion_view")) {
                ErrorCodeEnum.ERR_20246.setErrorCode(result);
                return result;
            }
            JSONArray configuredSecurityQuestions = customerSecurityQuestionResponseJSON
                    .optJSONArray("customersecurityquestion_view");
            if (configuredSecurityQuestions == null || configuredSecurityQuestions.length() == 0) {
                ErrorCodeEnum.ERR_20602.setErrorCode(result);
                return result;
            }

            // Validate Input Answers and Configured Answers
            boolean isVerified = verifySecurityQuestionsForGivenCustomer(inputSecurityQuestions,
                    configuredSecurityQuestions);

            result.addParam(new Param("verifyStatus", isVerified + "", FabricConstants.STRING));
            result.addParam(new Param("numberOfQuestionsVerified", securityQuestionsArrayLengthFromClient + "",
                    FabricConstants.STRING));

        } catch (Exception e) {
            LOG.error("Exception occured in CustomerSecurityQuestionVerifyService invoke method ", e);
            ErrorCodeEnum.ERR_20247.setErrorCode(result);
            statusParam = new Param("Status", "Exception", FabricConstants.STRING);
            result.addParam(statusParam);
        }
        return result;
    }

    /**
     * Method to validate Customer Security Answers
     * 
     * @param inputSecurityQuestions
     * @param configuredSecurityQuestions
     * @return verificationResult
     */
    public boolean verifySecurityQuestionsForGivenCustomer(JSONArray inputSecurityQuestions,
            JSONArray configuredSecurityQuestions) {

        String questionId = null;
        String configuredAnswer = null;
        String inputAnswer = null;
        JSONObject currRecord;
        boolean status = false;
        Map<String, String> securityQuestionAnswerMap = new HashMap<String, String>();

        for (Object currObject : inputSecurityQuestions)
            if (currObject instanceof JSONObject) {
                currRecord = JSONObject.class.cast(currObject);
                securityQuestionAnswerMap.put(currRecord.optString("questionId"),
                        currRecord.optString("customerAnswer"));
            }

        for (Object currObject : configuredSecurityQuestions) {
            if (currObject instanceof JSONObject) {
                currRecord = JSONObject.class.cast(currObject);
                questionId = currRecord.optString("SecurityQuestion_id");
                configuredAnswer = currRecord.optString("CustomerAnswer");
                inputAnswer = securityQuestionAnswerMap.get(questionId);
                if (securityQuestionAnswerMap.containsKey(questionId)) {
                    if (BCrypt.checkpw(inputAnswer, configuredAnswer)) {
                        status = true;
                    } else {
                        // Return false for any occurrence of incorrect answer
                        status = false;
                        return status;
                    }
                }
            }
        }
        return status;
    }

}