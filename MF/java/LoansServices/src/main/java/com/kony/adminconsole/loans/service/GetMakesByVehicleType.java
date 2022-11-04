package com.kony.adminconsole.loans.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

public class GetMakesByVehicleType implements JavaService2{
	
	private static final Logger LOGGER = Logger.getLogger(GetMakesByVehicleType.class);

	@SuppressWarnings("unchecked")
	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest dcRequest, DataControllerResponse dcResponse)
			throws Exception {
		Result result = new Result();
		Map<String, String> inputParams = (HashMap<String, String>)inputArray[1];
		Set<String> makeList = new HashSet<String>();
		try {
			if(inputParams.containsKey("VehicleType")) {
				String filter= "?$filter=ParentVehicleTypeName%20eq%20'"+inputParams.get("VehicleType").replaceAll(" ", "%20")+"'";
				JSONArray responseArray = hitVehicleMakesGetService(inputParams, filter, dcRequest);
				for(int j=0;j<responseArray.length();j++){
					JSONObject payload= responseArray.getJSONObject(j);
					makeList.add(payload.getString("MakeName")); 
				}
				result= CommonUtilities.getResultObjectFromJSONObject(getJsonObjectfromSet("MakeName", makeList));
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

	private JSONArray hitVehicleMakesGetService(Map<String, String>  inputParams,String filter , DataControllerRequest dcRequest) throws Exception {
		HashMap <String, String> customHeaders = new HashMap<String, String>();
		customHeaders.put("X-Kony-Authorization", dcRequest.getHeader("X-Kony-Authorization"));
		String value = HTTPOperations.hitGETServiceAndGetResponse(LoansServiceURLEnum.VEHICLE_MAKES_OBJECT.getServiceURL(dcRequest)+filter,(HashMap<String, String> ) customHeaders, dcRequest.getHeader("X-Kony-Authorization"));
		JSONObject valueResponseJSON= new JSONObject(value);
		JSONArray responseArray = valueResponseJSON.getJSONArray("records");
		if(responseArray.length() == 0){
			return null;
		}else{
			return responseArray; 
		}
	}
	
	private static JSONObject getJsonObjectfromSet(String name, Set<String> list){
		JSONObject json = new JSONObject();
		JSONArray array = new JSONArray();
		for(String value : list){
			JSONObject tempjson = new JSONObject();
			tempjson.put(name,value);
			array.put(tempjson);
		}
		json.put("Results", array);
		return json;
	}
}


