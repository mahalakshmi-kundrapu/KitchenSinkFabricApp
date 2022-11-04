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

public class LocationSearchService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CustomerSecurityQuestionRead.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result processedResult = new Result();

        String key = "";
        String query = "";
        String value = "";
        String systemUser = "";
        Dataset locationDetailDataSet = new Dataset();
        Param currValParam = null;
        Record locationRecord = null;
        try {

            if (requestInstance.getParameter("query") != null) {
                query = requestInstance.getParameter("query");
                if (StringUtils.isBlank(query)) {
                    return ErrorCodeEnum.ERR_20647.setErrorCode(processedResult);
                }
            } else {
                return ErrorCodeEnum.ERR_20647.setErrorCode(processedResult);
            }

            UserDetailsBean loggedInUserDetails = LoggedInUserHandler.getUserDetails(requestInstance);
            if (loggedInUserDetails != null) {
                systemUser = loggedInUserDetails.getUserId();
            }

            JSONObject readEndpointResponse = getSearchedLocationDetails(systemUser, query, requestInstance);
            if (readEndpointResponse == null || !readEndpointResponse.has(FabricConstants.OPSTATUS)
                    || readEndpointResponse.getInt(FabricConstants.OPSTATUS) != 0) {
                return ErrorCodeEnum.ERR_20371.setErrorCode(processedResult);
            }
            JSONArray locationDetailJSONArray = (JSONArray) readEndpointResponse.get("records");

            JSONObject locationJSON = null;

            locationDetailDataSet.setId("Locations");

            for (int i = 0; i < locationDetailJSONArray.length(); i++) {
                locationJSON = locationDetailJSONArray.getJSONObject(i);
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
            processedResult.addParam(new Param("exception", e.getMessage(), FabricConstants.STRING));
            LOG.error("Exception" + e);
            return ErrorCodeEnum.ERR_20371.setErrorCode(processedResult);
        }
    }

    public JSONObject getSearchedLocationDetails(String systemUser, String query,
            DataControllerRequest requestInstance) {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("_searchKeyword", query.trim());
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.LOCATION_SEARCH_PROC_SERVICE,
                postParametersMap, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);
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

}