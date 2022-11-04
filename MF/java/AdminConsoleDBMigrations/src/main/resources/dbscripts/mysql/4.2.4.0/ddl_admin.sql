ALTER TABLE `customerentitlement` 
ADD COLUMN `MaxTransactionLimit` DECIMAL(20,2) NULL AFTER `Service_id`,
ADD COLUMN `MaxDailyLimit` DECIMAL(20,2) NULL AFTER `MaxTransactionLimit`;

DROP TABLE IF EXISTS `bbcustomerservicelimit`;

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
		LEFT JOIN `service` ON (`service`.`id` = `customerentitlement`.`Service_id`)
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
    	LEFT JOIN `groupentitlement` ON (`groupentitlement`.`Group_id` = `customergroup`.`Group_id`)
		LEFT JOIN `service` ON (`service`.`id` = `groupentitlement`.`Service_id`)
	WHERE `customergroup`.`Customer_id` = _customerID;
    
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `reports_messages_received`;
DELIMITER $$
CREATE PROCEDURE `reports_messages_received`(
  in from_date VARCHAR(19), 
  in to_date VARCHAR(19), 
  in category_id VARCHAR(50), 
  in csr_name VARCHAR(50)
)
BEGIN 
set 
  @queryStatement = "
  SELECT
    count(*) messages_received_count 
  FROM
    requestmessage 
  where
    createdby not in 
    (
      select
        Username 
      from
        systemuser
    )
    ";
if from_date != '' 
and from_date != '' then 
set 
  @queryStatement = concat(
    @queryStatement, 
    "
    and createdts >= ", 
    quote(STR_TO_DATE(from_date, '%m/%d/%Y %H:%i:%s')), 
    " and createdts <= ", 
    quote(STR_TO_DATE(to_date, '%m/%d/%Y %H:%i:%s'))
  );
end if;
if category_id != '' then 
set 
  @queryStatement = concat(
    @queryStatement, " 
    and CustomerRequest_id in 
    (
      select
        id 
      from
        customerrequest 
      where
        RequestCategory_id = ", 
    quote(category_id), "
    )
    "
  );
end if;
if csr_name != '' then 
set 
  @queryStatement = concat(
    @queryStatement, " 
    and RepliedBy_id = ", 
    quote(csr_name)
  );
end if;
PREPARE stmt 
FROM 
  @queryStatement;
EXECUTE stmt;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `reports_messages_sent`;
DELIMITER $$
CREATE PROCEDURE `reports_messages_sent`(
  in from_date VARCHAR(19), 
  in to_date VARCHAR(19), 
  in category_id VARCHAR(50), 
  in csr_name VARCHAR(50)
)
BEGIN 
set 
  @queryStatement = "
    SELECT
      count(*) messages_sent_count 
    FROM
      requestmessage 
    where
      createdby in 
      (
        select
          Username 
        from
          systemuser
      )
      ";
if from_date != '' 
and from_date != '' then 
set 
  @queryStatement = concat(
    @queryStatement, 
    "
      and createdts >= ", 
    quote(STR_TO_DATE(from_date, '%m/%d/%Y %H:%i:%s')), 
    " and createdts <= ", 
    quote(STR_TO_DATE(to_date, '%m/%d/%Y %H:%i:%s'))
  );
end if;
if category_id != '' then 
set 
  @queryStatement = concat(
    @queryStatement, " 
      and CustomerRequest_id in 
      (
        select
          id 
        from
          customerrequest 
        where
          RequestCategory_id = ", 
    quote(category_id), "
      )
      "
  );
end if;
if csr_name != '' then 
set 
  @queryStatement = concat(
    @queryStatement, " 
      and RepliedBy_id = ", 
    quote(csr_name)
  );
end if;
PREPARE stmt 
FROM 
  @queryStatement;
EXECUTE stmt;
END$$
DELIMITER ;


DROP PROCEDURE IF EXISTS `reports_threads_averageage`;
DELIMITER $$
CREATE PROCEDURE `reports_threads_averageage`(
  in from_date VARCHAR(19), 
  in to_date VARCHAR(19), 
  in category_id VARCHAR(50), 
  in csr_name VARCHAR(50)
)
BEGIN 
set 
  @queryStatement = "
      select
        AVG(thread_life_records.thread_life) threads_averageage_count 
      from
        (
          select
            DATEDIFF(MAX(createdts), MIN(createdts)) thread_life 
          from
            requestmessage 
          where
            true ";
if from_date != '' 
and from_date != '' then 
set 
  @queryStatement = concat(
    @queryStatement, 
    " 
            and createdts >= ", 
    quote(STR_TO_DATE(from_date, '%m/%d/%Y %H:%i:%s')), 
    " and createdts <= ", 
    quote(STR_TO_DATE(to_date, '%m/%d/%Y %H:%i:%s'))

  );
end if;
if category_id != '' then 
set 
  @queryStatement = concat(
    @queryStatement, " 
            and CustomerRequest_id in 
            (
              select
                id 
              from
                customerrequest 
              where
                RequestCategory_id = ", 
    quote(category_id), "
            )
            "
  );
end if;
if csr_name != '' then 
set 
  @queryStatement = concat(
    @queryStatement, " 
            and RepliedBy_id = ", 
    quote(csr_name)
  );
end if;
set 
  @queryStatement = concat(
    @queryStatement, " 
          group by
            CustomerRequest_id) thread_life_records"
  );
PREPARE stmt 
FROM 
  @queryStatement;
EXECUTE stmt;
END$$
DELIMITER ;


DROP PROCEDURE IF EXISTS `reports_threads_new`;
DELIMITER $$
CREATE PROCEDURE `reports_threads_new`(
  in from_date VARCHAR(19), 
  in to_date VARCHAR(19), 
  in category_id VARCHAR(50), 
  in csr_name VARCHAR(50)
)
BEGIN 
set 
  @queryStatement = "
            SELECT
              count(*) threads_new_count 
            from
              customerrequest 
            where
              Status_id = 'SID_OPEN'";
if from_date != '' 
and from_date != '' then 
set 
  @queryStatement = concat(
    @queryStatement, 
    " 
              and createdts >= ", 
    quote(STR_TO_DATE(from_date, '%m/%d/%Y %H:%i:%s')), 
    " and createdts <= ", 
    quote(STR_TO_DATE(to_date, '%m/%d/%Y %H:%i:%s'))
  );
end if;
if category_id != '' then 
set 
  @queryStatement = concat(
    @queryStatement, " 
              and RequestCategory_id = ", 
    quote(category_id)
  );
end if;
if csr_name != '' then 
set 
  @queryStatement = concat(
    @queryStatement, " 
              and AssignedTo = ", 
    quote(csr_name)
  );
end if;
PREPARE stmt 
FROM 
  @queryStatement;
EXECUTE stmt;
END$$
DELIMITER ;


DROP PROCEDURE IF EXISTS `reports_threads_resolved`;
DELIMITER $$
CREATE PROCEDURE `reports_threads_resolved`(
  in from_date VARCHAR(19), 
  in to_date VARCHAR(19), 
  in category_id VARCHAR(50), 
  in csr_name VARCHAR(50)
)
BEGIN 
set 
  @queryStatement = "
              SELECT
                count(*) threads_resolved_count 
              from
                customerrequest 
              where
                Status_id = 'SID_RESOLVED'";
if from_date != '' 
and from_date != '' then 
set 
  @queryStatement = concat(
    @queryStatement, 
    " 
                and createdts >= ", 
    quote(STR_TO_DATE(from_date, '%m/%d/%Y %H:%i:%s')), 
    " and createdts <= ", 
    quote(STR_TO_DATE(to_date, '%m/%d/%Y %H:%i:%s'))
  );
end if;
if category_id != '' then 
set 
  @queryStatement = concat(
    @queryStatement, " 
                and RequestCategory_id = ", 
    quote(category_id)
  );
end if;
if csr_name != '' then 
set 
  @queryStatement = concat(
    @queryStatement, " 
                and AssignedTo = ", 
    quote(csr_name)
  );
end if;
PREPARE stmt 
FROM 
  @queryStatement;
EXECUTE stmt;
END$$
DELIMITER ;

ALTER TABLE `termsandconditions` CHANGE COLUMN `Description` `Description` LONGTEXT NULL DEFAULT NULL ;

DROP PROCEDURE IF EXISTS `customer_unread_message_count_proc`;
DELIMITER $$
CREATE PROCEDURE `customer_unread_message_count_proc`(
IN _customerId  varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci
)
BEGIN
	 SELECT 
        COUNT(`requestmessage`.`id`) AS `messageCount`
    FROM
        (`customerrequest`
        JOIN `requestmessage` ON ((`requestmessage`.`CustomerRequest_id` = `customerrequest`.`id`)))
    WHERE
        (`requestmessage`.`IsRead` = 'FALSE' and `customerrequest`.`softdeleteflag`= 0 and `customerrequest`.`Customer_id`= _customerId 
        and `customerrequest`.`Status_id` != 'SID_DELETED');
END$$
DELIMITER ;

DROP VIEW IF EXISTS customer_unread_message_count_view;

DROP VIEW IF EXISTS archivedcustomer_request_detailed_view;
CREATE VIEW `archivedcustomer_request_detailed_view` AS select `archivedcustomerrequest`.`id` AS `customerrequest_id`,`archivedcustomerrequest`.`RequestCategory_id` AS `customerrequest_RequestCategory_id`,`archivedcustomerrequest`.`lastupdatedbycustomer` AS `customerrequest_lastupdatedbycustomer`,`requestcategory`.`Name` AS `requestcategory_Name`,`archivedcustomerrequest`.`Customer_id` AS `customerrequest_Customer_id`,`customer`.`FirstName` AS `customer_FirstName`,`customer`.`MiddleName` AS `customer_MiddleName`,concat(`customer`.`FirstName`,`customer`.`LastName`) AS `customer_Fullname`,`customer`.`LastName` AS `customer_LastName`,`customer`.`UserName` AS `customer_Username`,`customer`.`Salutation` AS `customer_Salutation`,`customer`.`Gender` AS `customer_Gender`,`customer`.`DateOfBirth` AS `customer_DateOfBirth`,`customer`.`Status_id` AS `customer_Status_id`,NULL AS `customer_Ssn`,`customer`.`MaritalStatus_id` AS `customer_MaritalStatus_id`,`customer`.`SpouseName` AS `customer_SpouseName`,`customer`.`EmployementStatus_id` AS `customer_EmployementStatus_id`,`customer`.`IsEnrolledForOlb` AS `customer_IsEnrolledForOlb`,`customer`.`IsStaffMember` AS `customer_IsStaffMember`,`customer`.`Location_id` AS `customer_Location_id`,`customer`.`PreferredContactMethod` AS `customer_PreferredContactMethod`,`customer`.`PreferredContactTime` AS `customer_PreferredContactTime`,`archivedcustomerrequest`.`Priority` AS `customerrequest_Priority`,`archivedcustomerrequest`.`Status_id` AS `customerrequest_Status_id`,`archivedcustomerrequest`.`AssignedTo` AS `customerrequest_AssignedTo`,`archivedcustomerrequest`.`RequestSubject` AS `customerrequest_RequestSubject`,`archivedcustomerrequest`.`Accountid` AS `customerrequest_Accountid`,`archivedcustomerrequest`.`createdby` AS `customerrequest_createdby`,`archivedcustomerrequest`.`modifiedby` AS `customerrequest_modifiedby`,`archivedcustomerrequest`.`createdts` AS `customerrequest_createdts`,`archivedcustomerrequest`.`lastmodifiedts` AS `customerrequest_lastmodifiedts`,`archivedcustomerrequest`.`synctimestamp` AS `customerrequest_synctimestamp`,`archivedcustomerrequest`.`softdeleteflag` AS `customerrequest_softdeleteflag`,`archivedrequestmessage`.`id` AS `requestmessage_id`,`archivedrequestmessage`.`RepliedBy` AS `requestmessage_RepliedBy`,`archivedrequestmessage`.`RepliedBy_Name` AS `requestmessage_RepliedBy_Name`,`archivedrequestmessage`.`MessageDescription` AS `requestmessage_MessageDescription`,`archivedrequestmessage`.`ReplySequence` AS `requestmessage_ReplySequence`,`archivedrequestmessage`.`IsRead` AS `requestmessage_IsRead`,`archivedrequestmessage`.`createdby` AS `requestmessage_createdby`,`archivedrequestmessage`.`modifiedby` AS `requestmessage_modifiedby`,`archivedrequestmessage`.`createdts` AS `requestmessage_createdts`,`archivedrequestmessage`.`lastmodifiedts` AS `requestmessage_lastmodifiedts`,`archivedrequestmessage`.`synctimestamp` AS `requestmessage_synctimestamp`,`archivedrequestmessage`.`softdeleteflag` AS `requestmessage_softdeleteflag`,`archivedmessageattachment`.`id` AS `messageattachment_id`,`archivedmessageattachment`.`AttachmentType_id` AS `messageattachment_AttachmentType_id`,`archivedmessageattachment`.`Media_id` AS `messageattachment_Media_id`,`archivedmessageattachment`.`createdby` AS `messageattachment_createdby`,`archivedmessageattachment`.`modifiedby` AS `messageattachment_modifiedby`,`archivedmessageattachment`.`createdts` AS `messageattachment_createdts`,`archivedmessageattachment`.`lastmodifiedts` AS `messageattachment_lastmodifiedts`,`archivedmessageattachment`.`softdeleteflag` AS `messageattachment_softdeleteflag`,`archivedmedia`.`id` AS `media_id`,`archivedmedia`.`Name` AS `media_Name`,`archivedmedia`.`Size` AS `media_Size`,`archivedmedia`.`Type` AS `media_Type`,`archivedmedia`.`Description` AS `media_Description`,`archivedmedia`.`Url` AS `media_Url`,`archivedmedia`.`createdby` AS `media_createdby`,`archivedmedia`.`modifiedby` AS `media_modifiedby`,`archivedmedia`.`lastmodifiedts` AS `media_lastmodifiedts`,`archivedmedia`.`synctimestamp` AS `media_synctimestamp`,`archivedmedia`.`softdeleteflag` AS `media_softdeleteflag` from (((((`archivedcustomerrequest` left join `archivedrequestmessage` on((`archivedcustomerrequest`.`id` = `archivedrequestmessage`.`CustomerRequest_id`))) left join `archivedmessageattachment` on((`archivedrequestmessage`.`id` = `archivedmessageattachment`.`RequestMessage_id`))) left join `archivedmedia` on((`archivedmessageattachment`.`Media_id` = `archivedmedia`.`id`))) left join `customer` on((`archivedcustomerrequest`.`Customer_id` = `customer`.`id`))) left join `requestcategory` on((`archivedcustomerrequest`.`RequestCategory_id` = `requestcategory`.`id`)));

DROP VIEW IF EXISTS customer_request_detailed_view;
CREATE VIEW `customer_request_detailed_view` AS select `customerrequest`.`id` AS `customerrequest_id`,`customerrequest`.`RequestCategory_id` AS `customerrequest_RequestCategory_id`,`customerrequest`.`lastupdatedbycustomer` AS `customerrequest_lastupdatedbycustomer`,`requestcategory`.`Name` AS `requestcategory_Name`,`customerrequest`.`Customer_id` AS `customerrequest_Customer_id`,`customer`.`FirstName` AS `customer_FirstName`,`customer`.`MiddleName` AS `customer_MiddleName`,concat(`customer`.`FirstName`,`customer`.`LastName`) AS `customer_Fullname`,concat(`systemuser`.`FirstName`,`systemuser`.`LastName`) AS `customerrequest_AssignedTo_Name`,`customer`.`LastName` AS `customer_LastName`,`customer`.`UserName` AS `customer_Username`,`customer`.`Salutation` AS `customer_Salutation`,`customer`.`Gender` AS `customer_Gender`,`customer`.`DateOfBirth` AS `customer_DateOfBirth`,`customer`.`Status_id` AS `customer_Status_id`,NULL AS `customer_Ssn`,`customer`.`MaritalStatus_id` AS `customer_MaritalStatus_id`,`customer`.`SpouseName` AS `customer_SpouseName`,`customer`.`EmployementStatus_id` AS `customer_EmployementStatus_id`,`customer`.`IsEnrolledForOlb` AS `customer_IsEnrolledForOlb`,`customer`.`IsStaffMember` AS `customer_IsStaffMember`,`customer`.`Location_id` AS `customer_Location_id`,`customer`.`PreferredContactMethod` AS `customer_PreferredContactMethod`,`customer`.`PreferredContactTime` AS `customer_PreferredContactTime`,`customerrequest`.`Priority` AS `customerrequest_Priority`,`customerrequest`.`Status_id` AS `customerrequest_Status_id`,`customerrequest`.`AssignedTo` AS `customerrequest_AssignedTo`,`customerrequest`.`RequestSubject` AS `customerrequest_RequestSubject`,`customerrequest`.`Accountid` AS `customerrequest_Accountid`,`customerrequest`.`createdby` AS `customerrequest_createdby`,`customerrequest`.`modifiedby` AS `customerrequest_modifiedby`,`customerrequest`.`createdts` AS `customerrequest_createdts`,`customerrequest`.`lastmodifiedts` AS `customerrequest_lastmodifiedts`,`customerrequest`.`synctimestamp` AS `customerrequest_synctimestamp`,`customerrequest`.`softdeleteflag` AS `customerrequest_softdeleteflag`,`requestmessage`.`id` AS `requestmessage_id`,`requestmessage`.`RepliedBy` AS `requestmessage_RepliedBy`,`requestmessage`.`RepliedBy_Name` AS `requestmessage_RepliedBy_Name`,`requestmessage`.`MessageDescription` AS `requestmessage_MessageDescription`,`requestmessage`.`ReplySequence` AS `requestmessage_ReplySequence`,`requestmessage`.`IsRead` AS `requestmessage_IsRead`,`requestmessage`.`createdby` AS `requestmessage_createdby`,`requestmessage`.`modifiedby` AS `requestmessage_modifiedby`,`requestmessage`.`createdts` AS `requestmessage_createdts`,`requestmessage`.`lastmodifiedts` AS `requestmessage_lastmodifiedts`,`requestmessage`.`synctimestamp` AS `requestmessage_synctimestamp`,`requestmessage`.`softdeleteflag` AS `requestmessage_softdeleteflag`,`messageattachment`.`id` AS `messageattachment_id`,`messageattachment`.`AttachmentType_id` AS `messageattachment_AttachmentType_id`,`messageattachment`.`Media_id` AS `messageattachment_Media_id`,`messageattachment`.`createdby` AS `messageattachment_createdby`,`messageattachment`.`modifiedby` AS `messageattachment_modifiedby`,`messageattachment`.`createdts` AS `messageattachment_createdts`,`messageattachment`.`lastmodifiedts` AS `messageattachment_lastmodifiedts`,`messageattachment`.`softdeleteflag` AS `messageattachment_softdeleteflag`,`media`.`id` AS `media_id`,`media`.`Name` AS `media_Name`,`media`.`Size` AS `media_Size`,`media`.`Type` AS `media_Type`,`media`.`Description` AS `media_Description`,`media`.`Url` AS `media_Url`,`media`.`createdby` AS `media_createdby`,`media`.`modifiedby` AS `media_modifiedby`,`media`.`lastmodifiedts` AS `media_lastmodifiedts`,`media`.`synctimestamp` AS `media_synctimestamp`,`media`.`softdeleteflag` AS `media_softdeleteflag` from ((((((`customerrequest` left join `requestmessage` on((`customerrequest`.`id` = `requestmessage`.`CustomerRequest_id`))) left join `messageattachment` on((`requestmessage`.`id` = `messageattachment`.`RequestMessage_id`))) left join `media` on((`messageattachment`.`Media_id` = `media`.`id`))) left join `customer` on((`customerrequest`.`Customer_id` = `customer`.`id`))) left join `requestcategory` on((`customerrequest`.`RequestCategory_id` = `requestcategory`.`id`))) left join `systemuser` on((`customerrequest`.`AssignedTo` = `systemuser`.`id`)));

DROP VIEW IF EXISTS customerbasicinfo_view;
CREATE VIEW `customerbasicinfo_view` AS select `customer`.`UserName` AS `Username`,`customer`.`FirstName` AS `FirstName`,`customer`.`MiddleName` AS `MiddleName`,`customer`.`LastName` AS `LastName`,concat(ifnull(`customer`.`FirstName`,''),' ',ifnull(`customer`.`MiddleName`,''),' ',ifnull(`customer`.`LastName`,'')) AS `Name`,`customer`.`Salutation` AS `Salutation`,`customer`.`id` AS `Customer_id`,concat('****',right(`customer`.`Ssn`,4)) AS `SSN`,`customer`.`createdts` AS `CustomerSince`,`customer`.`Gender` AS `Gender`,`customer`.`DateOfBirth` AS `DateOfBirth`,`customer`.`Status_id` AS `CustomerStatus_id`,`customerstatus`.`Description` AS `CustomerStatus_name`,`customer`.`MaritalStatus_id` AS `MaritalStatus_id`,`maritalstatus`.`Description` AS `MaritalStatus_name`,`customer`.`SpouseName` AS `SpouseName`,`customer`.`lockedOn` AS `lockedOn`,`customer`.`lockCount` AS `lockCount`,`customer`.`EmployementStatus_id` AS `EmployementStatus_id`,`employementstatus`.`Description` AS `EmployementStatus_name`,(select group_concat(`customerflagstatus`.`Status_id`,' ' separator ',') from `customerflagstatus` where (`customerflagstatus`.`Customer_id` = `customer`.`id`)) AS `CustomerFlag_ids`,(select group_concat(`status`.`Description`,' ' separator ',') from `status` where `status`.`id` in (select `customerflagstatus`.`Status_id` from `customerflagstatus` where (`customerflagstatus`.`Customer_id` = `customer`.`id`))) AS `CustomerFlag`,`customer`.`IsEnrolledForOlb` AS `IsEnrolledForOlb`,`customer`.`IsStaffMember` AS `IsStaffMember`,`customer`.`Location_id` AS `Branch_id`,`location`.`Name` AS `Branch_name`,`location`.`Code` AS `Branch_code`,`customer`.`IsOlbAllowed` AS `IsOlbAllowed`,`customer`.`IsAssistConsented` AS `IsAssistConsented`,`customer`.`isEagreementSigned` AS `isEagreementSigned`,`customer`.`CustomerType_id` AS `CustomerType_id`,`customertype`.`Name` AS `CustomerType_Name`,`customertype`.`Description` AS `CustomerType_Description`,(select `membergroup`.`Name` from `membergroup` where `membergroup`.`id` in (select `customergroup`.`Group_id` from `customergroup` where (`customer`.`id` = `customergroup`.`Customer_id`)) limit 1) AS `Customer_Role`,(select `membergroup`.`isEAgreementActive` from `membergroup` where `membergroup`.`id` in (select `customergroup`.`Group_id` from `customergroup` where (`customer`.`id` = `customergroup`.`Customer_id`)) limit 1) AS `isEAgreementRequired`,`customer`.`Organization_Id` AS `organisation_id`,`organisation`.`Name` AS `organisation_name`,any_value(`primaryphone`.`Value`) AS `PrimaryPhoneNumber`,any_value(`primaryemail`.`Value`) AS `PrimaryEmailAddress`,`customer`.`DocumentsSubmitted` AS `DocumentsSubmitted`,`customer`.`ApplicantChannel` AS `ApplicantChannel`,`customer`.`Product` AS `Product`,`customer`.`Reason` AS `Reason` from ((((((((`customer` left join `location` on((`customer`.`Location_id` = `location`.`id`))) left join `organisation` on((`customer`.`Organization_Id` = `organisation`.`id`))) left join `customertype` on((`customer`.`CustomerType_id` = `customertype`.`id`))) left join `status` `customerstatus` on((`customer`.`Status_id` = `customerstatus`.`id`))) left join `status` `maritalstatus` on((`customer`.`MaritalStatus_id` = `maritalstatus`.`id`))) left join `status` `employementstatus` on((`customer`.`EmployementStatus_id` = `employementstatus`.`id`))) left join `customercommunication` `primaryphone` on(((`primaryphone`.`Customer_id` = `customer`.`id`) and (`primaryphone`.`isPrimary` = 1) and (`primaryphone`.`Type_id` = 'COMM_TYPE_PHONE')))) left join `customercommunication` `primaryemail` on(((`primaryemail`.`Customer_id` = `customer`.`id`) and (`primaryemail`.`isPrimary` = 1) and (`primaryemail`.`Type_id` = 'COMM_TYPE_EMAIL')))) group by `customer`.`id`;

ALTER TABLE `mfatype` 
DROP FOREIGN KEY `FK_mfatype_status_id`;
ALTER TABLE `mfatype` 
DROP COLUMN `Status_id`,
DROP INDEX `FK_mfatype_status_id_idx` ;


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
begin
	
    DECLARE _maxLockCount varchar(50);
	IF _searchType = 'GROUP_SEARCH' then

				set @queryStatement = "SELECT 
					customer.id, customer.FirstName, customer.MiddleName, customer.LastName,
					concat(IFNULL(customer.FirstName,''), ' ', IFNULL(customer.MiddleName,''), ' ', IFNULL(customer.LastName,'')) as name,
					customer.UserName as Username, customer.Salutation, customer.Gender,
					address.City_id, city.Name as City_name,
					status.Description as customer_status,
					customer.Location_id AS branch_id,
					location.Name AS branch_name
				FROM customer
                    JOIN (
						SELECT
							customer.id
						FROM
							customer
                            LEFT JOIN customerrequest ON (customerrequest.customer_id=customer.id)
							LEFT JOIN customergroup ON (customergroup.Customer_id=customer.id)
							LEFT JOIN membergroup ON (membergroup.id=customergroup.group_id)
							LEFT JOIN customeraddress ON (customer.id=customeraddress.Customer_id AND customeraddress.isPrimary=1 and customeraddress.Type_id='ADR_TYPE_HOME')
							LEFT JOIN address ON (customeraddress.Address_id = address.id)
							LEFT JOIN city ON (city.id = address.City_id)
							LEFT JOIN customerentitlement ON (customerentitlement.Customer_id=customer.id)
							LEFT JOIN customerproduct ON (customerproduct.Customer_id=customer.id)
							LEFT JOIN status ON (status.id=customer.Status_id)
							LEFT JOIN location ON (location.id=customer.Location_id) ";
			set @whereclause = "WHERE true";
			
            IF _username != "" then
					-- customer name
					set @whereclause = concat(@whereclause, " AND (concat(customer.firstname,' ', customer.middlename, ' ', customer.lastname) like concat('%',",quote(_username),",'%')");
					
					-- customer username
					set @whereclause = concat(@whereclause," OR customer.username like concat('%', ",quote(_username)," ,'%')");
					
					-- customer id
					set @whereclause = concat(@whereclause," OR customer.id like concat('%',", quote(_username) ,",'%')) ");
			end if;
			-- customer entitlement ids
			if _entitlementIDS != "" then
							set @whereclause = concat(@whereclause," AND (customerentitlement.Service_id in (",func_escape_input_for_in_operator(_entitlementIDS),") 
								OR customergroup.Group_id in ( select distinct Group_id from groupentitlement where Service_id in (",func_escape_input_for_in_operator(_entitlementIDS)," )))");
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
							set @whereclause = concat(@whereclause, " AND status.Description like concat('%',", quote(_customerStatus) ,",'%')");
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
			
			set @queryStatement = concat(@queryStatement, @whereclause, "  GROUP BY customer.id ",
            ") paginatedCustomers ON (paginatedCustomers.id=customer.id)
					
					LEFT JOIN customerrequest ON (customerrequest.customer_id=paginatedCustomers.id)
					LEFT JOIN customergroup ON (customergroup.Customer_id=paginatedCustomers.id)
					LEFT JOIN membergroup ON (membergroup.id=customergroup.group_id)
					LEFT JOIN customeraddress ON (paginatedCustomers.id=customeraddress.Customer_id AND customeraddress.isPrimary=1 and customeraddress.Type_id='ADR_TYPE_HOME')
					LEFT JOIN address ON (customeraddress.Address_id = address.id)
					LEFT JOIN city ON (city.id = address.City_id)
					LEFT JOIN customerentitlement ON (customerentitlement.Customer_id=paginatedCustomers.id)
					LEFT JOIN customerproduct ON (customerproduct.Customer_id=paginatedCustomers.id)
					LEFT JOIN status ON (status.id=customer.Status_id)
					LEFT JOIN location ON (location.id=customer.Location_id)  
			GROUP BY paginatedCustomers.id");
			
            IF _sortVariable = "DEFAULT" THEN
				set @queryStatement = concat(@queryStatement, " ORDER BY name");
            ELSEIF _sortVariable != "" THEN
				set @queryStatement = concat(@queryStatement, " ORDER BY ",_sortVariable);
            end if;
            IF _sortDirection != "" THEN
				set @queryStatement = concat(@queryStatement, " ",_sortDirection);
            end if;
            set @queryStatement = concat(@queryStatement, " LIMIT ",_pageOffset,",",_pageSize);
			-- select @queryStatement;
			 PREPARE stmt FROM @queryStatement;
			 execute stmt;
			 DEALLOCATE PREPARE stmt;

	ELSEIF _searchType = 'GROUP_SEARCH_TOTAL_COUNT' then

				set @queryStatement = "
					SELECT SUM(customerSearch.matchingCustomer) as SearchMatchs from 
                        (SELECT
							1 as matchingCustomer
						FROM
							customer
                            LEFT JOIN customerrequest ON (customerrequest.customer_id=customer.id)
							LEFT JOIN customergroup ON (customergroup.Customer_id=customer.id)
							LEFT JOIN membergroup ON (membergroup.id=customergroup.group_id)
							LEFT JOIN customeraddress ON (customer.id=customeraddress.Customer_id AND customeraddress.isPrimary=1 and customeraddress.Type_id='ADR_TYPE_HOME')
							LEFT JOIN address ON (customeraddress.Address_id = address.id)
							LEFT JOIN city ON (city.id = address.City_id)
							LEFT JOIN customerentitlement ON (customerentitlement.Customer_id=customer.id)
							LEFT JOIN customerproduct ON (customerproduct.Customer_id=customer.id)
							LEFT JOIN status ON (status.id=customer.Status_id)
							LEFT JOIN location ON (location.id=customer.Location_id) ";
			set @whereclause = "WHERE true";
			
            IF _username != "" then
					-- customer name
					set @whereclause = concat(@whereclause, " AND (concat(customer.firstname, ' ', customer.middlename, ' ', customer.lastname) like concat('%',",quote(_username),",'%')");
					
					-- customer username
					set @whereclause = concat(@whereclause," OR customer.username like concat('%', ",quote(_username)," ,'%')");
					
					-- customer id
					set @whereclause = concat(@whereclause," OR customer.id like concat('%',", quote(_username) ,",'%')) ");
			end if;
			-- customer entitlement ids
			if _entitlementIDS != "" then
							set @whereclause = concat(@whereclause," AND (customerentitlement.Service_id in (",func_escape_input_for_in_operator(_entitlementIDS),") 
								OR customergroup.Group_id in ( select distinct Group_id from groupentitlement where Service_id in (",func_escape_input_for_in_operator(_entitlementIDS)," )))");
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
							set @whereclause = concat(@whereclause, " AND status.Description like concat('%',", quote(_customerStatus) ,",'%')");
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
			
			set @queryStatement = concat(@queryStatement, @whereclause, "  GROUP BY customer.id) AS customerSearch ");
			-- select @queryStatement;
			PREPARE stmt FROM @queryStatement;
			execute stmt;
			DEALLOCATE PREPARE stmt;

	ELSEIF _searchType = 'CUSTOMER_SEARCH' then
			
			SELECT accountLockoutThreshold from passwordlockoutsettings where id='PLOCKID1' INTO _maxLockCount;
            
			set @queryStatement = concat("
			SELECT customer.id, customer.FirstName, customer.MiddleName, customer.LastName,customer.DateOfBirth,
				concat(IFNULL(customer.FirstName,''), ' ', IFNULL(customer.MiddleName,''), ' ', IFNULL(customer.LastName,'')) as name,
				customer.UserName as Username, customer.Salutation, customer.Gender,CONCAT('****', RIGHT(customer.Ssn, 4)) as Ssn,
                customer.CustomerType_id as CustomerTypeId, company.id as CompanyId, company.Name as CompanyName,
                IF(IFNULL(customer.lockCount,0) >= ",_maxLockCount,", 'SID_CUS_LOCKED',customer.Status_id) as Status_id,
				ANY_VALUE(PrimaryPhone.value) AS PrimaryPhoneNumber,
				ANY_VALUE(PrimaryEmail.value) AS PrimaryEmailAddress,
                GROUP_CONCAT(distinct(membergroup.Name), ',') as groups,
                customer.ApplicantChannel, customer.createdts
			FROM customer
			JOIN (
				SELECT
					customer.id,
                    GROUP_CONCAT(membergroup.Name) as cus_groups,
					GROUP_CONCAT(customerrequest.id) as requestids
				FROM customer
					LEFT JOIN customercommunication PrimaryPhone ON (PrimaryPhone.Customer_id=customer.id AND PrimaryPhone.isPrimary=1 AND PrimaryPhone.Type_id='COMM_TYPE_PHONE')
					LEFT JOIN customercommunication PrimaryEmail ON (PrimaryEmail.Customer_id=customer.id AND PrimaryEmail.isPrimary=1 AND PrimaryEmail.Type_id='COMM_TYPE_EMAIL')
                    LEFT JOIN customergroup ON (customergroup.Customer_id = customer.id)
                    LEFT JOIN membergroup ON (membergroup.id = customergroup.group_id)
					LEFT JOIN customerrequest ON (customerrequest.customer_id = customer.id)
                    LEFT JOIN organisation company ON (customer.Organization_id = company.id)
                    LEFT JOIN organisationmembership ON (customer.Organization_id = organisationmembership.Organization_id)
                    LEFT JOIN card ON (customer.id = card.User_id)
                    LEFT JOIN accounts ON (customer.id = accounts.User_id)
                    LEFT JOIN customeraccounts ON (customer.id = customeraccounts.Customer_id)
                WHERE true");
                
                IF _id != '' THEN
					set @queryStatement = concat(@queryStatement," and customer.id = ", quote(_id));
                end if;
                IF _name != '' THEN
					set @queryStatement = concat(@queryStatement," and concat(customer.firstname,' ',customer.middlename,' ',customer.lastname) like concat('%',",quote(_name),",'%')");
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
					set @queryStatement = concat(@queryStatement," and company.id = ",quote(_companyId));
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
                
				set @queryStatement = concat(@queryStatement," group by customer.id
                HAVING true");
                
                IF _group != '' THEN
					set @queryStatement = concat(@queryStatement," AND cus_groups like concat('%',", quote(_group) ,",'%')");
                end if;
                IF _requestID != '' THEN
					set @queryStatement = concat(@queryStatement," AND requestids like concat('%',", quote(_requestID) ,",'%')");
                end if;
                
                set @queryStatement = concat(@queryStatement,
                ") paginatedCustomers ON (paginatedCustomers.id=customer.id)
			LEFT JOIN customercommunication PrimaryPhone ON (PrimaryPhone.Customer_id=paginatedCustomers.id AND PrimaryPhone.isPrimary=1 AND PrimaryPhone.Type_id='COMM_TYPE_PHONE')
			LEFT JOIN customercommunication PrimaryEmail ON (PrimaryEmail.Customer_id=paginatedCustomers.id AND PrimaryEmail.isPrimary=1 AND PrimaryEmail.Type_id='COMM_TYPE_EMAIL')
			LEFT JOIN customergroup ON (customergroup.Customer_id=paginatedCustomers.id)
			LEFT JOIN membergroup ON (membergroup.id=customergroup.group_id)
            LEFT JOIN organisation company ON (customer.Organization_id = company.id)
            LEFT JOIN organisationmembership ON (customer.Organization_id = organisationmembership.Organization_id)
            LEFT JOIN card ON (paginatedCustomers.id = card.cardNumber)
            LEFT JOIN accounts ON (paginatedCustomers.id = accounts.User_id)
		    LEFT JOIN customeraccounts ON (paginatedCustomers.id = customeraccounts.Customer_id)
            group by customer.id ");
            
            IF _sortVariable = "DEFAULT" THEN
				set @queryStatement = concat(@queryStatement, " ORDER BY name");
            ELSEIF _sortVariable != "" THEN
				set @queryStatement = concat(@queryStatement, " ORDER BY ",_sortVariable);
            end if;
            IF _sortDirection != "" THEN
				set @queryStatement = concat(@queryStatement, " ",_sortDirection);
            end if;
            set @queryStatement = concat(@queryStatement, " LIMIT ",_pageOffset,",",_pageSize);
		 -- select @queryStatement;
		   PREPARE stmt FROM @queryStatement;
		   execute stmt;
		   DEALLOCATE PREPARE stmt;
            
		ELSEIF _searchType = 'CUSTOMER_SEARCH_TOTAL_COUNT' then
			set @queryStatement = concat("
			SELECT SUM(customersearch.matchingCustomer)  as SearchMatchs from 
				(
				SELECT 
					1 as matchingCustomer,
					GROUP_CONCAT(distinct(membergroup.Name)) as cus_groups,
					GROUP_CONCAT(customerrequest.id) as requestids
				FROM
					customer
						LEFT JOIN customercommunication PrimaryPhone ON (PrimaryPhone.Customer_id = customer.id AND PrimaryPhone.isPrimary = 1 AND PrimaryPhone.Type_id = 'COMM_TYPE_PHONE')
						LEFT JOIN customercommunication PrimaryEmail ON (PrimaryEmail.Customer_id = customer.id AND PrimaryEmail.isPrimary = 1 AND PrimaryEmail.Type_id = 'COMM_TYPE_EMAIL')
						LEFT JOIN customergroup ON (customergroup.Customer_id = customer.id)
                        LEFT JOIN membergroup ON (membergroup.id = customergroup.group_id)
						LEFT JOIN customerrequest ON (customerrequest.customer_id = customer.id)
				WHERE
					customer.id like concat('%',",quote(_id),",'%')  AND
					concat(customer.firstname,' ',customer.middlename,' ',customer.lastname) like concat('%',",quote(_name),",'%') AND
					(concat('',",quote(_SSN),") = '' or customer.Ssn = concat('',",quote(_SSN),")) AND
					(customer.username like concat('%',",quote(_username),",'%')) AND 
					IFNULL(PrimaryPhone.value, '') like concat('%', ",quote(_phone)," ,'%') AND 
					IFNULL(PrimaryEmail.value, '') like concat('%',",quote( _email)," ,'%')
				GROUP BY customer.id
				HAVING CASE
					WHEN ",quote(_group)," != '' THEN cus_groups LIKE CONCAT('%', ",quote(_group),", '%') ELSE TRUE
				END
					AND CASE WHEN ",quote( _requestID)," != '' THEN requestids LIKE CONCAT('%',",quote( _requestID),", '%') ELSE TRUE
				END
				) AS customersearch");
			-- select @queryStatement;
		   PREPARE stmt FROM @queryStatement;
		   execute stmt;
		   DEALLOCATE PREPARE stmt;
	end if;
END$$
DELIMITER ;

DROP VIEW IF EXISTS `internalusers_view`;
CREATE VIEW `internalusers_view` AS select `systemuser`.`id` AS `User_id`,`systemuser`.`Status_id` AS `Status_id`,`status`.`Description` AS `Status_Desc`,`systemuser`.`FirstName` AS `FirstName`,`systemuser`.`MiddleName` AS `MiddleName`,`systemuser`.`LastName` AS `LastName`,`systemuser`.`lastLogints` AS `lastLogints`,concat(`systemuser`.`FirstName`,' ',`systemuser`.`LastName`) AS `Name`,`systemuser`.`Username` AS `Username`,`systemuser`.`Email` AS `Email`,(select `userrole`.`Role_id` from `userrole` where (`systemuser`.`id` = `userrole`.`User_id`) limit 1) AS `Role_id`,(select `role`.`Description` from `role` where `role`.`id` in (select `userrole`.`Role_id` from `userrole` where (`systemuser`.`id` = `userrole`.`User_id`)) limit 1) AS `Role_Desc`,(select `role`.`Name` from `role` where `role`.`id` in (select `userrole`.`Role_id` from `userrole` where (`systemuser`.`id` = `userrole`.`User_id`)) limit 1) AS `Role_Name`,((select count(`userpermission`.`Permission_id`) from `userpermission` where (`systemuser`.`id` = `userpermission`.`User_id`)) + (select count(`rolepermission`.`Permission_id`) from `rolepermission` where `rolepermission`.`Role_id` in (select `userrole`.`Role_id` from `userrole` where (`systemuser`.`id` = `userrole`.`User_id`)))) AS `Permission_Count`,`systemuser`.`lastmodifiedts` AS `lastmodifiedts`,`systemuser`.`createdts` AS `createdts`,(select concat(`workaddress`.`addressLine1`,', ',ifnull(`workaddress`.`addressLine2`,''),', ',(select `city`.`Name` from `city` where (`city`.`id` = `workaddress`.`City_id`)),', ',(select `region`.`Name` from `region` where (`region`.`id` = `workaddress`.`Region_id`)),', ',(select `country`.`Name` from `country` where `country`.`id` in (select `city`.`Country_id` from `city` where (`city`.`id` = `workaddress`.`City_id`))),', ',`workaddress`.`zipCode`)) AS `Work_Addr`,(select concat(`homeaddress`.`addressLine1`,', ',ifnull(`homeaddress`.`addressLine2`,''),', ',(select `city`.`Name` from `city` where (`city`.`id` = `homeaddress`.`City_id`)),', ',(select `region`.`Name` from `region` where (`region`.`id` = `homeaddress`.`Region_id`)),', ',(select `country`.`Name` from `country` where `country`.`id` in (select `city`.`Country_id` from `city` where (`city`.`id` = `homeaddress`.`City_id`))),', ',`homeaddress`.`zipCode`)) AS `Home_Addr`,(select ifnull(`homeaddress`.`id`,'')) AS `Home_AddressID`,(select ifnull(`homeaddress`.`addressLine1`,'')) AS `Home_AddressLine1`,(select ifnull(`homeaddress`.`addressLine2`,'')) AS `Home_AddressLine2`,(select ifnull((select `city`.`Name` from `city` where (`city`.`id` = `homeaddress`.`City_id`)),'')) AS `Home_CityName`,(select ifnull(`homeaddress`.`City_id`,'')) AS `Home_CityID`,(select ifnull((select `region`.`Name` from `region` where (`region`.`id` = `homeaddress`.`Region_id`)),'')) AS `Home_StateName`,(select ifnull(`homeaddress`.`Region_id`,'')) AS `Home_StateID`,(select ifnull((select `country`.`Name` from `country` where `country`.`id` in (select `city`.`Country_id` from `city` where (`city`.`id` = `homeaddress`.`City_id`))),'')) AS `Home_CountryName`,(select ifnull((select `country`.`id` from `country` where `country`.`id` in (select `city`.`Country_id` from `city` where (`city`.`id` = `homeaddress`.`City_id`))),'')) AS `Home_CountryID`,(select ifnull(`homeaddress`.`zipCode`,'')) AS `Home_Zipcode`,(select ifnull(`workaddress`.`id`,'')) AS `Work_AddressID`,(select ifnull(`workaddress`.`addressLine1`,'')) AS `Work_AddressLine1`,(select ifnull(`workaddress`.`addressLine2`,'')) AS `Work_AddressLine2`,(select ifnull((select `city`.`Name` from `city` where (`city`.`id` = `workaddress`.`City_id`)),'')) AS `Work_CityName`,(select ifnull(`workaddress`.`City_id`,'')) AS `Work_CityID`,(select ifnull((select `region`.`Name` from `region` where (`region`.`id` = `workaddress`.`Region_id`)),'')) AS `Work_StateName`,(select ifnull(`workaddress`.`Region_id`,'')) AS `Work_StateID`,(select ifnull((select `country`.`Name` from `country` where `country`.`id` in (select `city`.`Country_id` from `city` where (`city`.`id` = `workaddress`.`City_id`))),'')) AS `Work_CountryName`,(select ifnull((select `country`.`id` from `country` where `country`.`id` in (select `city`.`Country_id` from `city` where (`city`.`id` = `workaddress`.`City_id`))),'')) AS `Work_CountryID`,(select ifnull(`workaddress`.`zipCode`,'')) AS `Work_Zipcode` from (((`systemuser` left join `status` on((`systemuser`.`Status_id` = `status`.`id`))) left join `address` `homeaddress` on(`homeaddress`.`id` in (select `useraddress`.`Address_id` from `useraddress` where ((`systemuser`.`id` = `useraddress`.`User_id`) and (`useraddress`.`Type_id` = 'ADR_TYPE_HOME'))))) left join `address` `workaddress` on(`workaddress`.`id` in (select `useraddress`.`Address_id` from `useraddress` where ((`systemuser`.`id` = `useraddress`.`User_id`) and (`useraddress`.`Type_id` = 'ADR_TYPE_WORK')))));

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
    FROM `alerthistory`
        LEFT JOIN `alertsubtype`ON (`alertsubtype`.`id`=`alerthistory`.`AlertSubTypeId`)
        LEFT JOIN `channeltext` ON (`channeltext`.`channelID` = `alerthistory`.`ChannelId`)
        WHERE  (`alerthistory`.`Customer_Id` = _customerId AND `channeltext`.`LanguageCode` = 'en-US') ORDER BY `alerthistory`.`DispatchDate` desc  LIMIT 800;
END$$
DELIMITER;
