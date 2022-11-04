package com.kony.adminconsole.dto;

public class BundleBean {

    private String userId;
    private String bundleId;
    private String bundleName;
    private String bundleAppId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    public String getBundleName() {
        return bundleName;
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    public String getBundleAppId() {
        return bundleAppId;
    }

    public void setBundleAppId(String bundleAppId) {
        this.bundleAppId = bundleAppId;
    }
}
