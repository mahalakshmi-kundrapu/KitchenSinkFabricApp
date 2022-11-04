DROP PROCEDURE IF EXISTS `get_customer_employement_details_proc`;
DELIMITER $$
CREATE PROCEDURE `get_customer_employement_details_proc`(IN Customer_id varchar(20000))
BEGIN
Select 
`employementdetails`.`Customer_id` AS Customer_id,
`employementdetails`.`EmploymentType` AS EmploymentType,
`employementdetails`.`CurrentEmployer` AS CurrentEmployer,
`employementdetails`.`Designation` AS Designation,
`employementdetails`.`PayPeriod` AS PayPeriod,
`employementdetails`.`GrossIncome` AS GrossIncome,
`employementdetails`.`WeekWorkingHours` AS WeekWorkingHours,
`employementdetails`.`EmploymentStartDate` AS EmployementStartDate,
`employementdetails`.`PreviousEmployer` AS PreviousEmployer,
`employementdetails`.`OtherEmployementType` AS OtherEmployementType,
`employementdetails`.`OtherEmployementDescription` AS OtherEmployementDescription,
`employementdetails`.`PreviousDesignation` AS PreviousDesignation,
`othersourceofincome`.`SourceType` AS OtherIncomeSourceType,
`othersourceofincome`.`PayPeriod` AS OtherIncomeSourcePayPeriod,
`othersourceofincome`.`GrossIncome` AS OtherGrossIncomeValue,
`othersourceofincome`.`WeekWorkingHours` AS OtherIncomeSourceWorkingHours,
`othersourceofincome`.`SourceOfIncomeDescription` AS OtherSourceOfIncomeDescription,
`othersourceofincome`.`SourceOfIncomeName` AS OtherSourceOfIncomeName,
`customeraddress`.`Address_id` AS Address_id,
`customeraddress`.`Type_id` AS Type_id,
`address`.`Region_id` AS Region_id,
`address`.`City_id` AS City_id,
`address`.`AddressLine1` AS AddressLine1,
`address`.`AddressLine2` AS AddressLine2,
`address`.`AddressLine3` AS AddressLine3,
`address`.`zipCode` AS ZipCode,
`address`.`state` AS State,
`address`.`cityName` AS city,
`address`.`country` AS Country
FROM
                employementdetails employementdetails
                LEFT JOIN
                othersourceofincome othersourceofincome ON employementdetails.Customer_id = othersourceofincome.Customer_id
    LEFT JOIN
    customeraddress customeraddress ON (customeraddress.Customer_id = employementdetails.Customer_id AND customeraddress.Type_id="ADR_TYPE_WORK")
    LEFT JOIN
    address address ON address.id=customeraddress.Address_id
WHERE
                employementdetails.Customer_id=Customer_id limit 1;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `get_customer_applicantinfo_proc`;
DELIMITER $$
CREATE PROCEDURE `get_customer_applicantinfo_proc`(
                IN `Customer_id` varchar(20000)
)
BEGIN
Select `customer`.`id` AS Customer_id,
`customeraddress`.`Address_id` AS Address_id,
`customeraddress`.`Type_id` AS Type_id,
`customeraddress`.`DurationOfStay` AS DurationOfStay,
`customeraddress`.`HomeOwnership` AS HomeOwnership,
`customer`.`FirstName` AS FirstName,
`customer`.`MiddleName` AS MiddleName,
`customer`.`LastName` AS LastName,
`customer`.`DateOfBirth` AS DateOfBirth,
`customer`.`Ssn` AS Ssn,
`customer`.`PreferredContactMethod` AS PreferredContactMethod,
`address`.`AddressLine1` AS AddressLine1,
`address`.`AddressLine2` AS AddressLine2,
`address`.`AddressLine3` AS AddressLine3,
`address`.`ZipCode` AS ZipCode,
`address`.`state` AS State,
`address`.`cityName` AS city,
`address`.`country` AS Country,
(Select `customercommunication`.`Value` from customercommunication where `customercommunication`.Type_id = 'COMM_TYPE_PHONE' AND `customercommunication`.`isPrimary`=1 AND `customercommunication`.`Customer_id`=Customer_id limit 1) AS PrimaryContactMethod,
(Select `customercommunication`.`Value` from customercommunication where `customercommunication`.Type_id = 'COMM_TYPE_EMAIL' AND customercommunication.isPrimary=1 AND `customercommunication`.`Customer_id`=Customer_id limit 1) AS Email
FROM
                customer customer
                LEFT JOIN
                customeraddress customeraddress ON (customeraddress.Customer_id = customer.id AND (customeraddress.Type_id = "ADR_TYPE_HOME" OR isnull(customeraddress.Type_id)))
    LEFT JOIN
    address address ON customeraddress.Address_id = address.id
    LEFT JOIN
    customercommunication customercommunication ON (customer.id=customercommunication.Customer_id AND customercommunication.isPrimary=1)
WHERE
                customer.id=Customer_id limit 1;
END$$
DELIMITER ;
