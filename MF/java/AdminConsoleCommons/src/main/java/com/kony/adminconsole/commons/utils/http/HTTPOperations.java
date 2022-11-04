package com.kony.adminconsole.commons.utils.http;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.dto.FileStreamHandlerBean;
import com.kony.adminconsole.commons.dto.JSONResponseHandlerBean;
import com.kony.adminconsole.commons.handler.EnvironmentConfigurationsHandler;
import com.kony.adminconsole.commons.handler.IServiceExecutable;
import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.konylabs.middleware.connectors.ConnectorUtils;
import com.konylabs.middleware.controller.DataControllerRequest;

/**
 * Utility methods used to perform http operations. Uses middleware httpclient
 * for executing the http requests.
 * 
 * @author Aditya Mankal, Venkateswara Rao Alla
 *
 */
public class HTTPOperations implements IServiceExecutable {

	private static final Logger LOG = Logger.getLogger(HTTPOperations.class);
	public static final String OKTA_AUTHORIZATION_HEADER = "";
	public static final String X_KONY_AUTHORIZATION_HEADER = "X-Kony-Authorization";

	public HTTPOperations() {
		// TODO: Change to Protected Constructor. Move to utils Package
	}

	// HTTP GET
	public static String hitGETServiceAndGetResponse(String serviceURL, Map<String, String> requestHeaders,
			String konyFabricAuthToken) {

		HttpGet httpGet = new HttpGet(serviceURL);
		httpGet.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
		return executeRequest(httpGet, konyFabricAuthToken, requestHeaders);
	}

	// HTTP PUT - APPLICATION_FORM_URLENCODED
	public static String hitPUTServiceAndGetResponse(String serviceURL, Map<String, String> bodyParametersMap,
			Map<String, String> requestHeaders, String konyFabricAuthToken) {

		HttpPut httpPut = new HttpPut(serviceURL);
		httpPut.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType());

		if (bodyParametersMap != null && !bodyParametersMap.isEmpty()) {
			List<NameValuePair> bodyParametersNameValuePairList = new ArrayList<NameValuePair>();
			for (Map.Entry<String, String> entry : bodyParametersMap.entrySet()) {
				bodyParametersNameValuePairList
						.add(new BasicNameValuePair(entry.getKey(), Objects.toString(entry.getValue(), null)));
			}
			httpPut.setEntity(new UrlEncodedFormEntity(bodyParametersNameValuePairList, StandardCharsets.UTF_8));
		}

		return executeRequest(httpPut, konyFabricAuthToken, requestHeaders);
	}

	// HTTP PUT - APPLICATION_JSON
	public static String hitPUTServiceAndGetResponse(String serviceURL, JSONObject requestBody,
			String konyFabricAuthToken, Map<String, String> requestHeaders) {

		HttpPut httpPut = new HttpPut(serviceURL);
		httpPut.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());

		if (requestBody != null) {
			String jsonString = requestBody.toString();
			StringEntity requestEntity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
			httpPut.setEntity(requestEntity);
		}

		return executeRequest(httpPut, konyFabricAuthToken, requestHeaders);

		/*
		 * public static String hitPOSTServiceAndGetResponse(String serviceURL,
		 * HashMap<String, String> postParametersMap, HashMap<String, String>
		 * requestHeaders, String konyFabricAuthToken) {
		 */
	}

	// HTTP POST - APPLICATION_FORM_URLENCODED
	public static String hitPOSTServiceAndGetResponse(String serviceURL, Map<String, String> postParametersMap,
			Map<String, String> requestHeaders, String konyFabricAuthToken) {

		HttpPost httpPost = new HttpPost(serviceURL);
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType());

		if (postParametersMap != null && !postParametersMap.isEmpty()) {
			List<NameValuePair> postParametersNameValuePairList = new ArrayList<NameValuePair>();
			for (Map.Entry<String, String> entry : postParametersMap.entrySet()) {
				postParametersNameValuePairList
						.add(new BasicNameValuePair(entry.getKey(), Objects.toString(entry.getValue(), null)));
			}
			httpPost.setEntity(new UrlEncodedFormEntity(postParametersNameValuePairList, StandardCharsets.UTF_8));
		}

		return executeRequest(httpPost, konyFabricAuthToken, requestHeaders);
	}

	// HTTP POST - APPLICATION_JSON
	public static String hitPOSTServiceAndGetResponse(String serviceURL, JSONObject jsonPostParameters,
			String konyFabricAuthToken, Map<String, String> requestHeaders) {

		HttpPost httpPost = new HttpPost(serviceURL);
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());

		if (jsonPostParameters != null) {
			String jsonString = jsonPostParameters.toString();
			StringEntity requestEntity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
			httpPost.setEntity(requestEntity);
		}

		return executeRequest(httpPost, konyFabricAuthToken, requestHeaders);
	}

	public static String hitPOSTServiceAndGetResponse(String serviceURL, InputStream inputStreamInstance,
			String konyFabricAuthToken, Map<String, String> requestHeaders) {

		HttpPost httpPostRequestInstance = new HttpPost(serviceURL);

		InputStreamEntity entity = new InputStreamEntity(inputStreamInstance);
		httpPostRequestInstance.setEntity(entity);
		httpPostRequestInstance.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.MULTIPART_FORM_DATA.getMimeType());

		// Execute and get the response.
		return executeRequest(httpPostRequestInstance, konyFabricAuthToken, requestHeaders);

	}
	
	public static FileStreamHandlerBean hitPOSTServiceAndGetFileStream(String serviceURL, JSONObject jsonPostParameters,
			String konyFabricAuthToken, Map<String, String> requestHeaders) {

		HttpPost httpPost = new HttpPost(serviceURL);
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());

		if (jsonPostParameters != null) {
			String jsonString = jsonPostParameters.toString();
			StringEntity requestEntity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
			httpPost.setEntity(requestEntity);
		}

		// Execute and get the response.
		return executeRequestForInputStreamResponse(httpPost, konyFabricAuthToken, requestHeaders);

	}

	public static JSONResponseHandlerBean hitPOSTServiceAndGetJSONResponse(String URL, Map<String, String> postParams,
			String konyFabricAuthToken, Map<String, String> requestHeaders) {
		HttpPost httpPost = new HttpPost(URL);
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType());

		if (postParams != null && !postParams.isEmpty()) {
			List<NameValuePair> postParametersNameValuePairList = new ArrayList<NameValuePair>();
			for (Map.Entry<String, String> entry : postParams.entrySet()) {
				postParametersNameValuePairList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			httpPost.setEntity(new UrlEncodedFormEntity(postParametersNameValuePairList, StandardCharsets.UTF_8));
		}

		return executeRequestForJSONResponse(httpPost, konyFabricAuthToken, requestHeaders);
	}

	public static String executeRequest(HttpUriRequest request, String konyFabricAuthToken,
			Map<String, String> requestHeaders) {
		try {
			CloseableHttpClient httpClient = getHttpClient();

			if (StringUtils.isNotBlank(konyFabricAuthToken)) {
				request.setHeader(X_KONY_AUTHORIZATION_HEADER, konyFabricAuthToken);
			}

			if (requestHeaders != null && !requestHeaders.isEmpty()) {
				for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
					request.setHeader(entry.getKey(), String.class.cast(entry.getValue()));
				}
			}
			String response = httpClient.execute(request, new StringResponseHandler());
			return response;
		} catch (Exception e) {
			LOG.error("Error occured while executing backend request", e);
		}
		return null;
	}
	
	public static FileStreamHandlerBean executeRequestForInputStreamResponse(HttpUriRequest request, String konyFabricAuthToken,
			Map<String, String> requestHeaders) {
		try {
			CloseableHttpClient httpClient = getHttpClient();

			if (StringUtils.isNotBlank(konyFabricAuthToken)) {
				request.setHeader(X_KONY_AUTHORIZATION_HEADER, konyFabricAuthToken);
			}

			if (requestHeaders != null && !requestHeaders.isEmpty()) {
				for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
					request.setHeader(entry.getKey(), String.class.cast(entry.getValue()));
				}
			}
			return httpClient.execute(request, new FileStreamResponseHandler());
			
		} catch (Exception e) {
			LOG.error("Error occured while executing backend request", e);
		}
		return null;
	}

	public static JSONResponseHandlerBean executeRequestForJSONResponse(HttpUriRequest request,
			String konyFabricAuthToken, Map<String, String> requestHeaders) {
		try {
			CloseableHttpClient httpClient = getHttpClient();

			if (StringUtils.isNotBlank(konyFabricAuthToken)) {
				request.setHeader(X_KONY_AUTHORIZATION_HEADER, konyFabricAuthToken);
			}

			if (requestHeaders != null && !requestHeaders.isEmpty()) {
				for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
					request.setHeader(entry.getKey(), entry.getValue());
				}
			}

			return httpClient.execute(request, new JSONResponseHandler());
		} catch (Exception e) {
			LOG.error("Error occured while executing backend request", e);
		}
		return null;
	}

	/**
	 * <p>
	 * Returns {@link CloseableHttpClient} instance from middleware. Lifecycle is
	 * managed by middleware.
	 * <p>
	 * <b>NOTE: Never call close() on this client and never participate this client
	 * in try-with-resources statement
	 * 
	 * @return {@link CloseableHttpClient} instance
	 */
	private static CloseableHttpClient getHttpClient() {
		return ConnectorUtils.getHttpClient(); // lifecycle is managed by middleware. Never call close() on this client
	}

	@Override
	public String invokeService(String serviceId, String operationId, Map<String, Object> postParameters,
			Map<String, Object> requestHeaders, DataControllerRequest request) throws Exception {
		String hostURL = EnvironmentConfigurationsHandler.getValue("AC_HOST_URL", request);
		String serviceURL = hostURL + "/services/" + serviceId + "/" + operationId;
		String serviceResponse = HTTPOperations.hitPOSTServiceAndGetResponse(serviceURL, convertMap(postParameters),
				convertMap(requestHeaders), CommonUtilities.getAuthToken(request));
		return serviceResponse;
	}

	@Override
	public BufferedHttpEntity invokePassthroughService(String serviceId, String operationId,
			Map<String, Object> postParameters, Map<String, Object> requestHeaders, DataControllerRequest request)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	private static Map<String, String> convertMap(Map<String, Object> sourceMap) {
		Map<String, String> resultMap = new HashMap<String, String>();
		if (sourceMap != null && !sourceMap.isEmpty()) {
			for (Entry<String, Object> currRecord : sourceMap.entrySet()) {
				resultMap.put(currRecord.getKey(), String.class.cast(currRecord.getValue()));
			}
		}
		return resultMap;
	}
}