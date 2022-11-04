package com.kony.adminconsole.dto;

import com.kony.adminconsole.utilities.ServiceURLEnum;

public class DeleteFromBackendBean {

    private String deleteId;
    private ServiceURLEnum deleteURL;

    public DeleteFromBackendBean(String deleteId, ServiceURLEnum deleteURL) {
        this.deleteId = deleteId;
        this.deleteURL = deleteURL;
    }

    public String getDeleteId() {
        return deleteId;
    }

    public void setDeleteId(String deleteId) {
        this.deleteId = deleteId;
    }

    public ServiceURLEnum getDeleteURL() {
        return deleteURL;
    }

    public void setDeleteURL(ServiceURLEnum deleteURL) {
        this.deleteURL = deleteURL;
    }
}
