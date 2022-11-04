package com.kony.adminconsole.commons.dto;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONResponseHandlerBean {

    private List<Header> headers;
    private String responseType;
    private JSONObject responseAsJSONObject;
    private JSONArray responseAsJSONArray;
    private boolean isResponseAJSONArray;

    public List<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public JSONObject getResponseAsJSONObject() {
        return responseAsJSONObject;
    }

    public void setResponseAsJSONObject(JSONObject responseAsJSONObject) {
        this.responseAsJSONObject = responseAsJSONObject;
    }

    public JSONArray getResponseAsJSONArray() {
        return responseAsJSONArray;
    }

    public void setResponseAsJSONArray(JSONArray responseAsJSONArray) {
        this.responseAsJSONArray = responseAsJSONArray;
    }

    public boolean isResponseAJSONArray() {
        return isResponseAJSONArray;
    }

    public void setResponseAJSONArray(boolean isResponseAJSONArray) {
        this.isResponseAJSONArray = isResponseAJSONArray;
    }

}
