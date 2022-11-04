-- >> CAMPAIGN MANAGEMENT <<
-- Table
DROP TABLE IF EXISTS `attributecriteria`;
CREATE TABLE `attributecriteria` (
  `id` varchar(50) NOT NULL,
  `name` varchar(50) NOT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `attributeoption`;
CREATE TABLE `attributeoption` (
  `id` varchar(50) NOT NULL,
  `endpoint_attributeoption_id` varchar(50) NOT NULL,
  `name` varchar(50) NOT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `attribute`;
CREATE TABLE `attribute` (
  `id` varchar(50) NOT NULL,
  `endpoint_attribute_id` varchar(50) NOT NULL,
  `name` varchar(50) NOT NULL,
  `attributetype` enum('SELECT','NUMERIC','DATE','ALPHANUMERIC','ALPHABETICAL') NOT NULL,
  `options` varchar(1000) DEFAULT NULL,
  `range` varchar(100) DEFAULT NULL,
  `criterias` varchar(1000) NOT NULL,
  `helptext` varchar(1000) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `model`;
CREATE TABLE `model` (
  `id` varchar(50) NOT NULL,
  `name` varchar(50) NOT NULL,
  `endpoint_url` varchar(200) NOT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `modelattribute`;
CREATE TABLE `modelattribute` (
  `model_id` varchar(50) NOT NULL,
  `attribute_id` varchar(50) NOT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`model_id`,`attribute_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `groupattribute`;
CREATE TABLE `groupattribute` (
  `group_id` varchar(50) NOT NULL,
  `admin_attributes` text,
  `endpoint_model_urls` text,
  `customer_count` int(11) NOT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `custcompletedcampaign`;
CREATE TABLE `custcompletedcampaign` (
  `customer_id` varchar(50) NOT NULL,
  `campaign_id` varchar(50) NOT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`customer_id`,`campaign_id`),
  KEY `fk_customerid_idx` (`customer_id`),
  KEY `fk_campaignid` (`campaign_id`),
  CONSTRAINT `fk_campaignid` FOREIGN KEY (`campaign_id`) REFERENCES `campaign` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_customerid` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
-- Table

-- Stored procedures
DROP PROCEDURE IF EXISTS `campaign_c360_group_read_proc`;
DROP PROCEDURE IF EXISTS `campaign_c360_specification_createupdate_proc`;

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
GROUP BY campaignplaceholder_groupby_id.channel, campaignplaceholder_groupby_id.screen,
campaignplaceholder_groupby_id.campaign_count;

END$$
DELIMITER ;

-- 1
DROP PROCEDURE IF EXISTS `campaign_dbp_specification_prelogin_get_proc`;
DELIMITER $$

CREATE PROCEDURE `campaign_dbp_specification_prelogin_get_proc`(
	IN _currentTimestamp VARCHAR(16) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _scale VARCHAR(10) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _deviceId VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
)
BEGIN

DECLARE placeholderId VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci;
DECLARE customerId VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci;
SET placeholderId = (
	SELECT id FROM campaignplaceholder 
		WHERE channel = 'MOBILE'
		AND screen = 'PRE_LOGIN'
		AND image_scale = _scale
);
SET customerId = (
	SELECT Customer_id FROM customerdevice 
    WHERE id = _deviceId
	AND Channel_id = 'CH_ID_MOB' 
    ORDER BY LastLoginTime DESC LIMIT 1
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
				(SELECT Group_id FROM customergroup WHERE Customer_id = customerId))
		AND campaign.start_datetime <= STR_TO_DATE(_currentTimestamp, '%m/%d/%Y %H:%i:%s') 
		AND campaign.end_datetime >= STR_TO_DATE(_currentTimestamp, '%m/%d/%Y %H:%i:%s')
        AND campaign.id NOT IN 
			(SELECT campaign_id FROM custcompletedcampaign WHERE Customer_id = customerId)
UNION ALL
SELECT 'DEFAULT_CAMPAIGN' AS campaign_id, campaignplaceholder_id, 
		image_url, destination_url, image_index, 1000 AS priority
		FROM defaultcampaignspecification 
		WHERE campaignplaceholder_id = placeholderId 
ORDER BY priority, image_index;

END$$
DELIMITER ;

-- 2
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
DECLARE customerId VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci;

SET placeholderId = (
	SELECT id FROM campaignplaceholder 
		WHERE channel = _channel
		AND screen = _screen
		AND image_scale = _scale
);
SET customerId = (
	SELECT id FROM customer 
		WHERE UserName = _username
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
				(SELECT Group_id FROM customergroup WHERE Customer_id = customerId))
		AND campaign.start_datetime <= STR_TO_DATE(_currentTimestamp, '%m/%d/%Y %H:%i:%s') 
		AND campaign.end_datetime >= STR_TO_DATE(_currentTimestamp, '%m/%d/%Y %H:%i:%s')
        AND campaign.id NOT IN 
			(SELECT campaign_id FROM custcompletedcampaign WHERE Customer_id = customerId)
UNION ALL
SELECT 'DEFAULT_CAMPAIGN' AS campaign_id, campaignplaceholder_id, 
		image_url, destination_url, image_index, 1000 AS priority
		FROM defaultcampaignspecification 
		WHERE campaignplaceholder_id = placeholderId 
ORDER BY priority, image_index;

END$$
DELIMITER ;

-- 3
DROP PROCEDURE IF EXISTS `campaign_c360_model_get_proc`;
DELIMITER $$

CREATE PROCEDURE `campaign_c360_model_get_proc`(
	IN _attributeId VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
)
BEGIN

SELECT model.id, model.name, model.endpoint_url FROM model 
LEFT JOIN modelattribute ON (model.id = modelattribute.model_id)
WHERE modelattribute.attribute_id = _attributeId;

END$$
DELIMITER ;

-- 4
DROP PROCEDURE IF EXISTS `campaign_c360_group_get_proc`;
DELIMITER $$

CREATE PROCEDURE `campaign_c360_group_get_proc`(
IN _campaignId VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
)
BEGIN

SELECT membergroup.id AS group_id, membergroup.Name AS group_name,membergroup.Description AS group_desc,
	groupattribute.admin_attributes AS attributes, groupattribute.customer_count AS customer_count
	FROM membergroup 
	LEFT JOIN campaigngroup ON (membergroup.id = campaigngroup.group_id)
    LEFT JOIN groupattribute ON (membergroup.id = groupattribute.group_id)
	WHERE campaigngroup.campaign_id = _campaignId;

END$$
DELIMITER ;

-- 5
DROP PROCEDURE IF EXISTS `campaign_c360_specification_manage_proc`;
DELIMITER $$

CREATE PROCEDURE `campaign_c360_specification_manage_proc`(
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

-- 6
DROP PROCEDURE IF EXISTS `campaign_c360_customergroup_delete_proc`;
DELIMITER $$

CREATE PROCEDURE `campaign_c360_customergroup_delete_proc`(
	IN _groupId VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci)
BEGIN

DECLARE cursorFinished INTEGER DEFAULT 0;

DECLARE customerId VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci;
DECLARE customerGroupCursor CURSOR FOR 
	SELECT Customer_id FROM customergroup WHERE Group_id = _groupId ;
    
DECLARE CONTINUE HANDLER FOR NOT FOUND SET cursorFinished = 1;

OPEN customerGroupCursor;
    getPlaceholder : LOOP
        FETCH customerGroupCursor INTO customerId;
        IF 
			cursorFinished = 1 THEN LEAVE getPlaceholder;
        END IF;
        DELETE FROM customergroup 
			WHERE Customer_id = customerId 
            AND Group_id = _groupId;
    END LOOP getPlaceholder;
CLOSE customerGroupCursor;

END$$
DELIMITER ;
-- Stored procedures
-- >> CAMPAIGN MANAGEMENT <<