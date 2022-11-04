--
-- Dumping routines for database 'konyadminconsole'
--
/*!50003 DROP PROCEDURE IF EXISTS `archived_request_search_proc` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;

CREATE PROCEDURE [konyadminconsole].[archived_request_search_proc](
    @_dateInitialPoint nvarchar(50), @_dateFinalPoint nvarchar(50), @_requestStatusID nvarchar(50),
    @_requestCategory nvarchar(50), @_offset nvarchar(50), @_sortCriteria nvarchar(50), @_sortOrder nvarchar(50),
    @_requestAssignedTo nvarchar(50), @_searchKey nvarchar(50), @_messageRepliedBy nvarchar(50),
    @_queryType nvarchar(50)
)
AS
BEGIN
SET NOCOUNT ON;
 DECLARE @selectClause varchar(max);
 DECLARE @stmt nvarchar(max);
 DECLARE @whereclause varchar(max);
 DECLARE @joinCustomer varchar(max);
 DECLARE @sortColumn varchar(max);
 IF @_queryType = 'count'
  SET @selectClause = 'count(acr.id) AS cnt';
ELSE
  SET @selectClause = 'acr.id AS customerrequest_id';

SET  @stmt = CONCAT('SELECT ', @selectClause, ' FROM konyadminconsole.archivedcustomerrequest acr ');
SET  @whereclause = ' WHERE 1=1';
SET  @joinCustomer=0;

IF @_dateInitialPoint != '' AND @_dateFinalPoint != ''
                BEGIN SET @whereclause=CONCAT(@whereclause, ' AND acr.createdts >= ''', @_dateInitialPoint, ''' AND ', ' acr.createdts <= ''', @_dateFinalPoint, '''');
END
ELSE IF @_dateInitialPoint != ''
                BEGIN SET @whereclause=CONCAT(@whereclause, ' AND convert(varchar(10), acr.createdts,121) >= convert(varchar(10),''', @_dateInitialPoint, ''',121)');
END
ELSE IF @_dateFinalPoint != ''
                BEGIN SET @whereclause=CONCAT(@whereclause, ' AND convert(varchar(10), acr.createdts,121) <= convert(varchar(10),''', @_dateFinalPoint, ''',121)'); 
END 

IF @_requestStatusID != '' BEGIN
                SET @whereclause=CONCAT(@whereclause, ' AND acr.Status_id IN (''', @_requestStatusID, ''')');
END 

IF @_requestCategory != '' BEGIN
                SET @whereclause=CONCAT(@whereclause, ' AND acr.RequestCategory_id = ''', @_requestCategory, '''');
END 

IF @_requestAssignedTo != '' BEGIN
                SET @whereclause=CONCAT(@whereclause, ' AND acr.AssignedTo = ''', @_requestAssignedTo, '''');
END 

IF @_messageRepliedBy != '' BEGIN
                SET @stmt=CONCAT(@stmt, 'LEFT JOIN archivedrequestmessage ON (acr.id = archivedrequestmessage.CustomerRequest_id) ');
                SET @whereclause=CONCAT(@whereclause, ' AND archivedrequestmessage.RepliedBy = '', @_messageRepliedBy, ');
END 

IF @_searchKey != '' BEGIN
                SET @joinCustomer=1;
                SET @whereclause=CONCAT(@whereclause,
                                ' AND (acr.Customer_id LIKE %', @_searchKey,
        '% OR acr.id LIKE %', @_searchKey,
        '% OR customer.Username LIKE %', @_searchKey,
        '%'')'
                );
END 

IF @_queryType != 'count' BEGIN
                IF @_sortCriteria='customer_Fullname' BEGIN
                                                SET @joinCustomer=1;
                END 
    
    SET  @sortColumn=CASE WHEN @_sortCriteria='customerrequest_AssignedTo' THEN  'acr.AssignedTo' ELSE   @_sortCriteria END;
                SET @sortColumn=CASE WHEN @_sortCriteria='customerrequest_createdts' THEN  'acr.createdts' ELSE   @sortColumn END;
                SET @sortColumn=CASE WHEN @_sortCriteria='customerrequest_RequestCategory_id' THEN  'acr.RequestCategory_id' ELSE   @sortColumn END;
                SET @sortColumn=CASE WHEN @_sortCriteria='customer_Fullname' THEN  'concat(customer.FirstName, customer.LastName)' ELSE   @sortColumn END;
                SET @sortColumn=CASE WHEN @_sortCriteria='customerrequest_Status_id' THEN  'acr.Status_id' ELSE   @sortColumn END;
                SET @sortColumn=CASE WHEN @_sortCriteria='customerrequest_AssignedTo_Name' THEN  'CONCAT(systemuser.FirstName,systemuser.LastName)' ELSE   @sortColumn END;
                
    SET @whereclause=CONCAT(@whereclause, ' ORDER BY ', CASE WHEN @sortColumn = '' THEN  'acr.lastmodifiedts' ELSE  @sortColumn END, ' ', CASE WHEN @sortColumn = '' OR @_sortOrder = '' THEN  'DESC' ELSE  @_sortOrder END);
    SET @whereclause=CONCAT(@whereclause, ' LIMIT ', CASE WHEN @_offset = '' THEN  '0' ELSE  @_offset END, ', 10');
END 

IF @joinCustomer=1 BEGIN
                SET @stmt=CONCAT(@stmt, 'LEFT JOIN customer ON (acr.Customer_id = customer.id) ');
END 

IF @_sortCriteria='customerrequest_AssignedTo_Name' BEGIN              
            SET @stmt=CONCAT(@stmt, 'LEFT JOIN systemuser ON (acr.AssignedTo = systemuser.id) ');        
END 
                

SET @stmt = CONCAT(@stmt, @whereclause);

SELECT @stmt;
execute(@stmt);
END ;

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [konyadminconsole].[customer_request_archive_proc]
AS 
   BEGIN

      SET  XACT_ABORT  ON
      SET  NOCOUNT  ON
      DECLARE
         @customer_request_cursor_done bit 
      DECLARE
         @customer_requestmessage_cursor_done bit 
      DECLARE
         @customer_requestmessageattachment_cursor_done bit 
      DECLARE
         @curr_request_id nvarchar(50) = N''
      DECLARE
         @curr_requestmessage_id nvarchar(50) = N''
      DECLARE
         @curr_media_id nvarchar(50) = N''
      DECLARE
         @curr_messageattachment_id nvarchar(50) = N'0'

      DECLARE
         @media_count int = 0
		 CREATE TABLE  #TEMP(cID INT IDENTITY(1,1),CURCOLUMN NVARCHAR(MAX))
		 INSERT INTO #TEMP 
		 SELECT id FROM konyadminconsole.customerrequest WHERE Status_id = 'SID_RESOLVED' and 
DATEDIFF(SECOND, Getdate(), lastmodifiedts)<=15552000
--SELECT * FROM #TEMP 
--DROP TABLE #TEMP

		 DECLARE @MIN_CID INT SET @MIN_CID = (SELECT MIN(cid) FROM #TEMP) 

DECLARE @MAX_CID INT SET @MAX_CID = (SELECT MAX(cid) FROM #TEMP) 
 
 

WHILE @MIN_CID <= @MAX_CID
      
         BEGIN
		 print '1'
         
               SET @customer_request_cursor_done = 0
               SET @customer_requestmessage_cursor_done = 0x1
               SET @customer_requestmessageattachment_cursor_done = 0x1
			    print '3'
            IF NOT @customer_request_cursor_done <> 0
               BEGIN
			   print '2'
                  INSERT archivedcustomerrequest(
                     id, 
                     RequestCategory_id, 
                     Customer_id, 
                     Priority, 
                     Status_id, 
                     RequestSubject, 
                     AssignedTo, 
                     Accountid, 
                     lastupdatedbycustomer, 
                     createdby, 
                     modifiedby, 
                     createdts, 
                     lastmodifiedts, 
                     synctimestamp, 
                     softdeleteflag)
                     SELECT 
                        konyadminconsole.customerrequest.id, 
                        konyadminconsole.customerrequest.RequestCategory_id, 
                        konyadminconsole.customerrequest.Customer_id, 
                        konyadminconsole.customerrequest.Priority, 
                        konyadminconsole.customerrequest.Status_id, 
                        konyadminconsole.customerrequest.RequestSubject, 
                        konyadminconsole.customerrequest.AssignedTo, 
                        konyadminconsole.customerrequest.Accountid, 
                        konyadminconsole.customerrequest.lastupdatedbycustomer, 
                        konyadminconsole.customerrequest.createdby, 
                        konyadminconsole.customerrequest.modifiedby, 
                        konyadminconsole.customerrequest.createdts, 
                        konyadminconsole.customerrequest.lastmodifiedts, 
                        konyadminconsole.customerrequest.synctimestamp, 
                        konyadminconsole.customerrequest.softdeleteflag
                     FROM konyadminconsole.customerrequest
                     WHERE konyadminconsole.customerrequest.id = @curr_request_id
					 print '4'
                  SELECT @curr_request_id
             

			     BEGIN

                     DECLARE
                         customer_requestmessage_cursor CURSOR LOCAL FORWARD_ONLY FOR 
                           SELECT konyadminconsole.requestmessage.id
                           FROM konyadminconsole.requestmessage
                           WHERE konyadminconsole.requestmessage.CustomerRequest_id = @curr_request_id

                     OPEN customer_requestmessage_cursor
                     WHILE (1 = 1)
                        BEGIN
                           FETCH customer_requestmessage_cursor
                               INTO @curr_requestmessage_id
                           IF @@FETCH_STATUS <> 0
                              SET @customer_request_cursor_done = 0x1
                              SET @customer_requestmessage_cursor_done = 0x1
                              SET @customer_requestmessageattachment_cursor_done = 0x1

                           IF NOT @customer_requestmessage_cursor_done <> 0
                              BEGIN

                                 INSERT archivedrequestmessage(
                                    id, 
                                    CustomerRequest_id, 
                                    MessageDescription, 
                                    RepliedBy, 
                                    RepliedBy_Name, 
                                    ReplySequence, 
                                    IsRead, 
                                    createdby, 
                                    modifiedby, 
                                    createdts, 
                                    lastmodifiedts, 
                                    synctimestamp, 
                                    softdeleteflag)
                                    SELECT 
                                       konyadminconsole.requestmessage.id, 
                                       konyadminconsole.requestmessage.CustomerRequest_id, 
                                       konyadminconsole.requestmessage.MessageDescription, 
                                       konyadminconsole.requestmessage.RepliedBy, 
                                       konyadminconsole.requestmessage.RepliedBy_Name, 
                                       konyadminconsole.requestmessage.ReplySequence, 
                                       konyadminconsole.requestmessage.IsRead, 
                                       konyadminconsole.requestmessage.createdby, 
                                       konyadminconsole.requestmessage.modifiedby, 
                                       konyadminconsole.requestmessage.createdts, 
                                       konyadminconsole.requestmessage.lastmodifiedts, 
                                       konyadminconsole.requestmessage.synctimestamp, 
                                       konyadminconsole.requestmessage.softdeleteflag
                                    FROM konyadminconsole.requestmessage
                                    WHERE konyadminconsole.requestmessage.id = @curr_requestmessage_id
                                 BEGIN

                                    DECLARE
                                        customer_messageattachment_cursor CURSOR LOCAL FORWARD_ONLY FOR 
                                          SELECT konyadminconsole.messageattachment.id
                                          FROM konyadminconsole.messageattachment
                                          WHERE konyadminconsole.messageattachment.RequestMessage_id = @curr_requestmessage_id

                                    OPEN customer_messageattachment_cursor
                                    WHILE (1 = 1)
                                       BEGIN
                                          FETCH customer_messageattachment_cursor
                                              INTO @curr_messageattachment_id
                                          IF @@FETCH_STATUS <> 0
                                             SET @customer_request_cursor_done = 0x1
                                             SET @customer_requestmessage_cursor_done = 0x1
                                             SET @customer_requestmessageattachment_cursor_done = 0x1

                                          IF NOT @customer_requestmessageattachment_cursor_done <> 0
                                             BEGIN

                                                SELECT @curr_media_id = messageattachment.Media_id
                                                FROM konyadminconsole.messageattachment
                                                WHERE konyadminconsole.messageattachment.id = @curr_messageattachment_id

                                                SELECT @media_count = count_big(archivedmedia.id)
                                                FROM konyadminconsole.archivedmedia
                                                WHERE konyadminconsole.archivedmedia.id = @curr_media_id

                                                IF @media_count = 0
                                                   INSERT archivedmedia(
                                                      id, 
                                                      Name, 
                                                      Type, 
                                                      Description, 
                                                      Url, 
                                                      Content, 
                                                      Size, 
                                                      createdby, 
                                                      modifiedby, 
                                                      createdts, 
                                                      lastmodifiedts, 
                                                      synctimestamp, 
                                                      softdeleteflag)
                                                      SELECT 
                                                         konyadminconsole.media.id, 
                                                         konyadminconsole.media.Name, 
                                                         konyadminconsole.media.Type, 
                                                         konyadminconsole.media.Description, 
                                                         konyadminconsole.media.Url, 
                                                         konyadminconsole.media.Content, 
                                                         konyadminconsole.media.Size, 
                                                         konyadminconsole.media.createdby, 
                                                         konyadminconsole.media.modifiedby, 
                                                         konyadminconsole.media.createdts, 
                                                         konyadminconsole.media.lastmodifiedts, 
                                                         konyadminconsole.media.synctimestamp, 
                                                         konyadminconsole.media.softdeleteflag
                                                      FROM konyadminconsole.media
                                                      WHERE konyadminconsole.media.id = @curr_media_id

                                                INSERT archivedmessageattachment(
                                                   id, 
                                                   RequestMessage_id, 
                                                   AttachmentType_id, 
                                                   Media_id, 
                                                   createdby, 
                                                   modifiedby, 
                                                   createdts, 
                                                   lastmodifiedts, 
                                                   synctimestamp, 
                                                   softdeleteflag)
                                                   SELECT 
                                                      konyadminconsole.messageattachment.id, 
                                                      konyadminconsole.messageattachment.RequestMessage_id, 
                                                      konyadminconsole.messageattachment.AttachmentType_id, 
                                                      konyadminconsole.messageattachment.Media_id, 
                                                      konyadminconsole.messageattachment.createdby, 
                                                      konyadminconsole.messageattachment.modifiedby, 
                                                      konyadminconsole.messageattachment.createdts, 
                                                      konyadminconsole.messageattachment.lastmodifiedts, 
                                                      konyadminconsole.messageattachment.synctimestamp, 
                                                      konyadminconsole.messageattachment.softdeleteflag
                                                   FROM konyadminconsole.messageattachment
                                                   WHERE konyadminconsole.messageattachment.id = @curr_messageattachment_id

                                                DELETE 
                                                FROM konyadminconsole.messageattachment
                                                WHERE konyadminconsole.messageattachment.id = @curr_messageattachment_id

                                                SELECT @media_count = count_big(messageattachment.id)
                                                FROM konyadminconsole.messageattachment
                                                WHERE konyadminconsole.messageattachment.Media_id = @curr_media_id

                                                IF @media_count = 0
                                                   DELETE 
                                                   FROM konyadminconsole.media
                                                   WHERE konyadminconsole.media.id = @curr_media_id

                                             END

                                          IF @customer_requestmessageattachment_cursor_done <> 0
                                             BREAK

                                       END

                                    SET @customer_request_cursor_done = 0x0
                                    SET @customer_requestmessage_cursor_done = 0x0
                                    SET @customer_requestmessageattachment_cursor_done = 0x0
                                    CLOSE customer_messageattachment_cursor
                                    DEALLOCATE customer_messageattachment_cursor

                                 END

                                 DELETE 
                                 FROM konyadminconsole.requestmessage
                                 WHERE konyadminconsole.requestmessage.id = @curr_requestmessage_id

                              END

                           IF @customer_requestmessage_cursor_done <> 0
                              BREAK

                        END

                     SET @customer_request_cursor_done = 0x0
                     SET @customer_requestmessage_cursor_done = 0x0
                     SET @customer_requestmessageattachment_cursor_done = 0x0
                     CLOSE customer_requestmessage_cursor
                     DEALLOCATE customer_requestmessage_cursor
                  END


                  DELETE 
                  FROM konyadminconsole.customerrequest
                  WHERE konyadminconsole.customerrequest.id = @curr_request_id

               END

            IF @customer_request_cursor_done <> 0
               BREAK
 SET @MIN_CID = @MIN_CID + 1
         END
		
      SET @customer_request_cursor_done = 0x0
      SET @customer_requestmessage_cursor_done = 0x0
      SET @customer_requestmessageattachment_cursor_done = 0x0
   END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `customer_request_search_proc` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
CREATE PROCEDURE [konyadminconsole].[customer_request_search_proc](
    @_dateInitialPoint nvarchar(50), @_dateFinalPoint nvarchar(50), @_requestStatusID nvarchar(50),
    @_requestCategory nvarchar(50), @_offset nvarchar(50), @_sortCriteria nvarchar(50), @_sortOrder nvarchar(50),
    @_requestAssignedTo nvarchar(50), @_searchKey nvarchar(50), @_messageRepliedBy nvarchar(50),
    @_queryType nvarchar(50)
)
AS
BEGIN
SET NOCOUNT ON;

DECLARE @selectClause varchar(max);
 DECLARE @stmt nvarchar(max);
 DECLARE @whereclause varchar(max);
 DECLARE @joinCustomer varchar(max);
 DECLARE @sortColumn varchar(max);
 IF @_queryType = 'count'
  SET @selectClause = 'count(cr.id) AS cnt';
ELSE
  SET @selectClause = 'cr.id AS customerrequest_id';
SET @stmt = CONCAT('SELECT ', @selectClause, ' FROM konyadminconsole.customerrequest cr ');
SET @whereclause = ' WHERE 1=1';
SET @joinCustomer=0;

IF @_dateInitialPoint != '' AND @_dateFinalPoint != ''
                BEGIN SET @whereclause=CONCAT(@whereclause, ' AND acr.createdts >= ''', @_dateInitialPoint, ''' AND ', ' acr.createdts <= ''', @_dateFinalPoint, '''');
END
ELSE IF @_dateInitialPoint != ''
                BEGIN SET @whereclause=CONCAT(@whereclause, ' AND convert(varchar(10), acr.createdts,121) >= convert(varchar(10),''', @_dateInitialPoint, ''',121)');
END
ELSE IF @_dateFinalPoint != ''
                BEGIN SET @whereclause=CONCAT(@whereclause, ' AND convert(varchar(10), acr.createdts,121) <= convert(varchar(10),''', @_dateFinalPoint, ''',121)'); 
END 

IF @_requestStatusID != '' BEGIN
                SET @whereclause=CONCAT(@whereclause, ' AND cr.Status_id IN (''', @_requestStatusID, ''')');
END 

IF @_requestCategory != '' BEGIN
                SET @whereclause=CONCAT(@whereclause, ' AND cr.RequestCategory_id = ''', @_requestCategory, '''');
END 

IF @_requestAssignedTo != '' BEGIN
                SET @whereclause=CONCAT(@whereclause, ' AND cr.AssignedTo = ''', @_requestAssignedTo, '''');
END 

IF @_messageRepliedBy != '' BEGIN
                SET @stmt=CONCAT(@stmt, 'LEFT JOIN konyadminconsole.requestmessage ON (cr.id = konyadminconsole.requestmessage.CustomerRequest_id) ');
                SET @whereclause=CONCAT(@whereclause, ' AND konyadminconsole.requestmessage.RepliedBy = '', @_messageRepliedBy, ');
END 

IF @_searchKey != '' BEGIN
                SET @joinCustomer=1;
                SET @whereclause=CONCAT(@whereclause,
                                ' AND (cr.Customer_id LIKE %', @_searchKey,        
        '% OR cr.id LIKE %', @_searchKey,
        '% OR konyadminconsole.customer.Username LIKE %', @_searchKey,
        '%'')'
                );
END 

IF @_queryType != 'count' BEGIN
                IF @_sortCriteria='customer_Fullname' BEGIN
                                                SET @joinCustomer=1;
                END 
    
    SET  @sortColumn=CASE WHEN @_sortCriteria='customerrequest_AssignedTo' THEN  'cr.AssignedTo' ELSE   @_sortCriteria END;
                SET @sortColumn=CASE WHEN @_sortCriteria='customerrequest_createdts' THEN  'cr.createdts' ELSE   @sortColumn END;
                SET @sortColumn=CASE WHEN @_sortCriteria='customerrequest_RequestCategory_id' THEN  'cr.RequestCategory_id' ELSE   @sortColumn END;
                SET @sortColumn=CASE WHEN @_sortCriteria='customer_Fullname' THEN  'CONCAT(konyadminconsole.customer.FirstName,konyadminconsole.customer.LastName)' ELSE   @sortColumn END;
                SET @sortColumn=CASE WHEN @_sortCriteria='customerrequest_Status_id' THEN  'cr.Status_id' ELSE   @sortColumn END;
                SET @sortColumn=CASE WHEN @_sortCriteria='customerrequest_AssignedTo_Name' THEN  'CONCAT(konyadminconsole.systemuser.FirstName,konyadminconsole.systemuser.LastName)' ELSE   @sortColumn END;
                
    SET @whereclause=CONCAT(@whereclause, ' ORDER BY ', CASE WHEN @sortColumn = '' THEN  'cr.lastmodifiedts' ELSE  @sortColumn END, ' ', CASE WHEN @sortColumn = '' OR @_sortOrder = '' THEN  'DESC' ELSE  @_sortOrder END);
    SET @whereclause=CONCAT(@whereclause, ' LIMIT ', CASE WHEN @_offset = '' THEN  '0' ELSE  @_offset END, ', 10');
END 

IF @joinCustomer=1 BEGIN
                SET @stmt=CONCAT(@stmt, 'LEFT JOIN konyadminconsole.customer ON (cr.Customer_id = customer.id) ');
END 

IF @_sortCriteria='customerrequest_AssignedTo_Name' BEGIN              
            SET @stmt=CONCAT(@stmt, 'LEFT JOIN konyadminconsole.systemuser ON (cr.AssignedTo = systemuser.id) ');         
END 
                

SET @stmt = CONCAT(@stmt, @whereclause);
execute(@stmt)
END
GO

DROP PROCEDURE konyadminconsole.customer_search_proc;
GO
CREATE PROCEDURE [konyadminconsole].[customer_search_proc]  
   @_searchType nvarchar(60),
   @_id nvarchar(50),
   @_name nvarchar(50),
   @_SSN nvarchar(50),
   @_username nvarchar(50),
   @_phone nvarchar(10),
   @_email nvarchar(50), 
   @_group nvarchar(40),
   @_requestID nvarchar(50),
   @_branchIDS nvarchar(2000),
   @_productIDS nvarchar(2000),
   @_cityIDS nvarchar(2000),
   @_entitlementIDS nvarchar(2000),
   @_groupIDS nvarchar(2000),
   @_customerStatus nvarchar(50),
   @_before nvarchar(20),
   @_after nvarchar(20),
   @_sortVariable nvarchar(100),
   @_sortDirection nvarchar(4),
   @_pageOffset bigint,
   @_pageSize bigint
AS 
   BEGIN

     declare @queryStatement nvarchar(max)
	 declare @whereclause nvarchar(max)
	 declare @ParmDefinition nvarchar(max)
	 
	 IF @_searchType = 'APPLICANT_SEARCH'
	 BEGIN
		SET @queryStatement =  concat('select konyadminconsole.applicant.id, konyadminconsole.applicant.FirstName,konyadminconsole.applicant.MiddleName, konyadminconsole.applicant.LastName,',
		'concat(konyadminconsole.applicant.FirstName,'' '', konyadminconsole.applicant.MiddleName, '' '' , konyadminconsole.applicant.LastName) as name,',
				'konyadminconsole.applicant.ContactNumber, konyadminconsole.applicant.EmailID, konyadminconsole.applicant.`Status` as Status_desc',
			'from applicant JOIN( select konyadminconsole.applicant.id from konyadminconsole.applicant where id like concat(''%'', ', @_id,',''%'')  and',
				'concat(firstname, middlename, lastname) like concat(''%'',',@_name,',''%'') and',
				'ISNULL(ContactNumber, '') like concat(''%'',', @_phone ,',''%'') and ',
				'ISNULL(EmailID, '') like concat(''%'',', @_email ,',''%'')',
				') paginatedApplicants ON (paginatedApplicants.id = konyadminconsole.applicant.id)',
				'Group by paginatedApplicants.id ');

		IF @_sortVariable = 'DEFAULT' or @_sortVariable = ''
			set @queryStatement = concat(@queryStatement, ' ORDER BY name');
		ELSE IF @_sortVariable != '' 
			set @queryStatement = concat(@queryStatement, ' ORDER BY ',@_sortVariable);
		IF @_sortDirection != '' 
			set @queryStatement = concat(@queryStatement, ' ',@_sortDirection);

		set @queryStatement = concat(@queryStatement, ' OFFSET ',@_pageOffset,' ROWS FETCH NEXT ',@_pageSize, ' ROWS ONLY');
		-- select @queryStatement;
		execute(@queryStatement);
	END
	ELSE IF @_searchType = 'APPLICANT_SEARCH_TOTAL_COUNT' 
	BEGIN
			SELECT sum(paginatedApplicants.applicant) as SearchMatchs from (
				select 1 as applicant
				from konyadminconsole.applicant
				where id like concat('%', @_id ,'%')  and
				concat(firstname, middlename, lastname) like concat('%',@_name,'%') and
				ISNULL(ContactNumber, '') like concat('%', @_phone ,'%') and 
				ISNULL(EmailID, '') like concat('%', @_email ,'%')
				) paginatedApplicants;
	END
	ELSE IF @_searchType = 'GROUP_SEARCH'
	BEGIN
				set @queryStatement = 'SELECT 
					konyadminconsole.customer.id, konyadminconsole.customer.FirstName, konyadminconsole.customer.MiddleName, konyadminconsole.customer.LastName,
					concat(konyadminconsole.customer.FirstName, '' '', konyadminconsole.customer.MiddleName, '' '', konyadminconsole.customer. LastName) as name,
					konyadminconsole.customer.Username, konyadminconsole.customer.Salutation, konyadminconsole.customer.Gender,
					konyadminconsole.address.City_id, konyadminconsole.city.Name as City_name,
					konyadminconsole.status.Description as customer_status,
					konyadminconsole.customer.Location_id AS branch_id,
					konyadminconsole.location.Name AS branch_name
				FROM konyadminconsole.customer
                    JOIN (
						SELECT
							konyadminconsole.customer.id
						FROM
							konyadminconsole.customer
                            LEFT JOIN konyadminconsole.customerrequest ON (konyadminconsole.customerrequest.customer_id=konyadminconsole.customer.id)
							LEFT JOIN konyadminconsole.customergroup ON (konyadminconsole.customergroup.Customer_id=konyadminconsole.customer.id)
							LEFT JOIN konyadminconsole.membergroup ON (konyadminconsole.membergroup.id=konyadminconsole.customergroup.group_id)
							LEFT JOIN konyadminconsole.customeraddress ON (konyadminconsole.customer.id=konyadminconsole.customeraddress.Customer_id AND konyadminconsole.customeraddress.isPrimary=1 and konyadminconsole.customeraddress.Type_id=''ADR_TYPE_HOME'')
							LEFT JOIN konyadminconsole.address ON (konyadminconsole.customeraddress.Address_id = konyadminconsole.address.id)
							LEFT JOIN konyadminconsole.city ON (konyadminconsole.city.id = konyadminconsole.address.City_id)
							LEFT JOIN konyadminconsole.customerentitlement ON (konyadminconsole.customerentitlement.Customer_id=konyadminconsole.customer.id)
							LEFT JOIN konyadminconsole.customerproduct ON (konyadminconsole.customerproduct.Customer_id=konyadminconsole.customer.id)
							LEFT JOIN konyadminconsole.status ON (konyadminconsole.status.id=konyadminconsole.customer.Status_id)
							LEFT JOIN konyadminconsole.location ON (konyadminconsole.location.id=konyadminconsole.customer.Location_id) ';
			set @whereclause = 'WHERE true';
			
            IF @_username != ''
			BEGIN
					-- customer name
					set @whereclause = concat(@whereclause, ' AND (concat(konyadminconsole.customer.firstname,'' '', konyadminconsole.customer.middlename, '' '', konyadminconsole.customer.lastname) like concat(''%'',',@_username,',''%'')');
					
					-- customer username
					set @whereclause = concat(@whereclause,' OR konyadminconsole.customer.username like concat(''%'', ',@_username,' ,''%'')');
					
					-- customer id
					set @whereclause = concat(@whereclause,' OR konyadminconsole.customer.id like concat(''%'',', @_username ,',''%'')) ');
			END
			-- customer entitlement ids
			if @_entitlementIDS != '' 
			BEGIN
							set @whereclause = concat(@whereclause,' AND konyadminconsole.customerentitlement.Service_id in (',@_entitlementIDS,') ');
			END
			
			-- customer group ids
			if @_groupIDS != '' 
			BEGIN
							set @whereclause = concat(@whereclause,' AND konyadminconsole.customergroup.Group_id in (',@_groupIDS,') ');
			END
			
			-- customer product ids
			if @_productIDS != '' 
			BEGIN
							set @whereclause = concat(@whereclause,' AND konyadminconsole.customerproduct.Product_id in (',@_productIDS,')');
			END
			
			-- customer branch ids
			if @_branchIDS != '' 
							set @whereclause = concat(@whereclause, ' AND konyadminconsole.customer.Location_id in (',@_branchIDS,')');
			
			-- customer status
			if @_customerStatus != '' 
			BEGIN
							set @whereclause = concat(@whereclause, ' AND konyadminconsole.status.Description like concat(''%'',', @_customerStatus ,',''%'')');
			END
			
			-- customer city ids
			if @_cityIDS != '' 
			BEGIN
							set @whereclause = concat(@whereclause, ' AND konyadminconsole.address.City_id in (',@_cityIDS,')');
			END
			
			-- customer date range search
			if @_before != '' and @_after != '' 
							set @whereclause = concat(@whereclause, ' AND date(konyadminconsole.customer.createdts) >= date ''', @_before ,''' and date(konyadminconsole.customer.createdts) <= date ''', @_after ,'''');
			else 
			BEGIN
				if @_before != '' 
							set @whereclause = concat(@whereclause, ' AND date(konyadminconsole.customer.createdts) <= date ''', @_before ,'''');
				else if @_after != '' 
							set @whereclause = concat(@whereclause, ' AND date(konyadminconsole.customer.createdts) >= date ''', @_after ,'''');
			END
			
			set @queryStatement = concat(@queryStatement, @whereclause, '  GROUP BY konyadminconsole.customer.id ',
            ') konyadminconsole.paginatedCustomers ON (konyadminconsole.paginatedCustomers.id=konyadminconsole.customer.id)
					
					LEFT JOIN konyadminconsole.customerrequest ON (konyadminconsole.customerrequest.customer_id=konyadminconsole.paginatedCustomers.id)
					LEFT JOIN konyadminconsole.customergroup ON (konyadminconsole.customergroup.Customer_id=konyadminconsole.paginatedCustomers.id)
					LEFT JOIN konyadminconsole.membergroup ON (konyadminconsole.membergroup.id=konyadminconsole.customergroup.group_id)
					LEFT JOIN konyadminconsole.customeraddress ON (konyadminconsole.paginatedCustomers.id=konyadminconsole.customeraddress.Customer_id AND konyadminconsole.customeraddress.isPrimary=1 and konyadminconsole.customeraddress.Type_id=''ADR_TYPE_HOME'')
					LEFT JOIN konyadminconsole.address ON (konyadminconsole.customeraddress.Address_id = konyadminconsole.address.id)
					LEFT JOIN konyadminconsole.city ON (konyadminconsole.city.id = konyadminconsole.address.City_id)
					LEFT JOIN konyadminconsole.customerentitlement ON (konyadminconsole.customerentitlement.Customer_id=konyadminconsole.paginatedCustomers.id)
					LEFT JOIN konyadminconsole.customerproduct ON (konyadminconsole.customerproduct.Customer_id=konyadminconsole.paginatedCustomers.id)
					LEFT JOIN konyadminconsole.status ON (konyadminconsole.status.id=konyadminconsole.customer.Status_id)
					LEFT JOIN konyadminconsole.location ON (konyadminconsole.location.id=konyadminconsole.customer.Location_id)  
			GROUP BY konyadminconsole.paginatedCustomers.id');
			
            IF @_sortVariable = 'DEFAULT' or @_sortVariable = ''
				set @queryStatement = concat(@queryStatement, ' ORDER BY name');
            ELSE IF @_sortVariable != ''
				set @queryStatement = concat(@queryStatement, ' ORDER BY ',@_sortVariable);
            IF @_sortDirection != ''
				set @queryStatement = concat(@queryStatement, ' ',@_sortDirection);

            set @queryStatement = concat(@queryStatement, ' OFFSET ',@_pageOffset,' ROWS FETCH NEXT ',@_pageSize, ' ROWS ONLY');
			execute(@queryStatement);
		END
		ELSE IF @_searchType = 'GROUP_SEARCH_TOTAL_COUNT'
		BEGIN
				set @queryStatement = '
					SELECT SUM(konyadminconsole.customerSearch.matchingCustomer) as SearchMatchs from 
                        (SELECT
							1 as matchingCustomer
						FROM
							konyadminconsole.customer
                            LEFT JOIN konyadminconsole.customerrequest ON (konyadminconsole.customerrequest.customer_id=konyadminconsole.customer.id)
							LEFT JOIN konyadminconsole.customergroup ON (konyadminconsole.customergroup.Customer_id=konyadminconsole.customer.id)
							LEFT JOIN konyadminconsole.membergroup ON (konyadminconsole.membergroup.id=konyadminconsole.customergroup.group_id)
							LEFT JOIN konyadminconsole.customeraddress ON (konyadminconsole.customer.id=konyadminconsole.customeraddress.Customer_id AND konyadminconsole.customeraddress.isPrimary=1 and konyadminconsole.customeraddress.Type_id=''ADR_TYPE_HOME'')
							LEFT JOIN konyadminconsole.address ON (konyadminconsole.customeraddress.Address_id = konyadminconsole.address.id)
							LEFT JOIN konyadminconsole.city ON (konyadminconsole.city.id = konyadminconsole.address.City_id)
							LEFT JOIN konyadminconsole.customerentitlement ON (konyadminconsole.customerentitlement.Customer_id=konyadminconsole.customer.id)
							LEFT JOIN konyadminconsole.customerproduct ON (konyadminconsole.customerproduct.Customer_id=konyadminconsole.customer.id)
							LEFT JOIN konyadminconsole.status ON (konyadminconsole.status.id=konyadminconsole.customer.Status_id)
							LEFT JOIN konyadminconsole.location ON (konyadminconsole.location.id=konyadminconsole.customer.Location_id) ';
			set @whereclause = 'WHERE true';
			
            IF @_username != ''
			BEGIN
					-- customer name
					set @whereclause = concat(@whereclause, ' AND (concat(konyadminconsole.customer.firstname, '' '', konyadminconsole.customer.middlename, '' '', konyadminconsole.customer.lastname) like concat(''%'',''',@_username,''',''%'')');
					
					-- customer username
					set @whereclause = concat(@whereclause,' OR konyadminconsole.customer.username like concat(''%'', ''',@_username,''' ,''%'')');
					
					-- customer id
					set @whereclause = concat(@whereclause,' OR konyadminconsole.customer.id like concat(''%'',''', @_username ,''',''%'')) ');
			END
			-- customer entitlement ids
			if @_entitlementIDS != ''
					set @whereclause = concat(@whereclause,' AND konyadminconsole.customerentitlement.Service_id in (',@_entitlementIDS,') ');

			-- customer group ids
			if @_groupIDS != ''
					set @whereclause = concat(@whereclause,' AND konyadminconsole.customergroup.Group_id in (',@_groupIDS,') ');
			
			-- customer product ids
			if @_productIDS != ''
							set @whereclause = concat(@whereclause,' AND konyadminconsole.customerproduct.Product_id in (',@_productIDS,')');
			
			-- customer branch ids
			if @_branchIDS != ''
							set @whereclause = concat(@whereclause, ' AND konyadminconsole.customer.Location_id in (',@_branchIDS,')');
			
			-- customer status
			if @_customerStatus != '' 
			BEGIN
							set @whereclause = concat(@whereclause, ' AND konyadminconsole.status.Description like concat(''%'',', @_customerStatus ,',''%'')');
			END
			
			-- customer city ids
			if @_cityIDS != '' 
			BEGIN
							set @whereclause = concat(@whereclause, ' AND konyadminconsole.address.City_id in (',@_cityIDS,')');
			END
			
			-- customer date range search
			if @_before != '' and @_after != '' 
							set @whereclause = concat(@whereclause, ' AND date(konyadminconsole.customer.createdts) >= date ''', @_before ,''' and date(konyadminconsole.customer.createdts) <= date ''', @_after ,'''');
			else 
			BEGIN
				if @_before != '' 
							set @whereclause = concat(@whereclause, ' AND date(konyadminconsole.customer.createdts) <= date ''', @_before ,'''');
				else if @_after != '' 
							set @whereclause = concat(@whereclause, ' AND date(konyadminconsole.customer.createdts) >= date ''', @_after ,'''');
			END
			
			set @queryStatement = concat(@queryStatement, @whereclause, '  GROUP BY konyadminconsole.customer.id) AS customerSearch ');
			execute(@queryStatement);
		END
		ELSE IF @_searchType = 'CUSTOMER_SEARCH'
		BEGIN
			set @queryStatement = '
			SELECT DISTINCT c1.id, c1.FirstName, c1.MiddleName, c1.LastName,
				concat(c1.FirstName, '' '', c1.MiddleName, '' '', c1.LastName) as name,
				c1.Username, c1.Salutation, c1.Gender,c1.Ssn,
				PrimaryPhone.value AS PrimaryPhoneNumber,
				PrimaryEmail.value AS PrimaryEmailAddress, c1.DateOfBirth
			FROM konyadminconsole.customer c1
			JOIN (
				SELECT
					c2.id
				FROM konyadminconsole.customer c2
					LEFT JOIN konyadminconsole.customercommunication PrimaryPhone ON (PrimaryPhone.Customer_id=c2.id AND PrimaryPhone.isPrimary=1 AND PrimaryPhone.Type_id=''COMM_TYPE_PHONE'')
					LEFT JOIN konyadminconsole.customercommunication PrimaryEmail ON (PrimaryEmail.Customer_id=c2.id AND PrimaryEmail.isPrimary=1 AND PrimaryEmail.Type_id=''COMM_TYPE_EMAIL'')
                    LEFT JOIN konyadminconsole.customergroup ON (konyadminconsole.customergroup.Customer_id = c2.id)
                    LEFT JOIN konyadminconsole.membergroup ON (konyadminconsole.membergroup.id = konyadminconsole.customergroup.group_id)
					LEFT JOIN konyadminconsole.customerrequest ON (konyadminconsole.customerrequest.customer_id = c2.id)
                WHERE c2.id like concat(''%'', @_id ,''%'')  AND
					concat(c2.firstname,'' '',c2.middlename,'' '',c2.lastname) like concat(''%'', @_name ,''%'') AND
					(@_SSN = '''' or c2.Ssn = @_SSN) AND
					(c2.username like concat(''%'', @_username ,''%'')) AND 
					ISNULL(PrimaryPhone.value, '''') like concat(''%'', @_phone ,''%'') AND 
					ISNULL(PrimaryEmail.value, '''') like concat(''%'', @_email ,''%'')
				group by c2.id
                HAVING 1=1';
                
                IF @_group != ''
					set @queryStatement = concat(@queryStatement,' AND cus_groups like concat(''%'', @_group ,''%'')');
                
                IF @_requestID != ''
					set @queryStatement = concat(@queryStatement,' AND requestids like concat(''%'', @_requestID ,''%'')');
                
                set @queryStatement = concat(@queryStatement,
                ') paginatedCustomers ON (paginatedCustomers.id = c1.id)
			LEFT JOIN konyadminconsole.customercommunication PrimaryPhone ON (PrimaryPhone.Customer_id=paginatedCustomers.id AND PrimaryPhone.isPrimary=1 AND PrimaryPhone.Type_id=''COMM_TYPE_PHONE'')
			LEFT JOIN konyadminconsole.customercommunication PrimaryEmail ON (PrimaryEmail.Customer_id=paginatedCustomers.id AND PrimaryEmail.isPrimary=1 AND PrimaryEmail.Type_id=''COMM_TYPE_EMAIL'')
			LEFT JOIN konyadminconsole.customergroup ON (konyadminconsole.customergroup.Customer_id=paginatedCustomers.id)
			LEFT JOIN konyadminconsole.membergroup ON (konyadminconsole.membergroup.id=konyadminconsole.customergroup.group_id)
            LEFT JOIN konyadminconsole.customerrequest ON (konyadminconsole.customerrequest.customer_id=c1.id)
            ');
            
            IF @_sortVariable = 'DEFAULT' or @_sortVariable = ''
				set @queryStatement = concat(@queryStatement, ' ORDER BY name');
            ELSE IF @_sortVariable != ''
				set @queryStatement = concat(@queryStatement, ' ORDER BY @_sortVariable');
            IF @_sortDirection != ''
				set @queryStatement = concat(@queryStatement, ' @_sortDirection');

			set @queryStatement = concat(@queryStatement, ' OFFSET @_pageOffset ROWS FETCH NEXT @_pageSize ROWS ONLY');
			-- select @queryStatement;
			
			set @ParmDefinition = N'@_id nvarchar(50), 
									@_name nvarchar(50), 
									@_SSN nvarchar(50),
									@_username nvarchar(50), 
									@_phone nvarchar(10), 
									@_email nvarchar(50),  
									@_group nvarchar(40), 
									@_requestID nvarchar(50), 
									@_sortVariable nvarchar(100), 
									@_sortDirection nvarchar(4), 
									@_pageOffset bigint, 
									@_pageSize bigint';

            EXECUTE sp_executesql @queryStatement, @ParmDefinition, 
			@_id = @_id, 
			@_name = @_name , 
			@_SSN = @_SSN , 
			@_username = @_username, 
			@_phone = @_phone, 
			@_email = @_email,  
			@_group = @_group, 
			@_requestID = @_requestID, 
			@_sortVariable = @_sortVariable,
			@_sortDirection = @_sortDirection,
			@_pageOffset = @_pageOffset, 
			@_pageSize = @_pageSize;


		END
		
		ELSE IF @_searchType = 'CUSTOMER_SEARCH_TOTAL_COUNT'
		BEGIN
			SELECT SUM(customersearch.matchingcustomer)  as SearchMatchs from 
				(
				SELECT 
					1 as matchingcustomer
				FROM
					konyadminconsole.customer c
						LEFT JOIN konyadminconsole.customercommunication PrimaryPhone ON (PrimaryPhone.customer_id = c.id AND PrimaryPhone.isPrimary = 1 AND PrimaryPhone.Type_id = 'COMM_TYPE_PHONE')
						LEFT JOIN konyadminconsole.customercommunication PrimaryEmail ON (PrimaryEmail.customer_id = c.id AND PrimaryEmail.isPrimary = 1 AND PrimaryEmail.Type_id = 'COMM_TYPE_EMAIL')
						LEFT JOIN konyadminconsole.customergroup ON (konyadminconsole.customergroup.customer_id = c.id)
                        LEFT JOIN konyadminconsole.membergroup ON (konyadminconsole.membergroup.id = konyadminconsole.customergroup.group_id)
						LEFT JOIN konyadminconsole.customerrequest ON (konyadminconsole.customerrequest.customer_id = c.id)
				WHERE
					c.id like concat('%',@_id,'%')  AND
					concat(c.firstname,' ',c.middlename,' ',c.lastname) like concat('%',@_name,'%') AND
					(concat('',@_SSN) = '' or c.Ssn = concat('',@_SSN)) AND
					(c.username like concat('%',@_username,'%')) AND 
					ISNULL(PrimaryPhone.value, '') like concat('%', @_phone ,'%') AND 
					ISNULL(PrimaryEmail.value, '') like concat('%', @_email ,'%')
				GROUP BY c.id
				) AS customersearch;
	END
END
GO

CREATE PROCEDURE [konyadminconsole].[location_details_proc]  @_locationId nchar(50)
AS 
   BEGIN
	  SET  XACT_ABORT  ON
	  SET  NOCOUNT  ON

      DECLARE @workingHours nvarchar(2000)
	  DECLARE @tempDayName nvarchar(2000)
	  DECLARE @tempStartTime nvarchar(2000)
	  DECLARE @tempEndTime nvarchar(2000)
	  DECLARE @tempServiceName nvarchar(2000)
	  DECLARE @servicesList nvarchar(max)
	  DECLARE @serviceName nvarchar(2000)
	  DECLARE @b int
	  DECLARE @pipeFlag int

	  DECLARE @a varchar(100)
	  DECLARE @wh varchar(100)
	  SET @a='';
	  SET @wh='';
	  DECLARE
          cur_1 CURSOR LOCAL FORWARD_ONLY FOR 
          SELECT konyadminconsole.dayschedule.WeekDayName, konyadminconsole.dayschedule.StartTime, konyadminconsole.dayschedule.EndTime
          FROM konyadminconsole.dayschedule, konyadminconsole.location
          WHERE konyadminconsole.location.WorkSchedule_id = konyadminconsole.dayschedule.WorkSchedule_id AND konyadminconsole.location.id = @_locationId

      OPEN cur_1

      WHILE (1 = 1)
      
         BEGIN

            SET @tempDayName = N''
			SET @tempStartTime = N''
			SET @tempEndTime = N''

            FETCH cur_1
                INTO @tempDayName, @tempStartTime, @tempEndTime

            IF @@FETCH_STATUS <> 0
               SET @b = 1

            IF @tempDayName <> ''
               /*BEGIN*/

			   IF @pipeFlag=0
			   
					SET @a=CONCAT(UPPER(LEFT(@tempDayName,1)), 
                             LOWER(SUBSTRING(@tempDayName, 2,10)),': ',SUBSTRING(@tempStartTime,1,5) ,'AM',' - ',SUBSTRING(@tempEndTime,1,5),'PM');
			   ELSE

					SET @a= (N' || ') + (upper(left(@tempDayName, 1))) + (lower(substring(@tempDayName, 2,10)))
                         + (N': ') + (substring(@tempStartTime, 1, 5)) + (N'AM') + (N' - ')
                         + (substring(@tempEndTime, 1, 5)) + (N'PM') ;

					SET @pipeFlag = 1;
					SET @wh = @wh + N' ' + @a + N'';

		/*	END*/


                  /* 
                  *   SSMA error messages:
                  *   M2SS0198: SSMA for MySQL  does not support user variables conversion

                  IF [@pipeFlag] = 0
                     /* 
                     *   SSMA error messages:
                     *   M2SS0198: SSMA for MySQL  does not support user variables conversion

                     SET [@a] = 
                        (upper(left(@tempDayName, 1)))
                         + 
                        (lower(m2ss.substring2(@tempDayName, 2)))
                         + 
                        (N': ')
                         + 
                        (substring(@tempStartTime, 1, 5))
                         + 
                        (N'AM')
                         + 
                        (N' - ')
                         + 
                        (substring(@tempEndTime, 1, 5))
                         + 
                        (N'PM')                     */


                  ELSE 
                     /* 
                     *   SSMA error messages:
                     *   M2SS0198: SSMA for MySQL  does not support user variables conversion

                     SET [@a] = 
                        (N' || ')
                         + 
                        (upper(left(@tempDayName, 1)))
                         + 
                        (lower(m2ss.substring2(@tempDayName, 2)))
                         + 
                        (N': ')
                         + 
                        (substring(@tempStartTime, 1, 5))
                         + 
                        (N'AM')
                         + 
                        (N' - ')
                         + 
                        (substring(@tempEndTime, 1, 5))
                         + 
                        (N'PM')                     */

                  */



                  /* 
                  *   SSMA error messages:
                  *   M2SS0198: SSMA for MySQL  does not support user variables conversion

                  SET [@pipeFlag] = 1                  */



                  



                  DECLARE
                     @db_null_statement int

            /*   END*/

            IF @b = 1
               BREAK

         END

      CLOSE cur_1

      DEALLOCATE cur_1

	   SET @workingHours = @wh;

	   SET @servicesList = 
         (
            SELECT NULL AS services
            FROM konyadminconsole.service, konyadminconsole.locationservice
            WHERE konyadminconsole.service.id = konyadminconsole.locationservice.Service_id AND konyadminconsole.locationservice.Location_id = @_locationId
         ) ;


      /* 
      *   SSMA error messages:
      *   M2SS0198: SSMA for MySQL  does not support user variables conversion

          */



      /* 
      *   SSMA error messages:
      *   M2SS0201: MySQL standard function GROUP_CONCAT is not supported in current SSMA version

      SET @servicesList = 
         (
            SELECT NULL AS services
            FROM konyadminconsole.service, konyadminconsole.locationservice
            WHERE service.id = locationservice.Service_id AND locationservice.Location_id = @_locationId
         )      */



      SELECT 
         konyadminconsole.location.id AS locationId, 
         konyadminconsole.location.Type_id AS type, 
         konyadminconsole.location.Name AS informationTitle, 
         CASE konyadminconsole.location.Status_id
            WHEN N'SID_ACTIVE' THEN N'OPEN'
            ELSE N'CLOSED'
         END AS status, 
         konyadminconsole.location.DisplayName AS displayName, 
         konyadminconsole.location.Description AS description, 
         konyadminconsole.location.PhoneNumber AS phone, 
         konyadminconsole.location.EmailId AS email, 
         konyadminconsole.location.WorkingDays AS workingDays, 
         konyadminconsole.location.IsMainBranch AS isMainBranch, 
         konyadminconsole.location.MainBranchCode AS mainBranchCode, 
         konyadminconsole.address.AddressLine1 AS addressLine1, 
         konyadminconsole.address.AddressLine2 AS addressLine2, 
         konyadminconsole.address.AddressLine3 AS addressLine3, 
         konyadminconsole.address.Latitude AS latitude, 
         konyadminconsole.address.Logitude AS longitude, 
         @workingHours AS workingHours, 
         @servicesList AS services
      FROM ((konyadminconsole.location 
         INNER JOIN konyadminconsole.address 
         ON ((konyadminconsole.location.Address_id = konyadminconsole.address.id))))
      WHERE konyadminconsole.location.id = @_locationId

   END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE   PROCEDURE [konyadminconsole].[location_range_proc]  
   @_currLatitude nvarchar(50),
   @_currLongitude nvarchar(50),
   @_radius int/*in Kms*/
AS 
   BEGIN

      SET  XACT_ABORT  ON

      SET  NOCOUNT  ON

      DECLARE
         @rowLatitide nvarchar(2000)

      DECLARE
         @rowLongitude nvarchar(2000)

      DECLARE
         @b int

      DECLARE
         @pipeFlag int

      /* 
      *   SSMA error messages:
      *   M2SS0201: MySQL standard function GROUP_CONCAT is not supported in current SSMA version
      *   M2SS0016: Identifier locationId cannot be converted because it was not resolved.*/

      SELECT 
         konyadminconsole.location.id AS locationId, 
         
            (
			null
               --SELECT NULL
               --FROM dayschedule, location
               --WHERE dayschedule.WorkSchedule_id = location.WorkSchedule_id AND location.id = location.id
            ) AS workingHours, 
         konyadminconsole.location.Name AS informationTitle, 
         konyadminconsole.location.PhoneNumber AS phone, 
         konyadminconsole.location.EmailId AS email, 
         CASE konyadminconsole.location.Status_id
            WHEN N'SID_ACTIVE' THEN N'OPEN'
            ELSE N'CLOSED'
         END AS status, 
         konyadminconsole.location.Type_id AS type, 
         
            (
               /* 
               *   SSMA error messages:
               *   M2SS0201: MySQL standard function GROUP_CONCAT is not supported in current SSMA version*/
			   null
               --SELECT NULL
               --FROM service, locationservice
               --WHERE service.id = locationservice.Service_id AND locationservice.Location_id = location.id               


            ) AS services, 
         
            (
               SELECT konyadminconsole.city.Name
               FROM konyadminconsole.city
               WHERE konyadminconsole.city.id = konyadminconsole.address.City_id
            ) AS city, 
         konyadminconsole.address.AddressLine1 AS addressLine1, 
         konyadminconsole.address.AddressLine2 AS addressLine2, 
         konyadminconsole.address.AddressLine3 AS addressLine3, 
         konyadminconsole.address.ZipCode AS zipCode, 
         konyadminconsole.address.Latitude AS latitude, 
         konyadminconsole.address.Logitude AS longitude--, 
         --(6371 * 2 * asin(sqrt(power(sin(((CAST(address.Latitude AS float(53))) - abs(CAST(@_currLatitude AS float(53)))) * pi() / 180 / 2), 2) + cos((CAST(address.Latitude AS float(53))) * pi() / 180) * cos(abs(CAST(@_currLatitude AS float(53))) * pi() / 180) * power(sin(((CAST(address.Logitude AS float(53))) - (CAST(@_currLongitude AS float(53)))) * pi() / 180 / 2), 2)))) AS distance
      FROM konyadminconsole.address, konyadminconsole.location
      WHERE konyadminconsole.address.id = konyadminconsole.location.Address_id
     -- and ((6371 * 2 * asin(sqrt(power(sin(((CAST(address.Latitude AS float(53))) - abs(CAST(@_currLatitude AS float(53)))) * pi() / 180 / 2), 2) + cos((CAST(address.Latitude AS float(53))) * pi() / 180) * cos(abs(CAST(@_currLatitude AS float(53))) * pi() / 180) * power(sin(((CAST(address.Logitude AS float(53))) - (CAST(@_currLongitude AS float(53)))) * pi() / 180 / 2), 2))))) <= @_radius      



   END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
--exec location_search_proc_mysql 'abc'


CREATE    PROCEDURE [konyadminconsole].[location_search_proc]( @_searchKeyword VARCHAR(max))
AS
BEGIN
SET NOCOUNT ON;
DECLARE @_next VARCHAR(max) = NULL;
DECLARE @_nextlen INT = NULL;
DECLARE @_value VARCHAR(max) = NULL;
DECLARE @sql1 VARCHAR(max) ;
DECLARE @sqlFrontPart VARCHAR(max) ;
DECLARE @sqlresult VARCHAR(max)  = null;
declare @counter int;
 declare @var nvarchar(max)

set @sqlFrontPart='SELECT * FROM locationdetails_view where';
  
set @counter=1;
iterator:
 WHILE @counter = 1
BEGIN

  IF LEN(RTRIM(LTRIM(@_searchKeyword))) = 0 OR @_searchKeyword IS NULL 
  BEGIN
--    LEAVE iterator;
 BREAK
  END 
   -- capture the next value from the list
  SET @_next = SUBSTRING(@_searchKeyword,1,1);
  -- save the length of the captured value; we will need to remove this
  -- many characters + 1 from the beginning of the string 
  -- before the next iteration
  SET @_nextlen = LEN(@_next);
  -- trim the value of leading and trailing spaces, in case of sloppy CSV strings
  SET @_value =  LOWER(RTRIM(LTRIM(@_next)));
  
   
  
   set @sql1=null;
   set @var=null;
   set @var=REPLACE (rtrim(ltrim(@_value)), ' ', '%');    
   set @sql1=concat('( 
    LOWER(city) like (','''',' % ',@var,' % ','''',') 
   or 
   LOWER(addressLine1) like (','''',' % ',@var,'%','''',')
   or
   LOWER(addressLine2) like (','''',' % ',@var,'%','''',')
    or
   LOWER(addressLine3) like (','''',' % ',@var,'%','''',')
   or
   LOWER(country) like (','''',' % ',@var,'%','''',') 
   or
   LOWER(zipcode) like (','''',' % ',@var,'%','''','))');
   
  if @counter=1
  begin
  set @sqlresult=@sql1;
  end
  else begin
  set @sqlresult=concat(@sql1,' AND ',@sqlresult) ;
  end 
  set @counter=@counter+1;
   
 -- select @sql1 from dual;
   SET @_searchKeyword = concat(@_searchKeyword,1,@_nextlen + 1,'');
   END;
   --select @counter from dual;
   set @sqlresult=concat('(',@sqlFrontPart,@sqlresult,')');
  -- select @sqlresult from dual;
 -- PREPARE stmt1 from @sqlresult;
 select   @sqlresult;
   EXECUTE (@sqlresult);
END ;

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create  PROCEDURE [konyadminconsole].[reports_messages_received]( @from_date VARCHAR(10), @to_date VARCHAR(10), @category_id VARCHAR(50), @csr_name VARCHAR(50))
AS
BEGIN
SET NOCOUNT ON;
DECLARE @queryStatement varchar(300);
set @queryStatement = 'SELECT count(*) AS messages_received_count FROM konyadminconsole.requestmessage 
where createdby not in (select Username from konyadminconsole.systemuser)';

if @from_date != '' and @from_date != '' 
begin
SET @queryStatement=@queryStatement+' and createdts >= ' + CONVERT(DATETIME,@from_date,126)+' and createdts <= '+ CONVERT(DATETIME,@to_date,126);
/*
set @queryStatement = concat(@queryStatement, "and createdts >= '",
STR_TO_DATE(@from_date, '%Y-%m-%d'),"' and createdts <= '",
STR_TO_DATE(@to_date, '%Y-%m-%d'),"'");
*/
end 


if @category_id != '' 
begin
SET @queryStatement=@queryStatement+ ' and CustomerRequest_id in 
(select id from konyadminconsole.customerrequest where RequestCategory_id = ' + @category_id;
/*
set @queryStatement = concat(@queryStatement, " and CustomerRequest_id in 
(select id from konyadminconsole.customerrequest where RequestCategory_id = '",@category_id,"')");
*/
end 


if @csr_name != '' 
begin
SET @queryStatement=@queryStatement + 'and RepliedBy = ' + @csr_name;
/*
set @queryStatement = concat(@queryStatement, " and RepliedBy = '",@csr_name,"'");
*/
end 
/*
PREPARE stmt FROM @queryStatement;*/

EXEC (@queryStatement);

END;
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE    PROCEDURE [konyadminconsole].[reports_messages_sent]( @from_date VARCHAR(10), @to_date VARCHAR(10), @category_id VARCHAR(50), @csr_name VARCHAR(50))
AS
BEGIN
SET NOCOUNT ON;
Declare @queryStatement varchar(300);
set @queryStatement ='SELECT count(*) AS messages_sent_count FROM konyadminconsole.requestmessage where createdby in (select Username from konyadminconsole.systemuser)';

if @from_date != '' and @from_date != '' 
begin
SET @queryStatement=@queryStatement + 'and createdts >= '+ CONVERT(DATETIME,@from_date,126) + ' and createdts <= ' +  CONVERT(DATETIME,@to_date,126);
/*
set @queryStatement = concat(@queryStatement, "and createdts >= '",
STR_TO_DATE(@from_date, '%Y-%m-%d'),"' and createdts <= '",
STR_TO_DATE(@to_date, '%Y-%m-%d'),"'");
*/
end 

if @category_id != '' 
begin
SET @queryStatement=@queryStatement + 'and CustomerRequest_id in 
(select id from konyadminconsole.customerrequest where RequestCategory_id = ' + @category_id;
/*
set @queryStatement = concat(@queryStatement, " and CustomerRequest_id in 
(select id from konyadminconsole.customerrequest where RequestCategory_id = '",@category_id,"')");
*/
end 

if @csr_name != '' 
begin
SET @queryStatement=@queryStatement + 'and RepliedBy = '+ @csr_name;
/*
set @queryStatement = concat(@queryStatement, " and RepliedBy = '",@csr_name,"'");
*/
end 
/*
PREPARE stmt FROM @queryStatement;*/
EXEC (@queryStatement);

END;
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
		CREATE    PROCEDURE [konyadminconsole].[reports_threads_averageage]( @from_date VARCHAR(10), @to_date VARCHAR(10), @category_id VARCHAR(50), 
		@csr_name VARCHAR(50))
		AS
		BEGIN
		SET NOCOUNT ON;
		Declare @queryStatement varchar(max);

		set @queryStatement =
		'select AVG(thread_life_records.thread_life) as threads_averageage_count from 
		(select DATEDIFF(yy,MAX(createdts), MIN(createdts)) as thread_life from konyadminconsole.requestmessage where 1=1 ';

		if @from_date != '' and @from_date != '' begin
		SET @queryStatement=@queryStatement + 'and createdts >= ' + CONVERT(DATETIME, @from_date, 126) + 'and createdts >= ' + CONVERT(DATETIME,@to_date,126);
		end 

		if @category_id != '' begin
			set @queryStatement = concat(@queryStatement, ' and CustomerRequest_id in 
			(select id from konyadminconsole.customerrequest where RequestCategory_id = ''',@category_id,''')');
		end 

		if @csr_name != '' begin
		set @queryStatement = concat(@queryStatement, ' and RepliedBy = ''',@csr_name,'''');
		end 

		set @queryStatement = concat(@queryStatement, ' group by CustomerRequest_id) thread_life_records');

		--PREPARE stmt FROM @queryStatement;
		--select @queryStatement
		EXECUTE (@queryStatement);

	END ;

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE    PROCEDURE [konyadminconsole].[reports_threads_new]( @from_date VARCHAR(10), @to_date VARCHAR(10), @category_id VARCHAR(50), @csr_name VARCHAR(50))
AS
BEGIN
SET NOCOUNT ON;
Declare @queryStatement varchar(300);
set @queryStatement =
'SELECT count(*) AS threads_new_count from konyadminconsole.customerrequest where Status_id != ' + 'SID_RESOLVED';

if @from_date != '' and @from_date != '' 
begin
SET @queryStatement=@queryStatement + 'and createdts >=' + ' ' + CONVERT(DATETIME,@from_date,126) + 'and createdts<='+' '+ CONVERT(DATETIME,@to_date,126);
	/*set @queryStatement = concat(@queryStatement, " and createdts >= '",
	CONVERT(DATETIME,@from_date,126),"' and createdts <= '",
	CONVERT(DATETIME,@to_date, 126),"'");*/
end 

if @category_id != '' 
begin
SET @queryStatement=@queryStatement + 'and RequestCategory_id ='+ @category_id;
/*set @queryStatement = concat(@queryStatement, " and RequestCategory_id = '",@category_id,"'");*/
end 

if @csr_name != '' 
begin
SET @queryStatement=@queryStatement+' and AssignedTo = ' + @csr_name;
/*set @queryStatement = concat(@queryStatement, " and AssignedTo = '",@csr_name,"'");*/
end 
EXEC (@queryStatement)

END;
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create  PROCEDURE [konyadminconsole].[reports_threads_resolved]( @from_date VARCHAR(10), 
@to_date VARCHAR(10), @category_id VARCHAR(50), @csr_name VARCHAR(50))
AS
BEGIN
SET NOCOUNT ON;
Declare @queryStatement varchar(300);
set @queryStatement = 'SELECT count(*) AS threads_resolved_count from konyadminconsole.customerrequest where Status_id =''SID_RESOLVED''';

if @from_date != '' and @from_date != '' 
begin
SET @queryStatement=@queryStatement + 'and createdts >= ' + CONVERT(DATETIME, @from_date, 126) + 'and createdts >= ' + CONVERT(DATETIME,@to_date,126);

/*set @queryStatement = concat(@queryStatement, " and createdts >= '",
STR_TO_DATE(@from_date, '%Y-%m-%d'),"' and createdts <= '",
STR_TO_DATE(@to_date, '%Y-%m-%d'),"'");*/

end 

if @category_id != '' begin
SET @queryStatement=@queryStatement + 'and RequestCategory_id=' + @category_id;
/*set @queryStatement = concat(@queryStatement, " and RequestCategory_id = '",@category_id,"'");*/

end 

if @csr_name != '' begin
SET @queryStatement=@queryStatement + 'and AssignedTo = ' + @csr_name;

/*set @queryStatement = concat(@queryStatement, " and AssignedTo = '",@csr_name,"'");*/

end 
exec (@queryStatement)
END;
