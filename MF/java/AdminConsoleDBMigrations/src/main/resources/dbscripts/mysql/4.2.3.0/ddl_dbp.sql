DROP VIEW IF EXISTS `wireaccounttransactionview`;
CREATE VIEW `wireaccounttransactionview` AS SELECT   `accounts`.`Account_id` AS `Account_id`,  `accounts`.`AccountName` AS `AccountName`,  `accounts`.`AccountHolder` AS `AccountHolder`,  `accounts`.`UserName` AS `UserName`,  `accounts`.`ExternalBankidentity_id` AS `ExternalBankidentity_id`,  `accounts`.`CurrencyCode` AS `CurrencyCode`,  `accounts`.`User_id` AS `User_id`,  `accounts`.`AvailableBalance` AS `AvailableBalance`,  `accounts`.`Bank_id` AS `Bank_id`,  `accounts`.`ShowTransactions` AS `ShowTransactions`,  `accounts`.`CurrentBalance` AS `CurrentBalance`,  `accounts`.`SwiftCode` AS `SwiftCode`,  `accounts`.`RoutingNumber` AS `RoutingNumber`,  `transaction`.`Id` AS `transactionId`,  `transaction`.`Type_id` AS `transactiontype`,  `transaction`.`Customer_id` AS `Customer_id`,  `transaction`.`ExpenseCategory_id` AS `ExpenseCategory_id`,  `transaction`.`billid` AS `Bill_id`,  `transaction`.`Reference_id` AS `Reference_id`,  `transaction`.`fromAccountNumber` AS `fromAccountNumber`,  `transaction`.`fromAccountBalance` AS `fromAccountBalance`,  `transaction`.`toAccountNumber` AS `toAccountNumber`,  `transaction`.`toAccountBalance` AS `toAccountBalance`,  `transaction`.`amount` AS `amount`,  `transaction`.`Status_id` AS `Status_id`,  `transaction`.`statusDesc` AS `statusDesc`,  `transaction`.`isScheduled` AS `isScheduled`,  `transaction`.`category` AS `category`,  `transaction`.`billCategory` AS `billCategory`,  `transaction`.`toExternalAccountNumber` AS `ExternalAccountNumber`,  `transaction`.`Person_Id` AS `Person_Id`,  `transaction`.`frequencyType` AS `frequencyType`,  `transaction`.`createdDate` AS `createdDate`,  `transaction`.`cashlessEmail` AS `cashlessEmail`,  `transaction`.`cashlessMode` AS `cashlessMode`,  `transaction`.`cashlessOTP` AS `cashlessOTP`,  `transaction`.`cashlessOTPValidDate` AS `cashlessOTPValidDate`,  `transaction`.`cashlessPersonName` AS `cashlessPersonName`,  `transaction`.`cashlessPhone` AS `cashlessPhone`,  `transaction`.`cashlessSecurityCode` AS `cashlessSecurityCode`,  `transaction`.`cashWithdrawalTransactionStatus` AS `cashWithdrawalTransactionStatus`,  `transaction`.`frequencyEndDate` AS `frequencyEndDate`,  `transaction`.`frequencyStartDate` AS `frequencyStartDate`,  `transaction`.`hasDepositImage` AS `hasDepositImage`,  `transaction`.`Payee_id` AS `payeeId`,  `transaction`.`payeeName` AS `payeeName`,  `transaction`.`p2pContact` AS `p2pContact`,  `transaction`.`Person_Id` AS `personId`,  `transaction`.`recurrenceDesc` AS `recurrenceDesc`,  `transaction`.`numberOfRecurrences` AS `numberOfRecurrences`,  `transaction`.`scheduledDate` AS `scheduledDate`,  `transaction`.`transactionComments` AS `transactionComments`,  `transaction`.`notes` AS `transactionsNotes`,  `transaction`.`description` AS `transDescription`,  `transaction`.`transactionDate` AS `transactionDate`,  `transaction`.`frontImage1` AS `frontImage1`,  `transaction`.`frontImage2` AS `frontImage2`,  `transaction`.`backImage1` AS `backImage1`,  `transaction`.`backImage2` AS `backImage2`,  `transaction`.`checkDesc` AS `checkDesc`,  `transaction`.`checkNumber1` AS `checkNumber1`,  `transaction`.`checkNumber2` AS `checkNumber2`,  `transaction`.`checkNumber` AS `checkNumber`,  `transaction`.`checkReason` AS `checkReason`,  `transaction`.`requestValidity` AS `requestValidity`,  `transaction`.`checkDateOfIssue` AS `checkDateOfIssue`,  `transaction`.`bankName1` AS `bankName1`,  `transaction`.`bankName2` AS `bankName2`,  `transaction`.`withdrawlAmount1` AS `withdrawlAmount1`,  `transaction`.`withdrawlAmount2` AS `withdrawlAmount2`,  `transaction`.`cashAmount` AS `cashAmount`,  `transaction`.`amountRecieved` AS `amountRecieved`,  `transaction`.`payeeCurrency` AS `payeeCurrency`,  `transaction`.`fee` AS `fee`,  `transaction`.`isDisputed` AS `isDisputed`,  `transaction`.`disputeReason` AS `disputeReason`,  `transaction`.`disputeDescription` AS `disputeDescription`,  `transaction`.`disputeDate` AS `disputeDate`,  `transaction`.`disputeStatus` AS `disputeStatus`,  `transactiontype`.`description` AS `description`,  `payee`.`nickName` AS `nickName`,  `payee`.`accountNumber` AS `payeeAccountNumber`,  `payee`.`Type_id` AS `payeeType`,  `payee`.`addressLine1` AS `payeeAddressLine2`,  `payee`.`addressLine2` AS `payeeAddressLine1`,  `accounts`.`IBAN` AS `iban` FROM  (((`accounts`  JOIN `transaction`)  JOIN `transactiontype`)  JOIN `payee`) WHERE  (((`accounts`.`Account_id` = `transaction`.`fromAccountNumber`)   OR (`accounts`.`Account_id` = `transaction`.`toAccountNumber`))   AND (`transaction`.`Type_id` = `transactiontype`.`Id`)   AND (`transaction`.`Payee_id` = `payee`.`Id`)) GROUP BY `transaction`.`Id` ORDER BY `transaction`.`createdDate` DESC;

ALTER TABLE `bbtaxtype` CHANGE COLUMN `taxType` `taxType` VARCHAR(300) NOT NULL ;
ALTER TABLE `bbtaxsubtype` CHANGE COLUMN `taxSubType` `taxSubType` VARCHAR(300) NOT NULL ;

DROP TABLE IF EXISTS `locale`;
CREATE TABLE `locale` (
  `Code` varchar(10) NOT NULL,
  `Language` varchar(300) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`Code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `dbxAlertCategory`;
DROP TABLE IF EXISTS `dbxalertcategory`;
CREATE TABLE `dbxalertcategory` (
  `id` varchar(255) NOT NULL,
  `Name` varchar(255) DEFAULT NULL,
  `accountLevel` tinyint(1) DEFAULT NULL,
  `status_id` varchar(100) DEFAULT NULL,
  `DisplaySequence` int(11) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_alertcategory_status_idx` (`status_id`),
  CONSTRAINT `FK_alertcategory_status` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `alertCondition`;
DROP TABLE IF EXISTS `alertcondition`;
CREATE TABLE `alertcondition` (
  `id` varchar(255) NOT NULL,
  `Name` varchar(255) DEFAULT NULL,
  `LanguageCode` varchar(10) NOT NULL,
  `NoOfFields` int(1) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`,`LanguageCode`),
  KEY `FK_alertcondition_locale_idx` (`LanguageCode`),
  CONSTRAINT `FK_alertcondition_locale` FOREIGN KEY (`LanguageCode`) REFERENCES `locale` (`Code`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `alertAttribute`;
DROP TABLE IF EXISTS `alertattribute`;
CREATE TABLE `alertattribute` (
  `id` varchar(255) NOT NULL,
  `LanguageCode` varchar(10) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`,`LanguageCode`),
  KEY `FK_alertattribute_locale_idx` (`LanguageCode`),
  CONSTRAINT `FK_alertattribute_locale` FOREIGN KEY (`LanguageCode`) REFERENCES `locale` (`Code`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `dbxAlertType`;
DROP TABLE IF EXISTS `dbxalerttype`;
CREATE TABLE `dbxalerttype` (
  `id` varchar(255) NOT NULL,
  `Name` varchar(255) DEFAULT NULL,
  `AlertCategoryId` varchar(255) NOT NULL,
  `AttributeId` varchar(100) DEFAULT NULL,
  `AlertConditionId` varchar(255) DEFAULT NULL,
  `Value1` varchar(255) DEFAULT NULL,
  `Value2` varchar(255) DEFAULT NULL,
  `Status_id` varchar(255) DEFAULT NULL,
  `IsGlobal` tinyint(4) DEFAULT NULL,
  `DisplaySequence` int(11) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_alerttype_alertcategory_idx` (`AlertCategoryId`),
  KEY `FK_alerttype_alertattribute_idx` (`AttributeId`),
  KEY `FK_alerttype_alertcondition_idx` (`AlertConditionId`),
  KEY `FK_alerttype_status_idx` (`Status_id`),
  CONSTRAINT `FK_alerttype_alertcategory` FOREIGN KEY (`AlertCategoryId`) REFERENCES `dbxalertcategory` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_alerttype_alertcondition` FOREIGN KEY (`AlertConditionId`) REFERENCES `alertcondition` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_alerttype_status` FOREIGN KEY (`Status_id`) REFERENCES `status` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `alertattributelistvalues`;
CREATE TABLE `alertattributelistvalues` (
  `id` varchar(255) NOT NULL,
  `AlertAttributeId` varchar(255) NOT NULL,
  `LanguageCode` varchar(10) NOT NULL,
  `name` varchar(250) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`AlertAttributeId`,`LanguageCode`,`id`),
  KEY `FK_alertattributelistvalues_alertattribute_idx` (`AlertAttributeId`),
  KEY `FK_alertattributelistvalues_locale_idx` (`LanguageCode`),
  CONSTRAINT `FK_alertattributelistvalues_alertattribute` FOREIGN KEY (`AlertAttributeId`) REFERENCES `alertattribute` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_alertattributelistvalues_locale` FOREIGN KEY (`LanguageCode`) REFERENCES `locale` (`Code`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `channel`;
CREATE TABLE `channel` (
  `id` varchar(100) NOT NULL,
  `status_id` varchar(100) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_channel_status_idx` (`status_id`),
  CONSTRAINT `FK_channel_status` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `alertcategorychannel`;
CREATE TABLE `alertcategorychannel` (
  `ChannelID` varchar(100) NOT NULL,
  `AlertCategoryId` varchar(100) NOT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ChannelID`,`AlertCategoryId`),
  KEY `FK_alertcategorychannel_alertcategory` (`AlertCategoryId`),
  CONSTRAINT `FK_alertcategorychannel_alertcategory` FOREIGN KEY (`AlertCategoryId`) REFERENCES `dbxalertcategory` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_alertcategorychannel_channel` FOREIGN KEY (`ChannelID`) REFERENCES `channel` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `alertcontentfields`;
CREATE TABLE `alertcontentfields` (
  `Code` varchar(255) NOT NULL,
  `Name` varchar(255) DEFAULT NULL,
  `DefaultValue` varchar(255) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`Code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `channeltext`;
CREATE TABLE `channeltext` (
  `channelID` varchar(100) NOT NULL,
  `LanguageCode` varchar(100) NOT NULL,
  `Description` varchar(1000) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`channelID`,`LanguageCode`),
  KEY `FK_channeltext_channel_idx` (`channelID`),
  KEY `FK_channeltext_locale_idx` (`LanguageCode`),
  CONSTRAINT `FK_channeltext_channel` FOREIGN KEY (`channelID`) REFERENCES `channel` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_channeltext_locale` FOREIGN KEY (`LanguageCode`) REFERENCES `locale` (`Code`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `dbxalerttypetext`;
CREATE TABLE `dbxalerttypetext` (
  `AlertTypeId` varchar(100) NOT NULL,
  `LanguageCode` varchar(10) NOT NULL,
  `DisplayName` varchar(255) DEFAULT NULL,
  `Description` varchar(1000) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`AlertTypeId`,`LanguageCode`),
  KEY `FK_alerttypetext_locale_idx` (`LanguageCode`),
  CONSTRAINT `FK_alerttypetext_alerttype` FOREIGN KEY (`AlertTypeId`) REFERENCES `dbxalerttype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_alerttypetext_locale` FOREIGN KEY (`LanguageCode`) REFERENCES `locale` (`Code`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `AlertSubType`;
DROP TABLE IF EXISTS `alertsubtype`;
CREATE TABLE `alertsubtype` (
  `id` varchar(100) NOT NULL,
  `AlertTypeId` varchar(255) NOT NULL,
  `Name` varchar(255) DEFAULT NULL,
  `Description` varchar(1000) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_alertsubtype_alerttype_idx` (`AlertTypeId`),
  CONSTRAINT `FK_alertsubtype_alerttype` FOREIGN KEY (`AlertTypeId`) REFERENCES `dbxalerttype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `alertHistory`;
DROP TABLE IF EXISTS `alerthistory`;
CREATE TABLE `alerthistory` (
  `id` varchar(100) NOT NULL,
  `EventId` varchar(100) NOT NULL,
  `AlertSubTypeId` varchar(100) NOT NULL,
  `AlertTypeId` varchar(100) NOT NULL,
  `AlertCategoryId` varchar(100) NOT NULL,
  `AlertStatusId` varchar(50) NOT NULL,
  `Customer_Id` varchar(50) NOT NULL,
  `LanguageCode` varchar(10) DEFAULT NULL,
  `ChannelId` varchar(100) NOT NULL,
  `Status` varchar(255) DEFAULT NULL,
  `Subject` varchar(255) DEFAULT NULL,
  `Message` text,
  `SenderName` varchar(255) DEFAULT NULL,
  `SenderEmail` varchar(255) DEFAULT NULL,
  `ReferenceNumber` varchar(100) DEFAULT NULL,
  `DispatchDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ErrorMessage` text,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_alerthistory_alertsubtype_idx` (`AlertSubTypeId`),
  KEY `FK_alerthistory_customer_idx` (`Customer_Id`),
  KEY `FK_alerthistory_locale_idx` (`LanguageCode`),
  KEY `FK_alerthistory_channel_idx` (`ChannelId`),
  KEY `FK_alerthistory_alerttype_idx` (`AlertTypeId`),
  KEY `FK_alerthistory_alertcategory_idx` (`AlertCategoryId`),
  CONSTRAINT `FK_alerthistory_alertcategory` FOREIGN KEY (`AlertCategoryId`) REFERENCES `dbxalertcategory` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_alerthistory_alertsubtype` FOREIGN KEY (`AlertSubTypeId`) REFERENCES `alertsubtype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_alerthistory_alerttype` FOREIGN KEY (`AlertTypeId`) REFERENCES `dbxalerttype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_alerthistory_customer` FOREIGN KEY (`Customer_Id`) REFERENCES `customer` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `alerttypeaccounttype`;
CREATE TABLE `alerttypeaccounttype` (
  `AccountTypeId` varchar(100) NOT NULL,
  `AlertTypeId` varchar(100) NOT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`AccountTypeId`,`AlertTypeId`),
  KEY `FK_alerttypeaccounttype_alerttype_idx` (`AlertTypeId`),
  CONSTRAINT `FK_alerttypeaccounttype_accounttype` FOREIGN KEY (`AccountTypeId`) REFERENCES `accounttype` (`TypeID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_alerttypeaccounttype_alerttype` FOREIGN KEY (`AlertTypeId`) REFERENCES `dbxalerttype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `app`;
CREATE TABLE `app` (
  `id` VARCHAR(50) NOT NULL,
  `Name` VARCHAR(50) NOT NULL,
  `Description` VARCHAR(50) NULL DEFAULT NULL,
  `Status_id` VARCHAR(50) NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)) ENGINE = InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `alerttypeapp`;
CREATE TABLE `alerttypeapp` (
  `AppId` varchar(100) NOT NULL,
  `AlertTypeId` varchar(100) NOT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`AppId`,`AlertTypeId`),
  KEY `FK_alerttypeapp_alerttype_idx` (`AlertTypeId`),
  CONSTRAINT `FK_alerttypeapp_alerttype` FOREIGN KEY (`AlertTypeId`) REFERENCES `dbxalerttype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_alerttypeapp_app` FOREIGN KEY (`AppId`) REFERENCES `app` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `alerttypecustomertype`;
CREATE TABLE `alerttypecustomertype` (
  `CustomerTypeId` varchar(100) NOT NULL,
  `AlertTypeId` varchar(100) NOT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`CustomerTypeId`,`AlertTypeId`),
  KEY `FK_alerttypecustomertype_alerttype_idx` (`AlertTypeId`),
  CONSTRAINT `FK_alerttypecustomertype_customertype` FOREIGN KEY (`CustomerTypeId`) REFERENCES `customertype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_alerttypecustomertype_alerttype` FOREIGN KEY (`AlertTypeId`) REFERENCES `dbxalerttype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `communicationtemplate`;
CREATE TABLE `communicationtemplate` (
  `Id` varchar(100) NOT NULL,
  `LanguageCode` varchar(100) DEFAULT NULL,
  `AlertSubTypeId` varchar(100) DEFAULT NULL,
  `ChannelID` varchar(100) DEFAULT NULL,
  `Status_id` varchar(45) DEFAULT NULL,
  `Name` varchar(255) DEFAULT NULL,
  `Text` text,
  `Subject` varchar(255) DEFAULT NULL,
  `SenderName` varchar(255) DEFAULT NULL,
  `SenderEmail` varchar(255) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`Id`),
  KEY `FK_communicationtemplate_locale_idx` (`LanguageCode`),
  KEY `FK_communicationtemplate_alertsubtype_idx` (`AlertSubTypeId`),
  KEY `FK_communicationtemplate_channel_idx` (`ChannelID`),
  KEY `FK_communicationtemplate_status_idx` (`Status_id`),
  CONSTRAINT `FK_communicationtemplate_alertsubtype` FOREIGN KEY (`AlertSubTypeId`) REFERENCES `alertsubtype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_communicationtemplate_channel` FOREIGN KEY (`ChannelID`) REFERENCES `channel` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_communicationtemplate_locale` FOREIGN KEY (`LanguageCode`) REFERENCES `locale` (`Code`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_communicationtemplate_status` FOREIGN KEY (`Status_id`) REFERENCES `status` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `customeralertcategorychannel`;
CREATE TABLE `customeralertcategorychannel` (
  `Customer_id` varchar(100) NOT NULL,
  `AlertCategoryId` varchar(100) NOT NULL,
  `ChannelId` varchar(100) NOT NULL,
  `AccountId` varchar(100) NOT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`Customer_id`,`AlertCategoryId`,`ChannelId`,`AccountId`),
  KEY `FK_customeralertcategorychannel_alertcategory_idx` (`AlertCategoryId`),
  KEY `FK_customeralertcategorychannel_channel_idx` (`ChannelId`),
  CONSTRAINT `FK_customeralertcategorychannel_alertcategory` FOREIGN KEY (`AlertCategoryId`) REFERENCES `dbxalertcategory` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_customeralertcategorychannel_channel` FOREIGN KEY (`ChannelId`) REFERENCES `channel` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_customeralertcategorychannel_customer` FOREIGN KEY (`Customer_id`) REFERENCES `customer` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `customeralertswitch`;
CREATE TABLE `customeralertswitch` (
  `Customer_id` varchar(100) NOT NULL,
  `AccountID` varchar(50) DEFAULT NULL,
  `AlertCategoryId` varchar(100) NOT NULL,
  `Status_id` varchar(100) NOT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`Customer_id`,`AlertCategoryId`),
  KEY `FK_customeralertswitch_alertcategory_idx` (`AlertCategoryId`),
  KEY `FK_customeralertswitch_status_idx` (`Status_id`),
  KEY `FK_customeralertswitch_accounts_idx` (`AccountID`),
  CONSTRAINT `FK_customeralertswitch_accounts` FOREIGN KEY (`AccountID`) REFERENCES `accounts` (`Account_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_customeralertswitch_alertcategory` FOREIGN KEY (`AlertCategoryId`) REFERENCES `dbxalertcategory` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_customeralertswitch_customer` FOREIGN KEY (`Customer_id`) REFERENCES `customer` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_customeralertswitch_status` FOREIGN KEY (`Status_id`) REFERENCES `status` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `dbxalertcategorytext`;
CREATE TABLE `dbxalertcategorytext` (
  `AlertCategoryId` varchar(100) NOT NULL,
  `LanguageCode` varchar(10) NOT NULL,
  `DisplayName` varchar(255) DEFAULT NULL,
  `Description` varchar(1000) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`AlertCategoryId`,`LanguageCode`),
  KEY `LanguageCode_idx` (`LanguageCode`),
  CONSTRAINT `FK_alertcategorytext_alertcategory` FOREIGN KEY (`AlertCategoryId`) REFERENCES `dbxalertcategory` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_alertcategorytext_locale` FOREIGN KEY (`LanguageCode`) REFERENCES `locale` (`Code`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `dbxCustomerAlertEntitlement`;
DROP TABLE IF EXISTS `dbxcustomeralertentitlement`;
CREATE TABLE `dbxcustomeralertentitlement` (
  `Customer_id` varchar(50) NOT NULL,
  `AlertTypeId` varchar(255) NOT NULL,
  `AccountId` varchar(255) DEFAULT NULL,
  `Value1` varchar(255) DEFAULT NULL,
  `Value2` varchar(255) DEFAULT NULL,
  `LastEventPushed` date DEFAULT NULL,
  `Balance` varchar(255) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`Customer_id`,`AlertTypeId`),
  KEY `FK_dbxcustomeralertentitlement_customer_idx` (`Customer_id`),
  KEY `FK_dbxcustomeralertentitlement_alerttype_idx` (`AlertTypeId`),
  KEY `FK_dbxcustomeralertentitlement_accounts_idx` (`AccountId`),
  CONSTRAINT `FK_dbxcustomeralertentitlement_accounts` FOREIGN KEY (`AccountId`) REFERENCES `accounts` (`Account_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_dbxcustomeralertentitlement_alerttype` FOREIGN KEY (`AlertTypeId`) REFERENCES `dbxalerttype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_dbxcustomeralertentitlement_customer` FOREIGN KEY (`Customer_id`) REFERENCES `customer` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `event`;
CREATE TABLE `event` (
  `Event_id` int(10) NOT NULL AUTO_INCREMENT,
  `EventType` varchar(255) NOT NULL,
  `EventSubType` varchar(255) DEFAULT NULL,
  `Status_id` varchar(50) DEFAULT NULL,
  `EventData` text,
  `OtherData` text,
  `IsProcessed` tinyint(4) NOT NULL,
  `Producer` varchar(50) NOT NULL,
  `PreProcessorResult` varchar(50) DEFAULT NULL,
  `PostProcessorResult` varchar(50) DEFAULT NULL,
  `Session` text,
  `Timestamp` datetime DEFAULT NULL,
  PRIMARY KEY (`Event_id`),
  UNIQUE KEY `Event_id_UNIQUE` (`Event_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARACTER SET = utf8;

DROP TABLE IF EXISTS `eventconsumer`;
CREATE TABLE `eventconsumer` (
  `ServiceId` varchar(50) NOT NULL,
  `OperationId` varchar(50) NOT NULL,
  `BatchLimit` int(11) NOT NULL,
  `LastEventId` int(10) NOT NULL,
  PRIMARY KEY (`ServiceId`,`OperationId`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8;

DROP TABLE IF EXISTS `eventconsumertypes`;
CREATE TABLE `eventconsumertypes` (
  `ServiceId` varchar(50) NOT NULL,
  `OperationId` varchar(50) NOT NULL,
  `EventType` varchar(20) NOT NULL,
  PRIMARY KEY (`ServiceId`,`OperationId`,`EventType`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8;

DROP TABLE IF EXISTS `eventtype`;
CREATE TABLE `eventtype` (
  `id` varchar(255) NOT NULL,
  `Name` varchar(255) NOT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `eventsubtype`;
CREATE TABLE `eventsubtype` (
  `id` varchar(255) NOT NULL,
  `eventtypeid` varchar(255) NOT NULL,
  `Name` varchar(255) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`,`eventtypeid`),
  KEY `fk_eventtypeid` (`eventtypeid`),
  CONSTRAINT `fk_eventtypeid` FOREIGN KEY (`eventtypeid`) REFERENCES `eventtype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `dbxCommunicationType`;
DROP TABLE IF EXISTS `dbxCustomerCommunication`;

ALTER TABLE `bbtemplate` DROP FOREIGN KEY `FK_bbtemplate_accounts`;
ALTER TABLE `bbtemplate` DROP INDEX `FK_bbtemplate_accounts_idx` ;
ALTER TABLE `bbtransaction` DROP FOREIGN KEY `FK_bbtransaction_accounts`;
ALTER TABLE `bbtransaction` DROP INDEX `FK_bbtransaction_accounts_idx` ;
ALTER TABLE `bbgeneraltransaction` DROP FOREIGN KEY `FK_bbgeneraltransaction_Accounts`;
ALTER TABLE `bbgeneraltransaction` DROP INDEX `FK_bbgeneraltransaction_accounts_idx` ;

ALTER TABLE `customeraccounts` ADD COLUMN `FavouriteStatus` INT(11) NOT NULL DEFAULT 0 AFTER `AccountName`;

DROP VIEW IF EXISTS `customeraccountsview`;
CREATE VIEW `customeraccountsview` AS    SELECT DISTINCT  `accounts`.`TaxId` AS `Taxid`,  `customeraccounts`.`Membership_id` AS `Membership_id`,  `customeraccounts`.`Customer_id` AS `Customer_id`,  `customeraccounts`.`Customer_id` AS `User_id`,  `accounts`.`Account_id` AS `Account_id`,  `accounts`.`Type_id` AS `Type_id`,  `accounts`.`UserName` AS `userName`,  `accounts`.`CurrencyCode` AS `currencyCode`,  `accounts`.`AccountHolder` AS `accountHolder`,  `accounts`.`error` AS `error`,  `accounts`.`Address` AS `Address`,  `accounts`.`Scheme` AS `Scheme`,  `accounts`.`Number` AS `number`,  `accounts`.`AvailableBalance` AS `availableBalance`,  `accounts`.`CurrentBalance` AS `currentBalance`,  `accounts`.`InterestRate` AS `interestRate`,  `accounts`.`AvailableCredit` AS `availableCredit`,  `accounts`.`MinimumDue` AS `minimumDue`,  `accounts`.`DueDate` AS `dueDate`,  `accounts`.`FirstPaymentDate` AS `firstPaymentDate`,  `accounts`.`ClosingDate` AS `closingDate`,  `accounts`.`PaymentTerm` AS `paymentTerm`,  `accounts`.`OpeningDate` AS `openingDate`,  `accounts`.`MaturityDate` AS `maturityDate`,  `accounts`.`DividendLastPaidAmount` AS `dividendLastPaidAmount`,  `accounts`.`DividendLastPaidDate` AS `dividendLastPaidDate`,  `accounts`.`DividendPaidYTD` AS `dividendPaidYTD`,  `accounts`.`DividendRate` AS `dividendRate`,  `accounts`.`DividendYTD` AS `dividendYTD`,  `accounts`.`EStatementmentEnable` AS `eStatementEnable`,  `customeraccounts`.`FavouriteStatus` AS `favouriteStatus`,  `accounts`.`StatusDesc` AS `statusDesc`,  `accounts`.`NickName` AS `nickName`,  `accounts`.`OriginalAmount` AS `originalAmount`,  `accounts`.`OutstandingBalance` AS `outstandingBalance`,  `accounts`.`PaymentDue` AS `paymentDue`,  `accounts`.`PaymentMethod` AS `paymentMethod`,  `accounts`.`SwiftCode` AS `swiftCode`,  `accounts`.`TotalCreditMonths` AS `totalCreditMonths`,  `accounts`.`TotalDebitsMonth` AS `totalDebitsMonth`,  `accounts`.`RoutingNumber` AS `routingNumber`,  `accounts`.`SupportBillPay` AS `supportBillPay`,  `accounts`.`SupportCardlessCash` AS `supportCardlessCash`,  `accounts`.`SupportTransferFrom` AS `supportTransferFrom`,  `accounts`.`SupportTransferTo` AS `supportTransferTo`,  `accounts`.`SupportDeposit` AS `supportDeposit`,  `accounts`.`UnpaidInterest` AS `unpaidInterest`,  `accounts`.`PreviousYearsDividends` AS `previousYearsDividends`,  `accounts`.`principalBalance` AS `principalBalance`,  `accounts`.`PrincipalValue` AS `principalValue`,  `accounts`.`RegularPaymentAmount` AS `regularPaymentAmount`,  `accounts`.`phone` AS `phoneId`,  `accounts`.`LastDividendPaidDate` AS `lastDividendPaidDate`,  `accounts`.`LastDividendPaidAmount` AS `lastDividendPaidAmount`,  `accounts`.`LastPaymentAmount` AS `lastPaymentAmount`,  `accounts`.`LastPaymentDate` AS `lastPaymentDate`,  `accounts`.`LastStatementBalance` AS `lastStatementBalance`,  `accounts`.`LateFeesDue` AS `lateFeesDue`,  `accounts`.`maturityAmount` AS `maturityAmount`,  `accounts`.`MaturityOption` AS `maturityOption`,  `accounts`.`payoffAmount` AS `payoffAmount`,  `accounts`.`PayOffCharge` AS `payOffCharge`,  `accounts`.`PendingDeposit` AS `pendingDeposit`,  `accounts`.`PendingWithdrawal` AS `pendingWithdrawal`,  `accounts`.`JointHolders` AS `jointHolders`,  `accounts`.`IsPFM` AS `isPFM`,  `accounts`.`InterestPaidYTD` AS `interestPaidYTD`,  `accounts`.`InterestPaidPreviousYTD` AS `interestPaidPreviousYTD`,  `accounts`.`InterestPaidLastYear` AS `interestPaidLastYear`,  `accounts`.`InterestEarned` AS `interestEarned`,  `accounts`.`CurrentAmountDue` AS `currentAmountDue`,  `accounts`.`CreditLimit` AS `creditLimit`,  `accounts`.`CreditCardNumber` AS `creditCardNumber`,  `accounts`.`BsbNum` AS `bsbNum`,  `accounts`.`BondInterestLastYear` AS `bondInterestLastYear`,  `accounts`.`BondInterest` AS `bondInterest`,  `accounts`.`AvailablePoints` AS `availablePoints`,  `accounts`.`AccountName` AS `accountName`,  `accounts`.`email` AS `email`,  `accounts`.`IBAN` AS `IBAN`,  `accounts`.`adminProductId` AS `adminProductId`,  `bank`.`Description` AS `bankname`,  `accounts`.`AccountPreference` AS `accountPreference`,  `accounttype`.`transactionLimit` AS `transactionLimit`,  `accounttype`.`transferLimit` AS `transferLimit`,  `accounttype`.`rates` AS `rates`,  `accounttype`.`termsAndConditions` AS `termsAndConditions`,  `accounttype`.`TypeDescription` AS `typeDescription`,  `accounttype`.`supportChecks` AS `supportChecks`,  `accounttype`.`displayName` AS `displayName`,  `accounts`.`accountSubType` AS `accountSubType`,  `accounts`.`description` AS `description`,  `accounts`.`schemeName` AS `schemeName`,  `accounts`.`identification` AS `identification`,  `accounts`.`secondaryIdentification` AS `secondaryIdentification`,  `accounts`.`servicerSchemeName` AS `servicerSchemeName`,  `accounts`.`servicerIdentification` AS `servicerIdentification`,  `accounts`.`dataCreditDebitIndicator` AS `dataCreditDebitIndicator`,  `accounts`.`dataType` AS `dataType`,  `accounts`.`dataDateTime` AS `dataDateTime`,  `accounts`.`dataCreditLineIncluded` AS `dataCreditLineIncluded`,  `accounts`.`dataCreditLineType` AS `dataCreditLineType`,  `accounts`.`dataCreditLineAmount` AS `dataCreditLineAmount`,  `accounts`.`dataCreditLineCurrency` AS `dataCreditLineCurrency`    FROM  (((`accounts`  JOIN `customeraccounts`)  JOIN `accounttype`)  LEFT JOIN `bank` ON ((`accounts`.`Bank_id` = `bank`.`id`)))    WHERE  ((`accounts`.`Account_id` = `customeraccounts`.`Account_id`)AND (`accounts`.`Type_id` = `accounttype`.`TypeID`));

DROP VIEW IF EXISTS `fetch_achtemplate_details_view`;
CREATE VIEW `fetch_achtemplate_details_view` AS select `bbtemplate`.`Template_id` AS `Template_id`,`bbtemplate`.`TemplateName` AS `TemplateName`,`bbtemplate`.`TemplateDescription` AS `TemplateDescription`,`bbtemplate`.`DebitAccount` AS `DebitAccount`,`bbtemplate`.`EffectiveDate` AS `EffectiveDate`,`bbtemplate`.`Request_id` AS `Request_id`,`bbtemplate`.`createdby` AS `createdby`,`bbtemplate`.`createdts` AS `createdts`,`bbtemplate`.`UpdatedBy` AS `UpdatedBy`,`bbtemplate`.`Updatedts` AS `Updatedts`,`bbtemplate`.`MaxAmount` AS `MaxAmount`,`bbtemplate`.`Status` AS `Status`,`bbtemplate`.`TransactionType_id` AS `TransactionType_id`,`bbtemplate`.`TemplateType_id` AS `TemplateType_id`,`bbtemplate`.`Company_id` AS `Company_id`,`bbtemplate`.`TotalAmount` AS `TotalAmount`,`bbtemplate`.`TemplateRequestType_id` AS `TemplateRequestType_id`,`bbtemplate`.`softDelete` AS `softDelete`,`bbtemplate`.`ActedBy` AS `ActedBy`,`customeraccounts`.`AccountName` AS `AccountName`,`customer`.`UserName` AS `CreateBy`,`bbstatus`.`status` AS `StatusValue`,`bbtransactiontype`.`TransactionTypeName` AS `TransactionTypeValue`,`bbtemplatetype`.`TemplateTypeName` AS `TemplateTypeValue`,`bbtemplaterequesttype`.`TemplateRequestTypeName` AS `TemplateRequestTypeValue`,`organisation`.`Name` AS `CompanyName`,`bbrequest`.`createdby` AS `RequestCreatedby`,`bbrequest`.`RequestType_id` AS `BBGeneralTransactionType_id` from ((((((((`bbtemplate` left join `customeraccounts` on((`bbtemplate`.`DebitAccount` = `customeraccounts`.`Account_id`))) left join `customer` on((`bbtemplate`.`createdby` = `customer`.`id`))) left join `bbstatus` on((`bbtemplate`.`Status` = `bbstatus`.`id`))) left join `bbtransactiontype` on((`bbtemplate`.`TransactionType_id` = `bbtransactiontype`.`TransactionType_id`))) left join `bbtemplatetype` on((`bbtemplate`.`TemplateType_id` = `bbtemplatetype`.`TemplateType_id`))) left join `bbtemplaterequesttype` on((`bbtemplate`.`TemplateRequestType_id` = `bbtemplaterequesttype`.`TemplateRequestType_id`))) left join `organisation` on((`bbtemplate`.`Company_id` = `organisation`.`id`))) left join `bbrequest` on((`bbtemplate`.`Request_id` = `bbrequest`.`Request_id`)));

DROP VIEW IF EXISTS `fetch_achtransaction_details_view`;
CREATE VIEW `fetch_achtransaction_details_view` AS select `bbtransaction`.`Transaction_id` AS `Transaction_id`,`bbtransaction`.`DebitAccount` AS `DebitAccount`,`bbtransaction`.`EffectiveDate` AS `EffectiveDate`,`bbtransaction`.`Request_id` AS `Request_id`,`bbtransaction`.`createdby` AS `createdby`,`bbtransaction`.`createdts` AS `createdts`,`bbtransaction`.`MaxAmount` AS `MaxAmount`,`bbtransaction`.`Status` AS `Status`,`bbtransaction`.`TransactionType_id` AS `TransactionType_id`,`bbtransaction`.`TemplateType_id` AS `TemplateType_id`,`bbtransaction`.`Company_id` AS `Company_id`,`bbtransaction`.`TemplateRequestType_id` AS `TemplateRequestType_id`,`bbtransaction`.`softDelete` AS `softDelete`,`bbtransaction`.`TemplateName` AS `TemplateName`,`bbtransaction`.`Template_id` AS `Template_id`,`bbtransaction`.`TotalAmount` AS `TotalAmount`,`bbtransaction`.`ConfirmationNumber` AS `ConfirmationNumber`,`bbtransaction`.`ActedBy` AS `ActedBy`,`customeraccounts`.`AccountName` AS `AccountName`,`customer`.`UserName` AS `userName`,`bbstatus`.`status` AS `StatusValue`,`bbtransactiontype`.`TransactionTypeName` AS `TransactionTypeValue`,`bbtemplatetype`.`TemplateTypeName` AS `TemplateTypeValue`,`bbtemplaterequesttype`.`TemplateRequestTypeName` AS `TemplateRequestTypeValue`,`organisation`.`Name` AS `CompanyName`,`bbrequest`.`createdby` AS `RequestCreatedby`,`bbrequest`.`RequestType_id` AS `BBGeneralTransactionType_id` from ((((((((`bbtransaction` left join `customeraccounts` on((`bbtransaction`.`DebitAccount` = `customeraccounts`.`Account_id`))) left join `customer` on((`bbtransaction`.`createdby` = `customer`.`id`))) left join `bbstatus` on((`bbtransaction`.`Status` = `bbstatus`.`id`))) left join `bbtransactiontype` on((`bbtransaction`.`TransactionType_id` = `bbtransactiontype`.`TransactionType_id`))) left join `bbtemplatetype` on((`bbtransaction`.`TemplateType_id` = `bbtemplatetype`.`TemplateType_id`))) left join `bbtemplaterequesttype` on((`bbtransaction`.`TemplateRequestType_id` = `bbtemplaterequesttype`.`TemplateRequestType_id`))) left join `organisation` on((`bbtransaction`.`Company_id` = `organisation`.`id`))) left join `bbrequest` on((`bbtransaction`.`Request_id` = `bbrequest`.`Request_id`)));

DROP VIEW IF EXISTS `alert_fetchcategory_view`;
CREATE VIEW `alert_fetchcategory_view` AS select `dbxalertcategory`.`id` AS `AlertCategoryId`,`dbxalertcategory`.`status_id` AS `status_id`,`dbxalertcategory`.`accountLevel` AS `accountLevel`,`dbxalerttype`.`id` AS `AlertTypeId` from ((`dbxalerttype` left join `dbxalertcategory` on((`dbxalertcategory`.`id` = `dbxalerttype`.`AlertCategoryId`))) left join `status` on((`dbxalertcategory`.`status_id` = `status`.`id`)));

DROP VIEW IF EXISTS `alertcustomerchannels_view`;
CREATE VIEW `alertcustomerchannels_view` AS select `dbxcustomeralertentitlement`.`AlertTypeId` AS `AlertTypeId`,`dbxalerttype`.`Name` AS `Name`,`customeralertcategorychannel`.`AlertCategoryId` AS `AlertCategoryId`,`customeralertcategorychannel`.`Customer_id` AS `Customer_id`,`customer`.`FirstName` AS `FirstName`,`customer`.`LastName` AS `LastName`,`customer`.`UserName` AS `UserName`,`dbxalerttype`.`AttributeId` AS `AttributeId`,`dbxalerttype`.`AlertConditionId` AS `AlertConditionId`,`dbxcustomeralertentitlement`.`Value1` AS `Value1`,`dbxcustomeralertentitlement`.`Value2` AS `Value2`,`dbxalerttype`.`IsGlobal` AS `IsGlobal`,`customeralertcategorychannel`.`ChannelId` AS `ChannelId`,`customeralertcategorychannel`.`AccountId` AS `AccountId`,`status`.`Type_id` AS `Status_id`,`customercommunication`.`Type_id` AS `Type_id`,`customercommunication`.`Value` AS `Value`,`customer`.`CountryCode` AS `CountryCode`,`dbxalertcategory`.`accountLevel` AS `accountLevel`,`customercommunication`.`isPrimary` AS `isPrimary` from ((((((`customeralertcategorychannel` left join `dbxcustomeralertentitlement` on((`customeralertcategorychannel`.`Customer_id` = `dbxcustomeralertentitlement`.`Customer_id`))) left join `dbxalerttype` on((`dbxcustomeralertentitlement`.`AlertTypeId` = `dbxalerttype`.`id`))) left join `status` on((`dbxalerttype`.`Status_id` = `status`.`id`))) left join `dbxalertcategory` on((`customeralertcategorychannel`.`AlertCategoryId` = `dbxalertcategory`.`id`))) left join `customer` on((`customer`.`id` = `dbxcustomeralertentitlement`.`Customer_id`))) left join `customercommunication` on((`customer`.`id` = `customercommunication`.`Customer_id`)));

DROP VIEW IF EXISTS `alertcustomersaccountchannels_view`;
CREATE VIEW `alertcustomersaccountchannels_view` AS select `dbxcustomeralertentitlement`.`AlertTypeId` AS `AlertTypeId`,`dbxalerttype`.`Name` AS `Name`,`customeralertcategorychannel`.`AlertCategoryId` AS `AlertCategoryId`,`customer`.`id` AS `Customer_Id`,`customer`.`FirstName` AS `FirstName`,`customer`.`LastName` AS `LastName`,`customer`.`UserName` AS `UserName`,`dbxalerttype`.`AttributeId` AS `AttributeId`,`dbxalerttype`.`AlertConditionId` AS `AlertConditionId`,`dbxcustomeralertentitlement`.`Value1` AS `Value1`,`dbxcustomeralertentitlement`.`Value2` AS `Value2`,`dbxalerttype`.`IsGlobal` AS `IsGlobal`,`customeralertcategorychannel`.`ChannelId` AS `ChannelId`,`customeralertcategorychannel`.`AccountId` AS `AccountId`,`status`.`Type_id` AS `Status_id`,`customercommunication`.`Type_id` AS `Type_id`,`customercommunication`.`Value` AS `Value`,`customer`.`CountryCode` AS `CountryCode`,`dbxalertcategory`.`accountLevel` AS `accountLevel`,`customercommunication`.`isPrimary` AS `isPrimary` from ((((((`dbxalerttype` left join `status` on((`dbxalerttype`.`Status_id` = `status`.`id`))) left join `customeralertcategorychannel` on((`dbxalerttype`.`AlertCategoryId` = `customeralertcategorychannel`.`AlertCategoryId`))) left join `dbxcustomeralertentitlement` on(((`dbxcustomeralertentitlement`.`Customer_id` = `customeralertcategorychannel`.`Customer_id`) and (`dbxcustomeralertentitlement`.`AlertTypeId` = `dbxalerttype`.`id`)))) left join `dbxalertcategory` on((`dbxalerttype`.`AlertCategoryId` = `dbxalertcategory`.`id`))) left join `customer` on((`customer`.`id` = `dbxcustomeralertentitlement`.`Customer_id`))) left join `customercommunication` on((`customer`.`id` = `customercommunication`.`Customer_id`)));

DROP VIEW IF EXISTS `alertglobalchannels_view`;
CREATE VIEW `alertglobalchannels_view` AS select `dbxalerttype`.`id` AS `AlertTypeId`,`dbxalerttype`.`Name` AS `Name`,`dbxalerttype`.`AlertCategoryId` AS `AlertCategoryId`,`customer`.`id` AS `Customer_Id`,`customer`.`FirstName` AS `FirstName`,`customer`.`LastName` AS `LastName`,`customer`.`UserName` AS `UserName`,`dbxalerttype`.`AttributeId` AS `AttributeId`,`dbxalerttype`.`AlertConditionId` AS `AlertConditionId`,`dbxalerttype`.`Value1` AS `Value1`,`dbxalerttype`.`Value2` AS `Value2`,`dbxalerttype`.`IsGlobal` AS `IsGlobal`,`alertcategorychannel`.`ChannelID` AS `ChannelId`,`status`.`Type_id` AS `Status_id`,`customercommunication`.`Type_id` AS `Type_id`,`customercommunication`.`Value` AS `Value`,`customer`.`CountryCode` AS `CountryCode`,`dbxalertcategory`.`accountLevel` AS `accountLevel`,`customercommunication`.`isPrimary` AS `isPrimary` from (((((`dbxalerttype` left join `status` on((`dbxalerttype`.`Status_id` = `status`.`id`))) left join `alertcategorychannel` on((`dbxalerttype`.`AlertCategoryId` = `alertcategorychannel`.`AlertCategoryId`))) left join `dbxalertcategory` on((`dbxalerttype`.`AlertCategoryId` = `dbxalertcategory`.`id`))) join `customer`) left join `customercommunication` on((`customer`.`id` = `customercommunication`.`Customer_id`)));

ALTER TABLE `OTP` 
ADD COLUMN `User_id` VARCHAR(50) NULL DEFAULT NULL AFTER `Phone`,
ADD COLUMN `serviceKey` VARCHAR(50) NULL DEFAULT NULL AFTER `User_id`,
ADD COLUMN `NumberOfRetries` INT(11) NULL DEFAULT 0 AFTER `serviceKey`,
CHANGE COLUMN `InvalidAttempt` `InvalidAttempt` INT(11) NULL DEFAULT 0,
CHANGE COLUMN `Otp` `Otp` VARCHAR(10) NULL DEFAULT NULL,
ADD COLUMN `Email` varchar(100) NULL DEFAULT NULL AFTER `NumberOfRetries`;


CREATE TABLE `otpcount` (
  `key` varchar(50) NOT NULL,
  `User_id` varchar(50) NOT NULL,
  `Date` varchar(20) NOT NULL,
  `Count` int(5) DEFAULT '1',
  `Phone` varchar(15) DEFAULT NULL,
  `Email` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `mfaservice` (
  `serviceKey` varchar(50) NOT NULL,
  `serviceName` varchar(50) DEFAULT NULL,
  `User_id` varchar(50) NOT NULL,
  `Createddts` datetime DEFAULT NULL,
  `retryCount` int(5) DEFAULT '0',
  `payload` varchar(10000) DEFAULT NULL,
  PRIMARY KEY (`serviceKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP VIEW IF EXISTS `organisationaccountsview`;
CREATE VIEW `organisationaccountsview` AS
    SELECT DISTINCT
        `accounttype`.`TypeDescription` AS `Account_Type`,
        `customeraccounts`.`Customer_id` AS `Customer_id`,
        `customeraccounts`.`Account_id` AS `Account_id`,
        `customeraccounts`.`Account_id` AS `accountID`,
        `customeraccounts`.`Organization_id` AS `Organization_Id`,
        `customeraccounts`.`createdts` AS `createdts`,
        `customeraccounts`.`lastmodifiedts` AS `lastmodifiedts`,
        `accounts`.`StatusDesc` AS `StatusDesc`,
        `accounts`.`AccountName` AS `AccountName`,
        `accounts`.`AvailableBalance` AS `availableBalance`,
        `accounts`.`CurrentBalance` AS `currentBalance`,
        `accounts`.`DividendRate` AS `dividendRate`,
        `accounts`.`EStatementmentEnable` AS `eStatementEnable`,
        `accounts`.`SwiftCode` AS `swiftCode`,
        `accounts`.`RoutingNumber` AS `routingNumber`,
        `accounts`.`AccountHolder` AS `accountHolder`,
        `accounts`.`LastDividendPaidDate` AS `lastDividendPaidDate`,
        `accounts`.`LastDividendPaidAmount` AS `lastDividendPaidAmount`,
        `accounts`.`DividendLastPaidAmount` AS `dividendLastPaidAmount`,
        `accounts`.`DividendLastPaidDate` AS `dividendLastPaidDate`,
        'joint' AS `Ownership`
    FROM
        ((`accounts`
        LEFT JOIN `accounttype` ON ((`accounttype`.`TypeID` = `accounts`.`Type_id`)))
        JOIN `customeraccounts`)
    WHERE
        (`accounts`.`Account_id` = `customeraccounts`.`Account_id`);
		
        
DROP VIEW IF EXISTS `customeraccountsview`;        
CREATE VIEW `customeraccountsview` AS
    SELECT DISTINCT
        `membershipaccounts`.`Membership_id` AS  `Membership_id`,
        `membershipaccounts`.`Taxid` AS  `Taxid`,
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
        `accounts`.`FavouriteStatus` AS `favouriteStatus`,
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
        LEFT JOIN `membershipaccounts` ON  `accounts`.`Account_id` = `membershipaccounts`.`Account_id`)
        LEFT JOIN `bank` ON ((`accounts`.`Bank_id` = `bank`.`id`)))
    WHERE
        ((`accounts`.`Account_id` = `customeraccounts`.`Account_id`)
            AND (`accounts`.`Type_id` = `accounttype`.`TypeID`));

 DROP TABLE IF EXISTS `passwordhistory`;           
  CREATE TABLE `passwordhistory` (
  `id` varchar(50) NOT NULL,
  `Customer_id` varchar(50) NOT NULL,
  `PreviousPassword` varchar(100) NOT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `lastsynctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_Customer_id_idx` (`Customer_id`),

  CONSTRAINT `FK_Customer_id` FOREIGN KEY (`Customer_id`) REFERENCES `customer` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `application` ADD COLUMN `defaultCountryDialCode` VARCHAR(45) NULL DEFAULT NULL AFTER `isAccountAggregationEnabled`; 

ALTER TABLE `passwordhistory` CHANGE COLUMN `PreviousPassword` `PreviousPassword` VARCHAR(100) NULL ;

DROP VIEW IF EXISTS `customercommunicationview`;
CREATE VIEW `customercommunicationview` AS
    SELECT 
        `customer`.`id` AS `id`,
        `customer`.`FirstName` AS `FirstName`,
        `customer`.`MiddleName` AS `MiddleName`,
        `customer`.`LastName` AS `LastName`,
        `customer`.`UserName` AS `UserName`,
        `customer`.`Gender` AS `Gender`,
        `customer`.`DateOfBirth` AS `DateOfBirth`,
        `customer`.`Ssn` AS `Ssn`,
        `customer`.`Status_id` AS `Status_id`,
        `customertype`.`Name` AS `CustomerType`,
        `primaryphone`.`Value` AS `Phone`,
        `primaryemail`.`Value` AS `Email`
    FROM
        (((`customer`
        LEFT JOIN `customercommunication` `primaryphone` ON (((`primaryphone`.`Customer_id` = `customer`.`id`)
            AND (`primaryphone`.`isPrimary` = 1)
            AND (`primaryphone`.`Type_id` = 'COMM_TYPE_PHONE'))))
        LEFT JOIN `customercommunication` `primaryemail` ON (((`primaryemail`.`Customer_id` = `customer`.`id`)
            AND (`primaryemail`.`isPrimary` = 1)
            AND (`primaryemail`.`Type_id` = 'COMM_TYPE_EMAIL'))))
        LEFT JOIN `customertype` ON ((`customertype`.`id` = `customer`.`CustomerType_id`)));

        
DROP VIEW IF EXISTS `fetch_achtransaction_details_view`;
CREATE VIEW `fetch_achtransaction_details_view` AS
    SELECT 
        `bbtransaction`.`Transaction_id` AS `Transaction_id`,
        `bbtransaction`.`DebitAccount` AS `DebitAccount`,
        `bbtransaction`.`EffectiveDate` AS `EffectiveDate`,
        `bbtransaction`.`Request_id` AS `Request_id`,
        `bbtransaction`.`createdby` AS `createdby`,
        `bbtransaction`.`createdts` AS `createdts`,
        `bbtransaction`.`MaxAmount` AS `MaxAmount`,
        `bbtransaction`.`Status` AS `Status`,
        `bbtransaction`.`TransactionType_id` AS `TransactionType_id`,
        `bbtransaction`.`TemplateType_id` AS `TemplateType_id`,
        `bbtransaction`.`Company_id` AS `Company_id`,
        `bbtransaction`.`TemplateRequestType_id` AS `TemplateRequestType_id`,
        `bbtransaction`.`softDelete` AS `softDelete`,
        `bbtransaction`.`TemplateName` AS `TemplateName`,
        `bbtransaction`.`Template_id` AS `Template_id`,
        `bbtransaction`.`TotalAmount` AS `TotalAmount`,
        `bbtransaction`.`ConfirmationNumber` AS `ConfirmationNumber`,
        `bbtransaction`.`ActedBy` AS `ActedBy`,
        `customeraccounts`.`AccountName` AS `AccountName`,
        `customer`.`UserName` AS `userName`,
        `bbstatus`.`status` AS `StatusValue`,
        `bbtransactiontype`.`TransactionTypeName` AS `TransactionTypeValue`,
        `bbtemplatetype`.`TemplateTypeName` AS `TemplateTypeValue`,
        `bbtemplaterequesttype`.`TemplateRequestTypeName` AS `TemplateRequestTypeValue`,
        `organisation`.`Name` AS `CompanyName`,
        `bbrequest`.`createdby` AS `RequestCreatedby`,
        `bbrequest`.`RequestType_id` AS `BBGeneralTransactionType_id`
    FROM
        ((((((((`bbtransaction`
        LEFT JOIN `customer` ON ((`bbtransaction`.`createdby` = `customer`.`id`)))
        LEFT JOIN `customeraccounts` ON (((`bbtransaction`.`createdby` = `customeraccounts`.`Customer_id`)
            AND (`bbtransaction`.`DebitAccount` = `customeraccounts`.`Account_id`))))
        LEFT JOIN `bbstatus` ON ((`bbtransaction`.`Status` = `bbstatus`.`id`)))
        LEFT JOIN `bbtransactiontype` ON ((`bbtransaction`.`TransactionType_id` = `bbtransactiontype`.`TransactionType_id`)))
        LEFT JOIN `bbtemplatetype` ON ((`bbtransaction`.`TemplateType_id` = `bbtemplatetype`.`TemplateType_id`)))
        LEFT JOIN `bbtemplaterequesttype` ON ((`bbtransaction`.`TemplateRequestType_id` = `bbtemplaterequesttype`.`TemplateRequestType_id`)))
        LEFT JOIN `organisation` ON ((`bbtransaction`.`Company_id` = `organisation`.`id`)))
        LEFT JOIN `bbrequest` ON ((`bbtransaction`.`Request_id` = `bbrequest`.`Request_id`)));


DROP VIEW IF EXISTS `fetch_achtemplate_details_view`;
CREATE VIEW `fetch_achtemplate_details_view` AS
    SELECT 
        `bbtemplate`.`Template_id` AS `Template_id`,
        `bbtemplate`.`TemplateName` AS `TemplateName`,
        `bbtemplate`.`TemplateDescription` AS `TemplateDescription`,
        `bbtemplate`.`DebitAccount` AS `DebitAccount`,
        `bbtemplate`.`EffectiveDate` AS `EffectiveDate`,
        `bbtemplate`.`Request_id` AS `Request_id`,
        `bbtemplate`.`createdby` AS `createdby`,
        `bbtemplate`.`createdts` AS `createdts`,
        `bbtemplate`.`UpdatedBy` AS `UpdatedBy`,
        `bbtemplate`.`Updatedts` AS `Updatedts`,
        `bbtemplate`.`MaxAmount` AS `MaxAmount`,
        `bbtemplate`.`Status` AS `Status`,
        `bbtemplate`.`TransactionType_id` AS `TransactionType_id`,
        `bbtemplate`.`TemplateType_id` AS `TemplateType_id`,
        `bbtemplate`.`Company_id` AS `Company_id`,
        `bbtemplate`.`TotalAmount` AS `TotalAmount`,
        `bbtemplate`.`TemplateRequestType_id` AS `TemplateRequestType_id`,
        `bbtemplate`.`softDelete` AS `softDelete`,
        `bbtemplate`.`ActedBy` AS `ActedBy`,
        `customeraccounts`.`AccountName` AS `AccountName`,
        `customer`.`UserName` AS `CreateBy`,
        `bbstatus`.`status` AS `StatusValue`,
        `bbtransactiontype`.`TransactionTypeName` AS `TransactionTypeValue`,
        `bbtemplatetype`.`TemplateTypeName` AS `TemplateTypeValue`,
        `bbtemplaterequesttype`.`TemplateRequestTypeName` AS `TemplateRequestTypeValue`,
        `organisation`.`Name` AS `CompanyName`,
        `bbrequest`.`createdby` AS `RequestCreatedby`,
        `bbrequest`.`RequestType_id` AS `BBGeneralTransactionType_id`
    FROM
        ((((((((`bbtemplate`
        LEFT JOIN `customer` ON ((`bbtemplate`.`createdby` = `customer`.`id`)))
        LEFT JOIN `customeraccounts` ON (((`bbtemplate`.`createdby` = `customeraccounts`.`Customer_id`)
            AND (`bbtemplate`.`DebitAccount` = `customeraccounts`.`Account_id`))))
        LEFT JOIN `bbstatus` ON ((`bbtemplate`.`Status` = `bbstatus`.`id`)))
        LEFT JOIN `bbtransactiontype` ON ((`bbtemplate`.`TransactionType_id` = `bbtransactiontype`.`TransactionType_id`)))
        LEFT JOIN `bbtemplatetype` ON ((`bbtemplate`.`TemplateType_id` = `bbtemplatetype`.`TemplateType_id`)))
        LEFT JOIN `bbtemplaterequesttype` ON ((`bbtemplate`.`TemplateRequestType_id` = `bbtemplaterequesttype`.`TemplateRequestType_id`)))
        LEFT JOIN `organisation` ON ((`bbtemplate`.`Company_id` = `organisation`.`id`)))
        LEFT JOIN `bbrequest` ON ((`bbtemplate`.`Request_id` = `bbrequest`.`Request_id`)));

DROP VIEW IF EXISTS `customeraccountsview`;

CREATE VIEW `customeraccountsview` AS    SELECT DISTINCT`membershipaccounts`.`Membership_id` AS `Membership_id`,`membershipaccounts`.`Taxid` AS `Taxid`,`customeraccounts`.`Customer_id` AS `Customer_id`,`customeraccounts`.`Customer_id` AS `User_id`,`accounts`.`Account_id` AS `Account_id`,`accounts`.`Type_id` AS `Type_id`,`accounts`.`UserName` AS `userName`,`accounts`.`CurrencyCode` AS `currencyCode`,`accounts`.`AccountHolder` AS `accountHolder`,`accounts`.`error` AS `error`,`accounts`.`Address` AS `Address`,`accounts`.`Scheme` AS `Scheme`,`accounts`.`Number` AS `number`,`accounts`.`AvailableBalance` AS `availableBalance`,`accounts`.`CurrentBalance` AS `currentBalance`,`accounts`.`InterestRate` AS `interestRate`,`accounts`.`AvailableCredit` AS `availableCredit`,`accounts`.`MinimumDue` AS `minimumDue`,`accounts`.`DueDate` AS `dueDate`,`accounts`.`FirstPaymentDate` AS `firstPaymentDate`,`accounts`.`ClosingDate` AS `closingDate`,`accounts`.`PaymentTerm` AS `paymentTerm`,`accounts`.`OpeningDate` AS `openingDate`,`accounts`.`MaturityDate` AS `maturityDate`,`accounts`.`DividendLastPaidAmount` AS `dividendLastPaidAmount`,`accounts`.`DividendLastPaidDate` AS `dividendLastPaidDate`,`accounts`.`DividendPaidYTD` AS `dividendPaidYTD`,`accounts`.`DividendRate` AS `dividendRate`,`accounts`.`DividendYTD` AS `dividendYTD`,`accounts`.`EStatementmentEnable` AS `eStatementEnable`,`customeraccounts`.`FavouriteStatus` AS `favouriteStatus`,`accounts`.`StatusDesc` AS `statusDesc`,`accounts`.`NickName` AS `nickName`,`accounts`.`OriginalAmount` AS `originalAmount`,`accounts`.`OutstandingBalance` AS `outstandingBalance`,`accounts`.`PaymentDue` AS `paymentDue`,`accounts`.`PaymentMethod` AS `paymentMethod`,`accounts`.`SwiftCode` AS `swiftCode`,`accounts`.`TotalCreditMonths` AS `totalCreditMonths`,`accounts`.`TotalDebitsMonth` AS `totalDebitsMonth`,`accounts`.`RoutingNumber` AS `routingNumber`,`accounts`.`SupportBillPay` AS `supportBillPay`,`accounts`.`SupportCardlessCash` AS `supportCardlessCash`,`accounts`.`SupportTransferFrom` AS `supportTransferFrom`,`accounts`.`SupportTransferTo` AS `supportTransferTo`,`accounts`.`SupportDeposit` AS `supportDeposit`,`accounts`.`UnpaidInterest` AS `unpaidInterest`,`accounts`.`PreviousYearsDividends` AS `previousYearsDividends`,`accounts`.`principalBalance` AS `principalBalance`,`accounts`.`PrincipalValue` AS `principalValue`,`accounts`.`RegularPaymentAmount` AS `regularPaymentAmount`,`accounts`.`phone` AS `phoneId`,`accounts`.`LastDividendPaidDate` AS `lastDividendPaidDate`,`accounts`.`LastDividendPaidAmount` AS `lastDividendPaidAmount`,`accounts`.`LastPaymentAmount` AS `lastPaymentAmount`,`accounts`.`LastPaymentDate` AS `lastPaymentDate`,`accounts`.`LastStatementBalance` AS `lastStatementBalance`,`accounts`.`LateFeesDue` AS `lateFeesDue`,`accounts`.`maturityAmount` AS `maturityAmount`,`accounts`.`MaturityOption` AS `maturityOption`,`accounts`.`payoffAmount` AS `payoffAmount`,`accounts`.`PayOffCharge` AS `payOffCharge`,`accounts`.`PendingDeposit` AS `pendingDeposit`,`accounts`.`PendingWithdrawal` AS `pendingWithdrawal`,`accounts`.`JointHolders` AS `jointHolders`,`accounts`.`IsPFM` AS `isPFM`,`accounts`.`InterestPaidYTD` AS `interestPaidYTD`,`accounts`.`InterestPaidPreviousYTD` AS `interestPaidPreviousYTD`,`accounts`.`InterestPaidLastYear` AS `interestPaidLastYear`,`accounts`.`InterestEarned` AS `interestEarned`,`accounts`.`CurrentAmountDue` AS `currentAmountDue`,`accounts`.`CreditLimit` AS `creditLimit`,`accounts`.`CreditCardNumber` AS `creditCardNumber`,`accounts`.`BsbNum` AS `bsbNum`,`accounts`.`BondInterestLastYear` AS `bondInterestLastYear`,`accounts`.`BondInterest` AS `bondInterest`,`accounts`.`AvailablePoints` AS `availablePoints`,`accounts`.`AccountName` AS `accountName`,`accounts`.`email` AS `email`,`accounts`.`IBAN` AS `IBAN`,`accounts`.`adminProductId` AS `adminProductId`,`bank`.`Description` AS `bankname`,`accounts`.`AccountPreference` AS `accountPreference`,`accounttype`.`transactionLimit` AS `transactionLimit`,`accounttype`.`transferLimit` AS `transferLimit`,`accounttype`.`rates` AS `rates`,`accounttype`.`termsAndConditions` AS `termsAndConditions`,`accounttype`.`TypeDescription` AS `typeDescription`,`accounttype`.`supportChecks` AS `supportChecks`,`accounttype`.`displayName` AS `displayName`,`accounts`.`accountSubType` AS `accountSubType`,`accounts`.`description` AS `description`,`accounts`.`schemeName` AS `schemeName`,`accounts`.`identification` AS `identification`,`accounts`.`secondaryIdentification` AS `secondaryIdentification`,`accounts`.`servicerSchemeName` AS `servicerSchemeName`,`accounts`.`servicerIdentification` AS `servicerIdentification`,`accounts`.`dataCreditDebitIndicator` AS `dataCreditDebitIndicator`,`accounts`.`dataType` AS `dataType`,`accounts`.`dataDateTime` AS `dataDateTime`,`accounts`.`dataCreditLineIncluded` AS `dataCreditLineIncluded`,`accounts`.`dataCreditLineType` AS `dataCreditLineType`,`accounts`.`dataCreditLineAmount` AS `dataCreditLineAmount`,`accounts`.`dataCreditLineCurrency` AS `dataCreditLineCurrency`    FROM((((`accounts`JOIN `customeraccounts`)JOIN `accounttype`)LEFT JOIN `membershipaccounts` ON ((`accounts`.`Account_id` = `membershipaccounts`.`Account_id`)))LEFT JOIN `bank` ON ((`accounts`.`Bank_id` = `bank`.`id`)))    WHERE((`accounts`.`Account_id` = `customeraccounts`.`Account_id`)    AND (`accounts`.`Type_id` = `accounttype`.`TypeID`));

ALTER TABLE `transaction` ADD COLUMN `serviceName` VARCHAR(45) NULL AFTER `dataStatus`;

CREATE TABLE `mfaserviceconfig` (
  `serviceName` varchar(50) NOT NULL,
  `transactionType` varchar(50) DEFAULT NULL,
  `field` varchar(50) DEFAULT NULL,
  `value` varchar(50) DEFAULT NULL,
  `appId` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`serviceName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


ALTER TABLE `customeraccounts` ADD INDEX `idx_customeraccounts_Customer_id` (`Customer_id` ASC);
ALTER TABLE `customeraccounts` ADD INDEX `idx_customeraccounts_Account_id` (`Account_id` ASC);
ALTER TABLE `membershipaccounts` ADD INDEX `idx_membershipaccounts_Account_id` (`Account_id` ASC);


