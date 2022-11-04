package com.kony.logservices.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.kony.logservices.core.BaseActivity;

/**
 * Pagination related DTO used to hold and serialize, deserialize any list of {@link BaseActivity} including pagination
 * details
 * 
 * @author Venkateswara Rao Alla
 *
 * @param <T>
 *            must be subclass of {@link BaseActivity}
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_NULL)
public class PaginationDTO<T extends BaseActivity> implements Serializable {

    private static final long serialVersionUID = 5169351989051622530L;

    private int page;
    private int pageSize;
    private int pageOffset;
    private String sortVariable;
    private String sortDirection;
    private boolean hasNextPage;
    private long count;
    private List<T> logs;
    private int opstatus = 0;
    private int httpStatusCode = 200;

    public String getSortVariable() {
        return sortVariable;
    }

    public void setSortVariable(String sortVariable) {
        this.sortVariable = sortVariable;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public int getPageOffset() {
        return pageOffset;
    }

    public void setPageOffset(int pageOffset) {
        this.pageOffset = pageOffset;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public List<T> getLogs() {
        return logs;
    }

    public void setLogs(List<T> logs) {
        this.logs = logs;
    }

    public int getOpstatus() {
        return opstatus;
    }

    public void setOpstatus(int opstatus) {
        this.opstatus = opstatus;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

}
