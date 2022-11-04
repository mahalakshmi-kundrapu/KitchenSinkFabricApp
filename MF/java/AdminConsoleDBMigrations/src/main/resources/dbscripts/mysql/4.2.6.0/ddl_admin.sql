-- >> CAMPAIGN MANAGEMENT <<
ALTER TABLE `campaignspecification` 
ADD COLUMN `display_count` INT(8) NULL DEFAULT 0 AFTER `destination_url`;

ALTER TABLE `defaultcampaignspecification` 
ADD COLUMN `display_count` INT(8) NULL DEFAULT 0 AFTER `destination_url`;

-- DBP procs
DROP PROCEDURE IF EXISTS `campaign_priority_update_proc`;
DROP PROCEDURE IF EXISTS `defaultcampaignspecification_update_proc`;
DROP PROCEDURE IF EXISTS `campaigngroup_membergroup_read_proc`;
DROP PROCEDURE IF EXISTS `campaign_specification_createupdate_proc`;

-- 1
DROP PROCEDURE IF EXISTS `campaign_dbp_count_proc`;
DELIMITER $$

CREATE PROCEDURE `campaign_dbp_count_proc`()
BEGIN

SELECT 
campaignplaceholder_groupby_id.channel, campaignplaceholder_groupby_id.screen, 
campaignplaceholder_groupby_id.campaign_count
FROM (
	SELECT 
		campaignplaceholder.channel, campaignplaceholder.screen, 
		count(*) AS campaign_count
	FROM defaultcampaignspecification LEFT JOIN campaignplaceholder 
	ON (defaultcampaignspecification.campaignplaceholder_id = campaignplaceholder.id)
	GROUP BY campaignplaceholder.id
	) 
AS campaignplaceholder_groupby_id 
GROUP BY campaignplaceholder_groupby_id.channel, campaignplaceholder_groupby_id.screen;

END$$
DELIMITER ;

-- 2
DROP PROCEDURE IF EXISTS `campaign_dbp_specification_prelogin_get_proc`;
DELIMITER $$

CREATE PROCEDURE `campaign_dbp_specification_prelogin_get_proc`(
	IN _currentTimestamp VARCHAR(16) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _scale VARCHAR(10) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _deviceId VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
)
BEGIN

DECLARE placeholderId VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci;
SET placeholderId = (
	SELECT id FROM campaignplaceholder 
		WHERE channel = 'MOBILE'
		AND screen = 'PRE_LOGIN'
		AND image_scale = _scale
);

SELECT campaignspecification.campaign_id, campaignspecification.campaignplaceholder_id,
		campaignspecification.image_url, campaignspecification.destination_url,
		1 AS image_index, campaign.priority
		FROM campaignspecification 
		LEFT JOIN campaign ON (campaignspecification.campaign_id = campaign.id) 
		WHERE campaignspecification.campaignplaceholder_id = placeholderId 
        AND campaign.status_id = 'SID_SCHEDULED_ACTIVE_COMPLETED'
		AND campaign.id IN (
			SELECT campaign_id FROM campaigngroup WHERE group_id IN 
				(SELECT Group_id FROM customergroup WHERE Customer_id = 
					(SELECT Customer_id FROM customerdevice WHERE id = _deviceId
						and Channel_id = 'CH_ID_MOB' order by LastLoginTime desc LIMIT 1)))
		AND campaign.start_datetime <= STR_TO_DATE(_currentTimestamp, '%m/%d/%Y %H:%i:%s') 
		AND campaign.end_datetime >= STR_TO_DATE(_currentTimestamp, '%m/%d/%Y %H:%i:%s')
UNION ALL
SELECT 'DEFAULT_CAMPAIGN' AS campaign_id, campaignplaceholder_id, 
		image_url, destination_url, image_index, 1000 AS priority
		FROM defaultcampaignspecification 
		WHERE campaignplaceholder_id = placeholderId 
ORDER BY priority, image_index;

END$$
DELIMITER ;

-- 3
DROP PROCEDURE IF EXISTS `campaign_dbp_specification_get_proc`;
DELIMITER $$

CREATE PROCEDURE `campaign_dbp_specification_get_proc`(
	IN _currentTimestamp VARCHAR(16) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _scale VARCHAR(10) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _username VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _channel VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _screen VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
)
BEGIN

DECLARE placeholderId VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci;
SET placeholderId = (
	SELECT id FROM campaignplaceholder 
		WHERE channel = _channel
		AND screen = _screen
		AND image_scale = _scale
);

SELECT campaignspecification.campaign_id, campaignspecification.campaignplaceholder_id,
		campaignspecification.image_url, campaignspecification.destination_url,
		1 AS image_index, campaign.priority
		FROM campaignspecification 
		LEFT JOIN campaign ON (campaignspecification.campaign_id = campaign.id) 
		WHERE campaignspecification.campaignplaceholder_id = placeholderId 
        AND campaign.status_id = 'SID_SCHEDULED_ACTIVE_COMPLETED'
		AND campaignspecification.campaign_id IN 
			(SELECT campaign_id FROM campaigngroup WHERE group_id IN 
				(SELECT Group_id FROM customergroup WHERE Customer_id = 
					(SELECT id FROM customer WHERE UserName = _username)))
		AND campaign.start_datetime <= STR_TO_DATE(_currentTimestamp, '%m/%d/%Y %H:%i:%s') 
		AND campaign.end_datetime >= STR_TO_DATE(_currentTimestamp, '%m/%d/%Y %H:%i:%s')
UNION ALL
SELECT 'DEFAULT_CAMPAIGN' AS campaign_id, campaignplaceholder_id, 
		image_url, destination_url, image_index, 1000 AS priority
		FROM defaultcampaignspecification 
		WHERE campaignplaceholder_id = placeholderId 
ORDER BY priority, image_index;

END$$
DELIMITER ;

-- 4
DROP PROCEDURE IF EXISTS `campaign_dbp_display_count_update_proc`;
DELIMITER $$

CREATE PROCEDURE `campaign_dbp_display_count_update_proc`(
    IN _campaignId VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _campaignPlaceholderId VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _imageIndex INT(2)
)
BEGIN

DECLARE displayCount INT(8);

IF _campaignId = 'DEFAULT_CAMPAIGN' THEN
	SET displayCount = (
		SELECT (display_count + 1) FROM defaultcampaignspecification 
		WHERE campaignplaceholder_id = _campaignPlaceholderId
        AND image_index = _imageIndex
	);

	UPDATE defaultcampaignspecification SET display_count = displayCount
	WHERE campaignplaceholder_id = _campaignPlaceholderId
    AND image_index = _imageIndex;
END IF;

IF _campaignId != 'DEFAULT_CAMPAIGN' THEN
	SET displayCount = (
		SELECT (display_count + 1) FROM campaignspecification 
		WHERE campaign_id = _campaignId 
		AND campaignplaceholder_id = _campaignPlaceholderId
	);

	UPDATE campaignspecification SET display_count = displayCount
	WHERE campaign_id = _campaignId 
    AND campaignplaceholder_id = _campaignPlaceholderId;
END IF;

END$$
DELIMITER ;
-- DBP procs

-- C360 procs
-- 1
DROP PROCEDURE IF EXISTS `campaign_c360_count_proc`;
DELIMITER $$

CREATE PROCEDURE `campaign_c360_count_proc`()
BEGIN

SELECT
	campaignplaceholder.channel, campaignplaceholder.screen, campaignplaceholder.image_resolution, 
	count(*) AS campaign_count
FROM defaultcampaignspecification LEFT JOIN campaignplaceholder 
ON (defaultcampaignspecification.campaignplaceholder_id = campaignplaceholder.id)
GROUP BY campaignplaceholder.id;

END$$
DELIMITER ;

-- 2
DROP PROCEDURE IF EXISTS `campaign_c360_specification_get_proc`;
DELIMITER $$

CREATE PROCEDURE `campaign_c360_specification_get_proc`(
in _campaignId VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
)
BEGIN

SELECT campaignspecification.campaign_id, 
campaignplaceholder.channel, campaignplaceholder.screen, 
campaignplaceholder.image_resolution, campaignplaceholder.image_scale,
campaignspecification.image_url, campaignspecification.destination_url
FROM campaignspecification 
LEFT JOIN campaignplaceholder ON (campaignspecification.campaignplaceholder_id = campaignplaceholder.id)
WHERE campaignspecification.campaign_id = _campaignId;

END$$
DELIMITER ;

-- 3
DROP PROCEDURE IF EXISTS `campaign_c360_specification_createupdate_proc`;
DELIMITER $$

CREATE PROCEDURE `campaign_c360_specification_createupdate_proc`(
	IN _campaignId VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
	IN _channel VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _screen VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _imageResolution VARCHAR(10) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _imageURL VARCHAR(200) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _destinationURL VARCHAR(200) CHARACTER SET UTF8 COLLATE utf8_general_ci,
	IN _userId VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
)
BEGIN
	
DECLARE placeholderId VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci;
DECLARE existingImageURL VARCHAR(200) CHARACTER SET UTF8 COLLATE utf8_general_ci;

SET placeholderId = (
	SELECT id FROM campaignplaceholder 
    WHERE channel = _channel
	AND screen = _screen
    AND image_resolution = _imageResolution
);

SET existingImageURL = (
	SELECT image_url FROM campaignspecification 
    WHERE campaign_id = _campaignId
    AND campaignplaceholder_id = placeholderId
);

IF existingImageURL IS NULL THEN
	SET @mainQuery = CONCAT(
		"insert into campaignspecification ",
        "(`campaign_id`, `campaignplaceholder_id`, `image_url`, ");
        
	IF _destinationURL != '' THEN 
		SET @mainQuery = CONCAT(@mainQuery, "`destination_url`, ");
	END IF;
    
    SET @mainQuery = CONCAT(@mainQuery, 
        "`createdby`) values (",
		quote(_campaignId), ", ",
		quote(placeholderId), ", ",
		quote(_imageURL), ", ");
        
	IF _destinationURL != '' THEN 
		SET @mainQuery = CONCAT(@mainQuery, quote(_destinationURL), ", ");
	END IF;
        
	SET @mainQuery = CONCAT(@mainQuery, quote(_userId), ")");
	
	PREPARE queryStatement FROM @mainQuery;
	EXECUTE queryStatement;
	DEALLOCATE PREPARE queryStatement;
END IF;

IF existingImageURL IS NOT NULL THEN
	UPDATE campaignspecification 
    SET image_url = _imageURL, modifiedby = _userId, destination_url = _destinationURL
	WHERE campaign_id = _campaignId
	AND campaignplaceholder_id = placeholderId;
END IF;

END$$
DELIMITER ;

-- 4
DROP PROCEDURE IF EXISTS `campaign_c360_specification_delete_proc`;
DELIMITER $$

CREATE PROCEDURE `campaign_c360_specification_delete_proc`(
	IN _campaignId VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
	IN _channel VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _screen VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
)
BEGIN

DECLARE cursorFinished INTEGER DEFAULT 0;

DECLARE campaignPlaceholderId VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci;
DECLARE campaignPlaceholderCursor CURSOR FOR 
	SELECT id FROM campaignplaceholder WHERE channel = _channel AND screen = _screen;
    
DECLARE CONTINUE HANDLER FOR NOT FOUND SET cursorFinished = 1;

OPEN campaignPlaceholderCursor;
    getPlaceholder : LOOP
        FETCH campaignPlaceholderCursor INTO campaignPlaceholderId;
        IF 
			cursorFinished = 1 THEN LEAVE getPlaceholder;
        END IF;
        DELETE FROM campaignspecification 
			WHERE campaign_id = _campaignId 
            AND campaignplaceholder_id = campaignPlaceholderId;
    END LOOP getPlaceholder;
CLOSE campaignPlaceholderCursor;

END$$
DELIMITER ;

-- 5
DROP PROCEDURE IF EXISTS `campaign_c360_default_specification_get_proc`;
DELIMITER $$

CREATE PROCEDURE `campaign_c360_default_specification_get_proc`()
BEGIN

SELECT  
campaignplaceholder.id,
campaignplaceholder.channel, campaignplaceholder.screen, 
campaignplaceholder.image_resolution, campaignplaceholder.image_scale, 
defaultcampaignspecification.image_index,
defaultcampaignspecification.image_url, defaultcampaignspecification.destination_url
FROM defaultcampaignspecification 
LEFT JOIN campaignplaceholder ON (defaultcampaignspecification.campaignplaceholder_id = campaignplaceholder.id);

END$$
DELIMITER ;

-- 6
DROP PROCEDURE IF EXISTS `campaign_c360_default_specification_manage_proc`;
DELIMITER $$

CREATE PROCEDURE `campaign_c360_default_specification_manage_proc`(
	IN _channel VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _screen VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _imageIndex INT(2),
    IN _imageResolution VARCHAR(10) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _imageURL VARCHAR(200) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _destinationURL VARCHAR(200) CHARACTER SET UTF8 COLLATE utf8_general_ci,
	IN _createdOrModifiedBy VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
)
BEGIN

DECLARE placeholderId VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci;
DECLARE currentImageURL VARCHAR(200) CHARACTER SET UTF8 COLLATE utf8_general_ci;

SET placeholderId = (
	SELECT id FROM campaignplaceholder 
    WHERE channel = _channel
	AND screen = _screen
    AND image_resolution = _imageResolution
);

SET currentImageURL = (
	SELECT image_url FROM defaultcampaignspecification 
    WHERE campaignplaceholder_id = placeholderId
    AND image_index = _imageIndex
);

IF currentImageURL IS NULL THEN
	INSERT INTO defaultcampaignspecification 
    (campaignplaceholder_id, image_index, image_url, destination_url, createdby) 
    VALUES (placeholderId, _imageIndex, _imageURL, _destinationURL, _createdOrModifiedBy);
END IF;

IF currentImageURL IS NOT NULL THEN
	UPDATE defaultcampaignspecification 
    SET image_url = image_url, destination_url = _destinationURL, modifiedby = _createdOrModifiedBy
    WHERE campaignplaceholder_id = placeholderId 
    AND image_index = _imageIndex;
END IF;

END$$
DELIMITER ;

-- 7
DROP PROCEDURE IF EXISTS `campaign_c360_priority_update_proc`;
DELIMITER $$

CREATE PROCEDURE `campaign_c360_priority_update_proc`(
in _priorityStart INT(100),
in _priorityEnd INT(100)
)
BEGIN

SET sql_safe_updates = 0;
  
UPDATE campaign 
	SET priority = priority + 1 
    WHERE priority >= _priorityStart AND priority < _priorityEnd;

SET sql_safe_updates = 1;

END$$
DELIMITER ;

-- 8
DROP PROCEDURE IF EXISTS `campaign_c360_group_read_proc`;
DELIMITER $$

CREATE PROCEDURE `campaign_c360_group_read_proc`(
IN _campaignId VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
)
BEGIN

SELECT id AS group_id, Name AS group_name FROM membergroup 
	LEFT JOIN campaigngroup ON (membergroup.id = campaigngroup.group_id)
	WHERE campaigngroup.campaign_id = _campaignId;

END$$
DELIMITER ;
-- >> CAMPAIGN MANAGEMENT <<

ALTER TABLE `termandconditiontext` 
ADD COLUMN `Version_Id` varchar(50) NOT NULL DEFAULT '1.0' AFTER `LanguageCode`,
ADD COLUMN `Status_id` varchar(50) NOT NULL DEFAULT 'SID_ACTIVE' AFTER `ContentType_id`, 
ADD COLUMN `ContentModifiedBy` varchar(50) NOT NULL DEFAULT 'Kony User' AFTER `ContentType_id`,
ADD COLUMN `ContentModifiedOn` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `ContentModifiedBy`,
ADD CONSTRAINT `FK_termandconditiontext_status`
  FOREIGN KEY (`Status_id`)
  REFERENCES `status` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION, 
ADD INDEX `FK_Termandconditiontext_Status_idx` (`Status_id` ASC);
  
ALTER TABLE `termandcondition` DROP COLUMN `ContentModifiedBy`;
ALTER TABLE `termandcondition` DROP COLUMN `ContentModifiedOn`;

ALTER TABLE termandconditiontext 
   DROP INDEX Termandconditiontext_unique_index, 
   ADD UNIQUE KEY `Termandconditiontext_unique_index` (`TermAndConditionId`,`LanguageCode`,`Version_Id`);
   
   
-- Enhancements for outage messages
ALTER TABLE `outagemessage` 
DROP FOREIGN KEY `FK_OutageMessage_Status`;

ALTER TABLE `outagemessage` 
ADD COLUMN `name` VARCHAR(100) NULL AFTER `id`,
ADD COLUMN `startTime` TIMESTAMP NULL AFTER `MessageText`,
ADD COLUMN `endTime` TIMESTAMP NULL AFTER `startTime`,
CHANGE COLUMN `Status_id` `Status_id` VARCHAR(50) NOT NULL ;
ALTER TABLE `outagemessage` 
ADD CONSTRAINT `FK_OutageMessage_Status`
  FOREIGN KEY (`Status_id`)
  REFERENCES `status` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
  
CREATE TABLE `outagemessageapp` (
  `Outagemessage_id` VARCHAR(50) NOT NULL,
  `App_id` VARCHAR(50) NOT NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`Outagemessage_id`, `App_id`),
  INDEX `FK_outagemessageapp_app_idx` (`App_id` ASC),
  CONSTRAINT `FK_outagemessageapp_app`
    FOREIGN KEY (`App_id`)
    REFERENCES `app` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_outagemessageapp_outagemessage`
    FOREIGN KEY (`Outagemessage_id`)
    REFERENCES `outagemessage` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP VIEW IF EXISTS `outagemessage_view`;
CREATE VIEW `outagemessage_view` AS select `outagemessage`.`id` AS `id`,`outagemessage`.`name` AS `name`,`outagemessage`.`startTime` AS `startTime`,`outagemessage`.`endTime` AS `endTime`,(select group_concat(`outagemessageapp`.`App_id`,'' separator ',') from `outagemessageapp` where (`outagemessageapp`.`Outagemessage_id` = `outagemessage`.`id`)) AS `app_id`,(select group_concat(`app`.`Name`,'' separator ',') from `app` where `app`.`id` in (select `outagemessageapp`.`App_id` from `outagemessageapp` where (`outagemessageapp`.`Outagemessage_id` = `outagemessage`.`id`))) AS `app_name`,`outagemessage`.`Status_id` AS `Status_id`,`outagemessage`.`MessageText` AS `MessageText`,`outagemessage`.`createdby` AS `createdby`,`outagemessage`.`modifiedby` AS `modifiedby`,`outagemessage`.`createdts` AS `createdts`,`outagemessage`.`lastmodifiedts` AS `lastmodifiedts` from `outagemessage`;

DROP PROCEDURE IF EXISTS `customer_basic_info_proc`;
DELIMITER $$
CREATE PROCEDURE `customer_basic_info_proc`(
in _customerId varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
)
BEGIN

SELECT accountLockoutThreshold,accountLockoutTime into @accountLockoutThreshold,@accountLockoutTime from passwordlockoutsettings ;

SELECT 
        `customer`.`UserName` AS `Username`,
        `customer`.`FirstName` AS `FirstName`,
        `customer`.`MiddleName` AS `MiddleName`,
        `customer`.`LastName` AS `LastName`,
        CONCAT(IFNULL(`customer`.`FirstName`, ''),
                ' ',
                IFNULL(`customer`.`MiddleName`, ''),
                ' ',
                IFNULL(`customer`.`LastName`, '')) AS `Name`,
        `customer`.`Salutation` AS `Salutation`,
        `customer`.`id` AS `Customer_id`,
        CONCAT('****', RIGHT(`customer`.`Ssn`, 4)) AS `SSN`,
        `customer`.`createdts` AS `CustomerSince`,
        `customer`.`Gender` AS `Gender`,
        `customer`.`DateOfBirth` AS `DateOfBirth`,
        IF(`customer`.`Status_id`= 'SID_CUS_SUSPENDED',`customer`.`Status_id`, 
        IF(`customer`.`lockCount`+1 >= @accountLockoutThreshold, 'SID_CUS_LOCKED',`customer`.`Status_id`)) AS `CustomerStatus_id`,
        `customerstatus`.`Description` AS `CustomerStatus_name`,
        `customer`.`MaritalStatus_id` AS `MaritalStatus_id`,
        `maritalstatus`.`Description` AS `MaritalStatus_name`,
        `customer`.`SpouseName` AS `SpouseName`,
        `customer`.`DrivingLicenseNumber` AS `DrivingLicenseNumber`,
        `customer`.`lockedOn` AS `lockedOn`,
        `customer`.`lockCount` AS `lockCount`,
        `customer`.`EmployementStatus_id` AS `EmployementStatus_id`,
        `employementstatus`.`Description` AS `EmployementStatus_name`,
        (SELECT 
                GROUP_CONCAT(`customerflagstatus`.`Status_id`, ' '
                        SEPARATOR ',')
            FROM
                `customerflagstatus`
            WHERE
                (`customerflagstatus`.`Customer_id` = `customer`.`id`)) AS `CustomerFlag_ids`,
        (SELECT 
                GROUP_CONCAT(`status`.`Description`, ' '
                        SEPARATOR ',')
            FROM
                `status`
            WHERE
                `status`.`id` IN (SELECT 
                        `customerflagstatus`.`Status_id`
                    FROM
                        `customerflagstatus`
                    WHERE
                        (`customerflagstatus`.`Customer_id` = `customer`.`id`))) AS `CustomerFlag`,
        `customer`.`IsEnrolledForOlb` AS `IsEnrolledForOlb`,
        `customer`.`IsStaffMember` AS `IsStaffMember`,
        `customer`.`Location_id` AS `Branch_id`,
        `location`.`Name` AS `Branch_name`,
        `location`.`Code` AS `Branch_code`,
        `customer`.`IsOlbAllowed` AS `IsOlbAllowed`,
        `customer`.`IsAssistConsented` AS `IsAssistConsented`,
        `customer`.`isEagreementSigned` AS `isEagreementSigned`,
        `customer`.`CustomerType_id` AS `CustomerType_id`,
        `customertype`.`Name` AS `CustomerType_Name`,
        `customertype`.`Description` AS `CustomerType_Description`,
		`membergroup`.`Name` AS `Customer_Role`,
		`membergroup`.`isEAgreementActive` AS `isEAgreementRequired`,
        `customer`.`Organization_Id` AS `organisation_id`,
        `organisation`.`Name` AS `organisation_name`,
        `primaryphone`.`Value` AS `PrimaryPhoneNumber`,
        `primaryemail`.`Value` AS `PrimaryEmailAddress`,
        `customer`.`DocumentsSubmitted` AS `DocumentsSubmitted`,
        `customer`.`ApplicantChannel` AS `ApplicantChannel`,
        `customer`.`Product` AS `Product`,
        `customer`.`Reason` AS `Reason`,
        @accountLockoutTime AS accountLockoutTime
    FROM
        ((((((((`customer`
        LEFT JOIN `location` ON ((`customer`.`Location_id` = `location`.`id`)))
        LEFT JOIN `organisation` ON ((`customer`.`Organization_Id` = `organisation`.`id`)))
        JOIN `customertype` ON ((`customer`.`CustomerType_id` = `customertype`.`id`)))
        LEFT JOIN `status` `customerstatus` ON ((`customer`.`Status_id` = `customerstatus`.`id`)))
        LEFT JOIN `status` `maritalstatus` ON ((`customer`.`MaritalStatus_id` = `maritalstatus`.`id`)))
        LEFT JOIN `status` `employementstatus` ON ((`customer`.`EmployementStatus_id` = `employementstatus`.`id`)))
        LEFT JOIN `customercommunication` `primaryphone` ON (((`primaryphone`.`Customer_id` = `customer`.`id`)
            AND (`primaryphone`.`isPrimary` = 1)
            AND (`primaryphone`.`Type_id` = 'COMM_TYPE_PHONE'))))
        LEFT JOIN `customercommunication` `primaryemail` ON (((`primaryemail`.`Customer_id` = `customer`.`id`)
            AND (`primaryemail`.`isPrimary` = 1)
            AND (`primaryemail`.`Type_id` = 'COMM_TYPE_EMAIL')))
		LEFT JOIN `customergroup` ON ((`customer`.`id` = `customergroup`.`Customer_id`))
        JOIN  `membergroup` ON ((`membergroup`.`id` = `customergroup`.`Group_id`)))
            
    WHERE `customer`.`id` = _customerId
    LIMIT 1;

END$$
DELIMITER ;

UPDATE `configurations` SET `config_type`='PREFERENCE' WHERE `configuration_id`='21';
ALTER TABLE `configurations` CHANGE COLUMN `config_type` `config_type` ENUM('PREFERENCE', 'IMAGE/ICON', 'SKIN') NOT NULL ;
ALTER TABLE `configurations` CHANGE COLUMN `target` `target` ENUM('CLIENT', 'SERVER') NOT NULL ;

ALTER TABLE `termandconditiontext` 
ADD COLUMN `Description` varchar(255) DEFAULT NULL AFTER `Version_Id`;

DROP VIEW IF EXISTS `internaluserdetails_view`;
CREATE VIEW `internaluserdetails_view` AS select `systemuser`.`id` AS `id`,`systemuser`.`Username` AS `Username`,`systemuser`.`Email` AS `Email`,`systemuser`.`Status_id` AS `Status_id`,`systemuser`.`Password` AS `Password`,`systemuser`.`Code` AS `Code`,`systemuser`.`FirstName` AS `FirstName`,`systemuser`.`MiddleName` AS `MiddleName`,`systemuser`.`LastName` AS `LastName`,`systemuser`.`FailedCount` AS `FailedCount`,`systemuser`.`LastPasswordChangedts` AS `LastPasswordChangedts`,`systemuser`.`ResetpasswordLink` AS `ResetpasswordLink`,`systemuser`.`ResetPasswordExpdts` AS `ResetPasswordExpdts`,`systemuser`.`lastLogints` AS `lastLogints`,`systemuser`.`createdby` AS `createdby`,`systemuser`.`createdts` AS `createdts`,`systemuser`.`modifiedby` AS `modifiedby`,`systemuser`.`lastmodifiedts` AS `lastmodifiedts`,`systemuser`.`synctimestamp` AS `synctimestamp`,`systemuser`.`softdeleteflag` AS `softdeleteflag`,`userrole`.`Role_id` AS `Role_id`,`userrole`.`hasSuperAdminPrivilages` AS `hasSuperAdminPrivilages`,`role`.`Name` AS `Role_Name`,`role`.`Status_id` AS `Role_Status_id` from ((`systemuser` left join `userrole` on((`userrole`.`User_id` = `systemuser`.`id`))) left join `role` on((`userrole`.`Role_id` = `role`.`id`)));

DROP VIEW IF EXISTS `customer_request_detailed_view`;
CREATE VIEW `customer_request_detailed_view` AS select `customerrequest`.`id` AS `customerrequest_id`,`customerrequest`.`RequestCategory_id` AS `customerrequest_RequestCategory_id`,`customerrequest`.`lastupdatedbycustomer` AS `customerrequest_lastupdatedbycustomer`,`requestcategory`.`Name` AS `requestcategory_Name`,`customerrequest`.`Customer_id` AS `customerrequest_Customer_id`,`customer`.`FirstName` AS `customer_FirstName`,`customer`.`MiddleName` AS `customer_MiddleName`,concat(`customer`.`FirstName`,' ',`customer`.`LastName`) AS `customer_Fullname`,concat(`systemuser`.`FirstName`,' ',`systemuser`.`LastName`) AS `customerrequest_AssignedTo_Name`,`customer`.`LastName` AS `customer_LastName`,`customer`.`UserName` AS `customer_Username`,`customer`.`Salutation` AS `customer_Salutation`,`customer`.`Gender` AS `customer_Gender`,`customer`.`DateOfBirth` AS `customer_DateOfBirth`,`customer`.`Status_id` AS `customer_Status_id`,NULL AS `customer_Ssn`,`customer`.`MaritalStatus_id` AS `customer_MaritalStatus_id`,`customer`.`SpouseName` AS `customer_SpouseName`,`customer`.`EmployementStatus_id` AS `customer_EmployementStatus_id`,`customer`.`IsEnrolledForOlb` AS `customer_IsEnrolledForOlb`,`customer`.`IsStaffMember` AS `customer_IsStaffMember`,`customer`.`Location_id` AS `customer_Location_id`,`customer`.`PreferredContactMethod` AS `customer_PreferredContactMethod`,`customer`.`PreferredContactTime` AS `customer_PreferredContactTime`,`customerrequest`.`Priority` AS `customerrequest_Priority`,`customerrequest`.`Status_id` AS `customerrequest_Status_id`,`customerrequest`.`AssignedTo` AS `customerrequest_AssignedTo`,`customerrequest`.`RequestSubject` AS `customerrequest_RequestSubject`,`customerrequest`.`Accountid` AS `customerrequest_Accountid`,`customerrequest`.`createdby` AS `customerrequest_createdby`,`customerrequest`.`modifiedby` AS `customerrequest_modifiedby`,`customerrequest`.`createdts` AS `customerrequest_createdts`,`customerrequest`.`lastmodifiedts` AS `customerrequest_lastmodifiedts`,`customerrequest`.`synctimestamp` AS `customerrequest_synctimestamp`,`customerrequest`.`softdeleteflag` AS `customerrequest_softdeleteflag`,`requestmessage`.`id` AS `requestmessage_id`,`requestmessage`.`RepliedBy` AS `requestmessage_RepliedBy`,`requestmessage`.`RepliedBy_Name` AS `requestmessage_RepliedBy_Name`,`requestmessage`.`MessageDescription` AS `requestmessage_MessageDescription`,`requestmessage`.`ReplySequence` AS `requestmessage_ReplySequence`,`requestmessage`.`IsRead` AS `requestmessage_IsRead`,`requestmessage`.`createdby` AS `requestmessage_createdby`,`requestmessage`.`modifiedby` AS `requestmessage_modifiedby`,`requestmessage`.`createdts` AS `requestmessage_createdts`,`requestmessage`.`lastmodifiedts` AS `requestmessage_lastmodifiedts`,`requestmessage`.`synctimestamp` AS `requestmessage_synctimestamp`,`requestmessage`.`softdeleteflag` AS `requestmessage_softdeleteflag`,`messageattachment`.`id` AS `messageattachment_id`,`messageattachment`.`AttachmentType_id` AS `messageattachment_AttachmentType_id`,`messageattachment`.`Media_id` AS `messageattachment_Media_id`,`messageattachment`.`createdby` AS `messageattachment_createdby`,`messageattachment`.`modifiedby` AS `messageattachment_modifiedby`,`messageattachment`.`createdts` AS `messageattachment_createdts`,`messageattachment`.`lastmodifiedts` AS `messageattachment_lastmodifiedts`,`messageattachment`.`softdeleteflag` AS `messageattachment_softdeleteflag`,`media`.`id` AS `media_id`,`media`.`Name` AS `media_Name`,`media`.`Size` AS `media_Size`,`media`.`Type` AS `media_Type`,`media`.`Description` AS `media_Description`,`media`.`Url` AS `media_Url`,`media`.`createdby` AS `media_createdby`,`media`.`modifiedby` AS `media_modifiedby`,`media`.`lastmodifiedts` AS `media_lastmodifiedts`,`media`.`synctimestamp` AS `media_synctimestamp`,`media`.`softdeleteflag` AS `media_softdeleteflag` from ((((((`customerrequest` join `customer` on((`customerrequest`.`Customer_id` = `customer`.`id`))) join `requestcategory` on((`customerrequest`.`RequestCategory_id` = `requestcategory`.`id`))) join `requestmessage` on((`customerrequest`.`id` = `requestmessage`.`CustomerRequest_id`))) left join `systemuser` on((`customerrequest`.`AssignedTo` = `systemuser`.`id`))) left join `messageattachment` on((`requestmessage`.`id` = `messageattachment`.`RequestMessage_id`))) left join `media` on((`messageattachment`.`Media_id` = `media`.`id`)));
