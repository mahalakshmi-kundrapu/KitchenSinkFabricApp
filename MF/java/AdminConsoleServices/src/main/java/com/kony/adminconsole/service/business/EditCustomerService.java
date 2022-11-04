package com.kony.adminconsole.service.business;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
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

public class EditCustomerService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CreateCustomerService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        try {
            if (requestInstance.getParameter("id") == null) {
                ErrorCodeEnum.ERR_20688.setErrorCode(result);
                return result;
            } else if (requestInstance.getParameter("Email") == null) {
                ErrorCodeEnum.ERR_20871.setErrorCode(result);
                return result;
            } else if (requestInstance.getParameter("Ssn") == null) {
                ErrorCodeEnum.ERR_21009.setErrorCode(result);
                return result;
            } else if (requestInstance.getParameter("Phone") == null) {
                ErrorCodeEnum.ERR_21010.setErrorCode(result);
                return result;
            } else if (requestInstance.getParameter("FirstName") == null) {
                ErrorCodeEnum.ERR_20869.setErrorCode(result);
                return result;
            } else if (requestInstance.getParameter("LastName") == null) {
                ErrorCodeEnum.ERR_20870.setErrorCode(result);
                return result;
            } else if (requestInstance.getParameter("DateOfBirth") == null) {
                ErrorCodeEnum.ERR_20878.setErrorCode(result);
                return result;
            } else if (requestInstance.getParameter("UserName") == null) {
                ErrorCodeEnum.ERR_20705.setErrorCode(result);
                return result;
            } else if (requestInstance.getParameter("accounts") == null) {
                ErrorCodeEnum.ERR_21008.setErrorCode(result);
                return result;
            } else if (requestInstance.getParameter("Role_id") == null) {
                ErrorCodeEnum.ERR_20521.setErrorCode(result);
                return result;
            } else {
                String id = requestInstance.getParameter("id");
                String Email = requestInstance.getParameter("Email");
                String Ssn = requestInstance.getParameter("Ssn");
                String Phone = requestInstance.getParameter("Phone");
                String FirstName = requestInstance.getParameter("FirstName");
                String LastName = requestInstance.getParameter("LastName");
                String DateOfBirth = requestInstance.getParameter("DateOfBirth");
                String UserName = requestInstance.getParameter("UserName");
                String accounts = requestInstance.getParameter("accounts");
                String Role_id = requestInstance.getParameter("Role_id");
                String MiddleName = null;
                String riskStatus = null;
                if (StringUtils.isNotBlank(requestInstance.getParameter("RiskStatus"))) {
                    riskStatus = requestInstance.getParameter("RiskStatus");
                }

                String isEagreementSigned = requestInstance.getParameter("isEagreementSigned");

                if (requestInstance.getParameter("MiddleName") != null) {
                    MiddleName = requestInstance.getParameter("MiddleName");
                }
                String DrivingLicenseNumber = null;
                if (requestInstance.getParameter("DrivingLicenseNumber") != null) {
                    DrivingLicenseNumber = requestInstance.getParameter("DrivingLicenseNumber");
                }
                String services = null;
                if (requestInstance.getParameter("services") != null) {
                    services = requestInstance.getParameter("services");
                }
                JSONObject editCustomerresponse = DBPServices.editCustomer(id, Email, Ssn, Phone, FirstName, LastName,
                        DateOfBirth, UserName, accounts, Role_id, MiddleName, DrivingLicenseNumber, services,
                        riskStatus, isEagreementSigned, requestInstance);
                if (editCustomerresponse.has("errorMessage")) {
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    result.addParam(new Param("errMsg", editCustomerresponse.getString("errorMessage"),
                            FabricConstants.STRING));
                    return result;
                } else {
                    if (editCustomerresponse == null || !editCustomerresponse.has(FabricConstants.OPSTATUS)
                            || editCustomerresponse.getInt(FabricConstants.OPSTATUS) != 0) {
                        ErrorCodeEnum.ERR_21014.setErrorCode(result);
                        result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.COMPANY, EventEnum.UPDATE,
                                ActivityStatusEnum.FAILED, "Edit Failed for the customer:" + id);
                        return result;
                    } else {
                        result.addParam(new Param("status", "Success", FabricConstants.STRING));
                        result.addParam(new Param("opstatus", editCustomerresponse.get("opstatus").toString(),
                                FabricConstants.STRING));
                        result.addParam(new Param("msg", editCustomerresponse.get("success").toString(),
                                FabricConstants.STRING));
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.COMPANY, EventEnum.UPDATE,
                                ActivityStatusEnum.SUCCESSFUL, "Edit Successful for the customer:" + id);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Unexepected Error in edit Customer ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.COMPANY, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Edit Failed for the customer:" + requestInstance.getParameter("id"));
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
        }
        return result;
    }
}