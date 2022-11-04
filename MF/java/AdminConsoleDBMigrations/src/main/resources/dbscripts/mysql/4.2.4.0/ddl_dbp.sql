
ALTER TABLE `customer` 
ADD COLUMN `isEngageProvisioned` TINYINT(1) NOT NULL DEFAULT '0';

ALTER TABLE `application` 
ADD COLUMN `isFeedbackEnabled` VARCHAR(45) NULL DEFAULT '1' AFTER `defaultCountryDialCode`,
ADD COLUMN `noOfDaysForRatingFromProfile` INT(11) NULL AFTER `isFeedbackEnabled`,
ADD COLUMN `noOfDaysForRatingFromTransactions` INT(11) NULL AFTER `noOfDaysForRatingFromProfile`,
ADD COLUMN `noOfDaysForAnotherAttemptForRating` INT(11) NULL AFTER `noOfDaysForRatingFromTransactions`;

CREATE TABLE `feedbackstatus` (
  `id` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `UserName` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `feedbackID` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint(1) DEFAULT '0',
  `deviceID` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8; 

ALTER TABLE `application` 
ADD COLUMN `maxtimesFeedbackperversion` VARCHAR(45) NULL DEFAULT NULL AFTER `noOfDaysForAnotherAttemptForRating`,
ADD COLUMN `majorVersionsForFeedback` VARCHAR(45) NULL DEFAULT NULL AFTER `maxtimesFeedbackperversion`;

 ALTER TABLE `customer` 
CHANGE COLUMN `FirstName` `FirstName` VARCHAR(200) NULL DEFAULT NULL ,
CHANGE COLUMN `LastName` `LastName` VARCHAR(200) NULL DEFAULT NULL ;

ALTER TABLE `credentialchecker` 
CHANGE COLUMN `createdts` `createdts` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `customersecurityquestions` 
ADD INDEX `FK_CustomerSecurityQuestion_CustomerQuestionID_idx` (`Customer_id` ASC, `SecurityQuestion_id` ASC);

ALTER TABLE `customersecurityquestions` 
DROP INDEX `Test_idx` ,
ADD INDEX `FK_CustomerSecurityQuestion_CustomerID` (`Customer_id` ASC);

ALTER TABLE `customercommunication` 
ADD INDEX `IXFK_CustomerCommunication_Customer_Primary` (`Customer_id` ASC, `isPrimary` ASC);

ALTER TABLE `mfaservice` 
ADD INDEX `IXFK_mfaservice_servicekey_user` (`serviceKey` ASC, `serviceName` ASC, `User_id` ASC);

ALTER TABLE `OTP` 
ADD INDEX `IXFK_OTP_servicesecurity` (`securityKey` ASC, `serviceKey` ASC);


ALTER TABLE `application` 
ADD COLUMN `bannerImageURL` VARCHAR(100) NULL AFTER `majorVersionsForFeedback`,
ADD COLUMN `desktopBannerImageURL` VARCHAR(100) NULL AFTER `bannerImageURL`,
ADD COLUMN `mobileBannerImageURL` VARCHAR(100) NULL AFTER `desktopBannerImageURL`,
ADD COLUMN `viewMoreDBXLink` VARCHAR(100) NULL AFTER `mobileBannerImageURL`;

ALTER TABLE `mfaservice` 
ADD COLUMN `isVerified` VARCHAR(5) NULL DEFAULT NULL AFTER `securityQuestions`;

ALTER TABLE `otpcount` 
CHANGE COLUMN `User_id` `User_id` VARCHAR(50) NULL DEFAULT NULL ;

DROP TABLE IF EXISTS `backendidentifiers`;

ALTER TABLE `accounts` 
ADD COLUMN `UpdatedBy` VARCHAR(45) NULL DEFAULT NULL AFTER `TaxId`,
ADD COLUMN `ActualUpdatedBY` VARCHAR(45) NULL DEFAULT NULL AFTER `UpdatedBy`;

ALTER TABLE `accounts` 
CHANGE COLUMN `LastUpdated` `LastUpdated` TIMESTAMP NULL DEFAULT NULL ;

CREATE OR REPLACE 
VIEW `customeraccountsview` AS
    SELECT DISTINCT
        `membershipaccounts`.`Membership_id` AS `Membership_id`,
        `membershipaccounts`.`Taxid` AS `Taxid`,
        `customeraccounts`.`Customer_id` AS `Customer_id`,
        `customeraccounts`.`Customer_id` AS `User_id`,
        `accounts`.`Account_id` AS `Account_id`,
        `accounts`.`Type_id` AS `Type_id`,
        `accounts`.`UserName` AS `userName`,
        `accounts`.`CurrencyCode` AS `currencyCode`,
        `accounts`.`AccountHolder` AS `accountHolder`,
        `accounts`.`error` AS `error`,
        `accounts`.`Address` AS `Address`,
        `accounts`.`Scheme` AS `Scheme`,
        `accounts`.`Number` AS `number`,
        `accounts`.`AvailableBalance` AS `availableBalance`,
        `accounts`.`CurrentBalance` AS `currentBalance`,
        `accounts`.`InterestRate` AS `interestRate`,
        `accounts`.`AvailableCredit` AS `availableCredit`,
        `accounts`.`MinimumDue` AS `minimumDue`,
        `accounts`.`DueDate` AS `dueDate`,
        `accounts`.`FirstPaymentDate` AS `firstPaymentDate`,
        `accounts`.`ClosingDate` AS `closingDate`,
        `accounts`.`PaymentTerm` AS `paymentTerm`,
        `accounts`.`OpeningDate` AS `openingDate`,
        `accounts`.`MaturityDate` AS `maturityDate`,
        `accounts`.`DividendLastPaidAmount` AS `dividendLastPaidAmount`,
        `accounts`.`DividendLastPaidDate` AS `dividendLastPaidDate`,
        `accounts`.`DividendPaidYTD` AS `dividendPaidYTD`,
        `accounts`.`DividendRate` AS `dividendRate`,
        `accounts`.`DividendYTD` AS `dividendYTD`,
        `accounts`.`EStatementmentEnable` AS `eStatementEnable`,
        `customeraccounts`.`FavouriteStatus` AS `favouriteStatus`,
        `accounts`.`StatusDesc` AS `statusDesc`,
        `accounts`.`NickName` AS `nickName`,
        `accounts`.`OriginalAmount` AS `originalAmount`,
        `accounts`.`OutstandingBalance` AS `outstandingBalance`,
        `accounts`.`PaymentDue` AS `paymentDue`,
        `accounts`.`PaymentMethod` AS `paymentMethod`,
        `accounts`.`SwiftCode` AS `swiftCode`,
        `accounts`.`TotalCreditMonths` AS `totalCreditMonths`,
        `accounts`.`TotalDebitsMonth` AS `totalDebitsMonth`,
        `accounts`.`RoutingNumber` AS `routingNumber`,
        `accounts`.`SupportBillPay` AS `supportBillPay`,
        `accounts`.`SupportCardlessCash` AS `supportCardlessCash`,
        `accounts`.`SupportTransferFrom` AS `supportTransferFrom`,
        `accounts`.`SupportTransferTo` AS `supportTransferTo`,
        `accounts`.`SupportDeposit` AS `supportDeposit`,
        `accounts`.`UnpaidInterest` AS `unpaidInterest`,
        `accounts`.`PreviousYearsDividends` AS `previousYearsDividends`,
        `accounts`.`principalBalance` AS `principalBalance`,
        `accounts`.`PrincipalValue` AS `principalValue`,
        `accounts`.`RegularPaymentAmount` AS `regularPaymentAmount`,
        `accounts`.`phone` AS `phoneId`,
        `accounts`.`LastDividendPaidDate` AS `lastDividendPaidDate`,
        `accounts`.`LastDividendPaidAmount` AS `lastDividendPaidAmount`,
        `accounts`.`LastPaymentAmount` AS `lastPaymentAmount`,
        `accounts`.`LastPaymentDate` AS `lastPaymentDate`,
        `accounts`.`LastStatementBalance` AS `lastStatementBalance`,
        `accounts`.`LateFeesDue` AS `lateFeesDue`,
        `accounts`.`maturityAmount` AS `maturityAmount`,
        `accounts`.`MaturityOption` AS `maturityOption`,
        `accounts`.`payoffAmount` AS `payoffAmount`,
        `accounts`.`PayOffCharge` AS `payOffCharge`,
        `accounts`.`PendingDeposit` AS `pendingDeposit`,
        `accounts`.`PendingWithdrawal` AS `pendingWithdrawal`,
        `accounts`.`JointHolders` AS `jointHolders`,
        `accounts`.`IsPFM` AS `isPFM`,
        `accounts`.`InterestPaidYTD` AS `interestPaidYTD`,
        `accounts`.`InterestPaidPreviousYTD` AS `interestPaidPreviousYTD`,
        `accounts`.`InterestPaidLastYear` AS `interestPaidLastYear`,
        `accounts`.`InterestEarned` AS `interestEarned`,
        `accounts`.`CurrentAmountDue` AS `currentAmountDue`,
        `accounts`.`CreditLimit` AS `creditLimit`,
        `accounts`.`CreditCardNumber` AS `creditCardNumber`,
        `accounts`.`BsbNum` AS `bsbNum`,
        `accounts`.`BondInterestLastYear` AS `bondInterestLastYear`,
        `accounts`.`BondInterest` AS `bondInterest`,
        `accounts`.`AvailablePoints` AS `availablePoints`,
        `accounts`.`AccountName` AS `accountName`,
        `accounts`.`email` AS `email`,
        `accounts`.`IBAN` AS `IBAN`,
        `accounts`.`adminProductId` AS `adminProductId`,
        `accounts`.`UpdatedBy` AS `UpdatedBy`,
        `accounts`.`LastUpdated` AS `LastUpdated`,
		`accounts`.`ActualUpdatedBY` AS `ActualUpdatedBY`,
        `bank`.`Description` AS `bankname`,
        `accounts`.`AccountPreference` AS `accountPreference`,
        `accounttype`.`transactionLimit` AS `transactionLimit`,
        `accounttype`.`transferLimit` AS `transferLimit`,
        `accounttype`.`rates` AS `rates`,
        `accounttype`.`termsAndConditions` AS `termsAndConditions`,
        `accounttype`.`TypeDescription` AS `typeDescription`,
        `accounttype`.`supportChecks` AS `supportChecks`,
        `accounttype`.`displayName` AS `displayName`,
        `accounts`.`accountSubType` AS `accountSubType`,
        `accounts`.`description` AS `description`,
        `accounts`.`schemeName` AS `schemeName`,
        `accounts`.`identification` AS `identification`,
        `accounts`.`secondaryIdentification` AS `secondaryIdentification`,
        `accounts`.`servicerSchemeName` AS `servicerSchemeName`,
        `accounts`.`servicerIdentification` AS `servicerIdentification`,
        `accounts`.`dataCreditDebitIndicator` AS `dataCreditDebitIndicator`,
        `accounts`.`dataType` AS `dataType`,
        `accounts`.`dataDateTime` AS `dataDateTime`,
        `accounts`.`dataCreditLineIncluded` AS `dataCreditLineIncluded`,
        `accounts`.`dataCreditLineType` AS `dataCreditLineType`,
        `accounts`.`dataCreditLineAmount` AS `dataCreditLineAmount`,
        `accounts`.`dataCreditLineCurrency` AS `dataCreditLineCurrency`
    FROM
        ((((`accounts`
        JOIN `customeraccounts`)
        JOIN `accounttype`)
        LEFT JOIN `membershipaccounts` ON ((`accounts`.`Account_id` = `membershipaccounts`.`Account_id`)))
        LEFT JOIN `bank` ON ((`accounts`.`Bank_id` = `bank`.`id`)))
    WHERE
        ((`accounts`.`Account_id` = `customeraccounts`.`Account_id`)
            AND (`accounts`.`Type_id` = `accounttype`.`TypeID`));
						
CREATE OR REPLACE VIEW `getaccountsview` AS
    SELECT 
        `accounts`.`Account_id` AS `Account_id`,
        `accounts`.`Type_id` AS `Type_id`,
        `accounts`.`UserName` AS `userName`,
        `accounts`.`CurrencyCode` AS `currencyCode`,
        `accounts`.`AccountHolder` AS `accountHolder`,
        `accounts`.`error` AS `error`,
        `accounts`.`Address` AS `Address`,
        `accounts`.`Scheme` AS `Scheme`,
        `accounts`.`Number` AS `number`,
        `accounts`.`AvailableBalance` AS `availableBalance`,
        `accounts`.`CurrentBalance` AS `currentBalance`,
        `accounts`.`InterestRate` AS `interestRate`,
        `accounts`.`AvailableCredit` AS `availableCredit`,
        `accounts`.`MinimumDue` AS `minimumDue`,
        `accounts`.`DueDate` AS `dueDate`,
        `accounts`.`FirstPaymentDate` AS `firstPaymentDate`,
        `accounts`.`ClosingDate` AS `closingDate`,
        `accounts`.`PaymentTerm` AS `paymentTerm`,
        `accounts`.`OpeningDate` AS `openingDate`,
        `accounts`.`MaturityDate` AS `maturityDate`,
        `accounts`.`DividendLastPaidAmount` AS `dividendLastPaidAmount`,
        `accounts`.`DividendLastPaidDate` AS `dividendLastPaidDate`,
        `accounts`.`DividendPaidYTD` AS `dividendPaidYTD`,
        `accounts`.`DividendRate` AS `dividendRate`,
        `accounts`.`DividendYTD` AS `dividendYTD`,
        `accounts`.`EStatementmentEnable` AS `eStatementEnable`,
        `accounts`.`FavouriteStatus` AS `favouriteStatus`,
        `accounts`.`StatusDesc` AS `statusDesc`,
        `accounts`.`NickName` AS `nickName`,
        `accounts`.`User_id` AS `User_id`,
        `accounts`.`OriginalAmount` AS `originalAmount`,
        `accounts`.`OutstandingBalance` AS `outstandingBalance`,
        `accounts`.`PaymentDue` AS `paymentDue`,
        `accounts`.`PaymentMethod` AS `paymentMethod`,
        `accounts`.`SwiftCode` AS `swiftCode`,
        `accounts`.`TotalCreditMonths` AS `totalCreditMonths`,
        `accounts`.`TotalDebitsMonth` AS `totalDebitsMonth`,
        `accounts`.`RoutingNumber` AS `routingNumber`,
        `accounts`.`SupportBillPay` AS `supportBillPay`,
        `accounts`.`SupportCardlessCash` AS `supportCardlessCash`,
        `accounts`.`SupportTransferFrom` AS `supportTransferFrom`,
        `accounts`.`SupportTransferTo` AS `supportTransferTo`,
        `accounts`.`SupportDeposit` AS `supportDeposit`,
        `accounts`.`UnpaidInterest` AS `unpaidInterest`,
        `accounts`.`PreviousYearsDividends` AS `previousYearsDividends`,
        `accounts`.`principalBalance` AS `principalBalance`,
        `accounts`.`PrincipalValue` AS `principalValue`,
        `accounts`.`RegularPaymentAmount` AS `regularPaymentAmount`,
        `accounts`.`phone` AS `phoneId`,
        `accounts`.`LastDividendPaidDate` AS `lastDividendPaidDate`,
        `accounts`.`LastDividendPaidAmount` AS `lastDividendPaidAmount`,
        `accounts`.`LastPaymentAmount` AS `lastPaymentAmount`,
        `accounts`.`LastPaymentDate` AS `lastPaymentDate`,
        `accounts`.`LastStatementBalance` AS `lastStatementBalance`,
        `accounts`.`LateFeesDue` AS `lateFeesDue`,
        `accounts`.`maturityAmount` AS `maturityAmount`,
        `accounts`.`MaturityOption` AS `maturityOption`,
        `accounts`.`payoffAmount` AS `payoffAmount`,
        `accounts`.`PayOffCharge` AS `payOffCharge`,
        `accounts`.`PendingDeposit` AS `pendingDeposit`,
        `accounts`.`PendingWithdrawal` AS `pendingWithdrawal`,
        `accounts`.`JointHolders` AS `jointHolders`,
        `accounts`.`IsPFM` AS `isPFM`,
        `accounts`.`InterestPaidYTD` AS `interestPaidYTD`,
        `accounts`.`InterestPaidPreviousYTD` AS `interestPaidPreviousYTD`,
        `accounts`.`InterestPaidLastYear` AS `interestPaidLastYear`,
        `accounts`.`InterestEarned` AS `interestEarned`,
        `accounts`.`CurrentAmountDue` AS `currentAmountDue`,
        `accounts`.`CreditLimit` AS `creditLimit`,
        `accounts`.`CreditCardNumber` AS `creditCardNumber`,
        `accounts`.`BsbNum` AS `bsbNum`,
        `accounts`.`BondInterestLastYear` AS `bondInterestLastYear`,
        `accounts`.`BondInterest` AS `bondInterest`,
        `accounts`.`AvailablePoints` AS `availablePoints`,
        `accounts`.`AccountName` AS `accountName`,
        `accounts`.`email` AS `email`,
        `accounts`.`IBAN` AS `IBAN`,
        `accounts`.`adminProductId` AS `adminProductId`,
        `bank`.`Description` AS `bankname`,
        `accounts`.`AccountPreference` AS `accountPreference`,
        `accounttype`.`transactionLimit` AS `transactionLimit`,
        `accounttype`.`transferLimit` AS `transferLimit`,
        `accounttype`.`rates` AS `rates`,
        `accounttype`.`termsAndConditions` AS `termsAndConditions`,
        `accounttype`.`TypeDescription` AS `typeDescription`,
        `accounttype`.`supportChecks` AS `supportChecks`,
        `accounttype`.`displayName` AS `displayName`,
        `accounts`.`accountSubType` AS `accountSubType`,
        `accounts`.`description` AS `description`,
        `accounts`.`schemeName` AS `schemeName`,
        `accounts`.`identification` AS `identification`,
        `accounts`.`secondaryIdentification` AS `secondaryIdentification`,
        `accounts`.`servicerSchemeName` AS `servicerSchemeName`,
        `accounts`.`servicerIdentification` AS `servicerIdentification`,
        `accounts`.`dataCreditDebitIndicator` AS `dataCreditDebitIndicator`,
        `accounts`.`dataType` AS `dataType`,
        `accounts`.`dataDateTime` AS `dataDateTime`,
        `accounts`.`dataCreditLineIncluded` AS `dataCreditLineIncluded`,
        `accounts`.`dataCreditLineType` AS `dataCreditLineType`,
        `accounts`.`dataCreditLineAmount` AS `dataCreditLineAmount`,
        `accounts`.`dataCreditLineCurrency` AS `dataCreditLineCurrency`,
        `accounts`.`UpdatedBy` AS `UpdatedBy`,
        `accounts`.`LastUpdated` AS `LastUpdated`,
		`accounts`.`ActualUpdatedBY` AS `ActualUpdatedBY`
    FROM
        ((`accounts`
        JOIN `accounttype`)
        JOIN `bank`)
    WHERE
        ((`accounts`.`Type_id` = `accounttype`.`TypeID`)
            AND (`accounts`.`Bank_id` = `bank`.`id`));
            
ALTER TABLE `feedback` 
ADD COLUMN `likeMost` VARCHAR(100) NULL AFTER `description`,
ADD COLUMN `improvement` VARCHAR(500) NULL AFTER `likeMost`;
 
ALTER TABLE `newuser` ADD UNIQUE INDEX `username` (`userName` ASC);

ALTER TABLE `mfaservice` 
CHANGE COLUMN `User_id` `User_id` VARCHAR(50) NULL DEFAULT NULL ;

ALTER TABLE `feedback` 
CHANGE COLUMN `rating` `rating` DOUBLE NULL DEFAULT NULL ; 
