package com.kony.service.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Class containing common JSON Parse methods
 *
 * @author Aditya Mankal
 */
public class JSONParser {

	/**
	 * Method to get the JSON Paths of all Elements of a JSON
	 * 
	 * @param inputJSON
	 * @return
	 */
	public static List<String> getPaths(String inputJSON) {
		List<String> pathList = new ArrayList<String>();
		JSONObject object = new JSONObject(inputJSON);
		String jsonPath = "$";
		if (inputJSON != JSONObject.NULL) {
			readObject(pathList, object, jsonPath);
		}
		return pathList;
	}

	/**
	 * Method to parse a JSON Object and extract JSON Paths
	 * 
	 * @param pathList
	 * @param object
	 * @param jsonPath
	 */
	private static void readObject(List<String> pathList, JSONObject object, String jsonPath) {
		Iterator<String> keysItr = object.keys();
		String parentPath = jsonPath;
		while (keysItr.hasNext()) {
			String key = keysItr.next();
			Object value = object.get(key);
			jsonPath = parentPath + "." + key;

			if (value instanceof JSONArray) {
				readArray(pathList, (JSONArray) value, jsonPath);
			} else if (value instanceof JSONObject) {
				readObject(pathList, (JSONObject) value, jsonPath);
			} else { // is a value
				pathList.add(jsonPath);
			}
		}
	}

	/**
	 * Method to parse a JSON Array and extract JSON Paths
	 * 
	 * @param pathList
	 * @param object
	 * @param jsonPath
	 */
	private static void readArray(List<String> pathList, JSONArray array, String jsonPath) {
		String parentPath = jsonPath;
		for (int i = 0; i < array.length(); i++) {
			Object value = array.get(i);
			jsonPath = parentPath + "[" + i + "]";

			if (value instanceof JSONArray) {
				readArray(pathList, (JSONArray) value, jsonPath);
			} else if (value instanceof JSONObject) {
				readObject(pathList, (JSONObject) value, jsonPath);
			} else { // is a value
				pathList.add(jsonPath);
			}
		}
	}

}
