package com.kony.service.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import com.kony.service.dto.HTTPResponse;

/**
 * Response handler consumes HTTP response and returns as {@link HTTPResponse}
 *
 * @author Aditya Mankal
 *
 */
public class HTTPResponseHandler implements ResponseHandler<HTTPResponse> {

	@Override
	public HTTPResponse handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
		HttpEntity entity = response.getEntity();

		int responseCode = response.getStatusLine().getStatusCode();
		Header[] responseHeaders = response.getAllHeaders();
		Map<String, String> responseHeadersMap = getHeadersAsMap(responseHeaders);
		String responseBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);
		return new HTTPResponse(responseHeadersMap, responseCode, responseBody);
	}

	private Map<String, String> getHeadersAsMap(Header[] httpHeaders) {
		Map<String, String> httpHeadersMap = new HashMap<String, String>();
		if (httpHeaders != null && httpHeaders.length > 0) {
			Header currHeader = null;
			for (int indexVar = 0; indexVar < httpHeaders.length; indexVar++) {
				currHeader = httpHeaders[indexVar];
				httpHeadersMap.put(currHeader.getName(), currHeader.getValue());
			}
		}
		return httpHeadersMap;
	}

}
