package com.kony.adminconsole.service.staticcontentmanagement.securityimages;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to Delete Security Images
 *
 * @author Aditya Mankal
 * 
 */
public class SecurityImagesDeleteService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(SecurityImagesDeleteService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result processedResult = new Result();
            Map<String, String> postParametersMap = new HashMap<String, String>();

            String imageID = requestInstance.getParameter("id");
            postParametersMap.put("id", imageID);
            String deleteSecurityImageResponse = Executor.invokeService(ServiceURLEnum.SECURITYIMAGE_DELETE,
                    postParametersMap, null, requestInstance);
            JSONObject deleteSecurityImageResponseJSON = CommonUtilities
                    .getStringAsJSONObject(deleteSecurityImageResponse);
            int opStatusCode = deleteSecurityImageResponseJSON.getInt(FabricConstants.OPSTATUS);
            if (opStatusCode == 0) {

                if (deleteSecurityImageResponseJSON.has("deletedRecords")) {
                    Param deletedRecords_Param = new Param("deletedRecords",
                            Integer.toString(deleteSecurityImageResponseJSON.getInt("deletedRecords")),
                            FabricConstants.INT);
                    processedResult.addParam(deletedRecords_Param);
                    /*
                     * if (deleteSecurityImageResponseJSON.getInt("deletedRecords") >= 1)
                     * AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.SECURITYIMAGES, EventEnum.DELETE,
                     * ActivityStatusEnum.SUCCESSFUL, "Secure images delete successful.");
                     */
                }
                return processedResult;
            }
            /*
             * AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.SECURITYIMAGES, EventEnum.DELETE,
             * ActivityStatusEnum.FAILED, "Secure images delete failed.");
             */
            ErrorCodeEnum.ERR_20223.setErrorCode(processedResult);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

}