package com.kony.adminconsole.loans.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.kony.adminconsole.loans.utils.LoansUtilities;
import com.kony.adminconsole.loans.utils.LoansUtilitiesConstants;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.exceptions.MetricsException;

public class SetNewPassword implements JavaService2 {
	/**
	 * Constants to hold the field names as Strings 
	 */
	public static final String CUSTOMER_ID = "id";
	public static final String CUSTOMER_PASSWORD = "Password";
	public static final String GetService_URL = "http://115.114.92.59:8080/services/DbpLocalServicesDb/konyadminconsole_customer_get";
	public static final String UpdateService_URL = "http://115.114.92.59:8080/services/DbpLocalServicesDb/konyadminconsole_customer_update";
	private static final Logger log = Logger.getLogger(SetNewPassword.class);

	@SuppressWarnings("rawtypes")
	@Override
	public Object invoke(String methodID, Object[] inputArray,
			DataControllerRequest dcRequest, DataControllerResponse dcResponse)
			throws Exception {
		Result result = new Result();
		Map inputParams = LoansUtilities.getInputParamMap(inputArray);

		if (preProcess(inputParams, dcRequest, result)) {
			String newPasswordValue=(String) inputParams.get(CUSTOMER_PASSWORD);
			inputParams.put(CUSTOMER_PASSWORD, newPasswordValue);

			//update the new password field in Customer table for given Customer_id
			String updateServiceURL = LoansServiceURLEnum.CUSTOMER_UPDATE.getServiceURL(dcRequest);
			
//			HTTPOperations.hitPOSTServiceAndGetResponse(updateServiceURL, (HashMap<String, String>) inputParams, null, null);
//			result=postProcess(inputParams, dcRequest, result);
			
			String customer_idValue=(String) inputParams.get(CUSTOMER_ID);
			JSONObject payloadJson=new JSONObject();
			payloadJson.put(CUSTOMER_ID, customer_idValue);
			payloadJson.put(CUSTOMER_PASSWORD, newPasswordValue);
			String payload=payloadJson.toString();
			
//			HTTPOperations.hitPOSTServiceAndGetResponse(updateServiceURL, (HashMap<String, String>) inputParams, null, null);
			
			String Response = sendPostRequest(updateServiceURL, payload);
			JSONObject responseJSON = null;
			if(Response != null){
				 responseJSON = new JSONObject(Response);
				 Object opstatusObject = responseJSON.get("opstatus");
					String opstatus = opstatusObject.toString();
					Object updatedRecordsObj = responseJSON.get("updatedRecords");
					String updatedRecords = updatedRecordsObj.toString();
//				Object CustomerArrayObject = responseJSON.get("customer");
				if(opstatus!=null && opstatus.equals("0") && updatedRecords.equals("1")){
					result.addParam(getParamObject("errmsg","Password has been set Successfully"));
					result.addParam(getParamObject("errcode","0"));
				}
				else{
					result.addParam(getParamObject("errmsg","Password change failed!"));
					result.addParam(getParamObject("errcode","9001"));
				}
			}
			else{
				log.debug("Response is Empty !");
			}
		}
		return result;
	}
	
	private Param getParamObject(String Key, String Value) {
		// TODO Auto-generated method stub
		Param param= new Param();
		param.setName(Key);
		param.setValue(Value);
		return param;
	}

	public static String sendPostRequest(String requestUrl, String payload) {
		String s="";
	    try {
	        URL url = new URL(requestUrl);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

	        connection.setDoInput(true);
	        connection.setDoOutput(true);
	        connection.setRequestMethod("POST");
	        connection.setRequestProperty("Accept", "application/json");
	        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
	        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
	        writer.write(payload);
	        writer.close();
	        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        StringBuffer jsonString = new StringBuffer();
	        String line;
	        while ((line = br.readLine()) != null) {
	                jsonString.append(line);
	        }
	        br.close();
	        connection.disconnect();
	        s=jsonString.toString();
	    } catch (Exception e) {
	            throw new RuntimeException(e.getMessage());
	    }
	    return s;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean preProcess(Map inputParams,
			DataControllerRequest dcRequest, Result result) {
		boolean status = true;
		String customer_idValue=(String) inputParams.get(CUSTOMER_ID);
		
		StringBuilder sb = new StringBuilder();
		if(LoansUtilities.isNotBlank(customer_idValue)) {
			sb.append(CUSTOMER_ID).append(LoansUtilitiesConstants.EQUAL).append(customer_idValue);
		}else{
			LoansUtilities.setValidationMsg("Customer id is blank", dcRequest, result);
			status = false;
		}
		inputParams.put(LoansUtilitiesConstants.FILTER, sb.toString());
		return status;
	}
	
	@SuppressWarnings("rawtypes")
	private Result postProcess(Map inputParams, DataControllerRequest dcRequest, Result result)
			throws MetricsException, ParseException {
		Result retVal = new Result();
		if (LoansUtilities.hasRecords(result)) {
			
		} else {
			
		}
		return retVal;
	}
}