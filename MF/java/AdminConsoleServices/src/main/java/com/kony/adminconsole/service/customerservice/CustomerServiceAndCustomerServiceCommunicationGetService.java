package com.kony.adminconsole.service.customerservice;

import java.util.ArrayList;
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
 * Service to retrieve the Customer Services and their corresponding Communication Records
 *
 * @author Aditya Mankal
 * 
 */
public class CustomerServiceAndCustomerServiceCommunicationGetService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CustomerServiceAndCustomerServiceCommunicationGetService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        try {

            Result processedResult = new Result();
            String serviceID = requestInstance.getParameter("Service_ID");
            String statusId = requestInstance.getParameter("status_id");

            Map<String, String> inputMap = new HashMap<String, String>();
            String filter = "";
            if (StringUtils.isNotBlank(serviceID)) {
                filter += "Service_id eq '" + serviceID + "'";
            }
            if (StringUtils.isNotBlank(statusId)) {
                if (StringUtils.isNotBlank(filter)) {
                    filter += " and ";
                }
                filter += "Service_Status_id eq '" + statusId + "' and ServiceCommunication_Status_id eq '" + statusId
                        + "'";
            }

            inputMap.put(ODataQueryConstants.FILTER, filter);
            inputMap.put(ODataQueryConstants.ORDER_BY, "Service_id,ServiceCommunication_Priority");

            String operationResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERSERVICE_COMMUNICATION_VIEW_READ,
                    inputMap, null, requestInstance);
            JSONObject operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
            if (operationResponseJSON != null && operationResponseJSON.has(FabricConstants.OPSTATUS)
                    && operationResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && operationResponseJSON.has("customerservice_communication_view")) {

                JSONArray customerServiceCommuncationJSONArray = operationResponseJSON
                        .getJSONArray("customerservice_communication_view");

                Dataset resultDataset = new Dataset();
                resultDataset.setId("records");
                ArrayList<String> processedServicesList = new ArrayList<String>();
                for (int indexVar = 0; indexVar < customerServiceCommuncationJSONArray.length(); indexVar++) {

                    JSONObject currJSONObject = customerServiceCommuncationJSONArray.getJSONObject(indexVar);
                    String currServiceID = currJSONObject.optString("Service_id");

                    if (processedServicesList.contains(currServiceID) || StringUtils.isBlank(currServiceID)) {
                        continue; // Implies that the current service and it's communication records have been
                                  // traversed
                    }

                    Record serviceRecord = new Record();
                    for (String currKey : currJSONObject.keySet()) {
                        // Adding the Service Details to the ServiceDetailsJSON Object
                        if (currKey.startsWith("Service_")) {
                            serviceRecord.addParam(
                                    new Param(currKey, currJSONObject.optString(currKey), FabricConstants.STRING));
                        }

                    }

                    Dataset serviceDataset = new Dataset();
                    for (int innerIndexVar = 0; innerIndexVar < customerServiceCommuncationJSONArray
                            .length(); innerIndexVar++) {
                        // Traversing all rows to find the communication records of the current Service
                        // ID

                        currJSONObject = customerServiceCommuncationJSONArray.getJSONObject(innerIndexVar);
                        String targetServiceID = currJSONObject.optString("Service_id");
                        if (!StringUtils.equalsIgnoreCase(currServiceID, targetServiceID)) {
                            continue;
                        }

                        Record currServiceCommunicationRecord = new Record();
                        for (String currKey : currJSONObject.keySet()) {
                            // Adding the Service Details to the ServiceDetailsJSON Object
                            if (currKey.startsWith("ServiceCommunication_")) {
                                currServiceCommunicationRecord.addParam(
                                        new Param(currKey, currJSONObject.optString(currKey), FabricConstants.STRING));
                            }
                        }
                        serviceDataset.addRecord(currServiceCommunicationRecord);
                    }

                    serviceDataset.setId("Communication_Records");
                    serviceRecord.addDataset(serviceDataset);
                    serviceRecord.setId(currServiceID);
                    resultDataset.addRecord(serviceRecord);
                    processedServicesList.add(currServiceID);
                }
                processedResult.addDataset(resultDataset);
                return processedResult;
            }
            throw new ApplicationException(ErrorCodeEnum.ERR_20100);

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