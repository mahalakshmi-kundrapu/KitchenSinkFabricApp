
ALTER TABLE `notification` RENAME TO  `adminnotification` ;

/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;

DROP TABLE IF EXISTS `OTP`;
CREATE TABLE `OTP` (
  `securityKey` varchar(50) NOT NULL,
  `Otp` varchar(7) DEFAULT NULL,
  PRIMARY KEY (`securityKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `accountcommunication`;
CREATE TABLE `accountcommunication` (
  `Type_id` varchar(50) DEFAULT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `Account_id` bigint(19) DEFAULT NULL,
  `sequence` int(11) DEFAULT NULL,
  `value` varchar(100) DEFAULT NULL,
  `extension` varchar(50) DEFAULT NULL,
  `description` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `accounts`;
CREATE TABLE `accounts` (
  `Account_id` varchar(50) NOT NULL,
  `AccountName` varchar(50) DEFAULT NULL,
  `UserName` varchar(40) DEFAULT NULL,
  `ExternalBankidentity_id` varchar(20) DEFAULT NULL,
  `CurrencyCode` varchar(50) DEFAULT NULL,
  `AvailableBalance` varchar(50) DEFAULT '0.00',
  `AccountHolder` varchar(500) DEFAULT NULL,
  `Address` varchar(45) DEFAULT NULL,
  `Scheme` varchar(5) DEFAULT NULL,
  `Number` varchar(50) DEFAULT NULL,
  `error` varchar(1) DEFAULT NULL,
  `Type_id` varchar(50) DEFAULT NULL,
  `Product_id` int(11) DEFAULT NULL,
  `Bank_id` varchar(50) DEFAULT NULL,
  `User_id` int(11) DEFAULT NULL,
  `Name` varchar(100) DEFAULT NULL,
  `Status_id` bigint(20) DEFAULT NULL,
  `StatusDesc` varchar(50) DEFAULT NULL,
  `SupportDeposit` int(11) DEFAULT '0',
  `SupportBillPay` int(11) DEFAULT '0',
  `SupportTransferFrom` int(11) DEFAULT '0',
  `SupportTransferTo` int(11) DEFAULT '0',
  `ShowTransactions` tinyint(1) DEFAULT '0',
  `CurrentBalance` decimal(10,2) DEFAULT '0.00',
  `InterestRate` decimal(10,2) DEFAULT '0.00',
  `AvailableCredit` decimal(10,2) DEFAULT '0.00',
  `MinimumDue` decimal(10,2) DEFAULT '0.00',
  `DueDate` date DEFAULT NULL,
  `PrincipalValue` decimal(10,2) DEFAULT '0.00',
  `FirstPaymentDate` date DEFAULT NULL,
  `ClosingDate` date DEFAULT NULL,
  `PaymentTerm` varchar(50) DEFAULT NULL,
  `OpeningDate` date DEFAULT NULL,
  `MaturityDate` date DEFAULT NULL,
  `TransactionLimit` decimal(10,2) DEFAULT '0.00',
  `TransferLimit` decimal(10,2) DEFAULT '0.00',
  `NickName` varchar(50) DEFAULT NULL,
  `LastStatementBalance` decimal(10,2) DEFAULT '0.00',
  `AvailablePoints` int(10) DEFAULT '0',
  `OutstandingBalance` decimal(10,2) DEFAULT '0.00',
  `CreditCardNumber` bigint(19) DEFAULT NULL,
  `IsPFM` bit(1) DEFAULT b'0',
  `SupportCardlessCash` int(11) DEFAULT '0',
  `FavouriteStatus` int(11) DEFAULT '0',
  `MaturityOption` varchar(50) DEFAULT NULL,
  `RoutingNumber` varchar(50) DEFAULT NULL,
  `SwiftCode` varchar(50) DEFAULT NULL,
  `JointHolders` varchar(500) DEFAULT NULL,
  `DividendRate` varchar(50) DEFAULT '0.00',
  `DividendYTD` varchar(50) DEFAULT '0.00',
  `LastDividendPaidAmount` varchar(50) DEFAULT '0.00',
  `LastDividendPaidDate` varchar(50) DEFAULT NULL,
  `PreviousYearDividend` varchar(50) DEFAULT '0.00',
  `BondInterest` varchar(50) DEFAULT '0.00',
  `BondInterestLastYear` varchar(50) DEFAULT '0.00',
  `TotalCreditMonths` varchar(50) DEFAULT '0',
  `TotalDebitsMonth` varchar(50) DEFAULT '0',
  `CurrentAmountDue` varchar(50) DEFAULT '0.00',
  `PaymentDue` varchar(50) DEFAULT '0.00',
  `LastPaymentDate` timestamp NULL DEFAULT NULL,
  `LastPaymentAmount` varchar(50) DEFAULT '0.00',
  `LateFeesDue` varchar(50) DEFAULT '0.00',
  `CreditLimit` varchar(50) DEFAULT '0.00',
  `InterestPaidYTD` varchar(50) DEFAULT '0.00',
  `InterestPaidPreviousYTD` varchar(50) DEFAULT '0.00',
  `UnpaidInterest` varchar(50) DEFAULT '0.00',
  `PaymentMethod` varchar(50) DEFAULT NULL,
  `RegularPaymentAmount` varchar(50) DEFAULT '0.00',
  `DividendPaidYTD` varchar(50) DEFAULT '0.00',
  `DividendLastPaidAmount` varchar(50) DEFAULT '0.00',
  `DividendLastPaidDate` varchar(50) DEFAULT NULL,
  `PreviousYearsDividends` varchar(50) DEFAULT '0.00',
  `PendingDeposit` varchar(50) DEFAULT '0.00',
  `PendingWithdrawal` varchar(50) DEFAULT '0.00',
  `InterestEarned` varchar(50) DEFAULT '0.00',
  `maturityAmount` varchar(50) DEFAULT '0.00',
  `principalBalance` varchar(50) DEFAULT '0.00',
  `OriginalAmount` varchar(50) DEFAULT '0.00',
  `payoffAmount` varchar(50) DEFAULT '0.00',
  `BsbNum` int(11) DEFAULT NULL,
  `PayOffCharge` varchar(50) DEFAULT '0.00',
  `InterestPaidLastYear` varchar(50) DEFAULT '0.00',
  `EStatementmentEnable` bit(1) DEFAULT b'0',
  `Phone_id` int(11) DEFAULT NULL,
  `LastUpdated` varchar(14) DEFAULT NULL,
  `BankName` varchar(50) DEFAULT NULL,
  `AccountPreference` varchar(45) DEFAULT NULL,
  `InternalAccount` varchar(1) DEFAULT NULL,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  `product` longtext,
  `email` varchar(150) DEFAULT NULL,
  `jointAccountHolder1` varchar(50) DEFAULT NULL,
  `jointAccountHolder2` varchar(50) DEFAULT NULL,
  `bankAddress` varchar(50) DEFAULT NULL,
  `intermediaryBankName` varchar(50) DEFAULT NULL,
  `intermediaryBankAddress` varchar(50) DEFAULT NULL,
  `intermediaryBankSwiftCode` varchar(50) DEFAULT NULL,
  `phone` varchar(50) DEFAULT NULL,
  `accountSubType` varchar(45) DEFAULT NULL,
  `description` varchar(45) DEFAULT NULL,
  `schemeName` varchar(40) DEFAULT NULL,
  `identification` varchar(256) DEFAULT NULL,
  `secondaryIdentification` varchar(34) DEFAULT NULL,
  `servicerSchemeName` varchar(45) DEFAULT NULL,
  `servicerIdentification` varchar(45) DEFAULT NULL,
  `dataCreditDebitIndicator` varchar(45) DEFAULT NULL,
  `dataType` varchar(45) DEFAULT NULL,
  `dataDateTime` datetime DEFAULT NULL,
  `dataCreditLineIncluded` varchar(45) DEFAULT NULL,
  `dataCreditLineType` varchar(45) DEFAULT NULL,
  `dataCreditLineAmount` varchar(45) DEFAULT NULL,
  `dataCreditLineCurrency` varchar(45) DEFAULT NULL,
  `IBAN` varchar(45) DEFAULT NULL,
  `adminProductId` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`Account_id`),
  KEY `id_idx` (`ExternalBankidentity_id`),
  KEY `TYPE_ID` (`Type_id`) COMMENT 'Type Id index',
  CONSTRAINT `External_id` FOREIGN KEY (`ExternalBankidentity_id`) REFERENCES `externalbankidentity` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `accountstatement`;
CREATE TABLE `accountstatement` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `description` varchar(100) DEFAULT NULL,
  `statementLink` varchar(100) DEFAULT NULL,
  `Account_id` varchar(50) NOT NULL,
  `month` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`Id`),
  KEY `Account_id` (`Account_id`),
  CONSTRAINT `FK_AccountStatement_Account` FOREIGN KEY (`Account_id`) REFERENCES `accounts` (`Account_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1306 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `accounttype`;
CREATE TABLE `accounttype` (
  `TypeID` varchar(50) NOT NULL,
  `TypeDescription` varchar(100) DEFAULT NULL,
  `displayName` varchar(100) DEFAULT NULL,
  `transactionLimit` decimal(10,2) DEFAULT NULL,
  `transferLimit` decimal(10,2) DEFAULT NULL,
  `dailyDepositLimit` decimal(10,2) DEFAULT NULL,
  `monthlyDepositLimit` decimal(10,2) DEFAULT NULL,
  `termsAndConditions` varchar(2000) DEFAULT NULL,
  `features` varchar(2000) DEFAULT NULL,
  `rates` varchar(2000) DEFAULT NULL,
  `info` varchar(2000) DEFAULT NULL,
  `supportChecks` int(11) DEFAULT '0',
  `countryCode` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`TypeID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `addetails`;
CREATE TABLE `addetails` (
  `id` bigint(20) NOT NULL,
  `action1` varchar(100) DEFAULT NULL,
  `action2` varchar(100) DEFAULT NULL,
  `imageURL` varchar(500) DEFAULT NULL,
  `description` varchar(100) DEFAULT NULL,
  `adType` varchar(45) DEFAULT NULL,
  `title` varchar(45) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `actionType` varchar(50) DEFAULT NULL,
  `imageURL2` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_e1892f2c8f5c41d7bdbc2e4c13f` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `advertisements`;
CREATE TABLE `advertisements` (
  `id` bigint(20) NOT NULL,
  `actionType` varchar(45) DEFAULT NULL,
  `action` varchar(45) DEFAULT NULL,
  `adimagesrc` varchar(45) DEFAULT NULL,
  `url` varchar(45) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_efb0964233154f50836bd4d7e6e` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
DROP TABLE IF EXISTS `annualpercentagerate`;
CREATE TABLE `annualpercentagerate` (
  `id` varchar(50) NOT NULL,
  `LoanType_id` varchar(50) DEFAULT NULL,
  `APRValue` varchar(50) NOT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT NULL,
  `lastmodifiedts` timestamp NULL DEFAULT NULL,
  `synctimestamp` timestamp NULL DEFAULT NULL,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `IXFK_AnnualPercentageRate_LoanType` (`LoanType_id`),
  CONSTRAINT `FK_AnnualPercentageRate_LoanType` FOREIGN KEY (`LoanType_id`) REFERENCES `loantype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `application`;
CREATE TABLE `application` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `OSType` varchar(100) DEFAULT NULL,
  `OSversion` decimal(10,2) DEFAULT NULL,
  `BannerURL` varchar(100) DEFAULT NULL,
  `VersionLink` varchar(100) DEFAULT NULL,
  `currencyCode` varchar(10) DEFAULT NULL,
  `BusinessDays` int(11) DEFAULT NULL,
  `BankName` varchar(45) DEFAULT NULL,
  `DistanceUnit` enum('MILES','KILOMETERS') DEFAULT NULL,
  `ocrApiKey` varchar(100) DEFAULT NULL,
  `ocrSecretKey` varchar(100) DEFAULT NULL,
  `facialLicenseString` varchar(100) DEFAULT NULL,
  `facialLicenseServerUrl` varchar(100) DEFAULT NULL,
  `appStoreLink` varchar(200) DEFAULT NULL,
  `playStoreLink` varchar(200) DEFAULT NULL,
  `ipadNativeAppLink` varchar(200) DEFAULT NULL,
  `androidTabletNativeAppLink` varchar(200) DEFAULT NULL,
  `isLanguageSelectionEnabled` bit(1) DEFAULT b'0',
  `isBackEndCurencySymbolEnabled` bit(1) DEFAULT b'0',
  `isCountryCodeEnabled` bit(1) DEFAULT b'0',
  `isSortCodeVisible` bit(1) DEFAULT b'0',
  `currenciesSupported` varchar(1000) DEFAULT NULL,
  `deploymentGeography` varchar(45) DEFAULT NULL,
  `isUTCDateFormattingEnabled` bit(1) DEFAULT b'0',
  `language` varchar(16) DEFAULT NULL,
  `defaultAccountType` varchar(45) DEFAULT NULL,
  `fundingAmount` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `appointment`;
CREATE TABLE `appointment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `appointmentTime` varchar(50) DEFAULT NULL,
  `appointmentWith` varchar(50) DEFAULT NULL,
  `dob` varchar(50) DEFAULT NULL,
  `email` varchar(50) DEFAULT NULL,
  `firstName` varchar(50) DEFAULT NULL,
  `lastName` varchar(50) DEFAULT NULL,
  `phone` varchar(50) DEFAULT NULL,
  `uid` varchar(50) DEFAULT NULL,
  `branch_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_58c39e1eafd848b0a294dcc78ed` (`branch_id`),
  KEY `FK_a0ee7493e7084abc84537172e54` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `attribute`;
CREATE TABLE `attribute` (
  `id` varchar(50) NOT NULL,
  `AttributeType_id` varchar(50) DEFAULT NULL,
  `DataType_id` varchar(50) DEFAULT NULL,
  `label` varchar(100) DEFAULT NULL,
  `value` text,
  PRIMARY KEY (`id`),
  KEY `FK_Attribute_AttributeType` (`AttributeType_id`),
  KEY `FK_Attribute_DataType` (`DataType_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `attributetype`;
CREATE TABLE `attributetype` (
  `id` varchar(50) NOT NULL,
  `description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `backendidentifiers`;
CREATE TABLE `backendidentifiers` (
  `id` varchar(50) NOT NULL,
  `identifierType` varchar(100) DEFAULT NULL,
  `URL` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='backEndIdentifiers';

DROP TABLE IF EXISTS `bank`;
CREATE TABLE `bank` (
  `id` varchar(50) NOT NULL,
  `Description` varchar(100) DEFAULT NULL,
  `Oauth2` tinyint(1) NOT NULL DEFAULT '0',
  `IdentityProvider` varchar(60) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `bankbranch`;
CREATE TABLE `bankbranch` (
  `id` int(11) NOT NULL,
  `address1` varchar(50) DEFAULT NULL,
  `address2` varchar(50) DEFAULT NULL,
  `city` varchar(50) DEFAULT NULL,
  `state` varchar(50) DEFAULT NULL,
  `zipCode` int(11) DEFAULT NULL,
  `phone` varchar(50) DEFAULT NULL,
  `workingHours` varchar(500) DEFAULT NULL,
  `services` varchar(500) DEFAULT NULL,
  `latitude` varchar(100) DEFAULT NULL,
  `longitude` varchar(50) DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  `email` varchar(50) DEFAULT NULL,
  `Type_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_branchtype_id_idx` (`Type_id`),
  CONSTRAINT `FK_branchtype_id` FOREIGN KEY (`Type_id`) REFERENCES `branchtype` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `bankcommunication`;
CREATE TABLE `bankcommunication` (
  `Type_id` varchar(50) NOT NULL,
  `Bank_id` varchar(50) DEFAULT NULL,
  `sequence` int(11) NOT NULL,
  `value` varchar(100) DEFAULT NULL,
  `extension` varchar(50) DEFAULT NULL,
  `description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`Type_id`,`sequence`),
  KEY `Bank_id` (`Bank_id`),
  KEY `Type_id` (`Type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `Category_id` varchar(50) DEFAULT NULL,
  `Type_id` varchar(50) DEFAULT NULL,
  `description` text,
  `bannerImage` text,
  `destinationURL` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `bill`;
CREATE TABLE `bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `Payee_id` int(11) DEFAULT NULL,
  `Account_id` bigint(19) DEFAULT NULL,
  `billDueDate` date DEFAULT NULL,
  `paidDate` date DEFAULT NULL,
  `description` varchar(50) DEFAULT NULL,
  `dueAmount` decimal(10,2) DEFAULT '0.00',
  `paidAmount` decimal(10,2) DEFAULT '0.00',
  `balanceAmount` decimal(10,2) DEFAULT '0.00',
  `minimumDue` decimal(10,2) DEFAULT '0.00',
  `ebillURL` text,
  `Status_id` varchar(50) DEFAULT NULL,
  `statusDesc` varchar(50) DEFAULT NULL,
  `billerMaster_id` int(11) DEFAULT NULL,
  `billGeneratedDate` date DEFAULT NULL,
  `currencyCode` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1176 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `billercategory`;
CREATE TABLE `billercategory` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `categoryName` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `billercompany`;
CREATE TABLE `billercompany` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `companyName` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `billermaster`;
CREATE TABLE `billermaster` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `billerName` varchar(100) DEFAULT NULL,
  `accountNumber` varchar(50) DEFAULT NULL,
  `zipCode` varchar(50) DEFAULT NULL,
  `mobileNumber` varchar(50) DEFAULT NULL,
  `phoneNumber` varchar(50) DEFAULT NULL,
  `address` varchar(100) DEFAULT NULL,
  `relationshipNumber` varchar(50) DEFAULT NULL,
  `policyNumber` varchar(100) DEFAULT NULL,
  `city` varchar(45) DEFAULT NULL,
  `state` varchar(45) DEFAULT NULL,
  `billerCategoryId` int(11) DEFAULT NULL,
  `ebillSupport` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `billerCategoryId` (`billerCategoryId`),
  KEY `FK_88a3436955754dbb9bdf148a064` (`billerCategoryId`),
  CONSTRAINT `FK_billermaster_billercategory` FOREIGN KEY (`billerCategoryId`) REFERENCES `billercategory` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `branchtype`;
CREATE TABLE `branchtype` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `Type` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `budget`;
CREATE TABLE `budget` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ExpenseCategory_id` int(11) DEFAULT NULL,
  `description` varchar(50) DEFAULT NULL,
  `totalBudget` decimal(10,2) DEFAULT '0.00',
  `usedBudget` decimal(10,2) DEFAULT '0.00',
  PRIMARY KEY (`id`),
  KEY `ExpenseCategory_id` (`ExpenseCategory_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `card`;
CREATE TABLE `card` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `card_Status` enum('Active','Inactive','Cancelled','Reported Lost','Replaced','Cancel Request Sent','Replace Request Sent','Locked') NOT NULL,
  `User_id` int(11) NOT NULL,
  `expirationDate` date NOT NULL,
  `pinNumber` varchar(10) NOT NULL,
  `reason` varchar(100) DEFAULT NULL,
  `cardNumber` bigint(16) NOT NULL,
  `cardType` enum('Credit','Debit') DEFAULT NULL,
  `action` enum('Activate','Deactivate','Cancel','Report Lost','Replace','PinChange','Cancel Request','Replace Request','Lock') DEFAULT NULL,
  `account_id` varchar(50) DEFAULT NULL,
  `creditLimit` varchar(50) DEFAULT '0.00',
  `availableCredit` varchar(50) DEFAULT '0.00',
  `serviceProvider` varchar(50) DEFAULT NULL,
  `billingAddress` varchar(50) DEFAULT NULL,
  `cardProductName` varchar(50) DEFAULT NULL,
  `secondaryCardHolder` varchar(50) DEFAULT NULL,
  `withdrawlLimit` varchar(50) DEFAULT '0.00',
  `isInternational` bit(1) DEFAULT b'0',
  `bankName` varchar(100) DEFAULT NULL,
  `cardHolderName` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`Id`),
  KEY `CardNumber` (`cardNumber`)
) ENGINE=InnoDB AUTO_INCREMENT=265 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `check`;
CREATE TABLE `check` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `frontImage` varchar(500) DEFAULT NULL,
  `backImage` varchar(500) DEFAULT NULL,
  `transactionId` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_CHECK_TRANSACTION` (`transactionId`),
  CONSTRAINT `FK_CHECK_TRANSACTION` FOREIGN KEY (`transactionId`) REFERENCES `transaction` (`Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `checkorder`;
CREATE TABLE `checkorder` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `account_id` bigint(19) NOT NULL,
  `orderTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `accountName` varchar(50) DEFAULT NULL,
  `accountNickName` varchar(50) DEFAULT NULL,
  `leafCount` int(10) DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `postBoxNumber` varchar(50) DEFAULT NULL,
  `state` varchar(50) DEFAULT NULL,
  `country` varchar(50) DEFAULT NULL,
  `zipCode` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1_Account_Check` (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
DROP TABLE IF EXISTS `configurationmasters`;
CREATE TABLE `configurationmasters` (
  `bundle_id` varchar(255) NOT NULL,
  `app_id` varchar(255) DEFAULT NULL,
  `channels` varchar(255) DEFAULT NULL,
  `user_id` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `device_id` varchar(255) DEFAULT NULL,
  `app_version` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`bundle_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `coremembership`;
CREATE TABLE `coremembership` (
  `id` varchar(50) NOT NULL,
  `Customer_id` varchar(50) DEFAULT NULL,
  `MemberId` varchar(50) DEFAULT NULL,
  `MemberType` varchar(50) DEFAULT NULL,
  `IDType_id` varchar(50) DEFAULT NULL,
  `IDValue` varchar(50) DEFAULT NULL,
  `createdts` varchar(50) DEFAULT NULL,
  `lastmodifiedts` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `currency`;
CREATE TABLE `currency` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `currencyCode` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
DROP TABLE IF EXISTS `customeraccounts`;
CREATE TABLE `customeraccounts` (
  `id` varchar(50) NOT NULL,
  `Customer_id` varchar(50) DEFAULT NULL,
  `MemberId` varchar(50) DEFAULT NULL,
  `Account_id` varchar(50) DEFAULT NULL,
  `AccountName` varchar(50) DEFAULT NULL,
  `IsViewAllowed` tinyint(1) NOT NULL DEFAULT '0',
  `IsDepositAllowed` tinyint(1) NOT NULL DEFAULT '0',
  `IsWithdrawAllowed` tinyint(1) NOT NULL DEFAULT '0',
  `IsOrganizationAccount` tinyint(1) NOT NULL DEFAULT '0',
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT NULL,
  `lastmodifiedts` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `customerpreviouspasswords`;
CREATE TABLE `customerpreviouspasswords` (
  `id` varchar(50) NOT NULL,
  `Customer_id` varchar(50) DEFAULT NULL,
  `PwdSequence` int(11) DEFAULT NULL,
  `Password` varchar(50) NOT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `createdts` varchar(50) DEFAULT NULL,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `customersecurityquestion`;
CREATE TABLE `customersecurityquestion` (
  `id` varchar(50) NOT NULL,
  `Customer_id` varchar(50) DEFAULT NULL,
  `Question_id` varchar(50) DEFAULT NULL,
  `Answer` varchar(100) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT NULL,
  `lastmodifiedts` timestamp NULL DEFAULT NULL,
  `synctimestamp` timestamp NULL DEFAULT NULL,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `customertype`;
CREATE TABLE `customertype` (
  `id` varchar(50) NOT NULL,
  `Description` varchar(100) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT NULL,
  `lastmodifiedts` timestamp NULL DEFAULT NULL,
  `synctimestamp` timestamp NULL DEFAULT NULL,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `dbpconfig`;
CREATE TABLE `dbpconfig` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `Module` varchar(50) DEFAULT NULL,
  `FieldName` varchar(50) DEFAULT NULL,
  `FieldValue` varchar(200) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `decisionresult`;
CREATE TABLE `decisionresult` (
  `id` varchar(50) NOT NULL,
  `Decision_id` varchar(50) NOT NULL,
  `BaseAttributeName` varchar(50) DEFAULT NULL,
  `BaseAttributeValue` varchar(50) DEFAULT NULL,
  `ResultAttributeName` varchar(50) DEFAULT NULL,
  `ResultAttributeValue` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT NULL,
  `lastmodifiedts` timestamp NULL DEFAULT NULL,
  `synctimestamp` timestamp NULL DEFAULT NULL,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `IXFK_DecisionResult_Decision` (`Decision_id`),
  CONSTRAINT `FK_DecisionResult_Decision` FOREIGN KEY (`Decision_id`) REFERENCES `decision` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `deviceregistration`;
CREATE TABLE `deviceregistration` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `User_id` int(11) DEFAULT NULL,
  `Status_id` varchar(50) DEFAULT NULL,
  `DeviceId` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_index` (`User_id`,`DeviceId`),
  KEY `User_id` (`User_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1256 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `digitalprofile`;
CREATE TABLE `digitalprofile` (
  `Id` varchar(50) NOT NULL,
  `Customer_id` varchar(50) DEFAULT NULL,
  `CreditScore` int(11) DEFAULT NULL,
  `NumberOfInquiries_6M` int(11) DEFAULT NULL,
  `NumberOfInquiries_12M` int(11) DEFAULT NULL,
  `NumberOfInquiries_24M` int(11) DEFAULT NULL,
  `TotalRevolvingOpenToBuyBalance` decimal(10,2) DEFAULT NULL,
  `UtilizationPercentOfRevolvingTrades` varchar(50) DEFAULT NULL,
  `SinceRecentDelinquency_M` int(11) DEFAULT NULL,
  `TotalNumberOfDerogatory` int(11) DEFAULT NULL,
  `SinceRecentlyFiledCollection_M` int(11) DEFAULT NULL,
  `TotalNumberOfTrades` int(11) DEFAULT NULL,
  `TotalNumberOfActiveTrades` int(11) DEFAULT NULL,
  `NumberOfTradesOpened_24M` int(11) DEFAULT NULL,
  `NumberOfTradeswithUtilization` int(11) DEFAULT NULL,
  `OldestOpenPersonalFinanceTrade_M` int(11) DEFAULT NULL,
  `LoanToIncomeRatio` decimal(10,2) DEFAULT NULL,
  `NumberOfLoanAapplications_24M` varchar(50) DEFAULT NULL,
  `DebtToIncomeRatio` decimal(10,2) DEFAULT NULL,
  `PrequalifyScore` int(11) DEFAULT NULL,
  `YearsOfMembership` int(11) DEFAULT NULL,
  `AccountsBalance` decimal(10,2) DEFAULT NULL,
  `Age` varchar(50) DEFAULT NULL,
  `City` varchar(50) DEFAULT NULL,
  `State` varchar(50) DEFAULT NULL,
  `ZipCode` int(11) DEFAULT NULL,
  `DurationOfStay` decimal(10,2) DEFAULT NULL,
  `HomeOwnership` varchar(50) DEFAULT NULL,
  `GrossMonthlyIncome` decimal(10,2) DEFAULT NULL,
  `AnnualIncome` decimal(12,2) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT NULL,
  `lastmodifiedts` timestamp NULL DEFAULT NULL,
  `synctimestamp` timestamp NULL DEFAULT NULL,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `dmaddinteractions`;
CREATE TABLE `dmaddinteractions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `navigationType` varchar(50) NOT NULL,
  `navigationURL` varchar(200) NOT NULL,
  `navigationId` varchar(200) NOT NULL,
  `text` varchar(200) NOT NULL,
  `colour` varchar(200) NOT NULL,
  `dm_add_id` int(11) NOT NULL,
  `textcolor` varchar(50) DEFAULT NULL,
  `buttonType` varchar(200) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `FK_dmaddinteractions_dmadvertisements` (`dm_add_id`),
  CONSTRAINT `FK_dmaddinteractions_dmadvertisements` FOREIGN KEY (`dm_add_id`) REFERENCES `dmadvertisements` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `dmadvertisements`;
CREATE TABLE `dmadvertisements` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `description` varchar(1000) DEFAULT NULL,
  `imageURL` varchar(200) DEFAULT NULL,
  `adType` varchar(50) DEFAULT NULL,
  `navigationType` varchar(50) DEFAULT NULL,
  `navigationURL` varchar(200) DEFAULT NULL,
  `visible` bit(1) DEFAULT NULL,
  `model` varchar(50) DEFAULT NULL,
  `flowPosition` varchar(100) DEFAULT NULL,
  `adTitle` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `emailtemplates`;
CREATE TABLE `emailtemplates` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `TemplateName` varchar(100) DEFAULT NULL,
  `TemplateText` text,
  `Subject` varchar(500) DEFAULT NULL,
  `SenderName` varchar(500) DEFAULT NULL,
  `SenderEmail` varchar(500) DEFAULT NULL,
  `AlertChannel` varchar(50) DEFAULT NULL,
  `AlertLanguageCode` varchar(50) DEFAULT NULL,
  `Alert_id` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `employementdetails`;
CREATE TABLE `employementdetails` (
  `id` varchar(50) NOT NULL,
  `Customer_id` varchar(50) DEFAULT NULL,
  `EmploymentType` varchar(50) DEFAULT NULL,
  `CurrentEmployer` varchar(50) DEFAULT NULL,
  `Designation` varchar(50) DEFAULT NULL,
  `PayPeriod` varchar(50) DEFAULT NULL,
  `GrossIncome` decimal(10,2) DEFAULT NULL,
  `WeekWorkingHours` varchar(50) DEFAULT NULL,
  `EmploymentStartDate` date DEFAULT NULL,
  `PreviousEmployer` varchar(50) DEFAULT NULL,
  `PreviousDesignation` varchar(50) DEFAULT NULL,
  `OtherEmployementType` varchar(50) DEFAULT NULL,
  `OtherEmployementDescription` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT NULL,
  `lastmodifiedts` timestamp NULL DEFAULT NULL,
  `synctimestamp` timestamp NULL DEFAULT NULL,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `errorstatuscode`;
CREATE TABLE `errorstatuscode` (
  `SNo` varchar(10) DEFAULT NULL,
  `Opstatus` varchar(50) DEFAULT NULL,
  `HttpStatusCode` varchar(50) DEFAULT NULL,
  `ErrorMsg` varchar(200) DEFAULT NULL,
  `Remarks` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `exchangerates`;
CREATE TABLE `exchangerates` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `currency` varchar(50) DEFAULT NULL,
  `toCurrency` varchar(50) DEFAULT NULL,
  `currencyType` varchar(50) DEFAULT NULL,
  `exchangeRate` decimal(10,4) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `expensecategory`;
CREATE TABLE `expensecategory` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `description` varchar(50) DEFAULT NULL,
  `isUndefined` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `expenseperiod`;
CREATE TABLE `expenseperiod` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `description` varchar(50) DEFAULT NULL,
  `startDate` date DEFAULT NULL,
  `endDate` date DEFAULT NULL,
  `amount` decimal(20,2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `externalaccount`;
CREATE TABLE `externalaccount` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `User_id` int(11) DEFAULT NULL,
  `Bank_id` varchar(50) DEFAULT NULL,
  `nickName` varchar(20) DEFAULT NULL,
  `firstName` varchar(100) DEFAULT NULL,
  `lastName` varchar(100) DEFAULT NULL,
  `routingNumber` varchar(30) DEFAULT NULL,
  `accountNumber` varchar(45) DEFAULT NULL,
  `accountType` varchar(45) DEFAULT NULL,
  `notes` varchar(100) DEFAULT NULL,
  `countryName` varchar(100) DEFAULT NULL,
  `swiftCode` varchar(45) DEFAULT NULL,
  `user_Account` varchar(100) DEFAULT NULL,
  `beneficiaryName` varchar(100) DEFAULT NULL,
  `isInternationalAccount` bit(1) DEFAULT NULL,
  `bankName` varchar(50) DEFAULT NULL,
  `isSameBankAccount` bit(1) DEFAULT b'1',
  `softDelete` bit(1) NOT NULL DEFAULT b'0',
  `isVerified` bit(1) DEFAULT NULL,
  `createdOn` date DEFAULT NULL,
  `externalaccount` tinyblob,
  `IBAN` varchar(45) DEFAULT NULL,
  `sortCode` varchar(45) DEFAULT NULL,
  `phoneCountryCode` varchar(10) DEFAULT NULL,
  `phoneNumber` varchar(15) DEFAULT NULL,
  `phoneExtension` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`Id`),
  UNIQUE KEY `user_AccountNo_IBAN_Unique` (`User_id`,`accountNumber`,`IBAN`),
  KEY `User_Id_External_idx` (`User_id`),
  KEY `External_AccountNumber` (`accountNumber`),
  CONSTRAINT `User_Id_External` FOREIGN KEY (`User_id`) REFERENCES `user` (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=1574 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `externalbank`;
CREATE TABLE `externalbank` (
  `id` varchar(50) NOT NULL,
  `BankId` varchar(50) DEFAULT NULL,
  `Scheme` varchar(5) DEFAULT NULL,
  `Address` varchar(45) DEFAULT NULL,
  `BankName` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp(6) NULL DEFAULT NULL,
  `lastmodifiedts` timestamp(6) NULL DEFAULT NULL,
  `synctimestamp` timestamp(6) NULL DEFAULT NULL,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  `IdentityProvider` varchar(60) DEFAULT NULL,
  `Oauth2` tinyint(1) DEFAULT NULL,
  `logo` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `externalbankidentity`;
CREATE TABLE `externalbankidentity` (
  `id` varchar(50) NOT NULL,
  `ExternalBank_id` varchar(50) DEFAULT NULL,
  `MainUser_id` varchar(50) NOT NULL,
  `User_id` varchar(50) DEFAULT NULL,
  `Password` varchar(50) DEFAULT NULL,
  `SessionToken` varchar(200) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp(6) NULL DEFAULT NULL,
  `softdeleteflag` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `Bank_id_idx` (`ExternalBank_id`),
  CONSTRAINT `FK_Bankid` FOREIGN KEY (`ExternalBank_id`) REFERENCES `externalbank` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `featurepreferences`;
CREATE TABLE `featurepreferences` (
  `id` varchar(50) DEFAULT NULL,
  `FeatureName` varchar(50) DEFAULT NULL,
  `IsAddAllowed` varchar(50) DEFAULT NULL,
  `IsEditAllowed` varchar(50) DEFAULT NULL,
  `IsDeleteAllowed` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` varchar(50) DEFAULT NULL,
  `lastmodifiedts` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `featureservices`;
CREATE TABLE `featureservices` (
  `id` varchar(50) DEFAULT NULL,
  `FeaturePrefernce_id` varchar(50) DEFAULT NULL,
  `Service_id` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL DEFAULT '0',
  `rating` int(11) DEFAULT NULL,
  `featureRequest` varchar(1000) DEFAULT NULL,
  `description` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `iban`;
CREATE TABLE `iban` (
  `id` int(11) NOT NULL,
  `IBAN` varchar(45) DEFAULT NULL,
  `bankName` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `idmconfiguration`;
CREATE TABLE `idmconfiguration` (
  `id` varchar(10) NOT NULL,
  `IDMKey` varchar(45) DEFAULT NULL,
  `IDMValue` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `informationcontent`;
CREATE TABLE `informationcontent` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `informationType` varchar(50) DEFAULT NULL,
  `informationContent` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `interestrates`;
CREATE TABLE `interestrates` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cdterm` varchar(50) DEFAULT NULL,
  `apy` varchar(50) DEFAULT NULL,
  `minimumDeposit` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;
DROP TABLE IF EXISTS `locationlanguage`;
CREATE TABLE `locationlanguage` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `Location_id` int(11) NOT NULL,
  `description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`,`Location_id`),
  KEY `Location_id` (`Location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `lockobjects`;
CREATE TABLE `lockobjects` (
  `ObjectId` varchar(45) NOT NULL DEFAULT '',
  `User` varchar(15) NOT NULL DEFAULT '',
  `ExternalId` varchar(45) NOT NULL DEFAULT '',
  `ObjectName` varchar(45) DEFAULT NULL,
  `Mode` varchar(45) DEFAULT NULL,
  `Locked` varchar(1) DEFAULT NULL,
  `currenttimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ObjectId`,`User`,`ExternalId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='LockObjtects\n';

DROP TABLE IF EXISTS `membereligibility`;
CREATE TABLE `membereligibility` (
  `id` varchar(50) NOT NULL,
  `ConditionName` varchar(50) NOT NULL,
  `ConditionValues` varchar(200) DEFAULT NULL,
  `ConditionLabel` varchar(200) DEFAULT NULL,
  `AdditionalConsideration` varchar(100) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT NULL,
  `lastmodifiedts` timestamp NULL DEFAULT NULL,
  `synctimestamp` timestamp NULL DEFAULT NULL,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `membership`;
CREATE TABLE `membership` (
  `id` int(11) NOT NULL,
  `Status_id` varchar(45) NOT NULL,
  `createdby` varchar(45) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `membershipowner`;
CREATE TABLE `membershipowner` (
  `id` varchar(50) NOT NULL,
  `Customer_id` varchar(50) DEFAULT NULL,
  `Is_Primary` varchar(45) DEFAULT NULL,
  `Membership_id` varchar(50) NOT NULL,
  `Membership_Type` varchar(50) NOT NULL,
  `IDType_id` varchar(50) DEFAULT NULL,
  `IDValue` varchar(50) NOT NULL,
  `createdts` varchar(50) DEFAULT NULL,
  `lastmodifiedts` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `Account_id` bigint(19) DEFAULT NULL,
  `Category_id` int(11) DEFAULT NULL,
  `Subcategory_id` int(11) DEFAULT NULL,
  `subject` varchar(100) DEFAULT NULL,
  `message` varchar(512) DEFAULT NULL,
  `sentDate` timestamp NULL DEFAULT NULL,
  `status` enum('Inbox','Drafts','Sent','Deleted') DEFAULT NULL,
  `isSoftDeleted` tinyint(1) DEFAULT '0',
  `isRead` tinyint(1) DEFAULT '0',
  `createdDate` timestamp NULL DEFAULT NULL,
  `receivedDate` timestamp NULL DEFAULT NULL,
  `softdeletedDate` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=402 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `messagecategory`;
CREATE TABLE `messagecategory` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `category` varchar(45) NOT NULL,
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `messagesubcategory`;
CREATE TABLE `messagesubcategory` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `subcategory` varchar(45) NOT NULL,
  `Category_id` int(11) NOT NULL,
  PRIMARY KEY (`Id`),
  KEY `FK_Subcategory_Category_idx` (`Category_id`),
  CONSTRAINT `FK_Subcategory_Category` FOREIGN KEY (`Category_id`) REFERENCES `messagecategory` (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `messagetype`;
CREATE TABLE `messagetype` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `Description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `module`;
CREATE TABLE `module` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `newuser`;
CREATE TABLE `newuser` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `userName` varchar(50) DEFAULT NULL,
  `passWord` varchar(50) DEFAULT NULL,
  `role` varchar(50) DEFAULT NULL,
  `email` varchar(50) DEFAULT NULL,
  `phone` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=87 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `notificationId` int(11) NOT NULL AUTO_INCREMENT,
  `notificationModule` varchar(100) DEFAULT NULL,
  `notificationSubModule` varchar(100) DEFAULT NULL,
  `notificationSubject` varchar(1000) DEFAULT NULL,
  `notificationText` varchar(10000) DEFAULT NULL,
  `notificationActionLink` varchar(500) DEFAULT NULL,
  `imageURL` varchar(200) DEFAULT NULL,
  `isRead` varchar(100) DEFAULT NULL,
  `receivedDate` datetime DEFAULT NULL,
  `user` tinyblob,
  PRIMARY KEY (`notificationId`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8;
DROP TABLE IF EXISTS `numberrange`;
CREATE TABLE `numberrange` (
  `ObjectId` varchar(45) NOT NULL DEFAULT '',
  `Length` int(11) DEFAULT NULL,
  `BankId` varchar(50) DEFAULT NULL,
  `ObjectName` varchar(45) DEFAULT NULL,
  `CurrentValue` int(11) DEFAULT NULL,
  `StartValue` int(11) DEFAULT NULL,
  `EndValue` int(11) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ObjectId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='NumberRange\n';

DROP TABLE IF EXISTS `operatinghours`;
CREATE TABLE `operatinghours` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `Location_id` int(11) NOT NULL,
  `description` varchar(100) DEFAULT NULL,
  `operatingDay` varchar(50) DEFAULT NULL,
  `startHour` varchar(10) DEFAULT NULL,
  `endHour` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`,`Location_id`),
  KEY `Location_id` (`Location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `organisation`;
CREATE TABLE `organisation` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `Type_Id` varchar(50) DEFAULT NULL,
  `Name` varchar(50) DEFAULT NULL,
  `Description` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Name_UNIQUE` (`Name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `organisationaddress`;
CREATE TABLE `organisationaddress` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `Organization_id` varchar(45) DEFAULT NULL,
  `Address_id` varchar(45) DEFAULT NULL,
  `DurationOfStay` varchar(45) DEFAULT NULL,
  `IsPrimary` bit(1) NOT NULL DEFAULT b'0',
  `createdby` varchar(45) DEFAULT NULL,
  `modifiedby` varchar(45) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT NULL,
  `lastmodifiedts` timestamp NULL DEFAULT NULL,
  `Type_id` varchar(45) DEFAULT NULL,
  `synctimestamp` timestamp NULL DEFAULT NULL,
  `softdeleteflag` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `organisationcommunication`;
CREATE TABLE `organisationcommunication` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `Type_id` varchar(50) DEFAULT NULL,
  `Organization_id` varchar(50) NOT NULL,
  `Sequence` int(11) DEFAULT NULL,
  `Value` varchar(100) DEFAULT NULL,
  `Extension` varchar(45) DEFAULT NULL,
  `Description` varchar(45) DEFAULT NULL,
  `IsPreferredContactMethod` bit(1) DEFAULT b'0',
  `PreferredContactTime` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT NULL,
  `lastmodifiedts` timestamp NULL DEFAULT NULL,
  `synctimestamp` timestamp NULL DEFAULT NULL,
  `softdeleteflag` bit(1) DEFAULT b'0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `organisationemployees`;
CREATE TABLE `organisationemployees` (
  `id` int(11) NOT NULL,
  `Organization_id` varchar(50) DEFAULT NULL,
  `Customer_id` varchar(50) DEFAULT NULL,
  `Is_Admin` bit(1) NOT NULL DEFAULT b'0',
  `Is_Owner` bit(1) NOT NULL DEFAULT b'0',
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT NULL,
  `lastmodifiedts` timestamp NULL DEFAULT NULL,
  `synctimestamp` timestamp NULL DEFAULT NULL,
  `softdeleteflag` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `organisationmembership`;
CREATE TABLE `organisationmembership` (
  `id` int(11) NOT NULL,
  `Organization_id` varchar(50) DEFAULT NULL,
  `Taxid` varchar(50) NOT NULL,
  `Membership_id` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `organisationowner`;
CREATE TABLE `organisationowner` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `Organization_id` varchar(50) DEFAULT NULL,
  `FirstName` varchar(50) DEFAULT NULL,
  `MidleName` varchar(50) DEFAULT NULL,
  `LastName` varchar(50) DEFAULT NULL,
  `DateOfBirth` date DEFAULT NULL,
  `IDType_id` varchar(50) DEFAULT NULL,
  `IdValue` varchar(50) DEFAULT NULL,
  `Email` varchar(50) DEFAULT NULL,
  `Phone` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `organisationtype`;
CREATE TABLE `organisationtype` (
  `id` int(11) NOT NULL,
  `Name` varchar(50) DEFAULT NULL,
  `Description` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `othersourceofincome`;
CREATE TABLE `othersourceofincome` (
  `id` varchar(50) NOT NULL,
  `IncomeInfo_id` varchar(50) DEFAULT NULL,
  `SourceType` varchar(50) NOT NULL,
  `PayPeriod` varchar(50) DEFAULT NULL,
  `GrossIncome` decimal(10,2) DEFAULT NULL,
  `WeekWorkingHours` varchar(50) DEFAULT NULL,
  `SourceofIncomeDescription` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT NULL,
  `lastmodifiedts` timestamp NULL DEFAULT NULL,
  `synctimestamp` timestamp NULL DEFAULT NULL,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `p2pregistration`;
CREATE TABLE `p2pregistration` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `displayName` varchar(50) DEFAULT NULL,
  `account_id` bigint(20) DEFAULT NULL,
  `isNpp` tinyint(1) DEFAULT '0',
  `isZell` tinyint(1) DEFAULT '0',
  `email` varchar(50) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `phone` varchar(50) DEFAULT NULL,
  `account` tinyblob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `payee`;
CREATE TABLE `payee` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `Type_id` varchar(50) DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  `accountNumber` varchar(50) NOT NULL,
  `companyName` varchar(50) DEFAULT NULL,
  `phone` varchar(50) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `firstName` varchar(50) DEFAULT NULL,
  `lastName` varchar(50) DEFAULT NULL,
  `eBillEnable` int(11) DEFAULT '0',
  `Region_id` int(11) DEFAULT NULL,
  `City_id` int(11) DEFAULT NULL,
  `cityName` varchar(50) DEFAULT NULL,
  `state` varchar(50) DEFAULT NULL,
  `addressLine1` varchar(50) DEFAULT NULL,
  `addressLine2` varchar(50) DEFAULT NULL,
  `zipCode` varchar(20) DEFAULT NULL,
  `User_Id` int(11) DEFAULT NULL,
  `nickName` varchar(50) NOT NULL,
  `softDelete` tinyint(1) DEFAULT '0',
  `billermaster_id` int(11) DEFAULT '1',
  `isAutoPayEnabled` tinyint(1) DEFAULT '0',
  `nameOnBill` varchar(50) DEFAULT NULL,
  `notes` varchar(50) DEFAULT NULL,
  `billerId` varchar(50) DEFAULT NULL,
  `country` varchar(50) DEFAULT NULL,
  `swiftCode` varchar(50) DEFAULT NULL,
  `routingCode` varchar(50) DEFAULT NULL,
  `bankName` varchar(50) DEFAULT NULL,
  `bankAddressLine1` varchar(50) DEFAULT NULL,
  `bankAddressLine2` varchar(50) DEFAULT NULL,
  `bankCity` varchar(50) DEFAULT NULL,
  `bankState` varchar(50) DEFAULT NULL,
  `bankZip` varchar(50) DEFAULT NULL,
  `isWiredRecepient` tinyint(1) DEFAULT '0',
  `internationalAccountNumber` varchar(50) DEFAULT NULL,
  `wireAccountType` varchar(50) DEFAULT NULL,
  `internationalRoutingCode` varchar(50) DEFAULT NULL,
  `isManuallyAdded` tinyint(1) DEFAULT '0',
  `phoneExtension` varchar(10) DEFAULT NULL,
  `phoneCountryCode` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=1568 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `payeeaddress`;
CREATE TABLE `payeeaddress` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `Region_id` int(11) DEFAULT NULL,
  `City_id` int(11) DEFAULT NULL,
  `cityName` varchar(100) DEFAULT NULL,
  `addressLine1` varchar(100) DEFAULT NULL,
  `addressLine2` varchar(100) DEFAULT NULL,
  `zipCode` varchar(20) DEFAULT NULL,
  `latitude` varchar(50) DEFAULT NULL,
  `logitude` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `payeetype`;
CREATE TABLE `payeetype` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `description` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `payperson`;
CREATE TABLE `payperson` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `firstName` varchar(45) DEFAULT NULL,
  `lastName` varchar(45) DEFAULT NULL,
  `phone` varchar(45) DEFAULT NULL,
  `email` varchar(45) DEFAULT NULL,
  `User_id` int(11) NOT NULL,
  `secondaryEmail` varchar(100) DEFAULT NULL,
  `secondoryPhoneNumber` varchar(100) DEFAULT NULL,
  `secondaryEmail2` varchar(100) DEFAULT NULL,
  `secondaryPhoneNumber2` varchar(100) DEFAULT NULL,
  `primaryContactForSending` varchar(100) DEFAULT NULL,
  `nickName` varchar(50) DEFAULT NULL,
  `name` varchar(45) DEFAULT NULL,
  `isSoftDelete` bit(1) DEFAULT b'0',
  `phoneExtension` varchar(10) DEFAULT NULL,
  `phoneCountryCode` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `User_id_Payperson_idx` (`User_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1368 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `pfmbargraph`;
CREATE TABLE `pfmbargraph` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `totalCashFlow` decimal(10,2) DEFAULT NULL,
  `monthId` int(11) DEFAULT NULL,
  `userId` int(11) DEFAULT NULL,
  `year` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `monthId` (`monthId`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `pfmbudgetsnapshot`;
CREATE TABLE `pfmbudgetsnapshot` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `Category_Id` int(11) DEFAULT NULL,
  `allocatedAmount` int(11) DEFAULT '0',
  `amountSpent` int(11) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_Category_Id_idx` (`Category_Id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `pfmcategory`;
CREATE TABLE `pfmcategory` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `categoryName` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `pfmmonth`;
CREATE TABLE `pfmmonth` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `monthName` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `pfmpiechart`;
CREATE TABLE `pfmpiechart` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cashSpent` decimal(10,2) DEFAULT NULL,
  `userId` int(11) DEFAULT NULL,
  `monthId` int(11) DEFAULT NULL,
  `categoryId` int(11) DEFAULT NULL,
  `year` int(4) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `monthId` (`monthId`),
  KEY `userId` (`userId`),
  KEY `categoryId` (`categoryId`)
) ENGINE=InnoDB AUTO_INCREMENT=116 DEFAULT CHARSET=utf8;
DROP TABLE IF EXISTS `pfmtransactions`;
CREATE TABLE `pfmtransactions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `userId` int(11) DEFAULT NULL,
  `monthId` int(11) DEFAULT NULL,
  `categoryId` int(11) DEFAULT NULL,
  `transactionDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `fromAccountNumber` bigint(19) DEFAULT NULL,
  `amount` decimal(10,2) DEFAULT '0.00',
  `notes` varchar(100) DEFAULT NULL,
  `description` varchar(100) DEFAULT NULL,
  `fromAccountName` varchar(100) DEFAULT NULL,
  `isMappedToMerchant` tinyint(1) DEFAULT '0',
  `isAnalyzed` tinyint(1) DEFAULT '0',
  `toAccountNumber` bigint(19) DEFAULT NULL,
  `toAccountName` varchar(100) DEFAULT NULL,
  `year` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `monthId` (`monthId`),
  KEY `userId` (`userId`),
  KEY `categoryId` (`categoryId`)
) ENGINE=InnoDB AUTO_INCREMENT=253 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `phone`;
CREATE TABLE `phone` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` varchar(50) DEFAULT NULL,
  `countryType` varchar(50) DEFAULT NULL,
  `extension` varchar(50) DEFAULT NULL,
  `phoneNumber` varchar(50) DEFAULT NULL,
  `isPrimary` varchar(50) DEFAULT NULL,
  `receivePromotions` varchar(50) DEFAULT NULL,
  `user_id` int(11) NOT NULL,
  `account_id` bigint(20) DEFAULT NULL,
  `phoneCountryCode` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_PHONE_USER` (`user_id`),
  KEY `FK_74bf6041414448008e1a25a684d` (`account_id`)
) ENGINE=InnoDB AUTO_INCREMENT=110 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `preferredaccount`;
CREATE TABLE `preferredaccount` (
  `Type_id` int(11) NOT NULL AUTO_INCREMENT,
  `Account_id` bigint(19) DEFAULT NULL,
  `description` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`Type_id`),
  KEY `Account_id` (`Account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `productdetail`;
CREATE TABLE `productdetail` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `Product_id` int(11) DEFAULT NULL,
  `Type_id` varchar(50) DEFAULT NULL,
  `header` varchar(100) DEFAULT NULL,
  `description` text,
  PRIMARY KEY (`id`),
  KEY `Product_id` (`Product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `querycoborrower`;
CREATE TABLE `querycoborrower` (
  `id` varchar(50) NOT NULL,
  `QueryResponse_id` varchar(50) DEFAULT NULL,
  `FirstName` varchar(50) DEFAULT NULL,
  `LastName` varchar(50) DEFAULT NULL,
  `PhoneNumber` varchar(15) DEFAULT NULL,
  `Email` varchar(50) NOT NULL,
  `Customer_id` varchar(50) DEFAULT NULL,
  `CoBorrower_Type` varchar(50) NOT NULL,
  `CoBorrowerSeq` int(11) DEFAULT NULL,
  `InvitationLink` varchar(200) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT NULL,
  `lastmodifiedts` timestamp NULL DEFAULT NULL,
  `synctimestamp` timestamp NULL DEFAULT NULL,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_QueryCoBorrower` (`Email`),
  KEY `IXFK_QueryCoBorrower_Customer` (`Customer_id`),
  KEY `IXFK_QueryCoBorrower_QueryResponse` (`QueryResponse_id`),
  CONSTRAINT `FK_QueryCoBorrower_QueryResponse` FOREIGN KEY (`QueryResponse_id`) REFERENCES `queryresponse` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `queryresponseconsent`;
CREATE TABLE `queryresponseconsent` (
  `id` varchar(50) NOT NULL,
  `QueryResponse_id` varchar(50) DEFAULT NULL,
  `Disclaimer_id` varchar(50) DEFAULT NULL,
  `Is_Accepted` varchar(50) DEFAULT NULL,
  `Is_Rejected` varchar(50) DEFAULT NULL,
  `Submitedby` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` varchar(50) DEFAULT NULL,
  `lastmodifiedts` timestamp NULL DEFAULT NULL,
  `synctimestamp` timestamp NULL DEFAULT NULL,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `IXFK_QueryResponseConsent_Disclaimer` (`Disclaimer_id`),
  KEY `IXFK_QueryResponseConsent_QueryResponse` (`QueryResponse_id`),
  CONSTRAINT `FK_QueryResponseConsent_Disclaimer` FOREIGN KEY (`Disclaimer_id`) REFERENCES `disclaimer` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_QueryResponseConsent_QueryResponse` FOREIGN KEY (`QueryResponse_id`) REFERENCES `queryresponse` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `scheduledtransaction`;
CREATE TABLE `scheduledtransaction` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `Payee_id` int(11) DEFAULT NULL,
  `Bill_id` int(11) DEFAULT NULL,
  `Type_id` int(11) DEFAULT NULL,
  `fromAccountNumber` bigint(19) DEFAULT NULL,
  `toAccountNumber` bigint(19) DEFAULT NULL,
  `amount` decimal(20,2) DEFAULT '0.00',
  `statusDesc` varchar(50) DEFAULT NULL,
  `notes` varchar(100) DEFAULT '',
  `description` varchar(100) DEFAULT ' ',
  `scheduledDate` date DEFAULT NULL,
  `transactionDate` timestamp NULL DEFAULT NULL,
  `createdDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `toExternalAccountNumber` bigint(19) DEFAULT NULL,
  `Person_Id` int(11) DEFAULT NULL,
  `frequencyType` enum('Once','Daily','Weekly','BiWeekly','Monthly') DEFAULT 'Once',
  `numberOfRecurrences` int(11) DEFAULT NULL,
  `frequencyStartDate` date DEFAULT NULL,
  `frequencyEndDate` date DEFAULT NULL,
  `category` enum('Auto & Transport','Bills & Utilities','Business Services','Education','Entertainment','Fees & Charges','Financial','Food & Dining','Gifts & Donations','Health & Fitness','Home','Income','Investments','Kids','Personal Care','Pets','Shopping','Taxes','Transfer','Travel','Uncategorised') DEFAULT 'Uncategorised',
  `recurrenceDesc` varchar(50) DEFAULT NULL,
  `p2pContact` varchar(50) DEFAULT NULL,
  `routingNumber` varchar(45) DEFAULT NULL,
  `user_id` int(11) NOT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`Id`),
  KEY `fromAccountNumber` (`fromAccountNumber`),
  KEY `toAccountNumber` (`toAccountNumber`),
  KEY `FK_USERID_idx` (`user_id`),
  KEY `FK_TRANSTYPE_idx` (`Type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `securityquestions`;
CREATE TABLE `securityquestions` (
  `QuestionId` int(11) NOT NULL AUTO_INCREMENT,
  `question` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`QuestionId`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `state`;
CREATE TABLE `state` (
  `id` int(11) NOT NULL,
  `state` varchar(45) DEFAULT NULL,
  `country_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `state_UNIQUE` (`state`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
DROP TABLE IF EXISTS `tbladdetails`;
CREATE TABLE `tbladdetails` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `addetails` varchar(2000) DEFAULT NULL,
  `actiondetails` varchar(2000) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_ad_user_id_idx` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `timeperiod`;
CREATE TABLE `timeperiod` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `transaction`;
CREATE TABLE `transaction` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `isScheduled` bit(1) NOT NULL DEFAULT b'0',
  `Customer_id` varchar(50) DEFAULT NULL,
  `ExpenseCategory_id` int(11) DEFAULT NULL,
  `Payee_id` int(11) DEFAULT NULL,
  `Bill_id` int(11) DEFAULT NULL,
  `Type_id` int(11) DEFAULT NULL,
  `Reference_id` varchar(50) DEFAULT NULL,
  `fromAccountNumber` varchar(50) DEFAULT NULL,
  `fromAccountBalance` decimal(10,2) DEFAULT '0.00',
  `toAccountNumber` varchar(50) DEFAULT NULL,
  `toAccountBalance` decimal(10,2) DEFAULT '0.00',
  `amount` decimal(20,2) DEFAULT '0.00',
  `convertedAmount` decimal(20,2) DEFAULT NULL,
  `transactionCurrency` varchar(45) DEFAULT NULL,
  `baseCurrency` varchar(45) DEFAULT NULL,
  `Status_id` varchar(50) DEFAULT NULL,
  `statusDesc` varchar(50) DEFAULT NULL,
  `notes` varchar(150) DEFAULT '',
  `checkNumber` int(11) DEFAULT '0',
  `imageURL1` text,
  `imageURL2` text,
  `hasDepositImage` tinyint(1) DEFAULT '0',
  `description` varchar(100) DEFAULT ' ',
  `scheduledDate` date DEFAULT NULL,
  `transactionDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `createdDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `transactionComments` varchar(100) DEFAULT NULL,
  `toExternalAccountNumber` varchar(45) DEFAULT NULL,
  `Person_Id` int(11) DEFAULT NULL,
  `frequencyType` enum('Once','Daily','Weekly','BiWeekly','Monthly','Yearly','Half Yearly','Quarterly','Every Two Weeks') DEFAULT 'Once',
  `numberOfRecurrences` int(11) DEFAULT '0',
  `frequencyStartDate` date DEFAULT NULL,
  `frequencyEndDate` date DEFAULT NULL,
  `checkImage` longtext,
  `checkImageBack` longtext,
  `cashlessOTPValidDate` timestamp NULL DEFAULT NULL,
  `cashlessOTP` varchar(50) DEFAULT NULL,
  `cashlessPhone` varchar(50) DEFAULT NULL,
  `cashlessEmail` varchar(50) DEFAULT NULL,
  `cashlessPersonName` varchar(50) DEFAULT NULL,
  `cashlessMode` varchar(50) DEFAULT NULL,
  `cashlessSecurityCode` varchar(50) DEFAULT NULL,
  `cashWithdrawalTransactionStatus` varchar(50) DEFAULT NULL,
  `cashlessPin` varchar(50) DEFAULT NULL,
  `category` enum('Auto & Transport','Bills & Utilities','Business Services','Education','Entertainment','Fees & Charges','Financial','Food & Dining','Gifts & Donations','Health & Fitness','Home','Income','Investments','Kids','Personal Care','Pets','Shopping','Taxes','Transfer','Travel','Uncategorised') DEFAULT 'Uncategorised',
  `billCategory` enum('Credit Card','Phone','Utilities','Insurance') DEFAULT NULL,
  `recurrenceDesc` varchar(50) DEFAULT NULL,
  `deliverBy` varchar(50) DEFAULT NULL,
  `p2pContact` varchar(50) DEFAULT NULL,
  `p2pRequiredDate` date DEFAULT NULL,
  `requestCreatedDate` varchar(50) DEFAULT NULL,
  `penaltyFlag` bit(1) DEFAULT b'0',
  `payoffFlag` bit(1) DEFAULT b'0',
  `viewReportLink` varchar(150) DEFAULT 'http://pmqa.konylabs.net/KonyWebBanking/view_report.png',
  `isPaypersonDeleted` bit(1) DEFAULT b'0',
  `fee` varchar(50) DEFAULT '0.00',
  `feeCurrency` varchar(45) DEFAULT NULL,
  `feePaidByReceipent` bit(1) DEFAULT NULL,
  `frontImage1` varchar(100) DEFAULT NULL,
  `frontImage2` varchar(100) DEFAULT NULL,
  `backImage1` varchar(100) DEFAULT NULL,
  `backImage2` varchar(100) DEFAULT NULL,
  `checkDesc` varchar(50) DEFAULT NULL,
  `checkNumber1` varchar(50) DEFAULT NULL,
  `checkNumber2` varchar(50) DEFAULT NULL,
  `bankName1` varchar(50) DEFAULT NULL,
  `bankName2` varchar(50) DEFAULT NULL,
  `withdrawlAmount1` varchar(50) DEFAULT '0.00',
  `withdrawlAmount2` varchar(50) DEFAULT '0.00',
  `cashAmount` varchar(50) DEFAULT '0.00',
  `payeeCurrency` varchar(50) DEFAULT 'INR',
  `billid` bigint(20) DEFAULT NULL,
  `isDisputed` bit(1) DEFAULT b'0',
  `disputeDescription` varchar(50) DEFAULT NULL,
  `disputeReason` varchar(50) DEFAULT NULL,
  `disputeStatus` varchar(50) DEFAULT NULL,
  `disputeDate` timestamp NULL DEFAULT NULL,
  `payeeName` varchar(50) DEFAULT NULL,
  `checkDateOfIssue` timestamp NULL DEFAULT NULL,
  `checkReason` varchar(50) DEFAULT NULL,
  `isPayeeDeleted` bit(1) DEFAULT b'0',
  `amountRecieved` varchar(50) DEFAULT '0',
  `requestValidity` timestamp NULL DEFAULT NULL,
  `statementReference` varchar(35) DEFAULT NULL,
  `transCreditDebitIndicator` varchar(45) DEFAULT NULL,
  `bookingDateTime` datetime DEFAULT NULL,
  `valueDateTime` datetime DEFAULT NULL,
  `transactionInformation` varchar(500) DEFAULT NULL,
  `addressLine` varchar(70) DEFAULT NULL,
  `transactionAmount` varchar(50) DEFAULT NULL,
  `chargeAmount` varchar(50) DEFAULT NULL,
  `chargeCurrency` varchar(50) DEFAULT NULL,
  `sourceCurrency` varchar(45) DEFAULT NULL,
  `targetCurrency` varchar(50) DEFAULT NULL,
  `unitCurrency` varchar(50) DEFAULT NULL,
  `exchangeRate` varchar(45) DEFAULT NULL,
  `contractIdentification` varchar(35) DEFAULT NULL,
  `quotationDate` datetime DEFAULT NULL,
  `instructedAmount` varchar(45) DEFAULT NULL,
  `instructedCurrency` varchar(45) DEFAULT NULL,
  `transactionCode` varchar(35) DEFAULT NULL,
  `transactionSubCode` varchar(45) DEFAULT NULL,
  `proprietaryTransactionCode` varchar(35) DEFAULT NULL,
  `proprietaryTransactionIssuer` varchar(35) DEFAULT NULL,
  `balanceCreditDebitIndicator` varchar(45) DEFAULT NULL,
  `balanceType` varchar(45) DEFAULT NULL,
  `balanceAmount` varchar(45) DEFAULT NULL,
  `balanceCurrency` varchar(45) DEFAULT NULL,
  `merchantName` varchar(45) DEFAULT NULL,
  `merchantCategoryCode` varchar(45) DEFAULT NULL,
  `creditorAgentSchemeName` varchar(45) DEFAULT NULL,
  `creditorAgentIdentification` varchar(45) DEFAULT NULL,
  `creditorAgentName` varchar(140) DEFAULT NULL,
  `creditorAgentaddressType` varchar(45) DEFAULT NULL,
  `creditorAgentDepartment` varchar(45) DEFAULT NULL,
  `creditorAgentSubDepartment` varchar(45) DEFAULT NULL,
  `creditorAgentStreetName` varchar(45) DEFAULT NULL,
  `creditorAgentBuildingNumber` varchar(45) DEFAULT NULL,
  `creditorAgentPostCode` varchar(45) DEFAULT NULL,
  `creditorAgentTownName` varchar(45) DEFAULT NULL,
  `creditorAgentCountrySubDivision` varchar(45) DEFAULT NULL,
  `creditorAgentCountry` varchar(45) DEFAULT NULL,
  `creditorAgentAddressLine` varchar(45) DEFAULT NULL,
  `creditorAccountSchemeName` varchar(45) DEFAULT NULL,
  `creditorAccountIdentification` varchar(45) DEFAULT NULL,
  `creditorAccountName` varchar(45) DEFAULT NULL,
  `creditorAccountSeconIdentification` varchar(45) DEFAULT NULL,
  `debtorAgentSchemeName` varchar(45) DEFAULT NULL,
  `debtorAgentIdentification` varchar(45) DEFAULT NULL,
  `debtorAgentName` varchar(45) DEFAULT NULL,
  `debtorAgentAddressType` varchar(45) DEFAULT NULL,
  `debtorAgentDepartment` varchar(45) DEFAULT NULL,
  `debtorAgentSubDepartment` varchar(45) DEFAULT NULL,
  `debtorAgentStreetName` varchar(45) DEFAULT NULL,
  `debtorAgentBuildingNumber` varchar(45) DEFAULT NULL,
  `dedtorAgentPostCode` varchar(45) DEFAULT NULL,
  `debtorAgentTownName` varchar(45) DEFAULT NULL,
  `debtorAgentCountrySubDivision` varchar(45) DEFAULT NULL,
  `debtorAgentCountry` varchar(45) DEFAULT NULL,
  `debtorAgentAddressLine` varchar(45) DEFAULT NULL,
  `debtorAccountSchemeName` varchar(45) DEFAULT NULL,
  `debtorAccountIdentification` varchar(45) DEFAULT NULL,
  `debtorAccountName` varchar(45) DEFAULT NULL,
  `debtorAccountSeconIdentification` varchar(45) DEFAULT NULL,
  `cardInstrumentSchemeName` varchar(45) DEFAULT NULL,
  `cardInstrumentAuthorisationType` varchar(45) DEFAULT NULL,
  `cardInstrumentName` varchar(45) DEFAULT NULL,
  `cardInstrumentIdentification` varchar(45) DEFAULT NULL,
  `IBAN` varchar(45) DEFAULT NULL,
  `sortCode` varchar(45) DEFAULT NULL,
  `FirstPaymentDateTime` timestamp NULL DEFAULT NULL,
  `NextPaymentDateTime` timestamp NULL DEFAULT NULL,
  `FinalPaymentDateTime` timestamp NULL DEFAULT NULL,
  `StandingOrderStatusCode` varchar(6) DEFAULT NULL,
  `FP_Amount` decimal(12,2) DEFAULT NULL,
  `FP_Currency` varchar(45) DEFAULT NULL,
  `NP_Amount` decimal(12,2) DEFAULT NULL,
  `NP_Currency` varchar(45) DEFAULT NULL,
  `FPA_Amount` decimal(12,2) DEFAULT NULL,
  `FPA_Currency` varchar(45) DEFAULT NULL,
  `ConsentId` varchar(45) DEFAULT NULL,
  `Initiation_InstructionIdentification` varchar(45) DEFAULT NULL,
  `Initiation_EndToEndIdentification` varchar(45) DEFAULT NULL,
  `RI_Reference` varchar(45) DEFAULT NULL,
  `RI_Unstructured` varchar(45) DEFAULT NULL,
  `RiskPaymentContextCode` varchar(45) DEFAULT NULL,
  `MerchantCustomerIdentification` varchar(200) DEFAULT NULL,
  `beneficiaryName` varchar(50) DEFAULT NULL,
  `bankName` varchar(50) DEFAULT NULL,
  `swiftCode` varchar(45) DEFAULT NULL,
  `DomesticPaymentId` varchar(45) DEFAULT NULL,
  `linkSelf` varchar(45) DEFAULT NULL,
  `StatusUpdateDateTime` timestamp(6) NULL DEFAULT NULL,
  `dataStatus` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`Id`),
  KEY `fromAccountNumber` (`fromAccountNumber`),
  KEY `toAccountNumber` (`toAccountNumber`),
  KEY `fromtoaccountnumber` (`fromAccountNumber`,`toAccountNumber`) COMMENT 'from and two account number',
  KEY `type_id` (`Type_id`) COMMENT 'transaction Type',
  KEY `payee_id` (`Payee_id`) COMMENT 'payee index'
) ENGINE=InnoDB AUTO_INCREMENT=8743 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `transactiontype`;
CREATE TABLE `transactiontype` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `transferseries`;
CREATE TABLE `transferseries` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `Type_id` varchar(50) DEFAULT NULL,
  `accountFrom` bigint(19) DEFAULT NULL,
  `accountTo` bigint(19) DEFAULT NULL,
  `amount` decimal(10,2) DEFAULT NULL,
  `notes` varchar(50) DEFAULT NULL,
  `Status_id` varchar(50) DEFAULT NULL,
  `createdDate` date DEFAULT NULL,
  `recurrenceType` varchar(50) DEFAULT NULL,
  `frequency` int(11) DEFAULT NULL,
  `occurrences` int(11) DEFAULT NULL,
  `startDate` date DEFAULT NULL,
  `endDate` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `accountFrom` (`accountFrom`),
  KEY `accountTo` (`accountTo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `Application_id` varchar(50) DEFAULT NULL,
  `Session_id` varchar(50) DEFAULT NULL,
  `Device_id` varchar(50) DEFAULT NULL,
  `userImage` longtext,
  `ssn` varchar(25) DEFAULT NULL,
  `userName` varchar(50) DEFAULT NULL,
  `passWord` varchar(50) DEFAULT NULL,
  `userFirstName` varchar(50) DEFAULT NULL,
  `userLastName` varchar(50) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `countryCode` varchar(45) DEFAULT NULL,
  `email` varchar(80) DEFAULT NULL,
  `default_account_transfers` varchar(50) DEFAULT NULL,
  `dateOfBirth` date DEFAULT NULL,
  `default_account_deposit` varchar(50) DEFAULT NULL,
  `defaultModule_id` int(11) DEFAULT NULL,
  `default_account_payments` varchar(50) DEFAULT NULL,
  `secondaryphone` varchar(20) DEFAULT NULL,
  `secondaryemail` varchar(30) DEFAULT NULL,
  `lastlogintime` timestamp NULL DEFAULT NULL,
  `areUserAlertsTurnedOn` tinyint(1) DEFAULT '0',
  `areDepositTermsAccepted` tinyint(1) DEFAULT '0',
  `areAccountStatementTermsAccepted` tinyint(1) DEFAULT '0',
  `unsuccessfulLoginAttempts` int(11) DEFAULT '0',
  `isUserAccountLocked` tinyint(1) DEFAULT '0',
  `userImageURL` varchar(500) DEFAULT NULL,
  `addressLine1` varchar(50) DEFAULT NULL,
  `addressLine2` varchar(45) DEFAULT NULL,
  `city` varchar(45) DEFAULT NULL,
  `state` varchar(45) DEFAULT NULL,
  `country` varchar(45) DEFAULT NULL,
  `zipcode` varchar(45) DEFAULT NULL,
  `isSuperAdmin` tinyint(1) DEFAULT '0',
  `userCompany` varchar(45) DEFAULT NULL,
  `validDate` timestamp NULL DEFAULT NULL,
  `pin` varchar(6) DEFAULT NULL,
  `isPinSet` tinyint(1) DEFAULT NULL,
  `role` enum('PREMIUM','BASIC') DEFAULT 'BASIC',
  `cvv` varchar(45) DEFAULT NULL,
  `otp` varchar(45) DEFAULT NULL,
  `lockCount` int(11) DEFAULT '0',
  `isEnrolled` tinyint(1) DEFAULT '1',
  `Bank_id` varchar(50) DEFAULT NULL,
  `default_account_cardless` varchar(50) DEFAULT NULL,
  `isBillPaySupported` tinyint(1) DEFAULT '0',
  `isP2PSupported` tinyint(1) DEFAULT '0',
  `isBillPayActivated` tinyint(1) DEFAULT '0',
  `isP2PActivated` tinyint(1) DEFAULT '0',
  `default_account_billPay` varchar(50) DEFAULT NULL,
  `default_to_account_p2p` varchar(50) DEFAULT NULL,
  `default_from_account_p2p` varchar(50) DEFAULT NULL,
  `isPhoneEnabled` tinyint(1) DEFAULT '0',
  `isEmailEnabled` tinyint(1) DEFAULT '0',
  `secondaryemail2` varchar(80) DEFAULT NULL,
  `secondaryphone2` varchar(20) DEFAULT NULL,
  `token` varchar(200) DEFAULT NULL,
  `isWireTransferActivated` tinyint(1) DEFAULT '0',
  `default_account_wire` bigint(20) DEFAULT NULL,
  `isWireTransferEligible` tinyint(1) DEFAULT '0',
  `currentLoginTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `maritalstatus` enum('Single','Married','Divorced','Widowed') DEFAULT NULL,
  `spousefirstname` varchar(50) DEFAULT NULL,
  `spouselastname` varchar(50) DEFAULT NULL,
  `noofdependents` varchar(10) DEFAULT NULL,
  `gender` enum('Male','Female') DEFAULT NULL,
  `showBillPayFromAccPopup` bit(1) DEFAULT b'1',
  `drivingLicenseNumber` varchar(50) DEFAULT NULL,
  `phoneExtension` varchar(10) DEFAULT NULL,
  `phoneCountryCode` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`Id`),
  UNIQUE KEY `User_Name` (`userName`),
  UNIQUE KEY `defaultModule_id_UNIQUE` (`defaultModule_id`),
  KEY `UserNamePassword` (`userName`,`passWord`),
  KEY `UserPassPin` (`userName`,`passWord`,`pin`),
  KEY `UserNameIndex` (`userName`)
) ENGINE=InnoDB AUTO_INCREMENT=206 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `useraccountalerts`;
CREATE TABLE `useraccountalerts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `User_id` int(11) DEFAULT NULL,
  `AccountNumber` bigint(19) DEFAULT NULL,
  `minimumBalance` decimal(20,2) DEFAULT NULL,
  `debitLimit` decimal(20,2) DEFAULT NULL,
  `creditLimit` decimal(20,2) DEFAULT NULL,
  `balanceUpdate_PeriodId` int(11) DEFAULT NULL,
  `PayementDueReminder_PeriodId` int(11) DEFAULT NULL,
  `depositMaturityReminder_PeriodId` int(11) DEFAULT NULL,
  `isEnabled` tinyint(1) DEFAULT '0',
  `successfulTransfer` tinyint(1) DEFAULT '0',
  `checkClearance` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=265 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `useraccounts`;
CREATE TABLE `useraccounts` (
  `id` varchar(50) NOT NULL,
  `Customer_id` varchar(50) DEFAULT NULL,
  `MemberId` varchar(50) DEFAULT NULL,
  `Account_id` varchar(50) DEFAULT NULL,
  `AccountName` varchar(50) DEFAULT NULL,
  `IsViewAllowed` tinyint(1) NOT NULL DEFAULT '0',
  `IsDepositAllowed` tinyint(1) NOT NULL DEFAULT '0',
  `IsWithdrawAllowed` tinyint(1) NOT NULL DEFAULT '0',
  `IsOrganizationAccount` tinyint(1) NOT NULL DEFAULT '0',
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT NULL,
  `lastmodifiedts` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `useralerts`;
CREATE TABLE `useralerts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `User_id` int(11) DEFAULT NULL,
  `bankingIDChange` tinyint(1) DEFAULT '0',
  `passwordChange` tinyint(1) DEFAULT '0',
  `passwordExpired` tinyint(1) DEFAULT '0',
  `communicationChange` tinyint(1) DEFAULT '0',
  `newPayeeAdded` tinyint(1) DEFAULT '0',
  `payeeDetailsUpdated` tinyint(1) DEFAULT '0',
  `newDealsAvailable` tinyint(1) DEFAULT '0',
  `dealsExpiring` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `User` (`User_id`),
  KEY `User_id` (`User_id`),
  CONSTRAINT `FK_USERALERTS_User` FOREIGN KEY (`User_id`) REFERENCES `user` (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=152 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `usercashflow`;
CREATE TABLE `usercashflow` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `monthCash` int(11) NOT NULL DEFAULT '0',
  `monthCredit` int(11) NOT NULL DEFAULT '0',
  `totalCash` int(11) NOT NULL DEFAULT '0',
  `totalCreditDebit` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `usercommunication`;
CREATE TABLE `usercommunication` (
  `Type_id` varchar(50) DEFAULT NULL,
  `User_id` int(11) DEFAULT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sequence` int(11) DEFAULT NULL,
  `value` varchar(100) DEFAULT NULL,
  `extension` varchar(50) DEFAULT NULL,
  `description` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `Type_id` (`Type_id`),
  KEY `User_id` (`User_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `usercreditcheck`;
CREATE TABLE `usercreditcheck` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `User_id` int(11) DEFAULT NULL,
  `isCreditCheck` tinyint(1) DEFAULT '0',
  `isSingatureUpload` tinyint(1) DEFAULT '0',
  `ssn` varchar(20) DEFAULT NULL,
  `singatureImage` longtext,
  `newuser_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_USERCREDITCHECK_USER` (`User_id`),
  CONSTRAINT `FK_USERCREDITCHECK_USER` FOREIGN KEY (`User_id`) REFERENCES `user` (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `usernotification`;
CREATE TABLE `usernotification` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `notification_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `isRead` varchar(11) DEFAULT NULL,
  `receivedDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `FK_USERNOTIFICATION_USER` (`user_id`),
  KEY `FK_USERNOTIFICATION_NOTIFICATION` (`notification_id`),
  CONSTRAINT `FK_USERNOTIFICATION_USER` FOREIGN KEY (`user_id`) REFERENCES `user` (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=441 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `userpersonalinfo`;
CREATE TABLE `userpersonalinfo` (
  `User_id` int(11) DEFAULT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `dateOfBirth` date DEFAULT NULL,
  `gender` enum('Male','Female') DEFAULT NULL,
  `userfirstname` varchar(50) DEFAULT NULL,
  `userlastname` varchar(50) DEFAULT NULL,
  `maritalstatus` enum('Single','Married','Divorced','Widowed') DEFAULT NULL,
  `spouseFirstName` varchar(50) DEFAULT NULL,
  `spouseLastName` varchar(50) DEFAULT NULL,
  `noOfDependents` varchar(10) DEFAULT NULL,
  `addressLine1` varchar(50) DEFAULT NULL,
  `addressLine2` varchar(50) DEFAULT NULL,
  `city` varchar(20) DEFAULT NULL,
  `state` varchar(20) DEFAULT NULL,
  `country` varchar(20) DEFAULT NULL,
  `zipcode` varchar(10) DEFAULT NULL,
  `employmentInfo` varchar(50) DEFAULT NULL,
  `company` varchar(50) DEFAULT NULL,
  `jobProfile` varchar(50) DEFAULT NULL,
  `experience` varchar(50) DEFAULT NULL,
  `annualIncome` varchar(50) DEFAULT NULL,
  `assets` varchar(50) DEFAULT NULL,
  `montlyExpenditure` varchar(50) DEFAULT NULL,
  `addressDoc` longtext,
  `signatureImage` longtext,
  `employementDoc` longtext,
  `incomeDoc` longtext,
  `ssn` varchar(50) DEFAULT NULL,
  `userPersonalInfo` tinyint(1) DEFAULT '0',
  `userEmploymentInfo` tinyint(1) DEFAULT '0',
  `userFinancialInfo` tinyint(1) DEFAULT '0',
  `userSecurityQuestions` tinyint(1) DEFAULT '0',
  `spousename` varchar(50) DEFAULT NULL,
  `newuser_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_USERPERSONALINFO_USER` (`User_id`),
  CONSTRAINT `FK_USERPERSONALINFO_USER` FOREIGN KEY (`User_id`) REFERENCES `user` (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `userproducts`;
CREATE TABLE `userproducts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `User_id` int(11) DEFAULT NULL,
  `Product_id` int(11) DEFAULT NULL,
  `newuser_id` int(11) DEFAULT NULL,
  `product` longtext,
  PRIMARY KEY (`id`),
  KEY `FK_USERPRODUCTS_USER` (`User_id`),
  KEY `FK_202b1ea662ea4874b4f80a7f347` (`Product_id`),
  CONSTRAINT `FK_USERPRODUCTS_USER` FOREIGN KEY (`User_id`) REFERENCES `user` (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `usersecurity`;
CREATE TABLE `usersecurity` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `User_id` int(11) NOT NULL DEFAULT '0',
  `question` int(11) NOT NULL,
  `answer` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`,`User_id`),
  KEY `User_id` (`User_id`),
  KEY `Question` (`question`),
  CONSTRAINT `FK_UserSecurity_SecurityQuestions` FOREIGN KEY (`question`) REFERENCES `securityquestions` (`QuestionId`),
  CONSTRAINT `FK_UserSecurity_User` FOREIGN KEY (`User_id`) REFERENCES `user` (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=441 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `userserviceprefernces`;
CREATE TABLE `userserviceprefernces` (
  `id` varchar(50) NOT NULL,
  `Customer_id` varchar(50) DEFAULT NULL,
  `Service_id` varchar(50) DEFAULT NULL,
  `PerDayLimit` varchar(50) DEFAULT NULL,
  `PerMonthLimit` varchar(50) DEFAULT NULL,
  `PerAccountLimit` varchar(50) DEFAULT NULL,
  `HasApprove` tinyint(1) NOT NULL DEFAULT '0',
  `HasDraftOnly` tinyint(1) NOT NULL DEFAULT '0',
  `HasCancel` tinyint(1) NOT NULL DEFAULT '0',
  `IsViewAll` tinyint(1) NOT NULL DEFAULT '0',
  `IsViewNone` tinyint(1) NOT NULL DEFAULT '0',
  `IsViewOwnTransfersOnly` tinyint(1) NOT NULL DEFAULT '0',
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT NULL,
  `lastmodifiedts` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `usertransactionhistory`;
CREATE TABLE `usertransactionhistory` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `description` varchar(45) DEFAULT NULL,
  `transactionDate` timestamp NULL DEFAULT NULL,
  `amount` int(11) DEFAULT '0',
  `depositAmount` int(11) DEFAULT '0',
  `transactionType` varchar(45) DEFAULT NULL,
  `referenceId` varchar(45) DEFAULT NULL,
  `closingBalanceAmount` int(11) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `vihicleinfo`;
CREATE TABLE `vihicleinfo` (
  `id` varchar(50) NOT NULL,
  `VehicleType` varchar(50) DEFAULT NULL,
  `Year` year(4) DEFAULT NULL,
  `Make` varchar(50) DEFAULT NULL,
  `Model` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `lastmodifiedts` timestamp NULL DEFAULT NULL,
  `synctimestamp` timestamp NULL DEFAULT NULL,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `wallet`;
CREATE TABLE `wallet` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_fee6cfa8db4440ba8e768221092` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP VIEW IF EXISTS `accountransactionview`;
CREATE VIEW `accountransactionview` AS select `accounts`.`Account_id` AS `Account_id`,`accounts`.`AccountName` AS `AccountName`,`accounts`.`AccountHolder` AS `AccountHolder`,`accounts`.`UserName` AS `UserName`,`accounts`.`ExternalBankidentity_id` AS `ExternalBankidentity_id`,`accounts`.`CurrencyCode` AS `CurrencyCode`,`accounts`.`User_id` AS `User_id`,`accounts`.`AvailableBalance` AS `AvailableBalance`,`accounts`.`Bank_id` AS `Bank_id`,`accounts`.`ShowTransactions` AS `ShowTransactions`,`accounts`.`CurrentBalance` AS `CurrentBalance`,`accounts`.`RoutingNumber` AS `RoutingNumber`,`transaction`.`Id` AS `transactionId`,`transaction`.`Type_id` AS `transactiontype`,`transaction`.`Customer_id` AS `Customer_id`,`transaction`.`ExpenseCategory_id` AS `ExpenseCategory_id`,`transaction`.`billid` AS `Bill_id`,`transaction`.`Reference_id` AS `Reference_id`,`transaction`.`fromAccountNumber` AS `fromAccountNumber`,`transaction`.`fromAccountBalance` AS `fromAccountBalance`,`transaction`.`toAccountNumber` AS `toAccountNumber`,`transaction`.`toAccountBalance` AS `toAccountBalance`,`transaction`.`amount` AS `amount`,`transaction`.`convertedAmount` AS `convertedAmount`,`transaction`.`transactionCurrency` AS `transactionCurrency`,`transaction`.`baseCurrency` AS `baseCurrency`,`transaction`.`Status_id` AS `Status_id`,`transaction`.`statusDesc` AS `statusDesc`,`transaction`.`isScheduled` AS `isScheduled`,`transaction`.`category` AS `category`,`transaction`.`billCategory` AS `billCategory`,`transaction`.`toExternalAccountNumber` AS `ExternalAccountNumber`,`transaction`.`Person_Id` AS `Person_Id`,`transaction`.`frequencyType` AS `frequencyType`,`transaction`.`createdDate` AS `createdDate`,`transaction`.`cashlessEmail` AS `cashlessEmail`,`transaction`.`cashlessMode` AS `cashlessMode`,`transaction`.`cashlessOTP` AS `cashlessOTP`,`transaction`.`cashlessOTPValidDate` AS `cashlessOTPValidDate`,`transaction`.`cashlessPersonName` AS `cashlessPersonName`,`transaction`.`cashlessPhone` AS `cashlessPhone`,`transaction`.`cashlessSecurityCode` AS `cashlessSecurityCode`,`transaction`.`cashWithdrawalTransactionStatus` AS `cashWithdrawalTransactionStatus`,`transaction`.`frequencyEndDate` AS `frequencyEndDate`,`transaction`.`frequencyStartDate` AS `frequencyStartDate`,`transaction`.`hasDepositImage` AS `hasDepositImage`,`transaction`.`Payee_id` AS `payeeId`,`transaction`.`payeeName` AS `payeeName`,`transaction`.`p2pContact` AS `p2pContact`,`transaction`.`Person_Id` AS `personId`,`transaction`.`recurrenceDesc` AS `recurrenceDesc`,`transaction`.`numberOfRecurrences` AS `numberOfRecurrences`,`transaction`.`scheduledDate` AS `scheduledDate`,`transaction`.`transactionComments` AS `transactionComments`,`transaction`.`notes` AS `transactionsNotes`,`transaction`.`description` AS `transDescription`,`transaction`.`transactionDate` AS `transactionDate`,`transaction`.`frontImage1` AS `frontImage1`,`transaction`.`frontImage2` AS `frontImage2`,`transaction`.`backImage1` AS `backImage1`,`transaction`.`backImage2` AS `backImage2`,`transaction`.`checkDesc` AS `checkDesc`,`transaction`.`checkNumber1` AS `checkNumber1`,`transaction`.`checkNumber2` AS `checkNumber2`,`transaction`.`checkNumber` AS `checkNumber`,`transaction`.`checkReason` AS `checkReason`,`transaction`.`requestValidity` AS `requestValidity`,`transaction`.`checkDateOfIssue` AS `checkDateOfIssue`,`transaction`.`bankName1` AS `bankName1`,`transaction`.`bankName2` AS `bankName2`,`transaction`.`withdrawlAmount1` AS `withdrawlAmount1`,`transaction`.`withdrawlAmount2` AS `withdrawlAmount2`,`transaction`.`cashAmount` AS `cashAmount`,`transaction`.`payeeCurrency` AS `payeeCurrency`,`transaction`.`fee` AS `fee`,`transaction`.`feePaidByReceipent` AS `feePaidByReceipent`,`transaction`.`feeCurrency` AS `feeCurrency`,`transaction`.`isDisputed` AS `isDisputed`,`transaction`.`disputeReason` AS `disputeReason`,`transaction`.`disputeDescription` AS `disputeDescription`,`transaction`.`disputeDate` AS `disputeDate`,`transaction`.`disputeStatus` AS `disputeStatus`,`transactiontype`.`description` AS `description`,`transaction`.`statementReference` AS `statementReference`,`transaction`.`transCreditDebitIndicator` AS `transCreditDebitIndicator`,`transaction`.`bookingDateTime` AS `bookingDateTime`,`transaction`.`valueDateTime` AS `valueDateTime`,`transaction`.`transactionInformation` AS `transactionInformation`,`transaction`.`addressLine` AS `addressLine`,`transaction`.`transactionAmount` AS `transactionAmount`,`transaction`.`chargeAmount` AS `chargeAmount`,`transaction`.`chargeCurrency` AS `chargeCurrency`,`transaction`.`sourceCurrency` AS `sourceCurrency`,`transaction`.`targetCurrency` AS `targetCurrency`,`transaction`.`unitCurrency` AS `unitCurrency`,`transaction`.`exchangeRate` AS `exchangeRate`,`transaction`.`contractIdentification` AS `contractIdentification`,`transaction`.`quotationDate` AS `quotationDate`,`transaction`.`instructedAmount` AS `instructedAmount`,`transaction`.`instructedCurrency` AS `instructedCurrency`,`transaction`.`transactionCode` AS `transactionCode`,`transaction`.`transactionSubCode` AS `transactionSubCode`,`transaction`.`proprietaryTransactionCode` AS `proprietaryTransactionCode`,`transaction`.`proprietaryTransactionIssuer` AS `proprietaryTransactionIssuer`,`transaction`.`balanceCreditDebitIndicator` AS `balanceCreditDebitIndicator`,`transaction`.`balanceType` AS `balanceType`,`transaction`.`balanceAmount` AS `balanceAmount`,`transaction`.`balanceCurrency` AS `balanceCurrency`,`transaction`.`merchantName` AS `merchantName`,`transaction`.`merchantCategoryCode` AS `merchantCategoryCode`,`transaction`.`creditorAgentSchemeName` AS `creditorAgentSchemeName`,`transaction`.`creditorAgentIdentification` AS `creditorAgentIdentification`,`transaction`.`creditorAgentName` AS `creditorAgentName`,`transaction`.`creditorAgentaddressType` AS `creditorAgentaddressType`,`transaction`.`creditorAgentDepartment` AS `creditorAgentDepartment`,`transaction`.`creditorAgentSubDepartment` AS `creditorAgentSubDepartment`,`transaction`.`creditorAgentStreetName` AS `creditorAgentStreetName`,`transaction`.`creditorAgentBuildingNumber` AS `creditorAgentBuildingNumber`,`transaction`.`creditorAgentPostCode` AS `creditorAgentPostCode`,`transaction`.`creditorAgentTownName` AS `creditorAgentTownName`,`transaction`.`creditorAgentCountrySubDivision` AS `creditorAgentCountrySubDivision`,`transaction`.`creditorAgentCountry` AS `creditorAgentCountry`,`transaction`.`creditorAgentAddressLine` AS `creditorAgentAddressLine`,`transaction`.`creditorAccountSchemeName` AS `creditorAccountSchemeName`,`transaction`.`creditorAccountIdentification` AS `creditorAccountIdentification`,`transaction`.`creditorAccountName` AS `creditorAccountName`,`transaction`.`creditorAccountSeconIdentification` AS `creditorAccountSeconIdentification`,`transaction`.`debtorAgentSchemeName` AS `debtorAgentSchemeName`,`transaction`.`debtorAgentIdentification` AS `debtorAgentIdentification`,`transaction`.`debtorAgentName` AS `debtorAgentName`,`transaction`.`debtorAgentAddressType` AS `debtorAgentAddressType`,`transaction`.`debtorAgentDepartment` AS `debtorAgentDepartment`,`transaction`.`debtorAgentSubDepartment` AS `debtorAgentSubDepartment`,`transaction`.`debtorAgentStreetName` AS `debtorAgentStreetName`,`transaction`.`debtorAgentBuildingNumber` AS `debtorAgentBuildingNumber`,`transaction`.`dedtorAgentPostCode` AS `dedtorAgentPostCode`,`transaction`.`debtorAgentTownName` AS `debtorAgentTownName`,`transaction`.`debtorAgentCountrySubDivision` AS `debtorAgentCountrySubDivision`,`transaction`.`debtorAgentCountry` AS `debtorAgentCountry`,`transaction`.`debtorAgentAddressLine` AS `debtorAgentAddressLine`,`transaction`.`debtorAccountSchemeName` AS `debtorAccountSchemeName`,`transaction`.`debtorAccountIdentification` AS `debtorAccountIdentification`,`transaction`.`debtorAccountName` AS `debtorAccountName`,`transaction`.`debtorAccountSeconIdentification` AS `debtorAccountSeconIdentification`,`transaction`.`cardInstrumentSchemeName` AS `cardInstrumentSchemeName`,`transaction`.`cardInstrumentAuthorisationType` AS `cardInstrumentAuthorisationType`,`transaction`.`cardInstrumentName` AS `cardInstrumentName`,`transaction`.`cardInstrumentIdentification` AS `cardInstrumentIdentification`,`transaction`.`FirstPaymentDateTime` AS `FirstPaymentDateTime`,`transaction`.`NextPaymentDateTime` AS `NextPaymentDateTime`,`transaction`.`FinalPaymentDateTime` AS `FinalPaymentDateTime`,`transaction`.`StandingOrderStatusCode` AS `StandingOrderStatusCode`,`transaction`.`FP_Amount` AS `FP_Amount`,`transaction`.`FP_Currency` AS `FP_Currency`,`transaction`.`NP_Amount` AS `NP_Amount`,`transaction`.`NP_Currency` AS `NP_Currency`,`transaction`.`FPA_Amount` AS `FPA_Amount`,`transaction`.`FPA_Currency` AS `FPA_Currency`,`transaction`.`IBAN` AS `IBAN`,`transaction`.`sortCode` AS `sortCode`,`transaction`.`beneficiaryName` AS `beneficiaryName`,`transaction`.`bankName` AS `bankName`,`transaction`.`swiftCode` AS `swiftCode` from ((`accounts` join `transaction`) join `transactiontype`) where (((`accounts`.`Account_id` = `transaction`.`fromAccountNumber`) or (`accounts`.`Account_id` = `transaction`.`toAccountNumber`)) and (`transaction`.`Type_id` = `transactiontype`.`Id`)) group by `transaction`.`Id` order by `transaction`.`createdDate` desc ;

DROP VIEW IF EXISTS `accountstatementview`;
CREATE VIEW `accountstatementview` AS select `accounts`.`Type_id` AS `Type_id`,`accountstatement`.`description` AS `Description`,`accountstatement`.`statementLink` AS `Statementlink`,`accountstatement`.`Account_id` AS `Account_id`,`accountstatement`.`month` AS `Month` from (`accounts` join `accountstatement`) where (`accounts`.`Account_id` = `accountstatement`.`Account_id`) ;

DROP VIEW IF EXISTS `accountsview`;
CREATE VIEW `accountsview` AS select `accounts`.`ExternalBankidentity_id` AS `ExternalBankidentity_id`,`externalbankidentity`.`MainUser_id` AS `MainUser_id`,`externalbankidentity`.`User_id` AS `User_id`,`externalbankidentity`.`Password` AS `Password`,`externalbankidentity`.`SessionToken` AS `SessionToken`,`externalbank`.`BankName` AS `BankName`,`externalbank`.`logo` AS `logo`,`externalbankidentity`.`ExternalBank_id` AS `ExternalBank_id`,`accounts`.`Account_id` AS `Account_id`,`accounts`.`AccountName` AS `AccountName`,`accounts`.`CurrencyCode` AS `CurrencyCode`,`accounts`.`AvailableBalance` AS `AvailableBalance`,`accounts`.`AccountHolder` AS `AccountHolder`,`accounts`.`Address` AS `Address`,`accounts`.`Scheme` AS `Scheme`,`accounts`.`Number` AS `Number`,`accounts`.`error` AS `error`,`accounts`.`LastUpdated` AS `LastUpdated`,`accounts`.`InternalAccount` AS `InternalAccount`,`accounts`.`Type_id` AS `Type_id`,`accounts`.`NickName` AS `NickName`,`accounts`.`FavouriteStatus` AS `FavouriteStatus`,`accounttype`.`TypeDescription` AS `TypeDescription` from (((`accounts` join `externalbankidentity`) join `externalbank`) join `accounttype`) where ((`accounts`.`ExternalBankidentity_id` = `externalbankidentity`.`id`) and (`externalbankidentity`.`ExternalBank_id` = `externalbank`.`id`) and (`accounts`.`Type_id` = `accounttype`.`TypeID`)) ;

DROP VIEW IF EXISTS `bankbranchview`;
CREATE VIEW `bankbranchview` AS select `bb`.`id` AS `id`,`bb`.`address1` AS `address1`,`bb`.`address2` AS `address2`,`bb`.`city` AS `city`,`bb`.`state` AS `state`,`bb`.`zipCode` AS `zipCode`,`bb`.`phone` AS `phone`,`bb`.`workingHours` AS `workingHours`,`bb`.`services` AS `services`,`bb`.`latitude` AS `latitude`,`bb`.`longitude` AS `longitude`,`bb`.`status` AS `status`,`bb`.`email` AS `email`,`bb`.`Type_id` AS `Type_id`,`bt`.`Type` AS `Type` from (`bankbranch` `bb` join `branchtype` `bt`) where (`bb`.`Type_id` = `bt`.`id`) ;

DROP VIEW IF EXISTS `billermasterview`;
CREATE VIEW `billermasterview` AS select `bb`.`accountNumber` AS `accountNumber`,`bb`.`address` AS `address`,`bb`.`billerCategoryId` AS `billerCategoryId`,`bb`.`billerName` AS `billerName`,`bb`.`city` AS `city`,`bb`.`ebillSupport` AS `ebillSupport`,`bb`.`id` AS `id`,`bb`.`state` AS `state`,`bb`.`zipCode` AS `zipCode`,`bc`.`categoryName` AS `billerCategoryName` from (`billermaster` `bb` join `billercategory` `bc`) where (`bb`.`billerCategoryId` = `bc`.`id`) ;

DROP VIEW IF EXISTS `billview`;
CREATE VIEW `billview` AS select `bb`.`balanceAmount` AS `balanceAmount`,`bb`.`billDueDate` AS `billDueDate`,`bb`.`billGeneratedDate` AS `billGeneratedDate`,`bb`.`description` AS `description`,`bb`.`dueAmount` AS `dueAmount`,`bb`.`ebillURL` AS `ebillURL`,`bb`.`id` AS `id`,`bb`.`paidAmount` AS `paidAmount`,`bb`.`paidDate` AS `paidDate`,`bb`.`Payee_id` AS `payeeId`,`bb`.`currencyCode` AS `currencyCode`,`ac`.`AccountName` AS `fromAccountName`,`ac`.`Account_id` AS `fromAccountNumber`,`ac`.`User_id` AS `User_id`,`py`.`name` AS `payeeName`,`py`.`softDelete` AS `softDelete`,`py`.`eBillEnable` AS `ebillStatus`,`py`.`nickName` AS `payeeNickName`,`py`.`addressLine1` AS `payeeAddressLine1`,`bc`.`categoryName` AS `billerCategory`,`bm`.`billerCategoryId` AS `billerCategoryId`,`bm`.`ebillSupport` AS `ebillSupport` from ((((`bill` `bb` join `payee` `py`) join `accounts` `ac`) join `billercategory` `bc`) join `billermaster` `bm`) where ((`bb`.`billerMaster_id` = `bm`.`id`) and (`bb`.`Account_id` = `ac`.`Account_id`) and (`bm`.`billerCategoryId` = `bc`.`id`) and (`bb`.`Payee_id` = `py`.`Id`)) ;

DROP VIEW IF EXISTS `customerview`;
CREATE VIEW `customerview` AS select `c`.`id` AS `id`,`c`.`FirstName` AS `FirstName`,`c`.`LastName` AS `LastName`,`c`.`UserName` AS `UserName`,`cc`.`Value` AS `Value`,`ct`.`Description` AS `description` from ((`customercommunication` `cc` join `customer` `c`) join `communicationtype` `ct`) where ((`cc`.`Type_id` = `ct`.`id`) and (`cc`.`Customer_id` = `c`.`id`)) ;

DROP VIEW IF EXISTS `fromaccountransactionview`;
CREATE VIEW `fromaccountransactionview` AS select `accounts`.`Account_id` AS `Account_id`,`accounts`.`AccountName` AS `AccountName`,`accounts`.`NickName` AS `NickName`,`accounts`.`AccountHolder` AS `AccountHolder`,`accounts`.`UserName` AS `UserName`,`accounts`.`ExternalBankidentity_id` AS `ExternalBankidentity_id`,`accounts`.`CurrencyCode` AS `CurrencyCode`,`accounts`.`User_id` AS `User_id`,`accounts`.`AvailableBalance` AS `AvailableBalance`,`accounts`.`Bank_id` AS `Bank_id`,`accounts`.`ShowTransactions` AS `ShowTransactions`,`accounts`.`CurrentBalance` AS `CurrentBalance`,`accounts`.`SwiftCode` AS `SwiftCode`,`accounts`.`RoutingNumber` AS `RoutingNumber`,`bank`.`Description` AS `BankNmae`,`transaction`.`Id` AS `transactionId`,`transaction`.`Type_id` AS `transactiontype`,`transaction`.`Customer_id` AS `Customer_id`,`transaction`.`ExpenseCategory_id` AS `ExpenseCategory_id`,`transaction`.`Bill_id` AS `Bill_id`,`transaction`.`Reference_id` AS `Reference_id`,`transaction`.`fromAccountNumber` AS `fromAccountNumber`,`transaction`.`fromAccountBalance` AS `fromAccountBalance`,`transaction`.`toAccountNumber` AS `toAccountNumber`,`transaction`.`toAccountBalance` AS `toAccountBalance`,`transaction`.`amount` AS `amount`,`transaction`.`Status_id` AS `Status_id`,`transaction`.`statusDesc` AS `statusDesc`,`transaction`.`isScheduled` AS `isScheduled`,`transaction`.`category` AS `category`,`transaction`.`billCategory` AS `billCategory`,`transaction`.`toExternalAccountNumber` AS `ExternalAccountNumber`,`transaction`.`Person_Id` AS `Person_Id`,`transaction`.`frequencyType` AS `frequencyType`,`transaction`.`createdDate` AS `createdDate`,`transaction`.`cashlessEmail` AS `cashlessEmail`,`transaction`.`cashlessMode` AS `cashlessMode`,`transaction`.`cashlessOTP` AS `cashlessOTP`,`transaction`.`cashlessOTPValidDate` AS `cashlessOTPValidDate`,`transaction`.`cashlessPersonName` AS `cashlessPersonName`,`transaction`.`cashlessPhone` AS `cashlessPhone`,`transaction`.`cashlessSecurityCode` AS `cashlessSecurityCode`,`transaction`.`cashWithdrawalTransactionStatus` AS `cashWithdrawalTransactionStatus`,`transaction`.`frequencyEndDate` AS `frequencyEndDate`,`transaction`.`frequencyStartDate` AS `frequencyStartDate`,`transaction`.`hasDepositImage` AS `hasDepositImage`,`transaction`.`Payee_id` AS `payeeId`,`transaction`.`p2pContact` AS `p2pContact`,`transaction`.`Person_Id` AS `personId`,`transaction`.`recurrenceDesc` AS `recurrenceDesc`,`transaction`.`numberOfRecurrences` AS `numberOfRecurrences`,`transaction`.`scheduledDate` AS `scheduledDate`,`transaction`.`transactionComments` AS `transactionComments`,`transaction`.`notes` AS `transactionsNotes`,`transaction`.`description` AS `transDescription`,`transaction`.`transactionDate` AS `transactionDate`,`transactiontype`.`description` AS `description`,`accounttype`.`TypeDescription` AS `TypeDescription`,`transaction`.`IBAN` AS `IBAN`,`transaction`.`sortCode` AS `sortCode` from ((((`accounts` join `transaction`) join `transactiontype`) join `accounttype`) join `bank`) where ((`accounts`.`Account_id` = `transaction`.`fromAccountNumber`) and (`transaction`.`Type_id` = `transactiontype`.`Id`) and (`accounttype`.`TypeID` = `accounts`.`Type_id`) and (`bank`.`id` = `accounts`.`Bank_id`)) group by `transaction`.`Id` order by `transaction`.`createdDate` desc ;

DROP VIEW IF EXISTS `getaccountsview`;
CREATE VIEW `getaccountsview` AS select `accounts`.`Account_id` AS `Account_id`,`accounts`.`Type_id` AS `Type_id`,`accounts`.`UserName` AS `userName`,`accounts`.`CurrencyCode` AS `currencyCode`,`accounts`.`AccountHolder` AS `accountHolder`,`accounts`.`error` AS `error`,`accounts`.`Address` AS `Address`,`accounts`.`Scheme` AS `Scheme`,`accounts`.`Number` AS `number`,`accounts`.`AvailableBalance` AS `availableBalance`,`accounts`.`CurrentBalance` AS `currentBalance`,`accounts`.`InterestRate` AS `interestRate`,`accounts`.`AvailableCredit` AS `availableCredit`,`accounts`.`MinimumDue` AS `minimumDue`,`accounts`.`DueDate` AS `dueDate`,`accounts`.`FirstPaymentDate` AS `firstPaymentDate`,`accounts`.`ClosingDate` AS `closingDate`,`accounts`.`PaymentTerm` AS `paymentTerm`,`accounts`.`OpeningDate` AS `openingDate`,`accounts`.`MaturityDate` AS `maturityDate`,`accounts`.`DividendLastPaidAmount` AS `dividendLastPaidAmount`,`accounts`.`DividendLastPaidDate` AS `dividendLastPaidDate`,`accounts`.`DividendPaidYTD` AS `dividendPaidYTD`,`accounts`.`DividendRate` AS `dividendRate`,`accounts`.`DividendYTD` AS `dividendYTD`,`accounts`.`EStatementmentEnable` AS `eStatementEnable`,`accounts`.`FavouriteStatus` AS `favouriteStatus`,`accounts`.`StatusDesc` AS `statusDesc`,`accounts`.`NickName` AS `nickName`,`accounts`.`User_id` AS `User_id`,`accounts`.`OriginalAmount` AS `originalAmount`,`accounts`.`OutstandingBalance` AS `outstandingBalance`,`accounts`.`PaymentDue` AS `paymentDue`,`accounts`.`PaymentMethod` AS `paymentMethod`,`accounts`.`SwiftCode` AS `swiftCode`,`accounts`.`TotalCreditMonths` AS `totalCreditMonths`,`accounts`.`TotalDebitsMonth` AS `totalDebitsMonth`,`accounts`.`RoutingNumber` AS `routingNumber`,`accounts`.`SupportBillPay` AS `supportBillPay`,`accounts`.`SupportCardlessCash` AS `supportCardlessCash`,`accounts`.`SupportTransferFrom` AS `supportTransferFrom`,`accounts`.`SupportTransferTo` AS `supportTransferTo`,`accounts`.`SupportDeposit` AS `supportDeposit`,`accounts`.`UnpaidInterest` AS `unpaidInterest`,`accounts`.`PreviousYearsDividends` AS `previousYearsDividends`,`accounts`.`principalBalance` AS `principalBalance`,`accounts`.`PrincipalValue` AS `principalValue`,`accounts`.`RegularPaymentAmount` AS `regularPaymentAmount`,`accounts`.`phone` AS `phoneId`,`accounts`.`LastDividendPaidDate` AS `lastDividendPaidDate`,`accounts`.`LastDividendPaidAmount` AS `lastDividendPaidAmount`,`accounts`.`LastPaymentAmount` AS `lastPaymentAmount`,`accounts`.`LastPaymentDate` AS `lastPaymentDate`,`accounts`.`LastStatementBalance` AS `lastStatementBalance`,`accounts`.`LateFeesDue` AS `lateFeesDue`,`accounts`.`maturityAmount` AS `maturityAmount`,`accounts`.`MaturityOption` AS `maturityOption`,`accounts`.`payoffAmount` AS `payoffAmount`,`accounts`.`PayOffCharge` AS `payOffCharge`,`accounts`.`PendingDeposit` AS `pendingDeposit`,`accounts`.`PendingWithdrawal` AS `pendingWithdrawal`,`accounts`.`JointHolders` AS `jointHolders`,`accounts`.`IsPFM` AS `isPFM`,`accounts`.`InterestPaidYTD` AS `interestPaidYTD`,`accounts`.`InterestPaidPreviousYTD` AS `interestPaidPreviousYTD`,`accounts`.`InterestPaidLastYear` AS `interestPaidLastYear`,`accounts`.`InterestEarned` AS `interestEarned`,`accounts`.`CurrentAmountDue` AS `currentAmountDue`,`accounts`.`CreditLimit` AS `creditLimit`,`accounts`.`CreditCardNumber` AS `creditCardNumber`,`accounts`.`BsbNum` AS `bsbNum`,`accounts`.`BondInterestLastYear` AS `bondInterestLastYear`,`accounts`.`BondInterest` AS `bondInterest`,`accounts`.`AvailablePoints` AS `availablePoints`,`accounts`.`AccountName` AS `accountName`,`accounts`.`email` AS `email`,`accounts`.`IBAN` AS `IBAN`,`accounts`.`adminProductId` AS `adminProductId`,`bank`.`Description` AS `bankname`,`accounts`.`AccountPreference` AS `accountPreference`,`accounttype`.`transactionLimit` AS `transactionLimit`,`accounttype`.`transferLimit` AS `transferLimit`,`accounttype`.`rates` AS `rates`,`accounttype`.`termsAndConditions` AS `termsAndConditions`,`accounttype`.`TypeDescription` AS `typeDescription`,`accounttype`.`supportChecks` AS `supportChecks`,`accounttype`.`displayName` AS `displayName`,`accounts`.`accountSubType` AS `accountSubType`,`accounts`.`description` AS `description`,`accounts`.`schemeName` AS `schemeName`,`accounts`.`identification` AS `identification`,`accounts`.`secondaryIdentification` AS `secondaryIdentification`,`accounts`.`servicerSchemeName` AS `servicerSchemeName`,`accounts`.`servicerIdentification` AS `servicerIdentification`,`accounts`.`dataCreditDebitIndicator` AS `dataCreditDebitIndicator`,`accounts`.`dataType` AS `dataType`,`accounts`.`dataDateTime` AS `dataDateTime`,`accounts`.`dataCreditLineIncluded` AS `dataCreditLineIncluded`,`accounts`.`dataCreditLineType` AS `dataCreditLineType`,`accounts`.`dataCreditLineAmount` AS `dataCreditLineAmount`,`accounts`.`dataCreditLineCurrency` AS `dataCreditLineCurrency` from ((`accounts` join `accounttype`) join `bank`) where ((`accounts`.`Type_id` = `accounttype`.`TypeID`) and (`accounts`.`Bank_id` = `bank`.`id`)) ;

DROP VIEW IF EXISTS `messageview`;
CREATE VIEW `messageview` AS select `msg`.`id` AS `id`,`msg`.`Account_id` AS `Account_id`,`acnts`.`AccountName` AS `accountName`,`acnts`.`NickName` AS `nickName`,`msg`.`Category_id` AS `Category_id`,`msgcategory`.`category` AS `category`,`msg`.`Subcategory_id` AS `Subcategory_id`,`msgsubcategory`.`subcategory` AS `subcategory`,`msg`.`subject` AS `subject`,`msg`.`message` AS `message`,`msg`.`sentDate` AS `sentDate`,`msg`.`status` AS `status`,`msg`.`isSoftDeleted` AS `isSoftDeleted`,`msg`.`isRead` AS `isRead`,`msg`.`createdDate` AS `createdDate`,`msg`.`receivedDate` AS `receivedDate`,`msg`.`softdeletedDate` AS `softdeletedDate`,`acnts`.`User_id` AS `User_id` from (((`message` `msg` join `accounts` `acnts`) join `messagecategory` `msgcategory`) join `messagesubcategory` `msgsubcategory`) where ((`msg`.`Account_id` = `acnts`.`Account_id`) and (`msgcategory`.`Id` = `msg`.`Category_id`) and (`msgsubcategory`.`Id` = `msg`.`Subcategory_id`)) ;

DROP VIEW IF EXISTS `notificationview`;
CREATE VIEW `notificationview` AS select `notification`.`notificationId` AS `notificationId`,`notification`.`imageURL` AS `imageURL`,`usernotification`.`isRead` AS `isRead`,`notification`.`notificationActionLink` AS `notificationActionLink`,`notification`.`notificationModule` AS `notificationModule`,`notification`.`notificationSubject` AS `notificationSubject`,`notification`.`notificationSubModule` AS `notificationSubModule`,`notification`.`notificationText` AS `notificationText`,`usernotification`.`receivedDate` AS `receivedDate`,`usernotification`.`id` AS `userNotificationId`,`usernotification`.`user_id` AS `user_id` from (`usernotification` left join `notification` on((`notification`.`notificationId` = `usernotification`.`notification_id`))) ;

DROP VIEW IF EXISTS `organisationview`;
CREATE VIEW `organisationview` AS select `organisation`.`id` AS `org_id`,`organisation`.`Name` AS `org_Name`,`organisationmembership`.`Organization_id` AS `orgmem_orgid`,`organisationmembership`.`Membership_id` AS `orgmem_memid`,`organisationcommunication`.`id` AS `orgcomm_id`,`organisationcommunication`.`Sequence` AS `orgcomm_Sequence`,`organisationcommunication`.`Value` AS `orgcomm_Value`,`organisationcommunication`.`Extension` AS `orgcomm_Extension`,`organisationcommunication`.`Description` AS `orgcomm_Description`,`organisationcommunication`.`IsPreferredContactMethod` AS `orgcomm_IsPreferredContactMethod`,`organisationcommunication`.`softdeleteflag` AS `orgcomm_softdeleteflag`,`organisationcommunication`.`Type_id` AS `orgcomm_Typeid`,`organisationcommunication`.`Organization_id` AS `orgcomm_orgid`,`organisationmembership`.`Taxid` AS `orgmem_taxid` from ((`organisation` join `organisationmembership`) join `organisationcommunication`) where ((`organisation`.`id` = `organisationmembership`.`Organization_id`) and (`organisation`.`id` = `organisationcommunication`.`Organization_id`)) ;

DROP VIEW IF EXISTS `payeetransactionview`;
CREATE VIEW `payeetransactionview` AS select `t`.`Id` AS `Id`,`t`.`isScheduled` AS `isScheduled`,`t`.`Customer_id` AS `Customer_id`,`t`.`ExpenseCategory_id` AS `ExpenseCategory_id`,`t`.`Payee_id` AS `Payee_id`,`t`.`Bill_id` AS `Bill_id`,`t`.`Type_id` AS `Type_id`,`t`.`Reference_id` AS `Reference_id`,`t`.`fromAccountNumber` AS `fromAccountNumber`,`t`.`fromAccountBalance` AS `fromAccountBalance`,`t`.`toAccountNumber` AS `toAccountNumber`,`t`.`toAccountBalance` AS `toAccountBalance`,`t`.`amount` AS `amount`,`t`.`Status_id` AS `Status_id`,`t`.`statusDesc` AS `statusDesc`,`t`.`notes` AS `notes`,`t`.`checkNumber` AS `checkNumber`,`t`.`imageURL1` AS `imageURL1`,`t`.`imageURL2` AS `imageURL2`,`t`.`hasDepositImage` AS `hasDepositImage`,`t`.`description` AS `description`,`t`.`scheduledDate` AS `scheduledDate`,`t`.`transactionDate` AS `transactionDate`,`t`.`createdDate` AS `createdDate`,`t`.`transactionComments` AS `transactionComments`,`t`.`toExternalAccountNumber` AS `toExternalAccountNumber`,`t`.`Person_Id` AS `Person_Id`,`t`.`frequencyType` AS `frequencyType`,`t`.`numberOfRecurrences` AS `numberOfRecurrences`,`t`.`frequencyStartDate` AS `frequencyStartDate`,`t`.`frequencyEndDate` AS `frequencyEndDate`,`t`.`checkImage` AS `checkImage`,`t`.`checkImageBack` AS `checkImageBack`,`t`.`cashlessOTPValidDate` AS `cashlessOTPValidDate`,`t`.`cashlessOTP` AS `cashlessOTP`,`t`.`cashlessPhone` AS `cashlessPhone`,`t`.`cashlessEmail` AS `cashlessEmail`,`t`.`cashlessPersonName` AS `cashlessPersonName`,`t`.`cashlessMode` AS `cashlessMode`,`t`.`cashlessSecurityCode` AS `cashlessSecurityCode`,`t`.`cashWithdrawalTransactionStatus` AS `cashWithdrawalTransactionStatus`,`t`.`cashlessPin` AS `cashlessPin`,`t`.`category` AS `category`,`t`.`billCategory` AS `billCategory`,`t`.`recurrenceDesc` AS `recurrenceDesc`,`t`.`deliverBy` AS `deliverBy`,`t`.`p2pContact` AS `p2pContact`,`t`.`p2pRequiredDate` AS `p2pRequiredDate`,`t`.`requestCreatedDate` AS `requestCreatedDate`,`t`.`penaltyFlag` AS `penaltyFlag`,`t`.`payoffFlag` AS `payoffFlag`,`t`.`viewReportLink` AS `viewReportLink`,`t`.`isPaypersonDeleted` AS `isPaypersonDeleted`,`t`.`fee` AS `fee`,`p`.`Id` AS `payeeId`,`p`.`Type_id` AS `payeeType`,`p`.`name` AS `name`,`p`.`nickName` AS `nickName`,`p`.`phone` AS `phone`,`p`.`email` AS `email`,`p`.`accountNumber` AS `accountNumber`,`p`.`billerId` AS `billerId`,`p`.`billermaster_id` AS `billermaster_id`,`p`.`softDelete` AS `softDelete`,`p`.`User_Id` AS `User_Id`,`p`.`addressLine1` AS `addressLine1`,`p`.`addressLine2` AS `addressLine2`,`p`.`eBillEnable` AS `eBillEnable`,`p`.`phoneExtension` AS `phoneExtension`,`p`.`phoneCountryCode` AS `phoneCountryCode`,`bm`.`ebillSupport` AS `ebillSupport`,`tt`.`description` AS `transactionType` from (((`transaction` `t` left join `payee` `p` on((`t`.`Payee_id` = `p`.`Id`))) join `transactiontype` `tt` on((`t`.`Type_id` = `tt`.`Id`))) left join `billermaster` `bm` on((`bm`.`id` = `p`.`billermaster_id`))) ;

DROP VIEW IF EXISTS `pfmbudgetsnapshotview`;
CREATE VIEW `pfmbudgetsnapshotview` AS select `pfmbudgetsnapshot`.`allocatedAmount` AS `allocatedAmount`,`pfmbudgetsnapshot`.`amountSpent` AS `amountSpent`,`pfmbudgetsnapshot`.`id` AS `budgetId`,`pfmbudgetsnapshot`.`Category_Id` AS `categoryId`,`pfmcategory`.`categoryName` AS `categoryName` from (`pfmbudgetsnapshot` join `pfmcategory`) where (`pfmbudgetsnapshot`.`Category_Id` = `pfmcategory`.`id`) ;

DROP VIEW IF EXISTS `pfmpiechartview`;
CREATE VIEW `pfmpiechartview` AS select `pfmpiechart`.`cashSpent` AS `cashSpent`,`pfmpiechart`.`monthId` AS `monthId`,`pfmpiechart`.`year` AS `year`,`pfmpiechart`.`categoryId` AS `categoryId`,`pfmmonth`.`monthName` AS `monthName`,`pfmcategory`.`categoryName` AS `categoryName` from ((`pfmpiechart` join `pfmmonth`) join `pfmcategory`) where ((`pfmpiechart`.`monthId` = `pfmmonth`.`id`) and (`pfmpiechart`.`categoryId` = `pfmcategory`.`id`)) ;

DROP VIEW IF EXISTS `toaccountransactionview`;
CREATE VIEW `toaccountransactionview` AS select `accounts`.`Account_id` AS `Account_id`,`accounts`.`AccountName` AS `AccountName`,`accounts`.`NickName` AS `NickName`,`accounts`.`AccountHolder` AS `AccountHolder`,`accounts`.`UserName` AS `UserName`,`accounts`.`ExternalBankidentity_id` AS `ExternalBankidentity_id`,`accounts`.`CurrencyCode` AS `CurrencyCode`,`accounts`.`User_id` AS `User_id`,`accounts`.`AvailableBalance` AS `AvailableBalance`,`accounts`.`Bank_id` AS `Bank_id`,`accounts`.`ShowTransactions` AS `ShowTransactions`,`accounts`.`CurrentBalance` AS `CurrentBalance`,`accounts`.`SwiftCode` AS `SwiftCode`,`accounts`.`RoutingNumber` AS `RoutingNumber`,`transaction`.`Id` AS `transactionId`,`transaction`.`Type_id` AS `transactiontype`,`transaction`.`Customer_id` AS `Customer_id`,`transaction`.`ExpenseCategory_id` AS `ExpenseCategory_id`,`transaction`.`Bill_id` AS `Bill_id`,`transaction`.`Reference_id` AS `Reference_id`,`transaction`.`fromAccountNumber` AS `fromAccountNumber`,`transaction`.`fromAccountBalance` AS `fromAccountBalance`,`transaction`.`toAccountNumber` AS `toAccountNumber`,`transaction`.`toAccountBalance` AS `toAccountBalance`,`transaction`.`amount` AS `amount`,`transaction`.`Status_id` AS `Status_id`,`transaction`.`statusDesc` AS `statusDesc`,`transaction`.`isScheduled` AS `isScheduled`,`transaction`.`category` AS `category`,`transaction`.`billCategory` AS `billCategory`,`transaction`.`toExternalAccountNumber` AS `ExternalAccountNumber`,`transaction`.`Person_Id` AS `Person_Id`,`transaction`.`frequencyType` AS `frequencyType`,`transaction`.`createdDate` AS `createdDate`,`transaction`.`cashlessEmail` AS `cashlessEmail`,`transaction`.`cashlessMode` AS `cashlessMode`,`transaction`.`cashlessOTP` AS `cashlessOTP`,`transaction`.`cashlessOTPValidDate` AS `cashlessOTPValidDate`,`transaction`.`cashlessPersonName` AS `cashlessPersonName`,`transaction`.`cashlessPhone` AS `cashlessPhone`,`transaction`.`cashlessSecurityCode` AS `cashlessSecurityCode`,`transaction`.`cashWithdrawalTransactionStatus` AS `cashWithdrawalTransactionStatus`,`transaction`.`frequencyEndDate` AS `frequencyEndDate`,`transaction`.`frequencyStartDate` AS `frequencyStartDate`,`transaction`.`hasDepositImage` AS `hasDepositImage`,`transaction`.`Payee_id` AS `payeeId`,`transaction`.`p2pContact` AS `p2pContact`,`transaction`.`Person_Id` AS `personId`,`transaction`.`recurrenceDesc` AS `recurrenceDesc`,`transaction`.`numberOfRecurrences` AS `numberOfRecurrences`,`transaction`.`scheduledDate` AS `scheduledDate`,`transaction`.`transactionComments` AS `transactionComments`,`transaction`.`notes` AS `transactionsNotes`,`transaction`.`description` AS `transDescription`,`transaction`.`transactionDate` AS `transactionDate`,`transactiontype`.`description` AS `description`,`accounttype`.`TypeDescription` AS `TypeDescription`,`transaction`.`IBAN` AS `IBAN`,`transaction`.`sortCode` AS `sortCode` from (((`accounts` join `transaction`) join `transactiontype`) join `accounttype`) where ((`accounts`.`Account_id` = `transaction`.`toAccountNumber`) and (`transaction`.`Type_id` = `transactiontype`.`Id`) and (`accounttype`.`TypeID` = `accounts`.`Type_id`)) group by `transaction`.`Id` order by `transaction`.`createdDate` desc ;

DROP VIEW IF EXISTS `userbanksview`;
CREATE VIEW `userbanksview` AS select `externalbankidentity`.`User_id` AS `User_id`,`externalbankidentity`.`MainUser_id` AS `MainUser_id`,`externalbank`.`id` AS `id`,`externalbank`.`BankName` AS `BankName`,`externalbank`.`BankId` AS `BankId` from (`externalbank` join `externalbankidentity`) where (`externalbank`.`id` = `externalbankidentity`.`ExternalBank_id`) ;

DROP VIEW IF EXISTS `wireaccounttransactionview`;
CREATE VIEW `wireaccounttransactionview` AS select `accounts`.`Account_id` AS `Account_id`,`accounts`.`AccountName` AS `AccountName`,`accounts`.`AccountHolder` AS `AccountHolder`,`accounts`.`UserName` AS `UserName`,`accounts`.`ExternalBankidentity_id` AS `ExternalBankidentity_id`,`accounts`.`CurrencyCode` AS `CurrencyCode`,`accounts`.`User_id` AS `User_id`,`accounts`.`AvailableBalance` AS `AvailableBalance`,`accounts`.`Bank_id` AS `Bank_id`,`accounts`.`ShowTransactions` AS `ShowTransactions`,`accounts`.`CurrentBalance` AS `CurrentBalance`,`accounts`.`SwiftCode` AS `SwiftCode`,`accounts`.`RoutingNumber` AS `RoutingNumber`,`transaction`.`Id` AS `transactionId`,`transaction`.`Type_id` AS `transactiontype`,`transaction`.`Customer_id` AS `Customer_id`,`transaction`.`ExpenseCategory_id` AS `ExpenseCategory_id`,`transaction`.`billid` AS `Bill_id`,`transaction`.`Reference_id` AS `Reference_id`,`transaction`.`fromAccountNumber` AS `fromAccountNumber`,`transaction`.`fromAccountBalance` AS `fromAccountBalance`,`transaction`.`toAccountNumber` AS `toAccountNumber`,`transaction`.`toAccountBalance` AS `toAccountBalance`,`transaction`.`amount` AS `amount`,`transaction`.`Status_id` AS `Status_id`,`transaction`.`statusDesc` AS `statusDesc`,`transaction`.`isScheduled` AS `isScheduled`,`transaction`.`category` AS `category`,`transaction`.`billCategory` AS `billCategory`,`transaction`.`toExternalAccountNumber` AS `ExternalAccountNumber`,`transaction`.`Person_Id` AS `Person_Id`,`transaction`.`frequencyType` AS `frequencyType`,`transaction`.`createdDate` AS `createdDate`,`transaction`.`cashlessEmail` AS `cashlessEmail`,`transaction`.`cashlessMode` AS `cashlessMode`,`transaction`.`cashlessOTP` AS `cashlessOTP`,`transaction`.`cashlessOTPValidDate` AS `cashlessOTPValidDate`,`transaction`.`cashlessPersonName` AS `cashlessPersonName`,`transaction`.`cashlessPhone` AS `cashlessPhone`,`transaction`.`cashlessSecurityCode` AS `cashlessSecurityCode`,`transaction`.`cashWithdrawalTransactionStatus` AS `cashWithdrawalTransactionStatus`,`transaction`.`frequencyEndDate` AS `frequencyEndDate`,`transaction`.`frequencyStartDate` AS `frequencyStartDate`,`transaction`.`hasDepositImage` AS `hasDepositImage`,`transaction`.`Payee_id` AS `payeeId`,`transaction`.`payeeName` AS `payeeName`,`transaction`.`p2pContact` AS `p2pContact`,`transaction`.`Person_Id` AS `personId`,`transaction`.`recurrenceDesc` AS `recurrenceDesc`,`transaction`.`numberOfRecurrences` AS `numberOfRecurrences`,`transaction`.`scheduledDate` AS `scheduledDate`,`transaction`.`transactionComments` AS `transactionComments`,`transaction`.`notes` AS `transactionsNotes`,`transaction`.`description` AS `transDescription`,`transaction`.`transactionDate` AS `transactionDate`,`transaction`.`frontImage1` AS `frontImage1`,`transaction`.`frontImage2` AS `frontImage2`,`transaction`.`backImage1` AS `backImage1`,`transaction`.`backImage2` AS `backImage2`,`transaction`.`checkDesc` AS `checkDesc`,`transaction`.`checkNumber1` AS `checkNumber1`,`transaction`.`checkNumber2` AS `checkNumber2`,`transaction`.`checkNumber` AS `checkNumber`,`transaction`.`checkReason` AS `checkReason`,`transaction`.`requestValidity` AS `requestValidity`,`transaction`.`checkDateOfIssue` AS `checkDateOfIssue`,`transaction`.`bankName1` AS `bankName1`,`transaction`.`bankName2` AS `bankName2`,`transaction`.`withdrawlAmount1` AS `withdrawlAmount1`,`transaction`.`withdrawlAmount2` AS `withdrawlAmount2`,`transaction`.`cashAmount` AS `cashAmount`,`transaction`.`amountRecieved` AS `amountRecieved`,`transaction`.`payeeCurrency` AS `payeeCurrency`,`transaction`.`fee` AS `fee`,`transaction`.`isDisputed` AS `isDisputed`,`transaction`.`disputeReason` AS `disputeReason`,`transaction`.`disputeDescription` AS `disputeDescription`,`transaction`.`disputeDate` AS `disputeDate`,`transaction`.`disputeStatus` AS `disputeStatus`,`transactiontype`.`description` AS `description`,`payee`.`nickName` AS `nickName` from (((`accounts` join `transaction`) join `transactiontype`) join `payee`) where (((`accounts`.`Account_id` = `transaction`.`fromAccountNumber`) or (`accounts`.`Account_id` = `transaction`.`toAccountNumber`)) and (`transaction`.`Type_id` = `transactiontype`.`Id`) and (`transaction`.`Payee_id` = `payee`.`Id`)) group by `transaction`.`Id` order by `transaction`.`createdDate` desc ;

/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;

ALTER TABLE `customer` 
ADD COLUMN `IDType_id` VARCHAR(50) NULL AFTER `Lastlogintime`,
ADD COLUMN `IDValue` VARCHAR(50) NULL AFTER `IDType_id`,
ADD COLUMN `IsCoreIdentityScope` VARCHAR(50) NULL AFTER `IDValue`,
ADD COLUMN `FullName` VARCHAR(50) NULL AFTER `IsCoreIdentityScope`,
ADD COLUMN `CustomerType_id` VARCHAR(50) NULL AFTER `FullName`,
ADD COLUMN `CustomerType` VARCHAR(45) NULL AFTER `CustomerType_id`,
ADD COLUMN `Status` VARCHAR(45) NULL AFTER `CustomerType`,
ADD COLUMN `Phone` VARCHAR(15) NULL AFTER `Status`,
ADD COLUMN `Is_MemberEligibile` TINYINT(1) NULL AFTER `Phone`,
ADD COLUMN `MemberEligibilityData` VARCHAR(100) NULL AFTER `Is_MemberEligibile`,
ADD COLUMN `Is_BBOA` TINYINT(1) NULL AFTER `MemberEligibilityData`,
ADD COLUMN `CreditUnionMemberSince` DATE NULL AFTER `Is_BBOA`,
ADD COLUMN `AtionProfile_id` VARCHAR(50) NULL AFTER `CreditUnionMemberSince`,
ADD COLUMN `RegistrationLink` VARCHAR(200) NULL AFTER `AtionProfile_id`,
ADD COLUMN `RegLinkResendCount` INT(11) NULL AFTER `RegistrationLink`,
ADD COLUMN `RegLinkValidity` TIMESTAMP NULL AFTER `RegLinkResendCount`,
ADD COLUMN `areDepositTermsAccepted` VARCHAR(50) NULL AFTER `RegLinkValidity`,
ADD COLUMN `areAccountStatementTermsAccepted` VARCHAR(50) NULL AFTER `areDepositTermsAccepted`,
ADD COLUMN `areUserAlertsTurnedOn` VARCHAR(50) NULL AFTER `areAccountStatementTermsAccepted`,
ADD COLUMN `isBillPaySupported` VARCHAR(50) NULL AFTER `areUserAlertsTurnedOn`,
ADD COLUMN `isBillPayActivated` VARCHAR(50) NULL AFTER `isBillPaySupported`,
ADD COLUMN `isP2PSupported` VARCHAR(50) NULL AFTER `isBillPayActivated`,
ADD COLUMN `isP2PActivated` VARCHAR(50) NULL AFTER `isP2PSupported`,
ADD COLUMN `isWireTransferEligible` VARCHAR(50) NULL AFTER `isP2PActivated`,
ADD COLUMN `isWireTransferActivated` VARCHAR(50) NULL AFTER `isWireTransferEligible`,
ADD COLUMN `Email` VARCHAR(50) NULL AFTER `isWireTransferActivated`;

ALTER TABLE `customer` 
CHANGE COLUMN `CustomerType_id` `CustomerType_id` VARCHAR(50) NULL DEFAULT NULL AFTER `Classification_id`,
CHANGE COLUMN `CustomerType` `CustomerType` VARCHAR(45) NULL DEFAULT NULL AFTER `CustomerType_id`,
CHANGE COLUMN `FullName` `FullName` VARCHAR(50) NULL DEFAULT NULL AFTER `LastName`,
CHANGE COLUMN `Status` `Status` VARCHAR(45) NULL DEFAULT NULL AFTER `Status_id`,
CHANGE COLUMN `Email` `Email` VARCHAR(50) NULL DEFAULT NULL AFTER `PreferredContactTime`,
CHANGE COLUMN `Phone` `Phone` VARCHAR(15) NULL DEFAULT NULL AFTER `Email`,
ADD INDEX `FK_Customer_CustomerType_idx` (`CustomerType_id` ASC),
ADD INDEX `FK_Customer_IDType_idx` (`IDType_id` ASC);

ALTER TABLE `customer` 
ADD COLUMN `lockedOn` TIMESTAMP NULL AFTER `isWireTransferActivated`;

ALTER TABLE `customer` 
ADD COLUMN `Organization_Id` VARCHAR(50) NULL AFTER `LockCount`;

DROP VIEW IF EXISTS `customerorganisationmembershipview`;
CREATE VIEW `customerorganisationmembershipview` AS select `customer`.`LastName` AS `LastName`,`customer`.`DateOfBirth` AS `DateOfBirth`,`customer`.`Ssn` AS `Ssn`,`customer`.`Phone` AS `Phone`,`customer`.`Email` AS `Email`,`customer`.`UserName` AS `UserName`,`customer`.`Gender` AS `Gender`,`customer`.`id` AS `id`,`customer`.`FirstName` AS `FirstName`,`customer`.`CustomerType` AS `CustomerType`,`customer`.`Organization_Id` AS `Organization_Id`,`organisationmembership`.`Membership_id` AS `Membership_id`,`organisationmembership`.`Taxid` AS `Taxid` from ((`customer` join `organisationemployees`) join `organisationmembership`) where (((`customer`.`Organization_Id` = `organisationemployees`.`Organization_id`) or (`customer`.`id` = `organisationemployees`.`Customer_id`)) and (`organisationemployees`.`Organization_id` = `organisationmembership`.`Organization_id`)) ;

DROP VIEW IF EXISTS `organizationownerview`;
CREATE VIEW `organizationownerview` AS select `organisationowner`.`LastName` AS `LastName`,`organisationowner`.`DateOfBirth` AS `DateOfBirth`,`organisationowner`.`MidleName` AS `Ssn`,`organisationowner`.`Phone` AS `Phone`,`organisationowner`.`Email` AS `Email`,`organisationowner`.`FirstName` AS `UserName`,`organisationowner`.`IDType_id` AS `IDType_id`,`organisationowner`.`IdValue` AS `IdValue`,`organisationowner`.`Organization_id` AS `Organization_Id`,`organisationmembership`.`Membership_id` AS `Membership_id`,`organisationmembership`.`Taxid` AS `Taxid` from (`organisationowner` join `organisationmembership`) where (`organisationowner`.`Organization_id` = `organisationmembership`.`Organization_id`) ;
