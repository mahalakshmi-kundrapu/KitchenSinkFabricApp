package com.kony.service.util;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <p>
 * Utility used to bind data to and from POJOs to JSON notation
 * </p>
 * <p>
 * Uses <a href="https://github.com/FasterXML/jackson-core">Jackson</a> library.
 * </p>
 * 
 * @author Venkateswara Rao Alla
 * @author Aditya Mankal
 *
 */
public class JSONUtilities {

	private static final Logger LOG = LogManager.getLogger(JSONUtilities.class);

	private JSONUtilities() {
		// Private Constructor
	}

	public static JSONObject getStringAsJSONObject(String jsonString) {
		JSONObject generatedJSONObject = new JSONObject();
		if (StringUtils.isBlank(jsonString))
			return null;
		try {
			generatedJSONObject = new JSONObject(jsonString);
			return generatedJSONObject;
		} catch (JSONException e) {
			LOG.error("Exception in parsing String to JSONObject. Exception:", e);
			return null;
		}
	}

	public static JSONArray getStringAsJSONArray(String jsonString) {
		JSONArray generatedJSONArray = new JSONArray();
		if (StringUtils.isBlank(jsonString))
			return null;
		try {
			generatedJSONArray = new JSONArray(jsonString);
			return generatedJSONArray;
		} catch (JSONException e) {
			LOG.error("Exception in parsing String to JSONArray. Exception:", e);
			return null;
		}
	}

	public static Map<String, String> getJSONAsMap(JSONObject inputJSON) {
		try {
			Map<String, String> map = new HashMap<String, String>();
			for (String key : inputJSON.keySet()) {
				map.put(key, inputJSON.optString(key));
			}
			return map;
		} catch (Exception e) {
			LOG.error("Exception in parsing JSONObject to Map. Exception:", e);
			return null;
		}
	}

	public static JSONObject getMapAsJSONObject(Map<String, String> inputMap) {
		try {
			Set<Entry<String, String>> inputMapEntrySet = inputMap.entrySet();
			JSONObject jsonObject = new JSONObject();
			for (Entry<String, String> entry : inputMapEntrySet) {
				jsonObject.put(entry.getKey(), entry.getValue());
			}
			return jsonObject;
		} catch (Exception e) {
			LOG.error("Exception in parsing Map to JSONObject. Exception:", e);
			return null;
		}
	}

	/**
	 * perfectly fine to create once & reuse.
	 * <p>
	 * More on this: http://stackoverflow.com/a/3909846/340290
	 * https://github.com/FasterXML/jackson-databind#1-minute-tutorial-pojos-to-json-and-back
	 */
	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	/**
	 * Converts Java object into JSON string
	 * 
	 * @param obj
	 * @return
	 * @throws IOException
	 */
	public static String stringify(Object obj) throws IOException {
		return OBJECT_MAPPER.writeValueAsString(obj);
	}

	/**
	 * Converts {@link Collection} of polymorphic Java objects into JSON string preserving type info. This is convenient
	 * method for classes which use {@link JsonTypeInfo} annotation.
	 * 
	 * @param coll
	 * @param rootType
	 * @return
	 * @throws IOException
	 */
	public static <T> String stringifyCollectionWithTypeInfo(Collection<? extends T> coll, Class<T> rootType)
			throws IOException {
		return OBJECT_MAPPER
				.writerFor(OBJECT_MAPPER.getTypeFactory().constructCollectionType(Collection.class, rootType))
				.writeValueAsString(coll);
	}

	/**
	 * Converts JSON string into Java object
	 * <p>
	 * usage: <code>DataObject dataObject = parse(jsonStr, DataObject.class);</code>
	 * </p>
	 * 
	 * @param json
	 * @param typeReference
	 * @return
	 * @throws IOException
	 */
	public static <T> T parse(String json, Class<T> clazz) throws IOException {
		return OBJECT_MAPPER.readValue(json, clazz);
	}

	/**
	 * Converts JSON string into Java object
	 * <p>
	 * usage: <code>List<DataObject> parsedData = parse(jsonStr, new TypeReference<List<DataObject>>() { });</code>
	 * </p>
	 * 
	 * @param json
	 * @param typeReference
	 * @return
	 * @throws IOException
	 */
	public static <T> T parse(String json, TypeReference<?> typeReference) throws IOException {
		return OBJECT_MAPPER.readValue(json, typeReference);
	}

	/**
	 * Converts JSON string into Java List of generic type
	 * <p>
	 * usage: <code>List<DataObject> parsedData = parseAsList(jsonStr, DataObject.class);</code>
	 * </p>
	 * 
	 * @param json
	 * @param type
	 * @return
	 * @throws IOException
	 */
	public static <T> List<T> parseAsList(String json, Class<T> type) throws IOException {
		return OBJECT_MAPPER.readValue(json, OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, type));
	}

	/**
	 * Converts JSON string into Java Map of generic type
	 * <p>
	 * usage: <code>List<DataObject> parsedData = parseAsList(jsonStr, DataObject.class);</code>
	 * </p>
	 * 
	 * @param json
	 * @param type
	 * @return
	 * @throws IOException
	 */
	public static <K, V> Map<K, V> parseAsMap(String json, Class<K> keyType, Class<V> valueType) throws IOException {
		return OBJECT_MAPPER.readValue(json,
				OBJECT_MAPPER.getTypeFactory().constructMapType(Map.class, keyType, valueType));
	}

	public static <T> JSONArray getCollectionAsJSONArray(Collection<T> list) throws IOException {
		String strigifiedList = stringify(list);
		return getStringAsJSONArray(strigifiedList);
	}

}
