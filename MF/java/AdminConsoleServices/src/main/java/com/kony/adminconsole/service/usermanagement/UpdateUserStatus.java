package com.kony.adminconsole.service.usermanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.handler.EmailHandler;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
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
import com.konylabs.middleware.dataobject.Result;

public class UpdateUserStatus implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(UpdateUserStatus.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result processedResult = new Result();
            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            String userID = requestInstance.getParameter("User_id");
            String statusID = requestInstance.getParameter("Status_id");

            String username = null;
            try {
                JSONObject readInternalUser = readInternalUser(userID, requestInstance);
                username = (readInternalUser.getJSONArray("internalusers_view")).getJSONObject(0).getString("Username");
            } catch (Exception ignored) {
            }

            // change status
            JSONObject updateInternalUser = updateInternalUser(userID, statusID, requestInstance);
            if (updateInternalUser.getInt(FabricConstants.OPSTATUS) != 0) {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.USERS, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED,
                        "User status change failed. Username: " + username + " Status: " + statusID);
                return ErrorCodeEnum.ERR_20545.setErrorCode(processedResult);
            }

            // audit action
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.USERS, EventEnum.UPDATE,
                    ActivityStatusEnum.SUCCESSFUL,
                    "User status changed successfully. Username: " + username + " Status: " + statusID);

            // get details of user
            JSONObject readUserDetails = readUserDetails(userID, requestInstance);
            JSONArray array = readUserDetails.optJSONArray("internaluserdetails_view");
            JSONObject UserDetails = null;
            if (array != null && array.length() > 0) {
                UserDetails = array.optJSONObject(0);
            }
            // Send status change email
            String subject = "Status changed";
            String status = statusID.equalsIgnoreCase(StatusEnum.SID_ACTIVE.name()) ? "Activated"
                    : (statusID.equalsIgnoreCase(StatusEnum.SID_SUSPENDED.name()) ? "Suspended" : "Deactivated");
            String recipientEmailId = UserDetails.getString("Email");
            String emailType = "InternalUserStatusChange";
            JSONObject additionalContext = new JSONObject();
            additionalContext.put("name", UserDetails.getString("FirstName"));
            additionalContext.put("username", UserDetails.getString("Username"));
            additionalContext.put("status", status);
            additionalContext.put("InternalUser_id", userID);
            JSONObject eamilres = EmailHandler.invokeSendEmailObjectService(requestInstance, authToken,
                    recipientEmailId, null, subject, emailType, additionalContext);
            Param emailStatus;
            if (eamilres.getInt(FabricConstants.OPSTATUS) != 0) {
                emailStatus = new Param("EmailStatus", eamilres.toString(), FabricConstants.STRING);
            } else {
                emailStatus = new Param("EmailStatus", "Success", FabricConstants.STRING);
            }

            Param statusParam = new Param("Status", "status changed", FabricConstants.STRING);
            processedResult.addParam(statusParam);
            processedResult.addParam(emailStatus);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    public JSONObject readInternalUser(String UserID, DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "User_id eq '" + UserID + "'");
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.INTERNALUSERS_VIEW_READ, postParametersMap,
                null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

    public JSONObject updateInternalUser(String UserID, String StatusID, DataControllerRequest requestInstance) {
        Map<String, String> headerParametersMap = new HashMap<String, String>();

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("id", UserID);
        postParametersMap.put("Status_id", StatusID);
        postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

        String updateEndpointResponse = Executor.invokeService(ServiceURLEnum.SYSTEMUSER_UPDATE, postParametersMap,
                headerParametersMap, requestInstance);
        return CommonUtilities.getStringAsJSONObject(updateEndpointResponse);

    }

    public JSONObject readUserDetails(String UserID, DataControllerRequest requestInstance) {
        Map<String, String> headerParametersMap = new HashMap<String, String>();

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "id eq '" + UserID + "'");
        postParametersMap.put(ODataQueryConstants.SELECT, "id,Status_id,Username,Email,FirstName,MiddleName,LastName");
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.INTERNALUSERDETAILS_VIEW_READ,
                postParametersMap, headerParametersMap, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);
    }

}