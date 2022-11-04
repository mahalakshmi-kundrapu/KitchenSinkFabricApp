package com.kony.adminconsole.dto;

import java.util.HashMap;
import java.util.Map;

public class PolicyBean {

    private boolean createPolicy;
    private boolean updatePolicy;
    private boolean deletePolicy;

    private boolean policyForCustomer;
    private String typeId;

    private String localeCode;
    private String policyDescription;

    private Map<String, String> createOrUpdatePolicyMap;
    private String policyId;

    public PolicyBean() {
        this.createPolicy = false;
        this.updatePolicy = false;
        this.deletePolicy = false;
        this.policyForCustomer = true;
        this.createOrUpdatePolicyMap = new HashMap<String, String>();
        policyId = null;
    }

    public boolean isCreatePolicy() {
        return createPolicy;
    }

    public void setCreatePolicy(boolean createPolicy) {
        this.createPolicy = createPolicy;
    }

    public boolean isUpdatePolicy() {
        return updatePolicy;
    }

    public void setUpdatePolicy(boolean updatePolicy) {
        this.updatePolicy = updatePolicy;
    }

    public boolean isDeletePolicy() {
        return deletePolicy;
    }

    public void setDeletePolicy(boolean deletePolicy) {
        this.deletePolicy = deletePolicy;
    }

    public boolean isPolicyForCustomer() {
        return policyForCustomer;
    }

    public void setPolicyForCustomer(boolean policyForCustomer) {
        this.policyForCustomer = policyForCustomer;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getLocaleCode() {
        return localeCode;
    }

    public void setLocaleCode(String localeCode) {
        this.localeCode = localeCode;
    }

    public String getPolicyDescription() {
        return policyDescription;
    }

    public void setPolicyDescription(String policyDescription) {
        this.policyDescription = policyDescription;
    }

    public Map<String, String> getCreateOrUpdatePolicyMap() {
        return createOrUpdatePolicyMap;
    }

    public void setCreateOrUpdatePolicyMap(Map<String, String> createOrUpdatePolicyMap) {
        this.createOrUpdatePolicyMap = createOrUpdatePolicyMap;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }
}
