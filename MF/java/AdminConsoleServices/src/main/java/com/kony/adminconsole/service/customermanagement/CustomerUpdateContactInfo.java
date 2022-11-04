package com.kony.adminconsole.service.customermanagement;

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
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.EventEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ModuleNameEnum;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * CustomerUpdateContactInfo service will update the contact information of a customer.
 * 
 * @author Alahari Prudhvi Akhil (KH2346)
 * 
 */
public class CustomerUpdateContactInfo implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CustomerUpdateContactInfo.class);
    private static final String PHONE_TYPE = "COMM_TYPE_PHONE";
    private static final String EMAIL_TYPE = "COMM_TYPE_EMAIL";

    private static final String INPUT_USERNAME = "UserName";
    private static final String INPUT_CUSTOMERID = "Customer_id";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result processedResult = new Result();

        String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
        String customerId = requestInstance.getParameter(INPUT_CUSTOMERID);
        String userName = null;
        try {
            CustomerHandler customerHandler = new CustomerHandler();
            if ((customerId == null || StringUtils.isBlank(customerId))
                    && requestInstance.getParameter(INPUT_USERNAME) != null) {
                requestInstance.setAttribute("isServiceBeingAccessedByOLB", true);
                userName = requestInstance.getParameter(INPUT_USERNAME);
                if (StringUtils.isBlank(userName)) {
                    ErrorCodeEnum.ERR_20612.setErrorCode(processedResult);
                    return processedResult;

                }

                customerId = customerHandler.getCustomerId(userName, requestInstance);
            }

            if (StringUtils.isBlank(customerId)) {
                ErrorCodeEnum.ERR_20613.setErrorCode(processedResult);
                return processedResult;
            }

            if (LoggedInUserHandler.getUserDetails(requestInstance).isAPIUser() == false) {
                // Check the access control for this customer for current logged-in internal
                // user
                CustomerHandler.doesCurrentLoggedinUserHasAccessToCustomer(null, customerId, requestInstance,
                        processedResult);
                if (processedResult.getParamByName(ErrorCodeEnum.ERROR_CODE_KEY) != null) {
                    return processedResult;
                }
                // End of access check
            }

            JSONArray ContactNumbers = null;
            if (requestInstance.getParameter("PhoneNumbers") != null) {
                ContactNumbers = new JSONArray(requestInstance.getParameter("PhoneNumbers"));
            }
            JSONArray EmailIds = null;
            if (requestInstance.getParameter("EmailIds") != null) {
                EmailIds = new JSONArray(requestInstance.getParameter("EmailIds"));
            }
            JSONArray Addresses = null;
            if (requestInstance.getParameter("Addresses") != null) {
                Addresses = new JSONArray(requestInstance.getParameter("Addresses"));
            }
            String PreferredContactMethod = requestInstance.getParameter("PreferredContactMethod");
            String PreferredContactTime = requestInstance.getParameter("PreferredContactTime");
            String deleteAddressID = requestInstance.getParameter("deleteAddressID");
            String deleteCommunicationID = requestInstance.getParameter("deleteCommunicationID");

            // Update customer phone number
            if (ContactNumbers != null) {
                for (int i = 0; i < ContactNumbers.length(); i++) {
                    JSONObject contactNum = ContactNumbers.getJSONObject(i);
                    if (contactNum.has("id")) {
                        JSONObject updatePhoneNumbers = updatePhoneNumbers(authToken, customerId, contactNum,
                                requestInstance);
                        processedResult.addParam(new Param("UpdateContactNumber" + i, updatePhoneNumbers.toString(),
                                FabricConstants.STRING));
                    } else {
                        JSONObject createPhoneNumbers = createPhoneNumbers(authToken, contactNum, customerId,
                                requestInstance);
                        processedResult.addParam(new Param("CreateContactNumber" + i, createPhoneNumbers.toString(),
                                FabricConstants.STRING));
                    }

                }
            }

            if (EmailIds != null) {
                // Update customer email id
                for (int i = 0; i < EmailIds.length(); i++) {
                    JSONObject contactEmail = EmailIds.getJSONObject(i);
                    if (contactEmail.has("id")) {
                        JSONObject updateEmailIds = updateEmailIds(authToken, customerId, contactEmail,
                                requestInstance);
                        processedResult.addParam(
                                new Param("UpdateEmail" + i, updateEmailIds.toString(), FabricConstants.STRING));
                    } else {
                        JSONObject createEmailIds = createEmailIds(authToken, contactEmail, customerId,
                                requestInstance);
                        processedResult.addParam(
                                new Param("CreateEmail" + i, createEmailIds.toString(), FabricConstants.STRING));
                    }

                }
            }
            if (Addresses != null) {
                // Update customer email id
                for (int i = 0; i < Addresses.length(); i++) {
                    JSONObject addrObj = Addresses.getJSONObject(i);
                    if (addrObj.has("Addr_id")) {
                        JSONObject updateAddressInfo = updateAddressInfo(authToken, customerId, addrObj,
                                requestInstance);
                        processedResult.addParam(
                                new Param("UpdateAddress" + i, updateAddressInfo.toString(), FabricConstants.STRING));
                    } else {
                        JSONObject createAddressInfo = createAddressInfo(authToken, addrObj, customerId,
                                requestInstance);
                        processedResult.addParam(
                                new Param("CreateAddress" + i, createAddressInfo.toString(), FabricConstants.STRING));
                    }

                }
            }

            if (PreferredContactMethod != null && PreferredContactTime != null) {
                JSONObject updatePreferredTimes = updatePreferredTimes(authToken, customerId, PreferredContactMethod,
                        PreferredContactTime, requestInstance);
                processedResult.addParam(
                        new Param("updatePreferredTimes", updatePreferredTimes.toString(), FabricConstants.STRING));

            }

            if (deleteAddressID != null) {
                JSONObject deleteResponse = deleteAddress(authToken, customerId, deleteAddressID, requestInstance);
                processedResult.addParam(new Param("DeleteAddress", deleteResponse.toString(), FabricConstants.STRING));
            }

            if (deleteCommunicationID != null) {
                deleteCommunication(authToken, customerId, deleteCommunicationID, requestInstance);
                processedResult.addParam(
                        new Param("DeleteCommunication", deleteCommunicationID.toString(), FabricConstants.STRING));
            }

            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                    ActivityStatusEnum.SUCCESSFUL,
                    "Manage customer personal details successful. Username: " + userName);
            Param statusParam = new Param("Status", "Operation successful", FabricConstants.STRING);
            processedResult.addParam(statusParam);

        } catch (ApplicationException ae) {
            LOG.error("Application exception", ae);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Manage customer personal details failed. Username: " + userName);
            ae.getErrorCodeEnum().setErrorCode(processedResult);
            processedResult.addParam(new Param("FailureReason", ae.getMessage(), FabricConstants.STRING));

        } catch (Exception e) {
            LOG.error("Unexpected error occured", e);

            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Manage customer personal details failed. Username: " + userName);
            ErrorCodeEnum.ERR_20001.setErrorCode(processedResult);
            processedResult.addParam(new Param("FailureReason", e.getMessage(), FabricConstants.STRING));
        }

        return processedResult;

    }

    private boolean checkForDuplicateContactNumbers(String value, String communication_id, String customerId, DataControllerRequest requestInstance) {
    	String filter = "Customer_id eq '" + customerId + "' and Type_id eq COMM_TYPE_PHONE";
    	if(StringUtils.isNotBlank(communication_id)) {
    		filter += " and id ne '"+communication_id+"'";
    	}
    	Map<String, String> postParametersMapget = new HashMap<String, String>();
        postParametersMapget.put(ODataQueryConstants.FILTER, filter);
        String readresponse = Executor.invokeService(ServiceURLEnum.CUSTOMERCOMMUNICATION_READ,
                postParametersMapget, null, requestInstance);
        JSONObject readEndpointResponse = CommonUtilities.getStringAsJSONObject(readresponse);
        
        JSONArray resarray = readEndpointResponse.getJSONArray("customercommunication");
        if (resarray != null) {
            value = value.replaceAll("[^a-zA-Z0-9]", "");
            for (int i = 0; i < resarray.length(); i++) {
                String requestvalue = resarray.getJSONObject(i).getString("Value").replaceAll("[^a-zA-Z0-9]", "");
                if (value.equals(requestvalue)) {
                	return true;
                }
            }
            return false;
        }
        return false;
    }

    private JSONObject deleteCommunication(String authToken, String customerID, String deleteCommunicationID,
            DataControllerRequest requestInstance) {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("id", deleteCommunicationID);
        String deleteresponse = Executor.invokeService(ServiceURLEnum.CUSTOMERCOMMUNICATION_DELETE, postParametersMap,
                null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(deleteresponse);
    }

    private JSONObject deleteAddress(String authToken, String customerID, String deleteAddressID,
            DataControllerRequest requestInstance) {
        Map<String, String> postParametersMap1 = new HashMap<String, String>();
        postParametersMap1.put("Customer_id", customerID);
        postParametersMap1.put("Address_id", deleteAddressID);
        Executor.invokeService(ServiceURLEnum.CUSTOMERADDRESS_DELETE, postParametersMap1, null, requestInstance);

        Map<String, String> postParametersMap2 = new HashMap<String, String>();
        postParametersMap2.put("id", deleteAddressID);
        String AddressDeleteResponse = Executor.invokeService(ServiceURLEnum.ADDRESS_DELETE, postParametersMap2, null,
                requestInstance);

        return CommonUtilities.getStringAsJSONObject(AddressDeleteResponse);
    }

    private JSONObject updatePreferredTimes(String authToken, String customerID, String PreferredContactMethod,
            String PreferredContactTime, DataControllerRequest requestInstance) {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("id", customerID);
        postParametersMap.put("PreferredContactMethod", PreferredContactMethod);
        postParametersMap.put("PreferredContactTime", PreferredContactTime);
        postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

        String updateEndpointResponse = Executor.invokeService(ServiceURLEnum.CUSTOMER_UPDATE, postParametersMap, null,
                requestInstance);
        return CommonUtilities.getStringAsJSONObject(updateEndpointResponse);
    }

    private JSONObject createAddressInfo(String authToken, JSONObject addrObj, String customerID,
            DataControllerRequest requestInstance) {

        // Reset primary addresses.
        if (requestInstance.getAttribute("isServiceBeingAccessedByOLB") != null
                && Boolean.parseBoolean(requestInstance.getAttribute("isServiceBeingAccessedByOLB").toString())) {
            if (addrObj.getString("isPrimary").equalsIgnoreCase("1")) {
                resetAllPrimaryAddresses(requestInstance, authToken, customerID);
            }
        }

        Map<String, String> postParametersMap1 = new HashMap<String, String>();
        Map<String, String> postParametersMap2 = new HashMap<String, String>();
        String id = String.valueOf(CommonUtilities.getNewId());
        postParametersMap1.put("Customer_id", customerID);
        postParametersMap1.put("Type_id", addrObj.getString("Addr_type"));
        postParametersMap1.put("isPrimary", addrObj.getString("isPrimary"));
        postParametersMap1.put("Address_id", id);
        postParametersMap2.put("addressLine1", addrObj.getString("addrLine1"));
        postParametersMap2.put("addressLine2", addrObj.getString("addrLine2"));
        postParametersMap2.put("zipCode", addrObj.getString("ZipCode"));
        postParametersMap2.put("City_id", addrObj.getString("City_id"));
        postParametersMap2.put("Region_id", addrObj.getString("Region_id"));
        postParametersMap2.put("id", id);
        String createEndpointResponse = Executor.invokeService(ServiceURLEnum.ADDRESS_CREATE, postParametersMap2, null,
                requestInstance);
        if (CommonUtilities.getStringAsJSONObject(createEndpointResponse).getInt(FabricConstants.OPSTATUS) != 0) {
            return CommonUtilities.getStringAsJSONObject(createEndpointResponse);
        }
        String createCustomerAddressres = Executor.invokeService(ServiceURLEnum.CUSTOMERADDRESS_CREATE,
                postParametersMap1, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(createCustomerAddressres);
    }

    private JSONObject updateAddressInfo(String authToken, String customerID, JSONObject addrObj,
            DataControllerRequest requestInstance) {

        // Reset primary addresses.
        if (requestInstance.getAttribute("isServiceBeingAccessedByOLB") != null
                && Boolean.parseBoolean(requestInstance.getAttribute("isServiceBeingAccessedByOLB").toString())) {
            if (addrObj.getString("isPrimary").equalsIgnoreCase("1")) {
                resetAllPrimaryAddresses(requestInstance, authToken, customerID);
            }
        }

        Map<String, String> postParametersMap1 = new HashMap<String, String>();
        Map<String, String> postParametersMap2 = new HashMap<String, String>();
        postParametersMap1.put("Address_id", addrObj.getString("Addr_id"));
        postParametersMap1.put("Customer_id", customerID);
        postParametersMap1.put("Type_id", addrObj.getString("Addr_type"));
        postParametersMap1.put("isPrimary", addrObj.getString("isPrimary"));
        postParametersMap2.put("addressLine1", addrObj.getString("addrLine1"));
        postParametersMap2.put("addressLine2", addrObj.getString("addrLine2"));
        postParametersMap2.put("zipCode", addrObj.getString("ZipCode"));
        postParametersMap2.put("City_id", addrObj.getString("City_id"));
        postParametersMap2.put("Region_id", addrObj.getString("Region_id"));
        postParametersMap2.put("id", addrObj.getString("Addr_id"));
        postParametersMap1.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
        postParametersMap2.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

        String updateEndpointResponse = Executor.invokeService(ServiceURLEnum.ADDRESS_UPDATE, postParametersMap2, null,
                requestInstance);
        if (CommonUtilities.getStringAsJSONObject(updateEndpointResponse).getInt(FabricConstants.OPSTATUS) != 0) {
            return CommonUtilities.getStringAsJSONObject(updateEndpointResponse);
        }
        String updateCustomerAddressRes = Executor.invokeService(ServiceURLEnum.CUSTOMERADDRESS_UPDATE,
                postParametersMap1, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(updateCustomerAddressRes);

    }

    private JSONObject createPhoneNumbers(String authToken, JSONObject contactNum, String customerID,
            DataControllerRequest requestInstance) throws ApplicationException {
        String contactValue = contactNum.optString("value");

        // Reset primary contact numbers.
        if (requestInstance.getAttribute("isServiceBeingAccessedByOLB") != null
                && Boolean.parseBoolean(requestInstance.getAttribute("isServiceBeingAccessedByOLB").toString())) {
        	contactValue = StringUtils.join(contactNum.optString("phoneCountryCode"),
                    CustomerGetContactInfo.CONTACT_VALUE_SEPERATOR, contactNum.optString("phoneNumber"),
                    CustomerGetContactInfo.CONTACT_VALUE_SEPERATOR, contactNum.optString("phoneExtension"));
            contactValue = contactValue.replace("--", "-").replaceAll("-$", "").replaceAll("^-", "");
            if (checkForDuplicateContactNumbers(contactValue, contactNum.optString("id"), customerID, requestInstance)) {
                throw new ApplicationException(ErrorCodeEnum.ERR_21585);
            }
            if (contactNum.getString("isPrimary").equalsIgnoreCase("1")) {
                resetAllPrimary(requestInstance, authToken, customerID, PHONE_TYPE);
            }
        }
        Map<String, String> postParametersMap = new HashMap<String, String>();
        String id = String.valueOf(CommonUtilities.getNewId());
        postParametersMap.put("Customer_id", customerID);
        postParametersMap.put("id", id);
        postParametersMap.put("Type_id", PHONE_TYPE);
        postParametersMap.put("isPrimary", contactNum.getString("isPrimary"));
        postParametersMap.put("Value", contactValue);
        if (!contactNum.optString("Extension").equals("")) {
            postParametersMap.put("Extension", contactNum.optString("Extension"));
        }
        String updateEndpointResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERCOMMUNICATION_CREATE,
                postParametersMap, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(updateEndpointResponse);
    }

    private JSONObject updatePhoneNumbers(String authToken, String customerID, JSONObject contactNum,
            DataControllerRequest requestInstance) throws ApplicationException {

        String contactValue = contactNum.optString("value");
        // Reset primary contact numbers.
        if (requestInstance.getAttribute("isServiceBeingAccessedByOLB") != null
                && Boolean.parseBoolean(requestInstance.getAttribute("isServiceBeingAccessedByOLB").toString())) {
            
	    	contactValue = StringUtils.join(contactNum.optString("phoneCountryCode"),
	                 CustomerGetContactInfo.CONTACT_VALUE_SEPERATOR, contactNum.optString("phoneNumber"),
	                 CustomerGetContactInfo.CONTACT_VALUE_SEPERATOR, contactNum.optString("phoneExtension"));
	        contactValue = contactValue.replace("--", "-").replaceAll("-$", "").replaceAll("^-", "");
             
        	if (checkForDuplicateContactNumbers(contactValue, contactNum.optString("id"), customerID, requestInstance)) {
                throw new ApplicationException(ErrorCodeEnum.ERR_21585);
            }
            if (contactNum.getString("isPrimary").equalsIgnoreCase("1")) {
                resetAllPrimary(requestInstance, authToken, customerID, PHONE_TYPE);
            }
        }

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("id", contactNum.getString("id"));
        postParametersMap.put("Customer_id", customerID);
        postParametersMap.put("Type_id", PHONE_TYPE);
        postParametersMap.put("isPrimary", contactNum.getString("isPrimary"));
        postParametersMap.put("Value", contactValue);
        if (!contactNum.optString("Extension").equals("")) {
            postParametersMap.put("Extension", contactNum.optString("Extension"));
        }
        postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

        String updateEndpointResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERCOMMUNICATION_UPDATE,
                postParametersMap, null, requestInstance);

        return CommonUtilities.getStringAsJSONObject(updateEndpointResponse);

    }

    private JSONObject createEmailIds(String authToken, JSONObject contactEmail, String customerID,
            DataControllerRequest requestInstance) {
        // Reset primary emails.
        if (requestInstance.getAttribute("isServiceBeingAccessedByOLB") != null
                && Boolean.parseBoolean(requestInstance.getAttribute("isServiceBeingAccessedByOLB").toString())) {
            if (contactEmail.getString("isPrimary").equalsIgnoreCase("1")) {
                resetAllPrimary(requestInstance, authToken, customerID, EMAIL_TYPE);
            }
        }

        Map<String, String> postParametersMap = new HashMap<String, String>();
        String id = String.valueOf(CommonUtilities.getNewId());
        postParametersMap.put("Customer_id", customerID);
        postParametersMap.put("id", id);
        postParametersMap.put("Type_id", EMAIL_TYPE);
        postParametersMap.put("isPrimary", contactEmail.getString("isPrimary"));
        postParametersMap.put("Value", contactEmail.getString("value"));
        if (!contactEmail.optString("Extension").equals("")) {
            postParametersMap.put("Extension", contactEmail.optString("Extension"));
        }
        String updateEndpointResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERCOMMUNICATION_CREATE,
                postParametersMap, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(updateEndpointResponse);
    }

    private JSONObject updateEmailIds(String authToken, String customerID, JSONObject contactEmail,
            DataControllerRequest requestInstance) {

        // Reset primary emails.
        if (requestInstance.getAttribute("isServiceBeingAccessedByOLB") != null
                && Boolean.parseBoolean(requestInstance.getAttribute("isServiceBeingAccessedByOLB").toString())) {
            if (contactEmail.getString("isPrimary").equalsIgnoreCase("1")) {
                resetAllPrimary(requestInstance, authToken, customerID, EMAIL_TYPE);
            }
        }

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("id", contactEmail.getString("id"));
        postParametersMap.put("Customer_id", customerID);
        postParametersMap.put("Type_id", EMAIL_TYPE);
        postParametersMap.put("isPrimary", contactEmail.getString("isPrimary"));
        postParametersMap.put("Value", contactEmail.getString("value"));
        if (!contactEmail.optString("Extension").equals("")) {
            postParametersMap.put("Extension", contactEmail.optString("Extension"));
        }
        postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

        String updateEndpointResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERCOMMUNICATION_UPDATE,
                postParametersMap, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(updateEndpointResponse);

    }

    private void resetAllPrimary(DataControllerRequest requestInstance, String authToken, String customerID,
            String type) {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.SELECT, "id");
        postParametersMap.put(ODataQueryConstants.FILTER,
                "Customer_id eq '" + customerID + "' and isPrimary eq true and Type_id eq '" + type + "'");

        JSONObject readEndpointResponse = CommonUtilities.getStringAsJSONObject(Executor
                .invokeService(ServiceURLEnum.CUSTOMERCOMMUNICATION_READ, postParametersMap, null, requestInstance));

        if (readEndpointResponse == null || (!readEndpointResponse.has(FabricConstants.OPSTATUS))
                || readEndpointResponse.getInt(FabricConstants.OPSTATUS) != 0)
            return;
        JSONArray communicationIds = readEndpointResponse.getJSONArray("customercommunication");
        for (int counter = 0; counter < communicationIds.length(); counter++) {
            Map<String, String> updatePostParametersMap = new HashMap<String, String>();
            updatePostParametersMap.put("id", communicationIds.getJSONObject(counter).getString("id"));
            updatePostParametersMap.put("isPrimary", "0");

            Executor.invokeService(ServiceURLEnum.CUSTOMERCOMMUNICATION_UPDATE, updatePostParametersMap, null,
                    requestInstance);
        }
    }

    private void resetAllPrimaryAddresses(DataControllerRequest requestInstance, String authToken, String customerID) {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.SELECT, "Address_id");
        postParametersMap.put(ODataQueryConstants.FILTER, "Customer_id eq '" + customerID + "' and isPrimary eq true");

        JSONObject readEndpointResponse = CommonUtilities.getStringAsJSONObject(
                Executor.invokeService(ServiceURLEnum.CUSTOMERADDRESS_READ, postParametersMap, null, requestInstance));

        if (readEndpointResponse == null || (!readEndpointResponse.has(FabricConstants.OPSTATUS))
                || readEndpointResponse.getInt(FabricConstants.OPSTATUS) != 0)
            return;
        JSONArray communicationIds = readEndpointResponse.getJSONArray("customeraddress");
        for (int counter = 0; counter < communicationIds.length(); counter++) {
            Map<String, String> updatePostParametersMap = new HashMap<String, String>();
            updatePostParametersMap.put("Customer_id", customerID);
            updatePostParametersMap.put("Address_id", communicationIds.getJSONObject(counter).getString("Address_id"));
            updatePostParametersMap.put("isPrimary", "0");

            Executor.invokeService(ServiceURLEnum.CUSTOMERADDRESS_UPDATE, updatePostParametersMap, null,
                    requestInstance);
        }
    }

}