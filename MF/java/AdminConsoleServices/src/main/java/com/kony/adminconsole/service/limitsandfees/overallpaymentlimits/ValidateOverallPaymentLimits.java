package com.kony.adminconsole.service.limitsandfees.overallpaymentlimits;

import java.math.BigDecimal;
import java.util.ArrayList;
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

public class ValidateOverallPaymentLimits {

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
	public Dataset validateLimits(Dataset prevLimits, JSONArray periodsLimitArray, ServiceURLEnum readPeriod, String authToken, DataControllerRequest requestInstance)
			throws Exception {
		try {
			Dataset errors = validateMandatoryFields(periodsLimitArray);
			if (!errors.getAllRecords().isEmpty()) {
				return errors;
			}
			errors = validatePeriodsLimits(prevLimits, periodsLimitArray, readPeriod, authToken, requestInstance);
			return errors;
		} catch (Exception e) {
			throw e;
		}
	}

	/** This method validates the mandatory fields */
	private Dataset validateMandatoryFields(JSONArray periodsLimitArray) {
		Dataset errorDataset = new Dataset("violations");
		for (int i = 0; i < periodsLimitArray.length(); i++) {
			JSONObject temp = periodsLimitArray.getJSONObject(i);
			Record record = new Record();
			String periodId = temp.optString("periodId");
			String periodMaxLimit = temp.optString("periodLimit");
			if (StringUtils.isBlank(periodId)) {
				record.addParam(
						new Param(FabricConstants.ERR_MSG, ErrorCodeEnum.ERR_20591.getMessage(), FabricConstants.STRING));
			}
			if (StringUtils.isBlank(periodMaxLimit)) {
				record.addParam(
						new Param(FabricConstants.ERR_MSG, ErrorCodeEnum.ERR_20592.getMessage(), FabricConstants.STRING));
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
	 * 
	 * @param prevLimits
	 */
	private Dataset validatePeriodsLimits(Dataset prevLimits, JSONArray periodsLimitArray, ServiceURLEnum readPeriod,
			String authToken, DataControllerRequest requestInstance) throws Exception {
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
		for (int i = 0; i < periodsDtls.length(); i++) {
			JSONObject temp = periodsDtls.getJSONObject(i);
			JSONObject json = new JSONObject();
			json.put("periodName", temp.getString("Name"));
			json.put("DayCount", temp.getString("DayCount"));
			periodIdMap.put(temp.getString("id"), json);
		}
		ArrayList<String> delPeriods = new ArrayList<>();
		LinkedHashMap<String, BigDecimal> periodIdAmtMapSortedInAsc = fetchPeriodIdAmtMapSortedInAsc(periodsLimitArray, periodIdMap, delPeriods);

		LinkedHashMap<String, JSONObject> tempPeriodIdMap = new LinkedHashMap<String, JSONObject>();
		tempPeriodIdMap.putAll(periodIdMap);

		for (String periodId : periodIdAmtMapSortedInAsc.keySet()) {
			BigDecimal maxAmout = periodIdAmtMapSortedInAsc.get(periodId);
			Integer noOfDays = periodIdMap.get(periodId).getInt("DayCount");
			String currPeriod = periodIdMap.get(periodId).getString("periodName");
			for (String temPId : periodIdMap.keySet()) {
				if (periodIdAmtMapSortedInAsc.containsKey(temPId) && !temPId.equalsIgnoreCase(periodId)) {
					BigDecimal tempMaxAmout = periodIdAmtMapSortedInAsc.get(temPId);
					Integer tempNoOfDays = periodIdMap.get(temPId).getInt("DayCount");
					String periodName = periodIdMap.get(temPId).getString("periodName");
					if (noOfDays.compareTo(tempNoOfDays) > 0 && maxAmout.compareTo(tempMaxAmout) < 0) {
						Record record = new Record();
						record.addParam(new Param(FabricConstants.ERR_MSG,
								currPeriod + " " + ErrorCodeEnum.ERR_20593.getMessage() + " " + periodName,
								FabricConstants.STRING));
						errorDataset.addRecord(record);
						break;
					} else if (noOfDays.compareTo(tempNoOfDays) < 0 && maxAmout.compareTo(tempMaxAmout) > 0) {
						Record record = new Record();
						record.addParam(new Param(FabricConstants.ERR_MSG,
								currPeriod + " " + ErrorCodeEnum.ERR_20594.getMessage() + " " + periodName,
								FabricConstants.STRING));
						errorDataset.addRecord(record);
						break;
					}
				}
			}
			periodIdMap.remove(periodId);
		}
		// validate with previous datasets
		if (prevLimits != null) {
			validateWithPreviousLimits(prevLimits, periodIdAmtMapSortedInAsc, delPeriods, tempPeriodIdMap,
					errorDataset);
		}
		return errorDataset;
	}

	private void validateWithPreviousLimits(Dataset prevLimits,
			LinkedHashMap<String, BigDecimal> periodIdAmtMapSortedInAsc, ArrayList<String> delPeriods,
			LinkedHashMap<String, JSONObject> tempPeriodIdMap, Dataset errorDataset) {
		Dataset periods = prevLimits.getRecord(0).getDatasetById("periodicLimits");
		for (Record record : periods.getAllRecords()) {
			String periodId = record.getParam("periodId").getValue();
			boolean isValidationRequired = !delPeriods.contains(periodId.toUpperCase()) && !periodIdAmtMapSortedInAsc.containsKey(periodId);
			if (isValidationRequired) {
				String periodName = record.getParam("periodName").getValue();
				Integer noOfDays = new Integer(record.getParam("dayCount").getValue());
				BigDecimal maxAmout = new BigDecimal(record.getParam("maximumLimit").getValue());
				for (String key : periodIdAmtMapSortedInAsc.keySet()) {
					BigDecimal tempMaxAmout = periodIdAmtMapSortedInAsc.get(key);
					Integer tempNoOfDays = tempPeriodIdMap.get(key).getInt("DayCount");
					String tempPeriod = tempPeriodIdMap.get(key).getString("periodName");

					if (noOfDays.compareTo(tempNoOfDays) > 0 && maxAmout.compareTo(tempMaxAmout) < 0) {
						Record tempRec = new Record();
						tempRec.addParam(new Param(FabricConstants.ERR_MSG,
								periodName + " " + ErrorCodeEnum.ERR_20593.getMessage() + " " + tempPeriod,
								FabricConstants.STRING));
						errorDataset.addRecord(tempRec);
						break;
					} else if (noOfDays.compareTo(tempNoOfDays) < 0 && maxAmout.compareTo(tempMaxAmout) > 0) {
						Record tempRec = new Record();
						tempRec.addParam(new Param(FabricConstants.ERR_MSG,
								periodName + " " + ErrorCodeEnum.ERR_20594.getMessage() + " " + tempPeriod,
								FabricConstants.STRING));
						errorDataset.addRecord(tempRec);
						break;
					}
				}
			}
		}
	}

	private LinkedHashMap<String, BigDecimal> fetchPeriodIdAmtMapSortedInAsc(JSONArray periodsLimitArray,
			LinkedHashMap<String, JSONObject> periodIdMap, ArrayList<String> delPeriods) {
		LinkedHashMap<String, BigDecimal> sortedMap = new LinkedHashMap<String, BigDecimal>();
		for (String key : periodIdMap.keySet()) {
			for (int i = 0; i < periodsLimitArray.length(); i++) {
				JSONObject temp = periodsLimitArray.getJSONObject(i);
				String periodId = temp.getString("periodId");
				boolean isDelete = temp.optString("deleteFlag").equalsIgnoreCase("Y");
				if (key.equalsIgnoreCase(periodId) && !isDelete) {
					sortedMap.put(key, temp.getBigDecimal("periodLimit"));
				}
				if (isDelete) {
					delPeriods.add(periodId.toUpperCase());
				}
			}
		}
		return sortedMap;
	}

}