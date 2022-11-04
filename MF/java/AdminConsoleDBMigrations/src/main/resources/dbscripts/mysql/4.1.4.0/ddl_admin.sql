ALTER TABLE `transactionlimit` 
CHANGE COLUMN `Description` `Description` VARCHAR(150) NULL DEFAULT NULL ;

ALTER TABLE `service` 
CHANGE COLUMN `Name` `Name` VARCHAR(100) NULL DEFAULT NULL ,
CHANGE COLUMN `DisplayName` `DisplayName` VARCHAR(100) NULL DEFAULT NULL ;

ALTER TABLE `membergroup` 
ADD COLUMN `isEAgreementActive` TINYINT(1) NULL AFTER `Status_id`;

DROP VIEW IF EXISTS `groups_view`;
CREATE VIEW `groups_view` AS select  `membergroup`.`id` AS `Group_id`, `membergroup`.`Type_id` AS `Type_id`, `membergroup`.`Description` AS `Group_Desc`, `membergroup`.`Status_id` AS `Status_id`, `membergroup`.`Name` AS `Group_Name`, `membergroup`.`isEAgreementActive` AS `isEAgreementActive`,(select count( `groupentitlement`.`Group_id`) from  `groupentitlement` where ( `groupentitlement`.`Group_id` =  `membergroup`.`id`)) AS `Entitlements_Count`,(select count( `customergroup`.`Customer_id`) from  `customergroup` where ( `customergroup`.`Group_id` =  `membergroup`.`id`)) AS `Customers_Count`,(case  `membergroup`.`Status_id` when 'SID_ACTIVE' then 'Active' else 'Inactive' end) AS `Status` from  `membergroup`;

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
    quote(_requestStatusID), "
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
SET 
  @sortColumn := IF(
    _sortCriteria = 'customerrequest_AssignedTo', 
    'acr.AssignedTo', _sortCriteria
  );
SET 
  @sortColumn = IF(
    _sortCriteria = 'customerrequest_createdts', 
    'acr.createdts', @sortColumn
  );
SET 
  @sortColumn = IF(
    _sortCriteria = 'customerrequest_RequestCategory_id', 
    'acr.RequestCategory_id', @sortColumn
  );
SET 
  @sortColumn = IF(
    _sortCriteria = 'customer_Fullname', 
    'concat(customer.FirstName, customer.LastName)', 
    @sortColumn
  );
SET 
  @sortColumn = IF(
    _sortCriteria = 'customerrequest_Status_id', 
    'acr.Status_id', @sortColumn
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
   quote(_requestStatusID), "
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
    @stmt, 'LEFT JOIN requestmessage ON (cr.id = requestmessage.CustomerRequest_id) '
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
    @stmt, 'LEFT JOIN customer ON (cr.Customer_id = customer.id) '
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
in _customerStatus char(50), 
in _before varchar(20), 
in _after varchar(20), 
in _maxLockCount varchar(4), 
in _sortVariable varchar(100), 
in _sortDirection varchar(4),  
in _pageOffset bigint, 
in _pageSize bigint)
begin
	IF _searchType = 'APPLICANT_SEARCH' then
		  set @queryStatement = concat("select applicant.id, applicant.FirstName,applicant.MiddleName, applicant.LastName, 
				concat(IFNULL(applicant.FirstName,''), ' ', IFNULL(applicant.MiddleName,''), ' ', IFNULL(applicant.LastName,'')) as name, 
				applicant.ContactNumber, applicant.EmailID, applicant.`Status` as Status, applicant.channel, 
                applicant.IssueDate, applicant.createdts, 'APPLICANT' as CustomerTypeId
			from applicant
			JOIN(
				select applicant.id
				from applicant
				where true");
                
			IF _id != "" THEN
				set @queryStatement = concat(@queryStatement, concat(" and id = ",quote(_id) ));
			end if;
			IF _name != "" THEN
				set @queryStatement = concat(@queryStatement, concat(" and concat(firstname, middlename, lastname) like concat('%',",quote(_name),",'%')"));
			end if;
            IF _phone != "" THEN
				set @queryStatement = concat(@queryStatement, concat(" and ContactNumber = ", quote(_phone) ));
			end if;
			IF _email != "" THEN
				set @queryStatement = concat(@queryStatement, concat(" and EmailID = ", quote(_email) ));
			end if;
			IF _IDValue != "" THEN
				set @queryStatement = concat(@queryStatement, concat(" and (IDType = ",quote(_IDType)," and IDValue = ",quote(_IDValue),")"));
			end if;
                
			set @queryStatement = concat(@queryStatement, ") paginatedApplicants ON (paginatedApplicants.id = applicant.id)
				Group by paginatedApplicants.id ");
				
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
            
	ELSEIF _searchType = 'GROUP_SEARCH' then

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
							set @whereclause = concat(@whereclause," AND customerentitlement.Service_id in (",func_escape_input_for_in_operator(_entitlementIDS),") 
								OR customergroup.Group_id in ( select distinct Group_id from groupentitlement where Service_id in (",func_escape_input_for_in_operator(_entitlementIDS)," ))");
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
							set @whereclause = concat(@whereclause," AND customerentitlement.Service_id in (",func_escape_input_for_in_operator(_entitlementIDS),") 
								OR customergroup.Group_id in ( select distinct Group_id from groupentitlement where Service_id in (",func_escape_input_for_in_operator(_entitlementIDS)," ))");
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
			IF _maxLockCount = '' THEN
				SET _maxLockCount = '4';
			END IF;
            
			set @queryStatement = concat("
			SELECT customer.id, customer.FirstName, customer.MiddleName, customer.LastName,customer.DateOfBirth,
				concat(IFNULL(customer.FirstName,''), ' ', IFNULL(customer.MiddleName,''), ' ', IFNULL(customer.LastName,'')) as name,
				customer.UserName as Username, customer.Salutation, customer.Gender,customer.Ssn,
                customer.CustomerType_id as CustomerTypeId, company.id as CompanyId, company.Name as CompanyName,
                IF(IFNULL(customer.lockCount,0) >= ",_maxLockCount,", 'SID_CUS_LOCKED',customer.Status_id) as Status_id,
				PrimaryPhone.value AS PrimaryPhoneNumber,
				PrimaryEmail.value AS PrimaryEmailAddress,
                GROUP_CONCAT(distinct(membergroup.Name), ',') as groups
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
					customer.id like concat('%',_id,'%')  AND
					concat(customer.firstname,' ',customer.middlename,' ',customer.lastname) like concat('%',_name,'%') AND
					(concat('',_SSN) = '' or customer.Ssn = concat('',_SSN)) AND
					(customer.username like concat('%',_username,'%')) AND 
					IFNULL(PrimaryPhone.value, '') like concat('%', _phone ,'%') AND 
					IFNULL(PrimaryEmail.value, '') like concat('%', _email ,'%')
				GROUP BY customer.id
				HAVING CASE
					WHEN _group != '' THEN cus_groups LIKE CONCAT('%', _group, '%') ELSE TRUE
				END
					AND CASE WHEN _requestID != '' THEN requestids LIKE CONCAT('%', _requestID, '%') ELSE TRUE
				END
				) AS customersearch;
	end if;
END$$
DELIMITER ;


DROP PROCEDURE IF EXISTS `DMS_create_user_proc`;
DELIMITER $$
CREATE PROCEDURE `DMS_create_user_proc`(
  in _username char(50), 
  in _group char(50)
)
BEGIN DECLARE customerId TEXT(2000);
DECLARE Id TEXT(2000);
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
end if;


INSERT INTO `customeraddress` (
  `Customer_id`, `Address_id`, `Type_id`, 
  `isPrimary`, `createdby`, `modifiedby`
) 
VALUES 
  (
    customerId, 'DMSADDR2', 'ADR_TYPE_WORK', 
    '0', 'Kony User', 'Kony Dev'
  );
INSERT INTO `customeraddress` (
  `Customer_id`, `Address_id`, `Type_id`, 
  `isPrimary`, `createdby`, `modifiedby`
) 
VALUES 
  (
    customerId, 'DMSADDR1', 'ADR_TYPE_HOME', 
    '1', 'Kony User', 'Kony Dev'
  );
set 
  customerCommId = concat("CID", customerId);
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
set 
  customerCommId = concat("CID", customerCommId);
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
    'HIGH', 'SID_OPEN', 'Communication Information Change', 
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
    reqMsgId, customerReqId, 'What is expected resolution date?', 
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
    reqMsgId, customerReqId, 'Please escalate the request.', 
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
    reqMsgId, customerReqId, 'Please share the current status of the request.', 
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
    '1234', '1', 'admin2', '2018-04-13 11:40:06', 
    '2018-04-13 11:40:06', '2018-04-13 11:40:06', 
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
    reqMsgId, customerReqId, '<p><span style=\"color: #000000;\">Hi,</span></p>\n<p><span style=\"color: #000000;\">&nbsp;</span></p>\n<p><span style=\"color: #000000;\">I had scheduled a bill payment to my internet provider- AT&amp;T for $25 for this Monday but the payment failed. Can you please help?</span></p>\n<p><span style=\"color: #000000;\">&nbsp;</span></p>\n<p><span style=\"font-size: 11.0pt; font-family: \'Calibri\',sans-serif; color: #000000;\">Thanks </span></p>', 
    'CUSTOMER', customerId, 'John bailey', '1', 'TRUE', 'jane.doe', 
    'jane.doe', '2018-02-26 19:11:15', 
    '2018-02-26 19:11:15', '2018-02-26 19:11:15', 
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
    '2018-02-26 19:11:15', '2018-02-26 19:11:15', 
    '2018-02-26 19:11:15', '0'
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
    reqMsgId, customerReqId, '<p><span style=\"color: #000000;\">Hi Richard,</span></p><p><span style=\"color: #000000;\">&nbsp;</span></p><p><span style=\"color: #000000;\">Thanks for letting me know. I could make this payment now. </span></p><p><span style=\"color: #000000;\">&nbsp;</span></p><p><span style=\"font-size: 11.0pt; font-family: \'Calibri\',sans-serif; color: #000000;\">Thanks</span></p>', 
     'CUSTOMER', customerId, 'John bailey', '3', 'TRUE', 'jane.doe', 
    'jane.doe', '2018-02-26 19:11:15', 
    '2018-02-26 19:11:15', '2018-02-26 19:11:15', 
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
    'UID11', '1234', '0', 'admin2', 
    '2018-04-13 11:40:06', '2018-04-13 11:40:06', 
    '2018-04-13 11:40:06', '0'
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
    '2018-04-13 11:40:06', 'Open', 'Web', 
    '10.10.1.1', 'Chrome', 'Windows', 
    '1234', '0', 'olbuser', '2018-04-13 11:40:06'
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 2, LogID + 2,_username, 'Profile', 'Update contact number', 
    'Changed Primary Contact Number', 
    '2018-04-13 11:40:06', 'Open', 'Web', 
    '10.10.1.2', 'Chrome', 'Windows', 
    '1234', '0', 'olbuser', '2018-04-13 11:40:06'
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 3, LogID + 3,_username, 'Bill Pay', 'Activate Bill Payment Service', 
    'Activate Bill Payment Service', 
    '2018-04-13 11:40:06', 'Open', 'Web', 
    '10.10.1.3', 'Chrome', 'Windows', 
    '1234', '0', 'olbuser', '2018-04-13 11:40:06'
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 4,LogID + 4, _username, 'Bill Pay', 'Add Payee', 
    'Added Payee CitiBank Credit Card', 
    '2018-04-13 11:40:06', 'Open', 'Web', 
    '10.10.1.4', 'Chrome', 'Windows', 
    '', '', '', '2018-04-13 11:40:06'
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 5,LogID + 5, _username, 'Bill Pay', 'Pay Bill', 
    'BillPayment to CitiBank Credit Card $450', 
    '2018-04-13 11:40:06', 'Open', 'Web', 
    '10.10.1.5', 'Chrome', 'Windows', 
    '', '', '', '2018-04-13 11:40:06'
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 6,LogID + 6, _username, 'Logout', 'Logout', 
    'Logout', '2018-04-13 11:40:06', 
    'Open', 'Web', '10.10.1.6', 'Chrome', 
    'Windows', '1234', '0', 'olbuser', 
    '2018-04-13 11:40:06'
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 7,LogID + 7, _username, 'Login', 'Login', 
    'Login with FaceID', '2018-04-13 11:40:06', 
    'Open', 'Mobile', '10.10.1.7', 'Moto', 
    'Android', '1234', '0', 'olbuser', 
    '2018-04-13 11:40:06'
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 8, LogID + 8,_username, 'Accounts', 'View Accounts', 
    'View Checking Account XX2455', 
    '2018-04-13 11:40:06', 'Open', 'Mobile', 
    '10.10.1.8', 'Moto', 'Android', '1234', 
    '0', 'olbuser', '2018-04-13 11:40:06'
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 9,LogID + 9, _username, 'Transfers', 'Add Recipient', 
    'Add IntraBank Recipient Tom Brummet', 
    '2018-04-13 11:40:06', 'Open', 'Mobile', 
    '10.10.1.9', 'Moto', 'Android', '1234', 
    '0', 'olbuser', '2018-04-13 11:40:06'
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 10, LogID + 10,_username, 'Transfers', 
    'IntraBank Fund Transfer', 'IntraBank FT to Tom Brumet $150', 
    '2018-04-13 11:40:06', 'Open', 'Mobile', 
    '10.10.1.10', 'Moto', 'Android', 
    '1234', '0', 'olbuser', '2018-04-13 11:40:06'
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 11, LogID + 11,_username, 'Accounts', 'View Accounts', 
    'View Checking Account XX2455', 
    '2018-04-13 11:40:06', 'Open', 'Mobile', 
    '10.10.1.11', 'Moto', 'Android', 
    '1234', '0', 'olbuser', '2018-04-13 11:40:06'
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 12, LogID + 12,_username, 'Messages', 'Send Message', 
    'Send Message subject Bill Payment Failed', 
    '2018-04-13 11:40:06', 'Open', 'Mobile', 
    '10.10.1.12', 'Moto', 'Android', 
    '1234', '0', 'olbuser', '2018-04-13 11:40:06'
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 13, LogID + 13,_username, 'P2P', 'Send Money', 
    'Send Money to Judy Blume $25', 
    '2018-04-13 11:40:06', 'Open', 'Mobile', 
    '10.10.1.13', 'Moto', 'Android', 
    '1234', '0', 'olbuser', '2018-04-13 11:40:06'
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 14,LogID + 14, _username, 'Logout', 'Logout', 
    'Logout', '2018-04-13 11:40:06', 
    'Open', 'Mobile', '10.10.1.14', 'Moto', 
    'Android', '1234', '0', 'olbuser', 
    '2018-04-13 11:40:06'
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 15,LogID + 15, _username, 'Login', 'Login', 
    'Login with Username/Password', 
    '2018-04-13 11:40:06', 'Open', 'Web', 
    '10.10.1.15', 'Chrome', 'Windows', 
    '', '', '', '2018-04-13 11:40:06'
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 16, LogID + 16,_username, 'Wire Transfer', 
    'Manage Payee', 'Edit Details for Payee Jane', 
    '2018-04-13 11:40:06', 'Open', 'Web', 
    '10.10.1.16', 'Chrome', 'Windows', 
    '', '', '', '2018-04-13 11:40:06'
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 17, LogID + 17,_username, 'Wire Transfer', 
    'Wire Transfer', 'Wire Transfer to Jane $200', 
    '2018-04-13 11:40:06', 'Open', 'Web', 
    '10.10.1.17', 'Chrome', 'Windows', 
    '', '', '', '2018-04-13 11:40:06'
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 18, LogID + 18,_username, 'Accounts', 'View Accounts', 
    'View Checking Account XX2455', 
    '2018-04-13 11:40:06', 'Open', 'Web', 
    '10.10.1.18', 'Chrome', 'Windows', 
    '', '', '', '2018-04-13 11:40:06'
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 19, LogID + 19,_username, 'Accounts', 'Add External Account', 
    'Add External Account Chase Bank xx7583', 
    '2018-04-13 11:40:06', 'Open', 'Web', 
    '10.10.1.19', 'Chrome', 'Windows', 
    '', '', '', '2018-04-13 11:40:06'
  );
INSERT INTO `konydbplog`.`customeractivity` 
(`id`, `sessionId`, `username`, `moduleName`, `activityType`, `description`, `eventts`, `status`, `channel`, `ipAddress`, `device`, `operatingSystem`,`referenceId`, `errorCode`, `createdBy`, `createdOn`)
VALUES 
  (
    LogID + 20, LogID + 20,_username, 'Logout', 'Logout', 
    'Logout', '2018-04-13 11:40:06', 
    'Open', 'Web', '10.10.1.20', 'Chrome', 
    'Windows', '', '', '', '2018-04-13 11:40:06'
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID1', 0, 'Kony User', 
    'Kony Dev', '2018-04-13 11:40:06', 
    '2018-04-13 11:40:06', '2018-04-13 11:40:06', 
    0
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID10', 0, 'Kony User', 
    'Kony Dev', '2018-04-13 11:40:06', 
    '2018-04-13 11:40:06', '2018-04-13 11:40:06', 
    0
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID12', 0, 'Kony User', 
    'Kony Dev', '2018-04-13 11:40:06', 
    '2018-04-13 11:40:06', '2018-04-13 11:40:06', 
    0
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID13', 0, 'Kony User', 
    'Kony Dev', '2018-04-13 11:40:06', 
    '2018-04-13 11:40:06', '2018-04-13 11:40:06', 
    0
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID14', 0, 'Kony User', 
    'Kony Dev', '2018-04-13 11:40:06', 
    '2018-04-13 11:40:06', '2018-04-13 11:40:06', 
    0
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID2', 0, 'Kony User', 
    'Kony Dev', '2018-04-13 11:40:06', 
    '2018-04-13 11:40:06', '2018-04-13 11:40:06', 
    0
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID3', 0, 'Kony User', 
    'Kony Dev', '2018-04-13 11:40:06', 
    '2018-04-13 11:40:06', '2018-04-13 11:40:06', 
    0
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID4', 0, 'Kony User', 
    'Kony Dev', '2018-04-13 11:40:06', 
    '2018-04-13 11:40:06', '2018-04-13 11:40:06', 
    0
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID5', 0, 'Kony User', 
    'Kony Dev', '2018-04-13 11:40:06', 
    '2018-04-13 11:40:06', '2018-04-13 11:40:06', 
    0
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID6', 0, 'Kony User', 
    'Kony Dev', '2018-04-13 11:40:06', 
    '2018-04-13 11:40:06', '2018-04-13 11:40:06', 
    0
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID7', 0, 'Kony User', 
    'Kony Dev', '2018-04-13 11:40:06', 
    '2018-04-13 11:40:06', '2018-04-13 11:40:06', 
    0
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID8', 0, 'Kony User', 
    'Kony Dev', '2018-04-13 11:40:06', 
    '2018-04-13 11:40:06', '2018-04-13 11:40:06', 
    0
  );
INSERT INTO `customernotification` 
VALUES 
  (
    customerId, 'NID9', 0, 'Kony User', 
    'Kony Dev', '2018-04-13 11:40:06', 
    '2018-04-13 11:40:06', '2018-04-13 11:40:06', 
    0
  );
END$$
DELIMITER ;


DROP VIEW IF EXISTS `archivedcustomer_request_detailed_view`;
CREATE VIEW `archivedcustomer_request_detailed_view` AS select `archivedcustomerrequest`.`id` AS `customerrequest_id`,`archivedcustomerrequest`.`RequestCategory_id` AS `customerrequest_RequestCategory_id`,`archivedcustomerrequest`.`lastupdatedbycustomer` AS `customerrequest_lastupdatedbycustomer`,`requestcategory`.`Name` AS `requestcategory_Name`,`archivedcustomerrequest`.`Customer_id` AS `customerrequest_Customer_id`,`customer`.`FirstName` AS `customer_FirstName`,`customer`.`MiddleName` AS `customer_MiddleName`,concat(`customer`.`FirstName`,`customer`.`LastName`) AS `customer_Fullname`,`customer`.`LastName` AS `customer_LastName`,`customer`.`UserName` AS `customer_Username`,`customer`.`Salutation` AS `customer_Salutation`,`customer`.`Gender` AS `customer_Gender`,`customer`.`DateOfBirth` AS `customer_DateOfBirth`,`customer`.`Status_id` AS `customer_Status_id`,`customer`.`Ssn` AS `customer_Ssn`,`customer`.`MaritalStatus_id` AS `customer_MaritalStatus_id`,`customer`.`SpouseName` AS `customer_SpouseName`,`customer`.`EmployementStatus_id` AS `customer_EmployementStatus_id`,`customer`.`IsEnrolledForOlb` AS `customer_IsEnrolledForOlb`,`customer`.`IsStaffMember` AS `customer_IsStaffMember`,`customer`.`Location_id` AS `customer_Location_id`,`customer`.`PreferredContactMethod` AS `customer_PreferredContactMethod`,`customer`.`PreferredContactTime` AS `customer_PreferredContactTime`,`archivedcustomerrequest`.`Priority` AS `customerrequest_Priority`,`archivedcustomerrequest`.`Status_id` AS `customerrequest_Status_id`,`archivedcustomerrequest`.`AssignedTo` AS `customerrequest_AssignedTo`,`archivedcustomerrequest`.`RequestSubject` AS `customerrequest_RequestSubject`,`archivedcustomerrequest`.`Accountid` AS `customerrequest_Accountid`,`archivedcustomerrequest`.`createdby` AS `customerrequest_createdby`,`archivedcustomerrequest`.`modifiedby` AS `customerrequest_modifiedby`,`archivedcustomerrequest`.`createdts` AS `customerrequest_createdts`,`archivedcustomerrequest`.`lastmodifiedts` AS `customerrequest_lastmodifiedts`,`archivedcustomerrequest`.`synctimestamp` AS `customerrequest_synctimestamp`,`archivedcustomerrequest`.`softdeleteflag` AS `customerrequest_softdeleteflag`,`archivedrequestmessage`.`id` AS `requestmessage_id`,`archivedrequestmessage`.`RepliedBy` AS `requestmessage_RepliedBy`,`archivedrequestmessage`.`RepliedBy_Name` AS `requestmessage_RepliedBy_Name`,`archivedrequestmessage`.`MessageDescription` AS `requestmessage_MessageDescription`,`archivedrequestmessage`.`ReplySequence` AS `requestmessage_ReplySequence`,`archivedrequestmessage`.`IsRead` AS `requestmessage_IsRead`,`archivedrequestmessage`.`createdby` AS `requestmessage_createdby`,`archivedrequestmessage`.`modifiedby` AS `requestmessage_modifiedby`,`archivedrequestmessage`.`createdts` AS `requestmessage_createdts`,`archivedrequestmessage`.`lastmodifiedts` AS `requestmessage_lastmodifiedts`,`archivedrequestmessage`.`synctimestamp` AS `requestmessage_synctimestamp`,`archivedrequestmessage`.`softdeleteflag` AS `requestmessage_softdeleteflag`,`archivedmessageattachment`.`id` AS `messageattachment_id`,`archivedmessageattachment`.`AttachmentType_id` AS `messageattachment_AttachmentType_id`,`archivedmessageattachment`.`Media_id` AS `messageattachment_Media_id`,`archivedmessageattachment`.`createdby` AS `messageattachment_createdby`,`archivedmessageattachment`.`modifiedby` AS `messageattachment_modifiedby`,`archivedmessageattachment`.`createdts` AS `messageattachment_createdts`,`archivedmessageattachment`.`lastmodifiedts` AS `messageattachment_lastmodifiedts`,`archivedmessageattachment`.`softdeleteflag` AS `messageattachment_softdeleteflag`,`archivedmedia`.`id` AS `media_id`,`archivedmedia`.`Name` AS `media_Name`,`archivedmedia`.`Size` AS `media_Size`,`archivedmedia`.`Type` AS `media_Type`,`archivedmedia`.`Description` AS `media_Description`,`archivedmedia`.`Url` AS `media_Url`,`archivedmedia`.`createdby` AS `media_createdby`,`archivedmedia`.`modifiedby` AS `media_modifiedby`,`archivedmedia`.`lastmodifiedts` AS `media_lastmodifiedts`,`archivedmedia`.`synctimestamp` AS `media_synctimestamp`,`archivedmedia`.`softdeleteflag` AS `media_softdeleteflag` from (((((`archivedcustomerrequest` left join `archivedrequestmessage` on((`archivedcustomerrequest`.`id` = `archivedrequestmessage`.`CustomerRequest_id`))) left join `archivedmessageattachment` on((`archivedrequestmessage`.`id` = `archivedmessageattachment`.`RequestMessage_id`))) left join `archivedmedia` on((`archivedmessageattachment`.`Media_id` = `archivedmedia`.`id`))) left join `customer` on((`archivedcustomerrequest`.`Customer_id` = `customer`.`id`))) left join `requestcategory` on((`archivedcustomerrequest`.`RequestCategory_id` = `requestcategory`.`id`)));

DROP VIEW IF EXISTS `customer_communication_view`;
CREATE VIEW `customer_communication_view` AS select `customer`.`id` AS `customer_id`,`customer`.`FirstName` AS `customer_FirstName`,`customer`.`MiddleName` AS `customer_MiddleName`,`customer`.`LastName` AS `customer_LastName`,`customer`.`UserName` AS `customer_Username`,`customer`.`Salutation` AS `customer_Salutation`,`customer`.`Gender` AS `customer_Gender`,`customer`.`DateOfBirth` AS `customer_DateOfBirth`,`customer`.`Status_id` AS `customer_Status_id`,`customer`.`Ssn` AS `customer_Ssn`,`customer`.`MaritalStatus_id` AS `customer_MaritalStatus_id`,`customer`.`SpouseName` AS `customer_SpouseName`,`customer`.`EmployementStatus_id` AS `customer_EmployementStatus_id`,`customer`.`IsEnrolledForOlb` AS `customer_IsEnrolledForOlb`,`customer`.`IsStaffMember` AS `customer_IsStaffMember`,`customer`.`Location_id` AS `customer_Location_id`,`customer`.`PreferredContactMethod` AS `customer_PreferredContactMethod`,`customer`.`PreferredContactTime` AS `customer_PreferredContactTime`,`customercommunication`.`id` AS `customercommunication_id`,`customercommunication`.`Type_id` AS `customercommunication_Type_id`,`customercommunication`.`isPrimary` AS `customercommunication`,`customercommunication`.`Value` AS `customercommunication_Value`,`customercommunication`.`Extension` AS `customercommunication_Extension`,`customercommunication`.`Description` AS `customercommunication_Description`,`customercommunication`.`createdby` AS `customercommunication_createdby`,`customercommunication`.`modifiedby` AS `customercommunication_modifiedby`,`customercommunication`.`createdts` AS `customercommunication_createdts`,`customercommunication`.`lastmodifiedts` AS `customercommunication_lastmodifiedts`,`customercommunication`.`synctimestamp` AS `customercommunication_synctimestamp`,`customercommunication`.`softdeleteflag` AS `customercommunication_softdeleteflag` from (`customer` left join `customercommunication` on((`customer`.`id` = `customercommunication`.`Customer_id`)));

DROP VIEW IF EXISTS `customer_request_detailed_view`;
CREATE VIEW `customer_request_detailed_view` AS select `customerrequest`.`id` AS `customerrequest_id`,`customerrequest`.`RequestCategory_id` AS `customerrequest_RequestCategory_id`,`customerrequest`.`lastupdatedbycustomer` AS `customerrequest_lastupdatedbycustomer`,`requestcategory`.`Name` AS `requestcategory_Name`,`customerrequest`.`Customer_id` AS `customerrequest_Customer_id`,`customer`.`FirstName` AS `customer_FirstName`,`customer`.`MiddleName` AS `customer_MiddleName`,concat(`customer`.`FirstName`,`customer`.`LastName`) AS `customer_Fullname`,concat(`systemuser`.`FirstName`,`systemuser`.`LastName`) AS `customerrequest_AssignedTo_Name`,`customer`.`LastName` AS `customer_LastName`,`customer`.`UserName` AS `customer_Username`,`customer`.`Salutation` AS `customer_Salutation`,`customer`.`Gender` AS `customer_Gender`,`customer`.`DateOfBirth` AS `customer_DateOfBirth`,`customer`.`Status_id` AS `customer_Status_id`,`customer`.`Ssn` AS `customer_Ssn`,`customer`.`MaritalStatus_id` AS `customer_MaritalStatus_id`,`customer`.`SpouseName` AS `customer_SpouseName`,`customer`.`EmployementStatus_id` AS `customer_EmployementStatus_id`,`customer`.`IsEnrolledForOlb` AS `customer_IsEnrolledForOlb`,`customer`.`IsStaffMember` AS `customer_IsStaffMember`,`customer`.`Location_id` AS `customer_Location_id`,`customer`.`PreferredContactMethod` AS `customer_PreferredContactMethod`,`customer`.`PreferredContactTime` AS `customer_PreferredContactTime`,`customerrequest`.`Priority` AS `customerrequest_Priority`,`customerrequest`.`Status_id` AS `customerrequest_Status_id`,`customerrequest`.`AssignedTo` AS `customerrequest_AssignedTo`,`customerrequest`.`RequestSubject` AS `customerrequest_RequestSubject`,`customerrequest`.`Accountid` AS `customerrequest_Accountid`,`customerrequest`.`createdby` AS `customerrequest_createdby`,`customerrequest`.`modifiedby` AS `customerrequest_modifiedby`,`customerrequest`.`createdts` AS `customerrequest_createdts`,`customerrequest`.`lastmodifiedts` AS `customerrequest_lastmodifiedts`,`customerrequest`.`synctimestamp` AS `customerrequest_synctimestamp`,`customerrequest`.`softdeleteflag` AS `customerrequest_softdeleteflag`,`requestmessage`.`id` AS `requestmessage_id`,`requestmessage`.`RepliedBy` AS `requestmessage_RepliedBy`,`requestmessage`.`RepliedBy_Name` AS `requestmessage_RepliedBy_Name`,`requestmessage`.`MessageDescription` AS `requestmessage_MessageDescription`,`requestmessage`.`ReplySequence` AS `requestmessage_ReplySequence`,`requestmessage`.`IsRead` AS `requestmessage_IsRead`,`requestmessage`.`createdby` AS `requestmessage_createdby`,`requestmessage`.`modifiedby` AS `requestmessage_modifiedby`,`requestmessage`.`createdts` AS `requestmessage_createdts`,`requestmessage`.`lastmodifiedts` AS `requestmessage_lastmodifiedts`,`requestmessage`.`synctimestamp` AS `requestmessage_synctimestamp`,`requestmessage`.`softdeleteflag` AS `requestmessage_softdeleteflag`,`messageattachment`.`id` AS `messageattachment_id`,`messageattachment`.`AttachmentType_id` AS `messageattachment_AttachmentType_id`,`messageattachment`.`Media_id` AS `messageattachment_Media_id`,`messageattachment`.`createdby` AS `messageattachment_createdby`,`messageattachment`.`modifiedby` AS `messageattachment_modifiedby`,`messageattachment`.`createdts` AS `messageattachment_createdts`,`messageattachment`.`lastmodifiedts` AS `messageattachment_lastmodifiedts`,`messageattachment`.`softdeleteflag` AS `messageattachment_softdeleteflag`,`media`.`id` AS `media_id`,`media`.`Name` AS `media_Name`,`media`.`Size` AS `media_Size`,`media`.`Type` AS `media_Type`,`media`.`Description` AS `media_Description`,`media`.`Url` AS `media_Url`,`media`.`createdby` AS `media_createdby`,`media`.`modifiedby` AS `media_modifiedby`,`media`.`lastmodifiedts` AS `media_lastmodifiedts`,`media`.`synctimestamp` AS `media_synctimestamp`,`media`.`softdeleteflag` AS `media_softdeleteflag` from ((((((`customerrequest` left join `requestmessage` on((`customerrequest`.`id` = `requestmessage`.`CustomerRequest_id`))) left join `messageattachment` on((`requestmessage`.`id` = `messageattachment`.`RequestMessage_id`))) left join `media` on((`messageattachment`.`Media_id` = `media`.`id`))) left join `customer` on((`customerrequest`.`Customer_id` = `customer`.`id`))) left join `requestcategory` on((`customerrequest`.`RequestCategory_id` = `requestcategory`.`id`))) left join `systemuser` on((`customerrequest`.`AssignedTo` = `systemuser`.`id`)));

DROP VIEW IF EXISTS `customerbasicinfo_view`;
CREATE VIEW `customerbasicinfo_view` AS select `customer`.`UserName` AS `Username`,`customer`.`FirstName` AS `FirstName`,`customer`.`MiddleName` AS `MiddleName`,`customer`.`LastName` AS `LastName`,concat(`customer`.`FirstName`,' ',`customer`.`MiddleName`,' ',`customer`.`LastName`) AS `Name`,`customer`.`Salutation` AS `Salutation`,`customer`.`id` AS `Customer_id`,`customer`.`Ssn` AS `SSN`,`customer`.`createdts` AS `CustomerSince`,`customer`.`Gender` AS `Gender`,`customer`.`DateOfBirth` AS `DateOfBirth`,`customer`.`Status_id` AS `CustomerStatus_id`,(select `status`.`Description` from `status` where (`customer`.`Status_id` = `status`.`id`)) AS `CustomerStatus_name`,`customer`.`MaritalStatus_id` AS `MaritalStatus_id`,(select `status`.`Description` from `status` where (`customer`.`MaritalStatus_id` = `status`.`id`)) AS `MaritalStatus_name`,`customer`.`SpouseName` AS `SpouseName`,`customer`.`EmployementStatus_id` AS `EmployementStatus_id`,(select `status`.`Description` from `status` where (`customer`.`EmployementStatus_id` = `status`.`id`)) AS `EmployementStatus_name`,(select group_concat(`customerflagstatus`.`Status_id`,' ' separator ',') from `customerflagstatus` where (`customerflagstatus`.`Customer_id` = `customer`.`id`)) AS `CustomerFlag_ids`,(select group_concat(`status`.`Description`,' ' separator ',') from `status` where `status`.`id` in (select `customerflagstatus`.`Status_id` from `customerflagstatus` where (`customerflagstatus`.`Customer_id` = `customer`.`id`))) AS `CustomerFlag`,`customer`.`IsEnrolledForOlb` AS `IsEnrolledForOlb`,`customer`.`IsStaffMember` AS `IsStaffMember`,`customer`.`Location_id` AS `Branch_id`,`location`.`Name` AS `Branch_name`,`location`.`Code` AS `Branch_code`,`customer`.`IsOlbAllowed` AS `IsOlbAllowed`,`customer`.`IsAssistConsented` AS `IsAssistConsented` from (`customer` left join `location` on((`customer`.`Location_id` = `location`.`id`)));

DROP VIEW IF EXISTS `customernotes_view`;
CREATE VIEW `customernotes_view` AS select `customernote`.`id` AS `id`,`customernote`.`Note` AS `Note`,`customernote`.`Customer_id` AS `Customer_id`,`customer`.`FirstName` AS `Customer_FirstName`,`customer`.`MiddleName` AS `Customer_MiddleName`,`customer`.`LastName` AS `Customer_LastName`,`customer`.`UserName` AS `Customer_Username`,`customer`.`Status_id` AS `Customer_Status_id`,`customernote`.`createdby` AS `InternalUser_id`,`systemuser`.`Username` AS `InternalUser_Username`,`systemuser`.`FirstName` AS `InternalUser_FirstName`,`systemuser`.`LastName` AS `InternalUser_LastName`,`systemuser`.`MiddleName` AS `InternalUser_MiddleName`,`systemuser`.`Email` AS `InternalUser_Email`,`customernote`.`createdts` AS `createdts`,`customernote`.`synctimestamp` AS `synctimestamp`,`customernote`.`softdeleteflag` AS `softdeleteflag` from ((`customernote` left join `systemuser` on((`customernote`.`createdby` = `systemuser`.`id`))) left join `customer` on((`customernote`.`Customer_id` = `customer`.`id`)));

DROP VIEW IF EXISTS `customerrequests_view`;
CREATE VIEW `customerrequests_view` AS select `customerrequest`.`id` AS `id`,`customerrequest`.`softdeleteflag` AS `softdeleteflag`,`customerrequest`.`Priority` AS `priority`,`customerrequest`.`createdts` AS `requestCreatedDate`,max(`requestmessage`.`createdts`) AS `recentMsgDate`,`requestcategory`.`Name` AS `requestcategory_id`,`customerrequest`.`Customer_id` AS `customer_id`,`customer`.`UserName` AS `username`,`status`.`Description` AS `status_id`,`status`.`id` AS `statusIdentifier`,`customerrequest`.`RequestSubject` AS `requestsubject`,`customerrequest`.`Accountid` AS `accountid`,concat(`systemuser`.`FirstName`,' ',`systemuser`.`LastName`) AS `assignTo`,count(`requestmessage`.`id`) AS `totalmsgs`,count((case when (`requestmessage`.`IsRead` = 'true') then 1 end)) AS `readmsgs`,count((case when (`requestmessage`.`IsRead` = 'false') then 1 end)) AS `unreadmsgs`,substring_index(group_concat(`requestmessage`.`MessageDescription` order by `requestmessage`.`createdts` ASC,`requestmessage`.`id` ASC separator '||'),'||',1) AS `firstMessage`,group_concat(`requestmessage`.`id` separator ',') AS `msgids`,count(`messageattachment`.`id`) AS `totalAttachments` from ((((((`customerrequest` left join `requestmessage` on((`customerrequest`.`id` = `requestmessage`.`CustomerRequest_id`))) left join `requestcategory` on((`requestcategory`.`id` = `customerrequest`.`RequestCategory_id`))) left join `customer` on((`customer`.`id` = `customerrequest`.`Customer_id`))) left join `status` on((`status`.`id` = `customerrequest`.`Status_id`))) left join `messageattachment` on((`messageattachment`.`RequestMessage_id` = `requestmessage`.`id`))) left join `systemuser` on((`systemuser`.`id` = `customerrequest`.`AssignedTo`))) group by `customerrequest`.`id` order by `customerrequest`.`createdts` desc,`customerrequest`.`id`;

DROP VIEW IF EXISTS `customer_device_information_view`;
CREATE VIEW `customer_device_information_view` AS select `customerdevice`.`id` AS `Device_id`,`customerdevice`.`Customer_id` AS `Customer_id`,`customer`.`UserName` AS `Customer_username`,`customerdevice`.`DeviceName` AS `DeviceName`,`customerdevice`.`LastLoginTime` AS `LastLoginTime`,`customerdevice`.`LastUsedIp` AS `LastUsedIp`,`customerdevice`.`Status_id` AS `Status_id`,`status`.`Description` AS `Status_name`,`customerdevice`.`OperatingSystem` AS `OperatingSystem`,`customerdevice`.`Channel_id` AS `Channel_id`,`servicechannel`.`Description` AS `Channel_Description`,`customerdevice`.`EnrollmentDate` AS `EnrollmentDate`,`customerdevice`.`createdby` AS `createdby`,`customerdevice`.`modifiedby` AS `modifiedby`,`customerdevice`.`createdts` AS `Registered_Date`,`customerdevice`.`lastmodifiedts` AS `lastmodifiedts` from (((`customerdevice` left join `status` on((`customerdevice`.`Status_id` = `status`.`id`))) left join `servicechannel` on((`servicechannel`.`id` = `customerdevice`.`Channel_id`))) left join `customer` on((`customer`.`id` = `customerdevice`.`Customer_id`)));

DROP TABLE IF EXISTS `currency`;
CREATE TABLE `currency` (
  `code` varchar(10) NOT NULL,
  `name` varchar(50) NOT NULL,
  `symbol` varchar(5) NOT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `locationcurrency`;
CREATE TABLE `locationcurrency` (
  `Location_id` varchar(50) NOT NULL,
  `currency_code` varchar(50) NOT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) unsigned zerofill DEFAULT '0',
  PRIMARY KEY (`Location_id`,`currency_code`),
  KEY `IXFK_LocationCurrency_Location` (`Location_id`),
  KEY `FH_LocationCurrency_currency_code_idx` (`currency_code`),
  CONSTRAINT `FH_LocationCurrency_Location_id` FOREIGN KEY (`Location_id`) REFERENCES `location` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FH_LocationCurrency_currency_code` FOREIGN KEY (`currency_code`) REFERENCES `currency` (`code`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `facility`;
CREATE TABLE `facility` (
  `id` varchar(50) NOT NULL,
  `code` varchar(50) NOT NULL UNIQUE,
  `name` varchar(50) NOT NULL,
  `description` varchar(1000) NOT NULL,
  `facilitytype` varchar(10) NOT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `locationfacility`;
CREATE TABLE `locationfacility` (
  `Location_id` varchar(50) NOT NULL,
  `facility_id` varchar(50) NOT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) unsigned zerofill DEFAULT '0',
  PRIMARY KEY (`Location_id`,`facility_id`),
  KEY `IXFK_LocationCustomerFacility_Location` (`Location_id`),
  KEY `FH_LocationCustomerFacility_facility_id_idx` (`facility_id`),
  CONSTRAINT `FH_LocationCustomerFacility_Location_id` FOREIGN KEY (`Location_id`) REFERENCES `location` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FH_LocationCustomerFacility_facility_id` FOREIGN KEY (`facility_id`) REFERENCES `facility` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `customersegment`;
CREATE TABLE `customersegment` (
  `id` varchar(50) NOT NULL,
  `type` varchar(50) NOT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `locationcustomersegment`;
CREATE TABLE `locationcustomersegment` (
  `Location_id` varchar(50) NOT NULL,
  `segment_id` varchar(50) NOT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) unsigned zerofill DEFAULT '0',
  PRIMARY KEY (`Location_id`,`segment_id`),
  KEY `IXFK_LocationCustomerSegment_Location` (`Location_id`),
  KEY `FH_LocationCustomerSegment_segment_id_idx` (`segment_id`),
  CONSTRAINT `FH_LocationCustomerSegment_Location_id` FOREIGN KEY (`Location_id`) REFERENCES `location` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FH_LocationCustomerSegment_segment_id` FOREIGN KEY (`segment_id`) REFERENCES `customersegment` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `location` ADD COLUMN `isMobile` TINYINT(1)unsigned zerofill DEFAULT '0' AFTER `WebSiteUrl`;

DROP VIEW IF EXISTS `locationdetails_view`;
CREATE VIEW `locationdetails_view` AS select distinct  `location`.`id` AS `locationId`,(select group_concat(distinct concat(upper(left( `dayschedule`.`WeekDayName`,1)),lower(substr( `dayschedule`.`WeekDayName`,2)),':',convert(substr( `dayschedule`.`StartTime`,1,5) using utf8),'-',convert(substr( `dayschedule`.`EndTime`,1,5) using utf8)) separator ' || ') from ( `dayschedule` join  `location` `l`) where (( `dayschedule`.`WorkSchedule_id` = `l`.`WorkSchedule_id`) and (`l`.`id` =  `location`.`id`))) AS `workingHours`, `location`.`Name` AS `informationTitle`, `location`.`Description` AS `Description`, `location`.`Code` AS `Code`, `location`.`PhoneNumber` AS `phone`, `location`.`EmailId` AS `email`,(case  `location`.`Status_id` when 'SID_ACTIVE' then 'OPEN' else 'CLOSED' end) AS `status`, `location`.`Type_id` AS `type`, `location`.`isMobile` AS `isMobile`, `location`.`IsMainBranch` AS `IsMainBranch`,(select group_concat( `service`.`Name` order by  `service`.`Name` ASC separator ' || ') from ( `service` join  `locationservice`) where (( `service`.`id` =  `locationservice`.`Service_id`) and ( `locationservice`.`Location_id` =  `location`.`id`))) AS `services`,(select group_concat( `currency`.`code` order by  `currency`.`code` ASC separator ',') from ( `currency` join  `locationcurrency`) where (( `currency`.`code` = convert( `locationcurrency`.`currency_code` using utf8)) and (convert( `locationcurrency`.`Location_id` using utf8) =  `location`.`id`))) AS `currencies`,(select group_concat( `customersegment`.`type` order by  `customersegment`.`type` ASC separator ',') from ( `customersegment` join  `locationcustomersegment`) where (( `customersegment`.`id` = convert( `locationcustomersegment`.`segment_id` using utf8)) and (convert( `locationcustomersegment`.`Location_id` using utf8) =  `location`.`id`))) AS `segments`,(select group_concat( `facility`.`name` order by  `facility`.`id` ASC separator ',') from ( `facility` join  `locationfacility`) where (( `facility`.`id` = convert( `locationfacility`.`facility_id` using utf8)) and (convert( `locationfacility`.`Location_id` using utf8) =  `location`.`id`))) AS `facilities_names`,(select group_concat( `facility`.`code` order by  `facility`.`id` ASC separator ',') from ( `facility` join  `locationfacility`) where (( `facility`.`id` = convert( `locationfacility`.`facility_id` using utf8)) and (convert( `locationfacility`.`Location_id` using utf8) =  `location`.`id`))) AS `facilities_codes`,(select  `city`.`Name` from  `city` where ( `city`.`id` =  `address`.`City_id`)) AS `city`,(select  `region`.`Name` from  `region` where ( `region`.`id` =  `address`.`Region_id`)) AS `region`,(select  `country`.`Name` from  `country` where ( `country`.`id` = (select  `region`.`Country_id` from DUAL  where ( `region`.`id` =  `address`.`Region_id`)))) AS `country`, `address`.`addressLine1` AS `addressLine1`, `address`.`addressLine2` AS `addressLine2`, `address`.`addressLine3` AS `addressLine3`, `address`.`zipCode` AS `zipCode`, `address`.`latitude` AS `latitude`, `address`.`logitude` AS `longitude` from (( `address` join  `location`) join  `region`) where (( `address`.`id` =  `location`.`Address_id`) and ( `region`.`id` =  `address`.`Region_id`)) order by  `location`.`id`;

DROP VIEW IF EXISTS `locationservices_view`;
CREATE VIEW `locationservices_view` AS select `location`.`id` AS `Location_id`,`location`.`Name` AS `Location_Name`,`location`.`DisplayName` AS `Location_Display_Name`,`location`.`Description` AS `Location_Description`,`location`.`PhoneNumber` AS `Location_Phone_Number`,`location`.`EmailId` AS `Location_EmailId`,`address`.`latitude` AS `Location_Latitude`,`address`.`logitude` AS `Location_Longitude`,`address`.`id` AS `Location_Address_id`,`location`.`IsMainBranch` AS `Location_IsMainBranch`,`location`.`Status_id` AS `Location_Status_id`,`location`.`Type_id` AS `Location_Type_id`,`location`.`Code` AS `Location_Code`,`location`.`softdeleteflag` AS `Location_DeleteFlag`,`location`.`WorkSchedule_id` AS `Location_WorkScheduleId`,`facility`.`id` AS `Facility_id`,`facility`.`code` AS `Facility_code`,`facility`.`name` AS `Facility_name`,`facility`.`description` AS `Facility_description`,(select group_concat(`currency`.`code` order by `currency`.`code` ASC separator ',') from (`currency` join `locationcurrency`) where ((`currency`.`code` = `locationcurrency`.`currency_code`) and (`locationcurrency`.`Location_id` = `location`.`id`))) AS `currencies`,`weekday`.`StartTime` AS `Weekday_StartTime`,`weekday`.`EndTime` AS `Weekday_EndTime`,`sunday`.`StartTime` AS `Sunday_StartTime`,`sunday`.`EndTime` AS `Sunday_EndTime`,`saturday`.`StartTime` AS `Saturday_StartTime`,`saturday`.`EndTime` AS `Saturday_EndTime`,concat(`address`.`addressLine1`,', ',(select `city`.`Name` from `city` where (`city`.`id` = `address`.`City_id`)),', ',(select `region`.`Name` from `region` where (`region`.`id` = `address`.`Region_id`)),', ',(select `country`.`Name` from `country` where `country`.`id` in (select `city`.`Country_id` from `city` where (`city`.`id` = `address`.`City_id`))),', ',`address`.`zipCode`) AS `ADDRESS` from ((((((`location` left join `locationfacility` on((`location`.`id` = `locationfacility`.`Location_id`))) left join `facility` on((`locationfacility`.`facility_id` = `facility`.`id`))) left join `dayschedule` `weekday` on(((`location`.`WorkSchedule_id` = `weekday`.`WorkSchedule_id`) and (`weekday`.`WeekDayName` = 'MONDAY')))) left join `dayschedule` `sunday` on(((`location`.`WorkSchedule_id` = `sunday`.`WorkSchedule_id`) and (`sunday`.`WeekDayName` = 'SUNDAY')))) left join `dayschedule` `saturday` on(((`location`.`WorkSchedule_id` = `saturday`.`WorkSchedule_id`) and (`saturday`.`WeekDayName` = 'SATURDAY')))) join `address` on((`address`.`id` = `location`.`Address_id`)));

DROP VIEW IF EXISTS `locationfacility_view`;
CREATE VIEW `locationfacility_view` AS select `location`.`id` AS `Location_id`,`facility`.`id` AS `Facility_id`,`facility`.`code` AS `Facility_code`,`facility`.`name` AS `Facility_Name`,`facility`.`description` AS `Facility_description`,`facility`.`facilitytype` AS `Facility_facilitytype` from ((`location` left join `locationfacility` on((`location`.`id` = `locationfacility`.`Location_id`))) join `facility` on((`locationfacility`.`facility_id` = `facility`.`id`)));

DROP PROCEDURE IF EXISTS `location_details_proc`;
DELIMITER $$
CREATE PROCEDURE `location_details_proc`(
  in _locationId char(50)
)
BEGIN DECLARE workingHours TEXT(2000);
DECLARE tempDayName TEXT(2000);
DECLARE tempStartTime TEXT(2000);
DECLARE tempEndTime TEXT(2000);
DECLARE servicesList TEXT(10000);
DECLARE currencyList TEXT(10000);

DECLARE b int(1);
DECLARE pipeFlag int(1);
DECLARE cur_1 CURSOR FOR 
select 
  dayschedule.WeekDayName, 
  dayschedule.StartTime, 
  dayschedule.EndTime 
from 
  dayschedule, 
  location 
WHERE 
  location.WorkSchedule_id = dayschedule.WorkSchedule_id 
  and location.id = _locationId;
DECLARE serviceCursor CURSOR FOR 
SELECT 
  service.name 
from 
  service, 
  locationservice 
WHERE 
  service.id = locationservice.Service_id 
  and locationservice.Location_id = _locationId;

DECLARE currencyCursor CURSOR FOR 
SELECT 
  currency.code 
from 
  currency, 
  locationcurrency 
WHERE 
  currency.code = locationcurrency.currency_code 
  and locationcurrency.Location_id = _locationId;
  
DECLARE CONTINUE HANDLER FOR NOT FOUND 
SET 
  b = 1;
set 
  @a = "";
set 
  @wh = "";
set 
  @pipeFlag = 0;
OPEN cur_1;
REPEAT 
set 
  tempDayName = "";
set 
  tempStartTime = "";
set 
  tempEndTime = "";
FETCH cur_1 INTO tempDayName, 
tempStartTime, 
tempEndTime;
if tempDayName <> "" then if @pipeFlag = 0 then 
set 
  @a = CONCAT(
    UCASE(
      LEFT(tempDayName, 1)
    ), 
    LCASE(
      SUBSTRING(tempDayName, 2)
    ), 
    ": ", 
    SUBSTRING(tempStartTime, 1, 5), 
    "AM", 
    " - ", 
    SUBSTRING(tempEndTime, 1, 5), 
    "PM"
  );
else 
set 
  @a = CONCAT(
    " || ", 
    UCASE(
      LEFT(tempDayName, 1)
    ), 
    LCASE(
      SUBSTRING(tempDayName, 2)
    ), 
    ": ", 
    SUBSTRING(tempStartTime, 1, 5), 
    "AM", 
    " - ", 
    SUBSTRING(tempEndTime, 1, 5), 
    "PM"
  );
end if;
set 
  @pipeFlag = 1;
set 
  @a = CONCAT(
    " || ", tempDayName, ": ", tempStartTime, 
    "AM", " - ", tempEndTime, "PM"
  );
set 
  @wh = CONCAT(@wh, " ", @a, "");
end if;
UNTIL b = 1 END REPEAT;
CLOSE cur_1;
set 
  workingHours = @wh;
set 
  servicesList = (
    SELECT 
      GROUP_CONCAT(
        service.name 
        ORDER BY 
          service.name ASC SEPARATOR ' || '
      ) as services 
    FROM 
      service, 
      locationservice 
    WHERE 
      service.id = locationservice.Service_id 
      and locationservice.Location_id = _locationId
  );  
set 
  currencyList = (
    SELECT 
      GROUP_CONCAT(
        currency.code 
        ORDER BY 
          currency.code ASC SEPARATOR ' , '
      ) as currencies 
    FROM 
      currency, 
      locationcurrency 
    WHERE 
      currency.code = locationcurrency.currency_code 
      and locationcurrency.Location_id = _locationId
  );  
SELECT 
  `location`.`id` AS locationId, 
  `location`.`Type_id` AS type, 
  `location`.`Name` AS informationTitle, 
  CASE `location`.`status_id` WHEN 'SID_ACTIVE' THEN 'OPEN' ELSE 'CLOSED' END AS status, 
  `location`.`DisplayName` AS displayName, 
  `location`.`Description` AS description, 
  `location`.`PhoneNumber` AS phone, 
  `location`.`EmailId` AS email, 
  `location`.`WorkingDays` AS workingDays, 
  `location`.`IsMainBranch` AS isMainBranch, 
  `location`.`MainBranchCode` AS mainBranchCode, 
  `address`.`AddressLine1` AS addressLine1, 
  `address`.`AddressLine2` AS addressLine2, 
  `address`.`AddressLine3` AS addressLine3, 
  `address`.`Latitude` AS latitude, 
  `address`.`Logitude` AS longitude, 
  workingHours AS workingHours,
  servicesList AS services, 
  currencyList AS currencies 
FROM 
  (
    (
      `location` 
      JOIN `address` ON (
        (
          `location`.`address_id` = `address`.`id`
        )
      )
    )
  ) 
WHERE 
  `location`.`id` = `_locationId`;
END$$
DELIMITER ;

DROP VIEW IF EXISTS `customerbasicinfo_view`;
CREATE VIEW `customerbasicinfo_view` AS select `customer`.`UserName` AS `Username`,`customer`.`FirstName` AS `FirstName`,`customer`.`MiddleName` AS `MiddleName`,`customer`.`LastName` AS `LastName`,concat(ifnull(`customer`.`FirstName`,''),' ',ifnull(`customer`.`MiddleName`,''),' ',ifnull(`customer`.`LastName`,'')) AS `Name`,`customer`.`Salutation` AS `Salutation`,`customer`.`id` AS `Customer_id`,`customer`.`Ssn` AS `SSN`,`customer`.`createdts` AS `CustomerSince`,`customer`.`Gender` AS `Gender`,`customer`.`DateOfBirth` AS `DateOfBirth`,`customer`.`Status_id` AS `CustomerStatus_id`,`customerstatus`.`Description` AS `CustomerStatus_name`,`customer`.`MaritalStatus_id` AS `MaritalStatus_id`,`maritalstatus`.`Description` AS `MaritalStatus_name`,`customer`.`SpouseName` AS `SpouseName`,`customer`.`lockedOn` AS `lockedOn`,`customer`.`lockCount` AS `lockCount`,`customer`.`EmployementStatus_id` AS `EmployementStatus_id`,`employementstatus`.`Description` AS `EmployementStatus_name`,(select group_concat(`customerflagstatus`.`Status_id`,' ' separator ',') from `customerflagstatus` where (`customerflagstatus`.`Customer_id` = `customer`.`id`)) AS `CustomerFlag_ids`,(select group_concat(`status`.`Description`,' ' separator ',') from `status` where `status`.`id` in (select `customerflagstatus`.`Status_id` from `customerflagstatus` where (`customerflagstatus`.`Customer_id` = `customer`.`id`))) AS `CustomerFlag`,`customer`.`IsEnrolledForOlb` AS `IsEnrolledForOlb`,`customer`.`IsStaffMember` AS `IsStaffMember`,`customer`.`Location_id` AS `Branch_id`,`location`.`Name` AS `Branch_name`,`location`.`Code` AS `Branch_code`,`customer`.`IsOlbAllowed` AS `IsOlbAllowed`,`customer`.`IsAssistConsented` AS `IsAssistConsented`,`customer`.`isEagreementSigned` AS `isEagreementSigned`,`customer`.`CustomerType_id` AS `CustomerType_id`,`customertype`.`Name` AS `CustomerType_Name`,`customertype`.`Description` AS `CustomerType_Description`,(select `membergroup`.`Name` from `membergroup` where `membergroup`.`id` in (select `customergroup`.`Group_id` from `customergroup` where (`customer`.`id` = `customergroup`.`Customer_id`)) limit 1) AS `Customer_Role`,(select `membergroup`.`isEAgreementActive` from `membergroup` where `membergroup`.`id` in (select `customergroup`.`Group_id` from `customergroup` where (`customer`.`id` = `customergroup`.`Customer_id`)) limit 1) AS `isEAgreementRequired`,`customer`.`Organization_Id` AS `organisation_id`,`organisation`.`Name` AS `organisation_name` from ((((((`customer` left join `location` on((`customer`.`Location_id` = `location`.`id`))) left join `organisation` on((`customer`.`Organization_Id` = `organisation`.`id`))) left join `customertype` on((`customer`.`CustomerType_id` = `customertype`.`id`))) left join `status` `customerstatus` on((`customer`.`Status_id` = `customerstatus`.`id`))) left join `status` `maritalstatus` on((`customer`.`MaritalStatus_id` = `maritalstatus`.`id`))) left join `status` `employementstatus` on((`customer`.`EmployementStatus_id` = `employementstatus`.`id`)));

CREATE TABLE `onboardingtermsandconditions` (
  `id` VARCHAR(50) NOT NULL,
  `name` TEXT NULL,
  `description` TEXT NULL,
  `status_id` VARCHAR(50) NULL,
  `createdby` VARCHAR(50) NULL,
  `modifiedby` VARCHAR(50) NULL,
  `createdts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `FK_OnboardingTermsAndConditions_Status_idx` (`status_id` ASC),
  CONSTRAINT `FK_OnboardingTermsAndConditions_Status`
    FOREIGN KEY (`status_id`)
    REFERENCES `status` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)ENGINE=InnoDB DEFAULT CHARSET=utf8;
    
ALTER TABLE `customerdevice` ADD COLUMN `appid` VARCHAR(100) NULL AFTER `EnrollmentDate`;

ALTER TABLE `membergroup` 
CHANGE COLUMN `Description` `Description` VARCHAR(250) NULL DEFAULT NULL ;

DROP VIEW IF EXISTS `applicant_view`;
CREATE VIEW `applicant_view` AS select `applicant`.`id` AS `Application_id`,`applicant`.`FirstName` AS `FirstName`,`applicant`.`MiddleName` AS `MiddleName`,`applicant`.`LastName` AS `LastName`,`applicant`.`DocumentsSubmitted` AS `DocumentsSubmitted`,`applicant`.`ContactNumber` AS `ContactNumber`,`applicant`.`EmailID` AS `EmailID`,`applicant`.`createdby` AS `applicant_createdby`,`applicant`.`modifiedby` AS `applicant_modifiedby`,`applicant`.`createdts` AS `applicant_createdts`,`applicant`.`lastmodifiedts` AS `applicant_lastmodifiedts`,`applicant`.`Channel` AS `Channel`,`applicant`.`Status` AS `Status`,`applicant`.`Product` AS `Product`,`applicant`.`Reason` AS `Reason` from `applicant`;

DROP PROCEDURE IF EXISTS `reports_messages_received`;
DELIMITER $$
CREATE PROCEDURE `reports_messages_received`(
  in from_date VARCHAR(10), 
  in to_date VARCHAR(10), 
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
    quote(STR_TO_DATE(from_date, '%Y-%m-%d')), 
    " and createdts <= ", 
    quote(STR_TO_DATE(to_date, '%Y-%m-%d'))
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
    and RepliedBy = ", 
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
  in from_date VARCHAR(10), 
  in to_date VARCHAR(10), 
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
    quote(STR_TO_DATE(from_date, '%Y-%m-%d')), 
    " and createdts <= ", 
    quote(STR_TO_DATE(to_date, '%Y-%m-%d'))
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
      and RepliedBy = ", 
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
  in from_date VARCHAR(10), 
  in to_date VARCHAR(10), 
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
    quote(STR_TO_DATE(from_date, '%Y-%m-%d')), 
    " and createdts <= ", 
    quote(STR_TO_DATE(to_date, '%Y-%m-%d'))

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
            and RepliedBy = ", 
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
  in from_date VARCHAR(10), 
  in to_date VARCHAR(10), 
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
              Status_id != 'SID_RESOLVED'";
if from_date != '' 
and from_date != '' then 
set 
  @queryStatement = concat(
    @queryStatement, 
    " 
              and createdts >= ", 
    quote(STR_TO_DATE(from_date, '%Y-%m-%d')), 
    " and createdts <= ", 
    quote(STR_TO_DATE(quote(to_date), '%Y-%m-%d'))
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
  in from_date VARCHAR(10), 
  in to_date VARCHAR(10), 
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
    quote(STR_TO_DATE(from_date, '%Y-%m-%d')), 
    " and createdts <= ", 
    quote(STR_TO_DATE(to_date, '%Y-%m-%d'))
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

DROP FUNCTION IF EXISTS `func_split_str`;
DELIMITER $$
CREATE FUNCTION `func_split_str`(
  X VARCHAR(255),
  delim VARCHAR(12),
  pos INT
) RETURNS varchar(255) CHARSET utf8
    READS SQL DATA
    DETERMINISTIC
RETURN REPLACE(SUBSTRING(SUBSTRING_INDEX(X, delim, pos),
       LENGTH(SUBSTRING_INDEX(X, delim, pos -1)) + 1),
       delim, '')$$
DELIMITER ;

DROP FUNCTION IF EXISTS `func_escape_input_for_in_operator`;
DELIMITER $$
CREATE FUNCTION `func_escape_input_for_in_operator`(
  _input VARCHAR(2000)
) RETURNS varchar(2000) CHARSET utf8
    DETERMINISTIC
BEGIN
    DECLARE result VARCHAR(2500);
    DECLARE element VARCHAR(512);
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

ALTER TABLE `location` CHANGE COLUMN `Description` `Description` VARCHAR(300) NULL DEFAULT NULL ;

DROP VIEW IF EXISTS `card_request_notification_count_view`;
CREATE VIEW `card_request_notification_count_view` AS select `cardaccountrequest_alias`.`Customer_id` AS `customerId`,`cardaccountrequest_alias`.`CardAccountNumber` AS `cardNumber`,'REQUEST' AS `reqType`,count(`cardaccountrequest_alias`.`CardAccountNumber`) AS `requestcount` from `cardaccountrequest` `cardaccountrequest_alias` group by `cardaccountrequest_alias`.`Customer_id`,`cardaccountrequest_alias`.`CardAccountNumber` union select `notificationcardinfo_alias`.`Customer_id` AS `customerId`,`notificationcardinfo_alias`.`CardNumber` AS `cardNumber`,'NOTIFICATION' AS `reqType`,1 AS `requestcount` from `notificationcardinfo` `notificationcardinfo_alias` group by `notificationcardinfo_alias`.`Customer_id`,`notificationcardinfo_alias`.`Notification_id`;
