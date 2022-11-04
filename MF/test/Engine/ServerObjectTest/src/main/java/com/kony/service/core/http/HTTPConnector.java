package com.kony.service.core.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.kony.service.dto.HTTPResponse;
import com.kony.service.handler.HTTPResponseHandler;

/**
 * Utility class to configure and execute HTTP calls
 * 
 * @author Aditya Mankal
 *
 */
public class HTTPConnector {

	private static final int CONNECT_TIMEOUT_IN_SECONDS = 30;
	private static final int SOCKET_CONNECT_TIMEOUT_IN_SECONDS = 30;
	private static final int CONNECTION_REQUEST_TIMEOUT_IN_SECONDS = 30;

	private CloseableHttpClient closeableHttpClient;
	private HttpRequestBase httpRequestBase;
	private StringEntity requestParams;

	public HTTPConnector(String requestType, String targetURL, String requestBody, Map<String, String> requestHeaders) throws UnsupportedEncodingException, MalformedURLException {
		targetURL = encodeURL(targetURL);

		if (requestType.equalsIgnoreCase(HttpGet.METHOD_NAME)) {
			requestType = HttpGet.METHOD_NAME;// Corrections in case
			httpRequestBase = new HttpGet(targetURL);
		}

		else if (requestType.equalsIgnoreCase(HttpPost.METHOD_NAME)) {
			requestType = HttpPost.METHOD_NAME;
			httpRequestBase = new HttpPost(targetURL);
			if (StringUtils.isNotBlank(requestBody)) {
				requestParams = new StringEntity(requestBody);
				((HttpPost) httpRequestBase).setEntity(requestParams);
			}
		}

		else if (requestType.equalsIgnoreCase(HttpPut.METHOD_NAME)) {
			requestType = HttpPut.METHOD_NAME;
			httpRequestBase = new HttpPut(targetURL);
			if (StringUtils.isNotBlank(requestBody)) {
				requestParams = new StringEntity(requestBody);
				((HttpPut) httpRequestBase).setEntity(requestParams);
			}
		}

		else if (requestType.equalsIgnoreCase(HttpPatch.METHOD_NAME)) {
			requestType = HttpPatch.METHOD_NAME;
			httpRequestBase = new HttpPatch(targetURL);
			if (StringUtils.isNotBlank(requestBody)) {
				requestParams = new StringEntity(requestBody);
				((HttpPatch) httpRequestBase).setEntity(requestParams);
			}
		}

		else if (requestType.equalsIgnoreCase(HttpDelete.METHOD_NAME)) {
			requestType = HttpDelete.METHOD_NAME;
			httpRequestBase = new HttpDelete(targetURL);

		}

		if (requestHeaders != null) {
			Set<String> keys = requestHeaders.keySet();
			for (String key : keys) {
				httpRequestBase.addHeader(key, requestHeaders.get(key));
			}
		}

		RequestConfig.Builder requestConfig = RequestConfig.custom();
		requestConfig.setConnectTimeout(CONNECT_TIMEOUT_IN_SECONDS * 1000);
		requestConfig.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_IN_SECONDS * 1000);
		requestConfig.setSocketTimeout(SOCKET_CONNECT_TIMEOUT_IN_SECONDS * 1000);

		httpRequestBase.setConfig(requestConfig.build());
		closeableHttpClient = HttpClientBuilder.create().build();
	}

	public HTTPResponse executeClientAndSendRequest() throws ClientProtocolException, IOException {
		if (httpRequestBase != null) {
			Date startDate = new Date();
			HTTPResponse httpResponse = closeableHttpClient.execute(httpRequestBase, new HTTPResponseHandler());
			Date endDate = new Date();
			double responseTime = endDate.getTime() - startDate.getTime();
			httpResponse = new HTTPResponse(httpResponse.getResponseHeaders(), httpResponse.getResponseCode(), httpResponse.getResponseBody(), responseTime);
			return httpResponse;
		}
		return null;
	}

	private static String getEncodedURL(String targetURL) throws UnsupportedEncodingException {
		return URLEncoder.encode(targetURL, StandardCharsets.UTF_8.name());
	}

	public static String encodeURL(String targetURL) throws MalformedURLException, UnsupportedEncodingException {
		URL targetURLInstance;
		String baseURL;
		targetURLInstance = new URL(targetURL);
		if (targetURLInstance.getQuery() == null)
			return targetURL;
		baseURL = targetURLInstance.getProtocol() + "://" + targetURLInstance.getHost() +
				((targetURLInstance.getPort() != -1) ? ":" + targetURLInstance.getPort() : "")
				+ targetURLInstance.getPath() + "?";
		String[] params = targetURLInstance.getQuery().split("&");
		String encodedQueryString = "";
		if (params != null && params.length > 0) {
			for (String param : params) {
				String[] pair = param.split("=");
				if (pair != null && pair.length == 2) {
					encodedQueryString += pair[0] + "=" + getEncodedURL(pair[1]);
				}
			}
		}
		baseURL += encodedQueryString;
		return baseURL;
	}

}