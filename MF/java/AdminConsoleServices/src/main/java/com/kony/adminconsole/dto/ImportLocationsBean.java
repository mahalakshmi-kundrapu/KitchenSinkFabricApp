package com.kony.adminconsole.dto;

import java.util.ArrayList;

import org.apache.commons.csv.CSVPrinter;

public class ImportLocationsBean {

    private boolean correctData;
    private int successCount;
    private int failureCount;
    private ArrayList<CreateInBackendBean> createList;
    private String authToken;
    private String user_ID;
    private CSVPrinter responseCsvPrinter;

    private String addressTableId;
    private String workscheduleTableId;
    private String dayscheduleTableId;
    private String locationTableId;
    private String locationfacilityTableId;
    private String locationcustomersegmentTableId;
    private String locationcurrencyTableId;

    public ImportLocationsBean() {
        correctData = true;
        successCount = 0;
        failureCount = 0;
        createList = new ArrayList<CreateInBackendBean>();
    }

    public boolean isCorrectData() {
        return correctData;
    }

    public void setCorrectData(boolean correctData) {
        this.correctData = correctData;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public ArrayList<CreateInBackendBean> getCreateList() {
        return createList;
    }

    public void setCreateList(ArrayList<CreateInBackendBean> createList) {
        this.createList = createList;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getUser_ID() {
        return user_ID;
    }

    public void setUser_ID(String user_ID) {
        this.user_ID = user_ID;
    }

    public CSVPrinter getResponseCsvPrinter() {
        return responseCsvPrinter;
    }

    public void setResponseCsvPrinter(CSVPrinter responseCsvPrinter) {
        this.responseCsvPrinter = responseCsvPrinter;
    }

    public String getAddressTableId() {
        return addressTableId;
    }

    public void setAddressTableId(String addressTableId) {
        this.addressTableId = addressTableId;
    }

    public String getWorkscheduleTableId() {
        return workscheduleTableId;
    }

    public void setWorkscheduleTableId(String workscheduleTableId) {
        this.workscheduleTableId = workscheduleTableId;
    }

    public String getDayscheduleTableId() {
        return dayscheduleTableId;
    }

    public void setDayscheduleTableId(String dayscheduleTableId) {
        this.dayscheduleTableId = dayscheduleTableId;
    }

    public String getLocationTableId() {
        return locationTableId;
    }

    public void setLocationTableId(String locationTableId) {
        this.locationTableId = locationTableId;
    }

    public String getLocationfacilityTableId() {
        return locationfacilityTableId;
    }

    public void setLocationfacilityTableId(String locationfacilityTableId) {
        this.locationfacilityTableId = locationfacilityTableId;
    }

    public String getLocationcustomersegmentTableId() {
        return locationcustomersegmentTableId;
    }

    public void setLocationcustomersegmentTableId(String locationcustomersegmentTableId) {
        this.locationcustomersegmentTableId = locationcustomersegmentTableId;
    }

    public String getLocationcurrencyTableId() {
        return locationcurrencyTableId;
    }

    public void setLocationcurrencyTableId(String locationcurrencyTableId) {
        this.locationcurrencyTableId = locationcurrencyTableId;
    }

}
