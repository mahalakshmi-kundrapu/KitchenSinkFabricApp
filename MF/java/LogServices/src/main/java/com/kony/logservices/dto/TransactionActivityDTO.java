package com.kony.logservices.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.kony.logservices.core.BaseActivity;

public class TransactionActivityDTO extends BaseActivity {

    private static final long serialVersionUID = -2632711015058528380L;

    private String transactionId;
    private String username;
    private String payeeName;
    private String serviceName;
    private String type;
    private String fromAccount;
    private String fromAccountType;
    private String toAccount;
    private String toAccountType;
    private BigDecimal amount;
    private String currencyCode;
    private String channel;
    private String status;
    private String description;
    private String routingNumber;
    private String batchId;
    private Date transactionDate;
    private String fromMobileOrEmail;
    private String toMobileOrEmail;
    private String swiftCode;
    private String internationalRoutingCode;
    private String module;
    private String customerId;
    private String device;
    private String operatingSystem;
    private String deviceId;
    private String ipAddress;
    private String referenceNumber;
    private String transactionDescription;
    private String errorCode;
    private String recipientType;
    private String recipientBankName;
    private String recipientAddress;
    private String recipientBankAddress;
    private String checkNumber;
    private String cashWithdrawalFor;
    private String ibanNumber;

    public String getInternationalRoutingCode() {
        return internationalRoutingCode;
    }

    public void setInternationalRoutingCode(String internationalRoutingCode) {
        this.internationalRoutingCode = internationalRoutingCode;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPayeeName() {
        return payeeName;
    }

    public void setPayeeName(String payeeName) {
        this.payeeName = payeeName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public String getFromAccountType() {
        return fromAccountType;
    }

    public void setFromAccountType(String fromAccountType) {
        this.fromAccountType = fromAccountType;
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }

    public String getToAccountType() {
        return toAccountType;
    }

    public void setToAccountType(String toAccountType) {
        this.toAccountType = toAccountType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getFromMobileOrEmail() {
        return fromMobileOrEmail;
    }

    public void setFromMobileOrEmail(String fromMobileOrEmail) {
        this.fromMobileOrEmail = fromMobileOrEmail;
    }

    public String getToMobileOrEmail() {
        return toMobileOrEmail;
    }

    public void setToMobileOrEmail(String toMobileOrEmail) {
        this.toMobileOrEmail = toMobileOrEmail;
    }

    public String getSwiftCode() {
        return swiftCode;
    }

    public void setSwiftCode(String swiftCode) {
        this.swiftCode = swiftCode;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getTransactionDescription() {
        return transactionDescription;
    }

    public void setTransactionDescription(String transactionDescription) {
        this.transactionDescription = transactionDescription;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }

    public String getRecipientBankName() {
        return recipientBankName;
    }

    public void setRecipientBankName(String recipientBankName) {
        this.recipientBankName = recipientBankName;
    }

    public String getRecipientAddress() {
        return recipientAddress;
    }

    public void setRecipientAddress(String recipientAddress) {
        this.recipientAddress = recipientAddress;
    }

    public String getRecipientBankAddress() {
        return recipientBankAddress;
    }

    public void setRecipientBankAddress(String recipientBankAddress) {
        this.recipientBankAddress = recipientBankAddress;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public String getCashWithdrawalFor() {
        return cashWithdrawalFor;
    }

    public void setCashWithdrawalFor(String cashWithdrawalFor) {
        this.cashWithdrawalFor = cashWithdrawalFor;
    }

    public String getIbanNumber() {
        return ibanNumber;
    }

    public void setIbanNumber(String ibanNumber) {
        this.ibanNumber = ibanNumber;
    }

}