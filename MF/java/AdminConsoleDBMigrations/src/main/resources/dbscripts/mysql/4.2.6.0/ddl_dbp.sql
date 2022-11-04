ALTER TABLE `application`
ADD COLUMN `isAlertAccountIDLevel` TINYINT(1) NOT NULL DEFAULT '1' AFTER `showAdsPostLogin`;

ALTER TABLE `dbxcustomeralertentitlement` 
DROP FOREIGN KEY `FK_dbxcustomeralertentitlement_accounts`;
ALTER TABLE `dbxcustomeralertentitlement` 
DROP INDEX `FK_dbxcustomeralertentitlement_accounts_idx` ;

ALTER TABLE `customeralertswitch` 
DROP FOREIGN KEY `FK_customeralertswitch_accounts`;
ALTER TABLE `customeralertswitch` 
DROP INDEX `FK_customeralertswitch_accounts_idx` ;


-- customeralertcategorychannel
ALTER TABLE `customeralertcategorychannel` 
ADD COLUMN `AccountType` VARCHAR(100) NOT NULL AFTER `AccountId`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`Customer_id`, `AlertCategoryId`, `ChannelId`, `AccountId`, `AccountType`);

ALTER TABLE `customeralertcategorychannel` 
CHANGE COLUMN `AccountId` `AccountId` VARCHAR(100) NOT NULL DEFAULT '*' ,
CHANGE COLUMN `AccountType` `AccountType` VARCHAR(100) NOT NULL DEFAULT '*' ;

-- dbxcustomeralertentitlement
UPDATE `dbxcustomeralertentitlement` SET `AccountId` = '*' where `AccountId` is null;

ALTER TABLE `dbxcustomeralertentitlement` 
ADD COLUMN `AccountType` VARCHAR(50) NOT NULL AFTER `AccountId`,
CHANGE COLUMN `AccountId` `AccountId` VARCHAR(255) NOT NULL ,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`Customer_id`, `AlertTypeId`, `AccountType`, `AccountId`);


-- customeralertswitch
UPDATE `customeralertswitch` SET `AccountID` = '*' where `AccountID` is null;

ALTER TABLE `customeralertswitch` 
ADD COLUMN `AccountType` VARCHAR(100) NOT NULL AFTER `AlertCategoryId`,
CHANGE COLUMN `AccountID` `AccountID` VARCHAR(50) NOT NULL ,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`Customer_id`, `AlertCategoryId`, `AccountType`, `AccountID`);

ALTER TABLE `customeralertswitch` 
CHANGE COLUMN `AccountID` `AccountID` VARCHAR(50) NOT NULL DEFAULT '*' ,
CHANGE COLUMN `AccountType` `AccountType` VARCHAR(100) NOT NULL DEFAULT '*' ;

CREATE TABLE `customertermsandconditions` (
  `id` varchar(50) NOT NULL,
  `customerId` varchar(50) NOT NULL,
  `termsAndConditionsCode` varchar(45) NOT NULL,
  `versionId` varchar(45) NOT NULL,
  `appId` varchar(45) DEFAULT NULL,
  `channelId` varchar(45) DEFAULT NULL,
  `createdby` varchar(45) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

DROP VIEW IF EXISTS `fetch_templaterecord_details_view`;
CREATE VIEW `fetch_templaterecord_details_view` AS
    SELECT 
        `bbtemplaterecord`.`TemplateRecord_id` AS `TemplateRecord_id`,
        `bbtemplaterecord`.`Record_Name` AS `Record_Name`,
        `bbtemplaterecord`.`ToAccountNumber` AS `ToAccountNumber`,
        `bbtemplaterecord`.`ABATRCNumber` AS `ABATRCNumber`,
        `bbtemplaterecord`.`Detail_id` AS `Detail_id`,
        `bbtemplaterecord`.`Amount` AS `Amount`,
        `bbtemplaterecord`.`AdditionalInfo` AS `AdditionalInfo`,
        `bbtemplaterecord`.`EIN` AS `EIN`,
        `bbtemplaterecord`.`IsZeroTaxDue` AS `IsZeroTaxDue`,
        `bbtemplaterecord`.`Template_id` AS `Template_id`,
        `bbtemplaterecord`.`TaxType_id` AS `TaxType_id`,
        `bbtemplaterecord`.`TemplateRequestType_id` AS `TemplateRequestType_id`,
        `bbtemplaterecord`.`softDelete` AS `softDelete`,
        `bbtemplaterecord`.`ToAccountType` AS `ToAccountType`,
        `bbtaxtype`.`taxType` AS `TaxType`,
        `achaccountstype`.`accountType` AS `ToAccountTypeValue`,
        `bbtemplaterequesttype`.`TemplateRequestTypeName` AS `TemplateRequestTypeValue`
    FROM
        (((`bbtemplaterecord`
        LEFT JOIN `bbtaxtype` ON ((`bbtemplaterecord`.`TaxType_id` = `bbtaxtype`.`id`)))
        JOIN `achaccountstype` ON ((`bbtemplaterecord`.`ToAccountType` = `achaccountstype`.`id`)))
        JOIN `bbtemplaterequesttype` ON ((`bbtemplaterecord`.`TemplateRequestType_id` = `bbtemplaterequesttype`.`TemplateRequestType_id`)));

ALTER TABLE `customer` 
ADD COLUMN `isVipCustomer` TINYINT(1) NULL DEFAULT '0' AFTER `DefaultLanguage`;

ALTER TABLE `customer` 
CHANGE COLUMN `isVipCustomer` `isVIPCustomer` TINYINT(1) NULL DEFAULT NULL ;

CREATE TABLE backendcertificate (
id INT NOT NULL AUTO_INCREMENT,
BackendName VARCHAR(45) NOT NULL,
CertName VARCHAR(45) NOT NULL,
CertValue LONGTEXT NOT NULL,
createdby VARCHAR(45) NULL,
modifiedby VARCHAR(45) NULL,
createdts TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
lastmodifiedts TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (id));

ALTER TABLE `mfaservice` 
CHANGE COLUMN `payload` `payload` TEXT NULL DEFAULT NULL ;

ALTER TABLE `transaction` 
CHANGE COLUMN `transactionDate` `transactionDate` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `transaction` 
ADD COLUMN `postedDate` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP AFTER `transactionDate`;

CREATE 
     OR REPLACE VIEW `accountransactionview` AS
    SELECT 
        `accounts`.`Account_id` AS `Account_id`,
        `accounts`.`AccountName` AS `AccountName`,
        `accounts`.`AccountHolder` AS `AccountHolder`,
        `accounts`.`UserName` AS `UserName`,
        `accounts`.`ExternalBankidentity_id` AS `ExternalBankidentity_id`,
        `accounts`.`CurrencyCode` AS `CurrencyCode`,
        `accounts`.`User_id` AS `User_id`,
        `accounts`.`AvailableBalance` AS `AvailableBalance`,
        `accounts`.`Bank_id` AS `Bank_id`,
        `accounts`.`ShowTransactions` AS `ShowTransactions`,
        `accounts`.`CurrentBalance` AS `CurrentBalance`,
        `accounts`.`RoutingNumber` AS `RoutingNumber`,
        `transaction`.`Id` AS `transactionId`,
        `transaction`.`Type_id` AS `transactiontype`,
        `transaction`.`Customer_id` AS `Customer_id`,
        `transaction`.`ExpenseCategory_id` AS `ExpenseCategory_id`,
        `transaction`.`billid` AS `Bill_id`,
        `transaction`.`Reference_id` AS `Reference_id`,
        `transaction`.`fromAccountNumber` AS `fromAccountNumber`,
        `transaction`.`fromAccountBalance` AS `fromAccountBalance`,
        `transaction`.`toAccountNumber` AS `toAccountNumber`,
        `transaction`.`toAccountBalance` AS `toAccountBalance`,
        `transaction`.`amount` AS `amount`,
        `transaction`.`convertedAmount` AS `convertedAmount`,
        `transaction`.`transactionCurrency` AS `transactionCurrency`,
        `transaction`.`baseCurrency` AS `baseCurrency`,
        `transaction`.`Status_id` AS `Status_id`,
        `transaction`.`statusDesc` AS `statusDesc`,
        `transaction`.`isScheduled` AS `isScheduled`,
        `transaction`.`category` AS `category`,
        `transaction`.`billCategory` AS `billCategory`,
        `transaction`.`toExternalAccountNumber` AS `ExternalAccountNumber`,
        `transaction`.`Person_Id` AS `Person_Id`,
        `transaction`.`frequencyType` AS `frequencyType`,
        `transaction`.`createdDate` AS `createdDate`,
        `transaction`.`cashlessEmail` AS `cashlessEmail`,
        `transaction`.`cashlessMode` AS `cashlessMode`,
        `transaction`.`cashlessOTP` AS `cashlessOTP`,
        `transaction`.`cashlessOTPValidDate` AS `cashlessOTPValidDate`,
        `transaction`.`cashlessPersonName` AS `cashlessPersonName`,
        `transaction`.`cashlessPhone` AS `cashlessPhone`,
        `transaction`.`cashlessSecurityCode` AS `cashlessSecurityCode`,
        `transaction`.`cashWithdrawalTransactionStatus` AS `cashWithdrawalTransactionStatus`,
        `transaction`.`frequencyEndDate` AS `frequencyEndDate`,
        `transaction`.`frequencyStartDate` AS `frequencyStartDate`,
        `transaction`.`hasDepositImage` AS `hasDepositImage`,
        `transaction`.`Payee_id` AS `payeeId`,
        `transaction`.`payeeName` AS `payeeName`,
        `transaction`.`p2pContact` AS `p2pContact`,
        `transaction`.`Person_Id` AS `personId`,
        `transaction`.`recurrenceDesc` AS `recurrenceDesc`,
        `transaction`.`numberOfRecurrences` AS `numberOfRecurrences`,
        `transaction`.`scheduledDate` AS `scheduledDate`,
        `transaction`.`transactionComments` AS `transactionComments`,
        `transaction`.`notes` AS `transactionsNotes`,
        `transaction`.`description` AS `transDescription`,
        `transaction`.`transactionDate` AS `transactionDate`,
        `transaction`.`postedDate` AS `postedDate`,
        `transaction`.`frontImage1` AS `frontImage1`,
        `transaction`.`frontImage2` AS `frontImage2`,
        `transaction`.`backImage1` AS `backImage1`,
        `transaction`.`backImage2` AS `backImage2`,
        `transaction`.`checkDesc` AS `checkDesc`,
        `transaction`.`checkNumber1` AS `checkNumber1`,
        `transaction`.`checkNumber2` AS `checkNumber2`,
        `transaction`.`checkNumber` AS `checkNumber`,
        `transaction`.`checkReason` AS `checkReason`,
        `transaction`.`requestValidity` AS `requestValidity`,
        `transaction`.`checkDateOfIssue` AS `checkDateOfIssue`,
        `transaction`.`bankName1` AS `bankName1`,
        `transaction`.`bankName2` AS `bankName2`,
        `transaction`.`withdrawlAmount1` AS `withdrawlAmount1`,
        `transaction`.`withdrawlAmount2` AS `withdrawlAmount2`,
        `transaction`.`cashAmount` AS `cashAmount`,
        `transaction`.`payeeCurrency` AS `payeeCurrency`,
        `transaction`.`fee` AS `fee`,
        `transaction`.`feePaidByReceipent` AS `feePaidByReceipent`,
        `transaction`.`feeCurrency` AS `feeCurrency`,
        `transaction`.`isDisputed` AS `isDisputed`,
        `transaction`.`disputeReason` AS `disputeReason`,
        `transaction`.`disputeDescription` AS `disputeDescription`,
        `transaction`.`disputeDate` AS `disputeDate`,
        `transaction`.`disputeStatus` AS `disputeStatus`,
        `transactiontype`.`description` AS `description`,
        `transaction`.`statementReference` AS `statementReference`,
        `transaction`.`transCreditDebitIndicator` AS `transCreditDebitIndicator`,
        `transaction`.`bookingDateTime` AS `bookingDateTime`,
        `transaction`.`valueDateTime` AS `valueDateTime`,
        `transaction`.`transactionInformation` AS `transactionInformation`,
        `transaction`.`addressLine` AS `addressLine`,
        `transaction`.`transactionAmount` AS `transactionAmount`,
        `transaction`.`chargeAmount` AS `chargeAmount`,
        `transaction`.`chargeCurrency` AS `chargeCurrency`,
        `transaction`.`sourceCurrency` AS `sourceCurrency`,
        `transaction`.`targetCurrency` AS `targetCurrency`,
        `transaction`.`unitCurrency` AS `unitCurrency`,
        `transaction`.`exchangeRate` AS `exchangeRate`,
        `transaction`.`contractIdentification` AS `contractIdentification`,
        `transaction`.`quotationDate` AS `quotationDate`,
        `transaction`.`instructedAmount` AS `instructedAmount`,
        `transaction`.`instructedCurrency` AS `instructedCurrency`,
        `transaction`.`transactionCode` AS `transactionCode`,
        `transaction`.`transactionSubCode` AS `transactionSubCode`,
        `transaction`.`proprietaryTransactionCode` AS `proprietaryTransactionCode`,
        `transaction`.`proprietaryTransactionIssuer` AS `proprietaryTransactionIssuer`,
        `transaction`.`balanceCreditDebitIndicator` AS `balanceCreditDebitIndicator`,
        `transaction`.`balanceType` AS `balanceType`,
        `transaction`.`balanceAmount` AS `balanceAmount`,
        `transaction`.`balanceCurrency` AS `balanceCurrency`,
        `transaction`.`merchantName` AS `merchantName`,
        `transaction`.`merchantCategoryCode` AS `merchantCategoryCode`,
        `transaction`.`creditorAgentSchemeName` AS `creditorAgentSchemeName`,
        `transaction`.`creditorAgentIdentification` AS `creditorAgentIdentification`,
        `transaction`.`creditorAgentName` AS `creditorAgentName`,
        `transaction`.`creditorAgentaddressType` AS `creditorAgentaddressType`,
        `transaction`.`creditorAgentDepartment` AS `creditorAgentDepartment`,
        `transaction`.`creditorAgentSubDepartment` AS `creditorAgentSubDepartment`,
        `transaction`.`creditorAgentStreetName` AS `creditorAgentStreetName`,
        `transaction`.`creditorAgentBuildingNumber` AS `creditorAgentBuildingNumber`,
        `transaction`.`creditorAgentPostCode` AS `creditorAgentPostCode`,
        `transaction`.`creditorAgentTownName` AS `creditorAgentTownName`,
        `transaction`.`creditorAgentCountrySubDivision` AS `creditorAgentCountrySubDivision`,
        `transaction`.`creditorAgentCountry` AS `creditorAgentCountry`,
        `transaction`.`creditorAgentAddressLine` AS `creditorAgentAddressLine`,
        `transaction`.`creditorAccountSchemeName` AS `creditorAccountSchemeName`,
        `transaction`.`creditorAccountIdentification` AS `creditorAccountIdentification`,
        `transaction`.`creditorAccountName` AS `creditorAccountName`,
        `transaction`.`creditorAccountSeconIdentification` AS `creditorAccountSeconIdentification`,
        `transaction`.`debtorAgentSchemeName` AS `debtorAgentSchemeName`,
        `transaction`.`debtorAgentIdentification` AS `debtorAgentIdentification`,
        `transaction`.`debtorAgentName` AS `debtorAgentName`,
        `transaction`.`debtorAgentAddressType` AS `debtorAgentAddressType`,
        `transaction`.`debtorAgentDepartment` AS `debtorAgentDepartment`,
        `transaction`.`debtorAgentSubDepartment` AS `debtorAgentSubDepartment`,
        `transaction`.`debtorAgentStreetName` AS `debtorAgentStreetName`,
        `transaction`.`debtorAgentBuildingNumber` AS `debtorAgentBuildingNumber`,
        `transaction`.`dedtorAgentPostCode` AS `dedtorAgentPostCode`,
        `transaction`.`debtorAgentTownName` AS `debtorAgentTownName`,
        `transaction`.`debtorAgentCountrySubDivision` AS `debtorAgentCountrySubDivision`,
        `transaction`.`debtorAgentCountry` AS `debtorAgentCountry`,
        `transaction`.`debtorAgentAddressLine` AS `debtorAgentAddressLine`,
        `transaction`.`debtorAccountSchemeName` AS `debtorAccountSchemeName`,
        `transaction`.`debtorAccountIdentification` AS `debtorAccountIdentification`,
        `transaction`.`debtorAccountName` AS `debtorAccountName`,
        `transaction`.`debtorAccountSeconIdentification` AS `debtorAccountSeconIdentification`,
        `transaction`.`cardInstrumentSchemeName` AS `cardInstrumentSchemeName`,
        `transaction`.`cardInstrumentAuthorisationType` AS `cardInstrumentAuthorisationType`,
        `transaction`.`cardInstrumentName` AS `cardInstrumentName`,
        `transaction`.`cardInstrumentIdentification` AS `cardInstrumentIdentification`,
        `transaction`.`FirstPaymentDateTime` AS `FirstPaymentDateTime`,
        `transaction`.`NextPaymentDateTime` AS `NextPaymentDateTime`,
        `transaction`.`FinalPaymentDateTime` AS `FinalPaymentDateTime`,
        `transaction`.`StandingOrderStatusCode` AS `StandingOrderStatusCode`,
        `transaction`.`FP_Amount` AS `FP_Amount`,
        `transaction`.`FP_Currency` AS `FP_Currency`,
        `transaction`.`NP_Amount` AS `NP_Amount`,
        `transaction`.`NP_Currency` AS `NP_Currency`,
        `transaction`.`FPA_Amount` AS `FPA_Amount`,
        `transaction`.`FPA_Currency` AS `FPA_Currency`,
        `transaction`.`IBAN` AS `IBAN`,
        `transaction`.`sortCode` AS `sortCode`,
        `transaction`.`beneficiaryName` AS `beneficiaryName`,
        `transaction`.`bankName` AS `bankName`,
        `transaction`.`swiftCode` AS `swiftCode`,
        `accounts`.`NickName` AS `nickName`
    FROM
        ((`accounts`
        JOIN `transaction`)
        JOIN `transactiontype`)
    WHERE
        (((`accounts`.`Account_id` = `transaction`.`fromAccountNumber`)
            OR (`accounts`.`Account_id` = `transaction`.`toAccountNumber`))
            AND (`transaction`.`Type_id` = `transactiontype`.`Id`))
    GROUP BY `transaction`.`Id`
    ORDER BY `transaction`.`createdDate` DESC;

CREATE 
     OR REPLACE VIEW `customeraccountransactionview` AS
    SELECT DISTINCT
        `customeraccounts`.`Customer_id` AS `User_id`,
        `transaction`.`Id` AS `transactionId`,
        `transaction`.`Type_id` AS `transactiontype`,
        `transaction`.`ExpenseCategory_id` AS `ExpenseCategory_id`,
        `transaction`.`billid` AS `Bill_id`,
        `transaction`.`Reference_id` AS `Reference_id`,
        `transaction`.`fromAccountNumber` AS `fromAccountNumber`,
        `transaction`.`fromAccountBalance` AS `fromAccountBalance`,
        `transaction`.`toAccountNumber` AS `toAccountNumber`,
        `transaction`.`toAccountBalance` AS `toAccountBalance`,
        `transaction`.`amount` AS `amount`,
        `transaction`.`convertedAmount` AS `convertedAmount`,
        `transaction`.`transactionCurrency` AS `transactionCurrency`,
        `transaction`.`baseCurrency` AS `baseCurrency`,
        `transaction`.`Status_id` AS `Status_id`,
        `transaction`.`statusDesc` AS `statusDesc`,
        `transaction`.`isScheduled` AS `isScheduled`,
        `transaction`.`category` AS `category`,
        `transaction`.`billCategory` AS `billCategory`,
        `transaction`.`toExternalAccountNumber` AS `ExternalAccountNumber`,
        `transaction`.`Person_Id` AS `Person_Id`,
        `transaction`.`frequencyType` AS `frequencyType`,
        `transaction`.`createdDate` AS `createdDate`,
        `transaction`.`cashlessEmail` AS `cashlessEmail`,
        `transaction`.`cashlessMode` AS `cashlessMode`,
        `transaction`.`cashlessOTP` AS `cashlessOTP`,
        `transaction`.`cashlessOTPValidDate` AS `cashlessOTPValidDate`,
        `transaction`.`cashlessPersonName` AS `cashlessPersonName`,
        `transaction`.`cashlessPhone` AS `cashlessPhone`,
        `transaction`.`cashlessSecurityCode` AS `cashlessSecurityCode`,
        `transaction`.`cashWithdrawalTransactionStatus` AS `cashWithdrawalTransactionStatus`,
        `transaction`.`frequencyEndDate` AS `frequencyEndDate`,
        `transaction`.`frequencyStartDate` AS `frequencyStartDate`,
        `transaction`.`hasDepositImage` AS `hasDepositImage`,
        `transaction`.`Payee_id` AS `payeeId`,
        `transaction`.`payeeName` AS `payeeName`,
        `transaction`.`p2pContact` AS `p2pContact`,
        `transaction`.`Person_Id` AS `personId`,
        `transaction`.`recurrenceDesc` AS `recurrenceDesc`,
        `transaction`.`numberOfRecurrences` AS `numberOfRecurrences`,
        `transaction`.`scheduledDate` AS `scheduledDate`,
        `transaction`.`transactionComments` AS `transactionComments`,
        `transaction`.`notes` AS `transactionsNotes`,
        `transaction`.`description` AS `transDescription`,
        `transaction`.`transactionDate` AS `transactionDate`,
        `transaction`.`postedDate` AS `postedDate`,
        `transaction`.`frontImage1` AS `frontImage1`,
        `transaction`.`frontImage2` AS `frontImage2`,
        `transaction`.`backImage1` AS `backImage1`,
        `transaction`.`backImage2` AS `backImage2`,
        `transaction`.`checkDesc` AS `checkDesc`,
        `transaction`.`checkNumber1` AS `checkNumber1`,
        `transaction`.`checkNumber2` AS `checkNumber2`,
        `transaction`.`checkNumber` AS `checkNumber`,
        `transaction`.`checkReason` AS `checkReason`,
        `transaction`.`requestValidity` AS `requestValidity`,
        `transaction`.`checkDateOfIssue` AS `checkDateOfIssue`,
        `transaction`.`bankName1` AS `bankName1`,
        `transaction`.`bankName2` AS `bankName2`,
        `transaction`.`withdrawlAmount1` AS `withdrawlAmount1`,
        `transaction`.`withdrawlAmount2` AS `withdrawlAmount2`,
        `transaction`.`cashAmount` AS `cashAmount`,
        `transaction`.`payeeCurrency` AS `payeeCurrency`,
        `transaction`.`fee` AS `fee`,
        `transaction`.`feePaidByReceipent` AS `feePaidByReceipent`,
        `transaction`.`feeCurrency` AS `feeCurrency`,
        `transaction`.`isDisputed` AS `isDisputed`,
        `transaction`.`disputeReason` AS `disputeReason`,
        `transaction`.`disputeDescription` AS `disputeDescription`,
        `transaction`.`disputeDate` AS `disputeDate`,
        `transaction`.`disputeStatus` AS `disputeStatus`,
        `transactiontype`.`description` AS `description`,
        `transaction`.`statementReference` AS `statementReference`,
        `transaction`.`transCreditDebitIndicator` AS `transCreditDebitIndicator`,
        `transaction`.`bookingDateTime` AS `bookingDateTime`,
        `transaction`.`valueDateTime` AS `valueDateTime`,
        `transaction`.`transactionInformation` AS `transactionInformation`,
        `transaction`.`addressLine` AS `addressLine`,
        `transaction`.`transactionAmount` AS `transactionAmount`,
        `transaction`.`chargeAmount` AS `chargeAmount`,
        `transaction`.`chargeCurrency` AS `chargeCurrency`,
        `transaction`.`sourceCurrency` AS `sourceCurrency`,
        `transaction`.`targetCurrency` AS `targetCurrency`,
        `transaction`.`unitCurrency` AS `unitCurrency`,
        `transaction`.`exchangeRate` AS `exchangeRate`,
        `transaction`.`contractIdentification` AS `contractIdentification`,
        `transaction`.`quotationDate` AS `quotationDate`,
        `transaction`.`instructedAmount` AS `instructedAmount`,
        `transaction`.`instructedCurrency` AS `instructedCurrency`,
        `transaction`.`transactionCode` AS `transactionCode`,
        `transaction`.`transactionSubCode` AS `transactionSubCode`,
        `transaction`.`proprietaryTransactionCode` AS `proprietaryTransactionCode`,
        `transaction`.`proprietaryTransactionIssuer` AS `proprietaryTransactionIssuer`,
        `transaction`.`balanceCreditDebitIndicator` AS `balanceCreditDebitIndicator`,
        `transaction`.`balanceType` AS `balanceType`,
        `transaction`.`balanceAmount` AS `balanceAmount`,
        `transaction`.`balanceCurrency` AS `balanceCurrency`,
        `transaction`.`merchantName` AS `merchantName`,
        `transaction`.`merchantCategoryCode` AS `merchantCategoryCode`,
        `transaction`.`creditorAgentSchemeName` AS `creditorAgentSchemeName`,
        `transaction`.`creditorAgentIdentification` AS `creditorAgentIdentification`,
        `transaction`.`creditorAgentName` AS `creditorAgentName`,
        `transaction`.`creditorAgentaddressType` AS `creditorAgentaddressType`,
        `transaction`.`creditorAgentDepartment` AS `creditorAgentDepartment`,
        `transaction`.`creditorAgentSubDepartment` AS `creditorAgentSubDepartment`,
        `transaction`.`creditorAgentStreetName` AS `creditorAgentStreetName`,
        `transaction`.`creditorAgentBuildingNumber` AS `creditorAgentBuildingNumber`,
        `transaction`.`creditorAgentPostCode` AS `creditorAgentPostCode`,
        `transaction`.`creditorAgentTownName` AS `creditorAgentTownName`,
        `transaction`.`creditorAgentCountrySubDivision` AS `creditorAgentCountrySubDivision`,
        `transaction`.`creditorAgentCountry` AS `creditorAgentCountry`,
        `transaction`.`creditorAgentAddressLine` AS `creditorAgentAddressLine`,
        `transaction`.`creditorAccountSchemeName` AS `creditorAccountSchemeName`,
        `transaction`.`creditorAccountIdentification` AS `creditorAccountIdentification`,
        `transaction`.`creditorAccountName` AS `creditorAccountName`,
        `transaction`.`creditorAccountSeconIdentification` AS `creditorAccountSeconIdentification`,
        `transaction`.`debtorAgentSchemeName` AS `debtorAgentSchemeName`,
        `transaction`.`debtorAgentIdentification` AS `debtorAgentIdentification`,
        `transaction`.`debtorAgentName` AS `debtorAgentName`,
        `transaction`.`debtorAgentAddressType` AS `debtorAgentAddressType`,
        `transaction`.`debtorAgentDepartment` AS `debtorAgentDepartment`,
        `transaction`.`debtorAgentSubDepartment` AS `debtorAgentSubDepartment`,
        `transaction`.`debtorAgentStreetName` AS `debtorAgentStreetName`,
        `transaction`.`debtorAgentBuildingNumber` AS `debtorAgentBuildingNumber`,
        `transaction`.`dedtorAgentPostCode` AS `dedtorAgentPostCode`,
        `transaction`.`debtorAgentTownName` AS `debtorAgentTownName`,
        `transaction`.`debtorAgentCountrySubDivision` AS `debtorAgentCountrySubDivision`,
        `transaction`.`debtorAgentCountry` AS `debtorAgentCountry`,
        `transaction`.`debtorAgentAddressLine` AS `debtorAgentAddressLine`,
        `transaction`.`debtorAccountSchemeName` AS `debtorAccountSchemeName`,
        `transaction`.`debtorAccountIdentification` AS `debtorAccountIdentification`,
        `transaction`.`debtorAccountName` AS `debtorAccountName`,
        `transaction`.`debtorAccountSeconIdentification` AS `debtorAccountSeconIdentification`,
        `transaction`.`cardInstrumentSchemeName` AS `cardInstrumentSchemeName`,
        `transaction`.`cardInstrumentAuthorisationType` AS `cardInstrumentAuthorisationType`,
        `transaction`.`cardInstrumentName` AS `cardInstrumentName`,
        `transaction`.`cardInstrumentIdentification` AS `cardInstrumentIdentification`,
        `transaction`.`FirstPaymentDateTime` AS `FirstPaymentDateTime`,
        `transaction`.`NextPaymentDateTime` AS `NextPaymentDateTime`,
        `transaction`.`FinalPaymentDateTime` AS `FinalPaymentDateTime`,
        `transaction`.`StandingOrderStatusCode` AS `StandingOrderStatusCode`,
        `transaction`.`FP_Amount` AS `FP_Amount`,
        `transaction`.`FP_Currency` AS `FP_Currency`,
        `transaction`.`NP_Amount` AS `NP_Amount`,
        `transaction`.`NP_Currency` AS `NP_Currency`,
        `transaction`.`FPA_Amount` AS `FPA_Amount`,
        `transaction`.`FPA_Currency` AS `FPA_Currency`,
        `transaction`.`IBAN` AS `IBAN`,
        `transaction`.`sortCode` AS `sortCode`,
        `transaction`.`beneficiaryName` AS `beneficiaryName`,
        `transaction`.`bankName` AS `bankName`,
        `transaction`.`swiftCode` AS `swiftCode`
    FROM
        ((`customeraccounts`
        JOIN `transaction`)
        JOIN `transactiontype`)
    WHERE
        (((`customeraccounts`.`Account_id` = `transaction`.`fromAccountNumber`)
            OR (`customeraccounts`.`Account_id` = `transaction`.`toAccountNumber`))
            AND (`customeraccounts`.`Customer_id` IS NOT NULL)
            AND (`transaction`.`Type_id` = `transactiontype`.`Id`))
    ORDER BY `transaction`.`createdDate` DESC;
    
alter table backendcertificate add column CertPublicKey longtext NOT NULL after CertValue;
 
alter table backendcertificate change  CertValue  CertPrivateKey longtext not null;

ALTER TABLE `customertermsandconditions` 
CHANGE COLUMN `channelId` `channel` VARCHAR(45) NULL DEFAULT NULL ;



ALTER TABLE `customertermsandconditions` 
ADD COLUMN `platform` VARCHAR(45) NULL DEFAULT NULL AFTER `channel`,
ADD COLUMN `browser` VARCHAR(45) NULL DEFAULT NULL AFTER `platform`;

ALTER TABLE `customertermsandconditions` 
ADD COLUMN `languageCode` VARCHAR(10) NOT NULL AFTER `termsAndConditionsCode`;


ALTER TABLE `customertermsandconditions`
ADD INDEX `IDX_CustomerId_get` (`customerId` ASC);
