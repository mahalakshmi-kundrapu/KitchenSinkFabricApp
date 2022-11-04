/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

DROP TABLE IF EXISTS `adminactivity`;
CREATE TABLE `adminactivity` (
  `id` varchar(50) NOT NULL,
  `event` varchar(50) DEFAULT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `username` varchar(50) DEFAULT NULL,
  `userRole` varchar(50) DEFAULT NULL,
  `moduleName` varchar(50) DEFAULT NULL,
  `eventts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` varchar(50) DEFAULT NULL,
  `createdBy` varchar(50) DEFAULT NULL,
  `createdOn` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_systemuseractivity_eventts` (`eventts`),
  KEY `idx_systemuseractivity_modulename` (`moduleName`),
  KEY `idx_systemuseractivity_username` (`username`),
  KEY `idx_systemuseractivity_userrole` (`userRole`),
  KEY `idx_systemuseractivity_event` (`event`),
  KEY `idx_systemuseractivity_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `admincustomeractivity`;
CREATE TABLE `admincustomeractivity` (
  `id` varchar(50) NOT NULL,
  `customerId` varchar(50) DEFAULT NULL,
  `adminName` varchar(255) DEFAULT NULL,
  `adminRole` varchar(50) DEFAULT NULL,
  `activityType` varchar(50) DEFAULT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `eventts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` varchar(50) DEFAULT NULL,
  `createdBy` varchar(50) DEFAULT NULL,
  `createdOn` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_admincustomeractivity_adminname` (`adminName`),
  KEY `idx_admincustomeractivity_activitytype` (`activityType`),
  KEY `idx_admincustomeractivity_eventts` (`eventts`),
  KEY `idx_admincustomeractivity_adminrole` (`adminRole`),
  KEY `idx_admincustomeractivity_customerusername` (`customerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `customeractivity`;
CREATE TABLE `customeractivity` (
  `id` varchar(50) NOT NULL,
  `username` varchar(50) DEFAULT NULL,
  `moduleName` varchar(100) DEFAULT NULL,
  `activityType` varchar(50) DEFAULT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `eventts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` varchar(50) DEFAULT NULL,
  `channel` varchar(50) DEFAULT NULL,
  `ipAddress` varchar(50) DEFAULT NULL,
  `device` varchar(50) DEFAULT NULL,
  `operatingSystem` varchar(50) DEFAULT NULL,
  `referenceId` varchar(50) DEFAULT NULL,
  `errorCode` varchar(50) DEFAULT NULL,
  `createdBy` varchar(50) DEFAULT NULL,
  `createdOn` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_customeractivity_modulename` (`moduleName`),
  KEY `idx_customeractivity_eventts` (`eventts`),
  KEY `idx_customeractivity_activitytype` (`activityType`),
  KEY `idx_customeractivity_username` (`username`),
  KEY `idx_customeractivity_status` (`status`),
  KEY `idx_customeractivity_channel` (`channel`),
  KEY `idx_customeractivity_operatingsystem` (`operatingSystem`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `transactionlog`;
CREATE TABLE `transactionlog` (
  `id` varchar(50) NOT NULL,
  `transactionId` varchar(50) DEFAULT NULL,
  `username` varchar(50) DEFAULT NULL,
  `payeeName` varchar(50) DEFAULT NULL,
  `serviceName` varchar(50) DEFAULT NULL,
  `type` varchar(50) DEFAULT NULL,
  `fromAccount` varchar(50) DEFAULT NULL,
  `fromAccountType` varchar(50) DEFAULT NULL,
  `toAccount` varchar(50) DEFAULT NULL,
  `toAccountType` varchar(50) DEFAULT NULL,
  `amount` decimal(20,2) DEFAULT NULL,
  `currencyCode` varchar(50) DEFAULT NULL,
  `channel` varchar(50) DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `routingNumber` varchar(50) DEFAULT NULL,
  `batchId` varchar(45) DEFAULT NULL,
  `transactionDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fromMobileOrEmail` varchar(50) DEFAULT NULL,
  `toMobileOrEmail` varchar(50) DEFAULT NULL,
  `swiftCode` varchar(50) DEFAULT NULL,
  `ibanCode` varchar(50) DEFAULT NULL,
  `createdBy` varchar(50) DEFAULT NULL,
  `createdOn` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_transactionlog_type` (`type`),
  KEY `idx_transactionlog_transactiondate` (`transactionDate`),
  KEY `idx_transactionlog_amount` (`amount`),
  KEY `idx_transactionlog_username` (`username`),
  KEY `idx_transactionlog_transactionid` (`transactionId`),
  KEY `idx_transactionlog_servicename` (`serviceName`),
  KEY `idx_transactionlog_fromaccounttype` (`fromAccountType`),
  KEY `idx_transactionlog_toaccounttype` (`toAccountType`),
  KEY `idx_transactionlog_currency` (`currencyCode`),
  KEY `idx_transactionlog_status` (`status`),
  KEY `idx_transactionlog_frommobileemail` (`fromMobileOrEmail`),
  KEY `idx_transactionlog_tomobileemail` (`toMobileOrEmail`),
  KEY `idx_transactionlog_payeename` (`payeeName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;