package com.kony.service.identity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.kony.service.constants.IdentityConstants;
import com.kony.service.core.http.HTTPConnector;
import com.kony.service.dto.HTTPResponse;
import com.kony.service.dto.IdentityResponse;
import com.kony.service.exception.AuthenticationFailureException;
import com.kony.service.util.JSONUtilities;
import com.sun.mail.util.SocketConnectException;

/**
 * Class to perform Anonyomous App User Login
 * 
 * @author Aditya Mankal
 *
 */
public class AnonymousAppUserIdentityManager implements IdentityManager {

	private static final Logger LOG = LogManager.getLogger(AuthenticatedAppUserIdentityManager.class);
	private static final double BUFFER_DURATION = 8; // Approximate Time taken by an Identity Service

	private IdentityResponse identityResponse = null;
	private Date authTokenFetchDateTime = null;// Last Retrieval Date of Authentication Token

	private String appKey;
	private String appSecret;
	private String authServiceURL;

	private int authTokenValidityInSeconds;

	/**
	 * 
	 */
	public AnonymousAppUserIdentityManager() {
		super();
	}

	/**
	 * @param identityService
	 */
	public AnonymousAppUserIdentityManager(String authServiceURL, String appKey, String appSecret, int authTokenValidityInSeconds) {
		this.appKey = appKey;
		this.appSecret = appSecret;
		this.authServiceURL = authServiceURL;
		this.authTokenValidityInSeconds = authTokenValidityInSeconds;
	}

	/**
	 * 
	 * Executes the Identity Request and returns Response
	 * 
	 * @return the identityServiceResponse
	 */
	@Override
	public IdentityResponse executeIdentityRequest() throws AuthenticationFailureException {

		if (identityResponse == null || StringUtils.isBlank(identityResponse.getAuthToken()) || hasAuthTokenExpired()) {

			LOG.debug("Executing Anonymous App User Login:" + authServiceURL);
			authTokenFetchDateTime = new Date();

			Map<String, String> requestHeaders = new HashMap<>();
			requestHeaders.put(IdentityConstants.X_KONY_APP_KEY, appKey);
			requestHeaders.put(IdentityConstants.X_KONY_APP_SECRET, appSecret);

			LOG.debug("Auth Service URL:" + authServiceURL);
			LOG.debug("Auth Service Request Headers:" + JSONUtilities.getMapAsJSONObject(requestHeaders).toString());

			// Execute Identity Request
			try {
				HTTPConnector httpConnInstance = new HTTPConnector(HttpPost.METHOD_NAME, authServiceURL, StringUtils.EMPTY, requestHeaders);
				HTTPResponse httpResponse = httpConnInstance.executeClientAndSendRequest();
				identityResponse = parseIdentityServiceResponse(httpResponse);
			} catch (SocketConnectException sce) {
				LOG.error("Socket Timeout in executing Identity Request. Exception:", sce);
				throw new AuthenticationFailureException("Authentication Failure. Socket Timeout in executing Identity Request:");
			} catch (Exception e) {
				LOG.error("Exception in Executing Identity Request", e);
				throw new AuthenticationFailureException("Authentication Failure. Exception:" + e.getMessage());
			}

		}
		return identityResponse;
	}

	/**
	 * 
	 * Parses the Identity Service Response and returns the DTO
	 * 
	 * @param httpResponse
	 * @return identityResponse
	 */
	private IdentityResponse parseIdentityServiceResponse(HTTPResponse httpResponse) {

		boolean isLoginSuccessful = false;
		String authToken = null, userAttributes = null, securityAttributes = null;
		String responseBody = null;
		try {

			responseBody = httpResponse.getResponseBody();
			JSONObject responseBodyJSON = JSONUtilities.getStringAsJSONObject(responseBody);
			if (responseBodyJSON != null) {

				// Parse Authentication Token
				if (!responseBodyJSON.isNull(IdentityConstants.AUTH_TOKEN_KEY)) {
					authToken = responseBodyJSON.getJSONObject(IdentityConstants.AUTH_TOKEN_KEY).optString("value");
					LOG.debug("Extracted Auth Token for Auth Service:" + this.authServiceURL);
					LOG.debug("Auth Token:" + authToken);
					isLoginSuccessful = true;
				}

			}
		} catch (Exception e) {
			LOG.error("Exception in parsing Identity Service Response. Exception:", e);
			LOG.debug("Identity Service Response:" + responseBody);
		}

		IdentityResponse identityResponse = new IdentityResponse(httpResponse, authToken, userAttributes, securityAttributes, isLoginSuccessful);
		return identityResponse;
	}

	private boolean hasAuthTokenExpired() {
		LOG.debug("Computing Auth Token Expiry Status for Auth Service:" + this.authServiceURL);
		boolean hasAuthTokenExpired = ((new Date().getTime() - authTokenFetchDateTime.getTime()) / 1000) >= (authTokenValidityInSeconds - BUFFER_DURATION);
		LOG.debug("Auth Token Expiry Status for Auth Service:" + this.authServiceURL + " : " + hasAuthTokenExpired);
		return hasAuthTokenExpired;
	}

}
