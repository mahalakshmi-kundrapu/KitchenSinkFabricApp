package com.kony.logservices.dto;

import java.util.Date;

import com.kony.logservices.core.BaseActivity;

public class AdminActivityDTO extends BaseActivity {

    private static final long serialVersionUID = 1002833793167367247L;

    private String event;
    private String description;
    private String username;
    private String userRole;
    private String moduleName;
    private Date eventts;
    private String status;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
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
