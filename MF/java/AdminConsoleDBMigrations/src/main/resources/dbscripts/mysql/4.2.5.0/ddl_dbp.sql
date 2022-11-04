CREATE 
     OR REPLACE ALGORITHM = UNDEFINED 
    SQL SECURITY DEFINER
VIEW `Paypersontransactionview` AS
    SELECT 
        `t`.`Id` AS `Id`,
        `t`.`isScheduled` AS `isScheduled`,
        `t`.`Customer_id` AS `Customer_id`,
        `t`.`ExpenseCategory_id` AS `ExpenseCategory_id`,
        `t`.`Payee_id` AS `Payee_id`,
       `t`.`Bill_id` AS `Bill_id`,
        `t`.`Type_id` AS `Type_id`,
        `t`.`Reference_id` AS `Reference_id`,
        `t`.`fromAccountNumber` AS `fromAccountNumber`,
        `t`.`fromAccountBalance` AS `fromAccountBalance`,
        `t`.`toAccountNumber` AS `toAccountNumber`,
        `t`.`toAccountBalance` AS `toAccountBalance`,
        `t`.`amount` AS `amount`,
        `t`.`Status_id` AS `Status_id`,
        `t`.`statusDesc` AS `statusDesc`,
        `t`.`notes` AS `notes`,
        `t`.`checkNumber` AS `checkNumber`,
        `t`.`imageURL1` AS `imageURL1`,
        `t`.`imageURL2` AS `imageURL2`,
        `t`.`hasDepositImage` AS `hasDepositImage`,
        `t`.`description` AS `description`,
        `t`.`scheduledDate` AS `scheduledDate`,
        `t`.`transactionDate` AS `transactionDate`,
        `t`.`createdDate` AS `createdDate`,
        `t`.`transactionComments` AS `transactionComments`,
        `t`.`toExternalAccountNumber` AS `toExternalAccountNumber`,
        `t`.`Person_Id` AS `Person_Id`,
        `t`.`frequencyType` AS `frequencyType`,
        `t`.`numberOfRecurrences` AS `numberOfRecurrences`,
        `t`.`frequencyStartDate` AS `frequencyStartDate`,
        `t`.`frequencyEndDate` AS `frequencyEndDate`,
        `t`.`checkImage` AS `checkImage`,
        `t`.`checkImageBack` AS `checkImageBack`,
        `t`.`cashlessOTPValidDate` AS `cashlessOTPValidDate`,
        `t`.`cashlessOTP` AS `cashlessOTP`,
        `t`.`cashlessPhone` AS `cashlessPhone`,
        `t`.`cashlessEmail` AS `cashlessEmail`,
        `t`.`cashlessPersonName` AS `cashlessPersonName`,
        `t`.`cashlessMode` AS `cashlessMode`,
        `t`.`cashlessSecurityCode` AS `cashlessSecurityCode`,
        `t`.`cashWithdrawalTransactionStatus` AS `cashWithdrawalTransactionStatus`,
        `t`.`cashlessPin` AS `cashlessPin`,
        `t`.`category` AS `category`,
        `t`.`billCategory` AS `billCategory`,
        `t`.`recurrenceDesc` AS `recurrenceDesc`,
        `t`.`deliverBy` AS `deliverBy`,
        `t`.`p2pContact` AS `p2pContact`,
        `t`.`p2pRequiredDate` AS `p2pRequiredDate`,
        `t`.`requestCreatedDate` AS `requestCreatedDate`,
        `t`.`penaltyFlag` AS `penaltyFlag`,
        `t`.`payoffFlag` AS `payoffFlag`,
        `t`.`viewReportLink` AS `viewReportLink`,
        `t`.`isPaypersonDeleted` AS `isPaypersonDeleted`,
        `t`.`fee` AS `fee`,
        `p`.`id` AS `paypersonID`,
        `p`.`firstName` AS `firstName`,
        `p`.`lastName` AS `lastName`,
        `p`.`phone` AS `phone`,
        `p`.`email` AS `email`,
        `p`.`User_id` AS `User_id`,
        `p`.`secondaryEmail` AS `secondaryEmail`,
        `p`.`secondoryPhoneNumber` AS `secondoryPhoneNumber`,
        `p`.`primaryContactForSending` AS `primaryContactForSending`,
        `p`.`nickName` AS `nickName`,
        `p`.`isSoftDelete` AS `isSoftDelete`,
        `tt`.`description` AS `transactionType`
    FROM
        ((`transaction` `t`
        LEFT JOIN `payperson` `p` ON ((`t`.`Person_Id` = `p`.`id`)))
        JOIN `transactiontype` `tt` ON ((`t`.`Type_id` = `tt`.`Id`)));

DROP TABLE IF EXISTS `service_permission_mapper`;
CREATE TABLE `service_permission_mapper` (
  `id` varchar(50) NOT NULL,
  `service_name` varchar(50) NOT NULL,
  `object_name` varchar(50) DEFAULT NULL,
  `operation` varchar(50) NOT NULL,
  `user_type` varchar(50) NOT NULL,
  `permissions` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNIQUE_service_permission_mapper` (`service_name`,`object_name`,`operation`,`user_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `eventactivitytype` (
  `id` VARCHAR(50) NOT NULL,
  `Description` VARCHAR(255) NULL,
  `createdby` VARCHAR(255) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(255) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NULL DEFAULT '0',
  PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `eventtype` 
ADD COLUMN `ActivityType` VARCHAR(50) NULL AFTER `Name`,
ADD INDEX `eventtype_activitytype_idx` (`ActivityType` ASC);

ALTER TABLE `eventtype` 
ADD CONSTRAINT `eventtype_activitytype`
  FOREIGN KEY (`ActivityType`)
  REFERENCES `eventactivitytype` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

  
ALTER TABLE `event` 
CHANGE COLUMN `Producer` `Producer` VARCHAR(255) NOT NULL ;

ALTER TABLE `customer` 
ADD INDEX `FK_UserName_get` (`UserName` ASC);

ALTER TABLE `pfmbargraph` 
ADD INDEX `year` (`year` ASC);

ALTER TABLE `dmadvertisements` 
ADD INDEX `flowPosition` (`flowPosition` ASC);

ALTER TABLE `customer` 
ADD COLUMN `DefaultLanguage` VARCHAR(45) NULL DEFAULT NULL AFTER `isEngageProvisioned`; 

ALTER TABLE `feedbackstatus` 
ADD COLUMN `customerID` VARCHAR(50) NULL DEFAULT NULL AFTER `deviceID`;

DROP TABLE IF EXISTS `archivedalerthistory`;
CREATE TABLE  `archivedalerthistory` (
  `Id` varchar(100) NOT NULL,
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
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `appmappingaid`;
CREATE TABLE IF NOT EXISTS  `appmappingaid` (
  `id` int(11) NOT NULL,
  `Appid` varchar(45) DEFAULT NULL,
  `Channel` varchar(45) DEFAULT NULL,
  `aid` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `application`
CHANGE COLUMN `isFeedbackEnabled` `isFeedbackEnabled` BIT(1) NULL DEFAULT b'1' ;

ALTER TABLE `application` 
ADD COLUMN `showAdsPostLogin` TINYINT(1) NOT NULL DEFAULT 1 AFTER `viewMoreDBXLink`;
