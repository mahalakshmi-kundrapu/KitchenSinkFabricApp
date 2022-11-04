package com.kony.adminconsole.dto;

import java.io.Serializable;

public class SystemUserBean implements Serializable {

    private static final long serialVersionUID = 825783054107661686L;

    private String Status_id;
    private String Username;
    private String Email;
    private String CurrUser;
    private String FirstName;
    private String MiddleName;
    private String LastName;
    private String createdby;
    private String modifiedby;

    public String getStatus_id() {
        return Status_id;
    }

    public void setStatus_id(String status_id) {
        Status_id = status_id;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getMiddleName() {
        return MiddleName;
    }

    public void setMiddleName(String middleName) {
        MiddleName = middleName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public String getModifiedby() {
        return modifiedby;
    }

    public void setModifiedby(String modifiedby) {
        this.modifiedby = modifiedby;
    }

    public String getCurrUser() {
        return CurrUser;
    }

    public void setCurrUser(String currUser) {
        CurrUser = currUser;
    }

}
