package com.kony.adminconsole.service.locationsandlocationservices;

import java.util.HashMap;
import java.util.Map;

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
 * Service to retrieve the Location, their schedules and their corresponding Location Records
 *
 * @author Aditya Mankal
 * 
 */
public class LocationAndServicesGetService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(LocationAndServicesGetService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result processedResult = new Result();

            String locationId = requestInstance.getParameter("Location_id");
            if (locationId == null) {
                ErrorCodeEnum.ERR_20364.setErrorCode(processedResult);
            }

            Map<String, String> locationMap = new HashMap<String, String>();
            locationMap.put(ODataQueryConstants.FILTER, "Location_id eq '" + locationId + "'");

            String readLocationResponse = Executor.invokeService(ServiceURLEnum.LOCATIONSERVICES_VIEW_READ, locationMap,
                    null, requestInstance);
            JSONObject readLocationResponseJSON = CommonUtilities.getStringAsJSONObject(readLocationResponse);

            if (readLocationResponseJSON != null && readLocationResponseJSON.has(FabricConstants.OPSTATUS)
                    && readLocationResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readLocationResponseJSON.has("locationservices_view")) {

                JSONArray readlocationServicesJSONArray = readLocationResponseJSON
                        .getJSONArray("locationservices_view");
                Dataset locationServicesDataSet = new Dataset();
                locationServicesDataSet.setId("records");
                JSONObject currLocationServicesJSONObject = null;
                for (Object currLocationServicesObject : readlocationServicesJSONArray) {
                    if (!(currLocationServicesObject instanceof JSONObject))
                        continue;
                    currLocationServicesJSONObject = (JSONObject) currLocationServicesObject;
                    Record currRecord = new Record();
                    for (String currKey : currLocationServicesJSONObject.keySet()) {
                        Param currValParam = new Param(currKey, currLocationServicesJSONObject.getString(currKey),
                                FabricConstants.STRING);
                        currRecord.addParam(currValParam);
                    }
                    locationServicesDataSet.addRecord(currRecord);
                }
                processedResult.addDataset(locationServicesDataSet);
            } else {
                LOG.error("Fetch Location Services Status:Failed");
                ErrorCodeEnum.ERR_20364.setErrorCode(processedResult);
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