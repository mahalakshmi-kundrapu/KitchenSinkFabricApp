package com.kony.adminconsole.service.customermanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.handler.AuditHandler;
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
 * CustomerCreateNote service is used to create a note for a customer
 * 
 * @author Alahari Prudhvi Akhil (KH2346)
 * 
 */
public class CustomerCreateNote implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(CustomerCreateNote.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        try {
            Result processedResult = new Result();
            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            String createdBy = null;

            String customerID = requestInstance.getParameter("Customer_id");
            // Not removing applicant id as inpur to retain backward compatibility
            String Applicant_id = requestInstance.getParameter("Applicant_id");
            String Note = requestInstance.getParameter("Note");
            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            if (userDetailsBeanInstance != null) {
                createdBy = userDetailsBeanInstance.getUserId();
            }

            if (StringUtils.isBlank(createdBy)) {
                ErrorCodeEnum.ERR_20748.setErrorCode(processedResult);
                processedResult.addParam(new Param("status", "failure", FabricConstants.STRING));
                return processedResult;
            }
            JSONObject createNoteJSON;

            if (StringUtils.isNotBlank(customerID)) {
                createNoteJSON = createNoteForCustomer(authToken, customerID, createdBy, Note, requestInstance);

            } else {
                createNoteJSON = createNoteForCustomer(authToken, Applicant_id, createdBy, Note, requestInstance);
            }

            if (createNoteJSON == null) {
                ErrorCodeEnum.ERR_20658.setErrorCode(processedResult);
                processedResult.addParam(new Param("status", "failure", FabricConstants.STRING));
                return processedResult;
            } else {
                processedResult.addParam(new Param("status", "success", FabricConstants.STRING));
            }

            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }

    }

    private JSONObject createNoteForCustomer(String authToken, String customerID, String createdby, String Note,
            DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        String id = String.valueOf(CommonUtilities.getNewId());
        postParametersMap.put("id", id);
        postParametersMap.put("Customer_id", customerID);
        postParametersMap.put("createdby", createdby);
        postParametersMap.put("Note", Note);
        String craeteEndpointResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERNOTE_CREATE, postParametersMap,
                null, requestInstance);

        JSONObject response = CommonUtilities.getStringAsJSONObject(craeteEndpointResponse);

        if (response == null || !response.has(FabricConstants.OPSTATUS)
                || response.getInt(FabricConstants.OPSTATUS) != 0) {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.CREATE,
                    ActivityStatusEnum.FAILED,
                    "Customer note create failed. Note: " + Note + ". Customer id:" + customerID);
            return null;
        }
        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.CREATE,
                ActivityStatusEnum.SUCCESSFUL,
                "Customer note create successful. Note: " + Note + ". Customer id:" + customerID);

        return response;
    }

}