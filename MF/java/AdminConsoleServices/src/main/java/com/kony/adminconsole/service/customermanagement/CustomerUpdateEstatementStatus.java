package com.kony.adminconsole.service.customermanagement;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.FabricConstants;
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
 * CustomerUpdateEstatementStatus service will update the e-statement status of a customer account (whether true or
 * false) A false e-Statement status indicates that the user wishes to get his account statement via paper
 * 
 * @author Mohit Khosla
 * 
 */
public class CustomerUpdateEstatementStatus implements JavaService2 {

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result processedResult = new Result();

        try {

            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);

            String customerID = requestInstance.getParameter("customerID");
            String accountID = requestInstance.getParameter("accountID");
            String eStatementStatus = requestInstance.getParameter("eStatementStatus");
            String eStatementEmail = requestInstance.getParameter("eStatementEmail");

            // Call OLB service to update e-Statement status
            JSONObject eStatementStatusJSON = DBPServices.updateEstatementStatus(authToken, accountID, eStatementStatus,
                    eStatementEmail, requestInstance);

            if (StringUtils.isNotBlank(eStatementStatusJSON.optString("success"))
                    && eStatementStatusJSON.getString("success").equals("success")) {
                String status = eStatementStatusJSON.getString("success");
                processedResult.addParam(new Param("eStatementUpdateStatus", status, FabricConstants.STRING));
                processedResult.addParam(
                        new Param("UpdatedBy", eStatementStatusJSON.optString("UpdatedBy"), FabricConstants.STRING));
                processedResult.addParam(new Param("LastUpdated", eStatementStatusJSON.optString("LastUpdated"),
                        FabricConstants.STRING));

                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                        ActivityStatusEnum.SUCCESSFUL,
                        "Customer e-Statement status update successful. Customer id: " + customerID);
            } else {
                String status = eStatementStatusJSON.optString("success");
                if (StringUtils.isNotBlank(status)) {
                    processedResult.addParam(new Param("eStatementUpdateStatus", status, FabricConstants.STRING));
                }

                ErrorCodeEnum.ERR_20861.setErrorCode(processedResult);

                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED,
                        "Customer e-Statement status update failed. Customer id: " + customerID);
            }
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(processedResult);
        }

        return processedResult;
    }
}