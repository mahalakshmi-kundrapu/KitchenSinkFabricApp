package com.kony.adminconsole.service.staticcontentmanagement.securityimages;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.kony.adminconsole.utilities.StatusEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to insert Security Images in Base 64 Format
 * 
 * @author Aditya Mankal, Mohit Khosla
 * 
 * 
 */
public class SecurityImagesCreateService implements JavaService2 {

    private static final int MAX_IMAGE_FILE_SIZE = 75000;
    private static final Logger LOG = Logger.getLogger(SecurityImagesCreateService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            int successfulInserts = 0, failedInserts = 0;

            Map<String, String> postParametersMap = new HashMap<String, String>();
            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            String userID = userDetailsBeanInstance.getUserId();
            Result processedResult = new Result();

            JSONArray securityImagesJSONArray = new JSONArray(requestInstance.getParameter("SecurityImagesList"));
            Record imageUploadStatusRecord = new Record();
            imageUploadStatusRecord.setId("ImageUploadStatus");

            for (int indexVar = 0; indexVar < securityImagesJSONArray.length(); indexVar++) {
                postParametersMap.clear();
                try {
                    Param currImageUploadStatusParam = new Param();
                    currImageUploadStatusParam.setType(FabricConstants.STRING);
                    currImageUploadStatusParam.setName("Image_" + indexVar + "_Status");
                    JSONObject currImageJSONObject = securityImagesJSONArray.getJSONObject(indexVar);
                    if (!currImageJSONObject.has("ImageBase64String")) {
                        /*
                         * AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.SECURITYIMAGES,
                         * EventEnum.CREATE, ActivityStatusEnum.FAILED,
                         * "Secure images create failed.Missing Base 64 String.");
                         */
                        currImageUploadStatusParam.setValue("ERROR:Missing Base 64 String.");
                        imageUploadStatusRecord.addParam(currImageUploadStatusParam);
                        continue;
                    }
                    String currImageBase64String = currImageJSONObject.getString("ImageBase64String");
                    currImageBase64String = currImageBase64String.replaceAll("\\s", "+");
                    String currImageFormat = currImageBase64String.substring(currImageBase64String.indexOf("/") + 1,
                            currImageBase64String.indexOf(";base64"));
                    // Ensuring that the image is of the correct format
                    currImageFormat = currImageFormat.toLowerCase();
                    if (!(currImageFormat.equals("jpeg") || currImageFormat.equals("jpg")
                            || currImageFormat.equals("png") || currImageFormat.equals("img")
                            || currImageFormat.equals("tiff"))) {
                        // Incorrect Image format
                        /*
                         * AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.SECURITYIMAGES,
                         * EventEnum.CREATE, ActivityStatusEnum.FAILED,
                         * "Secure images create failed. Incorrect Image format");
                         */
                        currImageUploadStatusParam
                                .setValue("ERROR:." + currImageFormat + " is not a valid image format.");
                        imageUploadStatusRecord.addParam(currImageUploadStatusParam);
                        continue;
                    }
                    if (currImageBase64String.length() > MAX_IMAGE_FILE_SIZE) {
                        // Incorrect image size
                        /*
                         * AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.SECURITYIMAGES,
                         * EventEnum.CREATE, ActivityStatusEnum.FAILED,
                         * "Secure images create failed. Incorrect image size");
                         */
                        currImageUploadStatusParam.setValue("ERROR:Image size is beyond the permissible limit");
                        imageUploadStatusRecord.addParam(currImageUploadStatusParam);
                        continue;
                    }
                    String imageID = CommonUtilities.getNewId().toString();
                    postParametersMap.put("Image", currImageBase64String);
                    postParametersMap.put("id", imageID);
                    postParametersMap.put("Status_id", StatusEnum.SID_ACTIVE.name());
                    postParametersMap.put("createdby", userID);
                    postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
                    String insertOperationResponse = Executor.invokeService(ServiceURLEnum.SECURITYIMAGE_CREATE,
                            postParametersMap, null, requestInstance);
                    JSONObject insertOperationResponseJSON = CommonUtilities
                            .getStringAsJSONObject(insertOperationResponse);
                    if (insertOperationResponseJSON != null && insertOperationResponseJSON.has(FabricConstants.OPSTATUS)
                            && insertOperationResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                        /*
                         * AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.SECURITYIMAGES,
                         * EventEnum.CREATE, ActivityStatusEnum.SUCCESSFUL, "Secure images create successful. imageID: "
                         * + imageID);
                         */
                        successfulInserts++;
                    } else {
                        /*
                         * AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.SECURITYIMAGES,
                         * EventEnum.CREATE, ActivityStatusEnum.FAILED, "Secure images create failed.");
                         */
                        failedInserts++;
                    }
                } catch (Exception e) {
                    /*
                     * AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.SECURITYIMAGES, EventEnum.CREATE,
                     * ActivityStatusEnum.FAILED, "Secure images create failed. Errmsg:" + e.getMessage());
                     */
                    failedInserts++;
                }
            }
            processedResult.addParam(
                    new Param("Successful Insert Count", Integer.toString(successfulInserts), FabricConstants.STRING));
            processedResult.addParam(
                    new Param("Failed Insert Count", Integer.toString(failedInserts), FabricConstants.STRING));
            if (failedInserts > 0) {
                ErrorCodeEnum.ERR_20221.setErrorCode(processedResult);
            }
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }
}