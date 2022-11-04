-- >> Services or Entitlements Module redesign <<

UPDATE service SET Feature_id= null;

ALTER TABLE `feature` 
DROP COLUMN `DisplayDescription`,
DROP COLUMN `DisplayName`,
DROP COLUMN `code`,
CHANGE COLUMN `Name` `name` VARCHAR(100) NOT NULL ,
CHANGE COLUMN `Description` `description` VARCHAR(400) NULL DEFAULT NULL ;

DELETE FROM feature;

DROP TABLE IF EXISTS `groupactionlimit`;
DROP TABLE IF EXISTS `appactionlimit`;
DROP TABLE IF EXISTS `actiondisplaynamedescription`;
DROP TABLE IF EXISTS `customeractionlimit`;

DROP TABLE IF EXISTS `featuretermsandconditions`;
DROP TABLE IF EXISTS `featuredisplaynamedescription`;
DROP TABLE IF EXISTS `appfeature`;
DROP TABLE IF EXISTS `limittype`;

ALTER TABLE `service` 
DROP FOREIGN KEY `FK_Service_Feature_id`;

ALTER TABLE `feature` 
CHANGE COLUMN `id` `id` VARCHAR(255) NOT NULL ;

CREATE TABLE `limittype` (
  `id` VARCHAR(50) NOT NULL,
  `description` VARCHAR(255) NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `limitsubtype` (
  `id` VARCHAR(50) NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `description` VARCHAR(50) NULL DEFAULT NULL,
  `LimitType_id` VARCHAR(50) NOT NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  INDEX `FK_limitsubtype_limittype_idx` (`LimitType_id` ASC),
  CONSTRAINT `FK_limitsubtype_limittype`
    FOREIGN KEY (`LimitType_id`)
    REFERENCES `limittype` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE=InnoDB DEFAULT CHARSET=utf8;
    
CREATE TABLE `membergrouptype` (
  `id` VARCHAR(50) NOT NULL,
  `description` VARCHAR(300) NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `actiontype` (
  `id` VARCHAR(50) NOT NULL,
  `description` VARCHAR(50) NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8; 
  
CREATE TABLE `featureaction` (
  `id` VARCHAR(255) NOT NULL,
  `Feature_id` VARCHAR(255) NULL,
  `Type_id` VARCHAR(50) NULL DEFAULT NULL,
  `name` VARCHAR(100) NULL DEFAULT NULL,
  `description` VARCHAR(300) NULL DEFAULT NULL,
  `isAccountLevel` TINYINT(1) NOT NULL DEFAULT 0,
  `isMFAApplicable` TINYINT(1) NOT NULL DEFAULT 0,
  `notes` VARCHAR(2000) NULL DEFAULT NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  INDEX `IXFK_featureaction_actiontype` (`Type_id` ASC),
  INDEX `FK_action_feature_id_idx` (`Feature_id` ASC),
  CONSTRAINT `FK_featureaction_actiontype`
    FOREIGN KEY (`Type_id`)
    REFERENCES `actiontype` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_action_feature_id`
    FOREIGN KEY (`Feature_id`)
    REFERENCES `feature` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE=InnoDB DEFAULT CHARSET=utf8; 
  
CREATE TABLE `actiondisplaynamedescription` (
  `Action_id` VARCHAR(255) NOT NULL,
  `Locale_id` VARCHAR(50) NOT NULL,
  `displayName` TEXT NOT NULL,
  `displayDescription` TEXT NOT NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`Action_id`, `Locale_id`),
  CONSTRAINT `FK_actiondisplaynamedescription_Action_id`
    FOREIGN KEY (`Action_id`)
    REFERENCES `featureaction` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_actiondisplaynamedescription_Locale_id`
    FOREIGN KEY (`Locale_id`)
    REFERENCES `locale` (`Code`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE=InnoDB DEFAULT CHARSET=utf8; 

    
CREATE TABLE `featuredisplaynamedescription` (
  `Feature_id` VARCHAR(255) NOT NULL,
  `Locale_id` VARCHAR(50) NOT NULL,
  `displayName` TEXT NOT NULL,
  `displayDescription` TEXT NOT NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`Feature_id`, `Locale_id`),
  INDEX `FK_featuredisplaynamedescription_locale_idx` (`Locale_id` ASC),
  CONSTRAINT `FK_featuredisplaynamedescription_Feature_id`
    FOREIGN KEY (`Feature_id`)
    REFERENCES `feature` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_featuredisplaynamedescription_locale`
    FOREIGN KEY (`Locale_id`)
    REFERENCES `locale` (`Code`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE=InnoDB DEFAULT CHARSET=utf8; 



CREATE TABLE `featureroletype` (
  `RoleType_id` VARCHAR(50) NOT NULL,
  `Feature_id` VARCHAR(255) NOT NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  INDEX `FK_featureroletype_Feature_id_idx` (`Feature_id` ASC),
  PRIMARY KEY (`RoleType_id`, `Feature_id`),
  CONSTRAINT `FK_featureroletype_Feature_id`
    FOREIGN KEY (`Feature_id`)
    REFERENCES `feature` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_featureroletype_membergrouptype`
    FOREIGN KEY (`RoleType_id`)
    REFERENCES `membergrouptype` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE=InnoDB DEFAULT CHARSET=utf8; 

    
CREATE TABLE `organisationactionlimit` (
  `id` VARCHAR(50) NOT NULL,
  `Organisation_id` int(11) NOT NULL,
  `Action_id` VARCHAR(255) NOT NULL,
  `LimitSubType_id` VARCHAR(50) NULL,
  `value` DECIMAL(20,2) NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNIQUE_organisationactionlimit` (`Organisation_id`,`Action_id`,`LimitSubType_id`),
  INDEX `FK_organisationactionlimit_organisation_idx` (`Organisation_id` ASC),
  INDEX `FK_organisationactionlimit_action_idx` (`Action_id` ASC),
  INDEX `FK_organisationactionlimit_limitsubtype_idx` (`LimitSubType_id` ASC),
  CONSTRAINT `FK_organisationactionlimit_organisation`
    FOREIGN KEY (`Organisation_id`)
    REFERENCES `organisation` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_organisationactionlimit_action`
    FOREIGN KEY (`Action_id`)
    REFERENCES `featureaction` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_organisationactionlimit_limitsubtype`
    FOREIGN KEY (`LimitSubType_id`)
    REFERENCES `limitsubtype` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE=InnoDB DEFAULT CHARSET=utf8;
    

CREATE TABLE `groupactionlimit` (
  `id` VARCHAR(50) NOT NULL,
  `Group_id` VARCHAR(50) NOT NULL,
  `Action_id` VARCHAR(255) NOT NULL,
  `LimitSubType_id` VARCHAR(50) NULL,
  `value` DECIMAL(20,2) NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNIQUE_groupactionlimit` (`Group_id`,`Action_id`,`LimitSubType_id`),
  INDEX `IXFK_groupactionlimit_Group` (`Group_id` ASC),
  INDEX `IXFK_groupactionlimit_Action` (`Action_id` ASC),
  INDEX `FK_groupactionlimit_limitsubtype_idx` (`LimitSubType_id` ASC),
  CONSTRAINT `FK_groupactionlimit_Group`
    FOREIGN KEY (`Group_id`)
    REFERENCES `membergroup` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_groupactionlimit_Action`
    FOREIGN KEY (`Action_id`)
    REFERENCES `featureaction` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_groupactionlimit_limitsubtype`
    FOREIGN KEY (`LimitSubType_id`)
    REFERENCES `limitsubtype` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE=InnoDB DEFAULT CHARSET=utf8;
  
    
CREATE TABLE `customeractionlimit` (
  `id` VARCHAR(50) NOT NULL,
  `RoleType_id` VARCHAR(50) NOT NULL,
  `Customer_id` VARCHAR(50) NOT NULL,
  `Action_id` VARCHAR(255) NOT NULL,
  `Account_id` VARCHAR(50) NULL,
  `LimitSubType_id` VARCHAR(50) NULL,
  `value` DECIMAL(20,2) NULL DEFAULT '0.00',
  `isAllowed` TINYINT(1) NOT NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNIQUE_customeractionlimit` (`RoleType_id`,`Customer_id`,`Action_id`,`Account_id`,`LimitSubType_id`),
  INDEX `IXFK_CustomerActionLimit_Customer` (`Customer_id` ASC),
  INDEX `IXFK_CustomerActionLimit_Service` (`Action_id` ASC),
  INDEX `FK_CustomerActionLimit_LimitSubtype_id_idx` (`LimitSubType_id` ASC),
  INDEX `FK_CustomerActionLimit_RoleType_idx` (`RoleType_id` ASC),
  CONSTRAINT `FK_CustomerActionLimit_Customer`
    FOREIGN KEY (`Customer_id`)
    REFERENCES `customer` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_CustomerActionLimit_LimitSubtype_id`
    FOREIGN KEY (`LimitSubType_id`)
    REFERENCES `limitsubtype` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_CustomerActionLimit_Action`
    FOREIGN KEY (`Action_id`)
    REFERENCES `featureaction` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_CustomerActionLimit_customertype`
    FOREIGN KEY (`RoleType_id`)
    REFERENCES `membergrouptype` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `customeractionlimit` RENAME TO  `customeraction` ;
ALTER TABLE `customeraction` 
DROP FOREIGN KEY `FK_CustomerActionLimit_LimitSubtype_id`;
ALTER TABLE `customeraction` 
DROP COLUMN `value`,
DROP COLUMN `LimitSubType_id`,
DROP INDEX `UNIQUE_customeractionlimit` ,
ADD UNIQUE INDEX `UNIQUE_customeractionlimit` (`RoleType_id` ASC, `Customer_id` ASC, `Action_id` ASC, `Account_id` ASC),
DROP INDEX `FK_CustomerActionLimit_LimitSubtype_id_idx` ;


ALTER TABLE `organisationactionlimit` 
DROP FOREIGN KEY `FK_organisationactionlimit_limitsubtype`;
ALTER TABLE `organisationactionlimit` 
CHANGE COLUMN `LimitSubType_id` `LimitType_id` VARCHAR(50) NULL DEFAULT NULL ,
ADD INDEX `FK_organisationactionlimit_limitsubtype_idx` (`LimitType_id` ASC),
DROP INDEX `FK_organisationactionlimit_limitsubtype_idx` ;

ALTER TABLE `organisationactionlimit` 
ADD CONSTRAINT `FK_organisationactionlimit_limitsubtype`
  FOREIGN KEY (`LimitType_id`)
  REFERENCES `limittype` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE `groupactionlimit` 
DROP FOREIGN KEY `FK_groupactionlimit_limitsubtype`;
ALTER TABLE `groupactionlimit` 
CHANGE COLUMN `LimitSubType_id` `LimitType_id` VARCHAR(50) NULL DEFAULT NULL ,
ADD INDEX `FK_groupactionlimit_limitsubtype_idx` (`LimitType_id` ASC),
DROP INDEX `FK_groupactionlimit_limitsubtype_idx` ;

ALTER TABLE `groupactionlimit` 
ADD CONSTRAINT `FK_groupactionlimit_limitsubtype`
  FOREIGN KEY (`LimitType_id`)
  REFERENCES `limittype` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
  
ALTER TABLE `membergroup` 
DROP FOREIGN KEY `FK_MemberGroup_Type_id`;
ALTER TABLE `membergroup` 
DROP INDEX `FK_MemberGroup_Type_id` ,
ADD INDEX `FK_MemberGroup_Type_id_idx` (`Type_id` ASC);

CREATE TABLE `organisationaccountsublimit` (
  `id` VARCHAR(50) NOT NULL,
  `name` VARCHAR(100) NULL,
  `Organisation_id` int(11) NULL,
  `Account_id` VARCHAR(50) NULL,
  `LimitSubType_id` VARCHAR(50) NULL,
  `value1` DECIMAL(20,2) NULL,
  `value2` DECIMAL(20,2) NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  INDEX `FK_organisationaccountsublimit_limitsubtype_idx` (`LimitSubType_id` ASC),
  INDEX `FK_organisationaccountsublimit_organisation_idx` (`Organisation_id` ASC),
  CONSTRAINT `FK_organisationaccountsublimit_limitsubtype`
    FOREIGN KEY (`LimitSubType_id`)
    REFERENCES `limitsubtype` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_organisationaccountsublimit_organisation`
    FOREIGN KEY (`Organisation_id`)
    REFERENCES `organisation` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `appactionmfa` RENAME TO  `mfa` ;

ALTER TABLE `mfa` 
DROP FOREIGN KEY `FK_appactionmfa_AppAction_id`;
ALTER TABLE `mfa` 
DROP COLUMN `IsMFARequired`,
DROP COLUMN `AppAction_id`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`id`),
DROP INDEX `FK_appactionmfa_AppAction_id_idx` ;

ALTER TABLE `featureaction` 
ADD COLUMN `MFA_id` VARCHAR(50) NULL AFTER `isMFAApplicable`,
ADD INDEX `FK_featureaction_mfa_idx` (`MFA_id` ASC);

ALTER TABLE `featureaction` 
ADD CONSTRAINT `FK_featureaction_mfa`
  FOREIGN KEY (`MFA_id`)
  REFERENCES `mfa` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE `customer` 
ADD COLUMN `OrganisationLimitMatrix_id` VARCHAR(50) NULL AFTER `Organization_Id`;

ALTER TABLE `customer` 
ADD INDEX `FK_Customer_Organizationaccountsublimit_idx` (`OrganisationLimitMatrix_id` ASC);

ALTER TABLE `customer` 
ADD CONSTRAINT `FK_Customer_Organizationaccountsublimit`
  FOREIGN KEY (`OrganisationLimitMatrix_id`)
  REFERENCES `organisationaccountsublimit` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
  
  
CREATE TABLE `actionlimit` (
  `Action_id` VARCHAR(255) NOT NULL,
  `LimitType_id` VARCHAR(50) NOT NULL,
  `value` DECIMAL(20,2) NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`Action_id`, `LimitType_id`),
  INDEX `FK_actionlimit_limittype_idx` (`LimitType_id` ASC),
  CONSTRAINT `FK_actionlimit_featureaction`
    FOREIGN KEY (`Action_id`)
    REFERENCES `featureaction` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_actionlimit_limittype`
    FOREIGN KEY (`LimitType_id`)
    REFERENCES `limittype` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE VIEW `organisation_action_limits_view` AS select `organisationactionlimit`.`id` AS `id`,`organisationactionlimit`.`Organisation_id` AS `Organisation_id`,`organisationactionlimit`.`Action_id` AS `Action_id`,`organisationactionlimit`.`LimitType_id` AS `LimitType_id`,`organisationactionlimit`.`value` AS `value`,`featureaction`.`isAccountLevel` AS `isAccountLevel` from (`organisationactionlimit` left join `featureaction` on((`featureaction`.`id` = `organisationactionlimit`.`Action_id`)));

DROP VIEW IF EXISTS `customergroupinfo_view`;
CREATE VIEW `customergroupinfo_view` AS select `customergroup`.`Customer_id` AS `Customer_id`,`customergroup`.`Group_id` AS `Group_id`,`membergroup`.`Name` AS `Group_name`,`membergroup`.`Description` AS `Group_Desc`,`membergroup`.`Status_id` AS `GroupStatus_id`,(select `status`.`Description` from `status` where (`membergroup`.`Status_id` = `status`.`id`)) AS `GroupStatus_name`,`membergroup`.`createdby` AS `Group_createdby`,`membergroup`.`modifiedby` AS `Group_modifiedby`,`membergroup`.`createdts` AS `Group_createdts`,`membergroup`.`lastmodifiedts` AS `Group_lastmodifiedts`,`membergroup`.`synctimestamp` AS `Group_synctimestamp`,`membergroup`.`Type_id` AS `Group_Type_id` from (`customergroup` join `membergroup` on((`customergroup`.`Group_id` = `membergroup`.`id`)));

CREATE VIEW `feature_actions_view` AS select `featureaction`.`id` AS `id`,`featureaction`.`Feature_id` AS `Feature_id`,`featureaction`.`name` AS `action_name`,`featureaction`.`description` AS `action_description`,`featureaction`.`isAccountLevel` AS `isAccountLevel`,`featureaction`.`isMFAApplicable` AS `isMFAApplicable`,`featureaction`.`notes` AS `notes`,`featureaction`.`Type_id` AS `action_Type_id`,`feature`.`Status_id` AS `feature_status_id`,`feature`.`name` AS `feature_name`,`feature`.`description` AS `feature_description`,`feature`.`Type_id` AS `feature_Type_id`,`actionlimit`.`LimitType_id` AS `LimitType_id`,`actionlimit`.`value` AS `value` from ((`featureaction` left join `feature` on((`feature`.`id` = `featureaction`.`Feature_id`))) left join `actionlimit` on((`actionlimit`.`Action_id` = `featureaction`.`id`)));

ALTER TABLE `featureaction` 
ADD COLUMN `TermsAndConditions_id` VARCHAR(50) NULL AFTER `MFA_id`,
ADD INDEX `FK_featureaction_termandcondition_idx` (`TermsAndConditions_id` ASC);

ALTER TABLE `featureaction` 
ADD CONSTRAINT `FK_featureaction_termandcondition`
  FOREIGN KEY (`TermsAndConditions_id`)
  REFERENCES `termandcondition` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE `customeraction` 
ADD COLUMN `LimitSubType_id` VARCHAR(50) NULL AFTER `isAllowed`,
ADD COLUMN `value` DECIMAL(20,2) NULL AFTER `LimitSubType_id`,
ADD INDEX `FK_CustomerActionLimit_limitsubtype_idx` (`LimitSubType_id` ASC);

ALTER TABLE `customeraction` 
ADD CONSTRAINT `FK_CustomerActionLimit_limitsubtype`
  FOREIGN KEY (`LimitSubType_id`)
  REFERENCES `limitsubtype` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

DROP PROCEDURE IF EXISTS `customer_requests_assign_proc`;
DELIMITER $$
CREATE PROCEDURE `customer_requests_assign_proc`(
IN _requestIds TEXT  CHARACTER SET UTF8 COLLATE utf8_general_ci,
in _csrID varchar(200)  CHARACTER SET UTF8 COLLATE utf8_general_ci
)
BEGIN
		SET @updateclause = concat( "UPDATE `customerrequest` SET customerrequest.AssignedTo = ",quote(_csrID)," where customerrequest.id in (",func_escape_input_for_in_operator(_requestIds),")");
     	PREPARE stmt FROM @updateclause; EXECUTE stmt; DEALLOCATE PREPARE stmt;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `archived_request_search_proc`;
DELIMITER $$
CREATE PROCEDURE `archived_request_search_proc`(
  in _dateInitialPoint char(50), 
  in _dateFinalPoint char(50), 
  in _requestStatusID char(50), 
  in _requestCategory char(50), 
  in _offset char(50), 
  in _sortCriteria char(50), 
  in _sortOrder char(50), 
  in _requestAssignedTo char(50), 
  in _searchKey char(50), 
  in _messageRepliedBy char(50), 
  in _recordsPerPage varchar(50),
  in _queryType char(50)
)
BEGIN 
SET 
  @selectClause := IF(
    IFNULL(_queryType, '') = "count", 
    'count(acr.id) AS cnt', 
    'acr.id AS customerrequest_id'
  );
SET 
  @stmt := CONCAT(
    'SELECT ', @selectClause, ' FROM archivedcustomerrequest acr '
  );
SET 
  @whereclause := ' WHERE true';
SET 
  @joinCustomer := 0;
IF _dateInitialPoint != '' 
AND _dateFinalPoint != '' THEN 
SET 
  @whereclause = CONCAT(
    @whereclause, " 
    AND TIMESTAMP(DATE(acr.createdts)) >= ", 
    quote(_dateInitialPoint), " 
    AND ", 
    " TIMESTAMP(DATE(acr.createdts)) <= ", quote(_dateFinalPoint)
  );
ELSEIF _dateInitialPoint != '' THEN 
SET 
  @whereclause = CONCAT(
    @whereclause, " 
    AND TIMESTAMP(DATE(acr.createdts)) = ", 
    quote(_dateInitialPoint)
  );
ELSEIF _dateFinalPoint != '' THEN 
SET 
  @whereclause = CONCAT(
    @whereclause, " 
    AND TIMESTAMP(DATE(acr.createdts)) = ", 
    quote(_dateFinalPoint)
  );
END IF;
IF _requestStatusID != '' THEN 
SET 
  @whereclause = CONCAT(
    @whereclause, " 
    AND acr.Status_id IN 
    (
      ", 
    func_escape_input_for_in_operator(_requestStatusID), "
    )
    "
  );
END IF;
IF _requestCategory != '' THEN 
SET 
  @whereclause = CONCAT(
    @whereclause, " 
    AND acr.RequestCategory_id = ", 
    quote(_requestCategory)
  );
END IF;
IF _requestAssignedTo != '' THEN 
SET 
  @whereclause = CONCAT(
    @whereclause, " 
    AND acr.AssignedTo = ", 
    quote(_requestAssignedTo)
  );
END IF;
IF _messageRepliedBy != '' THEN 
SET 
  @stmt = CONCAT(
    @stmt, 'LEFT JOIN archivedrequestmessage ON (acr.id = archivedrequestmessage.CustomerRequest_id) '
  );
SET 
  @whereclause = CONCAT(
    @whereclause, " 
  AND archivedrequestmessage.RepliedBy_id = ", 
    quote(_messageRepliedBy)
  );
END IF;
IF _searchKey != '' THEN 
SET 
  @joinCustomer = 1;
SET _searchKey=CONCAT("%",_searchKey,"%");
SET 
  @whereclause = CONCAT(
    @whereclause, " 
  AND 
  (
    acr.Customer_id LIKE ", 
    quote(_searchKey), " OR acr.id LIKE ", 
    quote(_searchKey), " OR customer.UserName LIKE ", 
    quote(_searchKey), ")"
  );
END IF;
IF _queryType != 'count' THEN IF _sortCriteria = 'customer_Fullname' THEN 
SET 
  @joinCustomer = 1;
END IF;

SET @sortColumn='cr.lastmodifiedts';
IF( _sortCriteria = 'customerrequest_Customer_id') THEN
    SET @sortColumn = 'acr.Customer_id';
ELSEIF( _sortCriteria = 'customerrequest_AssignedTo') THEN
    SET @sortColumn = 'acr.AssignedTo';
ELSEIF( _sortCriteria = 'customerrequest_createdts') THEN
    SET @sortColumn = 'acr.createdts';
ELSEIF( _sortCriteria = 'customerrequest_RequestCategory_id') THEN
    SET @sortColumn = 'acr.RequestCategory_id';
ELSEIF( _sortCriteria = 'customer_Fullname') THEN
    SET @sortColumn = 'CONCAT(customer.FirstName,customer.LastName)';
ELSEIF( _sortCriteria = 'customerrequest_Status_id') THEN
    SET @sortColumn = 'acr.Status_id';
ELSEIF( _sortCriteria = 'customerrequest_AssignedTo_Name') THEN
    SET @sortColumn = 'CONCAT(systemuser.FirstName,systemuser.LastName)';
END IF;

SET 
  @whereclause = CONCAT(
    @whereclause, 
    " 
  ORDER BY
    ", 
    IF(
      @sortColumn = '', 'acr.lastmodifiedts', 
      @sortColumn
    ), 
    ' ', 
    IF(
      @sortColumn = '' 
      OR _sortOrder = '', 
      'DESC', 
      _sortOrder
    )
  );
SET 
  @whereclause = CONCAT(
    @whereclause, 
    " LIMIT ", 
    IF(_offset = '', '0', _offset),
    ',',
    IF(_recordsPerPage = '', '10', _recordsPerPage)
  );
END IF;
IF @joinCustomer = 1 THEN 
SET 
  @stmt = CONCAT(
    @stmt, 'LEFT JOIN customer ON (acr.Customer_id = customer.id) '
  );
END IF;
IF _sortCriteria = 'customerrequest_AssignedTo_Name' THEN 
SET 
  @stmt = CONCAT(
    @stmt, 'LEFT JOIN systemuser ON (acr.AssignedTo = systemuser.id) '
  );
END IF;
SET 
  @stmt = CONCAT(@stmt, @whereclause);
PREPARE stmt 
FROM 
  @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `customer_request_search_proc`;
DELIMITER $$
CREATE PROCEDURE `customer_request_search_proc`(
  in _dateInitialPoint varchar(50), 
  in _dateFinalPoint varchar(50), 
  in _requestStatusID varchar(50), 
  in _requestCategory varchar(50), 
  in _offset varchar(50), 
  in _sortCriteria varchar(50), 
  in _sortOrder varchar(50), 
  in _requestAssignedTo varchar(50), 
  in _searchKey varchar(50), 
  in _messageRepliedBy varchar(50), 
  in _recordsPerPage varchar(50),
  in _queryType varchar(50)
)
BEGIN 
SET 
  @selectClause := IF(
    IFNULL(_queryType, '') = "count", 
    'count(cr.id) AS cnt', 
    'cr.id AS customerrequest_id'
  );
SET 
  @stmt := CONCAT(
    'SELECT ', @selectClause, ' FROM customerrequest cr '
  );
SET 
  @whereclause := ' WHERE true';
SET 
  @joinCustomer := 0;
IF _dateInitialPoint != '' 
AND _dateFinalPoint != '' THEN 
SET 
  @whereclause = CONCAT(
    @whereclause, " 
  AND TIMESTAMP(DATE(cr.createdts)) >= ", 
    quote(_dateInitialPoint), "
  AND ", 
    " TIMESTAMP(DATE(cr.createdts)) <= ",quote(_dateFinalPoint)
  );
ELSEIF _dateInitialPoint != '' THEN 
SET 
  @whereclause = CONCAT(
    @whereclause, " 
  AND TIMESTAMP(DATE(cr.createdts)) = ", 
    quote(_dateInitialPoint)
  );
ELSEIF _dateFinalPoint != '' THEN 
SET 
  @whereclause = CONCAT(
    @whereclause, " 
  AND TIMESTAMP(DATE(cr.createdts)) = ", 
    quote(_dateFinalPoint)
  );
END IF;
IF _requestStatusID != '' THEN 
SET 
  @whereclause = CONCAT(
    @whereclause, " 
  AND cr.Status_id IN 
  (
    ", 
   func_escape_input_for_in_operator(_requestStatusID), "
  )
  "
  );
END IF;
IF _requestCategory != '' THEN 
SET 
  @whereclause = CONCAT(
    @whereclause, " 
  AND cr.RequestCategory_id = ", 
    quote(_requestCategory)
  );
END IF;
IF _requestAssignedTo != '' THEN 
SET 
  @whereclause = CONCAT(
    @whereclause, " 
  AND cr.AssignedTo = ", 
    quote(_requestAssignedTo)
  );
END IF;
IF _messageRepliedBy != '' THEN 
SET 
  @stmt = CONCAT(
    @stmt, 'JOIN requestmessage ON (cr.id = requestmessage.CustomerRequest_id) '
  );
SET 
  @whereclause = CONCAT(
    @whereclause, " 
  AND requestmessage.RepliedBy_id = ", 
    quote(_messageRepliedBy)
  );
END IF;
IF _searchKey != '' THEN 
SET 
  @joinCustomer = 1;
SET _searchKey=CONCAT("%",_searchKey,"%");
SET 
  @whereclause = CONCAT(
    @whereclause, " 
  AND 
  (
    cr.Customer_id LIKE ", 
    quote(_searchKey), " OR cr.id LIKE ", 
    quote(_searchKey), " OR customer.UserName LIKE ", 
    quote(_searchKey), ")"
  );
END IF;
IF _queryType != 'count' THEN IF _sortCriteria = 'customer_Fullname' THEN 
SET 
  @joinCustomer = 1;
END IF;

SET @sortColumn='cr.lastmodifiedts';
IF( _sortCriteria = 'customerrequest_Customer_id') THEN
    SET @sortColumn = 'cr.Customer_id';
ELSEIF( _sortCriteria = 'customerrequest_AssignedTo') THEN
    SET @sortColumn = 'cr.AssignedTo';
ELSEIF( _sortCriteria = 'customerrequest_createdts') THEN
    SET @sortColumn = 'cr.createdts';
ELSEIF( _sortCriteria = 'customerrequest_RequestCategory_id') THEN
    SET @sortColumn = 'cr.RequestCategory_id';
ELSEIF( _sortCriteria = 'customer_Fullname') THEN
    SET @sortColumn = 'CONCAT(customer.FirstName,customer.LastName)';
ELSEIF( _sortCriteria = 'customerrequest_Status_id') THEN
    SET @sortColumn = 'cr.Status_id';
ELSEIF( _sortCriteria = 'customerrequest_AssignedTo_Name') THEN
    SET @sortColumn = 'CONCAT(systemuser.FirstName,systemuser.LastName)';
END IF;
  
  
SET 
  @whereclause = CONCAT(
    @whereclause, 
    " 
ORDER BY
  ", 
    IF(
      @sortColumn = '', 'cr.lastmodifiedts', 
      @sortColumn
    ), 
    ' ', 
    IF(
      @sortColumn = '' 
      OR _sortOrder = '', 
      'DESC', 
      _sortOrder
    )
  );

SET 
  @whereclause = CONCAT(
    @whereclause, 
    " LIMIT ", 
    IF(_offset = '', '0', _offset),
    ',',
    IF(_recordsPerPage = '', '10', _recordsPerPage)
  );
END IF;
IF @joinCustomer = 1 THEN 
SET 
  @stmt = CONCAT(
    @stmt, 'JOIN customer ON (cr.Customer_id = customer.id) '
  );
END IF;
IF _sortCriteria = 'customerrequest_AssignedTo_Name' THEN 
SET 
  @stmt = CONCAT(
    @stmt, 'LEFT JOIN systemuser ON (cr.AssignedTo = systemuser.id) '
  );
END IF;

SET 
  @stmt = CONCAT(@stmt, @whereclause);
PREPARE stmt 
FROM 
  @stmt;

 -- select @stmt;  
 EXECUTE stmt;
 DEALLOCATE PREPARE stmt;
END$$
DELIMITER ;

DROP FUNCTION IF EXISTS `func_escape_input_for_in_operator`;
DELIMITER $$
CREATE FUNCTION `func_escape_input_for_in_operator`(
  _input TEXT
) RETURNS text CHARSET utf8
    DETERMINISTIC
BEGIN
    DECLARE result TEXT;
    DECLARE element TEXT;
    DECLARE ctr LONG;
    SET ctr = 1;
    SET result = '';
    
    readinput: LOOP
		SET element = func_split_str(_input, ',', ctr);
		
		IF element = '' THEN
			LEAVE readinput;
		END IF;
        
        IF result != '' THEN
			set result = concat ( result ,',');
		END IF;
		
        set result = concat ( result , quote(element));        
        SET ctr = ctr + 1;
		ITERATE readinput;
    END LOOP readinput;
    
    RETURN (result);
END$$
DELIMITER ;

DROP FUNCTION IF EXISTS `func_split_str`;
DELIMITER $$
CREATE FUNCTION `func_split_str`(
  X TEXT,
  delim TEXT,
  pos INT
) RETURNS text CHARSET utf8
    READS SQL DATA
    DETERMINISTIC
RETURN REPLACE(SUBSTRING(SUBSTRING_INDEX(X, delim, pos),
       LENGTH(SUBSTRING_INDEX(X, delim, pos -1)) + 1),
       delim, '')$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `mfa_c360_feature_get_proc`;
DELIMITER $$
CREATE PROCEDURE `mfa_c360_feature_get_proc`()
BEGIN
SELECT DISTINCT feature.id AS feature_id, feature.name AS feature_name
	FROM feature 
	JOIN featureaction ON (feature.id = featureaction.Feature_id)
	WHERE featureaction.isMFAApplicable = 1 and feature.Status_id = "SID_FEATURE_ACTIVE"
	ORDER BY feature.id;

END$$
DELIMITER ;

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
        LEFT JOIN  `membergroup` ON ((`membergroup`.`id` = `customergroup`.`Group_id`)))
            
    WHERE `customer`.`id` = _customerId
    LIMIT 1;

END$$
DELIMITER ;

DROP VIEW IF EXISTS `group_features_actions_view`;
CREATE VIEW `group_features_actions_view` AS select `groupactionlimit`.`Group_id` AS `Group_id`,`groupactionlimit`.`Action_id` AS `Action_id`,`groupactionlimit`.`LimitType_id` AS `LimitType_id`,`groupactionlimit`.`value` AS `value`,`groupactionlimit`.`id` AS `groupactionlimit_id`,`featureaction`.`name` AS `Action_name`,`featureaction`.`description` AS `Action_description`,`featureaction`.`Type_id` AS `Action_Type_id`,`featureaction`.`Feature_id` AS `Feature_id`,`featureaction`.`isMFAApplicable` AS `isMFAApplicable`,`featureaction`.`isAccountLevel` AS `isAccountLevel`,`feature`.`name` AS `Feature_name`,`feature`.`description` AS `Feature_description`,`feature`.`Type_id` AS `Feature_Type_id`,`feature`.`Status_id` AS `Feature_Status_id` from (((`groupactionlimit` left join `membergroup` on((`membergroup`.`id` = `groupactionlimit`.`Group_id`))) left join `featureaction` on((`featureaction`.`id` = `groupactionlimit`.`Action_id`))) left join `feature` on((`feature`.`id` = `featureaction`.`Feature_id`)));

ALTER TABLE `feature` 
ADD COLUMN `Service_Fee` DECIMAL(20,2) NULL AFTER `Status_id`;