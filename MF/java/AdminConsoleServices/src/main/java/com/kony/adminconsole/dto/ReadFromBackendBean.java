package com.kony.adminconsole.dto;

import java.util.Map;

import com.kony.adminconsole.utilities.ServiceURLEnum;

public class ReadFromBackendBean {

    private Map<String, String> parameterMap;
    ServiceURLEnum URL;
    String responseKey;
    String successDescription;
    String failureDescription;

    public ReadFromBackendBean(Map<String, String> parameterMap, ServiceURLEnum URL, String responseKey,
            String successDescription, String failureDescription) {
        this.parameterMap = parameterMap;
        this.URL = URL;
        this.responseKey = responseKey;
        this.successDescription = successDescription;
        this.failureDescription = failureDescription;
    }

    public Map<String, String> getParameterMap() {
        return parameterMap;
    }

    public void addParameterMap(Map<String, String> parameterMap) {
        this.parameterMap = parameterMap;
    }

    public ServiceURLEnum getURL() {
        return URL;
    }

    public void setURL(ServiceURLEnum URL) {
        this.URL = URL;
    }

    public String getResponseKey() {
        return responseKey;
    }

    public void setResponseKey(String responseKey) {
        this.responseKey = responseKey;
    }

    public String getSuccessDescription() {
        return successDescription;
    }

    public void setSuccessDescription(String successDescription) {
        this.successDescription = successDescription;
    }

    public String getFailureDescription() {
        return failureDescription;
    }

    public void setFailureDescription(String failureDescription) {
        this.failureDescription = failureDescription;
    }

}
