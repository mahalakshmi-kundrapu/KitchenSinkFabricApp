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

public class LocationDetailsService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CustomerSecurityQuestionRead.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        String systemUser = "";
        String locationID = "";
        Result processedResult = new Result();
        LOG.info("inside LocationDetailsService:");
        try {

            if (requestInstance.getParameter("placeID") != null) {
                locationID = requestInstance.getParameter("placeID");
                LOG.info("inside LocationDetailsService locationID:" + locationID);
                if (StringUtils.isBlank(locationID)) {
                    return ErrorCodeEnum.ERR_20648.setErrorCode(processedResult);
                }
            } else {
                return ErrorCodeEnum.ERR_20648.setErrorCode(processedResult);

            }

            UserDetailsBean loggedInUserDetails = LoggedInUserHandler.getUserDetails(requestInstance);
            if (loggedInUserDetails != null) {
                systemUser = loggedInUserDetails.getUserId();
            }

            JSONObject readEndpointResponse = getLocationDetails(systemUser, locationID, requestInstance);
            if (readEndpointResponse == null || !readEndpointResponse.has(FabricConstants.OPSTATUS)
                    || readEndpointResponse.getInt(FabricConstants.OPSTATUS) != 0) {
                return ErrorCodeEnum.ERR_20371.setErrorCode(processedResult);
            }
            JSONArray locationDetailJSONArray = (JSONArray) readEndpointResponse.get("locationdetails_view");

            JSONObject locationJSON = null;

            Dataset locationDetailDataSet = new Dataset();
            locationDetailDataSet.setId("PlaceDetails");
            String key = "";
            String value = "";
            Record locationRecord;
            Param currValParam = null;

            locationJSON = locationDetailJSONArray.getJSONObject(0);
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
            processedResult = new Result();
            processedResult.addDataset(locationDetailDataSet);
            return processedResult;

        } catch (Exception e) {
            LOG.error(" LocationDetailsService Exception" + e);
            return ErrorCodeEnum.ERR_20001.setErrorCode(processedResult);
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
            LOG.info("day" + days);
            boolean isDaysExist = inputString.contains(days.toUpperCase()) || inputString.contains(days)
                    || inputString.contains(days.toLowerCase());

            if (!isDaysExist) {
                inputString = inputString + " || " + days + " : Closed";
            }
        }
        return inputString;
    }

    public JSONObject getLocationDetails(String systemUser, String locationId, DataControllerRequest requestInstance) {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("_locationId", locationId);
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.LOCATIONDETAILS_VIEW_READ,
                postParametersMap, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);
    }

}