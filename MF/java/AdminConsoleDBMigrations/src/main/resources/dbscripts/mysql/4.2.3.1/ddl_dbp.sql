ALTER TABLE `transaction` ADD COLUMN `payPersonName` VARCHAR(45) NULL DEFAULT NULL AFTER `serviceName`;
ALTER TABLE `transaction` ADD COLUMN `payPersonNickName` VARCHAR(45) NULL DEFAULT NULL AFTER `payPersonName`;
ALTER TABLE `transaction` ADD COLUMN `p2pAlternateContact` VARCHAR(45) NULL DEFAULT NULL AFTER `payPersonNickName`;
ALTER TABLE `transaction` ADD COLUMN `billerId` VARCHAR(45) NULL DEFAULT NULL AFTER `p2pAlternateContact`;

DROP TABLE IF EXISTS `mfaservice`;
CREATE TABLE `mfaservice` (
  `serviceKey` varchar(50) NOT NULL,
  `serviceName` varchar(50) DEFAULT NULL,
  `User_id` varchar(50) NOT NULL,
  `Createddts` datetime DEFAULT NULL,
  `retryCount` int(5) DEFAULT '0',
  `payload` varchar(10000) DEFAULT NULL,
  `securityQuestions` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`serviceKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `backendidentifier`;
CREATE TABLE `backendidentifier` (
  `id` VARCHAR(50) NOT NULL,
  `Customer_id` VARCHAR(50) NOT NULL,
  `BackendId` VARCHAR(45) NULL DEFAULT NULL,
  `BackendType` VARCHAR(45) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `alertsubtype` 
ADD COLUMN `Status_id` VARCHAR(50) NULL AFTER `Description`,
ADD INDEX `FK_alertsubtype_status_idx` (`Status_id` ASC);

ALTER TABLE `alertsubtype` 
ADD CONSTRAINT `FK_alertsubtype_status`
  FOREIGN KEY (`Status_id`)
  REFERENCES `status` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

UPDATE `alertsubtype` SET `Status_id` = 'SID_ACTIVE';

DROP VIEW IF EXISTS  alertcustomersaccountchannels_view;
DROP VIEW IF EXISTS  alertcustomerchannels_view;
DROP VIEW IF EXISTS  alertglobalchannels_view;
DROP VIEW IF EXISTS  alert_fetchcategory_view;


CREATE VIEW alerts_fetch_globaldata_view AS
    SELECT 
        dbxalerttype.id AS AlertTypeId,
        dbxalerttype.AlertCategoryId AS AlertCategoryId,
        dbxalerttype.AttributeId AS AttributeId,
        dbxalerttype.AlertConditionId AS AlertConditionId,
        dbxalerttype.Value1 AS Value1,
        dbxalerttype.Value2 AS Value2,
        dbxalerttype.Status_id AS alerttype_status_id,
        dbxalerttype.IsGlobal AS IsGlobal,
        dbxalertcategory.status_id AS alertcategory_status_id,
        alertcategorychannel.ChannelID AS ChannelId,
        alertsubtype.id AS AlertSubTypeId,
        alertsubtype.Status_id AS alertsubtypetype_status_id,
        dbxalertcategory.accountLevel AS accountLevel
    FROM
        dbxalerttype
        LEFT JOIN alertcategorychannel ON dbxalerttype.AlertCategoryId = alertcategorychannel.AlertCategoryId
        LEFT JOIN dbxalertcategory ON dbxalerttype.AlertCategoryId = dbxalertcategory.id
        LEFT JOIN alertsubtype ON dbxalerttype.id = alertsubtype.AlertTypeId;
		
CREATE VIEW alertcustomerchannels_view AS
    SELECT 
        dbxcustomeralertentitlement.AlertTypeId AS AlertTypeId,
        dbxalerttype.Name AS Name,
        customeralertcategorychannel.AlertCategoryId AS AlertCategoryId,
        customeralertcategorychannel.Customer_id AS Customer_id,
        dbxalerttype.AttributeId AS AttributeId,
        dbxalerttype.AlertConditionId AS AlertConditionId,
        dbxcustomeralertentitlement.Value1 AS Value1,
        dbxcustomeralertentitlement.Value2 AS Value2,
        dbxalerttype.IsGlobal AS IsGlobal,
        customeralertcategorychannel.ChannelId AS ChannelId,
        customeralertcategorychannel.AccountId AS AccountId,
        status.Type_id AS Status_id,
        dbxalertcategory.accountLevel AS accountLevel
    FROM
        customeralertcategorychannel
        LEFT JOIN dbxcustomeralertentitlement ON customeralertcategorychannel.Customer_id = dbxcustomeralertentitlement.Customer_id
        LEFT JOIN dbxalerttype ON dbxcustomeralertentitlement.AlertTypeId = dbxalerttype.id
        LEFT JOIN status ON dbxalerttype.Status_id = status.id
        LEFT JOIN dbxalertcategory ON customeralertcategorychannel.AlertCategoryId = dbxalertcategory.id;

CREATE VIEW alertcustomersaccountchannels_view AS
    SELECT 
        dbxcustomeralertentitlement.AlertTypeId AS AlertTypeId,
        dbxalerttype.Name AS Name,
        customeralertcategorychannel.Customer_id AS Customer_id,
        customeralertcategorychannel.AlertCategoryId AS AlertCategoryId,
        dbxalerttype.AttributeId AS AttributeId,
        dbxalerttype.AlertConditionId AS AlertConditionId,
        dbxcustomeralertentitlement.Value1 AS Value1,
        dbxcustomeralertentitlement.Value2 AS Value2,
        dbxalerttype.IsGlobal AS IsGlobal,
        customeralertcategorychannel.ChannelId AS ChannelId,
        customeralertcategorychannel.AccountId AS AccountId,
        status.Type_id AS Status_id,
        customeralertswitch.Status_id AS AccountSubscriptionStatus,
        dbxalertcategory.accountLevel AS accountLevel
    FROM
        dbxalerttype
        LEFT JOIN status ON dbxalerttype.Status_id = status.id
        LEFT JOIN customeralertcategorychannel ON dbxalerttype.AlertCategoryId = customeralertcategorychannel.AlertCategoryId
        LEFT JOIN dbxcustomeralertentitlement ON dbxcustomeralertentitlement.Customer_id = customeralertcategorychannel.Customer_id
            AND dbxcustomeralertentitlement.AlertTypeId = dbxalerttype.id
        LEFT JOIN dbxalertcategory ON dbxalerttype.AlertCategoryId = dbxalertcategory.id
        LEFT JOIN customeralertswitch ON customeralertcategorychannel.Customer_id = customeralertswitch.Customer_id
            AND customeralertcategorychannel.AlertCategoryId = customeralertswitch.AlertCategoryId;

DROP TABLE IF EXISTS `backendidentifier`;
CREATE TABLE `backendidentifier` (
  `id` varchar(50) NOT NULL,
  `Customer_id` varchar(50) NOT NULL,
  `sequenceNumber` varchar(45) DEFAULT NULL,
  `BackendId` varchar(45) DEFAULT NULL,
  `BackendType` varchar(45) DEFAULT NULL,
  `identifier_name` varchar(45) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8; 
 
