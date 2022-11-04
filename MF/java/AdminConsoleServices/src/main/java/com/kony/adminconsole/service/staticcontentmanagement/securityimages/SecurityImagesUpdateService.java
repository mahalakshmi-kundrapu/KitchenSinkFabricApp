package com.kony.adminconsole.service.staticcontentmanagement.securityimages;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to Update(Activate/Deactivate) Security Images
 *
 * @author Aditya Mankal
 * 
 */
public class SecurityImagesUpdateService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(SecurityImagesUpdateService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result processedResult = new Result();

            Map<String, String> postParametersMap = new HashMap<String, String>();
            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            String userID = userDetailsBeanInstance.getUserId();

            String imageID = requestInstance.getParameter("id");
            String statusID = requestInstance.getParameter("status_ID");
            postParametersMap.put("id", imageID);
            postParametersMap.put("Status_id", statusID);
            postParametersMap.put("modifiedby", userID);
            postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
            String updateSecurityImageResponse = Executor.invokeService(ServiceURLEnum.SECURITYIMAGE_UPDATE,
                    postParametersMap, null, requestInstance);
            JSONObject updateSecurityImageResponseJSON = CommonUtilities
                    .getStringAsJSONObject(updateSecurityImageResponse);
            if (updateSecurityImageResponseJSON != null && updateSecurityImageResponseJSON.has(FabricConstants.OPSTATUS)
                    && updateSecurityImageResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {

                if (updateSecurityImageResponseJSON.has("updatedRecords")) {
                    Param updatedRecords_Param = new Param("updatedRecords",
                            Integer.toString(updateSecurityImageResponseJSON.getInt("updatedRecords")),
                            FabricConstants.INT);
                    processedResult.addParam(updatedRecords_Param);
                    if (updateSecurityImageResponseJSON.optInt("updatedRecords") >= 1) {
                        /*
                         * AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.SECURITYIMAGES,
                         * EventEnum.UPDATE, ActivityStatusEnum.SUCCESSFUL, "Secure images update success. imageID: " +
                         * imageID);
                         */
                    }
                }
                return processedResult;
            }
            /*
             * AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.SECURITYIMAGES, EventEnum.UPDATE,
             * ActivityStatusEnum.FAILED, "Secure images update failed. imageID: " + imageID);
             */
            ErrorCodeEnum.ERR_20222.setErrorCode(processedResult);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

}