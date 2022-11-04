package com.kony.adminconsole.loans.service.dpservices;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.http.HTTPOperations;

/**
 * @author KH2302 Bhowmik
 *
 */

public class HitDpCallable implements Callable<JSONObject> {

	/**
	 * Baseurl to make a https post call
	 */
	private String baseurl;
	/**
	 * tempmap contains body param to be passed for http call
	 */
	private Map<String, String> tempmap = new HashMap<String, String>();

	/**
	 * Class Constructor to initialize class variables
	 * 
	 * @param param1
	 *            set value to BaseUrl
	 * @param param2
	 *            set value to tempmap
	 */
	HitDpCallable(String param1, Map<String, String> param2) {
		this.baseurl = param1;
		this.tempmap = param2;
	}

	/**
	 * Auto implemented call method from the inherited abstract method Callable.
	 * Makes a call to given url and return response.
	 * 
	 * @return response
	 * @throws Exception
	 */
	@Override
	public JSONObject call() throws Exception {
		String readEndpointResponse = HTTPOperations.hitPOSTServiceAndGetResponse(baseurl,(HashMap<String, String>) tempmap, null, null);
		JSONObject response = new JSONObject(readEndpointResponse);
		return response;
	}
}