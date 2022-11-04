package com.kony.adminconsole.service.customermanagement;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.exception.DBPAuthenticationException;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.DBPServices;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.EventEnum;
import com.kony.adminconsole.utilities.ModuleNameEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * CustomerUpdateDBPStatus service will update DBP user status
 * 
 * @author Alahari Prudhvi Akhil (KH2346)
 * 
 */
public class CustomerUpdateDBPStatus implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CustomerUpdateDBPStatus.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result processedResult = new Result();
        String customerUsername = requestInstance.getParameter("customerUsername");
        String status = requestInstance.getParameter("status");
        try {

            JSONObject endpointResponse = DBPServices.updateDBPUserStatus(customerUsername, status, requestInstance);
            if (endpointResponse != null && endpointResponse.has(FabricConstants.OPSTATUS)
                    && endpointResponse.getInt(FabricConstants.OPSTATUS) == 0) {
                processedResult.addParam(new Param("status", endpointResponse.getString("Status")));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                        ActivityStatusEnum.SUCCESSFUL, "Status of the customer updated succesfully. Status: " + status
                                + " Customer username: " + customerUsername);
            } else {
                ErrorCodeEnum.ERR_20531.setErrorCode(processedResult);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED, "Update DBP status of the customer Failed.  Status: " + status
                                + " Customer username: " + customerUsername);
                return processedResult;
            }
        } catch (DBPAuthenticationException dbpException) {
            LOG.error("DBP login failed. " + dbpException.getMessage());
            ErrorCodeEnum.ERR_20933.setErrorCode(processedResult);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Update DBP status of the customer Failed. Status: " + status
                            + " Customer username: " + customerUsername);
            processedResult.addParam(new Param("FailureReason", dbpException.getMessage(), FabricConstants.STRING));
        } catch (Exception e) {
            LOG.error("Unexpected error has occurred. " + e.getMessage());
            ErrorCodeEnum.ERR_20691.setErrorCode(processedResult);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Update DBP status of the customer Failed. Status: " + status
                            + " Customer username: " + customerUsername);
            processedResult.addParam(new Param("FailureReason", e.getMessage(), FabricConstants.STRING));
        }
        return processedResult;
    }

}