package com.kony.adminconsole.service.customermanagement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.exception.DBPAuthenticationException;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.handler.CustomerHandler;
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
 * This service will send a reset password link to the end customer
 * 
 * @author Alahari Prudhvi Akhil (KH2346)
 * 
 */
public class CustomerResetPassword implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CustomerResetPassword.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result processedResult = new Result();
        String customerUsername = requestInstance.getParameter("customerUsername");
        if (StringUtils.isBlank(customerUsername)) {
            ErrorCodeEnum.ERR_20533.setErrorCode(processedResult);
            return processedResult;
        }
        String CustomerId = new CustomerHandler().getCustomerId(customerUsername, requestInstance);
        try {
            JSONObject primaryEmailsObject = CustomerHandler.getCustomerPrimaryEmailAddress(CustomerId,
                    requestInstance);
            String emailAddress;
            if (primaryEmailsObject == null || !primaryEmailsObject.has(FabricConstants.OPSTATUS)
                    || primaryEmailsObject.getInt(FabricConstants.OPSTATUS) != 0
                    || !primaryEmailsObject.has("customercommunication")) {
                ErrorCodeEnum.ERR_20691.setErrorCode(processedResult);
                processedResult.addParam(
                        new Param("FailureReason", String.valueOf(primaryEmailsObject), FabricConstants.STRING));
                return processedResult;
            }

            if (primaryEmailsObject.getJSONArray("customercommunication").length() == 0) {
                ErrorCodeEnum.ERR_20534.setErrorCode(processedResult);
                return processedResult;
            }
            emailAddress = primaryEmailsObject.getJSONArray("customercommunication").getJSONObject(0)
                    .getString("Value");
            JSONObject endpointResponse = DBPServices.resetDBPUserPassword(emailAddress, customerUsername,
                    requestInstance);
            if (endpointResponse != null && endpointResponse.has(FabricConstants.OPSTATUS)
                    && endpointResponse.getInt(FabricConstants.OPSTATUS) == 0 && endpointResponse.has("Status")) {
                processedResult.addParam(new Param("status", endpointResponse.getString("Status")));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                        ActivityStatusEnum.SUCCESSFUL, "Reset Password Successful for the customer:" + CustomerId);
            } else {
                ErrorCodeEnum.ERR_20532.setErrorCode(processedResult);
                processedResult
                        .addParam(new Param("FailureReason", String.valueOf(endpointResponse), FabricConstants.STRING));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED, "Reset Password Failed for the customer:" + CustomerId);
                return processedResult;
            }
        } catch (DBPAuthenticationException dbpException) {
            LOG.error("DBP login failed. " + dbpException.getMessage());
            ErrorCodeEnum.ERR_20933.setErrorCode(processedResult);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Reset Password Failed for the customer:" + CustomerId);
            processedResult.addParam(new Param("FailureReason", dbpException.getMessage(), FabricConstants.STRING));
        } catch (Exception e) {
            LOG.error("Unexpected error has occurred. " + e.getMessage());
            ErrorCodeEnum.ERR_20691.setErrorCode(processedResult);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Reset Password Failed for the customer:" + CustomerId);
            processedResult.addParam(new Param("FailureReason", e.getMessage(), FabricConstants.STRING));
        }
        return processedResult;
    }

}