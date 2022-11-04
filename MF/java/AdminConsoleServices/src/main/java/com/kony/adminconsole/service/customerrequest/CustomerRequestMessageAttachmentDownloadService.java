package com.kony.adminconsole.service.customerrequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to download the Message Attachment
 * 
 * @author Aditya Mankal
 * 
 */
public class CustomerRequestMessageAttachmentDownloadService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CustomerRequestMessageAttachmentDownloadService.class);
    private static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        String mediaId;
        Result processedResult = new Result();

        try {
            Map<String, String> queryParamsMap = (Map<String, String>) requestInstance.getAttribute("queryparams");
            mediaId = queryParamsMap.get("mediaId");

            Map<String, String> postParametersMap = new HashMap<>();
            postParametersMap.put(ODataQueryConstants.SELECT, "Name,Content");
            postParametersMap.put(ODataQueryConstants.FILTER, "id eq '" + mediaId + "'");
            String readMediaResponse = Executor.invokeService(ServiceURLEnum.MEDIA_READ, postParametersMap, null,
                    requestInstance);
            JSONObject readMediaResponseJSON = CommonUtilities.getStringAsJSONObject(readMediaResponse);

            if (readMediaResponseJSON == null || !readMediaResponseJSON.has(FabricConstants.OPSTATUS)
                    || readMediaResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                    || !readMediaResponseJSON.has("media")) {
                LOG.error("Failed to read Media Content");
                ErrorCodeEnum.ERR_20130.setErrorCode(processedResult);
                return processedResult;
            }
            LOG.debug("Succesful Read Media Table Operation");

            Map<String, String> responseHeaders = new HashMap<String, String>();
            responseHeaders.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM.getMimeType());

            if (readMediaResponseJSON.getJSONArray("media") != null
                    && readMediaResponseJSON.getJSONArray("media").length() > 0) {
                JSONObject currFileJSONObject = readMediaResponseJSON.getJSONArray("media").getJSONObject(0);
                responseHeaders.put(CONTENT_DISPOSITION_HEADER,
                        "attachment; filename=\"" + currFileJSONObject.getString("Name") + "\"");
                File file = constructFileObjectFromBase64String(currFileJSONObject.getString("Content"));
                responseInstance.setAttribute(FabricConstants.CHUNKED_RESULTS_IN_JSON,
                        new BufferedHttpEntity(new FileEntity(file)));
                responseInstance.getHeaders().putAll(responseHeaders);
                file.deleteOnExit();
                responseInstance.setStatusCode(HttpStatus.SC_OK);
                LOG.info("Succesfully constructed the File object");
            } else {
                LOG.error("Attempted to retrieve non existing file. Request rejected.");
                ErrorCodeEnum.ERR_20562.setErrorCode(processedResult);
                return processedResult;
            }
        } catch (Exception e) {
            LOG.error("Failed while downloading customer request message attachment", e);
            ErrorCodeEnum.ERR_20687.setErrorCode(processedResult);

            String errorMessage =
                    "Failed to download customer request message attachment. Please contact administrator.";
            CommonUtilities.fileDownloadFailure(responseInstance, errorMessage);
        }
        return processedResult;
    }

    public File constructFileObjectFromBase64String(String base64FileContent) throws Exception {
        byte[] decodedFileContent = Base64.decodeBase64(base64FileContent.getBytes("UTF-8"));
        File file = File.createTempFile("customerRequest", "messageattachment");
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(decodedFileContent);
        } catch (IOException e) {
            LOG.error("Exception in Constructing  File Output Stream", e);
        }

        finally {
            try {
                if (fileOutputStream != null)
                    fileOutputStream.close();
            } catch (IOException e) {
                LOG.error("Exception in Closing  File Output Stream", e);
            }

        }
        return file;
    }
}