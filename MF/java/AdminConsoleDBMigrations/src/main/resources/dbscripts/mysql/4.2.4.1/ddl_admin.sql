
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
BEGIN
	
    DECLARE _maxLockCount varchar(50);

	IF _searchType LIKE 'GROUP_SEARCH%' then
		set @search_select_statement = "SELECT 
					customer.id, customer.FirstName, customer.MiddleName, customer.LastName,
					concat(IFNULL(customer.FirstName,''), ' ', IFNULL(customer.MiddleName,''), ' ', IFNULL(customer.LastName,'')) as name,
					customer.UserName as Username, customer.Salutation, customer.Gender, customer.IsStaffMember,
					customer.modifiedby, customer.lastmodifiedts, customer.Status_id, PrimaryEmail.value AS PrimaryEmail,
					status.Description as customer_status,
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
							
							IF( _customerStatus != '', "LEFT JOIN status ON (status.id=customer.Status_id)",""),
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
			
			set @queryStatement = concat(@queryStatement,@whereclause,
            ") paginatedCustomers ON (paginatedCustomers.id=customer.id)
					
					LEFT JOIN customercommunication PrimaryEmail ON (PrimaryEmail.Customer_id=paginatedCustomers.id AND PrimaryEmail.isPrimary=1 AND PrimaryEmail.Type_id='COMM_TYPE_EMAIL')
					LEFT JOIN customergroup ON (customergroup.Customer_id=paginatedCustomers.id)
					LEFT JOIN customeraddress ON (paginatedCustomers.id=customeraddress.Customer_id AND customeraddress.isPrimary=1 and customeraddress.Type_id='ADR_TYPE_HOME')
					LEFT JOIN address ON (customeraddress.Address_id = address.id)
					LEFT JOIN city ON (city.id = address.City_id)
					LEFT JOIN location ON (location.id=customer.Location_id)  
					LEFT JOIN status ON (status.id=customer.Status_id)
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
        JOIN  `membergroup` ON ((`membergroup`.`id` = `customergroup`.`Group_id`)))
            
    WHERE `customer`.`id` = _customerId
    LIMIT 1;

END$$
DELIMITER ;

ALTER TABLE `configurationbundles` 
DROP INDEX `bundle_id_UNIQUE` ;

ALTER TABLE `configurations` 
DROP INDEX `configuration_id_UNIQUE`;

ALTER TABLE `dmaddinteractions` 
DROP INDEX `id` ;

ALTER TABLE `dmadvertisements` 
DROP INDEX `id` ;

ALTER TABLE `event` 
DROP INDEX `Event_id_UNIQUE` ;

ALTER TABLE `loansconfigurationmasters` 
DROP INDEX `bundle_id_UNIQUE` ;

ALTER TABLE `customerrequest` 
ADD INDEX `IDX_CustomerRequest_createdts` (`createdts` DESC);

ALTER TABLE `requestmessage` 
ADD INDEX `IDX_requestmessage_repliedby` (`RepliedBy` ASC);

ALTER TABLE `alerthistory` 
ADD INDEX `IDX_alerthistory_dispatchdate_idx` (`DispatchDate` DESC);

ALTER TABLE `archivedcustomerrequest` 
ADD INDEX `IDX_archivedcustomerrequest_lastmodifiedts` (`lastmodifiedts` DESC);

ALTER TABLE `requestmessage` 
ADD INDEX `IDX_requestmessage_replysequence` (`ReplySequence` DESC);

ALTER TABLE `customerrequest` 
ADD INDEX `FK_customerrequest_lastmodifiedts` (`lastmodifiedts` DESC);

ALTER TABLE `service` 
ADD INDEX `IDX_service_name` (`Name` ASC);

ALTER TABLE `customer` 
ADD INDEX `IDX_UserName_get` (`UserName` ASC);

ALTER TABLE `pfmbargraph`
ADD INDEX `IDX_pfmbargraph_year` (`year` ASC);

ALTER TABLE `dmadvertisements` 
ADD INDEX `IDX_dmadvertisements_flowPosition` (`flowPosition` ASC);

DROP VIEW IF EXISTS `alertcategory_view`;
CREATE VIEW `alertcategory_view` AS select `dbxalertcategory`.`id` AS `alertcategory_id`,`dbxalertcategory`.`status_id` AS `alertcategory_status_id`,`dbxalertcategory`.`accountLevel` AS `alertcategory_accountLevel`,`dbxalertcategory`.`DisplaySequence` AS `alertcategory_DisplaySequence`,`dbxalertcategory`.`softdeleteflag` AS `alertcategory_softdeleteflag`,`dbxalertcategory`.`Name` AS `alertcategory_Name`,`dbxalertcategorytext`.`LanguageCode` AS `alertcategorytext_LanguageCode`,`dbxalertcategorytext`.`DisplayName` AS `alertcategorytext_DisplayName`,`dbxalertcategorytext`.`Description` AS `alertcategorytext_Description`,`dbxalertcategorytext`.`createdby` AS `alertcategorytext_createdby`,`dbxalertcategorytext`.`modifiedby` AS `alertcategorytext_modifiedby`,`dbxalertcategorytext`.`createdts` AS `alertcategorytext_createdts`,`dbxalertcategorytext`.`lastmodifiedts` AS `alertcategorytext_lastmodifiedts`,`dbxalertcategorytext`.`synctimestamp` AS `alertcategorytext_synctimestamp`,`dbxalertcategorytext`.`softdeleteflag` AS `alertcategorytext_softdeleteflag` from (`dbxalertcategorytext` join `dbxalertcategory` on((`dbxalertcategorytext`.`AlertCategoryId` = `dbxalertcategory`.`id`)));

DROP VIEW IF EXISTS `alerts_fetch_globaldata_view`;
CREATE VIEW `alerts_fetch_globaldata_view` AS select `dbxalerttype`.`id` AS `AlertTypeId`,`dbxalerttype`.`AlertCategoryId` AS `AlertCategoryId`,`dbxalerttype`.`AttributeId` AS `AttributeId`,`dbxalerttype`.`AlertConditionId` AS `AlertConditionId`,`dbxalerttype`.`Value1` AS `Value1`,`dbxalerttype`.`Value2` AS `Value2`,`dbxalerttype`.`Status_id` AS `alerttype_status_id`,`dbxalerttype`.`IsGlobal` AS `IsGlobal`,`dbxalertcategory`.`status_id` AS `alertcategory_status_id`,`alertcategorychannel`.`ChannelID` AS `ChannelId`,`alertsubtype`.`id` AS `AlertSubTypeId`,`alertsubtype`.`Status_id` AS `alertsubtypetype_status_id`,`dbxalertcategory`.`accountLevel` AS `accountLevel` from (((`dbxalerttype` join `alertcategorychannel` on((`dbxalerttype`.`AlertCategoryId` = `alertcategorychannel`.`AlertCategoryId`))) join `dbxalertcategory` on((`dbxalerttype`.`AlertCategoryId` = `dbxalertcategory`.`id`))) join `alertsubtype` on((`dbxalerttype`.`id` = `alertsubtype`.`AlertTypeId`)));

DROP VIEW IF EXISTS `alerttype_view`;
CREATE VIEW `alerttype_view` AS select `dbxalerttype`.`id` AS `alerttype_id`,`dbxalerttype`.`Name` AS `alerttype_Name`,`dbxalerttype`.`AlertCategoryId` AS `alerttype_AlertCategoryId`,`dbxalerttype`.`AttributeId` AS `alerttype_AttributeId`,`dbxalerttype`.`AlertConditionId` AS `alerttype_AlertConditionId`,`dbxalerttype`.`Value1` AS `alerttype_Value1`,`dbxalerttype`.`Value2` AS `alerttype_Value2`,`dbxalerttype`.`Status_id` AS `alerttype_Status_id`,`dbxalerttype`.`IsGlobal` AS `alerttype_IsGlobal`,`dbxalerttype`.`DisplaySequence` AS `alerttype_DisplaySequence`,`dbxalerttype`.`softdeleteflag` AS `alerttype_softdeleteflag`,`dbxalerttypetext`.`LanguageCode` AS `alerttypetext_LanguageCode`,`dbxalerttypetext`.`DisplayName` AS `alerttypetext_DisplayName`,`dbxalerttypetext`.`Description` AS `alerttypetext_Description`,`dbxalerttypetext`.`createdby` AS `alerttypetext_createdby`,`dbxalerttypetext`.`modifiedby` AS `alerttypetext_modifiedby`,`dbxalerttypetext`.`createdts` AS `alerttypetext_createdts`,`dbxalerttypetext`.`lastmodifiedts` AS `alerttypetext_lastmodifiedts`,`dbxalerttypetext`.`synctimestamp` AS `alerttypetext_synctimestamp`,`dbxalerttypetext`.`softdeleteflag` AS `alerttypetext_softdeleteflag` from (`dbxalerttypetext` join `dbxalerttype` on((`dbxalerttypetext`.`AlertTypeId` = `dbxalerttype`.`id`)));

DROP VIEW IF EXISTS `archivedcustomer_request_detailed_view`;
CREATE VIEW `archivedcustomer_request_detailed_view` AS select `archivedcustomerrequest`.`id` AS `customerrequest_id`,`archivedcustomerrequest`.`RequestCategory_id` AS `customerrequest_RequestCategory_id`,`archivedcustomerrequest`.`lastupdatedbycustomer` AS `customerrequest_lastupdatedbycustomer`,`requestcategory`.`Name` AS `requestcategory_Name`,`archivedcustomerrequest`.`Customer_id` AS `customerrequest_Customer_id`,`customer`.`FirstName` AS `customer_FirstName`,`customer`.`MiddleName` AS `customer_MiddleName`,concat(`customer`.`FirstName`,`customer`.`LastName`) AS `customer_Fullname`,concat(`systemuser`.`FirstName`,`systemuser`.`LastName`) AS `customerrequest_AssignedTo_Name`,`customer`.`LastName` AS `customer_LastName`,`customer`.`UserName` AS `customer_Username`,`customer`.`Salutation` AS `customer_Salutation`,`customer`.`Gender` AS `customer_Gender`,`customer`.`DateOfBirth` AS `customer_DateOfBirth`,`customer`.`Status_id` AS `customer_Status_id`,NULL AS `customer_Ssn`,`customer`.`MaritalStatus_id` AS `customer_MaritalStatus_id`,`customer`.`SpouseName` AS `customer_SpouseName`,`customer`.`EmployementStatus_id` AS `customer_EmployementStatus_id`,`customer`.`IsEnrolledForOlb` AS `customer_IsEnrolledForOlb`,`customer`.`IsStaffMember` AS `customer_IsStaffMember`,`customer`.`Location_id` AS `customer_Location_id`,`customer`.`PreferredContactMethod` AS `customer_PreferredContactMethod`,`customer`.`PreferredContactTime` AS `customer_PreferredContactTime`,`archivedcustomerrequest`.`Priority` AS `customerrequest_Priority`,`archivedcustomerrequest`.`Status_id` AS `customerrequest_Status_id`,`archivedcustomerrequest`.`AssignedTo` AS `customerrequest_AssignedTo`,`archivedcustomerrequest`.`RequestSubject` AS `customerrequest_RequestSubject`,`archivedcustomerrequest`.`Accountid` AS `customerrequest_Accountid`,`archivedcustomerrequest`.`createdby` AS `customerrequest_createdby`,`archivedcustomerrequest`.`modifiedby` AS `customerrequest_modifiedby`,`archivedcustomerrequest`.`createdts` AS `customerrequest_createdts`,`archivedcustomerrequest`.`lastmodifiedts` AS `customerrequest_lastmodifiedts`,`archivedcustomerrequest`.`synctimestamp` AS `customerrequest_synctimestamp`,`archivedcustomerrequest`.`softdeleteflag` AS `customerrequest_softdeleteflag`,`archivedrequestmessage`.`id` AS `requestmessage_id`,`archivedrequestmessage`.`RepliedBy` AS `requestmessage_RepliedBy`,`archivedrequestmessage`.`RepliedBy_Name` AS `requestmessage_RepliedBy_Name`,`archivedrequestmessage`.`MessageDescription` AS `requestmessage_MessageDescription`,`archivedrequestmessage`.`ReplySequence` AS `requestmessage_ReplySequence`,`archivedrequestmessage`.`IsRead` AS `requestmessage_IsRead`,`archivedrequestmessage`.`createdby` AS `requestmessage_createdby`,`archivedrequestmessage`.`modifiedby` AS `requestmessage_modifiedby`,`archivedrequestmessage`.`createdts` AS `requestmessage_createdts`,`archivedrequestmessage`.`lastmodifiedts` AS `requestmessage_lastmodifiedts`,`archivedrequestmessage`.`synctimestamp` AS `requestmessage_synctimestamp`,`archivedrequestmessage`.`softdeleteflag` AS `requestmessage_softdeleteflag`,`archivedmessageattachment`.`id` AS `messageattachment_id`,`archivedmessageattachment`.`AttachmentType_id` AS `messageattachment_AttachmentType_id`,`archivedmessageattachment`.`Media_id` AS `messageattachment_Media_id`,`archivedmessageattachment`.`createdby` AS `messageattachment_createdby`,`archivedmessageattachment`.`modifiedby` AS `messageattachment_modifiedby`,`archivedmessageattachment`.`createdts` AS `messageattachment_createdts`,`archivedmessageattachment`.`lastmodifiedts` AS `messageattachment_lastmodifiedts`,`archivedmessageattachment`.`softdeleteflag` AS `messageattachment_softdeleteflag`,`archivedmedia`.`id` AS `media_id`,`archivedmedia`.`Name` AS `media_Name`,`archivedmedia`.`Size` AS `media_Size`,`archivedmedia`.`Type` AS `media_Type`,`archivedmedia`.`Description` AS `media_Description`,`archivedmedia`.`Url` AS `media_Url`,`archivedmedia`.`createdby` AS `media_createdby`,`archivedmedia`.`modifiedby` AS `media_modifiedby`,`archivedmedia`.`lastmodifiedts` AS `media_lastmodifiedts`,`archivedmedia`.`synctimestamp` AS `media_synctimestamp`,`archivedmedia`.`softdeleteflag` AS `media_softdeleteflag` from ((((((`archivedcustomerrequest` join `archivedrequestmessage` on((`archivedcustomerrequest`.`id` = `archivedrequestmessage`.`CustomerRequest_id`))) join `customer` on((`archivedcustomerrequest`.`Customer_id` = `customer`.`id`))) join `requestcategory` on((`archivedcustomerrequest`.`RequestCategory_id` = `requestcategory`.`id`))) left join `systemuser` on((`archivedcustomerrequest`.`AssignedTo` = `systemuser`.`id`))) left join `archivedmessageattachment` on((`archivedrequestmessage`.`id` = `archivedmessageattachment`.`RequestMessage_id`))) left join `archivedmedia` on((`archivedmessageattachment`.`Media_id` = `archivedmedia`.`id`)));

DROP VIEW IF EXISTS `bankfortransfer_view`;
CREATE VIEW `bankfortransfer_view` AS select `bankfortransfer`.`id` AS `id`,`bankfortransfer`.`Code` AS `Bank_Code`,`bankfortransfer`.`Name` AS `Bank_Name`,`bankfortransfer`.`Logo` AS `Logo`,`bankfortransfer`.`Status_id` AS `Status_id`,`bankfortransfer`.`Url` AS `Bank_Url`,`bankfortransfer`.`Address_id` AS `Address_id`,`address`.`addressLine1` AS `AddressLine1`,`address`.`addressLine2` AS `AddressLine2`,`address`.`City_id` AS `City_id`,`city`.`Name` AS `City_Name`,`region`.`id` AS `Region_id`,`region`.`Name` AS `Region_Name`,`region`.`Country_id` AS `Country_id`,`country`.`Name` AS `Country_Name`,`bankservice`.`RoutingCode` AS `Routing_Code`,`bankservice`.`RoutingNumber` AS `Routing_Number`,`bankservice`.`Service_id` AS `Service_id`,`service`.`Name` AS `Service_Name` from ((((((`bankfortransfer` left join `bankservice` on((`bankfortransfer`.`id` = `bankservice`.`BankForTransfer_id`))) join `address` on((`bankfortransfer`.`Address_id` = `address`.`id`))) join `city` on((`address`.`City_id` = `city`.`id`))) join `region` on((`address`.`Region_id` = `region`.`id`))) join `country` on((`region`.`Country_id` = `country`.`id`))) join `service` on((`bankservice`.`Service_id` = `service`.`id`)));

DROP VIEW IF EXISTS `branch_view`;
CREATE VIEW `branch_view` AS select `loc`.`id` AS `Branch_id`,ifnull(`loc`.`Type_id`,'') AS `Branch_Typeid`,ifnull(`loc`.`Name`,'') AS `Branch_Name`,ifnull(`loc`.`DisplayName`,'') AS `Branch_DisplayName`,ifnull(`loc`.`Description`,'') AS `Branch_Description`,ifnull(`loc`.`Code`,'') AS `Branch_Code`,ifnull(`loc`.`PhoneNumber`,'') AS `Branch_PhoneNumber`,ifnull(`loc`.`EmailId`,'') AS `Branch_EmailId`,ifnull(`loc`.`Status_id`,'') AS `Branch_Status_id`,ifnull(`loc`.`IsMainBranch`,'') AS `Branch_IsMainBranch`,ifnull(`loc`.`WorkSchedule_id`,'') AS `Branch_WorkSchedule_id`,ifnull(`loc`.`MainBranchCode`,'') AS `Branch_MainBranchCode`,ifnull(`loc`.`WebSiteUrl`,'') AS `Branch_WebSiteUrl`,`address`.`City_id` AS `City_id`,`city`.`Name` AS `City_Name`,`loc`.`Address_id` AS `Address_id`,concat(`address`.`addressLine1`,', ',ifnull(`address`.`addressLine2`,''),', ',`city`.`Name`,', ',`region`.`Name`,', ',`country`.`Name`,', ',ifnull(`address`.`zipCode`,'')) AS `Branch_Complete_Addr`,ifnull(`loc`.`createdby`,'') AS `Branch_createdby`,ifnull(`loc`.`modifiedby`,'') AS `Branch_modifiedby`,ifnull(`loc`.`createdts`,'') AS `Branch_createdts`,ifnull(`loc`.`lastmodifiedts`,'') AS `Branch_lastmodifiedts`,ifnull(`loc`.`synctimestamp`,'') AS `Branch_synctimestamp` from ((((`location` `loc` join `address` on(((`loc`.`Address_id` = `address`.`id`) and (`loc`.`Type_id` = 'Branch')))) join `city` on((`city`.`id` = `address`.`City_id`))) join `region` on((`region`.`id` = `address`.`Region_id`))) join `country` on((`city`.`Country_id` = `country`.`id`)));

DROP VIEW IF EXISTS `card_request_notification_count_view`;
CREATE VIEW `card_request_notification_count_view` AS select `cardaccountrequest_alias`.`Customer_id` AS `customerId`,`cardaccountrequest_alias`.`CardAccountNumber` AS `cardNumber`,'REQUEST' AS `reqType`,count(`cardaccountrequest_alias`.`CardAccountNumber`) AS `requestcount` from `cardaccountrequest` `cardaccountrequest_alias` group by `cardaccountrequest_alias`.`Customer_id`,`cardaccountrequest_alias`.`CardAccountNumber` union select `notificationcardinfo_alias`.`Customer_id` AS `customerId`,`notificationcardinfo_alias`.`CardNumber` AS `cardNumber`,'NOTIFICATION' AS `reqType`,1 AS `requestcount` from `notificationcardinfo` `notificationcardinfo_alias`;

DROP VIEW IF EXISTS `channel_view`;
CREATE VIEW `channel_view` AS select `channel`.`id` AS `channel_id`,`channel`.`status_id` AS `channel_status_id`,`channeltext`.`LanguageCode` AS `channeltext_LanguageCode`,`channeltext`.`Description` AS `channeltext_Description`,`channeltext`.`createdby` AS `channeltext_createdby`,`channeltext`.`modifiedby` AS `channeltext_modifiedby`,`channeltext`.`createdts` AS `channeltext_createdts`,`channeltext`.`lastmodifiedts` AS `channeltext_lastmodifiedts`,`channeltext`.`synctimestamp` AS `channeltext_synctimestamp`,`channeltext`.`softdeleteflag` AS `channeltext_softdeleteflag` from (`channeltext` join `channel` on((`channeltext`.`channelID` = `channel`.`id`)));

DROP VIEW IF EXISTS `communicationtemplate_channel_view`;
CREATE VIEW `communicationtemplate_channel_view` AS select `communicationtemplate`.`Id` AS `communicationtemplate_id`,`communicationtemplate`.`Name` AS `communicationtemplate_Name`,`communicationtemplate`.`Text` AS `communicationtemplate_Text`,`communicationtemplate`.`AlertSubTypeId` AS `communicationtemplate_AlertSubTypeId`,`communicationtemplate`.`LanguageCode` AS `communicationtemplate_LanguageCode`,`communicationtemplate`.`ChannelID` AS `communicationtemplate_ChannelID`,`communicationtemplate`.`Status_id` AS `communicationtemplate_Status_id`,`communicationtemplate`.`Subject` AS `communicationtemplate_Subject`,`communicationtemplate`.`SenderName` AS `communicationtemplate_SenderName`,`communicationtemplate`.`SenderEmail` AS `communicationtemplate_SenderEmail`,`channeltext`.`Description` AS `channeltext_Description`,`channeltext`.`softdeleteflag` AS `channeltext_softdeleteflag`,`communicationtemplate`.`softdeleteflag` AS `communicationtemplate_softdeleteflag` from (`communicationtemplate` join `channeltext` on(((`communicationtemplate`.`ChannelID` = `channeltext`.`channelID`) and (`communicationtemplate`.`LanguageCode` = `channeltext`.`LanguageCode`))));

DROP VIEW IF EXISTS `configuration_view`;
CREATE VIEW `configuration_view` AS select `configurations`.`configuration_id` AS `configuration_id`,`configurations`.`bundle_id` AS `bundle_id`,`configurationbundles`.`bundle_name` AS `bundle_name`,`configurations`.`config_type` AS `type`,`configurations`.`config_key` AS `key`,`configurations`.`description` AS `description`,`configurations`.`config_value` AS `value`,`configurations`.`target` AS `target`,`configurationbundles`.`app_id` AS `app_id` from (`configurations` join `configurationbundles` on((`configurationbundles`.`bundle_id` = `configurations`.`bundle_id`)));

DROP VIEW IF EXISTS `customeraddressmbview`;
CREATE VIEW `customeraddressmbview` AS select `ca`.`Customer_id` AS `CustomerId`,`ca`.`Address_id` AS `Address_id`,`ca`.`Type_id` AS `Type_id`,`ca`.`DurationOfStay` AS `DurationOfStay`,`ca`.`HomeOwnership` AS `HomeOwnership`,`ca`.`isPrimary` AS `isPrimary`,`at`.`Description` AS `AddressType`,`a`.`addressLine1` AS `AddressLine1`,`a`.`addressLine2` AS `AddressLine2`,`a`.`addressLine3` AS `AddressLine3`,`a`.`zipCode` AS `ZipCode`,`a`.`cityName` AS `CityName`,`a`.`country` AS `CountryName`,`a`.`state` AS `State` from ((`customeraddress` `ca` join `address` `a` on((`a`.`id` = `ca`.`Address_id`))) join `addresstype` `at` on((`ca`.`Type_id` = `at`.`id`))) where (`a`.`softdeleteflag` = '0');

DROP VIEW IF EXISTS `customeraddress_view`;
CREATE VIEW `customeraddress_view` AS select `c`.`id` AS `CustomerId`,`ca`.`Address_id` AS `Address_id`,`ca`.`isPrimary` AS `isPrimary`,`ca`.`Type_id` AS `AddressType`,`a`.`id` AS `AddressId`,`a`.`addressLine1` AS `AddressLine1`,`a`.`addressLine2` AS `AddressLine2`,`a`.`zipCode` AS `ZipCode`,`a`.`Region_id` AS `Region_id`,`a`.`City_id` AS `City_id`,`cit`.`Country_id` AS `Country_id`,`cit`.`Name` AS `CityName`,`reg`.`Name` AS `RegionName`,`reg`.`Code` AS `RegionCode`,`coun`.`Name` AS `CountryName`,`coun`.`Code` AS `CountryCode` from (((((`customeraddress` `ca` join `customer` `c` on((`ca`.`Customer_id` = `c`.`id`))) join `address` `a` on((`a`.`id` = `ca`.`Address_id`))) join `region` `reg` on((`reg`.`id` = `a`.`Region_id`))) join `city` `cit` on((`cit`.`id` = `a`.`City_id`))) join `country` `coun` on((`coun`.`id` = `cit`.`Country_id`)));

DROP VIEW IF EXISTS `customerbasicinfo_view`;
CREATE VIEW `customerbasicinfo_view` AS select `customer`.`UserName` AS `Username`,`customer`.`FirstName` AS `FirstName`,`customer`.`MiddleName` AS `MiddleName`,`customer`.`LastName` AS `LastName`,concat(ifnull(`customer`.`FirstName`,''),' ',ifnull(`customer`.`MiddleName`,''),' ',ifnull(`customer`.`LastName`,'')) AS `Name`,`customer`.`Salutation` AS `Salutation`,`customer`.`id` AS `Customer_id`,concat('****',right(`customer`.`Ssn`,4)) AS `SSN`,`customer`.`createdts` AS `CustomerSince`,`customer`.`Gender` AS `Gender`,`customer`.`DateOfBirth` AS `DateOfBirth`,`customer`.`Status_id` AS `CustomerStatus_id`,`customerstatus`.`Description` AS `CustomerStatus_name`,`customer`.`MaritalStatus_id` AS `MaritalStatus_id`,`maritalstatus`.`Description` AS `MaritalStatus_name`,`customer`.`SpouseName` AS `SpouseName`,`customer`.`lockedOn` AS `lockedOn`,`customer`.`lockCount` AS `lockCount`,`customer`.`EmployementStatus_id` AS `EmployementStatus_id`,`employementstatus`.`Description` AS `EmployementStatus_name`,(select group_concat(`customerflagstatus`.`Status_id`,' ' separator ',') from `customerflagstatus` where (`customerflagstatus`.`Customer_id` = `customer`.`id`)) AS `CustomerFlag_ids`,(select group_concat(`status`.`Description`,' ' separator ',') from `status` where `status`.`id` in (select `customerflagstatus`.`Status_id` from `customerflagstatus` where (`customerflagstatus`.`Customer_id` = `customer`.`id`))) AS `CustomerFlag`,`customer`.`IsEnrolledForOlb` AS `IsEnrolledForOlb`,`customer`.`IsStaffMember` AS `IsStaffMember`,`customer`.`Location_id` AS `Branch_id`,`location`.`Name` AS `Branch_name`,`location`.`Code` AS `Branch_code`,`customer`.`IsOlbAllowed` AS `IsOlbAllowed`,`customer`.`IsAssistConsented` AS `IsAssistConsented`,`customer`.`isEagreementSigned` AS `isEagreementSigned`,`customer`.`CustomerType_id` AS `CustomerType_id`,`customertype`.`Name` AS `CustomerType_Name`,`customertype`.`Description` AS `CustomerType_Description`,(select `membergroup`.`Name` from `membergroup` where `membergroup`.`id` in (select `customergroup`.`Group_id` from `customergroup` where (`customer`.`id` = `customergroup`.`Customer_id`)) limit 1) AS `Customer_Role`,(select `membergroup`.`isEAgreementActive` from `membergroup` where `membergroup`.`id` in (select `customergroup`.`Group_id` from `customergroup` where (`customer`.`id` = `customergroup`.`Customer_id`)) limit 1) AS `isEAgreementRequired`,`customer`.`Organization_Id` AS `organisation_id`,`organisation`.`Name` AS `organisation_name`,any_value(`primaryphone`.`Value`) AS `PrimaryPhoneNumber`,any_value(`primaryemail`.`Value`) AS `PrimaryEmailAddress`,`customer`.`DocumentsSubmitted` AS `DocumentsSubmitted`,`customer`.`ApplicantChannel` AS `ApplicantChannel`,`customer`.`Product` AS `Product`,`customer`.`Reason` AS `Reason` from ((((((((`customer` join `customertype` on((`customer`.`CustomerType_id` = `customertype`.`id`))) join `status` `customerstatus` on((`customer`.`Status_id` = `customerstatus`.`id`))) join `customercommunication` `primaryphone` on(((`primaryphone`.`Customer_id` = `customer`.`id`) and (`primaryphone`.`isPrimary` = 1) and (`primaryphone`.`Type_id` = 'COMM_TYPE_PHONE')))) join `customercommunication` `primaryemail` on(((`primaryemail`.`Customer_id` = `customer`.`id`) and (`primaryemail`.`isPrimary` = 1) and (`primaryemail`.`Type_id` = 'COMM_TYPE_EMAIL')))) left join `location` on((`customer`.`Location_id` = `location`.`id`))) left join `organisation` on((`customer`.`Organization_Id` = `organisation`.`id`))) left join `status` `maritalstatus` on((`customer`.`MaritalStatus_id` = `maritalstatus`.`id`))) left join `status` `employementstatus` on((`customer`.`EmployementStatus_id` = `employementstatus`.`id`))) group by `customer`.`id`;

DROP VIEW IF EXISTS `customer_communication_view`;
CREATE VIEW `customer_communication_view` AS select `customer`.`id` AS `customer_id`,`customer`.`FirstName` AS `customer_FirstName`,`customer`.`MiddleName` AS `customer_MiddleName`,`customer`.`LastName` AS `customer_LastName`,`customer`.`UserName` AS `customer_Username`,`customer`.`Salutation` AS `customer_Salutation`,`customer`.`Gender` AS `customer_Gender`,`customer`.`DateOfBirth` AS `customer_DateOfBirth`,`customer`.`Status_id` AS `customer_Status_id`,`customer`.`Ssn` AS `customer_Ssn`,`customer`.`MaritalStatus_id` AS `customer_MaritalStatus_id`,`customer`.`SpouseName` AS `customer_SpouseName`,`customer`.`EmployementStatus_id` AS `customer_EmployementStatus_id`,`customer`.`IsEnrolledForOlb` AS `customer_IsEnrolledForOlb`,`customer`.`IsStaffMember` AS `customer_IsStaffMember`,`customer`.`Location_id` AS `customer_Location_id`,`customer`.`PreferredContactMethod` AS `customer_PreferredContactMethod`,`customer`.`PreferredContactTime` AS `customer_PreferredContactTime`,`customercommunication`.`id` AS `customercommunication_id`,`customercommunication`.`Type_id` AS `customercommunication_Type_id`,`customercommunication`.`isPrimary` AS `customercommunication`,`customercommunication`.`Value` AS `customercommunication_Value`,`customercommunication`.`Extension` AS `customercommunication_Extension`,`customercommunication`.`Description` AS `customercommunication_Description`,`customercommunication`.`createdby` AS `customercommunication_createdby`,`customercommunication`.`modifiedby` AS `customercommunication_modifiedby`,`customercommunication`.`createdts` AS `customercommunication_createdts`,`customercommunication`.`lastmodifiedts` AS `customercommunication_lastmodifiedts`,`customercommunication`.`synctimestamp` AS `customercommunication_synctimestamp`,`customercommunication`.`softdeleteflag` AS `customercommunication_softdeleteflag` from (`customer` join `customercommunication` on((`customer`.`id` = `customercommunication`.`Customer_id`)));

DROP VIEW IF EXISTS `customercommunicationview`;
CREATE VIEW `customercommunicationview` AS select `customer`.`id` AS `id`,`customer`.`FirstName` AS `FirstName`,`customer`.`MiddleName` AS `MiddleName`,`customer`.`LastName` AS `LastName`,`customer`.`UserName` AS `UserName`,`customer`.`Gender` AS `Gender`,`customer`.`DateOfBirth` AS `DateOfBirth`,`customer`.`Ssn` AS `Ssn`,`customer`.`Status_id` AS `Status_id`,`customertype`.`Name` AS `CustomerType`,`primaryphone`.`Value` AS `Phone`,`primaryemail`.`Value` AS `Email` from (((`customer` left join `customercommunication` `primaryphone` on(((`primaryphone`.`Customer_id` = `customer`.`id`) and (`primaryphone`.`isPrimary` = 1) and (`primaryphone`.`Type_id` = 'COMM_TYPE_PHONE')))) left join `customercommunication` `primaryemail` on(((`primaryemail`.`Customer_id` = `customer`.`id`) and (`primaryemail`.`isPrimary` = 1) and (`primaryemail`.`Type_id` = 'COMM_TYPE_EMAIL')))) join `customertype` on((`customertype`.`id` = `customer`.`CustomerType_id`)));

DROP VIEW IF EXISTS `customer_device_information_view`;
CREATE VIEW `customer_device_information_view` AS select `customerdevice`.`id` AS `Device_id`,`customerdevice`.`Customer_id` AS `Customer_id`,`customer`.`UserName` AS `Customer_username`,`customerdevice`.`DeviceName` AS `DeviceName`,`customerdevice`.`LastLoginTime` AS `LastLoginTime`,`customerdevice`.`LastUsedIp` AS `LastUsedIp`,`customerdevice`.`Status_id` AS `Status_id`,`status`.`Description` AS `Status_name`,`customerdevice`.`OperatingSystem` AS `OperatingSystem`,`customerdevice`.`Channel_id` AS `Channel_id`,`servicechannel`.`Description` AS `Channel_Description`,`customerdevice`.`EnrollmentDate` AS `EnrollmentDate`,`customerdevice`.`createdby` AS `createdby`,`customerdevice`.`modifiedby` AS `modifiedby`,`customerdevice`.`createdts` AS `Registered_Date`,`customerdevice`.`lastmodifiedts` AS `lastmodifiedts` from (((`customerdevice` join `customer` on((`customer`.`id` = `customerdevice`.`Customer_id`))) join `status` on((`customerdevice`.`Status_id` = `status`.`id`))) join `servicechannel` on((`servicechannel`.`id` = `customerdevice`.`Channel_id`)));

DROP VIEW IF EXISTS `customergroupinfo_view`;
CREATE VIEW `customergroupinfo_view` AS select `customergroup`.`Customer_id` AS `Customer_id`,`customergroup`.`Group_id` AS `Group_id`,`membergroup`.`Name` AS `Group_name`,`membergroup`.`Description` AS `Group_Desc`,`membergroup`.`Status_id` AS `GroupStatus_id`,(select `status`.`Description` from `status` where (`membergroup`.`Status_id` = `status`.`id`)) AS `GroupStatus_name`,`membergroup`.`createdby` AS `Group_createdby`,`membergroup`.`modifiedby` AS `Group_modifiedby`,`membergroup`.`createdts` AS `Group_createdts`,`membergroup`.`lastmodifiedts` AS `Group_lastmodifiedts`,`membergroup`.`synctimestamp` AS `Group_synctimestamp` from (`customergroup` join `membergroup` on((`customergroup`.`Group_id` = `membergroup`.`id`)));

DROP VIEW IF EXISTS `customer_indirect_permissions_view`;
CREATE VIEW `customer_indirect_permissions_view` AS select `customergroup`.`Customer_id` AS `Customer_id`,`membergroup`.`id` AS `Group_id`,`membergroup`.`Name` AS `Group_name`,`membergroup`.`Description` AS `Group_desc`,`service`.`id` AS `Service_id`,`service`.`Name` AS `Service_Name`,`service`.`Description` AS `Service_Description`,`service`.`Status_id` AS `Service_Status_id`,`service`.`DisplayName` AS `Service_DisplayName`,`service`.`DisplayDescription` AS `Service_DisplayDescription` from (((`customergroup` join `membergroup` on((`customergroup`.`Group_id` = `membergroup`.`id`))) left join `groupentitlement` on((`customergroup`.`Group_id` = `groupentitlement`.`Group_id`))) left join `service` on((`groupentitlement`.`Service_id` = `service`.`id`)));

DROP VIEW IF EXISTS `customernotes_view`;
CREATE VIEW `customernotes_view` AS select `customernote`.`id` AS `id`,`customernote`.`Note` AS `Note`,`customernote`.`Customer_id` AS `Customer_id`,`customer`.`FirstName` AS `Customer_FirstName`,`customer`.`MiddleName` AS `Customer_MiddleName`,`customer`.`LastName` AS `Customer_LastName`,`customer`.`UserName` AS `Customer_Username`,`customer`.`Status_id` AS `Customer_Status_id`,`customernote`.`createdby` AS `InternalUser_id`,`systemuser`.`Username` AS `InternalUser_Username`,`systemuser`.`FirstName` AS `InternalUser_FirstName`,`systemuser`.`LastName` AS `InternalUser_LastName`,`systemuser`.`MiddleName` AS `InternalUser_MiddleName`,`systemuser`.`Email` AS `InternalUser_Email`,`customernote`.`createdts` AS `createdts`,`customernote`.`synctimestamp` AS `synctimestamp`,`customernote`.`softdeleteflag` AS `softdeleteflag` from ((`customernote` join `systemuser` on((`customernote`.`createdby` = `systemuser`.`id`))) join `customer` on((`customernote`.`Customer_id` = `customer`.`id`)));

DROP VIEW IF EXISTS `customerpermissions_view`;
CREATE VIEW `customerpermissions_view` AS select `customerentitlement`.`Customer_id` AS `Customer_id`,`customerentitlement`.`Service_id` AS `Service_id`,`service`.`Name` AS `Service_name`,`service`.`Description` AS `Service_description`,`service`.`Notes` AS `Service_notes`,`service`.`Status_id` AS `Status_id`,`status`.`Description` AS `Status_description`,`customerentitlement`.`TransactionFee_id` AS `TransactionFee_id`,`transactionfee`.`Description` AS `TransactionFee_description`,`customerentitlement`.`TransactionLimit_id` AS `TransactionLimit_id`,`transactionlimit`.`Description` AS `TransactionLimit_description`,`service`.`Type_id` AS `ServiceType_id`,`servicetype`.`Description` AS `ServiceType_description`,`service`.`Channel_id` AS `Channel_id`,`servicechannel`.`Description` AS `ChannelType_description`,`service`.`MinTransferLimit` AS `MinTransferLimit`,`service`.`MaxTransferLimit` AS `MaxTransferLimit`,`service`.`DisplayName` AS `Display_Name`,`service`.`DisplayDescription` AS `Display_Description` from ((((((`customerentitlement` join `service` on((`customerentitlement`.`Service_id` = `service`.`id`))) join `status` on((`service`.`Status_id` = `status`.`id`))) join `servicetype` on((`servicetype`.`id` = `service`.`Type_id`))) join `servicechannel` on((`servicechannel`.`id` = `service`.`Channel_id`))) left join `transactionfee` on((`transactionfee`.`id` = `customerentitlement`.`TransactionFee_id`))) left join `transactionlimit` on((`transactionlimit`.`id` = `customerentitlement`.`TransactionLimit_id`)));

DROP VIEW IF EXISTS `customer_request_category_count_view`;
CREATE VIEW `customer_request_category_count_view` AS select `requestcategory`.`id` AS `requestcategory_id`,`requestcategory`.`Name` AS `requestcategory_Name`,count(`customerrequest`.`id`) AS `request_count` from (`requestcategory` join `customerrequest` on(((`customerrequest`.`RequestCategory_id` = `requestcategory`.`id`) and (`customerrequest`.`Status_id` = 'SID_OPEN')))) group by `requestcategory`.`id`;

DROP VIEW IF EXISTS `customer_request_csr_count_view`;
CREATE VIEW `customer_request_csr_count_view` AS select `customerrequest`.`Status_id` AS `customerrequest_Status_id`,`status`.`Description` AS `status_Description`,`customerrequest`.`AssignedTo` AS `customerrequest_assignedTo`,count(`customerrequest`.`id`) AS `request_count` from (`customerrequest` join `status` on((`customerrequest`.`Status_id` = `status`.`id`))) group by `customerrequest`.`Status_id`,`customerrequest`.`AssignedTo`;

DROP VIEW IF EXISTS `customer_request_detailed_view`;
CREATE VIEW `customer_request_detailed_view` AS select `customerrequest`.`id` AS `customerrequest_id`,`customerrequest`.`RequestCategory_id` AS `customerrequest_RequestCategory_id`,`customerrequest`.`lastupdatedbycustomer` AS `customerrequest_lastupdatedbycustomer`,`requestcategory`.`Name` AS `requestcategory_Name`,`customerrequest`.`Customer_id` AS `customerrequest_Customer_id`,`customer`.`FirstName` AS `customer_FirstName`,`customer`.`MiddleName` AS `customer_MiddleName`,concat(`customer`.`FirstName`,`customer`.`LastName`) AS `customer_Fullname`,concat(`systemuser`.`FirstName`,`systemuser`.`LastName`) AS `customerrequest_AssignedTo_Name`,`customer`.`LastName` AS `customer_LastName`,`customer`.`UserName` AS `customer_Username`,`customer`.`Salutation` AS `customer_Salutation`,`customer`.`Gender` AS `customer_Gender`,`customer`.`DateOfBirth` AS `customer_DateOfBirth`,`customer`.`Status_id` AS `customer_Status_id`,NULL AS `customer_Ssn`,`customer`.`MaritalStatus_id` AS `customer_MaritalStatus_id`,`customer`.`SpouseName` AS `customer_SpouseName`,`customer`.`EmployementStatus_id` AS `customer_EmployementStatus_id`,`customer`.`IsEnrolledForOlb` AS `customer_IsEnrolledForOlb`,`customer`.`IsStaffMember` AS `customer_IsStaffMember`,`customer`.`Location_id` AS `customer_Location_id`,`customer`.`PreferredContactMethod` AS `customer_PreferredContactMethod`,`customer`.`PreferredContactTime` AS `customer_PreferredContactTime`,`customerrequest`.`Priority` AS `customerrequest_Priority`,`customerrequest`.`Status_id` AS `customerrequest_Status_id`,`customerrequest`.`AssignedTo` AS `customerrequest_AssignedTo`,`customerrequest`.`RequestSubject` AS `customerrequest_RequestSubject`,`customerrequest`.`Accountid` AS `customerrequest_Accountid`,`customerrequest`.`createdby` AS `customerrequest_createdby`,`customerrequest`.`modifiedby` AS `customerrequest_modifiedby`,`customerrequest`.`createdts` AS `customerrequest_createdts`,`customerrequest`.`lastmodifiedts` AS `customerrequest_lastmodifiedts`,`customerrequest`.`synctimestamp` AS `customerrequest_synctimestamp`,`customerrequest`.`softdeleteflag` AS `customerrequest_softdeleteflag`,`requestmessage`.`id` AS `requestmessage_id`,`requestmessage`.`RepliedBy` AS `requestmessage_RepliedBy`,`requestmessage`.`RepliedBy_Name` AS `requestmessage_RepliedBy_Name`,`requestmessage`.`MessageDescription` AS `requestmessage_MessageDescription`,`requestmessage`.`ReplySequence` AS `requestmessage_ReplySequence`,`requestmessage`.`IsRead` AS `requestmessage_IsRead`,`requestmessage`.`createdby` AS `requestmessage_createdby`,`requestmessage`.`modifiedby` AS `requestmessage_modifiedby`,`requestmessage`.`createdts` AS `requestmessage_createdts`,`requestmessage`.`lastmodifiedts` AS `requestmessage_lastmodifiedts`,`requestmessage`.`synctimestamp` AS `requestmessage_synctimestamp`,`requestmessage`.`softdeleteflag` AS `requestmessage_softdeleteflag`,`messageattachment`.`id` AS `messageattachment_id`,`messageattachment`.`AttachmentType_id` AS `messageattachment_AttachmentType_id`,`messageattachment`.`Media_id` AS `messageattachment_Media_id`,`messageattachment`.`createdby` AS `messageattachment_createdby`,`messageattachment`.`modifiedby` AS `messageattachment_modifiedby`,`messageattachment`.`createdts` AS `messageattachment_createdts`,`messageattachment`.`lastmodifiedts` AS `messageattachment_lastmodifiedts`,`messageattachment`.`softdeleteflag` AS `messageattachment_softdeleteflag`,`media`.`id` AS `media_id`,`media`.`Name` AS `media_Name`,`media`.`Size` AS `media_Size`,`media`.`Type` AS `media_Type`,`media`.`Description` AS `media_Description`,`media`.`Url` AS `media_Url`,`media`.`createdby` AS `media_createdby`,`media`.`modifiedby` AS `media_modifiedby`,`media`.`lastmodifiedts` AS `media_lastmodifiedts`,`media`.`synctimestamp` AS `media_synctimestamp`,`media`.`softdeleteflag` AS `media_softdeleteflag` from ((((((`customerrequest` join `customer` on((`customerrequest`.`Customer_id` = `customer`.`id`))) join `requestcategory` on((`customerrequest`.`RequestCategory_id` = `requestcategory`.`id`))) join `requestmessage` on((`customerrequest`.`id` = `requestmessage`.`CustomerRequest_id`))) left join `systemuser` on((`customerrequest`.`AssignedTo` = `systemuser`.`id`))) left join `messageattachment` on((`requestmessage`.`id` = `messageattachment`.`RequestMessage_id`))) left join `media` on((`messageattachment`.`Media_id` = `media`.`id`)));

DROP VIEW IF EXISTS `customer_request_status_count_view`;
CREATE VIEW `customer_request_status_count_view` AS select count(`cr`.`id`) AS `Count`,`s1`.`id` AS `Status_id` from (`status` `s1` join `customerrequest` `cr` on((`s1`.`id` = `cr`.`Status_id`))) where ((`s1`.`Type_id` = 'STID_CUSTOMERREQUEST') and (`s1`.`id` in ('SID_CANCELLED','SID_DELETED','SID_INPROGRESS','SID_ONHOLD','SID_OPEN','SID_RESOLVED'))) group by `s1`.`id` union all select count(`acr`.`id`) AS `Count`,`s2`.`id` AS `Status_id` from (`status` `s2` join `archivedcustomerrequest` `acr` on((`s2`.`id` = `acr`.`Status_id`))) where ((`s2`.`Type_id` = 'STID_CUSTOMERREQUEST') and (`s2`.`id` = 'SID_ARCHIVED')) group by `s2`.`id`;

DROP VIEW IF EXISTS `customerrequests_view`;
CREATE VIEW `customerrequests_view` AS select `customerrequest`.`id` AS `id`,`customerrequest`.`softdeleteflag` AS `softdeleteflag`,`customerrequest`.`Priority` AS `priority`,`customerrequest`.`createdts` AS `requestCreatedDate`,max(`requestmessage`.`createdts`) AS `recentMsgDate`,`requestcategory`.`Name` AS `requestcategory_id`,`customerrequest`.`Customer_id` AS `customer_id`,`customer`.`UserName` AS `username`,`status`.`Description` AS `status_id`,`status`.`id` AS `statusIdentifier`,`customerrequest`.`RequestSubject` AS `requestsubject`,`customerrequest`.`Accountid` AS `accountid`,concat(`systemuser`.`FirstName`,' ',`systemuser`.`LastName`) AS `assignTo`,count(`requestmessage`.`id`) AS `totalmsgs`,count((case when (`requestmessage`.`IsRead` = 'true') then 1 end)) AS `readmsgs`,count((case when (`requestmessage`.`IsRead` = 'false') then 1 end)) AS `unreadmsgs`,substring_index(group_concat(`requestmessage`.`MessageDescription` order by `requestmessage`.`createdts` ASC,`requestmessage`.`id` ASC separator '||'),'||',1) AS `firstMessage`,group_concat(`requestmessage`.`id` separator ',') AS `msgids`,count(`messageattachment`.`id`) AS `totalAttachments` from ((((((`customerrequest` join `requestmessage` on((`customerrequest`.`id` = `requestmessage`.`CustomerRequest_id`))) join `requestcategory` on((`requestcategory`.`id` = `customerrequest`.`RequestCategory_id`))) join `customer` on((`customer`.`id` = `customerrequest`.`Customer_id`))) join `status` on((`status`.`id` = `customerrequest`.`Status_id`))) left join `messageattachment` on((`messageattachment`.`RequestMessage_id` = `requestmessage`.`id`))) left join `systemuser` on((`systemuser`.`id` = `customerrequest`.`AssignedTo`))) group by `customerrequest`.`id` order by `customerrequest`.`createdts` desc,`customerrequest`.`id`;

DROP VIEW IF EXISTS `customerservice_communication_view`;
CREATE VIEW `customerservice_communication_view` AS select `customerservice`.`id` AS `Service_id`,`customerservice`.`Name` AS `Service_Name`,`customerservice`.`Status_id` AS `Service_Status_id`,`customerservice`.`Description` AS `Service_Description`,`customerservice`.`softdeleteflag` AS `Service_SoftDeleteFlag`,`servicecommunication`.`id` AS `ServiceCommunication_id`,`servicecommunication`.`Type_id` AS `ServiceCommunication_Typeid`,`servicecommunication`.`Value` AS `ServiceCommunication_Value`,`servicecommunication`.`Extension` AS `ServiceCommunication_Extension`,`servicecommunication`.`Description` AS `ServiceCommunication_Description`,`servicecommunication`.`Status_id` AS `ServiceCommunication_Status_id`,`servicecommunication`.`Priority` AS `ServiceCommunication_Priority`,`servicecommunication`.`createdby` AS `ServiceCommunication_createdby`,`servicecommunication`.`modifiedby` AS `ServiceCommunication_modifiedby`,`servicecommunication`.`createdts` AS `ServiceCommunication_createdts`,`servicecommunication`.`lastmodifiedts` AS `ServiceCommunication_lastmodifiedts`,`servicecommunication`.`synctimestamp` AS `ServiceCommunication_synctimestamp`,`servicecommunication`.`softdeleteflag` AS `ServiceCommunication_SoftDeleteFlag` from (`servicecommunication` join `customerservice` on((`servicecommunication`.`Service_id` = `customerservice`.`id`)));

DROP VIEW IF EXISTS `fetch_templaterecord_details_view`;
CREATE VIEW `fetch_templaterecord_details_view` AS select `bbtemplaterecord`.`TemplateRecord_id` AS `TemplateRecord_id`,`bbtemplaterecord`.`Record_Name` AS `Record_Name`,`bbtemplaterecord`.`ToAccountNumber` AS `ToAccountNumber`,`bbtemplaterecord`.`ABATRCNumber` AS `ABATRCNumber`,`bbtemplaterecord`.`Detail_id` AS `Detail_id`,`bbtemplaterecord`.`Amount` AS `Amount`,`bbtemplaterecord`.`AdditionalInfo` AS `AdditionalInfo`,`bbtemplaterecord`.`EIN` AS `EIN`,`bbtemplaterecord`.`IsZeroTaxDue` AS `IsZeroTaxDue`,`bbtemplaterecord`.`Template_id` AS `Template_id`,`bbtemplaterecord`.`TaxType_id` AS `TaxType_id`,`bbtemplaterecord`.`TemplateRequestType_id` AS `TemplateRequestType_id`,`bbtemplaterecord`.`softDelete` AS `softDelete`,`bbtemplaterecord`.`ToAccountType` AS `ToAccountType`,`bbtaxtype`.`taxType` AS `TaxType`,`achaccountstype`.`accountType` AS `ToAccountTypeValue`,`bbtemplaterequesttype`.`TemplateRequestTypeName` AS `TemplateRequestTypeValue` from (((`bbtemplaterecord` join `bbtaxtype` on((`bbtemplaterecord`.`TaxType_id` = `bbtaxtype`.`id`))) join `achaccountstype` on((`bbtemplaterecord`.`ToAccountType` = `achaccountstype`.`id`))) join `bbtemplaterequesttype` on((`bbtemplaterecord`.`TemplateRequestType_id` = `bbtemplaterequesttype`.`TemplateRequestType_id`)));

DROP VIEW IF EXISTS `groupservices_view`;
CREATE VIEW `groupservices_view` AS select `groupentitlement`.`Group_id` AS `Group_id`,`groupentitlement`.`Service_id` AS `Service_id`,`service`.`Name` AS `Service_name`,`service`.`Description` AS `Service_description`,`service`.`Notes` AS `Service_notes`,`service`.`Status_id` AS `Status_id`,(select `status`.`Description` from `status` where (`status`.`id` = `service`.`Status_id`)) AS `Status_description`,`groupentitlement`.`TransactionFee_id` AS `TransactionFee_id`,(select `transactionfee`.`Description` from `transactionfee` where (`transactionfee`.`id` = `groupentitlement`.`TransactionFee_id`)) AS `TransactionFee_description`,`groupentitlement`.`TransactionLimit_id` AS `TransactionLimit_id`,(select `transactionlimit`.`Description` from `transactionlimit` where (`transactionlimit`.`id` = `groupentitlement`.`TransactionLimit_id`)) AS `TransactionLimit_description`,`service`.`Type_id` AS `ServiceType_id`,(select `servicetype`.`Description` from `servicetype` where (`servicetype`.`id` = `service`.`Type_id`)) AS `ServiceType_description`,`service`.`Channel_id` AS `Channel_id`,(select `servicechannel`.`Description` from `servicechannel` where (`servicechannel`.`id` = `service`.`Channel_id`)) AS `ChannelType_description`,`service`.`MinTransferLimit` AS `MinTransferLimit`,`service`.`MaxTransferLimit` AS `MaxTransferLimit`,`service`.`DisplayName` AS `Display_Name`,`service`.`DisplayDescription` AS `Display_Description` from (`groupentitlement` join `service` on((`groupentitlement`.`Service_id` = `service`.`id`)));

DROP VIEW IF EXISTS `groups_view`;
CREATE VIEW `groups_view` AS select `membergroup`.`id` AS `Group_id`,`membergroup`.`Type_id` AS `Type_id`,`customertype`.`Name` AS `Type_Name`,`membergroup`.`Description` AS `Group_Desc`,`membergroup`.`Status_id` AS `Status_id`,`membergroup`.`Name` AS `Group_Name`,`membergroup`.`isEAgreementActive` AS `isEAgreementActive`,(select count(`groupentitlement`.`Group_id`) from `groupentitlement` where (`groupentitlement`.`Group_id` = `membergroup`.`id`)) AS `Entitlements_Count`,(select count(`customergroup`.`Customer_id`) from `customergroup` where (`customergroup`.`Group_id` = `membergroup`.`id`)) AS `Customers_Count`,(case `membergroup`.`Status_id` when 'SID_ACTIVE' then 'Active' else 'Inactive' end) AS `Status` from (`membergroup` join `customertype` on((`membergroup`.`Type_id` = `customertype`.`id`)));

DROP VIEW IF EXISTS `internaluserdetails_view`;
CREATE VIEW `internaluserdetails_view` AS select `systemuser`.`id` AS `id`,`systemuser`.`Username` AS `Username`,`systemuser`.`Email` AS `Email`,`systemuser`.`Status_id` AS `Status_id`,`systemuser`.`Password` AS `Password`,`systemuser`.`Code` AS `Code`,`systemuser`.`FirstName` AS `FirstName`,`systemuser`.`MiddleName` AS `MiddleName`,`systemuser`.`LastName` AS `LastName`,`systemuser`.`FailedCount` AS `FailedCount`,`systemuser`.`LastPasswordChangedts` AS `LastPasswordChangedts`,`systemuser`.`ResetpasswordLink` AS `ResetpasswordLink`,`systemuser`.`ResetPasswordExpdts` AS `ResetPasswordExpdts`,`systemuser`.`lastLogints` AS `lastLogints`,`systemuser`.`createdby` AS `createdby`,`systemuser`.`createdts` AS `createdts`,`systemuser`.`modifiedby` AS `modifiedby`,`systemuser`.`lastmodifiedts` AS `lastmodifiedts`,`systemuser`.`synctimestamp` AS `synctimestamp`,`systemuser`.`softdeleteflag` AS `softdeleteflag`,`userrole`.`Role_id` AS `Role_id`,`userrole`.`hasSuperAdminPrivilages` AS `hasSuperAdminPrivilages`,`role`.`Name` AS `Role_Name`,`role`.`Status_id` AS `Role_Status_id` from ((`systemuser` join `userrole` on((`userrole`.`User_id` = `systemuser`.`id`))) join `role` on((`userrole`.`Role_id` = `role`.`id`)));

DROP VIEW IF EXISTS `internalusers_view`;
CREATE VIEW `internalusers_view` AS select `systemuser`.`id` AS `User_id`,`systemuser`.`Status_id` AS `Status_id`,`status`.`Description` AS `Status_Desc`,`systemuser`.`FirstName` AS `FirstName`,`systemuser`.`MiddleName` AS `MiddleName`,`systemuser`.`LastName` AS `LastName`,`systemuser`.`lastLogints` AS `lastLogints`,concat(`systemuser`.`FirstName`,' ',`systemuser`.`LastName`) AS `Name`,`systemuser`.`Username` AS `Username`,`systemuser`.`Email` AS `Email`,(select `userrole`.`Role_id` from `userrole` where (`systemuser`.`id` = `userrole`.`User_id`) limit 1) AS `Role_id`,(select `role`.`Description` from `role` where `role`.`id` in (select `userrole`.`Role_id` from `userrole` where (`systemuser`.`id` = `userrole`.`User_id`)) limit 1) AS `Role_Desc`,(select `role`.`Name` from `role` where `role`.`id` in (select `userrole`.`Role_id` from `userrole` where (`systemuser`.`id` = `userrole`.`User_id`)) limit 1) AS `Role_Name`,((select count(`userpermission`.`Permission_id`) from `userpermission` where (`systemuser`.`id` = `userpermission`.`User_id`)) + (select count(`rolepermission`.`Permission_id`) from `rolepermission` where `rolepermission`.`Role_id` in (select `userrole`.`Role_id` from `userrole` where (`systemuser`.`id` = `userrole`.`User_id`)))) AS `Permission_Count`,`systemuser`.`lastmodifiedts` AS `lastmodifiedts`,`systemuser`.`createdts` AS `createdts`,(select concat(`workaddress`.`addressLine1`,', ',ifnull(`workaddress`.`addressLine2`,''),', ',(select `city`.`Name` from `city` where (`city`.`id` = `workaddress`.`City_id`)),', ',(select `region`.`Name` from `region` where (`region`.`id` = `workaddress`.`Region_id`)),', ',(select `country`.`Name` from `country` where `country`.`id` in (select `city`.`Country_id` from `city` where (`city`.`id` = `workaddress`.`City_id`))),', ',`workaddress`.`zipCode`)) AS `Work_Addr`,(select concat(`homeaddress`.`addressLine1`,', ',ifnull(`homeaddress`.`addressLine2`,''),', ',(select `city`.`Name` from `city` where (`city`.`id` = `homeaddress`.`City_id`)),', ',(select `region`.`Name` from `region` where (`region`.`id` = `homeaddress`.`Region_id`)),', ',(select `country`.`Name` from `country` where `country`.`id` in (select `city`.`Country_id` from `city` where (`city`.`id` = `homeaddress`.`City_id`))),', ',`homeaddress`.`zipCode`)) AS `Home_Addr`,(select ifnull(`homeaddress`.`id`,'')) AS `Home_AddressID`,(select ifnull(`homeaddress`.`addressLine1`,'')) AS `Home_AddressLine1`,(select ifnull(`homeaddress`.`addressLine2`,'')) AS `Home_AddressLine2`,(select ifnull((select `city`.`Name` from `city` where (`city`.`id` = `homeaddress`.`City_id`)),'')) AS `Home_CityName`,(select ifnull(`homeaddress`.`City_id`,'')) AS `Home_CityID`,(select ifnull((select `region`.`Name` from `region` where (`region`.`id` = `homeaddress`.`Region_id`)),'')) AS `Home_StateName`,(select ifnull(`homeaddress`.`Region_id`,'')) AS `Home_StateID`,(select ifnull((select `country`.`Name` from `country` where `country`.`id` in (select `city`.`Country_id` from `city` where (`city`.`id` = `homeaddress`.`City_id`))),'')) AS `Home_CountryName`,(select ifnull((select `country`.`id` from `country` where `country`.`id` in (select `city`.`Country_id` from `city` where (`city`.`id` = `homeaddress`.`City_id`))),'')) AS `Home_CountryID`,(select ifnull(`homeaddress`.`zipCode`,'')) AS `Home_Zipcode`,(select ifnull(`workaddress`.`id`,'')) AS `Work_AddressID`,(select ifnull(`workaddress`.`addressLine1`,'')) AS `Work_AddressLine1`,(select ifnull(`workaddress`.`addressLine2`,'')) AS `Work_AddressLine2`,(select ifnull((select `city`.`Name` from `city` where (`city`.`id` = `workaddress`.`City_id`)),'')) AS `Work_CityName`,(select ifnull(`workaddress`.`City_id`,'')) AS `Work_CityID`,(select ifnull((select `region`.`Name` from `region` where (`region`.`id` = `workaddress`.`Region_id`)),'')) AS `Work_StateName`,(select ifnull(`workaddress`.`Region_id`,'')) AS `Work_StateID`,(select ifnull((select `country`.`Name` from `country` where `country`.`id` in (select `city`.`Country_id` from `city` where (`city`.`id` = `workaddress`.`City_id`))),'')) AS `Work_CountryName`,(select ifnull((select `country`.`id` from `country` where `country`.`id` in (select `city`.`Country_id` from `city` where (`city`.`id` = `workaddress`.`City_id`))),'')) AS `Work_CountryID`,(select ifnull(`workaddress`.`zipCode`,'')) AS `Work_Zipcode` from (((`systemuser` join `status` on((`systemuser`.`Status_id` = `status`.`id`))) left join `address` `homeaddress` on(`homeaddress`.`id` in (select `useraddress`.`Address_id` from `useraddress` where ((`systemuser`.`id` = `useraddress`.`User_id`) and (`useraddress`.`Type_id` = 'ADR_TYPE_HOME'))))) left join `address` `workaddress` on(`workaddress`.`id` in (select `useraddress`.`Address_id` from `useraddress` where ((`systemuser`.`id` = `useraddress`.`User_id`) and (`useraddress`.`Type_id` = 'ADR_TYPE_WORK')))));

DROP VIEW IF EXISTS `locationdetails_view`;
CREATE VIEW `locationdetails_view` AS select distinct `location`.`id` AS `locationId`,(select group_concat(distinct concat(upper(left(`dayschedule`.`WeekDayName`,1)),lower(substr(`dayschedule`.`WeekDayName`,2)),':',convert(substr(`dayschedule`.`StartTime`,1,5) using utf8),'-',convert(substr(`dayschedule`.`EndTime`,1,5) using utf8)) separator ' || ') from (`dayschedule` join `location` `l`) where ((`dayschedule`.`WorkSchedule_id` = `l`.`WorkSchedule_id`) and (`l`.`id` = `location`.`id`))) AS `workingHours`,`location`.`Name` AS `informationTitle`,`location`.`Description` AS `Description`,`location`.`Code` AS `Code`,`location`.`PhoneNumber` AS `phone`,`location`.`EmailId` AS `email`,(case `location`.`Status_id` when 'SID_ACTIVE' then 'OPEN' else 'CLOSED' end) AS `status`,`location`.`Type_id` AS `type`,`location`.`isMobile` AS `isMobile`,`location`.`IsMainBranch` AS `IsMainBranch`,(select group_concat(`service`.`Name` order by `service`.`Name` ASC separator ' || ') from (`service` join `locationservice`) where ((`service`.`id` = `locationservice`.`Service_id`) and (`locationservice`.`Location_id` = `location`.`id`))) AS `services`,(select group_concat(`currency`.`code` order by `currency`.`code` ASC separator ',') from (`currency` join `locationcurrency`) where ((`currency`.`code` = convert(`locationcurrency`.`currency_code` using utf8)) and (convert(`locationcurrency`.`Location_id` using utf8) = `location`.`id`))) AS `currencies`,(select group_concat(`customersegment`.`type` order by `customersegment`.`type` ASC separator ',') from (`customersegment` join `locationcustomersegment`) where ((`customersegment`.`id` = convert(`locationcustomersegment`.`segment_id` using utf8)) and (convert(`locationcustomersegment`.`Location_id` using utf8) = `location`.`id`))) AS `segments`,(select group_concat(`facility`.`name` order by `facility`.`id` ASC separator ',') from (`facility` join `locationfacility`) where ((`facility`.`id` = convert(`locationfacility`.`facility_id` using utf8)) and (convert(`locationfacility`.`Location_id` using utf8) = `location`.`id`))) AS `facilities_names`,(select group_concat(`facility`.`code` order by `facility`.`id` ASC separator ',') from (`facility` join `locationfacility`) where ((`facility`.`id` = convert(`locationfacility`.`facility_id` using utf8)) and (convert(`locationfacility`.`Location_id` using utf8) = `location`.`id`))) AS `facilities_codes`,(select `city`.`Name` from `city` where (`city`.`id` = `address`.`City_id`)) AS `city`,(select `region`.`Name` from `region` where (`region`.`id` = `address`.`Region_id`)) AS `region`,(select `country`.`Name` from `country` where (`country`.`id` = (select `region`.`Country_id` from DUAL  where (`region`.`id` = `address`.`Region_id`)))) AS `country`,`address`.`addressLine1` AS `addressLine1`,`address`.`addressLine2` AS `addressLine2`,`address`.`addressLine3` AS `addressLine3`,`address`.`zipCode` AS `zipCode`,`address`.`latitude` AS `latitude`,`address`.`logitude` AS `longitude` from ((`address` join `location`) join `region`) where ((`address`.`id` = `location`.`Address_id`) and (`region`.`id` = `address`.`Region_id`));

DROP VIEW IF EXISTS `location_view`;
CREATE VIEW `location_view` AS select distinct `location`.`id` AS `id`,`location`.`Name` AS `Name`,`location`.`Code` AS `Code`,`location`.`Description` AS `Description`,`location`.`PhoneNumber` AS `PhoneNumber`,`location`.`Type_id` AS `Type_id`,(case `location`.`Status_id` when 'SID_ACTIVE' then 'Active' else 'Inactive' end) AS `Status_id` from `location`;

DROP VIEW IF EXISTS `notificationview`;
CREATE VIEW `notificationview` AS select `notification`.`notificationId` AS `notificationId`,`notification`.`imageURL` AS `imageURL`,`usernotification`.`isRead` AS `isRead`,`notification`.`notificationActionLink` AS `notificationActionLink`,`notification`.`notificationModule` AS `notificationModule`,`notification`.`notificationSubject` AS `notificationSubject`,`notification`.`notificationSubModule` AS `notificationSubModule`,`notification`.`notificationText` AS `notificationText`,`usernotification`.`receivedDate` AS `receivedDate`,`usernotification`.`id` AS `userNotificationId`,`usernotification`.`user_id` AS `user_id` from (`usernotification` join `notification` on((`notification`.`notificationId` = `usernotification`.`notification_id`)));

DROP VIEW IF EXISTS `organisationemployeesview`;
CREATE VIEW `organisationemployeesview` AS select `organisationemployees`.`id` AS `orgemp_id`,`organisationemployees`.`Organization_id` AS `orgemp_orgid`,`organisationemployees`.`Customer_id` AS `orgemp_cusid`,`customer`.`id` AS `customer_id`,`customer`.`FirstName` AS `FirstName`,`customer`.`MiddleName` AS `MiddleName`,`customer`.`LastName` AS `LastName`,`customer`.`UserName` AS `UserName`,`customer`.`DrivingLicenseNumber` AS `DrivingLicenseNumber`,`customer`.`DateOfBirth` AS `DateOfBirth`,`customer`.`Ssn` AS `Ssn`,`customercommunication`.`id` AS `custcomm_id`,`customercommunication`.`Type_id` AS `custcomm_typeid`,`customercommunication`.`Customer_id` AS `custcomm_custid`,`customercommunication`.`Value` AS `custcomm_value`,`customer`.`Status_id` AS `Status_id`,`customer`.`createdts` AS `createdts`,`customer`.`Lastlogintime` AS `Lastlogintime`,`customergroup`.`Group_id` AS `Group_id`,`membergroup`.`Name` AS `role_name`,`customer`.`createdby` AS `createdby` from ((((`organisationemployees` join `customer` on((`organisationemployees`.`Customer_id` = `customer`.`id`))) left join `customercommunication` on((`customer`.`id` = `customercommunication`.`Customer_id`))) left join `customergroup` on((`customer`.`id` = `customergroup`.`Customer_id`))) join `membergroup` on((`membergroup`.`id` = `customergroup`.`Group_id`)));

DROP VIEW IF EXISTS `outagemessage_view`;
CREATE VIEW `outagemessage_view` AS select `outagemessage`.`id` AS `id`,`service`.`Name` AS `Name`,`service`.`Status_id` AS `service_Status_id`,`outagemessage`.`Channel_id` AS `Channel_id`,`outagemessage`.`Service_id` AS `Service_id`,`outagemessage`.`Status_id` AS `Status_id`,`outagemessage`.`MessageText` AS `MessageText`,`outagemessage`.`createdby` AS `createdby`,`outagemessage`.`modifiedby` AS `modifiedby`,`outagemessage`.`createdts` AS `createdts`,`outagemessage`.`lastmodifiedts` AS `lastmodifiedts`,`outagemessage`.`synctimestamp` AS `synctimestamp`,`outagemessage`.`softdeleteflag` AS `softdeleteflag` from (`outagemessage` join `service` on((`outagemessage`.`Service_id` = `service`.`id`)));

DROP VIEW IF EXISTS `permissions_view`;
CREATE VIEW `permissions_view` AS select `permission`.`id` AS `Permission_id`,`permission`.`Type_id` AS `PermissionType_id`,`permission`.`Name` AS `Permission_Name`,`permission`.`Description` AS `Permission_Desc`,`permission`.`Status_id` AS `Status_id`,`status`.`Description` AS `Status_Desc`,(select count(`rolepermission`.`Role_id`) from `rolepermission` where (`rolepermission`.`Permission_id` = `permission`.`id`)) AS `Role_Count`,((select count(`userpermission`.`Permission_id`) from `userpermission` where (`userpermission`.`Permission_id` = `permission`.`id`)) + (select count(`userrole`.`User_id`) from `userrole` where `userrole`.`Role_id` in (select `rolepermission`.`Role_id` from `rolepermission` where (`rolepermission`.`Permission_id` = `permission`.`id`)))) AS `Users_Count`,(case `permission`.`Status_id` when 'SID_ACTIVE' then 'Active' else 'Inactive' end) AS `Status` from (`permission` join `status` on((`permission`.`Status_id` = `status`.`id`)));

DROP VIEW IF EXISTS `policy_view`;
CREATE VIEW `policy_view` AS select `policycontent`.`id` AS `id`,`policytype`.`id` AS `Type_id`,`policycontent`.`Locale_Code` AS `Locale`,`policycontent`.`Content` AS `PolicyContent` from (`policycontent` join `policytype` on((`policycontent`.`Type_id` = `policytype`.`id`)));

DROP VIEW IF EXISTS `region_details_view`;
CREATE VIEW `region_details_view` AS select `region`.`id` AS `region_Id`,`region`.`Code` AS `region_Code`,`region`.`Name` AS `region_Name`,`region`.`Country_id` AS `country_Id`,`country`.`Code` AS `country_Code`,`country`.`Name` AS `country_Name`,`region`.`createdby` AS `region_createdby`,`region`.`modifiedby` AS `region_modifiedby`,`region`.`createdts` AS `region_createdts`,`region`.`lastmodifiedts` AS `region_lastmodifiedts` from (`region` join `country` on((`country`.`id` = `region`.`Country_id`)));

DROP VIEW IF EXISTS `rolepermission_view`;
CREATE VIEW `rolepermission_view` AS select `role`.`Name` AS `Role_Name`,`role`.`Description` AS `Role_Description`,`role`.`Status_id` AS `Role_Status_id`,`rolepermission`.`Role_id` AS `Role_id`,`permission`.`id` AS `Permission_id`,`permission`.`Type_id` AS `Permission_Type_id`,`permission`.`Status_id` AS `Permission_Status_id`,`permission`.`DataType_id` AS `DataType_id`,`permission`.`Name` AS `Permission_Name`,`permission`.`Description` AS `Permission_Description`,`permission`.`isComposite` AS `Permission_isComposite`,`permission`.`PermissionValue` AS `PermissionValue`,`permission`.`createdby` AS `Permission_createdby`,`permission`.`modifiedby` AS `Permission_modifiedby`,`permission`.`createdts` AS `Permission_createdts`,`permission`.`lastmodifiedts` AS `Permission_lastmodifiedts`,`permission`.`synctimestamp` AS `Permission_synctimestamp`,`permission`.`softdeleteflag` AS `Permission_softdeleteflag` from ((`rolepermission` join `permission` on((`rolepermission`.`Permission_id` = `permission`.`id`))) join `role` on((`role`.`id` = `rolepermission`.`Role_id`)));

DROP VIEW IF EXISTS `roles_view`;
CREATE VIEW `roles_view` AS select `role`.`id` AS `role_id`,`role`.`Type_id` AS `roleType_id`,`role`.`Name` AS `role_Name`,`role`.`Description` AS `role_Desc`,`role`.`Status_id` AS `Status_id`,`status`.`Description` AS `Status_Desc`,(select count(`rolepermission`.`Role_id`) from `rolepermission` where (`rolepermission`.`Role_id` = `role`.`id`)) AS `permission_Count`,(select count(`userrole`.`User_id`) from `userrole` where `userrole`.`Role_id` in (select `rolepermission`.`Role_id` from `rolepermission` where (`rolepermission`.`Role_id` = `role`.`id`))) AS `Users_Count`,(case `role`.`Status_id` when 'SID_ACTIVE' then 'Active' else 'Inactive' end) AS `Status` from (`role` join `status` on((`role`.`Status_id` = `status`.`id`)));

DROP VIEW IF EXISTS `service_view`;
CREATE VIEW `service_view` AS select `service`.`id` AS `id`,`service`.`Type_id` AS `Type_id`,`service`.`Channel_id` AS `Channel_id`,`service`.`Name` AS `Name`,`service`.`Description` AS `Description`,`service`.`DisplayName` AS `DisplayName`,`service`.`DisplayDescription` AS `DisplayDescription`,`service`.`Category_id` AS `Category_Id`,`service`.`code` AS `Code`,`service`.`Status_id` AS `Status_id`,`service`.`Notes` AS `Notes`,`service`.`MaxTransferLimit` AS `MaxTransferLimit`,`service`.`MinTransferLimit` AS `MinTransferLimit`,`service`.`TransferDenominations` AS `TransferDenominations`,`service`.`IsFutureTransaction` AS `IsFutureTransaction`,`service`.`TransactionCharges` AS `TransactionCharges`,`service`.`IsAuthorizationRequired` AS `IsAuthorizationRequired`,`service`.`IsSMSAlertActivated` AS `IsSMSAlertActivated`,`service`.`SMSCharges` AS `SMSCharges`,`service`.`IsBeneficiarySMSAlertActivated` AS `IsBeneficiarySMSAlertActivated`,`service`.`BeneficiarySMSCharge` AS `BeneficiarySMSCharge`,`service`.`HasWeekendOperation` AS `HasWeekendOperation`,`service`.`IsOutageMessageActive` AS `IsOutageMessageActive`,`service`.`IsAlertActive` AS `IsAlertActive`,`service`.`IsTCActive` AS `IsTCActive`,`service`.`IsAgreementActive` AS `IsAgreementActive`,`service`.`IsCampaignActive` AS `IsCampaignActive`,`service`.`WorkSchedule_id` AS `WorkSchedule_id`,`service`.`TransactionFee_id` AS `TransactionFee_id`,`service`.`TransactionLimit_id` AS `TransactionLimit_id`,`service`.`createdby` AS `createdby`,`service`.`modifiedby` AS `modifiedby`,`service`.`createdts` AS `createdts`,`service`.`lastmodifiedts` AS `lastmodifiedts`,`service`.`synctimestamp` AS `synctimestamp`,`service`.`softdeleteflag` AS `softdeleteflag`,`status`.`Description` AS `Status`,`category`.`Name` AS `Category_Name`,`servicechannel`.`Description` AS `Channel`,`servicetype`.`Description` AS `Type_Name`,`workschedule`.`Description` AS `WorkSchedule_Desc` from (((((`service` join `status` on((`service`.`Status_id` = `status`.`id`))) join `servicetype` on((`service`.`Type_id` = `servicetype`.`id`))) left join `category` on((`service`.`Category_id` = `category`.`id`))) left join `servicechannel` on((`service`.`Channel_id` = `servicechannel`.`id`))) left join `workschedule` on((`service`.`WorkSchedule_id` = `workschedule`.`id`)));

DROP VIEW IF EXISTS `travelnotifications_view`;
CREATE VIEW `travelnotifications_view` AS select `travelnotification`.`id` AS `notificationId`,`travelnotification`.`PlannedDepartureDate` AS `startDate`,`travelnotification`.`PlannedReturnDate` AS `endDate`,`travelnotification`.`Destinations` AS `destinations`,`travelnotification`.`AdditionalNotes` AS `additionalNotes`,`travelnotification`.`Status_id` AS `Status_id`,`travelnotification`.`phonenumber` AS `contactNumber`,`notificationcardinfo`.`Customer_id` AS `customerId`,`travelnotification`.`createdts` AS `date`,group_concat(concat(`notificationcardinfo`.`CardName`,' ',`notificationcardinfo`.`CardNumber`) separator ',') AS `cardNumber`,count(`notificationcardinfo`.`CardNumber`) AS `cardCount`,`status`.`Description` AS `status` from ((`travelnotification` join `notificationcardinfo` on((`travelnotification`.`id` = `notificationcardinfo`.`Notification_id`))) join `status` on((`travelnotification`.`Status_id` = `status`.`id`))) group by `travelnotification`.`id`;

DROP VIEW IF EXISTS `userpermission_view`;
CREATE VIEW `userpermission_view` AS select `userrole`.`User_id` AS `User_id`,`userrole`.`Role_id` AS `Role_id`,`rolepermission`.`Permission_id` AS `Permission_id`,`permission`.`Name` AS `Permission_Name`,`permission`.`Description` AS `Permission_Desc`,`permission`.`isComposite` AS `Permission_isComposite` from ((`userrole` join `rolepermission` on((`userrole`.`Role_id` = `rolepermission`.`Role_id`))) join `permission` on((`permission`.`id` = `rolepermission`.`Permission_id`)));

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
        JOIN `alertsubtype`ON (`alertsubtype`.`id`=`alerthistory`.`AlertSubTypeId`)
		JOIN `channeltext` ON (`channeltext`.`channelID` = `alerthistory`.`ChannelId`)
        WHERE  (`alerthistory`.`Customer_Id` = _customerId AND `channeltext`.`LanguageCode` = 'en-US') ORDER BY `alerthistory`.`DispatchDate` desc  LIMIT 800;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `customer_action_group_action_limits_proc`;
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
        JOIN `app` ON ((`appaction`.`App_id` = `app`.`id`)))
		JOIN `service` ON ((`service`.`id` = `customeractionlimit`.`Action_id`)))
		LEFT JOIN `feature` ON ((`feature`.`id` = `service`.`Feature_id`)))
        LEFT JOIN `appaction` ON ((`service`.`id` = `appaction`.`Action_id`)))
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
        JOIN `service` ON ((`service`.`id` = `groupactionlimit`.`Action_id`)))
		JOIN `app` ON ((`appaction`.`App_id` = `app`.`id`)))
        LEFT JOIN `groupactionlimit` ON ((`groupactionlimit`.`Group_id` = `customergroup`.`Group_id`)))
        LEFT JOIN `feature` ON ((`feature`.`id` = `service`.`Feature_id`)))
        LEFT JOIN `appaction` ON ((`service`.`id` = `appaction`.`Action_id`)))
        LEFT JOIN `appactionlimit` ON ((`appaction`.`id` = `appactionlimit`.`AppAction_id`)))
	where `customergroup`.`Customer_id` = _customerID and `service`.`Status_id` != 'SID_INACTIVE' and `feature`.`Status_id` != 'SID_INACTIVE';
END$$
DELIMITER ;

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
		LEFT JOIN `groupentitlement` ON (`groupentitlement`.`Group_id` = `customergroup`.`Group_id`)
		JOIN `service` ON (`service`.`id` = `groupentitlement`.`Service_id`)
	WHERE `customergroup`.`Customer_id` = _customerID;
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
		JOIN `archivedrequestmessage` ON (`archivedcustomerrequest`.`id` = `archivedrequestmessage`.`CustomerRequest_id`)
		JOIN `customer` ON (`archivedcustomerrequest`.`Customer_id` = `customer`.`id`)
		JOIN `requestcategory` ON (`archivedcustomerrequest`.`RequestCategory_id` = `requestcategory`.`id`)
        LEFT JOIN `archivedmessageattachment` ON (`archivedrequestmessage`.`id` = `archivedmessageattachment`.`RequestMessage_id`)
        LEFT JOIN `archivedmedia` ON (`archivedmessageattachment`.`Media_id` = `archivedmedia`.`id`)
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
		JOIN `requestmessage` ON (`customerrequest`.`id` = `requestmessage`.`CustomerRequest_id`)
		JOIN `customer` ON (`customerrequest`.`Customer_id` = `customer`.`id`)
		JOIN `requestcategory` ON (`customerrequest`.`RequestCategory_id` = `requestcategory`.`id`)
        LEFT JOIN `systemuser` ON (`customerrequest`.`AssignedTo` = `systemuser`.`id`)
        LEFT JOIN `messageattachment` ON (`requestmessage`.`id` = `messageattachment`.`RequestMessage_id`)
        LEFT JOIN `media` ON (`messageattachment`.`Media_id` = `media`.`id`))
	WHERE true ";

IF _customerID != '' THEN
		SET @queryStatement = concat(@queryStatement," and customer.id = ", quote(_customerID));
  END if;

  IF _customerFirstName != '' THEN
		SET @queryStatement = concat(@queryStatement," and customer.FirstName = ", quote(_customerFirstName));
  END if;

  IF _customerMiddleName != '' THEN
		SET @queryStatement = concat(@queryStatement," and customer.MiddleName = ", quote(_customerMiddleName));
  END if;

  IF _customerLastName != '' THEN
		SET @queryStatement = concat(@queryStatement," and customer.LastName = ", quote(_customerLastName));
  END if;

  IF _customerUsername != '' THEN
		SET @queryStatement = concat(@queryStatement," and customer.LastName = ", quote(_customerUsername));
  END if;

  IF _messageRepliedBy != '' THEN
		SET @queryStatement = concat(@queryStatement," and requestmessage.RepliedBy = ", quote(_messageRepliedBy));
  END if;

  IF _requestSubject != '' THEN
		SET @queryStatement = concat(@queryStatement," and customerrequest.RequestSubject = ", quote(_requestSubject));
  END if;

  IF _requestAssignedTo != '' THEN
		SET @queryStatement = concat(@queryStatement," and customerrequest.AssignedTo = ", quote(_requestAssignedTo));
  END if;

 IF _requestCategory != '' THEN
		SET @queryStatement = concat(@queryStatement," and customerrequest.RequestCategory_id = ", quote(_requestCategory));
  END if;

   IF _requestID != '' THEN
		SET @queryStatement = concat(@queryStatement," and customerrequest.id = ", quote(_requestID));
  END if;

   IF _requestStatusID != '' THEN
		SET @queryStatement = concat(@queryStatement," and customerrequest.Status_id = ", quote(_requestStatusID));
  END if;
  
   IF _customerName != '' THEN
		SET @queryStatement = concat(@queryStatement," and  CONCAT(`customer`.`FirstName`,`customer`.`LastName`).Status_id = ", quote(_customerName));
  END if;
  
IF  _dateInitialPoint != '' THEN
     IF LOCATE("=", _dateInitialPoint)!=0 THEN
    	SET @queryStatement = concat(@queryStatement," and requestmessage.createdts = ", quote(_dateInitialPoint));
     ELSEIF LOCATE(">", _dateInitialPoint)!=0 THEN
        SET @queryStatement = concat(@queryStatement," and requestmessage.createdts > ", quote(_dateInitialPoint));
     ELSEIF LOCATE("<", _dateInitialPoint)!=0 THEN
        SET @queryStatement = concat(@queryStatement," and requestmessage.createdts < ", quote(_dateInitialPoint));
	 ELSEIF _dateFinalPoint != '' THEN
        SET @queryStatement = concat(@queryStatement," and requestmessage.createdts > ", quote(_dateInitialPoint), " and requestmessage.createdts < ", quote(_dateFinalPoint));
	 END IF;	
 END IF;	
 		
 		SET @queryStatement = concat(@queryStatement," ORDER BY `requestmessage`.`ReplySequence` DESC");
		PREPARE stmt FROM @queryStatement;
		execute stmt;
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
SET 
  @sortColumn := IF(
    _sortCriteria = 'customerrequest_AssignedTo', 
    'cr.AssignedTo', _sortCriteria
  );
SET 
  @sortColumn = IF(
    _sortCriteria = 'customerrequest_createdts', 
    'cr.createdts', @sortColumn
  );
SET 
  @sortColumn = IF(
    _sortCriteria = 'customerrequest_RequestCategory_id', 
    'cr.RequestCategory_id', @sortColumn
  );
SET 
  @sortColumn = IF(
    _sortCriteria = 'customer_Fullname', 
    'CONCAT(customer.FirstName,customer.LastName)', 
    @sortColumn
  );
SET 
  @sortColumn = IF(
    _sortCriteria = 'customerrequest_Status_id', 
    'cr.Status_id', @sortColumn
  );
SET 
  @sortColumn = IF(
    _sortCriteria = 'customerrequest_AssignedTo_Name', 
    'CONCAT(systemuser.FirstName,systemuser.LastName)', 
    @sortColumn
  );
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

DROP PROCEDURE IF EXISTS `get_customer_employement_details_proc`;
DELIMITER $$
CREATE PROCEDURE `get_customer_employement_details_proc`(IN Customer_id varchar(20000))
BEGIN
SELECT 
`employementdetails`.`Customer_id` AS Customer_id,
`employementdetails`.`EmploymentType` AS EmploymentType,
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
`address`.`zipCode` AS ZipCode,
`address`.`state` AS State,
`address`.`cityName` AS city,
`address`.`country` AS Country
FROM employementdetails employementdetails LEFT JOIN
		othersourceofincome othersourceofincome ON employementdetails.Customer_id = othersourceofincome.Customer_id
LEFT JOIN
    customeraddress customeraddress ON (customeraddress.Customer_id = employementdetails.Customer_id AND customeraddress.Type_id="ADR_TYPE_WORK")
JOIN
    address address ON address.id=customeraddress.Address_id
WHERE
	employementdetails.Customer_id=Customer_id limit 1;
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
    count(id) messages_received_count 
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
      count(id) messages_sent_count 
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
              count(id) threads_new_count 
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
                count(id) threads_resolved_count 
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
