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
    reqMsgId, customerReqId, '<p><span style=\"color: #000000;\">Hi,</span></p>\n<p><span style=\"color: #000000;\">&nbsp;</span></p>\n<p><span style=\"color: #000000;\">I had scheduled a bill payment to my internet provider- AT&amp;T for $25, this Monday but the payment failed. Can you help me out?</span></p>\n<p><span style=\"color: #000000;\">&nbsp;</span></p>\n<p><span style=\"font-size: 11.0pt; font-family: \'Calibri\',sans-serif; color: #000000;\">Thanks </span></p>', 
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
    reqMsgId, customerReqId, '<p><span style=\"color: #000000;\">Hi Richard,</span></p><p><span style=\"color: #000000;\">&nbsp;</span></p><p><span style=\"color: #000000;\">Thanks for letting me know. I will go ahead with the payment now. </span></p><p><span style=\"color: #000000;\">&nbsp;</span></p><p><span style=\"font-size: 11.0pt; font-family: \'Calibri\',sans-serif; color: #000000;\">Thanks</span></p>', 
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
    'UID10', '1234', '0', 'admin2', 
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

DROP VIEW IF EXISTS `groups_view`;
CREATE VIEW `groups_view` AS
    SELECT 
        `membergroup`.`id` AS `Group_id`,
        `membergroup`.`Type_id` AS `Type_id`,
        `customertype`.`Name` AS `Type_Name`,
        `membergroup`.`Description` AS `Group_Desc`,
        `membergroup`.`Status_id` AS `Status_id`,
        `membergroup`.`Name` AS `Group_Name`,
        `membergroup`.`isEAgreementActive` AS `isEAgreementActive`,
        (SELECT 
                COUNT(`groupentitlement`.`Group_id`)
            FROM
                `groupentitlement`
            WHERE
                (`groupentitlement`.`Group_id` = `membergroup`.`id`)) AS `Entitlements_Count`,
        (SELECT 
                COUNT(`customergroup`.`Customer_id`)
            FROM
                `customergroup`
            WHERE
                (`customergroup`.`Group_id` = `membergroup`.`id`)) AS `Customers_Count`,
        (CASE `membergroup`.`Status_id`
            WHEN 'SID_ACTIVE' THEN 'Active'
            ELSE 'Inactive'
        END) AS `Status`
    FROM
       ( `membergroup`
       LEFT JOIN `customertype` ON ((`membergroup`.`Type_id` = `customertype`.`id`)));

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
				customer.UserName as Username, customer.Salutation, customer.Gender,customer.Ssn,
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


CREATE TABLE `mfatype` (
  `id` VARCHAR(50) NOT NULL,
  `Name` VARCHAR(50) NOT NULL,
  `Description` VARCHAR(300) NULL,
  `Status_id` VARCHAR(50) NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  INDEX `FK_mfatype_status_id_idx` (`Status_id` ASC),
  CONSTRAINT `FK_mfatype_status_id`
    FOREIGN KEY (`Status_id`)
    REFERENCES `status` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `feature` (
  `id` VARCHAR(50) NOT NULL,
  `Name` VARCHAR(100) NOT NULL,
  `DisplayName` VARCHAR(100) NULL,
  `Description` VARCHAR(400) NULL DEFAULT NULL,
  `Status_id` VARCHAR(50) NOT NULL,
  `IsMonetary` TINYINT(1) NOT NULL DEFAULT '0',
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)) ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `appaction` (
  `id` VARCHAR(50) NOT NULL,
  `App_id` VARCHAR(50) NOT NULL,
  `Action_id` VARCHAR(50) NOT NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  INDEX `FK_appaction_action_id_idx` (`Action_id` ASC),
  INDEX `FK_appaction_app_id_idx` (`App_id` ASC),
  CONSTRAINT `FK_appaction_action_id`
    FOREIGN KEY (`Action_id`)
    REFERENCES `service` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_appaction_app_id`
    FOREIGN KEY (`App_id`)
    REFERENCES `app` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `frequencytype` (
  `id` VARCHAR(50) NOT NULL,
  `Description` VARCHAR(300) NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)) ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `appactionmfa` (
  `id` VARCHAR(50) NOT NULL,
  `AppAction_id` VARCHAR(50) NOT NULL,
  `IsMFARequired` TINYINT(1) NOT NULL DEFAULT '0',
  `FrequencyType_id` VARCHAR(50) NOT NULL,
  `FrequencyValue` VARCHAR(50) NULL,
  `Status_id` VARCHAR(50) NOT NULL,
  `Description` VARCHAR(300) NULL,
  `PrimaryMFAType` VARCHAR(50) NOT NULL,
  `SecondaryMFAType` VARCHAR(50) NOT NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  INDEX `FK_appaction_PrimaryMFA_id_idx` (`PrimaryMFAType` ASC),
  INDEX `FK_appaction_SecondaryMFA_id_idx` (`SecondaryMFAType` ASC),
  PRIMARY KEY (`AppAction_id`, `id`),
  INDEX `FK_appactionmfa_status_id_idx` (`Status_id` ASC),
  INDEX `FK_appactionmfa_Frequency_id_idx` (`FrequencyType_id` ASC),
  INDEX `FK_appactionmfa_AppAction_id_idx` (`AppAction_id` ASC),
  CONSTRAINT `FK_appaction_PrimaryMFA_id`
    FOREIGN KEY (`PrimaryMFAType`)
    REFERENCES `mfatype` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_appaction_SecondaryMFA_id`
    FOREIGN KEY (`SecondaryMFAType`)
    REFERENCES `mfatype` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_appactionmfa_status_id`
    FOREIGN KEY (`Status_id`)
    REFERENCES `status` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_appactionmfa_AppAction_id`
    FOREIGN KEY (`AppAction_id`)
    REFERENCES `appaction` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_appactionmfa_Frequency_id`
    FOREIGN KEY (`FrequencyType_id`)
    REFERENCES `frequencytype` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `mfakey` (
  `id` VARCHAR(50) NOT NULL,
  `Description` VARCHAR(300) NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)) ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `mfaproperty` (
  `id` VARCHAR(50) NOT NULL,
  `value` VARCHAR(300) NOT NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)) ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `mfaconfigurations` (
  `MFA_id` VARCHAR(50) NOT NULL,
  `MFAKey_id` VARCHAR(50) NOT NULL,
  `MFAProperty_id` VARCHAR(50) NOT NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`MFA_id`, `MFAKey_id`, `MFAProperty_id`),
  INDEX `FK_mfaconfigurations_mfakey_id_idx` (`MFAKey_id` ASC),
  INDEX `FK_mfaConfigurations_Property_id_idx` (`MFAProperty_id` ASC),
  CONSTRAINT `FK_mfaConfigurations_MFA_id`
    FOREIGN KEY (`MFA_id`)
    REFERENCES `mfatype` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_mfaconfigurations_mfakey_id`
    FOREIGN KEY (`MFAKey_id`)
    REFERENCES `mfakey` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_mfaConfigurations_Property_id`
    FOREIGN KEY (`MFAProperty_id`)
    REFERENCES `mfaproperty` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `limittype` (
  `id` VARCHAR(50) NOT NULL,
  `Name` VARCHAR(50) NOT NULL,
  `Description` VARCHAR(50) NULL DEFAULT NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)) ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `appactionlimit` (
  `AppAction_id` VARCHAR(50) NOT NULL,
  `LimitType_id` VARCHAR(50) NOT NULL,
  `Value` DECIMAL(20,2) NOT NULL DEFAULT '0.00',
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`AppAction_id`, `LimitType_id`),
  INDEX `FK_appactionlimit_Limit_id_idx` (`LimitType_id` ASC),
  CONSTRAINT `FK_appactionlimit_Limit_id`
    FOREIGN KEY (`LimitType_id`)
    REFERENCES `limittype` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_appactionlimit_AppAction_id`
    FOREIGN KEY (`AppAction_id`)
    REFERENCES `appaction` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `appfeature` (
  `id` VARCHAR(50) NOT NULL,
  `App_id` VARCHAR(50) NOT NULL,
  `Feature_id` VARCHAR(50) NOT NULL,
  `IsTandCAgreementRequired` TINYINT(1) NOT NULL DEFAULT '1',
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  INDEX `FK_appfeature_Feature_id_idx` (`Feature_id` ASC),
  CONSTRAINT `FK_appfeature_App_id`
    FOREIGN KEY (`App_id`)
    REFERENCES `app` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_appfeature_Feature_id`
    FOREIGN KEY (`Feature_id`)
    REFERENCES `feature` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8;

CREATE TABLE `featuretermsandconditions` (
  `AppFeature_id` VARCHAR(50) NOT NULL,
  `Locale_id` VARCHAR(50) NOT NULL,
  `Value` TEXT NOT NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`Locale_id`, `AppFeature_id`),
  INDEX `FK_FeatureTermsandConditions_Language_id_idx` (`Locale_id` ASC),
  INDEX `FK_featuretermsandconditions_AppFeature_id_idx` (`AppFeature_id` ASC),
  CONSTRAINT `FK_FeatureTermsandConditions_Language_id`
    FOREIGN KEY (`Locale_id`)
    REFERENCES `locale` (`Code`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_featuretermsandconditions_AppFeature_id`
    FOREIGN KEY (`AppFeature_id`)
    REFERENCES `appfeature` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `customeractionlimit` (
  `Customer_id` VARCHAR(50) NOT NULL,
  `Action_id` VARCHAR(50) NOT NULL,
  `LimitType_id` VARCHAR(50) NOT NULL,
  `Value` DECIMAL(20,2) NOT NULL DEFAULT '0.00',
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`Customer_id`, `Action_id`, `LimitType_id`),
  INDEX `FK_customeractionlimit_Customer_id_idx` (`Customer_id` ASC),
  INDEX `FK_customeractionlimit_Action_id_idx` (`Action_id` ASC),
  INDEX `FK_CustomerEntitlement_Limittype_id_idx` (`LimitType_id` ASC),
  CONSTRAINT `FK_customeractionlimit_Customer_id`
    FOREIGN KEY (`Customer_id`)
    REFERENCES `customer` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_customeractionlimit_Limittype_id`
    FOREIGN KEY (`LimitType_id`)
    REFERENCES `limittype` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_customeractionlimit_Action_id`
    FOREIGN KEY (`Action_id`)
    REFERENCES `service` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `groupactionlimit` (
  `Group_id` VARCHAR(50) NOT NULL,
  `Action_id` VARCHAR(50) NOT NULL,
  `LimitType_id` VARCHAR(50) NOT NULL,
  `Value` DECIMAL(20,2) NOT NULL DEFAULT '0.00',
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`Group_id`, `Action_id`, `LimitType_id`),
  INDEX `IXFK_groupactionlimit_Group` (`Group_id` ASC),
  INDEX `IXFK_groupactionlimit_Action` (`Action_id` ASC),
  INDEX `FK_GroupEntitlements_Limittype_idx` (`LimitType_id` ASC),
  CONSTRAINT `FK_groupactionlimit_Group_id`
    FOREIGN KEY (`Group_id`)
    REFERENCES `membergroup` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_groupactionlimit_Limittype_id`
    FOREIGN KEY (`LimitType_id`)
    REFERENCES `limittype` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_groupactionlimit_Action_id`
    FOREIGN KEY (`Action_id`)
    REFERENCES `service` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE = InnoDB DEFAULT CHARSET=utf8;
    
ALTER TABLE `service` 
ADD COLUMN `Feature_id` VARCHAR(50) NULL AFTER `Channel_id`,
ADD COLUMN `Disclaimer` TEXT NULL AFTER `Status_id`,
ADD INDEX `FK_service_Feature_id_idx` (`Feature_id` ASC);

ALTER TABLE `service` ADD CONSTRAINT `FK_Service_Feature_id`
  FOREIGN KEY (`Feature_id`)
  REFERENCES `feature` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

DROP VIEW IF EXISTS `channel_view`;
CREATE VIEW `channel_view` AS select `channel`.`id` AS `channel_id`,`channel`.`status_id` AS `channel_status_id`,`channeltext`.`LanguageCode` AS `channeltext_LanguageCode`,`channeltext`.`Description` AS `channeltext_Description`,`channeltext`.`createdby` AS `channeltext_createdby`,`channeltext`.`modifiedby` AS `channeltext_modifiedby`,`channeltext`.`createdts` AS `channeltext_createdts`,`channeltext`.`lastmodifiedts` AS `channeltext_lastmodifiedts`,`channeltext`.`synctimestamp` AS `channeltext_synctimestamp`,`channeltext`.`softdeleteflag` AS `channeltext_softdeleteflag` from (`channeltext` left join `channel` on((`channeltext`.`channelID` = `channel`.`id`)));

DROP VIEW IF EXISTS `alertcategory_view`;
CREATE VIEW `alertcategory_view` AS select `dbxalertcategory`.`id` AS `alertcategory_id`,`dbxalertcategory`.`status_id` AS `alertcategory_status_id`,`dbxalertcategory`.`accountLevel` AS `alertcategory_accountLevel`,`dbxalertcategory`.`DisplaySequence` AS `alertcategory_DisplaySequence`,`dbxalertcategory`.`softdeleteflag` AS `alertcategory_softdeleteflag`,`dbxalertcategory`.`Name` AS `alertcategory_Name`,`dbxalertcategorytext`.`LanguageCode` AS `alertcategorytext_LanguageCode`,`dbxalertcategorytext`.`DisplayName` AS `alertcategorytext_DisplayName`,`dbxalertcategorytext`.`Description` AS `alertcategorytext_Description`,`dbxalertcategorytext`.`createdby` AS `alertcategorytext_createdby`,`dbxalertcategorytext`.`modifiedby` AS `alertcategorytext_modifiedby`,`dbxalertcategorytext`.`createdts` AS `alertcategorytext_createdts`,`dbxalertcategorytext`.`lastmodifiedts` AS `alertcategorytext_lastmodifiedts`,`dbxalertcategorytext`.`synctimestamp` AS `alertcategorytext_synctimestamp`,`dbxalertcategorytext`.`softdeleteflag` AS `alertcategorytext_softdeleteflag` from (`dbxalertcategorytext` left join `dbxalertcategory` on((`dbxalertcategorytext`.`AlertCategoryId` = `dbxalertcategory`.`id`)));

DROP VIEW IF EXISTS `alerttype_view`;
CREATE VIEW `alerttype_view` AS select `dbxalerttype`.`id` AS `alerttype_id`,`dbxalerttype`.`Name` AS `alerttype_Name`,`dbxalerttype`.`AlertCategoryId` AS `alerttype_AlertCategoryId`,`dbxalerttype`.`AttributeId` AS `alerttype_AttributeId`,`dbxalerttype`.`AlertConditionId` AS `alerttype_AlertConditionId`,`dbxalerttype`.`Value1` AS `alerttype_Value1`,`dbxalerttype`.`Value2` AS `alerttype_Value2`,`dbxalerttype`.`Status_id` AS `alerttype_Status_id`,`dbxalerttype`.`IsGlobal` AS `alerttype_IsGlobal`,`dbxalerttype`.`DisplaySequence` AS `alerttype_DisplaySequence`,`dbxalerttype`.`softdeleteflag` AS `alerttype_softdeleteflag`,`dbxalerttypetext`.`LanguageCode` AS `alerttypetext_LanguageCode`,`dbxalerttypetext`.`DisplayName` AS `alerttypetext_DisplayName`,`dbxalerttypetext`.`Description` AS `alerttypetext_Description`,`dbxalerttypetext`.`createdby` AS `alerttypetext_createdby`,`dbxalerttypetext`.`modifiedby` AS `alerttypetext_modifiedby`,`dbxalerttypetext`.`createdts` AS `alerttypetext_createdts`,`dbxalerttypetext`.`lastmodifiedts` AS `alerttypetext_lastmodifiedts`,`dbxalerttypetext`.`synctimestamp` AS `alerttypetext_synctimestamp`,`dbxalerttypetext`.`softdeleteflag` AS `alerttypetext_softdeleteflag` from (`dbxalerttypetext` left join `dbxalerttype` on((`dbxalerttypetext`.`AlertTypeId` = `dbxalerttype`.`id`)));

DROP VIEW IF EXISTS `customer_alertcategory_view`;
CREATE VIEW `customer_alertcategory_view` AS select `customeralertswitch`.`Customer_id` AS `customer_id`,`customeralertswitch`.`AccountID` AS `accountID`,`customeralertswitch`.`Status_id` AS `preference_Status_id`,`dbxalertcategory`.`id` AS `alertcategory_id`,`dbxalertcategory`.`status_id` AS `alertcategory_status_id`,`dbxalertcategory`.`accountLevel` AS `alertcategory_accountLevel`,`dbxalertcategory`.`DisplaySequence` AS `alertcategory_DisplaySequence`,`dbxalertcategory`.`softdeleteflag` AS `alertcategory_softdeleteflag`,`dbxalertcategorytext`.`LanguageCode` AS `alertcategorytext_LanguageCode`,`dbxalertcategory`.`Name` AS `alertcategory_Name`,`dbxalertcategorytext`.`DisplayName` AS `alertcategorytext_DisplayName`,`dbxalertcategorytext`.`Description` AS `alertcategorytext_Description`,`dbxalertcategorytext`.`createdby` AS `alertcategorytext_createdby`,`dbxalertcategorytext`.`modifiedby` AS `alertcategorytext_modifiedby`,`dbxalertcategorytext`.`createdts` AS `alertcategorytext_createdts`,`dbxalertcategorytext`.`lastmodifiedts` AS `alertcategorytext_lastmodifiedts`,`dbxalertcategorytext`.`synctimestamp` AS `alertcategorytext_synctimestamp`,`dbxalertcategorytext`.`softdeleteflag` AS `alertcategorytext_softdeleteflag` from ((`customeralertswitch` left join `dbxalertcategory` on((`customeralertswitch`.`AlertCategoryId` = `dbxalertcategory`.`id`))) left join `dbxalertcategorytext` on((`customeralertswitch`.`AlertCategoryId` = `dbxalertcategorytext`.`AlertCategoryId`)));

ALTER TABLE `feature` ADD COLUMN `code` VARCHAR(50) NOT NULL AFTER `Name`;

DROP VIEW IF EXISTS `alertattribute_view`;
CREATE VIEW `alertattribute_view` AS select `alertattribute`.`id` AS `alertattribute_id`,`alertattribute`.`LanguageCode` AS `alertattribute_LanguageCode`,`alertattribute`.`name` AS `alertattribute_name`,`alertattribute`.`type` AS `alertattribute_type`,`alertattribute`.`softdeleteflag` AS `alertattribute_softdeleteflag`,`alertattributelistvalues`.`id` AS `alertattributelistvalues_id`,`alertattributelistvalues`.`AlertAttributeId` AS `alertattributelistvalues_AlertAttributeId`,`alertattributelistvalues`.`LanguageCode` AS `alertattributelistvalues_LanguageCode`,`alertattributelistvalues`.`name` AS `alertattributelistvalues_name`,`alertattributelistvalues`.`softdeleteflag` AS `alertattributelistvalues_softdeleteflag` from (`alertattribute` left join `alertattributelistvalues` on((`alertattribute`.`id` = `alertattributelistvalues`.`AlertAttributeId`)));

ALTER TABLE `mfaconfigurations` 
DROP FOREIGN KEY `FK_mfaConfigurations_Property_id`;
ALTER TABLE `mfaconfigurations` 
DROP COLUMN `MFAProperty_id`,
ADD COLUMN `value` VARCHAR(300) NOT NULL AFTER `MFAKey_id`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`MFA_id`, `MFAKey_id`),
DROP INDEX `FK_mfaConfigurations_Property_id_idx` ;

DROP TABLE `mfaproperty`;

ALTER TABLE `appactionmfa` 
ADD COLUMN `SMSText` TEXT NULL AFTER `SecondaryMFAType`,
ADD COLUMN `EmailSubject` TEXT NULL AFTER `SMSText`,
ADD COLUMN `EmailBody` TEXT NULL AFTER `EmailSubject`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`id`, `AppAction_id`);

CREATE TABLE `usernamerules` (
  `id` varchar(50) NOT NULL,
  `IsCustomer` tinyint(1) NOT NULL DEFAULT '1',
  `minLength` int(2) NOT NULL,
  `maxLength` int(2) NOT NULL,
  `symbolsAllowed` tinyint(1) NOT NULL DEFAULT '1',
  `supportedSymbols` varchar(100) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `lastsynctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `passwordrules` (
  `id` varchar(50) NOT NULL,
  `IsCustomer` tinyint(1) NOT NULL DEFAULT '1',
  `minLength` int(2) NOT NULL,
  `maxLength` int(2) NOT NULL,
  `atleastOneLowerCase` tinyint(1) NOT NULL DEFAULT '1',
  `atleastOneUpperCase` tinyint(1) NOT NULL DEFAULT '1',
  `atleastOneNumber` tinyint(1) NOT NULL DEFAULT '1',
  `atleastOneSymbol` tinyint(1) NOT NULL DEFAULT '1',
  `charRepeatCount` int(1) NOT NULL DEFAULT '1',
  `supportedSymbols` varchar(100) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `lastsynctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `policytype` (
  `id` varchar(50) NOT NULL,
  `Type` varchar(50) NOT NULL,
  `IsCustomer` tinyint(1) NOT NULL DEFAULT '0',
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `lastsynctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_policyname_idx` (`Type`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `policycontent` (
  `id` varchar(50) NOT NULL,
  `Type_id` varchar(50) NOT NULL,
  `Locale_Code` varchar(5) NOT NULL,
  `Content` varchar(1000) NOT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `lastsynctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_Locale_Code_idx` (`Locale_Code`),
  KEY `FK_Policy_id_idx` (`Type_id`),
  CONSTRAINT `FK_Locale_Code` FOREIGN KEY (`Locale_Code`) REFERENCES `locale` (`Code`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_Policy_id` FOREIGN KEY (`Type_id`) REFERENCES `policytype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `passwordlockoutsettings` (
  `id` varchar(50) NOT NULL,
  `passwordValidity` int(3) NOT NULL,
  `passwordExpiryWarningRequired` tinyint(1) NOT NULL DEFAULT '1',
  `passwordExpiryWarningThreshold` int(2) NOT NULL,
  `passwordHistoryCount` int(1) NOT NULL DEFAULT '1',
  `accountLockoutThreshold` int(2) NOT NULL,
  `accountLockoutTime` int(4) NOT NULL,
  `recoveryEmailLinkValidity` int(4) NOT NULL,
  `createdby` varchar(64) DEFAULT NULL,
  `modifiedby` varchar(64) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE VIEW `policy_view` AS
    SELECT 
        `policycontent`.`id` AS `id`,
        `policytype`.`id` AS `Type_id`,
        `policycontent`.`Locale_Code` AS `Locale`,
        `policycontent`.`Content` AS `PolicyContent`
    FROM
        (`policycontent`
        LEFT JOIN `policytype` ON ((`policycontent`.`Type_id` = `policytype`.`id`))); 

ALTER TABLE `groupactionlimit` 
ADD COLUMN `id` VARCHAR(50) NOT NULL FIRST,
CHANGE COLUMN `LimitType_id` `LimitType_id` VARCHAR(50) NULL ,
CHANGE COLUMN `Value` `Value` DECIMAL(20,2) NULL DEFAULT NULL ,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`id`);

ALTER TABLE `groupactionlimit` ADD UNIQUE `groupactionlimit_unique_index` (`Group_id`, `Action_id`, `LimitType_id`);

ALTER TABLE `appaction` ADD UNIQUE `appaction_unique_index` (`App_id`, `Action_id`);

ALTER TABLE `customeractionlimit` 
DROP FOREIGN KEY `FK_customeractionlimit_Limittype_id`;
ALTER TABLE `customeractionlimit` 
ADD COLUMN `id` VARCHAR(50) NOT NULL FIRST,
CHANGE COLUMN `LimitType_id` `LimitType_id` VARCHAR(50) NULL ,
CHANGE COLUMN `Value` `Value` DECIMAL(20,2) NULL DEFAULT NULL ,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`id`);

ALTER TABLE `customeractionlimit` 
ADD CONSTRAINT `FK_customeractionlimit_Limittype_id`
  FOREIGN KEY (`LimitType_id`)
  REFERENCES `limittype` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE `customeractionlimit` ADD UNIQUE `customeractionlimit_unique_index` (`Customer_id`, `Action_id`, `LimitType_id`);

CREATE VIEW `customer_action_group_action_limits_view` AS select `customeractionlimit`.`Customer_id` AS `Customer_id`,`customeractionlimit`.`LimitType_id` AS `Customer_LimitType_id`,concat('',`customeractionlimit`.`Value`) AS `Customer_Limit_Value`,NULL AS `Group_LimitType_id`,NULL AS `Group_Limit_Value`,`service`.`id` AS `Action_id`,`service`.`Name` AS `Action_Name`,`service`.`code` AS `Action_Code`,`service`.`Description` AS `Action_Description`,`service`.`Status_id` AS `Action_Status_id`,`app`.`id` AS `App_id`,`app`.`Name` AS `App_Name`,`appactionlimit`.`LimitType_id` AS `LimitType_id`,concat('',`appactionlimit`.`Value`) AS `Limit_Value`,`feature`.`id` AS `Feature_id`,`feature`.`Name` AS `Feature_Name`,`feature`.`code` AS `Feature_Code`,`feature`.`Description` AS `Feature_Description`,`feature`.`Status_id` AS `Feature_Status_id` from (((((`customeractionlimit` left join `service` on((`service`.`id` = `customeractionlimit`.`Action_id`))) left join `feature` on((`feature`.`id` = `service`.`Feature_id`))) left join `appaction` on((`service`.`id` = `appaction`.`Action_id`))) left join `app` on((`appaction`.`App_id` = `app`.`id`))) left join `appactionlimit` on((`appaction`.`id` = `appactionlimit`.`AppAction_id`))) union select `customergroup`.`Customer_id` AS `Customer_id`,NULL AS `Customer_LimitType_id`,NULL AS `Customer_Limit_Value`,`groupactionlimit`.`LimitType_id` AS `Group_LimitType_id`,concat('',`groupactionlimit`.`Value`) AS `Group_Limit_Value`,`service`.`id` AS `Action_id`,`service`.`Name` AS `Action_Name`,`service`.`code` AS `Action_Code`,`service`.`Description` AS `Action_Description`,`service`.`Status_id` AS `Action_Status_id`,`app`.`id` AS `App_id`,`app`.`Name` AS `App_Name`,`appactionlimit`.`LimitType_id` AS `LimitType_id`,concat('',`appactionlimit`.`Value`) AS `Limit_Value`,`feature`.`id` AS `Feature_id`,`feature`.`Name` AS `Feature_Name`,`feature`.`code` AS `Feature_Code`,`feature`.`Description` AS `Feature_Description`,`feature`.`Status_id` AS `Feature_Status_id` from ((((((`customergroup` left join `groupactionlimit` on((`groupactionlimit`.`Group_id` = `customergroup`.`Group_id`))) left join `service` on((`service`.`id` = `groupactionlimit`.`Action_id`))) left join `feature` on((`feature`.`id` = `service`.`Feature_id`))) left join `appaction` on((`service`.`id` = `appaction`.`Action_id`))) left join `app` on((`appaction`.`App_id` = `app`.`id`))) left join `appactionlimit` on((`appaction`.`id` = `appactionlimit`.`AppAction_id`)));

ALTER TABLE `feature` 
DROP COLUMN `IsMonetary`,
ADD COLUMN `Type_id` VARCHAR(50) NULL AFTER `Description`;

CREATE TABLE `featuretype` (
  `id` VARCHAR(50) NOT NULL,
  `Description` VARCHAR(50) NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`))
ENGINE = InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `feature` 
ADD INDEX `FK_feature_type_id_idx` (`Type_id` ASC),
ADD INDEX `FK_feature_status_id_idx` (`Status_id` ASC);

ALTER TABLE `feature` 
ADD CONSTRAINT `FK_feature_type_id`
  FOREIGN KEY (`Type_id`)
  REFERENCES `featuretype` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION,
ADD CONSTRAINT `FK_feature_status_id`
  FOREIGN KEY (`Status_id`)
  REFERENCES `status` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
  
ALTER TABLE `feature` ADD COLUMN `DisplayDescription` VARCHAR(400) NULL AFTER `Description`;

DROP VIEW IF EXISTS `alerttypeapp_view`;
CREATE VIEW `alerttypeapp_view` AS select `app`.`id` AS `app_Id`,`dbxalerttype`.`id` AS `alerttype_AlertTypeId`,`dbxalerttype`.`Name` AS `alerttype_Name`,`app`.`Name` AS `app_Name`,`app`.`Description` AS `app_Description`,(select if((count(0) > 0),TRUE,FALSE) from `alerttypeapp` where ((`alerttypeapp`.`AppId` = `app`.`id`) and (`alerttypeapp`.`AlertTypeId` = `dbxalerttype`.`id`))) AS `isSupported`,`dbxalerttype`.`softdeleteflag` AS `alerttype_softdeleteflag`,`app`.`softdeleteflag` AS `app_softdeleteflag` from (`app` join `dbxalerttype`);

DROP VIEW IF EXISTS `alerttypeaccounttype_view`;
CREATE VIEW `alerttypeaccounttype_view` AS select `accounttype`.`TypeID` AS `accounttype_TypeID`,`accounttype`.`TypeDescription` AS `accounttype_TypeDescription`,`accounttype`.`displayName` AS `accounttype_displayName`,`dbxalerttype`.`id` AS `alerttype_AlertTypeId`,`dbxalerttype`.`Name` AS `alerttype_Name`,(select if((count(0) > 0),TRUE,FALSE) from `alerttypeaccounttype` where ((`alerttypeaccounttype`.`AccountTypeId` = `accounttype`.`TypeID`) and (`alerttypeaccounttype`.`AlertTypeId` = `dbxalerttype`.`id`))) AS `isSupported`,`dbxalerttype`.`softdeleteflag` AS `alerttype_softdeleteflag` from (`accounttype` join `dbxalerttype`);

DROP VIEW IF EXISTS `alerttypecustomertype_view`;
CREATE VIEW `alerttypecustomertype_view` AS select `customertype`.`id` AS `customertype_TypeID`,`customertype`.`Name` AS `customertype_Name`,`dbxalerttype`.`id` AS `alerttype_AlertTypeId`,`dbxalerttype`.`Name` AS `alerttype_Name`,(select if((count(0) > 0),TRUE,FALSE) from `alerttypecustomertype` where ((`alerttypecustomertype`.`CustomerTypeId` = `customertype`.`id`) and (`alerttypecustomertype`.`AlertTypeId` = `dbxalerttype`.`id`))) AS `isSupported`,`customertype`.`softdeleteflag` AS `customertype_softdeleteflag`,`dbxalerttype`.`softdeleteflag` AS `alerttype_softdeleteflag` from (`customertype` join `dbxalerttype`);

ALTER TABLE `csrassistgrant` 
ADD COLUMN `userRoleId` VARCHAR(50) NULL AFTER `userId`,
ADD INDEX `FK_CSRAssistGrant_Role_idx` (`userRoleId` ASC);

ALTER TABLE `csrassistgrant` 
ADD CONSTRAINT `FK_CSRAssistGrant_Role`
  FOREIGN KEY (`userRoleId`)
  REFERENCES `role` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE `csrassistgrant` 
ADD COLUMN `appId` VARCHAR(50) NULL AFTER `isConsumed`;

DROP VIEW IF EXISTS `communicationtemplate_channel_view`;
CREATE VIEW `communicationtemplate_channel_view` AS select `communicationtemplate`.`Id` AS `communicationtemplate_id`,`communicationtemplate`.`Name` AS `communicationtemplate_Name`,`communicationtemplate`.`Text` AS `communicationtemplate_Text`,`communicationtemplate`.`AlertSubTypeId` AS `communicationtemplate_AlertSubTypeId`,`communicationtemplate`.`LanguageCode` AS `communicationtemplate_LanguageCode`,`communicationtemplate`.`ChannelID` AS `communicationtemplate_ChannelID`,`communicationtemplate`.`Status_id` AS `communicationtemplate_Status_id`,`communicationtemplate`.`Subject` AS `communicationtemplate_Subject`,`communicationtemplate`.`SenderName` AS `communicationtemplate_SenderName`,`communicationtemplate`.`SenderEmail` AS `communicationtemplate_SenderEmail`,`channeltext`.`Description` AS `channeltext_Description`,`channeltext`.`softdeleteflag` AS `channeltext_softdeleteflag`,`communicationtemplate`.`softdeleteflag` AS `communicationtemplate_softdeleteflag` from (`communicationtemplate` left join `channeltext` on(((`communicationtemplate`.`ChannelID` = `channeltext`.`channelID`) and (`communicationtemplate`.`LanguageCode` = `channeltext`.`LanguageCode`))));

ALTER TABLE `csrassistgrant` 
ADD COLUMN `userName` VARCHAR(50) NULL AFTER `customerId`;

ALTER TABLE `customer` 
ADD COLUMN `IDState` VARCHAR(50) NULL AFTER `IDValue`,
ADD COLUMN `IDCountry` VARCHAR(50) NULL AFTER `IDState`,
ADD COLUMN `IDIssueDate` DATE NULL AFTER `IDCountry`,
ADD COLUMN `IDExpiryDate` DATE NULL AFTER `IDIssueDate`,
ADD COLUMN `MothersMaidenName` VARCHAR(50) NULL AFTER `isEagreementSigned`,
ADD COLUMN `AddressValidationStatus` VARCHAR(50) NULL AFTER `MothersMaidenName`,
ADD COLUMN `Product` VARCHAR(300) NULL AFTER `AddressValidationStatus`,
ADD COLUMN `EligbilityCriteria` TEXT NULL AFTER `Product`,
ADD COLUMN `Reason` TEXT NULL AFTER `EligbilityCriteria`,
ADD COLUMN `ApplicantChannel` VARCHAR(50) NULL AFTER `Reason`,
ADD COLUMN `DocumentsSubmitted` TEXT NULL AFTER `ApplicantChannel`;

DROP TABLE `applicantnote`;
DROP TABLE `applicant`;
DROP VIEW `applicant_view`;
DROP VIEW `applicantnotes_view`;

DROP VIEW IF EXISTS `customerbasicinfo_view`;
CREATE VIEW `customerbasicinfo_view` AS select `customer`.`UserName` AS `Username`,`customer`.`FirstName` AS `FirstName`,`customer`.`MiddleName` AS `MiddleName`,`customer`.`LastName` AS `LastName`,concat(ifnull(`customer`.`FirstName`,''),' ',ifnull(`customer`.`MiddleName`,''),' ',ifnull(`customer`.`LastName`,'')) AS `Name`,`customer`.`Salutation` AS `Salutation`,`customer`.`id` AS `Customer_id`,`customer`.`Ssn` AS `SSN`,`customer`.`createdts` AS `CustomerSince`,`customer`.`Gender` AS `Gender`,`customer`.`DateOfBirth` AS `DateOfBirth`,`customer`.`Status_id` AS `CustomerStatus_id`,`customerstatus`.`Description` AS `CustomerStatus_name`,`customer`.`MaritalStatus_id` AS `MaritalStatus_id`,`maritalstatus`.`Description` AS `MaritalStatus_name`,`customer`.`SpouseName` AS `SpouseName`,`customer`.`lockedOn` AS `lockedOn`,`customer`.`lockCount` AS `lockCount`,`customer`.`EmployementStatus_id` AS `EmployementStatus_id`,`employementstatus`.`Description` AS `EmployementStatus_name`,(select group_concat(`customerflagstatus`.`Status_id`,' ' separator ',') from `customerflagstatus` where (`customerflagstatus`.`Customer_id` = `customer`.`id`)) AS `CustomerFlag_ids`,(select group_concat(`status`.`Description`,' ' separator ',') from `status` where `status`.`id` in (select `customerflagstatus`.`Status_id` from `customerflagstatus` where (`customerflagstatus`.`Customer_id` = `customer`.`id`))) AS `CustomerFlag`,`customer`.`IsEnrolledForOlb` AS `IsEnrolledForOlb`,`customer`.`IsStaffMember` AS `IsStaffMember`,`customer`.`Location_id` AS `Branch_id`,`location`.`Name` AS `Branch_name`,`location`.`Code` AS `Branch_code`,`customer`.`IsOlbAllowed` AS `IsOlbAllowed`,`customer`.`IsAssistConsented` AS `IsAssistConsented`,`customer`.`isEagreementSigned` AS `isEagreementSigned`,`customer`.`CustomerType_id` AS `CustomerType_id`,`customertype`.`Name` AS `CustomerType_Name`,`customertype`.`Description` AS `CustomerType_Description`,(select `membergroup`.`Name` from `membergroup` where `membergroup`.`id` in (select `customergroup`.`Group_id` from `customergroup` where (`customer`.`id` = `customergroup`.`Customer_id`)) limit 1) AS `Customer_Role`,(select `membergroup`.`isEAgreementActive` from `membergroup` where `membergroup`.`id` in (select `customergroup`.`Group_id` from `customergroup` where (`customer`.`id` = `customergroup`.`Customer_id`)) limit 1) AS `isEAgreementRequired`,`customer`.`Organization_Id` AS `organisation_id`,`organisation`.`Name` AS `organisation_name`,any_value(`primaryphone`.`Value`) AS `PrimaryPhoneNumber`,any_value(`primaryemail`.`Value`) AS `PrimaryEmailAddress`,`customer`.`DocumentsSubmitted` AS `DocumentsSubmitted`,`customer`.`ApplicantChannel` AS `ApplicantChannel`,`customer`.`Product` AS `Product`,`customer`.`Reason` AS `Reason` from ((((((((`customer` left join `location` on((`customer`.`Location_id` = `location`.`id`))) left join `organisation` on((`customer`.`Organization_Id` = `organisation`.`id`))) left join `customertype` on((`customer`.`CustomerType_id` = `customertype`.`id`))) left join `status` `customerstatus` on((`customer`.`Status_id` = `customerstatus`.`id`))) left join `status` `maritalstatus` on((`customer`.`MaritalStatus_id` = `maritalstatus`.`id`))) left join `status` `employementstatus` on((`customer`.`EmployementStatus_id` = `employementstatus`.`id`))) left join `customercommunication` `primaryphone` on(((`primaryphone`.`Customer_id` = `customer`.`id`) and (`primaryphone`.`isPrimary` = 1) and (`primaryphone`.`Type_id` = 'COMM_TYPE_PHONE')))) left join `customercommunication` `primaryemail` on(((`primaryemail`.`Customer_id` = `customer`.`id`) and (`primaryemail`.`isPrimary` = 1) and (`primaryemail`.`Type_id` = 'COMM_TYPE_EMAIL')))) group by `customer`.`id`;

CREATE TABLE `actiondisplaynamedescription` (
  `Action_id` VARCHAR(50) NOT NULL,
  `Locale_id` VARCHAR(50) NOT NULL,
  `DisplayName` TEXT NOT NULL,
  `DisplayDescription` TEXT NOT NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`Action_id`, `Locale_id`),
  INDEX `FK_actiondisplaynamedescription_Locale_id_idx` (`Locale_id` ASC),
  CONSTRAINT `FK_actiondisplaynamedescription_Action_is`
    FOREIGN KEY (`Action_id`)
    REFERENCES `service` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_actiondisplaynamedescription_Locale_id`
    FOREIGN KEY (`Locale_id`)
    REFERENCES `locale` (`Code`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `featuredisplaynamedescription` (
  `Feature_id` VARCHAR(50) NOT NULL,
  `Locale_id` VARCHAR(50) NOT NULL,
  `DisplayName` TEXT NOT NULL,
  `DisplayDescription` TEXT NOT NULL,
  `createdby` VARCHAR(50) NULL DEFAULT NULL,
  `modifiedby` VARCHAR(50) NULL DEFAULT NULL,
  `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`Feature_id`, `Locale_id`),
  INDEX `FK_featuredisplaynamedescription_Locale_id_idx` (`Locale_id` ASC),
  CONSTRAINT `FK_featuredisplaynamedescription_Feature_id`
    FOREIGN KEY (`Feature_id`)
    REFERENCES `feature` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_featuredisplaynamedescription_Locale_id`
    FOREIGN KEY (`Locale_id`)
    REFERENCES `locale` (`Code`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `csrassistgrant` DROP FOREIGN KEY `FK_CSRAssistGrant_Role`;
ALTER TABLE `csrassistgrant` DROP INDEX `FK_CSRAssistGrant_Role_idx` ;

CREATE TABLE `mfavariablereference` (
  `Code` varchar(255) NOT NULL,
  `Name` varchar(255) DEFAULT NULL,
  `createdby` varchar(255) DEFAULT NULL,
  `modifiedby` varchar(255) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`Code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `csrassistgrant` DROP FOREIGN KEY `FK_CSRAssistGrant_SystemUser`;
ALTER TABLE `csrassistgrant` DROP INDEX `FK_CSRAssistGrant_SystemUser_idx` ;

CREATE TABLE `countrycode` (
  `id` varchar(50) NOT NULL,
  `Code` varchar(50) NOT NULL,
  `Name` varchar(250) NOT NULL,
  `ISDCode` varchar(50) NOT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE `policies`;

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
            AND alerthistory.Customer_Id =_customerId) order by alerthistory.DispatchDate limit 800;
END$$
DELIMITER ;

DROP TABLE IF EXISTS `customersecurityquestion`;