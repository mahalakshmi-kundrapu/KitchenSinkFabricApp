DROP PROCEDURE IF EXISTS `DMS_create_user_proc`;
DELIMITER $$
CREATE PROCEDURE `DMS_create_user_proc`(
  in _username char(50)
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
SELECT SYSDATE() + 1 INTO customerId;
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
    customerId, 'GROUP_TESTING', 'Kony User', 
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
END $$ 
DELIMITER ;