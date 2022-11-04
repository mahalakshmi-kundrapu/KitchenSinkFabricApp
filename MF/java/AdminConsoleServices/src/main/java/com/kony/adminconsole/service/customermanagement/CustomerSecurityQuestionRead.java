package com.kony.adminconsole.service.customermanagement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

public class CustomerSecurityQuestionRead implements JavaService2 {

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
        int randomNumber = 0;
        int numberOfQuestionsToSent = 2;
        CustomerHandler customerHandler = new CustomerHandler();
        JSONObject securityQuestionJSON = null;
        try {

            if (requestInstance.getParameter("userName") != null) {
                userName = requestInstance.getParameter("userName");
                if (StringUtils.isBlank(userName)) {
                    ErrorCodeEnum.ERR_20612.setErrorCode(result);
                    return result;

                }
            } else {
                ErrorCodeEnum.ERR_20612.setErrorCode(result);
                return result;
            }
            customerId = customerHandler.getCustomerId(userName, requestInstance);
            if (StringUtils.isBlank(customerId)) {
                ErrorCodeEnum.ERR_20613.setErrorCode(result);
                return result;

            }

            UserDetailsBean loggedInUserDetails = LoggedInUserHandler.getUserDetails(requestInstance);
            if (loggedInUserDetails != null) {
                systemUser = loggedInUserDetails.getUserId();
            }

            JSONObject readResJSON = customerHandler.getSecurityQuestionsForGivenCustomerWithoutAnswer(systemUser,
                    customerId, authToken, requestInstance);
            if (readResJSON == null || !readResJSON.has(FabricConstants.OPSTATUS)
                    || readResJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                ErrorCodeEnum.ERR_20246.setErrorCode(result);
                return result;

            }
            JSONArray securityQuestionsForCustomer = (JSONArray) readResJSON.get("customersecurityquestion_view");
            randomNumber = CommonUtilities.generateRandomWithRange(0, securityQuestionsForCustomer.length() - 1);

            if (securityQuestionsForCustomer.length() == 0) {
                ErrorCodeEnum.ERR_20602.setErrorCode(result);
                return result;

            }

            Dataset securityQuestionsCustomerDataSet = new Dataset();
            securityQuestionsCustomerDataSet.setId("records");

            for (int i = 0; i < numberOfQuestionsToSent; i++, randomNumber++) {

                securityQuestionJSON = securityQuestionsForCustomer
                        .getJSONObject(randomNumber % securityQuestionsForCustomer.length());
                Record securityQuestionRecord = new Record();
                for (String currKey : securityQuestionJSON.keySet()) {
                    Param currValParam = new Param(currKey, securityQuestionJSON.getString(currKey),
                            FabricConstants.STRING);
                    securityQuestionRecord.addParam(currValParam);
                }
                securityQuestionsCustomerDataSet.addRecord(securityQuestionRecord);
            }

            result.addDataset(securityQuestionsCustomerDataSet);
            return result;
        } catch (ClassCastException e) {
            LOG.error("Exception occured in CustomerSecurityQuestionRead invoke method ", e);
            statusParam = new Param("Status", "Exception", FabricConstants.STRING);
            errorCodeParam = new Param("errorCode", "EXCEPTION", FabricConstants.STRING);
            errorMessageParam = new Param("errorMessage", e.getMessage(), FabricConstants.STRING);
            result.addParam(errorCodeParam);
            result.addParam(errorMessageParam);
            result.addParam(statusParam);
        }
        return result;
    }

}