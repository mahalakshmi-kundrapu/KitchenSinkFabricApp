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

public class CreateCompanyService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CreateCompanyService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        try {
            // Validate UserName
            if (requestInstance.getParameter("Type") == null) {
                ErrorCodeEnum.ERR_21004.setErrorCode(result);
                return result;
            } else if (requestInstance.getParameter("Name") == null) {
                ErrorCodeEnum.ERR_21005.setErrorCode(result);
                return result;
            } else if (StringUtils.isBlank(requestInstance.getParameter("Communication"))) {
                ErrorCodeEnum.ERR_21006.setErrorCode(result);
                return result;
            } else if (requestInstance.getParameter("Address") == null) {
                ErrorCodeEnum.ERR_21007.setErrorCode(result);
                return result;
            } else if (requestInstance.getParameter("AccountsList") == null) {
                ErrorCodeEnum.ERR_21008.setErrorCode(result);
                return result;
            } else {
                String Type = requestInstance.getParameter("Type");
                String Name = requestInstance.getParameter("Name");
                String Communication = requestInstance.getParameter("Communication");
                String Address = requestInstance.getParameter("Address");
                String AccountsList = requestInstance.getParameter("AccountsList");
                String Description = null;
                if (requestInstance.getParameter("Description") != null) {
                    Description = requestInstance.getParameter("Description");
                }
                String Owner = null;
                if (requestInstance.getParameter("Owner") != null) {
                    Owner = requestInstance.getParameter("Owner");
                }
                String Membership = null;
                if (requestInstance.getParameter("Membership") != null) {
                    Membership = requestInstance.getParameter("Membership");
                }
                JSONObject createCompanyresponse = DBPServices.createCompany(Type, Name, Description, Communication,
                        Address, Owner, Membership, AccountsList, requestInstance);
                if (createCompanyresponse.has("errorMessage")) {
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    result.addParam(new Param("errMsg", createCompanyresponse.getString("errorMessage"),
                            FabricConstants.STRING));
                    return result;
                } else {
                    if (createCompanyresponse == null || !createCompanyresponse.has(FabricConstants.OPSTATUS)
                            || createCompanyresponse.getInt(FabricConstants.OPSTATUS) != 0) {
                        ErrorCodeEnum.ERR_21012.setErrorCode(result);
                        result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.COMPANY, EventEnum.CREATE,
                                ActivityStatusEnum.FAILED, "Company Creation Failed");
                        return result;
                    } else {
                        result.addParam(new Param("status", "Success", FabricConstants.STRING));
                        result.addParam(new Param("opstatus", createCompanyresponse.get("opstatus").toString(),
                                FabricConstants.STRING));
                        result.addParam(new Param("id", createCompanyresponse.getString("id"), FabricConstants.STRING));
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.COMPANY, EventEnum.CREATE,
                                ActivityStatusEnum.SUCCESSFUL,
                                "Company Succefully Created:" + createCompanyresponse.getString("id"));
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Unexepected Error in create Company ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.COMPANY, EventEnum.CREATE,
                    ActivityStatusEnum.FAILED, "Company Creation Failed");
        }
        return result;
    }

}