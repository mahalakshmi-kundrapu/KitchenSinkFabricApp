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
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class CustomerSecurityQuestionCreate implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(CustomerSecurityQuestionRead.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
        Param errorCodeParam;
        Param errorMessageParam;
        Param statusParam;

        String systemUser = "";
        String customerId = "";
        String userName = "";
        JSONArray securityQuestionsArray = null;
        CustomerHandler customerHandler = new CustomerHandler();

        try {

            if (requestInstance.getParameter("userName") != null) {
                userName = requestInstance.getParameter("userName");
                if (StringUtils.isBlank(userName)) {
                    ErrorCodeEnum.ERR_20612.setErrorCode(result);
                    result.addParam(new Param("status", "failure", FabricConstants.STRING));
                    return result;

                }
            } else {
                ErrorCodeEnum.ERR_20612.setErrorCode(result);
                result.addParam(new Param("status", "failure", FabricConstants.STRING));
                return result;
            }
            customerId = customerHandler.getCustomerId(userName, requestInstance);
            if (StringUtils.isBlank(customerId)) {
                ErrorCodeEnum.ERR_20613.setErrorCode(result);
                result.addParam(new Param("status", "failure", FabricConstants.STRING));
                return result;

            }

            if (requestInstance.getParameter("securityQuestions") != null) {
                securityQuestionsArray = new JSONArray(requestInstance.getParameter("securityQuestions"));
                if (securityQuestionsArray.length() == 0) {
                    ErrorCodeEnum.ERR_20601.setErrorCode(result);
                    result.addParam(new Param("status", "failure", FabricConstants.STRING));
                    return result;

                }
            } else {
                ErrorCodeEnum.ERR_20601.setErrorCode(result);
                result.addParam(new Param("status", "failure", FabricConstants.STRING));
                return result;

            }

            UserDetailsBean loggedInUserDetails = LoggedInUserHandler.getUserDetails(requestInstance);
            if (loggedInUserDetails != null) {
                systemUser = loggedInUserDetails.getUserId();
            }
            // Delete existing customer questions
            customerHandler.deleteAllSecurityQuestionsForACustomer(customerId, requestInstance, authToken);

            JSONObject createSecurityQuestionsResponseJSON = createCustomerSecurityQuestions(customerId,
                    securityQuestionsArray, authToken, systemUser, requestInstance);
            if (createSecurityQuestionsResponseJSON == null
                    || !createSecurityQuestionsResponseJSON.has(FabricConstants.OPSTATUS)
                    || createSecurityQuestionsResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                ErrorCodeEnum.ERR_20245.setErrorCode(result);
                result.addParam(new Param("status", "failure", FabricConstants.STRING));
                return result;
            }
            result.addParam(new Param("status", "success", FabricConstants.STRING));
            result.addParam(new Param("questionsMapped", securityQuestionsArray.length() + "", FabricConstants.STRING));

        } catch (ClassCastException e) {
            LOG.error("Exception occured in CustomerSecurityQuestionCreate invoke method ", e);
            statusParam = new Param("Status", "Exception", FabricConstants.STRING);
            errorCodeParam = new Param("errorCode", "EXCEPTION", FabricConstants.STRING);
            errorMessageParam = new Param("errorMessage", e.getMessage(), FabricConstants.STRING);
            result.addParam(errorCodeParam);
            result.addParam(errorMessageParam);
            result.addParam(statusParam);
        }
        return result;
    }

    private JSONObject createCustomerSecurityQuestions(String customerId, JSONArray securityQuestionsArray,
            String authToken, String systemUser, DataControllerRequest requestInstance) {
        JSONObject createResponseJSON = null;
        Map<String, String> postParametersMap = new HashMap<String, String>();

        for (int i = 0; i < securityQuestionsArray.length(); i++) {
            String questionId = ((JSONObject) securityQuestionsArray.get(i)).get("questionId").toString().trim();
            String customerAnswer = ((JSONObject) securityQuestionsArray.get(i)).get("customerAnswer").toString()
                    .trim();

            postParametersMap.clear();
            postParametersMap.put("Customer_id", customerId);
            postParametersMap.put("SecurityQuestion_id", questionId);
            postParametersMap.put("CustomerAnswer", BCrypt.hashpw(customerAnswer, BCrypt.gensalt(11)));
            postParametersMap.put("createdby", systemUser);
            postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
            postParametersMap.put("modifiedby", "NULL");
            postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
            postParametersMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());
            postParametersMap.put("softdeleteflag", "0");

            String createResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERSECURITYQUESTIONS_CREATE,
                    postParametersMap, null, requestInstance);
            createResponseJSON = CommonUtilities.getStringAsJSONObject(createResponse);
            int getOpStatusCode = createResponseJSON.getInt(FabricConstants.OPSTATUS);
            if (getOpStatusCode != 0) {
                // rollback logic
                LOG.error("question  mapping  failed");
                return createResponseJSON;
            }

        }
        return createResponseJSON;
    }

}