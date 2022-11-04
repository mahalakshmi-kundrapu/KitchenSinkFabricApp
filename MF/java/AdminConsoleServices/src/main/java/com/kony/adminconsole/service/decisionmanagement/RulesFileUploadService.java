package com.kony.adminconsole.service.decisionmanagement;
/*
* 
* @author Sai Krishna Aitha
*
*/

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.utilities.DBPServices;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class RulesFileUploadService implements JavaService2 {
    public static final Logger LOG = Logger.getLogger(RulesFileUploadService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result result = new Result();
        try {
            // Upload Decision Rule File
            JSONObject uploadRuleFileResponse = DBPServices.uploadRuleFile(requestInstance);
            if (uploadRuleFileResponse == null || !uploadRuleFileResponse.has(FabricConstants.OPSTATUS)
                    || uploadRuleFileResponse.getInt(FabricConstants.OPSTATUS) != 0) {
                ErrorCodeEnum.ERR_21578.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                /*
                 * AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.DECISIONMANAGEMENT, EventEnum.UPDATE,
                 * ActivityStatusEnum.FAILED, "File upload failed");
                 */
                return result;
            }
            result.addParam(new Param("status", uploadRuleFileResponse.optString("success"), FabricConstants.STRING));
            LOG.debug("File Upload Status:" + uploadRuleFileResponse.optString("success"));
            /*
             * AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.DECISIONMANAGEMENT, EventEnum.UPDATE,
             * ActivityStatusEnum.SUCCESSFUL, "File succesfully uploaded");
             */
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
        return null;
    }

}
