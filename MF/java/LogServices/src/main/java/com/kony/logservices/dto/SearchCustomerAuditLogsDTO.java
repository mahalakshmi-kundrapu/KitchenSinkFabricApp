package com.kony.logservices.dto;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class SearchCustomerAuditLogsDTO {
    private static final Logger LOG = Logger.getLogger(SearchCustomerAuditLogsDTO.class);

    private String customerid;
    private String username;
    private boolean isCSRAssist = false;
    private boolean isCSRAssistFlagSet = false;
    private String searchText;
    private String module;
    private String activityType;
    private String startDate;
    private String endDate;
    private String startAmount;
    private String endAmount;
    private String sortVariable;
    private String sortDirection;

    private int pageSize = -1;
    private int pageOffset = -1;

    public String getStartAmount() {
        return startAmount;
    }

    public void setStartAmount(String startAmount) {
        this.startAmount = startAmount;
    }

    public String getEndAmount() {
        return endAmount;
    }

    public void setEndAmount(String endAmount) {
        this.endAmount = endAmount;
    }

    public boolean isCSRAssistFlagSet() {
        return this.isCSRAssistFlagSet;
    }

    public boolean getIsCSRAssist() {
        return isCSRAssist;
    }

    public void setIsCSRAssist(String isCSRAssist) {
        try {
            if (StringUtils.isNotBlank(isCSRAssist)) {
                this.isCSRAssist = Boolean.parseBoolean(isCSRAssist);
                this.isCSRAssistFlagSet = true;
            }
        } catch (Exception e) {
            LOG.error("Error while parsing the boolean flag isCSRAssist");
            this.isCSRAssistFlagSet = false;
        }
    }

    public String getSortVariable() {
        if (StringUtils.isBlank(sortVariable)) {
            return "createdts";
        }
        return sortVariable;
    }

    public void setSortVariable(String sortVariable) {
        this.sortVariable = sortVariable;
    }

    public String getSortDirection() {
        if (StringUtils.isBlank(sortDirection)) {
            return "DESC";
        }
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public String getCustomerid() {
        return customerid;
    }

    public void setCustomerid(String customerid) {
        this.customerid = customerid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public int getPageSize() {
        if (this.pageSize == -1) {
            return 30;
        }
        return pageSize;
    }

    public void setPageSize(String pageSize) {
        if (StringUtils.isNotBlank(pageSize)) {
            this.pageSize = Integer.parseInt(pageSize);
        }
    }

    public int getPageOffset() {
        if (this.pageOffset == -1) {
            return 0;
        }
        return pageOffset;
    }

    public void setPageOffset(String pageOffset) {
        if (StringUtils.isNotBlank(pageOffset)) {
            this.pageOffset = Integer.parseInt(pageOffset);
        }
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

}
