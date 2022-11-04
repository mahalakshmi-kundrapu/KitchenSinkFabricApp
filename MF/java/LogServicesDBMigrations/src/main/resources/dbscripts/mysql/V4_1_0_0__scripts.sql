ALTER TABLE `customeractivity` 
ADD COLUMN `customerId` VARCHAR(50) NULL AFTER `createdOn`,
ADD COLUMN `typeOfMFA` VARCHAR(50) NULL AFTER `customerId`,
ADD COLUMN `payeeName` VARCHAR(50) NULL AFTER `typeOfMFA`,
ADD COLUMN `accountNumber` VARCHAR(50) NULL AFTER `payeeName`,
ADD COLUMN `relationshipNumber` VARCHAR(50) NULL AFTER `accountNumber`,
ADD COLUMN `phoneNumber` VARCHAR(50) NULL AFTER `relationshipNumber`,
ADD COLUMN `email` VARCHAR(50) NULL AFTER `phoneNumber`,
ADD COLUMN `bankName` VARCHAR(50) NULL AFTER `email`,
ADD COLUMN `maskedAccountNumber` VARCHAR(50) NULL AFTER `bankName`;

ALTER TABLE `transactionlog` 
ADD COLUMN `module` VARCHAR(255) NULL AFTER `createdOn`,
ADD COLUMN `customerId` VARCHAR(50) NULL AFTER `module`,
ADD COLUMN `device` VARCHAR(50) NULL AFTER `customerId`,
ADD COLUMN `operatingSystem` VARCHAR(50) NULL AFTER `device`,
ADD COLUMN `deviceId` VARCHAR(50) NULL AFTER `operatingSystem`,
ADD COLUMN `ipAddress` VARCHAR(50) NULL AFTER `deviceId`,
ADD COLUMN `referenceNumber` VARCHAR(50) NULL AFTER `ipAddress`,
ADD COLUMN `transactionDescription` VARCHAR(1000) NULL AFTER `referenceNumber`,
ADD COLUMN `errorCode` VARCHAR(50) NULL AFTER `transactionDescription`,
ADD COLUMN `recipientType` VARCHAR(50) NULL AFTER `errorCode`,
ADD COLUMN `recipientBankName` VARCHAR(50) NULL AFTER `recipientType`,
ADD COLUMN `recipientAddress` VARCHAR(1000) NULL AFTER `recipientBankName`,
ADD COLUMN `recipientBankAddress` VARCHAR(1000) NULL AFTER `recipientAddress`,
ADD COLUMN `checkNumber` VARCHAR(50) NULL AFTER `recipientBankAddress`,
ADD COLUMN `cashWithdrawalFor` VARCHAR(50) NULL AFTER `checkNumber`;

ALTER TABLE `transactionlog` CHANGE COLUMN `ibanCode` `internationalRoutingCode` VARCHAR(50) NULL DEFAULT NULL ;

