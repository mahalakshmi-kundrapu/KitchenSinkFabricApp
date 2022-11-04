package com.kony.logservices.dto;

import java.util.List;

public class GetLastNCustomerSessionsDTO {

    private List<CustomerActivityDTO> sessions;

    public List<CustomerActivityDTO> getSessions() {
        return sessions;
    }

    public void setSessions(List<CustomerActivityDTO> sessions) {
        this.sessions = sessions;
    }

}
