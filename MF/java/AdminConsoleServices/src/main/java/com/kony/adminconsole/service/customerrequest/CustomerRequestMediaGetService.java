package com.kony.adminconsole.service.customerrequest;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
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
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to download the message attachment
 *
 * @author Aditya Mankal
 * 
 */
public class CustomerRequestMediaGetService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(CustomerRequestMediaGetService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {

            String mediaID, isArchivedRequest;

            mediaID = requestInstance.getParameter("mediaID");
            isArchivedRequest = requestInstance.getParameter("isArchivedRequest");
            String fieldPrefixString = StringUtils.EMPTY;// set to the word 'archived' in case of an ArchivedRequest Get
                                                         // Call.

            Result processedResult = new Result();
            Map<String, String> postParametersMap = new HashMap<String, String>();

            if (StringUtils.isBlank(mediaID)) {
                processedResult.addParam(new Param("validationError",
                        "Media ID is a mandatory input. It cannot be NULL/Empty", FabricConstants.STRING));
                ErrorCodeEnum.ERR_20131.setErrorCode(processedResult);
                return processedResult;
            }

            Record resultRecord = new Record();
            resultRecord.setId("records");

            postParametersMap.put(ODataQueryConstants.FILTER, "id eq '" + mediaID + "'");

            String readMediaServiceResponse;
            JSONObject readMediaServiceResponseJSON;
            JSONArray mediaRecordsJSONArray = new JSONArray();

            if (StringUtils.equalsIgnoreCase(isArchivedRequest, "TRUE")
                    || StringUtils.equalsIgnoreCase(isArchivedRequest, "1")) {
                readMediaServiceResponse = Executor.invokeService(ServiceURLEnum.ARCHIVEDMEDIA_READ, postParametersMap,
                        null, requestInstance);
                readMediaServiceResponseJSON = CommonUtilities.getStringAsJSONObject(readMediaServiceResponse);
                fieldPrefixString = "archived";
            } else {
                readMediaServiceResponse = Executor.invokeService(ServiceURLEnum.MEDIA_READ, postParametersMap, null,
                        requestInstance);
                readMediaServiceResponseJSON = CommonUtilities.getStringAsJSONObject(readMediaServiceResponse);
            }
            if (readMediaServiceResponseJSON == null || !readMediaServiceResponseJSON.has(FabricConstants.OPSTATUS)
                    || readMediaServiceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                    || !readMediaServiceResponseJSON.has(fieldPrefixString + "media")) {
                ErrorCodeEnum.ERR_20130.setErrorCode(processedResult);
                return processedResult;
            }

            mediaRecordsJSONArray = readMediaServiceResponseJSON.getJSONArray(fieldPrefixString + "media");
            JSONObject currMediaObject;

            Record mediaRecord = new Record();
            mediaRecord.setId(mediaID);

            for (Object currRequestObject : mediaRecordsJSONArray) {
                currMediaObject = (JSONObject) currRequestObject;
                for (String currKey : currMediaObject.keySet()) {
                    Param currRequestEntryItemParam = new Param(currKey, currMediaObject.optString(currKey),
                            FabricConstants.STRING);
                    mediaRecord.addParam(currRequestEntryItemParam);
                }
            }

            Dataset recordsDataset = new Dataset();
            recordsDataset.setId("records");
            recordsDataset.addRecord(mediaRecord);
            processedResult.addDataset(recordsDataset);

            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

}