package com.kony.adminconsole.service.staticcontentmanagement.securityimages;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.handler.PaginationHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to Retrieve Security Images
 *
 * @author Aditya Mankal
 * 
 */
public class SecurityImagesGetService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(SecurityImagesGetService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Map<String, String> postParametersMap = new HashMap<String, String>();

            PaginationHandler.setOffset(requestInstance, postParametersMap);
            Result processedResult = new Result();
            JSONObject readSecurityImagesResponseJSON = PaginationHandler.getPaginatedData(
                    ServiceURLEnum.SECURITY_IMAGES_VIEW_READ, postParametersMap, null, requestInstance);
            if (readSecurityImagesResponseJSON != null && readSecurityImagesResponseJSON.has(FabricConstants.OPSTATUS)
                    && readSecurityImagesResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readSecurityImagesResponseJSON.has("security_images_view")) {
                PaginationHandler.addPaginationMetadataToResultObject(processedResult, readSecurityImagesResponseJSON);
                JSONArray securityImagesJSONArray = readSecurityImagesResponseJSON.getJSONArray("security_images_view");
                Dataset securityImagesDataSet = new Dataset();
                securityImagesDataSet.setId("records");
                for (int indexVar = 0; indexVar < securityImagesJSONArray.length(); indexVar++) {
                    JSONObject currImageJSONObject = securityImagesJSONArray.getJSONObject(indexVar);
                    Record currImageRecord = new Record();
                    for (String currKey : currImageJSONObject.keySet()) {
                        Param currValParam = new Param(currKey, currImageJSONObject.optString(currKey),
                                FabricConstants.STRING);
                        currImageRecord.addParam(currValParam);
                    }
                    securityImagesDataSet.addRecord(currImageRecord);
                }
                processedResult.addDataset(securityImagesDataSet);
                return processedResult;
            }
            ErrorCodeEnum.ERR_20224.setErrorCode(processedResult);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

}