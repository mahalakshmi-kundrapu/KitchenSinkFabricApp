package com.kony.adminconsole.loans.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @author KH2302 Bhowmik
 *
 */

@SuppressWarnings({ "rawtypes", "unchecked" })
public class DpUtilities {

	private static final Logger LOGGER = Logger.getLogger(DpUtilities.class);

	/**
	 * mapToJsonObject method is used convert a map to JSONObject
	 * 
	 * @param inputmap
	 *            : Map as input
	 * @return JSONObject
	 */
	public JSONObject mapToJsonObject(Map inputmap) {
		JSONObject payload = new JSONObject();
		try {
			Iterator<?> itr = inputmap.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				payload.put(key, inputmap.get(key));
			}
		} catch (Exception e) {
			LOGGER.debug(e);
			throw e;
		}
		return payload;
	}

	/**
	 * convertJSONtoMap method is used convert a map to JSONObject
	 * 
	 * @param JSONObject
	 *            : JSONObject as input
	 * @return Map
	 */
	public Map convertJSONtoMap(JSONObject jsonObject) throws Exception {
		Map<String, String> input = new HashMap<String, String>();
		try {
			Iterator<?> itr = jsonObject.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				input.put(key, jsonObject.optString(key).toString());
			}
		} catch (Exception e) {
			LOGGER.debug(e);
			throw e;
		}
		return input;
	}

	/**
	 * appendJSONtoMap method appends all the fields from JSONObject in Map with
	 * comma(,) seperator.
	 * 
	 * @param Map<String,
	 *            String> : Map to which JSON value are to be appended
	 * @param JSONObject
	 *            : JSONObject whose values are to be appended
	 * @return Map
	 */
	public Map<String, String> appendJSONtoMap(Map<String, String> map, JSONObject jsonObject) throws Exception {
		try {
			if (map.isEmpty()) {
				map = convertJSONtoMap(jsonObject);
			} else {
				Iterator<?> mapitr = map.keySet().iterator();
				while (mapitr.hasNext()) {
					String mapkey = (String) mapitr.next();
					String jsonkey = null;
					jsonkey = jsonObject.optString(mapkey);
					map.merge(mapkey, jsonkey, (String oldVal, String newVal) -> oldVal.concat("," + newVal));
				}
			}
		} catch (Exception e) {
			LOGGER.debug(e);
			throw e;
		}
		return map;
	}
}
