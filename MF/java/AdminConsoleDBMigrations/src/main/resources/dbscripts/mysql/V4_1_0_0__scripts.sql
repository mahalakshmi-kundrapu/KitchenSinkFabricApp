ALTER TABLE `customer` DROP FOREIGN KEY `FK_Customer_CustomerType`;
ALTER TABLE `customer` DROP COLUMN `Type_id`,DROP INDEX `IXFK_Customer_CustomerType`;
DROP TABLE `customertype`;

UPDATE `alerttype` SET `Name`='Transactional' WHERE `id`='ALERT_TYPE_ID_2_TP';

CREATE TABLE `compositepermission` (
  `id` varchar(50) NOT NULL,
  `Permission_id` varchar(50) NOT NULL,
  `Name` varchar(50) NOT NULL,
  `Description` varchar(300) DEFAULT NULL,
  `isEnabled` tinyint(1) NOT NULL DEFAULT '0',
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_CompositePermission_PermissionId_idx` (`Permission_id`),
  CONSTRAINT `FK_CompositePermission_PermissionId` FOREIGN KEY (`Permission_id`) REFERENCES `permission` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `rolecompositepermission` (
  `Role_id` varchar(50) NOT NULL,
  `CompositePermission_id` varchar(50) NOT NULL,
  `isEnabled` tinyint(1) NOT NULL DEFAULT '0',
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`Role_id`,`CompositePermission_id`),
  KEY `FK_RoleCompositePermission_CompositePermission_idx` (`CompositePermission_id`),
  CONSTRAINT `FK_RoleCompositePermission_CompositePermission` FOREIGN KEY (`CompositePermission_id`) REFERENCES `compositepermission` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_RoleCompositePermission_Role` FOREIGN KEY (`Role_id`) REFERENCES `role` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `usercompositepermission` (
  `User_id` varchar(50) NOT NULL,
  `CompositePermission_id` varchar(50) NOT NULL,
  `isEnabled` tinyint(1) NOT NULL DEFAULT '0',
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`User_id`,`CompositePermission_id`),
  KEY `FK_UserCompositePermission_CompositePermission_idx` (`CompositePermission_id`),
  CONSTRAINT `FK_UserCompositePermission_CompositePermission` FOREIGN KEY (`CompositePermission_id`) REFERENCES `compositepermission` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_UserCompositePermission_User` FOREIGN KEY (`User_id`) REFERENCES `systemuser` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `permission` ADD COLUMN `isComposite` TINYINT(1) NOT NULL DEFAULT '0' AFTER `Description`;

UPDATE `permission` SET `isComposite`='1' WHERE `id`='PID45';

INSERT INTO `compositepermission` (`id`, `Permission_id`, `Name`, `Description`, `isEnabled`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('CPID01', 'PID45', 'CSRMultipleBillPay', 'Pay multiple bills saved payees', '1', 'Kony Dev', 'Kony User', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '0');
INSERT INTO `compositepermission` (`id`, `Permission_id`, `Name`, `Description`, `isEnabled`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('CPID02', 'PID45', 'CSREBillActivationDeactivation', 'Activate or deactivate e-Bill feature for a specific biller', '1', 'Kony Dev', 'Kony User', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '0');
INSERT INTO `compositepermission` (`id`, `Permission_id`, `Name`, `Description`, `isEnabled`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('CPID03', 'PID45', 'CSRStopCheckPayment', 'Stop a check payment (single/series of check)', '1', 'Kony Dev', 'Kony User', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '0');
INSERT INTO `compositepermission` (`id`, `Permission_id`, `Name`, `Description`, `isEnabled`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('CPID04', 'PID45', 'CSRSendMoneyToAPayee', 'Send money (P2P) to a saved payee', '0', 'Kony Dev', 'Kony User', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '0');
INSERT INTO `compositepermission` (`id`, `Permission_id`, `Name`, `Description`, `isEnabled`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('CPID06', 'PID45', 'CSRLoanPayOff', 'Loan Payoff', '1', 'Kony Dev', 'Kony User', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '0');
INSERT INTO `compositepermission` (`id`, `Permission_id`, `Name`, `Description`, `isEnabled`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('CPID07', 'PID45', 'CSRDisputeTransaction', 'Dispute a specific transaction on behalf of the user', '1', 'Kony Dev', 'Kony User', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '0');
INSERT INTO `compositepermission` (`id`, `Permission_id`, `Name`, `Description`, `isEnabled`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('CPID08', 'PID45', 'CSRTransferToSamBankAccount', 'Transfer to someone elses account in the same bank (to a saved recipient)', '1', 'Kony Dev', 'Kony User', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '0');
INSERT INTO `compositepermission` (`id`, `Permission_id`, `Name`, `Description`, `isEnabled`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('CPID09', 'PID45', 'CSRInternalAccountToAccountTransfer', 'Internal account-to-account transfer (to a saved recipient)', '0', 'Kony Dev', 'Kony User', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '0');
INSERT INTO `compositepermission` (`id`, `Permission_id`, `Name`, `Description`, `isEnabled`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('CPID10', 'PID45', 'CSRTransferToDomesticAccount', 'Transfer to an account in another domestic bank (to a saved recipient)', '1', 'Kony Dev', 'Kony User', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '0');
INSERT INTO `compositepermission` (`id`, `Permission_id`, `Name`, `Description`, `isEnabled`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('CPID11', 'PID45', 'CSRTransferToInternationalAccount', 'Transfer to an account in another international bank (to a saved recipient)', '1', 'Kony Dev', 'Kony User', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '0');
INSERT INTO `compositepermission` (`id`, `Permission_id`, `Name`, `Description`, `isEnabled`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('CPID12', 'PID45', 'CSRSingleBillPay', 'Pay a single bill to saved payee', '1', 'Kony Dev', 'Kony User', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '0');
INSERT INTO `compositepermission` (`id`, `Permission_id`, `Name`, `Description`, `isEnabled`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('CPID13', 'PID45', 'CSRWireTransfer', 'Wire transfer (to a saved recipient)', '0', 'Kony Dev', 'Kony User', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '0');

INSERT INTO `rolecompositepermission` (`Role_id`, `CompositePermission_id`, `isEnabled`, `createdby`, `modifiedby`) VALUES ('RID_SUPERADMIN', 'CPID01', '1', 'Kony Dev', 'Kony User');
INSERT INTO `rolecompositepermission` (`Role_id`, `CompositePermission_id`, `isEnabled`, `createdby`, `modifiedby`) VALUES ('RID_SUPERADMIN', 'CPID02', '1', 'Kony Dev', 'Kony User');
INSERT INTO `rolecompositepermission` (`Role_id`, `CompositePermission_id`, `isEnabled`, `createdby`, `modifiedby`) VALUES ('RID_SUPERADMIN', 'CPID03', '1', 'Kony Dev', 'Kony User');
INSERT INTO `rolecompositepermission` (`Role_id`, `CompositePermission_id`, `isEnabled`, `createdby`, `modifiedby`) VALUES ('RID_SUPERADMIN', 'CPID04', '1', 'Kony Dev', 'Kony User');
INSERT INTO `rolecompositepermission` (`Role_id`, `CompositePermission_id`, `isEnabled`, `createdby`, `modifiedby`) VALUES ('RID_SUPERADMIN', 'CPID06', '1', 'Kony Dev', 'Kony User');
INSERT INTO `rolecompositepermission` (`Role_id`, `CompositePermission_id`, `isEnabled`, `createdby`, `modifiedby`) VALUES ('RID_SUPERADMIN', 'CPID07', '1', 'Kony Dev', 'Kony User');
INSERT INTO `rolecompositepermission` (`Role_id`, `CompositePermission_id`, `isEnabled`, `createdby`, `modifiedby`) VALUES ('RID_SUPERADMIN', 'CPID08', '1', 'Kony Dev', 'Kony User');
INSERT INTO `rolecompositepermission` (`Role_id`, `CompositePermission_id`, `isEnabled`, `createdby`, `modifiedby`) VALUES ('RID_SUPERADMIN', 'CPID09', '1', 'Kony Dev', 'Kony User');
INSERT INTO `rolecompositepermission` (`Role_id`, `CompositePermission_id`, `isEnabled`, `createdby`, `modifiedby`) VALUES ('RID_SUPERADMIN', 'CPID10', '1', 'Kony Dev', 'Kony User');
INSERT INTO `rolecompositepermission` (`Role_id`, `CompositePermission_id`, `isEnabled`, `createdby`, `modifiedby`) VALUES ('RID_SUPERADMIN', 'CPID11', '1', 'Kony Dev', 'Kony User');
INSERT INTO `rolecompositepermission` (`Role_id`, `CompositePermission_id`, `isEnabled`, `createdby`, `modifiedby`) VALUES ('RID_SUPERADMIN', 'CPID12', '1', 'Kony Dev', 'Kony User');
INSERT INTO `rolecompositepermission` (`Role_id`, `CompositePermission_id`, `isEnabled`, `createdby`, `modifiedby`) VALUES ('RID_SUPERADMIN', 'CPID13', '1', 'Kony Dev', 'Kony User');
INSERT INTO `rolecompositepermission` (`Role_id`, `CompositePermission_id`, `isEnabled`, `createdby`, `modifiedby`) VALUES ('RID_BUSINESS', 'CPID01', '1', 'Kony Dev', 'Kony User');
INSERT INTO `rolecompositepermission` (`Role_id`, `CompositePermission_id`, `isEnabled`, `createdby`, `modifiedby`) VALUES ('RID_BUSINESS', 'CPID02', '1', 'Kony Dev', 'Kony User');
INSERT INTO `rolecompositepermission` (`Role_id`, `CompositePermission_id`, `isEnabled`, `createdby`, `modifiedby`) VALUES ('RID_BUSINESS', 'CPID03', '1', 'Kony Dev', 'Kony User');
INSERT INTO `rolecompositepermission` (`Role_id`, `CompositePermission_id`, `isEnabled`, `createdby`, `modifiedby`) VALUES ('RID_BUSINESS', 'CPID04', '1', 'Kony Dev', 'Kony User');
INSERT INTO `rolecompositepermission` (`Role_id`, `CompositePermission_id`, `isEnabled`, `createdby`, `modifiedby`) VALUES ('RID_BUSINESS', 'CPID06', '1', 'Kony Dev', 'Kony User');
INSERT INTO `rolecompositepermission` (`Role_id`, `CompositePermission_id`, `isEnabled`, `createdby`, `modifiedby`) VALUES ('RID_BUSINESS', 'CPID07', '1', 'Kony Dev', 'Kony User');
INSERT INTO `rolecompositepermission` (`Role_id`, `CompositePermission_id`, `isEnabled`, `createdby`, `modifiedby`) VALUES ('RID_BUSINESS', 'CPID08', '1', 'Kony Dev', 'Kony User');
INSERT INTO `rolecompositepermission` (`Role_id`, `CompositePermission_id`, `isEnabled`, `createdby`, `modifiedby`) VALUES ('RID_BUSINESS', 'CPID09', '1', 'Kony Dev', 'Kony User');
INSERT INTO `rolecompositepermission` (`Role_id`, `CompositePermission_id`, `isEnabled`, `createdby`, `modifiedby`) VALUES ('RID_BUSINESS', 'CPID10', '1', 'Kony Dev', 'Kony User');
INSERT INTO `rolecompositepermission` (`Role_id`, `CompositePermission_id`, `isEnabled`, `createdby`, `modifiedby`) VALUES ('RID_BUSINESS', 'CPID11', '1', 'Kony Dev', 'Kony User');
INSERT INTO `rolecompositepermission` (`Role_id`, `CompositePermission_id`, `isEnabled`, `createdby`, `modifiedby`) VALUES ('RID_BUSINESS', 'CPID12', '1', 'Kony Dev', 'Kony User');
INSERT INTO `rolecompositepermission` (`Role_id`, `CompositePermission_id`, `isEnabled`, `createdby`, `modifiedby`) VALUES ('RID_BUSINESS', 'CPID13', '1', 'Kony Dev', 'Kony User');

CREATE TABLE `csrassistgrant` (
  `id` varchar(50) NOT NULL,
  `userId` varchar(50) NOT NULL,
  `customerId` varchar(50) NOT NULL,
  `isConsumed` tinyint(1) NOT NULL DEFAULT '0',
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `lastretrievedts` timestamp NULL DEFAULT NULL,
  `tokenconsumedts` timestamp NULL DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_CSRAssistGrant_SystemUser_idx` (`userId`),
  KEY `FK_CSRAssistGrant_Customer_idx` (`customerId`),
  CONSTRAINT `FK_CSRAssistGrant_Customer` FOREIGN KEY (`customerId`) REFERENCES `customer` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_CSRAssistGrant_SystemUser` FOREIGN KEY (`userId`) REFERENCES `systemuser` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `customer` 
ADD COLUMN `Classification_id` VARCHAR(50) NULL AFTER `id`,
ADD COLUMN `Password` VARCHAR(50) NULL AFTER `Username`,
ADD COLUMN `unsuccessfulLoginAttempts` INT NULL AFTER `Password`,
ADD COLUMN `LockCount` VARCHAR(50) NULL AFTER `unsuccessfulLoginAttempts`,
ADD COLUMN `DrivingLicenseNumber` VARCHAR(50) NULL AFTER `DateOfBirth`,
ADD COLUMN `Cvv` VARCHAR(50) NULL AFTER `Ssn`,
ADD COLUMN `Token` VARCHAR(200) NULL AFTER `Cvv`,
ADD COLUMN `Pin` VARCHAR(10) NULL AFTER `Token`,
ADD COLUMN `NoOfDependents` VARCHAR(50) NULL AFTER `SpouseName`,
ADD COLUMN `Role` VARCHAR(50) NULL AFTER `NoOfDependents`,
ADD COLUMN `UserCompany` VARCHAR(50) NULL AFTER `EmployementStatus_id`,
ADD COLUMN `SecurityImage_id` VARCHAR(50) NULL AFTER `UserCompany`,
ADD COLUMN `CountryCode` VARCHAR(50) NULL AFTER `Location_id`,
ADD COLUMN `UserImage` LONGTEXT NULL AFTER `CountryCode`,
ADD COLUMN `UserImageURL` VARCHAR(50) NULL AFTER `UserImage`,
ADD COLUMN `OlbEnrollmentStatus_id` VARCHAR(50) NULL AFTER `UserImageURL`,
ADD COLUMN `Otp` VARCHAR(50) NULL AFTER `OlbEnrollmentStatus_id`,
ADD COLUMN `PreferedOtpMethod` VARCHAR(50) NULL AFTER `Otp`,
ADD COLUMN `OtpGenaratedts` TIMESTAMP NULL AFTER `PreferedOtpMethod`,
ADD COLUMN `ValidDate` TIMESTAMP NULL AFTER `OtpGenaratedts`,
ADD COLUMN `isUserAccountLocked` TINYINT(1) NOT NULL DEFAULT 0 AFTER `ValidDate`,
ADD COLUMN `IsPinSet` TINYINT(1) NOT NULL DEFAULT 0 AFTER `isUserAccountLocked`,
ADD COLUMN `IsPhoneEnabled` TINYINT(1) NOT NULL DEFAULT 0 AFTER `IsAssistConsented`,
ADD COLUMN `IsEmailEnabled` TINYINT(1) NOT NULL DEFAULT 0 AFTER `IsPhoneEnabled`,
ADD COLUMN `isEnrolled` TINYINT(1) NOT NULL DEFAULT 0 AFTER `IsEmailEnabled`,
ADD COLUMN `isSuperAdmin` TINYINT(1) NOT NULL DEFAULT 0 AFTER `isEnrolled`,
ADD COLUMN `CurrentLoginTime` TIMESTAMP NULL AFTER `isSuperAdmin`,
ADD COLUMN `Lastlogintime` TIMESTAMP NULL AFTER `CurrentLoginTime`,
CHANGE COLUMN `Status_id` `Status_id` VARCHAR(50) NULL DEFAULT NULL AFTER `LastName`,
CHANGE COLUMN `PreferredContactMethod` `PreferredContactMethod` VARCHAR(50) NULL DEFAULT NULL AFTER `Pin`,
CHANGE COLUMN `PreferredContactTime` `PreferredContactTime` VARCHAR(50) NULL DEFAULT NULL AFTER `PreferredContactMethod`,
CHANGE COLUMN `Location_id` `Location_id` VARCHAR(50) NULL DEFAULT NULL AFTER `SecurityImage_id`,
CHANGE COLUMN `IsEnrolledForOlb` `IsEnrolledForOlb` TINYINT(1) NOT NULL DEFAULT 0 AFTER `IsPinSet`;

ALTER TABLE `customer` 
 ADD INDEX `IXFK_Customer_SecurityImage` (`SecurityImage_id` ASC);
 
 ALTER TABLE `customer` 
 ADD CONSTRAINT `FK_Customer_SecurityImage`
	FOREIGN KEY (`SecurityImage_id`) REFERENCES `securityimage` (`id`);
	
ALTER TABLE `customer` 
 ADD CONSTRAINT `FK_Customer_Status_04`
	FOREIGN KEY (`OlbEnrollmentStatus_id`) REFERENCES `status` (`id`);
	
CREATE TABLE `customerpreference` (
  `id` varchar(50) NOT NULL,
  `Customer_id` varchar(50) DEFAULT NULL,
  `DefaultAccountTransfers` varchar(50) DEFAULT NULL,
  `DefaultModule_id` varchar(50) DEFAULT NULL,
  `DefaultAccountPayments` varchar(50) DEFAULT NULL,
  `DefaultAccountCardless` varchar(50) DEFAULT NULL,
  `DefaultAccountBillPay` varchar(50) DEFAULT NULL,
  `DefaultToAccountP2P` varchar(50) DEFAULT NULL,
  `DefaultFromAccountP2P` varchar(50) DEFAULT NULL,
  `DefaultAccountWire` varchar(50) DEFAULT NULL,
  `areUserAlertsTurnedOn` varchar(50) DEFAULT NULL,
  `areDepositTermsAccepted` varchar(50) DEFAULT NULL,
  `areAccountStatementTermsAccepted` varchar(50) DEFAULT NULL,
  `isBillPaySupported` varchar(50) DEFAULT NULL,
  `isP2PSupported` varchar(50) DEFAULT NULL,
  `isBillPayActivated` varchar(50) DEFAULT NULL,
  `isP2PActivated` varchar(50) DEFAULT NULL,
  `isWireTransferActivated` varchar(50) DEFAULT NULL,
  `isWireTransferEligible` varchar(50) DEFAULT NULL,
  `ShowBillPayFromAccPopup` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_CustomerPreference_Customer_idx` (`Customer_id`),
  CONSTRAINT `FK_CustomerPreference_Customer` FOREIGN KEY (`Customer_id`) REFERENCES `customer` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `country` (`id`, `Code`, `Name`, `createdby`, `modifiedby`) VALUES ('COUNTRY_ID3', 'GER', 'Germany', 'Kony User', 'Kony Dev');
INSERT INTO `country` (`id`, `Code`, `Name`, `createdby`, `modifiedby`) VALUES ('COUNTRY_ID4', 'CHE', 'Switzerland', 'Kony User', 'Kony Dev');
INSERT INTO `region` (`id`, `Country_id`, `Code`, `Name`, `createdby`, `modifiedby`) VALUES ('REGION_ID82', 'COUNTRY_ID3', 'BY', 'Bavaria', 'Kony User', 'Kony Dev');
INSERT INTO `region` (`id`, `Country_id`, `Code`, `Name`, `createdby`, `modifiedby`) VALUES ('REGION_ID83', 'COUNTRY_ID3', 'BE', 'Berlin', 'Kony User', 'Kony Dev');
INSERT INTO `region` (`id`, `Country_id`, `Code`, `Name`, `createdby`, `modifiedby`) VALUES ('REGION_ID84', 'COUNTRY_ID4', 'GE', 'Geneva', 'Kony User', 'Kony Dev');
INSERT INTO `region` (`id`, `Country_id`, `Code`, `Name`, `createdby`, `modifiedby`) VALUES ('REGION_ID85', 'COUNTRY_ID4', 'GL', 'Glarus', 'Kony User', 'Kony Dev');
INSERT INTO `city` (`id`, `Region_id`, `Country_id`, `Name`, `createdby`, `modifiedby`) VALUES ('CITY_ID1509', 'REGION_ID83', 'COUNTRY_ID3', 'Berlin', 'Kony User', 'Kony Dev');
INSERT INTO `city` (`id`, `Region_id`, `Country_id`, `Name`, `createdby`, `modifiedby`) VALUES ('CITY_ID1510', 'REGION_ID82', 'COUNTRY_ID3', 'Munich', 'Kony User', 'Kony Dev');
INSERT INTO `city` (`id`, `Region_id`, `Country_id`, `Name`, `createdby`, `modifiedby`) VALUES ('CITY_ID1511', 'REGION_ID84', 'COUNTRY_ID4', 'Geneva', 'Kony User', 'Kony Dev');
INSERT INTO `city` (`id`, `Region_id`, `Country_id`, `Name`, `createdby`, `modifiedby`) VALUES ('CITY_ID1512', 'REGION_ID85', 'COUNTRY_ID4', 'Glarus', 'Kony User', 'Kony Dev');

INSERT INTO `membergroup` (`id`, `Name`, `Description`, `Status_id`, `createdby`, `modifiedby`) VALUES ('DEFAULT_GROUP', 'Default Group', 'All customers created for Online and Mobile Banking application demos', 'SID_ACTIVE', 'Kony User', 'Kony Dev');
INSERT INTO `groupentitlement` (`Group_id`, `Service_id`, `TransactionFee_id`, `TransactionLimit_id`, `createdby`, `modifiedby`) VALUES ('DEFAULT_GROUP', 'SERVICE_ID_1', 'TID1', 'TID1', 'Kony user', 'Kony user');
INSERT INTO `groupentitlement` (`Group_id`, `Service_id`, `TransactionFee_id`, `TransactionLimit_id`, `createdby`, `modifiedby`) VALUES ('DEFAULT_GROUP', 'SERVICE_ID_2', 'TID1', 'TID1', 'Kony user', 'Kony user');
INSERT INTO `groupentitlement` (`Group_id`, `Service_id`, `TransactionFee_id`, `TransactionLimit_id`, `createdby`, `modifiedby`) VALUES ('DEFAULT_GROUP', 'SERVICE_ID_3', 'TID1', 'TID1', 'Kony user', 'Kony user');
INSERT INTO `groupentitlement` (`Group_id`, `Service_id`, `TransactionFee_id`, `TransactionLimit_id`, `createdby`, `modifiedby`) VALUES ('DEFAULT_GROUP', 'SERVICE_ID_4', 'TID1', 'TID1', 'Kony user', 'Kony user');
INSERT INTO `groupentitlement` (`Group_id`, `Service_id`, `TransactionFee_id`, `TransactionLimit_id`, `createdby`, `modifiedby`) VALUES ('DEFAULT_GROUP', 'SERVICE_ID_5', 'TID1', 'TID1', 'Kony user', 'Kony user');
INSERT INTO `groupentitlement` (`Group_id`, `Service_id`, `TransactionFee_id`, `TransactionLimit_id`, `createdby`, `modifiedby`) VALUES ('DEFAULT_GROUP', 'SERVICE_ID_6', 'TID1', 'TID1', 'Kony user', 'Kony user');
INSERT INTO `groupentitlement` (`Group_id`, `Service_id`, `TransactionFee_id`, `TransactionLimit_id`, `createdby`, `modifiedby`) VALUES ('DEFAULT_GROUP', 'SERVICE_ID_7', 'TID1', 'TID1', 'Kony user', 'Kony user');
INSERT INTO `groupentitlement` (`Group_id`, `Service_id`, `TransactionFee_id`, `TransactionLimit_id`, `createdby`, `modifiedby`) VALUES ('DEFAULT_GROUP', 'SERVICE_ID_8', 'TID1', 'TID1', 'Kony user', 'Kony user');
INSERT INTO `groupentitlement` (`Group_id`, `Service_id`, `TransactionFee_id`, `TransactionLimit_id`, `createdby`, `modifiedby`) VALUES ('DEFAULT_GROUP', 'SERVICE_ID_9', 'TID1', 'TID1', 'Kony user', 'Kony user');

SET SQL_SAFE_UPDATES=0;
update customer set DateOfBirth = (SELECT DATE(STR_TO_DATE(DateOfBirth, '%d-%m-%Y')));

ALTER TABLE `requestmessage` 
ADD COLUMN `RepliedBy_id` VARCHAR(50) NULL AFTER `RepliedBy`;

update requestmessage set RepliedBy_id = (select id from systemuser 
	where concat(systemuser.FirstName,' ', ifnull(systemuser.MiddleName,''), ' ', systemuser.LastName) 
    = requestmessage.RepliedBy_Name limit 1) 
    where RepliedBy = 'ADMIN|CSR';

ALTER TABLE `customer` 
CHANGE COLUMN `DateOfBirth` `DateOfBirth` DATE NULL DEFAULT NULL ;

ALTER TABLE `faqs` 
ADD UNIQUE INDEX `QuestionCode_UNIQUE` (`QuestionCode` ASC);
