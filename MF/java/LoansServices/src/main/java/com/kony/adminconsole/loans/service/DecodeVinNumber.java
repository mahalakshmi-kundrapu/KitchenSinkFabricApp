package com.kony.adminconsole.loans.service;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.errorhandling.LoansException;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

public class DecodeVinNumber implements JavaService2{
	
	protected static final ResourceBundle VEHICLECONFIGPROP = ResourceBundle.getBundle("VehicleConfigurations");
	private static final Logger LOGGER = Logger.getLogger(DecodeVinNumber.class);
	
	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest dcRequest, DataControllerResponse dcResponse) throws Exception {
		Result result = new Result();
		Map<String, String> inputParams = (HashMap<String, String>)inputArray[1];
        try {
        	if(inputParams.containsKey("VIN")) {
        		String Value = HTTPOperations.hitPOSTServiceAndGetResponse(LoansServiceURLEnum.NHTSAVEHICLEAPI_DECODEVINVALUES.getServiceURL(dcRequest), inputParams, null, null);
        		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
        		JSONArray ValueResponseArray = ValueResponseJSON.getJSONArray("Results");
        		JSONObject payload= ValueResponseArray.getJSONObject(0);
        		if(!payload.get("VehicleType").toString().isEmpty()){
        		String vehicleType = payload.get("VehicleType").toString().replaceAll(" ", "_");
        		if(VEHICLECONFIGPROP.containsKey("DECODE_"+vehicleType)){
        			String ConvertedMake=VEHICLECONFIGPROP.getString("DECODE_"+vehicleType);
        			payload.put("VehicleType", ConvertedMake);
        			ValueResponseArray.put(0,payload);
        			ValueResponseJSON.put("Results", ValueResponseArray);
        		} else{
        			throw new LoansException(ErrorCodeEnum.ERR_36002);
        		}
        		} else{
        			throw new LoansException(ErrorCodeEnum.ERR_36001);
        		}
        		result= CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
			}
		}catch (LoansException exception) {
			LOGGER.debug(exception);
			result=exception.updateResultObject(result);
		} catch (Exception e) {
			LOGGER.debug(e);
			LoansException unknownException=new LoansException(ErrorCodeEnum.ERR_31000);
			result=unknownException.updateResultObject(result);
		}
		return result;
	}

}
