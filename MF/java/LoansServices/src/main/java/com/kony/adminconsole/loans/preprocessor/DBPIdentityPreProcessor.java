package com.kony.adminconsole.loans.preprocessor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.utils.EnvironmentConfigurationsHandler;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.kony.adminconsole.loans.utils.LoansUtilitiesConstants;
import com.konylabs.middleware.common.DataPreProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

/**
 * @author KH2303
 *
 */
public class DBPIdentityPreProcessor implements DataPreProcessor2 {

	private static final Logger log = Logger.getLogger(DBPIdentityPreProcessor.class);
	private static final Properties LoansConfigProperties = loadLoansConfigProps();

	@SuppressWarnings("rawtypes")
	@Override
	public boolean execute(HashMap inputParams, DataControllerRequest dcRequest, DataControllerResponse dcResponse,
			Result result) {
		try {
			log.info("[DBPIdentityPreProcessor] Entered PreProcessor");

			String claimsToken = callDBPIdentityServiceForAuthToken(dcRequest);
			Map<String, Object> headerMap = dcRequest.getHeaderMap();
			headerMap.put(LoansUtilitiesConstants.X_KONY_AUTHORIZATION_VALUE, claimsToken);
			log.info("[DBPIdentityPreProcessor] Claims token set in the HeaderMap");
		} catch (Exception e) {
			log.error("[DBPIdentityPreProcessor] Error occured in PreProcessor", e);
		}

		return true;
	}

	/**
	 * Method to hit DBP Identity service
	 * 
	 * @param dcRequest
	 *            - DataControllerRequest
	 * 
	 * @return claimsToken - Claims token value
	 *
	 */
	public String callDBPIdentityServiceForAuthToken(DataControllerRequest dcRequest) {
		JSONObject claimsToken = null;
		Map<String, String> inputHeader = new HashMap<String, String>();
		JSONObject params = new JSONObject();
		params.put(LoansUtilitiesConstants.SHARED_SECRET, EnvironmentConfigurationsHandler.getValue(LoansUtilitiesConstants.LOANS_SHARED_SECRET, dcRequest));
		String URL = EnvironmentConfigurationsHandler.getValue(LoansUtilitiesConstants.AC_DBP_AUTH_URL, dcRequest)
				+ LoansConfigProperties.getProperty(LoansUtilitiesConstants.DBP_IDENTITY);
		inputHeader.put(LoansUtilitiesConstants.DBP_APP_KEY, EnvironmentConfigurationsHandler.getValue(LoansUtilitiesConstants.AC_DBP_APP_KEY, dcRequest));
		inputHeader.put(LoansUtilitiesConstants.DBP_APP_SECRET, EnvironmentConfigurationsHandler.getValue(LoansUtilitiesConstants.AC_DBP_APP_SECRET, dcRequest));
		inputHeader.put(LoansUtilitiesConstants.DBP_REPORTING_PARAMS, EnvironmentConfigurationsHandler.getValue(LoansUtilitiesConstants.AC_DBP_AUTH_REPORTING_PARAMS, dcRequest));
		try {

			String Response = HTTPOperations.hitPOSTServiceAndGetResponse(URL, params, null, inputHeader);
			JSONObject ResponseJSON = new JSONObject(Response.replaceAll("\"", "\\\""));
			if (ResponseJSON != null) {
				if (ResponseJSON instanceof JSONObject) {
					claimsToken = (JSONObject) ResponseJSON.get(LoansUtilitiesConstants.CLAIMS_TOKEN);
				} else {
					log.error("Wrong Response received from AccessAPI" + Response);
				}
			}
		} catch (Exception e) {
			log.error("[DBPIdentityPreProcessor] Error occured in callDBPIdentityServiceForAuthToken", e);
		}
		return (String) claimsToken.get("value");
	}

	/**
	 * Method to load the LoansServiceURLCollection properties
	 * 
	 * @return properties - properties from LoansServiceURLCollection
	 *
	 */
	private static Properties loadLoansConfigProps() {
		Properties properties = new Properties();
		try (InputStream serviceConfigInputStream = LoansServiceURLEnum.class.getClassLoader()
				.getResourceAsStream("LoansServiceURLCollection.properties");) {
			properties.load(serviceConfigInputStream);
		} catch (Exception e) {
			log.error("[DBPIdentityPreProcessor] Error occured while loading LoansServiceURLCollection.properties", e);
		}
		return properties;
	}
}