package com.kony.adminconsole.loans.service;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.errorhandling.LoansException;
import com.kony.adminconsole.loans.service.messaging.SendEmail;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

public class ConfigurationServices implements JavaService2 {
	private static final Logger logger = Logger.getLogger(SendEmail.class);

	public ConfigurationServices() {
	}

	public Object invoke(String arg0, Object[] arg1, DataControllerRequest arg2, DataControllerResponse arg3)
			throws Exception {
		Result result=new Result();
		try {
			String urlBase = LoansServiceURLEnum.CONFIGURATIONS_GET.getServiceURL(arg2);
			Map inputMap = (Map) arg1[1];
			result=getConfigurationsMasters(urlBase, inputMap, arg2);
		} catch (LoansException loansException) {
			result = loansException.constructResultObject();
		} catch (Exception e) {
			result = ErrorCodeEnum.ERR_31000.constructResultObject();
			logger.error(e.getMessage());
		}
		return result;
	}

	public Result getConfigurationsMasters(String urlBase, Map inputMap, DataControllerRequest dcRequest) throws Exception {
		Result res = new Result();
		try{
			String tempTime = null;
			String urlBaseMasters = null;
			JSONObject jsonObj = new JSONObject();
			String greatest_timestamp_String = null;

			String input = (String) inputMap.get("id1");
			int input_count = StringUtils.countMatches(input, "|");
			String id1val;
			JSONObject id2;
			if (input_count == 1) {
				id2 = new JSONObject(input.substring(input.indexOf("|") + 1, input.length()));
				id1val = input.substring(0, input.indexOf("|"));
			} else {
				id2 = null;
				id1val = input;
			}

			urlBaseMasters = urlBase + "loansconfigurationmasters_get?$filter=";

			String[] combinations = id1val.split(";");
			int i = 0;
			String user_id = new String();
			String downloadServerKeys = new String();
			Record recordResult = new Record();
			Record updatedConfigurations = new Record();
			updatedConfigurations.setID("updatedConfigurations");
			Record timeStamp = new Record();
			timeStamp.setID("timeStamp");
			Record finalBundlesRecord = new Record();
			finalBundlesRecord.setID("finalBundles");
			recordResult.setID("output");
			LinkedHashSet<String> finalBundles = new LinkedHashSet();
			LinkedHashSet<String> bundles_in_combination = new LinkedHashSet();

			String localTime = new String();
			if (id2 != null) {
				localTime = id2.get("LastUpdatedDateTime").toString();
			}

			while (i < combinations.length) {
				String[] key_val = combinations[i].split(":");
				if (key_val[0].equals("user_id")) {
					user_id = key_val[1];
				}
				if (key_val[0].equalsIgnoreCase("downloadServerKeys")) {
					downloadServerKeys = key_val[1];
				}
				if (!key_val[0].equalsIgnoreCase("downloadServerKeys")) {
					urlBaseMasters = urlBaseMasters + URLEncoder.encode(" ", "UTF-8")
							+ URLEncoder.encode(key_val[0], "UTF-8") + URLEncoder.encode(" eq ", "UTF-8")
							+ URLEncoder.encode(key_val[1], "UTF-8") + URLEncoder.encode(" and ", "UTF-8");
				}
				i++;
				if (i == combinations.length) {
					urlBaseMasters = urlBaseMasters.substring(0, urlBaseMasters.length() - 5);
				}
			}

			JSONObject bundleTemp = null;
			HashMap<String, String> customHeaders = new HashMap();
			Map<String, String> inputParams = new HashMap<String, String>();
			customHeaders.put("X-Kony-Authorization", dcRequest.getHeader("X-Kony-Authorization"));
			String Value = HTTPOperations.hitPOSTServiceAndGetResponse(urlBaseMasters,
					(HashMap<String, String>) inputParams, customHeaders, null);
			bundleTemp = CommonUtilities.getStringAsJSONObject(Value);
			JSONArray recs = bundleTemp.getJSONArray("loansconfigurationmasters");
			for (int k = 0; k < recs.length(); k++) {
				JSONObject rec = recs.getJSONObject(k);
				if (((user_id.equals(null)) || (user_id.isEmpty()))
						&& ((rec.get("user_id").toString().equals("null")) || (rec.get("user_id").toString().isEmpty()))) {
					String[] bundles = rec.get("bundle_id").toString().split(",");
					for (int x = 0; x < bundles.length; x++) {
						if ((bundles_in_combination.isEmpty()) || (!bundles_in_combination.contains(bundles[x]))) {
							bundles_in_combination.add(bundles[x]);
							if ((finalBundles.isEmpty()) || (!finalBundles.contains(bundles[x]))) {
								finalBundles.add(bundles[x]);
							}
						}
					}
				}
				if ((!user_id.equals(null)) && (!user_id.isEmpty()) && (rec.get("user_id").toString().equals(user_id))) {
					String[] bundles = rec.get("bundle_id").toString().split(",");
					for (int x = 0; x < bundles.length; x++) {
						if ((bundles_in_combination.isEmpty()) || (!bundles_in_combination.contains(bundles[x]))) {
							bundles_in_combination.add(bundles[x]);
							if ((finalBundles.isEmpty()) || (!finalBundles.contains(bundles[x]))) {
								finalBundles.add(bundles[x]);
							}

						}
					}
				}
			}

			finalBundlesRecord.setParam(new Param("bundles", StringUtils.join(finalBundles, ","), "JSON"));
			finalBundlesRecord.setParam(new Param("downloadServerKeys", downloadServerKeys, "JSON"));
			String[] finalBundlesString = StringUtils.join(finalBundles, ",").split(",");

			for (int bundle_num = 0; bundle_num < finalBundlesString.length; bundle_num++) {
				if (finalBundlesString[bundle_num].length() != 0) {
					JSONObject updatedConfigBundleReturned = getConfigBundles(urlBase, finalBundlesString[bundle_num],
							dcRequest);
					String serverTime = new String();
					serverTime = updatedConfigBundleReturned.get("lastUpdatedTime").toString();

					String updatedTime = new String();
					if (id2 != null)
						updatedTime = getUpdateTime(serverTime, localTime);
					if (updatedTime != null) {

						jsonObj.put("key", updatedConfigBundleReturned.get("config_key").toString());
						jsonObj.put("value", updatedConfigBundleReturned.get("config_value").toString());
						jsonObj.put("type", updatedConfigBundleReturned.get("config_type").toString());
						jsonObj.put("description", updatedConfigBundleReturned.get("description").toString());

						updatedConfigurations
								.setParam(new Param(finalBundlesString[bundle_num], jsonObj.toString(), "JSON"));
					}

					if (tempTime == null) {
						tempTime = serverTime;

					} else {
						SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.ENGLISH);
						Date da = df.parse(tempTime);
						double timestamp = Math.floor(da.getTime() / 1000L);

						SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.ENGLISH);
						Date da1 = df1.parse(serverTime);
						double timestamp1 = Math.floor(da1.getTime() / 1000L);

						int resVal = Double.compare(timestamp, timestamp1);
						if (resVal < 0) {
							tempTime = serverTime;
						}
					}
					greatest_timestamp_String = tempTime;
				}
			}

			timeStamp.setParam(new Param("LastUpdatedDateTime", greatest_timestamp_String, "JSON"));

			recordResult.setRecord(timeStamp);
			recordResult.setRecord(updatedConfigurations);
			recordResult.setRecord(finalBundlesRecord);
			res.setRecord(recordResult);
			return res;
		}catch(LoansException loansException){
			throw loansException;
		}catch(Exception e){
			LoansException loansException=new LoansException(ErrorCodeEnum.ERR_31000);
				logger.error(e.getMessage());
			throw loansException;
		}
		
	}

	public JSONObject getConfigBundles(String urlBase, String bundle_id, DataControllerRequest dcRequest)
			throws Exception {
		try{
			String urlBaseConfigurations = urlBase + "loansconfigurations_get?$filter=";
			urlBaseConfigurations = urlBaseConfigurations
					+ URLEncoder.encode(new StringBuilder("bundle_id eq ").append(bundle_id).toString(), "UTF-8");
			JSONObject jsonToBeReturned = new JSONObject();
			JSONObject bundleTemp = null;
			HashMap<String, String> customHeaders = new HashMap();
			customHeaders.put("X-Kony-Authorization", dcRequest.getHeader("X-Kony-Authorization"));
			Map<String, String> inputParams = new HashMap<String, String>();
			String Value = HTTPOperations.hitPOSTServiceAndGetResponse(urlBaseConfigurations,
					(HashMap<String, String>) inputParams, customHeaders, null);
			bundleTemp = CommonUtilities.getStringAsJSONObject(Value);
			JSONArray temp = bundleTemp.getJSONArray("loansconfigurations");
			jsonToBeReturned = temp.getJSONObject(0);

			return jsonToBeReturned;
		}catch(Exception e){
			LoansException loansException=new LoansException(ErrorCodeEnum.ERR_31000);
				logger.error(e.getMessage());
			throw loansException;
		}
		
	}

	public String getUpdateTime(String serverTime, String localTime) throws LoansException {
		try{
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.ENGLISH);
			Date da = df.parse(serverTime);
			Date db = df.parse(localTime);
			double serverTimeStamp = Math.floor(da.getTime() / 1000L);
			double localTimeStamp = Math.floor(db.getTime() / 1000L);
			int resval = Double.compare(serverTimeStamp, localTimeStamp);
			if (resval > 0) {
				return serverTime;
			}
			return null;
		}catch(Exception e){
			LoansException loansException=new LoansException(ErrorCodeEnum.ERR_31000);
				logger.error(e.getMessage());
			throw loansException;
		}
		
	}
}