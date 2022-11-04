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
 * CustomerEnroll service used to send enrollment link to a customer
 * 
 * @author Alahari Prudhvi Akhil (KH2346)
 * 
 */
public class CustomerEnroll implements JavaService2 {

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result processedResult = new Result();
        String customerID = requestInstance.getParameter("Customer_id");
        String customerEmail = requestInstance.getParameter("Customer_Email");
        String customerUsername = requestInstance.getParameter("Customer_username");
        if (StringUtils.isBlank(customerID)) {
            ErrorCodeEnum.ERR_20565.setErrorCode(processedResult);
            return processedResult;
        }
        if (StringUtils.isBlank(customerUsername)) {
            ErrorCodeEnum.ERR_20533.setErrorCode(processedResult);
            return processedResult;
        }
        if (StringUtils.isBlank(customerEmail)) {
            ErrorCodeEnum.ERR_20619.setErrorCode(processedResult);
            return processedResult;
        }

        try {

            JSONObject emailres = DBPServices.enrollDBXUser(customerUsername, customerEmail, requestInstance);
            if (emailres == null || !emailres.has(FabricConstants.OPSTATUS)
                    || emailres.getInt(FabricConstants.OPSTATUS) != 0) {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.COMMUNICATION,
                        ActivityStatusEnum.FAILED,
                        "Send enrolment link to customer failed. Username: " + customerUsername);
                processedResult.addParam(new Param("FailureReason", String.valueOf(emailres), FabricConstants.STRING));
                ErrorCodeEnum.ERR_20540.setErrorCode(processedResult);
                return processedResult;
            } else {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.COMMUNICATION,
                        ActivityStatusEnum.SUCCESSFUL,
                        "Send enrolment link to customer successful. Username: " + customerUsername);
            }
            processedResult.addParam(new Param("Status", "Operation successful", FabricConstants.STRING));
            return processedResult;
        } catch (Exception exp) {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.COMMUNICATION,
                    ActivityStatusEnum.FAILED, "Send enrolment link to customer failed. Username: " + customerUsername);
            processedResult.addParam(new Param("FailureReason", exp.getMessage(), FabricConstants.STRING));
            ErrorCodeEnum.ERR_20659.setErrorCode(processedResult);
            return processedResult;

        }

    }

}