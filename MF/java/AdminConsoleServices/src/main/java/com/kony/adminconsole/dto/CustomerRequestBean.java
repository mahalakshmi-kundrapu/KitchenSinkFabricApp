package com.kony.adminconsole.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kony.adminconsole.commons.handler.MultipartPayloadHandler.FormItem;
import com.kony.adminconsole.core.security.UserDetailsBean;

/**
 * Bean for Customer Request and Messages
 *
 * @author Aditya Mankal
 * 
 */
public class CustomerRequestBean {

    private String csrId;
    private String accountId;
    private String messageId;
    private String requestId;
    private String customerId;
    private String messageStatus;
    private String requestStatus;
    private String requestSubject;
    private boolean isSentMessage;
    private boolean isDraftMessage;
    private boolean isCreateRequest;
    private boolean isRequestByAdmin;
    private String requestPriority;
    private String currentUsername;
    private String requestCategoryId;
    private String messageDescription;
    private String customerUsername;
    private String customerSalutation;
    private String customerFirstName;
    private String customerMiddleName;
    private String customerLastName;
    private List<FormItem> formItems;
    private String discardedAttachments;
    private UserDetailsBean userDetailsBeanInstance;
    private Map<String, String> mediaCollection = new HashMap<>();

    /**
     * @return the requestId
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * @param requestId
     *            the requestId to set
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * @return the requestSubject
     */
    public String getRequestSubject() {
        return requestSubject;
    }

    /**
     * @param requestSubject
     *            the requestSubject to set
     */
    public void setRequestSubject(String requestSubject) {
        this.requestSubject = requestSubject;
    }

    /**
     * @return the requestCategoryId
     */
    public String getRequestCategoryId() {
        return requestCategoryId;
    }

    /**
     * @param requestCategoryId
     *            the requestCategoryId to set
     */
    public void setRequestCategoryId(String requestCategoryId) {
        this.requestCategoryId = requestCategoryId;
    }

    /**
     * @return the csrId
     */
    public String getCsrId() {
        return csrId;
    }

    /**
     * @param csrId
     *            the csrId to set
     */
    public void setCsrId(String csrId) {
        this.csrId = csrId;
    }

    /**
     * @return the messageId
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * @param messageId
     *            the messageId to set
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * @return the messageDescription
     */
    public String getMessageDescription() {
        return messageDescription;
    }

    /**
     * @param messageDescription
     *            the messageDescription to set
     */
    public void setMessageDescription(String messageDescription) {
        this.messageDescription = messageDescription;
    }

    /**
     * @return the isDraftMessage
     */
    public boolean isDraftMessage() {
        return isDraftMessage;
    }

    /**
     * @param isDraftMessage
     *            the isDraftMessage to set
     */
    public void setDraftMessage(boolean isDraftMessage) {
        this.isDraftMessage = isDraftMessage;
    }

    /**
     * @return the isSentMessage
     */
    public boolean isSentMessage() {
        return isSentMessage;
    }

    /**
     * @param isSentMessage
     *            the isSentMessage to set
     */
    public void setSentMessage(boolean isSentMessage) {
        this.isSentMessage = isSentMessage;
    }

    /**
     * @return the formItems
     */
    public List<FormItem> getFormItems() {
        return formItems;
    }

    /**
     * @param formItems
     *            the formItems to set
     */
    public void setFormItems(List<FormItem> formItems) {
        this.formItems = formItems;
    }

    /**
     * @return the customerId
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * @param customerId
     *            the customerId to set
     */
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    /**
     * @return the userDetailsBeanInstance
     */
    public UserDetailsBean getUserDetailsBeanInstance() {
        return userDetailsBeanInstance;
    }

    /**
     * @param userDetailsBeanInstance
     *            the userDetailsBeanInstance to set
     */
    public void setUserDetailsBeanInstance(UserDetailsBean userDetailsBeanInstance) {
        this.userDetailsBeanInstance = userDetailsBeanInstance;
    }

    /**
     * @return the requestPriority
     */
    public String getRequestPriority() {
        return requestPriority;
    }

    /**
     * @param requestPriority
     *            the requestPriority to set
     */
    public void setRequestPriority(String requestPriority) {
        this.requestPriority = requestPriority;
    }

    /**
     * @return the accountId
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * @param accountId
     *            the accountId to set
     */
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    /**
     * @return the discardedAttachments
     */
    public String getDiscardedAttachments() {
        return discardedAttachments;
    }

    /**
     * @param discardedAttachments
     *            the discardedAttachments to set
     */
    public void setDiscardedAttachments(String discardedAttachments) {
        this.discardedAttachments = discardedAttachments;
    }

    /**
     * @return the messageStatus
     */
    public String getMessageStatus() {
        return messageStatus;
    }

    /**
     * @param messageStatus
     *            the messageStatus to set
     */
    public void setMessageStatus(String messageStatus) {
        this.messageStatus = messageStatus;
    }

    /**
     * @return the requestStatus
     */
    public String getRequestStatus() {
        return requestStatus;
    }

    /**
     * @param requestStatus
     *            the requestStatus to set
     */
    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    /**
     * @return the userName
     */
    /**
     * @return the customerUsername
     */
    public String getCustomerUsername() {
        return customerUsername;
    }

    /**
     * @param customerUsername
     *            the customerUsername to set
     */
    public void setCustomerUsername(String customerUsername) {
        this.customerUsername = customerUsername;
    }

    /**
     * @return the isRequestByAdmin
     */
    public boolean isRequestByAdmin() {
        return isRequestByAdmin;
    }

    /**
     * @param isRequestByAdmin
     *            the isRequestByAdmin to set
     */
    public void setRequestByAdmin(boolean isRequestByAdmin) {
        this.isRequestByAdmin = isRequestByAdmin;
    }

    /**
     * @return the currentUsername
     */
    public String getCurrentUsername() {
        return currentUsername;
    }

    /**
     * @param currentUsername
     *            the currentUsername to set
     */
    public void setCurrentUsername(String currentUsername) {
        this.currentUsername = currentUsername;
    }

    /**
     * @return the mediaCollection
     */
    public Map<String, String> getMediaCollection() {
        return mediaCollection;
    }

    /**
     * @param mediaCollection
     *            the mediaCollection to set
     */
    public void setMediaCollection(Map<String, String> mediaCollection) {
        this.mediaCollection = mediaCollection;
    }

    /**
     * @return the customerSalutation
     */
    public String getCustomerSalutation() {
        return customerSalutation;
    }

    /**
     * @param customerSalutation
     *            the customerSalutation to set
     */
    public void setCustomerSalutation(String customerSalutation) {
        this.customerSalutation = customerSalutation;
    }

    /**
     * @return the customerFirstName
     */
    public String getCustomerFirstName() {
        return customerFirstName;
    }

    /**
     * @param customerFirstName
     *            the customerFirstName to set
     */
    public void setCustomerFirstName(String customerFirstName) {
        this.customerFirstName = customerFirstName;
    }

    /**
     * @return the customerMiddleName
     */
    public String getCustomerMiddleName() {
        return customerMiddleName;
    }

    /**
     * @param customerMiddleName
     *            the customerMiddleName to set
     */
    public void setCustomerMiddleName(String customerMiddleName) {
        this.customerMiddleName = customerMiddleName;
    }

    /**
     * @return the customerLastName
     */
    public String getCustomerLastName() {
        return customerLastName;
    }

    /**
     * @param customerLastName
     *            the customerLastName to set
     */
    public void setCustomerLastName(String customerLastName) {
        this.customerLastName = customerLastName;
    }

    public boolean isCreateRequest() {
        return isCreateRequest;
    }

    public void setCreateRequest(boolean isCreateRequest) {
        this.isCreateRequest = isCreateRequest;
    }

}
