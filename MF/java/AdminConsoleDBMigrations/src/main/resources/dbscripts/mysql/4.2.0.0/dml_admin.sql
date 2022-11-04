DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_AUTHORIZER') and (`Service_id` = 'SERVICE_ID_18');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_AUTHORIZER') and (`Service_id` = 'SERVICE_ID_21');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_AUTHORIZER') and (`Service_id` = 'SERVICE_ID_25');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_AUTHORIZER') and (`Service_id` = 'SERVICE_ID_29');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_AUTHORIZER') and (`Service_id` = 'SERVICE_ID_33');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_AUTHORIZER') and (`Service_id` = 'SERVICE_ID_37');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_AUTHORIZER') and (`Service_id` = 'SERVICE_ID_51');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_AUTHORIZER') and (`Service_id` = 'SERVICE_ID_55');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_AUTHORIZER') and (`Service_id` = 'SERVICE_ID_15');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_AUTHORIZER') and (`Service_id` = 'SERVICE_ID_35');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_AUTHORIZER') and (`Service_id` = 'SERVICE_ID_36');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_AUTHORIZER') and (`Service_id` = 'SERVICE_ID_38');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_ADMINISTRATOR') and (`Service_id` = 'SERVICE_ID_15');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_VIEWER') and (`Service_id` = 'SERVICE_ID_16');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_VIEWER') and (`Service_id` = 'SERVICE_ID_23');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_VIEWER') and (`Service_id` = 'SERVICE_ID_27');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_VIEWER') and (`Service_id` = 'SERVICE_ID_31');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_VIEWER') and (`Service_id` = 'SERVICE_ID_35');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_VIEWER') and (`Service_id` = 'SERVICE_ID_46');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_VIEWER') and (`Service_id` = 'SERVICE_ID_49');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_VIEWER') and (`Service_id` = 'SERVICE_ID_53');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_CREATOR') and (`Service_id` = 'SERVICE_ID_15');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_CREATOR') and (`Service_id` = 'SERVICE_ID_35');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_CREATOR') and (`Service_id` = 'SERVICE_ID_38');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_ADMINISTRATOR') and (`Service_id` = 'SERVICE_ID_35');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_ADMINISTRATOR') and (`Service_id` = 'SERVICE_ID_36');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_ADMINISTRATOR') and (`Service_id` = 'SERVICE_ID_37');
DELETE FROM `groupentitlement` WHERE (`Group_id` = 'GROUP_ADMINISTRATOR') and (`Service_id` = 'SERVICE_ID_38');

INSERT INTO `groupentitlement` (`Group_id`, `Service_id`, `createdby`) VALUES ('GROUP_AUTHORIZER', 'SERVICE_ID_59', 'UID11');
INSERT INTO `groupentitlement` (`Group_id`, `Service_id`, `createdby`) VALUES ('GROUP_AUTHORIZER', 'SERVICE_ID_60', 'UID11');
INSERT INTO `groupentitlement` (`Group_id`, `Service_id`, `createdby`) VALUES ('GROUP_AUTHORIZER', 'SERVICE_ID_62', 'UID11');
INSERT INTO `groupentitlement` (`Group_id`, `Service_id`, `createdby`) VALUES ('GROUP_AUTHORIZER', 'SERVICE_ID_63', 'UID11');
INSERT INTO `groupentitlement` (`Group_id`, `Service_id`, `createdby`) VALUES ('GROUP_AUTHORIZER', 'SERVICE_ID_64', 'UID11');
INSERT INTO `groupentitlement` (`Group_id`, `Service_id`, `createdby`) VALUES ('GROUP_AUTHORIZER', 'SERVICE_ID_66', 'UID11');
INSERT INTO `groupentitlement` (`Group_id`, `Service_id`, `TransactionFee_id`, `TransactionLimit_id`, `createdby`) VALUES ('GROUP_AUTHORIZER', 'SERVICE_ID_57', 'TID1', 'TID3', 'UID11');
INSERT INTO `groupentitlement` (`Group_id`, `Service_id`, `TransactionFee_id`, `TransactionLimit_id`, `createdby`) VALUES ('GROUP_AUTHORIZER', 'SERVICE_ID_58', 'TID1', 'TID3', 'UID11');
INSERT INTO `groupentitlement` (`Group_id`, `Service_id`, `TransactionFee_id`, `TransactionLimit_id`, `createdby`) VALUES ('GROUP_CREATOR', 'SERVICE_ID_48', 'TID1', 'TID9', 'UID11');

INSERT INTO `customernotification` (`Customer_id`, `Notification_id`, `IsRead`, `createdby`, `modifiedby`) VALUES ('1', 'NID1', '1', 'Kony user', 'Kony user');
INSERT INTO `customernotification` (`Customer_id`, `Notification_id`, `IsRead`, `createdby`, `modifiedby`) VALUES ('1', 'NID2', '1', 'Kony user', 'Kony user');
INSERT INTO `customernotification` (`Customer_id`, `Notification_id`, `IsRead`, `createdby`, `modifiedby`) VALUES ('1', 'NID3', '1', 'Kony user', 'Kony user');

UPDATE `adminnotification` SET `StartDate` = '2019-01-22', `ExpirationDate` = '2020-02-22' WHERE (`Id` = 'NID1');
UPDATE `adminnotification` SET `StartDate` = '2019-01-22', `ExpirationDate` = '2020-02-22' WHERE (`Id` = 'NID10');
UPDATE `adminnotification` SET `StartDate` = '2019-01-22', `ExpirationDate` = '2020-02-22' WHERE (`Id` = 'NID12');
UPDATE `adminnotification` SET `StartDate` = '2019-01-22', `ExpirationDate` = '2020-02-22' WHERE (`Id` = 'NID11');
UPDATE `adminnotification` SET `StartDate` = '2019-01-22', `ExpirationDate` = '2020-02-22' WHERE (`Id` = 'NID13');
UPDATE `adminnotification` SET `StartDate` = '2019-01-22', `ExpirationDate` = '2020-02-22' WHERE (`Id` = 'NID14');
UPDATE `adminnotification` SET `StartDate` = '2019-01-22', `ExpirationDate` = '2020-02-22' WHERE (`Id` = 'NID2');
UPDATE `adminnotification` SET `StartDate` = '2019-01-22', `ExpirationDate` = '2020-02-22' WHERE (`Id` = 'NID3');
UPDATE `adminnotification` SET `StartDate` = '2019-01-22', `ExpirationDate` = '2020-02-22' WHERE (`Id` = 'NID4');
UPDATE `adminnotification` SET `StartDate` = '2019-01-22', `ExpirationDate` = '2020-02-22' WHERE (`Id` = 'NID5');
UPDATE `adminnotification` SET `StartDate` = '2019-01-22', `ExpirationDate` = '2020-02-22' WHERE (`Id` = 'NID6');
UPDATE `adminnotification` SET `StartDate` = '2019-01-22', `ExpirationDate` = '2020-02-22' WHERE (`Id` = 'NID7');
UPDATE `adminnotification` SET `StartDate` = '2019-01-22', `ExpirationDate` = '2020-02-22' WHERE (`Id` = 'NID8');
UPDATE `adminnotification` SET `StartDate` = '2019-01-22', `ExpirationDate` = '2020-02-22' WHERE (`Id` = 'NID9');