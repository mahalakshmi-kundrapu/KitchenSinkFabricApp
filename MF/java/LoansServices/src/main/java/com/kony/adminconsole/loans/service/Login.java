package com.kony.adminconsole.loans.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.loans.utils.LoansMetricsData;
import com.kony.adminconsole.loans.utils.LoansMetricsProcessorHelper;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.kony.adminconsole.loans.utils.LoansUtilitiesConstants;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.exceptions.MetricsException;
import com.konylabs.middleware.session.Session;

public class Login implements JavaService2 {
	private static final Logger LOGGER = Logger.getLogger(Login.class);
	@SuppressWarnings("rawtypes")
	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest dcRequest,
			DataControllerResponse dcResponse) throws Exception {
		try {
		Result result = new Result();
		Map inputParams = (HashMap) inputArray[1];
		;
		if (preProcess(inputParams, dcRequest, result)) {
			String URL = LoansServiceURLEnum.CUSTOMER_GET.getServiceURL(dcRequest);
			String Value = HTTPOperations.hitPOSTServiceAndGetResponse(URL, (HashMap) inputParams, (HashMap) null,
					null);
			JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
			result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
			result = postProcess(inputParams, dcRequest, result);
		}

		return result;
		}catch (Exception e) {
			   LOGGER.error("Exception in CreateQueryResponse : " + e.getMessage());
			   Param exceptionToBeShownToUser = new Param();
			   exceptionToBeShownToUser.setValue(e.getMessage());
			   Result result = new Result();
			   result.addParam(exceptionToBeShownToUser);
			   return result;
			  }
	}

	public static boolean hasRecords(Result result) {
		if (hasError(result)) {
			return false;
		}
		Dataset ds = result.getAllDatasets().get(0);
		return null != ds && null != ds.getAllRecords() && ds.getAllRecords().size() > 0;
	}

	public static boolean hasError(Result result) {
		boolean status = false;
		if (null == result || null != result.getParamByName(LoansUtilitiesConstants.VALIDATION_ERROR)) {
			status = true;
		}
		return status;
	}

	@SuppressWarnings("rawtypes")
	private Result postProcess(Map inputParams, DataControllerRequest dcRequest, Result result)
			throws MetricsException, ParseException, Exception {
		Result retVal = new Result();
		if (hasRecords(result)) {
			if (getLockStatus(result)) {
				Param p = new Param(LoansUtilitiesConstants.VALIDATION_ERROR, "user is locked", "String");
				retVal.addParam(p);
				pushCustomMetricData(dcRequest, "user_status", "locked");
			} else {
				retVal = sessionAttributes(result);
				Dataset ds = result.getDatasetById(LoansUtilitiesConstants.DS_USER);
				Record usrAttr = ds.getRecord(0);
				Session sessionObj = dcRequest.getSession(true);
				sessionObj.setAttribute("username", usrAttr.getParam(LoansUtilitiesConstants.U_ID).getValue());
			}
		} else {
			String username = (String) inputParams.get(LoansUtilitiesConstants.USR_NAME);
			if (isUserLocked(username,dcRequest,inputParams)) {
				Param p = new Param(LoansUtilitiesConstants.VALIDATION_ERROR, "user is locked", "String");
				retVal.addParam(p);
				pushCustomMetricData(dcRequest, "user_status", "locked");
			} else {
				 updateLockCount(username,dcRequest,inputParams);
				Param p = new Param(LoansUtilitiesConstants.VALIDATION_ERROR, "incorrect username and password",
						"String");
				retVal.addParam(p);
				pushCustomMetricData(dcRequest, "login_status", "failed");
			}
		}
		return retVal;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void updateLockCount(String username,DataControllerRequest dcRequest,Map inputParams) throws Exception {
		String filter = LoansUtilitiesConstants.USER_NAME + LoansUtilitiesConstants.EQUAL + username;
		Result result = new Result();
		Map inputParam = new HashMap();
		inputParam.put(LoansUtilitiesConstants.FILTER, filter);
		String url = LoansServiceURLEnum.USER_GET.getServiceURL(dcRequest);
		String ValueR = HTTPOperations.hitPOSTServiceAndGetResponse(url, (HashMap) inputParam, (HashMap) null,
				null);
		JSONObject ValueResJSON = CommonUtilities.getStringAsJSONObject(ValueR);
		result = CommonUtilities.getResultObjectFromJSONObject(ValueResJSON);
		String id = getFieldValue(result, "Id");
		int count = 1;
		String lockcount = getFieldValue(result, "lockCount");
		if (isNotBlank(lockcount)) {
			count = Integer.parseInt(lockcount) + 1;
		}
		Map input = new HashMap();
		input.put("Id", id);
		input.put("lockCount", count);
		String URL = LoansServiceURLEnum.USER_UPDATE.getServiceURL(dcRequest);
		String Value = HTTPOperations.hitPOSTServiceAndGetResponse(URL, (HashMap) inputParams, (HashMap) null,
				null);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
	}

	private boolean isUserLocked(String username,DataControllerRequest dcRequest,Map inputParams) throws Exception {
		String filter = LoansUtilitiesConstants.USER_NAME + LoansUtilitiesConstants.EQUAL + username;
		Result result = new Result();
		Map inputParam = new HashMap();
		inputParam.put(LoansUtilitiesConstants.FILTER, filter);
		String URL = LoansServiceURLEnum.USER_GET.getServiceURL(dcRequest);
		String Value = HTTPOperations.hitPOSTServiceAndGetResponse(URL, (HashMap) inputParam, (HashMap) null,
				null);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
		return getLockStatus(result);
	}
	

	public static String getFieldValue(Result result, String fieldName) {
		String id = null;
		if (hasRecords(result)) {
			Dataset ds = result.getAllDatasets().get(0);
			id = getParamValue(ds.getRecord(0).getParam(fieldName));
		}
		return id;
	}

	public static String getParamValue(Param p) {
		String value = null;
		if (null != p) {
			value = p.getValue();
		}
		return value;
	}

	private boolean getLockStatus(Result result) {
		String lockcount = getFieldValue(result, "lockCount");
		if (isNotBlank(lockcount)) {
			int count = Integer.parseInt(lockcount);
			return count > 5;
		}
		return false;
	}

	private Result sessionAttributes(Result result) {
		Result retVal = new Result();
		Dataset ds = result.getDatasetById(LoansUtilitiesConstants.DS_USER);
		Record sessionAttr = new Record();
		String token = UUID.randomUUID().toString();
		sessionAttr.setId("security_attributes");
		Param sessionId = new Param("session_token", token, "String");
		Param sessionTtl = new Param("session_ttl", null, "String");
		sessionAttr.addParam(sessionId);
		sessionAttr.addParam(sessionTtl);

		Record usrAttr = ds.getRecord(0);
		usrAttr.setId("user_attributes");
		// usrAttr.setParam(new Param("user_id", usrAttr.getParam(
		// LoansUtilitiesConstants.U_ID).getValue(), "String"));
		Param user_a = new Param("user_attributes", "Id", "String");
		usrAttr.addParam(user_a);
		retVal.addRecord(usrAttr);
		retVal.addRecord(sessionAttr);

		return retVal;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean preProcess(Map inputParams, DataControllerRequest dcRequest, Result result) {
		boolean status = true;
		String username = (String) inputParams.get(LoansUtilitiesConstants.USR_NAME);
		String password = (String) inputParams.get(LoansUtilitiesConstants.PWD_FIELD);
		String pin = (String) inputParams.get(LoansUtilitiesConstants.PIN);
		StringBuilder sb = new StringBuilder();
		if (isNotBlank(password) && !"$password".equalsIgnoreCase(password)) {
			sb.append(LoansUtilitiesConstants.USER_NAME).append(LoansUtilitiesConstants.EQUAL).append(username)
					.append(LoansUtilitiesConstants.AND).append(LoansUtilitiesConstants.PWD_FIELD)
					.append(LoansUtilitiesConstants.EQUAL).append(password);
		} else if (isNotBlank(pin)) {
			sb.append(LoansUtilitiesConstants.USER_NAME).append(LoansUtilitiesConstants.EQUAL).append(username)
					.append(LoansUtilitiesConstants.AND).append(LoansUtilitiesConstants.PIN)
					.append(LoansUtilitiesConstants.EQUAL).append(pin);
		} else {
			setValidationMsg("Incorrect details.", dcRequest, result);
			status = false;
		}
		inputParams.put(LoansUtilitiesConstants.FILTER, sb.toString());
		return status;
	}

	public static boolean isNotBlank(String s) {
		return !(null == s || s.isEmpty() || s.trim().isEmpty());
	}

	public static void setValidationMsg(String message, DataControllerRequest dcRequest, Result result) {
		Param validionMsg = new Param(LoansUtilitiesConstants.VALIDATION_ERROR, message,
				LoansUtilitiesConstants.STRING_TYPE);
		result.addParam(validionMsg);
		Param status = new Param(LoansUtilitiesConstants.HTTP_STATUS_CODE, LoansUtilitiesConstants.HTTP_ERROR_CODE,
				LoansUtilitiesConstants.STRING_TYPE);
		result.addParam(status);
		Param opstatus = new Param(LoansUtilitiesConstants.OP_STATUS, LoansUtilitiesConstants.HTTP_ERROR_CODE,
				LoansUtilitiesConstants.STRING_TYPE);
		result.addParam(opstatus);
	}

	private void pushCustomMetricData(DataControllerRequest dcRequest, String metricName, String metricValue)
			throws MetricsException, ParseException {
		LoansMetricsProcessorHelper helper = new LoansMetricsProcessorHelper();
		List<LoansMetricsData> custMetrics = new ArrayList<>();
		custMetrics.add(new LoansMetricsData(metricName, metricValue, LoansMetricsData.STRING));
		helper.addCustomMetrics(dcRequest, custMetrics, false);
	}
}