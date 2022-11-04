DROP PROCEDURE IF EXISTS `DMS_create_user_proc`;
DELIMITER $$
CREATE PROCEDURE `DMS_create_user_proc`(
  in _username varchar(50), 
  in _group varchar(50)
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
