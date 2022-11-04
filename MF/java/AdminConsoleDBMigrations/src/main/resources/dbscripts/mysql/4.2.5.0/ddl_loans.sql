DROP procedure IF EXISTS `sp_getQuestionOptions`;

DELIMITER $$
CREATE PROCEDURE `sp_getQuestionOptions`(IN `QueryDefinitionID` varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci)
BEGIN
SELECT
      querySec.Name AS QuerySection_Name,
      querySec.abstractname AS QuerySection_FriendlyName,
      querySec.Parent_id AS QuerySection_Parent_id,
      querySec.ApplicantAllowedTo AS QuerySection_ApplicantAllowedTo,
      querySec.IndCoApplicantAllowedTo AS QuerySection_IndCoApplicantAllowedTo,
      querySec.JointCoApplicantAllowedTo AS QuerySection_JointCoApplicantAllowedTo,
      ifNULL(( 
      SELECT
         CAST(CONCAT('[', GROUP_CONCAT(JSON_OBJECT('Question_FriendlyName', querysectionquestion.abstractName, 'ParentQuestion_FriendlyName', querysectionquestion.ParentQuerySectionQuestion_AbstractName, 'ParentQuestion_Value', querysectionquestion.ParentQuestionOptionValue, 'OptionItems', 
         (
            SELECT
               CAST(CONCAT('[', GROUP_CONCAT(JSON_OBJECT( 'OptionItem_id', optionitem.id, 'OptionItem_DefaultValue', optionitem.DefaultValue,'OptionItem_Note',optionitem.Label ) separator ','), ']') AS JSON) 
            FROM
               optionitem optionitem 
            WHERE
               optionitem.OptionGroup_id in 
               (
                  SELECT
                     OptionGroup_id 
                  FROM
                     questiondefinition 
                  WHERE
                     questiondefinition.id = querysectionquestion.QuestionDefinition_id 
               )
            ORDER BY
               optionitem.Sequence 
         )
) SEPARATOR ','), ']') AS CHAR CHARSET UTF8MB4) 
      FROM
         `querysectionquestion` querysectionquestion 
      WHERE
         querySec.id = querysectionquestion.QuerySection_id ), '') AS `QuestionDefinition` 
      FROM
         querysection querySec 
      WHERE
         querySec.QueryDefinition_id = QueryDefinitionID 
         AND querySec.softdeleteflag = FALSE 
      ORDER BY
         querySec.Sequence;
END$$
DELIMITER ;

DROP procedure IF EXISTS `sp_getQuestionOptionsForSection`;

DELIMITER $$
CREATE PROCEDURE `sp_getQuestionOptionsForSection`(IN `QueryDefinitionID` varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci, IN `QuerySectionID` varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci)
BEGIN
SELECT
      querySec.Name AS QuerySection_Name,
      querySec.abstractname AS QuerySection_FriendlyName,
      querySec.Parent_id AS QuerySection_Parent_id,
      querySec.ApplicantAllowedTo AS QuerySection_ApplicantAllowedTo,
      querySec.IndCoApplicantAllowedTo AS QuerySection_IndCoApplicantAllowedTo,
      querySec.JointCoApplicantAllowedTo AS QuerySection_JointCoApplicantAllowedTo,
      ifNULL(( 
      SELECT
         CAST(CONCAT('[', GROUP_CONCAT(JSON_OBJECT('Question_FriendlyName', querysectionquestion.abstractName, 'ParentQuestion_FriendlyName', querysectionquestion.ParentQuerySectionQuestion_AbstractName, 'ParentQuestion_Value', querysectionquestion.ParentQuestionOptionValue, 'OptionItems', 
         (
            SELECT
               CAST(CONCAT('[', GROUP_CONCAT(JSON_OBJECT( 'OptionItem_id', optionitem.id, 'OptionItem_DefaultValue', optionitem.DefaultValue,'OptionItem_Note',optionitem.Label  ) separator ','), ']') AS JSON) 
            FROM
               optionitem optionitem 
            WHERE
               optionitem.OptionGroup_id in 
               (
                  SELECT
                     OptionGroup_id 
                  FROM
                     questiondefinition 
                  WHERE
                     questiondefinition.id = querysectionquestion.QuestionDefinition_id 
               )
            ORDER BY
               optionitem.Sequence 
         )
) SEPARATOR ','), ']') AS CHAR CHARSET UTF8MB4) 
      FROM
         `querysectionquestion` querysectionquestion 
      WHERE
         querySec.id = querysectionquestion.QuerySection_id ), '') AS `QuestionDefinition` 
      FROM
         querysection querySec 
      WHERE
         querySec.QueryDefinition_id = QueryDefinitionID
		 AND querySec.abstractname = QuerySectionID
         AND querySec.softdeleteflag = FALSE 
      ORDER BY
         querySec.Sequence;
END$$

DELIMITER ;


ALTER TABLE optionitem MODIFY DefaultValue VARCHAR(100);

DROP procedure IF EXISTS `sp_getSectionLoanAnswers`;
DELIMITER $$

CREATE PROCEDURE `sp_getSectionLoanAnswers`(IN QueryResponseID varchar(100) CHARACTER SET UTF8 COLLATE utf8_general_ci, IN QuerySection_id varchar(100) CHARACTER SET UTF8 COLLATE utf8_general_ci)
BEGIN
SELECT
      querySecQue.abstractName AS Question_FriendlyName,
      questionResp.id AS QuestionResponse_id,
      questionResp.ResponseValue AS Value,
      questionResp.OptionItem_id AS OptionItem_id 
   FROM
      questionresponse questionResp 
      INNER JOIN
         querysectionquestion querySecQue 
         ON querySecQue.id = questionResp.QuerySectionQuestion_id 
   WHERE
      questionResp.QueryResponse_id = QueryResponseID AND questionResp.QuerySection_id = (SELECT id from querysection where abstractname=QuerySection_id AND QueryDefinition_id=questionResp.QueryDefinition_id)
      AND questionResp.softdeleteflag is not true;
END$$
DELIMITER ;

ALTER TABLE `optionitem` CHANGE COLUMN `Label` `Label` VARCHAR(200) NULL DEFAULT NULL ;

DROP procedure IF EXISTS `sp_getAllApplications`;

DELIMITER $$
CREATE PROCEDURE `sp_getAllApplications`(IN `User_id` VARCHAR(50))
BEGIN
(SELECT
    queryresp.id AS id,
    queryresp.QueryDefinition_id AS QueryDefinition_id,
    queryresp.Customer_id AS User_id,
    queryresp.CoBorrower_id AS CoBorrower_id,
    queryresp.createdts AS StartDate,
    queryresp.lastmodifiedts AS LastEditedDate,
    queryresp.LoanProduct_id AS LoanProduct_id,
    loanProd.LoanType_id AS LoanType_id,
    queryresp.Status_id AS Status,
    querycoborrower.CoBorrower_Type AS CoBorrower_Type,
    (SELECT
            CAST(CONCAT('[',
                            GROUP_CONCAT(JSON_OBJECT('QuerySectionQuestion_id',
                                        questResp.QuerySectionQuestion_id,
                                        'ResponseValue',
                                        questResp.ResponseValue)
                                SEPARATOR ','),
                            ']')
                    AS CHAR CHARSET UTF8MB4)
        FROM
            `questionresponse` questResp
        WHERE
            questResp.queryresponse_id = queryresp.id
                 AND `questResp`.`QuerySectionQuestion_id` IN ('VA_LOAN_AMOUNT' , 'VA_COBORROWER',
                'CCA_CARDLIMIT',
                'PA_LOANAMOUNT',
                'PA_COBORROWER', 'MA_REQUIREDLOANAMOUNT' ,'MA_COBORROWER')) AS `QuestionResponse`
FROM
    `queryresponse` queryresp
        JOIN
    loanproduct loanProd ON queryresp.LoanProduct_id = loanProd.id
        LEFT JOIN
    querycoborrower querycoborrower ON queryresp.id = querycoborrower.queryresponse_id
WHERE
    (`queryresp`.`Customer_id` = User_id)
        AND `queryresp`.`QueryDefinition_id` IN ('PERSONAL_APPLICATION' , 'VEHICLE_APPLICATION',
        'CREDIT_CARD_APPLICATION' , 'MORTGAGE_APPLICATION') AND `queryresp`.softdeleteflag IS FALSE) UNION ALL (SELECT
    queryresp.id AS id,
    queryresp.QueryDefinition_id AS QueryDefinition_id,
    queryresp.Customer_id AS User_id,
    queryresp.CoBorrower_id AS CoBorrower_id,
    queryresp.createdts AS StartDate,
    queryresp.lastmodifiedts AS LastEditedDate,
    queryresp.LoanProduct_id AS LoanProduct_id,
    loanProd.LoanType_id AS LoanType_id,
    queryresp.Status_id AS Status,
    querycoborrower.CoBorrower_Type AS CoBorrower_Type,
    (SELECT
            CAST(CONCAT('[',
                            GROUP_CONCAT(JSON_OBJECT('QuerySectionQuestion_id',
                                        questResp.QuerySectionQuestion_id,
                                        'ResponseValue',
                                        questResp.ResponseValue)
                                SEPARATOR ','),
                            ']')
                    AS CHAR CHARSET UTF8MB4)
        FROM
            `questionresponse` questResp
        WHERE
            questResp.queryresponse_id = queryresp.id
                AND `questResp`.`QuerySectionQuestion_id` IN ('VA_LOAN_AMOUNT', 'VA_COBORROWER',
                'CCA_CARDLIMIT',
                'PA_LOANAMOUNT',
                'PA_COBORROWER')) AS `QuestionResponse`
FROM
    `queryresponse` queryresp
        JOIN
    loanproduct loanProd ON queryresp.LoanProduct_id = loanProd.id
        LEFT JOIN
    querycoborrower querycoborrower ON queryresp.id = querycoborrower.queryresponse_id
WHERE
    (`queryresp`.`CoBorrower_id` = User_id)
        AND `queryresp`.`QueryDefinition_id` IN ('PERSONAL_APPLICATION' , 'VEHICLE_APPLICATION',
        'CREDIT_CARD_APPLICATION','MORTGAGE_APPLICATION') AND `queryresp`.softdeleteflag IS FALSE) ORDER BY LastEditedDate DESC;
END$$

DELIMITER ;

DROP procedure IF EXISTS `sp_getLoanAnswersSectionwise`;

DELIMITER $$
CREATE PROCEDURE `sp_getLoanAnswersSectionwise`(IN QueryResponseID varchar(100) , IN QuerySection_id varchar(100))
BEGIN
SELECT
      querySecQue.abstractName AS Question_FriendlyName,
      questionResp.id AS QuestionResponse_id,
      questionResp.ResponseValue AS Value,
      questionResp.OptionItem_id AS OptionItem_id 
   FROM
      questionresponse questionResp 
      INNER JOIN
         querysectionquestion querySecQue 
         ON querySecQue.id = questionResp.QuerySectionQuestion_id 
   WHERE
      questionResp.QueryResponse_id = QueryResponseID AND questionResp.QuerySection_id = (SELECT id from querysection where abstractname=QuerySection_id AND QueryDefinition_id=questionResp.QueryDefinition_id)
      AND questionResp.softdeleteflag is not true;
END$$
DELIMITER ;

ALTER TABLE `loanproduct` 
ADD COLUMN `InterestRate` VARCHAR(50) NULL AFTER `softdeleteflag`,
ADD COLUMN `Point` VARCHAR(50) NULL AFTER `InterestRate`,
ADD COLUMN `BestChoiceIf` LONGTEXT NULL AFTER `Point`,
ADD COLUMN `Disadvantages` LONGTEXT NULL AFTER `BestChoiceIf`,
ADD COLUMN `APRRef` VARCHAR(50) NULL AFTER `Disadvantages`,
ADD COLUMN `InterestRateRef` VARCHAR(50) NULL AFTER `APRRef`,
ADD COLUMN `PointRef` VARCHAR(50) NULL AFTER `InterestRateRef`,
ADD COLUMN `Priority` VARCHAR(45) NULL AFTER `PointRef`;

ALTER TABLE `queryresponse` 
ADD COLUMN `Application_id` VARCHAR(20) NULL AFTER `Customer_id`,
ADD UNIQUE INDEX `Application_id_UNIQUE` (`Application_id` ASC);

--
-- Table structure for legaldeclarationsresponse
--

DROP TABLE IF EXISTS `legaldeclarationsresponse`;

CREATE TABLE `legaldeclarationsresponse` (
  `id` VARCHAR(50) NOT NULL,
  `QueryReponse_id` VARCHAR(50) NULL,
  `Borrower_id` VARCHAR(50) NULL,
  `IsPrimaryResident` VARCHAR(3) NULL,
  `HasRelationshipWithSeller` VARCHAR(3) NULL,
  `TypeOfProperty` VARCHAR(20) NULL,
  `TitleOfProperty` VARCHAR(20) NULL,
  `IsBorrowingMoney` VARCHAR(3) NULL,
  `HasAnyOtherMortgageLoan` VARCHAR(3) NULL,
  `OtherMortgageLoanAmount` VARCHAR(3) NULL,
  `HasNewCredit` VARCHAR(3) NULL,
  `HasPropertyLien` VARCHAR(3) NULL,
  `IsGuarantorOfAnyLoan` VARCHAR(3) NULL,
  `HasOutsandingJudgements` VARCHAR(3) NULL,
  `HasFinancialLiability` VARCHAR(3) NULL,
  `IsDelinquent` VARCHAR(3) NULL,
  `HasForeclosureInPast` VARCHAR(3) NULL,
  `HasPreForeclosureSale` VARCHAR(3) NULL,
  `HasPropertyForeclosedInPast` VARCHAR(3) NULL,
  `HasDeclaredBankruptcyInPast` VARCHAR(3) NULL,
  `BankruptcyType` VARCHAR(20) NULL,
  `createdby` VARCHAR(50) NULL,
  `modifiedby` VARCHAR(50) NULL,
  `createdts` TIMESTAMP NULL,
  `lastmodifiedts` TIMESTAMP NULL,
  `synctimestamp` TIMESTAMP NULL,
  `softdeleteflag` TINYINT(1) NULL,
  PRIMARY KEY (`id`),
  INDEX `FK_LegalDeclarationsResponse_QueryResponse_idx` (`QueryReponse_id` ASC),
  CONSTRAINT `FK_LegalDeclarationsResponse_QueryResponse`
    FOREIGN KEY (`QueryReponse_id`)
    REFERENCES `queryresponse` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION) ENGINE=InnoDB DEFAULT CHARSET=utf8;
	
ALTER TABLE `legaldeclarationsresponse` 
CHANGE COLUMN `createdts` `createdts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ,
CHANGE COLUMN `lastmodifiedts` `lastmodifiedts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
CHANGE COLUMN `synctimestamp` `synctimestamp` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ;


--
-- Table structure for borrower
--

DROP TABLE IF EXISTS `borrower`;

CREATE TABLE `borrower` (
  `id` VARCHAR(50) NOT NULL,
  `QueryResponse_id` VARCHAR(50) NULL,
  `Customer_id` VARCHAR(50) NULL,
  `FirstName` VARCHAR(50) NULL,
  `LastName` VARCHAR(50) NULL,
  `Email` VARCHAR(50) NULL,
  `PhoneNumber` VARCHAR(50) NULL,
  `CreditType` VARCHAR(50) NULL,
  `BorrowerType` VARCHAR(50) NULL,
  `InviteChallenge` VARCHAR(50) NULL,
  `IsAgreementAccepted` VARCHAR(50) NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC)) ENGINE=InnoDB DEFAULT CHARSET=utf8;

  
ALTER TABLE `borrower` 
CHANGE COLUMN `IsAgreementAccepted` `IsAgreementAccepted` TINYINT(1) NULL DEFAULT NULL ;

ALTER TABLE `borrower` 
ADD COLUMN `createdby` VARCHAR(50) NULL AFTER `IsAgreementAccepted`,
ADD COLUMN `modifiedby` VARCHAR(50) NULL AFTER `createdby`,
ADD COLUMN `createdts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP AFTER `modifiedby`,
ADD COLUMN `lastmodifiedts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `createdts`,
ADD COLUMN `synctimestamp` TIMESTAMP NULL AFTER `lastmodifiedts`,
ADD COLUMN `softdeleteflag` TINYINT(1) NULL DEFAULT '0' AFTER `synctimestamp`;

ALTER TABLE `borrower` 
ADD COLUMN `ApplicantType` VARCHAR(50) NULL DEFAULT NULL AFTER `InviteChallenge`;

DROP procedure IF EXISTS `sp_getAllApplications`;

DELIMITER $$

CREATE PROCEDURE `sp_getAllApplications`(IN `User_id` VARCHAR(50))
BEGIN
(SELECT
    queryresp.id AS id,
    queryresp.QueryDefinition_id AS QueryDefinition_id,
    queryresp.Customer_id AS User_id,
    queryresp.CoBorrower_id AS CoBorrower_id,
    queryresp.createdts AS StartDate,
    queryresp.lastmodifiedts AS LastEditedDate,
    queryresp.LoanProduct_id AS LoanProduct_id,
    queryresp.Application_id AS Application_id,
    loanProd.LoanType_id AS LoanType_id,
    queryresp.Status_id AS Status,
    querycoborrower.CoBorrower_Type AS CoBorrower_Type,
    (SELECT
            CAST(CONCAT('[',
                            GROUP_CONCAT(JSON_OBJECT('QuerySectionQuestion_id',
                                        questResp.QuerySectionQuestion_id,
                                        'ResponseValue',
                                        questResp.ResponseValue)
                                SEPARATOR ','),
                            ']')
                    AS CHAR CHARSET UTF8MB4)
        FROM
            `questionresponse` questResp
        WHERE
            questResp.queryresponse_id = queryresp.id
                 AND `questResp`.`QuerySectionQuestion_id` IN ('VA_LOAN_AMOUNT' , 'VA_COBORROWER',
                'CCA_CARDLIMIT',
                'PA_LOANAMOUNT',
                'PA_COBORROWER', 'MA_REQUIREDLOANAMOUNT' ,'MA_COBORROWER')) AS `QuestionResponse`
FROM
    `queryresponse` queryresp
        JOIN
    loanproduct loanProd ON queryresp.LoanProduct_id = loanProd.id
        LEFT JOIN
    querycoborrower querycoborrower ON queryresp.id = querycoborrower.queryresponse_id
WHERE
    (`queryresp`.`Customer_id` = User_id COLLATE utf8_unicode_ci)
        AND `queryresp`.`QueryDefinition_id` IN ('PERSONAL_APPLICATION' , 'VEHICLE_APPLICATION',
        'CREDIT_CARD_APPLICATION' , 'MORTGAGE_APPLICATION') AND `queryresp`.softdeleteflag IS FALSE) UNION ALL (SELECT
    queryresp.id AS id,
    queryresp.QueryDefinition_id AS QueryDefinition_id,
    queryresp.Customer_id AS User_id,
    queryresp.CoBorrower_id AS CoBorrower_id,
    queryresp.createdts AS StartDate,
    queryresp.lastmodifiedts AS LastEditedDate,
    queryresp.LoanProduct_id AS LoanProduct_id,
	queryresp.Application_id AS Application_id,
    loanProd.LoanType_id AS LoanType_id,
    queryresp.Status_id AS STATUS,
    querycoborrower.CoBorrower_Type AS CoBorrower_Type,
    (SELECT
            CAST(CONCAT('[',
                            GROUP_CONCAT(JSON_OBJECT('QuerySectionQuestion_id',
                                        questResp.QuerySectionQuestion_id,
                                        'ResponseValue',
                                        questResp.ResponseValue)
                                SEPARATOR ','),
                            ']')
                    AS CHAR CHARSET UTF8MB4)
        FROM
            `questionresponse` questResp
        WHERE
            questResp.queryresponse_id = queryresp.id
                AND `questResp`.`QuerySectionQuestion_id` IN ('VA_LOAN_AMOUNT', 'VA_COBORROWER',
                'CCA_CARDLIMIT',
                'PA_LOANAMOUNT',
                'PA_COBORROWER')) AS `QuestionResponse`
FROM
    `queryresponse` queryresp
        JOIN
    loanproduct loanProd ON queryresp.LoanProduct_id = loanProd.id
        LEFT JOIN
    querycoborrower querycoborrower ON queryresp.id = querycoborrower.queryresponse_id
WHERE
    (`queryresp`.`CoBorrower_id` = User_id COLLATE utf8_unicode_ci)
        AND `queryresp`.`QueryDefinition_id` IN ('PERSONAL_APPLICATION' , 'VEHICLE_APPLICATION',
        'CREDIT_CARD_APPLICATION', 'MORTGAGE_APPLICATION') AND `queryresp`.softdeleteflag IS FALSE) ORDER BY LastEditedDate DESC;
END$$
DELIMITER ;

DROP TABLE IF EXISTS `incomeresponse`;

CREATE TABLE `incomeresponse` (
  `id` varchar(50) NOT NULL,
  `QueryResponse_id` varchar(50) NOT NULL,
  `Borrower_id` varchar(50) NOT NULL,
  `HasAdditionalEmployment` tinyint(1) DEFAULT NULL,
  `HasPreviousEmployment` tinyint(1) DEFAULT NULL,
  `HasOtherSourcesOfIncome` tinyint(1) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_IncomeResponse_QueryResponse_idx` (`QueryResponse_id`),
  KEY `FK_IncomeResponse_Borrower_idx` (`Borrower_id`),
  CONSTRAINT `FK_IncomeResponse_Borrower` FOREIGN KEY (`Borrower_id`) REFERENCES `borrower` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_IncomeResponse_QueryResponse` FOREIGN KEY (`QueryResponse_id`) REFERENCES `queryresponse` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- employmentdetailsresponse Table definition
--
DROP TABLE IF EXISTS `employmentdetailsresponse`;

CREATE TABLE `employmentdetailsresponse` (
  `id` varchar(50) NOT NULL,
  `IncomeResponse_id` varchar(50) NOT NULL,
  `QueryResponse_id` varchar(50) NOT NULL,
  `Borrower_id` varchar(50) DEFAULT NULL,
  `EmploymentDetailType_id` varchar(50) DEFAULT NULL,
  `EmploymentType_id` varchar(50) DEFAULT NULL,
  `ProvideEmploymentDetails` tinyint(1) DEFAULT NULL,
  `EmployerName` varchar(50) DEFAULT NULL,
  `EmployerAddressLine1` varchar(100) DEFAULT NULL,
  `EmployerAddressLine2` varchar(100) DEFAULT NULL,
  `EmployerAddressCity` varchar(50) DEFAULT NULL,
  `EmployerAddressState` varchar(50) DEFAULT NULL,
  `EmployerAddressCountry` varchar(50) DEFAULT NULL,
  `EmployerAddressZipCode` int(11) DEFAULT NULL,
  `EmployerPhoneNumber` varchar(15) DEFAULT NULL,
  `TotalGrossIncome` decimal(10,2) DEFAULT NULL,
  `EmployeeDesignation` varchar(50) DEFAULT NULL,
  `BusinessShare_id` varchar(50) DEFAULT NULL,
  `BusinessMonthlyLoss` decimal(10,2) DEFAULT NULL,
  `ProfessionStartDate` date DEFAULT NULL,
  `LineOfWorkDuration` varchar(10) DEFAULT NULL,
  `IsOtherParty` tinyint(1) DEFAULT NULL,
  `PrevStartDate` date DEFAULT NULL,
  `PrevEndDate` date DEFAULT NULL,
  `Sequence` int(11) NOT NULL AUTO_INCREMENT,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `Sequence_UNIQUE` (`Sequence`),
  KEY `FK_EmploymentDetailsResponse_Borrower_idx` (`Borrower_id`),
  KEY `FK_EmploymentDetailsResponse_QueryResponse_idx` (`QueryResponse_id`),
  KEY `FK_EmploymentDetailsResponse_IncomeResponse_idx` (`IncomeResponse_id`),
  KEY `FK_EmploymentDetailsResponse_OptionItem_idx` (`EmploymentType_id`),
  KEY `FK_EmploymentDetailsResponse_OptionItem2_idx` (`BusinessShare_id`),
  KEY `FK_EmploymentDetailsResponse_OptionItem3_idx` (`EmploymentDetailType_id`),
  CONSTRAINT `FK_EmploymentDetailsResponse_Borrower` FOREIGN KEY (`Borrower_id`) REFERENCES `borrower` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_EmploymentDetailsResponse_IncomeResponse` FOREIGN KEY (`IncomeResponse_id`) REFERENCES `incomeresponse` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_EmploymentDetailsResponse_OptionItem` FOREIGN KEY (`EmploymentType_id`) REFERENCES `optionitem` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_EmploymentDetailsResponse_OptionItem2` FOREIGN KEY (`BusinessShare_id`) REFERENCES `optionitem` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_EmploymentDetailsResponse_OptionItem3` FOREIGN KEY (`EmploymentDetailType_id`) REFERENCES `optionitem` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_EmploymentDetailsResponse_QueryResponse` FOREIGN KEY (`QueryResponse_id`) REFERENCES `queryresponse` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- incomedistributionresponse table
--

DROP TABLE IF EXISTS `incomedistributionresponse`;

CREATE TABLE `incomedistributionresponse` (
  `id` varchar(50) NOT NULL,
  `QueryResponse_id` varchar(50) NOT NULL,
  `Borrower_id` varchar(50) NOT NULL,
  `EmploymentDetailsResponse_id` varchar(50) DEFAULT NULL,
  `IncomeDetail_id` varchar(50) DEFAULT NULL,
  `PayPeriod_id` varchar(50) DEFAULT NULL,
  `Amount` decimal(10,2) DEFAULT NULL,
  `WorkingHours` int(11) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_IncomeDistributionResponse_QueryResponse_idx` (`QueryResponse_id`),
  KEY `FK_IncomeDistributionResponse_Borrower_idx` (`Borrower_id`),
  KEY `FK_IncomeDistributionResponse_OptionItem_idx` (`IncomeDetail_id`),
  KEY `FK_IncomeDistributionResponse_OptionItem2_idx` (`PayPeriod_id`),
  KEY `FK_IncomeDistributionResponse_EmploymentDetaislResponse_idx` (`EmploymentDetailsResponse_id`),
  CONSTRAINT `FK_IncomeDistributionResponse_Borrower` FOREIGN KEY (`Borrower_id`) REFERENCES `borrower` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_IncomeDistributionResponse_EmploymentDetaislResponse` FOREIGN KEY (`EmploymentDetailsResponse_id`) REFERENCES `employmentdetailsresponse` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_IncomeDistributionResponse_OptionItem` FOREIGN KEY (`IncomeDetail_id`) REFERENCES `optionitem` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_IncomeDistributionResponse_OptionItem2` FOREIGN KEY (`PayPeriod_id`) REFERENCES `optionitem` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_IncomeDistributionResponse_QueryResponse` FOREIGN KEY (`QueryResponse_id`) REFERENCES `queryresponse` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 
-- otherincomesresponse table definition
-- 

DROP TABLE IF EXISTS `otherincomesresponse`;

CREATE TABLE `otherincomesresponse` (
  `id` varchar(50) NOT NULL,
  `QueryResponse_id` varchar(50) NOT NULL,
  `Borrower_id` varchar(50) NOT NULL,
  `IncomeResponse_id` varchar(50) NOT NULL,
  `IncomeSource_id` varchar(50) DEFAULT NULL,
  `IncomePayPeriod_id` varchar(50) DEFAULT NULL,
  `Amount` decimal(10,2) DEFAULT NULL,
  `WorkingHours` int(11) DEFAULT NULL,
  `Sequence` int(11) NOT NULL AUTO_INCREMENT,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `Sequence_UNIQUE` (`Sequence`),
  KEY `FK_OtherIncomesResponse_QueryResponse_idx` (`QueryResponse_id`),
  KEY `FK_OtherIncomesResponse_Borrower_idx` (`Borrower_id`),
  KEY `FK_OtherIncomesResponse_IncomeResponse_idx` (`IncomeResponse_id`),
  KEY `FK_OtherIncomesResponse_OptionItem_idx` (`IncomeSource_id`),
  KEY `FK_OtherIncomesResponse_OptionItem2_idx` (`IncomePayPeriod_id`),
  CONSTRAINT `FK_OtherIncomesResponse_Borrower` FOREIGN KEY (`Borrower_id`) REFERENCES `borrower` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_OtherIncomesResponse_IncomeResponse` FOREIGN KEY (`IncomeResponse_id`) REFERENCES `incomeresponse` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_OtherIncomesResponse_OptionItem` FOREIGN KEY (`IncomeSource_id`) REFERENCES `optionitem` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_OtherIncomesResponse_OptionItem2` FOREIGN KEY (`IncomePayPeriod_id`) REFERENCES `optionitem` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_OtherIncomesResponse_QueryResponse` FOREIGN KEY (`QueryResponse_id`) REFERENCES `queryresponse` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `personalinfoaddressresponse`;

-- 
-- Table personalinfoaddressresponse definition 
-- 

CREATE TABLE IF NOT EXISTS `personalinfoaddressresponse` (
  `id` varchar(50) NOT NULL,
  `QueryResponse_id` varchar(50) DEFAULT NULL,
  `Borrower_id` varchar(50) DEFAULT NULL,
  `AddressLine1` varchar(50) DEFAULT NULL,
  `AddressLine2` varchar(50) DEFAULT NULL,
  `AddressCity` varchar(50) DEFAULT NULL,
  `AddressState` varchar(50) DEFAULT NULL,
  `AddressZip` varchar(50) DEFAULT NULL,
  `AddressCountry` varchar(50) DEFAULT NULL,
  `AddressType_id` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_personalinfoaddressresponse_queryresponse` (`QueryResponse_id`),
  KEY `FK_personalinfoaddressresponse_borrower` (`Borrower_id`),
  KEY `FK_personalinfoaddressresponse_addresstype` (`AddressType_id`),
  CONSTRAINT `FK_personalinfoaddressresponse_addresstype` FOREIGN KEY (`AddressType_id`) REFERENCES `addresstype` (`id`),
  CONSTRAINT `FK_personalinfoaddressresponse_borrower` FOREIGN KEY (`Borrower_id`) REFERENCES `borrower` (`id`),
  CONSTRAINT `FK_personalinfoaddressresponse_queryresponse` FOREIGN KEY (`QueryResponse_id`) REFERENCES `queryresponse` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `personalinforesponse`;
-- 
-- table definiton personalinforesponse
-- 
CREATE TABLE IF NOT EXISTS `personalinforesponse` (
  `id` varchar(50) NOT NULL,
  `QueryResponse_id` varchar(50) DEFAULT NULL,
  `Borrower_id` varchar(50) DEFAULT NULL,
  `FirstName` varchar(50) DEFAULT NULL,
  `MiddleName` varchar(50) DEFAULT NULL,
  `LastName` varchar(50) DEFAULT NULL,
  `Suffix` varchar(50) DEFAULT NULL,
  `DOB` date DEFAULT NULL,
  `CitizenshipStatus` varchar(50) DEFAULT NULL,
  `MaritalStatus` varchar(50) DEFAULT NULL,
  `NoOfDependents` varchar(50) DEFAULT NULL,
  `valuesForDependents` varchar(50) DEFAULT NULL,
  `CurrentAddressType` varchar(50) DEFAULT NULL,
  `CurrentAddressDuration` varchar(50) DEFAULT NULL,
  `SSN` int(11) DEFAULT NULL,
  `Email` varchar(50) DEFAULT NULL,
  `MobileNumber` int(11) DEFAULT NULL,
  `OfficeNumber` int(11) DEFAULT NULL,
  `HomeNumber` int(11) DEFAULT NULL,
  `PrimaryContact` varchar(50) DEFAULT NULL,
  `IsUSArmedForce` tinyint(1) DEFAULT NULL,
  `IsCurrOnService` tinyint(1) DEFAULT NULL,
  `IsCurrOnServiceDOS` tinyint(1) DEFAULT NULL,
  `ExpDOS` date DEFAULT NULL,
  `IsSurvivingSpouse` tinyint(1) DEFAULT NULL,
  `IsNonActiveMemResorNationalGaurd` tinyint(1) DEFAULT NULL,
  `IsCurrRetiredorDischargedorSeprated` tinyint(1) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_personalinforesponse_queryresponse` (`QueryResponse_id`),
  KEY `FK_personalinforesponse_borrower` (`Borrower_id`),
  CONSTRAINT `FK_personalinforesponse_borrower` FOREIGN KEY (`Borrower_id`) REFERENCES `borrower` (`id`),
  CONSTRAINT `FK_personalinforesponse_queryresponse` FOREIGN KEY (`QueryResponse_id`) REFERENCES `queryresponse` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP procedure IF EXISTS `sp_getAllApplications`;

DELIMITER $$

CREATE PROCEDURE `sp_getAllApplications`(
	IN `User_id` VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci)
BEGIN
(SELECT
    queryresp.id AS id,
    queryresp.QueryDefinition_id AS QueryDefinition_id,
    queryresp.Customer_id AS User_id,
    queryresp.CoBorrower_id AS CoBorrower_id,
    queryresp.createdts AS StartDate,
    queryresp.lastmodifiedts AS LastEditedDate,
    queryresp.LoanProduct_id AS LoanProduct_id,
    queryresp.Application_id AS Application_id,
    loanProd.LoanType_id AS LoanType_id,
    queryresp.Status_id AS Status,
    querycoborrower.CoBorrower_Type AS CoBorrower_Type,
    (SELECT
            CAST(CONCAT('[',
                            GROUP_CONCAT(JSON_OBJECT('QuerySectionQuestion_id',
                                        questResp.QuerySectionQuestion_id,
                                        'ResponseValue',
                                        questResp.ResponseValue)
                                SEPARATOR ','),
                            ']')
                    AS CHAR CHARSET UTF8MB4)
        FROM
            `questionresponse` questResp
        WHERE
            questResp.queryresponse_id = queryresp.id
                 AND `questResp`.`QuerySectionQuestion_id` IN ('VA_LOAN_AMOUNT' , 'VA_COBORROWER',
                'CCA_CARDLIMIT',
                'PA_LOANAMOUNT',
                'PA_COBORROWER', 'MA_REQUIREDLOANAMOUNT' ,'MA_COBORROWER')) AS `QuestionResponse`
FROM
    `queryresponse` queryresp
        JOIN
    loanproduct loanProd ON queryresp.LoanProduct_id = loanProd.id
        LEFT JOIN
    querycoborrower querycoborrower ON queryresp.id = querycoborrower.queryresponse_id
WHERE
    (`queryresp`.`Customer_id` = User_id COLLATE utf8_unicode_ci)
        AND `queryresp`.`QueryDefinition_id` IN ('PERSONAL_APPLICATION' , 'VEHICLE_APPLICATION',
        'CREDIT_CARD_APPLICATION' , 'MORTGAGE_APPLICATION') AND `queryresp`.softdeleteflag IS FALSE) UNION ALL (SELECT
    queryresp.id AS id,
    queryresp.QueryDefinition_id AS QueryDefinition_id,
    queryresp.Customer_id AS User_id,
    queryresp.CoBorrower_id AS CoBorrower_id,
    queryresp.createdts AS StartDate,
    queryresp.lastmodifiedts AS LastEditedDate,
    queryresp.LoanProduct_id AS LoanProduct_id,
	queryresp.Application_id AS Application_id,
    loanProd.LoanType_id AS LoanType_id,
    queryresp.Status_id AS STATUS,
    querycoborrower.CoBorrower_Type AS CoBorrower_Type,
    (SELECT
            CAST(CONCAT('[',
                            GROUP_CONCAT(JSON_OBJECT('QuerySectionQuestion_id',
                                        questResp.QuerySectionQuestion_id,
                                        'ResponseValue',
                                        questResp.ResponseValue)
                                SEPARATOR ','),
                            ']')
                    AS CHAR CHARSET UTF8MB4)
        FROM
            `questionresponse` questResp
        WHERE
            questResp.queryresponse_id = queryresp.id
                AND `questResp`.`QuerySectionQuestion_id` IN ('VA_LOAN_AMOUNT', 'VA_COBORROWER',
                'CCA_CARDLIMIT',
                'PA_LOANAMOUNT',
                'PA_COBORROWER')) AS `QuestionResponse`
FROM
    `queryresponse` queryresp
        JOIN
    loanproduct loanProd ON queryresp.LoanProduct_id = loanProd.id
        LEFT JOIN
    querycoborrower querycoborrower ON queryresp.id = querycoborrower.queryresponse_id
WHERE
    (`queryresp`.`CoBorrower_id` = User_id COLLATE utf8_unicode_ci)
        AND `queryresp`.`QueryDefinition_id` IN ('PERSONAL_APPLICATION' , 'VEHICLE_APPLICATION',
        'CREDIT_CARD_APPLICATION', 'MORTGAGE_APPLICATION') AND `queryresp`.softdeleteflag IS FALSE) ORDER BY LastEditedDate DESC;
END$$
DELIMITER ;

--
-- Table structure for loanofficers
--

DROP TABLE IF EXISTS `loanofficers`;


CREATE TABLE `loanofficers` (
  `id` VARCHAR(50) NOT NULL,
  `FirstName` VARCHAR(50) NULL,
  `LastName` VARCHAR(50) NULL,
  `EmailId` VARCHAR(50) NULL,
  `PhoneNo` INT(11) NULL,
  `AddressLine` VARCHAR(50) NULL,
  `City` VARCHAR(50) NULL,
  `State` VARCHAR(50) NULL,
  `Zipcode` INT(11) NULL,
  `EmpNo` VARCHAR(50) NULL,
  `Country` VARCHAR(50) NULL,
  PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;
  
ALTER TABLE `loanofficers` 
ADD COLUMN `createdby` VARCHAR(50) NULL AFTER `Country`,
ADD COLUMN `modifiedby` VARCHAR(50) NULL AFTER `createdby`,
ADD COLUMN `createdts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP AFTER `modifiedby`,
ADD COLUMN `lastmodifiedts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `createdts`,
ADD COLUMN `synctimestamp` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `lastmodifiedts`,
ADD COLUMN `softdeleteflag` TINYINT(1) NULL AFTER `synctimestamp`;

--
-- Table structure for loanofficers
--

DROP TABLE IF EXISTS `loanofficersresponse`;

CREATE TABLE `loanofficersresponse` (
  `id` VARCHAR(50) NOT NULL,
  `QueryResponse_id` VARCHAR(50) NULL,
  `Borrower_id` VARCHAR(50) NULL,
  `HasLoanOfficer` TINYINT(1) NULL,
  `EmpNo` VARCHAR(50) NULL,
  PRIMARY KEY (`id`))ENGINE=InnoDB DEFAULT CHARSET=utf8;
  
ALTER TABLE `loanofficersresponse` 
ADD INDEX `FK_LoanOfficersResponse_Borrower_idx` (`Borrower_id` ASC);

ALTER TABLE `loanofficersresponse` 
ADD CONSTRAINT `FK_LoanOfficersResponse_Borrower`
  FOREIGN KEY (`Borrower_id`)
  REFERENCES `borrower` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE `loanofficersresponse` 
CHANGE COLUMN `EmpNo` `LoansOfficers_EmpNo` VARCHAR(50) NULL DEFAULT NULL ;

ALTER TABLE `loanofficersresponse` 
ADD COLUMN `createdby` VARCHAR(50) NULL AFTER `LoansOfficers_EmpNo`,
ADD COLUMN `modifiedby` VARCHAR(50) NULL AFTER `createdby`,
ADD COLUMN `createdts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP AFTER `modifiedby`,
ADD COLUMN `lastmodifiedts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `createdts`,
ADD COLUMN `synctimestamp` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `lastmodifiedts`,
ADD COLUMN `softdeleteflag` TINYINT(1) NULL AFTER `synctimestamp`,
ADD INDEX `FK_LoanOfficersResponse_QueryResponse_idx` (`QueryResponse_id` ASC);

ALTER TABLE `loanofficersresponse` 
ADD CONSTRAINT `FK_LoanOfficersResponse_QueryResponse`
  FOREIGN KEY (`QueryResponse_id`)
  REFERENCES `queryresponse` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
  
  --
-- Table structure for loaninforesponse
--

DROP TABLE IF EXISTS `loaninforesponse`;

CREATE TABLE `loaninforesponse` (
  `id` VARCHAR(50) NOT NULL,
  `QueryResponse_id` VARCHAR(50) NULL,
  `Borrower_id` VARCHAR(50) NULL,
  `LoanPurpose` VARCHAR(50) NULL,
  `HasIdentifiedHomeToPurchase` TINYINT(1) NULL,
  `PurchasePrice` INT(11) NULL,
  `DownPaymentValue` INT(11) NULL,
  `DownPaymentUnit` VARCHAR(50) NULL,
  `EstimatedLoanAmount` INT(11) NULL,
  `RefinanceType` VARCHAR(50) NULL,
  `MortgageBalance` INT(11) NULL,
  `PropertyValue` INT(11) NULL,
  `RequiredLoanAmount` INT(11) NULL,
  `createdby` VARCHAR(50) NULL,
  `modifiedby` VARCHAR(50) NULL,
  `createdts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NULL,
  PRIMARY KEY (`id`),
  INDEX `FK_LoanInfoResponse_QueryResponse_idx` (`QueryResponse_id` ASC),
  CONSTRAINT `FK_LoanInfoResponse_QueryResponse`
    FOREIGN KEY (`QueryResponse_id`)
    REFERENCES `queryresponse` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)ENGINE=InnoDB DEFAULT CHARSET=utf8;
	
--
-- Table structure for propertyinforesponse
--

DROP TABLE IF EXISTS `propertyinforesponse`;

CREATE TABLE `propertyinforesponse` (
  `id` VARCHAR(50) NOT NULL,
  `QueryResponse_id` VARCHAR(50) NULL,
  `Borrower_id` VARCHAR(50) NULL,
  `PurchasePlan` VARCHAR(50) NULL,  
  `PropertyAddressLine1` VARCHAR(50) NULL,
  `PropertyAddressLine2` VARCHAR(50) NULL,
  `PropertyAddressCity` VARCHAR(50) NULL,
  `PropertyAddressState` VARCHAR(50) NULL,
  `PropertyAddressCountry` VARCHAR(50) NULL,
  `PropertyAddressZip` INT(11) NULL,
  `PropertyType` VARCHAR(50) NULL,
  `PropertyUsage` VARCHAR(50) NULL,
  `NumberOfUnits` INT(11) NULL,  
  `IsMixedUseProperty` TINYINT(1) NULL,
  `createdby` VARCHAR(50) NULL,
  `modifiedby` VARCHAR(50) NULL,
  `createdts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NULL,
  PRIMARY KEY (`id`),
  INDEX `FK_PropertyInfoResponse_QueryResponse_idx` (`QueryResponse_id` ASC),
  CONSTRAINT `FK_PropertyInfoResponse_QueryResponse`
    FOREIGN KEY (`QueryResponse_id`)
    REFERENCES `queryresponse` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)ENGINE=InnoDB DEFAULT CHARSET=utf8;
	
--
-- Table structure for realestateagentresponse
--

DROP TABLE IF EXISTS `realestateagentresponse`;

CREATE TABLE `realestateagentresponse` (
  `id` VARCHAR(50) NOT NULL,
  `QueryResponse_id` VARCHAR(50) NULL,
  `Borrower_id` VARCHAR(50) NULL,
  `FirstName` VARCHAR(50) NULL,  
  `LastName` VARCHAR(50) NULL,
  `EmailId` VARCHAR(50) NULL,
  `MobileNumber` INT(11) NULL,
  `createdby` VARCHAR(50) NULL,
  `modifiedby` VARCHAR(50) NULL,
  `createdts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NULL,
  PRIMARY KEY (`id`),
  INDEX `FK_RealEstateAgentResponse_QueryResponse_idx` (`QueryResponse_id` ASC),
  CONSTRAINT `FK_RealEstateAgentResponse_QueryResponse`
    FOREIGN KEY (`QueryResponse_id`)
    REFERENCES `queryresponse` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `realestateagentresponse` 
ADD COLUMN `HasAgent` TINYINT(1) NULL AFTER `softdeleteflag`,
ADD INDEX `FK_RealEstateagentResponse_Borrower_idx` (`Borrower_id` ASC);
;
ALTER TABLE `realestateagentresponse` 
ADD CONSTRAINT `FK_RealEstateagentResponse_Borrower`
  FOREIGN KEY (`Borrower_id`)
  REFERENCES `borrower` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

--
-- Table structure for additionalmortgageresponse
--

DROP TABLE IF EXISTS `additionalmortgageresponse`;

CREATE TABLE `additionalmortgageresponse` (
  `id` VARCHAR(50) NOT NULL,
  `QueryResponse_id` VARCHAR(50) NULL,
  `Borrower_id` VARCHAR(50) NULL,
  `Sequence` INT(11) NULL,  
  `CreditorName` VARCHAR(50) NULL,
  `LienType` VARCHAR(50) NULL,
  `MonthlyMortgage` INT(11) NULL,
  `AmountToBeDrawn` INT(11) NULL,
  `CreditLimit` INT(11) NULL,  
  `createdby` VARCHAR(50) NULL,
  `modifiedby` VARCHAR(50) NULL,
  `createdts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NULL,
  PRIMARY KEY (`id`),
  INDEX `FK_AdditionalMortgageResponse_QueryResponse_idx` (`QueryResponse_id` ASC),
  CONSTRAINT `FK_AdditionalMortgageResponse_QueryResponse`
    FOREIGN KEY (`QueryResponse_id`)
    REFERENCES `queryresponse` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)ENGINE=InnoDB DEFAULT CHARSET=utf8;
	
ALTER TABLE `additionalmortgageresponse` 
CHANGE COLUMN `Sequence` `Sequence` INT(11) NOT NULL AUTO_INCREMENT ,
ADD UNIQUE INDEX `Sequence_UNIQUE` (`Sequence` ASC);
	
--
-- Table structure for sectionstatus
--

DROP TABLE IF EXISTS `sectionstatus`;

CREATE TABLE `sectionstatus` (
  `id` VARCHAR(50) NULL,
  `QueryResponse_id` VARCHAR(50) NOT NULL,
  `Borrower_id` VARCHAR(50) NOT NULL,
  `LoanType` VARCHAR(50) NULL,
  `ApplicantType` VARCHAR(50) NULL,
  `ApplicantPersonalInfo` TINYINT(1) NULL DEFAULT 0,
  `MyMortgage` TINYINT(1) NULL DEFAULT 0,
  `LoanAndPropertyInfo` TINYINT(1) NULL DEFAULT 0,
  `CoApplicantAndAgentInfo` TINYINT(1) NULL DEFAULT 0,
  `LoanSelection` TINYINT(1) NULL DEFAULT 0,
  `LoanOfficerSelection` TINYINT(1) NULL DEFAULT 0,
  `UserDetails` TINYINT(1) NULL DEFAULT 0,
  `Income` TINYINT(1) NULL DEFAULT 0,
  `Assets` TINYINT(1) NULL DEFAULT 0,
  `LiabilitiesAndExpenses` TINYINT(1) NULL DEFAULT 0,
  `LegalDeclarations` TINYINT(1) NULL DEFAULT 0,
  `Demographics` TINYINT(1) NULL DEFAULT 0,
  `Consent` TINYINT(1) NULL DEFAULT 0,
  `ReviewAndSubmit` TINYINT(1) NULL DEFAULT 0,
  PRIMARY KEY (`QueryResponse_id`, `Borrower_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for liabilitiesheaderresponse
--

DROP TABLE IF EXISTS `liabilitiesheaderresponse`;

CREATE TABLE `liabilitiesheaderresponse` (
  `id` varchar(50) NOT NULL,
  `QueryResponse_id` varchar(50) NOT NULL,
  `Borrower_id` varchar(50) NOT NULL,
  `HasLiabilities` tinyint(1) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`QueryResponse_id`,`Borrower_id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `FK_LiabilitiesHeaderResponse_BorrowerId_idx` (`Borrower_id`),
  CONSTRAINT `FK_LiabilitiesHeaderResponse_BorrowerId` FOREIGN KEY (`Borrower_id`) REFERENCES `borrower` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_LiabilitiesHeaderResponse_QueryResponse` FOREIGN KEY (`QueryResponse_id`) REFERENCES `queryresponse` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for liabilitiesresponse
--

DROP TABLE IF EXISTS `liabilitiesresponse`;

CREATE TABLE `liabilitiesresponse` (
  `id` varchar(50) NOT NULL,
  `QueryResponse_id` varchar(50) DEFAULT NULL,
  `Borrower_id` varchar(50) DEFAULT NULL,
  `LiabilitiesHeaderResponse_id` varchar(50) DEFAULT NULL,
  `Sequence` int(11) NOT NULL AUTO_INCREMENT,
  `AccountType` varchar(50) DEFAULT NULL,
  `CompanyName` varchar(50) DEFAULT NULL,
  `AccountNumber` varchar(50) DEFAULT NULL,
  `UnpaidBalance` decimal(10,0) DEFAULT NULL,
  `MonthlyPayment` decimal(10,0) DEFAULT NULL,
  `IsPaidOff` tinyint(1) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `Sequence_UNIQUE` (`Sequence`),
  KEY `FK_LiabilitiesResponse_QueryResponse_idx` (`QueryResponse_id`),
  KEY `FK_LiabilitiesResponse_LiabilitiesHeaderResponse_idx` (`LiabilitiesHeaderResponse_id`),
  KEY `FK_LiabilitiesResponse_BorrowerId_idx` (`Borrower_id`),
  KEY `FK_LiabilitiesResponse_AccountType_idx` (`AccountType`),
  CONSTRAINT `FK_LiabilitiesResponse_AccountType` FOREIGN KEY (`AccountType`) REFERENCES `optionitem` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_LiabilitiesResponse_BorrowerId` FOREIGN KEY (`Borrower_id`) REFERENCES `borrower` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_LiabilitiesResponse_LiabilitiesHeaderResponse` FOREIGN KEY (`LiabilitiesHeaderResponse_id`) REFERENCES `liabilitiesheaderresponse` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_LiabilitiesResponse_QueryResponse` FOREIGN KEY (`QueryResponse_id`) REFERENCES `queryresponse` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

--
-- Table structure for expensesheaderresponse
--

DROP TABLE IF EXISTS `expensesheaderresponse`;

CREATE TABLE `expensesheaderresponse` (
  `id` varchar(50) NOT NULL,
  `QueryResponse_id` varchar(50) NOT NULL,
  `Borrower_id` varchar(50) NOT NULL,
  `HasExpenses` tinyint(1) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`QueryResponse_id`,`Borrower_id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `FK_ExpensesHeaderResponse_QueryResponse_idx` (`Borrower_id`),
  CONSTRAINT `FK_ExpensesHeaderResponse_BorrowerId` FOREIGN KEY (`QueryResponse_id`) REFERENCES `queryresponse` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_ExpensesHeaderResponse_QueryResponse` FOREIGN KEY (`Borrower_id`) REFERENCES `borrower` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for expensesresponse
--

DROP TABLE IF EXISTS `expensesresponse`;

CREATE TABLE `expensesresponse` (
  `id` varchar(50) NOT NULL,
  `QueryResponse_id` varchar(50) DEFAULT NULL,
  `Borrower_id` varchar(50) DEFAULT NULL,
  `ExpensesHeaderResponse_id` varchar(50) DEFAULT NULL,
  `Sequence` int(11) NOT NULL AUTO_INCREMENT,
  `ExpenseType` varchar(50) DEFAULT NULL,
  `MonthlyPayment` decimal(10,0) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `Sequence_UNIQUE` (`Sequence`),
  KEY `FK_ExpensesResponse_QueryResponse_idx` (`QueryResponse_id`),
  KEY `FK_ExpensesResponse_ExpensesHeaderResponse_idx` (`ExpensesHeaderResponse_id`),
  KEY `FK_ExpensesResponse_BorrowerId_idx` (`Borrower_id`),
  KEY `FK_ExpensesResponse_ExpenseType_idx` (`ExpenseType`),
  CONSTRAINT `FK_ExpensesResponse_BorrowerId` FOREIGN KEY (`Borrower_id`) REFERENCES `borrower` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_ExpensesResponse_ExpenseType` FOREIGN KEY (`ExpenseType`) REFERENCES `optionitem` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_ExpensesResponse_ExpensesHeaderResponse` FOREIGN KEY (`ExpensesHeaderResponse_id`) REFERENCES `expensesheaderresponse` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_ExpensesResponse_QueryResponse` FOREIGN KEY (`QueryResponse_id`) REFERENCES `queryresponse` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;

-- 
--  demographicsresponse Table Structure
-- 

DROP TABLE IF EXISTS `demographicsresponse`;

CREATE TABLE `demographicsresponse` (
  `id` varchar(50) NOT NULL,
  `QueryResponse_id` varchar(50) DEFAULT NULL,
  `Borrower_id` varchar(50) DEFAULT NULL,
  `EthinicityDontWish` tinyint(1) DEFAULT NULL,
  `Ethinicity` varchar(50) DEFAULT NULL,
  `EthinicityOptions` varchar(500) DEFAULT NULL,
  `GenderDontWish` tinyint(1) DEFAULT NULL,
  `Gender` varchar(50) DEFAULT NULL,
  `RaceDontWish` tinyint(1) DEFAULT NULL,
  `RaceOptions` varchar(500) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Borrower_id_UNIQUE` (`Borrower_id`),
  KEY `FK_DemographicsResponse_BorrowerId_idx` (`Borrower_id`),
  KEY `FK_DemographicsResponse_QueryResponseId_idx` (`QueryResponse_id`),
  CONSTRAINT `FK_DemographicsResponse_BorrowerId` FOREIGN KEY (`Borrower_id`) REFERENCES `borrower` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_DemographicsResponse_QueryResponseId` FOREIGN KEY (`QueryResponse_id`) REFERENCES `queryresponse` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `querycoborrower` 
CHANGE COLUMN `Key` `Borrower_id` VARCHAR(50) NULL DEFAULT NULL ;

-- 
--  assetsresponse Table Structure
-- 

DROP TABLE IF EXISTS `assetsresponse`;

CREATE TABLE `assetsresponse` (
    `id` VARCHAR(50) NOT NULL,
    `QueryResponse_id` VARCHAR(50) NOT NULL,
    `Borrower_id` VARCHAR(50) NOT NULL,
    `HasBankAssets` TINYINT(1) DEFAULT NULL,
    `HasRealEstateAssets` TINYINT(1) DEFAULT NULL,
    `HasGiftedAssets` TINYINT(1) DEFAULT NULL,
    `HasOtherAssets` TINYINT(1) DEFAULT NULL,
    `Sequence` INT(11) NOT NULL AUTO_INCREMENT,
    `createdby` VARCHAR(50) DEFAULT NULL,
    `modifiedby` VARCHAR(50) DEFAULT NULL,
    `createdts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    `lastmodifiedts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `synctimestamp` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `softdeleteflag` TINYINT(1) DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `Sequence_UNIQUE` (`Sequence`),
    KEY `FK_AssetsResponse_QueryResponse_idx` (`QueryResponse_id`),
    KEY `FK_AssetsResponse_BorrowerId_idx` (`Borrower_id`),
    CONSTRAINT `FK_AssetsResponse_BorrowerId` FOREIGN KEY (`Borrower_id`)
        REFERENCES `borrower` (`id`)
        ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT `FK_AssetsResponse_QueryResponse` FOREIGN KEY (`QueryResponse_id`)
        REFERENCES `queryresponse` (`id`)
        ON DELETE NO ACTION ON UPDATE NO ACTION
)  ENGINE=INNODB DEFAULT CHARSET=UTF8;


-- 
--  bankaccountsresponse Table Structure
-- 

DROP TABLE IF EXISTS `bankaccountsresponse`;

CREATE TABLE `bankaccountsresponse` (
    `id` VARCHAR(50) NOT NULL,
    `AssetsResponse_id` VARCHAR(50) NOT NULL,
    `QueryResponse_id` VARCHAR(50) NOT NULL,
    `Borrower_id` VARCHAR(50) NOT NULL,
    `FinancialInstituteName` VARCHAR(100) DEFAULT NULL,
    `AccountType` VARCHAR(50) DEFAULT NULL,
    `AccountNumber` VARCHAR(100) DEFAULT NULL,
    `CashOrMarketValue` INT DEFAULT NULL,
    `InternationalAccountFlag` TINYINT(1),
    `Sequence` INT(11) NOT NULL AUTO_INCREMENT,
    `createdby` VARCHAR(50) DEFAULT NULL,
    `modifiedby` VARCHAR(50) DEFAULT NULL,
    `createdts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    `lastmodifiedts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `synctimestamp` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `softdeleteflag` TINYINT(1) DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `Sequence_UNIQUE` (`Sequence`),
    KEY `FK_BankAccountsResponse_QueryResponse_idx` (`QueryResponse_id`),
    KEY `FK_BankAccountsResponse_BorrowerId_idx` (`Borrower_id`),
    KEY `FK_BankAccountsResponse_AssetsResponse_idx` (`AssetsResponse_id`),
    CONSTRAINT `FK_BankAccountsResponse_BorrowerId` FOREIGN KEY (`Borrower_id`)
        REFERENCES `borrower` (`id`)
        ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT `FK_BankAccountsResponse_QueryResponse` FOREIGN KEY (`QueryResponse_id`)
        REFERENCES `queryresponse` (`id`)
        ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT `FK_BankAccountsResponse_AssetsResponse` FOREIGN KEY (`AssetsResponse_id`)
        REFERENCES `assetsresponse` (`id`)
        ON DELETE NO ACTION ON UPDATE NO ACTION
)  ENGINE=INNODB DEFAULT CHARSET=UTF8;

-- 
--  realestatesresponse Table Structure
-- 

DROP TABLE IF EXISTS `realestatesresponse`;

CREATE TABLE `realestatesresponse` (
    `id` VARCHAR(50) NOT NULL,
    `AssetsResponse_id` VARCHAR(50) NOT NULL,
    `QueryResponse_id` VARCHAR(50) NOT NULL,
    `Borrower_id` VARCHAR(50) NOT NULL,
    `Status` VARCHAR(50) DEFAULT NULL,
    `MarketValue` INT DEFAULT NULL,
    `PropertyUsageType` VARCHAR(50) DEFAULT NULL,
    `MonthlyRentalIncome` INT DEFAULT NULL,
    `MonthlyPayments` INT DEFAULT NULL,
    `PropertyAddessLine1` VARCHAR(100) DEFAULT NULL,
    `PropertyAddessLine2` VARCHAR(100) DEFAULT NULL,
    `City` VARCHAR(50) DEFAULT NULL,
    `State` VARCHAR(50) DEFAULT NULL,
    `Zip` VARCHAR(50) DEFAULT NULL,
    `Country` VARCHAR(50) DEFAULT NULL,
    `OngoingMortgageFlag` TINYINT(1) DEFAULT NULL,
    `CreditorName` VARCHAR(50) DEFAULT NULL,
    `CreditorAccountNumber` VARCHAR(100) DEFAULT NULL,
    `MonthlyMortgagePayments` INT DEFAULT NULL,
    `MortgageType` VARCHAR(50) DEFAULT NULL,
    `CreditLimit` INT DEFAULT NULL,
    `UnpaidBalance` INT DEFAULT NULL,
    `PlanToPayoffFlag` TINYINT(1) DEFAULT NULL,
    `Sequence` INT(11) NOT NULL AUTO_INCREMENT,
    `createdby` VARCHAR(50) DEFAULT NULL,
    `modifiedby` VARCHAR(50) DEFAULT NULL,
    `createdts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    `lastmodifiedts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `synctimestamp` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `softdeleteflag` TINYINT(1) DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `Sequence_UNIQUE` (`Sequence`),
    KEY `FK_RealEstatesResponse_QueryResponse_idx` (`QueryResponse_id`),
    KEY `FK_RealEstatesResponse_BorrowerId_idx` (`Borrower_id`),
    KEY `FK_RealEstatesResponse_AssetsResponse_idx` (`AssetsResponse_id`),
    CONSTRAINT `FK_RealEstatesResponse_BorrowerId` FOREIGN KEY (`Borrower_id`)
        REFERENCES `borrower` (`id`)
        ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT `FK_RealEstatesResponse_QueryResponse` FOREIGN KEY (`QueryResponse_id`)
        REFERENCES `queryresponse` (`id`)
        ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT `FK_RealEstatesResponse_AssetsResponse` FOREIGN KEY (`AssetsResponse_id`)
        REFERENCES `assetsresponse` (`id`)
        ON DELETE NO ACTION ON UPDATE NO ACTION
)  ENGINE=INNODB DEFAULT CHARSET=UTF8;

-- 
--  giftsandgrantsresponse Table Structure
-- 

DROP TABLE IF EXISTS `giftsandgrantsresponse`;

CREATE TABLE `giftsandgrantsresponse` (
    `id` VARCHAR(50) NOT NULL,
    `AssetsResponse_id` VARCHAR(50) NOT NULL,
    `QueryResponse_id` VARCHAR(50) NOT NULL,
    `Borrower_id` VARCHAR(50) NOT NULL,
    `AssetType` VARCHAR(50) DEFAULT NULL,
    `AssetSource` VARCHAR(50) DEFAULT NULL,
    `CashOrMarketValue` INT DEFAULT NULL,
    `DepositedStatus` TINYINT(1) DEFAULT NULL,
    `Sequence` INT(11) NOT NULL AUTO_INCREMENT,
    `createdby` VARCHAR(50) DEFAULT NULL,
    `modifiedby` VARCHAR(50) DEFAULT NULL,
    `createdts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    `lastmodifiedts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `synctimestamp` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `softdeleteflag` TINYINT(1) DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `Sequence_UNIQUE` (`Sequence`),
    KEY `FK_GiftsAndGrantsResponse_QueryResponse_idx` (`QueryResponse_id`),
    KEY `FK_GiftsAndGrantsResponse_BorrowerId_idx` (`Borrower_id`),
    KEY `FK_GiftsAndGrantsResponse_AssetsResponse_idx` (`AssetsResponse_id`),
    CONSTRAINT `FK_GiftsAndGrantsResponse_BorrowerId` FOREIGN KEY (`Borrower_id`)
        REFERENCES `borrower` (`id`)
        ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT `FK_GiftsAndGrantsResponse_QueryResponse` FOREIGN KEY (`QueryResponse_id`)
        REFERENCES `queryresponse` (`id`)
        ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT `FK_GiftsAndGrantsResponse_AssetsResponse` FOREIGN KEY (`AssetsResponse_id`)
        REFERENCES `assetsresponse` (`id`)
        ON DELETE NO ACTION ON UPDATE NO ACTION
)  ENGINE=INNODB DEFAULT CHARSET=UTF8;

-- 
--  otherassetsresponse Table Structure
-- 

DROP TABLE IF EXISTS `otherassetsresponse`;

CREATE TABLE `otherassetsresponse` (
    `id` VARCHAR(50) NOT NULL,
    `AssetsResponse_id` VARCHAR(50) NOT NULL,
    `QueryResponse_id` VARCHAR(50) NOT NULL,
    `Borrower_id` VARCHAR(50) NOT NULL,
    `AssetType` VARCHAR(50) DEFAULT NULL,
    `CashOrMarketValue` INT DEFAULT NULL,
    `Sequence` INT(11) NOT NULL AUTO_INCREMENT,
    `createdby` VARCHAR(50) DEFAULT NULL,
    `modifiedby` VARCHAR(50) DEFAULT NULL,
    `createdts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    `lastmodifiedts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `synctimestamp` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `softdeleteflag` TINYINT(1) DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `Sequence_UNIQUE` (`Sequence`),
    KEY `FK_OtherAssetsResponse_QueryResponse_idx` (`QueryResponse_id`),
    KEY `FK_OtherAssetsResponse_BorrowerId_idx` (`Borrower_id`),
    KEY `FK_OtherAssetsResponse_AssetsResponse_idx` (`AssetsResponse_id`),
    CONSTRAINT `FK_OtherAssetsResponse_BorrowerId` FOREIGN KEY (`Borrower_id`)
        REFERENCES `borrower` (`id`)
        ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT `FK_OtherAssetsResponse_QueryResponse` FOREIGN KEY (`QueryResponse_id`)
        REFERENCES `queryresponse` (`id`)
        ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT `FK_OtherAssetsResponse_AssetsResponse` FOREIGN KEY (`AssetsResponse_id`)
        REFERENCES `assetsresponse` (`id`)
        ON DELETE NO ACTION ON UPDATE NO ACTION
)  ENGINE=INNODB DEFAULT CHARSET=UTF8;

ALTER TABLE `legaldeclarationsresponse` 
CHANGE COLUMN `IsPrimaryResident` `IsPrimaryResident` TINYINT NULL DEFAULT NULL ,
CHANGE COLUMN `HasRelationshipWithSeller` `HasRelationshipWithSeller` TINYINT NULL DEFAULT NULL ,
CHANGE COLUMN `IsBorrowingMoney` `IsBorrowingMoney` TINYINT NULL DEFAULT NULL ,
CHANGE COLUMN `HasAnyOtherMortgageLoan` `HasAnyOtherMortgageLoan` TINYINT NULL DEFAULT NULL ,
CHANGE COLUMN `HasNewCredit` `HasNewCredit` TINYINT NULL DEFAULT NULL ,
CHANGE COLUMN `HasPropertyLien` `HasPropertyLien` TINYINT NULL DEFAULT NULL ,
CHANGE COLUMN `IsGuarantorOfAnyLoan` `IsGuarantorOfAnyLoan` TINYINT NULL DEFAULT NULL ,
CHANGE COLUMN `HasOutsandingJudgements` `HasOutsandingJudgements` TINYINT NULL DEFAULT NULL ,
CHANGE COLUMN `HasFinancialLiability` `HasFinancialLiability` TINYINT NULL DEFAULT NULL ,
CHANGE COLUMN `IsDelinquent` `IsDelinquent` TINYINT NULL DEFAULT NULL ,
CHANGE COLUMN `HasForeclosureInPast` `HasForeclosureInPast` TINYINT NULL DEFAULT NULL ,
CHANGE COLUMN `HasPreForeclosureSale` `HasPreForeclosureSale` TINYINT NULL DEFAULT NULL ,
CHANGE COLUMN `HasPropertyForeclosedInPast` `HasPropertyForeclosedInPast` TINYINT NULL DEFAULT NULL ,
CHANGE COLUMN `HasDeclaredBankruptcyInPast` `HasDeclaredBankruptcyInPast` TINYINT NULL DEFAULT NULL ,
CHANGE COLUMN `softdeleteflag` `softdeleteflag` TINYINT(1) NULL DEFAULT 0 ,
ADD UNIQUE INDEX `Borrower_id_UNIQUE` (`Borrower_id` ASC);

ALTER TABLE `legaldeclarationsresponse` 
ADD CONSTRAINT `FK_legaldeclarationsresponse_Borrower_id`
  FOREIGN KEY (`Borrower_id`)
  REFERENCES `borrower` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE `legaldeclarationsresponse` 
DROP FOREIGN KEY `FK_LegalDeclarationsResponse_QueryResponse`;
ALTER TABLE `legaldeclarationsresponse` 
CHANGE COLUMN `QueryReponse_id` `QueryResponse_id` VARCHAR(50) NULL DEFAULT NULL ;
ALTER TABLE `legaldeclarationsresponse` 
ADD CONSTRAINT `FK_LegalDeclarationsResponse_QueryResponse`
  FOREIGN KEY (`QueryResponse_id`)
  REFERENCES `queryresponse` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE `personalinforesponse` 
ADD COLUMN `IsMailingAddCheckbox` TINYINT(1) NULL DEFAULT '0' AFTER `softdeleteflag`;


ALTER TABLE `personalinforesponse` 
CHANGE COLUMN `MobileNumber` `MobileNumber` VARCHAR(10) NULL DEFAULT NULL ,
CHANGE COLUMN `OfficeNumber` `OfficeNumber` VARCHAR(10) NULL DEFAULT NULL ,
CHANGE COLUMN `HomeNumber` `HomeNumber` VARCHAR(10) NULL DEFAULT NULL ;


ALTER TABLE `personalinforesponse` 
CHANGE COLUMN `valuesForDependents` `ValuesForDependents` VARCHAR(50) NULL DEFAULT NULL ,
ADD UNIQUE INDEX `Borrower_id_UNIQUE` (`Borrower_id` ASC);

-- 
-- Table Definition `losapplications`
-- 
DROP TABLE IF EXISTS `losapplications`;

CREATE TABLE `losapplications` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `Customer_id` VARCHAR(50) NULL,
  `Status_id` VARCHAR(50) NULL,
  `QueryResponse_id` VARCHAR(50) NULL,
  PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;
  
ALTER TABLE `legaldeclarationsresponse` 
CHANGE COLUMN `IsPrimaryResident` `IsPrimaryResident` TINYINT(1) NULL DEFAULT NULL ,
CHANGE COLUMN `HasRelationshipWithSeller` `HasRelationshipWithSeller` TINYINT(1) NULL DEFAULT NULL ,
CHANGE COLUMN `IsBorrowingMoney` `IsBorrowingMoney` TINYINT(1) NULL DEFAULT NULL ,
CHANGE COLUMN `HasAnyOtherMortgageLoan` `HasAnyOtherMortgageLoan` TINYINT(1) NULL DEFAULT NULL ,
CHANGE COLUMN `HasNewCredit` `HasNewCredit` TINYINT(1) NULL DEFAULT NULL ,
CHANGE COLUMN `HasPropertyLien` `HasPropertyLien` TINYINT(1) NULL DEFAULT NULL ,
CHANGE COLUMN `IsGuarantorOfAnyLoan` `IsGuarantorOfAnyLoan` TINYINT(1) NULL DEFAULT NULL ,
CHANGE COLUMN `HasOutsandingJudgements` `HasOutsandingJudgements` TINYINT(1) NULL DEFAULT NULL ,
CHANGE COLUMN `HasFinancialLiability` `HasFinancialLiability` TINYINT(1) NULL DEFAULT NULL ,
CHANGE COLUMN `IsDelinquent` `IsDelinquent` TINYINT(1) NULL DEFAULT NULL ,
CHANGE COLUMN `HasForeclosureInPast` `HasForeclosureInPast` TINYINT(1) NULL DEFAULT NULL ,
CHANGE COLUMN `HasPreForeclosureSale` `HasPreForeclosureSale` TINYINT(1) NULL DEFAULT NULL ,
CHANGE COLUMN `HasPropertyForeclosedInPast` `HasPropertyForeclosedInPast` TINYINT(1) NULL DEFAULT NULL ,
CHANGE COLUMN `HasDeclaredBankruptcyInPast` `HasDeclaredBankruptcyInPast` TINYINT(1) NULL DEFAULT NULL ;

--
-- Table Definition `optionmetadata`
--
DROP TABLE IF EXISTS `optionmetadata`;

CREATE TABLE `optionmetadata` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `LoanType_id` varchar(50) DEFAULT NULL,
  `OptionGroup_id` varchar(50) DEFAULT NULL,
  `FieldIdentifier` varchar(50) DEFAULT NULL,
  `Section_id` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `FK_OptionMetaData_OptionGroup` (`OptionGroup_id`),
  KEY `FK_OptionMetaData_LoanType_idx` (`LoanType_id`),
  CONSTRAINT `FK_OptionMetaData_LoanType` FOREIGN KEY (`LoanType_id`) REFERENCES `loantype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_OptionMetaData_OptionGroup` FOREIGN KEY (`OptionGroup_id`) REFERENCES `optiongroup` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

ALTER TABLE `demographicsresponse` 
CHANGE COLUMN `Ethinicity` `Ethinicity` TINYINT(1) NULL DEFAULT NULL ;

--
-- Table Definition `additionalemploymentsresponse`
--
DROP TABLE IF EXISTS `additionalemploymentsresponse`;

CREATE TABLE `additionalemploymentsresponse` (
  `id` varchar(50) NOT NULL,
  `IncomeResponse_id` varchar(50) NOT NULL,
  `QueryResponse_id` varchar(50) NOT NULL,
  `Borrower_id` varchar(50) DEFAULT NULL,
  `EmploymentType_id` varchar(50) DEFAULT NULL,
  `ProvideEmploymentDetails` tinyint(1) DEFAULT NULL,
  `EmployerName` varchar(50) DEFAULT NULL,
  `EmployerAddressLine1` varchar(100) DEFAULT NULL,
  `EmployerAddressLine2` varchar(100) DEFAULT NULL,
  `EmployerAddressCity` varchar(50) DEFAULT NULL,
  `EmployerAddressState` varchar(50) DEFAULT NULL,
  `EmployerAddressCountry` varchar(50) DEFAULT NULL,
  `EmployerAddressZipCode` int(11) DEFAULT NULL,
  `EmployerPhoneNumber` varchar(15) DEFAULT NULL,
  `TotalGrossIncome` decimal(10,2) DEFAULT NULL,
  `EmployeeDesignation` varchar(50) DEFAULT NULL,
  `BusinessShare_id` varchar(50) DEFAULT NULL,
  `BusinessMonthlyLoss` decimal(10,2) DEFAULT NULL,
  `ProfessionStartDate` date DEFAULT NULL,
  `LineOfWorkDuration` varchar(10) DEFAULT NULL,
  `IsOtherParty` tinyint(1) DEFAULT NULL,
  `PrevStartDate` date DEFAULT NULL,
  `PrevEndDate` date DEFAULT NULL,
  `Sequence` int(11) NOT NULL AUTO_INCREMENT,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `Sequence_UNIQUE` (`Sequence`),
  KEY `FK_AdditionalEmploymentsResponse_Borrower_idx` (`Borrower_id`),
  KEY `FK_AdditionalEmploymentsResponse_IncomeResponse_idx` (`IncomeResponse_id`),
  KEY `FK_AdditionalEmploymentsResponse_OptionItem_idx` (`BusinessShare_id`),
  KEY `FK_AdditionalEmploymentsResponse_OptionItem2_idx` (`EmploymentType_id`),
  CONSTRAINT `FK_AdditionalEmploymentsResponse_Borrower` FOREIGN KEY (`Borrower_id`) REFERENCES `borrower` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_AdditionalEmploymentsResponse_IncomeResponse` FOREIGN KEY (`IncomeResponse_id`) REFERENCES `incomeresponse` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_AdditionalEmploymentsResponse_OptionItem` FOREIGN KEY (`BusinessShare_id`) REFERENCES `optionitem` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_AdditionalEmploymentsResponse_OptionItem2` FOREIGN KEY (`EmploymentType_id`) REFERENCES `optionitem` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table Definition `additionalincomesresponse`
--
DROP TABLE IF EXISTS `additionalincomesresponse`;

CREATE TABLE `additionalincomesresponse` (
  `id` varchar(50) NOT NULL,
  `QueryResponse_id` varchar(50) NOT NULL,
  `Borrower_id` varchar(50) NOT NULL,
  `AdditionalEmploymentsResponse_id` varchar(50) DEFAULT NULL,
  `IncomeDetail_id` varchar(50) DEFAULT NULL,
  `PayPeriod_id` varchar(50) DEFAULT NULL,
  `Amount` decimal(10,2) DEFAULT NULL,
  `WorkingHours` int(11) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) DEFAULT '0',
  KEY `FK_AdditionalIncomesResponse_QueryResponse_idx` (`QueryResponse_id`),
  KEY `FK_AdditionalIncomesResponse_Borrower_idx` (`Borrower_id`),
  KEY `FK_AdditionalIncomesResponse_AdditionalEmploymentsResponse_idx` (`AdditionalEmploymentsResponse_id`),
  KEY `FK_AdditionalIncomesResponse_OptionItem_idx` (`IncomeDetail_id`),
  KEY `FK_AdditionalIncomesResponse_OptionItem2_idx` (`PayPeriod_id`),
  CONSTRAINT `FK_AdditionalIncomesResponse_AdditionalEmploymentsResponse` FOREIGN KEY (`AdditionalEmploymentsResponse_id`) REFERENCES `additionalemploymentsresponse` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_AdditionalIncomesResponse_Borrower` FOREIGN KEY (`Borrower_id`) REFERENCES `borrower` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_AdditionalIncomesResponse_OptionItem` FOREIGN KEY (`IncomeDetail_id`) REFERENCES `optionitem` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_AdditionalIncomesResponse_OptionItem2` FOREIGN KEY (`PayPeriod_id`) REFERENCES `optionitem` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_AdditionalIncomesResponse_QueryResponse` FOREIGN KEY (`QueryResponse_id`) REFERENCES `queryresponse` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `employmentdetailsresponse` 
DROP COLUMN `Sequence`,
DROP INDEX `Sequence_UNIQUE` ;

ALTER TABLE `realestateagentresponse` 
CHANGE COLUMN `MobileNumber` `MobileNumber` VARCHAR(15) NULL DEFAULT NULL ;

ALTER TABLE `additionalmortgageresponse` 
ADD COLUMN `HasAdditionalMortgage` TINYINT(1) NULL AFTER `softdeleteflag`;

ALTER TABLE `legaldeclarationsresponse` 
CHANGE COLUMN `OtherMortgageLoanAmount` `OtherMortgageLoanAmount` VARCHAR(50) NULL DEFAULT NULL ;

ALTER TABLE `legaldeclarationsresponse` 
CHANGE COLUMN `BankruptcyType` `BankruptcyType` VARCHAR(60) NULL DEFAULT NULL ;

ALTER TABLE `sectionstatus` 
CHANGE COLUMN `id` `id` VARCHAR(50) NOT NULL ,
CHANGE COLUMN `ApplicantPersonalInfo` `ApplicantPersonalInfo` VARCHAR(10) NULL DEFAULT 'notDone' ,
CHANGE COLUMN `MyMortgage` `MyMortgage` VARCHAR(10) NULL DEFAULT 'notDone' ,
CHANGE COLUMN `LoanAndPropertyInfo` `LoanAndPropertyInfo` VARCHAR(10) NULL DEFAULT 'notDone' ,
CHANGE COLUMN `CoApplicantAndAgentInfo` `CoApplicantAndAgentInfo` VARCHAR(10) NULL DEFAULT 'notDone' ,
CHANGE COLUMN `LoanSelection` `LoanSelection` VARCHAR(10) NULL DEFAULT 'notDone' ,
CHANGE COLUMN `LoanOfficerSelection` `LoanOfficerSelection` VARCHAR(10) NULL DEFAULT 'notDone' ,
CHANGE COLUMN `UserDetails` `UserDetails` VARCHAR(10) NULL DEFAULT 'notDone' ,
CHANGE COLUMN `Income` `Income` VARCHAR(10) NULL DEFAULT 'notDone' ,
CHANGE COLUMN `Assets` `Assets` VARCHAR(10) NULL DEFAULT 'notDone' ,
CHANGE COLUMN `LiabilitiesAndExpenses` `LiabilitiesAndExpenses` VARCHAR(10) NULL DEFAULT 'notDone' ,
CHANGE COLUMN `LegalDeclarations` `LegalDeclarations` VARCHAR(10) NULL DEFAULT 'notDone' ,
CHANGE COLUMN `Demographics` `Demographics` VARCHAR(10) NULL DEFAULT 'notDone' ,
CHANGE COLUMN `Consent` `Consent` VARCHAR(10) NULL DEFAULT 'notDone' ,
CHANGE COLUMN `ReviewAndSubmit` `ReviewAndSubmit` VARCHAR(10) NULL DEFAULT 'notDone' ,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`QueryResponse_id`, `Borrower_id`, `id`);
;

DROP procedure IF EXISTS `sp_getAllApplications`;
DELIMITER $$
CREATE PROCEDURE `sp_getAllApplications`(IN `User_id` VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci, IN `Status_id` VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci)
BEGIN
IF Status_id = 'ALL' THEN

(SELECT
    queryresp.id AS id,
    queryresp.QueryDefinition_id AS QueryDefinition_id,
    queryresp.Customer_id AS User_id,
    queryresp.CoBorrower_id AS CoBorrower_id,
    queryresp.createdts AS StartDate,
    queryresp.lastmodifiedts AS LastEditedDate,
    queryresp.LoanProduct_id AS LoanProduct_id,
    queryresp.Application_id AS Application_id,
    loanProd.LoanType_id AS LoanType_id,
    queryresp.Status_id AS Status,
    querycoborrower.CoBorrower_Type AS CoBorrower_Type,
    (SELECT
            CAST(CONCAT('[',
                            GROUP_CONCAT(JSON_OBJECT('QuerySectionQuestion_id',
                                        questResp.QuerySectionQuestion_id,
                                        'ResponseValue',
                                        questResp.ResponseValue)
                                SEPARATOR ','),
                            ']')
                    AS CHAR CHARSET UTF8MB4)
        FROM
            `questionresponse` questResp
        WHERE
            questResp.queryresponse_id = queryresp.id
                 AND `questResp`.`QuerySectionQuestion_id` IN ('VA_LOAN_AMOUNT' , 'VA_COBORROWER',
                'CCA_CARDLIMIT',
                'PA_LOANAMOUNT',
                'PA_COBORROWER', 'MA_REQUIREDLOANAMOUNT' ,'MA_COBORROWER')) AS `QuestionResponse`
FROM
    `queryresponse` queryresp
        JOIN
    loanproduct loanProd ON queryresp.LoanProduct_id = loanProd.id
        LEFT JOIN
    querycoborrower querycoborrower ON queryresp.id = querycoborrower.queryresponse_id
WHERE
    (`queryresp`.`Customer_id` = User_id COLLATE utf8_unicode_ci)
        AND `queryresp`.`QueryDefinition_id` IN ('PERSONAL_APPLICATION' , 'VEHICLE_APPLICATION',
        'CREDIT_CARD_APPLICATION' , 'MORTGAGE_APPLICATION') AND `queryresp`.softdeleteflag IS FALSE) UNION ALL (SELECT
    queryresp.id AS id,
    queryresp.QueryDefinition_id AS QueryDefinition_id,
    queryresp.Customer_id AS User_id,
    queryresp.CoBorrower_id AS CoBorrower_id,
    queryresp.createdts AS StartDate,
    queryresp.lastmodifiedts AS LastEditedDate,
    queryresp.LoanProduct_id AS LoanProduct_id,
    queryresp.Application_id AS Application_id,
    loanProd.LoanType_id AS LoanType_id,
    queryresp.Status_id AS STATUS,
    querycoborrower.CoBorrower_Type AS CoBorrower_Type,
    (SELECT
            CAST(CONCAT('[',
                            GROUP_CONCAT(JSON_OBJECT('QuerySectionQuestion_id',
                                        questResp.QuerySectionQuestion_id,
                                        'ResponseValue',
                                        questResp.ResponseValue)
                                SEPARATOR ','),
                            ']')
                    AS CHAR CHARSET UTF8MB4)
        FROM
            `questionresponse` questResp
        WHERE
            questResp.queryresponse_id = queryresp.id
                AND `questResp`.`QuerySectionQuestion_id` IN ('VA_LOAN_AMOUNT', 'VA_COBORROWER',
                'CCA_CARDLIMIT',
                'PA_LOANAMOUNT',
                'PA_COBORROWER')) AS `QuestionResponse`
FROM
    `queryresponse` queryresp
        JOIN
    loanproduct loanProd ON queryresp.LoanProduct_id = loanProd.id
        LEFT JOIN
    querycoborrower querycoborrower ON queryresp.id = querycoborrower.queryresponse_id
WHERE
    (`queryresp`.`CoBorrower_id` = User_id COLLATE utf8_unicode_ci)
        AND `queryresp`.`QueryDefinition_id` IN ('PERSONAL_APPLICATION' , 'VEHICLE_APPLICATION',
        'CREDIT_CARD_APPLICATION', 'MORTGAGE_APPLICATION') AND `queryresp`.softdeleteflag IS FALSE) ORDER BY LastEditedDate DESC;
        ELSE
           (SELECT
    queryresp.id AS id,
    queryresp.QueryDefinition_id AS QueryDefinition_id,
    queryresp.Customer_id AS User_id,
    queryresp.CoBorrower_id AS CoBorrower_id,
    queryresp.createdts AS StartDate,
    queryresp.lastmodifiedts AS LastEditedDate,
    queryresp.LoanProduct_id AS LoanProduct_id,
    queryresp.Application_id AS Application_id,
    loanProd.LoanType_id AS LoanType_id,
    queryresp.Status_id AS Status,
    querycoborrower.CoBorrower_Type AS CoBorrower_Type,
    (SELECT
            CAST(CONCAT('[',
                            GROUP_CONCAT(JSON_OBJECT('QuerySectionQuestion_id',
                                        questResp.QuerySectionQuestion_id,
                                        'ResponseValue',
                                        questResp.ResponseValue)
                                SEPARATOR ','),
                            ']')
                    AS CHAR CHARSET UTF8MB4)
        FROM
            `questionresponse` questResp
        WHERE
            questResp.queryresponse_id = queryresp.id
                 AND `questResp`.`QuerySectionQuestion_id` IN ('VA_LOAN_AMOUNT' , 'VA_COBORROWER',
                'CCA_CARDLIMIT',
                'PA_LOANAMOUNT',
                'PA_COBORROWER')) AS `QuestionResponse`
FROM
    `queryresponse` queryresp
        JOIN
    loanproduct loanProd ON queryresp.LoanProduct_id = loanProd.id
        LEFT JOIN
    querycoborrower querycoborrower ON queryresp.id = querycoborrower.queryresponse_id
WHERE
    (`queryresp`.`Customer_id` = User_id COLLATE utf8_unicode_ci)
    AND `queryresp`.`Status_id` = Status_id
        AND `queryresp`.`QueryDefinition_id` IN ('PERSONAL_APPLICATION' , 'VEHICLE_APPLICATION',
        'CREDIT_CARD_APPLICATION') AND `queryresp`.softdeleteflag IS FALSE) UNION ALL (SELECT
    queryresp.id AS id,
    queryresp.QueryDefinition_id AS QueryDefinition_id,
    queryresp.Customer_id AS User_id,
    queryresp.CoBorrower_id AS CoBorrower_id,
    queryresp.createdts AS StartDate,
    queryresp.lastmodifiedts AS LastEditedDate,
    queryresp.LoanProduct_id AS LoanProduct_id,
    queryresp.Application_id AS Application_id,
    loanProd.LoanType_id AS LoanType_id,
    queryresp.Status_id AS STATUS,
    querycoborrower.CoBorrower_Type AS CoBorrower_Type,
    (SELECT
            CAST(CONCAT('[',
                            GROUP_CONCAT(JSON_OBJECT('QuerySectionQuestion_id',
                                        questResp.QuerySectionQuestion_id,
                                        'ResponseValue',
                                        questResp.ResponseValue)
                                SEPARATOR ','),
                            ']')
                    AS CHAR CHARSET UTF8MB4)
        FROM
            `questionresponse` questResp
        WHERE
            questResp.queryresponse_id = queryresp.id
                AND `questResp`.`QuerySectionQuestion_id` IN ('VA_LOAN_AMOUNT', 'VA_COBORROWER',
                'CCA_CARDLIMIT',
                'PA_LOANAMOUNT',
                'PA_COBORROWER')) AS `QuestionResponse`
FROM
    `queryresponse` queryresp
        JOIN
    loanproduct loanProd ON queryresp.LoanProduct_id = loanProd.id
        LEFT JOIN
    querycoborrower querycoborrower ON queryresp.id = querycoborrower.queryresponse_id
WHERE
    (`queryresp`.`CoBorrower_id` = User_id COLLATE utf8_unicode_ci)
        AND `queryresp`.`QueryDefinition_id` IN ('PERSONAL_APPLICATION' , 'VEHICLE_APPLICATION',
        'CREDIT_CARD_APPLICATION') AND `queryresp`.softdeleteflag IS FALSE) ORDER BY LastEditedDate DESC;
        END IF;
END$$
DELIMITER ;

ALTER TABLE `personalinfoaddressresponse` 
ADD COLUMN `PersonalInfoResponse_id` VARCHAR(50) NULL AFTER `softdeleteflag`,
ADD INDEX `FK_PersonalInfoAddressResponse_personalinforesponse_idx` (`PersonalInfoResponse_id` ASC);
ALTER TABLE `personalinfoaddressresponse` 
ADD CONSTRAINT `FK_PersonalInfoAddressResponse_personalinforesponse`
  FOREIGN KEY (`PersonalInfoResponse_id`)
  REFERENCES `personalinforesponse` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
  
-- 
-- Table Definition `additionalmortgageheaderresponse`
-- 
DROP TABLE IF EXISTS `additionalmortgageheaderresponse`;
CREATE TABLE `additionalmortgageheaderresponse` (
  `id` VARCHAR(50) NOT NULL,
  `QueryResponse_id` VARCHAR(50) NULL,
  `Borrower_id` VARCHAR(50) NULL,
  `HasAdditionalMortgage` TINYINT(1) NULL,
  `createdby` VARCHAR(50) NULL,
  `modifiedby` VARCHAR(50) NULL,
  `createdts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` TINYINT(1) NULL,
  PRIMARY KEY (`id`),
  INDEX `FK_AdditionalMortgageHeaderResponse_QueryResponse_idx` (`QueryResponse_id` ASC),
  CONSTRAINT `FK_AdditionalMortgageHeaderResponse_QueryResponse`
    FOREIGN KEY (`QueryResponse_id`)
    REFERENCES `queryresponse` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)ENGINE=InnoDB DEFAULT CHARSET=utf8;
	

ALTER TABLE `additionalmortgageresponse` 
DROP COLUMN `HasAdditionalMortgage`,
ADD COLUMN `AdditionalMortgageHeaderResponse_id` VARCHAR(50) NULL AFTER `softdeleteflag`;

ALTER TABLE `employmentdetailsresponse` 
DROP FOREIGN KEY `FK_EmploymentDetailsResponse_IncomeResponse`;
ALTER TABLE `employmentdetailsresponse` 
DROP INDEX `FK_EmploymentDetailsResponse_IncomeResponse_idx` ;

ALTER TABLE `incomedistributionresponse` 
DROP FOREIGN KEY `FK_IncomeDistributionResponse_EmploymentDetaislResponse`;
ALTER TABLE `incomedistributionresponse` 
DROP INDEX `FK_IncomeDistributionResponse_EmploymentDetaislResponse_idx` ;

ALTER TABLE `additionalemploymentsresponse` 
DROP FOREIGN KEY `FK_AdditionalEmploymentsResponse_IncomeResponse`;
ALTER TABLE `additionalemploymentsresponse` 
DROP INDEX `FK_AdditionalEmploymentsResponse_IncomeResponse_idx` ;

ALTER TABLE `additionalincomesresponse` 
DROP FOREIGN KEY `FK_AdditionalIncomesResponse_AdditionalEmploymentsResponse`;
ALTER TABLE `additionalincomesresponse` 
DROP INDEX `FK_AdditionalIncomesResponse_AdditionalEmploymentsResponse_idx` ;

ALTER TABLE `otherincomesresponse` 
DROP FOREIGN KEY `FK_OtherIncomesResponse_IncomeResponse`;
ALTER TABLE `otherincomesresponse` 
DROP INDEX `FK_OtherIncomesResponse_IncomeResponse_idx` ;


ALTER TABLE `incomeresponse` 
DROP PRIMARY KEY,
ADD PRIMARY KEY (`QueryResponse_id`, `Borrower_id`);


ALTER TABLE `employmentdetailsresponse` 
DROP FOREIGN KEY `FK_EmploymentDetailsResponse_Borrower`,
DROP FOREIGN KEY `FK_EmploymentDetailsResponse_OptionItem3`;
ALTER TABLE `employmentdetailsresponse` 
CHANGE COLUMN `Borrower_id` `Borrower_id` VARCHAR(50) NOT NULL ,
CHANGE COLUMN `EmploymentDetailType_id` `EmploymentDetailType_id` VARCHAR(50) NOT NULL ,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`IncomeResponse_id`, `QueryResponse_id`, `Borrower_id`, `EmploymentDetailType_id`);
ALTER TABLE `employmentdetailsresponse` 
ADD CONSTRAINT `FK_EmploymentDetailsResponse_Borrower`
  FOREIGN KEY (`Borrower_id`)
  REFERENCES `borrower` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION,
ADD CONSTRAINT `FK_EmploymentDetailsResponse_OptionItem3`
  FOREIGN KEY (`EmploymentDetailType_id`)
  REFERENCES `optionitem` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;


ALTER TABLE `incomeresponse` 
ADD UNIQUE INDEX `id_UNIQUE` (`id` ASC);

ALTER TABLE `employmentdetailsresponse` 
ADD UNIQUE INDEX `id_UNIQUE` (`id` ASC);


ALTER TABLE `additionalemploymentsresponse` 
ADD CONSTRAINT `FK_AdditionalEmploymentsResponse_IncomeResponse`
  FOREIGN KEY (`IncomeResponse_id`)
  REFERENCES `incomeresponse` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

  
  
ALTER TABLE `additionalincomesresponse` 
ADD PRIMARY KEY (`id`);

ALTER TABLE `additionalincomesresponse` 
ADD CONSTRAINT `FK_AdditionalIncomesResponse_AdditionalEmploymentsResponse`
  FOREIGN KEY (`AdditionalEmploymentsResponse_id`)
  REFERENCES `additionalemploymentsresponse` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;


ALTER TABLE `incomedistributionresponse` 
ADD INDEX `FK_IncomeDistributionResponse_EmploymentDetailsResponse_idx` (`EmploymentDetailsResponse_id` ASC);
ALTER TABLE `incomedistributionresponse` 
ADD CONSTRAINT `FK_IncomeDistributionResponse_EmploymentDetailsResponse`
  FOREIGN KEY (`EmploymentDetailsResponse_id`)
  REFERENCES `employmentdetailsresponse` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE `otherincomesresponse` 
ADD INDEX `FK_OtherIncomesResponse_IncomeResponse_idx` (`IncomeResponse_id` ASC);
ALTER TABLE `otherincomesresponse` 
ADD CONSTRAINT `FK_OtherIncomesResponse_IncomeResponse`
  FOREIGN KEY (`IncomeResponse_id`)
  REFERENCES `incomeresponse` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;



ALTER TABLE `employmentdetailsresponse` 
ADD CONSTRAINT `FK_EmploymentDetailsResponse_IncomeResponse`
  FOREIGN KEY (`IncomeResponse_id`)
  REFERENCES `incomeresponse` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
  
ALTER TABLE `legaldeclarationsresponse` 
CHANGE COLUMN `TypeOfProperty` `TypeOfProperty` VARCHAR(50) NULL DEFAULT NULL ,
CHANGE COLUMN `TitleOfProperty` `TitleOfProperty` VARCHAR(50) NULL DEFAULT NULL ;

DROP procedure IF EXISTS `sp_getAllApplications`;
DELIMITER $$
CREATE PROCEDURE `sp_getAllApplications`(
    IN `User_id` VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci, IN `Status_id` VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci)
BEGIN
IF Status_id = 'ALL' THEN

(SELECT
    queryresp.id AS id,
    queryresp.QueryDefinition_id AS QueryDefinition_id,
    queryresp.Customer_id AS User_id,
    queryresp.CoBorrower_id AS CoBorrower_id,
    queryresp.createdts AS StartDate,
    queryresp.lastmodifiedts AS LastEditedDate,
    queryresp.LoanProduct_id AS LoanProduct_id,
    queryresp.Application_id AS Application_id,
    loanProd.LoanType_id AS LoanType_id,
    queryresp.Status_id AS Status,
    querycoborrower.CoBorrower_Type AS CoBorrower_Type,
    (SELECT
            CAST(CONCAT('[',
                            GROUP_CONCAT(JSON_OBJECT('QuerySectionQuestion_id',
                                        questResp.QuerySectionQuestion_id,
                                        'ResponseValue',
                                        questResp.ResponseValue)
                                SEPARATOR ','),
                            ']')
                    AS CHAR CHARSET UTF8MB4)
        FROM
            `questionresponse` questResp
        WHERE
            questResp.queryresponse_id = queryresp.id
                 AND `questResp`.`QuerySectionQuestion_id` IN ('VA_LOAN_AMOUNT' , 'VA_COBORROWER',
                'CCA_CARDLIMIT',
                'PA_LOANAMOUNT',
                'PA_COBORROWER')) AS `QuestionResponse`
FROM
    `queryresponse` queryresp
        JOIN
    loanproduct loanProd ON queryresp.LoanProduct_id = loanProd.id
        LEFT JOIN
    querycoborrower querycoborrower ON queryresp.id = querycoborrower.queryresponse_id
WHERE
    (`queryresp`.`Customer_id` = User_id COLLATE utf8_unicode_ci)
        AND `queryresp`.`QueryDefinition_id` IN ('PERSONAL_APPLICATION' , 'VEHICLE_APPLICATION',
        'CREDIT_CARD_APPLICATION') AND `queryresp`.softdeleteflag IS FALSE) UNION ALL (SELECT
    queryresp.id AS id,
    queryresp.QueryDefinition_id AS QueryDefinition_id,
    queryresp.Customer_id AS User_id,
    queryresp.CoBorrower_id AS CoBorrower_id,
    queryresp.createdts AS StartDate,
    queryresp.lastmodifiedts AS LastEditedDate,
    queryresp.LoanProduct_id AS LoanProduct_id,
    queryresp.Application_id AS Application_id,
    loanProd.LoanType_id AS LoanType_id,
    queryresp.Status_id AS STATUS,
    querycoborrower.CoBorrower_Type AS CoBorrower_Type,
    (SELECT
            CAST(CONCAT('[',
                            GROUP_CONCAT(JSON_OBJECT('QuerySectionQuestion_id',
                                        questResp.QuerySectionQuestion_id,
                                        'ResponseValue',
                                        questResp.ResponseValue)
                                SEPARATOR ','),
                            ']')
                    AS CHAR CHARSET UTF8MB4)
        FROM
            `questionresponse` questResp
        WHERE
            questResp.queryresponse_id = queryresp.id
                AND `questResp`.`QuerySectionQuestion_id` IN ('VA_LOAN_AMOUNT', 'VA_COBORROWER',
                'CCA_CARDLIMIT',
                'PA_LOANAMOUNT',
                'PA_COBORROWER')) AS `QuestionResponse`
FROM
    `queryresponse` queryresp
        JOIN
    loanproduct loanProd ON queryresp.LoanProduct_id = loanProd.id
        LEFT JOIN
    querycoborrower querycoborrower ON queryresp.id = querycoborrower.queryresponse_id
WHERE
    (`queryresp`.`CoBorrower_id` = User_id COLLATE utf8_unicode_ci)
        AND `queryresp`.`QueryDefinition_id` IN ('PERSONAL_APPLICATION' , 'VEHICLE_APPLICATION',
        'CREDIT_CARD_APPLICATION') AND `queryresp`.softdeleteflag IS FALSE) ORDER BY LastEditedDate DESC;
        ELSE
           (SELECT
    queryresp.id AS id,
    queryresp.QueryDefinition_id AS QueryDefinition_id,
    queryresp.Customer_id AS User_id,
    queryresp.CoBorrower_id AS CoBorrower_id,
    queryresp.createdts AS StartDate,
    queryresp.lastmodifiedts AS LastEditedDate,
    queryresp.LoanProduct_id AS LoanProduct_id,
    queryresp.Application_id AS Application_id,
    loanProd.LoanType_id AS LoanType_id,
    queryresp.Status_id AS Status,
    querycoborrower.CoBorrower_Type AS CoBorrower_Type,
    (SELECT
            CAST(CONCAT('[',
                            GROUP_CONCAT(JSON_OBJECT('QuerySectionQuestion_id',
                                        questResp.QuerySectionQuestion_id,
                                        'ResponseValue',
                                        questResp.ResponseValue)
                                SEPARATOR ','),
                            ']')
                    AS CHAR CHARSET UTF8MB4)
        FROM
            `questionresponse` questResp
        WHERE
            questResp.queryresponse_id = queryresp.id
                 AND `questResp`.`QuerySectionQuestion_id` IN ('VA_LOAN_AMOUNT' , 'VA_COBORROWER',
                'CCA_CARDLIMIT',
                'PA_LOANAMOUNT',
                'PA_COBORROWER')) AS `QuestionResponse`
FROM
    `queryresponse` queryresp
        JOIN
    loanproduct loanProd ON queryresp.LoanProduct_id = loanProd.id
        LEFT JOIN
    querycoborrower querycoborrower ON queryresp.id = querycoborrower.queryresponse_id
WHERE
    (`queryresp`.`Customer_id` = User_id COLLATE utf8_unicode_ci)
    AND `queryresp`.`Status_id` = Status_id
        AND `queryresp`.`QueryDefinition_id` IN ('PERSONAL_APPLICATION' , 'VEHICLE_APPLICATION',
        'CREDIT_CARD_APPLICATION') AND `queryresp`.softdeleteflag IS FALSE) UNION ALL (SELECT
    queryresp.id AS id,
    queryresp.QueryDefinition_id AS QueryDefinition_id,
    queryresp.Customer_id AS User_id,
    queryresp.CoBorrower_id AS CoBorrower_id,
    queryresp.createdts AS StartDate,
    queryresp.lastmodifiedts AS LastEditedDate,
    queryresp.LoanProduct_id AS LoanProduct_id,
    queryresp.Application_id AS Application_id,
    loanProd.LoanType_id AS LoanType_id,
    queryresp.Status_id AS STATUS,
    querycoborrower.CoBorrower_Type AS CoBorrower_Type,
    (SELECT
            CAST(CONCAT('[',
                            GROUP_CONCAT(JSON_OBJECT('QuerySectionQuestion_id',
                                        questResp.QuerySectionQuestion_id,
                                        'ResponseValue',
                                        questResp.ResponseValue)
                                SEPARATOR ','),
                            ']')
                    AS CHAR CHARSET UTF8MB4)
        FROM
            `questionresponse` questResp
        WHERE
            questResp.queryresponse_id = queryresp.id
                AND `questResp`.`QuerySectionQuestion_id` IN ('VA_LOAN_AMOUNT', 'VA_COBORROWER',
                'CCA_CARDLIMIT',
                'PA_LOANAMOUNT',
                'PA_COBORROWER')) AS `QuestionResponse`
FROM
    `queryresponse` queryresp
        JOIN
    loanproduct loanProd ON queryresp.LoanProduct_id = loanProd.id
        LEFT JOIN
    querycoborrower querycoborrower ON queryresp.id = querycoborrower.queryresponse_id
WHERE
    (`queryresp`.`CoBorrower_id` = User_id COLLATE utf8_unicode_ci)
    AND `queryresp`.`Status_id` = Status_id
        AND `queryresp`.`QueryDefinition_id` IN ('PERSONAL_APPLICATION' , 'VEHICLE_APPLICATION',
        'CREDIT_CARD_APPLICATION') AND `queryresp`.softdeleteflag IS FALSE) ORDER BY LastEditedDate DESC;
        END IF;
END$$
DELIMITER ;

ALTER TABLE realestatesresponse
DROP PropertyAddessLine1;

ALTER TABLE realestatesresponse
DROP PropertyAddessLine2;

ALTER TABLE realestatesresponse
ADD PropertyAddressLine1 varchar(100);

ALTER TABLE realestatesresponse
ADD PropertyAddressLine2 varchar(100);

ALTER TABLE `losapplications` 
ADD COLUMN `createdby` VARCHAR(50) NULL AFTER `QueryResponse_id`,
ADD COLUMN `modifiedby` VARCHAR(50) NULL AFTER `createdby`,
ADD COLUMN `createdts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `modifiedby`,
ADD COLUMN `lastupdatedts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `createdts`,
ADD COLUMN `synctimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `lastupdatedts`,
ADD COLUMN `softdeleteflag` TINYINT(1) UNSIGNED ZEROFILL NULL DEFAULT '0' AFTER `synctimestamp`;

ALTER TABLE `sectionstatus` 
ADD COLUMN `createdby` VARCHAR(50) NULL DEFAULT NULL AFTER `ReviewAndSubmit`,
ADD COLUMN `modifiedby` VARCHAR(50) NULL DEFAULT NULL AFTER `createdby`,
ADD COLUMN `createdts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP AFTER `modifiedby`,
ADD COLUMN `lastmodifiedts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `createdts`,
ADD COLUMN `softdeleteflag` TINYINT(4) NULL DEFAULT '0' AFTER `lastmodifiedts`;


--
-- Table structure for loanselectorresponse
--

DROP TABLE IF EXISTS `loanselectorresponse`;

CREATE TABLE `loanselectorresponse` (
	`id` VARCHAR(50) NOT NULL,
	`QueryResponse_id` VARCHAR(50) NULL,
	`Borrower_id` VARCHAR(50) NULL COLLATE 'utf8_general_ci',
	`Product_id` VARCHAR(50) NULL COLLATE 'utf8_general_ci',
	`ProductName` VARCHAR(50) NULL COLLATE 'utf8_general_ci',
	`ProductRate` VARCHAR(50) NULL COLLATE 'utf8_general_ci',
	`ProductAPR` VARCHAR(50) NULL COLLATE 'utf8_general_ci',
	`MonthlyPayment` VARCHAR(50) NULL COLLATE 'utf8_general_ci',
	`SelectProductLater` TINYINT(1) NULL DEFAULT '0',
	`createdby` VARCHAR(50) NULL COLLATE 'utf8_general_ci',
	`modifiedby` VARCHAR(50) NULL COLLATE 'utf8_general_ci',
	`createdts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
	`lastmodifiedts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`softdeleteflag` TINYINT(1) NULL DEFAULT '0'
)
COLLATE='utf8_general_ci';

DROP procedure IF EXISTS `sp_getAllApplications`;

DELIMITER $$
CREATE PROCEDURE `sp_getAllApplications`(
    IN `User_id` VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci, IN `Status_id` VARCHAR(50) CHARACTER SET UTF8 COLLATE utf8_general_ci)
BEGIN
IF Status_id = 'PENDING' THEN

(SELECT
    queryresp.id AS id,
    queryresp.QueryDefinition_id AS QueryDefinition_id,
    queryresp.Customer_id AS User_id,
    queryresp.CoBorrower_id AS CoBorrower_id,
    queryresp.createdts AS StartDate,
    queryresp.lastmodifiedts AS LastEditedDate,
    queryresp.LoanProduct_id AS LoanProduct_id,
    queryresp.Application_id AS Application_id,
    loanProd.LoanType_id AS LoanType_id,
    queryresp.Status_id AS Status,
    querycoborrower.CoBorrower_Type AS CoBorrower_Type,
    (SELECT
            CAST(CONCAT('[',
                            GROUP_CONCAT(JSON_OBJECT('QuerySectionQuestion_id',
                                        questResp.QuerySectionQuestion_id,
                                        'ResponseValue',
                                        questResp.ResponseValue)
                                SEPARATOR ','),
                            ']')
                    AS CHAR CHARSET UTF8MB4)
        FROM
            `questionresponse` questResp
        WHERE
            questResp.queryresponse_id = queryresp.id
                 AND `questResp`.`QuerySectionQuestion_id` IN ('VA_LOAN_AMOUNT' , 'VA_COBORROWER',
                'CCA_CARDLIMIT',
                'PA_LOANAMOUNT',
                'PA_COBORROWER')) AS `QuestionResponse`
FROM
    `queryresponse` queryresp
        JOIN
    loanproduct loanProd ON queryresp.LoanProduct_id = loanProd.id
        LEFT JOIN
    querycoborrower querycoborrower ON queryresp.id = querycoborrower.queryresponse_id
WHERE
    (`queryresp`.`Customer_id` = User_id COLLATE utf8_unicode_ci)
        AND `queryresp`.`QueryDefinition_id` IN ('PERSONAL_APPLICATION' , 'VEHICLE_APPLICATION',
        'CREDIT_CARD_APPLICATION') AND `queryresp`.softdeleteflag IS FALSE) UNION ALL (SELECT
    queryresp.id AS id,
    queryresp.QueryDefinition_id AS QueryDefinition_id,
    queryresp.Customer_id AS User_id,
    queryresp.CoBorrower_id AS CoBorrower_id,
    queryresp.createdts AS StartDate,
    queryresp.lastmodifiedts AS LastEditedDate,
    queryresp.LoanProduct_id AS LoanProduct_id,
    queryresp.Application_id AS Application_id,
    loanProd.LoanType_id AS LoanType_id,
    queryresp.Status_id AS STATUS,
    querycoborrower.CoBorrower_Type AS CoBorrower_Type,
    (SELECT
            CAST(CONCAT('[',
                            GROUP_CONCAT(JSON_OBJECT('QuerySectionQuestion_id',
                                        questResp.QuerySectionQuestion_id,
                                        'ResponseValue',
                                        questResp.ResponseValue)
                                SEPARATOR ','),
                            ']')
                    AS CHAR CHARSET UTF8MB4)
        FROM
            `questionresponse` questResp
        WHERE
            questResp.queryresponse_id = queryresp.id
                AND `questResp`.`QuerySectionQuestion_id` IN ('VA_LOAN_AMOUNT', 'VA_COBORROWER',
                'CCA_CARDLIMIT',
                'PA_LOANAMOUNT',
                'PA_COBORROWER')) AS `QuestionResponse`
FROM
    `queryresponse` queryresp
        JOIN
    loanproduct loanProd ON queryresp.LoanProduct_id = loanProd.id
        LEFT JOIN
    querycoborrower querycoborrower ON queryresp.id = querycoborrower.queryresponse_id
WHERE
    (`queryresp`.`CoBorrower_id` = User_id COLLATE utf8_unicode_ci)
        AND `queryresp`.`QueryDefinition_id` IN ('PERSONAL_APPLICATION' , 'VEHICLE_APPLICATION',
        'CREDIT_CARD_APPLICATION') AND `queryresp`.softdeleteflag IS FALSE) ORDER BY LastEditedDate DESC;
ELSE
           (SELECT
    queryresp.id AS id,
    queryresp.QueryDefinition_id AS QueryDefinition_id,
    queryresp.Customer_id AS User_id,
    queryresp.CoBorrower_id AS CoBorrower_id,
    queryresp.createdts AS StartDate,
    losapps.lastupdatedts AS LastEditedDate,
    queryresp.LoanProduct_id AS LoanProduct_id,
    queryresp.Application_id AS Application_id,
    loanProd.LoanType_id AS LoanType_id,
    queryresp.Status_id AS Status,
    querycoborrower.CoBorrower_Type AS CoBorrower_Type,
    (SELECT
            CAST(CONCAT('[',
                            GROUP_CONCAT(JSON_OBJECT('QuerySectionQuestion_id',
                                        questResp.QuerySectionQuestion_id,
                                        'ResponseValue',
                                        questResp.ResponseValue)
                                SEPARATOR ','),
                            ']')
                    AS CHAR CHARSET UTF8MB4)
        FROM
            `questionresponse` questResp
        WHERE
            questResp.queryresponse_id = queryresp.id
                 AND `questResp`.`QuerySectionQuestion_id` IN ('VA_LOAN_AMOUNT' , 'VA_COBORROWER',
                'CCA_CARDLIMIT',
                'PA_LOANAMOUNT',
                'PA_COBORROWER')) AS `QuestionResponse`
FROM
    `queryresponse` queryresp
        JOIN
    loanproduct loanProd ON queryresp.LoanProduct_id = loanProd.id
        LEFT JOIN
    querycoborrower querycoborrower ON queryresp.id = querycoborrower.queryresponse_id
		LEFT JOIN
	losapplications losapps ON queryresp.id = losapps.queryresponse_id
WHERE
    (`queryresp`.`Customer_id` = User_id COLLATE utf8_unicode_ci)
    AND `queryresp`.`Status_id` = Status_id
        AND `queryresp`.`QueryDefinition_id` IN ('PERSONAL_APPLICATION' , 'VEHICLE_APPLICATION',
        'CREDIT_CARD_APPLICATION') AND `queryresp`.softdeleteflag IS FALSE) UNION ALL (SELECT
    queryresp.id AS id,
    queryresp.QueryDefinition_id AS QueryDefinition_id,
    queryresp.Customer_id AS User_id,
    queryresp.CoBorrower_id AS CoBorrower_id,
    queryresp.createdts AS StartDate,
    losapps.lastupdatedts AS LastEditedDate,
    queryresp.LoanProduct_id AS LoanProduct_id,
    queryresp.Application_id AS Application_id,
    loanProd.LoanType_id AS LoanType_id,
    queryresp.Status_id AS STATUS,
    querycoborrower.CoBorrower_Type AS CoBorrower_Type,
    (SELECT
            CAST(CONCAT('[',
                            GROUP_CONCAT(JSON_OBJECT('QuerySectionQuestion_id',
                                        questResp.QuerySectionQuestion_id,
                                        'ResponseValue',
                                        questResp.ResponseValue)
                                SEPARATOR ','),
                            ']')
                    AS CHAR CHARSET UTF8MB4)
        FROM
            `questionresponse` questResp
        WHERE
            questResp.queryresponse_id = queryresp.id
                AND `questResp`.`QuerySectionQuestion_id` IN ('VA_LOAN_AMOUNT', 'VA_COBORROWER',
                'CCA_CARDLIMIT',
                'PA_LOANAMOUNT',
                'PA_COBORROWER')) AS `QuestionResponse`
FROM
    `queryresponse` queryresp
        JOIN
    loanproduct loanProd ON queryresp.LoanProduct_id = loanProd.id
        LEFT JOIN
    querycoborrower querycoborrower ON queryresp.id = querycoborrower.queryresponse_id
		LEFT JOIN
	losapplications losapps ON queryresp.id = losapps.queryresponse_id
WHERE
    (`queryresp`.`CoBorrower_id` = User_id COLLATE utf8_unicode_ci)
    AND `queryresp`.`Status_id` = Status_id
        AND `queryresp`.`QueryDefinition_id` IN ('PERSONAL_APPLICATION' , 'VEHICLE_APPLICATION',
        'CREDIT_CARD_APPLICATION') AND `queryresp`.softdeleteflag IS FALSE) ORDER BY LastEditedDate DESC;
        END IF;
END$$        
DELIMITER ;

ALTER TABLE `additionalmortgageresponse` 
CHANGE COLUMN `softdeleteflag` `softdeleteflag` TINYINT(1) NULL DEFAULT '0' ,
ADD INDEX `FK_AdditionalMortgageResponse_Borrower_idx` (`Borrower_id` ASC);

ALTER TABLE `additionalmortgageresponse` 
ADD CONSTRAINT `FK_AdditionalMortgageResponse_Borrower`
  FOREIGN KEY (`Borrower_id`)
  REFERENCES `borrower` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
  
ALTER TABLE `additionalmortgageheaderresponse` 
CHANGE COLUMN `softdeleteflag` `softdeleteflag` TINYINT(1) NULL DEFAULT '0' ,
ADD UNIQUE INDEX `Borrower_id_UNIQUE` (`Borrower_id` ASC);

ALTER TABLE `additionalmortgageheaderresponse` 
ADD CONSTRAINT `FK_AdditionalMortgageHeaderResponse_Borrower`
  FOREIGN KEY (`Borrower_id`)
  REFERENCES `borrower` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE `realestateagentresponse` 
CHANGE COLUMN `softdeleteflag` `softdeleteflag` TINYINT(1) NULL DEFAULT '0' ,
ADD UNIQUE INDEX `Borrower_id_UNIQUE` (`Borrower_id` ASC);

ALTER TABLE `propertyinforesponse` 
CHANGE COLUMN `softdeleteflag` `softdeleteflag` TINYINT(1) NULL DEFAULT '0' ,
ADD UNIQUE INDEX `Borrower_id_UNIQUE` (`Borrower_id` ASC);

ALTER TABLE `loaninforesponse` 
CHANGE COLUMN `softdeleteflag` `softdeleteflag` TINYINT(1) NULL DEFAULT '0' ,
ADD UNIQUE INDEX `Borrower_id_UNIQUE` (`Borrower_id` ASC);

ALTER TABLE `loanofficersresponse` 
CHANGE COLUMN `softdeleteflag` `softdeleteflag` TINYINT(1) NULL DEFAULT '0' ,
ADD UNIQUE INDEX `Borrower_id_UNIQUE` (`Borrower_id` ASC);

ALTER TABLE `demographicsresponse` 
CHANGE COLUMN `softdeleteflag` `softdeleteflag` TINYINT(1) NULL DEFAULT '0' ;
