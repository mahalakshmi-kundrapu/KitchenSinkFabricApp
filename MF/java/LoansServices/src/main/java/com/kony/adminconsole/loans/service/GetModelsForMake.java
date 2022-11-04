package com.kony.adminconsole.loans.service;

import java.util.HashMap;
import java.util.Map;

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

public class GetModelsForMake implements JavaService2{
	
	private static final Logger LOGGER = Logger.getLogger(GetModelsForMake.class);

	@SuppressWarnings("unchecked")
	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest dcRequest, DataControllerResponse dcResponse)
			throws Exception {
		Result result = new Result();
		Map<String, String> inputParams = (HashMap<String, String>)inputArray[1];
		try {
			if(inputParams.containsKey("VehicleType") && inputParams.containsKey("MakeName") && inputParams.containsKey("Year")) {
				String filter= "?$filter=ParentVehicleTypeName%20eq%20'"+inputParams.get("VehicleType")+"'%20and%20Make_Name%20eq%20'"+inputParams.get("MakeName")+"'%20and%20Year%20eq%20'"+inputParams.get("Year")+"'";
				result= CommonUtilities.getResultObjectFromJSONObject(hitVehicleModelsGetService(inputParams, filter, dcRequest));
			}
		}
		catch (Exception e)
		{
			LOGGER.debug(e);
			LoansException unknownException=new LoansException(ErrorCodeEnum.ERR_31000);
			result=unknownException.updateResultObject(result);
		}
		return result;
	}

	private JSONObject hitVehicleModelsGetService(Map<String, String>  inputParams,String filter , DataControllerRequest dcRequest) throws Exception {
		HashMap <String, String> customHeaders = new HashMap<String, String>();
		customHeaders.put("X-Kony-Authorization", dcRequest.getHeader("X-Kony-Authorization"));
		String value = HTTPOperations.hitGETServiceAndGetResponse(LoansServiceURLEnum.VEHICLE_MODELS_OBJECT.getServiceURL(dcRequest)+filter.replaceAll(" ", "%20"),(HashMap<String, String> ) customHeaders, dcRequest.getHeader("X-Kony-Authorization"));
		JSONObject valueResponseJSON= new JSONObject(value);
		JSONArray responseArray = valueResponseJSON.getJSONArray("records");
		if(responseArray.length() == 0){
			return null;
		}else{
			valueResponseJSON = new JSONObject();
			valueResponseJSON.put("Results", responseArray);
			return valueResponseJSON;
		}
	}
}


