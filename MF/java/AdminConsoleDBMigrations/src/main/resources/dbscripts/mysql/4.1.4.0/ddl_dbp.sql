ALTER TABLE `application` 
ADD COLUMN `isBusinessBankingEnabled` VARCHAR(45) NULL AFTER `fundingAmount`;

ALTER TABLE `customer` 
CHANGE COLUMN `Username` `UserName` VARCHAR(50) NOT NULL ;

ALTER TABLE `customer` 
CHANGE COLUMN `FullName` `FullName` VARCHAR(150) NULL DEFAULT NULL ;

ALTER TABLE `customer` 
CHANGE COLUMN `LockCount` `lockCount` INT(11) NULL DEFAULT NULL ;

ALTER TABLE `customer` 
DROP FOREIGN KEY `FK_Customer_Status_04`;
ALTER TABLE `customer` 
CHANGE COLUMN `OlbEnrollmentStatus_id` `OlbEnrolmentStatus_id` VARCHAR(50) NULL DEFAULT NULL ;
ALTER TABLE `customer` 
ADD CONSTRAINT `FK_Customer_Status_04`
  FOREIGN KEY (`OlbEnrolmentStatus_id`)
  REFERENCES `status` (`id`);

ALTER TABLE `customeraccounts` 
CHANGE COLUMN `MemberId` `Membership_id` VARCHAR(50) NULL DEFAULT NULL ;

DROP TABLE IF EXISTS `newaccount`;
CREATE TABLE `newaccount` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `firstName` varchar(50) DEFAULT NULL,
  `lastName` varchar(50) DEFAULT NULL,
  `address` varchar(50) DEFAULT NULL,
  `dateofbirth` date DEFAULT NULL,
  `ssn` varchar(50) DEFAULT NULL,
  `accountType` int(11) DEFAULT NULL,
  `locationId` int(11) DEFAULT NULL,
  `productId` int(11) DEFAULT NULL,
  `userId` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `accountType` (`accountType`),
  KEY `userId` (`userId`),
  KEY `locationId` (`locationId`),
  KEY `productId` (`productId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP VIEW IF EXISTS `memtinaccountsview`;
CREATE VIEW `memtinaccountsview` AS select distinct `accounttype`.`TypeDescription` AS `Account_Type`,`customeraccounts`.`Customer_id` AS `Customer_id`,`customeraccounts`.`Account_id` AS `Account_id`,`customeraccounts`.`AccountName` AS `accountName`,`organisationmembership`.`Organization_id` AS `Organization_Id`,`organisationmembership`.`Membership_id` AS `Membership_id`,`organisationmembership`.`Taxid` AS `Taxid` from ((`accounttype` join `accounts`) join ((`customeraccounts` join `organisationemployees`) join `organisationmembership`)) where ((`accounttype`.`TypeID` = `accounts`.`Type_id`) and (`accounts`.`Account_id` = `customeraccounts`.`Account_id`) and (`customeraccounts`.`Membership_id` = `organisationmembership`.`Membership_id`));

DROP VIEW IF EXISTS `organisationaccountsview`;
CREATE VIEW `organisationaccountsview` AS select distinct `accounttype`.`TypeDescription` AS `Account_Type`,`customeraccounts`.`Customer_id` AS `Customer_id`,`customeraccounts`.`Account_id` AS `Account_id`,`customeraccounts`.`AccountName` AS `accountName`,`customer`.`LastName` AS `LastName`,`customer`.`DateOfBirth` AS `DateOfBirth`,`customer`.`Ssn` AS `Ssn`,`customer`.`Phone` AS `phone`,`customer`.`Email` AS `Email`,`customer`.`UserName` AS `username`,`customer`.`Gender` AS `Gender`,`customer`.`FirstName` AS `FirstName`,`customer`.`CustomerType` AS `CustomerType`,`customer`.`Organization_Id` AS `Organization_Id`,`organisationmembership`.`Membership_id` AS `Membership_id`,`organisationmembership`.`Taxid` AS `Taxid` from (((`accounttype` join `accounts`) join (`customeraccounts` join `customer`)) join (`organisationemployees` join `organisationmembership`)) where ((`accounttype`.`TypeID` = `accounts`.`Type_id`) and (`accounts`.`Account_id` = `customeraccounts`.`Account_id`) and (`customer`.`id` = `customeraccounts`.`Customer_id`) and ((`customer`.`id` = `organisationemployees`.`Customer_id`) or (`customer`.`Organization_Id` = `organisationemployees`.`Organization_id`)) and (`organisationemployees`.`Organization_id` = `organisationmembership`.`Organization_id`));

ALTER TABLE `organisationaddress` 
CHANGE COLUMN `createdts` `createdts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ,
CHANGE COLUMN `lastmodifiedts` `lastmodifiedts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ,
CHANGE COLUMN `synctimestamp` `synctimestamp` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `othersourceofincome` 
ADD COLUMN `Customer_id` VARCHAR(50) NULL AFTER `WeekWorkingHours`,
ADD COLUMN `SourceOfIncomeName` VARCHAR(50) NULL AFTER `Customer_id`;

DROP PROCEDURE IF EXISTS `get_customer_applicantinfo_proc`;
DELIMITER $$
CREATE PROCEDURE `get_customer_applicantinfo_proc`(IN `Customer_id` varchar(20000))
BEGIN
Select `customer`.`id` AS Customer_id,
`customeraddress`.`Address_id` AS Address_id,
`customeraddress`.`Type_id` AS Type_id,
`customeraddress`.`DurationOfStay` AS DurationOfStay,
`customeraddress`.`HomeOwnership` AS HomeOwnership,
`customer`.`FirstName` AS FirstName,
`customer`.`MiddleName` AS MiddleName,
`customer`.`LastName` AS LastName,
`customer`.`DateOfBirth` AS DateOfBirth,
`customer`.`Ssn` AS Ssn,
`customer`.`PreferredContactMethod` AS PreferredContactMethod,
`address`.`AddressLine1` AS AddressLine1,
`address`.`ZipCode` AS ZipCode,
`region`.`Name` AS State,
`city`.`Name` AS city,
`country`.`Name` AS Country,
(Select `customercommunication`.`Value` from customercommunication where `customercommunication`.Type_id = 'COMM_TYPE_PHONE' AND `customercommunication`.`isPrimary`=1 AND `customercommunication`.`Customer_id`=Customer_id limit 1) AS PrimaryContactMethod,
(Select `customercommunication`.`Value` from customercommunication where `customercommunication`.Type_id = 'COMM_TYPE_EMAIL' AND customercommunication.isPrimary=1 AND `customercommunication`.`Customer_id`=Customer_id limit 1) AS Email
FROM
	customer customer
	LEFT JOIN
	customeraddress customeraddress ON customeraddress.Customer_id = customer.id
    LEFT JOIN
    address address ON customeraddress.Address_id = address.id
    LEFT JOIN
    region region ON address.Region_id=region.id
    LEFT JOIN
    city city ON address.City_id=city.id
    LEFT JOIN
    country country ON region.Country_id=country.id
    LEFT JOIN
    customercommunication customercommunication ON customer.id=customercommunication.Customer_id
WHERE
	customer.id=Customer_id AND customercommunication.isPrimary=1 AND (customeraddress.Type_id = "ADR_TYPE_HOME" OR isnull(customeraddress.Type_id)) group by customer.id;
END $$
DELIMITER ;


DROP PROCEDURE IF EXISTS `get_customer_employement_details_proc`;
DELIMITER $$
CREATE PROCEDURE `get_customer_employement_details_proc`(IN Customer_id varchar(20000))
BEGIN
Select `employementdetails`.`Customer_id` AS Customer_id,
`employementdetails`.`EmploymentType` AS EmployementType,
`employementdetails`.`CurrentEmployer` AS CurrentEmployer,
`employementdetails`.`Designation` AS Designation,
`employementdetails`.`PayPeriod` AS PayPeriod,
`employementdetails`.`GrossIncome` AS GrossIncome,
`employementdetails`.`WeekWorkingHours` AS WeekWorkingHours,
`employementdetails`.`EmploymentStartDate` AS EmployementStartDate,
`employementdetails`.`PreviousEmployer` AS PreviousEmployer,
`employementdetails`.`OtherEmployementType` AS OtherEmployementType,
`employementdetails`.`OtherEmployementDescription` AS OtherEmployementDescription,
`employementdetails`.`PreviousDesignation` AS PreviousDesignation,
`othersourceofincome`.`SourceType` AS OtherIncomeSourceType,
`othersourceofincome`.`PayPeriod` AS OtherIncomeSourcePayPeriod,
`othersourceofincome`.`GrossIncome` AS OtherGrossIncomeValue,
`othersourceofincome`.`WeekWorkingHours` AS OtherIncomeSourceWorkingHours,
`othersourceofincome`.`SourceOfIncomeDescription` AS OtherSourceOfIncomeDescription,
`othersourceofincome`.`SourceOfIncomeName` AS OtherSourceOfIncomeName,
`customeraddress`.`Address_id` AS Address_id,
`customeraddress`.`Type_id` AS Type_id,
`address`.`Region_id` AS Region_id,
`address`.`City_id` AS City_id,
`address`.`AddressLine1` AS AddressLine1,
`address`.`AddressLine2` AS AddressLine2,
`address`.`AddressLine3` AS AddressLine3,
`address`.`ZipCode` AS ZipCode,
`region`.`Name` AS State,
`city`.`Name` AS city,
`country`.`Name` AS Country
FROM
	employementdetails employementdetails
	LEFT JOIN
	othersourceofincome othersourceofincome ON employementdetails.Customer_id = othersourceofincome.Customer_id
    LEFT JOIN
    customeraddress customeraddress ON customeraddress.Customer_id = othersourceofincome.Customer_id
    LEFT JOIN
    address address ON address.id=customeraddress.Address_id
    LEFT JOIN
    region region ON address.Region_id=region.id
    LEFT JOIN
    city city ON region.id=city.Region_id
    LEFT JOIN
    country country ON city.Country_id=country.id
WHERE
	employementdetails.Customer_id=Customer_id AND customeraddress.Type_id="ADR_TYPE_WORK" group by employementdetails.Customer_id;
END $$
DELIMITER ;

DROP VIEW IF EXISTS `customerorganisationmembershipview`;
CREATE VIEW `customerorganisationmembershipview` AS select distinct `customer`.`LastName` AS `LastName`,`customer`.`DateOfBirth` AS `DateOfBirth`,`customer`.`Ssn` AS `Ssn`,`customer`.`Phone` AS `Phone`,`customer`.`Email` AS `Email`,`customer`.`UserName` AS `UserName`,`customer`.`Gender` AS `Gender`,`customer`.`id` AS `id`,`customer`.`FirstName` AS `FirstName`,`customer`.`CustomerType` AS `CustomerType`,`customer`.`Organization_Id` AS `Organization_Id`,`organisationmembership`.`Membership_id` AS `Membership_id`,`organisationmembership`.`Taxid` AS `Taxid` from ((`customer` join `organisationemployees`) join `organisationmembership`) where (((`customer`.`Organization_Id` = `organisationemployees`.`Organization_id`) or (`customer`.`id` = `organisationemployees`.`Customer_id`)) and (`organisationemployees`.`Organization_id` = `organisationmembership`.`Organization_id`));

DROP VIEW IF EXISTS `customerview`;
CREATE VIEW `customerview` AS select `c`.`id` AS `id`,`c`.`FirstName` AS `FirstName`,`c`.`LastName` AS `LastName`,`c`.`UserName` AS `UserName`,`cc`.`Value` AS `Value`,`ct`.`Description` AS `description` from ((`customercommunication` `cc` join `customer` `c`) join `communicationtype` `ct`) where ((`cc`.`Type_id` = `ct`.`id`) and (`cc`.`Customer_id` = `c`.`id`));

DROP VIEW IF EXISTS `organisationview`;
CREATE VIEW `organisationview` AS select `organisation`.`id` AS `org_id`,`organisation`.`Name` AS `org_Name`,`organisationmembership`.`Organization_id` AS `orgmem_orgid`,`organisationmembership`.`Membership_id` AS `orgmem_memid`,`organisationcommunication`.`id` AS `orgcomm_id`,`organisationcommunication`.`Sequence` AS `orgcomm_Sequence`,`organisationcommunication`.`Value` AS `orgcomm_Value`,`organisationcommunication`.`Extension` AS `orgcomm_Extension`,`organisationcommunication`.`Description` AS `orgcomm_Description`,`organisationcommunication`.`IsPreferredContactMethod` AS `orgcomm_IsPreferredContactMethod`,`organisationcommunication`.`softdeleteflag` AS `orgcomm_softdeleteflag`,`organisationcommunication`.`Type_id` AS `orgcomm_Typeid`,`organisationcommunication`.`Organization_id` AS `orgcomm_orgid`,`organisationmembership`.`Taxid` AS `orgmem_taxid` from ((`organisation` join `organisationmembership`) join `organisationcommunication`) where ((`organisation`.`id` = `organisationmembership`.`Organization_id`) and (`organisation`.`id` = `organisationcommunication`.`Organization_id`)) group by `organisation`.`id`;

DROP VIEW IF EXISTS `organizationownerview`;
CREATE VIEW `organizationownerview` AS select distinct `organisationowner`.`LastName` AS `LastName`,`organisationowner`.`DateOfBirth` AS `DateOfBirth`,`organisationowner`.`MidleName` AS `Ssn`,`organisationowner`.`Phone` AS `Phone`,`organisationowner`.`Email` AS `Email`,`organisationowner`.`FirstName` AS `UserName`,`organisationowner`.`IDType_id` AS `IDType_id`,`organisationowner`.`IdValue` AS `IdValue`,`organisationowner`.`Organization_id` AS `Organization_Id`,`organisationmembership`.`Membership_id` AS `Membership_id`,`organisationmembership`.`Taxid` AS `Taxid` from (`organisationowner` join `organisationmembership`) where (`organisationowner`.`Organization_id` = `organisationmembership`.`Organization_id`);

ALTER TABLE `queryresponseconsent` 
DROP FOREIGN KEY `FK_QueryResponseConsent_Disclaimer`;
ALTER TABLE `queryresponseconsent` 
DROP INDEX `IXFK_QueryResponseConsent_Disclaimer` ;


CREATE TABLE `bbtransactiontype` (
  `TransactionType_id` int(11) NOT NULL AUTO_INCREMENT,
  `TransactionTypeName` varchar(45) NOT NULL,
  PRIMARY KEY (`TransactionType_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

CREATE TABLE `achaccountstype` (
  `id` int(2) NOT NULL,
  `accountType` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `bbtemplatetype` (
  `TemplateType_id` int(11) NOT NULL AUTO_INCREMENT,
  `TemplateTypeName` varchar(45) NOT NULL,
  PRIMARY KEY (`TemplateType_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

CREATE TABLE `achfileformattype` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `FileType` varchar(45) NOT NULL,
  `Fileextension` varchar(10) DEFAULT NULL,
  `MIMEtype` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

CREATE TABLE `bbtaxtype` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `taxType` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `bbtemplaterequesttype` (
  `TemplateRequestType_id` int(11) NOT NULL AUTO_INCREMENT,
  `TemplateRequestTypeName` varchar(45) NOT NULL,
  `TransactionType_id` int(11) NOT NULL,
  PRIMARY KEY (`TemplateRequestType_id`),
  KEY `FK_bbtemplaterequesttype_TransactionType_id_idx` (`TransactionType_id`),
  CONSTRAINT `FK_bbtemplaterequesttype_TransactionType_id` FOREIGN KEY (`TransactionType_id`) REFERENCES `bbtransactiontype` (`TransactionType_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;

CREATE TABLE `bbtaxsubtype` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `taxSubType` varchar(150) NOT NULL,
  `taxType` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_bbtaxsubtype_bbtaxtype` (`taxType`),
  CONSTRAINT `FK_bbtaxsubtype_bbtaxtype` FOREIGN KEY (`taxType`) REFERENCES `bbtaxtype` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;

CREATE TABLE `bbstatus` (
  `id` int(2) NOT NULL,
  `status` tinytext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `bbgeneraltransactiontype` (
  `TransactionType` varchar(50) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8;

CREATE TABLE `bbtransaction` (
  `Transaction_id` int(11) NOT NULL AUTO_INCREMENT,
  `DebitAccount` varchar(45) DEFAULT NULL,
  `EffectiveDate` date DEFAULT NULL,
  `Request_id` bigint(20) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `MaxAmount` double DEFAULT NULL,
  `Status` int(2) DEFAULT NULL,
  `TransactionType_id` int(11) DEFAULT NULL,
  `TemplateType_id` int(11) DEFAULT NULL,
  `Company_id` int(11) DEFAULT NULL,
  `TemplateRequestType_id` int(11) DEFAULT NULL,
  `softDelete` int(1) NOT NULL DEFAULT '0',
  `TemplateName` varchar(45) DEFAULT 'No Template Used',
  `ConfirmationNumber` varchar(45) DEFAULT NULL,
  `ActedBy` varchar(50) DEFAULT NULL,
  `Template_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`Transaction_id`),
  KEY `FK_bbtransaction_Transaction_Type_idx` (`TransactionType_id`),
  KEY `FK_bbtransaction_Template_Type_idx` (`TemplateType_id`),
  KEY `FK_bbtransaction_Organization_idx` (`Company_id`),
  KEY `FK_bbtransaction_TemplateRequest_Type_idx` (`TemplateRequestType_id`),
  KEY `FK_bbtransaction_bbstatus_idx` (`Status`),
  KEY `FK_bbtransaction_accounts_idx` (`DebitAccount`),
  KEY `FK_bbtransaction_user_idx` (`createdby`),
  CONSTRAINT `FK_bbtransaction_accounts` FOREIGN KEY (`DebitAccount`) REFERENCES `accounts` (`Account_id`),
  CONSTRAINT `FK_bbtransaction_bbstatus` FOREIGN KEY (`Status`) REFERENCES `bbstatus` (`id`),
  CONSTRAINT `FK_bbtransaction_user` FOREIGN KEY (`createdby`) REFERENCES `customer` (`id`),
  CONSTRAINT `FK_bbtransaction_Transaction_Type` FOREIGN KEY (`TransactionType_id`) REFERENCES `bbtransactiontype` (`TransactionType_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_bbtransaction_Organization` FOREIGN KEY (`Company_id`) REFERENCES `organisation` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_bbtransaction_Template_Type` FOREIGN KEY (`TemplateType_id`) REFERENCES `bbtemplatetype` (`TemplateType_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_bbtransaction_TemplateRequest_Type` FOREIGN KEY (`TemplateRequestType_id`) REFERENCES `bbtemplaterequesttype` (`TemplateRequestType_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8;

CREATE TABLE `bbtemplate` (
  `Template_id` int(11) NOT NULL AUTO_INCREMENT,
  `TemplateName` varchar(45) DEFAULT NULL,
  `TemplateDescription` varchar(45) DEFAULT NULL,
  `DebitAccount` varchar(45) DEFAULT NULL,
  `EffectiveDate` date DEFAULT NULL,
  `MaxAmount` double DEFAULT NULL,
  `Request_id` int(11) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT NULL,
  `Status` int(2) DEFAULT NULL,
  `UpdatedBy` varchar(50) DEFAULT NULL,
  `Updatedts` timestamp NULL DEFAULT NULL,
  `TransactionType_id` int(11) DEFAULT NULL,
  `TemplateType_id` int(11) DEFAULT NULL,
  `Company_id` int(11) DEFAULT NULL,
  `TemplateRequestType_id` int(11) DEFAULT NULL,
  `softDelete` int(1) NOT NULL DEFAULT '0',
  `ActedBy` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`Template_id`),
  KEY `FK_bbtemplate_Transaction_Type_idx` (`TransactionType_id`),
  KEY `FK_bbtemplate_Template_Type_idx` (`TemplateType_id`),
  KEY `FK_bbtemplate_Organization_idx` (`Company_id`),
  KEY `FK_bbtemplate_TemplateRequest_Type_idx` (`TemplateRequestType_id`),
  KEY `FK_bbtemplate_bbstatus_idx` (`Status`),
  KEY `FK_bbtemplate_user_idx` (`createdby`),
  KEY `FK_bbtemplate_accounts_idx` (`DebitAccount`),
  KEY `FK_bbtemplate_user_2_idx` (`UpdatedBy`),
  CONSTRAINT `FK_bbtemplate_accounts` FOREIGN KEY (`DebitAccount`) REFERENCES `accounts` (`Account_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_bbtemplate_bbstatus` FOREIGN KEY (`Status`) REFERENCES `bbstatus` (`id`),
  CONSTRAINT `FK_bbtemplate_user` FOREIGN KEY (`createdby`) REFERENCES `customer` (`id`),
  CONSTRAINT `FK_bbtemplate_user_2` FOREIGN KEY (`UpdatedBy`) REFERENCES `customer` (`id`),
  CONSTRAINT `FK_bbtemplate_Template_Type` FOREIGN KEY (`TemplateType_id`) REFERENCES `bbtemplatetype` (`TemplateType_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_bbtemplate_TemplateRequest_Type` FOREIGN KEY (`TemplateRequestType_id`) REFERENCES `bbtemplaterequesttype` (`TemplateRequestType_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_bbtemplate_Transaction_Type` FOREIGN KEY (`TransactionType_id`) REFERENCES `bbtransactiontype` (`TransactionType_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_bbtemplate_Organization` FOREIGN KEY (`Company_id`) REFERENCES `organisation` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8;

CREATE TABLE `bbgeneraltransaction` (
  `Transaction_id` int(11) NOT NULL AUTO_INCREMENT,
  `TransactionDate` date DEFAULT NULL,
  `Payee` varchar(50) DEFAULT NULL,
  `Amount` double DEFAULT NULL,
  `Frequency` int(11) DEFAULT NULL,
  `Status` int(11) DEFAULT NULL,
  `Reccurence` int(11) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT NULL,
  `Request_id` bigint(20) DEFAULT NULL,
  `UpdatedBy` varchar(50) DEFAULT NULL,
  `updatedts` timestamp NULL DEFAULT NULL,
  `DebitOrCreditAccount` varchar(45) DEFAULT NULL,
  `BBGeneralTransactionType_id` int(11) DEFAULT NULL,
  `Company_id` int(11) DEFAULT NULL,
  `ActedBy` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`Transaction_id`),
  KEY `FK_bbgeneraltransaction_user_idx` (`createdby`),
  KEY `FK_bbgeneraltransaction_user_2_idx` (`UpdatedBy`),
  KEY `FK_bbgeneraltransaction_bbstatus_idx` (`Status`),
  KEY `FK_bbgeneraltransaction_accounts_idx` (`DebitOrCreditAccount`),
  KEY `FK_bbgeneraltransaction_TransactionType_idx` (`BBGeneralTransactionType_id`),
  KEY `FK_bbgeneraltransaction_Organization_idx` (`Company_id`),
  CONSTRAINT `FK_bbgeneraltransaction_bbstatus` FOREIGN KEY (`Status`) REFERENCES `bbstatus` (`id`),
  CONSTRAINT `FK_bbgeneraltransaction_user` FOREIGN KEY (`createdby`) REFERENCES `customer` (`id`),
  CONSTRAINT `FK_bbgeneraltransaction_user_2` FOREIGN KEY (`UpdatedBy`) REFERENCES `customer` (`id`),
  CONSTRAINT `FK_bbgeneraltransaction_Accounts` FOREIGN KEY (`DebitOrCreditAccount`) REFERENCES `accounts` (`Account_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_bbgeneraltransaction_GeneralTransactionType` FOREIGN KEY (`BBGeneralTransactionType_id`) REFERENCES `bbgeneraltransactiontype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_bbgeneraltransaction_Organization` FOREIGN KEY (`Company_id`) REFERENCES `organisation` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8 COMMENT='This table controls the common(non-ACH) transactions.';

CREATE TABLE `bbtransactionrecord` (
  `TransactionRecord_id` int(11) NOT NULL AUTO_INCREMENT,
  `ToAccountNumber` varchar(45) DEFAULT NULL,
  `ToAccountType` int(11) DEFAULT NULL,
  `ABATRCNumber` varchar(45) DEFAULT NULL,
  `Detail_id` varchar(45) DEFAULT NULL,
  `Amount` double DEFAULT NULL,
  `AdditionalInfo` varchar(500) DEFAULT NULL,
  `EIN` varchar(45) DEFAULT NULL,
  `IsZeroTaxDue` tinyint(4) DEFAULT NULL,
  `TaxType_id` int(11) DEFAULT NULL,
  `Transaction_id` int(11) DEFAULT NULL,
  `softDelete` int(1) NOT NULL DEFAULT '0',
  `TemplateRequestType_id` int(11) DEFAULT NULL,
  `Record_Name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`TransactionRecord_id`),
  KEY `FK_bbtransactionrecord_TemplateRequest_Type_idx` (`TemplateRequestType_id`),
  KEY `FK_bbtransactionrecord_Tax_Type_idx` (`TaxType_id`),
  KEY `FK_bbtransactionrecord_Achaccount_Type_idx` (`ToAccountType`),
  KEY `FK_bbtransactionrecord_TransactionId_idx` (`Transaction_id`),
  CONSTRAINT `FK_bbtransactionrecord_Tax_Type` FOREIGN KEY (`TaxType_id`) REFERENCES `bbtaxtype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_bbtransactionrecord_TransactionId` FOREIGN KEY (`Transaction_id`) REFERENCES `bbtransaction` (`Transaction_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_bbtransactionrecord_Achaccount_Type` FOREIGN KEY (`ToAccountType`) REFERENCES `achaccountstype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_bbtransactionrecord_TemplateRequest_Type` FOREIGN KEY (`TemplateRequestType_id`) REFERENCES `bbtemplaterequesttype` (`TemplateRequestType_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8;

CREATE TABLE `bbtemplaterecord` (
  `TemplateRecord_id` int(11) NOT NULL AUTO_INCREMENT,
  `Record_Name` varchar(45) DEFAULT NULL,
  `ToAccountNumber` varchar(45) DEFAULT NULL,
  `ABATRCNumber` varchar(45) DEFAULT NULL,
  `Detail_id` varchar(45) DEFAULT NULL,
  `Amount` double DEFAULT NULL,
  `AdditionalInfo` varchar(500) DEFAULT NULL,
  `EIN` varchar(45) DEFAULT NULL,
  `IsZeroTaxDue` tinyint(4) DEFAULT NULL,
  `Template_id` int(11) DEFAULT NULL,
  `TaxType_id` int(11) DEFAULT NULL,
  `TemplateRequestType_id` int(11) DEFAULT NULL,
  `softDelete` int(1) DEFAULT NULL,
  `ToAccountType` int(2) DEFAULT NULL,
  PRIMARY KEY (`TemplateRecord_id`),
  KEY `FK_bbtemplaterecord_Template_id_idx` (`Template_id`),
  KEY `FK_bbtemplaterecord_Tax_Type_id_idx` (`TaxType_id`),
  KEY `FK_bbtemplaterecord_Template_Request_Type_id_idx` (`TemplateRequestType_id`),
  KEY `FK_bbtemplaterecord_Account_Type_id_idx` (`ToAccountType`),
  CONSTRAINT `FK_bbtemplaterecord_Account_Type_id` FOREIGN KEY (`ToAccountType`) REFERENCES `achaccountstype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_bbtemplaterecord_Tax_Type_id` FOREIGN KEY (`TaxType_id`) REFERENCES `bbtaxtype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_bbtemplaterecord_Template_id` FOREIGN KEY (`Template_id`) REFERENCES `bbtemplate` (`Template_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_bbtemplaterecord_Template_Request_Type_id` FOREIGN KEY (`TemplateRequestType_id`) REFERENCES `bbtemplaterequesttype` (`TemplateRequestType_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=utf8;

CREATE TABLE `bbtransactionsubrecord` (
  `TranscationSubRecord_id` int(11) NOT NULL,
  `Amount` double NOT NULL,
  `TransactionRecord_id` int(11) NOT NULL,
  `TaxSubCategory_id` int(11) NOT NULL,
  `softDelete` int(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`TranscationSubRecord_id`),
  KEY `FK_bbtransactionsubrecord_Taxsub_Type_id_idx` (`TaxSubCategory_id`),
  KEY `FK_bbtransactionsubrecord_TransactionRecord_id_idx` (`TransactionRecord_id`),
  CONSTRAINT `FK_bbtransactionsubrecord_Taxsub_Type_id` FOREIGN KEY (`TaxSubCategory_id`) REFERENCES `bbtaxsubtype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_bbtransactionsubrecord_TransactionRecord_id` FOREIGN KEY (`TransactionRecord_id`) REFERENCES `bbtransactionrecord` (`TransactionRecord_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `bbtemplatesubrecord` (
  `TemplateSubRecord_id` int(11) NOT NULL AUTO_INCREMENT,
  `Amount` double DEFAULT NULL,
  `TemplateRecord_id` int(11) NOT NULL,
  `TaxSubCategory_id` int(11) NOT NULL,
  `softDelete` int(11) DEFAULT '0',
  PRIMARY KEY (`TemplateSubRecord_id`),
  KEY `FK_bbtemplatesubrecord_Taxsub_Type_id_idx` (`TaxSubCategory_id`),
  KEY `FK_bbtemplatesubrecord_TemplateRecord_id_idx` (`TemplateRecord_id`),
  CONSTRAINT `FK_bbtemplatesubrecord_Taxsub_Type_id` FOREIGN KEY (`TaxSubCategory_id`) REFERENCES `bbtaxsubtype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_bbtemplatesubrecord_TemplateRecord_id` FOREIGN KEY (`TemplateRecord_id`) REFERENCES `bbtemplaterecord` (`TemplateRecord_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=81 DEFAULT CHARSET=utf8;

CREATE TABLE `achfile` (
  `ACHFile_id` int(11) NOT NULL AUTO_INCREMENT,
  `ACHFileName` varchar(45) DEFAULT NULL,
  `softDelete` tinyint(1) DEFAULT '0',
  `DebitAmount` double DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT NULL,
  `RequestType` varchar(45) DEFAULT NULL,
  `NumberOfCredits` int(11) DEFAULT NULL,
  `NumberOfDebits` int(11) DEFAULT NULL,
  `NumberOfPrenotes` int(11) DEFAULT NULL,
  `Request_id` bigint(20) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `Contents` blob,
  `FileSize` float NOT NULL,
  `CreditAmount` double DEFAULT NULL,
  `NumberOfRecords` int(11) DEFAULT NULL,
  `ACHFileFormatType_id` int(11) DEFAULT NULL,
  `Status` int(2) DEFAULT NULL,
  `Company_id` int(11) DEFAULT NULL,
  `ActedBy` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`ACHFile_id`),
  KEY `FK_achfile_bbstatus_idx` (`Status`),
  KEY `FK_achfile_Company_id_idx` (`Company_id`),
  KEY `FK_achfile_achfileformattype_idx` (`ACHFileFormatType_id`),
  KEY `FK_achfile_user_idx` (`createdby`),
  CONSTRAINT `FK_achfile_Company_id` FOREIGN KEY (`Company_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FK_achfile_achfileformattype` FOREIGN KEY (`ACHFileFormatType_id`) REFERENCES `achfileformattype` (`id`),
  CONSTRAINT `FK_achfile_bbstatus` FOREIGN KEY (`Status`) REFERENCES `bbstatus` (`id`),
  CONSTRAINT `FK_achfile_user` FOREIGN KEY (`createdby`) REFERENCES `customer` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;

CREATE TABLE `bbrequest` (
  `Request_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createdby` varchar(50) DEFAULT NULL,
  `Company_id` int(11) DEFAULT NULL,
  `ReceivedApprovals` int(11) DEFAULT NULL,
  `RequestType_id` int(11) DEFAULT NULL,
  `RequiredApprovals` int(11) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT NULL,
  `Status` int(2) DEFAULT NULL,
  `softDelete` tinyint(2) DEFAULT '0',
  PRIMARY KEY (`Request_id`),
  KEY `FK_bbrequest_user_idx` (`createdby`),
  KEY `FK_bbrequest_bbstatus_idx` (`Status`),
  KEY `FK_bbrequest_businessbankingcompany_idx` (`Company_id`),
  KEY `FK_bbrequest_RequestType_id_idx` (`RequestType_id`),
  CONSTRAINT `FK_bbrequest_RequestType_id` FOREIGN KEY (`RequestType_id`) REFERENCES `bbgeneraltransactiontype` (`id`),
  CONSTRAINT `FK_bbrequest_bbstatus` FOREIGN KEY (`Status`) REFERENCES `bbstatus` (`id`),
  CONSTRAINT `FK_bbrequest_businessbankingcompany` FOREIGN KEY (`Company_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FK_bbrequest_user` FOREIGN KEY (`createdby`) REFERENCES `customer` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8;

CREATE TABLE `bbactedrequest` (
  `Approval_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `Request_id` bigint(20) NOT NULL,
  `Comments` varchar(500) NOT NULL,
  `createdby` varchar(50) NOT NULL,
  `Status` int(2) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT NULL,
  `Action` varchar(45) DEFAULT NULL,
  `Company_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`Approval_id`),
  KEY `FK_bbactedrequest_bbstatus_idx` (`Status`),
  KEY `FK_bbactedrequest_user_idx` (`createdby`),
  KEY `FK_bbactedrequest_Request_id_idx` (`Request_id`),
  KEY `FK_bbactedrequest_Organisation_id_idx` (`Company_id`),
  CONSTRAINT `FK_bbactedrequest_bbstatus` FOREIGN KEY (`Status`) REFERENCES `bbstatus` (`id`),
  CONSTRAINT `FK_bbactedrequest_user_id` FOREIGN KEY (`createdby`) REFERENCES `customer` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_bbactedrequest_Organisation_id` FOREIGN KEY (`Company_id`) REFERENCES `organisation` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_bbactedrequest_Request_id` FOREIGN KEY (`Request_id`) REFERENCES `bbrequest` (`Request_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=109 DEFAULT CHARSET=utf8;

DROP VIEW IF EXISTS `fetch_achfilesdetails_view`;
CREATE VIEW `fetch_achfilesdetails_view` AS select `achfile`.`ACHFile_id` AS `ACHFile_id`,`achfile`.`ACHFileName` AS `ACHFileName`,`achfile`.`softDelete` AS `softDelete`,`achfile`.`DebitAmount` AS `DebitAmount`,`achfile`.`createdts` AS `createdts`,`achfile`.`RequestType` AS `RequestType`,`achfile`.`NumberOfCredits` AS `NumberOfCredits`,`achfile`.`NumberOfDebits` AS `NumberOfDebits`,`achfile`.`NumberOfPrenotes` AS `NumberOfPrenotes`,`achfile`.`Request_id` AS `Request_id`,`achfile`.`Contents` AS `Contents`,`achfile`.`FileSize` AS `FileSize`,`achfile`.`CreditAmount` AS `CreditAmount`,`achfile`.`NumberOfRecords` AS `NumberOfRecords`,`achfile`.`ACHFileFormatType_id` AS `ACHFileFormatType_id`,`achfile`.`Status` AS `Status`,`achfile`.`Company_id` AS `Company_id`,`achfile`.`ActedBy` AS `ActedBy`,`customer`.`id` AS `createdby`,`customer`.`UserName` AS `userName`,`bbstatus`.`status` AS `StatusValue`,`achfileformattype`.`FileType` AS `formatType`,`organisation`.`Name` AS `companyName`,`bbrequest`.`createdby` AS `RequestCreatedby`,`bbrequest`.`RequestType_id` AS `BBGeneralTransactionType_id` from (((((`achfile` left join `customer` on((`customer`.`id` = `achfile`.`createdby`))) left join `bbstatus` on((`bbstatus`.`id` = `achfile`.`Status`))) left join `achfileformattype` on((`achfileformattype`.`id` = `achfile`.`ACHFileFormatType_id`))) left join `organisation` on((`organisation`.`id` = `achfile`.`Company_id`))) left join `bbrequest` on((`achfile`.`Request_id` = `bbrequest`.`Request_id`)));

DROP VIEW IF EXISTS `fetch_achtemplate_details_view`;
CREATE VIEW `fetch_achtemplate_details_view` AS select `bbtemplate`.`Template_id` AS `Template_id`,`bbtemplate`.`TemplateName` AS `TemplateName`,`bbtemplate`.`TemplateDescription` AS `TemplateDescription`,`bbtemplate`.`DebitAccount` AS `DebitAccount`,`bbtemplate`.`EffectiveDate` AS `EffectiveDate`,`bbtemplate`.`Request_id` AS `Request_id`,`bbtemplate`.`createdby` AS `createdby`,`bbtemplate`.`createdts` AS `createdts`,`bbtemplate`.`UpdatedBy` AS `UpdatedBy`,`bbtemplate`.`Updatedts` AS `Updatedts`,`bbtemplate`.`MaxAmount` AS `MaxAmount`,`bbtemplate`.`Status` AS `Status`,`bbtemplate`.`TransactionType_id` AS `TransactionType_id`,`bbtemplate`.`TemplateType_id` AS `TemplateType_id`,`bbtemplate`.`Company_id` AS `Company_id`,`bbtemplate`.`TemplateRequestType_id` AS `TemplateRequestType_id`,`bbtemplate`.`softDelete` AS `softDelete`,`bbtemplate`.`ActedBy` AS `ActedBy`,`accounts`.`AccountName` AS `AccountName`,`customer`.`UserName` AS `CreateBy`,`bbstatus`.`status` AS `StatusValue`,`bbtransactiontype`.`TransactionTypeName` AS `TransactionTypeValue`,`bbtemplatetype`.`TemplateTypeName` AS `TemplateTypeValue`,`bbtemplaterequesttype`.`TemplateRequestTypeName` AS `TemplateRequestTypeValue`,`organisation`.`Name` AS `CompanyName`,`bbrequest`.`createdby` AS `RequestCreatedby`,`bbrequest`.`RequestType_id` AS `BBGeneralTransactionType_id` from ((((((((`bbtemplate` left join `accounts` on((`bbtemplate`.`DebitAccount` = `accounts`.`Account_id`))) left join `customer` on((`bbtemplate`.`createdby` = `customer`.`id`))) left join `bbstatus` on((`bbtemplate`.`Status` = `bbstatus`.`id`))) left join `bbtransactiontype` on((`bbtemplate`.`TransactionType_id` = `bbtransactiontype`.`TransactionType_id`))) left join `bbtemplatetype` on((`bbtemplate`.`TemplateType_id` = `bbtemplatetype`.`TemplateType_id`))) left join `bbtemplaterequesttype` on((`bbtemplate`.`TemplateRequestType_id` = `bbtemplaterequesttype`.`TemplateRequestType_id`))) left join `organisation` on((`bbtemplate`.`Company_id` = `organisation`.`id`))) left join `bbrequest` on((`bbtemplate`.`Request_id` = `bbrequest`.`Request_id`)));

DROP VIEW IF EXISTS `fetch_achtemplatesubrecord_details_view`;
CREATE VIEW `fetch_achtemplatesubrecord_details_view` AS select `bbtemplatesubrecord`.`TemplateSubRecord_id` AS `TemplateSubRecord_id`,`bbtemplatesubrecord`.`TemplateRecord_id` AS `TemplateRecord_id`,`bbtemplatesubrecord`.`TaxSubCategory_id` AS `TaxSubCategory_id`,`bbtemplatesubrecord`.`Amount` AS `Amount`,`bbtemplatesubrecord`.`softDelete` AS `softDelete`,`bbtaxsubtype`.`taxSubType` AS `taxSubType` from (`bbtemplatesubrecord` left join `bbtaxsubtype` on((`bbtemplatesubrecord`.`TaxSubCategory_id` = `bbtaxsubtype`.`id`)));

DROP VIEW IF EXISTS `fetch_achtransaction_details_view`;
CREATE VIEW `fetch_achtransaction_details_view` AS select `bbtransaction`.`Transaction_id` AS `Transaction_id`,`bbtransaction`.`DebitAccount` AS `DebitAccount`,`bbtransaction`.`EffectiveDate` AS `EffectiveDate`,`bbtransaction`.`Request_id` AS `Request_id`,`bbtransaction`.`createdby` AS `createdby`,`bbtransaction`.`createdts` AS `createdts`,`bbtransaction`.`MaxAmount` AS `MaxAmount`,`bbtransaction`.`Status` AS `Status`,`bbtransaction`.`TransactionType_id` AS `TransactionType_id`,`bbtransaction`.`TemplateType_id` AS `TemplateType_id`,`bbtransaction`.`Company_id` AS `Company_id`,`bbtransaction`.`TemplateRequestType_id` AS `TemplateRequestType_id`,`bbtransaction`.`softDelete` AS `softDelete`,`bbtransaction`.`TemplateName` AS `TemplateName`,`bbtransaction`.`Template_id` AS `Template_id`,`bbtransaction`.`ConfirmationNumber` AS `ConfirmationNumber`,`bbtransaction`.`ActedBy` AS `ActedBy`,`accounts`.`AccountName` AS `AccountName`,`customer`.`UserName` AS `userName`,`bbstatus`.`status` AS `StatusValue`,`bbtransactiontype`.`TransactionTypeName` AS `TransactionTypeValue`,`bbtemplatetype`.`TemplateTypeName` AS `TemplateTypeValue`,`bbtemplaterequesttype`.`TemplateRequestTypeName` AS `TemplateRequestTypeValue`,`organisation`.`Name` AS `CompanyName`,`bbrequest`.`createdby` AS `RequestCreatedby`,`bbrequest`.`RequestType_id` AS `BBGeneralTransactionType_id` from ((((((((`bbtransaction` left join `accounts` on((`bbtransaction`.`DebitAccount` = `accounts`.`Account_id`))) left join `customer` on((`bbtransaction`.`createdby` = `customer`.`id`))) left join `bbstatus` on((`bbtransaction`.`Status` = `bbstatus`.`id`))) left join `bbtransactiontype` on((`bbtransaction`.`TransactionType_id` = `bbtransactiontype`.`TransactionType_id`))) left join `bbtemplatetype` on((`bbtransaction`.`TemplateType_id` = `bbtemplatetype`.`TemplateType_id`))) left join `bbtemplaterequesttype` on((`bbtransaction`.`TemplateRequestType_id` = `bbtemplaterequesttype`.`TemplateRequestType_id`))) left join `organisation` on((`bbtransaction`.`Company_id` = `organisation`.`id`))) left join `bbrequest` on((`bbtransaction`.`Request_id` = `bbrequest`.`Request_id`)));

DROP VIEW IF EXISTS `fetch_achtransactionsubrecord_details_view`;
CREATE VIEW `fetch_achtransactionsubrecord_details_view` AS select `bbtransactionsubrecord`.`TranscationSubRecord_id` AS `TranscationSubRecord_id`,`bbtransactionsubrecord`.`TransactionRecord_id` AS `TransactionRecord_id`,`bbtransactionsubrecord`.`TaxSubCategory_id` AS `TaxSubCategory_id`,`bbtransactionsubrecord`.`Amount` AS `Amount`,`bbtransactionsubrecord`.`softDelete` AS `softDelete`,`bbtaxsubtype`.`taxSubType` AS `taxSubType` from (`bbtransactionsubrecord` left join `bbtaxsubtype` on((`bbtransactionsubrecord`.`TaxSubCategory_id` = `bbtaxsubtype`.`id`)));

DROP VIEW IF EXISTS `fetch_generaltransactions_Details_view`;
CREATE VIEW `fetch_generaltransactions_Details_view` AS select `bbgeneraltransaction`.`Transaction_id` AS `Transaction_id`,`bbgeneraltransaction`.`TransactionDate` AS `TransactionDate`,`bbgeneraltransaction`.`Payee` AS `Payee`,`bbgeneraltransaction`.`Amount` AS `Amount`,`bbgeneraltransaction`.`Frequency` AS `Frequency`,`bbgeneraltransaction`.`Status` AS `Status`,`bbgeneraltransaction`.`Reccurence` AS `Reccurence`,`bbgeneraltransaction`.`createdby` AS `createdby`,`bbgeneraltransaction`.`createdts` AS `createdts`,`bbgeneraltransaction`.`Request_id` AS `Request_id`,`bbgeneraltransaction`.`UpdatedBy` AS `UpdatedBy`,`bbgeneraltransaction`.`updatedts` AS `updatedts`,`bbgeneraltransaction`.`ActedBy` AS `ActedBy`,`bbgeneraltransaction`.`DebitOrCreditAccount` AS `DebitOrCreditAccount`,`bbgeneraltransaction`.`BBGeneralTransactionType_id` AS `TransactionType_id`,`bbgeneraltransactiontype`.`TransactionType` AS `TransactionType`,`bbgeneraltransaction`.`Company_id` AS `Company_id`,`organisation`.`Name` AS `companyName`,`bbrequest`.`createdby` AS `RequestCreatedby`,`bbstatus`.`status` AS `StatusValue`,`bbrequest`.`RequestType_id` AS `BBGeneralTransactionType_id` from ((((`bbgeneraltransaction` left join `bbgeneraltransactiontype` on((`bbgeneraltransaction`.`BBGeneralTransactionType_id` = `bbgeneraltransactiontype`.`id`))) left join `organisation` on((`bbgeneraltransaction`.`Company_id` = `organisation`.`id`))) left join `bbrequest` on((`bbgeneraltransaction`.`Request_id` = `bbrequest`.`Request_id`))) left join `bbstatus` on((`bbgeneraltransaction`.`Status` = `bbstatus`.`id`)));

DROP VIEW IF EXISTS `fetch_templaterecord_details_view`;
CREATE VIEW `fetch_templaterecord_details_view` AS select `bbtemplaterecord`.`TemplateRecord_id` AS `TemplateRecord_id`,`bbtemplaterecord`.`Record_Name` AS `Record_Name`,`bbtemplaterecord`.`ToAccountNumber` AS `ToAccountNumber`,`bbtemplaterecord`.`ABATRCNumber` AS `ABATRCNumber`,`bbtemplaterecord`.`Detail_id` AS `Detail_id`,`bbtemplaterecord`.`Amount` AS `Amount`,`bbtemplaterecord`.`AdditionalInfo` AS `AdditionalInfo`,`bbtemplaterecord`.`EIN` AS `EIN`,`bbtemplaterecord`.`IsZeroTaxDue` AS `IsZeroTaxDue`,`bbtemplaterecord`.`Template_id` AS `Template_id`,`bbtemplaterecord`.`TaxType_id` AS `TaxType_id`,`bbtemplaterecord`.`TemplateRequestType_id` AS `TemplateRequestType_id`,`bbtemplaterecord`.`softDelete` AS `softDelete`,`bbtemplaterecord`.`ToAccountType` AS `ToAccountType`,`bbtaxtype`.`taxType` AS `TaxType`,`achaccountstype`.`accountType` AS `ToAccountTypeValue` from ((`bbtemplaterecord` left join `bbtaxtype` on((`bbtemplaterecord`.`TaxType_id` = `bbtaxtype`.`id`))) left join `achaccountstype` on((`bbtemplaterecord`.`ToAccountType` = `achaccountstype`.`id`)));

DROP VIEW IF EXISTS `fetch_transactionrecord_details_view`;
CREATE VIEW `fetch_transactionrecord_details_view` AS select `bbtransactionrecord`.`TransactionRecord_id` AS `TransactionRecord_id`,`bbtransactionrecord`.`Record_Name` AS `Record_Name`,`bbtransactionrecord`.`ToAccountNumber` AS `ToAccountNumber`,`bbtransactionrecord`.`ABATRCNumber` AS `ABATRCNumber`,`bbtransactionrecord`.`Detail_id` AS `Detail_id`,`bbtransactionrecord`.`Amount` AS `Amount`,`bbtransactionrecord`.`AdditionalInfo` AS `AdditionalInfo`,`bbtransactionrecord`.`EIN` AS `EIN`,`bbtransactionrecord`.`IsZeroTaxDue` AS `IsZeroTaxDue`,`bbtransactionrecord`.`Transaction_id` AS `Transaction_id`,`bbtransactionrecord`.`TaxType_id` AS `TaxType_id`,`bbtransactionrecord`.`TemplateRequestType_id` AS `TemplateRequestType_id`,`bbtransactionrecord`.`softDelete` AS `softDelete`,`bbtransactionrecord`.`ToAccountType` AS `ToAccountType`,`bbtaxtype`.`taxType` AS `TaxType`,`achaccountstype`.`accountType` AS `ToAccountTypeValue` from ((`bbtransactionrecord` left join `bbtaxtype` on((`bbtransactionrecord`.`TaxType_id` = `bbtaxtype`.`id`))) left join `achaccountstype` on((`bbtransactionrecord`.`ToAccountType` = `achaccountstype`.`id`)));

ALTER TABLE `customer` 
DROP COLUMN `Role`,
DROP COLUMN `Phone`,
DROP COLUMN `Email`,
DROP COLUMN `CustomerType`;

ALTER TABLE `customer` DROP FOREIGN KEY `FK_Customer_Type_id`;
ALTER TABLE `customer` DROP COLUMN `Type_id`, DROP INDEX `FK_Customer_Type_id`;
ALTER TABLE `customer` ADD CONSTRAINT `FK_Customer_Type_id` FOREIGN KEY (`CustomerType_id`) REFERENCES `customertype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE `organisationtype` CHANGE COLUMN `id` `id` VARCHAR(50) NOT NULL ;

ALTER TABLE `customer` DROP COLUMN `Status`;
ALTER TABLE `customer` ADD COLUMN `isEagreementSigned` TINYINT(1) NOT NULL DEFAULT '0' AFTER `lockedOn`;

DROP VIEW IF EXISTS `customerorganisationmembershipview`;
CREATE VIEW `customerorganisationmembershipview` AS select distinct `customer`.`LastName` AS `LastName`,`customer`.`DateOfBirth` AS `DateOfBirth`,`customer`.`Ssn` AS `Ssn`,`primaryphone`.`Value` AS `Phone`,`primaryemail`.`Value` AS `Email`,`customer`.`UserName` AS `UserName`,`customer`.`Gender` AS `Gender`,`customer`.`id` AS `id`,`customer`.`FirstName` AS `FirstName`,`customer`.`CustomerType_id` AS `CustomerType`,`customer`.`Organization_Id` AS `Organization_Id`,`organisationmembership`.`Membership_id` AS `Membership_id`,`organisationmembership`.`Taxid` AS `Taxid` from ((((`customer` left join `customercommunication` `primaryphone` on(((`primaryphone`.`Customer_id` = `customer`.`id`) and (`primaryphone`.`isPrimary` = 1) and (`primaryphone`.`Type_id` = 'COMM_TYPE_PHONE')))) left join `customercommunication` `primaryemail` on(((`primaryemail`.`Customer_id` = `customer`.`id`) and (`primaryemail`.`isPrimary` = 1) and (`primaryemail`.`Type_id` = 'COMM_TYPE_EMAIL')))) join `organisationemployees`) join `organisationmembership`) where (((`customer`.`Organization_Id` = `organisationemployees`.`Organization_id`) or (`customer`.`id` = `organisationemployees`.`Customer_id`)) and (`organisationemployees`.`Organization_id` = `organisationmembership`.`Organization_id`));

DROP VIEW IF EXISTS `organisationaccountsview`;
CREATE VIEW `organisationaccountsview` AS select distinct `accounttype`.`TypeDescription` AS `Account_Type`,`customeraccounts`.`Customer_id` AS `Customer_id`,`customeraccounts`.`Account_id` AS `Account_id`,`customeraccounts`.`AccountName` AS `accountName`,`customer`.`LastName` AS `LastName`,`customer`.`DateOfBirth` AS `DateOfBirth`,`customer`.`Ssn` AS `Ssn`,`primaryphone`.`Value` AS `phone`,`primaryemail`.`Value` AS `Email`,`customer`.`UserName` AS `username`,`customer`.`Gender` AS `Gender`,`customer`.`FirstName` AS `FirstName`,`customer`.`CustomerType_id` AS `CustomerType`,`customer`.`Organization_Id` AS `Organization_Id`,`organisationmembership`.`Membership_id` AS `Membership_id`,`organisationmembership`.`Taxid` AS `Taxid` from ((((`accounttype` join `accounts`) join ((`customeraccounts` join `customer`) left join `customercommunication` `primaryphone` on(((`primaryphone`.`Customer_id` = `customer`.`id`) and (`primaryphone`.`isPrimary` = 1) and (`primaryphone`.`Type_id` = 'COMM_TYPE_PHONE'))))) left join `customercommunication` `primaryemail` on(((`primaryemail`.`Customer_id` = `customer`.`id`) and (`primaryemail`.`isPrimary` = 1) and (`primaryemail`.`Type_id` = 'COMM_TYPE_EMAIL')))) join (`organisationemployees` join `organisationmembership`)) where ((`accounttype`.`TypeID` = `accounts`.`Type_id`) and (`accounts`.`Account_id` = `customeraccounts`.`Account_id`) and (`customer`.`id` = `customeraccounts`.`Customer_id`) and ((`customer`.`id` = `organisationemployees`.`Customer_id`) or (`customer`.`Organization_Id` = `organisationemployees`.`Organization_id`)) and (`organisationemployees`.`Organization_id` = `organisationmembership`.`Organization_id`));

UPDATE `customer` set `CustomerType_id`='TYPE_ID_RETAIL' where `CustomerType_id` is null or `CustomerType_id` = '';
UPDATE `customer` set `Status_id`='SID_CUS_ACTIVE' where `Status_id` is null or `Status_id`='';

ALTER TABLE `customer` 
DROP FOREIGN KEY `FK_Customer_Status_03`,
DROP FOREIGN KEY `FK_Customer_Type_id`;
ALTER TABLE `customer` 
CHANGE COLUMN `CustomerType_id` `CustomerType_id` VARCHAR(50) NOT NULL DEFAULT 'TYPE_ID_RETAIL' ,
CHANGE COLUMN `Status_id` `Status_id` VARCHAR(50) NOT NULL DEFAULT 'SID_CUS_ACTIVE' ;
ALTER TABLE `customer` 
ADD CONSTRAINT `FK_Customer_Status_03`
  FOREIGN KEY (`Status_id`)
  REFERENCES `status` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION,
ADD CONSTRAINT `FK_Customer_Type_id`
  FOREIGN KEY (`CustomerType_id`)
  REFERENCES `customertype` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
 
  DROP TABLE `usersecurity`;
  DROP TABLE `securityquestions`;

DROP VIEW IF EXISTS `fetch_transactionrecord_details_view`;
CREATE VIEW `fetch_transactionrecord_details_view` AS select `bbtransactionrecord`.`TransactionRecord_id` AS `TransactionRecord_id`,`bbtransactionrecord`.`Record_Name` AS `Record_Name`,`bbtransactionrecord`.`ToAccountNumber` AS `ToAccountNumber`,`bbtransactionrecord`.`ABATRCNumber` AS `ABATRCNumber`,`bbtransactionrecord`.`Detail_id` AS `Detail_id`,`bbtransactionrecord`.`Amount` AS `Amount`,`bbtransactionrecord`.`AdditionalInfo` AS `AdditionalInfo`,`bbtransactionrecord`.`EIN` AS `EIN`,`bbtransactionrecord`.`IsZeroTaxDue` AS `IsZeroTaxDue`,`bbtransactionrecord`.`Transaction_id` AS `Transaction_id`,`bbtransactionrecord`.`TaxType_id` AS `TaxType_id`,`bbtransactionrecord`.`TemplateRequestType_id` AS `TemplateRequestType_id`,`bbtransactionrecord`.`softDelete` AS `softDelete`,`bbtransactionrecord`.`ToAccountType` AS `ToAccountType`,`bbtaxtype`.`taxType` AS `TaxType`,`achaccountstype`.`accountType` AS `ToAccountTypeValue`,`bbtemplaterequesttype`.`TemplateRequestTypeName` AS `TemplateRequestTypeValue` from (((`bbtransactionrecord` left join `bbtaxtype` on((`bbtransactionrecord`.`TaxType_id` = `bbtaxtype`.`id`))) left join `achaccountstype` on((`bbtransactionrecord`.`ToAccountType` = `achaccountstype`.`id`))) left join `bbtemplaterequesttype` on((`bbtransactionrecord`.`TemplateRequestType_id` = `bbtemplaterequesttype`.`TemplateRequestType_id`)));

DROP VIEW IF EXISTS `fetch_templaterecord_details_view`;
CREATE VIEW `fetch_templaterecord_details_view` AS select `bbtemplaterecord`.`TemplateRecord_id` AS `TemplateRecord_id`,`bbtemplaterecord`.`Record_Name` AS `Record_Name`,`bbtemplaterecord`.`ToAccountNumber` AS `ToAccountNumber`,`bbtemplaterecord`.`ABATRCNumber` AS `ABATRCNumber`,`bbtemplaterecord`.`Detail_id` AS `Detail_id`,`bbtemplaterecord`.`Amount` AS `Amount`,`bbtemplaterecord`.`AdditionalInfo` AS `AdditionalInfo`,`bbtemplaterecord`.`EIN` AS `EIN`,`bbtemplaterecord`.`IsZeroTaxDue` AS `IsZeroTaxDue`,`bbtemplaterecord`.`Template_id` AS `Template_id`,`bbtemplaterecord`.`TaxType_id` AS `TaxType_id`,`bbtemplaterecord`.`TemplateRequestType_id` AS `TemplateRequestType_id`,`bbtemplaterecord`.`softDelete` AS `softDelete`,`bbtemplaterecord`.`ToAccountType` AS `ToAccountType`,`bbtaxtype`.`taxType` AS `TaxType`,`achaccountstype`.`accountType` AS `ToAccountTypeValue`,`bbtemplaterequesttype`.`TemplateRequestTypeName` AS `TemplateRequestTypeValue` from (((`bbtemplaterecord` left join `bbtaxtype` on((`bbtemplaterecord`.`TaxType_id` = `bbtaxtype`.`id`))) left join `achaccountstype` on((`bbtemplaterecord`.`ToAccountType` = `achaccountstype`.`id`))) left join `bbtemplaterequesttype` on((`bbtemplaterecord`.`TemplateRequestType_id` = `bbtemplaterequesttype`.`TemplateRequestType_id`)));

DROP VIEW IF EXISTS `fetch_generaltransactions_Details_view`;
CREATE VIEW `fetch_generaltransactions_Details_view` AS select `bbgeneraltransaction`.`Transaction_id` AS `Transaction_id`,`bbgeneraltransaction`.`TransactionDate` AS `TransactionDate`,`bbgeneraltransaction`.`Payee` AS `Payee`,`bbgeneraltransaction`.`Amount` AS `Amount`,`bbgeneraltransaction`.`Frequency` AS `Frequency`,`bbgeneraltransaction`.`Status` AS `Status`,`bbgeneraltransaction`.`Reccurence` AS `Reccurence`,`bbgeneraltransaction`.`createdby` AS `createdby`,`bbgeneraltransaction`.`createdts` AS `createdts`,`bbgeneraltransaction`.`Request_id` AS `Request_id`,`bbgeneraltransaction`.`UpdatedBy` AS `UpdatedBy`,`bbgeneraltransaction`.`updatedts` AS `updatedts`,`bbgeneraltransaction`.`ActedBy` AS `ActedBy`,`bbgeneraltransaction`.`DebitOrCreditAccount` AS `DebitOrCreditAccount`,`bbgeneraltransaction`.`BBGeneralTransactionType_id` AS `TransactionType_id`,`bbgeneraltransactiontype`.`TransactionType` AS `TransactionType`,`bbgeneraltransaction`.`Company_id` AS `Company_id`,`organisation`.`Name` AS `companyName`,`bbrequest`.`createdby` AS `RequestCreatedby`,`bbstatus`.`status` AS `StatusValue`,`bbrequest`.`RequestType_id` AS `BBGeneralTransactionType_id`,`customer`.`UserName` AS `userName` from (((((`bbgeneraltransaction` left join `bbgeneraltransactiontype` on((`bbgeneraltransaction`.`BBGeneralTransactionType_id` = `bbgeneraltransactiontype`.`id`))) left join `organisation` on((`bbgeneraltransaction`.`Company_id` = `organisation`.`id`))) left join `bbrequest` on((`bbgeneraltransaction`.`Request_id` = `bbrequest`.`Request_id`))) left join `bbstatus` on((`bbgeneraltransaction`.`Status` = `bbstatus`.`id`))) left join `customer` on((`bbgeneraltransaction`.`createdby` = `customer`.`id`)));

ALTER TABLE `bbtransactionsubrecord` 
CHANGE COLUMN `TranscationSubRecord_id` `TranscationSubRecord_id` INT(11) NOT NULL AUTO_INCREMENT ;

DROP VIEW IF EXISTS `orgemployeedetails`;
CREATE VIEW `orgemployeedetails` AS select `c`.`id` AS `id`,`c`.`FirstName` AS `FirstName`,`c`.`MiddleName` AS `MiddleName`,`c`.`LastName` AS `LastName`,`c`.`UserName` AS `Username`,`c`.`Gender` AS `Gender`,`c`.`DateOfBirth` AS `DateOfBirth`,`c`.`DrivingLicenseNumber` AS `DrivingLicenseNumber`,`c`.`Ssn` AS `Ssn`,`c`.`UserCompany` AS `UserCompany`,`c`.`Lastlogintime` AS `Lastlogintime`,`c`.`Status_id` AS `Status`,`ca`.`Account_id` AS `Account_id`,`ca`.`AccountName` AS `AccountName` from (`customer` `c` left join `customeraccounts` `ca` on((`c`.`id` = `ca`.`Customer_id`)));

ALTER TABLE `bbgeneraltransactiontype` ADD COLUMN `ServiceID` VARCHAR(45) NULL AFTER `id`;

ALTER TABLE `bbgeneraltransaction` CHANGE COLUMN `Frequency` `Frequency` VARCHAR(50) NULL DEFAULT NULL ;

CREATE TABLE `credentialchecker` (
  `id` varchar(50) NOT NULL,
  `UserName` varchar(45) NOT NULL,
  `linktype` varchar(45) NOT NULL,
  `createdts` datetime NOT NULL,
  PRIMARY KEY (`id`))  ENGINE=InnoDB DEFAULT CHARSET=utf8;



ALTER TABLE `accounts` 
CHANGE COLUMN `Bank_id` `Bank_id` VARCHAR(50) NOT NULL DEFAULT '1' ,
CHANGE COLUMN `User_id` `User_id` VARCHAR(50) NULL DEFAULT NULL ;

ALTER TABLE `accounts` 
CHANGE COLUMN `AccountPreference` `AccountPreference` INT(11) NOT NULL DEFAULT 0 ;
ALTER TABLE `accounts` 
ADD COLUMN `TaxId` VARCHAR(45) NULL AFTER `adminProductId`;

ALTER TABLE `achfile` 
ADD COLUMN `updatedts` TIMESTAMP NULL AFTER `ActedBy`;

ALTER TABLE `address` 
CHANGE COLUMN `User_id` `User_id` VARCHAR(50) NULL DEFAULT NULL ;

CREATE TABLE `alertAttribute` (
  `id` varchar(255) NOT NULL,
  `AlertSubTypeId` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` varchar(255) DEFAULT NULL,
  `lastmodifiedts` varchar(255) DEFAULT NULL,
  `synctimestamp` varchar(255) DEFAULT NULL,
  `softdeleteflag` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `alertCondition` (
  `id` varchar(255) NOT NULL,
  `AlertSubTypeId` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` varchar(255) DEFAULT NULL,
  `lastmodifiedts` varchar(255) DEFAULT NULL,
  `synctimestamp` varchar(255) DEFAULT NULL,
  `softdeleteflag` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `alertHistory` (
  `id` varchar(255) NOT NULL,
  `AlertSubTypeId` varchar(255) DEFAULT NULL,
  `Customer_Id` varchar(255) DEFAULT NULL,
  `Status` varchar(255) DEFAULT NULL,
  `Channel` varchar(255) DEFAULT NULL,
  `Message` varchar(255) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` varchar(255) DEFAULT NULL,
  `lastmodifiedts` varchar(255) DEFAULT NULL,
  `synctimestamp` varchar(255) DEFAULT NULL,
  `softdeleteflag` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `AlertSubType` (
  `id` varchar(255) NOT NULL,
  `AlertTypeId` varchar(255) DEFAULT NULL,
  `Name` varchar(255) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `Status_id` varchar(255) DEFAULT NULL,
  `IsSmsActive` varchar(255) DEFAULT NULL,
  `IsEmailActive` varchar(255) DEFAULT NULL,
  `IsPushActive` varchar(255) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` varchar(255) DEFAULT NULL,
  `lastmodifiedts` varchar(255) DEFAULT NULL,
  `synctimestamp` varchar(255) DEFAULT NULL,
  `softdeleteflag` varchar(255) DEFAULT NULL
)  ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `application` 
CHANGE COLUMN `isBusinessBankingEnabled` `isBusinessBankingEnabled` TINYINT(1) NOT NULL DEFAULT 0 ;

ALTER TABLE `bbgeneraltransaction` 
ADD COLUMN `TransactionEntry` MEDIUMTEXT NULL AFTER `ActedBy`;

ALTER TABLE `bbtransaction` 
ADD COLUMN `updatedts` TIMESTAMP NULL AFTER `Template_id`;

ALTER TABLE `card` 
CHANGE COLUMN `User_id` `User_id` VARCHAR(50) NOT NULL ;

ALTER TABLE `customer` 
ADD COLUMN `Bank_id` VARCHAR(50) NULL AFTER `softdeleteflag`,
ADD COLUMN `Session_id` VARCHAR(50) NULL AFTER `Bank_id`,
ADD COLUMN `MaritalStatus` VARCHAR(50) NULL AFTER `Session_id`,
ADD COLUMN `SpouseFirstName` VARCHAR(50) NULL AFTER `MaritalStatus`,
ADD COLUMN `SpouseLastName` VARCHAR(50) NULL AFTER `SpouseFirstName`,
ADD COLUMN `EmploymentInfo` VARCHAR(50) NULL AFTER `SpouseLastName`,
CHANGE COLUMN `Password` `Password` VARCHAR(100) NULL DEFAULT NULL ,
CHANGE COLUMN `UserImageURL` `UserImageURL` VARCHAR(200) NULL DEFAULT NULL ,
CHANGE COLUMN `isBillPaySupported` `isBillPaySupported` BIT(1) NULL DEFAULT b'0' ,
CHANGE COLUMN `isBillPayActivated` `isBillPayActivated` BIT(1) NULL DEFAULT b'0' ,
CHANGE COLUMN `isP2PSupported` `isP2PSupported` BIT(1) NULL DEFAULT b'0' ,
CHANGE COLUMN `isP2PActivated` `isP2PActivated` BIT(1) NULL DEFAULT b'0' ,
CHANGE COLUMN `isWireTransferEligible` `isWireTransferEligible` BIT(1) NULL DEFAULT b'0' ,
CHANGE COLUMN `isWireTransferActivated` `isWireTransferActivated` BIT(1) NULL DEFAULT b'0' ;

ALTER TABLE `customeraccounts` 
ADD COLUMN `Organization_id` VARCHAR(45) NULL AFTER `Account_id`,
ADD COLUMN `IsOrgAccountUnLinked` TINYINT(1) NOT NULL DEFAULT 0 AFTER `IsOrganizationAccount`;

ALTER TABLE `customeraddress` 
DROP FOREIGN KEY `FK_CustomerAddress_AddressType`;
ALTER TABLE `customeraddress` 
CHANGE COLUMN `Type_id` `Type_id` VARCHAR(50) NOT NULL DEFAULT 'ADR_TYPE_HOME' ;
ALTER TABLE `customeraddress` 
ADD CONSTRAINT `FK_CustomerAddress_AddressType`
  FOREIGN KEY (`Type_id`)
  REFERENCES `addresstype` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

  ALTER TABLE `customerpreference` 
ADD COLUMN `DefaultAccountDeposit` VARCHAR(45) NULL AFTER `Customer_id`,
CHANGE COLUMN `ShowBillPayFromAccPopup` `ShowBillPayFromAccPopup` BIT(1) NOT NULL DEFAULT b'0' ;

CREATE TABLE `customertypeconfig` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `CustomerType_id` varchar(45) DEFAULT NULL,
  `Appid` varchar(45) DEFAULT NULL,
  `AccessPermitted` bit(1) DEFAULT b'0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `dbxAlertCategory` (
  `id` varchar(255) NOT NULL,
  `Name` varchar(255) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` varchar(255) DEFAULT NULL,
  `lastmodifiedts` varchar(255) DEFAULT NULL,
  `synctimestamp` varchar(255) DEFAULT NULL,
  `softdeleteflag` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `dbxAlertType` (
  `id` varchar(255) NOT NULL,
  `AlertCategoryId` varchar(255) DEFAULT NULL,
  `Name` varchar(255) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `Status_id` varchar(255) DEFAULT NULL,
  `IsSmsActive` varchar(255) DEFAULT NULL,
  `IsEmailActive` varchar(255) DEFAULT NULL,
  `IsPushActive` varchar(255) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` varchar(255) DEFAULT NULL,
  `lastmodifiedts` varchar(255) DEFAULT NULL,
  `synctimestamp` varchar(255) DEFAULT NULL,
  `softdeleteflag` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `dbxCommunicationType` (
  `id` varchar(255) NOT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` varchar(255) DEFAULT NULL,
  `lastmodifiedts` varchar(255) DEFAULT NULL,
  `synctimestamp` varchar(255) DEFAULT NULL,
  `softdeleteflag` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `dbxCustomerAlertEntitlement` (
  `id` varchar(255) NOT NULL,
  `Customer_id` varchar(255) DEFAULT NULL,
  `AccountId` varchar(255) DEFAULT NULL,
  `AlertSubTypeId` varchar(255) DEFAULT NULL,
  `AlertAttributeId` varchar(255) DEFAULT NULL,
  `AlertConditionId` varchar(255) DEFAULT NULL,
  `Value1` varchar(255) DEFAULT NULL,
  `Value2` varchar(255) DEFAULT NULL,
  `Frequency` varchar(255) DEFAULT NULL,
  `DaysBeforeReminder` varchar(255) DEFAULT NULL,
  `IsSmsActive` varchar(255) DEFAULT NULL,
  `IsEmailActive` varchar(255) DEFAULT NULL,
  `IsPushActive` varchar(255) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` varchar(255) DEFAULT NULL,
  `lastmodifiedts` varchar(255) DEFAULT NULL,
  `synctimestamp` varchar(255) DEFAULT NULL,
  `softdeleteflag` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `dbxCustomerCommunication` (
  `id` varchar(255) NOT NULL,
  `Type_id` varchar(255) DEFAULT NULL,
  `Customer_id` varchar(255) DEFAULT NULL,
  `isPrimary` varchar(255) DEFAULT NULL,
  `Value` varchar(255) DEFAULT NULL,
  `Extension` varchar(255) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `IsPreferredContactMethod` varchar(255) DEFAULT NULL,
  `PreferredContactTime` varchar(255) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` varchar(255) DEFAULT NULL,
  `lastmodifiedts` varchar(255) DEFAULT NULL,
  `synctimestamp` varchar(255) DEFAULT NULL,
  `softdeleteflag` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `externalaccount` 
DROP FOREIGN KEY `User_Id_External`;
ALTER TABLE `externalaccount` 
DROP INDEX `user_AccountNo_IBAN_Unique` ;

CREATE TABLE `globalAlert` (
  `id` varchar(255) NOT NULL,
  `AlertSubTypeId` varchar(255) DEFAULT NULL,
  `AlertAttributeId` varchar(255) DEFAULT NULL,
  `AlertConditionId` varchar(255) DEFAULT NULL,
  `Value1` varchar(255) DEFAULT NULL,
  `Value2` varchar(255) DEFAULT NULL,
  `Frequency` varchar(255) DEFAULT NULL,
  `DaysBeforeReminder` varchar(255) DEFAULT NULL,
  `isUserCustomizable` varchar(255) DEFAULT NULL,
  `IsSmsActive` varchar(255) DEFAULT NULL,
  `IsEmailActive` varchar(255) DEFAULT NULL,
  `IsPushActive` varchar(255) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` varchar(255) DEFAULT NULL,
  `lastmodifiedts` varchar(255) DEFAULT NULL,
  `synctimestamp` varchar(255) DEFAULT NULL,
  `softdeleteflag` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `LinkConfig` (
  `EncodedLink` varchar(100) NOT NULL,
  `LinkType` varchar(50) DEFAULT NULL,
  `UserName` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`EncodedLink`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `membership` 
CHANGE COLUMN `id` `id` VARCHAR(50) NOT NULL ;

CREATE TABLE `membershipaccounts` (
  `id` varchar(50) NOT NULL,
  `Membership_id` varchar(50) NOT NULL,
  `Account_id` varchar(50) DEFAULT NULL,
  `accountName` varchar(50) NOT NULL,
  `IsOrganizationAccount` tinyint(1) DEFAULT '0',
  `Taxid` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby:` varchar(50) DEFAULT NULL,
  `createdts` varchar(50) DEFAULT NULL,
  `lastmodifiedts` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
)  ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `membershipowner` 
CHANGE COLUMN `Membership_id` `Membership_id` VARCHAR(50) NOT NULL DEFAULT ' ' ,
CHANGE COLUMN `Membership_Type` `Membership_Type` VARCHAR(50) NOT NULL DEFAULT ' ' ,
CHANGE COLUMN `IDValue` `IDValue` VARCHAR(50) NOT NULL DEFAULT ' ' ;

ALTER TABLE `organisationemployees` 
CHANGE COLUMN `id` `id` VARCHAR(50) NOT NULL ;

ALTER TABLE `organisationowner` 
ADD COLUMN `Ssn` VARCHAR(45) NULL AFTER `Phone`;

ALTER TABLE `OTP` 
ADD COLUMN `OtpType` VARCHAR(45) NULL AFTER `Otp`,
ADD COLUMN `InvalidAttempt` VARCHAR(45) NULL AFTER `OtpType`,
ADD COLUMN `createdts` VARCHAR(45) NULL AFTER `InvalidAttempt`;

ALTER TABLE `OTP` 
CHANGE COLUMN `InvalidAttempt` `InvalidAttempt` INT(11) NULL DEFAULT NULL ,
CHANGE COLUMN `createdts` `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `payee` 
CHANGE COLUMN `User_Id` `User_Id` VARCHAR(50) NULL DEFAULT NULL ;

ALTER TABLE `payperson` 
CHANGE COLUMN `User_id` `User_id` VARCHAR(50) NOT NULL ;

ALTER TABLE `pfmbargraph` 
CHANGE COLUMN `userId` `userId` VARCHAR(50) NULL DEFAULT NULL ;

ALTER TABLE `pfmpiechart` 
CHANGE COLUMN `userId` `userId` VARCHAR(50) NULL DEFAULT NULL ;

ALTER TABLE `pfmtransactions` 
CHANGE COLUMN `userId` `userId` VARCHAR(50) NULL DEFAULT NULL ;

ALTER TABLE `phone` 
CHANGE COLUMN `user_id` `user_id` VARCHAR(50) NOT NULL ;

ALTER TABLE `useralerts` 
DROP FOREIGN KEY `FK_USERALERTS_User`;
ALTER TABLE `useralerts` 
CHANGE COLUMN `User_id` `User_id` VARCHAR(50) NULL DEFAULT NULL ,
DROP INDEX `User` ;

ALTER TABLE `usernotification` 
DROP FOREIGN KEY `FK_USERNOTIFICATION_USER`;
ALTER TABLE `usernotification` 
CHANGE COLUMN `user_id` `user_id` VARCHAR(50) NULL DEFAULT NULL ,
DROP INDEX `FK_USERNOTIFICATION_USER` ;

CREATE TABLE `usersecurity` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `User_id` varchar(50) NOT NULL DEFAULT '0',
  `question` int(11) NOT NULL,
  `answer` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `User_id` (`User_id`),
  KEY `Question` (`question`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP VIEW IF EXISTS `allaccountsview`;
CREATE VIEW `allaccountsview` AS select distinct `accounts`.`AccountHolder` AS `AccountHolder`,`accounts`.`Account_id` AS `Account_id`,`accounts`.`AccountName` AS `AccountName`,`accounts`.`Type_id` AS `Type_id`,`membershipaccounts`.`Membership_id` AS `Membership_id`,`membershipaccounts`.`Taxid` AS `Taxid`,`membershipaccounts`.`IsOrganizationAccount` AS `IsOrganizationAccount` from (`accounts` join `membershipaccounts`) where (`accounts`.`Account_id` = `membershipaccounts`.`Account_id`);

DROP VIEW IF EXISTS `customeraccountsview`;
CREATE VIEW `customeraccountsview` AS select distinct `accounts`.`TaxId` AS `Taxid`,`customeraccounts`.`Membership_id` AS `Membership_id`,`customeraccounts`.`Customer_id` AS `Customer_id`,`accounts`.`Account_id` AS `Account_id`,`accounts`.`Type_id` AS `Type_id`,`accounts`.`UserName` AS `userName`,`accounts`.`CurrencyCode` AS `currencyCode`,`accounts`.`AccountHolder` AS `accountHolder`,`accounts`.`error` AS `error`,`accounts`.`Address` AS `Address`,`accounts`.`Scheme` AS `Scheme`,`accounts`.`Number` AS `number`,`accounts`.`AvailableBalance` AS `availableBalance`,`accounts`.`CurrentBalance` AS `currentBalance`,`accounts`.`InterestRate` AS `interestRate`,`accounts`.`AvailableCredit` AS `availableCredit`,`accounts`.`MinimumDue` AS `minimumDue`,`accounts`.`DueDate` AS `dueDate`,`accounts`.`FirstPaymentDate` AS `firstPaymentDate`,`accounts`.`ClosingDate` AS `closingDate`,`accounts`.`PaymentTerm` AS `paymentTerm`,`accounts`.`OpeningDate` AS `openingDate`,`accounts`.`MaturityDate` AS `maturityDate`,`accounts`.`DividendLastPaidAmount` AS `dividendLastPaidAmount`,`accounts`.`DividendLastPaidDate` AS `dividendLastPaidDate`,`accounts`.`DividendPaidYTD` AS `dividendPaidYTD`,`accounts`.`DividendRate` AS `dividendRate`,`accounts`.`DividendYTD` AS `dividendYTD`,`accounts`.`EStatementmentEnable` AS `eStatementEnable`,`accounts`.`FavouriteStatus` AS `favouriteStatus`,`accounts`.`StatusDesc` AS `statusDesc`,`accounts`.`NickName` AS `nickName`,`accounts`.`OriginalAmount` AS `originalAmount`,`accounts`.`OutstandingBalance` AS `outstandingBalance`,`accounts`.`PaymentDue` AS `paymentDue`,`accounts`.`PaymentMethod` AS `paymentMethod`,`accounts`.`SwiftCode` AS `swiftCode`,`accounts`.`TotalCreditMonths` AS `totalCreditMonths`,`accounts`.`TotalDebitsMonth` AS `totalDebitsMonth`,`accounts`.`RoutingNumber` AS `routingNumber`,`accounts`.`SupportBillPay` AS `supportBillPay`,`accounts`.`SupportCardlessCash` AS `supportCardlessCash`,`accounts`.`SupportTransferFrom` AS `supportTransferFrom`,`accounts`.`SupportTransferTo` AS `supportTransferTo`,`accounts`.`SupportDeposit` AS `supportDeposit`,`accounts`.`UnpaidInterest` AS `unpaidInterest`,`accounts`.`PreviousYearsDividends` AS `previousYearsDividends`,`accounts`.`principalBalance` AS `principalBalance`,`accounts`.`PrincipalValue` AS `principalValue`,`accounts`.`RegularPaymentAmount` AS `regularPaymentAmount`,`accounts`.`phone` AS `phoneId`,`accounts`.`LastDividendPaidDate` AS `lastDividendPaidDate`,`accounts`.`LastDividendPaidAmount` AS `lastDividendPaidAmount`,`accounts`.`LastPaymentAmount` AS `lastPaymentAmount`,`accounts`.`LastPaymentDate` AS `lastPaymentDate`,`accounts`.`LastStatementBalance` AS `lastStatementBalance`,`accounts`.`LateFeesDue` AS `lateFeesDue`,`accounts`.`maturityAmount` AS `maturityAmount`,`accounts`.`MaturityOption` AS `maturityOption`,`accounts`.`payoffAmount` AS `payoffAmount`,`accounts`.`PayOffCharge` AS `payOffCharge`,`accounts`.`PendingDeposit` AS `pendingDeposit`,`accounts`.`PendingWithdrawal` AS `pendingWithdrawal`,`accounts`.`JointHolders` AS `jointHolders`,`accounts`.`IsPFM` AS `isPFM`,`accounts`.`InterestPaidYTD` AS `interestPaidYTD`,`accounts`.`InterestPaidPreviousYTD` AS `interestPaidPreviousYTD`,`accounts`.`InterestPaidLastYear` AS `interestPaidLastYear`,`accounts`.`InterestEarned` AS `interestEarned`,`accounts`.`CurrentAmountDue` AS `currentAmountDue`,`accounts`.`CreditLimit` AS `creditLimit`,`accounts`.`CreditCardNumber` AS `creditCardNumber`,`accounts`.`BsbNum` AS `bsbNum`,`accounts`.`BondInterestLastYear` AS `bondInterestLastYear`,`accounts`.`BondInterest` AS `bondInterest`,`accounts`.`AvailablePoints` AS `availablePoints`,`accounts`.`AccountName` AS `accountName`,`accounts`.`email` AS `email`,`accounts`.`IBAN` AS `IBAN`,`accounts`.`adminProductId` AS `adminProductId`,`bank`.`Description` AS `bankname`,`accounts`.`AccountPreference` AS `accountPreference`,`accounttype`.`transactionLimit` AS `transactionLimit`,`accounttype`.`transferLimit` AS `transferLimit`,`accounttype`.`rates` AS `rates`,`accounttype`.`termsAndConditions` AS `termsAndConditions`,`accounttype`.`TypeDescription` AS `typeDescription`,`accounttype`.`supportChecks` AS `supportChecks`,`accounttype`.`displayName` AS `displayName`,`accounts`.`accountSubType` AS `accountSubType`,`accounts`.`description` AS `description`,`accounts`.`schemeName` AS `schemeName`,`accounts`.`identification` AS `identification`,`accounts`.`secondaryIdentification` AS `secondaryIdentification`,`accounts`.`servicerSchemeName` AS `servicerSchemeName`,`accounts`.`servicerIdentification` AS `servicerIdentification`,`accounts`.`dataCreditDebitIndicator` AS `dataCreditDebitIndicator`,`accounts`.`dataType` AS `dataType`,`accounts`.`dataDateTime` AS `dataDateTime`,`accounts`.`dataCreditLineIncluded` AS `dataCreditLineIncluded`,`accounts`.`dataCreditLineType` AS `dataCreditLineType`,`accounts`.`dataCreditLineAmount` AS `dataCreditLineAmount`,`accounts`.`dataCreditLineCurrency` AS `dataCreditLineCurrency` from (((`accounts` join `customeraccounts`) join `accounttype`) left join `bank` on((`accounts`.`Bank_id` = `bank`.`id`))) where ((`accounts`.`Account_id` = `customeraccounts`.`Account_id`) and (`accounts`.`Type_id` = `accounttype`.`TypeID`));

DROP VIEW IF EXISTS `customercommunicationview`;
CREATE VIEW `customercommunicationview` AS select `customer`.`id` AS `id`,`customer`.`FirstName` AS `FirstName`,`customer`.`MiddleName` AS `MiddleName`,`customer`.`LastName` AS `LastName`,`customer`.`UserName` AS `UserName`,`primaryphone`.`Value` AS `Phone`,`primaryemail`.`Value` AS `Email` from ((`customer` left join `customercommunication` `primaryphone` on(((`primaryphone`.`Customer_id` = `customer`.`id`) and (`primaryphone`.`isPrimary` = 1) and (`primaryphone`.`Type_id` = 'COMM_TYPE_PHONE')))) left join `customercommunication` `primaryemail` on(((`primaryemail`.`Customer_id` = `customer`.`id`) and (`primaryemail`.`isPrimary` = 1) and (`primaryemail`.`Type_id` = 'COMM_TYPE_EMAIL'))));

DROP VIEW IF EXISTS `customerpreferencesview`;
CREATE VIEW `customerpreferencesview` AS select distinct `address`.`addressLine1` AS `addressLine1`,`address`.`addressLine2` AS `addressLine2`,`address`.`state` AS `state`,`address`.`cityName` AS `city`,`address`.`country` AS `country`,`address`.`zipCode` AS `zipcode`,`customer`.`areUserAlertsTurnedOn` AS `areUserAlertsTurnedOn`,`customer`.`areAccountStatementTermsAccepted` AS `areAccountStatementTermsAccepted`,`customer`.`areDepositTermsAccepted` AS `areDepositTermsAccepted`,`customerpreference`.`DefaultAccountDeposit` AS `default_account_deposit`,`customerpreference`.`DefaultAccountBillPay` AS `default_account_billPay`,`customerpreference`.`DefaultAccountPayments` AS `default_account_payments`,`customerpreference`.`DefaultAccountCardless` AS `default_account_cardless`,`customerpreference`.`DefaultAccountTransfers` AS `default_account_transfers`,`customerpreference`.`DefaultModule_id` AS `DefaultModule_id`,`customerpreference`.`DefaultAccountWire` AS `default_account_wire`,`customerpreference`.`DefaultFromAccountP2P` AS `default_from_account_p2p`,`customerpreference`.`DefaultToAccountP2P` AS `default_to_account_p2p`,`customer`.`isP2PActivated` AS `isP2PActivated`,`customer`.`isP2PSupported` AS `isP2PSupported`,`customer`.`isBillPaySupported` AS `isBillPaySupported`,`customer`.`isBillPayActivated` AS `isBillPayActivated`,`customer`.`isWireTransferActivated` AS `isWireTransferActivated`,`customer`.`isWireTransferEligible` AS `isWireTransferEligible`,`customerpreference`.`ShowBillPayFromAccPopup` AS `showBillPayFromAccPopup`,`customer`.`FirstName` AS `userFirstName`,`customer`.`LastName` AS `userLastName`,`customer`.`Gender` AS `gender`,`customer`.`IsPinSet` AS `isPinSet`,`customer`.`DateOfBirth` AS `DateOfBirth`,`customer`.`NoOfDependents` AS `noofdependents`,`customer`.`SpouseName` AS `spousefirstname`,`customer`.`Ssn` AS `ssn`,`customer`.`CountryCode` AS `CountryCode`,`customer`.`UserImage` AS `userImage`,`customer`.`UserImageURL` AS `userImageURL`,`customer`.`isEagreementSigned` AS `isEagreementSigned`,`customer`.`id` AS `id`,`customer`.`UserName` AS `UserName`,`customer`.`MaritalStatus_id` AS `maritalstatus`,`customer`.`Lastlogintime` AS `lastlogintime`,`customer`.`Bank_id` AS `Bank_id`,`primaryphone`.`Value` AS `Phone`,`primaryemail`.`Value` AS `Email` from ((((`customer` left join `customerpreference` on((`customerpreference`.`Customer_id` = `customer`.`id`))) left join (`customeraddress` join `address` on((`address`.`id` = `customeraddress`.`Address_id`))) on(((`customer`.`id` = `customeraddress`.`Customer_id`) and (`customeraddress`.`isPrimary` = 1)))) left join `customercommunication` `primaryphone` on(((`primaryphone`.`Customer_id` = `customer`.`id`) and (`primaryphone`.`isPrimary` = 1) and (`primaryphone`.`Type_id` = 'COMM_TYPE_PHONE')))) left join `customercommunication` `primaryemail` on(((`primaryemail`.`Customer_id` = `customer`.`id`) and (`primaryemail`.`isPrimary` = 1) and (`primaryemail`.`Type_id` = 'COMM_TYPE_EMAIL'))));

DROP VIEW IF EXISTS `organisationaccountsview`;
CREATE VIEW `organisationaccountsview` AS select distinct `accounttype`.`TypeDescription` AS `Account_Type`,`customeraccounts`.`Customer_id` AS `Customer_id`,`customeraccounts`.`Account_id` AS `Account_id`,`customeraccounts`.`Account_id` AS `accountID`,`customeraccounts`.`Organization_id` AS `Organization_Id`,`customeraccounts`.`createdts` AS `createdts`,`customeraccounts`.`lastmodifiedts` AS `lastmodifiedts`,`organisationmembership`.`Membership_id` AS `Membership_id`,`organisationmembership`.`Taxid` AS `Taxid`,`accounts`.`StatusDesc` AS `StatusDesc`,`accounts`.`AccountName` AS `AccountName`,`accounts`.`AvailableBalance` AS `availableBalance`,`accounts`.`CurrentBalance` AS `currentBalance`,`accounts`.`DividendRate` AS `dividendRate`,`accounts`.`EStatementmentEnable` AS `eStatementEnable`,`accounts`.`SwiftCode` AS `swiftCode`,`accounts`.`RoutingNumber` AS `routingNumber`,`accounts`.`AccountHolder` AS `accountHolder`,`accounts`.`LastDividendPaidDate` AS `lastDividendPaidDate`,`accounts`.`LastDividendPaidAmount` AS `lastDividendPaidAmount`,`accounts`.`DividendLastPaidAmount` AS `dividendLastPaidAmount`,`accounts`.`DividendLastPaidDate` AS `dividendLastPaidDate`,'joint' AS `Ownership` from (((`accounts` left join `accounttype` on((`accounttype`.`TypeID` = `accounts`.`Type_id`))) join `customeraccounts`) join `organisationmembership`) where ((`accounts`.`Account_id` = `customeraccounts`.`Account_id`) and (`customeraccounts`.`Organization_id` = `organisationmembership`.`Organization_id`));

DROP VIEW IF EXISTS `organisationemployeesview`;
CREATE VIEW `organisationemployeesview` AS select `organisationemployees`.`id` AS `orgemp_id`,`organisationemployees`.`Organization_id` AS `orgemp_orgid`,`organisationemployees`.`Customer_id` AS `orgemp_cusid`,`customer`.`id` AS `customer_id`,`customer`.`FirstName` AS `FirstName`,`customer`.`MiddleName` AS `MiddleName`,`customer`.`LastName` AS `LastName`,`customer`.`UserName` AS `UserName`,`customer`.`DrivingLicenseNumber` AS `DrivingLicenseNumber`,`customer`.`DateOfBirth` AS `DateOfBirth`,`customer`.`Ssn` AS `Ssn`,`customercommunication`.`id` AS `custcomm_id`,`customercommunication`.`Type_id` AS `custcomm_typeid`,`customercommunication`.`Customer_id` AS `custcomm_custid`,`customercommunication`.`Value` AS `custcomm_value`,`customer`.`Status_id` AS `Status_id`,`customer`.`createdts` AS `createdts`,`customer`.`Lastlogintime` AS `Lastlogintime`,`customergroup`.`Group_id` AS `Group_id`,`membergroup`.`Name` AS `role_name` from ((((`organisationemployees` join `customer` on((`organisationemployees`.`Customer_id` = `customer`.`id`))) left join `customercommunication` on((`customer`.`id` = `customercommunication`.`Customer_id`))) left join `customergroup` on((`customer`.`id` = `customergroup`.`Customer_id`))) left join `membergroup` on((`membergroup`.`id` = `customergroup`.`Group_id`)));

DROP VIEW IF EXISTS `organisationview`;
CREATE VIEW `organisationview` AS select `organisation`.`id` AS `org_id`,`organisation`.`Name` AS `org_Name`,`organisation`.`Type_Id` AS `org_typeId`,`organisationcommunication`.`Value` AS `orgcomm_Value`,`samplemember`.`Membership_id` AS `orgmem_memid`,`samplemember`.`Taxid` AS `orgmem_taxid`,`address`.`cityName` AS `cityName`,`address`.`addressLine1` AS `addressLine1`,`address`.`addressLine2` AS `addressLine2`,`address`.`zipCode` AS `zipCode`,`sampleowner`.`FirstName` AS `orgown_firstName`,`sampleowner`.`MidleName` AS `orgown_midleName`,`sampleowner`.`LastName` AS `orgown_lastName`,`sampleowner`.`DateOfBirth` AS `orgown_dob`,`sampleowner`.`Ssn` AS `orgown_ssn`,`sampleowner`.`Email` AS `orgown_email`,`sampleowner`.`Phone` AS `orgown_phone`,`address`.`state` AS `State`,`address`.`country` AS `Country`,`customertype`.`Name` AS `TypeName` from ((((((`organisation` join `organisationcommunication`) join `organisationaddress`) join `address`) join `customertype`) left join `organisationmembership` `samplemember` on((`organisation`.`id` = `samplemember`.`Organization_id`))) left join `organisationowner` `sampleowner` on((`sampleowner`.`Organization_id` = `organisation`.`id`))) where ((`organisation`.`id` = `organisationcommunication`.`Organization_id`) and (`organisation`.`id` = `organisationaddress`.`Organization_id`) and (`organisationaddress`.`Address_id` = `address`.`id`) and (`organisation`.`Type_Id` = `customertype`.`id`));

ALTER TABLE `customercommunication` 
ADD COLUMN `type` VARCHAR(50) NULL AFTER `softdeleteflag`,
ADD COLUMN `countryType` VARCHAR(50) NULL AFTER `type`,
ADD COLUMN `receivePromotions` VARCHAR(45) NULL AFTER `countryType`,
ADD COLUMN `phoneCountryCode` VARCHAR(10) NULL AFTER `receivePromotions`;

ALTER TABLE `customer` CHANGE COLUMN `Bank_id` `Bank_id` VARCHAR(50) NULL DEFAULT '1' ;

ALTER TABLE `customeraccounts` CHANGE COLUMN `IsOrgAccountUnLinked` `IsOrgAccountUnLinked` TINYINT(1) NULL DEFAULT '0' ;

DROP VIEW IF EXISTS `organizationownerview`;
CREATE VIEW `organizationownerview` AS    SELECT DISTINCT `organisationowner`.`LastName` AS `LastName`, `organisationowner`.`DateOfBirth` AS `DateOfBirth`, `organisationowner`.`Ssn` AS `Ssn`, `organisationowner`.`Phone` AS `Phone`, `organisationowner`.`Email` AS `Email`, `organisationowner`.`FirstName` AS `FirstName`, `organisationowner`.`IDType_id` AS `IDType_id`, `organisationowner`.`IdValue` AS `IdValue`, `organisationowner`.`Organization_id` AS `Organization_Id`, `organisationmembership`.`Membership_id` AS `Membership_id`, `organisationmembership`.`Taxid` AS `Taxid`    FROM (`organisationowner` JOIN `organisationmembership`)    WHERE (`organisationowner`.`Organization_id` = `organisationmembership`.`Organization_id`);

ALTER TABLE `customer` CHANGE COLUMN `areUserAlertsTurnedOn` `areUserAlertsTurnedOn` TINYINT(1) NULL DEFAULT '0' ;

ALTER TABLE `externalaccount` CHANGE COLUMN `User_id` `User_id` VARCHAR(50) NULL DEFAULT NULL ;

ALTER TABLE `bbtemplate` ADD COLUMN `TotalAmount` DOUBLE NULL DEFAULT NULL AFTER `ActedBy`;
ALTER TABLE `bbtransaction` ADD COLUMN `TotalAmount` DOUBLE NULL DEFAULT NULL AFTER `updatedts`;

DROP VIEW IF EXISTS `customeraccountsview`;
CREATE VIEW `customeraccountsview` AS SELECT DISTINCT `accounts`.`TaxId` AS `Taxid`, `customeraccounts`.`Membership_id` AS `Membership_id`, `customeraccounts`.`Customer_id` AS `Customer_id`, `customeraccounts`.`Customer_id` AS `User_id`, `accounts`.`Account_id` AS `Account_id`, `accounts`.`Type_id` AS `Type_id`, `accounts`.`UserName` AS `userName`, `accounts`.`CurrencyCode` AS `currencyCode`, `accounts`.`AccountHolder` AS `accountHolder`, `accounts`.`error` AS `error`, `accounts`.`Address` AS `Address`, `accounts`.`Scheme` AS `Scheme`, `accounts`.`Number` AS `number`, `accounts`.`AvailableBalance` AS `availableBalance`, `accounts`.`CurrentBalance` AS `currentBalance`, `accounts`.`InterestRate` AS `interestRate`, `accounts`.`AvailableCredit` AS `availableCredit`, `accounts`.`MinimumDue` AS `minimumDue`, `accounts`.`DueDate` AS `dueDate`, `accounts`.`FirstPaymentDate` AS `firstPaymentDate`, `accounts`.`ClosingDate` AS `closingDate`, `accounts`.`PaymentTerm` AS `paymentTerm`, `accounts`.`OpeningDate` AS `openingDate`, `accounts`.`MaturityDate` AS `maturityDate`, `accounts`.`DividendLastPaidAmount` AS `dividendLastPaidAmount`, `accounts`.`DividendLastPaidDate` AS `dividendLastPaidDate`, `accounts`.`DividendPaidYTD` AS `dividendPaidYTD`, `accounts`.`DividendRate` AS `dividendRate`, `accounts`.`DividendYTD` AS `dividendYTD`, `accounts`.`EStatementmentEnable` AS `eStatementEnable`, `accounts`.`FavouriteStatus` AS `favouriteStatus`, `accounts`.`StatusDesc` AS `statusDesc`, `accounts`.`NickName` AS `nickName`, `accounts`.`OriginalAmount` AS `originalAmount`, `accounts`.`OutstandingBalance` AS `outstandingBalance`, `accounts`.`PaymentDue` AS `paymentDue`, `accounts`.`PaymentMethod` AS `paymentMethod`, `accounts`.`SwiftCode` AS `swiftCode`, `accounts`.`TotalCreditMonths` AS `totalCreditMonths`, `accounts`.`TotalDebitsMonth` AS `totalDebitsMonth`, `accounts`.`RoutingNumber` AS `routingNumber`, `accounts`.`SupportBillPay` AS `supportBillPay`, `accounts`.`SupportCardlessCash` AS `supportCardlessCash`, `accounts`.`SupportTransferFrom` AS `supportTransferFrom`, `accounts`.`SupportTransferTo` AS `supportTransferTo`, `accounts`.`SupportDeposit` AS `supportDeposit`, `accounts`.`UnpaidInterest` AS `unpaidInterest`, `accounts`.`PreviousYearsDividends` AS `previousYearsDividends`, `accounts`.`principalBalance` AS `principalBalance`, `accounts`.`PrincipalValue` AS `principalValue`, `accounts`.`RegularPaymentAmount` AS `regularPaymentAmount`, `accounts`.`phone` AS `phoneId`, `accounts`.`LastDividendPaidDate` AS `lastDividendPaidDate`, `accounts`.`LastDividendPaidAmount` AS `lastDividendPaidAmount`, `accounts`.`LastPaymentAmount` AS `lastPaymentAmount`, `accounts`.`LastPaymentDate` AS `lastPaymentDate`, `accounts`.`LastStatementBalance` AS `lastStatementBalance`, `accounts`.`LateFeesDue` AS `lateFeesDue`, `accounts`.`maturityAmount` AS `maturityAmount`, `accounts`.`MaturityOption` AS `maturityOption`, `accounts`.`payoffAmount` AS `payoffAmount`, `accounts`.`PayOffCharge` AS `payOffCharge`, `accounts`.`PendingDeposit` AS `pendingDeposit`, `accounts`.`PendingWithdrawal` AS `pendingWithdrawal`, `accounts`.`JointHolders` AS `jointHolders`, `accounts`.`IsPFM` AS `isPFM`, `accounts`.`InterestPaidYTD` AS `interestPaidYTD`, `accounts`.`InterestPaidPreviousYTD` AS `interestPaidPreviousYTD`, `accounts`.`InterestPaidLastYear` AS `interestPaidLastYear`, `accounts`.`InterestEarned` AS `interestEarned`, `accounts`.`CurrentAmountDue` AS `currentAmountDue`, `accounts`.`CreditLimit` AS `creditLimit`, `accounts`.`CreditCardNumber` AS `creditCardNumber`, `accounts`.`BsbNum` AS `bsbNum`, `accounts`.`BondInterestLastYear` AS `bondInterestLastYear`, `accounts`.`BondInterest` AS `bondInterest`, `accounts`.`AvailablePoints` AS `availablePoints`, `accounts`.`AccountName` AS `accountName`, `accounts`.`email` AS `email`, `accounts`.`IBAN` AS `IBAN`, `accounts`.`adminProductId` AS `adminProductId`, `bank`.`Description` AS `bankname`, `accounts`.`AccountPreference` AS `accountPreference`, `accounttype`.`transactionLimit` AS `transactionLimit`, `accounttype`.`transferLimit` AS `transferLimit`, `accounttype`.`rates` AS `rates`, `accounttype`.`termsAndConditions` AS `termsAndConditions`, `accounttype`.`TypeDescription` AS `typeDescription`, `accounttype`.`supportChecks` AS `supportChecks`, `accounttype`.`displayName` AS `displayName`, `accounts`.`accountSubType` AS `accountSubType`, `accounts`.`description` AS `description`, `accounts`.`schemeName` AS `schemeName`, `accounts`.`identification` AS `identification`, `accounts`.`secondaryIdentification` AS `secondaryIdentification`, `accounts`.`servicerSchemeName` AS `servicerSchemeName`, `accounts`.`servicerIdentification` AS `servicerIdentification`, `accounts`.`dataCreditDebitIndicator` AS `dataCreditDebitIndicator`, `accounts`.`dataType` AS `dataType`, `accounts`.`dataDateTime` AS `dataDateTime`, `accounts`.`dataCreditLineIncluded` AS `dataCreditLineIncluded`, `accounts`.`dataCreditLineType` AS `dataCreditLineType`, `accounts`.`dataCreditLineAmount` AS `dataCreditLineAmount`, `accounts`.`dataCreditLineCurrency` AS `dataCreditLineCurrency` FROM (((`accounts` JOIN `customeraccounts`) JOIN `accounttype`) LEFT JOIN `bank` ON ((`accounts`.`Bank_id` = `bank`.`id`))) WHERE ((`accounts`.`Account_id` = `customeraccounts`.`Account_id`)  AND (`accounts`.`Type_id` = `accounttype`.`TypeID`));

ALTER TABLE `customer` 
CHANGE COLUMN `isBillPaySupported` `isBillPaySupported` BIT(1) NULL DEFAULT b'1' ,
CHANGE COLUMN `isP2PSupported` `isP2PSupported` BIT(1) NULL DEFAULT b'1' ,
CHANGE COLUMN `isWireTransferEligible` `isWireTransferEligible` BIT(1) NULL DEFAULT b'1' ;

DROP VIEW IF EXISTS `orgemployeedetails`;
CREATE VIEW `orgemployeedetails` AS SELECT`c`.`id` AS `id`, `c`.`FirstName` AS `FirstName`, `c`.`MiddleName` AS `MiddleName`, `c`.`LastName` AS `LastName`, `c`.`UserName` AS `Username`, `c`.`Gender` AS `Gender`, `c`.`DateOfBirth` AS `DateOfBirth`, `c`.`DrivingLicenseNumber` AS `DrivingLicenseNumber`, `c`.`Ssn` AS `Ssn`, `c`.`UserCompany` AS `UserCompany`, `c`.`Lastlogintime` AS `Lastlogintime`, `c`.`Status_id` AS `Status`, `ca`.`Account_id` AS `Account_id`, `ca`.`AccountName` AS `AccountName`, `c`.`createdby` AS `createdby` FROM (`customer` `c` LEFT JOIN `customeraccounts` `ca` ON ((`c`.`id` = `ca`.`Customer_id`)));

DROP VIEW IF EXISTS `organisationemployeesview`;
CREATE VIEW `organisationemployeesview` AS SELECT  `organisationemployees`.`id` AS `orgemp_id`, `organisationemployees`.`Organization_id` AS `orgemp_orgid`, `organisationemployees`.`Customer_id` AS `orgemp_cusid`, `customer`.`id` AS `customer_id`, `customer`.`FirstName` AS `FirstName`, `customer`.`MiddleName` AS `MiddleName`, `customer`.`LastName` AS `LastName`, `customer`.`UserName` AS `UserName`, `customer`.`DrivingLicenseNumber` AS `DrivingLicenseNumber`, `customer`.`DateOfBirth` AS `DateOfBirth`, `customer`.`Ssn` AS `Ssn`, `customercommunication`.`id` AS `custcomm_id`, `customercommunication`.`Type_id` AS `custcomm_typeid`, `customercommunication`.`Customer_id` AS `custcomm_custid`, `customercommunication`.`Value` AS `custcomm_value`, `customer`.`Status_id` AS `Status_id`, `customer`.`createdts` AS `createdts`, `customer`.`Lastlogintime` AS `Lastlogintime`, `customergroup`.`Group_id` AS `Group_id`, `membergroup`.`Name` AS `role_name`, `customer`.`createdby` AS `createdby`    FROM ((((`organisationemployees` JOIN `customer` ON ((`organisationemployees`.`Customer_id` = `customer`.`id`))) LEFT JOIN `customercommunication` ON ((`customer`.`id` = `customercommunication`.`Customer_id`))) LEFT JOIN `customergroup` ON ((`customer`.`id` = `customergroup`.`Customer_id`))) LEFT JOIN `membergroup` ON ((`membergroup`.`id` = `customergroup`.`Group_id`)));

DROP VIEW IF EXISTS `fetch_achtemplate_details_view`;
CREATE VIEW `fetch_achtemplate_details_view` AS select `bbtemplate`.`Template_id` AS `Template_id`,`bbtemplate`.`TemplateName` AS `TemplateName`,`bbtemplate`.`TemplateDescription` AS `TemplateDescription`,`bbtemplate`.`DebitAccount` AS `DebitAccount`,`bbtemplate`.`EffectiveDate` AS `EffectiveDate`,`bbtemplate`.`Request_id` AS `Request_id`,`bbtemplate`.`createdby` AS `createdby`,`bbtemplate`.`createdts` AS `createdts`,`bbtemplate`.`UpdatedBy` AS `UpdatedBy`,`bbtemplate`.`Updatedts` AS `Updatedts`,`bbtemplate`.`MaxAmount` AS `MaxAmount`,`bbtemplate`.`Status` AS `Status`,`bbtemplate`.`TransactionType_id` AS `TransactionType_id`,`bbtemplate`.`TemplateType_id` AS `TemplateType_id`,`bbtemplate`.`Company_id` AS `Company_id`,`bbtemplate`.`TotalAmount` AS `TotalAmount`,`bbtemplate`.`TemplateRequestType_id` AS `TemplateRequestType_id`,`bbtemplate`.`softDelete` AS `softDelete`,`bbtemplate`.`ActedBy` AS `ActedBy`,`accounts`.`AccountName` AS `AccountName`,`customer`.`UserName` AS `CreateBy`,`bbstatus`.`status` AS `StatusValue`,`bbtransactiontype`.`TransactionTypeName` AS `TransactionTypeValue`,`bbtemplatetype`.`TemplateTypeName` AS `TemplateTypeValue`,`bbtemplaterequesttype`.`TemplateRequestTypeName` AS `TemplateRequestTypeValue`,`organisation`.`Name` AS `CompanyName`,`bbrequest`.`createdby` AS `RequestCreatedby`,`bbrequest`.`RequestType_id` AS `BBGeneralTransactionType_id` from ((((((((`bbtemplate` left join `accounts` on((`bbtemplate`.`DebitAccount` = `accounts`.`Account_id`))) left join `customer` on((`bbtemplate`.`createdby` = `customer`.`id`))) left join `bbstatus` on((`bbtemplate`.`Status` = `bbstatus`.`id`))) left join `bbtransactiontype` on((`bbtemplate`.`TransactionType_id` = `bbtransactiontype`.`TransactionType_id`))) left join `bbtemplatetype` on((`bbtemplate`.`TemplateType_id` = `bbtemplatetype`.`TemplateType_id`))) left join `bbtemplaterequesttype` on((`bbtemplate`.`TemplateRequestType_id` = `bbtemplaterequesttype`.`TemplateRequestType_id`))) left join `organisation` on((`bbtemplate`.`Company_id` = `organisation`.`id`))) left join `bbrequest` on((`bbtemplate`.`Request_id` = `bbrequest`.`Request_id`)));

DROP VIEW IF EXISTS `fetch_achtransaction_details_view`;
CREATE VIEW `fetch_achtransaction_details_view` AS select `bbtransaction`.`Transaction_id` AS `Transaction_id`,`bbtransaction`.`DebitAccount` AS `DebitAccount`,`bbtransaction`.`EffectiveDate` AS `EffectiveDate`,`bbtransaction`.`Request_id` AS `Request_id`,`bbtransaction`.`createdby` AS `createdby`,`bbtransaction`.`createdts` AS `createdts`,`bbtransaction`.`MaxAmount` AS `MaxAmount`,`bbtransaction`.`Status` AS `Status`,`bbtransaction`.`TransactionType_id` AS `TransactionType_id`,`bbtransaction`.`TemplateType_id` AS `TemplateType_id`,`bbtransaction`.`Company_id` AS `Company_id`,`bbtransaction`.`TemplateRequestType_id` AS `TemplateRequestType_id`,`bbtransaction`.`softDelete` AS `softDelete`,`bbtransaction`.`TemplateName` AS `TemplateName`,`bbtransaction`.`Template_id` AS `Template_id`,`bbtransaction`.`TotalAmount` AS `TotalAmount`,`bbtransaction`.`ConfirmationNumber` AS `ConfirmationNumber`,`bbtransaction`.`ActedBy` AS `ActedBy`,`accounts`.`AccountName` AS `AccountName`,`customer`.`UserName` AS `userName`,`bbstatus`.`status` AS `StatusValue`,`bbtransactiontype`.`TransactionTypeName` AS `TransactionTypeValue`,`bbtemplatetype`.`TemplateTypeName` AS `TemplateTypeValue`,`bbtemplaterequesttype`.`TemplateRequestTypeName` AS `TemplateRequestTypeValue`,`organisation`.`Name` AS `CompanyName`,`bbrequest`.`createdby` AS `RequestCreatedby`,`bbrequest`.`RequestType_id` AS `BBGeneralTransactionType_id` from ((((((((`bbtransaction` left join `accounts` on((`bbtransaction`.`DebitAccount` = `accounts`.`Account_id`))) left join `customer` on((`bbtransaction`.`createdby` = `customer`.`id`))) left join `bbstatus` on((`bbtransaction`.`Status` = `bbstatus`.`id`))) left join `bbtransactiontype` on((`bbtransaction`.`TransactionType_id` = `bbtransactiontype`.`TransactionType_id`))) left join `bbtemplatetype` on((`bbtransaction`.`TemplateType_id` = `bbtemplatetype`.`TemplateType_id`))) left join `bbtemplaterequesttype` on((`bbtransaction`.`TemplateRequestType_id` = `bbtemplaterequesttype`.`TemplateRequestType_id`))) left join `organisation` on((`bbtransaction`.`Company_id` = `organisation`.`id`))) left join `bbrequest` on((`bbtransaction`.`Request_id` = `bbrequest`.`Request_id`)));

ALTER TABLE `payee` ADD COLUMN `IBAN` VARCHAR(45) NULL AFTER `phoneCountryCode`;
ALTER TABLE `application` ADD COLUMN `isAccountAggregationEnabled` BIT(1) NULL DEFAULT b'0' AFTER `isBusinessBankingEnabled`;

ALTER TABLE `application` CHANGE COLUMN `isAccountAggregationEnabled` `isAccountAggregationEnabled` BIT(1) NULL DEFAULT b'1' ;

ALTER TABLE `application` CHANGE COLUMN `isAccountAggregationEnabled` `isAccountAggregationEnabled` VARCHAR(45) NULL DEFAULT 'false' ;

DROP VIEW IF EXISTS `accountransactionview`;
CREATE VIEW `accountransactionview` AS SELECT `accounts`.`Account_id` AS `Account_id`,  `accounts`.`AccountName` AS `AccountName`,  `accounts`.`AccountHolder` AS `AccountHolder`,  `accounts`.`UserName` AS `UserName`,  `accounts`.`ExternalBankidentity_id` AS `ExternalBankidentity_id`,  `accounts`.`CurrencyCode` AS `CurrencyCode`,  `accounts`.`User_id` AS `User_id`,  `accounts`.`AvailableBalance` AS `AvailableBalance`,  `accounts`.`Bank_id` AS `Bank_id`,  `accounts`.`ShowTransactions` AS `ShowTransactions`,  `accounts`.`CurrentBalance` AS `CurrentBalance`,  `accounts`.`RoutingNumber` AS `RoutingNumber`,  `transaction`.`Id` AS `transactionId`,  `transaction`.`Type_id` AS `transactiontype`,  `transaction`.`Customer_id` AS `Customer_id`,  `transaction`.`ExpenseCategory_id` AS `ExpenseCategory_id`,  `transaction`.`billid` AS `Bill_id`,  `transaction`.`Reference_id` AS `Reference_id`,  `transaction`.`fromAccountNumber` AS `fromAccountNumber`,  `transaction`.`fromAccountBalance` AS `fromAccountBalance`,  `transaction`.`toAccountNumber` AS `toAccountNumber`,  `transaction`.`toAccountBalance` AS `toAccountBalance`,  `transaction`.`amount` AS `amount`,  `transaction`.`convertedAmount` AS `convertedAmount`,  `transaction`.`transactionCurrency` AS `transactionCurrency`,  `transaction`.`baseCurrency` AS `baseCurrency`,  `transaction`.`Status_id` AS `Status_id`,  `transaction`.`statusDesc` AS `statusDesc`,  `transaction`.`isScheduled` AS `isScheduled`,  `transaction`.`category` AS `category`,  `transaction`.`billCategory` AS `billCategory`,  `transaction`.`toExternalAccountNumber` AS `ExternalAccountNumber`,  `transaction`.`Person_Id` AS `Person_Id`,  `transaction`.`frequencyType` AS `frequencyType`,  `transaction`.`createdDate` AS `createdDate`,  `transaction`.`cashlessEmail` AS `cashlessEmail`,  `transaction`.`cashlessMode` AS `cashlessMode`,  `transaction`.`cashlessOTP` AS `cashlessOTP`,  `transaction`.`cashlessOTPValidDate` AS `cashlessOTPValidDate`,  `transaction`.`cashlessPersonName` AS `cashlessPersonName`,  `transaction`.`cashlessPhone` AS `cashlessPhone`,  `transaction`.`cashlessSecurityCode` AS `cashlessSecurityCode`,  `transaction`.`cashWithdrawalTransactionStatus` AS `cashWithdrawalTransactionStatus`,  `transaction`.`frequencyEndDate` AS `frequencyEndDate`,  `transaction`.`frequencyStartDate` AS `frequencyStartDate`,  `transaction`.`hasDepositImage` AS `hasDepositImage`,  `transaction`.`Payee_id` AS `payeeId`,  `transaction`.`payeeName` AS `payeeName`,  `transaction`.`p2pContact` AS `p2pContact`,  `transaction`.`Person_Id` AS `personId`,  `transaction`.`recurrenceDesc` AS `recurrenceDesc`,  `transaction`.`numberOfRecurrences` AS `numberOfRecurrences`,  `transaction`.`scheduledDate` AS `scheduledDate`,  `transaction`.`transactionComments` AS `transactionComments`,  `transaction`.`notes` AS `transactionsNotes`,  `transaction`.`description` AS `transDescription`,  `transaction`.`transactionDate` AS `transactionDate`,  `transaction`.`frontImage1` AS `frontImage1`,  `transaction`.`frontImage2` AS `frontImage2`,  `transaction`.`backImage1` AS `backImage1`,  `transaction`.`backImage2` AS `backImage2`,  `transaction`.`checkDesc` AS `checkDesc`,  `transaction`.`checkNumber1` AS `checkNumber1`,  `transaction`.`checkNumber2` AS `checkNumber2`,  `transaction`.`checkNumber` AS `checkNumber`,  `transaction`.`checkReason` AS `checkReason`,  `transaction`.`requestValidity` AS `requestValidity`,  `transaction`.`checkDateOfIssue` AS `checkDateOfIssue`,  `transaction`.`bankName1` AS `bankName1`,  `transaction`.`bankName2` AS `bankName2`,  `transaction`.`withdrawlAmount1` AS `withdrawlAmount1`,  `transaction`.`withdrawlAmount2` AS `withdrawlAmount2`,  `transaction`.`cashAmount` AS `cashAmount`,  `transaction`.`payeeCurrency` AS `payeeCurrency`,  `transaction`.`fee` AS `fee`,  `transaction`.`feePaidByReceipent` AS `feePaidByReceipent`,  `transaction`.`feeCurrency` AS `feeCurrency`,  `transaction`.`isDisputed` AS `isDisputed`,  `transaction`.`disputeReason` AS `disputeReason`,  `transaction`.`disputeDescription` AS `disputeDescription`,  `transaction`.`disputeDate` AS `disputeDate`,  `transaction`.`disputeStatus` AS `disputeStatus`,  `transactiontype`.`description` AS `description`,  `transaction`.`statementReference` AS `statementReference`,  `transaction`.`transCreditDebitIndicator` AS `transCreditDebitIndicator`,  `transaction`.`bookingDateTime` AS `bookingDateTime`,  `transaction`.`valueDateTime` AS `valueDateTime`,  `transaction`.`transactionInformation` AS `transactionInformation`,  `transaction`.`addressLine` AS `addressLine`,  `transaction`.`transactionAmount` AS `transactionAmount`,  `transaction`.`chargeAmount` AS `chargeAmount`,  `transaction`.`chargeCurrency` AS `chargeCurrency`,  `transaction`.`sourceCurrency` AS `sourceCurrency`,  `transaction`.`targetCurrency` AS `targetCurrency`,  `transaction`.`unitCurrency` AS `unitCurrency`,  `transaction`.`exchangeRate` AS `exchangeRate`,  `transaction`.`contractIdentification` AS `contractIdentification`,  `transaction`.`quotationDate` AS `quotationDate`,  `transaction`.`instructedAmount` AS `instructedAmount`,  `transaction`.`instructedCurrency` AS `instructedCurrency`,  `transaction`.`transactionCode` AS `transactionCode`,  `transaction`.`transactionSubCode` AS `transactionSubCode`,  `transaction`.`proprietaryTransactionCode` AS `proprietaryTransactionCode`,  `transaction`.`proprietaryTransactionIssuer` AS `proprietaryTransactionIssuer`,  `transaction`.`balanceCreditDebitIndicator` AS `balanceCreditDebitIndicator`,  `transaction`.`balanceType` AS `balanceType`,  `transaction`.`balanceAmount` AS `balanceAmount`,  `transaction`.`balanceCurrency` AS `balanceCurrency`,  `transaction`.`merchantName` AS `merchantName`,  `transaction`.`merchantCategoryCode` AS `merchantCategoryCode`,  `transaction`.`creditorAgentSchemeName` AS `creditorAgentSchemeName`,  `transaction`.`creditorAgentIdentification` AS `creditorAgentIdentification`,  `transaction`.`creditorAgentName` AS `creditorAgentName`,  `transaction`.`creditorAgentaddressType` AS `creditorAgentaddressType`,  `transaction`.`creditorAgentDepartment` AS `creditorAgentDepartment`,  `transaction`.`creditorAgentSubDepartment` AS `creditorAgentSubDepartment`,  `transaction`.`creditorAgentStreetName` AS `creditorAgentStreetName`,  `transaction`.`creditorAgentBuildingNumber` AS `creditorAgentBuildingNumber`,  `transaction`.`creditorAgentPostCode` AS `creditorAgentPostCode`,  `transaction`.`creditorAgentTownName` AS `creditorAgentTownName`,  `transaction`.`creditorAgentCountrySubDivision` AS `creditorAgentCountrySubDivision`,  `transaction`.`creditorAgentCountry` AS `creditorAgentCountry`,  `transaction`.`creditorAgentAddressLine` AS `creditorAgentAddressLine`,  `transaction`.`creditorAccountSchemeName` AS `creditorAccountSchemeName`,  `transaction`.`creditorAccountIdentification` AS `creditorAccountIdentification`,  `transaction`.`creditorAccountName` AS `creditorAccountName`,  `transaction`.`creditorAccountSeconIdentification` AS `creditorAccountSeconIdentification`,  `transaction`.`debtorAgentSchemeName` AS `debtorAgentSchemeName`,  `transaction`.`debtorAgentIdentification` AS `debtorAgentIdentification`,  `transaction`.`debtorAgentName` AS `debtorAgentName`,  `transaction`.`debtorAgentAddressType` AS `debtorAgentAddressType`,  `transaction`.`debtorAgentDepartment` AS `debtorAgentDepartment`,  `transaction`.`debtorAgentSubDepartment` AS `debtorAgentSubDepartment`,  `transaction`.`debtorAgentStreetName` AS `debtorAgentStreetName`,  `transaction`.`debtorAgentBuildingNumber` AS `debtorAgentBuildingNumber`,  `transaction`.`dedtorAgentPostCode` AS `dedtorAgentPostCode`,  `transaction`.`debtorAgentTownName` AS `debtorAgentTownName`,  `transaction`.`debtorAgentCountrySubDivision` AS `debtorAgentCountrySubDivision`,  `transaction`.`debtorAgentCountry` AS `debtorAgentCountry`,  `transaction`.`debtorAgentAddressLine` AS `debtorAgentAddressLine`,  `transaction`.`debtorAccountSchemeName` AS `debtorAccountSchemeName`,  `transaction`.`debtorAccountIdentification` AS `debtorAccountIdentification`,  `transaction`.`debtorAccountName` AS `debtorAccountName`,  `transaction`.`debtorAccountSeconIdentification` AS `debtorAccountSeconIdentification`,  `transaction`.`cardInstrumentSchemeName` AS `cardInstrumentSchemeName`,  `transaction`.`cardInstrumentAuthorisationType` AS `cardInstrumentAuthorisationType`,  `transaction`.`cardInstrumentName` AS `cardInstrumentName`,  `transaction`.`cardInstrumentIdentification` AS `cardInstrumentIdentification`,  `transaction`.`FirstPaymentDateTime` AS `FirstPaymentDateTime`,  `transaction`.`NextPaymentDateTime` AS `NextPaymentDateTime`,  `transaction`.`FinalPaymentDateTime` AS `FinalPaymentDateTime`,  `transaction`.`StandingOrderStatusCode` AS `StandingOrderStatusCode`,  `transaction`.`FP_Amount` AS `FP_Amount`,  `transaction`.`FP_Currency` AS `FP_Currency`,  `transaction`.`NP_Amount` AS `NP_Amount`,  `transaction`.`NP_Currency` AS `NP_Currency`,  `transaction`.`FPA_Amount` AS `FPA_Amount`,  `transaction`.`FPA_Currency` AS `FPA_Currency`,  `transaction`.`IBAN` AS `IBAN`,  `transaction`.`sortCode` AS `sortCode`,  `transaction`.`beneficiaryName` AS `beneficiaryName`,  `transaction`.`bankName` AS `bankName`,  `transaction`.`swiftCode` AS `swiftCode`,  `accounts`.`NickName` AS `nickName` FROM  ((`accounts`  JOIN `transaction`)  JOIN `transactiontype`) WHERE  (((`accounts`.`Account_id` = `transaction`.`fromAccountNumber`)OR (`accounts`.`Account_id` = `transaction`.`toAccountNumber`))AND (`transaction`.`Type_id` = `transactiontype`.`Id`)) GROUP BY `transaction`.`Id` ORDER BY `transaction`.`createdDate` DESC;

DROP VIEW IF EXISTS `customeraddressmbview`;
CREATE VIEW `customeraddressmbview` AS select `c`.`id` AS `CustomerId`,`ca`.`Address_id` AS `Address_id`,`ca`.`isPrimary` AS `isPrimary`,`ca`.`Type_id` AS `AddressType`,`a`.`id` AS `AddressId`,`a`.`addressLine1` AS `AddressLine1`,`a`.`addressLine2` AS `AddressLine2`,`a`.`addressLine3` AS `AddressLine3`,`a`.`zipCode` AS `ZipCode`,`a`.`cityName` AS `CityName`,`a`.`country` AS `CountryName`,`a`.`state` AS `State` from ((`customeraddress` `ca` left join `customer` `c` on((`ca`.`Customer_id` = `c`.`id`))) left join `address` `a` on((`a`.`id` = `ca`.`Address_id`))); 

DROP VIEW IF EXISTS `customercommunicationview`;
CREATE VIEW `customercommunicationview` AS SELECT  `customer`.`id` AS `id`,  `customer`.`FirstName` AS `FirstName`,  `customer`.`MiddleName` AS `MiddleName`,  `customer`.`LastName` AS `LastName`,  `customer`.`UserName` AS `UserName`,  `customer`.`Gender` AS `Gender`,  `customer`.`DateOfBirth` AS `DateOfBirth`,  `customer`.`Ssn` AS `Ssn`,  `customertype`.`Name` AS `CustomerType`,  `primaryphone`.`Value` AS `Phone`,  `primaryemail`.`Value` AS `Email` FROM  (((`customer`  LEFT JOIN `customercommunication` `primaryphone` ON (((`primaryphone`.`Customer_id` = `customer`.`id`)AND (`primaryphone`.`isPrimary` = 1)AND (`primaryphone`.`Type_id` = 'COMM_TYPE_PHONE'))))  LEFT JOIN `customercommunication` `primaryemail` ON (((`primaryemail`.`Customer_id` = `customer`.`id`)AND (`primaryemail`.`isPrimary` = 1)AND (`primaryemail`.`Type_id` = 'COMM_TYPE_EMAIL'))))  LEFT JOIN `customertype` ON ((`customertype`.`id` = `customer`.`CustomerType_id`)));

DROP VIEW IF EXISTS `wireaccounttransactionview`;
CREATE VIEW `wireaccounttransactionview` AS SELECT`accounts`.`Account_id` AS `Account_id`,  `accounts`.`AccountName` AS `AccountName`,  `accounts`.`AccountHolder` AS `AccountHolder`,  `accounts`.`UserName` AS `UserName`,  `accounts`.`ExternalBankidentity_id` AS `ExternalBankidentity_id`,  `accounts`.`CurrencyCode` AS `CurrencyCode`,  `accounts`.`User_id` AS `User_id`,  `accounts`.`AvailableBalance` AS `AvailableBalance`,  `accounts`.`Bank_id` AS `Bank_id`,  `accounts`.`ShowTransactions` AS `ShowTransactions`,  `accounts`.`CurrentBalance` AS `CurrentBalance`,  `accounts`.`SwiftCode` AS `SwiftCode`,  `accounts`.`RoutingNumber` AS `RoutingNumber`,  `transaction`.`Id` AS `transactionId`,  `transaction`.`Type_id` AS `transactiontype`,  `transaction`.`Customer_id` AS `Customer_id`,  `transaction`.`ExpenseCategory_id` AS `ExpenseCategory_id`,  `transaction`.`billid` AS `Bill_id`,  `transaction`.`Reference_id` AS `Reference_id`,  `transaction`.`fromAccountNumber` AS `fromAccountNumber`,  `transaction`.`fromAccountBalance` AS `fromAccountBalance`,  `transaction`.`toAccountNumber` AS `toAccountNumber`,  `transaction`.`toAccountBalance` AS `toAccountBalance`,  `transaction`.`amount` AS `amount`,  `transaction`.`Status_id` AS `Status_id`,  `transaction`.`statusDesc` AS `statusDesc`,  `transaction`.`isScheduled` AS `isScheduled`,  `transaction`.`category` AS `category`,  `transaction`.`billCategory` AS `billCategory`,  `transaction`.`toExternalAccountNumber` AS `ExternalAccountNumber`,  `transaction`.`Person_Id` AS `Person_Id`,  `transaction`.`frequencyType` AS `frequencyType`,  `transaction`.`createdDate` AS `createdDate`,  `transaction`.`cashlessEmail` AS `cashlessEmail`,  `transaction`.`cashlessMode` AS `cashlessMode`,  `transaction`.`cashlessOTP` AS `cashlessOTP`,  `transaction`.`cashlessOTPValidDate` AS `cashlessOTPValidDate`,  `transaction`.`cashlessPersonName` AS `cashlessPersonName`,  `transaction`.`cashlessPhone` AS `cashlessPhone`,  `transaction`.`cashlessSecurityCode` AS `cashlessSecurityCode`,  `transaction`.`cashWithdrawalTransactionStatus` AS `cashWithdrawalTransactionStatus`,  `transaction`.`frequencyEndDate` AS `frequencyEndDate`,  `transaction`.`frequencyStartDate` AS `frequencyStartDate`,  `transaction`.`hasDepositImage` AS `hasDepositImage`,  `transaction`.`Payee_id` AS `payeeId`,  `transaction`.`payeeName` AS `payeeName`,  `transaction`.`p2pContact` AS `p2pContact`,  `transaction`.`Person_Id` AS `personId`,  `transaction`.`recurrenceDesc` AS `recurrenceDesc`,  `transaction`.`numberOfRecurrences` AS `numberOfRecurrences`,  `transaction`.`scheduledDate` AS `scheduledDate`,  `transaction`.`transactionComments` AS `transactionComments`,  `transaction`.`notes` AS `transactionsNotes`,  `transaction`.`description` AS `transDescription`,  `transaction`.`transactionDate` AS `transactionDate`,  `transaction`.`frontImage1` AS `frontImage1`,  `transaction`.`frontImage2` AS `frontImage2`,  `transaction`.`backImage1` AS `backImage1`,  `transaction`.`backImage2` AS `backImage2`,  `transaction`.`checkDesc` AS `checkDesc`,  `transaction`.`checkNumber1` AS `checkNumber1`,  `transaction`.`checkNumber2` AS `checkNumber2`,  `transaction`.`checkNumber` AS `checkNumber`,  `transaction`.`checkReason` AS `checkReason`,  `transaction`.`requestValidity` AS `requestValidity`,  `transaction`.`checkDateOfIssue` AS `checkDateOfIssue`,  `transaction`.`bankName1` AS `bankName1`,  `transaction`.`bankName2` AS `bankName2`,  `transaction`.`withdrawlAmount1` AS `withdrawlAmount1`,  `transaction`.`withdrawlAmount2` AS `withdrawlAmount2`,  `transaction`.`cashAmount` AS `cashAmount`,  `transaction`.`amountRecieved` AS `amountRecieved`,  `transaction`.`payeeCurrency` AS `payeeCurrency`,  `transaction`.`fee` AS `fee`,  `transaction`.`isDisputed` AS `isDisputed`,  `transaction`.`disputeReason` AS `disputeReason`,  `transaction`.`disputeDescription` AS `disputeDescription`,  `transaction`.`disputeDate` AS `disputeDate`,  `transaction`.`disputeStatus` AS `disputeStatus`,  `transactiontype`.`description` AS `description`,  `payee`.`nickName` AS `nickName`,  `payee`.`accountNumber` AS `payeeAccountNumber`,  `payee`.`Type_id` AS `payeeType`,  `payee`.`addressLine1` AS `payeeAddressLine2`,  `payee`.`addressLine2` AS `payeeAddressLine1`  FROM  (((`accounts`  JOIN `transaction`)  JOIN `transactiontype`)  JOIN `payee`) WHERE  (((`accounts`.`Account_id` = `transaction`.`fromAccountNumber`)OR (`accounts`.`Account_id` = `transaction`.`toAccountNumber`))AND (`transaction`.`Type_id` = `transactiontype`.`Id`)AND (`transaction`.`Payee_id` = `payee`.`Id`)) GROUP BY `transaction`.`Id` ORDER BY `transaction`.`createdDate` DESC;
