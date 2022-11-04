
DROP PROCEDURE IF EXISTS `alert_history_proc`;
DELIMITER $$
CREATE PROCEDURE `alert_history_proc`(
	IN _customerId varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
  )
BEGIN
 SELECT 
        `alerthistory`.`id` AS `alerthistory_id`,
        `alerthistory`.`EventId` AS `alerthistory_EventId`,
        `alerthistory`.`AlertSubTypeId` AS `alerthistory_AlertSubTypeId`,
        `alerthistory`.`AlertTypeId` AS `alerthistory_AlertTypeId`,
        `alerthistory`.`AlertCategoryId` AS `alerthistory_AlertCategoryId`,
        `alerthistory`.`Customer_Id` AS `alerthistory_Customer_Id`,
        `alerthistory`.`LanguageCode` AS `alerthistory_LanguageCode`,
        `alerthistory`.`ChannelId` AS `alerthistory_ChannelId`,
        `alerthistory`.`Status` AS `alerthistory_Status`,
        `alerthistory`.`Subject` AS `alerthistory_Subject`,
        `alerthistory`.`Message` AS `alerthistory_Message`,
        `alerthistory`.`SenderName` AS `alerthistory_SenderName`,
        `alerthistory`.`SenderEmail` AS `alerthistory_SenderEmail`,
        `alerthistory`.`ReferenceNumber` AS `alerthistory_ReferenceNumber`,
        `alerthistory`.`createdts` AS `alerthistory_sentDate`,
        `alerthistory`.`softdeleteflag` AS `alerthistory_softdeleteflag`,
        `alertsubtype`.`Name` AS `alertsubtype_Name`,
        `alertsubtype`.`Description` AS `alertsubtype_Description`,
        `channeltext`.`Description` AS `channeltext_Description`
    FROM
        (`alerthistory`
        LEFT JOIN `alertsubtype` ON (`alerthistory`.`AlertSubTypeId` = `alertsubtype`.`id`)
        LEFT JOIN `channeltext` ON (`channeltext`.`channelID` = `alerthistory`.`ChannelId`)
            AND (`channeltext`.`LanguageCode` = 'en-US')
            AND `alerthistory`.`Customer_Id` =_customerId) order by `alerthistory`.`DispatchDate` limit 800;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `customer_request_message_search_proc`;
DELIMITER $$
CREATE PROCEDURE `customer_request_message_search_proc`(
	IN _customerID varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _customerName varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _customerFirstName varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _customerMiddleName varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _customerLastName varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _customerUsername varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _messageRepliedBy varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _requestSubject varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _requestAssignedTo varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
	IN _requestCategory varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
	IN _requestID varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
	IN _requestStatusID varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
	IN _dateInitialPoint varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
	IN _dateFinalPoint varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
    )
BEGIN
 SET @queryStatement = "
		SELECT 
        `customerrequest`.`id` AS `customerrequest_id`,
        `customerrequest`.`RequestCategory_id` AS `customerrequest_RequestCategory_id`,
        `customerrequest`.`lastupdatedbycustomer` AS `customerrequest_lastupdatedbycustomer`,
        `requestcategory`.`Name` AS `requestcategory_Name`,
        `customerrequest`.`Customer_id` AS `customerrequest_Customer_id`,
        `customer`.`FirstName` AS `customer_FirstName`,
        `customer`.`MiddleName` AS `customer_MiddleName`,
        CONCAT(`customer`.`FirstName`,
                `customer`.`LastName`) AS `customer_Fullname`,
        CONCAT(`systemuser`.`FirstName`,
                `systemuser`.`LastName`) AS `customerrequest_AssignedTo_Name`,
        `customer`.`LastName` AS `customer_LastName`,
        `customer`.`UserName` AS `customer_Username`,
        `customer`.`Salutation` AS `customer_Salutation`,
        `customer`.`Gender` AS `customer_Gender`,
        `customer`.`DateOfBirth` AS `customer_DateOfBirth`,
        `customer`.`Status_id` AS `customer_Status_id`,
        `customer`.`Ssn` AS `customer_Ssn`,
        `customer`.`MaritalStatus_id` AS `customer_MaritalStatus_id`,
        `customer`.`SpouseName` AS `customer_SpouseName`,
        `customer`.`EmployementStatus_id` AS `customer_EmployementStatus_id`,
        `customer`.`IsEnrolledForOlb` AS `customer_IsEnrolledForOlb`,
        `customer`.`IsStaffMember` AS `customer_IsStaffMember`,
        `customer`.`Location_id` AS `customer_Location_id`,
        `customer`.`PreferredContactMethod` AS `customer_PreferredContactMethod`,
        `customer`.`PreferredContactTime` AS `customer_PreferredContactTime`,
        `customerrequest`.`Priority` AS `customerrequest_Priority`,
        `customerrequest`.`Status_id` AS `customerrequest_Status_id`,
        `customerrequest`.`AssignedTo` AS `customerrequest_AssignedTo`,
        `customerrequest`.`RequestSubject` AS `customerrequest_RequestSubject`,
        `customerrequest`.`Accountid` AS `customerrequest_Accountid`,
        `customerrequest`.`createdby` AS `customerrequest_createdby`,
        `customerrequest`.`modifiedby` AS `customerrequest_modifiedby`,
        `customerrequest`.`createdts` AS `customerrequest_createdts`,
        `customerrequest`.`lastmodifiedts` AS `customerrequest_lastmodifiedts`,
        `customerrequest`.`synctimestamp` AS `customerrequest_synctimestamp`,
        `customerrequest`.`softdeleteflag` AS `customerrequest_softdeleteflag`,
        `requestmessage`.`id` AS `requestmessage_id`,
        `requestmessage`.`RepliedBy` AS `requestmessage_RepliedBy`,
        `requestmessage`.`RepliedBy_Name` AS `requestmessage_RepliedBy_Name`,
        `requestmessage`.`MessageDescription` AS `requestmessage_MessageDescription`,
        `requestmessage`.`ReplySequence` AS `requestmessage_ReplySequence`,
        `requestmessage`.`IsRead` AS `requestmessage_IsRead`,
        `requestmessage`.`createdby` AS `requestmessage_createdby`,
        `requestmessage`.`modifiedby` AS `requestmessage_modifiedby`,
        `requestmessage`.`createdts` AS `requestmessage_createdts`,
        `requestmessage`.`lastmodifiedts` AS `requestmessage_lastmodifiedts`,
        `requestmessage`.`synctimestamp` AS `requestmessage_synctimestamp`,
        `requestmessage`.`softdeleteflag` AS `requestmessage_softdeleteflag`,
        `messageattachment`.`id` AS `messageattachment_id`,
        `messageattachment`.`AttachmentType_id` AS `messageattachment_AttachmentType_id`,
        `messageattachment`.`Media_id` AS `messageattachment_Media_id`,
        `messageattachment`.`createdby` AS `messageattachment_createdby`,
        `messageattachment`.`modifiedby` AS `messageattachment_modifiedby`,
        `messageattachment`.`createdts` AS `messageattachment_createdts`,
        `messageattachment`.`lastmodifiedts` AS `messageattachment_lastmodifiedts`,
        `messageattachment`.`softdeleteflag` AS `messageattachment_softdeleteflag`,
        `media`.`id` AS `media_id`,
        `media`.`Name` AS `media_Name`,
        `media`.`Size` AS `media_Size`,
        `media`.`Type` AS `media_Type`,
        `media`.`Description` AS `media_Description`,
        `media`.`Url` AS `media_Url`,
        `media`.`createdby` AS `media_createdby`,
        `media`.`modifiedby` AS `media_modifiedby`,
        `media`.`lastmodifiedts` AS `media_lastmodifiedts`,
        `media`.`synctimestamp` AS `media_synctimestamp`,
        `media`.`softdeleteflag` AS `media_softdeleteflag`
    FROM
        (`customerrequest`
        LEFT JOIN `requestmessage` ON (`customerrequest`.`id` = `requestmessage`.`CustomerRequest_id`)
        LEFT JOIN `messageattachment` ON (`requestmessage`.`id` = `messageattachment`.`RequestMessage_id`)
        LEFT JOIN `media` ON (`messageattachment`.`Media_id` = `media`.`id`)
        LEFT JOIN `customer` ON (`customerrequest`.`Customer_id` = `customer`.`id`)
        LEFT JOIN `requestcategory` ON (`customerrequest`.`RequestCategory_id` = `requestcategory`.`id`)
        LEFT JOIN `systemuser` ON (`customerrequest`.`AssignedTo` = `systemuser`.`id`))
	WHERE true ";

IF _customerID != '' THEN
		set @queryStatement = concat(@queryStatement," and customer.id = ", quote(_customerID));
  END if;

  IF _customerFirstName != '' THEN
		set @queryStatement = concat(@queryStatement," and customer.FirstName = ", quote(_customerFirstName));
  END if;

  IF _customerMiddleName != '' THEN
		set @queryStatement = concat(@queryStatement," and customer.MiddleName = ", quote(_customerMiddleName));
  END if;

  IF _customerLastName != '' THEN
		set @queryStatement = concat(@queryStatement," and customer.LastName = ", quote(_customerLastName));
  END if;

  IF _customerUsername != '' THEN
		set @queryStatement = concat(@queryStatement," and customer.LastName = ", quote(_customerUsername));
  END if;

  IF _messageRepliedBy != '' THEN
		set @queryStatement = concat(@queryStatement," and requestmessage.RepliedBy = ", quote(_messageRepliedBy));
  END if;

  IF _requestSubject != '' THEN
		set @queryStatement = concat(@queryStatement," and customerrequest.RequestSubject = ", quote(_requestSubject));
  END if;

  IF _requestAssignedTo != '' THEN
		set @queryStatement = concat(@queryStatement," and customerrequest.AssignedTo = ", quote(_requestAssignedTo));
  END if;

 IF _requestCategory != '' THEN
		set @queryStatement = concat(@queryStatement," and customerrequest.RequestCategory_id = ", quote(_requestCategory));
  END if;

   IF _requestID != '' THEN
		set @queryStatement = concat(@queryStatement," and customerrequest.id = ", quote(_requestID));
  END if;

   IF _requestStatusID != '' THEN
		set @queryStatement = concat(@queryStatement," and customerrequest.Status_id = ", quote(_requestStatusID));
  END if;
  
   IF _customerName != '' THEN
		set @queryStatement = concat(@queryStatement," and  CONCAT(`customer`.`FirstName`,`customer`.`LastName`).Status_id = ", quote(_customerName));
  END if;
  
IF  _dateInitialPoint != '' THEN
     IF LOCATE("=", _dateInitialPoint)!=0 THEN
    	set @queryStatement = concat(@queryStatement," and requestmessage.createdts = ", quote(_dateInitialPoint));
     ELSEIF LOCATE(">", _dateInitialPoint)!=0 THEN
        set @queryStatement = concat(@queryStatement," and requestmessage.createdts > ", quote(_dateInitialPoint));
     ELSEIF LOCATE("<", _dateInitialPoint)!=0 THEN
        set @queryStatement = concat(@queryStatement," and requestmessage.createdts < ", quote(_dateInitialPoint));
	 ELSEIF _dateFinalPoint != '' THEN
        set @queryStatement = concat(@queryStatement," and requestmessage.createdts > ", quote(_dateInitialPoint), " and requestmessage.createdts < ", quote(_dateFinalPoint));
	 END IF;	
 END IF;	
 		
 		set @queryStatement = concat(@queryStatement," ORDER BY `requestmessage`.`ReplySequence` DESC");
		PREPARE stmt FROM @queryStatement;
		execute stmt;
		DEALLOCATE PREPARE stmt;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `customer_request_archived_message_search_proc`;
DELIMITER $$
CREATE PROCEDURE `customer_request_archived_message_search_proc`(
	IN _customerID varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _customerName varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _customerFirstName varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _customerMiddleName varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _customerLastName varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _customerUsername varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _messageRepliedBy varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _requestSubject varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
    IN _requestAssignedTo varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
	IN _requestCategory varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
	IN _requestID varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
	IN _requestStatusID varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
	IN _dateInitialPoint varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci,
	IN _dateFinalPoint varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
    )
BEGIN
 SET @queryStatement = "
		SELECT 
        `archivedcustomerrequest`.`id` AS `customerrequest_id`,
        `archivedcustomerrequest`.`RequestCategory_id` AS `customerrequest_RequestCategory_id`,
        `archivedcustomerrequest`.`lastupdatedbycustomer` AS `customerrequest_lastupdatedbycustomer`,
        `requestcategory`.`Name` AS `requestcategory_Name`,
        `archivedcustomerrequest`.`Customer_id` AS `customerrequest_Customer_id`,
        `customer`.`FirstName` AS `customer_FirstName`,
        `customer`.`MiddleName` AS `customer_MiddleName`,
        CONCAT(`customer`.`FirstName`,
                `customer`.`LastName`) AS `customer_Fullname`,
        CONCAT(`systemuser`.`FirstName`,
                `systemuser`.`LastName`) AS `customerrequest_AssignedTo_Name`,
        `customer`.`LastName` AS `customer_LastName`,
        `customer`.`UserName` AS `customer_Username`,
        `customer`.`Salutation` AS `customer_Salutation`,
        `customer`.`Gender` AS `customer_Gender`,
        `customer`.`DateOfBirth` AS `customer_DateOfBirth`,
        `customer`.`Status_id` AS `customer_Status_id`,
        `customer`.`Ssn` AS `customer_Ssn`,
        `customer`.`MaritalStatus_id` AS `customer_MaritalStatus_id`,
        `customer`.`SpouseName` AS `customer_SpouseName`,
        `customer`.`EmployementStatus_id` AS `customer_EmployementStatus_id`,
        `customer`.`IsEnrolledForOlb` AS `customer_IsEnrolledForOlb`,
        `customer`.`IsStaffMember` AS `customer_IsStaffMember`,
        `customer`.`Location_id` AS `customer_Location_id`,
        `customer`.`PreferredContactMethod` AS `customer_PreferredContactMethod`,
        `customer`.`PreferredContactTime` AS `customer_PreferredContactTime`,
        `archivedcustomerrequest`.`Priority` AS `customerrequest_Priority`,
        `archivedcustomerrequest`.`Status_id` AS `customerrequest_Status_id`,
        `archivedcustomerrequest`.`AssignedTo` AS `customerrequest_AssignedTo`,
        `archivedcustomerrequest`.`RequestSubject` AS `customerrequest_RequestSubject`,
        `archivedcustomerrequest`.`Accountid` AS `customerrequest_Accountid`,
        `archivedcustomerrequest`.`createdby` AS `customerrequest_createdby`,
        `archivedcustomerrequest`.`modifiedby` AS `customerrequest_modifiedby`,
        `archivedcustomerrequest`.`createdts` AS `customerrequest_createdts`,
        `archivedcustomerrequest`.`lastmodifiedts` AS `customerrequest_lastmodifiedts`,
        `archivedcustomerrequest`.`synctimestamp` AS `customerrequest_synctimestamp`,
        `archivedcustomerrequest`.`softdeleteflag` AS `customerrequest_softdeleteflag`,
        `archivedrequestmessage`.`id` AS `requestmessage_id`,
        `archivedrequestmessage`.`RepliedBy` AS `requestmessage_RepliedBy`,
        `archivedrequestmessage`.`RepliedBy_Name` AS `requestmessage_RepliedBy_Name`,
        `archivedrequestmessage`.`MessageDescription` AS `requestmessage_MessageDescription`,
        `archivedrequestmessage`.`ReplySequence` AS `requestmessage_ReplySequence`,
        `archivedrequestmessage`.`IsRead` AS `requestmessage_IsRead`,
        `archivedrequestmessage`.`createdby` AS `requestmessage_createdby`,
        `archivedrequestmessage`.`modifiedby` AS `requestmessage_modifiedby`,
        `archivedrequestmessage`.`createdts` AS `requestmessage_createdts`,
        `archivedrequestmessage`.`lastmodifiedts` AS `requestmessage_lastmodifiedts`,
        `archivedrequestmessage`.`synctimestamp` AS `requestmessage_synctimestamp`,
        `archivedrequestmessage`.`softdeleteflag` AS `requestmessage_softdeleteflag`,
        `archivedmessageattachment`.`id` AS `messageattachment_id`,
        `archivedmessageattachment`.`AttachmentType_id` AS `messageattachment_AttachmentType_id`,
        `archivedmessageattachment`.`Media_id` AS `messageattachment_Media_id`,
        `archivedmessageattachment`.`createdby` AS `messageattachment_createdby`,
        `archivedmessageattachment`.`modifiedby` AS `messageattachment_modifiedby`,
        `archivedmessageattachment`.`createdts` AS `messageattachment_createdts`,
        `archivedmessageattachment`.`lastmodifiedts` AS `messageattachment_lastmodifiedts`,
        `archivedmessageattachment`.`softdeleteflag` AS `messageattachment_softdeleteflag`,
        `archivedmedia`.`id` AS `media_id`,
        `archivedmedia`.`Name` AS `media_Name`,
        `archivedmedia`.`Size` AS `media_Size`,
        `archivedmedia`.`Type` AS `media_Type`,
        `archivedmedia`.`Description` AS `media_Description`,
        `archivedmedia`.`Url` AS `media_Url`,
        `archivedmedia`.`createdby` AS `media_createdby`,
        `archivedmedia`.`modifiedby` AS `media_modifiedby`,
        `archivedmedia`.`lastmodifiedts` AS `media_lastmodifiedts`,
        `archivedmedia`.`synctimestamp` AS `media_synctimestamp`,
        `archivedmedia`.`softdeleteflag` AS `media_softdeleteflag`
    FROM
        (`archivedcustomerrequest`
        LEFT JOIN `archivedrequestmessage` ON (`archivedcustomerrequest`.`id` = `archivedrequestmessage`.`CustomerRequest_id`)
        LEFT JOIN `archivedmessageattachment` ON (`archivedrequestmessage`.`id` = `archivedmessageattachment`.`RequestMessage_id`)
        LEFT JOIN `archivedmedia` ON (`archivedmessageattachment`.`Media_id` = `archivedmedia`.`id`)
        LEFT JOIN `customer` ON (`archivedcustomerrequest`.`Customer_id` = `customer`.`id`)
        LEFT JOIN `requestcategory` ON (`archivedcustomerrequest`.`RequestCategory_id` = `requestcategory`.`id`)
        LEFT JOIN `systemuser` ON (`archivedcustomerrequest`.`AssignedTo` = `systemuser`.`id`)) 
	WHERE true ";

IF _customerID != '' THEN
		set @queryStatement = concat(@queryStatement,"customer.id = ", quote(_customerID));
  END if;

  IF _customerFirstName != '' THEN
		set @queryStatement = concat(@queryStatement," and customer.FirstName = ", quote(_customerFirstName));
  END if;

  IF _customerMiddleName != '' THEN
		set @queryStatement = concat(@queryStatement," and customer.MiddleName = ", quote(_customerMiddleName));
  END if;

  IF _customerLastName != '' THEN
		set @queryStatement = concat(@queryStatement," and customer.LastName = ", quote(_customerLastName));
  END if;

  IF _customerUsername != '' THEN
		set @queryStatement = concat(@queryStatement," and customer.LastName = ", quote(_customerUsername));
  END if;

  IF _messageRepliedBy != '' THEN
		set @queryStatement = concat(@queryStatement," and archivedrequestmessage.RepliedBy = ", quote(_messageRepliedBy));
  END if;

  IF _requestSubject != '' THEN
		set @queryStatement = concat(@queryStatement," and archivedcustomerrequest.RequestSubject = ", quote(_requestSubject));
  END if;

  IF _requestAssignedTo != '' THEN
		set @queryStatement = concat(@queryStatement," and archivedcustomerrequest.AssignedTo = ", quote(_requestAssignedTo));
  END if;

 IF _requestCategory != '' THEN
		set @queryStatement = concat(@queryStatement," and archivedcustomerrequest.RequestCategory_id = ", quote(_requestCategory));
  END if;

   IF _requestID != '' THEN
		set @queryStatement = concat(@queryStatement," and archivedcustomerrequest.id = ", quote(_requestID));
  END if;

   IF _requestStatusID != '' THEN
		set @queryStatement = concat(@queryStatement," and archivedcustomerrequest.Status_id = ", quote(_requestStatusID));
  END if;
  
   IF _customerName != '' THEN
		set @queryStatement = concat(@queryStatement," and  CONCAT(`customer`.`FirstName`,`customer`.`LastName`).Status_id = ", quote(_customerName));
  END if;
  
IF  _dateInitialPoint != '' THEN
     IF LOCATE("=", _dateInitialPoint)!=0 THEN
    	set @queryStatement = concat(@queryStatement," and archivedrequestmessage.createdts = ", quote(_dateInitialPoint));
     ELSEIF LOCATE(">", _dateInitialPoint)!=0 THEN
        set @queryStatement = concat(@queryStatement," and archivedrequestmessage.createdts > ", quote(_dateInitialPoint));
     ELSEIF LOCATE("<", _dateInitialPoint)!=0 THEN
        set @queryStatement = concat(@queryStatement," and archivedrequestmessage.createdts < ", quote(_dateInitialPoint));
	 ELSEIF _dateFinalPoint != '' THEN
        set @queryStatement = concat(@queryStatement," and archivedrequestmessage.createdts > ", quote(_dateInitialPoint), " and requestmessage.createdts < ", quote(_dateFinalPoint));
	 END IF;	
 END IF;	
		
 		set @queryStatement = concat(@queryStatement," ORDER BY `archivedrequestmessage`.`ReplySequence` DESC");
 		PREPARE stmt FROM @queryStatement;
		execute stmt;
		DEALLOCATE PREPARE stmt;
END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `customer_action_group_action_limits_proc`(
in _customerID varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
)
BEGIN
SELECT 
        `customeractionlimit`.`Customer_id` AS `Customer_id`,
        `customeractionlimit`.`LimitType_id` AS `Customer_LimitType_id`,
        CONCAT('', `customeractionlimit`.`Value`) AS `Customer_Limit_Value`,
        NULL AS `Group_LimitType_id`,
        NULL AS `Group_Limit_Value`,
        `service`.`id` AS `Action_id`,
        `service`.`Name` AS `Action_Name`,
        `service`.`code` AS `Action_Code`,
        `service`.`Description` AS `Action_Description`,
        `service`.`Status_id` AS `Action_Status_id`,
        `app`.`id` AS `App_id`,
        `app`.`Name` AS `App_Name`,
        `appactionlimit`.`LimitType_id` AS `LimitType_id`,
        CONCAT('', `appactionlimit`.`Value`) AS `Limit_Value`,
        `feature`.`id` AS `Feature_id`,
        `feature`.`Name` AS `Feature_Name`,
        `feature`.`code` AS `Feature_Code`,
        `feature`.`Description` AS `Feature_Description`,
        `feature`.`Status_id` AS `Feature_Status_id`
    FROM
        (((((`customeractionlimit`
        LEFT JOIN `service` ON ((`service`.`id` = `customeractionlimit`.`Action_id`)))
        LEFT JOIN `feature` ON ((`feature`.`id` = `service`.`Feature_id`)))
        LEFT JOIN `appaction` ON ((`service`.`id` = `appaction`.`Action_id`)))
        LEFT JOIN `app` ON ((`appaction`.`App_id` = `app`.`id`)))
        LEFT JOIN `appactionlimit` ON ((`appaction`.`id` = `appactionlimit`.`AppAction_id`))) 
	where `customeractionlimit`.`Customer_id` = _customerID
    UNION SELECT 
        `customergroup`.`Customer_id` AS `Customer_id`,
        NULL AS `Customer_LimitType_id`,
        NULL AS `Customer_Limit_Value`,
        `groupactionlimit`.`LimitType_id` AS `Group_LimitType_id`,
        CONCAT('', `groupactionlimit`.`Value`) AS `Group_Limit_Value`,
        `service`.`id` AS `Action_id`,
        `service`.`Name` AS `Action_Name`,
        `service`.`code` AS `Action_Code`,
        `service`.`Description` AS `Action_Description`,
        `service`.`Status_id` AS `Action_Status_id`,
        `app`.`id` AS `App_id`,
        `app`.`Name` AS `App_Name`,
        `appactionlimit`.`LimitType_id` AS `LimitType_id`,
        CONCAT('', `appactionlimit`.`Value`) AS `Limit_Value`,
        `feature`.`id` AS `Feature_id`,
        `feature`.`Name` AS `Feature_Name`,
        `feature`.`code` AS `Feature_Code`,
        `feature`.`Description` AS `Feature_Description`,
        `feature`.`Status_id` AS `Feature_Status_id`
    FROM
        ((((((`customergroup`
        LEFT JOIN `groupactionlimit` ON ((`groupactionlimit`.`Group_id` = `customergroup`.`Group_id`)))
        LEFT JOIN `service` ON ((`service`.`id` = `groupactionlimit`.`Action_id`)))
        LEFT JOIN `feature` ON ((`feature`.`id` = `service`.`Feature_id`)))
        LEFT JOIN `appaction` ON ((`service`.`id` = `appaction`.`Action_id`)))
        LEFT JOIN `app` ON ((`appaction`.`App_id` = `app`.`id`)))
        LEFT JOIN `appactionlimit` ON ((`appaction`.`id` = `appactionlimit`.`AppAction_id`)))
	where `customergroup`.`Customer_id` = _customerID and `service`.`Status_id` != 'SID_INACTIVE' and `feature`.`Status_id` != 'SID_INACTIVE';
END$$
DELIMITER ;

DROP VIEW IF EXISTS `customer_action_group_action_limits_view`;

DELIMITER $$
CREATE PROCEDURE `requestmessages_proc`(
in _customerRequestID varchar(50)  CHARACTER SET UTF8 COLLATE utf8_general_ci,
in _fetchDraftMessages tinyint(1) 
)
BEGIN

    SELECT 
        `requestmessage`.`id` AS `Message_id`,
        (SELECT 
                COUNT(`messageattachment`.`id`)
            FROM
                `messageattachment`
            WHERE
                (`messageattachment`.`RequestMessage_id` = `requestmessage`.`id`)) AS `totalAttachments`,
        `messageattachment`.`id` AS `Messageattachment_id`,
        `messageattachment`.`Media_id` AS `Media_id`,
        `media`.`Name` AS `Media_Name`,
        `media`.`Type` AS `Media_Type`,
        `media`.`Size` AS `Media_Size`,
        `requestmessage`.`CustomerRequest_id` AS `CustomerRequest_id`,
        `requestmessage`.`RepliedBy` AS `RepliedBy`,
        `requestmessage`.`MessageDescription` AS `MessageDescription`,
        `requestmessage`.`ReplySequence` AS `ReplySequence`,
        `requestmessage`.`IsRead` AS `IsRead`,
        `requestmessage`.`createdby` AS `createdby`,
        `requestmessage`.`RepliedBy_Name` AS `createdby_name`,
        `requestmessage`.`modifiedby` AS `modifiedby`,
        `requestmessage`.`createdts` AS `createdts`,
        `requestmessage`.`lastmodifiedts` AS `lastmodifiedts`,
        `requestmessage`.`synctimestamp` AS `synctimestamp`,
        `requestmessage`.`softdeleteflag` AS `softdeleteflag`
    FROM
        ((`requestmessage`
        LEFT JOIN `messageattachment` ON ((`requestmessage`.`id` = `messageattachment`.`RequestMessage_id`)))
        LEFT JOIN `media` ON ((`messageattachment`.`Media_id` = `media`.`id`)))
	WHERE `requestmessage`.`CustomerRequest_id` = _customerRequestID and If(_fetchDraftMessages = 1, true, 
    `requestmessage`.`IsRead` != 'draft' )
    ORDER BY `requestmessage`.`ReplySequence` asc;

END$$
DELIMITER ;

DROP VIEW IF EXISTS `requestmessages_view`;
