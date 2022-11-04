package com.kony.adminconsole.service.staticcontentmanagement.securityquestions;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.service.authmodule.APICustomIdentityService;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to Retrieve Security Questions
 *
 * @author Aditya Mankal
 * 
 */
public class SecurityQuestionsGetService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(SecurityQuestionsGetService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result processedResult = new Result();
            Map<String, String> postParametersMap = new HashMap<String, String>();
            postParametersMap.put(ODataQueryConstants.ORDER_BY, "lastmodifiedts desc");
            String statusId = requestInstance.getParameter("status_id");
            if (StringUtils.isNotBlank(statusId)) {
                postParametersMap.put(ODataQueryConstants.FILTER, "SecurityQuestion_Status eq '" + statusId + "'");
            }

            // Fetched User Details from Identity Scope
            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            if (StringUtils.equalsIgnoreCase(userDetailsBeanInstance.getUserId(),
                    APICustomIdentityService.API_USER_ID)) {
                // Service is being accessed by OLB. Send only active security questions
                postParametersMap.put(ODataQueryConstants.FILTER, "SecurityQuestion_Status eq 'SID_ACTIVE'");
            }

            String readSecurityQuestionsResponse = Executor.invokeService(ServiceURLEnum.SECURITY_QUESTIONS_VIEW_READ,
                    postParametersMap, null, requestInstance);
            JSONObject readSecurityQuestionsResponseJSON = CommonUtilities
                    .getStringAsJSONObject(readSecurityQuestionsResponse);
            if (readSecurityQuestionsResponseJSON != null
                    && readSecurityQuestionsResponseJSON.has(FabricConstants.OPSTATUS)
                    && readSecurityQuestionsResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readSecurityQuestionsResponseJSON.has("security_questions_view")) {
                LOG.debug("Fetch Security Questions Status:Successful");
                JSONArray securityQuestionsJSONArray = readSecurityQuestionsResponseJSON
                        .getJSONArray("security_questions_view");
                Dataset securityQuestionsDataSet = new Dataset();
                securityQuestionsDataSet.setId("records");
                for (int indexVar = 0; indexVar < securityQuestionsJSONArray.length(); indexVar++) {
                    JSONObject currQuestionJSONObject = securityQuestionsJSONArray.getJSONObject(indexVar);
                    Record currQuestionRecord = new Record();
                    for (String currKey : currQuestionJSONObject.keySet()) {
                        Param currValParam = new Param(currKey, currQuestionJSONObject.optString(currKey),
                                FabricConstants.STRING);
                        currQuestionRecord.addParam(currValParam);
                    }
                    securityQuestionsDataSet.addRecord(currQuestionRecord);
                }

                processedResult.addDataset(securityQuestionsDataSet);
                return processedResult;
            }
            LOG.error("Fetch Security Questions Status:Failed");
            ErrorCodeEnum.ERR_20244.setErrorCode(processedResult);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

}