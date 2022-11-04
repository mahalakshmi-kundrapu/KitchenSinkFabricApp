package com.kony.adminconsole.loans.service.nhtsaservices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.errorhandling.LoansException;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * @author KH2302 Bhowmik
 *
 */

public class FetchVehicleModels implements JavaService2{

	/**
	 * Logger logger used to log the debug/info/error statements.
	 */
	private static final Logger LOGGER = Logger.getLogger(FetchVehicleModels.class);
	/**
	 * This field holds the ResourceBundle instance for
	 * VehicleConfigurations.properties which contains all the corresponding
	 * configurations and urls.
	 */
	protected static final ResourceBundle VEHICLECONFIGPROP = ResourceBundle.getBundle("VehicleConfigurations");
	/**
	 * batchSize contains value for number of records to be fetched
	 * single batch.
	 */
	static final int BATCHSIZE = Integer.parseInt(VEHICLECONFIGPROP.getString("batchsize"));
	/**
	 * ExecutorService is used for thread-handling on the Java platform and
	 * provides methods to manage the progress-tracking and termination of
	 * asynchronous tasks
	 */
	protected final ExecutorService executor = Executors.newWorkStealingPool(50);

	/**
	 * 
	 * This method is executed automatically by middleware as soon as the
	 * request is sent by the user.
	 * 
	 * @param methodID
	 *            Name of operation that is being executed.
	 * @param inputArray
	 *            request properties such as SESSIONID,Preprocessor name,Method
	 *            name,userAgent. The request payload. Service execution time.
	 * @param dcRequest
	 *            Session Details,RemoteAdress,HeaderMap,files
	 * @param dcResponse
	 *            Contains Charset Encoding,devicecookies,deviceheaders
	 * @return result Contains Object returned to the console after processing
	 *         the request.
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest dcRequest, DataControllerResponse dcResponse) throws Exception {
		Result result = new Result();
		try{
			int batch = 0;
			Map<String, String> inputParams = (HashMap<String, String>)inputArray[1];
			while (true) {
				List<Future<JSONObject>> list = new ArrayList<Future<JSONObject>>();
				JSONArray vehicleTypes = hitVehicleMakeGetService((HashMap<String, String>)inputParams, dcRequest, batch);
				if(vehicleTypes==null)break;
				for(int i=0; i<vehicleTypes.length();i++){
					JSONObject object = vehicleTypes.getJSONObject(i);
					Future<JSONObject> future = executor.submit(new FetchVehicleModelsCallable(object , dcRequest));
					list.add(future);
				}
				for (Future<JSONObject> fut : list) {
					fut.get();
				}
				batch = batch + BATCHSIZE;
			}
			result=ErrorCodeEnum.ERR_33200.updateResultObject(result);
		} catch(Exception e){
			LOGGER.debug(e);
			LoansException unknownException=new LoansException(ErrorCodeEnum.ERR_31000);
			result=unknownException.updateResultObject(result);
		}
		return result;
	}

	/**
	 * hitVehicleMakeGetService method is used to hit get for VehicleMake api
	 * @param inputParams
	 *            contains any filter params if required to append with url.
	 * @param dcRequest
	 *            contains Session Details,RemoteAdress,HeaderMap,files used to
	 *            fetch base URL.
	 * @param batch
	 *            contains batch size to fetch.
	 * @return
	 */
	private JSONArray hitVehicleMakeGetService(HashMap<String, String> inputParams, DataControllerRequest dcRequest, int batch) {
		HashMap<String, String> customHeaders = new HashMap<String, String>();
		String filter="";
		if(inputParams.containsKey("MakeName") && !inputParams.get("MakeName").isEmpty())
		{
			filter = "?$filter=MakeName%20eq%20'"+inputParams.get("MakeName").replaceAll(" ","%20").replaceAll("\t", "").replaceAll("\"", "%22")+"'&$orderby=id&$top=" + BATCHSIZE + "&$skip=" + batch;
		}else {
			filter = "?$orderby=id&$top=" + BATCHSIZE + "&$skip=" + batch;
		}
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

}
