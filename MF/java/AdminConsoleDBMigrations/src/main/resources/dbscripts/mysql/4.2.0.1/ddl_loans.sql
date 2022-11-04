DROP procedure IF EXISTS `sp_deleteCoApplicantData`;
DELIMITER $$
CREATE PROCEDURE `sp_deleteCoApplicantData`(IN sectionsToDelete varchar(200), IN applicationID varchar(100))
BEGIN
SET @FirstPart = "update questionresponse set softdeleteflag =1 where QuerySectionQuestion_id in (select id from querysectionquestion where QuerySection_id IN (";
SET @Query = CONCAT(@FirstPart, sectionsToDelete, ")) AND questionresponse.QueryResponse_id = \'" ,applicationID, "\';");
PREPARE queryStatement FROM @Query;
EXECUTE queryStatement;
DEALLOCATE PREPARE queryStatement;
END$$
DELIMITER ;

ALTER TABLE `questionresponse` DROP COLUMN `customsoftdeleteflag`;
ALTER TABLE `queryresponse` ADD INDEX `Created_Date_idx` (`createdts` ASC);
ALTER TABLE `queryresponse` ADD INDEX `Customer_id_idx` (`Customer_id` ASC);
ALTER TABLE `querycoborrower` ADD INDEX `QueryResponse_id_idx` (`QueryResponse_id` ASC);
ALTER TABLE `queryresponse` ADD INDEX `CoBorrower_id_idx` (`CoBorrower_id` ASC);
ALTER TABLE `questionresponse` ADD INDEX `QueryResponse_id_idx` (`QueryResponse_id` ASC);

DROP procedure IF EXISTS `sp_getAllApplications`;
DELIMITER $$
CREATE PROCEDURE `sp_getAllApplications`(IN `User_id` varchar(50))
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
                'PA_COBORROWER')) AS `QuestionResponse`
FROM
    `queryresponse` queryresp
        JOIN
    loanproduct loanProd ON queryresp.LoanProduct_id = loanProd.id
        LEFT JOIN
    querycoborrower querycoborrower ON queryresp.id = querycoborrower.queryresponse_id
WHERE
    (`queryresp`.`Customer_id` = User_id)
        AND `queryresp`.`QueryDefinition_id` IN ('PERSONAL_APPLICATION' , 'VEHICLE_APPLICATION',
        'CREDIT_CARD_APPLICATION')) UNION ALL (SELECT
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
        'CREDIT_CARD_APPLICATION')) ORDER BY LastEditedDate DESC;
END$$
DELIMITER ;

ALTER TABLE `questionresponse` ADD CONSTRAINT `FK_QuestionResponse_QueryResponse` FOREIGN KEY (`QueryResponse_id`) REFERENCES `queryresponse` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

DROP procedure IF EXISTS `sp_getQuestionOptions`;
DELIMITER $$
CREATE PROCEDURE `sp_getQuestionOptions`(IN `QueryDefinitionID` varchar(50))
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
               CAST(CONCAT('[', GROUP_CONCAT(JSON_OBJECT( 'OptionItem_id', optionitem.id, 'OptionItem_DefaultValue', optionitem.DefaultValue ) separator ','), ']') AS JSON) 
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

DROP procedure IF EXISTS `sp_getLoanAnswers`;
DELIMITER $$
CREATE PROCEDURE `sp_getLoanAnswers`(IN QueryResponseID varchar(100))
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
      questionResp.QueryResponse_id = QueryResponseID 
      AND questionResp.softdeleteflag is not true;
END$$
DELIMITER ;
