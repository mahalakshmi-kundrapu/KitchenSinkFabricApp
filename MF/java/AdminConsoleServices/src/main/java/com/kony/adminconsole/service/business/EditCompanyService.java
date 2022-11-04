package com.kony.adminconsole.service.business;

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

public class EditCompanyService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(EditCompanyService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        try {
            if (requestInstance.getParameter("id") == null) {
                ErrorCodeEnum.ERR_21011.setErrorCode(result);
                return result;
            } else if (requestInstance.getParameter("Type") == null) {
                ErrorCodeEnum.ERR_21004.setErrorCode(result);
                return result;
            } else if (requestInstance.getParameter("Name") == null) {
                ErrorCodeEnum.ERR_21005.setErrorCode(result);
                return result;
            } else if (requestInstance.getParameter("Communication") == null) {
                ErrorCodeEnum.ERR_21006.setErrorCode(result);
                return result;
            } else if (requestInstance.getParameter("Address") == null) {
                ErrorCodeEnum.ERR_21007.setErrorCode(result);
                return result;
            } else if (requestInstance.getParameter("AccountsList") == null) {
                ErrorCodeEnum.ERR_21008.setErrorCode(result);
                return result;
            } else {
                String Id = requestInstance.getParameter("id");
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
                JSONObject editCompanyresponse = DBPServices.editCompany(Id, Type, Name, Description, Communication,
                        Address, Owner, AccountsList, requestInstance);
                if (editCompanyresponse.has("errorMessage")) {
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    result.addParam(
                            new Param("errMsg", editCompanyresponse.getString("errorMessage"), FabricConstants.STRING));
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.COMPANY, EventEnum.UPDATE,
                            ActivityStatusEnum.FAILED, "Company Edit Failed");
                    return result;
                } else {
                    if (editCompanyresponse == null || !editCompanyresponse.has(FabricConstants.OPSTATUS)
                            || editCompanyresponse.getInt(FabricConstants.OPSTATUS) != 0) {
                        ErrorCodeEnum.ERR_21013.setErrorCode(result);
                        result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.COMPANY, EventEnum.UPDATE,
                                ActivityStatusEnum.FAILED, "Company Edit Failed");
                        return result;
                    } else {
                        result.addParam(new Param("status", "Success", FabricConstants.STRING));
                        result.addParam(new Param("opstatus", editCompanyresponse.get("opstatus").toString(),
                                FabricConstants.STRING));
                        result.addParam(new Param("message", editCompanyresponse.get("success").toString(),
                                FabricConstants.STRING));
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.COMPANY, EventEnum.UPDATE,
                                ActivityStatusEnum.SUCCESSFUL, "Company Edited Successfully");
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Unexepected Error in Edit CompanyService ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.COMPANY, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Company Edit Failed");
        }
        return result;
    }
}