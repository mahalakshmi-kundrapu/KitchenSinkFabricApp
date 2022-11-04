package com.kony.adminconsole.service.customerrequest;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
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
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to manage the retrieve the Message Templates
 *
 * @author Aditya Mankal
 * 
 */
public class MessageTemplateGetService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(MessageTemplateGetService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        try {

            Result processedResult = new Result();

            // Read Inputs
            String templateID = requestInstance.getParameter("templateID");

            // Prepare Filter Query Map
            Map<String, String> inputMap = new HashMap<String, String>();
            if (StringUtils.isNotBlank(templateID)) {
                // Fetch specific Template when Template ID is passed
                inputMap.put(ODataQueryConstants.FILTER, "id eq '" + templateID + "'");
            }

            String operationResponse = Executor.invokeService(ServiceURLEnum.MESSAGETEMPLATE_READ, inputMap, null,
                    requestInstance);
            JSONObject operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
            if (operationResponseJSON != null && operationResponseJSON.has(FabricConstants.OPSTATUS)
                    && operationResponseJSON.optInt(FabricConstants.OPSTATUS) == 0
                    && operationResponseJSON.has("messagetemplate")) {
                LOG.debug("Message Template Data Fetched. Constructing Response");

                JSONArray messageTemplateJSONArray = operationResponseJSON.getJSONArray("messagetemplate");
                Dataset messageTemplateDataset = new Dataset();
                messageTemplateDataset.setId("messagetemplate");

                for (int indexVar = 0; indexVar < messageTemplateJSONArray.length(); indexVar++) {
                    Record currRecord = new Record();
                    JSONObject currFAQObject = messageTemplateJSONArray.getJSONObject(indexVar);
                    for (String currKey : currFAQObject.keySet()) {
                        Param currValParam = new Param(currKey, currFAQObject.optString(currKey),
                                FabricConstants.STRING);
                        currRecord.addParam(currValParam);
                    }
                    messageTemplateDataset.addRecord(currRecord);
                }
                processedResult.addDataset(messageTemplateDataset);
                LOG.debug("Returning Success Response");
                return processedResult;
            } else {
                // Failed Operation
                LOG.error("Failed Operation. Response" + operationResponse);
                throw new ApplicationException(ErrorCodeEnum.ERR_20144);
            }
        } catch (ApplicationException e) {
            Result errorResult = new Result();
            LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
            e.getErrorCodeEnum().setErrorCode(errorResult);
            return errorResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }

    }

}