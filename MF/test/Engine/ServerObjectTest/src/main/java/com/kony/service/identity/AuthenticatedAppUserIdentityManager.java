package com.kony.service.identity;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
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
 * Class to perform Authenticated App User Login
 * 
 * @author Aditya Mankal
 *
 */
public class AuthenticatedAppUserIdentityManager implements IdentityManager {

	private static final Logger LOG = LogManager.getLogger(AuthenticatedAppUserIdentityManager.class);
	private static final double BUFFER_DURATION = 8; // Approximate Time taken by an Identity Service

	private IdentityResponse identityResponse = null;
	private Date authTokenFetchDateTime = null;// Last Retrieval Date of Authentication Token

	private String appKey;
	private String appSecret;
	private String requestBody;
	private String providerName;
	private String authServiceURL;
	private String identityServiceURL;
	private int authTokenValidityInSeconds;
	private Map<String, String> requestHeaders;

	/**
	 * 
	 */
	public AuthenticatedAppUserIdentityManager() {
		super();
	}

	/**
	 * @param identityService
	 * @throws URISyntaxException
	 */
	public AuthenticatedAppUserIdentityManager(String authServiceURL, String providerName, String appKey, String appSecret, String requestBody,
			Map<String, String> requestHeaders, int authTokenValidityInSeconds) throws URISyntaxException {
		this.appKey = appKey;
		this.appSecret = appSecret;
		this.requestBody = requestBody;
		this.providerName = providerName;
		this.authServiceURL = authServiceURL;
		this.requestHeaders = requestHeaders;
		this.authTokenValidityInSeconds = authTokenValidityInSeconds;
		this.identityServiceURL = new URIBuilder(authServiceURL).addParameter(IdentityConstants.PROVIDER_NAME_KEY, providerName).build().toString();
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

			LOG.debug("Executing Authenticated App User Login:" + identityServiceURL);
			authTokenFetchDateTime = new Date();

			requestHeaders = requestHeaders == null ? new HashMap<>() : requestHeaders;
			requestBody = requestBody == null ? StringUtils.EMPTY : requestBody;

			if (!requestHeaders.containsKey(HttpHeaders.CONTENT_TYPE)) {
				requestHeaders.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
			}

			// Use Default App-Key and App-Secret if Custom values have not been passed
			if (!requestHeaders.containsKey(IdentityConstants.X_KONY_APP_KEY)) {
				requestHeaders.put(IdentityConstants.X_KONY_APP_KEY, appKey);
			}
			if (!requestHeaders.containsKey(IdentityConstants.X_KONY_APP_SECRET)) {
				requestHeaders.put(IdentityConstants.X_KONY_APP_SECRET, appSecret);
			}

			LOG.debug("Auth Service URL:" + authServiceURL);
			LOG.debug("Provider Name:" + providerName);
			LOG.debug("Auth Service Request Body:" + requestBody);
			LOG.debug("Auth Service Request Headers:" + JSONUtilities.getMapAsJSONObject(requestHeaders).toString());

			// Execute Identity Request
			try {
				HTTPConnector httpConnInstance = new HTTPConnector(HttpPost.METHOD_NAME, identityServiceURL, requestBody, requestHeaders);
				HTTPResponse httpResponse = httpConnInstance.executeClientAndSendRequest();
				identityResponse = parseIdentityServiceResponse(httpResponse);
			} catch (SocketConnectException sce) {
				LOG.error("Socket Timeout in executing Identity Request. Exception:", sce);
				throw new AuthenticationFailureException("Authentication Failure. Socket Timeout in executing Identity Request:");
			} catch (Exception e) {
				LOG.error("Exception in Executing Identity Request", e);
				throw new AuthenticationFailureException("Authentication Failure. Exception:" + e.getMessage());
			}

			if (identityResponse == null || StringUtils.isBlank(identityResponse.getAuthToken())) {
				String identityServiceCallResponse = StringUtils.EMPTY;
				if (identityResponse != null && identityResponse.getHttpResponse() != null) {
					identityServiceCallResponse = identityResponse.getHttpResponse().getResponseBody();
				}
				throw new AuthenticationFailureException("Authentication Failure. Response:" + identityServiceCallResponse);
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
					LOG.debug("Extracted Auth Token for Identity Provider:" + providerName);
					LOG.debug("Auth Token:" + authToken);
					isLoginSuccessful = true;
				}

				// Parse User Attributes
				if (!responseBodyJSON.isNull(IdentityConstants.PROFILE_KEY) &&
						!responseBodyJSON.getJSONObject(IdentityConstants.PROFILE_KEY).isNull(IdentityConstants.USER_ATTRIBUTES_KEY)) {
					userAttributes = responseBodyJSON.getJSONObject(IdentityConstants.PROFILE_KEY).getJSONObject(IdentityConstants.USER_ATTRIBUTES_KEY).toString();
					LOG.debug("Extracted User Attributes for Identity Provider:" + providerName);
					LOG.debug("User Attributes:" + userAttributes);
				}

				// Parse Security Attributes
				if (!responseBodyJSON.isNull(IdentityConstants.PROVIDER_TOKEN_KEY)
						&& responseBodyJSON.getJSONObject(IdentityConstants.PROVIDER_TOKEN_KEY).has(IdentityConstants.PARAMS_KEY)
						&& responseBodyJSON.getJSONObject(IdentityConstants.PROVIDER_TOKEN_KEY).getJSONObject(IdentityConstants.PARAMS_KEY)
								.has(IdentityConstants.SECURITY_ATTRIBUTES_KEY)) {
					securityAttributes = responseBodyJSON.getJSONObject(IdentityConstants.PROVIDER_TOKEN_KEY).getJSONObject(IdentityConstants.PARAMS_KEY)
							.getJSONObject(IdentityConstants.SECURITY_ATTRIBUTES_KEY).toString();
					LOG.debug("Extracted Security Attributes for Identity Provider:" + providerName);
					LOG.debug("Security Attributes:" + securityAttributes);
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
		LOG.debug("Computing Auth Token Expiry Status for Identity Provider:" + providerName);
		boolean hasAuthTokenExpired = ((new Date().getTime() - authTokenFetchDateTime.getTime()) / 1000) >= (authTokenValidityInSeconds - BUFFER_DURATION);
		LOG.debug("Auth Token Expiry Status for Identity Provider:" + providerName + " : " + hasAuthTokenExpired);
		return hasAuthTokenExpired;
	}

}
