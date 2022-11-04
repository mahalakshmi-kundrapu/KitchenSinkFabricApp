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
    AND TIMESTAMP(DATE(acr.createdts)) >= '", 
    _dateInitialPoint, "' 
    AND ", 
    " TIMESTAMP(DATE(acr.createdts)) <= '", _dateFinalPoint, 
    "'"
  );
ELSEIF _dateInitialPoint != '' THEN 
SET 
  @whereclause = CONCAT(
    @whereclause, " 
    AND TIMESTAMP(DATE(acr.createdts)) = '", 
    _dateInitialPoint, "'"
  );
ELSEIF _dateFinalPoint != '' THEN 
SET 
  @whereclause = CONCAT(
    @whereclause, " 
    AND TIMESTAMP(DATE(acr.createdts)) = '", 
    _dateFinalPoint, "'"
  );
END IF;
IF _requestStatusID != '' THEN 
SET 
  @whereclause = CONCAT(
    @whereclause, " 
    AND acr.Status_id IN 
    (
      '", 
    _requestStatusID, "'
    )
    "
  );
END IF;
IF _requestCategory != '' THEN 
SET 
  @whereclause = CONCAT(
    @whereclause, " 
    AND acr.RequestCategory_id = '", 
    _requestCategory, "'"
  );
END IF;
IF _requestAssignedTo != '' THEN 
SET 
  @whereclause = CONCAT(
    @whereclause, " 
    AND acr.AssignedTo = '", 
    _requestAssignedTo, "'"
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
  AND archivedrequestmessage.RepliedBy_id = '", 
    _messageRepliedBy, "'"
  );
END IF;
IF _searchKey != '' THEN 
SET 
  @joinCustomer = 1;
SET 
  @whereclause = CONCAT(
    @whereclause, " 
  AND 
  (
    acr.Customer_id LIKE '%", 
    _searchKey, "%' OR acr.id LIKE '%", 
    _searchKey, "%' OR customer.Username LIKE '%", 
    _searchKey, "%')"
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
END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS `customer_request_archive_proc`;
DELIMITER $$
CREATE PROCEDURE `customer_request_archive_proc`()
BEGIN
#Proc to Faclitate Archiving and Purging of Requests.
#Author : Aditya Mankal (KH2322) 

#NOT Found Handlers - START
DECLARE customer_request_cursor_done BOOLEAN DEFAULT FALSE;
DECLARE customer_requestmessage_cursor_done BOOLEAN DEFAULT FALSE;  
DECLARE customer_requestmessageattachment_cursor_done BOOLEAN DEFAULT FALSE;
#NOT Found Handlers - END

DECLARE curr_request_id VARCHAR(50) DEFAULT '';
DECLARE curr_requestmessage_id VARCHAR(50) DEFAULT '';
DECLARE curr_media_id VARCHAR(50) DEFAULT '';
DECLARE curr_messageattachment_id VARCHAR(50) DEFAULT 0;
DECLARE media_count INT(10) DEFAULT 0;
DECLARE customer_request_cursor CURSOR FOR SELECT id FROM customerrequest WHERE Status_id = 'SID_RESOLVED' and TIMESTAMPDIFF(SQL_TSI_SECOND ,lastmodifiedts, now())>=15552000;#6months
DECLARE CONTINUE HANDLER FOR NOT FOUND SET customer_request_cursor_done = TRUE,customer_requestmessage_cursor_done=TRUE,customer_requestmessageattachment_cursor_done=TRUE;
	OPEN customer_request_cursor;
	customer_customerrequest_loop:REPEAT 
	FETCH customer_request_cursor INTO curr_request_id;
	IF NOT customer_request_cursor_done THEN
		INSERT INTO archivedcustomerrequest SELECT *FROM customerrequest where id=curr_request_id;
		UPDATE archivedcustomerrequest SET `Status_id`='SID_ARCHIVED' WHERE id=curr_request_id;
			select curr_request_id;
			
			REQUESTMESSAGEBLOCK: BEGIN
			  DECLARE customer_requestmessage_cursor CURSOR FOR SELECT id FROM requestmessage WHERE CustomerRequest_id = curr_request_id;
				OPEN customer_requestmessage_cursor;
				   customer_requestmessage_loop:REPEAT 
					FETCH customer_requestmessage_cursor INTO curr_requestmessage_id;
					IF NOT customer_requestmessage_cursor_done THEN
						INSERT INTO archivedrequestmessage SELECT *FROM requestmessage where id = curr_requestmessage_id;
						
						  MESSAGEATTACHMENTBLOCK:BEGIN
							   DECLARE customer_messageattachment_cursor CURSOR FOR SELECT id FROM messageattachment WHERE RequestMessage_id =curr_requestmessage_id;								
								OPEN customer_messageattachment_cursor;
								  customer_messageattachment_loop:REPEAT 
									  FETCH customer_messageattachment_cursor INTO curr_messageattachment_id;
								      IF NOT customer_requestmessageattachment_cursor_done THEN
									  SELECT Media_id from messageattachment where id = curr_messageattachment_id into curr_media_id;
									  
									  SELECT count(id) from archivedmedia where id = curr_media_id into media_count;
									  IF media_count = 0 THEN #Attempt to INSERT only in case of a non archived attachment
										INSERT INTO archivedmedia SELECT *FROM media where id = curr_media_id;
									  END IF;
									  INSERT INTO archivedmessageattachment SELECT *FROM messageattachment where id = curr_messageattachment_id;
								   
									  DELETE FROM messageattachment where id = curr_messageattachment_id;
									  SELECT count(id) from messageattachment where Media_id = curr_media_id into media_count;
									  IF media_count = 0 THEN #Attempt to delete when no other message is pointing to the media as an attachment
										DELETE FROM media where id = curr_media_id;
									  END IF;
								   
								  END IF;
								  UNTIL customer_requestmessageattachment_cursor_done 
								  END REPEAT customer_messageattachment_loop;
								  SET customer_request_cursor_done=FALSE;
								  SET customer_requestmessage_cursor_done=FALSE;
								  SET customer_requestmessageattachment_cursor_done=FALSE;									  
								CLOSE customer_messageattachment_cursor;
							END MESSAGEATTACHMENTBLOCK;
							
							DELETE FROM requestmessage where id = curr_requestmessage_id;
					END IF;	
				  UNTIL customer_requestmessage_cursor_done 
				  END REPEAT customer_requestmessage_loop;
				  SET customer_request_cursor_done=FALSE;
				  SET customer_requestmessage_cursor_done=FALSE;
				  SET customer_requestmessageattachment_cursor_done=FALSE;
				CLOSE customer_requestmessage_cursor;
			END REQUESTMESSAGEBLOCK;
			
		 DELETE FROM customerrequest where id = curr_request_id;
        END IF;
	UNTIL customer_request_cursor_done 
	END REPEAT customer_customerrequest_loop;
	SET customer_request_cursor_done=FALSE;
	SET customer_requestmessage_cursor_done=FALSE;
	SET customer_requestmessageattachment_cursor_done=FALSE;	
	CLOSE customer_request_cursor;
END $$
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
  AND TIMESTAMP(DATE(cr.createdts)) >= '", 
    _dateInitialPoint, "' 
  AND ", 
    " TIMESTAMP(DATE(cr.createdts)) <= '", _dateFinalPoint, 
    "'"
  );
ELSEIF _dateInitialPoint != '' THEN 
SET 
  @whereclause = CONCAT(
    @whereclause, " 
  AND TIMESTAMP(DATE(cr.createdts)) = '", 
    _dateInitialPoint, "'"
  );
ELSEIF _dateFinalPoint != '' THEN 
SET 
  @whereclause = CONCAT(
    @whereclause, " 
  AND TIMESTAMP(DATE(cr.createdts)) = '", 
    _dateFinalPoint, "'"
  );
END IF;
IF _requestStatusID != '' THEN 
SET 
  @whereclause = CONCAT(
    @whereclause, " 
  AND cr.Status_id IN 
  (
    '", 
    _requestStatusID, "'
  )
  "
  );
END IF;
IF _requestCategory != '' THEN 
SET 
  @whereclause = CONCAT(
    @whereclause, " 
  AND cr.RequestCategory_id = '", 
    _requestCategory, "'"
  );
END IF;
IF _requestAssignedTo != '' THEN 
SET 
  @whereclause = CONCAT(
    @whereclause, " 
  AND cr.AssignedTo = '", 
    _requestAssignedTo, "'"
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
  AND requestmessage.RepliedBy_id = '", 
    _messageRepliedBy, "'"
  );
END IF;
IF _searchKey != '' THEN 
SET 
  @joinCustomer = 1;
SET 
  @whereclause = CONCAT(
    @whereclause, " 
  AND 
  (
    cr.Customer_id LIKE '%", 
    _searchKey, "%' OR cr.id LIKE '%", 
    _searchKey, "%' OR customer.Username LIKE '%", 
    _searchKey, "%')"
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
END $$
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
in _requestID varchar(50),
in _branchIDS varchar(2000), 
in _productIDS varchar(2000), 
in _cityIDS varchar(2000), 
in _entitlementIDS varchar(2000), 
in _groupIDS varchar(2000), 
in _customerStatus char(50), 
in _before varchar(20), 
in _after varchar(20), 
in _sortVariable varchar(100), 
in _sortDirection varchar(4),  
in _pageOffset bigint, 
in _pageSize bigint)
begin
	IF _searchType = 'APPLICANT_SEARCH' then
		  set @queryStatement = concat("select applicant.id, applicant.FirstName,applicant.MiddleName, applicant.LastName, 
				concat(applicant.FirstName, ' ', applicant.MiddleName, ' ', applicant.LastName) as name, 
				applicant.ContactNumber, applicant.EmailID, applicant.`Status` as Status_desc
			from applicant
			JOIN(
				select applicant.id
				from applicant
				where
				id like concat('%', '",_id ,"','%')  and
				concat(firstname, middlename, lastname) like concat('%','",_name,"','%') and
				coalesce(ContactNumber, '') like concat('%','", _phone ,"','%') and 
				coalesce(EmailID, '') like concat('%','", _email ,"','%')
				) paginatedApplicants ON (paginatedApplicants.id = applicant.id)
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
            
	ELSEIF _searchType = 'APPLICANT_SEARCH_TOTAL_COUNT' then

			SELECT sum(paginatedApplicants.applicant) as SearchMatchs from (
				select 1 as applicant
				from applicant
				where id like concat('%', _id ,'%')  and
				concat(firstname, middlename, lastname) like concat('%',_name,'%') and
				coalesce(ContactNumber, '') like concat('%', _phone ,'%') and 
				coalesce(EmailID, '') like concat('%', _email ,'%')
				) paginatedApplicants;
            
	ELSEIF _searchType = 'GROUP_SEARCH' then

				set @queryStatement = "SELECT 
					customer.id, customer.FirstName, customer.MiddleName, customer.LastName,
					concat(customer.FirstName, ' ', customer.MiddleName, ' ', customer. LastName) as name,
					customer.Username, customer.Salutation, customer.Gender,
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
					set @whereclause = concat(@whereclause, " AND (concat(customer.firstname,' ', customer.middlename, ' ', customer.lastname) like concat('%','",_username,"','%')");
					
					-- customer username
					set @whereclause = concat(@whereclause," OR customer.username like concat('%', '",_username,"' ,'%')");
					
					-- customer id
					set @whereclause = concat(@whereclause," OR customer.id like concat('%','", _username ,"','%')) ");
			end if;
			-- customer entitlement ids
			if _entitlementIDS != "" then
							set @whereclause = concat(@whereclause," AND customerentitlement.Service_id in (",_entitlementIDS,") 
								OR customergroup.Group_id in ( select distinct Group_id from groupentitlement where Service_id in (",_entitlementIDS," ))");
			end if;
			
			-- customer group ids
			if _groupIDS != "" then
							set @whereclause = concat(@whereclause," AND customergroup.Group_id in (",_groupIDS,") ");
			end if;
			
			-- customer product ids
			if _productIDS != "" then
							set @whereclause = concat(@whereclause," AND customerproduct.Product_id in (",_productIDS,")");
			end if;
			
			-- customer branch ids
			if _branchIDS != "" then
							set @whereclause = concat(@whereclause, " AND customer.Location_id in (",_branchIDS,")");
			end if;
			
			-- customer status
			if _customerStatus != "" then
							set @whereclause = concat(@whereclause, " AND status.Description like concat('%','", _customerStatus ,"','%')");
			end if;
			
			-- customer city ids
			if _cityIDS != "" then
							set @whereclause = concat(@whereclause, " AND address.City_id in (",_cityIDS,")");
			end if;
			
			-- customer date range search
			if _before != "" and _after != "" then
							set @whereclause = concat(@whereclause, " AND date(customer.createdts) >= date '", _before ,"' and date(customer.createdts) <= date '", _after ,"'");
			else 
				if _before != "" then
							set @whereclause = concat(@whereclause, " AND date(customer.createdts) <= date '", _before ,"'");
				elseif _after != "" then
							set @whereclause = concat(@whereclause, " AND date(customer.createdts) >= date '", _after ,"'");
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
					set @whereclause = concat(@whereclause, " AND (concat(customer.firstname, ' ', customer.middlename, ' ', customer.lastname) like concat('%','",_username,"','%')");
					
					-- customer username
					set @whereclause = concat(@whereclause," OR customer.username like concat('%', '",_username,"' ,'%')");
					
					-- customer id
					set @whereclause = concat(@whereclause," OR customer.id like concat('%','", _username ,"','%')) ");
			end if;
			-- customer entitlement ids
			if _entitlementIDS != "" then
							set @whereclause = concat(@whereclause," AND customerentitlement.Service_id in (",_entitlementIDS,") 
								OR customergroup.Group_id in ( select distinct Group_id from groupentitlement where Service_id in (",_entitlementIDS," ))");
			end if;
			
			-- customer group ids
			if _groupIDS != "" then
							set @whereclause = concat(@whereclause," AND customergroup.Group_id in (",_groupIDS,") ");
			end if;
			
			-- customer product ids
			if _productIDS != "" then
							set @whereclause = concat(@whereclause," AND customerproduct.Product_id in (",_productIDS,")");
			end if;
			
			-- customer branch ids
			if _branchIDS != "" then
							set @whereclause = concat(@whereclause, " AND customer.Location_id in (",_branchIDS,")");
			end if;
			
			-- customer status
			if _customerStatus != "" then
							set @whereclause = concat(@whereclause, " AND status.Description like concat('%','", _customerStatus ,"','%')");
			end if;
			
			-- customer city ids
			if _cityIDS != "" then
							set @whereclause = concat(@whereclause, " AND address.City_id in (",_cityIDS,")");
			end if;
			
			-- customer date range search
			if _before != "" and _after != "" then
							set @whereclause = concat(@whereclause, " AND date(customer.createdts) >= date '", _before ,"' and date(customer.createdts) <= date '", _after ,"'");
			else 
				if _before != "" then
							set @whereclause = concat(@whereclause, " AND date(customer.createdts) <= date '", _before ,"'");
				elseif _after != "" then
							set @whereclause = concat(@whereclause, " AND date(customer.createdts) >= date '", _after ,"'");
				end if;
			end if;
			
			set @queryStatement = concat(@queryStatement, @whereclause, "  GROUP BY customer.id) AS customerSearch ");
			-- select @queryStatement;
			PREPARE stmt FROM @queryStatement;
			execute stmt;
			DEALLOCATE PREPARE stmt;

	ELSEIF _searchType = 'CUSTOMER_SEARCH' then
			set @queryStatement = concat("
			SELECT customer.id, customer.FirstName, customer.MiddleName, customer.LastName,customer.DateOfBirth,
				concat(customer.FirstName, ' ', customer.MiddleName, ' ', customer.LastName) as name,
				customer.Username, customer.Salutation, customer.Gender,customer.Ssn,
				PrimaryPhone.value AS PrimaryPhoneNumber,
				PrimaryEmail.value AS PrimaryEmailAddress,
                GROUP_CONCAT(distinct(membergroup.Name), ' ') as cus_groups,
				GROUP_CONCAT(customerrequest.id, ' ') as requestids
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
                WHERE customer.id like concat('%','", _id ,"','%')  AND
					concat(customer.firstname,' ',customer.middlename,' ',customer.lastname) like concat('%','",_name,"','%') AND
					('",_SSN,"' = '' or customer.Ssn = '",_SSN,"') AND
					(customer.username like concat('%','",_username,"','%')) AND 
					coalesce(PrimaryPhone.value, '') like concat('%','", _phone ,"','%') AND 
					coalesce(PrimaryEmail.value, '') like concat('%','", _email ,"','%')
				group by customer.id
                HAVING true");
                
                IF _group != '' THEN
					set @queryStatement = concat(@queryStatement," AND cus_groups like concat('%','", _group ,"','%')");
                end if;
                
                IF _requestID != '' THEN
					set @queryStatement = concat(@queryStatement," AND requestids like concat('%','", _requestID ,"','%')");
                end if;
                
                set @queryStatement = concat(@queryStatement,
                ") paginatedCustomers ON (paginatedCustomers.id=customer.id)
			LEFT JOIN customercommunication PrimaryPhone ON (PrimaryPhone.Customer_id=paginatedCustomers.id AND PrimaryPhone.isPrimary=1 AND PrimaryPhone.Type_id='COMM_TYPE_PHONE')
			LEFT JOIN customercommunication PrimaryEmail ON (PrimaryEmail.Customer_id=paginatedCustomers.id AND PrimaryEmail.isPrimary=1 AND PrimaryEmail.Type_id='COMM_TYPE_EMAIL')
			LEFT JOIN customergroup ON (customergroup.Customer_id=paginatedCustomers.id)
			LEFT JOIN membergroup ON (membergroup.id=customergroup.group_id)
            LEFT JOIN customerrequest ON (customerrequest.customer_id=customer.id)
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
       --  select @queryStatement;
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
					coalesce(PrimaryPhone.value, '') like concat('%', _phone ,'%') AND 
					coalesce(PrimaryEmail.value, '') like concat('%', _email ,'%')
				GROUP BY customer.id
				HAVING CASE
					WHEN _group != '' THEN cus_groups LIKE CONCAT('%', _group, '%') ELSE TRUE
				END
					AND CASE WHEN _requestID != '' THEN requestids LIKE CONCAT('%', _requestID, '%') ELSE TRUE
				END
				) AS customersearch;
	end if;
END $$
DELIMITER ;

DROP procedure IF EXISTS `DMS_create_user_proc`;
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
SELECT ROUND(UNIX_TIMESTAMP(CURTIME(4)) * 10) INTO customerId;
INSERT INTO `customer` (
  `id`, `FirstName`, `MiddleName`, 
  `LastName`, `Username`, `Salutation`, 
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
    '2018-04-13 11:40:06', 'SID_CUS_ACTIVE', 
    '***2564', 'SID_SINGLE', '', 'SID_EMPLOYED', 
    '1', '0', 'LID1', 'Call', 'Morning, Afternoon', 
    'Kony User', 'Kony Dev'
  );

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
    'UID4', '090871', 'KonyUser', 'KonyDev', 
    '0'
  );
set 
  reqMsgId = concat("MSG", customerId);
INSERT INTO `requestmessage` (
  `id`, `CustomerRequest_id`, `MessageDescription`, 
  `RepliedBy`, `ReplySequence`, `IsRead`, 
  `createdby`, `modifiedby`, `softdeleteflag`
) 
VALUES 
  (
    reqMsgId, customerReqId, 'What is expected resolution date?', 
    'UID3', '1', 'TRUE', 'KonyUser', 'konyolbuser', 
    '0'
  );
set 
  reqMsgId = concat("MSG", customerId + 1);
INSERT INTO `requestmessage` (
  `id`, `CustomerRequest_id`, `MessageDescription`, 
  `RepliedBy`, `ReplySequence`, `IsRead`, 
  `createdby`, `modifiedby`, `softdeleteflag`
) 
VALUES 
  (
    reqMsgId, customerReqId, 'Please escalate the request.', 
    'UID3', '2', 'TRUE', 'KonyUser', 'konyolbuser', 
    '0'
  );
set 
  reqMsgId = concat("MSG", customerId + 2);
INSERT INTO `requestmessage` (
  `id`, `CustomerRequest_id`, `MessageDescription`, 
  `RepliedBy`, `ReplySequence`, `IsRead`, 
  `createdby`, `modifiedby`, `softdeleteflag`
) 
VALUES 
  (
    reqMsgId, customerReqId, 'Please share the current status of the request.', 
    'UID3', '3', 'TRUE', 'KonyUser', 'konyolbuser', 
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
    'Bill Payment Failed', 'UID11', 
    'undefined', '1', 'admin2', '2018-04-13 11:40:06', 
    '2018-04-13 11:40:06', '2018-04-13 11:40:06', 
    '0'
  );
set 
  reqMsgId = concat("MSG", customerId + 3);
INSERT INTO `requestmessage` (
  `id`, `CustomerRequest_id`, `MessageDescription`, 
  `RepliedBy`, `ReplySequence`, `IsRead`, 
  `createdby`, `modifiedby`, `createdts`, 
  `lastmodifiedts`, `synctimestamp`, 
  `softdeleteflag`
) 
VALUES 
  (
    reqMsgId, customerReqId, '<p><span style=\"color: #000000;\">Hi,</span></p>\n<p><span style=\"color: #000000;\">&nbsp;</span></p>\n<p><span style=\"color: #000000;\">I had scheduled a bill payment to my internet provider- AT&amp;T for $25 for this Monday but the payment failed. Can you please help?</span></p>\n<p><span style=\"color: #000000;\">&nbsp;</span></p>\n<p><span style=\"font-size: 11.0pt; font-family: \'Calibri\',sans-serif; color: #000000;\">Thanks </span></p>', 
    'UID10', '1', 'TRUE', 'jane.doe', 
    'jane.doe', '2018-02-26 19:11:15', 
    '2018-02-26 19:11:15', '2018-02-26 19:11:15', 
    '0'
  );
set 
  reqMsgId = concat("MSG", customerId + 4);
INSERT INTO `requestmessage` (
  `id`, `CustomerRequest_id`, `MessageDescription`, 
  `RepliedBy`, `ReplySequence`, `IsRead`, 
  `createdby`, `modifiedby`, `createdts`, 
  `lastmodifiedts`, `synctimestamp`, 
  `softdeleteflag`
) 
VALUES 
  (
    reqMsgId, customerReqId, '<p><span style=\"color: #000000;\">Dear Customer,</span></p>\n<p><span style=\"color: #000000;\">&nbsp;</span></p>\n<p><span style=\"color: #000000;\">Thanks for writing to us. We can see that the payment failed because you had insufficient funds in your account on Monday, 19<sup>th</sup> Feb 2018. You seem to have the required balance at the moment. You can try making this payment now. </span></p>\n<p><span style=\"color: #000000;\">&nbsp;</span></p>\n<p><span style=\"color: #000000;\">Do let us know in case you face any other issues. </span></p>\n<p><span style=\"color: #000000;\">&nbsp;</span></p>\n<p><span style=\"color: #000000;\">Thanks,</span></p>\n<p><span style=\"color: #000000;\">Richard</span></p>\n<p><span style=\"font-size: 11.0pt; font-family: \'Calibri\',sans-serif; color: #000000;\">Kony Bank, Customer Service Team</span></p>', 
    'UID10', '2', 'TRUE', 'admin1', 'admin1', 
    '2018-02-26 19:11:15', '2018-02-26 19:11:15', 
    '2018-02-26 19:11:15', '0'
  );
set 
  reqMsgId = concat("MSG", customerId + 5);
INSERT INTO `requestmessage` (
  `id`, `CustomerRequest_id`, `MessageDescription`, 
  `RepliedBy`, `ReplySequence`, `IsRead`, 
  `createdby`, `modifiedby`, `createdts`, 
  `lastmodifiedts`, `synctimestamp`, 
  `softdeleteflag`
) 
VALUES 
  (
    reqMsgId, customerReqId, '<p><span style=\"color: #000000;\">Hi Richard,</span></p><p><span style=\"color: #000000;\">&nbsp;</span></p><p><span style=\"color: #000000;\">Thanks for letting me know. I could make this payment now. </span></p><p><span style=\"color: #000000;\">&nbsp;</span></p><p><span style=\"font-size: 11.0pt; font-family: \'Calibri\',sans-serif; color: #000000;\">Thanks</span></p>', 
    'UID10', '3', 'TRUE', 'jane.doe', 
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
    'UID11', 'undefined', '0', 'admin2', 
    '2018-04-13 11:40:06', '2018-04-13 11:40:06', 
    '2018-04-13 11:40:06', '0'
  );
set 
  reqMsgId = concat("MSG", customerId + 6);
INSERT INTO `requestmessage` (
  `id`, `CustomerRequest_id`, `MessageDescription`, 
  `RepliedBy`, `ReplySequence`, `IsRead`, 
  `createdby`, `modifiedby`, `createdts`, 
  `lastmodifiedts`, `synctimestamp`, 
  `softdeleteflag`
) 
VALUES 
  (
    reqMsgId, customerReqId, ' <p><span style=\"color: #000000;\">Dear Customer,</span></p> <p><span style=\"color: #000000;\">&nbsp;</span></p> <p><span style=\"color: #000000;\">Thank you for informing us about your travel plans. Your card is already enabled for international transactions in Australia. You can use the card at all POS transaction points hassle free. Have a great trip!</span></p> <p><span style=\"color: #000000;\">&nbsp;</span></p> <p><span style=\"font-size: 11.0pt; font-family: \'Calibri\',sans-serif; color: #000000;\">Thanks</span></p> <p><span style=\"font-size: 11.0pt; font-family: \'Calibri\',sans-serif; color: #000000;\">Celeste</span></p> <p><span style=\"font-size: 11.0pt; font-family: \'Calibri\',sans-serif; color: #000000;\">Kony Bank, Customer Service</span></p>', 
    'UID10', '2', 'TRUE', 'admin1', 'admin1', 
    '2018-02-26 19:11:15', '2018-02-26 19:11:15', 
    '2018-02-26 19:11:15', '0'
  );
set 
  reqMsgId = concat("MSG", customerId + 7);
INSERT INTO `requestmessage` (
  `id`, `CustomerRequest_id`, `MessageDescription`, 
  `RepliedBy`, `ReplySequence`, `IsRead`, 
  `createdby`, `modifiedby`, `createdts`, 
  `lastmodifiedts`, `synctimestamp`, 
  `softdeleteflag`
) 
VALUES 
  (
    reqMsgId, customerReqId, ' <p><span style=\"color: #000000;\">Hi,</span></p> <p><span style=\"color: #000000;\">&nbsp;</span></p> <p><span style=\"color: #000000;\">I will be travelling to Australia from 1<sup>st</sup>-10<sup>th</sup> May 2018 and will be using my Kony Bank Credit Card for the same. Please enable international transactions on this card for this period.</span></p> <p><span style=\"color: #000000;\">&nbsp;</span></p> <p><span style=\"font-size: 11.0pt; font-family: \'Calibri\',sans-serif; color: #000000;\">Thanks</span></p>', 
    'UID10', '2', 'TRUE', 'admin1', 'admin1', 
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


DROP PROCEDURE IF EXISTS `location_details_proc`;
DELIMITER $$
CREATE  PROCEDURE `location_details_proc`(
  in _locationId char(50)
)
BEGIN DECLARE workingHours TEXT(2000);
DECLARE tempDayName TEXT(2000);
DECLARE tempStartTime TEXT(2000);
DECLARE tempEndTime TEXT(2000);
DECLARE tempServiceName TEXT(2000);
DECLARE servicesList TEXT(10000);
DECLARE serviceName TEXT(2000);
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
  `address`.`addressLine1` AS addressLine1, 
  `address`.`addressLine2` AS addressLine2, 
  `address`.`addressLine3` AS addressLine3, 
  `address`.`latitude` AS latitude, 
  `address`.`logitude` AS longitude, 
  workingHours AS workingHours, 
  servicesList AS services 
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
END $$ 
DELIMITER ;

DROP PROCEDURE IF EXISTS `location_range_proc`;
DELIMITER $$ 
CREATE  PROCEDURE `location_range_proc`(
  in _currLatitude TEXT(50), 
  in _currLongitude TEXT(50), 
  in _radius int(50) )
BEGIN DECLARE rowLatitide TEXT(2000);
DECLARE rowLongitude TEXT(2000);
DECLARE b int(1);
DECLARE pipeFlag int(1);
SELECT 
  location.id AS locationId, 
  (
    SELECT 
      GROUP_CONCAT(
        DISTINCT CONCAT(
          UCASE(
            LEFT(dayschedule.weekdayname, 1)
          ), 
          LCASE(
            SUBSTRING(dayschedule.weekdayname, 2)
          ), 
          ':', 
          SUBSTRING(dayschedule.StartTime, 1, 5), 
          '-', 
          SUBSTRING(dayschedule.endTime, 1, 5)
        ) SEPARATOR ' || '
      ) 
    FROM 
      dayschedule, 
      location 
    WHERE 
      dayschedule.WorkSchedule_id = location.WorkSchedule_id 
      and location.id = locationId
  ) AS workingHours, 
  location.name AS informationTitle, 
  location.phoneNumber AS phone, 
  location.emailId AS email, 
  CASE `location`.`status_id` WHEN 'SID_ACTIVE' THEN 'OPEN' ELSE 'CLOSED' END AS status, 
  location.type_id AS type, 
  (
    SELECT 
      GROUP_CONCAT(
        service.name 
        ORDER BY 
          service.name ASC SEPARATOR ' || '
      ) 
    FROM 
      service, 
      locationservice 
    WHERE 
      service.id = locationservice.Service_id 
      AND locationservice.Location_id = location.id
  ) AS services, 
  (
    SELECT 
      city.name 
    FROM 
      city 
    WHERE 
      city.id = address.City_id
  ) AS city, 
  address.addressLine1 AS addressLine1, 
  address.addressLine2 AS addressLine2, 
  address.addressLine3 AS addressLine3, 
  address.zipCode AS zipCode, 
  address.latitude AS latitude, 
  address.logitude AS longitude, 
  (
    6371 * 2 * ASIN(
      SQRT(
        POWER(
          SIN(
            (
              latitude - ABS(_currLatitude)
            ) * PI() / 180 / 2
          ), 
          2
        ) + COS(
          latitude * PI() / 180
        ) * COS(
          ABS(_currLatitude) * PI() / 180
        ) * POWER(
          SIN(
            (Logitude - _currLongitude) * PI() / 180 / 2
          ), 
          2
        )
      )
    )
  ) AS distance 
FROM 
  address, 
  location 
WHERE 
  address.id = location.address_id 
HAVING 
  distance <= _radius;
END $$ 
DELIMITER ;

DROP procedure IF EXISTS `location_search_proc`;
DELIMITER $$
CREATE PROCEDURE `location_search_proc`(
  in _searchKeyword TEXT(1000)
)
BEGIN DECLARE _next TEXT DEFAULT NULL;
DECLARE _nextlen INT DEFAULT NULL;
DECLARE _value TEXT DEFAULT NULL;
DECLARE sql1 TEXT (200000);
DECLARE sqlFrontPart TEXT (200000);
DECLARE sqlresult TEXT (200000) default null;
declare counter int(50);
set 
  @sqlFrontPart = 'SELECT * FROM locationdetails_view where';
set 
  @counter = 1;
iterator : LOOP IF LENGTH(
  TRIM(_searchKeyword)
) = 0 
OR _searchKeyword IS NULL THEN LEAVE iterator;
END IF;
SET 
  _next = SUBSTRING_INDEX(_searchKeyword, ',', 1);
SET 
  _nextlen = LENGTH(_next);
SET 
  _value = LCASE(
    TRIM(_next)
  );
set 
  @sql1 = null;
set 
  @var = null;
set 
  @var = REPLACE (
    trim(_value), 
    ' ', 
    '%'
  );
set 
  @sql1 = concat(
    '( 
 LOWER(city) like (', '"', '%', 
    @var, '%', '"', ') 
  or LOWER(addressLine1) like (', 
    '"', '%', @var, '%', '"', ') 
  or LOWER(addressLine2) like (', 
    '"', '%', @var, '%', '"', ') 
  or LOWER(addressLine3) like (', 
    '"', '%', @var, '%', '"', ') 
  or LOWER(country) like (', 
    '"', '%', @var, '%', '"', ') 
  or LOWER(zipcode) like (', 
    '"', '%', @var, '%', '"', '))'
  );
if @counter = 1 then 
set 
  @sqlresult = @sql1;
else 
set 
  @sqlresult = concat(@sql1, ' AND ', @sqlresult);
end if;
set 
  @counter = @counter + 1;

SET 
  _searchKeyword = INSERT(_searchKeyword, 1, _nextlen + 1, '');
END LOOP;

set 
  @sqlresult = concat(
    '(', @sqlFrontPart, @sqlresult, ')'
  );
PREPARE stmt1 
from 
  @sqlresult;
EXECUTE stmt1;

END $$
DELIMITER ;


DROP PROCEDURE IF EXISTS `reports_messages_received`;
DELIMITER $$ 
CREATE  PROCEDURE `reports_messages_received`(
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
    and createdts >= '", 
    STR_TO_DATE(from_date, '%Y-%m-%d'), 
    "' and createdts <= '", 
    STR_TO_DATE(to_date, '%Y-%m-%d'), 
    "'"
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
        RequestCategory_id = '", 
    category_id, "'
    )
    "
  );
end if;
if csr_name != '' then 
set 
  @queryStatement = concat(
    @queryStatement, " 
    and RepliedBy = '", 
    csr_name, "'"
  );
end if;
PREPARE stmt 
FROM 
  @queryStatement;
EXECUTE stmt;
END $$ 
DELIMITER ;

DROP PROCEDURE IF EXISTS `reports_messages_sent`;
DELIMITER $$ 
CREATE  PROCEDURE `reports_messages_sent`(
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
      and createdts >= '", 
    STR_TO_DATE(from_date, '%Y-%m-%d'), 
    "' and createdts <= '", 
    STR_TO_DATE(to_date, '%Y-%m-%d'), 
    "'"
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
          RequestCategory_id = '", 
    category_id, "'
      )
      "
  );
end if;
if csr_name != '' then 
set 
  @queryStatement = concat(
    @queryStatement, " 
      and RepliedBy = '", 
    csr_name, "'"
  );
end if;
PREPARE stmt 
FROM 
  @queryStatement;
EXECUTE stmt;
END $$ 
DELIMITER ;

DROP PROCEDURE IF EXISTS `reports_threads_averageage`;
DELIMITER $$
CREATE  PROCEDURE `reports_threads_averageage`(
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
            and createdts >= '", 
    STR_TO_DATE(from_date, '%Y-%m-%d'), 
    "' and createdts <= '", 
    STR_TO_DATE(to_date, '%Y-%m-%d'), 
    "'"
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
                RequestCategory_id = '", 
    category_id, "'
            )
            "
  );
end if;
if csr_name != '' then 
set 
  @queryStatement = concat(
    @queryStatement, " 
            and RepliedBy = '", 
    csr_name, "'"
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
END $$ 
DELIMITER ;

DROP PROCEDURE IF EXISTS `reports_threads_new`;
DELIMITER $$ 
CREATE  PROCEDURE `reports_threads_new`(
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
              and createdts >= '", 
    STR_TO_DATE(from_date, '%Y-%m-%d'), 
    "' and createdts <= '", 
    STR_TO_DATE(to_date, '%Y-%m-%d'), 
    "'"
  );
end if;
if category_id != '' then 
set 
  @queryStatement = concat(
    @queryStatement, " 
              and RequestCategory_id = '", 
    category_id, "'"
  );
end if;
if csr_name != '' then 
set 
  @queryStatement = concat(
    @queryStatement, " 
              and AssignedTo = '", 
    csr_name, "'"
  );
end if;
PREPARE stmt 
FROM 
  @queryStatement;
EXECUTE stmt;
END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS `reports_threads_resolved`;
DELIMITER $$ 
CREATE  PROCEDURE `reports_threads_resolved`(
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
                and createdts >= '", 
    STR_TO_DATE(from_date, '%Y-%m-%d'), 
    "' and createdts <= '", 
    STR_TO_DATE(to_date, '%Y-%m-%d'), 
    "'"
  );
end if;
if category_id != '' then 
set 
  @queryStatement = concat(
    @queryStatement, " 
                and RequestCategory_id = '", 
    category_id, "'"
  );
end if;
if csr_name != '' then 
set 
  @queryStatement = concat(
    @queryStatement, " 
                and AssignedTo = '", 
    csr_name, "'"
  );
end if;
PREPARE stmt 
FROM 
  @queryStatement;
EXECUTE stmt;
END $$ 
DELIMITER ;




DROP VIEW IF EXISTS `applicant_view`;
CREATE VIEW `applicant_view` AS select `applicant`.`id` AS `Application_id`,`applicant`.`FirstName` AS `FirstName`,`applicant`.`MiddleName` AS `MiddleName`,`applicant`.`LastName` AS `LastName`,`applicant`.`DocumentsSubmitted` AS `DocumentsSubmitted`,`applicant`.`ContactNumber` AS `ContactNumber`,`applicant`.`EmailID` AS `EmailID`,`applicant`.`createdby` AS `applicant_createdby`,`applicant`.`modifiedby` AS `applicant_modifiedby`,`applicant`.`createdts` AS `applicant_createdts`,`applicant`.`lastmodifiedts` AS `applicant_lastmodifiedts`,`applicant`.`Channel` AS `Channel`,`applicant`.`Status` AS `Status`,`applicant`.`Product` AS `Product` from `applicant`;

DROP VIEW IF EXISTS `applicantnotes_view`;
CREATE VIEW `applicantnotes_view` AS select `applicantnote`.`id` AS `id`,`applicantnote`.`Note` AS `Note`,`applicantnote`.`Applicant_id` AS `applicant_id`,`applicant`.`FirstName` AS `applicant_FirstName`,`applicant`.`MiddleName` AS `applicant_MiddleName`,`applicant`.`LastName` AS `applicant_LastName`,`applicant`.`Status` AS `applicant_Status`,`applicantnote`.`createdby` AS `InternalUser_id`,`systemuser`.`Username` AS `InternalUser_Username`,`systemuser`.`FirstName` AS `InternalUser_FirstName`,`systemuser`.`LastName` AS `InternalUser_LastName`,`systemuser`.`MiddleName` AS `InternalUser_MiddleName`,`systemuser`.`Email` AS `InternalUser_Email`,`applicantnote`.`createdts` AS `createdts`,`applicantnote`.`synctimestamp` AS `synctimestamp`,`applicantnote`.`softdeleteflag` AS `softdeleteflag` from ((`applicantnote` left join `systemuser` on((`applicantnote`.`createdby` = `systemuser`.`id`))) left join `applicant` on((`applicantnote`.`Applicant_id` = `applicant`.`id`)));

DROP VIEW IF EXISTS `archivedcustomer_request_detailed_view`;
CREATE VIEW `archivedcustomer_request_detailed_view` AS select `archivedcustomerrequest`.`id` AS `customerrequest_id`,`archivedcustomerrequest`.`RequestCategory_id` AS `customerrequest_RequestCategory_id`,`archivedcustomerrequest`.`lastupdatedbycustomer` AS `customerrequest_lastupdatedbycustomer`,`requestcategory`.`Name` AS `requestcategory_Name`,`archivedcustomerrequest`.`Customer_id` AS `customerrequest_Customer_id`,`customer`.`FirstName` AS `customer_FirstName`,`customer`.`MiddleName` AS `customer_MiddleName`,concat(`customer`.`FirstName`,`customer`.`LastName`) AS `customer_Fullname`,`customer`.`LastName` AS `customer_LastName`,`customer`.`Username` AS `customer_Username`,`customer`.`Salutation` AS `customer_Salutation`,`customer`.`Gender` AS `customer_Gender`,`customer`.`DateOfBirth` AS `customer_DateOfBirth`,`customer`.`Status_id` AS `customer_Status_id`,`customer`.`Ssn` AS `customer_Ssn`,`customer`.`MaritalStatus_id` AS `customer_MaritalStatus_id`,`customer`.`SpouseName` AS `customer_SpouseName`,`customer`.`EmployementStatus_id` AS `customer_EmployementStatus_id`,`customer`.`IsEnrolledForOlb` AS `customer_IsEnrolledForOlb`,`customer`.`IsStaffMember` AS `customer_IsStaffMember`,`customer`.`Location_id` AS `customer_Location_id`,`customer`.`PreferredContactMethod` AS `customer_PreferredContactMethod`,`customer`.`PreferredContactTime` AS `customer_PreferredContactTime`,`archivedcustomerrequest`.`Priority` AS `customerrequest_Priority`,`archivedcustomerrequest`.`Status_id` AS `customerrequest_Status_id`,`archivedcustomerrequest`.`AssignedTo` AS `customerrequest_AssignedTo`,`archivedcustomerrequest`.`RequestSubject` AS `customerrequest_RequestSubject`,`archivedcustomerrequest`.`Accountid` AS `customerrequest_Accountid`,`archivedcustomerrequest`.`createdby` AS `customerrequest_createdby`,`archivedcustomerrequest`.`modifiedby` AS `customerrequest_modifiedby`,`archivedcustomerrequest`.`createdts` AS `customerrequest_createdts`,`archivedcustomerrequest`.`lastmodifiedts` AS `customerrequest_lastmodifiedts`,`archivedcustomerrequest`.`synctimestamp` AS `customerrequest_synctimestamp`,`archivedcustomerrequest`.`softdeleteflag` AS `customerrequest_softdeleteflag`,`archivedrequestmessage`.`id` AS `requestmessage_id`,`archivedrequestmessage`.`RepliedBy` AS `requestmessage_RepliedBy`,`archivedrequestmessage`.`RepliedBy_Name` AS `requestmessage_RepliedBy_Name`,`archivedrequestmessage`.`MessageDescription` AS `requestmessage_MessageDescription`,`archivedrequestmessage`.`ReplySequence` AS `requestmessage_ReplySequence`,`archivedrequestmessage`.`IsRead` AS `requestmessage_IsRead`,`archivedrequestmessage`.`createdby` AS `requestmessage_createdby`,`archivedrequestmessage`.`modifiedby` AS `requestmessage_modifiedby`,`archivedrequestmessage`.`createdts` AS `requestmessage_createdts`,`archivedrequestmessage`.`lastmodifiedts` AS `requestmessage_lastmodifiedts`,`archivedrequestmessage`.`synctimestamp` AS `requestmessage_synctimestamp`,`archivedrequestmessage`.`softdeleteflag` AS `requestmessage_softdeleteflag`,`archivedmessageattachment`.`id` AS `messageattachment_id`,`archivedmessageattachment`.`AttachmentType_id` AS `messageattachment_AttachmentType_id`,`archivedmessageattachment`.`Media_id` AS `messageattachment_Media_id`,`archivedmessageattachment`.`createdby` AS `messageattachment_createdby`,`archivedmessageattachment`.`modifiedby` AS `messageattachment_modifiedby`,`archivedmessageattachment`.`createdts` AS `messageattachment_createdts`,`archivedmessageattachment`.`lastmodifiedts` AS `messageattachment_lastmodifiedts`,`archivedmessageattachment`.`softdeleteflag` AS `messageattachment_softdeleteflag`,`archivedmedia`.`id` AS `media_id`,`archivedmedia`.`Name` AS `media_Name`,`archivedmedia`.`Size` AS `media_Size`,`archivedmedia`.`Type` AS `media_Type`,`archivedmedia`.`Description` AS `media_Description`,`archivedmedia`.`Url` AS `media_Url`,`archivedmedia`.`createdby` AS `media_createdby`,`archivedmedia`.`modifiedby` AS `media_modifiedby`,`archivedmedia`.`lastmodifiedts` AS `media_lastmodifiedts`,`archivedmedia`.`synctimestamp` AS `media_synctimestamp`,`archivedmedia`.`softdeleteflag` AS `media_softdeleteflag` from (((((`archivedcustomerrequest` left join `archivedrequestmessage` on((`archivedcustomerrequest`.`id` = `archivedrequestmessage`.`CustomerRequest_id`))) left join `archivedmessageattachment` on((`archivedrequestmessage`.`id` = `archivedmessageattachment`.`RequestMessage_id`))) left join `archivedmedia` on((`archivedmessageattachment`.`Media_id` = `archivedmedia`.`id`))) left join `customer` on((`archivedcustomerrequest`.`Customer_id` = `customer`.`id`))) left join `requestcategory` on((`archivedcustomerrequest`.`RequestCategory_id` = `requestcategory`.`id`)));

DROP VIEW IF EXISTS `bankfortransfer_view`;
CREATE VIEW `bankfortransfer_view` AS select `bankfortransfer`.`id` AS `id`,`bankfortransfer`.`Code` AS `Bank_Code`,`bankfortransfer`.`Name` AS `Bank_Name`,`bankfortransfer`.`Logo` AS `Logo`,`bankfortransfer`.`Status_id` AS `Status_id`,`bankfortransfer`.`Url` AS `Bank_Url`,`bankfortransfer`.`Address_id` AS `Address_id`,`address`.`addressLine1` AS `AddressLine1`,`address`.`addressLine2` AS `AddressLine2`,`address`.`City_id` AS `City_id`,`city`.`Name` AS `City_Name`,`region`.`id` AS `Region_id`,`region`.`Name` AS `Region_Name`,`region`.`Country_id` AS `Country_id`,`country`.`Name` AS `Country_Name`,`bankservice`.`RoutingCode` AS `Routing_Code`,`bankservice`.`RoutingNumber` AS `Routing_Number`,`bankservice`.`Service_id` AS `Service_id`,`service`.`Name` AS `Service_Name` from ((((((`bankfortransfer` left join `address` on((`bankfortransfer`.`Address_id` = `address`.`id`))) left join `bankservice` on((`bankfortransfer`.`id` = `bankservice`.`BankForTransfer_id`))) left join `city` on((`address`.`City_id` = `city`.`id`))) left join `region` on((`address`.`Region_id` = `region`.`id`))) left join `country` on((`region`.`Country_id` = `country`.`id`))) left join `service` on((`bankservice`.`Service_id` = `service`.`id`)));

DROP VIEW IF EXISTS `branch_view`;
CREATE VIEW `branch_view` AS select `loc`.`id` AS `Branch_id`,ifnull(`loc`.`Type_id`,'') AS `Branch_Typeid`,ifnull(`loc`.`Name`,'') AS `Branch_Name`,ifnull(`loc`.`DisplayName`,'') AS `Branch_DisplayName`,ifnull(`loc`.`Description`,'') AS `Branch_Description`,ifnull(`loc`.`Code`,'') AS `Branch_Code`,ifnull(`loc`.`PhoneNumber`,'') AS `Branch_PhoneNumber`,ifnull(`loc`.`EmailId`,'') AS `Branch_EmailId`,ifnull(`loc`.`Status_id`,'') AS `Branch_Status_id`,ifnull(`loc`.`IsMainBranch`,'') AS `Branch_IsMainBranch`,ifnull(`loc`.`WorkSchedule_id`,'') AS `Branch_WorkSchedule_id`,ifnull(`loc`.`MainBranchCode`,'') AS `Branch_MainBranchCode`,ifnull(`loc`.`WebSiteUrl`,'') AS `Branch_WebSiteUrl`,`address`.`City_id` AS `City_id`,`city`.`Name` AS `City_Name`,`loc`.`Address_id` AS `Address_id`,concat(`address`.`addressLine1`,', ',ifnull(`address`.`addressLine2`,''),', ',`city`.`Name`,', ',`region`.`Name`,', ',`country`.`Name`,', ',ifnull(`address`.`zipCode`,'')) AS `Branch_Complete_Addr`,ifnull(`loc`.`createdby`,'') AS `Branch_createdby`,ifnull(`loc`.`modifiedby`,'') AS `Branch_modifiedby`,ifnull(`loc`.`createdts`,'') AS `Branch_createdts`,ifnull(`loc`.`lastmodifiedts`,'') AS `Branch_lastmodifiedts`,ifnull(`loc`.`synctimestamp`,'') AS `Branch_synctimestamp` from ((((`location` `loc` left join `address` on(((`loc`.`Address_id` = `address`.`id`) and (`loc`.`Type_id` = 'Branch')))) left join `city` on((`city`.`id` = `address`.`City_id`))) left join `region` on((`region`.`id` = `address`.`Region_id`))) left join `country` on((`city`.`Country_id` = `country`.`id`)));

DROP VIEW IF EXISTS `customer_communication_view`;
CREATE VIEW `customer_communication_view` AS select `customer`.`id` AS `customer_id`,`customer`.`FirstName` AS `customer_FirstName`,`customer`.`MiddleName` AS `customer_MiddleName`,`customer`.`LastName` AS `customer_LastName`,`customer`.`Username` AS `customer_Username`,`customer`.`Salutation` AS `customer_Salutation`,`customer`.`Gender` AS `customer_Gender`,`customer`.`DateOfBirth` AS `customer_DateOfBirth`,`customer`.`Status_id` AS `customer_Status_id`,`customer`.`Ssn` AS `customer_Ssn`,`customer`.`MaritalStatus_id` AS `customer_MaritalStatus_id`,`customer`.`SpouseName` AS `customer_SpouseName`,`customer`.`EmployementStatus_id` AS `customer_EmployementStatus_id`,`customer`.`IsEnrolledForOlb` AS `customer_IsEnrolledForOlb`,`customer`.`IsStaffMember` AS `customer_IsStaffMember`,`customer`.`Location_id` AS `customer_Location_id`,`customer`.`PreferredContactMethod` AS `customer_PreferredContactMethod`,`customer`.`PreferredContactTime` AS `customer_PreferredContactTime`,`customercommunication`.`id` AS `customercommunication_id`,`customercommunication`.`Type_id` AS `customercommunication_Type_id`,`customercommunication`.`isPrimary` AS `customercommunication`,`customercommunication`.`Value` AS `customercommunication_Value`,`customercommunication`.`Extension` AS `customercommunication_Extension`,`customercommunication`.`Description` AS `customercommunication_Description`,`customercommunication`.`createdby` AS `customercommunication_createdby`,`customercommunication`.`modifiedby` AS `customercommunication_modifiedby`,`customercommunication`.`createdts` AS `customercommunication_createdts`,`customercommunication`.`lastmodifiedts` AS `customercommunication_lastmodifiedts`,`customercommunication`.`synctimestamp` AS `customercommunication_synctimestamp`,`customercommunication`.`softdeleteflag` AS `customercommunication_softdeleteflag` from (`customer` left join `customercommunication` on((`customer`.`id` = `customercommunication`.`Customer_id`)));

DROP VIEW IF EXISTS `customer_indirect_permissions_view`;
CREATE VIEW `customer_indirect_permissions_view` AS select `customergroup`.`Customer_id` AS `Customer_id`,`membergroup`.`id` AS `Group_id`,`membergroup`.`Name` AS `Group_name`,`membergroup`.`Description` AS `Group_desc`,`service`.`id` AS `Service_id`,`service`.`Name` AS `Service_Name`,`service`.`Description` AS `Service_Description`,`service`.`Status_id` AS `Service_Status_id`,`service`.`DisplayName` AS `Service_DisplayName`,`service`.`DisplayDescription` AS `Service_DisplayDescription` from (((`customergroup` left join `groupentitlement` on((`customergroup`.`Group_id` = `groupentitlement`.`Group_id`))) left join `membergroup` on((`customergroup`.`Group_id` = `membergroup`.`id`))) left join `service` on((`groupentitlement`.`Service_id` = `service`.`id`)));

DROP VIEW IF EXISTS `customer_request_category_count_view`;
CREATE VIEW `customer_request_category_count_view` AS select `requestcategory`.`id` AS `requestcategory_id`,`requestcategory`.`Name` AS `requestcategory_Name`,count(`customerrequest`.`id`) AS `request_count` from (`requestcategory` left join `customerrequest` on(((`customerrequest`.`RequestCategory_id` = `requestcategory`.`id`) and (`customerrequest`.`Status_id` = 'SID_OPEN')))) group by `requestcategory`.`id`;

DROP VIEW IF EXISTS `customer_request_csr_count_view`;
CREATE VIEW `customer_request_csr_count_view` AS select `customerrequest`.`Status_id` AS `customerrequest_Status_id`,`status`.`Description` AS `status_Description`,`customerrequest`.`AssignedTo` AS `customerrequest_assignedTo`,count(`customerrequest`.`id`) AS `request_count` from (`customerrequest` left join `status` on((`customerrequest`.`Status_id` = `status`.`id`))) group by `customerrequest`.`Status_id`,`customerrequest`.`AssignedTo`;

DROP VIEW IF EXISTS `customer_request_detailed_view`;
CREATE VIEW `customer_request_detailed_view` AS select `customerrequest`.`id` AS `customerrequest_id`,`customerrequest`.`RequestCategory_id` AS `customerrequest_RequestCategory_id`,`customerrequest`.`lastupdatedbycustomer` AS `customerrequest_lastupdatedbycustomer`,`requestcategory`.`Name` AS `requestcategory_Name`,`customerrequest`.`Customer_id` AS `customerrequest_Customer_id`,`customer`.`FirstName` AS `customer_FirstName`,`customer`.`MiddleName` AS `customer_MiddleName`,concat(`customer`.`FirstName`,`customer`.`LastName`) AS `customer_Fullname`,concat(`systemuser`.`FirstName`,`systemuser`.`LastName`) AS `customerrequest_AssignedTo_Name`,`customer`.`LastName` AS `customer_LastName`,`customer`.`Username` AS `customer_Username`,`customer`.`Salutation` AS `customer_Salutation`,`customer`.`Gender` AS `customer_Gender`,`customer`.`DateOfBirth` AS `customer_DateOfBirth`,`customer`.`Status_id` AS `customer_Status_id`,`customer`.`Ssn` AS `customer_Ssn`,`customer`.`MaritalStatus_id` AS `customer_MaritalStatus_id`,`customer`.`SpouseName` AS `customer_SpouseName`,`customer`.`EmployementStatus_id` AS `customer_EmployementStatus_id`,`customer`.`IsEnrolledForOlb` AS `customer_IsEnrolledForOlb`,`customer`.`IsStaffMember` AS `customer_IsStaffMember`,`customer`.`Location_id` AS `customer_Location_id`,`customer`.`PreferredContactMethod` AS `customer_PreferredContactMethod`,`customer`.`PreferredContactTime` AS `customer_PreferredContactTime`,`customerrequest`.`Priority` AS `customerrequest_Priority`,`customerrequest`.`Status_id` AS `customerrequest_Status_id`,`customerrequest`.`AssignedTo` AS `customerrequest_AssignedTo`,`customerrequest`.`RequestSubject` AS `customerrequest_RequestSubject`,`customerrequest`.`Accountid` AS `customerrequest_Accountid`,`customerrequest`.`createdby` AS `customerrequest_createdby`,`customerrequest`.`modifiedby` AS `customerrequest_modifiedby`,`customerrequest`.`createdts` AS `customerrequest_createdts`,`customerrequest`.`lastmodifiedts` AS `customerrequest_lastmodifiedts`,`customerrequest`.`synctimestamp` AS `customerrequest_synctimestamp`,`customerrequest`.`softdeleteflag` AS `customerrequest_softdeleteflag`,`requestmessage`.`id` AS `requestmessage_id`,`requestmessage`.`RepliedBy` AS `requestmessage_RepliedBy`,`requestmessage`.`RepliedBy_Name` AS `requestmessage_RepliedBy_Name`,`requestmessage`.`MessageDescription` AS `requestmessage_MessageDescription`,`requestmessage`.`ReplySequence` AS `requestmessage_ReplySequence`,`requestmessage`.`IsRead` AS `requestmessage_IsRead`,`requestmessage`.`createdby` AS `requestmessage_createdby`,`requestmessage`.`modifiedby` AS `requestmessage_modifiedby`,`requestmessage`.`createdts` AS `requestmessage_createdts`,`requestmessage`.`lastmodifiedts` AS `requestmessage_lastmodifiedts`,`requestmessage`.`synctimestamp` AS `requestmessage_synctimestamp`,`requestmessage`.`softdeleteflag` AS `requestmessage_softdeleteflag`,`messageattachment`.`id` AS `messageattachment_id`,`messageattachment`.`AttachmentType_id` AS `messageattachment_AttachmentType_id`,`messageattachment`.`Media_id` AS `messageattachment_Media_id`,`messageattachment`.`createdby` AS `messageattachment_createdby`,`messageattachment`.`modifiedby` AS `messageattachment_modifiedby`,`messageattachment`.`createdts` AS `messageattachment_createdts`,`messageattachment`.`lastmodifiedts` AS `messageattachment_lastmodifiedts`,`messageattachment`.`softdeleteflag` AS `messageattachment_softdeleteflag`,`media`.`id` AS `media_id`,`media`.`Name` AS `media_Name`,`media`.`Size` AS `media_Size`,`media`.`Type` AS `media_Type`,`media`.`Description` AS `media_Description`,`media`.`Url` AS `media_Url`,`media`.`createdby` AS `media_createdby`,`media`.`modifiedby` AS `media_modifiedby`,`media`.`lastmodifiedts` AS `media_lastmodifiedts`,`media`.`synctimestamp` AS `media_synctimestamp`,`media`.`softdeleteflag` AS `media_softdeleteflag` from ((((((`customerrequest` left join `requestmessage` on((`customerrequest`.`id` = `requestmessage`.`CustomerRequest_id`))) left join `messageattachment` on((`requestmessage`.`id` = `messageattachment`.`RequestMessage_id`))) left join `media` on((`messageattachment`.`Media_id` = `media`.`id`))) left join `customer` on((`customerrequest`.`Customer_id` = `customer`.`id`))) left join `requestcategory` on((`customerrequest`.`RequestCategory_id` = `requestcategory`.`id`))) left join `systemuser` on((`customerrequest`.`AssignedTo` = `systemuser`.`id`)));

DROP VIEW IF EXISTS `customer_request_status_count_view`;
CREATE VIEW `customer_request_status_count_view` AS select count(`cr`.`id`) AS `Count`,`s1`.`id` AS `Status_id` from (`status` `s1` left join `customerrequest` `cr` on((`s1`.`id` = `cr`.`Status_id`))) where ((`s1`.`Type_id` = 'STID_CUSTOMERREQUEST') and (`s1`.`id` <> 'SID_ARCHIVED')) group by `s1`.`id` union select count(`acr`.`id`) AS `Count`,`s2`.`id` AS `Status_id` from (`status` `s2` left join `archivedcustomerrequest` `acr` on((`s2`.`id` = `acr`.`Status_id`))) where ((`s2`.`Type_id` = 'STID_CUSTOMERREQUEST') and (`s2`.`id` = 'SID_ARCHIVED')) group by `s2`.`id`;

DROP VIEW IF EXISTS `customer_unread_message_count_view`;
CREATE VIEW `customer_unread_message_count_view` AS select `customerrequest`.`Customer_id` AS `customerID`,count(`requestmessage`.`id`) AS `messageCount` from (`customerrequest` join `requestmessage` on((`requestmessage`.`CustomerRequest_id` = `customerrequest`.`id`))) where (`requestmessage`.`IsRead` = 'FALSE') group by `customerrequest`.`Customer_id`;

DROP VIEW IF EXISTS `customeraddress_view`;
CREATE VIEW `customeraddress_view` AS select `c`.`id` AS `CustomerId`,`ca`.`Address_id` AS `Address_id`,`ca`.`isPrimary` AS `isPrimary`,`ca`.`Type_id` AS `AddressType`,`a`.`id` AS `AddressId`,`a`.`addressLine1` AS `AddressLine1`,`a`.`addressLine2` AS `AddressLine2`,`a`.`zipCode` AS `ZipCode`,`a`.`Region_id` AS `Region_id`,`a`.`City_id` AS `City_id`,`cit`.`Country_id` AS `Country_id`,`cit`.`Name` AS `CityName`,`reg`.`Name` AS `RegionName`,`reg`.`Code` AS `RegionCode`,`coun`.`Name` AS `CountryName`,`coun`.`Code` AS `CountryCode` from (((((`customeraddress` `ca` left join `customer` `c` on((`ca`.`Customer_id` = `c`.`id`))) left join `address` `a` on((`a`.`id` = `ca`.`Address_id`))) left join `region` `reg` on((`reg`.`id` = `a`.`Region_id`))) left join `city` `cit` on((`cit`.`id` = `a`.`City_id`))) left join `country` `coun` on((`coun`.`id` = `cit`.`Country_id`)));

DROP VIEW IF EXISTS `customerbasicinfo_view`;
CREATE VIEW `customerbasicinfo_view` AS select `customer`.`Username` AS `Username`,`customer`.`FirstName` AS `FirstName`,`customer`.`MiddleName` AS `MiddleName`,`customer`.`LastName` AS `LastName`,concat(`customer`.`FirstName`,' ',`customer`.`MiddleName`,' ',`customer`.`LastName`) AS `Name`,`customer`.`Salutation` AS `Salutation`,`customer`.`id` AS `Customer_id`,`customer`.`Ssn` AS `SSN`,`customer`.`createdts` AS `CustomerSince`,`customer`.`Gender` AS `Gender`,`customer`.`DateOfBirth` AS `DateOfBirth`,`customer`.`Status_id` AS `CustomerStatus_id`,(select `status`.`Description` from `status` where (`customer`.`Status_id` = `status`.`id`)) AS `CustomerStatus_name`,`customer`.`MaritalStatus_id` AS `MaritalStatus_id`,(select `status`.`Description` from `status` where (`customer`.`MaritalStatus_id` = `status`.`id`)) AS `MaritalStatus_name`,`customer`.`SpouseName` AS `SpouseName`,`customer`.`EmployementStatus_id` AS `EmployementStatus_id`,(select `status`.`Description` from `status` where (`customer`.`EmployementStatus_id` = `status`.`id`)) AS `EmployementStatus_name`,(select group_concat(`customerflagstatus`.`Status_id`,' ' separator ',') from `customerflagstatus` where (`customerflagstatus`.`Customer_id` = `customer`.`id`)) AS `CustomerFlag_ids`,(select group_concat(`status`.`Description`,' ' separator ',') from `status` where `status`.`id` in (select `customerflagstatus`.`Status_id` from `customerflagstatus` where (`customerflagstatus`.`Customer_id` = `customer`.`id`))) AS `CustomerFlag`,`customer`.`IsEnrolledForOlb` AS `IsEnrolledForOlb`,`customer`.`IsStaffMember` AS `IsStaffMember`,`customer`.`Location_id` AS `Branch_id`,`location`.`Name` AS `Branch_name`,`location`.`Code` AS `Branch_code`,`customer`.`IsOlbAllowed` AS `IsOlbAllowed`,`customer`.`IsAssistConsented` AS `IsAssistConsented` from (`customer` left join `location` on((`customer`.`Location_id` = `location`.`id`)));

DROP VIEW IF EXISTS `customergroupinfo_view`;
CREATE VIEW `customergroupinfo_view` AS select `customergroup`.`Customer_id` AS `Customer_id`,`customergroup`.`Group_id` AS `Group_id`,`membergroup`.`Name` AS `Group_name`,`membergroup`.`Description` AS `Group_Desc`,`membergroup`.`Status_id` AS `GroupStatus_id`,(select `status`.`Description` from `status` where (`membergroup`.`Status_id` = `status`.`id`)) AS `GroupStatus_name`,`membergroup`.`createdby` AS `Group_createdby`,`membergroup`.`modifiedby` AS `Group_modifiedby`,`membergroup`.`createdts` AS `Group_createdts`,`membergroup`.`lastmodifiedts` AS `Group_lastmodifiedts`,`membergroup`.`synctimestamp` AS `Group_synctimestamp` from (`customergroup` left join `membergroup` on((`customergroup`.`Group_id` = `membergroup`.`id`)));

DROP VIEW IF EXISTS `customernotes_view`;
CREATE VIEW `customernotes_view` AS select `customernote`.`id` AS `id`,`customernote`.`Note` AS `Note`,`customernote`.`Customer_id` AS `Customer_id`,`customer`.`FirstName` AS `Customer_FirstName`,`customer`.`MiddleName` AS `Customer_MiddleName`,`customer`.`LastName` AS `Customer_LastName`,`customer`.`Username` AS `Customer_Username`,`customer`.`Status_id` AS `Customer_Status_id`,`customernote`.`createdby` AS `InternalUser_id`,`systemuser`.`Username` AS `InternalUser_Username`,`systemuser`.`FirstName` AS `InternalUser_FirstName`,`systemuser`.`LastName` AS `InternalUser_LastName`,`systemuser`.`MiddleName` AS `InternalUser_MiddleName`,`systemuser`.`Email` AS `InternalUser_Email`,`customernote`.`createdts` AS `createdts`,`customernote`.`synctimestamp` AS `synctimestamp`,`customernote`.`softdeleteflag` AS `softdeleteflag` from ((`customernote` left join `systemuser` on((`customernote`.`createdby` = `systemuser`.`id`))) left join `customer` on((`customernote`.`Customer_id` = `customer`.`id`)));

DROP VIEW IF EXISTS `customernotifications_view`;
CREATE VIEW `customernotifications_view` AS (select `cn`.`Customer_id` AS `customer_Id`,`cn`.`IsRead` AS `isread`,`n`.`Name` AS `Name`,`n`.`Description` AS `Description`,`n`.`StartDate` AS `StartDate`,`n`.`ExpirationDate` AS `ExpirationDate`,`n`.`Status_id` AS `Status_id`,`n`.`createdby` AS `createdby`,`n`.`modifiedby` AS `modifiedby`,`n`.`createdts` AS `createdts`,`n`.`lastmodifiedts` AS `lastmodifiedts`,`n`.`synctimestamp` AS `synctimestamp`,`n`.`softdeleteflag` AS `softdeleteflag` from (`adminnotification` `n` join `customernotification` `cn` on((`n`.`Id` = `cn`.`Notification_id`))));

DROP VIEW IF EXISTS `customerpermissions_view`;
CREATE VIEW `customerpermissions_view` AS select `customerentitlement`.`Customer_id` AS `Customer_id`,`customerentitlement`.`Service_id` AS `Service_id`,`service`.`Name` AS `Service_name`,`service`.`Description` AS `Service_description`,`service`.`Notes` AS `Service_notes`,`service`.`Status_id` AS `Status_id`,`status`.`Description` AS `Status_description`,`customerentitlement`.`TransactionFee_id` AS `TransactionFee_id`,`transactionfee`.`Description` AS `TransactionFee_description`,`customerentitlement`.`TransactionLimit_id` AS `TransactionLimit_id`,`transactionlimit`.`Description` AS `TransactionLimit_description`,`service`.`Type_id` AS `ServiceType_id`,`servicetype`.`Description` AS `ServiceType_description`,`service`.`Channel_id` AS `Channel_id`,`servicechannel`.`Description` AS `ChannelType_description`,`service`.`MinTransferLimit` AS `MinTransferLimit`,`service`.`MaxTransferLimit` AS `MaxTransferLimit`,`service`.`DisplayName` AS `Display_Name`,`service`.`DisplayDescription` AS `Display_Description` from ((((((`customerentitlement` left join `service` on((`customerentitlement`.`Service_id` = `service`.`id`))) left join `status` on((`service`.`Status_id` = `status`.`id`))) left join `transactionfee` on((`transactionfee`.`id` = `customerentitlement`.`TransactionFee_id`))) left join `transactionlimit` on((`transactionlimit`.`id` = `customerentitlement`.`TransactionLimit_id`))) left join `servicetype` on((`servicetype`.`id` = `service`.`Type_id`))) left join `servicechannel` on((`servicechannel`.`id` = `service`.`Channel_id`)));

DROP VIEW IF EXISTS `customerrequests_view`;
CREATE VIEW `customerrequests_view` AS select `customerrequest`.`id` AS `id`,`customerrequest`.`softdeleteflag` AS `softdeleteflag`,`customerrequest`.`Priority` AS `priority`,`customerrequest`.`createdts` AS `requestCreatedDate`,max(`requestmessage`.`createdts`) AS `recentMsgDate`,`requestcategory`.`Name` AS `requestcategory_id`,`customerrequest`.`Customer_id` AS `customer_id`,`customer`.`Username` AS `username`,`status`.`Description` AS `status_id`,`status`.`id` AS `statusIdentifier`,`customerrequest`.`RequestSubject` AS `requestsubject`,`customerrequest`.`Accountid` AS `accountid`,concat(`systemuser`.`FirstName`,' ',`systemuser`.`LastName`) AS `assignTo`,count(`requestmessage`.`id`) AS `totalmsgs`,count((case when (`requestmessage`.`IsRead` = 'true') then 1 end)) AS `readmsgs`,count((case when (`requestmessage`.`IsRead` = 'false') then 1 end)) AS `unreadmsgs`,substring_index(group_concat(`requestmessage`.`MessageDescription` order by `requestmessage`.`createdts` ASC,`requestmessage`.`id` ASC separator '||'),'||',1) AS `firstMessage`,group_concat(`requestmessage`.`id` separator ',') AS `msgids`,count(`messageattachment`.`id`) AS `totalAttachments` from ((((((`customerrequest` left join `requestmessage` on((`customerrequest`.`id` = `requestmessage`.`CustomerRequest_id`))) left join `requestcategory` on((`requestcategory`.`id` = `customerrequest`.`RequestCategory_id`))) left join `customer` on((`customer`.`id` = `customerrequest`.`Customer_id`))) left join `status` on((`status`.`id` = `customerrequest`.`Status_id`))) left join `messageattachment` on((`messageattachment`.`RequestMessage_id` = `requestmessage`.`id`))) left join `systemuser` on((`systemuser`.`id` = `customerrequest`.`AssignedTo`))) group by `customerrequest`.`id` order by `customerrequest`.`createdts` desc,`customerrequest`.`id`;

DROP VIEW IF EXISTS `customersecurityquestion_view`;
CREATE VIEW `customersecurityquestion_view` AS select `customersecurityquestions`.`Customer_id` AS `Customer_id`,`customersecurityquestions`.`SecurityQuestion_id` AS `SecurityQuestion_id`,`customersecurityquestions`.`CustomerAnswer` AS `CustomerAnswer`,`customersecurityquestions`.`createdby` AS `createdby`,`customersecurityquestions`.`modifiedby` AS `modifiedby`,`customersecurityquestions`.`createdts` AS `createdts`,`customersecurityquestions`.`lastmodifiedts` AS `lastmodifiedts`,`securityquestion`.`Question` AS `Question`,`securityquestion`.`Status_id` AS `QuestionStatus_id`,`customer`.`Status_id` AS `CustomerStatus_id` from ((`customersecurityquestions` join `securityquestion` on((`securityquestion`.`id` = `customersecurityquestions`.`SecurityQuestion_id`))) join `customer` on((`customer`.`id` = `customersecurityquestions`.`Customer_id`)));

DROP VIEW IF EXISTS `customerservice_communication_view`;
CREATE VIEW `customerservice_communication_view` AS select `customerservice`.`id` AS `Service_id`,`customerservice`.`Name` AS `Service_Name`,`customerservice`.`Status_id` AS `Service_Status_id`,`customerservice`.`Description` AS `Service_Description`,`customerservice`.`softdeleteflag` AS `Service_SoftDeleteFlag`,`servicecommunication`.`id` AS `ServiceCommunication_id`,`servicecommunication`.`Type_id` AS `ServiceCommunication_Typeid`,`servicecommunication`.`Value` AS `ServiceCommunication_Value`,`servicecommunication`.`Extension` AS `ServiceCommunication_Extension`,`servicecommunication`.`Description` AS `ServiceCommunication_Description`,`servicecommunication`.`Status_id` AS `ServiceCommunication_Status_id`,`servicecommunication`.`Priority` AS `ServiceCommunication_Priority`,`servicecommunication`.`createdby` AS `ServiceCommunication_createdby`,`servicecommunication`.`modifiedby` AS `ServiceCommunication_modifiedby`,`servicecommunication`.`createdts` AS `ServiceCommunication_createdts`,`servicecommunication`.`lastmodifiedts` AS `ServiceCommunication_lastmodifiedts`,`servicecommunication`.`synctimestamp` AS `ServiceCommunication_synctimestamp`,`servicecommunication`.`softdeleteflag` AS `ServiceCommunication_SoftDeleteFlag` from (`servicecommunication` left join `customerservice` on((`servicecommunication`.`Service_id` = `customerservice`.`id`)));

DROP VIEW IF EXISTS `faqcategory_view`;
CREATE VIEW `faqcategory_view` AS select `faqs`.`id` AS `id`,`faqs`.`Status_id` AS `Status_id`,`faqs`.`QuestionCode` AS `QuestionCode`,`faqs`.`Question` AS `Question`,`faqs`.`Channel_id` AS `Channel_id`,`faqs`.`Answer` AS `Answer`,`faqs`.`FaqCategory_Id` AS `CategoryId`,`faqcategory`.`Name` AS `CategoryName` from (`faqs` join `faqcategory` on((`faqcategory`.`id` = `faqs`.`FaqCategory_Id`)));

DROP VIEW IF EXISTS `groups_view`;
CREATE VIEW `groups_view` AS select `membergroup`.`id` AS `Group_id`,`membergroup`.`Description` AS `Group_Desc`,`membergroup`.`Status_id` AS `Status_id`,`membergroup`.`Name` AS `Group_Name`,(select count(`groupentitlement`.`Group_id`) from `groupentitlement` where (`groupentitlement`.`Group_id` = `membergroup`.`id`)) AS `Entitlements_Count`,(select count(`customergroup`.`Customer_id`) from `customergroup` where (`customergroup`.`Group_id` = `membergroup`.`id`)) AS `Customers_Count`,(case `membergroup`.`Status_id` when 'SID_ACTIVE' then 'Active' else 'Inactive' end) AS `Status` from `membergroup`;

DROP VIEW IF EXISTS `groupservices_view`;
CREATE VIEW `groupservices_view` AS select `groupentitlement`.`Group_id` AS `Group_id`,`groupentitlement`.`Service_id` AS `Service_id`,`service`.`Name` AS `Service_name`,`service`.`Description` AS `Service_description`,`service`.`Notes` AS `Service_notes`,`service`.`Status_id` AS `Status_id`,(select `status`.`Description` from `status` where (`status`.`id` = `service`.`Status_id`)) AS `Status_description`,`groupentitlement`.`TransactionFee_id` AS `TransactionFee_id`,(select `transactionfee`.`Description` from `transactionfee` where (`transactionfee`.`id` = `groupentitlement`.`TransactionFee_id`)) AS `TransactionFee_description`,`groupentitlement`.`TransactionLimit_id` AS `TransactionLimit_id`,(select `transactionlimit`.`Description` from `transactionlimit` where (`transactionlimit`.`id` = `groupentitlement`.`TransactionLimit_id`)) AS `TransactionLimit_description`,`service`.`Type_id` AS `ServiceType_id`,(select `servicetype`.`Description` from `servicetype` where (`servicetype`.`id` = `service`.`Type_id`)) AS `ServiceType_description`,`service`.`Channel_id` AS `Channel_id`,(select `servicechannel`.`Description` from `servicechannel` where (`servicechannel`.`id` = `service`.`Channel_id`)) AS `ChannelType_description`,`service`.`MinTransferLimit` AS `MinTransferLimit`,`service`.`MaxTransferLimit` AS `MaxTransferLimit`,`service`.`DisplayName` AS `Display_Name`,`service`.`DisplayDescription` AS `Display_Description` from (`groupentitlement` left join `service` on((`groupentitlement`.`Service_id` = `service`.`id`)));

DROP VIEW IF EXISTS `internaluserdetails_view`;
CREATE VIEW `internaluserdetails_view` AS select `systemuser`.`id` AS `id`,`systemuser`.`Username` AS `Username`,`systemuser`.`Email` AS `Email`,`systemuser`.`Status_id` AS `Status_id`,`systemuser`.`Password` AS `Password`,`systemuser`.`Code` AS `Code`,`systemuser`.`FirstName` AS `FirstName`,`systemuser`.`MiddleName` AS `MiddleName`,`systemuser`.`LastName` AS `LastName`,`systemuser`.`FailedCount` AS `FailedCount`,`systemuser`.`LastPasswordChangedts` AS `LastPasswordChangedts`,`systemuser`.`ResetpasswordLink` AS `ResetpasswordLink`,`systemuser`.`ResetPasswordExpdts` AS `ResetPasswordExpdts`,`systemuser`.`lastLogints` AS `lastLogints`,`systemuser`.`createdby` AS `createdby`,`systemuser`.`createdts` AS `createdts`,`systemuser`.`modifiedby` AS `modifiedby`,`systemuser`.`lastmodifiedts` AS `lastmodifiedts`,`systemuser`.`synctimestamp` AS `synctimestamp`,`systemuser`.`softdeleteflag` AS `softdeleteflag`,`userrole`.`Role_id` AS `Role_id`,`userrole`.`hasSuperAdminPrivilages` AS `hasSuperAdminPrivilages`,`role`.`Name` AS `Role_Name`,`role`.`Status_id` AS `Role_Status_id` from ((`systemuser` left join `userrole` on((`userrole`.`User_id` = `systemuser`.`id`))) left join `role` on((`userrole`.`Role_id` = `role`.`id`)));

DROP VIEW IF EXISTS `internalusers_view`;
CREATE VIEW `internalusers_view` AS select `systemuser`.`id` AS `User_id`,`systemuser`.`Status_id` AS `Status_id`,`status`.`Description` AS `Status_Desc`,`systemuser`.`FirstName` AS `FirstName`,`systemuser`.`MiddleName` AS `MiddleName`,`systemuser`.`LastName` AS `LastName`,`systemuser`.`lastLogints` AS `lastLogints`,concat(`systemuser`.`FirstName`,' ',`systemuser`.`LastName`) AS `Name`,`systemuser`.`Username` AS `Username`,`systemuser`.`Email` AS `Email`,(select `userrole`.`Role_id` from `userrole` where (`systemuser`.`id` = `userrole`.`User_id`)) AS `Role_id`,(select `role`.`Description` from `role` where `role`.`id` in (select `userrole`.`Role_id` from `userrole` where (`systemuser`.`id` = `userrole`.`User_id`))) AS `Role_Desc`,(select `role`.`Name` from `role` where `role`.`id` in (select `userrole`.`Role_id` from `userrole` where (`systemuser`.`id` = `userrole`.`User_id`))) AS `Role_Name`,((select count(`userpermission`.`Permission_id`) from `userpermission` where (`systemuser`.`id` = `userpermission`.`User_id`)) + (select count(`rolepermission`.`Permission_id`) from `rolepermission` where `rolepermission`.`Role_id` in (select `userrole`.`Role_id` from `userrole` where (`systemuser`.`id` = `userrole`.`User_id`)))) AS `Permission_Count`,`systemuser`.`lastmodifiedts` AS `lastmodifiedts`,`systemuser`.`createdts` AS `createdts`,(select concat(`workaddress`.`addressLine1`,', ',ifnull(`workaddress`.`addressLine2`,''),', ',(select `city`.`Name` from `city` where (`city`.`id` = `workaddress`.`City_id`)),', ',(select `region`.`Name` from `region` where (`region`.`id` = `workaddress`.`Region_id`)),', ',(select `country`.`Name` from `country` where `country`.`id` in (select `city`.`Country_id` from `city` where (`city`.`id` = `workaddress`.`City_id`))),', ',`workaddress`.`zipCode`)) AS `Work_Addr`,(select concat(`homeaddress`.`addressLine1`,', ',ifnull(`homeaddress`.`addressLine2`,''),', ',(select `city`.`Name` from `city` where (`city`.`id` = `homeaddress`.`City_id`)),', ',(select `region`.`Name` from `region` where (`region`.`id` = `homeaddress`.`Region_id`)),', ',(select `country`.`Name` from `country` where `country`.`id` in (select `city`.`Country_id` from `city` where (`city`.`id` = `homeaddress`.`City_id`))),', ',`homeaddress`.`zipCode`)) AS `Home_Addr`,(select ifnull(`homeaddress`.`id`,'')) AS `Home_AddressID`,(select ifnull(`homeaddress`.`addressLine1`,'')) AS `Home_AddressLine1`,(select ifnull(`homeaddress`.`addressLine2`,'')) AS `Home_AddressLine2`,(select ifnull((select `city`.`Name` from `city` where (`city`.`id` = `homeaddress`.`City_id`)),'')) AS `Home_CityName`,(select ifnull(`homeaddress`.`City_id`,'')) AS `Home_CityID`,(select ifnull((select `region`.`Name` from `region` where (`region`.`id` = `homeaddress`.`Region_id`)),'')) AS `Home_StateName`,(select ifnull(`homeaddress`.`Region_id`,'')) AS `Home_StateID`,(select ifnull((select `country`.`Name` from `country` where `country`.`id` in (select `city`.`Country_id` from `city` where (`city`.`id` = `homeaddress`.`City_id`))),'')) AS `Home_CountryName`,(select ifnull((select `country`.`id` from `country` where `country`.`id` in (select `city`.`Country_id` from `city` where (`city`.`id` = `homeaddress`.`City_id`))),'')) AS `Home_CountryID`,(select ifnull(`homeaddress`.`zipCode`,'')) AS `Home_Zipcode`,(select ifnull(`workaddress`.`id`,'')) AS `Work_AddressID`,(select ifnull(`workaddress`.`addressLine1`,'')) AS `Work_AddressLine1`,(select ifnull(`workaddress`.`addressLine2`,'')) AS `Work_AddressLine2`,(select ifnull((select `city`.`Name` from `city` where (`city`.`id` = `workaddress`.`City_id`)),'')) AS `Work_CityName`,(select ifnull(`workaddress`.`City_id`,'')) AS `Work_CityID`,(select ifnull((select `region`.`Name` from `region` where (`region`.`id` = `workaddress`.`Region_id`)),'')) AS `Work_StateName`,(select ifnull(`workaddress`.`Region_id`,'')) AS `Work_StateID`,(select ifnull((select `country`.`Name` from `country` where `country`.`id` in (select `city`.`Country_id` from `city` where (`city`.`id` = `workaddress`.`City_id`))),'')) AS `Work_CountryName`,(select ifnull((select `country`.`id` from `country` where `country`.`id` in (select `city`.`Country_id` from `city` where (`city`.`id` = `workaddress`.`City_id`))),'')) AS `Work_CountryID`,(select ifnull(`workaddress`.`zipCode`,'')) AS `Work_Zipcode` from (((`systemuser` left join `status` on((`systemuser`.`Status_id` = `status`.`id`))) left join `address` `homeaddress` on(`homeaddress`.`id` in (select `useraddress`.`Address_id` from `useraddress` where ((`systemuser`.`id` = `useraddress`.`User_id`) and (`useraddress`.`Type_id` = 'ADR_TYPE_HOME'))))) left join `address` `workaddress` on(`workaddress`.`id` in (select `useraddress`.`Address_id` from `useraddress` where ((`systemuser`.`id` = `useraddress`.`User_id`) and (`useraddress`.`Type_id` = 'ADR_TYPE_WORK')))));

DROP VIEW IF EXISTS `locationdetails_view`;
CREATE VIEW `locationdetails_view` AS select distinct `location`.`id` AS `locationId`,(select group_concat(distinct concat(ucase(left(`dayschedule`.`WeekDayName`,1)),lcase(substr(`dayschedule`.`WeekDayName`,2)),':',substr(`dayschedule`.`StartTime`,1,5),'-',substr(`dayschedule`.`EndTime`,1,5)) separator ' || ') from (`dayschedule` join `location` `l`) where ((`dayschedule`.`WorkSchedule_id` = `l`.`WorkSchedule_id`) and (`l`.`id` = `location`.`id`))) AS `workingHours`,`location`.`Name` AS `informationTitle`,`location`.`PhoneNumber` AS `phone`,`location`.`EmailId` AS `email`,(case `location`.`Status_id` when 'SID_ACTIVE' then 'OPEN' else 'CLOSED' end) AS `status`,`location`.`Type_id` AS `type`,(select group_concat(`service`.`Name` order by `service`.`Name` ASC separator ' || ') from (`service` join `locationservice`) where ((`service`.`id` = `locationservice`.`Service_id`) and (`locationservice`.`Location_id` = `location`.`id`))) AS `services`,(select `city`.`Name` from `city` where (`city`.`id` = `address`.`City_id`)) AS `city`,(select `region`.`Name` from `region` where (`region`.`id` = `address`.`Region_id`)) AS `region`,(select `country`.`Name` from `country` where (`country`.`id` = (select `region`.`Country_id` from DUAL  where (`region`.`id` = `address`.`Region_id`)))) AS `country`,`address`.`addressLine1` AS `addressLine1`,`address`.`addressLine2` AS `addressLine2`,`address`.`addressLine3` AS `addressLine3`,`address`.`zipCode` AS `zipCode`,`address`.`latitude` AS `latitude`,`address`.`logitude` AS `longitude` from ((`address` join `location`) join `region`) where ((`address`.`id` = `location`.`Address_id`) and (`region`.`id` = `address`.`Region_id`)) order by `location`.`id`;

DROP VIEW IF EXISTS `locationservices_view`;
CREATE VIEW `locationservices_view` AS select `location`.`id` AS `Location_id`,`location`.`Name` AS `Location_Name`,`location`.`DisplayName` AS `Location_Display_Name`,`location`.`Description` AS `Location_Description`,`location`.`PhoneNumber` AS `Location_Phone_Number`,`location`.`EmailId` AS `Location_EmailId`,`address`.`Latitude` AS `Location_Latitude`,`address`.`Logitude` AS `Location_Longitude`,`address`.`id` AS `Location_Address_id`,`location`.`IsMainBranch` AS `Location_IsMainBranch`,`location`.`Status_id` AS `Location_Status_id`,`location`.`Type_id` AS `Location_Type_id`,`location`.`Code` AS `Location_Code`,`location`.`softdeleteflag` AS `Location_DeleteFlag`,`location`.`WorkSchedule_id` AS `Location_WorkScheduleId`,`service`.`id` AS `Service_id`,`service`.`Type_id` AS `Service_Type_id`,`service`.`Status_id` AS `Service_Status_id`,`service`.`Name` AS `Service_Name`,`service`.`Description` AS `Service_Description`,`weekday`.`StartTime` AS `Weekday_StartTime`,`weekday`.`EndTime` AS `Weekday_EndTime`,`sunday`.`StartTime` AS `Sunday_StartTime`,`sunday`.`EndTime` AS `Sunday_EndTime`,`saturday`.`StartTime` AS `Saturday_StartTime`,`saturday`.`EndTime` AS `Saturday_EndTime`,concat(`address`.`addressLine1`,', ',(select `city`.`Name` from `city` where (`city`.`id` = `address`.`City_id`)),', ',(select `region`.`Name` from `region` where (`region`.`id` = `address`.`Region_id`)),', ',(select `country`.`Name` from `country` where `country`.`id` in (select `city`.`Country_id` from `city` where (`city`.`id` = `address`.`City_id`))),', ',`address`.`zipCode`) AS `ADDRESS` from ((((((`location` left join `locationservice` on((`location`.`id` = `locationservice`.`Location_id`))) left join `service` on((`locationservice`.`Service_id` = `service`.`id`))) left join `dayschedule` `weekday` on(((`location`.`WorkSchedule_id` = `weekday`.`WorkSchedule_id`) and (`weekday`.`WeekDayName` = 'MONDAY')))) left join `dayschedule` `sunday` on(((`location`.`WorkSchedule_id` = `sunday`.`WorkSchedule_id`) and (`sunday`.`WeekDayName` = 'SUNDAY')))) left join `dayschedule` `saturday` on(((`location`.`WorkSchedule_id` = `saturday`.`WorkSchedule_id`) and (`saturday`.`WeekDayName` = 'SATURDAY')))) join `address` on((`address`.`id` = `location`.`Address_id`)));

DROP VIEW IF EXISTS `outagemessage_view`;
CREATE VIEW `outagemessage_view` AS select `outagemessage`.`id` AS `id`,`service`.`Name` AS `Name`,`service`.`Status_id` AS `service_Status_id`,`outagemessage`.`Channel_id` AS `Channel_id`,`outagemessage`.`Service_id` AS `Service_id`,`outagemessage`.`Status_id` AS `Status_id`,`outagemessage`.`MessageText` AS `MessageText`,`outagemessage`.`createdby` AS `createdby`,`outagemessage`.`modifiedby` AS `modifiedby`,`outagemessage`.`createdts` AS `createdts`,`outagemessage`.`lastmodifiedts` AS `lastmodifiedts`,`outagemessage`.`synctimestamp` AS `synctimestamp`,`outagemessage`.`softdeleteflag` AS `softdeleteflag` from (`outagemessage` left join `service` on((`outagemessage`.`Service_id` = `service`.`id`)));

DROP VIEW IF EXISTS `overallpaymentlimits_view`;
CREATE VIEW `overallpaymentlimits_view` AS select `transactiongroup`.`id` AS `TransactionGroupId`,`transactiongroup`.`Name` AS `TransactionGroupName`,`transactiongroupservice`.`id` AS `TransactionGroupServiceId`,`transactiongroupservice`.`Service_id` AS `ServiceId`,`service`.`Name` AS `ServiceName`,`transactiongroup`.`Status_id` AS `Status_id`,`periodiclimit`.`TransactionLimit_id` AS `TransactionLimitId`,`periodiclimit`.`id` AS `PeriodicLimitId`,`periodiclimit`.`Period_id` AS `Period_id`,`periodiclimit`.`MaximumLimit` AS `MaximumLimit`,`periodiclimit`.`Currency` AS `Currency`,`period`.`Name` AS `PeriodName`,`period`.`DayCount` AS `DayCount`,`period`.`Order` AS `Order` from ((((`transactiongroup` join `transactiongroupservice` on((`transactiongroupservice`.`TransactionGroup_id` = `transactiongroup`.`id`))) join `periodiclimit` on((`periodiclimit`.`TransactionLimit_id` = `transactiongroup`.`TransactionLimit_id`))) join `period` on((`periodiclimit`.`Period_id` = `period`.`id`))) join `service` on((`service`.`id` = `transactiongroupservice`.`Service_id`)));

DROP VIEW IF EXISTS `periodiclimitenduser_view`;
CREATE VIEW `periodiclimitenduser_view` AS select `customerentitlement`.`Customer_id` AS `Customer_id`,`customerentitlement`.`Service_id` AS `Service_id`,`customerentitlement`.`TransactionFee_id` AS `TransactionFee_id`,`customerentitlement`.`TransactionLimit_id` AS `TransactionLimit_id`,`periodiclimit`.`id` AS `PeriodLimit_id`,`periodiclimit`.`Period_id` AS `Period_id`,`period`.`Name` AS `Period_Name`,`periodiclimit`.`MaximumLimit` AS `MaximumLimit`,`periodiclimit`.`Code` AS `Code`,`periodiclimit`.`Currency` AS `Currency` from ((`periodiclimit` join `customerentitlement` on((`customerentitlement`.`TransactionLimit_id` = `periodiclimit`.`TransactionLimit_id`))) join `period` on((`periodiclimit`.`Period_id` = `period`.`id`)));

DROP VIEW IF EXISTS `periodiclimitservice_view`;
CREATE VIEW `periodiclimitservice_view` AS select `service`.`id` AS `Service_id`,`service`.`TransactionFee_id` AS `TransactionFee_id`,`service`.`TransactionLimit_id` AS `TransactionLimit_id`,`periodiclimit`.`id` AS `PeriodLimit_id`,`periodiclimit`.`Period_id` AS `Period_id`,`period`.`Name` AS `Period_Name`,`periodiclimit`.`MaximumLimit` AS `MaximumLimit`,`periodiclimit`.`Code` AS `Code`,`periodiclimit`.`Currency` AS `Currency` from ((`periodiclimit` join `service` on((`service`.`TransactionLimit_id` = `periodiclimit`.`TransactionLimit_id`))) join `period` on((`periodiclimit`.`Period_id` = `period`.`id`)));

DROP VIEW IF EXISTS `periodiclimitusergroup_view`;
CREATE VIEW `periodiclimitusergroup_view` AS select `groupentitlement`.`Group_id` AS `Group_id`,`groupentitlement`.`Service_id` AS `Service_id`,`groupentitlement`.`TransactionFee_id` AS `TransactionFee_id`,`groupentitlement`.`TransactionLimit_id` AS `TransactionLimit_id`,`periodiclimit`.`id` AS `PeriodLimit_id`,`periodiclimit`.`Period_id` AS `Period_id`,`period`.`Name` AS `Period_Name`,`periodiclimit`.`MaximumLimit` AS `MaximumLimit`,`periodiclimit`.`Code` AS `Code`,`periodiclimit`.`Currency` AS `Currency` from ((`periodiclimit` join `groupentitlement` on((`groupentitlement`.`TransactionLimit_id` = `periodiclimit`.`TransactionLimit_id`))) join `period` on((`periodiclimit`.`Period_id` = `period`.`id`)));

DROP VIEW IF EXISTS `permissions_view`;
CREATE VIEW `permissions_view` AS select `permission`.`id` AS `Permission_id`,`permission`.`Type_id` AS `PermissionType_id`,`permission`.`Name` AS `Permission_Name`,`permission`.`Description` AS `Permission_Desc`,`permission`.`Status_id` AS `Status_id`,`status`.`Description` AS `Status_Desc`,(select count(`rolepermission`.`Role_id`) from `rolepermission` where (`rolepermission`.`Permission_id` = `permission`.`id`)) AS `Role_Count`,((select count(`userpermission`.`Permission_id`) from `userpermission` where (`userpermission`.`Permission_id` = `permission`.`id`)) + (select count(`userrole`.`User_id`) from `userrole` where `userrole`.`Role_id` in (select `rolepermission`.`Role_id` from `rolepermission` where (`rolepermission`.`Permission_id` = `permission`.`id`)))) AS `Users_Count`,(case `permission`.`Status_id` when 'SID_ACTIVE' then 'Active' else 'Inactive' end) AS `Status` from (`permission` left join `status` on((`permission`.`Status_id` = `status`.`id`)));

DROP VIEW IF EXISTS `permissionsallusers_view`;
CREATE VIEW `permissionsallusers_view` AS select `userpermission`.`User_id` AS `User_id`,`userpermission`.`Permission_id` AS `Permission_id`,`permission`.`Name` AS `Permission_Name`,`permission`.`Status_id` AS `Permission_Status_id`,`permission`.`Description` AS `Permission_Description`,`systemuser`.`Status_id` AS `User_Status_id`,`systemuser`.`Username` AS `UserName`,`systemuser`.`Email` AS `Email`,`systemuser`.`FirstName` AS `FirstName`,`systemuser`.`MiddleName` AS `MiddleName`,`systemuser`.`LastName` AS `LastName`,`systemuser`.`createdby` AS `createdby`,`systemuser`.`modifiedby` AS `updatedby`,`systemuser`.`createdts` AS `createdts`,`systemuser`.`lastmodifiedts` AS `updatedts`,'true' AS `isDirect`,`systemuser`.`softdeleteflag` AS `softdeleteflag` from ((`userpermission` join `systemuser` on((`userpermission`.`User_id` = `systemuser`.`id`))) join `permission` on((`userpermission`.`Permission_id` = `permission`.`id`))) union select `userrole`.`User_id` AS `User_id`,`rolepermission`.`Permission_id` AS `Permission_id`,`permission`.`Name` AS `Permission_Name`,`permission`.`Status_id` AS `Permission_Status_id`,`permission`.`Description` AS `Permission_Description`,`systemuser`.`Status_id` AS `User_Status_id`,`systemuser`.`Username` AS `UserName`,`systemuser`.`Email` AS `Email`,`systemuser`.`FirstName` AS `FirstName`,`systemuser`.`MiddleName` AS `MiddleName`,`systemuser`.`LastName` AS `LastName`,`systemuser`.`createdby` AS `createdby`,`systemuser`.`modifiedby` AS `updatedby`,`systemuser`.`createdts` AS `createdts`,`systemuser`.`lastmodifiedts` AS `updatedts`,'false' AS `isDirect`,`systemuser`.`softdeleteflag` AS `softdeleteflag` from (((`rolepermission` join `userrole` on((`userrole`.`Role_id` = `rolepermission`.`Role_id`))) join `systemuser` on((`userrole`.`User_id` = `systemuser`.`id`))) join `permission` on((`rolepermission`.`Permission_id` = `permission`.`id`)));

DROP VIEW IF EXISTS `requestmessages_view`;
CREATE  VIEW `requestmessages_view` AS (select `requestmessage`.`id` AS `Message_id`,(select count(`messageattachment`.`id`) from `messageattachment` where (`messageattachment`.`RequestMessage_id` = `requestmessage`.`id`)) AS `totalAttachments`,`messageattachment`.`id` AS `Messageattachment_id`,`messageattachment`.`Media_id` AS `Media_id`,`media`.`Name` AS `Media_Name`,`media`.`Type` AS `Media_Type`,`media`.`Size` AS `Media_Size`,`requestmessage`.`CustomerRequest_id` AS `CustomerRequest_id`,`requestmessage`.`MessageDescription` AS `MessageDescription`,`requestmessage`.`ReplySequence` AS `ReplySequence`,`requestmessage`.`IsRead` AS `IsRead`,`requestmessage`.`createdby` AS `createdby`,`requestmessage`.`RepliedBy_Name` AS `createdby_name`,`requestmessage`.`modifiedby` AS `modifiedby`,`requestmessage`.`createdts` AS `createdts`,`requestmessage`.`lastmodifiedts` AS `lastmodifiedts`,`requestmessage`.`synctimestamp` AS `synctimestamp`,`requestmessage`.`softdeleteflag` AS `softdeleteflag` from ((`requestmessage` left join `messageattachment` on((`requestmessage`.`id` = `messageattachment`.`RequestMessage_id`))) left join `media` on((`messageattachment`.`Media_id` = `media`.`id`))));

DROP VIEW IF EXISTS `rolepermission_view`;
CREATE VIEW `rolepermission_view` AS select `role`.`Name` AS `Role_Name`,`role`.`Description` AS `Role_Description`,`role`.`Status_id` AS `Role_Status_id`,`rolepermission`.`Role_id` AS `Role_id`,`permission`.`id` AS `Permission_id`,`permission`.`Type_id` AS `Permission_Type_id`,`permission`.`Status_id` AS `Permission_Status_id`,`permission`.`DataType_id` AS `DataType_id`,`permission`.`Name` AS `Permission_Name`,`permission`.`Description` AS `Permission_Description`,`permission`.`isComposite` AS `Permission_isComposite`,`permission`.`PermissionValue` AS `PermissionValue`,`permission`.`createdby` AS `Permission_createdby`,`permission`.`modifiedby` AS `Permission_modifiedby`,`permission`.`createdts` AS `Permission_createdts`,`permission`.`lastmodifiedts` AS `Permission_lastmodifiedts`,`permission`.`synctimestamp` AS `Permission_synctimestamp`,`permission`.`softdeleteflag` AS `Permission_softdeleteflag` from ((`rolepermission` left join `permission` on((`rolepermission`.`Permission_id` = `permission`.`id`))) join `role` on((`role`.`id` = `rolepermission`.`Role_id`))); 

DROP VIEW IF EXISTS `roles_view`;
CREATE VIEW `roles_view` AS select `role`.`id` AS `role_id`,`role`.`Type_id` AS `roleType_id`,`role`.`Name` AS `role_Name`,`role`.`Description` AS `role_Desc`,`role`.`Status_id` AS `Status_id`,`status`.`Description` AS `Status_Desc`,(select count(`rolepermission`.`Role_id`) from `rolepermission` where (`rolepermission`.`Role_id` = `role`.`id`)) AS `permission_Count`,(select count(`userrole`.`User_id`) from `userrole` where `userrole`.`Role_id` in (select `rolepermission`.`Role_id` from `rolepermission` where (`rolepermission`.`Role_id` = `role`.`id`))) AS `Users_Count`,(case `role`.`Status_id` when 'SID_ACTIVE' then 'Active' else 'Inactive' end) AS `Status` from (`role` left join `status` on((`role`.`Status_id` = `status`.`id`)));

DROP VIEW IF EXISTS `roleuser_view`;
CREATE VIEW `roleuser_view` AS select `userrole`.`User_id` AS `User_id`,`userrole`.`Role_id` AS `Role_id`,`systemuser`.`Status_id` AS `Status_id`,`systemuser`.`Username` AS `Username`,`systemuser`.`FirstName` AS `FirstName`,`systemuser`.`MiddleName` AS `MiddleName`,`systemuser`.`LastName` AS `LastName`,`systemuser`.`Email` AS `Email`,`systemuser`.`modifiedby` AS `UpdatedBy`,`systemuser`.`lastmodifiedts` AS `LastModifiedTimeStamp` from (`userrole` join `systemuser` on((`userrole`.`User_id` = `systemuser`.`id`)));

DROP VIEW IF EXISTS `security_images_view`;
CREATE VIEW `security_images_view` AS select `securityimage`.`id` AS `SecurityImage_id`,`securityimage`.`Image` AS `SecurityImageBase64String`,`securityimage`.`Status_id` AS `SecurityImage_Status`,(select count(`customersecurityimages`.`Customer_id`) from `customersecurityimages` where (`customersecurityimages`.`Image_id` = `securityimage`.`id`)) AS `UserCount`,`securityimage`.`softdeleteflag` AS `softdeleteflag` from `securityimage`;

DROP VIEW IF EXISTS `security_questions_view`;
CREATE VIEW `security_questions_view` AS select `securityquestion`.`id` AS `SecurityQuestion_id`,`securityquestion`.`Question` AS `SecurityQuestion`,`securityquestion`.`Status_id` AS `SecurityQuestion_Status`,`securityquestion`.`lastmodifiedts` AS `lastmodifiedts`,(select count(`customersecurityquestions`.`Customer_id`) from `customersecurityquestions` where (`customersecurityquestions`.`SecurityQuestion_id` = `securityquestion`.`id`)) AS `UserCount`,`securityquestion`.`softdeleteflag` AS `softdeleteflag` from `securityquestion`;

DROP VIEW IF EXISTS `service_view`;
CREATE VIEW `service_view` AS select `service`.`id` AS `id`,`service`.`Type_id` AS `Type_id`,`service`.`Channel_id` AS `Channel_id`,`service`.`Name` AS `Name`,`service`.`Description` AS `Description`,`service`.`DisplayName` AS `DisplayName`,`service`.`DisplayDescription` AS `DisplayDescription`,`service`.`Category_id` AS `Category_Id`,`service`.`code` AS `Code`,`service`.`Status_id` AS `Status_id`,`service`.`Notes` AS `Notes`,`service`.`MaxTransferLimit` AS `MaxTransferLimit`,`service`.`MinTransferLimit` AS `MinTransferLimit`,`service`.`TransferDenominations` AS `TransferDenominations`,`service`.`IsFutureTransaction` AS `IsFutureTransaction`,`service`.`TransactionCharges` AS `TransactionCharges`,`service`.`IsAuthorizationRequired` AS `IsAuthorizationRequired`,`service`.`IsSMSAlertActivated` AS `IsSMSAlertActivated`,`service`.`SMSCharges` AS `SMSCharges`,`service`.`IsBeneficiarySMSAlertActivated` AS `IsBeneficiarySMSAlertActivated`,`service`.`BeneficiarySMSCharge` AS `BeneficiarySMSCharge`,`service`.`HasWeekendOperation` AS `HasWeekendOperation`,`service`.`IsOutageMessageActive` AS `IsOutageMessageActive`,`service`.`IsAlertActive` AS `IsAlertActive`,`service`.`IsTCActive` AS `IsTCActive`,`service`.`IsAgreementActive` AS `IsAgreementActive`,`service`.`IsCampaignActive` AS `IsCampaignActive`,`service`.`WorkSchedule_id` AS `WorkSchedule_id`,`service`.`TransactionFee_id` AS `TransactionFee_id`,`service`.`TransactionLimit_id` AS `TransactionLimit_id`,`service`.`createdby` AS `createdby`,`service`.`modifiedby` AS `modifiedby`,`service`.`createdts` AS `createdts`,`service`.`lastmodifiedts` AS `lastmodifiedts`,`service`.`synctimestamp` AS `synctimestamp`,`service`.`softdeleteflag` AS `softdeleteflag`,`status`.`Description` AS `Status`,`category`.`Name` AS `Category_Name`,`servicechannel`.`Description` AS `Channel`,`servicetype`.`Description` AS `Type_Name`,`workschedule`.`Description` AS `WorkSchedule_Desc` from (((((`service` left join `servicechannel` on((`service`.`Channel_id` = `servicechannel`.`id`))) left join `category` on((`service`.`Category_id` = `category`.`id`))) left join `status` on((`service`.`Status_id` = `status`.`id`))) left join `servicetype` on((`service`.`Type_id` = `servicetype`.`id`))) left join `workschedule` on((`service`.`WorkSchedule_id` = `workschedule`.`id`)));

DROP VIEW IF EXISTS `systemuser_permissions_view`;
CREATE VIEW `systemuser_permissions_view` AS select `up`.`User_id` AS `User_id`,`up`.`Permission_id` AS `Permission_id`,`p`.`Name` AS `Permission_name`,`p`.`Status_id` AS `Permission_status` from (`userpermission` `up` join `permission` `p` on((`up`.`Permission_id` = `p`.`id`))) union select `ur`.`User_id` AS `User_id`,`p`.`id` AS `Permission_id`,`p`.`Name` AS `Permission_name`,`p`.`Status_id` AS `Permission_status` from (((`role` `r` join `userrole` `ur` on((`r`.`id` = `ur`.`Role_id`))) join `rolepermission` `rp` on((`ur`.`Role_id` = `rp`.`Role_id`))) join `permission` `p` on((`rp`.`Permission_id` = `p`.`id`)));

DROP VIEW IF EXISTS `systemuser_view`;
CREATE VIEW `systemuser_view` AS select `systemuser`.`id` AS `UserID`,`systemuser`.`Username` AS `Username`,`systemuser`.`FirstName` AS `FirstName`,`systemuser`.`MiddleName` AS `MiddleName`,`systemuser`.`LastName` AS `LastName`,`systemuser`.`Email` AS `Email`,`systemuser`.`Status_id` AS `Status_id`,`systemuser`.`modifiedby` AS `UpdatedBy`,`systemuser`.`lastmodifiedts` AS `LastModifiedTimeStamp` from `systemuser`;

DROP VIEW IF EXISTS `transactionfeegroup_view`;
CREATE VIEW `transactionfeegroup_view` AS select `groupentitlement`.`Group_id` AS `Group_id`,`groupentitlement`.`Service_id` AS `Service_id`,`groupentitlement`.`TransactionFee_id` AS `TransactionFee_id`,`transactionfeeslab`.`MinimumTransactionValue` AS `MinimumTransactionValue`,`transactionfeeslab`.`MaximumTransactionValue` AS `MaximumTransactionValue`,`transactionfeeslab`.`Fees` AS `Fees`,`transactionfeeslab`.`id` AS `transactionFeeSlab_id` from (`transactionfeeslab` join `groupentitlement` on((`groupentitlement`.`TransactionFee_id` = `transactionfeeslab`.`TransactionFee_id`)));

DROP VIEW IF EXISTS `transactionfeesenduser_view`;
CREATE VIEW `transactionfeesenduser_view` AS select `customerentitlement`.`Customer_id` AS `customer_id`,`customerentitlement`.`Service_id` AS `Service_id`,`customerentitlement`.`TransactionFee_id` AS `TransactionFee_id`,`customerentitlement`.`TransactionLimit_id` AS `TransactionLimit_id`,`transactionfee`.`Description` AS `transactionfee_Description`,`transactionfeeslab`.`MinimumTransactionValue` AS `MinimumTransactionValue`,`transactionfeeslab`.`MaximumTransactionValue` AS `MaximumTransactionValue`,`transactionfeeslab`.`Currency` AS `Currency`,`transactionfeeslab`.`Fees` AS `Fees`,`transactionfeeslab`.`id` AS `transactionFeeSlab_id` from ((`transactionfee` join `customerentitlement` on((`customerentitlement`.`TransactionFee_id` = `transactionfee`.`id`))) join `transactionfeeslab` on((`transactionfeeslab`.`TransactionFee_id` = `transactionfee`.`id`)));

DROP VIEW IF EXISTS `transactionfeeservice_view`;
CREATE VIEW `transactionfeeservice_view` AS select `service`.`id` AS `Service_id`,`service`.`TransactionFee_id` AS `TransactionFee_id`,`service`.`TransactionLimit_id` AS `TransactionLimit_id`,`transactionfeeslab`.`MinimumTransactionValue` AS `MinimumTransactionValue`,`transactionfeeslab`.`MaximumTransactionValue` AS `MaximumTransactionValue`,`transactionfeeslab`.`Currency` AS `Currency`,`transactionfeeslab`.`Fees` AS `Fees`,`transactionfeeslab`.`id` AS `transactionFeeSlab_id` from (`transactionfeeslab` join `service` on((`service`.`TransactionFee_id` = `transactionfeeslab`.`TransactionFee_id`)));

DROP VIEW IF EXISTS `userdirectpermission_view`;
CREATE VIEW `userdirectpermission_view` AS select `userpermission`.`User_id` AS `User_id`,`userpermission`.`Permission_id` AS `Permission_id`,`permission`.`Name` AS `Permission_Name`,`permission`.`Status_id` AS `Permission_Status_id`,`permission`.`Description` AS `Permission_Description`,`permission`.`isComposite` AS `Permission_isComposite`,`systemuser`.`Status_id` AS `User_Status_id`,`systemuser`.`Username` AS `UserName`,`systemuser`.`Email` AS `Email`,`systemuser`.`FirstName` AS `FirstName`,`systemuser`.`MiddleName` AS `MiddleName`,`systemuser`.`LastName` AS `LastName`,`systemuser`.`createdby` AS `createdby`,`systemuser`.`modifiedby` AS `updatedby`,`systemuser`.`createdts` AS `createdts`,`systemuser`.`lastmodifiedts` AS `updatedts`,`systemuser`.`softdeleteflag` AS `softdeleteflag` from ((`userpermission` join `systemuser` on((`userpermission`.`User_id` = `systemuser`.`id`))) join `permission` on((`userpermission`.`Permission_id` = `permission`.`id`)));

DROP VIEW IF EXISTS `userpermission_view`;
CREATE VIEW `userpermission_view` AS select `userrole`.`User_id` AS `User_id`,`userrole`.`Role_id` AS `Role_id`,`rolepermission`.`Permission_id` AS `Permission_id`,`permission`.`Name` AS `Permission_Name`,`permission`.`Description` AS `Permission_Desc`,`permission`.`isComposite` AS `Permission_isComposite` from ((`userrole` left join `rolepermission` on((`userrole`.`Role_id` = `rolepermission`.`Role_id`))) left join `permission` on((`permission`.`id` = `rolepermission`.`Permission_id`)));

DROP VIEW IF EXISTS `customer_device_information_view`;
CREATE VIEW `customer_device_information_view` AS select `customerdevice`.`id` AS `Device_id`,`customerdevice`.`Customer_id` AS `Customer_id`,`customer`.`Username` AS `Customer_username`,`customerdevice`.`DeviceName` AS `DeviceName`,`customerdevice`.`LastLoginTime` AS `LastLoginTime`,`customerdevice`.`LastUsedIp` AS `LastUsedIp`,`customerdevice`.`Status_id` AS `Status_id`,`status`.`Description` AS `Status_name`,`customerdevice`.`OperatingSystem` AS `OperatingSystem`,`customerdevice`.`Channel_id` AS `Channel_id`,`servicechannel`.`Description` AS `Channel_Description`,`customerdevice`.`EnrollmentDate` AS `EnrollmentDate`,`customerdevice`.`createdby` AS `createdby`,`customerdevice`.`modifiedby` AS `modifiedby`,`servicechannel`.`createdts` AS `Registered_Date`,`customerdevice`.`lastmodifiedts` AS `lastmodifiedts` from (((`customerdevice` left join `status` on((`customerdevice`.`Status_id` = `status`.`id`))) left join `servicechannel` on((`servicechannel`.`id` = `customerdevice`.`Channel_id`))) left join `customer` on((`customer`.`id` = `customerdevice`.`Customer_id`)));

DROP VIEW IF EXISTS `card_request_notification_count_view`;
CREATE VIEW `card_request_notification_count_view` AS select `cardaccountrequest_alias`.`Customer_id` AS `customerId`,`cardaccountrequest_alias`.`CardAccountNumber` AS `cardNumber`,'REQUEST' AS `reqType`,count(`cardaccountrequest_alias`.`CardAccountNumber`) AS `requestcount` from `cardaccountrequest` `cardaccountrequest_alias` group by `cardaccountrequest_alias`.`Customer_id`,`cardaccountrequest_alias`.`CardAccountNumber` union select `notificationcardinfo_alias`.`Customer_id` AS `customerId`,`notificationcardinfo_alias`.`CardNumber` AS `cardNumber`,'NOTIFICATION' AS `reqType`,count(`notificationcardinfo_alias`.`CardNumber`) AS `requestcount` from `notificationcardinfo` `notificationcardinfo_alias` group by `notificationcardinfo_alias`.`Customer_id`,`notificationcardinfo_alias`.`CardNumber`;

DROP VIEW IF EXISTS `travelnotifications_view`;
CREATE VIEW `travelnotifications_view` AS select `travelnotification`.`id` AS `notificationId`,`travelnotification`.`PlannedDepartureDate` AS `startDate`,`travelnotification`.`PlannedReturnDate` AS `endDate`,`travelnotification`.`Destinations` AS `destinations`,`travelnotification`.`AdditionalNotes` AS `additionalNotes`,`travelnotification`.`Status_id` AS `Status_id`,`travelnotification`.`phonenumber` AS `contactNumber`,`notificationcardinfo`.`Customer_id` AS `customerId`,`travelnotification`.`createdts` AS `date`,group_concat(concat(`notificationcardinfo`.`CardName`,' ',`notificationcardinfo`.`CardNumber`) separator ',') AS `cardNumber`,count(`notificationcardinfo`.`CardNumber`) AS `cardCount`,`status`.`Description` AS `status` from ((`travelnotification` left join `notificationcardinfo` on((`travelnotification`.`id` = `notificationcardinfo`.`Notification_id`))) left join `status` on((`travelnotification`.`Status_id` = `status`.`id`))) group by `travelnotification`.`id`;

DROP VIEW IF EXISTS `cardaccountrequest_view`;
CREATE VIEW `cardaccountrequest_view` AS select `cardaccountrequest`.`id` AS `Request_id`,`cardaccountrequest`.`Date` AS `Date`,`cardaccountrequesttype`.`DisplayName` AS `Type`,`cardaccountrequest`.`CardAccountNumber` AS `CardAccountNumber`,`cardaccountrequest`.`CardAccountName` AS `CardAccountName`,`cardaccountrequest`.`RequestReason` AS `Reason`,`status`.`Description` AS `Status`,`cardaccountrequest`.`Customer_id` AS `CustomerId`,`customercommunication`.`Value` AS `CommunicationValue`,`communicationtype`.`Description` AS `DeliveryMode`,concat_ws(`address`.`addressLine1`,`address`.`addressLine2`,`address`.`addressLine3`,`city`.`Name`,`region`.`Name`,`country`.`Name`,`address`.`zipCode`) AS `Address` from ((((((((`cardaccountrequest` left join `cardaccountrequesttype` on((`cardaccountrequest`.`RequestType_id` = `cardaccountrequesttype`.`id`))) left join `status` on((`cardaccountrequest`.`Status_id` = `status`.`id`))) left join `address` on((`cardaccountrequest`.`Address_id` = `address`.`id`))) left join `city` on((`address`.`City_id` = `city`.`id`))) left join `region` on((`address`.`Region_id` = `region`.`id`))) left join `country` on((`city`.`Country_id` = `country`.`id`))) left join `customercommunication` on((`cardaccountrequest`.`Communication_id` = `customercommunication`.`id`))) left join `communicationtype` on((`customercommunication`.`Type_id` = `communicationtype`.`id`)));

DROP VIEW IF EXISTS `location_view`;
CREATE VIEW `location_view` AS select distinct `location`.`id` AS `id`,`location`.`Name` AS `Name`,`location`.`Code` AS `Code`,`location`.`Description` AS `Description`,`location`.`PhoneNumber` AS `PhoneNumber`,`location`.`Type_id` AS `Type_id`,(case `location`.`Status_id` when 'SID_ACTIVE' then 'Active' else 'Inactive' end) AS `Status_id` from `location` order by `location`.`id`;

DROP VIEW IF EXISTS `region_details_view`;
CREATE VIEW `region_details_view` AS select `region`.`id` AS `region_Id`,`region`.`Code` AS `region_Code`,`region`.`Name` AS `region_Name`,`region`.`Country_id` AS `country_Id`,`country`.`Code` AS `country_Code`,`country`.`Name` AS `country_Name`,`region`.`createdby` AS `region_createdby`,`region`.`modifiedby` AS `region_modifiedby`,`region`.`createdts` AS `region_createdts`,`region`.`lastmodifiedts` AS `region_lastmodifiedts` from (`region` left join `country` on((`country`.`id` = `region`.`Country_id`)));




CREATE TABLE `otherproducttype` (
  `id` varchar(50) NOT NULL,
  `Name` varchar(50) NOT NULL,
  `Description` varchar(1000) NOT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `product` 
ADD COLUMN `OtherProductType_id` VARCHAR(50) NULL AFTER `Type_id`,
ADD INDEX `FK_OtherProduct_Type_idx` (`OtherProductType_id` ASC);

ALTER TABLE `product` 
ADD CONSTRAINT `FK_OtherProduct_Type`
  FOREIGN KEY (`OtherProductType_id`)
  REFERENCES `otherproducttype` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE `product` 
ADD COLUMN `MarketingStateId` VARCHAR(50) NULL AFTER `Status_id`,
ADD COLUMN `SecondaryProduct_id` VARCHAR(50) NULL AFTER `MarketingStateId`,
ADD UNIQUE INDEX `MarketingStateId_UNIQUE` (`MarketingStateId` ASC),
ADD INDEX `FK_SecondaryProductId_idx` (`SecondaryProduct_id` ASC);

ALTER TABLE `product` 
ADD CONSTRAINT `FK_SecondaryProductId`
  FOREIGN KEY (`SecondaryProduct_id`)
  REFERENCES `product` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE `otherproducttype` CHANGE COLUMN `Name` `Name` VARCHAR(200) NOT NULL ;

DROP VIEW IF EXISTS `productdetail_view`;
CREATE VIEW `productdetail_view` AS  SELECT `product`.`id` AS `productId`,`product`.`Type_id` AS `Type_id`, `product`.`ProductCode` AS `productTypeId`, `product`.`Name` AS `productName`, `product`.`Status_id` AS `Status_id`, `product`.`ProductFeatures` AS `features`, `product`.`ProductCharges` AS `rates`, `product`.`AdditionalInformation` AS `info`, `product`.`productDescription` AS `productDescription`, `product`.`termsAndConditions` AS `termsAndConditions`, `product`.`createdby` AS `createdby`, `product`.`modifiedby` AS `modifiedby`, `product`.`createdts` AS `createdts`, `product`.`lastmodifiedts` AS `lastmodifiedts`, `product`.`synctimestamp` AS `synctimestamp`, `product`.`softdeleteflag` AS `softdeleteflag`, `producttype`.`Name` AS `productType`, `product`.`MarketingStateId` AS `MarketingStateId`, `product`.`SecondaryProduct_id` AS `SecondaryProduct_id`, `otherproducttype`.`Name` AS `otherproducttype_Name`, `otherproducttype`.`Description` AS `otherproducttype_Description`, `otherproducttype`.`id` AS `otherproducttype_id`    FROM (`product` JOIN `producttype` ON ((`product`.`Type_id` = `producttype`.`id`)) JOIN `otherproducttype` ON ((`product`.`OtherProductType_id` = `otherproducttype`.`id`)));

ALTER TABLE `applicant` 
ADD COLUMN `DateOfBirth` DATE NULL AFTER `LastName`,
ADD COLUMN `MothersMaidenName` VARCHAR(50) NULL AFTER `DateOfBirth`,
ADD COLUMN `Address_id` VARCHAR(50) NULL AFTER `MothersMaidenName`,
ADD COLUMN `IDType` VARCHAR(50) NOT NULL AFTER `EmailID`,
ADD COLUMN `IDValue` VARCHAR(50) NULL AFTER `IDType`,
ADD COLUMN `IDState` VARCHAR(50) NULL AFTER `IDValue`,
ADD COLUMN `IDCountry` VARCHAR(50) NULL AFTER `IDState`,
ADD COLUMN `IssueDate` DATE NULL AFTER `IDCountry`,
ADD COLUMN `ExpiryDate` DATE NULL AFTER `IssueDate`,
CHANGE COLUMN `EmailID` `EmailID` VARCHAR(50) NULL AFTER `Address_id`,
CHANGE COLUMN `Product` `Product` VARCHAR(50) NULL ,
CHANGE COLUMN `ContactNumber` `ContactNumber` VARCHAR(50) NULL DEFAULT NULL ;

CREATE TABLE `idtype` (
  `IDType` VARCHAR(50) NOT NULL,
  `IDName` VARCHAR(50) NULL,
  PRIMARY KEY (`IDType`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `applicant` 
ADD INDEX `FK_Applicant_IDType_idx` (`IDType` ASC);

ALTER TABLE `applicant` 
ADD CONSTRAINT `FK_Applicant_IDType`
  FOREIGN KEY (`IDType`)
  REFERENCES `idtype` (`IDType`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE `applicant` 
ADD INDEX `FK_Applicant_Address_idx` (`Address_id` ASC);

ALTER TABLE `applicant` 
ADD CONSTRAINT `FK_Applicant_Address`
  FOREIGN KEY (`Address_id`)
  REFERENCES `address` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE `applicant` 
ADD INDEX `FK_Applicant_Status_idx` (`Status` ASC);

ALTER TABLE `applicant` 
ADD CONSTRAINT `FK_Applicant_Status`
  FOREIGN KEY (`Status`)
  REFERENCES `status` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

CREATE TABLE `eligibilitycriteria` (
  `id` VARCHAR(50) NOT NULL,
  `Description` TEXT NULL,
  `Status_id` VARCHAR(50) CHARACTER SET 'utf8' NULL,
  `createdby` VARCHAR(50) NULL,
  `modifiedby` VARCHAR(50) NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT 0 ,
  PRIMARY KEY (`id`)
  )ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `applicant` 
CHANGE COLUMN `Product` `Product` VARCHAR(50) NULL ;

ALTER TABLE `eligibilitycriteria` 
ADD INDEX `FK_EligibilityCriteria_Status_idx` (`Status_id` ASC);

ALTER TABLE `eligibilitycriteria` 
ADD CONSTRAINT `FK_EligibilityCriteria_Status`
  FOREIGN KEY (`Status_id`)
  REFERENCES `status` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE `applicant` 
ADD COLUMN `EligbilityCriteria` VARCHAR(50) NOT NULL AFTER `Product`,
ADD COLUMN `Reason` TEXT NULL AFTER `Status`,
ADD INDEX `FK_Applicant_EligibilityCriteria_idx` (`EligbilityCriteria` ASC);

ALTER TABLE `applicant` 
ADD CONSTRAINT `FK_Applicant_EligibilityCriteria`
  FOREIGN KEY (`EligbilityCriteria`)
  REFERENCES `eligibilitycriteria` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE `applicant` 
ADD COLUMN `AddressValidationStatus` VARCHAR(10) NULL DEFAULT NULL AFTER `Address_id`;

ALTER TABLE `address` 
ADD COLUMN `cityName` VARCHAR(100) NULL AFTER `logitude`,
ADD COLUMN `User_id` INT(11) NULL AFTER `cityName`,
ADD COLUMN `country` VARCHAR(50) NULL AFTER `User_id`,
ADD COLUMN `type` ENUM('home', 'office') NULL AFTER `country`,
ADD COLUMN `state` VARCHAR(100) NULL AFTER `type`,
CHANGE COLUMN `AddressLine1` `addressLine1` VARCHAR(100) NULL DEFAULT NULL ,
CHANGE COLUMN `AddressLine2` `addressLine2` VARCHAR(100) NULL DEFAULT NULL ,
CHANGE COLUMN `AddressLine3` `addressLine3` VARCHAR(100) NULL DEFAULT NULL ,
CHANGE COLUMN `ZipCode` `zipCode` VARCHAR(20) NULL DEFAULT NULL ,
CHANGE COLUMN `Latitude` `latitude` VARCHAR(20) NULL DEFAULT NULL ,
CHANGE COLUMN `Logitude` `logitude` VARCHAR(20) NULL DEFAULT NULL ;
ALTER TABLE `address` ADD COLUMN `isPreferredAddress` TINYINT(1) NULL AFTER `logitude`;
ALTER TABLE `address` CHANGE COLUMN `isPreferredAddress` `isPreferredAddress` BIT(1) NULL DEFAULT NULL ;

ALTER TABLE `alert` 
ADD COLUMN `Account_id` VARCHAR(50) NULL AFTER `AlertContent`,
ADD COLUMN `isActive` TINYINT(1) NULL AFTER `Account_id`,
ADD COLUMN `hasValue` TINYINT(1) NULL AFTER `isActive`,
ADD COLUMN `currentValue` VARCHAR(50) NULL AFTER `hasValue`,
ADD COLUMN `defaultValue` VARCHAR(50) NULL AFTER `currentValue`;

ALTER TABLE `product` 
ADD COLUMN `accountType` INT(11) NULL AFTER `termsAndConditions`,
ADD COLUMN `stateId` INT(11) NULL AFTER `accountType`,
ADD COLUMN `rates` VARCHAR(2000) NULL AFTER `stateId`,
ADD COLUMN `productImageURL` VARCHAR(100) NULL AFTER `rates`;

ALTER TABLE `customeraddress` 
ADD COLUMN `DurationOfStay` VARCHAR(50) NULL AFTER `isPrimary`,
ADD COLUMN `HomeOwnership` VARCHAR(50) NULL AFTER `DurationOfStay`;

ALTER TABLE `customercommunication` 
ADD COLUMN `IsPreferredContactMethod` TINYINT(1) NULL AFTER `Description`,
ADD COLUMN `PreferredContactTime` VARCHAR(50) NULL AFTER `IsPreferredContactMethod`;

ALTER TABLE `customerpreference` 
ADD COLUMN `PreferedOtpMethod` VARCHAR(50) NULL AFTER `ShowBillPayFromAccPopup`;

ALTER TABLE `idtype` 
ADD COLUMN `createdby` VARCHAR(50) NULL DEFAULT NULL AFTER `IDName`,
ADD COLUMN `modifiedby` VARCHAR(50) NULL DEFAULT NULL AFTER `createdby`,
ADD COLUMN `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `modifiedby`,
ADD COLUMN `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `createdts`,
ADD COLUMN `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `lastmodifiedts`,
ADD COLUMN `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0' AFTER `synctimestamp`;

ALTER TABLE `customer` ADD CONSTRAINT `FK_Customer_IDType`
  FOREIGN KEY (`IDType_id`)
  REFERENCES `idtype` (`IDType`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

Drop table configurations;
CREATE TABLE `configurationbundles` (
  `bundle_id` varchar(50) NOT NULL,
  `bundle_name` varchar(255) NOT NULL,
  `app_id` varchar(255) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`bundle_id`),
  UNIQUE KEY `bundle_name_UNIQUE` (`bundle_name`),
  UNIQUE KEY `bundle_id_UNIQUE` (`bundle_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `configurations` (
  `configuration_id` varchar(255) NOT NULL,
  `bundle_id` varchar(50) NOT NULL,
  `config_type` varchar(50) NOT NULL,
  `config_key` varchar(600) NOT NULL,
  `description` TEXT DEFAULT NULL,
  `config_value` TEXT DEFAULT NULL,
  `target` varchar(10) NOT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`configuration_id`),
  UNIQUE KEY `configuration_id_UNIQUE` (`configuration_id`),
  UNIQUE KEY `UK_bundleId_configKey` (`bundle_id`,`config_key`),
  KEY `FK_bundle_id_idx` (`bundle_id`),
  KEY `FK_config_type_idx` (`config_type`),
  CONSTRAINT `FK_bundle_id` FOREIGN KEY (`bundle_id`) REFERENCES `configurationbundles` (`bundle_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP VIEW IF EXISTS `configuration_view`;
CREATE VIEW `configuration_view` AS    SELECT `configurations`.`configuration_id` AS `configuration_id`, `configurations`.`bundle_id` AS `bundle_id`, `configurationbundles`.`bundle_name` AS `bundle_name`, `configurations`.`config_type` AS `type`, `configurations`.`config_key` AS `key`, `configurations`.`description` AS `description`, `configurations`.`config_value` AS `value`, `configurations`.`target` AS `target`, `configurationbundles`.`app_id` AS `app_id`    FROM (`configurations` LEFT JOIN `configurationbundles` ON ((`configurationbundles`.`bundle_id` = `configurations`.`bundle_id`)));

CREATE TABLE `bbcustomerservicelimit` (
  `id` VARCHAR(50) NOT NULL,
  `Customer_id` VARCHAR(50) NOT NULL,
  `Service_id` VARCHAR(50) NOT NULL,
  `MaxTransactionLimit` DECIMAL(20,2) DEFAULT NULL,
  `MaxDailyLimit` DECIMAL(20,2) DEFAULT NULL,
  `createdby` VARCHAR(50) DEFAULT NULL,
  `modifiedby` VARCHAR(50) DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `bbcustomerservicelimit` 
ADD CONSTRAINT `FK_BBCustomerServiceLimit_Customer`
  FOREIGN KEY (`Customer_id`)
  REFERENCES `customer` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE `bbcustomerservicelimit` 
ADD INDEX `FK_BBCustomerServiceLimit_Customer_idx` (`Customer_id` ASC);

ALTER TABLE `bbcustomerservicelimit` 
ADD CONSTRAINT `FK_BBCustomerServiceLimit_Service`
  FOREIGN KEY (`Service_id`)
  REFERENCES `service` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE `bbcustomerservicelimit` 
ADD INDEX `FK_BBCustomerServiceLimit_Service_idx` (`Service_id` ASC);

ALTER TABLE `customertype` 
ADD COLUMN `Name` VARCHAR(50) NULL AFTER `id`;

ALTER TABLE `customer` 
ADD COLUMN `Type_id` VARCHAR(50) NULL AFTER `Email`;

ALTER TABLE `customer` 
ADD CONSTRAINT `FK_Customer_Type_id`
  FOREIGN KEY (`Type_id`)
  REFERENCES `customertype` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE `membergroup` 
ADD COLUMN `Type_id` VARCHAR(50) NULL AFTER `Description`;

ALTER TABLE `membergroup` 
ADD CONSTRAINT `FK_MemberGroup_Type_id`
  FOREIGN KEY (`Type_id`)
  REFERENCES `customertype` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
