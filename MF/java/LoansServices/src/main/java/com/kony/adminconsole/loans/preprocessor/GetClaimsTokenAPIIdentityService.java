package com.kony.adminconsole.loans.preprocessor;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.dto.JSONResponseHandlerBean;
import com.kony.adminconsole.commons.handler.EnvironmentConfigurationsHandler;
import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.utils.Executor;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.kony.adminconsole.loans.utils.LoansUtilitiesConstants;
import com.konylabs.middleware.common.DataPreProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.session.Session;

@SuppressWarnings("rawtypes")
public class GetClaimsTokenAPIIdentityService implements DataPreProcessor2 {

	static String claimsToken;
	private static final Logger log = Logger.getLogger(GetClaimsTokenAPIIdentityService.class);

	@Override
	public boolean execute(HashMap hashMap, DataControllerRequest request, DataControllerResponse response,
			Result resultObj) throws ParseException,Exception{
		boolean result = true;
		try {
			if (claimsToken == null || claimsToken.isEmpty()) {
				// hit identity and get new Claims Token
				getNewClaimsToken(request);
			} else {
				isClaimsTokenValid(request, hashMap);
			}
		}catch (Exception e) {
			log.error("Error in claims token preprocessor ", e);
		}
		return result;
	}

	public void isClaimsTokenValid(DataControllerRequest request, HashMap inputparams) {
		String value = HTTPOperations.hitPOSTServiceAndGetResponse(
				LoansServiceURLEnum.LOANTYPE_PING.getServiceURL(request), inputparams, null, claimsToken);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(value);
		if ((ValueResponseJSON.has("opstatus")) && (ValueResponseJSON.getInt("opstatus") != 0)) {
			getNewClaimsToken(request);
		} else {
			updateRequestHeader(request);
		}
	}

	public void getNewClaimsToken(DataControllerRequest request) {
		String newClaimsToken;
		try {
			newClaimsToken = callAccessAPIIntegrationServiceForAuthToken(request,
					request.getHeader("X-Kony-Authorization"));
			if (newClaimsToken != null) {
				claimsToken = newClaimsToken;
			}
			updateRequestHeader(request);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateRequestHeader(DataControllerRequest request) {
		// populate in the current request header. This value will be accessed
		// in the Java service to hit RDBMS service.
		Map<String, Object> headerMap = request.getHeaderMap();
		if (headerMap.containsKey("x-kony-authorization")) {
			headerMap.put("x-kony-authorization", claimsToken);
		}
		headerMap.put("X-Kony-AC-API-Access-By", "loans");
	}

	private static String callAccessAPIIntegrationServiceForAuthToken(DataControllerRequest request, String XKony)
			throws Exception {
		String ClaimsToken = null;
		try {
			JSONResponseHandlerBean Response = HTTPOperations.hitPOSTServiceAndGetJSONResponse(
					LoansServiceURLEnum.GET_TOKEN_CUSTOMAPI_GET.getServiceURL(request), null, XKony, null);
			JSONObject ResponseJSON = Response.getResponseAsJSONObject();
			if (ResponseJSON != null) {
				if (ResponseJSON instanceof JSONObject) {
					ClaimsToken = ResponseJSON.getString("Claims_Token");
					// log.debug("Claims_Token is empty or null in Response
					// "+Response.toString());
					// throw new Exception("Claims_Token is empty or null in
					// Response");
				} else {
					log.debug("Wrong Response received from AccessAPI" + Response.toString());
					throw new Exception("Wrong Response received from AccessAPI");
				}
			}
		} catch (Exception e) {
			log.debug("Unknown Error Occured " + e.getStackTrace());
			log.debug("Error from Previous Service" + e.getMessage());
			throw new Exception("Error in AccessAPI " + e.getMessage());
		}
		return ClaimsToken;
	}
}
