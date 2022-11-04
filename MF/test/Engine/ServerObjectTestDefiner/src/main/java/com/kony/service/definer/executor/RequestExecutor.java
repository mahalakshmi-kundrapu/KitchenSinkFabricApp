package com.kony.service.definer.executor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kony.service.constants.IdentityConstants;
import com.kony.service.constants.SecurityLevel;
import com.kony.service.core.http.HTTPConnector;
import com.kony.service.definer.dto.ServiceRequest;
import com.kony.service.dto.HTTPResponse;
import com.kony.service.dto.IdentityResponse;
import com.kony.service.dto.IdentityService;
import com.kony.service.dto.RequestPayload;
import com.kony.service.dto.RequestResponse;
import com.kony.service.exception.AuthenticationFailureException;
import com.kony.service.identity.AnonymousAppUserIdentityManager;
import com.kony.service.identity.AuthenticatedAppUserIdentityManager;
import com.kony.service.identity.IdentityManager;
import com.kony.service.util.JSONParser;
import com.kony.service.util.JSONUtilities;

/**
 * Class to Execute a Request using input of type {@link RequestPayload}
 *
 * @author Aditya Mankal
 */
public class RequestExecutor {

	private static final Logger LOG = LogManager.getLogger(RequestExecutor.class);

	/**
	 * Method to execute a Request
	 * 
	 * @param serviceRequest
	 * @return Request Response
	 */
	public static RequestResponse executeRequest(ServiceRequest serviceRequest) {

		HTTPResponse httpResponse = null;
		List<String> responseBodyAssertionPaths = new ArrayList<>();
		List<String> responseHeaderAssertionPaths = new ArrayList<>();
		boolean isAuthRequestSuccessful = true;

		try {
			String url = serviceRequest.getUrl();

			String requestBody = serviceRequest.getRequestBody();
			String requestMethod = serviceRequest.getRequestMethod();

			Map<String, String> requestHeaders = serviceRequest.getRequestHeaders();
			requestHeaders = requestHeaders == null ? new HashMap<>() : requestHeaders;

			LOG.debug("Configured Security Level:" + serviceRequest.getSecurityLevel());
			if (!StringUtils.equals(serviceRequest.getSecurityLevel(), SecurityLevel.PUBLIC.getValue())) {
				IdentityResponse identityResponse = executeIdentityRequest(serviceRequest);
				requestHeaders.put(IdentityConstants.X_KONY_AUTHORIZATION, identityResponse.getAuthToken());
			}

			// Prepare HTTP Client
			HTTPConnector httpConnector = null;
			try {
				LOG.debug("Preparing HTTP Client");
				httpConnector = new HTTPConnector(requestMethod, url, requestBody, requestHeaders);
			} catch (UnsupportedEncodingException | MalformedURLException e) {
				// Invalid Service Configuration
				LOG.error("Bad Request: Exception:", e);
				httpResponse = new HTTPResponse(new HashMap<>(), HttpStatus.SC_BAD_REQUEST, StringUtils.EMPTY);
			}

			// Execute HTTP Client
			LOG.debug("Executing HTTP Request");
			httpResponse = httpConnector.executeClientAndSendRequest();

			LOG.debug("HTTP Response recieved");
			// Parse HTTP Response
			if (httpResponse != null) {
				String responseBodyJSONString = httpResponse.getResponseBody();
				String responseHeaderJSONString = JSONUtilities.getMapAsJSONObject(httpResponse.getResponseHeaders()).toString();
				if (httpResponse.getResponseCode() == HttpStatus.SC_OK) {
					responseBodyAssertionPaths = JSONParser.getPaths(responseBodyJSONString);
					responseHeaderAssertionPaths = JSONParser.getPaths(responseHeaderJSONString);
				}
				LOG.debug("Returning service response");
			} else {
				// Failed Service
				httpResponse = new HTTPResponse(new HashMap<>(), HttpStatus.SC_INTERNAL_SERVER_ERROR, StringUtils.EMPTY);
				LOG.error("Failed to Execute Service");
			}
		} catch (AuthenticationFailureException afe) {
			LOG.debug("Failed to fetch Authentication Token");
			isAuthRequestSuccessful = false;
		} catch (SocketTimeoutException sce) {
			LOG.error("IO Exception. Exception:", sce);
			httpResponse = new HTTPResponse(new HashMap<>(), HttpStatus.SC_GATEWAY_TIMEOUT, StringUtils.EMPTY);
		} catch (URISyntaxException e) {
			LOG.error("URI Syntax Exception. Check Request Payload & Configuration JSON:", e);
			httpResponse = new HTTPResponse(new HashMap<>(), HttpStatus.SC_BAD_REQUEST, StringUtils.EMPTY);
		} catch (IOException e) {
			LOG.error("IO Exception. Check Request Payload & Configuration JSON:", e);
			httpResponse = new HTTPResponse(new HashMap<>(), HttpStatus.SC_INTERNAL_SERVER_ERROR, StringUtils.EMPTY);
		}

		// Return service response
		RequestResponse requestResponse = new RequestResponse(httpResponse, responseBodyAssertionPaths, responseHeaderAssertionPaths, isAuthRequestSuccessful);
		return requestResponse;
	}

	/**
	 * Method to execute the Identity Request and fetch the Identity Response
	 * 
	 * @param serviceRequest
	 * @return Identity Response
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private static IdentityResponse executeIdentityRequest(ServiceRequest serviceRequest) throws URISyntaxException, IOException {

		IdentityManager currIdentityManager = null;
		IdentityResponse currIdentityServiceResponse = null;

		if (StringUtils.equals(serviceRequest.getSecurityLevel(), SecurityLevel.AUTHENTICATED_APP_USER.getValue())) {
			// Authenticated App User

			// Fetch the configured Identity Service
			IdentityService currIdentityService = serviceRequest.getIdentityService();
			if (currIdentityService == null) {
				// Throw Bad Request response
				LOG.error("Missing Identity Service Information for request of type 'Authenticated App User'");
				return null;
			}

			if (serviceRequest.isCustomAuthRequest()) {
				// Consider Custom Auth Request Body and Auth Request Headers
				currIdentityService.setRequestBody(serviceRequest.getCustomAuthBody());
				currIdentityService.setRequestHeaders(serviceRequest.getCustomAuthHeaders());
			}
			// Initialise an Identity Manager for the current Identity Provider
			currIdentityManager = new AuthenticatedAppUserIdentityManager(
					serviceRequest.getAuthServiceURL(),
					currIdentityService.getProviderName(),
					serviceRequest.getAppKey(),
					serviceRequest.getAppSecret(),
					currIdentityService.getRequestBody(),
					currIdentityService.getRequestHeaders(),
					currIdentityService.getAuthTokenValidityInSeconds());

			LOG.debug("Identity Service:" + serviceRequest.getIdentityService() + "Is Custom Auth Request:" + serviceRequest.isCustomAuthRequest() + "Auth Request Body:"
					+ serviceRequest.getIdentityService().getRequestBody().toString() + "Auth Request Headers:"
					+ JSONUtilities.stringify(serviceRequest.getIdentityService().getRequestHeaders()));

			// Execute Identity Request
			currIdentityServiceResponse = currIdentityManager.executeIdentityRequest();
		} else if (StringUtils.equals(serviceRequest.getSecurityLevel(), SecurityLevel.ANONYMOUS_APP_USER.getValue())) {
			// Anonyoumous App User
			currIdentityManager = new AnonymousAppUserIdentityManager(serviceRequest.getAuthServiceURL(), serviceRequest.getAppKey(), serviceRequest.getAppSecret(), 1800);

			// Execute Identity Request
			currIdentityServiceResponse = currIdentityManager.executeIdentityRequest();
		}

		// Return Identity Response
		return currIdentityServiceResponse;
	}

}
