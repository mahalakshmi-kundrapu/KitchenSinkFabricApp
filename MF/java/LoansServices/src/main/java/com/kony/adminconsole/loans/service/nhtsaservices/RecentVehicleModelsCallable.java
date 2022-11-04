package com.kony.adminconsole.loans.service.nhtsaservices;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.konylabs.middleware.controller.DataControllerRequest;

/**
 * @author KH2302 Bhowmik
 *
 */

public class RecentVehicleModelsCallable implements Callable<JSONObject> {
	
	/**
	 * Logger logger used to log the debug/info/error statements.
	 */
	private static final Logger LOGGER = Logger.getLogger(FetchVehicleMakes.class);
	/**
	 * contains Session Details,RemoteAdress,HeaderMap,files used to
	 * fetch base URL.
	 */
	private DataControllerRequest dcRequest;
	/**
	 * Object containing parameters to hit NHTSA
	 */
	private JSONObject object;
	/**
	 * This field holds the ResourceBundle instance for
	 * VehicleConfigurations.properties which contains all the corresponding
	 * configurations and urls.
	 */
	protected static final ResourceBundle VEHICLECONFIGPROP = ResourceBundle.getBundle("VehicleConfigurations");
	
	RecentVehicleModelsCallable(JSONObject param1, DataControllerRequest param2){
		this.object=param1;
		this.dcRequest=param2;
	}
	
	@Override
	public JSONObject call() throws Exception {
		try{
			JSONObject response=null;
			Map<String, String> inputParams = new HashMap<String, String>();
			inputParams.put("MakeId", object.get("MakeId").toString());
			inputParams.put("VehicleTypeName", object.getString("VehicleTypeName").replaceAll("\t", ""));
			LocalDate currentDate = LocalDate.now();
			for(int year=currentDate.getYear()+1;year>currentDate.getYear()-1;year--){
				try{
					inputParams.put("Year", String.valueOf(year));
					JSONObject recordsJSON = new JSONObject();
					JSONArray arrayJSON  = hitGetModelsForMakeIdYear(inputParams, dcRequest);
					for(int j=0; j<arrayJSON.length();j++){
						JSONObject obj = arrayJSON.getJSONObject(j);
						obj.put("ParentVehicleTypeName", object.getString("ParentVehicleTypeName").replaceAll("\t", ""));
						obj.put("Year", String.valueOf(year));
					}
					recordsJSON.put("records", arrayJSON);
					response = hitVehicleModelsCreateService(recordsJSON, dcRequest);
					LOGGER.debug(response);
				} catch(Exception e){
					LOGGER.debug("Error fetching MakeId-" + object.getInt("MakeId")+" VehicleTypeName-"+object.getString("VehicleTypeName").replaceAll("\t", "")+ ": "+ e);
				}
			}
			return response;
		} catch(Exception e){
			LOGGER.debug("Error fetching MakeId-" + object.getInt("MakeId")+" VehicleTypeName-"+object.getString("VehicleTypeName").replaceAll("\t", "")+ ": "+ e);
			return null;
		}
	}


	
	/**
	 * @param inputParams
	 *            contains any filter params if required to append with url.
	 * @param dcRequest
	 *            contains Session Details,RemoteAdress,HeaderMap,files used to
	 *            fetch base URL.
	 * @return
	 */
	private JSONArray hitGetModelsForMakeIdYear(Map<String, String> inputParams, DataControllerRequest dcRequest) {
		String value = HTTPOperations.hitPOSTServiceAndGetResponse(LoansServiceURLEnum.NHTSAVEHICLEAPI_GETMODELSFORMAKEIDYEAR.getServiceURL(dcRequest), inputParams, null, null);
		if(isJson(value)){
			JSONObject valueResponseJSON= new JSONObject(value);
			return valueResponseJSON.getJSONArray("Results");
		} else
			return null;
	}
	
	/**
	 * hitVehicleModelsCreateService method is used to get create for VehicleModels api
	 * @param recordsJSON
	 *            Contains list of records to be created.
	 * @param dcRequest
	 *            contains Session Details,RemoteAdress,HeaderMap,files used to
	 *            fetch base URL.
	 * @return
	 */
	private JSONObject hitVehicleModelsCreateService(JSONObject recordsJSON, DataControllerRequest dcRequest) {
		HashMap<String, String> customHeaders = new HashMap<String, String>();
		customHeaders.put("X-Kony-Authorization", dcRequest.getHeader("X-Kony-Authorization"));
		String value = HTTPOperations.hitPUTServiceAndGetResponse(LoansServiceURLEnum.VEHICLE_MODELS_OBJECT.getServiceURL(dcRequest), recordsJSON, null, customHeaders);
		JSONObject valueResponseJSON= new JSONObject(value);
		if(!"0".equals(valueResponseJSON.get("opstatus").toString())){
			value = HTTPOperations.hitPOSTServiceAndGetResponse(LoansServiceURLEnum.VEHICLE_MODELS_OBJECT.getServiceURL(dcRequest), recordsJSON, null, customHeaders);
			valueResponseJSON= new JSONObject(value);
		}
		return valueResponseJSON;
	}

	/**
	 * isJson method return true boolean value for valid json.
	 * @param value
	 * @return
	 */
	private boolean isJson(String value) {
		try{
			JSONObject object = new JSONObject(value);
			if(object.getJSONArray("Results").length()==0) return false;
			return true;
		} catch(Exception e){
			return false;
		}
	}
}
