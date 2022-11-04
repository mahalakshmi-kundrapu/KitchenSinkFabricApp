package com.kony.adminconsole.service.accountrequests;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.handler.StatusHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class RequestsCreateService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(RequestsCreateService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            Result failedResult = new Result();
            if (requestInstance.getParameter("Username") == null) {
                ErrorCodeEnum.ERR_20565.setErrorCode(failedResult);
                return failedResult;
            } else if (requestInstance.getParameter("CardAccountNumber") == null) {
                ErrorCodeEnum.ERR_20699.setErrorCode(failedResult);
                return failedResult;
            } else if (requestInstance.getParameter("CardAccountName") == null) {
                ErrorCodeEnum.ERR_20700.setErrorCode(failedResult);
                return failedResult;
            } else if (requestInstance.getParameter("AccountType") == null) {
                ErrorCodeEnum.ERR_20701.setErrorCode(failedResult);
                return failedResult;
            } else if (requestInstance.getParameter("RequestCode") == null) {
                ErrorCodeEnum.ERR_20702.setErrorCode(failedResult);
                return failedResult;
            } else if (requestInstance.getParameter("Channel") == null) {
                ErrorCodeEnum.ERR_20703.setErrorCode(failedResult);
                return failedResult;
            } else {
                Map<String, String> postParametersMap = new HashMap<String, String>();
                StatusHandler statusHandler = new StatusHandler();
                CustomerHandler customerHandler = new CustomerHandler();
                String username = requestInstance.getParameter("Username");
                String userid = customerHandler.getCustomerId(username, requestInstance);
                String cardAccountRequestId = String.valueOf(CommonUtilities.getNumericId());
                postParametersMap.put("id", cardAccountRequestId);
                postParametersMap.put("Customer_id", userid);
                postParametersMap.put("CardAccountNumber", requestInstance.getParameter("CardAccountNumber"));
                postParametersMap.put("CardAccountName", requestInstance.getParameter("CardAccountName"));
                postParametersMap.put("AccountType", requestInstance.getParameter("AccountType"));
                postParametersMap.put("RequestType_id",
                        getRequestIdByCode(requestInstance.getParameter("RequestCode"), authToken, requestInstance));
                if (requestInstance.getParameter("RequestReason") != null) {
                    postParametersMap.put("RequestReason", requestInstance.getParameter("RequestReason"));
                }
                postParametersMap.put("Channel_id",
                        getchannelIdByName(requestInstance.getParameter("Channel"), authToken, requestInstance));
                postParametersMap.put("Status_id",
                        statusHandler.getStatusIdForGivenStatus(null, "New", authToken, requestInstance));
                if (requestInstance.getParameter("Address_id") != null) {
                    postParametersMap.put("Address_id", requestInstance.getParameter("Address_id"));
                }
                if (requestInstance.getParameter("communication_id") != null) {
                    postParametersMap.put("Communication_id", requestInstance.getParameter("communication_id"));
                }
                if (requestInstance.getParameter("AdditionalNotes") != null) {
                    postParametersMap.put("AdditionalNotes", requestInstance.getParameter("AdditionalNotes"));
                }
                postParametersMap.put("createdby", userid);
                postParametersMap.put("modifiedby", userid);
                String createCardRequestResponse = Executor.invokeService(ServiceURLEnum.CARDACCOUNTREQUEST_CREATE,
                        postParametersMap, null, requestInstance);
                JSONObject createCardRequestResponseJSON = CommonUtilities
                        .getStringAsJSONObject(createCardRequestResponse);
                int getOpStatusCode = createCardRequestResponseJSON.getInt(FabricConstants.OPSTATUS);
                if (getOpStatusCode == 0) {
                    Result result = new Result();
                    Param id_Param = new Param("id", cardAccountRequestId.toString(), FabricConstants.STRING);
                    result.addParam(id_Param);
                    return result;
                } else {
                    ErrorCodeEnum.ERR_20691.setErrorCode(failedResult);
                    return failedResult;

                }

            }
        } catch (Exception e) {
            LOG.error("Exception in Request Create Service", e);
            Result failedResult = new Result();
            ErrorCodeEnum.ERR_20691.setErrorCode(failedResult);
            return failedResult;
        }

    }

    private String getRequestIdByCode(String code, String authToken, DataControllerRequest requestInstance) {
        String requestTypeId = "";
        try {
            Map<String, String> postParametersMap = new HashMap<String, String>();
            postParametersMap.put(ODataQueryConstants.SELECT, "id");
            postParametersMap.put(ODataQueryConstants.FILTER, "Code eq'" + code + "'");
            String readEndpointResponse = Executor.invokeService(ServiceURLEnum.CARDACCOUNTREQUESTTYPE_READ,
                    postParametersMap, null, requestInstance);
            JSONArray responseArray = (JSONArray) CommonUtilities.getStringAsJSONObject(readEndpointResponse)
                    .get("cardaccountrequesttype");
            if (responseArray.length() != 0 || responseArray != null) {
                requestTypeId = ((JSONObject) responseArray.get(0)).getString("id");
            }
        } catch (Exception e) {
            LOG.error("Exception in getRequestIdByCode", e);
        }

        return requestTypeId;
    }

    public String getchannelIdByName(String description, String authToken, DataControllerRequest requestInstance) {
        String channelId = "";
        try {
            Map<String, String> postParametersMap = new HashMap<String, String>();
            postParametersMap.put(ODataQueryConstants.SELECT, "id");
            postParametersMap.put(ODataQueryConstants.FILTER, "Description eq'" + description + "'");
            String readEndpointResponse = Executor.invokeService(ServiceURLEnum.SERVICECHANNEL_READ, postParametersMap,
                    null, requestInstance);
            JSONArray responseArray = (JSONArray) CommonUtilities.getStringAsJSONObject(readEndpointResponse)
                    .get("servicechannel");
            if (responseArray.length() != 0 || responseArray != null) {
                channelId = ((JSONObject) responseArray.get(0)).getString("id");
            }
        } catch (Exception e) {
            LOG.error("Exception in getRequestIdByCode", e);
        }

        return channelId;
    }

}