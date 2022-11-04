package com.kony.adminconsole.service.limitsandfees;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.controller.DataControllerRequest;

public class ValidatePeriodicLimitUtil {

    public JSONObject validateLimits(String serviceId, JSONArray periodsLimitArray, String baseURL, String authToken,
            DataControllerRequest dataControllerRequest) throws Exception {

        JSONObject getRecordForServiceId = getRecordsForServiceID(serviceId, authToken, dataControllerRequest);
        if (getRecordForServiceId.getInt(FabricConstants.OPSTATUS) != 0) {
            return constructResult(getRecordForServiceId.getString(FabricConstants.OPSTATUS),
                    getRecordForServiceId.toString());
        }
        // validate service min max limits
        boolean isInServiceLimit = validateServiceMaxAndMinLimit(getRecordForServiceId, periodsLimitArray);
        if (!isInServiceLimit) {
            // service min - max limit failed
            return constructResult(ErrorCodeEnum.ERR_20567.getErrorCodeAsString(),
                    ErrorCodeEnum.ERR_20567.getMessage());
        }
        // fetch periodic min max limits
        JSONObject getPeriodicRecordForServiceId = getPeriodicLimitForServiceID(serviceId, authToken,
                dataControllerRequest);
        if (getRecordForServiceId.getInt(FabricConstants.OPSTATUS) != 0) {
            return constructResult(getRecordForServiceId.getString(FabricConstants.OPSTATUS),
                    getRecordForServiceId.toString());
        }
        boolean isINservicePeriodicLimits = validateServicePeriodicLimit(getPeriodicRecordForServiceId,
                periodsLimitArray);

        if (!isINservicePeriodicLimits) {
            // periodic min max failed
            return constructResult(ErrorCodeEnum.ERR_20568.getErrorCodeAsString(),
                    ErrorCodeEnum.ERR_20568.getMessage());
        }
        return null;

    }

    private JSONObject getRecordsForServiceID(String serviceId, String authToken,
            DataControllerRequest requestInstance) {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "id eq '" + serviceId + "'");
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.SERVICE_READ, postParametersMap, null,
                requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);
    }

    private JSONObject getPeriodicLimitForServiceID(String serviceID, String authToken,
            DataControllerRequest requestInstance) {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "Service_id eq '" + serviceID + "'");
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.PERIODICLIMITSERVICE_VIEW_READ,
                postParametersMap, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

    public boolean validateServiceMaxAndMinLimit(JSONObject getRecordForServiceId, JSONArray periodsLimitArray)
            throws Exception {

        String serviceMaxTransferLimit = null;
        String serviceMinTransferLimit = "0";

        if (((JSONObject) ((JSONArray) getRecordForServiceId.get("service")).get(0)).has("MaxTransferLimit")) {
            serviceMaxTransferLimit = ((JSONObject) ((JSONArray) getRecordForServiceId.get("service")).get(0))
                    .getString("MaxTransferLimit");
        }
        if (((JSONObject) ((JSONArray) getRecordForServiceId.get("service")).get(0)).has("MinTransferLimit")) {
            serviceMinTransferLimit = ((JSONObject) ((JSONArray) getRecordForServiceId.get("service")).get(0))
                    .getString("MinTransferLimit");
        }
        if (serviceMaxTransferLimit == null) {
            // no maximum limit hence true
            return true;
        }

        for (int i = 0; i < periodsLimitArray.length(); i++) {
            String periodId = ((JSONObject) periodsLimitArray.get(i)).get("periodId").toString().trim();
            String periodLimit = ((JSONObject) periodsLimitArray.get(i)).get("periodLimit").toString().trim();
            if (periodId != null && ("P1").equalsIgnoreCase(periodId)) { // validating for daily
                if (periodLimit != null) {
                    float plimit = Float.parseFloat(periodLimit.trim());
                    float maxSerLimit = Float.parseFloat(serviceMaxTransferLimit.trim());
                    float minSerLimit = Float.parseFloat(serviceMinTransferLimit.trim());
                    if (plimit < minSerLimit || plimit > maxSerLimit) {
                        return false;
                    }
                } else {
                    throw new Exception("PERIOD_LIMIT_EMPTY");
                }
                return true;
            }
        }
        return true;
    }

    public boolean validateServicePeriodicLimit(JSONObject getPeriodicRecordForServiceId, JSONArray periodsLimitArray)
            throws Exception {

        try {
            int inputPeriodsLength = periodsLimitArray.length();

            JSONArray servicePeriodArray = (JSONArray) getPeriodicRecordForServiceId.get("periodiclimitservice_view");
            int servicePeriodLength = servicePeriodArray.length();

            for (int i = 0; i < inputPeriodsLength; i++) {
                String periodId = ((JSONObject) periodsLimitArray.get(i)).get("periodId").toString().trim();
                String periodLimit = ((JSONObject) periodsLimitArray.get(i)).get("periodLimit").toString().trim();
                for (int j = 0; j < servicePeriodLength; j++) {

                    String servicePeriodId = ((JSONObject) servicePeriodArray.get(i)).get("Period_id").toString()
                            .trim();
                    if (servicePeriodId.equalsIgnoreCase(periodId)) {
                        String servicePeriodLimit = ((JSONObject) servicePeriodArray.get(i)).get("MaximumLimit")
                                .toString().trim();
                        // float iperiodLimit = Integer.parseInt(periodLimit);
                        float iperiodLimit = Float.parseFloat(periodLimit);
                        float sPeriodLimit = Float.parseFloat(servicePeriodLimit);

                        if (iperiodLimit > sPeriodLimit) {
                            return false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return true;
    }

    private JSONObject constructResult(String opstatus, String msg) {
        JSONObject response = new JSONObject();
        response.put(FabricConstants.OPSTATUS, opstatus);
        response.put(FabricConstants.ERR_MSG, msg);
        return response;
    }
}