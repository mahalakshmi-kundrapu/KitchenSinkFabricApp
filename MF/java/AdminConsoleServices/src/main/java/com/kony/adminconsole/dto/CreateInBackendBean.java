package com.kony.adminconsole.dto;

import java.util.Map;

import com.kony.adminconsole.utilities.ServiceURLEnum;

public class CreateInBackendBean {

    private Map<String, String> createMap;
    ServiceURLEnum createURL;
    ServiceURLEnum deleteURL;
    String responseKey;
    String successDescription;
    String failureDescription;

    public CreateInBackendBean(Map<String, String> createMap, ServiceURLEnum createURL, ServiceURLEnum deleteURL,
            String responseKey, String successDescription, String failureDescription) {
        this.createMap = createMap;
        this.createURL = createURL;
        this.deleteURL = deleteURL;
        this.responseKey = responseKey;
        this.successDescription = successDescription;
        this.failureDescription = failureDescription;
    }

    public Map<String, String> getCreateMap() {
        return createMap;
    }

    public void setCreateMap(Map<String, String> createMap) {
        this.createMap = createMap;
    }

    public ServiceURLEnum getCreateURL() {
        return createURL;
    }

    public void setCreateURL(ServiceURLEnum createURL) {
        this.createURL = createURL;
    }

    public ServiceURLEnum getDeleteURL() {
        return deleteURL;
    }

    public void setDeleteURL(ServiceURLEnum deleteURL) {
        this.deleteURL = deleteURL;
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
