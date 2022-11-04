ALTER TABLE `feedback` CHANGE COLUMN `user_id` `user_id` VARCHAR(50) NULL DEFAULT NULL ;

CREATE TABLE `customerexpense` (
  `Amount` decimal(20,2) DEFAULT NULL,
  `id` varchar(50) NOT NULL,
  `Type` varchar(50) NOT NULL,
  `Customer_id` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET=utf8;

DROP VIEW IF EXISTS `customeraddressmbview`;
CREATE VIEW `customeraddressmbview` AS
    SELECT 
        `ca`.`Customer_id` AS `CustomerId`,
        `ca`.`Address_id` AS `Address_id`,
        `ca`.`Type_id` AS `Type_id`,
        `ca`.`DurationOfStay` AS `DurationOfStay`,
        `ca`.`HomeOwnership` AS `HomeOwnership`,
        `ca`.`isPrimary` AS `isPrimary`,
        `at`.`Description` AS `AddressType`,
        `a`.`addressLine1` AS `AddressLine1`,
        `a`.`addressLine2` AS `AddressLine2`,
        `a`.`addressLine3` AS `AddressLine3`,
        `a`.`zipCode` AS `ZipCode`,
        `a`.`cityName` AS `CityName`,
        `a`.`country` AS `CountryName`,
        `a`.`state` AS `State`
    FROM
        ((`customeraddress` `ca`
        LEFT JOIN `address` `a` ON ((`a`.`id` = `ca`.`Address_id`)))
        LEFT JOIN `addresstype` `at` ON ((`ca`.`Type_id` = `at`.`id`)))
    WHERE
        (`a`.`softdeleteflag` = '0');

DROP PROCEDURE IF EXISTS `get_customer_applicantinfo_proc`;
DELIMITER $$
CREATE PROCEDURE `get_customer_applicantinfo_proc`(IN `Customer_id` varchar(20000))
BEGIN
Select `customer`.`id` AS Customer_id,
`customer`.`FirstName` AS FirstName,
`customer`.`MiddleName` AS MiddleName,
`customer`.`LastName` AS LastName,
`customer`.`DateOfBirth` AS DateOfBirth,
`customer`.`Ssn` AS Ssn,
`customer`.`PreferredContactMethod` AS PreferredContactMethod,
(Select `customercommunication`.`Value` from customercommunication where `customercommunication`.Type_id = 'COMM_TYPE_PHONE' AND `customercommunication`.`isPrimary`=1 AND BINARY `customercommunication`.`Customer_id`= BINARY `Customer_id` limit 1) as Phone,
(Select `customercommunication`.`Value` from customercommunication where `customercommunication`.Type_id = 'COMM_TYPE_EMAIL' AND `customercommunication`.`isPrimary`=1 AND BINARY `customercommunication`.`Customer_id`= BINARY `Customer_id` limit 1) as Email
FROM
	customer customer
WHERE
	BINARY customer.id=BINARY `Customer_id`;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `transaction_proc_get`;
DELIMITER $$
CREATE PROCEDURE `transaction_proc_get`(in transactions_query varchar(20000))
BEGIN
SET @stmt := CONCAT('', transactions_query);
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
END$$
DELIMITER ;
