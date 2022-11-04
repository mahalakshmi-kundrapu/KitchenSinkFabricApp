package com.kony.adminconsole.service.limitsandfees;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;

public class PeriodicLimitsValidationUtil {

    /**
     * This method validates overall payment limits <br>
     * 1. Mandatory fields <br>
     * 2. periodic limits i.e weekly limit should be within daily and monthly limits
     * 
     * @param periodsLimitArray
     * @param authToken
     * @param readPeriod
     * @return dataset
     * @throws Exception
     */
    public Dataset validateLimits(JSONArray periodsLimitArray, ServiceURLEnum readPeriod, String authToken,
            DataControllerRequest requestInstance) throws Exception {
        try {
            Dataset errors = validateMandatoryFields(periodsLimitArray);
            if (!errors.getAllRecords().isEmpty()) {
                return errors;
            }
            errors = validatePeriodsLimits(periodsLimitArray, readPeriod, authToken, requestInstance);
            return errors;
        } catch (Exception e) {
            throw e;
        }
    }

    /** This method validates the mandatory fields */
    private Dataset validateMandatoryFields(JSONArray periodsLimitArray) {
        Dataset errorDataset = new Dataset("violations");
        JSONObject temp = null;
        Record record = null;
        String periodId = "";
        String periodMaxLimit = "";
        for (int i = 0; i < periodsLimitArray.length(); i++) {
            temp = periodsLimitArray.getJSONObject(i);
            record = new Record();
            periodId = temp.optString("periodId");
            periodMaxLimit = temp.optString("periodLimit");
            if (StringUtils.isBlank(periodId)) {
                record.addParam(new Param(FabricConstants.ERR_MSG, ErrorCodeEnum.ERR_20591.getMessage(),
                        FabricConstants.STRING));
            }
            if (StringUtils.isBlank(periodMaxLimit)) {
                record.addParam(new Param(FabricConstants.ERR_MSG, ErrorCodeEnum.ERR_20592.getMessage(),
                        FabricConstants.STRING));
            }
            if (!record.getAllParams().isEmpty()) {
                record.addParam(new Param("periodId", periodId, FabricConstants.STRING));
                record.addParam(new Param("periodLimit", periodMaxLimit, FabricConstants.STRING));
                errorDataset.addRecord(record);
            }
        }
        return errorDataset;
    }

    /**
     * This method checks the limits range. i.e. weekly maxLimit should be in between daily and monthly limits
     */
    private Dataset validatePeriodsLimits(JSONArray periodsLimitArray, ServiceURLEnum readPeriod, String authToken,
            DataControllerRequest requestInstance) throws Exception {
        Dataset errorDataset = new Dataset("violations");

        // fetch all periods to find the order

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.ORDER_BY, "DayCount desc");
        String viewResponse = Executor.invokeService(readPeriod, postParametersMap, null, requestInstance);
        JSONObject response = CommonUtilities.getStringAsJSONObject(viewResponse);
        if (response.getInt(FabricConstants.OPSTATUS) != 0) {
            throw new Exception("Error in validating limits");
        }
        LinkedHashMap<String, JSONObject> periodIdMap = new LinkedHashMap<String, JSONObject>();
        JSONArray periodsDtls = response.getJSONArray("period");
        JSONObject temp = null;
        JSONObject json = null;
        for (int i = 0; i < periodsDtls.length(); i++) {
            temp = periodsDtls.getJSONObject(i);
            json = new JSONObject();
            json.put("periodName", temp.getString("Name"));
            json.put("DayCount", temp.getString("DayCount"));
            periodIdMap.put(temp.getString("id"), json);
        }

        LinkedHashMap<String, BigDecimal> periodIdAmtMapSortedInAsc = periodIdAmtMapSortedInAsc(periodsLimitArray,
                periodIdMap);

        for (String periodId : periodIdAmtMapSortedInAsc.keySet()) {
            BigDecimal maxAmout = periodIdAmtMapSortedInAsc.get(periodId);
            Integer noOfDays = periodIdMap.get(periodId).getInt("DayCount");
            String currPeriod = periodIdMap.get(periodId).getString("periodName");
            String periodName = "";
            Integer tempNoOfDays = 0;
            BigDecimal tempMaxAmout = null;
            for (String temPId : periodIdMap.keySet()) {
                if (periodIdAmtMapSortedInAsc.containsKey(temPId) && !temPId.equalsIgnoreCase(periodId)) {
                    tempMaxAmout = periodIdAmtMapSortedInAsc.get(temPId);
                    tempNoOfDays = periodIdMap.get(temPId).getInt("DayCount");
                    periodName = periodIdMap.get(temPId).getString("periodName");
                    if (noOfDays.compareTo(tempNoOfDays) > 0 && maxAmout.compareTo(tempMaxAmout) < 0) {
                        Record record = new Record();
                        record.addParam(new Param(FabricConstants.ERR_MSG,
                                currPeriod + " " + ErrorCodeEnum.ERR_20593.getMessage() + " : " + periodName,
                                FabricConstants.STRING));
                        errorDataset.addRecord(record);
                        break;
                    } else if (noOfDays.compareTo(tempNoOfDays) < 0 && maxAmout.compareTo(tempMaxAmout) > 0) {
                        Record record = new Record();
                        record.addParam(new Param(FabricConstants.ERR_MSG,
                                currPeriod + " " + ErrorCodeEnum.ERR_20594.getMessage() + " : " + periodName,
                                FabricConstants.STRING));
                        errorDataset.addRecord(record);
                        break;
                    }
                }
            }
            periodIdMap.remove(periodId);
        }
        return errorDataset;
    }

    private LinkedHashMap<String, BigDecimal> periodIdAmtMapSortedInAsc(JSONArray periodsLimitArray,
            LinkedHashMap<String, JSONObject> periodIdMap) {
        LinkedHashMap<String, BigDecimal> sortedMap = new LinkedHashMap<String, BigDecimal>();
        JSONObject temp = null;
        for (String key : periodIdMap.keySet()) {
            for (int i = 0; i < periodsLimitArray.length(); i++) {
                temp = periodsLimitArray.getJSONObject(i);
                if (key.equalsIgnoreCase(temp.getString("periodId"))) {
                    sortedMap.put(key, temp.getBigDecimal("periodLimit"));
                }
            }
        }
        return sortedMap;
    }

}