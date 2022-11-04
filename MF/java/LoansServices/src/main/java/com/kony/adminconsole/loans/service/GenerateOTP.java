package com.kony.adminconsole.loans.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.kony.adminconsole.loans.utils.LoansUtilities;
import com.kony.adminconsole.loans.utils.LoansUtilitiesConstants;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.exceptions.MetricsException;

/**
 * @author KH2267 Dinesh
 *
 */
public class GenerateOTP implements JavaService2 {
	/**
	 * Constants to hold the field names as Strings 
	 */
	public static final String CUSTOMER_ID = "id";
	public static final String CUSTOMER_OTP = "Otp";
	private static final Logger log = Logger.getLogger(GenerateOTP.class);
	@SuppressWarnings("rawtypes")
	@Override
	public Object invoke(String methodID, Object[] inputArray,
			DataControllerRequest dcRequest, DataControllerResponse dcResponse)
			throws Exception {
		Result result = new Result();
		Map inputParams = LoansUtilities.getInputParamMap(inputArray);

		if (preProcess(inputParams, dcRequest, result)) {
			//logic to generate OTP
			String generatedOTP=getOTPString();

			inputParams.put(CUSTOMER_OTP, generatedOTP);
			//update the OTP and OtpGenaratedts fields in Customer table for given Customer_id
			String customer_idValue=(String) inputParams.get(CUSTOMER_ID);
			JSONObject payloadJson=new JSONObject();
			payloadJson.put(CUSTOMER_ID, customer_idValue);
			payloadJson.put(CUSTOMER_OTP, generatedOTP);
			String payload=payloadJson.toString();
			
			String updateServiceURL = LoansServiceURLEnum.CUSTOMER_UPDATE.getServiceURL(dcRequest);
//			updateServiceURL = UpdateService_URL;
//			HTTPOperations.hitPOSTServiceAndGetResponse(updateServiceURL, (HashMap<String, String>) inputParams, null, null);
			
			String Response = sendPostRequest(updateServiceURL,payload);
			JSONObject responseJSON = null;
			if(Response != null){
				 responseJSON = new JSONObject(Response);
				Object opstatusObject = responseJSON.get("opstatus");
				String opstatus = opstatusObject.toString();
				Object updatedRecordsObj = responseJSON.get("updatedRecords");
				String updatedRecords = updatedRecordsObj.toString();
				Object CustomerArrayObject = responseJSON.get("customer");
//			result=postProcess(inputParams, dcRequest, result);
			
				String getServiceURL = LoansServiceURLEnum.CUSTOMER_GET.getServiceURL(dcRequest);
	//			getServiceURL = GetService_URL;
				String Value = HTTPOperations.hitPOSTServiceAndGetResponse(getServiceURL, (HashMap<String, String>) inputParams, null, null);
				JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
				Object CustomerArrayObj = responseJSON.get("customer");
				Object OTPFromUpdateResponse=null;
				if(CustomerArrayObject instanceof JSONArray){
					JSONArray CustomerArray = new JSONArray(CustomerArrayObject.toString());
					if(CustomerArray.length()==1){
						OTPFromUpdateResponse = CustomerArray.getJSONObject(0).get("Otp");
					}
					else{
						log.debug("Customer Array doesn't contain any record for specific CustomerId"+customer_idValue+"Customer Object "+CustomerArrayObject.toString());
					}
				}
				else{
					if(CustomerArrayObject!=null){
						log.debug("Response from Update Call is not Expected Array"+CustomerArrayObject.toString());
					}
				}
				if(CustomerArrayObj instanceof JSONArray){
					JSONArray CustomerArray = new JSONArray(CustomerArrayObj.toString());
					if(CustomerArray.length()==1){
						ValueResponseJSON = CustomerArray.getJSONObject(0);
					}
					else{
						log.debug("Customer Array doesn't contain any record for specific CustomerId"+customer_idValue+"Customer Object "+CustomerArrayObject.toString());
					}
					if(ValueResponseJSON.get("Otp").toString()!=null && ValueResponseJSON.get("Otp").toString().equals(OTPFromUpdateResponse.toString())){
						result.addParam(getParamObject("errmsg","OTP Generated Successfully"));
						result.addParam(getParamObject("errcode","0"));
					}
					else{
						result.addParam(getParamObject("errmsg","OTP Generation Failed"));
						result.addParam(getParamObject("errcode","9001"));
					}
				}
			}
			else{
				log.debug("Response is Empty !");
			}
		}

		return result;
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
	
	public static String getOTPString() {
	    // It will generate 6 digit random Number.
	    // from 0 to 999999
	    Random rnd = new Random();
	    int number = rnd.nextInt(999999);

	    // this will convert any number sequence into 6 character.
	    return "123456";
//	    return String.format("%06d", number);
	}
	private Param getParamObject(String Key, String Value) {
		Param param= new Param();
		param.setName(Key);
		param.setValue(Value);
		return param;
	}
}