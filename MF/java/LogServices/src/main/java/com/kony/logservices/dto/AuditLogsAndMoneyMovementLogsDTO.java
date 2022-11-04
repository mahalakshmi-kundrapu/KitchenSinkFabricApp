package com.kony.logservices.dto;

import com.kony.logservices.core.BaseActivity;

public class AuditLogsAndMoneyMovementLogsDTO extends BaseActivity {

    private static final long serialVersionUID = 1009876789877367247L;

    private String Id;
    private String EventId;
    private String EventType;
    private String EventSubType;
    private String Status_Id;
    private String sessionId;
    private String AppId;
    private String UserName;
    private String Customer_Id;
    private Boolean isCSRAssist;
    private String appSessionId;
    private String payeeNickName;
    private String relationshipNumber;
    private String AdminUserName;
    private String AdminUserRole;
    private String Producer;
    private String EventData;
    private String mfa_State;
    private String mfa_ServiceKey;
    private String mfa_Type;
    private String phoneNumber;
    private String email;
    private String deviceModel;
    private String operatingSystem;
    private String deviceId;
    private String channel;
    private String appVersion;
    private String platform;
    private String ipAddress;
    private String eventts;
    private String createdby;
    private String createdts;

    private String isScheduled;
    private String Payee_id;
    private String Bill_id;
    private String Type_id;
    private String Reference_id;
    private String fromAccountNumber;
    private String toAccountNumber;
    private String amount;
    private String transactionCurrency;
    private String baseCurrency;
    private String notes;
    private String checkNumber;
    private String description;
    private String scheduledDate;
    private String transactionDate;
    private String createdDate;
    private String transactionComments;
    private String toExternalAccountNumber;
    private String Person_Id;
    private String frequencyType;
    private String frequencyStartDate;
    private String frequencyEndDate;
    private String cashlessPin;
    private String category;
    private String billCategory;
    private String recurrenceDesc;
    private String deliverBy;
    private String p2pContact;
    private String p2pRequiredDate;
    private String requestCreatedDate;
    private String penaltyFlag;
    private String payoffFlag;
    private String viewReportLink;
    private String isPaypersonDeleted;
    private String fee;
    private String feeCurrency;
    private String feePaidByReceipent;
    private String checkDesc;
    private String checkNumber1;
    private String checkNumber2;
    private String withdrawlAmount1;
    private String withdrawlAmount2;
    private String cashAmount;
    private String payeeCurrency;
    private String isDisputed;
    private String disputeDescription;
    private String disputeReason;
    private String disputeStatus;
    private String disputeDate;
    private String payeeName;
    private String checkDateOfIssue;
    private String checkReason;
    private String isPayeeDeleted;
    private String amountRecieved;
    private String addressLine;
    private String transactionAmount;
    private String chargeAmount;
    private String chargeCurrency;
    private String sourceCurrency;
    private String targetCurrency;
    private String unitCurrency;
    private String exchangeRate;
    private String balanceType;
    private String balanceAmount;
    private String balanceCurrency;
    private String cardInstrumentName;
    private String cardInstrumentIdentification;
    private String IBAN;
    private String RI_Reference;
    private String beneficiaryName;
    private String bankName;
    private String swiftCode;
    private String DomesticPaymentId;
    private String payPersonName;

    public String getRelationshipNumber() {
        return relationshipNumber;
    }

    public void setRelationshipNumber(String relationshipNumber) {
        this.relationshipNumber = relationshipNumber;
    }

    public String getEventts() {
        return eventts;
    }

    public void setEventts(String eventts) {
        this.eventts = eventts;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getEventId() {
        return EventId;
    }

    public void setEventId(String eventId) {
        EventId = eventId;
    }

    public String getEventType() {
        return EventType;
    }

    public void setEventType(String eventType) {
        EventType = eventType;
    }

    public String getEventSubType() {
        return EventSubType;
    }

    public void setEventSubType(String eventSubType) {
        EventSubType = eventSubType;
    }

    public String getStatus_Id() {
        return Status_Id;
    }

    public void setStatus_Id(String status_Id) {
        Status_Id = status_Id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAppId() {
        return AppId;
    }

    public void setAppId(String appId) {
        AppId = appId;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getCustomer_Id() {
        return Customer_Id;
    }

    public void setCustomer_Id(String customer_Id) {
        Customer_Id = customer_Id;
    }

    public Boolean getIsCSRAssist() {
        return isCSRAssist;
    }

    public void setIsCSRAssist(Boolean isCSRAssist) {
        this.isCSRAssist = isCSRAssist;
    }

    public String getAppSessionId() {
        return appSessionId;
    }

    public void setAppSessionId(String appSessionId) {
        this.appSessionId = appSessionId;
    }

    public String getPayeeNickName() {
        return payeeNickName;
    }

    public void setPayeeNickName(String payeeNickName) {
        this.payeeNickName = payeeNickName;
    }

    public String getAdminUserName() {
        return AdminUserName;
    }

    public void setAdminUserName(String adminUserName) {
        AdminUserName = adminUserName;
    }

    public String getAdminUserRole() {
        return AdminUserRole;
    }

    public void setAdminUserRole(String adminUserRole) {
        AdminUserRole = adminUserRole;
    }

    public String getProducer() {
        return Producer;
    }

    public void setProducer(String producer) {
        Producer = producer;
    }

    public String getEventData() {
        return EventData;
    }

    public void setEventData(String eventData) {
        EventData = eventData;
    }

    public String getMfa_State() {
        return mfa_State;
    }

    public void setMfa_State(String mfa_State) {
        this.mfa_State = mfa_State;
    }

    public String getMfa_ServiceKey() {
        return mfa_ServiceKey;
    }

    public void setMfa_ServiceKey(String mfa_ServiceKey) {
        this.mfa_ServiceKey = mfa_ServiceKey;
    }

    public String getMfa_Type() {
        return mfa_Type;
    }

    public void setMfa_Type(String mfa_Type) {
        this.mfa_Type = mfa_Type;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
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

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public String getCreatedts() {
        return createdts;
    }

    public void setCreatedts(String createdts) {
        this.createdts = createdts;
    }

    public String getIsScheduled() {
        return isScheduled;
    }

    public void setIsScheduled(String isScheduled) {
        this.isScheduled = isScheduled;
    }

    public String getPayee_id() {
        return Payee_id;
    }

    public void setPayee_id(String payee_id) {
        Payee_id = payee_id;
    }

    public String getBill_id() {
        return Bill_id;
    }

    public void setBill_id(String bill_id) {
        Bill_id = bill_id;
    }

    public String getType_id() {
        return Type_id;
    }

    public void setType_id(String type_id) {
        Type_id = type_id;
    }

    public String getReference_id() {
        return Reference_id;
    }

    public void setReference_id(String reference_id) {
        Reference_id = reference_id;
    }

    public String getFromAccountNumber() {
        return fromAccountNumber;
    }

    public void setFromAccountNumber(String fromAccountNumber) {
        this.fromAccountNumber = fromAccountNumber;
    }

    public String getToAccountNumber() {
        return toAccountNumber;
    }

    public void setToAccountNumber(String toAccountNumber) {
        this.toAccountNumber = toAccountNumber;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTransactionCurrency() {
        return transactionCurrency;
    }

    public void setTransactionCurrency(String transactionCurrency) {
        this.transactionCurrency = transactionCurrency;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(String scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getTransactionComments() {
        return transactionComments;
    }

    public void setTransactionComments(String transactionComments) {
        this.transactionComments = transactionComments;
    }

    public String getToExternalAccountNumber() {
        return toExternalAccountNumber;
    }

    public void setToExternalAccountNumber(String toExternalAccountNumber) {
        this.toExternalAccountNumber = toExternalAccountNumber;
    }

    public String getPerson_Id() {
        return Person_Id;
    }

    public void setPerson_Id(String person_Id) {
        Person_Id = person_Id;
    }

    public String getFrequencyType() {
        return frequencyType;
    }

    public void setFrequencyType(String frequencyType) {
        this.frequencyType = frequencyType;
    }

    public String getFrequencyStartDate() {
        return frequencyStartDate;
    }

    public void setFrequencyStartDate(String frequencyStartDate) {
        this.frequencyStartDate = frequencyStartDate;
    }

    public String getFrequencyEndDate() {
        return frequencyEndDate;
    }

    public void setFrequencyEndDate(String frequencyEndDate) {
        this.frequencyEndDate = frequencyEndDate;
    }

    public String getCashlessPin() {
        return cashlessPin;
    }

    public void setCashlessPin(String cashlessPin) {
        this.cashlessPin = cashlessPin;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBillCategory() {
        return billCategory;
    }

    public void setBillCategory(String billCategory) {
        this.billCategory = billCategory;
    }

    public String getRecurrenceDesc() {
        return recurrenceDesc;
    }

    public void setRecurrenceDesc(String recurrenceDesc) {
        this.recurrenceDesc = recurrenceDesc;
    }

    public String getDeliverBy() {
        return deliverBy;
    }

    public void setDeliverBy(String deliverBy) {
        this.deliverBy = deliverBy;
    }

    public String getP2pContact() {
        return p2pContact;
    }

    public void setP2pContact(String p2pContact) {
        this.p2pContact = p2pContact;
    }

    public String getP2pRequiredDate() {
        return p2pRequiredDate;
    }

    public void setP2pRequiredDate(String p2pRequiredDate) {
        this.p2pRequiredDate = p2pRequiredDate;
    }

    public String getRequestCreatedDate() {
        return requestCreatedDate;
    }

    public void setRequestCreatedDate(String requestCreatedDate) {
        this.requestCreatedDate = requestCreatedDate;
    }

    public String getPenaltyFlag() {
        return penaltyFlag;
    }

    public void setPenaltyFlag(String penaltyFlag) {
        this.penaltyFlag = penaltyFlag;
    }

    public String getPayoffFlag() {
        return payoffFlag;
    }

    public void setPayoffFlag(String payoffFlag) {
        this.payoffFlag = payoffFlag;
    }

    public String getViewReportLink() {
        return viewReportLink;
    }

    public void setViewReportLink(String viewReportLink) {
        this.viewReportLink = viewReportLink;
    }

    public String getIsPaypersonDeleted() {
        return isPaypersonDeleted;
    }

    public void setIsPaypersonDeleted(String isPaypersonDeleted) {
        this.isPaypersonDeleted = isPaypersonDeleted;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getFeeCurrency() {
        return feeCurrency;
    }

    public void setFeeCurrency(String feeCurrency) {
        this.feeCurrency = feeCurrency;
    }

    public String getFeePaidByReceipent() {
        return feePaidByReceipent;
    }

    public void setFeePaidByReceipent(String feePaidByReceipent) {
        this.feePaidByReceipent = feePaidByReceipent;
    }

    public String getCheckDesc() {
        return checkDesc;
    }

    public void setCheckDesc(String checkDesc) {
        this.checkDesc = checkDesc;
    }

    public String getCheckNumber1() {
        return checkNumber1;
    }

    public void setCheckNumber1(String checkNumber1) {
        this.checkNumber1 = checkNumber1;
    }

    public String getCheckNumber2() {
        return checkNumber2;
    }

    public void setCheckNumber2(String checkNumber2) {
        this.checkNumber2 = checkNumber2;
    }

    public String getWithdrawlAmount1() {
        return withdrawlAmount1;
    }

    public void setWithdrawlAmount1(String withdrawlAmount1) {
        this.withdrawlAmount1 = withdrawlAmount1;
    }

    public String getWithdrawlAmount2() {
        return withdrawlAmount2;
    }

    public void setWithdrawlAmount2(String withdrawlAmount2) {
        this.withdrawlAmount2 = withdrawlAmount2;
    }

    public String getCashAmount() {
        return cashAmount;
    }

    public void setCashAmount(String cashAmount) {
        this.cashAmount = cashAmount;
    }

    public String getPayeeCurrency() {
        return payeeCurrency;
    }

    public void setPayeeCurrency(String payeeCurrency) {
        this.payeeCurrency = payeeCurrency;
    }

    public String getIsDisputed() {
        return isDisputed;
    }

    public void setIsDisputed(String isDisputed) {
        this.isDisputed = isDisputed;
    }

    public String getDisputeDescription() {
        return disputeDescription;
    }

    public void setDisputeDescription(String disputeDescription) {
        this.disputeDescription = disputeDescription;
    }

    public String getDisputeReason() {
        return disputeReason;
    }

    public void setDisputeReason(String disputeReason) {
        this.disputeReason = disputeReason;
    }

    public String getDisputeStatus() {
        return disputeStatus;
    }

    public void setDisputeStatus(String disputeStatus) {
        this.disputeStatus = disputeStatus;
    }

    public String getDisputeDate() {
        return disputeDate;
    }

    public void setDisputeDate(String disputeDate) {
        this.disputeDate = disputeDate;
    }

    public String getPayeeName() {
        return payeeName;
    }

    public void setPayeeName(String payeeName) {
        this.payeeName = payeeName;
    }

    public String getCheckDateOfIssue() {
        return checkDateOfIssue;
    }

    public void setCheckDateOfIssue(String checkDateOfIssue) {
        this.checkDateOfIssue = checkDateOfIssue;
    }

    public String getCheckReason() {
        return checkReason;
    }

    public void setCheckReason(String checkReason) {
        this.checkReason = checkReason;
    }

    public String getIsPayeeDeleted() {
        return isPayeeDeleted;
    }

    public void setIsPayeeDeleted(String isPayeeDeleted) {
        this.isPayeeDeleted = isPayeeDeleted;
    }

    public String getAmountRecieved() {
        return amountRecieved;
    }

    public void setAmountRecieved(String amountRecieved) {
        this.amountRecieved = amountRecieved;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public String getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(String transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getChargeAmount() {
        return chargeAmount;
    }

    public void setChargeAmount(String chargeAmount) {
        this.chargeAmount = chargeAmount;
    }

    public String getChargeCurrency() {
        return chargeCurrency;
    }

    public void setChargeCurrency(String chargeCurrency) {
        this.chargeCurrency = chargeCurrency;
    }

    public String getSourceCurrency() {
        return sourceCurrency;
    }

    public void setSourceCurrency(String sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(String targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public String getUnitCurrency() {
        return unitCurrency;
    }

    public void setUnitCurrency(String unitCurrency) {
        this.unitCurrency = unitCurrency;
    }

    public String getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(String exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getBalanceType() {
        return balanceType;
    }

    public void setBalanceType(String balanceType) {
        this.balanceType = balanceType;
    }

    public String getBalanceAmount() {
        return balanceAmount;
    }

    public void setBalanceAmount(String balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public String getBalanceCurrency() {
        return balanceCurrency;
    }

    public void setBalanceCurrency(String balanceCurrency) {
        this.balanceCurrency = balanceCurrency;
    }

    public String getCardInstrumentName() {
        return cardInstrumentName;
    }

    public void setCardInstrumentName(String cardInstrumentName) {
        this.cardInstrumentName = cardInstrumentName;
    }

    public String getCardInstrumentIdentification() {
        return cardInstrumentIdentification;
    }

    public void setCardInstrumentIdentification(String cardInstrumentIdentification) {
        this.cardInstrumentIdentification = cardInstrumentIdentification;
    }

    public String getIBAN() {
        return IBAN;
    }

    public void setIBAN(String iBAN) {
        IBAN = iBAN;
    }

    public String getRI_Reference() {
        return RI_Reference;
    }

    public void setRI_Reference(String rI_Reference) {
        RI_Reference = rI_Reference;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getSwiftCode() {
        return swiftCode;
    }

    public void setSwiftCode(String swiftCode) {
        this.swiftCode = swiftCode;
    }

    public String getDomesticPaymentId() {
        return DomesticPaymentId;
    }

    public void setDomesticPaymentId(String domesticPaymentId) {
        DomesticPaymentId = domesticPaymentId;
    }

    public String getPayPersonName() {
        return payPersonName;
    }

    public void setPayPersonName(String payPersonName) {
        this.payPersonName = payPersonName;
    }

}
