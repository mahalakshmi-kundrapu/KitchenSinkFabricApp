package com.kony.logservices.dto;

import com.kony.logservices.core.BaseActivity;

public class MoneyMovementLogDTO extends BaseActivity {

    private static final long serialVersionUID = 1005645693167367247L;

    private String Customer_id;
    private int ExpenseCategory_id;
    private int Payee_id;
    private int Bill_id;
    private int Type_id;
    private String Reference_id;
    private String fromAccountNumber;
    private double fromAccountBalance;
    private String toAccountNumber;
    private double toAccountBalance;
    private double amount;
    private double convertedAmount;
    private String transactionCurrency;
    private String baseCurrency;
    private String Status_id;
    private String statusDesc;
    private String notes;
    private int checkNumber;
    private String description;
    private java.util.Date scheduledDate;
    private java.util.Date transactionDate;
    private java.util.Date createdDate;
    private String transactionComments;
    private String toExternalAccountNumber;
    private int Person_Id;
    private int numberOfRecurrences;
    private java.util.Date frequencyStartDate;
    private java.util.Date frequencyEndDate;
    private java.util.Date cashlessOTPValidDate;
    private String cashlessOTP;
    private String cashlessPhone;
    private String cashlessEmail;
    private String cashlessPersonName;
    private String cashlessMode;
    private String cashlessSecurityCode;
    private String cashWithdrawalTransactionStatus;
    private String cashlessPin;
    private String recurrenceDesc;
    private String deliverBy;
    private String p2pContact;
    private java.util.Date p2pRequiredDate;
    private String requestCreatedDate;
    private String viewReportLink;
    private String fee;
    private String feeCurrency;
    private String frontImage1;
    private String frontImage2;
    private String backImage1;
    private String backImage2;
    private String checkDesc;
    private String checkNumber1;
    private String checkNumber2;
    private String bankName1;
    private String bankName2;
    private String withdrawlAmount1;
    private String withdrawlAmount2;
    private String cashAmount;
    private String payeeCurrency;
    private int billid;
    private String disputeDescription;
    private String disputeReason;
    private String disputeStatus;
    private java.util.Date disputeDate;
    private String payeeName;
    private java.util.Date checkDateOfIssue;
    private String checkReason;
    private String amountRecieved;
    private java.util.Date requestValidity;
    private String statementReference;
    private String transCreditDebitIndicator;
    private String transactionInformation;
    private String addressLine;
    private String transactionAmount;
    private String chargeAmount;
    private String chargeCurrency;
    private String sourceCurrency;
    private String targetCurrency;
    private String unitCurrency;
    private String exchangeRate;
    private String contractIdentification;
    private String instructedAmount;
    private String instructedCurrency;
    private String transactionCode;
    private String transactionSubCode;
    private String proprietaryTransactionCode;
    private String proprietaryTransactionIssuer;
    private String balanceCreditDebitIndicator;
    private String balanceType;
    private String balanceAmount;
    private String balanceCurrency;
    private String merchantName;
    private String merchantCategoryCode;
    private String creditorAgentSchemeName;
    private String creditorAgentIdentification;
    private String creditorAgentName;
    private String creditorAgentaddressType;
    private String creditorAgentDepartment;
    private String creditorAgentSubDepartment;
    private String creditorAgentStreetName;
    private String creditorAgentBuildingNumber;
    private String creditorAgentPostCode;
    private String creditorAgentTownName;
    private String creditorAgentCountrySubDivision;
    private String creditorAgentCountry;
    private String creditorAgentAddressLine;
    private String creditorAccountSchemeName;
    private String creditorAccountIdentification;
    private String creditorAccountName;
    private String creditorAccountSeconIdentification;
    private String debtorAgentSchemeName;
    private String debtorAgentIdentification;
    private String debtorAgentName;
    private String debtorAgentAddressType;
    private String debtorAgentDepartment;
    private String debtorAgentSubDepartment;
    private String debtorAgentStreetName;
    private String debtorAgentBuildingNumber;
    private String dedtorAgentPostCode;
    private String debtorAgentTownName;
    private String debtorAgentCountrySubDivision;
    private String debtorAgentCountry;
    private String debtorAgentAddressLine;
    private String debtorAccountSchemeName;
    private String debtorAccountIdentification;
    private String debtorAccountName;
    private String debtorAccountSeconIdentification;
    private String cardInstrumentSchemeName;
    private String cardInstrumentAuthorisationType;
    private String cardInstrumentName;
    private String cardInstrumentIdentification;
    private String IBAN;
    private String sortCode;
    private java.util.Date FirstPaymentDateTime;
    private java.util.Date NextPaymentDateTime;
    private java.util.Date FinalPaymentDateTime;
    private String StandingOrderStatusCode;
    private double FP_Amount;
    private String FP_Currency;
    private double NP_Amount;
    private String NP_Currency;
    private double FPA_Amount;
    private String FPA_Currency;
    private String ConsentId;
    private String Initiation_InstructionIdentification;
    private String Initiation_EndToEndIdentification;
    private String RI_Reference;
    private String RI_Unstructured;
    private String RiskPaymentContextCode;
    private String MerchantCustomerIdentification;
    private String beneficiaryName;
    private String bankName;
    private String swiftCode;
    private String DomesticPaymentId;
    private String linkSelf;
    private java.util.Date StatusUpdateDateTime;
    private String dataStatus;
    private String serviceName;
    private String payPersonName;
    private boolean isScheduled;
    private boolean penaltyFlag;
    private boolean payoffFlag;
    private boolean isPaypersonDeleted;
    private boolean feePaidByReceipent;
    private boolean isDisputed;
    private boolean isPayeeDeleted;
    // private enum category {};
    private String checkImageBack;
    private String checkImage;
    private String imageURL1;
    private String imageURL2;
    private boolean hasDepositImage;
    private java.util.Date bookingDateTime;
    private java.util.Date valueDateTime;
    private java.util.Date quotationDate;
    private String frequencyType;
    private String category;
    private String billCategory;

    public String getCustomer_id() {
        return Customer_id;
    }

    public void setCustomer_id(String Customer_id) {
        this.Customer_id = Customer_id;
    }

    public int getExpenseCategory_id() {
        return ExpenseCategory_id;
    }

    public void setExpenseCategory_id(int ExpenseCategory_id) {
        this.ExpenseCategory_id = ExpenseCategory_id;
    }

    public int getPayee_id() {
        return Payee_id;
    }

    public void setPayee_id(int Payee_id) {
        this.Payee_id = Payee_id;
    }

    public int getBill_id() {
        return Bill_id;
    }

    public void setBill_id(int Bill_id) {
        this.Bill_id = Bill_id;
    }

    public int getType_id() {
        return Type_id;
    }

    public void setType_id(int Type_id) {
        this.Type_id = Type_id;
    }

    public String getReference_id() {
        return Reference_id;
    }

    public void setReference_id(String Reference_id) {
        this.Reference_id = Reference_id;
    }

    public String getfromAccountNumber() {
        return fromAccountNumber;
    }

    public void setfromAccountNumber(String fromAccountNumber) {
        this.fromAccountNumber = fromAccountNumber;
    }

    public double getfromAccountBalance() {
        return fromAccountBalance;
    }

    public void setfromAccountBalance(double fromAccountBalance) {
        this.fromAccountBalance = fromAccountBalance;
    }

    public String gettoAccountNumber() {
        return toAccountNumber;
    }

    public void settoAccountNumber(String toAccountNumber) {
        this.toAccountNumber = toAccountNumber;
    }

    public double gettoAccountBalance() {
        return toAccountBalance;
    }

    public void settoAccountBalance(double toAccountBalance) {
        this.toAccountBalance = toAccountBalance;
    }

    public double getamount() {
        return amount;
    }

    public void setamount(double amount) {
        this.amount = amount;
    }

    public double getconvertedAmount() {
        return convertedAmount;
    }

    public void setconvertedAmount(double convertedAmount) {
        this.convertedAmount = convertedAmount;
    }

    public String gettransactionCurrency() {
        return transactionCurrency;
    }

    public void settransactionCurrency(String transactionCurrency) {
        this.transactionCurrency = transactionCurrency;
    }

    public String getbaseCurrency() {
        return baseCurrency;
    }

    public void setbaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public String getStatus_id() {
        return Status_id;
    }

    public void setStatus_id(String Status_id) {
        this.Status_id = Status_id;
    }

    public String getstatusDesc() {
        return statusDesc;
    }

    public void setstatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public String getnotes() {
        return notes;
    }

    public void setnotes(String notes) {
        this.notes = notes;
    }

    public int getcheckNumber() {
        return checkNumber;
    }

    public void setcheckNumber(int checkNumber) {
        this.checkNumber = checkNumber;
    }

    public String getdescription() {
        return description;
    }

    public void setdescription(String description) {
        this.description = description;
    }

    public java.util.Date getscheduledDate() {
        return scheduledDate;
    }

    public void setscheduledDate(java.util.Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public java.util.Date gettransactionDate() {
        return transactionDate;
    }

    public void settransactionDate(java.util.Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public java.util.Date getcreatedDate() {
        return createdDate;
    }

    public void setcreatedDate(java.util.Date createdDate) {
        this.createdDate = createdDate;
    }

    public String gettransactionComments() {
        return transactionComments;
    }

    public void settransactionComments(String transactionComments) {
        this.transactionComments = transactionComments;
    }

    public String gettoExternalAccountNumber() {
        return toExternalAccountNumber;
    }

    public void settoExternalAccountNumber(String toExternalAccountNumber) {
        this.toExternalAccountNumber = toExternalAccountNumber;
    }

    public int getPerson_Id() {
        return Person_Id;
    }

    public void setPerson_Id(int Person_Id) {
        this.Person_Id = Person_Id;
    }

    public int getnumberOfRecurrences() {
        return numberOfRecurrences;
    }

    public void setnumberOfRecurrences(int numberOfRecurrences) {
        this.numberOfRecurrences = numberOfRecurrences;
    }

    public java.util.Date getfrequencyStartDate() {
        return frequencyStartDate;
    }

    public void setfrequencyStartDate(java.util.Date frequencyStartDate) {
        this.frequencyStartDate = frequencyStartDate;
    }

    public java.util.Date getfrequencyEndDate() {
        return frequencyEndDate;
    }

    public void setfrequencyEndDate(java.util.Date frequencyEndDate) {
        this.frequencyEndDate = frequencyEndDate;
    }

    public java.util.Date getcashlessOTPValidDate() {
        return cashlessOTPValidDate;
    }

    public void setcashlessOTPValidDate(java.util.Date cashlessOTPValidDate) {
        this.cashlessOTPValidDate = cashlessOTPValidDate;
    }

    public String getcashlessOTP() {
        return cashlessOTP;
    }

    public void setcashlessOTP(String cashlessOTP) {
        this.cashlessOTP = cashlessOTP;
    }

    public String getcashlessPhone() {
        return cashlessPhone;
    }

    public void setcashlessPhone(String cashlessPhone) {
        this.cashlessPhone = cashlessPhone;
    }

    public String getcashlessEmail() {
        return cashlessEmail;
    }

    public void setcashlessEmail(String cashlessEmail) {
        this.cashlessEmail = cashlessEmail;
    }

    public String getcashlessPersonName() {
        return cashlessPersonName;
    }

    public void setcashlessPersonName(String cashlessPersonName) {
        this.cashlessPersonName = cashlessPersonName;
    }

    public String getcashlessMode() {
        return cashlessMode;
    }

    public void setcashlessMode(String cashlessMode) {
        this.cashlessMode = cashlessMode;
    }

    public String getcashlessSecurityCode() {
        return cashlessSecurityCode;
    }

    public void setcashlessSecurityCode(String cashlessSecurityCode) {
        this.cashlessSecurityCode = cashlessSecurityCode;
    }

    public String getcashWithdrawalTransactionStatus() {
        return cashWithdrawalTransactionStatus;
    }

    public void setcashWithdrawalTransactionStatus(String cashWithdrawalTransactionStatus) {
        this.cashWithdrawalTransactionStatus = cashWithdrawalTransactionStatus;
    }

    public String getcashlessPin() {
        return cashlessPin;
    }

    public void setcashlessPin(String cashlessPin) {
        this.cashlessPin = cashlessPin;
    }

    public String getrecurrenceDesc() {
        return recurrenceDesc;
    }

    public void setrecurrenceDesc(String recurrenceDesc) {
        this.recurrenceDesc = recurrenceDesc;
    }

    public String getdeliverBy() {
        return deliverBy;
    }

    public void setdeliverBy(String deliverBy) {
        this.deliverBy = deliverBy;
    }

    public String getp2pContact() {
        return p2pContact;
    }

    public void setp2pContact(String p2pContact) {
        this.p2pContact = p2pContact;
    }

    public java.util.Date getp2pRequiredDate() {
        return p2pRequiredDate;
    }

    public void setp2pRequiredDate(java.util.Date p2pRequiredDate) {
        this.p2pRequiredDate = p2pRequiredDate;
    }

    public String getrequestCreatedDate() {
        return requestCreatedDate;
    }

    public void setrequestCreatedDate(String requestCreatedDate) {
        this.requestCreatedDate = requestCreatedDate;
    }

    public String getviewReportLink() {
        return viewReportLink;
    }

    public void setviewReportLink(String viewReportLink) {
        this.viewReportLink = viewReportLink;
    }

    public String getfee() {
        return fee;
    }

    public void setfee(String fee) {
        this.fee = fee;
    }

    public String getfeeCurrency() {
        return feeCurrency;
    }

    public void setfeeCurrency(String feeCurrency) {
        this.feeCurrency = feeCurrency;
    }

    public String getfrontImage1() {
        return frontImage1;
    }

    public void setfrontImage1(String frontImage1) {
        this.frontImage1 = frontImage1;
    }

    public String getfrontImage2() {
        return frontImage2;
    }

    public void setfrontImage2(String frontImage2) {
        this.frontImage2 = frontImage2;
    }

    public String getbackImage1() {
        return backImage1;
    }

    public void setbackImage1(String backImage1) {
        this.backImage1 = backImage1;
    }

    public String getbackImage2() {
        return backImage2;
    }

    public void setbackImage2(String backImage2) {
        this.backImage2 = backImage2;
    }

    public String getcheckDesc() {
        return checkDesc;
    }

    public void setcheckDesc(String checkDesc) {
        this.checkDesc = checkDesc;
    }

    public String getcheckNumber1() {
        return checkNumber1;
    }

    public void setcheckNumber1(String checkNumber1) {
        this.checkNumber1 = checkNumber1;
    }

    public String getcheckNumber2() {
        return checkNumber2;
    }

    public void setcheckNumber2(String checkNumber2) {
        this.checkNumber2 = checkNumber2;
    }

    public String getbankName1() {
        return bankName1;
    }

    public void setbankName1(String bankName1) {
        this.bankName1 = bankName1;
    }

    public String getbankName2() {
        return bankName2;
    }

    public void setbankName2(String bankName2) {
        this.bankName2 = bankName2;
    }

    public String getwithdrawlAmount1() {
        return withdrawlAmount1;
    }

    public void setwithdrawlAmount1(String withdrawlAmount1) {
        this.withdrawlAmount1 = withdrawlAmount1;
    }

    public String getwithdrawlAmount2() {
        return withdrawlAmount2;
    }

    public void setwithdrawlAmount2(String withdrawlAmount2) {
        this.withdrawlAmount2 = withdrawlAmount2;
    }

    public String getcashAmount() {
        return cashAmount;
    }

    public void setcashAmount(String cashAmount) {
        this.cashAmount = cashAmount;
    }

    public String getpayeeCurrency() {
        return payeeCurrency;
    }

    public void setpayeeCurrency(String payeeCurrency) {
        this.payeeCurrency = payeeCurrency;
    }

    public int getbillid() {
        return billid;
    }

    public void setbillid(int billid) {
        this.billid = billid;
    }

    public String getdisputeDescription() {
        return disputeDescription;
    }

    public void setdisputeDescription(String disputeDescription) {
        this.disputeDescription = disputeDescription;
    }

    public String getdisputeReason() {
        return disputeReason;
    }

    public void setdisputeReason(String disputeReason) {
        this.disputeReason = disputeReason;
    }

    public String getdisputeStatus() {
        return disputeStatus;
    }

    public void setdisputeStatus(String disputeStatus) {
        this.disputeStatus = disputeStatus;
    }

    public java.util.Date getdisputeDate() {
        return disputeDate;
    }

    public void setdisputeDate(java.util.Date disputeDate) {
        this.disputeDate = disputeDate;
    }

    public String getpayeeName() {
        return payeeName;
    }

    public void setpayeeName(String payeeName) {
        this.payeeName = payeeName;
    }

    public java.util.Date getcheckDateOfIssue() {
        return checkDateOfIssue;
    }

    public void setcheckDateOfIssue(java.util.Date checkDateOfIssue) {
        this.checkDateOfIssue = checkDateOfIssue;
    }

    public String getcheckReason() {
        return checkReason;
    }

    public void setcheckReason(String checkReason) {
        this.checkReason = checkReason;
    }

    public String getamountRecieved() {
        return amountRecieved;
    }

    public void setamountRecieved(String amountRecieved) {
        this.amountRecieved = amountRecieved;
    }

    public java.util.Date getrequestValidity() {
        return requestValidity;
    }

    public void setrequestValidity(java.util.Date requestValidity) {
        this.requestValidity = requestValidity;
    }

    public String getstatementReference() {
        return statementReference;
    }

    public void setstatementReference(String statementReference) {
        this.statementReference = statementReference;
    }

    public String gettransCreditDebitIndicator() {
        return transCreditDebitIndicator;
    }

    public void settransCreditDebitIndicator(String transCreditDebitIndicator) {
        this.transCreditDebitIndicator = transCreditDebitIndicator;
    }

    public String gettransactionInformation() {
        return transactionInformation;
    }

    public void settransactionInformation(String transactionInformation) {
        this.transactionInformation = transactionInformation;
    }

    public String getaddressLine() {
        return addressLine;
    }

    public void setaddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public String gettransactionAmount() {
        return transactionAmount;
    }

    public void settransactionAmount(String transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getchargeAmount() {
        return chargeAmount;
    }

    public void setchargeAmount(String chargeAmount) {
        this.chargeAmount = chargeAmount;
    }

    public String getchargeCurrency() {
        return chargeCurrency;
    }

    public void setchargeCurrency(String chargeCurrency) {
        this.chargeCurrency = chargeCurrency;
    }

    public String getsourceCurrency() {
        return sourceCurrency;
    }

    public void setsourceCurrency(String sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
    }

    public String gettargetCurrency() {
        return targetCurrency;
    }

    public void settargetCurrency(String targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public String getunitCurrency() {
        return unitCurrency;
    }

    public void setunitCurrency(String unitCurrency) {
        this.unitCurrency = unitCurrency;
    }

    public String getexchangeRate() {
        return exchangeRate;
    }

    public void setexchangeRate(String exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getcontractIdentification() {
        return contractIdentification;
    }

    public void setcontractIdentification(String contractIdentification) {
        this.contractIdentification = contractIdentification;
    }

    public String getinstructedAmount() {
        return instructedAmount;
    }

    public void setinstructedAmount(String instructedAmount) {
        this.instructedAmount = instructedAmount;
    }

    public String getinstructedCurrency() {
        return instructedCurrency;
    }

    public void setinstructedCurrency(String instructedCurrency) {
        this.instructedCurrency = instructedCurrency;
    }

    public String gettransactionCode() {
        return transactionCode;
    }

    public void settransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public String gettransactionSubCode() {
        return transactionSubCode;
    }

    public void settransactionSubCode(String transactionSubCode) {
        this.transactionSubCode = transactionSubCode;
    }

    public String getproprietaryTransactionCode() {
        return proprietaryTransactionCode;
    }

    public void setproprietaryTransactionCode(String proprietaryTransactionCode) {
        this.proprietaryTransactionCode = proprietaryTransactionCode;
    }

    public String getproprietaryTransactionIssuer() {
        return proprietaryTransactionIssuer;
    }

    public void setproprietaryTransactionIssuer(String proprietaryTransactionIssuer) {
        this.proprietaryTransactionIssuer = proprietaryTransactionIssuer;
    }

    public String getbalanceCreditDebitIndicator() {
        return balanceCreditDebitIndicator;
    }

    public void setbalanceCreditDebitIndicator(String balanceCreditDebitIndicator) {
        this.balanceCreditDebitIndicator = balanceCreditDebitIndicator;
    }

    public String getbalanceType() {
        return balanceType;
    }

    public void setbalanceType(String balanceType) {
        this.balanceType = balanceType;
    }

    public String getbalanceAmount() {
        return balanceAmount;
    }

    public void setbalanceAmount(String balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public String getbalanceCurrency() {
        return balanceCurrency;
    }

    public void setbalanceCurrency(String balanceCurrency) {
        this.balanceCurrency = balanceCurrency;
    }

    public String getmerchantName() {
        return merchantName;
    }

    public void setmerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getmerchantCategoryCode() {
        return merchantCategoryCode;
    }

    public void setmerchantCategoryCode(String merchantCategoryCode) {
        this.merchantCategoryCode = merchantCategoryCode;
    }

    public String getcreditorAgentSchemeName() {
        return creditorAgentSchemeName;
    }

    public void setcreditorAgentSchemeName(String creditorAgentSchemeName) {
        this.creditorAgentSchemeName = creditorAgentSchemeName;
    }

    public String getcreditorAgentIdentification() {
        return creditorAgentIdentification;
    }

    public void setcreditorAgentIdentification(String creditorAgentIdentification) {
        this.creditorAgentIdentification = creditorAgentIdentification;
    }

    public String getcreditorAgentName() {
        return creditorAgentName;
    }

    public void setcreditorAgentName(String creditorAgentName) {
        this.creditorAgentName = creditorAgentName;
    }

    public String getcreditorAgentaddressType() {
        return creditorAgentaddressType;
    }

    public void setcreditorAgentaddressType(String creditorAgentaddressType) {
        this.creditorAgentaddressType = creditorAgentaddressType;
    }

    public String getcreditorAgentDepartment() {
        return creditorAgentDepartment;
    }

    public void setcreditorAgentDepartment(String creditorAgentDepartment) {
        this.creditorAgentDepartment = creditorAgentDepartment;
    }

    public String getcreditorAgentSubDepartment() {
        return creditorAgentSubDepartment;
    }

    public void setcreditorAgentSubDepartment(String creditorAgentSubDepartment) {
        this.creditorAgentSubDepartment = creditorAgentSubDepartment;
    }

    public String getcreditorAgentStreetName() {
        return creditorAgentStreetName;
    }

    public void setcreditorAgentStreetName(String creditorAgentStreetName) {
        this.creditorAgentStreetName = creditorAgentStreetName;
    }

    public String getcreditorAgentBuildingNumber() {
        return creditorAgentBuildingNumber;
    }

    public void setcreditorAgentBuildingNumber(String creditorAgentBuildingNumber) {
        this.creditorAgentBuildingNumber = creditorAgentBuildingNumber;
    }

    public String getcreditorAgentPostCode() {
        return creditorAgentPostCode;
    }

    public void setcreditorAgentPostCode(String creditorAgentPostCode) {
        this.creditorAgentPostCode = creditorAgentPostCode;
    }

    public String getcreditorAgentTownName() {
        return creditorAgentTownName;
    }

    public void setcreditorAgentTownName(String creditorAgentTownName) {
        this.creditorAgentTownName = creditorAgentTownName;
    }

    public String getcreditorAgentCountrySubDivision() {
        return creditorAgentCountrySubDivision;
    }

    public void setcreditorAgentCountrySubDivision(String creditorAgentCountrySubDivision) {
        this.creditorAgentCountrySubDivision = creditorAgentCountrySubDivision;
    }

    public String getcreditorAgentCountry() {
        return creditorAgentCountry;
    }

    public void setcreditorAgentCountry(String creditorAgentCountry) {
        this.creditorAgentCountry = creditorAgentCountry;
    }

    public String getcreditorAgentAddressLine() {
        return creditorAgentAddressLine;
    }

    public void setcreditorAgentAddressLine(String creditorAgentAddressLine) {
        this.creditorAgentAddressLine = creditorAgentAddressLine;
    }

    public String getcreditorAccountSchemeName() {
        return creditorAccountSchemeName;
    }

    public void setcreditorAccountSchemeName(String creditorAccountSchemeName) {
        this.creditorAccountSchemeName = creditorAccountSchemeName;
    }

    public String getcreditorAccountIdentification() {
        return creditorAccountIdentification;
    }

    public void setcreditorAccountIdentification(String creditorAccountIdentification) {
        this.creditorAccountIdentification = creditorAccountIdentification;
    }

    public String getcreditorAccountName() {
        return creditorAccountName;
    }

    public void setcreditorAccountName(String creditorAccountName) {
        this.creditorAccountName = creditorAccountName;
    }

    public String getcreditorAccountSeconIdentification() {
        return creditorAccountSeconIdentification;
    }

    public void setcreditorAccountSeconIdentification(String creditorAccountSeconIdentification) {
        this.creditorAccountSeconIdentification = creditorAccountSeconIdentification;
    }

    public String getdebtorAgentSchemeName() {
        return debtorAgentSchemeName;
    }

    public void setdebtorAgentSchemeName(String debtorAgentSchemeName) {
        this.debtorAgentSchemeName = debtorAgentSchemeName;
    }

    public String getdebtorAgentIdentification() {
        return debtorAgentIdentification;
    }

    public void setdebtorAgentIdentification(String debtorAgentIdentification) {
        this.debtorAgentIdentification = debtorAgentIdentification;
    }

    public String getdebtorAgentName() {
        return debtorAgentName;
    }

    public void setdebtorAgentName(String debtorAgentName) {
        this.debtorAgentName = debtorAgentName;
    }

    public String getdebtorAgentAddressType() {
        return debtorAgentAddressType;
    }

    public void setdebtorAgentAddressType(String debtorAgentAddressType) {
        this.debtorAgentAddressType = debtorAgentAddressType;
    }

    public String getdebtorAgentDepartment() {
        return debtorAgentDepartment;
    }

    public void setdebtorAgentDepartment(String debtorAgentDepartment) {
        this.debtorAgentDepartment = debtorAgentDepartment;
    }

    public String getdebtorAgentSubDepartment() {
        return debtorAgentSubDepartment;
    }

    public void setdebtorAgentSubDepartment(String debtorAgentSubDepartment) {
        this.debtorAgentSubDepartment = debtorAgentSubDepartment;
    }

    public String getdebtorAgentStreetName() {
        return debtorAgentStreetName;
    }

    public void setdebtorAgentStreetName(String debtorAgentStreetName) {
        this.debtorAgentStreetName = debtorAgentStreetName;
    }

    public String getdebtorAgentBuildingNumber() {
        return debtorAgentBuildingNumber;
    }

    public void setdebtorAgentBuildingNumber(String debtorAgentBuildingNumber) {
        this.debtorAgentBuildingNumber = debtorAgentBuildingNumber;
    }

    public String getdedtorAgentPostCode() {
        return dedtorAgentPostCode;
    }

    public void setdedtorAgentPostCode(String dedtorAgentPostCode) {
        this.dedtorAgentPostCode = dedtorAgentPostCode;
    }

    public String getdebtorAgentTownName() {
        return debtorAgentTownName;
    }

    public void setdebtorAgentTownName(String debtorAgentTownName) {
        this.debtorAgentTownName = debtorAgentTownName;
    }

    public String getdebtorAgentCountrySubDivision() {
        return debtorAgentCountrySubDivision;
    }

    public void setdebtorAgentCountrySubDivision(String debtorAgentCountrySubDivision) {
        this.debtorAgentCountrySubDivision = debtorAgentCountrySubDivision;
    }

    public String getdebtorAgentCountry() {
        return debtorAgentCountry;
    }

    public void setdebtorAgentCountry(String debtorAgentCountry) {
        this.debtorAgentCountry = debtorAgentCountry;
    }

    public String getdebtorAgentAddressLine() {
        return debtorAgentAddressLine;
    }

    public void setdebtorAgentAddressLine(String debtorAgentAddressLine) {
        this.debtorAgentAddressLine = debtorAgentAddressLine;
    }

    public String getdebtorAccountSchemeName() {
        return debtorAccountSchemeName;
    }

    public void setdebtorAccountSchemeName(String debtorAccountSchemeName) {
        this.debtorAccountSchemeName = debtorAccountSchemeName;
    }

    public String getdebtorAccountIdentification() {
        return debtorAccountIdentification;
    }

    public void setdebtorAccountIdentification(String debtorAccountIdentification) {
        this.debtorAccountIdentification = debtorAccountIdentification;
    }

    public String getdebtorAccountName() {
        return debtorAccountName;
    }

    public void setdebtorAccountName(String debtorAccountName) {
        this.debtorAccountName = debtorAccountName;
    }

    public String getdebtorAccountSeconIdentification() {
        return debtorAccountSeconIdentification;
    }

    public void setdebtorAccountSeconIdentification(String debtorAccountSeconIdentification) {
        this.debtorAccountSeconIdentification = debtorAccountSeconIdentification;
    }

    public String getcardInstrumentSchemeName() {
        return cardInstrumentSchemeName;
    }

    public void setcardInstrumentSchemeName(String cardInstrumentSchemeName) {
        this.cardInstrumentSchemeName = cardInstrumentSchemeName;
    }

    public String getcardInstrumentAuthorisationType() {
        return cardInstrumentAuthorisationType;
    }

    public void setcardInstrumentAuthorisationType(String cardInstrumentAuthorisationType) {
        this.cardInstrumentAuthorisationType = cardInstrumentAuthorisationType;
    }

    public String getcardInstrumentName() {
        return cardInstrumentName;
    }

    public void setcardInstrumentName(String cardInstrumentName) {
        this.cardInstrumentName = cardInstrumentName;
    }

    public String getcardInstrumentIdentification() {
        return cardInstrumentIdentification;
    }

    public void setcardInstrumentIdentification(String cardInstrumentIdentification) {
        this.cardInstrumentIdentification = cardInstrumentIdentification;
    }

    public String getIBAN() {
        return IBAN;
    }

    public void setIBAN(String IBAN) {
        this.IBAN = IBAN;
    }

    public String getsortCode() {
        return sortCode;
    }

    public void setsortCode(String sortCode) {
        this.sortCode = sortCode;
    }

    public java.util.Date getFirstPaymentDateTime() {
        return FirstPaymentDateTime;
    }

    public void setFirstPaymentDateTime(java.util.Date FirstPaymentDateTime) {
        this.FirstPaymentDateTime = FirstPaymentDateTime;
    }

    public java.util.Date getNextPaymentDateTime() {
        return NextPaymentDateTime;
    }

    public void setNextPaymentDateTime(java.util.Date NextPaymentDateTime) {
        this.NextPaymentDateTime = NextPaymentDateTime;
    }

    public java.util.Date getFinalPaymentDateTime() {
        return FinalPaymentDateTime;
    }

    public void setFinalPaymentDateTime(java.util.Date FinalPaymentDateTime) {
        this.FinalPaymentDateTime = FinalPaymentDateTime;
    }

    public String getStandingOrderStatusCode() {
        return StandingOrderStatusCode;
    }

    public void setStandingOrderStatusCode(String StandingOrderStatusCode) {
        this.StandingOrderStatusCode = StandingOrderStatusCode;
    }

    public double getFP_Amount() {
        return FP_Amount;
    }

    public void setFP_Amount(double FP_Amount) {
        this.FP_Amount = FP_Amount;
    }

    public String getFP_Currency() {
        return FP_Currency;
    }

    public void setFP_Currency(String FP_Currency) {
        this.FP_Currency = FP_Currency;
    }

    public double getNP_Amount() {
        return NP_Amount;
    }

    public void setNP_Amount(double NP_Amount) {
        this.NP_Amount = NP_Amount;
    }

    public String getNP_Currency() {
        return NP_Currency;
    }

    public void setNP_Currency(String NP_Currency) {
        this.NP_Currency = NP_Currency;
    }

    public double getFPA_Amount() {
        return FPA_Amount;
    }

    public void setFPA_Amount(double FPA_Amount) {
        this.FPA_Amount = FPA_Amount;
    }

    public String getFPA_Currency() {
        return FPA_Currency;
    }

    public void setFPA_Currency(String FPA_Currency) {
        this.FPA_Currency = FPA_Currency;
    }

    public String getConsentId() {
        return ConsentId;
    }

    public void setConsentId(String ConsentId) {
        this.ConsentId = ConsentId;
    }

    public String getInitiation_InstructionIdentification() {
        return Initiation_InstructionIdentification;
    }

    public void setInitiation_InstructionIdentification(String Initiation_InstructionIdentification) {
        this.Initiation_InstructionIdentification = Initiation_InstructionIdentification;
    }

    public String getInitiation_EndToEndIdentification() {
        return Initiation_EndToEndIdentification;
    }

    public void setInitiation_EndToEndIdentification(String Initiation_EndToEndIdentification) {
        this.Initiation_EndToEndIdentification = Initiation_EndToEndIdentification;
    }

    public String getRI_Reference() {
        return RI_Reference;
    }

    public void setRI_Reference(String RI_Reference) {
        this.RI_Reference = RI_Reference;
    }

    public String getRI_Unstructured() {
        return RI_Unstructured;
    }

    public void setRI_Unstructured(String RI_Unstructured) {
        this.RI_Unstructured = RI_Unstructured;
    }

    public String getRiskPaymentContextCode() {
        return RiskPaymentContextCode;
    }

    public void setRiskPaymentContextCode(String RiskPaymentContextCode) {
        this.RiskPaymentContextCode = RiskPaymentContextCode;
    }

    public String getMerchantCustomerIdentification() {
        return MerchantCustomerIdentification;
    }

    public void setMerchantCustomerIdentification(String MerchantCustomerIdentification) {
        this.MerchantCustomerIdentification = MerchantCustomerIdentification;
    }

    public String getbeneficiaryName() {
        return beneficiaryName;
    }

    public void setbeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getbankName() {
        return bankName;
    }

    public void setbankName(String bankName) {
        this.bankName = bankName;
    }

    public String getswiftCode() {
        return swiftCode;
    }

    public void setswiftCode(String swiftCode) {
        this.swiftCode = swiftCode;
    }

    public String getDomesticPaymentId() {
        return DomesticPaymentId;
    }

    public void setDomesticPaymentId(String DomesticPaymentId) {
        this.DomesticPaymentId = DomesticPaymentId;
    }

    public String getlinkSelf() {
        return linkSelf;
    }

    public void setlinkSelf(String linkSelf) {
        this.linkSelf = linkSelf;
    }

    public java.util.Date getStatusUpdateDateTime() {
        return StatusUpdateDateTime;
    }

    public void setStatusUpdateDateTime(java.util.Date StatusUpdateDateTime) {
        this.StatusUpdateDateTime = StatusUpdateDateTime;
    }

    public String getdataStatus() {
        return dataStatus;
    }

    public void setdataStatus(String dataStatus) {
        this.dataStatus = dataStatus;
    }

    public String getserviceName() {
        return serviceName;
    }

    public void setserviceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getpayPersonName() {
        return payPersonName;
    }

    public void setpayPersonName(String payPersonName) {
        this.payPersonName = payPersonName;
    }

    public boolean getisScheduled() {
        return isScheduled;
    }

    public void setisScheduled(String isScheduled) {
        if (isScheduled.equals("0"))
            this.isScheduled = false;
        else
            this.isScheduled = true;
    }

    public boolean getpenaltyFlag() {
        return penaltyFlag;
    }

    public void setpenaltyFlag(String penaltyFlag) {
        if (penaltyFlag.equals("0"))
            this.penaltyFlag = false;
        else
            this.penaltyFlag = true;
    }

    public boolean getpayoffFlag() {
        return payoffFlag;
    }

    public void setpayoffFlag(String payoffFlag) {
        if (payoffFlag.equals("0"))
            this.payoffFlag = false;
        else
            this.payoffFlag = true;
    }

    public boolean getisPaypersonDeleted() {
        return isPaypersonDeleted;
    }

    public void setisPaypersonDeleted(String isPaypersonDeleted) {
        if (isPaypersonDeleted.equals("0"))
            this.isPaypersonDeleted = false;
        else
            this.isPaypersonDeleted = true;
    }

    public boolean getfeePaidByReceipent() {
        return feePaidByReceipent;
    }

    public void setfeePaidByReceipent(String feePaidByReceipent) {
        if (feePaidByReceipent.equals("0"))
            this.feePaidByReceipent = false;
        else
            this.feePaidByReceipent = true;
    }

    public boolean getisDisputed() {
        return isDisputed;
    }

    public void setisDisputed(String isDisputed) {
        if (isDisputed.equals("0"))
            this.isDisputed = false;
        else
            this.isDisputed = true;
    }

    public boolean getisPayeeDeleted() {
        return isPayeeDeleted;
    }

    public void setisPayeeDeleted(String isPayeeDeleted) {
        if (isPayeeDeleted.equalsIgnoreCase("0")) {
            this.isPayeeDeleted = false;
        } else {
            this.isPayeeDeleted = true;
        }

    }

    public String getcheckImageBack() {
        return checkImageBack;
    }

    public void setcheckImageBack(String checkImageBack) {
        this.checkImageBack = checkImageBack;
    }

    public String getcheckImage() {
        return checkImage;
    }

    public void setcheckImage(String checkImage) {
        this.checkImage = checkImage;
    }

    public String getimageURL1() {
        return imageURL1;
    }

    public void setimageURL1(String imageURL1) {
        this.imageURL1 = imageURL1;
    }

    public String getimageURL2() {
        return imageURL2;
    }

    public void setimageURL2(String imageURL2) {
        this.imageURL2 = imageURL2;
    }

    public boolean gethasDepositImage() {
        return hasDepositImage;
    }

    public void sethasDepositImage(boolean hasDepositImage) {
        this.hasDepositImage = hasDepositImage;
    }

    public java.util.Date getbookingDateTime() {
        return bookingDateTime;
    }

    public void setbookingDateTime(java.util.Date bookingDateTime) {
        this.bookingDateTime = bookingDateTime;
    }

    public java.util.Date getvalueDateTime() {
        return valueDateTime;
    }

    public void setvalueDateTime(java.util.Date valueDateTime) {
        this.valueDateTime = valueDateTime;
    }

    public java.util.Date getquotationDate() {
        return quotationDate;
    }

    public void setquotationDate(java.util.Date quotationDate) {
        this.quotationDate = quotationDate;
    }

    public String getfrequencyType() {
        return frequencyType;
    }

    public void setfrequencyType(String frequencyType) {
        this.frequencyType = frequencyType;
    }

    public String getcategory() {
        return category;
    }

    public void setcategory(String category) {
        this.category = category;
    }

    public String getbillCategory() {
        return billCategory;
    }

    public void setbillcategory(String billCategory) {
        this.billCategory = billCategory;
    }
}
