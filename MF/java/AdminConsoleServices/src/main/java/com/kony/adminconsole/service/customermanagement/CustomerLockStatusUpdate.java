package com.kony.adminconsole.service.customermanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.DBPServices;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.EventEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ModuleNameEnum;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.kony.adminconsole.utilities.StatusEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * CustomerLockStatusUpdate service will update lock status for a customer
 * 
 * @author Alahari Prudhvi Akhil (KH2346)
 * 
 */
public class CustomerLockStatusUpdate implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(CustomerLockStatusUpdate.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result processedResult = new Result();
        try {

            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            String customerUsername = requestInstance.getParameter("customerUsername");
            String customerID = requestInstance.getParameter("customerID");

            // Check mandatory fields
            if (customerUsername == null) {
                ErrorCodeEnum.ERR_20653.setErrorCode(processedResult);
                return processedResult;

            }

            if (customerID == null) {
                JSONObject response = getCustomerBasicInfo(authToken, customerUsername, requestInstance);

                if (response != null && response.has(FabricConstants.OPSTATUS)
                        && response.getInt(FabricConstants.OPSTATUS) == 0) {
                    customerID = response.getJSONArray("customerbasicinfo_view").getJSONObject(0)
                            .getString("Customer_id");
                } else {
                    ErrorCodeEnum.ERR_20652.setErrorCode(processedResult);
                    return processedResult;

                }
            }

            // Update customer status at core-banking
            JSONObject coreBankingResponse = DBPServices.updateLockStatus(authToken, customerUsername, requestInstance);
            if (coreBankingResponse != null && coreBankingResponse.has(FabricConstants.OPSTATUS)
                    && coreBankingResponse.getInt(FabricConstants.OPSTATUS) == 0) {

                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                        ActivityStatusEnum.SUCCESSFUL, "Update lock status successful. username: " + customerUsername
                                + " Status: " + coreBankingResponse.getString("success"));
                Record corebankingUpdate = new Record();
                corebankingUpdate.setId("CoreBankingUpdate");
                Param statusParam = new Param("Status", coreBankingResponse.getString("success"),
                        FabricConstants.STRING);
                Param responseMsg = new Param("responseMsg", coreBankingResponse.toString(), FabricConstants.STRING);

                corebankingUpdate.addParam(statusParam);
                corebankingUpdate.addParam(responseMsg);
                processedResult.addRecord(corebankingUpdate);

            } else {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                        ActivityStatusEnum.SUCCESSFUL, "Update lock status failed. username: " + customerUsername);
                ErrorCodeEnum.ERR_20651.setErrorCode(processedResult);
                return processedResult;

            }

            // Update customer status at AdminConsole.
            JSONObject customerUpdateResponse = updateLockStatusAtadminConsole(authToken, customerID,
                    coreBankingResponse.getString("success"), requestInstance);
            if (customerUpdateResponse != null && customerUpdateResponse.has(FabricConstants.OPSTATUS)
                    && customerUpdateResponse.getInt(FabricConstants.OPSTATUS) == 0) {

                Record adminConsoleUpdate = new Record();
                adminConsoleUpdate.setId("AdminConsoleUpdate");
                Param statusParam = new Param("Status", "SUCCESS", FabricConstants.STRING);
                Param responseMsg = new Param("responseMsg", customerUpdateResponse.toString(), FabricConstants.STRING);

                adminConsoleUpdate.addParam(statusParam);
                adminConsoleUpdate.addParam(responseMsg);
                processedResult.addRecord(adminConsoleUpdate);

            } else {
                ErrorCodeEnum.ERR_20650.setErrorCode(processedResult);
                return processedResult;

            }

            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    public JSONObject updateLockStatusAtadminConsole(String authToken, String customerID, String corebankingStatus,
            DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("id", customerID);
        if (corebankingStatus.equalsIgnoreCase("USER UNLOCKED")) {
            postParametersMap.put("Status_id", StatusEnum.SID_CUS_ACTIVE.name());
        } else {
            postParametersMap.put("Status_id", StatusEnum.SID_CUS_LOCKED.name());
        }

        // Update call
        String updateEndpointResponse = Executor.invokeService(ServiceURLEnum.CUSTOMER_UPDATE, postParametersMap, null,
                requestInstance);
        return CommonUtilities.getStringAsJSONObject(updateEndpointResponse);

    }

    public JSONObject getCustomerBasicInfo(String authToken, String customerUsername,
            DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "Username eq " + customerUsername);

        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERBASICINFO_VIEW_READ,
                postParametersMap, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

}