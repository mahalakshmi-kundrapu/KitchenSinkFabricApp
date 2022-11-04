package com.kony.adminconsole.service.locationsandlocationservices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.service.customermanagement.CustomerSecurityQuestionRead;
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

public class LocationRangeService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CustomerSecurityQuestionRead.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result processedResult = new Result();
        String systemUser = "";
        String currLatitude = "";
        String currLongitude = "";
        String radius = "";
        String key = "";
        String value = "";
        Record locationRecord = null;
        Param currValParam = null;
        Dataset locationDetailDataSet = new Dataset();
        JSONObject locationJSON = null;

        try {

            if (requestInstance.getParameter("currLatitude") != null) {
                currLatitude = requestInstance.getParameter("currLatitude");
                if (StringUtils.isBlank(currLatitude)) {
                    return ErrorCodeEnum.ERR_20645.setErrorCode(processedResult);

                }
            } else {
                return ErrorCodeEnum.ERR_20645.setErrorCode(processedResult);

            }
            if (requestInstance.getParameter("currLongitude") != null) {
                currLongitude = requestInstance.getParameter("currLongitude");
                if (StringUtils.isBlank(currLongitude)) {
                    return ErrorCodeEnum.ERR_20644.setErrorCode(processedResult);

                }
            } else {
                return ErrorCodeEnum.ERR_20644.setErrorCode(processedResult);
            }
            if (requestInstance.getParameter("radius") != null) {
                radius = requestInstance.getParameter("radius");
                if (StringUtils.isBlank(radius)) {
                    return ErrorCodeEnum.ERR_20646.setErrorCode(processedResult);
                }
            } else {
                return ErrorCodeEnum.ERR_20646.setErrorCode(processedResult);
            }
            UserDetailsBean loggedInUserDetails = LoggedInUserHandler.getUserDetails(requestInstance);
            if (loggedInUserDetails != null) {
                systemUser = loggedInUserDetails.getUserId();
            }

            JSONObject readEndpointResponse = getLocationRange(systemUser, currLatitude, currLongitude, radius,
                    requestInstance);
            if (readEndpointResponse == null || !readEndpointResponse.has(FabricConstants.OPSTATUS)
                    || readEndpointResponse.getInt(FabricConstants.OPSTATUS) != 0) {
                return ErrorCodeEnum.ERR_20372.setErrorCode(processedResult);
            }
            JSONArray locationRangeDetailJSONArray = (JSONArray) readEndpointResponse.get("records");
            locationDetailDataSet.setId("Locations");
            for (int i = 0; i < locationRangeDetailJSONArray.length(); i++) {
                locationJSON = locationRangeDetailJSONArray.getJSONObject(i);
                locationRecord = new Record();
                for (String currKey : locationJSON.keySet()) {
                    key = currKey;
                    value = locationJSON.getString(currKey);
                    if (key.equalsIgnoreCase("workinghours")) {
                        value = modifyWorkingHours(locationJSON.getString(currKey));
                    }
                    currValParam = new Param(key, value, FabricConstants.STRING);
                    locationRecord.addParam(currValParam);
                }
                locationDetailDataSet.addRecord(locationRecord);
            }
            processedResult.addDataset(locationDetailDataSet);
            return processedResult;
        } catch (Exception e) {
            LOG.error(" LocationRangeService Exception" + e);
            processedResult.addParam(new Param("exception", e.getMessage(), FabricConstants.STRING));
            return ErrorCodeEnum.ERR_20372.setErrorCode(processedResult);
        }
    }

    public static String modifyWorkingHours(String inputString) {
        ArrayList<String> daysList = new ArrayList<String>();
        daysList.add("Sunday");
        daysList.add("Monday");
        daysList.add("Tuesday");
        daysList.add("Wednesday");
        daysList.add("Thursday");
        daysList.add("Friday");
        daysList.add("Saturday");
        for (int i = 0; i < daysList.size(); i++) {
            String days = daysList.get(i);
            boolean isDaysExist = inputString.contains(days.toUpperCase()) || inputString.contains(days)
                    || inputString.contains(days.toLowerCase());
            if (!isDaysExist) {
                inputString = inputString + " || " + days + " : Closed";
            }
        }
        return inputString;
    }

    public JSONObject getLocationRange(String systemUser, String currLatitude, String currLongitude, String radius,
            DataControllerRequest requestInstance) {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("_currLatitude", currLatitude);
        postParametersMap.put("_currLongitude", currLongitude);
        postParametersMap.put("_radius", radius);
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.LOCATION_RANGE_PROC_SERVICE,
                postParametersMap, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);
    }
}