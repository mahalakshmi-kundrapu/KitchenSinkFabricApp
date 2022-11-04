package com.kony.logservices.dto;

import java.util.List;

public class GetAllActivitiesInACustomerSessionDTO {

    private List<CustomerActivityDTO> activities;

    public List<CustomerActivityDTO> getActivities() {
        return activities;
    }

    public void setActivities(List<CustomerActivityDTO> activities) {
        this.activities = activities;
    }

}
