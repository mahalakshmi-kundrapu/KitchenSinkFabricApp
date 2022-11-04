package com.kony.adminconsole.service.accountrequests;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.handler.PaginationHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

public class RequestsGetService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(RequestsGetService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            String userName;

            Result processedResult = new Result();
            if (requestInstance.getParameter("Username") == null) {
                ErrorCodeEnum.ERR_20705.setErrorCode(processedResult);
                return processedResult;
            }
            CustomerHandler customerHandler = new CustomerHandler();
            userName = requestInstance.getParameter("Username");

            Map<String, String> postParametersMap = new HashMap<String, String>();
            PaginationHandler.setOffset(requestInstance, postParametersMap);

            String customerId = customerHandler.getCustomerId(userName, requestInstance);
            postParametersMap.put(ODataQueryConstants.FILTER, "CustomerId eq '" + customerId + "'");

            JSONObject readRequestsResponseJSON = PaginationHandler.getPaginatedData(
                    ServiceURLEnum.CARDACCOUNTREQUEST_VIEW_READ, postParametersMap, null, requestInstance);

            if (readRequestsResponseJSON != null && readRequestsResponseJSON.has(FabricConstants.OPSTATUS)
                    && readRequestsResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readRequestsResponseJSON.has("cardaccountrequest_view")) {
                LOG.debug("Fetch Card Requests Status:Successful");
                JSONArray requestsJSONArray = readRequestsResponseJSON.getJSONArray("cardaccountrequest_view");
                Dataset requestsDataSet = new Dataset();
                requestsDataSet.setId("CardAccountRequests");
                JSONObject currRequestsJSONObject = null;
                for (int indexVar = 0; indexVar < requestsJSONArray.length(); indexVar++) {
                    currRequestsJSONObject = requestsJSONArray.getJSONObject(indexVar);
                    Record currRecord = new Record();
                    Param Request_id_Param = new Param("Request_id", currRequestsJSONObject.getString("Request_id"),
                            FabricConstants.STRING);
                    currRecord.addParam(Request_id_Param);
                    Param Date_Param = new Param("Date", currRequestsJSONObject.getString("Date"),
                            FabricConstants.STRING);
                    currRecord.addParam(Date_Param);
                    Param Type_Param = new Param("Type", currRequestsJSONObject.getString("Type"),
                            FabricConstants.STRING);
                    currRecord.addParam(Type_Param);
                    Param CardAccountNumber_Param = new Param("CardAccountNumber",
                            currRequestsJSONObject.getString("CardAccountNumber"), FabricConstants.STRING);
                    currRecord.addParam(CardAccountNumber_Param);
                    Param CardAccountName_Param = new Param("CardAccountNumber",
                            currRequestsJSONObject.getString("CardAccountName"), FabricConstants.STRING);
                    currRecord.addParam(CardAccountName_Param);
                    if (currRequestsJSONObject.has("Reason")) {
                        Param Reason_Param = new Param("Reason", currRequestsJSONObject.getString("Reason"),
                                FabricConstants.STRING);
                        currRecord.addParam(Reason_Param);
                    } else {
                        Param Reason_Param = new Param("Reason", "N/A", FabricConstants.STRING);
                        currRecord.addParam(Reason_Param);
                    }
                    if (currRequestsJSONObject.has("CommunicationValue")) {
                        Param DeliveryDetails_Param = new Param("DeliveryDetails",
                                currRequestsJSONObject.getString("CommunicationValue"), FabricConstants.STRING);
                        currRecord.addParam(DeliveryDetails_Param);
                        Param DeliveryMode_Param = new Param("DeliveryMode",
                                currRequestsJSONObject.getString("DeliveryMode"), FabricConstants.STRING);
                        currRecord.addParam(DeliveryMode_Param);
                    } else {
                        if (currRequestsJSONObject.has("Address")) {
                            String address = currRequestsJSONObject.getString("Address").replaceAll("NULL", "");
                            Param DeliveryDetails_Param = new Param("DeliveryDetails", address, FabricConstants.STRING);
                            currRecord.addParam(DeliveryDetails_Param);
                            Param DeliveryMode_Param = new Param("DeliveryMode", "Address", FabricConstants.STRING);
                            currRecord.addParam(DeliveryMode_Param);
                        }
                    }
                    Param Status_Param = new Param("Status", currRequestsJSONObject.getString("Status"),
                            FabricConstants.STRING);
                    currRecord.addParam(Status_Param);
                    Param customerId_Param = new Param("CustomerId", currRequestsJSONObject.getString("CustomerId"),
                            FabricConstants.STRING);
                    currRecord.addParam(customerId_Param);
                    requestsDataSet.addRecord(currRecord);
                }
                processedResult.addDataset(requestsDataSet);
                return processedResult;
            }
            LOG.error("Fetch Card Requests Status:Failed");
            ErrorCodeEnum.ERR_20303.setErrorCode(processedResult);
            return processedResult;
        } catch (Exception e) {
            LOG.error("Exception in Request Get Service", e);
            Result failedResult = new Result();
            ErrorCodeEnum.ERR_20691.setErrorCode(failedResult);
            return failedResult;
        }
    }
}