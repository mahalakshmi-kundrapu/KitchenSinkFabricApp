package com.kony.adminconsole.commons.utils.http;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kony.adminconsole.commons.dto.JSONResponseHandlerBean;

public class JSONResponseHandler implements ResponseHandler<JSONResponseHandlerBean> {

	private static final Logger LOG = Logger.getLogger(JSONResponseHandler.class);
	
	@Override
	public JSONResponseHandlerBean handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
		HttpEntity entity = response.getEntity();
		List<com.kony.adminconsole.commons.dto.Header> headersDTO = new ArrayList<>();
		for(org.apache.http.Header responseHeader : response.getAllHeaders()) {
			com.kony.adminconsole.commons.dto.Header headerDTO = new com.kony.adminconsole.commons.dto.Header();
			headerDTO.setName(responseHeader.getName());
			headerDTO.setValue(responseHeader.getValue());
			List<com.kony.adminconsole.commons.dto.HeaderElement> headerElementsDTO = new ArrayList<>();
			for(org.apache.http.HeaderElement responseHeaderElement : responseHeader.getElements()) {
				com.kony.adminconsole.commons.dto.HeaderElement headerElementDTO = new com.kony.adminconsole.commons.dto.HeaderElement();
				headerElementDTO.setName(responseHeaderElement.getName());
				headerElementDTO.setValue(responseHeaderElement.getValue());
				org.apache.http.NameValuePair[] responseHeaderNameValuePairs = responseHeaderElement.getParameters();
				List<com.kony.adminconsole.commons.dto.NameValuePair> headerNameValuePairsDTO = new ArrayList<>();
				for(org.apache.http.NameValuePair responseHeaderNameValuePair : responseHeaderNameValuePairs) {
					com.kony.adminconsole.commons.dto.NameValuePair headerNameValuePairDTO = new com.kony.adminconsole.commons.dto.NameValuePair();
					headerNameValuePairDTO.setName(responseHeaderNameValuePair.getName());
					headerNameValuePairDTO.setValue(responseHeaderNameValuePair.getValue());
					headerNameValuePairsDTO.add(headerNameValuePairDTO);
				}
				headerElementDTO.setParameters(headerNameValuePairsDTO);
				headerElementsDTO.add(headerElementDTO);
			}
			headerDTO.setElements(headerElementsDTO);
			headersDTO.add(headerDTO);
		}
		
		JSONResponseHandlerBean jsonResponseHandlerBean = new JSONResponseHandlerBean();
		jsonResponseHandlerBean.setHeaders(headersDTO);
		String responseStr = EntityUtils.toString(entity, StandardCharsets.UTF_8);
		responseStr = StringUtils.isNotBlank(responseStr) ? responseStr.trim() : "";
		if (StringUtils.startsWith(responseStr, "{") && StringUtils.endsWith(responseStr, "}")) {
			jsonResponseHandlerBean.setResponseAsJSONObject(new JSONObject(responseStr));
		} else if (StringUtils.startsWith(responseStr, "[") && StringUtils.endsWith(responseStr, "]")) {
			jsonResponseHandlerBean.setResponseAsJSONArray(new JSONArray(responseStr));
			jsonResponseHandlerBean.setResponseAJSONArray(true);
		} else if (StringUtils.isNotBlank(responseStr)) {
			// throw invalid json response format exception
			LOG.error("Invalid JSON format in response");
			throw new JSONException("Invalid JSON format in response");
		}
		jsonResponseHandlerBean.setResponseType(entity.getContentType().getValue());
        return jsonResponseHandlerBean;
	}

}
