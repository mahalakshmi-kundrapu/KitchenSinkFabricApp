package com.kony.adminconsole.service.customermanagement;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.handler.CustomerHandler;
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
 * CustomerGetContactInfo service will fetch customer contact information
 * 
 * @author Alahari Prudhvi Akhil (KH2346)
 * 
 */
public class CustomerGetContactInfo implements JavaService2 {

    public static final String CONTACT_VALUE_SEPERATOR = "-";
    private static final Logger LOG = Logger.getLogger(CustomerGetContactInfo.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result processedResult = new Result();
        try {
            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            String customerID = requestInstance.getParameter("Customer_id");

            // Fetch customer username
            String userName = null;
            CustomerHandler customerHandler = new CustomerHandler();
            if ((customerID == null || StringUtils.isBlank(customerID))
                    && requestInstance.getParameter("username") != null) {
                requestInstance.setAttribute("isServiceBeingAccessedByOLB", true);
                userName = requestInstance.getParameter("username");
                if (StringUtils.isBlank(userName)) {
                    ErrorCodeEnum.ERR_20612.setErrorCode(processedResult);
                    Param statusParam = new Param("Status", "Failure", FabricConstants.STRING);
                    processedResult.addParam(statusParam);
                    return processedResult;

                }

                customerID = customerHandler.getCustomerId(userName, requestInstance);
            }
            if (StringUtils.isBlank(customerID)) {
                ErrorCodeEnum.ERR_20613.setErrorCode(processedResult);
                Param statusParam = new Param("Status", "Failure", FabricConstants.STRING);
                processedResult.addParam(statusParam);
                return processedResult;
            }

            JSONObject readCustomerAddr = readCustomerAddr(authToken, customerID, requestInstance);

            if (readCustomerAddr == null || !readCustomerAddr.has(FabricConstants.OPSTATUS)
                    || readCustomerAddr.getInt(FabricConstants.OPSTATUS) != 0) {
                ErrorCodeEnum.ERR_20881.setErrorCode(processedResult);
                Param statusParam = new Param("Status", "Failure", FabricConstants.STRING);
                processedResult.addParam(statusParam);
                return processedResult;

            }

            // read from customercommunication table
            JSONObject readCustomerContactInfo = readCustomerMobileInfo(authToken, customerID, requestInstance);

            if (readCustomerContactInfo == null || !readCustomerContactInfo.has(FabricConstants.OPSTATUS)
                    || readCustomerContactInfo.getInt(FabricConstants.OPSTATUS) != 0) {
                ErrorCodeEnum.ERR_20882.setErrorCode(processedResult);
                Param statusParam = new Param("Status", "Failure", FabricConstants.STRING);
                processedResult.addParam(statusParam);
                return processedResult;

            }
            JSONObject readCustomerEmailInfo = readCustomerEmailInfo(authToken, customerID, requestInstance);

            if (readCustomerEmailInfo == null || !readCustomerEmailInfo.has(FabricConstants.OPSTATUS)
                    || readCustomerEmailInfo.getInt(FabricConstants.OPSTATUS) != 0) {
                ErrorCodeEnum.ERR_20883.setErrorCode(processedResult);
                Param statusParam = new Param("Status", "Failure", FabricConstants.STRING);
                processedResult.addParam(statusParam);
                return processedResult;

            }

            // read customer table
            JSONObject readCustomerPreferredInfo = readCustomerPreferredInfo(authToken, customerID, requestInstance);
            if (readCustomerPreferredInfo == null || !readCustomerPreferredInfo.has(FabricConstants.OPSTATUS)
                    || readCustomerPreferredInfo.getInt(FabricConstants.OPSTATUS) != 0) {
                ErrorCodeEnum.ERR_20884.setErrorCode(processedResult);
                Param statusParam = new Param("Status", "Failure", FabricConstants.STRING);
                processedResult.addParam(statusParam);
                return processedResult;

            }

            JSONArray PhoneNumbers = (JSONArray) readCustomerContactInfo.get("customercommunication");
            Dataset phoneNumbersArray = new Dataset();
            phoneNumbersArray.setId("ContactNumbers");
            for (int count = 0; count < PhoneNumbers.length(); count++) {
                Record phoneNumber = constructRecordFromJSON((JSONObject) PhoneNumbers.get(count));
                phoneNumbersArray.addRecord(phoneNumber);
            }
            JSONArray emailIds = (JSONArray) readCustomerEmailInfo.get("customercommunication");
            Dataset emailIdsArray = new Dataset();
            emailIdsArray.setId("EmailIds");
            for (int count = 0; count < emailIds.length(); count++) {
                Record emailId = constructRecordFromJSON((JSONObject) emailIds.get(count));
                emailIdsArray.addRecord(emailId);
            }

            JSONArray customerInfo = (JSONArray) readCustomerPreferredInfo.get("customer");
            Record preferred = constructRecordFromJSON((JSONObject) customerInfo.get(0));
            preferred.setId("PreferredTime&Method");

            JSONArray AddressDetails = (JSONArray) readCustomerAddr.get("customeraddress_view");
            Dataset AddressDetailsArray = new Dataset();
            AddressDetailsArray.setId("Addresses");
            for (int count = 0; count < AddressDetails.length(); count++) {
                Record Addresses = constructRecordFromJSON((JSONObject) AddressDetails.get(count));
                AddressDetailsArray.addRecord(Addresses);
            }

            processedResult.addDataset(phoneNumbersArray);
            processedResult.addDataset(emailIdsArray);
            processedResult.addDataset(AddressDetailsArray);
            processedResult.addRecord(preferred);

            Param statusParam = new Param("Status", "Succesful", FabricConstants.STRING);
            processedResult.addParam(statusParam);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    public JSONObject readCustomerAddr(String AuthToken, String customerID, DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "CustomerId eq '" + customerID + "'");
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERADDRESS_VIEW_READ,
                postParametersMap, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

    public JSONObject readCustomerMobileInfo(String AuthToken, String customerID,
            DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER,
                "Customer_id eq '" + customerID + "' and Type_id eq COMM_TYPE_PHONE");
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERCOMMUNICATION_READ,
                postParametersMap, null, requestInstance);
        JSONObject readEndpointResponseJSON = CommonUtilities.getStringAsJSONObject(readEndpointResponse);
        if (readEndpointResponseJSON != null && readEndpointResponseJSON.has(FabricConstants.OPSTATUS)
                && readEndpointResponseJSON.optInt(FabricConstants.OPSTATUS) == 0) {
            if (readEndpointResponseJSON.has("customercommunication")) {
                JSONArray customerCommunicationRecords = readEndpointResponseJSON.getJSONArray("customercommunication");
                String phoneNumber, extension, countryCode;
                String currValue;
                String currValueArray[];
                for (int indexVar = 0; indexVar < customerCommunicationRecords.length(); indexVar++) {
                    JSONObject currRecord = customerCommunicationRecords.optJSONObject(indexVar);
                    currValue = currRecord.optString("Value");
                    if (StringUtils.isNotBlank(currValue)) {
                        currValueArray = StringUtils.split(currValue, CONTACT_VALUE_SEPERATOR);
                        if (currValueArray != null && currValueArray.length >= 2) {
                            countryCode = currValueArray[0];
                            currRecord.put("phoneCountryCode", countryCode);
                            phoneNumber = currValueArray[1];
                            currRecord.put("phoneNumber", phoneNumber);
                            if (currValueArray.length == 3) {
                                extension = currValueArray[2];
                                currRecord.put("phoneExtension", extension);
                            }
                        }

                    }
                }
            }

        }

        return readEndpointResponseJSON;

    }

    public JSONObject readCustomerEmailInfo(String AuthToken, String customerID,
            DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER,
                "Customer_id eq '" + customerID + "' and Type_id eq COMM_TYPE_EMAIL");
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERCOMMUNICATION_READ,
                postParametersMap, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

    public JSONObject readCustomerPreferredInfo(String AuthToken, String customerID,
            DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.SELECT, "PreferredContactMethod,PreferredContactTime");
        postParametersMap.put(ODataQueryConstants.FILTER, "id eq '" + customerID + "'");
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.CUSTOMER_READ, postParametersMap, null,
                requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

    public Record constructRecordFromJSON(JSONObject JSON) {
        Record response = new Record();
        if (JSON == null || JSON.length() == 0) {
            return response;
        }
        Iterator<String> keys = JSON.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            Param param = new Param(key, JSON.getString(key), FabricConstants.STRING);
            response.addParam(param);
        }

        return response;
    }

}