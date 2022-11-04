
DELIMITER $$
CREATE PROCEDURE insertBasicMFAScenarios()
BEGIN
	IF(SELECT count(*) from `appactionmfa` where `AppAction_id`='BUSINESS_BANKING_SERVICE_ID_11') = 0  THEN 
		INSERT IGNORE INTO `appactionmfa` (`id`, `AppAction_id`, `IsMFARequired`, `FrequencyType_id`, `FrequencyValue`, `Status_id`, `Description`, `PrimaryMFAType`, `SecondaryMFAType`, `SMSText`, `EmailSubject`, `EmailBody`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('1032696269','BUSINESS_BANKING_SERVICE_ID_11',0,'VALUE_BASED','10','SID_INACTIVE','MFA','SECURE_ACCESS_CODE','SECURITY_QUESTIONS','[#]OTP[/#]','MFA OTP','[#]OTP[/#]','fe482de6-bbe5-4282-b3a8-dd147c1b6885','admin1',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0) ;
	END IF;
	IF(SELECT count(*) from `appactionmfa` where `AppAction_id`='RETAIL_BANKING_SERVICE_ID_1') = 0  THEN 
		INSERT IGNORE INTO `appactionmfa` (`id`, `AppAction_id`, `IsMFARequired`, `FrequencyType_id`, `FrequencyValue`, `Status_id`, `Description`, `PrimaryMFAType`, `SecondaryMFAType`, `SMSText`, `EmailSubject`, `EmailBody`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('1043829291','RETAIL_BANKING_SERVICE_ID_1',0,'VALUE_BASED','5','SID_INACTIVE','Create Intra Bank Fund Transfer','SECURE_ACCESS_CODE','SECURITY_QUESTIONS','[#]OTP[/#]','Secure Access Code','[#]OTP[/#]','UID10','admin1',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
	END IF;
	IF(SELECT count(*) from `appactionmfa` where `AppAction_id`='RETAIL_BANKING_SERVICE_ID_68') = 0  THEN 
	 INSERT IGNORE INTO `appactionmfa` (`id`, `AppAction_id`, `IsMFARequired`, `FrequencyType_id`, `FrequencyValue`, `Status_id`, `Description`, `PrimaryMFAType`, `SecondaryMFAType`, `SMSText`, `EmailSubject`, `EmailBody`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('1069008617','RETAIL_BANKING_SERVICE_ID_68',0,'ALWAYS',NULL,'SID_INACTIVE','otp','SECURITY_QUESTIONS','SECURE_ACCESS_CODE','[#]OTP[/#]','Secure Access Code','[#]OTP[/#]','UID10','admin1',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
	 END IF;
	IF(SELECT count(*) from `appactionmfa` where `AppAction_id`='BUSINESS_BANKING_SERVICE_ID_67') = 0  THEN 
	 INSERT IGNORE INTO `appactionmfa` (`id`, `AppAction_id`, `IsMFARequired`, `FrequencyType_id`, `FrequencyValue`, `Status_id`, `Description`, `PrimaryMFAType`, `SecondaryMFAType`, `SMSText`, `EmailSubject`, `EmailBody`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('1118779959','BUSINESS_BANKING_SERVICE_ID_67',0,'ALWAYS',NULL,'SID_INACTIVE','Login','SECURE_ACCESS_CODE','SECURITY_QUESTIONS','[#]OTP[/#] is your secure access code. Please use this to complete your login','Your requested secure access code','Dear Customer,<br class=\"\"><br class=\"\">[#]OTP[/#] is your secure access code. Please use this to complete your login.<br class=\"\"><br class=\"\"><br class=\"\">Regards,<br class=\"\">DBX Bank','UID10','admin1',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
	 END IF;
	IF(SELECT count(*) from `appactionmfa` where `AppAction_id`='RETAIL_BANKING_SERVICE_ID_73') = 0  THEN 
	 INSERT IGNORE INTO `appactionmfa` (`id`, `AppAction_id`, `IsMFARequired`, `FrequencyType_id`, `FrequencyValue`, `Status_id`, `Description`, `PrimaryMFAType`, `SecondaryMFAType`, `SMSText`, `EmailSubject`, `EmailBody`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('1261437124','RETAIL_BANKING_SERVICE_ID_73',0,'ALWAYS',NULL,'SID_INACTIVE','Cardmanagement-replace','SECURE_ACCESS_CODE','SECURITY_QUESTIONS','[#]OTP[/#]','Secure Access Code','[#]OTP[/#]','UID10','anvesh.marripudi.o2f@kony.com',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
	 END IF;
	IF(SELECT count(*) from `appactionmfa` where `AppAction_id`='RETAIL_BANKING_SERVICE_ID_5') = 0  THEN 
	 INSERT IGNORE INTO `appactionmfa` (`id`, `AppAction_id`, `IsMFARequired`, `FrequencyType_id`, `FrequencyValue`, `Status_id`, `Description`, `PrimaryMFAType`, `SecondaryMFAType`, `SMSText`, `EmailSubject`, `EmailBody`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('1263952855','RETAIL_BANKING_SERVICE_ID_5',0,'ALWAYS',NULL,'SID_INACTIVE','Wire transfer','SECURE_ACCESS_CODE','SECURITY_QUESTIONS','[#]OTP[/#]','Wire Transfer','[#]OTP[/#]','UID10','admin1',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
	 END IF;
	IF(SELECT count(*) from `appactionmfa` where `AppAction_id`='RETAIL_BANKING_SERVICE_ID_71') = 0  THEN 
	 INSERT IGNORE INTO `appactionmfa` (`id`, `AppAction_id`, `IsMFARequired`, `FrequencyType_id`, `FrequencyValue`, `Status_id`, `Description`, `PrimaryMFAType`, `SecondaryMFAType`, `SMSText`, `EmailSubject`, `EmailBody`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('1318584743','RETAIL_BANKING_SERVICE_ID_71',0,'ALWAYS',NULL,'SID_INACTIVE','Cardmanagement-unlock','SECURE_ACCESS_CODE','SECURITY_QUESTIONS','[#]OTP[/#]','Secure Access Code','[#]OTP[/#]','UID10','admin1',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
	 END IF;
	IF(SELECT count(*) from `appactionmfa` where `AppAction_id`='RETAIL_BANKING_SERVICE_ID_3') = 0  THEN 
	 INSERT IGNORE INTO `appactionmfa` (`id`, `AppAction_id`, `IsMFARequired`, `FrequencyType_id`, `FrequencyValue`, `Status_id`, `Description`, `PrimaryMFAType`, `SecondaryMFAType`, `SMSText`, `EmailSubject`, `EmailBody`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('1333023862','RETAIL_BANKING_SERVICE_ID_3',0,'ALWAYS',NULL,'SID_INACTIVE','Interbank transfer','SECURE_ACCESS_CODE','SECURITY_QUESTIONS','[#]OTP[/#]','Secure Access Code','[#]OTP[/#]','UID10','govind.vanga',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
	 END IF;
	IF(SELECT count(*) from `appactionmfa` where `AppAction_id`='ONBOARDING_SERVICE_ID_67') = 0  THEN 
	 INSERT IGNORE INTO `appactionmfa` (`id`, `AppAction_id`, `IsMFARequired`, `FrequencyType_id`, `FrequencyValue`, `Status_id`, `Description`, `PrimaryMFAType`, `SecondaryMFAType`, `SMSText`, `EmailSubject`, `EmailBody`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('1339866089','ONBOARDING_SERVICE_ID_67',0,'ALWAYS',NULL,'SID_INACTIVE','OTP','SECURE_ACCESS_CODE','SECURITY_QUESTIONS','OTP is [#]OTP[/#]','OTP','OTP is [#]OTP[/#]','UID10','admin1',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
	 END IF;
	IF(SELECT count(*) from `appactionmfa` where `AppAction_id`='BUSINESS_BANKING_SERVICE_ID_57') = 0  THEN 
	 INSERT IGNORE INTO `appactionmfa` (`id`, `AppAction_id`, `IsMFARequired`, `FrequencyType_id`, `FrequencyValue`, `Status_id`, `Description`, `PrimaryMFAType`, `SecondaryMFAType`, `SMSText`, `EmailSubject`, `EmailBody`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('1365825668','BUSINESS_BANKING_SERVICE_ID_57',0,'VALUE_BASED','1000','SID_INACTIVE','MFA BB','SECURE_ACCESS_CODE','SECURITY_QUESTIONS','[#]OTP[/#]','BB OTP','[#]OTP[/#]','fe482de6-bbe5-4282-b3a8-dd147c1b6885','govind.vanga',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
	 END IF;
	IF(SELECT count(*) from `appactionmfa` where `AppAction_id`='RETAIL_BANKING_SERVICE_ID_6') = 0  THEN 
	 INSERT IGNORE INTO `appactionmfa` (`id`, `AppAction_id`, `IsMFARequired`, `FrequencyType_id`, `FrequencyValue`, `Status_id`, `Description`, `PrimaryMFAType`, `SecondaryMFAType`, `SMSText`, `EmailSubject`, `EmailBody`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('1397871175','RETAIL_BANKING_SERVICE_ID_6',0,'ALWAYS',NULL,'SID_INACTIVE','OTP','SECURE_ACCESS_CODE','SECURITY_QUESTIONS','[#]OTP[/#]','OTP','[#]OTP[/#]','UID10','admin1',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
	 END IF;
	IF(SELECT count(*) from `appactionmfa` where `AppAction_id`='RETAIL_BANKING_SERVICE_ID_74') = 0  THEN 
	 INSERT IGNORE INTO `appactionmfa` (`id`, `AppAction_id`, `IsMFARequired`, `FrequencyType_id`, `FrequencyValue`, `Status_id`, `Description`, `PrimaryMFAType`, `SecondaryMFAType`, `SMSText`, `EmailSubject`, `EmailBody`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('1425773498','RETAIL_BANKING_SERVICE_ID_74',0,'ALWAYS',NULL,'SID_INACTIVE','Card Management-Change Pin','SECURE_ACCESS_CODE','SECURITY_QUESTIONS','[#]OTP[/#]\n','OTP','<br>[#]OTP[/#]<br><br class=\"\">','UID10','admin1',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
	 END IF;
	IF(SELECT count(*) from `appactionmfa` where `AppAction_id`='RETAIL_BANKING_SERVICE_ID_7') = 0  THEN 
	 INSERT IGNORE INTO `appactionmfa` (`id`, `AppAction_id`, `IsMFARequired`, `FrequencyType_id`, `FrequencyValue`, `Status_id`, `Description`, `PrimaryMFAType`, `SecondaryMFAType`, `SMSText`, `EmailSubject`, `EmailBody`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('1452783723','RETAIL_BANKING_SERVICE_ID_7',0,'VALUE_BASED','5','SID_INACTIVE','Retail Banking - Bill Pay','SECURITY_QUESTIONS','SECURE_ACCESS_CODE','Your OTP is [#]OTP[/#]','OTP','HI,<br><br class=\"\"><br>Your OTP is [#]OTP[/#]','UID10','admin1',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
	 END IF;
	IF(SELECT count(*) from `appactionmfa` where `AppAction_id`='CONSUMER_LENDING_SERVICE_ID_67') = 0  THEN 
	 INSERT IGNORE INTO `appactionmfa` (`id`, `AppAction_id`, `IsMFARequired`, `FrequencyType_id`, `FrequencyValue`, `Status_id`, `Description`, `PrimaryMFAType`, `SecondaryMFAType`, `SMSText`, `EmailSubject`, `EmailBody`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('1468107479','CONSUMER_LENDING_SERVICE_ID_67',0,'ALWAYS',NULL,'SID_INACTIVE','OTP','SECURE_ACCESS_CODE','SECURITY_QUESTIONS','OTP is [#]OTP[/#]','OTP','OTP is [#]OTP[/#]','UID10','admin1',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
	 END IF;
	IF(SELECT count(*) from `appactionmfa` where `AppAction_id`='RETAIL_BANKING_SERVICE_ID_72') = 0  THEN 
	 INSERT IGNORE INTO `appactionmfa` (`id`, `AppAction_id`, `IsMFARequired`, `FrequencyType_id`, `FrequencyValue`, `Status_id`, `Description`, `PrimaryMFAType`, `SecondaryMFAType`, `SMSText`, `EmailSubject`, `EmailBody`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('1472980002','RETAIL_BANKING_SERVICE_ID_72',0,'ALWAYS',NULL,'SID_INACTIVE','cardmanagement-replacecard','SECURITY_QUESTIONS','SECURE_ACCESS_CODE','[#]OTP[/#]','Secure Access Code','[#]OTP[/#]','UID10','admin1',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
	 END IF;
	IF(SELECT count(*) from `appactionmfa` where `AppAction_id`='RETAIL_BANKING_SERVICE_ID_75') = 0  THEN 
	 INSERT IGNORE INTO `appactionmfa` (`id`, `AppAction_id`, `IsMFARequired`, `FrequencyType_id`, `FrequencyValue`, `Status_id`, `Description`, `PrimaryMFAType`, `SecondaryMFAType`, `SMSText`, `EmailSubject`, `EmailBody`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('1518695287','RETAIL_BANKING_SERVICE_ID_75',0,'ALWAYS',NULL,'SID_INACTIVE','Card Management-Cancel Card','SECURITY_QUESTIONS','SECURE_ACCESS_CODE','[#]OTP[/#]\n','OTp','<br>[#]OTP[/#]<br><br class=\"\">','UID10','admin1',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
	 END IF;
	IF(SELECT count(*) from `appactionmfa` where `AppAction_id`='RETAIL_BANKING_SERVICE_ID_70') = 0  THEN 
	 INSERT IGNORE INTO `appactionmfa` (`id`, `AppAction_id`, `IsMFARequired`, `FrequencyType_id`, `FrequencyValue`, `Status_id`, `Description`, `PrimaryMFAType`, `SecondaryMFAType`, `SMSText`, `EmailSubject`, `EmailBody`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('1588624560','RETAIL_BANKING_SERVICE_ID_70',0,'ALWAYS',NULL,'SID_INACTIVE','Card Management- Lock Card','SECURITY_QUESTIONS','SECURE_ACCESS_CODE','[#]OTP[/#]\n','OTP','<br>[#]OTP[/#]<br><br class=\"\">','UID10','admin1',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
	 END IF;
	IF(SELECT count(*) from `appactionmfa` where `AppAction_id`='RETAIL_BANKING_SERVICE_ID_69') = 0  THEN 
	 INSERT IGNORE INTO `appactionmfa` (`id`, `AppAction_id`, `IsMFARequired`, `FrequencyType_id`, `FrequencyValue`, `Status_id`, `Description`, `PrimaryMFAType`, `SecondaryMFAType`, `SMSText`, `EmailSubject`, `EmailBody`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('1692872967','RETAIL_BANKING_SERVICE_ID_69',0,'ALWAYS',NULL,'SID_INACTIVE','[#]OTP[/#]','SECURITY_QUESTIONS','SECURE_ACCESS_CODE','[#]OTP[/#]','Secure Access Code','[#]OTP[/#]','UID10','admin1',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
	 END IF;
	IF(SELECT count(*) from `appactionmfa` where `AppAction_id`='RETAIL_BANKING_SERVICE_ID_4') = 0  THEN 
	 INSERT IGNORE INTO `appactionmfa` (`id`, `AppAction_id`, `IsMFARequired`, `FrequencyType_id`, `FrequencyValue`, `Status_id`, `Description`, `PrimaryMFAType`, `SecondaryMFAType`, `SMSText`, `EmailSubject`, `EmailBody`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('1710928803','RETAIL_BANKING_SERVICE_ID_4',0,'ALWAYS',NULL,'SID_INACTIVE','International transfer','SECURITY_QUESTIONS','SECURE_ACCESS_CODE','[#]OTP[/#]','Secure Access Code','[#]OTP[/#]','UID10','admin1',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
	 END IF;
	IF(SELECT count(*) from `appactionmfa` where `AppAction_id`='RETAIL_BANKING_SERVICE_ID_2') = 0  THEN 
	 INSERT IGNORE INTO `appactionmfa` (`id`, `AppAction_id`, `IsMFARequired`, `FrequencyType_id`, `FrequencyValue`, `Status_id`, `Description`, `PrimaryMFAType`, `SecondaryMFAType`, `SMSText`, `EmailSubject`, `EmailBody`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('1745986962','RETAIL_BANKING_SERVICE_ID_2',0,'VALUE_BASED','1','SID_INACTIVE','Create internal transfer','SECURITY_QUESTIONS','SECURE_ACCESS_CODE','[#]OTP[/#]','Your Secure Access Code','[#]OTP[/#]','UID10','govind.vanga',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
	 END IF;
	IF(SELECT count(*) from `appactionmfa` where `AppAction_id`='RETAIL_BANKING_SERVICE_ID_67') = 0  THEN 
	 INSERT IGNORE INTO `appactionmfa` (`id`, `AppAction_id`, `IsMFARequired`, `FrequencyType_id`, `FrequencyValue`, `Status_id`, `Description`, `PrimaryMFAType`, `SecondaryMFAType`, `SMSText`, `EmailSubject`, `EmailBody`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('1815885335','RETAIL_BANKING_SERVICE_ID_67',0,'ALWAYS','','SID_INACTIVE','Login','SECURE_ACCESS_CODE','SECURITY_QUESTIONS','[#]OTP[/#]','Secure OTP','[#]OTP[/#]','UID10','anvesh.marripudi.o2f@kony.com',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
	 END IF;
	IF(SELECT count(*) from `appactionmfa` where `AppAction_id`='RETAIL_BANKING_SERVICE_ID_8') = 0  THEN 
	 INSERT IGNORE INTO `appactionmfa` (`id`, `AppAction_id`, `IsMFARequired`, `FrequencyType_id`, `FrequencyValue`, `Status_id`, `Description`, `PrimaryMFAType`, `SecondaryMFAType`, `SMSText`, `EmailSubject`, `EmailBody`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('1868396778','RETAIL_BANKING_SERVICE_ID_8',0,'VALUE_BASED','5','SID_INACTIVE','MFA transaction','SECURE_ACCESS_CODE','SECURITY_QUESTIONS','[#]OTP[/#]','Secure access code','[#]OTP[/#]','UID10','admin1',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
	 END IF;
END$$
DELIMITER ;
call insertBasicMFAScenarios();
DROP PROCEDURE IF EXISTS `insertBasicMFAScenarios`;

-- >> CAMPAIGN MANAGEMENT <<
-- Table
DROP TABLE IF EXISTS `campaign`;
CREATE TABLE `campaign` (
  `id` VARCHAR(50) NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `status_id` VARCHAR(50) NOT NULL,
  `priority` INT(3) NOT NULL,
  `start_datetime` TIMESTAMP NULL,
  `end_datetime` TIMESTAMP NULL,
  `description` VARCHAR(150) NULL,
  `createdby` VARCHAR(50) NULL,
  `modifiedby` VARCHAR(50) NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  INDEX `FK_Campaign_status_idx` (`status_id` ASC),
  CONSTRAINT `FK_Campaign_status`
    FOREIGN KEY (`status_id`)
    REFERENCES `status` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `campaignplaceholder`;
CREATE TABLE `campaignplaceholder` (
  `id` VARCHAR(50) NOT NULL,
  `channel` VARCHAR(50) NOT NULL,
  `screen` VARCHAR(50) NOT NULL,
  `image_resolution` VARCHAR(10) NOT NULL,
  `image_scale` VARCHAR(10) NOT NULL,
  `image_size` VARCHAR(20) NOT NULL,
  `createdby` VARCHAR(50) NULL,
  `modifiedby` VARCHAR(50) NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `campaignspecification`;
CREATE TABLE `campaignspecification` (
  `campaign_id` VARCHAR(50) NOT NULL,
  `campaignplaceholder_id` VARCHAR(50) NOT NULL,
  `image_url` VARCHAR(200) NOT NULL,
  `destination_url` VARCHAR(200) NULL,
  `createdby` VARCHAR(50) NULL,
  `modifiedby` VARCHAR(50) NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`campaign_id`,`campaignplaceholder_id`),
  KEY `FK_CampaignSpecification_campaignplaceholder_id_idx` (`campaignplaceholder_id`),
  CONSTRAINT `FK_CampaignSpecification_campaign_id` 
  	FOREIGN KEY (`campaign_id`) 
  	REFERENCES `campaign` (`id`) 
  	ON DELETE NO ACTION 
  	ON UPDATE NO ACTION,
  CONSTRAINT `FK_CampaignSpecification_campaignplaceholder_id` 
  	FOREIGN KEY (`campaignplaceholder_id`) 
  	REFERENCES `campaignplaceholder` (`id`) 
  	ON DELETE NO ACTION 
  	ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `campaigngroup`;
CREATE TABLE `campaigngroup` (
  `campaign_id` VARCHAR(50) NOT NULL,
  `group_id` VARCHAR(50) NOT NULL,
  `createdby` VARCHAR(50) NULL,
  `modifiedby` VARCHAR(50) NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`campaign_id`, `group_id`),
  INDEX `FK_CampaignGroup_group_id_idx` (`group_id` ASC),
  CONSTRAINT `FK_CampaignGroup_campaign_id`
    FOREIGN KEY (`campaign_id`)
    REFERENCES `campaign` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_CampaignGroup_group_id`
    FOREIGN KEY (`group_id`)
    REFERENCES `membergroup` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `defaultcampaign`;
CREATE TABLE `defaultcampaign` (
  `name` varchar(50) NOT NULL,
  `description` varchar(150) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `defaultcampaignspecification`;
CREATE TABLE `defaultcampaignspecification` (
  `campaignplaceholder_id` varchar(50) NOT NULL,
  `image_index` int(2) NOT NULL,
  `image_url` varchar(200) DEFAULT NULL,
  `destination_url` varchar(200) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`campaignplaceholder_id`,`image_index`),
  CONSTRAINT `FK_DefaultCampaignSpecification_campaignplaceholder_id` 
  	FOREIGN KEY (`campaignplaceholder_id`) 
  	REFERENCES `campaignplaceholder` (`id`) 
  	ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
-- Table

-- Stored procedures
-- 1
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

SELECT campaignspecification.campaign_id, 
		campaignspecification.image_url, campaignspecification.destination_url,
		campaign.priority
		FROM campaignspecification 
		LEFT JOIN campaign ON (campaignspecification.campaign_id = campaign.id) 
		WHERE campaignspecification.campaignplaceholder_id = placeholderId 
		AND campaignspecification.campaign_id IN (
			SELECT id FROM campaign WHERE 
				status_id = 'SID_SCHEDULED_ACTIVE_COMPLETED' AND 
				id IN (SELECT campaign_id FROM campaigngroup WHERE group_id IN 
					(SELECT Group_id FROM customergroup WHERE Customer_id = 
						(SELECT id FROM customer WHERE UserName = _username)))
				AND start_datetime <= STR_TO_DATE(_currentTimestamp, '%m/%d/%Y %H:%i:%s') 
			AND end_datetime >= STR_TO_DATE(_currentTimestamp, '%m/%d/%Y %H:%i:%s'))
UNION ALL
SELECT 'DEFAULT_CAMPAIGN' AS campaign_id, image_url, destination_url, 1000 AS priority
		FROM defaultcampaignspecification 
		WHERE campaignplaceholder_id = placeholderId 
ORDER BY priority;

END$$
DELIMITER ;
-- 1
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

SELECT campaignspecification.campaign_id, 
		campaignspecification.image_url, campaignspecification.destination_url,
		campaign.priority
		FROM campaignspecification 
		LEFT JOIN campaign ON (campaignspecification.campaign_id = campaign.id) 
		WHERE campaignspecification.campaignplaceholder_id = placeholderId 
		AND campaignspecification.campaign_id IN (
			SELECT id FROM campaign WHERE 
            status_id = 'SID_SCHEDULED_ACTIVE_COMPLETED' AND 
            id IN (SELECT campaign_id FROM campaigngroup WHERE group_id IN 
					(SELECT Group_id FROM customergroup WHERE Customer_id = 
						(SELECT id FROM customer WHERE UserName = 
							(SELECT Username FROM customer WHERE id = 
								(SELECT Customer_id FROM customerdevice WHERE id = _deviceId
									and Channel_id = 'CH_ID_MOB' order by LastLoginTime desc LIMIT 1))
                        )))
				AND start_datetime <= STR_TO_DATE(_currentTimestamp, '%m/%d/%Y %H:%i:%s') 
			AND end_datetime >= STR_TO_DATE(_currentTimestamp, '%m/%d/%Y %H:%i:%s'))
UNION ALL
SELECT 'DEFAULT_CAMPAIGN' AS campaign_id, image_url, destination_url, 1000 AS priority
		FROM defaultcampaignspecification 
		WHERE campaignplaceholder_id = placeholderId 
ORDER BY priority;

END$$
DELIMITER ;
-- 2
-- 3
DROP PROCEDURE IF EXISTS `campaign_c360_specification_get_proc`;
DELIMITER $$

CREATE PROCEDURE `campaign_c360_specification_get_proc`(
in _campaignId varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
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
-- 4
DROP PROCEDURE IF EXISTS `campaign_c360_default_specification_get_proc`;
DELIMITER $$

CREATE PROCEDURE `campaign_c360_default_specification_get_proc`()
BEGIN

SELECT  
campaignplaceholder.channel, campaignplaceholder.screen, 
campaignplaceholder.image_resolution, campaignplaceholder.image_scale, 
defaultcampaignspecification.image_index,
defaultcampaignspecification.image_url, defaultcampaignspecification.destination_url
FROM defaultcampaignspecification 
LEFT JOIN campaignplaceholder ON (defaultcampaignspecification.campaignplaceholder_id = campaignplaceholder.id);

END$$
DELIMITER ;
-- 4
-- 5
DROP PROCEDURE IF EXISTS `campaigngroup_membergroup_read_proc`;
DELIMITER $$
CREATE PROCEDURE `campaigngroup_membergroup_read_proc`(
	IN _campaignId varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
)
BEGIN

SET @queryStatement = "
	SELECT id AS group_id, Name AS group_name from
		membergroup LEFT JOIN campaigngroup ON (membergroup.id = campaigngroup.group_id)
		WHERE campaigngroup.campaign_id = ";
SET @queryStatement = concat(@queryStatement, quote(_campaignId));
  
PREPARE stmt FROM @queryStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

END$$
DELIMITER ;
-- 5
-- 6
DROP PROCEDURE IF EXISTS `campaign_priority_update_proc`;
DELIMITER $$

CREATE PROCEDURE `campaign_priority_update_proc`(
	IN _priorityStart INT(100),
	IN _priorityEnd INT(100)
)
BEGIN

SET sql_safe_updates = 0;
UPDATE campaign 
	SET priority = priority + 1 
    WHERE priority >= _priorityStart AND priority < _priorityEnd;
SET sql_safe_updates = 1;

END$$
DELIMITER ;
-- 6
-- 7
DROP PROCEDURE IF EXISTS `campaign_specification_createupdate_proc`;
DELIMITER $$

CREATE PROCEDURE `campaign_specification_createupdate_proc`(
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
END IF;

IF existingImageURL IS NOT NULL THEN
	SET @mainQuery = CONCAT(
		"update campaignspecification set image_url = ", quote(_imageURL), 
		", modifiedby = ", quote(_userId),
		", destination_url = ", quote(_destinationURL),
		" where campaign_id = ", quote(_campaignId),
		" and campaignplaceholder_id = ", quote(placeholderId));
END IF;

PREPARE queryStatement FROM @mainQuery;
EXECUTE queryStatement;
DEALLOCATE PREPARE queryStatement;

END$$
DELIMITER ;
-- 7
-- 8
DROP PROCEDURE IF EXISTS `defaultcampaignspecification_update_proc`;
DELIMITER $$

CREATE PROCEDURE `defaultcampaignspecification_update_proc`(
	IN _channel VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _screen VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _imageIndex INT(2),
    IN _imageResolution VARCHAR(10) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _imageURL VARCHAR(200) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _destinationURL VARCHAR(200) CHARACTER SET UTF8 COLLATE utf8_general_ci,
	IN _modifiedBy VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
)
BEGIN

DECLARE placeholderId VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci;
SET placeholderId = (
	SELECT id FROM campaignplaceholder 
    WHERE channel = _channel
	AND screen = _screen
    AND image_resolution = _imageResolution
);

SET @mainQuery = CONCAT(
	"update defaultcampaignspecification set image_url = ", quote(_imageURL), 
	", modifiedby = ", quote(_modifiedBy),
	", destination_url = ", quote(_destinationURL),
	" where campaignplaceholder_id = ", quote(placeholderId),
	" and image_index = ", quote(_imageIndex));

PREPARE queryStatement FROM @mainQuery;
EXECUTE queryStatement;
DEALLOCATE PREPARE queryStatement;

END$$
DELIMITER ;
-- Stored procedures
-- >> CAMPAIGN MANAGEMENT <<

DROP TABLE IF EXISTS `lead`;
CREATE TABLE `lead` (
  `id` varchar(50) NOT NULL,
  `firstName` varchar(50) NOT NULL,
  `middleName` varchar(50) DEFAULT NULL,
  `lastName` varchar(50) DEFAULT NULL,
  `salutation` varchar(45) DEFAULT NULL,
  `isCustomer` tinyint(4) NOT NULL DEFAULT '0',
  `customerId` varchar(50) DEFAULT NULL,
  `product_id` varchar(50) DEFAULT NULL,
  `csr_id` varchar(50) DEFAULT NULL,
  `status_id` varchar(50) DEFAULT NULL,
  `countryCode` varchar(10) DEFAULT NULL,
  `phoneNumber` varchar(50) DEFAULT NULL,
  `extension` varchar(10) DEFAULT NULL,
  `email` varchar(50) DEFAULT NULL,
  `closureReason` text,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_lead_systemuser_idx` (`csr_id`),
  KEY `FK_lead_product_idx` (`product_id`),
  KEY `FK_lead_status_idx` (`status_id`),
  KEY `FK_lead_customer_idx` (`customerId`),
  CONSTRAINT `FK_lead_customer` FOREIGN KEY (`customerId`) REFERENCES `customer` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_lead_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_lead_status` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_lead_systemuser` FOREIGN KEY (`csr_id`) REFERENCES `systemuser` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `archivedlead`;
CREATE TABLE `archivedlead` (
  `id` varchar(50) NOT NULL,
  `firstName` varchar(50) NOT NULL,
  `middleName` varchar(50) DEFAULT NULL,
  `lastName` varchar(50) DEFAULT NULL,
  `salutation` varchar(45) DEFAULT NULL,
  `isCustomer` tinyint(4) NOT NULL DEFAULT '0',
  `customerId` varchar(50) DEFAULT NULL,
  `product_id` varchar(50) DEFAULT NULL,
  `csr_id` varchar(50) DEFAULT NULL,
  `status_id` varchar(50) DEFAULT NULL,
  `countryCode` varchar(10) DEFAULT NULL,
  `phoneNumber` varchar(50) DEFAULT NULL,
  `extension` varchar(10) DEFAULT NULL,
  `email` varchar(50) DEFAULT NULL,
  `closureReason` text,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_archivedlead_systemuser_idx` (`csr_id`),
  KEY `FK_archivedlead_product_idx` (`product_id`),
  KEY `FK_archivedlead_status_idx` (`status_id`),
  KEY `FK_archivedlead_customer_idx` (`customerId`),
  CONSTRAINT `FK_archivedlead_customer` FOREIGN KEY (`customerId`) REFERENCES `customer` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_archivedlead_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_archivedlead_status` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_archivedlead_systemuser` FOREIGN KEY (`csr_id`) REFERENCES `systemuser` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `status` 
DROP PRIMARY KEY,
ADD PRIMARY KEY (`id`, `Type_id`);

DROP VIEW IF EXISTS `lead_csr_count_view`;
CREATE VIEW `lead_csr_count_view` AS select `lead`.`csr_id` AS `csrId`,count(`lead`.`id`) AS `count` from `lead` group by `lead`.`csr_id`;

DROP VIEW IF EXISTS `lead_status_count_view`;
CREATE VIEW `lead_status_count_view` AS select count(`le`.`id`) AS `count`,`s1`.`id` AS `leadStatus` from (`status` `s1` left join `lead` `le` on((`s1`.`id` = `le`.`status_id`))) where ((`s1`.`Type_id` = 'STID_LEADSTATUS') and (`s1`.`id` <> 'SID_ARCHIVED')) group by `s1`.`id` union select count(`ale`.`id`) AS `count`,`s2`.`id` AS `leadStatus` from (`status` `s2` left join `archivedlead` `ale` on((`s2`.`id` = `ale`.`status_id`))) where ((`s2`.`Type_id` = 'STID_LEADSTATUS') and (`s2`.`id` = 'SID_ARCHIVED')) group by `s2`.`id`;

DROP PROCEDURE IF EXISTS `lead_search_proc`;
DELIMITER $$
CREATE PROCEDURE `lead_search_proc`(
IN _productId varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _leadId  varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _productType varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _customerId varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _leadType varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _statusIds varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _assignedCSRID varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _modifiedStartDate varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _modifiedEndDate varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _phoneNumber varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _emailAddress varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _sortOrder varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _sortCriteria varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _offset BIGINT,
IN _recordsPerPage BIGINT
)
BEGIN

SET @tableName="lead";
 
IF _statusIds = 'SID_ARCHIVED' THEN
	SET @tableName="archivedlead";
END IF;

 SET @queryStatement =CONCAT( "
 SELECT 
",@tableName,".`id` AS `id`,
",@tableName,".`firstName` AS `firstName`,
",@tableName,".`middleName` AS `middleName`,
",@tableName,".`lastName` AS `lastName`,
",@tableName,".`salutation` AS `salutation`,
",@tableName,".`isCustomer` AS `isCustomer`,
",@tableName,".`customerId` AS `customerId`,
`customer`.`CustomerType_id` AS `customerType`,
",@tableName,".`product_id` AS `productId`,
`product`.`name` AS `productName`,
`product`.`Type_id` AS `productType`,
",@tableName,".`csr_id` AS `csrId`,
`su1`.`FirstName` AS `assignedToFirstName`,
`su1`.`MiddleName` AS `assignedToMiddleName`,
`su1`.`LastName` AS `assignedToLastName`,
",@tableName,".`status_id` AS `statusId`,
",@tableName,".`closureReason` AS `closureReason`,
",@tableName,".`countryCode` AS `countryCode`,
",@tableName,".`phoneNumber` AS `phoneNumber`,
",@tableName,".`extension` AS `extension`,
",@tableName,".`email` AS `email`,
",@tableName,".`createdby` AS `createdby`,
`su2`.`FirstName` AS `createdByFirstName`,
`su2`.`MiddleName` AS `createdByMiddleName`,
`su2`.`LastName` AS `createdByLastName`,
`su3`.`FirstName` AS `modifiedbyFirstName`,
`su3`.`MiddleName` AS `modifiedbyMiddleName`,
`su3`.`LastName` AS `modifiedbyLastName`,
",@tableName,".`modifiedby` AS `modifiedby`,
",@tableName,".`createdts` AS `createdts`,
",@tableName,".`lastmodifiedts` AS `lastmodifiedts`,
",@tableName,".`synctimestamp` AS `synctimestamp`,
",@tableName,".`softdeleteflag` AS `softdeleteflag`
    FROM
        (`",@tableName,"`
        LEFT JOIN `systemuser` su1 ON `",@tableName,"`.`csr_id` = `su1`.`id`
		LEFT JOIN `product` ON `",@tableName,"`.`product_id` = `product`.`id`
        LEFT JOIN `systemuser` su2 ON `",@tableName,"`.`createdby` = `su2`.`id`
		LEFT JOIN `systemuser` su3 ON `",@tableName,"`.`modifiedby` = `su3`.`id`
        LEFT JOIN `customer` ON `",@tableName,"`.`customerId` = `customer`.`id`) where true");
        
		IF _leadId != '' THEN
			SET @queryStatement = concat(@queryStatement," and ",@tableName,".id = ", quote(_leadId));
		END IF;
		
		IF _productId != '' THEN
			SET @queryStatement = concat(@queryStatement," and ",@tableName,".product_id = ", quote(_productId));
		END IF;
        
		IF _productType != '' THEN
			SET @queryStatement = concat(@queryStatement," and ", "product.Type_id` = ", quote(_productType));
		END IF;
        
		IF _customerId != '' THEN
			SET @queryStatement = concat(@queryStatement," and ",@tableName,".customerId = ", quote(_customerId));
		END IF;
        
		IF _assignedCSRID != '' THEN
			SET @queryStatement = concat(@queryStatement," and ",@tableName,".csr_id = ", quote(_assignedCSRID));
		END IF;
        
		IF _phoneNumber != '' THEN
			SET @queryStatement = concat(@queryStatement," and ",@tableName,".phoneNumber like concat(", quote(_phoneNumber),",'%')");
		END IF;
        
		IF _emailAddress != '' THEN
			SET @queryStatement = concat(@queryStatement," and ",@tableName,".email = ", quote(_emailAddress));
		END IF;
		
		IF _leadType = 'CUSTOMER' THEN
				SET @queryStatement = concat(@queryStatement," and ",@tableName,".isCustomer = 1");
		ELSEIF _leadType = 'NON-CUSTOMER' THEN
				SET @queryStatement = concat(@queryStatement," and ",@tableName,".isCustomer = 0");
		END IF;
        
		IF _statusIds != '' THEN
			SET @queryStatement = concat(@queryStatement," and ",@tableName,".status_id in  (", func_escape_input_for_in_operator(_statusIds),")");
		END IF;
        
		IF _modifiedStartDate != '' AND _modifiedEndDate != '' THEN
			SET @queryStatement = concat(@queryStatement," and DATE(",@tableName,".lastmodifiedts) >=",quote(DATE(_modifiedStartDate)), " and  DATE(",@tableName,".lastmodifiedts) <=",quote(DATE(_modifiedEndDate)));
		END IF;
		
		IF _sortOrder != 'asc' AND _sortOrder != 'desc' THEN
			SET _sortOrder = "desc";
		END IF;
        
		IF _sortCriteria = '' THEN
			SET _sortCriteria ="lastmodifiedts";
		END IF;
	
   SET @queryStatement = concat(@queryStatement," order by `",_sortCriteria,"` ",_sortOrder);
		SET @queryStatement = concat(@queryStatement," LIMIT ",_recordsPerPage," OFFSET ",_offset);
		PREPARE stmt FROM @queryStatement;
		EXECUTE stmt;
		DEALLOCATE PREPARE stmt;
END$$
DELIMITER ;

ALTER TABLE `product` 
ADD COLUMN `isLeadSupported` TINYINT(1) NOT NULL DEFAULT 0 AFTER `Status_id`;

DROP VIEW IF EXISTS `productdetail_view`;
CREATE VIEW `productdetail_view` AS select `product`.`id` AS `productId`,`product`.`Type_id` AS `Type_id`,`product`.`isLeadSupported` AS `isLeadSupported`,`product`.`ProductCode` AS `productTypeId`,`product`.`Name` AS `productName`,`product`.`Status_id` AS `Status_id`,`product`.`ProductFeatures` AS `features`,`product`.`ProductCharges` AS `rates`,`product`.`AdditionalInformation` AS `info`,`product`.`productDescription` AS `productDescription`,`product`.`termsAndConditions` AS `termsAndConditions`,`product`.`createdby` AS `createdby`,`product`.`modifiedby` AS `modifiedby`,`product`.`createdts` AS `createdts`,`product`.`lastmodifiedts` AS `lastmodifiedts`,`product`.`synctimestamp` AS `synctimestamp`,`product`.`softdeleteflag` AS `softdeleteflag`,`producttype`.`Name` AS `productType`,`product`.`MarketingStateId` AS `MarketingStateId`,`product`.`SecondaryProduct_id` AS `SecondaryProduct_id`,`otherproducttype`.`Name` AS `otherproducttype_Name`,`otherproducttype`.`Description` AS `otherproducttype_Description`,`otherproducttype`.`id` AS `otherproducttype_id` from ((`product` join `producttype` on((`product`.`Type_id` = `producttype`.`id`))) join `otherproducttype` on((`product`.`OtherProductType_id` = `otherproducttype`.`id`)));

DROP TABLE IF EXISTS `closurereason`;
CREATE TABLE `closurereason` (
  `id` varchar(50) NOT NULL,
  `reason` text,
  `status_id` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT NULL,
  `lastmodifiedts` timestamp NULL DEFAULT NULL,
  `synctimestamp` timestamp NULL DEFAULT NULL,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_closurereason_status_idx` (`status_id`),
  CONSTRAINT `FK_closurereason_status` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `leadnote`;
CREATE TABLE `leadnote` (
  `id` varchar(50) NOT NULL,
  `lead_Id` varchar(50) DEFAULT NULL,
  `note` text,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_leadnote_lead_idx` (`lead_Id`),
  CONSTRAINT `FK_leadnote_lead` FOREIGN KEY (`lead_Id`) REFERENCES `lead` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `archivedleadnote`;
CREATE TABLE `archivedleadnote` (
  `id` varchar(50) NOT NULL,
  `lead_Id` varchar(50) DEFAULT NULL,
  `note` text,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_archivedleadnote_archivedlead_idx` (`lead_Id`),
  CONSTRAINT `FK_archivedleadnote_archivedlead` FOREIGN KEY (`lead_Id`) REFERENCES `archivedlead` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP PROCEDURE IF EXISTS `lead_search_count_proc`;
DELIMITER $$
CREATE PROCEDURE `lead_search_count_proc`(
IN _productId varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _productType varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _customerId varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _leadType varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _statusIds varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _assignedCSRID varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _modifiedStartDate varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _modifiedEndDate varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _phoneNumber varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _emailAddress varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
)
BEGIN

SET @tableName="lead";
 
IF _statusIds = 'SID_ARCHIVED' THEN
	SET @tableName="archivedlead";
END IF;

 SET @queryStatement =CONCAT( "
 SELECT 
count(",@tableName,".`id`) AS `count` FROM
        (`",@tableName,"`
        LEFT JOIN `systemuser` su1 ON `",@tableName,"`.`csr_id` = `su1`.`id`
		LEFT JOIN `product` ON `",@tableName,"`.`product_id` = `product`.`id`
        LEFT JOIN `systemuser` su2 ON `",@tableName,"`.`createdby` = `su2`.`id`
		LEFT JOIN `systemuser` su3 ON `",@tableName,"`.`modifiedby` = `su3`.`id`) where true");
        
        IF _productId != '' THEN
		SET @queryStatement = concat(@queryStatement," and ",@tableName,".product_id = ", quote(_productId));
        END IF;
        
		IF _productType != '' THEN
		SET @queryStatement = concat(@queryStatement," and ", "product.Type_id` = ", quote(_productType));
        END IF;
        
         IF _customerId != '' THEN
		SET @queryStatement = concat(@queryStatement," and ",@tableName,".customerId = ", quote(_customerId));
        END IF;
        
		IF _assignedCSRID != '' THEN
		SET @queryStatement = concat(@queryStatement," and ",@tableName,".csr_id = ", quote(_assignedCSRID));
		END IF;
        
         IF _phoneNumber != '' THEN
		SET @queryStatement = concat(@queryStatement," and ",@tableName,".phoneNumber like concat(", quote(_phoneNumber),",'%')");
		END IF;
        
        IF _emailAddress != '' THEN
		SET @queryStatement = concat(@queryStatement," and ",@tableName,".email = ", quote(_emailAddress));
		END IF;
		
		IF _leadType = 'CUSTOMER' THEN
		SET @queryStatement = concat(@queryStatement," and ",@tableName,".isCustomer = 1");
        ELSEIF _leadType = 'NON-CUSTOMER' THEN
        	set @queryStatement = concat(@queryStatement," and ",@tableName,".isCustomer = 0");
		END IF;
        
		IF _statusIds != '' THEN
		SET @queryStatement = concat(@queryStatement," and ",@tableName,".status_id in  (", func_escape_input_for_in_operator(_statusIds),")");
		END IF;
        
		IF _modifiedStartDate != '' AND _modifiedEndDate != '' THEN
		SET @queryStatement = concat(@queryStatement," and DATE(",@tableName,".lastmodifiedts) >=",quote(DATE(_modifiedStartDate)), " and  DATE(",@tableName,".lastmodifiedts) <=",quote(DATE(_modifiedEndDate)));
		END IF;

		-- select @queryStatement;
		PREPARE stmt FROM @queryStatement;
		EXECUTE stmt;
		DEALLOCATE PREPARE stmt;
END$$
DELIMITER ;

DROP TABLE IF EXISTS `termandcondition`;
CREATE TABLE `termandcondition` (
  `id` varchar(50) NOT NULL,
  `Code` varchar(255) NOT NULL,
  `Title` varchar(255) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `Termandcondition_unique_index` (`Code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `contenttype`;
CREATE TABLE `contenttype` (
  `id` VARCHAR(50) NOT NULL,
  `Name` VARCHAR(50) NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`))  ENGINE = InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `termandconditiontext`;
CREATE TABLE `termandconditiontext` (
  `id` varchar(50) NOT NULL,
  `TermAndConditionId` varchar(50) NOT NULL,
  `LanguageCode` varchar(10) NOT NULL,
  `Content` LONGTEXT DEFAULT NULL,
  `ContentType_id` VARCHAR(50) NOT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `Termandconditiontext_unique_index` (`TermAndConditionId`, `LanguageCode`),
  INDEX `FK_termandconditiontext_contenttype_id_idx` (`ContentType_id` ASC),
  CONSTRAINT `FK_termandconditiontext_termandcondition` FOREIGN KEY (`TermAndConditionId`) REFERENCES `termandcondition` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_termandconditiontext_locale` FOREIGN KEY (`LanguageCode`) REFERENCES `locale` (`Code`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_termandconditiontext_contenttype_id` FOREIGN KEY (`ContentType_id`) REFERENCES `contenttype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
  
DROP TABLE IF EXISTS `browsersupporttype`;
CREATE TABLE `browsersupporttype` (
  `id` VARCHAR(50) NOT NULL,
  `name` VARCHAR(50) NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;
  
DROP TABLE IF EXISTS `browsersupport`;
CREATE TABLE `browsersupport` (
  `id` varchar(50) NOT NULL,
  `name` varchar(50) DEFAULT NULL,
  `icon` LONGTEXT DEFAULT NULL,
  `minimumVersionSupported` varchar(50) DEFAULT NULL,
  `downloadLink` varchar(500) DEFAULT NULL,
  `supportedOperatingSystems` varchar(100) DEFAULT NULL,
  `supportType` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `browsersupport_supporttype_id_idx` (`supportType`),
  CONSTRAINT `browsersupport_supporttype_id` FOREIGN KEY (`supportType`) REFERENCES `browsersupporttype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `browsersupportdisplaynametext`;
CREATE TABLE `browsersupportdisplaynametext` (
  `BrowserSupport_id` varchar(50) NOT NULL,
  `Locale_id` varchar(50) NOT NULL,
  `displayNameText` varchar(500) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`BrowserSupport_id`,`Locale_id`),
  KEY `browsersupportdisplaynametext_locale_id_idx` (`Locale_id`),
  CONSTRAINT `browsersupportdisplaynametext_browsersupport_id` FOREIGN KEY (`BrowserSupport_id`) REFERENCES `browsersupport` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `browsersupportdisplaynametext_locale_id` FOREIGN KEY (`Locale_id`) REFERENCES `locale` (`Code`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `configurations` ADD COLUMN `isPreLoginConfiguration` TINYINT(1) NOT NULL DEFAULT 0 AFTER `target`;

DROP VIEW IF EXISTS `configuration_view`;
CREATE VIEW `configuration_view` AS select `configurations`.`configuration_id` AS `configuration_id`,`configurations`.`bundle_id` AS `bundle_id`,`configurationbundles`.`bundle_name` AS `bundle_name`,`configurations`.`config_type` AS `type`,`configurations`.`config_key` AS `key`,`configurations`.`description` AS `description`,`configurations`.`config_value` AS `value`,`configurations`.`target` AS `target`,`configurations`.`isPreLoginConfiguration` AS `isPreLoginConfiguration`,`configurationbundles`.`app_id` AS `app_id` from (`configurations` left join `configurationbundles` on((`configurationbundles`.`bundle_id` = `configurations`.`bundle_id`)));

ALTER TABLE `termandcondition` 
ADD COLUMN `ContentModifiedBy` varchar(50) NOT NULL DEFAULT 'Kony User' AFTER `Description`,
ADD COLUMN `ContentModifiedOn` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `ContentModifiedBy`,
ADD CONSTRAINT `Termandcondition_title_unique_index` UNIQUE (`Title`);

DROP TABLE IF EXISTS `termandconditionapp`;
CREATE TABLE `termandconditionapp` (
  `id` varchar(50) NOT NULL,
  `TermAndConditionId` varchar(50) NOT NULL,
  `AppId` varchar(50) NOT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `Termandconditionapp_unique_index` (`TermAndConditionId`, `AppId`),
  CONSTRAINT `FK_termandconditionapp_termandcondition` FOREIGN KEY (`TermAndConditionId`) REFERENCES `termandcondition` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_termandconditionapp_app` FOREIGN KEY (`AppId`) REFERENCES `app` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP VIEW IF EXISTS `customerbasicinfo_view`;
CREATE VIEW `customerbasicinfo_view` AS select `customer`.`UserName` AS `Username`,`customer`.`FirstName` AS `FirstName`,`customer`.`MiddleName` AS `MiddleName`,`customer`.`LastName` AS `LastName`,concat(ifnull(`customer`.`FirstName`,''),' ',ifnull(`customer`.`MiddleName`,''),' ',ifnull(`customer`.`LastName`,'')) AS `Name`,`customer`.`Salutation` AS `Salutation`,`customer`.`id` AS `Customer_id`,concat('****',right(`customer`.`Ssn`,4)) AS `SSN`,`customer`.`createdts` AS `CustomerSince`,`customer`.`Gender` AS `Gender`,`customer`.`DateOfBirth` AS `DateOfBirth`,`customer`.`Status_id` AS `CustomerStatus_id`,`customerstatus`.`Description` AS `CustomerStatus_name`,`customer`.`MaritalStatus_id` AS `MaritalStatus_id`,`maritalstatus`.`Description` AS `MaritalStatus_name`,`customer`.`SpouseName` AS `SpouseName`,`customer`.`DrivingLicenseNumber` AS `DrivingLicenseNumber`,`customer`.`lockedOn` AS `lockedOn`,`customer`.`lockCount` AS `lockCount`,`customer`.`EmployementStatus_id` AS `EmployementStatus_id`,`employementstatus`.`Description` AS `EmployementStatus_name`,(select group_concat(`customerflagstatus`.`Status_id`,' ' separator ',') from `customerflagstatus` where (`customerflagstatus`.`Customer_id` = `customer`.`id`)) AS `CustomerFlag_ids`,(select group_concat(`status`.`Description`,' ' separator ',') from `status` where `status`.`id` in (select `customerflagstatus`.`Status_id` from `customerflagstatus` where (`customerflagstatus`.`Customer_id` = `customer`.`id`))) AS `CustomerFlag`,`customer`.`IsEnrolledForOlb` AS `IsEnrolledForOlb`,`customer`.`IsStaffMember` AS `IsStaffMember`,`customer`.`Location_id` AS `Branch_id`,`location`.`Name` AS `Branch_name`,`location`.`Code` AS `Branch_code`,`customer`.`IsOlbAllowed` AS `IsOlbAllowed`,`customer`.`IsAssistConsented` AS `IsAssistConsented`,`customer`.`isEagreementSigned` AS `isEagreementSigned`,`customer`.`CustomerType_id` AS `CustomerType_id`,`customertype`.`Name` AS `CustomerType_Name`,`customertype`.`Description` AS `CustomerType_Description`,(select `membergroup`.`Name` from `membergroup` where `membergroup`.`id` in (select `customergroup`.`Group_id` from `customergroup` where (`customer`.`id` = `customergroup`.`Customer_id`)) limit 1) AS `Customer_Role`,(select `membergroup`.`isEAgreementActive` from `membergroup` where `membergroup`.`id` in (select `customergroup`.`Group_id` from `customergroup` where (`customer`.`id` = `customergroup`.`Customer_id`)) limit 1) AS `isEAgreementRequired`,`customer`.`Organization_Id` AS `organisation_id`,`organisation`.`Name` AS `organisation_name`,any_value(`primaryphone`.`Value`) AS `PrimaryPhoneNumber`,any_value(`primaryemail`.`Value`) AS `PrimaryEmailAddress`,`customer`.`DocumentsSubmitted` AS `DocumentsSubmitted`,`customer`.`ApplicantChannel` AS `ApplicantChannel`,`customer`.`Product` AS `Product`,`customer`.`Reason` AS `Reason` from ((((((((`customer` left join `location` on((`customer`.`Location_id` = `location`.`id`))) left join `organisation` on((`customer`.`Organization_Id` = `organisation`.`id`))) left join `customertype` on((`customer`.`CustomerType_id` = `customertype`.`id`))) left join `status` `customerstatus` on((`customer`.`Status_id` = `customerstatus`.`id`))) left join `status` `maritalstatus` on((`customer`.`MaritalStatus_id` = `maritalstatus`.`id`))) left join `status` `employementstatus` on((`customer`.`EmployementStatus_id` = `employementstatus`.`id`))) left join `customercommunication` `primaryphone` on(((`primaryphone`.`Customer_id` = `customer`.`id`) and (`primaryphone`.`isPrimary` = 1) and (`primaryphone`.`Type_id` = 'COMM_TYPE_PHONE')))) left join `customercommunication` `primaryemail` on(((`primaryemail`.`Customer_id` = `customer`.`id`) and (`primaryemail`.`isPrimary` = 1) and (`primaryemail`.`Type_id` = 'COMM_TYPE_EMAIL')))) group by `customer`.`id`;

DROP PROCEDURE IF EXISTS `lead_notes_search_proc`;
DELIMITER $$
CREATE PROCEDURE `lead_notes_search_proc`(
IN _leadId varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _leadStatusId varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _sortOrder varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _sortCriteria varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _offset BIGINT,
IN _recordsPerPage BIGINT
)
BEGIN

SET @tableName="leadnote";
 
IF _leadStatusId = 'SID_ARCHIVED' THEN
	SET @tableName="archivedleadnote";
END IF;

SET @queryStatement =CONCAT( "
SELECT  ",@tableName,".`id` AS `id`,",
@tableName,".`lead_Id` AS `lead_Id`,",
@tableName,".`note` AS `note`,
`su1`.`FirstName` AS `createdByFirstName`,
`su1`.`MiddleName` AS `createdByMiddleName`,
`su1`.`LastName` AS `createdByLastName`,
`su2`.`FirstName` AS `modifiedbyFirstName`,
`su2`.`MiddleName` AS `modifiedbyMiddleName`,
`su2`.`LastName` AS `modifiedbyLastName`,",
@tableName,".`createdby` AS `createdby`,",
@tableName,".`modifiedby` AS `modifiedby`,",
@tableName,".`createdts` AS `createdts`,",
@tableName,".`lastmodifiedts` AS `lastmodifiedts`,",
@tableName,".`synctimestamp` AS `synctimestamp`,",
@tableName,".`softdeleteflag` AS `softdeleteflag`
FROM (`",@tableName,"`
        LEFT JOIN `systemuser` su1 ON `",@tableName,"`.`createdby` = `su1`.`id`
        LEFT JOIN `systemuser` su2 ON `",@tableName,"`.`modifiedby` = `su2`.`id`
 ) where true");

		IF _leadId != '' THEN
		SET @queryStatement = concat(@queryStatement," and ",@tableName,".lead_Id = ", quote(_leadId));
        END IF;
        
		IF _sortOrder != 'asc' OR  _sortOrder != 'desc' THEN
		SET _sortOrder = "desc";
		END IF;
		
        IF _sortCriteria = '' THEN
		SET _sortCriteria ="lastmodifiedts";
		END IF;
        SET @queryStatement = concat(@queryStatement," order by ",@tableName,".",_sortCriteria," ",_sortOrder);
		SET @queryStatement = concat(@queryStatement," LIMIT ",_recordsPerPage," OFFSET ",_offset);

		-- select @queryStatement;
		 PREPARE stmt FROM @queryStatement;
		 EXECUTE stmt;
		 DEALLOCATE PREPARE stmt;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `lead_auto_archive_proc`;
DELIMITER $$
CREATE PROCEDURE `lead_auto_archive_proc`()
BEGIN
#Proc to Faclitate Archiving and Purging of Leads.
#Author : Aditya Mankal (KH2322) 

#NOT Found Handlers - START
DECLARE lead_cursor_done BOOLEAN DEFAULT FALSE;
DECLARE lead_note_cursor_done BOOLEAN DEFAULT FALSE;  
#NOT Found Handlers - END

DECLARE curr_lead_id VARCHAR(50) DEFAULT '';
DECLARE curr_leadnote_id VARCHAR(50) DEFAULT '';
DECLARE lead_note_count INT(10) DEFAULT 0;
DECLARE lead_cursor CURSOR FOR SELECT id FROM lead WHERE TIMESTAMPDIFF(SQL_TSI_SECOND ,lastmodifiedts, now())>= ((select PropertyValue from systemconfiguration where PropertyName='LEAD_AUTO_ARCHIVE_PERIOD_IN_DAYS')*24*60*60);
DECLARE CONTINUE HANDLER FOR NOT FOUND SET lead_cursor_done = TRUE,lead_note_cursor_done=TRUE;

	OPEN lead_cursor;
	lead_loop:REPEAT 
	FETCH lead_cursor INTO curr_lead_id;
	IF NOT lead_cursor_done THEN
		INSERT INTO archivedlead SELECT *FROM lead where id=curr_lead_id;
		UPDATE archivedlead SET `Status_id`='SID_ARCHIVED',  `closureReason` = 'Automatically archived' WHERE id=curr_lead_id;
		SELECT curr_lead_id;
			
			LEADNOTEBLOCK: BEGIN
			  DECLARE leadnote_cursor CURSOR FOR SELECT id FROM leadnote WHERE lead_Id = curr_lead_id;
			    OPEN leadnote_cursor;
				   leadnote_loop:REPEAT 
					FETCH leadnote_cursor INTO curr_leadnote_id;
					IF NOT lead_note_cursor_done THEN
						INSERT INTO archivedleadnote SELECT *FROM leadnote where id = curr_leadnote_id;	
						DELETE FROM leadnote where id = curr_leadnote_id;
					END IF;	
				  UNTIL lead_note_cursor_done 
				  END REPEAT leadnote_loop;
				  SET lead_cursor_done=FALSE;
				  SET lead_note_cursor_done=FALSE;
				CLOSE leadnote_cursor;
			END LEADNOTEBLOCK;
			
		 DELETE FROM lead where id = curr_lead_id;
        END IF;
	UNTIL lead_cursor_done 
	END REPEAT lead_loop;
	SET lead_cursor_done=FALSE;
	SET lead_note_cursor_done=FALSE;
	CLOSE lead_cursor;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `lead_archive_proc`;
DELIMITER $$
CREATE PROCEDURE `lead_archive_proc`(
    IN _leadId varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _leadClosureReason varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _modifiedby varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
)
BEGIN
#Proc to Faclitate Archiving and Purging of a specifc lead.
#Author : Aditya Mankal (KH2322) 

#NOT Found Handlers - START
DECLARE lead_note_cursor_done BOOLEAN DEFAULT FALSE;  
#NOT Found Handlers - END

DECLARE curr_leadnote_id VARCHAR(50) DEFAULT '';
DECLARE lead_note_count INT(10) DEFAULT 0;
DECLARE CONTINUE HANDLER FOR NOT FOUND SET lead_note_cursor_done=TRUE;

  INSERT INTO archivedlead SELECT *FROM lead where id=_leadId;
	UPDATE archivedlead SET `Status_id`='SID_ARCHIVED' , `closureReason` = _leadClosureReason, `lastmodifiedts`= CURRENT_TIMESTAMP, `modifiedby` = _modifiedby WHERE id=_leadId;
	SELECT _leadId;
			
		LEADNOTEBLOCK: BEGIN
			DECLARE leadnote_cursor CURSOR FOR SELECT id FROM leadnote WHERE lead_Id = _leadId;
			   OPEN leadnote_cursor;
				   leadnote_loop:REPEAT 
					FETCH leadnote_cursor INTO curr_leadnote_id;
					IF NOT lead_note_cursor_done THEN
						INSERT INTO archivedleadnote SELECT *FROM leadnote where id = curr_leadnote_id;	
						DELETE FROM leadnote where id = curr_leadnote_id;
					END IF;	
				  UNTIL lead_note_cursor_done 
				END REPEAT leadnote_loop;
				SET lead_note_cursor_done=FALSE;
				CLOSE leadnote_cursor;
		END LEADNOTEBLOCK;	
		DELETE FROM lead where id = _leadId;
       
	SET lead_note_cursor_done=FALSE;
END$$
DELIMITER ;

DROP TABLE IF EXISTS `appversiontype`;
CREATE TABLE `appversiontype` (
  `id` varchar(50) NOT NULL,
  `description` varchar(300) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `appchannel`;
CREATE TABLE `appchannel` (
  `id` varchar(50) NOT NULL,
  `description` varchar(300) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `appversion`;
CREATE TABLE `appversion` (
  `id` varchar(50) NOT NULL,
  `version` varchar(50) NOT NULL,
  `versionType` varchar(50) NOT NULL DEFAULT 'MAJOR',
  `isSupported` tinyint(1) NOT NULL DEFAULT '1',
  `app` varchar(50) NOT NULL,
  `channel` varchar(50) NOT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `appversion_app_idx` (`app`),
  KEY `appversion_appchannel_idx` (`channel`),
  KEY `appversion_appversiontype_idx` (`versionType`),
  CONSTRAINT `appversion_app` FOREIGN KEY (`app`) REFERENCES `app` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `appversion_appchannel` FOREIGN KEY (`channel`) REFERENCES `appchannel` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `appversion_appversiontype` FOREIGN KEY (`versionType`) REFERENCES `appversiontype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DELIMITER $$
CREATE PROCEDURE `lead_assign_proc`(
IN _leadIds  varchar(10000) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _csrID  varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _modifiedby varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
)
BEGIN
	SET @updateStatement = CONCAT("UPDATE lead SET csr_id = '",_csrID,"', modifiedby = '",_modifiedby,"', lastmodifiedts = CURRENT_TIMESTAMP where id in (",func_escape_input_for_in_operator(_leadIds),")");
	PREPARE stmt FROM @updateStatement;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END$$
DELIMITER ;

DROP TABLE IF EXISTS `userrolecustomerrole`;
CREATE TABLE `userrolecustomerrole` (
  `UserRole_id` varchar(50) NOT NULL,
  `CustomerRole_id` varchar(50) NOT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`UserRole_id`,`CustomerRole_id`),
  KEY `userrolecustomerrole_customerrole_id_idx` (`CustomerRole_id`),
  KEY `userrolecustomerrole_userrole_id_idx` (`UserRole_id`),
  CONSTRAINT `userrolecustomerrole_customerrole_id` FOREIGN KEY (`CustomerRole_id`) REFERENCES `membergroup` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `userrolecustomerrole_userrole_id` FOREIGN KEY (`UserRole_id`) REFERENCES `role` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DELIMITER $$
CREATE PROCEDURE `internal_user_access_on_given_customer_proc`(
IN _loggedinSystemUserId  varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _customerId  varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _customerUsername  varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
)
BEGIN

DECLARE isCustomerAccessible VARCHAR(6);
if(_customerId = '' and _customerUsername != '') THEN
	SET _customerId = (SELECT id from customer where UserName = _customerUsername);
END IF;

SET isCustomerAccessible = (_customerId in (SELECT customergroup.Customer_id FROM systemuser 
LEFT JOIN userrole ON (userrole.User_id = systemuser.id) 
LEFT JOIN userrolecustomerrole ON (userrolecustomerrole.UserRole_id = userrole.Role_id)
LEFT JOIN customergroup ON (customergroup.Group_id = userrolecustomerrole.CustomerRole_id)
WHERE id = _loggedinSystemUserId));

IF ISNULL(isCustomerAccessible) or isCustomerAccessible = 0 THEN
	SET isCustomerAccessible = 'false';
ELSEIF(isCustomerAccessible = 1) THEN
	SET isCustomerAccessible = 'true';
END IF;

SELECT isCustomerAccessible;
END$$
DELIMITER ;

DROP VIEW IF EXISTS `internal_role_to_customer_role_mapping_view`;
CREATE VIEW `internal_role_to_customer_role_mapping_view` AS select `membergroup`.`id` AS `CustomerRole_id`,`membergroup`.`Name` AS `CustomerRole_Name`,`membergroup`.`Description` AS `CustomerRole_Description`,`membergroup`.`Type_id` AS `CustomerRole_Type_id`,`membergroup`.`Status_id` AS `CustomerRole_Status_id`,`role`.`id` AS `InternalRole_id`,`role`.`Type_id` AS `InternalRole_Type_id`,`role`.`Status_id` AS `InternalRole_Status_id`,`role`.`Name` AS `InternalRole_Name`,`role`.`Description` AS `InternalRole_Description` from ((`userrolecustomerrole` left join `membergroup` on((`membergroup`.`id` = `userrolecustomerrole`.`CustomerRole_id`))) left join `role` on((`role`.`id` = `userrolecustomerrole`.`UserRole_id`)));

DROP PROCEDURE IF EXISTS `customer_search_proc`;
DELIMITER $$
CREATE PROCEDURE `customer_search_proc`( 
in _searchType varchar(60),
in _id varchar(50), 
in _name varchar(50),  
in _SSN varchar(50),
in _username varchar(50), 
in _phone varchar(100), 
in _email varchar(100),
in _IsStaffMember varchar(10),
in _cardorAccountnumber varchar(50),
in _TIN varchar(50),
in _group varchar(40), 
in _IDType varchar(50),
in _IDValue varchar(50),
in _companyId varchar(50),
in _requestID varchar(50),
in _branchIDS varchar(2000), 
in _productIDS varchar(2000), 
in _cityIDS varchar(2000), 
in _entitlementIDS varchar(2000), 
in _groupIDS varchar(2000), 
in _customerStatus varchar(50), 
in _before varchar(20), 
in _after varchar(20), 
in _sortVariable varchar(100), 
in _sortDirection varchar(4),  
in _pageOffset bigint, 
in _pageSize bigint)
BEGIN
	
    DECLARE _maxLockCount varchar(50);

	IF _searchType LIKE 'GROUP_SEARCH%' then
		set @search_select_statement = "SELECT 
					customer.id, customer.FirstName, customer.MiddleName, customer.LastName,
					concat(IFNULL(customer.FirstName,''), ' ', IFNULL(customer.MiddleName,''), ' ', IFNULL(customer.LastName,'')) as name,
					customer.UserName as Username, customer.Salutation, customer.Gender, customer.IsStaffMember,
					customer.modifiedby, customer.lastmodifiedts, customer.Status_id, PrimaryEmail.value AS PrimaryEmail,
					GROUP_CONCAT(customergroup.Group_id) as assigned_group_ids,
					address.City_id, city.Name as City_name,
					customer.Location_id AS branch_id,
					location.Name AS branch_name";
		set @search_count_statement = "SELECT count(distinct customer.id) as SearchMatchs";
		set @queryStatement = concat("
				FROM customer
                    JOIN (
						SELECT
							customer.id
						FROM
							customer ",
							IF( _groupIDS != '' OR _entitlementIDS != '', " LEFT JOIN customergroup ON (customergroup.Customer_id=customer.id)", ""),
							IF( _cityIDS != '', " LEFT JOIN customeraddress ON (customer.id=customeraddress.Customer_id AND customeraddress.isPrimary=1 and customeraddress.Type_id='ADR_TYPE_HOME')
							LEFT JOIN address ON (customeraddress.Address_id = address.id) ", ""),

							IF( _entitlementIDS != '', " LEFT JOIN customerentitlement ON (customerentitlement.Customer_id=customer.id) ", ""),
							IF( _productIDS != '', " LEFT JOIN customerproduct ON (customerproduct.Customer_id=customer.id) ", ""));
			set @whereclause = "WHERE true";
			
            IF _username != "" then
				-- customer name
				set @whereclause = concat(@whereclause, " AND (customer.firstname like concat(",quote(_username),",'%')");
				
				-- customer username
				set @whereclause = concat(@whereclause," OR customer.username like concat(",quote(_username)," ,'%')");
				
				-- customer id
				set @whereclause = concat(@whereclause," OR customer.id like concat(", quote(_username) ,",'%')) ");
			end if;
            
            -- Is staff member
			if _IsStaffMember != "" then
				if _IsStaffMember = "true" then
					set @whereclause = concat(@whereclause," AND customer.IsStaffMember = '1'");
				else
					set @whereclause = concat(@whereclause," AND customer.IsStaffMember = '0'");
				end if;
			end if;
            
			-- customer entitlement ids
			if _entitlementIDS != "" then
				set @whereclause = concat(@whereclause," AND (customerentitlement.Service_id in (",func_escape_input_for_in_operator(_entitlementIDS),") 
							OR customergroup.Group_id in ( select Group_id from groupentitlement where Service_id in (",func_escape_input_for_in_operator(_entitlementIDS)," )))");
			end if;
			
			-- customer group ids
			if _groupIDS != "" then
				set @whereclause = concat(@whereclause," AND customergroup.Group_id in (",func_escape_input_for_in_operator(_groupIDS),") ");
			end if;
			
			-- customer product ids
			if _productIDS != "" then
				set @whereclause = concat(@whereclause," AND customerproduct.Product_id in (",func_escape_input_for_in_operator(_productIDS),")");
			end if;
			
			-- customer branch ids
			if _branchIDS != "" then
				set @whereclause = concat(@whereclause, " AND customer.Location_id in (",func_escape_input_for_in_operator(_branchIDS),")");
			end if;
			
			-- customer status
			if _customerStatus != "" then
				set @whereclause = concat(@whereclause, " AND customer.Status_id = ", quote(_customerStatus));
			end if;
			
			-- customer city ids
			if _cityIDS != "" then
				set @whereclause = concat(@whereclause, " AND address.City_id in (",func_escape_input_for_in_operator(_cityIDS),")");
			end if;
			
			-- customer date range search
			if _before != "" and _after != "" then
				set @whereclause = concat(@whereclause, " AND date(customer.createdts) >= date ", quote(_before) ," and date(customer.createdts) <= date ", quote(_after));
			else 
				if _before != "" then
					set @whereclause = concat(@whereclause, " AND date(customer.createdts) <= date ", quote(_before));
				elseif _after != "" then
					set @whereclause = concat(@whereclause, " AND date(customer.createdts) >= date ", quote(_after));
				end if;
			end if;
			
			set @queryStatement = concat(@queryStatement,@whereclause,
            ") paginatedCustomers ON (paginatedCustomers.id=customer.id)
					
					LEFT JOIN customercommunication PrimaryEmail ON (PrimaryEmail.Customer_id=paginatedCustomers.id AND PrimaryEmail.isPrimary=1 AND PrimaryEmail.Type_id='COMM_TYPE_EMAIL')
					LEFT JOIN customergroup ON (customergroup.Customer_id=paginatedCustomers.id)
					LEFT JOIN customeraddress ON (paginatedCustomers.id=customeraddress.Customer_id AND customeraddress.isPrimary=1 and customeraddress.Type_id='ADR_TYPE_HOME')
					LEFT JOIN address ON (customeraddress.Address_id = address.id)
					LEFT JOIN city ON (city.id = address.City_id)
					LEFT JOIN location ON (location.id=customer.Location_id)  
			");
			
			IF _searchType = 'GROUP_SEARCH' THEN

				set @queryStatement = concat(@search_select_statement, @queryStatement, " group by paginatedCustomers.id ");
				IF _sortVariable = "DEFAULT" OR _sortVariable = "" THEN
					set @queryStatement = concat(@queryStatement, " ORDER BY FirstName");
	            ELSEIF _sortVariable != "" THEN
					set @queryStatement = concat(@queryStatement, " ORDER BY ",_sortVariable);
	            end if;
	            IF _sortDirection != "" THEN
					set @queryStatement = concat(@queryStatement, " ",_sortDirection);
	            end if;
	            set @queryStatement = concat(@queryStatement, " LIMIT ",_pageOffset,",",_pageSize);

			ELSEIF _searchType = 'GROUP_SEARCH_TOTAL_COUNT' then
				set @queryStatement = concat(@search_count_statement, @queryStatement);
			END IF;


	ELSEIF _searchType LIKE 'CUSTOMER_SEARCH%' then
			
			SELECT accountLockoutThreshold from passwordlockoutsettings where id='PLOCKID1' INTO _maxLockCount;
            
            SET @search_select_statement = concat("SELECT customer.id, customer.FirstName, customer.MiddleName, customer.LastName,customer.DateOfBirth,
				concat(IFNULL(customer.FirstName,''), ' ', IFNULL(customer.MiddleName,''), ' ', IFNULL(customer.LastName,'')) as name,
				customer.UserName as Username, customer.Salutation, customer.Gender,CONCAT('****', RIGHT(customer.Ssn, 4)) as Ssn,
                customer.CustomerType_id as CustomerTypeId, company.id as CompanyId, company.Name as CompanyName,
                IF(IFNULL(customer.lockCount,0) >= ",_maxLockCount,", 'SID_CUS_LOCKED',customer.Status_id) as Status_id,
				PrimaryPhone.value AS PrimaryPhoneNumber,
				PrimaryEmail.value AS PrimaryEmailAddress,
                GROUP_CONCAT(membergroup.Name) as groups,
                customer.ApplicantChannel, customer.createdts");
            SET @search_count_statement = "SELECT count(distinct customer.id) as SearchMatchs";
			SET @queryStatement = concat("
			FROM customer
			JOIN (
				SELECT
					customer.id
				FROM customer ",
					IF( _phone != '', " JOIN customercommunication PrimaryPhone ON (PrimaryPhone.Customer_id=customer.id AND PrimaryPhone.isPrimary=1 AND PrimaryPhone.Type_id='COMM_TYPE_PHONE') ",""),
					IF(_email != '', "  JOIN customercommunication PrimaryEmail ON (PrimaryEmail.Customer_id=customer.id AND PrimaryEmail.isPrimary=1 AND PrimaryEmail.Type_id='COMM_TYPE_EMAIL') ",""),
                    IF( _TIN != '', " LEFT JOIN organisationmembership ON (customer.Organization_id = organisationmembership.Organization_id)", ""),
                    IF( _cardorAccountnumber != '', " LEFT JOIN card ON (customer.id = card.User_id)
                    LEFT JOIN accounts ON (customer.id = accounts.User_id)
                    LEFT JOIN customeraccounts ON (customer.id = customeraccounts.Customer_id)", ""));

                SET @queryStatement = concat(@queryStatement," WHERE true");
                
                IF _id != '' THEN
					set @queryStatement = concat(@queryStatement," and customer.id = ", quote(_id));
                end if;
                
                 IF _name != '' THEN
					set @queryStatement = concat(@queryStatement," and customer.FirstName like concat(", quote(_name),",'%')");
                end if;

                IF _SSN != '' THEN
					set @queryStatement = concat(@queryStatement," and customer.Ssn = ",quote(_SSN));
                end if;

                IF _username != '' THEN
					set @queryStatement = concat(@queryStatement," and customer.username = ",quote(_username));
                end if;

                IF _phone != '' THEN
					IF length(_phone) > 9 THEN
						set @queryStatement = concat(@queryStatement," and PrimaryPhone.value like concat('%',",quote(_phone),",'%')");
                    ELSE
						set @queryStatement = concat(@queryStatement," and PrimaryPhone.value = ",quote(_phone));
					end if;
                end if;

                IF _email != '' THEN
					set @queryStatement = concat(@queryStatement," and PrimaryEmail.value = ",quote(_email));
                end if;

                IF _companyId != '' THEN
					set @queryStatement = concat(@queryStatement," and customer.Organization_id = ",quote(_companyId));
                end if;

				IF _IDValue != '' THEN
                    IF _IDType = 'ID_DRIVING_LICENSE' THEN
						set @queryStatement = concat(@queryStatement," and (customer.DrivingLicenseNumber = ",quote(_IDValue)," or 
                        (customer.IDType_id = ",quote(_IDType)," and customer.IDValue = ",quote(_IDValue),"))");
                    ELSE
						set @queryStatement = concat(@queryStatement," and (customer.IDType_id = ",quote(_IDType)," and customer.IDValue = ",quote(_IDValue),")");
					end if;
                end if;
                
				IF _TIN != '' THEN
					set @queryStatement = concat(@queryStatement," and organisationmembership.Taxid = ",quote(_TIN));
                end if;
                
                IF _cardorAccountnumber != '' THEN
					set @queryStatement = concat(@queryStatement," and (card.cardNumber = ",quote(_cardorAccountnumber),
                    " or accounts.Account_id = ",quote(_cardorAccountnumber)," or customeraccounts.Account_id = ",quote(_cardorAccountnumber),")");
                end if;
                
                set @queryStatement = concat(@queryStatement,
                ") paginatedCustomers ON (paginatedCustomers.id=customer.id)
			LEFT JOIN customercommunication PrimaryPhone ON (PrimaryPhone.Customer_id=paginatedCustomers.id AND PrimaryPhone.isPrimary=1 AND PrimaryPhone.Type_id='COMM_TYPE_PHONE')
			LEFT JOIN customercommunication PrimaryEmail ON (PrimaryEmail.Customer_id=paginatedCustomers.id AND PrimaryEmail.isPrimary=1 AND PrimaryEmail.Type_id='COMM_TYPE_EMAIL')
			LEFT JOIN customergroup ON (customergroup.Customer_id=paginatedCustomers.id)
			LEFT JOIN membergroup ON (membergroup.id=customergroup.group_id)
            LEFT JOIN organisation company ON (customer.Organization_id = company.id)");

            IF _searchType = 'CUSTOMER_SEARCH' THEN

            	set @queryStatement = concat(@search_select_statement, @queryStatement, " group by paginatedCustomers.id ");
            	IF _sortVariable = "DEFAULT" OR _sortVariable = "" THEN
					set @queryStatement = concat(@queryStatement, " ORDER BY FirstName");
	            ELSEIF _sortVariable != "" THEN
					set @queryStatement = concat(@queryStatement, " ORDER BY ",_sortVariable);
	            end if;
	            IF _sortDirection != "" THEN
					set @queryStatement = concat(@queryStatement, " ",_sortDirection);
	            end if;
	            set @queryStatement = concat(@queryStatement, " LIMIT ",_pageOffset,",",_pageSize);

            ELSEIF _searchType = 'CUSTOMER_SEARCH_TOTAL_COUNT' THEN
            	set @queryStatement = concat(@search_count_statement, @queryStatement);
            END IF;
	END IF;

	-- select @queryStatement;
	PREPARE stmt FROM @queryStatement; EXECUTE stmt; DEALLOCATE PREPARE stmt;

END$$
DELIMITER ;


ALTER TABLE `systemuser` 
CHANGE COLUMN `LastPasswordChangedts` `LastPasswordChangedts` TIMESTAMP NULL ;

DROP PROCEDURE IF EXISTS `systemuser_permission_proc`;
DELIMITER $$
CREATE PROCEDURE `systemuser_permission_proc`(
IN _userId varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci)
BEGIN
 SELECT 
        `up`.`Permission_id` AS `id`,
        `p`.`Name` AS `name`,
        `p`.`Status_id` AS `status`,
        `p`.`PermissionValue` as `PermissionValue`,
		`p`.`softdeleteflag` AS `softdeleteflag`
    FROM
        (`userpermission` `up`
        JOIN `permission` `p` ON (`up`.`Permission_id` = `p`.`id`)) where  `up`.`User_id`= _userId 
    UNION SELECT 
	    `p`.`id` AS `id`,
        `p`.`Name` AS `name`,
        `p`.`Status_id` AS `status`,
		`p`.`PermissionValue` as `PermissionValue`,
		`p`.`softdeleteflag` AS `softdeleteflag`
    FROM
        (`userrole` `ur` JOIN `role` `r` ON (`r`.`id` = `ur`.`Role_id`)
        JOIN `rolepermission` `rp` ON (`ur`.`Role_id` = `rp`.`Role_id`)
        JOIN `permission` `p` ON (`rp`.`Permission_id` = `p`.`id`)) where `ur`.`User_id`= _userId ;
END$$
DELIMITER ;

ALTER TABLE `compositepermission` 
ADD COLUMN `Entitlement_id` VARCHAR(50) NULL DEFAULT NULL AFTER `Description`,
CHANGE COLUMN `Name` `Name` VARCHAR(50) NULL DEFAULT NULL ;

ALTER TABLE `compositepermission` 
ADD INDEX `FK_CompositePermission_Entitlement_idx` (`Entitlement_id` ASC);

ALTER TABLE `compositepermission` 
ADD CONSTRAINT `FK_CompositePermission_Entitlement`
  FOREIGN KEY (`Entitlement_id`)
  REFERENCES `service` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

DROP VIEW IF EXISTS `composite_permissions_view`;
CREATE VIEW `composite_permissions_view` AS select `compositepermission`.`id` AS `id`,if(isnull(`compositepermission`.`Entitlement_id`),`compositepermission`.`Name`,`service`.`Name`) AS `Name`,if(isnull(`compositepermission`.`Entitlement_id`),`compositepermission`.`Description`,`service`.`Description`) AS `Description`,`compositepermission`.`Entitlement_id` AS `Service_id`,`compositepermission`.`Permission_id` AS `Permission_id`,`compositepermission`.`isEnabled` AS `isEnabled`,`compositepermission`.`createdby` AS `createdby`,`compositepermission`.`modifiedby` AS `modifiedby`,`compositepermission`.`createdts` AS `createdts`,`compositepermission`.`lastmodifiedts` AS `lastmodifiedts`,`compositepermission`.`synctimestamp` AS `synctimestamp`,`compositepermission`.`softdeleteflag` AS `softdeleteflag` from (`compositepermission` left join `service` on((`service`.`id` = `compositepermission`.`Entitlement_id`)));

ALTER TABLE `customer` 
ADD INDEX `IDX_Customer_createdts` (`createdts` ASC),
ADD INDEX `IDX_Customer_firstname` (`FirstName` ASC);

ALTER TABLE `customer` 
ADD INDEX `IDX_Customer_SSN` (`Ssn` ASC),
ADD INDEX `IDX_Customer_DrivingLicense` (`DrivingLicenseNumber` ASC),
ADD INDEX `IDX_Customer_IDValue` (`IDValue` ASC),
ADD INDEX `IDX_Customer_Organization_id` (`Organization_Id` ASC);

ALTER TABLE `status` 
ADD INDEX `IDX_Status_StatusDescription` (`Description` ASC);

ALTER TABLE `location` 
ADD INDEX `IDX_Location_LocationName` (`Name` ASC);

ALTER TABLE `city` 
ADD INDEX `IDX_City_Name` (`Name` ASC);

ALTER TABLE `customercommunication` 
ADD INDEX `IDX_CustomerCommunication_Value` (`Value` ASC);

ALTER TABLE `organisationmembership` 
ADD INDEX `IDX_OrganisationMembership_Taxid` (`Taxid` ASC);

ALTER TABLE `card` 
ADD INDEX `IDX_Card_User_id` (`User_id` ASC);

ALTER TABLE `accounts` 
ADD INDEX `IDX_accounts_User_id` (`User_id` ASC);

ALTER TABLE `organisationmembership` 
ADD INDEX `IDX_OrganizationMembership_Organization_id` (`Organization_id` ASC);

DROP PROCEDURE IF EXISTS `DMS_create_user_proc`;
DELIMITER $$
CREATE PROCEDURE `DMS_create_user_proc`(
  in _username varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci, 
  in _group varchar(50)
)
BEGIN DECLARE customerId TEXT(2000);
DECLARE Id TEXT(2000);
DECLARE dmsaddressId VARCHAR(50);
DECLARE tempDayName TEXT(2000);
DECLARE tempStartTime TEXT(2000);
DECLARE tempEndTime TEXT(2000);
DECLARE tempServiceName TEXT(2000);
DECLARE servicesList TEXT(10000);
DECLARE serviceName TEXT(2000);
DECLARE b int(1);
DECLARE pipeFlag int(1);
DECLARE addressId TEXT(2000);
DECLARE customerCommId TEXT(2000);
DECLARE customerReqId TEXT(2000);
DECLARE reqMsgId TEXT(2000);
DECLARE LogID Text(2000);

SELECT customer.id from customer where customer.UserName = _username into customerId ;
IF customerId is null then
	SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)) * 10) INTO customerId;
    INSERT INTO `customer` (
	  `id`, `FirstName`, `MiddleName`, 
	  `LastName`, `UserName`, `Salutation`, 
	  `Gender`, `DateOfBirth`, `Status_id`, 
	  `Ssn`, `MaritalStatus_id`, `SpouseName`, 
	  `EmployementStatus_id`, `IsEnrolledForOlb`, 
	  `IsStaffMember`, `Location_id`, 
	  `PreferredContactMethod`, `PreferredContactTime`, 
	  `createdby`, `modifiedby`
	) 
	VALUES 
	(
		customerId, 'John', '', 
		'Bailey', _username, 'Mr.', 'Male', 
		'1999-11-11', 'SID_CUS_ACTIVE', 
		'123456789', 'SID_SINGLE', '', 'SID_EMPLOYED', 
		'1', '0', 'LID1', 'Call', 'Morning, Afternoon', 
		'Kony User', 'Kony Dev'
	  );
      set customerCommId = concat("CID", customerId);
	INSERT INTO `customercommunication` (
	  `id`, `Type_id`, `Customer_id`, `isPrimary`, 
	  `Value`, `Extension`, `Description`, 
	  `createdby`, `modifiedby`
	) 
	VALUES 
	  (
		customerCommId, 'COMM_TYPE_EMAIL', 
		customerId, '1', 'john.bailey@yahoo.com', 
		'Personal', 'NULL', 'Kony User', 
		'Kony Dev'
	  );
	SELECT SYSDATE() + 2 INTO customerCommId;
	set customerCommId = concat("CID", customerCommId);
	INSERT INTO `customercommunication` (
	  `id`, `Type_id`, `Customer_id`, `isPrimary`, 
	  `Value`, `Extension`, `Description`, 
	  `createdby`, `modifiedby`
	) 
	VALUES 
	  (
		customerCommId, 'COMM_TYPE_PHONE', 
		customerId, '1', '8729899218', 'Personal', 
		'NULL', 'Kony User', 'Kony Dev'
	  );
  
end if;

SELECT concat('Addr1',ROUND(UNIX_TIMESTAMP(CURTIME(4)) * 10000)) into dmsaddressId;
INSERT INTO `address` (`id`, `Region_id`, `City_id`, `addressLine1`,`addressLine2`, `zipCode`, `latitude`, `logitude`, `createdby`, `modifiedby`)
 VALUES (dmsaddressId , 'R57', 'CITY1318', '9225 Bee Caves Rd #300','Fusce Rd.', '20620', '40.7128', '74.0060', 'Kony User', 'Kony Dev');

INSERT INTO `customeraddress` (
  `Customer_id`, `Address_id`, `Type_id`, 
  `isPrimary`, `createdby`, `modifiedby`
) 
VALUES 
  (
    customerId, dmsaddressId, 'ADR_TYPE_WORK', 
    '0', 'Kony User', 'Kony Dev'
  );
  
  SELECT concat('Addr2',ROUND(UNIX_TIMESTAMP(CURTIME(4)) * 10000)) into dmsaddressId;
INSERT INTO `address` (`id`, `Region_id`, `City_id`, `addressLine1`,`addressLine2`, `zipCode`, `latitude`, `logitude`, `createdby`, `modifiedby`)
 VALUES (dmsaddressId , 'R52', 'CITY1289', 'P.O. Box 283 8562','Sit Rd.', '20645', '40.7128', '74.0060', 'Kony User', 'Kony Dev');

INSERT INTO `customeraddress` (
  `Customer_id`, `Address_id`, `Type_id`, 
  `isPrimary`, `createdby`, `modifiedby`
) 
VALUES 
  (
    customerId, dmsaddressId, 'ADR_TYPE_HOME', 
    '1', 'Kony User', 'Kony Dev'
  );

INSERT INTO `customergroup` (
  `Customer_id`, `Group_id`, `createdby`, 
  `modifiedby`, `softdeleteflag`
) 
VALUES 
  (
    customerId, _group, 'Kony User', 
    'Kony Dev', '0'
  );
INSERT INTO `customerproduct` (
  `Customer_id`, `Product_id`, `createdby`, 
  `modifiedby`, `softdeleteflag`
) 
VALUES 
  (
    customerId, 'PRODUCT2', 'Kony User', 
    'Kony Dev', '0'
  );
INSERT INTO `customerproduct` (
  `Customer_id`, `Product_id`, `createdby`, 
  `modifiedby`, `softdeleteflag`
) 
VALUES 
  (
    customerId, 'PRODUCT4', 'Kony User', 
    'Kony Dev', '0'
  );
INSERT INTO `customerproduct` (
  `Customer_id`, `Product_id`, `createdby`, 
  `modifiedby`, `softdeleteflag`
) 
VALUES 
  (
    customerId, 'PRODUCT7', 'Kony User', 
    'Kony Dev', '0'
  );
INSERT INTO `customerproduct` (
  `Customer_id`, `Product_id`, `createdby`, 
  `modifiedby`, `softdeleteflag`
) 
VALUES 
  (
    customerId, 'PRODUCT14', 'Kony User', 
    'Kony Dev', '0'
  );
INSERT INTO `customerproduct` (
  `Customer_id`, `Product_id`, `createdby`, 
  `modifiedby`, `softdeleteflag`
) 
VALUES 
  (
    customerId, 'PRODUCT10', 'Kony User', 
    'Kony Dev', '0'
  );
set 
  customerReqId = concat("REC", customerId);
  
  
INSERT INTO `customerrequest` (
  `id`, `RequestCategory_id`, `Customer_id`, 
  `Priority`, `Status_id`, `RequestSubject`, 
  `AssignedTo`, `Accountid`, `createdby`, 
  `modifiedby`, `softdeleteflag`
) 
VALUES 
  (
    customerReqId, 'RCID_DEPOSITS', customerId, 
    'HIGH', 'SID_OPEN', 'Communication information change', 
    null, '090871', 'KonyUser', 'KonyDev', 
    '0'
  );
set 
  reqMsgId = concat("MSG", customerId);
INSERT INTO `requestmessage` (
  `id`, `CustomerRequest_id`, `MessageDescription`, 
  `RepliedBy`, `RepliedBy_id`, `RepliedBy_Name`,`ReplySequence`, `IsRead`, 
  `createdby`, `modifiedby`, `softdeleteflag`
) 
VALUES 
  (
    reqMsgId, customerReqId, 'What is the expected resolution date?', 
    'CUSTOMER', customerId, 'John bailey', '1', 'TRUE', 'KonyUser', 'konyolbuser', 
    '0'
  );
set 
  reqMsgId = concat("MSG", customerId + 1);
INSERT INTO `requestmessage` (
  `id`, `CustomerRequest_id`, `MessageDescription`, 
  `RepliedBy`, `RepliedBy_id`, `RepliedBy_Name`,`ReplySequence`, `IsRead`, 
  `createdby`, `modifiedby`, `softdeleteflag`
) 
VALUES 
  (
    reqMsgId, customerReqId, 'Can you escalate the request?', 
    'CUSTOMER', customerId, 'John bailey', '2', 'TRUE', 'KonyUser', 'konyolbuser', 
    '0'
  );
set 
  reqMsgId = concat("MSG", customerId + 2);
INSERT INTO `requestmessage` (
  `id`, `CustomerRequest_id`, `MessageDescription`, 
  `RepliedBy`, `RepliedBy_id`, `RepliedBy_Name`,`ReplySequence`, `IsRead`, 
  `createdby`, `modifiedby`, `softdeleteflag`
) 
VALUES 
  (
    reqMsgId, customerReqId, 'Share the current status of the request', 
    'CUSTOMER', customerId, 'John bailey', '3', 'TRUE', 'KonyUser', 'konyolbuser', 
    '0'
  );
set 
  customerReqId = concat("REC", customerId + 1);
INSERT INTO `customerrequest` (
  `id`, `RequestCategory_id`, `Customer_id`, 
  `Priority`, `Status_id`, `RequestSubject`, 
  `AssignedTo`, `Accountid`, `lastupdatedbycustomer`, 
  `createdby`, `createdts`, `lastmodifiedts`, 
  `synctimestamp`, `softdeleteflag`
) 
VALUES 
  (
    customerReqId, 'RCID_CREDITCARD', 
    customerId, 'High', 'SID_INPROGRESS', 
    'Bill Payment Failed', 'UID10', 
    '1234', '1', 'admin2', CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 
    '0'
  );
set 
  reqMsgId = concat("MSG", customerId + 3);
INSERT INTO `requestmessage` (
  `id`, `CustomerRequest_id`, `MessageDescription`, 
  `RepliedBy`,  `RepliedBy_id`, `RepliedBy_Name`,`ReplySequence`, `IsRead`, 
  `createdby`, `modifiedby`, `createdts`, 
  `lastmodifiedts`, `synctimestamp`, 
  `softdeleteflag`
) 
VALUES 
  (
    reqMsgId, customerReqId, '<p><span style=\"color: #000000;\">Hi,</span></p>\n<p><span style=\"color: #000000;\">&nbsp;</span></p>\n<p><span style=\"color: #000000;\">I had scheduled a bill payment to my internet provider- AT&amp;T for $25, this Monday but the payment failed. Can you help me out?</span></p>\n<p><span style=\"color: #000000;\">&nbsp;</span></p>\n<p><span style=\"font-size: 11.0pt; font-family: \'Calibri\',sans-serif; color: #000000;\">Thanks </span></p>', 
    'CUSTOMER', customerId, 'John bailey', '1', 'TRUE', 'jane.doe', 
    'jane.doe', CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 
    '0'
  );
set 
  reqMsgId = concat("MSG", customerId + 4);
INSERT INTO `requestmessage` (
  `id`, `CustomerRequest_id`, `MessageDescription`, 
  `RepliedBy`,  `RepliedBy_id`, `RepliedBy_Name`,`ReplySequence`, `IsRead`, 
  `createdby`, `modifiedby`, `createdts`, 
  `lastmodifiedts`, `synctimestamp`, 
  `softdeleteflag`
) 
VALUES 
  (
    reqMsgId, customerReqId, '<p><span style=\"color: #000000;\">Dear Customer,</span></p>\n<p><span style=\"color: #000000;\">&nbsp;</span></p>\n<p><span style=\"color: #000000;\">Thanks for writing to us. We can see that the payment failed because you had insufficient funds in your account on Monday, 19<sup>th</sup> Feb 2018. You seem to have the required balance at the moment. You can try making this payment now. </span></p>\n<p><span style=\"color: #000000;\">&nbsp;</span></p>\n<p><span style=\"color: #000000;\">Do let us know in case you face any other issues. </span></p>\n<p><span style=\"color: #000000;\">&nbsp;</span></p>\n<p><span style=\"color: #000000;\">Thanks,</span></p>\n<p><span style=\"color: #000000;\">Richard</span></p>\n<p><span style=\"font-size: 11.0pt; font-family: \'Calibri\',sans-serif; color: #000000;\">Kony Bank, Customer Service Team</span></p>', 
    'ADMIN|CSR', 'UID10', 'admin', '2', 'TRUE', 'admin1', 'admin1', 
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, '0'
  );
set 
  reqMsgId = concat("MSG", customerId + 5);
INSERT INTO `requestmessage` (
  `id`, `CustomerRequest_id`, `MessageDescription`, 
  `RepliedBy`,  `RepliedBy_id`, `RepliedBy_Name`,`ReplySequence`, `IsRead`, 
  `createdby`, `modifiedby`, `createdts`, 
  `lastmodifiedts`, `synctimestamp`, 
  `softdeleteflag`
) 
VALUES 
  (
    reqMsgId, customerReqId, '<p><span style=\"color: #000000;\">Hi Richard,</span></p><p><span style=\"color: #000000;\">&nbsp;</span></p><p><span style=\"color: #000000;\">Thanks for letting me know. I will go ahead with the payment now. </span></p><p><span style=\"color: #000000;\">&nbsp;</span></p><p><span style=\"font-size: 11.0pt; font-family: \'Calibri\',sans-serif; color: #000000;\">Thanks</span></p>', 
     'CUSTOMER', customerId, 'John bailey', '3', 'TRUE', 'jane.doe', 
    'jane.doe', CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 
    '0'
  );
set 
  customerReqId = concat("REC", customerId + 2);
INSERT INTO `customerrequest` (
  `id`, `RequestCategory_id`, `Customer_id`, 
  `Priority`, `Status_id`, `RequestSubject`, 
  `AssignedTo`, `Accountid`, `lastupdatedbycustomer`, 
  `createdby`, `createdts`, `lastmodifiedts`, 
  `synctimestamp`, `softdeleteflag`
) 
VALUES 
  (
    customerReqId, 'RCID_CREDITCARD', 
    customerId, 'High', 'SID_INPROGRESS', 
    'International Transactions on Credit Card', 
    'UID10', '1234', '0', 'admin2', 
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, '0'
  );
set 
  reqMsgId = concat("MSG", customerId + 6);
INSERT INTO `requestmessage` (
  `id`, `CustomerRequest_id`, `MessageDescription`, 
  `RepliedBy`, `RepliedBy_id`, `RepliedBy_Name`,`ReplySequence`, `IsRead`, 
  `createdby`, `modifiedby`, `createdts`, 
  `lastmodifiedts`, `synctimestamp`, 
  `softdeleteflag`
) 
VALUES 
  (
    reqMsgId, customerReqId, ' <p><span style=\"color: #000000;\">Dear Customer,</span></p> <p><span style=\"color: #000000;\">&nbsp;</span></p> <p><span style=\"color: #000000;\">Thank you for informing us about your travel plans. Your card is already enabled for international transactions in Australia. You can use the card at all POS transaction points hassle free. Have a great trip!</span></p> <p><span style=\"color: #000000;\">&nbsp;</span></p> <p><span style=\"font-size: 11.0pt; font-family: \'Calibri\',sans-serif; color: #000000;\">Thanks</span></p> <p><span style=\"font-size: 11.0pt; font-family: \'Calibri\',sans-serif; color: #000000;\">Celeste</span></p> <p><span style=\"font-size: 11.0pt; font-family: \'Calibri\',sans-serif; color: #000000;\">Kony Bank, Customer Service</span></p>', 
    'ADMIN|CSR', 'UID10', 'admin', '2', 'TRUE', 'admin1', 'admin1', 
    '2018-02-26 19:11:15', '2018-02-26 19:11:15', 
    '2018-02-26 19:11:15', '0'
  );
set 
  reqMsgId = concat("MSG", customerId + 7);
INSERT INTO `requestmessage` (
  `id`, `CustomerRequest_id`, `MessageDescription`, 
  `RepliedBy`, `RepliedBy_id`, `RepliedBy_Name`,`ReplySequence`, `IsRead`, 
  `createdby`, `modifiedby`, `createdts`, 
  `lastmodifiedts`, `synctimestamp`, 
  `softdeleteflag`
) 
VALUES 
  (
    reqMsgId, customerReqId, ' <p><span style=\"color: #000000;\">Hi,</span></p> <p><span style=\"color: #000000;\">&nbsp;</span></p> <p><span style=\"color: #000000;\">I will be travelling to Australia from 1<sup>st</sup>-10<sup>th</sup> May 2018 and will be using my Kony Bank Credit Card for the same. Please enable international transactions on this card for this period.</span></p> <p><span style=\"color: #000000;\">&nbsp;</span></p> <p><span style=\"font-size: 11.0pt; font-family: \'Calibri\',sans-serif; color: #000000;\">Thanks</span></p>', 
    'CUSTOMER', customerId, 'John bailey', '2', 'TRUE', 'admin1', 'admin1', 
    '2018-02-26 19:11:15', '2018-02-26 19:11:15', 
    '2018-02-26 19:11:15', '0'
  );
SELECT SYSDATE() + 1 INTO LogID;
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 1,LogID + 1, _username, 'Login', 'Login', 
    'Login with Username/Password', 
    CURRENT_TIMESTAMP, 'Open', 'Web', 
    '10.10.1.1', 'Chrome', 'Windows', 
    '1234', '0', 'olbuser', CURRENT_TIMESTAMP
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 2, LogID + 2,_username, 'Profile', 'Update contact number', 
    'Changed Primary Contact Number', 
    CURRENT_TIMESTAMP, 'Open', 'Web', 
    '10.10.1.2', 'Chrome', 'Windows', 
    '1234', '0', 'olbuser', CURRENT_TIMESTAMP
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 3, LogID + 3,_username, 'Bill Pay', 'Activate Bill Payment Service', 
    'Activate Bill Payment Service', 
    CURRENT_TIMESTAMP, 'Open', 'Web', 
    '10.10.1.3', 'Chrome', 'Windows', 
    '1234', '0', 'olbuser', CURRENT_TIMESTAMP
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 4,LogID + 4, _username, 'Bill Pay', 'Add Payee', 
    'Added Payee CitiBank Credit Card', 
    CURRENT_TIMESTAMP, 'Open', 'Web', 
    '10.10.1.4', 'Chrome', 'Windows', 
    '', '', '', CURRENT_TIMESTAMP
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 5,LogID + 5, _username, 'Bill Pay', 'Pay Bill', 
    'BillPayment to CitiBank Credit Card $450', 
    CURRENT_TIMESTAMP, 'Open', 'Web', 
    '10.10.1.5', 'Chrome', 'Windows', 
    '', '', '', CURRENT_TIMESTAMP
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 6,LogID + 6, _username, 'Logout', 'Logout', 
    'Logout', CURRENT_TIMESTAMP, 
    'Open', 'Web', '10.10.1.6', 'Chrome', 
    'Windows', '1234', '0', 'olbuser', 
    CURRENT_TIMESTAMP
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 7,LogID + 7, _username, 'Login', 'Login', 
    'Login with FaceID', CURRENT_TIMESTAMP, 
    'Open', 'Mobile', '10.10.1.7', 'Moto', 
    'Android', '1234', '0', 'olbuser', 
    CURRENT_TIMESTAMP
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 8, LogID + 8,_username, 'Accounts', 'View Accounts', 
    'View Checking Account XX2455', 
    CURRENT_TIMESTAMP, 'Open', 'Mobile', 
    '10.10.1.8', 'Moto', 'Android', '1234', 
    '0', 'olbuser', CURRENT_TIMESTAMP
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 9,LogID + 9, _username, 'Transfers', 'Add Recipient', 
    'Add IntraBank Recipient Tom Brummet', 
    CURRENT_TIMESTAMP, 'Open', 'Mobile', 
    '10.10.1.9', 'Moto', 'Android', '1234', 
    '0', 'olbuser', CURRENT_TIMESTAMP
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 10, LogID + 10,_username, 'Transfers', 
    'IntraBank Fund Transfer', 'IntraBank FT to Tom Brumet $150', 
    CURRENT_TIMESTAMP, 'Open', 'Mobile', 
    '10.10.1.10', 'Moto', 'Android', 
    '1234', '0', 'olbuser', CURRENT_TIMESTAMP
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 11, LogID + 11,_username, 'Accounts', 'View Accounts', 
    'View Checking Account XX2455', 
    CURRENT_TIMESTAMP, 'Open', 'Mobile', 
    '10.10.1.11', 'Moto', 'Android', 
    '1234', '0', 'olbuser', CURRENT_TIMESTAMP
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 12, LogID + 12,_username, 'Messages', 'Send Message', 
    'Send Message subject Bill Payment Failed', 
    CURRENT_TIMESTAMP, 'Open', 'Mobile', 
    '10.10.1.12', 'Moto', 'Android', 
    '1234', '0', 'olbuser', CURRENT_TIMESTAMP
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 13, LogID + 13,_username, 'P2P', 'Send Money', 
    'Send Money to Judy Blume $25', 
    CURRENT_TIMESTAMP, 'Open', 'Mobile', 
    '10.10.1.13', 'Moto', 'Android', 
    '1234', '0', 'olbuser', CURRENT_TIMESTAMP
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 14,LogID + 14, _username, 'Logout', 'Logout', 
    'Logout', CURRENT_TIMESTAMP, 
    'Open', 'Mobile', '10.10.1.14', 'Moto', 
    'Android', '1234', '0', 'olbuser', 
    CURRENT_TIMESTAMP
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 15,LogID + 15, _username, 'Login', 'Login', 
    'Login with Username/Password', 
    CURRENT_TIMESTAMP, 'Open', 'Web', 
    '10.10.1.15', 'Chrome', 'Windows', 
    '', '', '', CURRENT_TIMESTAMP
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 16, LogID + 16,_username, 'Wire Transfer', 
    'Manage Payee', 'Edit Details for Payee Jane', 
    CURRENT_TIMESTAMP, 'Open', 'Web', 
    '10.10.1.16', 'Chrome', 'Windows', 
    '', '', '', CURRENT_TIMESTAMP
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 17, LogID + 17,_username, 'Wire Transfer', 
    'Wire Transfer', 'Wire Transfer to Jane $200', 
    CURRENT_TIMESTAMP, 'Open', 'Web', 
    '10.10.1.17', 'Chrome', 'Windows', 
    '', '', '', CURRENT_TIMESTAMP
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 18, LogID + 18,_username, 'Accounts', 'View Accounts', 
    'View Checking Account XX2455', 
    CURRENT_TIMESTAMP, 'Open', 'Web', 
    '10.10.1.18', 'Chrome', 'Windows', 
    '', '', '', CURRENT_TIMESTAMP
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 19, LogID + 19,_username, 'Accounts', 'Add External Account', 
    'Add External Account Chase Bank xx7583', 
    CURRENT_TIMESTAMP, 'Open', 'Web', 
    '10.10.1.19', 'Chrome', 'Windows', 
    '', '', '', CURRENT_TIMESTAMP
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 20, LogID + 20,_username, 'Logout', 'Logout', 
    'Logout', CURRENT_TIMESTAMP, 
    'Open', 'Web', '10.10.1.20', 'Chrome', 
    'Windows', '', '', '', CURRENT_TIMESTAMP
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID1', 0, 'Kony User', 
    'Kony Dev', CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 
    0
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID10', 0, 'Kony User', 
    'Kony Dev', CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 
    0
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID12', 0, 'Kony User', 
    'Kony Dev', CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 
    0
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID13', 0, 'Kony User', 
    'Kony Dev', CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 
    0
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID14', 0, 'Kony User', 
    'Kony Dev', CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 
    0
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID2', 0, 'Kony User', 
    'Kony Dev', CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 
    0
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID3', 0, 'Kony User', 
    'Kony Dev', CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 
    0
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID4', 0, 'Kony User', 
    'Kony Dev', CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 
    0
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID5', 0, 'Kony User', 
    'Kony Dev', CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 
    0
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID6', 0, 'Kony User', 
    'Kony Dev', CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 
    0
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID7', 0, 'Kony User', 
    'Kony Dev', CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 
    0
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID8', 0, 'Kony User', 
    'Kony Dev', CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 
    0
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID9', 0, 'Kony User', 
    'Kony Dev', CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 
    0
  );
END$$
DELIMITER ;

ALTER TABLE `csrassistgrant` 
ADD COLUMN `internalUserName` VARCHAR(50) NULL AFTER `userName`;

DROP PROCEDURE IF EXISTS `customer_entitlements_proc`;
DELIMITER $$
CREATE PROCEDURE `customer_entitlements_proc`(
in _customerID varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
)
BEGIN
	SELECT 
		`service`.`id` AS `serviceId`,
        `service`.`Name` AS `serviceName`,
        `service`.`Description` AS `serviceDesc`,
        `service`.`Notes` AS `serviceNotes`,
        IF(`customerentitlement`.`MaxTransactionLimit` != '' && !ISNULL(`customerentitlement`.`MaxTransactionLimit`) 
        ,`customerentitlement`.`MaxTransactionLimit`, `service`.`MaxTransferLimit`) AS `maxTransferLimit`,
        `service`.`MinTransferLimit` AS `minTransferLimit`,
        `service`.`DisplayName` AS `displayName`,
        `service`.`DisplayDescription` AS `displayDesc`
	FROM `customerentitlement`
		JOIN `service` ON (`service`.`id` = `customerentitlement`.`Service_id`)
	WHERE `customerentitlement`.`Customer_id` = _customerID
    UNION
    SELECT 
		`service`.`id` AS `serviceId`,
        `service`.`Name` AS `serviceName`,
        `service`.`Description` AS `serviceDesc`,
        `service`.`Notes` AS `serviceNotes`,
        `service`.`MaxTransferLimit` AS `maxTransferLimit`,
        `service`.`MinTransferLimit` AS `minTransferLimit`,
        `service`.`DisplayName` AS `displayName`,
        `service`.`DisplayDescription` AS `displayDesc`
	FROM `customergroup`
    	JOIN `groupentitlement` ON (`groupentitlement`.`Group_id` = `customergroup`.`Group_id`)
		JOIN `service` ON (`service`.`id` = `groupentitlement`.`Service_id`)
	WHERE `customergroup`.`Customer_id` = _customerID;
    
END$$
DELIMITER ;


ALTER TABLE `csrassistgrant` 
ADD COLUMN `CustomerType` VARCHAR(50) NULL AFTER `customerId`;

DROP procedure IF EXISTS `customer_group_unlink_proc`;
DELIMITER $$
CREATE  PROCEDURE `customer_group_unlink_proc`(
IN roleId varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
)
BEGIN
Delete from customergroup where Group_id = roleId;

END$$
DELIMITER;



DROP procedure IF EXISTS `customer_group_proc`;
DELIMITER $$
CREATE PROCEDURE `customer_group_proc`(
IN _customerIds LONGTEXT CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _groupId varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
IN _numberOfRows BIGINT,
IN _userId varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
)
BEGIN
DECLARE INSERTED_RECORDS_COUNT BIGINT DEFAULT 0;
DECLARE INDEXVALUE BIGINT DEFAULT 1;
DECLARE CustomerId LONGTEXT CHARACTER SET UTF8 COLLATE utf8_general_ci DEFAULT NULL;
DECLARE buffer LONGTEXT CHARACTER SET UTF8 COLLATE utf8_general_ci DEFAULT NULL;
set buffer ="";
iterator:
LOOP
	IF LENGTH(TRIM(_customerIds)) = 0 OR _customerIds IS NULL OR INDEXVALUE > _numberOfRows THEN
		LEAVE iterator;
	END IF;
	SET CustomerId = func_split_str(_customerIds,",",INDEXVALUE);
    IF EXISTS(select * from customergroup where Customer_id=CustomerId and Group_id=_groupId) THEN
		SET INDEXVALUE = INDEXVALUE+1;
    else
		INSERT IGNORE INTO customergroup(Customer_id,Group_id,createdby) values(CustomerId,_groupId,_userId);
		IF(ROW_COUNT() = 0) THEN
			SET buffer=concat(buffer,",",CustomerId);
		END IF;
		SET INSERTED_RECORDS_COUNT= INSERTED_RECORDS_COUNT+ ROW_COUNT();
		SET INDEXVALUE = INDEXVALUE+1;
    end if;
END LOOP;
SET buffer = right(buffer,LENGTH(buffer)-1);
SELECT buffer AS 'FAILED_RECORDS';
END$$
DELIMITER ;

DROP TABLE IF EXISTS `entitystatus`;
CREATE TABLE `entitystatus` (
  `Entity_id` varchar(50) NOT NULL,
  `Status` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`Entity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- Support for multiple channels in services module

CREATE TABLE `service_channels` (
  `Service_id` VARCHAR(50) NOT NULL,
  `Channel_id` VARCHAR(50) NOT NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`Service_id`, `Channel_id`),
  INDEX `service_channels_Channel_id_idx` (`Channel_id` ASC),
  CONSTRAINT `service_channels_Service_id`
    FOREIGN KEY (`Service_id`)
    REFERENCES `service` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `service_channels_Channel_id`
    FOREIGN KEY (`Channel_id`)
    REFERENCES `servicechannel` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `service` 
DROP FOREIGN KEY `FK_Service_ServiceChannel`;
ALTER TABLE `service` 
DROP COLUMN `Channel_id`,
DROP INDEX `IXFK_Service_ServiceChannel` ;


DROP VIEW IF EXISTS `customerpermissions_view`;
CREATE VIEW `customerpermissions_view` AS select `customerentitlement`.`Customer_id` AS `Customer_id`,`customerentitlement`.`Service_id` AS `Service_id`,`service`.`Name` AS `Service_name`,`service`.`Description` AS `Service_description`,`service`.`Notes` AS `Service_notes`,`service`.`Status_id` AS `Status_id`,`status`.`Description` AS `Status_description`,`customerentitlement`.`TransactionFee_id` AS `TransactionFee_id`,`transactionfee`.`Description` AS `TransactionFee_description`,`customerentitlement`.`TransactionLimit_id` AS `TransactionLimit_id`,`transactionlimit`.`Description` AS `TransactionLimit_description`,`service`.`Type_id` AS `ServiceType_id`,`servicetype`.`Description` AS `ServiceType_description`,`service`.`MinTransferLimit` AS `MinTransferLimit`,`service`.`MaxTransferLimit` AS `MaxTransferLimit`,`service`.`DisplayName` AS `Display_Name`,`service`.`DisplayDescription` AS `Display_Description` from (((((`customerentitlement` left join `service` on((`customerentitlement`.`Service_id` = `service`.`id`))) left join `status` on((`service`.`Status_id` = `status`.`id`))) left join `transactionfee` on((`transactionfee`.`id` = `customerentitlement`.`TransactionFee_id`))) left join `transactionlimit` on((`transactionlimit`.`id` = `customerentitlement`.`TransactionLimit_id`))) left join `servicetype` on((`servicetype`.`id` = `service`.`Type_id`)));

DROP VIEW IF EXISTS `service_view`;
CREATE VIEW `service_view` AS select `service`.`id` AS `id`,`service`.`Type_id` AS `Type_id`,group_concat(`sc`.`Channel_id` separator ',') AS `Channel_id`,group_concat(`servicechannel`.`Description` separator ',') AS `Channel`,`service`.`Name` AS `Name`,`service`.`Description` AS `Description`,`service`.`DisplayName` AS `DisplayName`,`service`.`DisplayDescription` AS `DisplayDescription`,`service`.`Category_id` AS `Category_Id`,`service`.`code` AS `Code`,`service`.`Status_id` AS `Status_id`,`service`.`Notes` AS `Notes`,`service`.`MaxTransferLimit` AS `MaxTransferLimit`,`service`.`MinTransferLimit` AS `MinTransferLimit`,`service`.`TransferDenominations` AS `TransferDenominations`,`service`.`IsFutureTransaction` AS `IsFutureTransaction`,`service`.`TransactionCharges` AS `TransactionCharges`,`service`.`IsAuthorizationRequired` AS `IsAuthorizationRequired`,`service`.`IsSMSAlertActivated` AS `IsSMSAlertActivated`,`service`.`SMSCharges` AS `SMSCharges`,`service`.`IsBeneficiarySMSAlertActivated` AS `IsBeneficiarySMSAlertActivated`,`service`.`BeneficiarySMSCharge` AS `BeneficiarySMSCharge`,`service`.`HasWeekendOperation` AS `HasWeekendOperation`,`service`.`IsOutageMessageActive` AS `IsOutageMessageActive`,`service`.`IsAlertActive` AS `IsAlertActive`,`service`.`IsTCActive` AS `IsTCActive`,`service`.`IsAgreementActive` AS `IsAgreementActive`,`service`.`IsCampaignActive` AS `IsCampaignActive`,`service`.`WorkSchedule_id` AS `WorkSchedule_id`,`service`.`TransactionFee_id` AS `TransactionFee_id`,`service`.`TransactionLimit_id` AS `TransactionLimit_id`,`service`.`createdby` AS `createdby`,`service`.`modifiedby` AS `modifiedby`,`service`.`createdts` AS `createdts`,`service`.`lastmodifiedts` AS `lastmodifiedts`,`service`.`synctimestamp` AS `synctimestamp`,`service`.`softdeleteflag` AS `softdeleteflag`,`status`.`Description` AS `Status`,`category`.`Name` AS `Category_Name`,`servicetype`.`Description` AS `Type_Name`,`workschedule`.`Description` AS `WorkSchedule_Desc` from ((((((`service` left join `service_channels` `sc` on((`sc`.`Service_id` = `service`.`id`))) left join `servicechannel` on((`sc`.`Channel_id` = `servicechannel`.`id`))) left join `category` on((`service`.`Category_id` = `category`.`id`))) left join `status` on((`service`.`Status_id` = `status`.`id`))) left join `servicetype` on((`service`.`Type_id` = `servicetype`.`id`))) left join `workschedule` on((`service`.`WorkSchedule_id` = `workschedule`.`id`))) group by `service`.`id`;

DROP VIEW IF EXISTS `groupservices_view`;
CREATE VIEW `groupservices_view` AS select `groupentitlement`.`Group_id` AS `Group_id`,`groupentitlement`.`Service_id` AS `Service_id`,`service`.`Name` AS `Service_name`,`service`.`Description` AS `Service_description`,`service`.`Notes` AS `Service_notes`,`service`.`Status_id` AS `Status_id`,(select `status`.`Description` from `status` where (`status`.`id` = `service`.`Status_id`)) AS `Status_description`,`groupentitlement`.`TransactionFee_id` AS `TransactionFee_id`,(select `transactionfee`.`Description` from `transactionfee` where (`transactionfee`.`id` = `groupentitlement`.`TransactionFee_id`)) AS `TransactionFee_description`,`groupentitlement`.`TransactionLimit_id` AS `TransactionLimit_id`,(select `transactionlimit`.`Description` from `transactionlimit` where (`transactionlimit`.`id` = `groupentitlement`.`TransactionLimit_id`)) AS `TransactionLimit_description`,`service`.`Type_id` AS `ServiceType_id`,(select `servicetype`.`Description` from `servicetype` where (`servicetype`.`id` = `service`.`Type_id`)) AS `ServiceType_description`,group_concat(`sc`.`Channel_id` separator ',') AS `Channel_id`,group_concat(`servicechannel`.`Description` separator ',') AS `ChannelType_description`,`service`.`MinTransferLimit` AS `MinTransferLimit`,`service`.`MaxTransferLimit` AS `MaxTransferLimit`,`service`.`DisplayName` AS `Display_Name`,`service`.`DisplayDescription` AS `Display_Description` from (((`groupentitlement` left join `service` on((`groupentitlement`.`Service_id` = `service`.`id`))) left join `service_channels` `sc` on((`sc`.`Service_id` = `service`.`id`))) left join `servicechannel` on((`sc`.`Channel_id` = `servicechannel`.`id`))) group by `groupentitlement`.`Group_id`,`groupentitlement`.`Service_id`;

ALTER TABLE `locationfile` 
ADD COLUMN `successcount` INT(5) NULL AFTER `id`,
ADD COLUMN `failurecount` INT(5) NULL AFTER `successcount`,
ADD COLUMN `locationfilestatus` INT(1) NULL AFTER `failurecount`;

DROP VIEW IF EXISTS `customer_request_category_count_view`;
CREATE VIEW `customer_request_category_count_view` AS select `requestcategory`.`id` AS `requestcategory_id`,`requestcategory`.`Name` AS `requestcategory_Name`,count(`customerrequest`.`id`) AS `request_count` from (`requestcategory` left join `customerrequest` on(((`customerrequest`.`RequestCategory_id` = `requestcategory`.`id`) and (`customerrequest`.`Status_id` = 'SID_OPEN')))) group by `requestcategory`.`id`;

DROP VIEW IF EXISTS `groupentitlement_view`;
CREATE VIEW `groupentitlement_view` AS
    SELECT 
        `groupentitlement`.`Group_id` AS `Group_id`,
        `service`.`id` AS `Service_id`,
        `service`.`Name` AS `Service_name`,
        `service`.`Description` AS `Service_description`,
        `service`.`Type_id` AS `Service_type_id`,
        `periodiclimit`.`MaximumLimit` AS `MaxDailyLimit`,
        `service`.`MaxTransferLimit` AS `MaxTransferLimit`,
        `service`.`MinTransferLimit` AS `MinTransferLimit`
    FROM
        ((`groupentitlement`
        LEFT JOIN `service` ON ((`groupentitlement`.`Service_id` = `service`.`id`)))
        LEFT JOIN `periodiclimit` ON ((`service`.`TransactionLimit_id` = `periodiclimit`.`TransactionLimit_id`)));
        

DROP VIEW IF EXISTS `customer_request_csr_count_view`;
CREATE VIEW `customer_request_csr_count_view` AS select `customerrequest`.`Status_id` AS `customerrequest_Status_id`,(select `status`.`Description` from `status` where (`status`.`id` = `customerrequest`.`Status_id`) limit 1) AS `status_Description`,`customerrequest`.`AssignedTo` AS `customerrequest_assignedTo`,count(`customerrequest`.`id`) AS `request_count` from `customerrequest` group by `customerrequest`.`AssignedTo`,`customerrequest`.`Status_id`;

DROP VIEW IF EXISTS `customerrequests_view`;
CREATE VIEW `customerrequests_view` AS select `customerrequest`.`id` AS `id`,`customerrequest`.`softdeleteflag` AS `softdeleteflag`,`customerrequest`.`Priority` AS `priority`,`customerrequest`.`createdts` AS `requestCreatedDate`,max(`requestmessage`.`createdts`) AS `recentMsgDate`,`requestcategory`.`Name` AS `requestcategory_id`,`customerrequest`.`Customer_id` AS `customer_id`,`customer`.`UserName` AS `username`,(select `status`.`Description` from `status` where (`status`.`id` = `customerrequest`.`Status_id`) limit 1) AS `status_id`,`customerrequest`.`id` AS `statusIdentifier`,`customerrequest`.`RequestSubject` AS `requestsubject`,`customerrequest`.`Accountid` AS `accountid`,concat(`systemuser`.`FirstName`,' ',`systemuser`.`LastName`) AS `assignTo`,count(`requestmessage`.`id`) AS `totalmsgs`,count((case when (`requestmessage`.`IsRead` = 'true') then 1 end)) AS `readmsgs`,count((case when (`requestmessage`.`IsRead` = 'false') then 1 end)) AS `unreadmsgs`,substring_index(group_concat(`requestmessage`.`MessageDescription` order by `requestmessage`.`createdts` ASC,`requestmessage`.`id` ASC separator '||'),'||',1) AS `firstMessage`,group_concat(`requestmessage`.`id` separator ',') AS `msgids`,count(`messageattachment`.`id`) AS `totalAttachments` from (((((`customerrequest` left join `requestmessage` on((`customerrequest`.`id` = `requestmessage`.`CustomerRequest_id`))) left join `requestcategory` on((`requestcategory`.`id` = `customerrequest`.`RequestCategory_id`))) left join `customer` on((`customer`.`id` = `customerrequest`.`Customer_id`))) left join `messageattachment` on((`messageattachment`.`RequestMessage_id` = `requestmessage`.`id`))) left join `systemuser` on((`systemuser`.`id` = `customerrequest`.`AssignedTo`))) group by `customerrequest`.`id` order by `customerrequest`.`createdts` desc,`customerrequest`.`id`;

DROP VIEW IF EXISTS `groupentitlement_view`;
CREATE VIEW `groupentitlement_view` AS select `groupentitlement`.`Group_id` AS `Group_id`,`service`.`id` AS `Service_id`,`service`.`Name` AS `Service_name`,`service`.`Description` AS `Service_description`,`service`.`Type_id` AS `Service_type_id`,`periodiclimit`.`MaximumLimit` AS `MaxDailyLimit`,`service`.`MaxTransferLimit` AS `MaxTransferLimit`,`service`.`MinTransferLimit` AS `MinTransferLimit`,`groupentitlement`.`TransactionFee_id` AS `TransactionFee_id`,`groupentitlement`.`TransactionLimit_id` AS `TransactionLimit_id`,`periodiclimit`.`Code` AS `Code` from ((`groupentitlement` left join `service` on((`groupentitlement`.`Service_id` = `service`.`id`))) left join `periodiclimit` on((`service`.`TransactionLimit_id` = `periodiclimit`.`TransactionLimit_id`)));

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
        IF(`customer`.`lockCount`+1 >= @accountLockoutThreshold, 'SID_CUS_LOCKED',`customer`.`Status_id`) AS `CustomerStatus_id`,
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
        LEFT JOIN  `membergroup` ON ((`membergroup`.`id` = `customergroup`.`Group_id`)))
            
    WHERE `customer`.`id` = _customerId
    LIMIT 1;

END$$
DELIMITER ;

DROP VIEW IF EXISTS `cardaccountrequest_view`;
CREATE VIEW `cardaccountrequest_view` AS select `cardaccountrequest`.`id` AS `Request_id`,`cardaccountrequest`.`Date` AS `Date`,`cardaccountrequesttype`.`DisplayName` AS `Type`,`cardaccountrequest`.`CardAccountNumber` AS `CardAccountNumber`,`cardaccountrequest`.`CardAccountName` AS `CardAccountName`,`cardaccountrequest`.`RequestReason` AS `Reason`,(select `status`.`Description` from `status` where (`cardaccountrequest`.`Status_id` = `status`.`id`) limit 1) AS `Status`,`cardaccountrequest`.`Customer_id` AS `CustomerId`,`customercommunication`.`Value` AS `CommunicationValue`,`communicationtype`.`Description` AS `DeliveryMode`,concat_ws(`address`.`addressLine1`,`address`.`addressLine2`,`address`.`addressLine3`,`city`.`Name`,`region`.`Name`,`country`.`Name`,`address`.`zipCode`) AS `Address` from (((((((`cardaccountrequest` left join `cardaccountrequesttype` on((`cardaccountrequest`.`RequestType_id` = `cardaccountrequesttype`.`id`))) left join `address` on((`cardaccountrequest`.`Address_id` = `address`.`id`))) left join `city` on((`address`.`City_id` = `city`.`id`))) left join `region` on((`address`.`Region_id` = `region`.`id`))) left join `country` on((`city`.`Country_id` = `country`.`id`))) left join `customercommunication` on((`cardaccountrequest`.`Communication_id` = `customercommunication`.`id`))) left join `communicationtype` on((`customercommunication`.`Type_id` = `communicationtype`.`id`)));
