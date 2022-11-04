package com.kony.logservices.dto;

import java.util.Date;

import com.kony.logservices.core.BaseActivity;

public class AdminCustomerActivityDTO extends BaseActivity {

    private static final long serialVersionUID = 1971751380907540L;

    private String customerId;
    private String adminName;
    private String adminRole;
    private String activityType;
    private String description;
    private Date eventts;
    private String status;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getAdminRole() {
        return adminRole;
    }

    public void setAdminRole(String adminRole) {
        this.adminRole = adminRole;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getEventts() {
        return eventts;
    }

    public void setEventts(Date eventts) {
        this.eventts = eventts;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
