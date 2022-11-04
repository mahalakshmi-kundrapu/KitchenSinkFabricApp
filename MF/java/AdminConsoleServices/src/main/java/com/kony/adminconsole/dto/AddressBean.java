package com.kony.adminconsole.dto;

import java.io.Serializable;

public class AddressBean implements Serializable {

    private static final long serialVersionUID = -687989239521818563L;

    private String City_id;
    private String CityName;
    private String AddressLine1;
    private String AddressLine2;
    private String AddressLine3;
    private String ZipCode;
    private String Region_id;

    public String getCity_id() {
        return City_id;
    }

    public void setCity_id(String city_id) {
        City_id = city_id;
    }

    public String getCityName() {
        return CityName;
    }

    public void setCityName(String cityName) {
        CityName = cityName;
    }

    public String getAddressLine1() {
        return AddressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        AddressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return AddressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        AddressLine2 = addressLine2;
    }

    public String getAddressLine3() {
        return AddressLine3;
    }

    public void setAddressLine3(String addressLine3) {
        AddressLine3 = addressLine3;
    }

    public String getZipCode() {
        return ZipCode;
    }

    public void setZipCode(String zipCode) {
        ZipCode = zipCode;
    }

    public String getRegion_id() {
        return Region_id;
    }

    public void setRegion_id(String region_id) {
        Region_id = region_id;
    }

}
