INSERT IGNORE INTO `address` (`id`, `Region_id`, `City_id`, `addressLine1`, `addressLine2`, `addressLine3`, `zipCode`, `latitude`, `logitude`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('DMSADDR1','R57','CITY1318','Ap #867-859 ','Sit Rd.',NULL,'CF243DG','89709880','8908098098','Kony User','Kony Dev',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0),('DMSADDR2','R52','CITY1289','P.O. Box 283 8562','Fusce Rd.',NULL,'20620','89709880','8908098098','Kony User','Kony Dev',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
INSERT IGNORE INTO `location` (`id`, `Type_id`, `Code`, `Name`, `DisplayName`, `Description`, `PhoneNumber`, `EmailId`, `Address_id`, `Status_id`, `WorkingDays`, `WorkSchedule_id`, `IsMainBranch`, `MainBranchCode`, `WebSiteUrl`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('LID1','Branch','007','New York Service Center Branch','New York Service Center Branch','New York Service Center Branch Description','134-512-5324','nycservice@kony.com','DMSADDR1','SID_ACTIVE',NULL,'WORK_SCH_ID1',0,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
INSERT IGNORE INTO `customer` (`id`, `Classification_id`, `FirstName`, `MiddleName`, `LastName`, `Status_id`, `Username`, `Password`, `unsuccessfulLoginAttempts`, `LockCount`, `Salutation`, `Gender`, `DateOfBirth`, `DrivingLicenseNumber`, `Ssn`, `Cvv`, `Token`, `Pin`, `PreferredContactMethod`, `PreferredContactTime`, `MaritalStatus_id`, `SpouseName`, `NoOfDependents`, `Role`, `EmployementStatus_id`, `UserCompany`, `SecurityImage_id`, `Location_id`, `IsOlbAllowed`, `IsStaffMember`, `CountryCode`, `UserImage`, `UserImageURL`, `OlbEnrollmentStatus_id`, `Otp`, `PreferedOtpMethod`, `OtpGenaratedts`, `ValidDate`, `isUserAccountLocked`, `IsPinSet`, `IsEnrolledForOlb`, `IsAssistConsented`, `IsPhoneEnabled`, `IsEmailEnabled`, `isEnrolled`, `isSuperAdmin`, `CurrentLoginTime`, `Lastlogintime`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('1',NULL,'John','Bailey','Lenna','SID_CUS_ACTIVE','dbpolbuser','kony1234',NULL,NULL,'Mr.','Male','1999-11-11 00:00:00',NULL,'122432212',NULL,NULL,NULL,'Call, Email','Morning, Afternoon','SID_MARRIED','',NULL,NULL,'SID_EMPLOYED',NULL,NULL,'LID1',1,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0,1,1,0,0,0,0,NULL,NULL,'Kony User','admin2',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0),('2',NULL,'John','Bailey','rbuser','SID_CUS_ACTIVE','dbprbuser','kony1234',NULL,NULL,'Mr.','Male','1999-11-11 00:00:00',NULL,'122432212',NULL,NULL,NULL,'Call, Email','Morning, Afternoon','SID_MARRIED','',NULL,NULL,'SID_EMPLOYED',NULL,NULL,'LID1',1,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0,1,0,0,0,0,0,NULL,NULL,'Kony User','admin2',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
INSERT IGNORE INTO `customergroup` (`Customer_id`, `Group_id`) VALUES ('1', 'DEFAULT_GROUP');
INSERT IGNORE INTO `customergroup` (`Customer_id`, `Group_id`) VALUES ('2', 'DEFAULT_GROUP');

UPDATE membergroup SET Name='Priority Customers' WHERE id='GID_PRIORITY_MEMBERS';
UPDATE alert SET Description='Customer''s online banking account is now active/unlocked for use' WHERE id='ALERT_ID_1_4_USER_UNLOCK';
UPDATE role SET Description='This role has minimal permissions that help the users perform the dedicated tasks in the admin console. Branch staffs who want to support customers with OLB and Mobile app can also be associated with this role' WHERE id='RID_FRONTLINE_STAFF';
UPDATE permission SET Description='To use the CSR Assist functionality for a selected customer' WHERE id='PID45';
UPDATE permission SET Name='CreateCustomer', Description='To create a new customer - by uploading/importing CSV' WHERE id='PID41';
UPDATE permission SET Name='UpdateCustomerEntitlements', Description='Permission to add new entitlements for a selected customer' WHERE id='PID12';
UPDATE permission SET Name='UpdateCustomerDevice', Description='Permission to manage a selected customer''s devices for OLB and MB' WHERE id='PID11';
UPDATE permission SET Name='UpdateCustomerContact', Description='Permission to update a selected customer''s contact details' WHERE id='PID10';
UPDATE permission SET Name='ViewCustomer', Description='Permission to search for a customer and view his details' WHERE id='PID09';
UPDATE permission SET Name='UpdateCustomer', Description='Permission to search for any customer and update their details' WHERE id='PID03';
UPDATE permission SET Name='AssignCustomerGroup', Description='Permission to add customers to a group. This deals mainly with going to a group' WHERE id='PID07';
UPDATE permission SET Name='UpdateCustomerGroup', Description='Permission to update a customer''s group (add and remove) from the customer''s details page' WHERE id='PID08';
UPDATE permission SET Description='Permission to create a new customer group' WHERE id='PID04';
UPDATE permission SET Description='Permission to view the list and details of customer groups' WHERE id='PID05';

INSERT INTO otherproducttype (`id`, `Name`, `Description`, `createdby`, `modifiedby`) VALUES ('OTHER_PRODUCT_TYPE_ID1', 'Savings account', 'Savings account', 'Kony user', 'Kony user');
UPDATE product set OtherProductType_id = 'OTHER_PRODUCT_TYPE_ID1';
UPDATE `product` SET `MarketingStateId` = '1' WHERE (`id` = 'PRODUCT1');
UPDATE `product` SET `MarketingStateId` = '2' WHERE (`id` = 'PRODUCT10');
UPDATE `product` SET `MarketingStateId` = '3' WHERE (`id` = 'PRODUCT11');
UPDATE `product` SET `MarketingStateId` = '4' WHERE (`id` = 'PRODUCT12');
UPDATE `product` SET `MarketingStateId` = '5' WHERE (`id` = 'PRODUCT13');
UPDATE `product` SET `MarketingStateId` = '6' WHERE (`id` = 'PRODUCT14');
UPDATE `product` SET `MarketingStateId` = '7' WHERE (`id` = 'PRODUCT2');
UPDATE `product` SET `MarketingStateId` = '8' WHERE (`id` = 'PRODUCT3');
UPDATE `product` SET `MarketingStateId` = '9' WHERE (`id` = 'PRODUCT4');
UPDATE `product` SET `MarketingStateId` = '10' WHERE (`id` = 'PRODUCT5');
UPDATE `product` SET `MarketingStateId` = '11' WHERE (`id` = 'PRODUCT6');
UPDATE `product` SET `MarketingStateId` = '12' WHERE (`id` = 'PRODUCT7');
UPDATE `product` SET `MarketingStateId` = '13' WHERE (`id` = 'PRODUCT8');
UPDATE `product` SET `MarketingStateId` = '14' WHERE (`id` = 'PRODUCT9');
UPDATE product set SecondaryProduct_id = 'PRODUCT1';

INSERT INTO `country` (`id`, `Code`, `Name`, `createdby`, `modifiedby`) VALUES ('COUNTRY_ID5', 'EUR', 'Europe', 'Kony User', 'Kony Dev');
INSERT INTO `membergroup` (`id`, `Name`, `Description`, `Status_id`, `createdby`, `modifiedby`) VALUES ('DEFAULT_EUROPE_GROUP', 'Europe Customers Group', 'All customers created for online and Mobile Banking in the Europe region will be assigned to this group automatically', 'SID_ACTIVE', 'Kony User', 'Kony Dev');
INSERT  INTO `groupentitlement` (`Group_id`, `Service_id`, `TransactionFee_id`, `TransactionLimit_id`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('DEFAULT_EUROPE_GROUP','SERVICE_ID_2','TID1','TID1','Kony Dev',NULL,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
INSERT  INTO `groupentitlement` (`Group_id`, `Service_id`, `TransactionFee_id`, `TransactionLimit_id`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('DEFAULT_EUROPE_GROUP','SERVICE_ID_3','TID1','TID1','Kony Dev',NULL,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
INSERT  INTO `groupentitlement` (`Group_id`, `Service_id`, `TransactionFee_id`, `TransactionLimit_id`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('DEFAULT_EUROPE_GROUP','SERVICE_ID_4','TID1','TID1','Kony Dev',NULL,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
INSERT  INTO `groupentitlement` (`Group_id`, `Service_id`, `TransactionFee_id`, `TransactionLimit_id`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('DEFAULT_EUROPE_GROUP','SERVICE_ID_7','TID1','TID1','Kony Dev',NULL,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
INSERT  INTO `groupentitlement` (`Group_id`, `Service_id`, `TransactionFee_id`, `TransactionLimit_id`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('DEFAULT_EUROPE_GROUP','SERVICE_ID_8','TID1','TID1','Kony Dev',NULL,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);

INSERT INTO `region` (`id`, `Country_id`, `Code`, `Name`, `createdby`, `modifiedby`) VALUES ('REGION_ID86', 'COUNTRY_ID5', 'TR', 'Tirana', 'Kony User', 'Kony Dev');
INSERT INTO `region` (`id`, `Country_id`, `Code`, `Name`, `createdby`, `modifiedby`) VALUES ('REGION_ID87', 'COUNTRY_ID5', 'OL', 'Oslo', 'Kony User', 'Kony Dev');

INSERT INTO `city` (`id`, `Region_id`, `Country_id`, `Name`, `createdby`, `modifiedby`) VALUES ('C4', 'REGION_ID86', 'COUNTRY_ID5', 'Albania', 'Kony Dev', 'Kony Dev');
INSERT INTO `city` (`id`, `Region_id`, `Country_id`, `Name`, `createdby`, `modifiedby`) VALUES ('C5', 'REGION_ID87', 'COUNTRY_ID5', 'Norway', 'Kony Dev', 'Kony Dev');

INSERT INTO `status` (`id`, `Type_id`, `Code`, `Description`, `createdby`, `modifiedby`, `createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('SID_APP_FAILED','STID_APPLICANTSTATUS',NULL,'Failed Application','Kony User','Kony Dev',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);

INSERT INTO `idtype` (`IDType`, `IDName`) VALUES ('ID_DRIVING_LICENSE', 'Driving License');
INSERT INTO `idtype` (`IDType`, `IDName`) VALUES ('ID_PASSPORT', 'Passport');

INSERT INTO `eligibilitycriteria` (`id`, `Description`, `Status_id`, `createdby`, `modifiedby`,`createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('KONY_DBX_BANK_MEMBER_ACQ', 'I am related to a current Kony DBX bank member.(Eligibile relationships include spouse,domestic partner,', 'SID_ACTIVE', 'Kony User', 'Kony Dev',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
INSERT INTO `eligibilitycriteria` (`id`, `Description`, `Status_id`, `createdby`, `modifiedby`,`createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('KONY_DBX_BANK_CLIENT_ACQ', 'I am an employee ,retiree or family memeber of an employee of a company Kony DBX bank serves.', 'SID_ACTIVE', 'Kony User', 'Kony Dev',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
INSERT INTO `eligibilitycriteria` (`id`, `Description`, `Status_id`, `createdby`, `modifiedby`,`createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('KONY_DBX_BANK_MEMBER', 'I am a member of a membership organization Kony DBX bank Servers.', 'SID_ACTIVE', 'Kony User', 'Kony Dev',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);
INSERT INTO `eligibilitycriteria` (`id`, `Description`, `Status_id`, `createdby`, `modifiedby`,`createdts`, `lastmodifiedts`, `synctimestamp`, `softdeleteflag`) VALUES ('KONY_DBX_BANK_AREA_RESIDENT', 'I live,work,worship or attend school in one of these communities', 'SID_ACTIVE', 'Kony User', 'Kony Dev',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0);

INSERT INTO `configurationbundles` (`bundle_id`, `bundle_name`, `app_id`, `createdby`, `modifiedby`) VALUES ('DBP_CONFIG_BUNDLE', 'DBP', 'DBP', 'Kony dev', 'Kony dev');
INSERT INTO `configurations` (`configuration_id`, `bundle_id`, `config_type`, `config_key`, `description`, `config_value`, `target`, `createdby`, `modifiedby`) VALUES ('1', 'DBP_CONFIG_BUNDLE', '1', 'AUTO_UNLOCK_DURATION', 'Time to automatically unlock a customer.(in minutes)', '30', 'Server', 'Kony dev', 'Kony dev');

INSERT INTO `configurationbundles` (`bundle_id`, `bundle_name`, `app_id`, `createdby`, `modifiedby`) VALUES ('NUO_CONFIG_BUNDLE', 'NUO', 'NUO', 'Kony dev', 'Kony dev');
INSERT INTO `configurations` (`configuration_id`, `bundle_id`, `config_type`, `config_key`, `description`, `config_value`, `target`, `createdby`, `modifiedby`) VALUES ('2', 'NUO_CONFIG_BUNDLE', '1', 'DEFAULT_ACC_TYPE', 'Default Account Type Assigned to a New User', 'Savings', 'Server', 'Kony dev', 'Kony dev');
INSERT INTO `configurations` (`configuration_id`, `bundle_id`, `config_type`, `config_key`, `description`, `config_value`, `target`, `createdby`, `modifiedby`) VALUES ('3', 'NUO_CONFIG_BUNDLE', '1', 'AUTO_FUNDING_AMOUNT', 'Default Amount Added to a new Account', '5', 'Server', 'Kony dev', 'Kony dev');
INSERT INTO `configurations` (`configuration_id`, `bundle_id`, `config_type`, `config_key`, `description`, `config_value`, `target`, `createdby`, `modifiedby`) VALUES ('4', 'NUO_CONFIG_BUNDLE', '1', 'LAST_REJECTION_DATE', 'Duration after which a rejected applicant can re-apply(in months).', '6', 'Server', 'Kony dev', 'Kony dev');
