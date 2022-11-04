
DROP PROCEDURE IF EXISTS `sp_getAllApplications`;
DELIMITER $$
CREATE PROCEDURE `sp_getAllApplications`(IN `User_id` varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci)
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
        'CREDIT_CARD_APPLICATION') AND `queryresp`.softdeleteflag IS FALSE) UNION ALL (SELECT
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
        'CREDIT_CARD_APPLICATION') AND `queryresp`.softdeleteflag IS FALSE) ORDER BY LastEditedDate DESC;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `sp_getCustomerPrequalifyPackage`;
DELIMITER $$
CREATE PROCEDURE `sp_getCustomerPrequalifyPackage`(IN Customer_id varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci)
BEGIN

SELECT 
cpp.id as id, 
cpp.Customer_id as Customer_id,
cpp.PrequalifyPackage_id as PrequalifyPackage_id,
cpp.createdby as createdby,
cpp.modifiedby as modifiedby,
cpp.createdts as createdts,
cpp.lastmodifiedts as lastmodifiedts,
cpp.synctimestamp as synctimestamp,
cpp.softdeleteflag as softdeleteflag,
(select cast(concat('[',group_concat(json_object('id',pp.id ,'LoanType_id',pp.LoanType_id,'LoanProduct_id',pp.LoanProduct_id,'Name',pp.Name,'Code',pp.Code,'Description',pp.Description,'LoanAmount',pp.LoanAmount,'LoanTerms',pp.LoanTerms, 'APR',pp.APR,'Rate',pp.rate,'MonthlyPayment',pp.MonthlyPayment,'AnnualFee',pp.AnnualFee,'BenfitsandRewards',pp.BenfitsandRewards,'TransferInformation',pp.TransferInformation,'PrequalifyCondition',pp.PrequalifyCondition,'createdby',pp.createdby,'modifiedby',pp.modifiedby,'createdts',pp.createdts,'lastmodifiedts',pp.lastmodifiedts,'synctimestamp',pp.synctimestamp,'softdeleteflag',pp.softdeleteflag,
'LoanProduct', cast((select concat('[',group_concat(json_object('id',lp.id,'LoanType_id',lp.LoanType_id,'MinLimitAmount',lp.MinLimitAmount,'MaxLimitAmount',lp.MaxLimitAmount,'Name',lp.Name,'Description',lp.Description,'Code',lp.Code,'AtAGlance',lp.AtAGlance,'APR',lp.APR,'AnnualFee',lp.AnnualFee,'Rewards',lp.Rewards,'createdby',lp.createdby,'modifiedby',lp.modifiedby,'createdts',lp.createdts,'lastmodifiedts',lp.lastmodifiedts,'synctimestamp',lp.synctimestamp,'softdeleteflag',lp.softdeleteflag) separator ','),']')from loanproduct lp where lp.id = pp.LoanProduct_id) as json))separator ','),']') as char charset utf8mb4)from `prequalifypackage` pp where pp.id =cpp.PrequalifyPackage_id) AS `PrequalifyPackage`
FROM customerprequalifypackage cpp where cpp.Customer_id = Customer_id;

END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `sp_getDecisionFailureData`;
DELIMITER $$
CREATE PROCEDURE `sp_getDecisionFailureData`(IN id_list varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci, IN job_id varchar(50) CHARACTER SET UTF8 COLLATE utf8_general_ci, IN top INT, IN skip INT)
BEGIN
IF id_list IS NOT NULL THEN 
SELECT df.id,df.job_id,df.decision_id,df.failureTriggerJob_id,df.exception,cdd.Customer_id,cdd.CreditScore,cdd.EmploymentType,cdd.AnnualIncome,cdd.AccountBalance,cdd.Age,cdd.PrequalifyScore FROM decisionfailure df join customerdpdata cdd on baseAttributeValue=Customer_id where find_in_set(df.id , id_list) order by df.createdts,df.id LIMIT top OFFSET skip;
ELSEIF job_id IS NOT NULL THEN 
SELECT df.id,df.job_id,df.decision_id,cdd.Customer_id,df.exception,cdd.CreditScore,cdd.EmploymentType,cdd.AnnualIncome,cdd.AccountBalance,cdd.Age,cdd.PrequalifyScore FROM decisionfailure df join customerdpdata cdd on baseAttributeValue=Customer_id where df.job_id=job_id order by df.createdts,df.id LIMIT top OFFSET skip;
ELSE
SELECT df.id,df.job_id,df.decision_id,df.failureTriggerJob_id,df.exception,cdd.Customer_id,cdd.CreditScore,cdd.EmploymentType,cdd.AnnualIncome,cdd.AccountBalance,cdd.Age,cdd.PrequalifyScore FROM decisionfailure df join customerdpdata cdd on baseAttributeValue=Customer_id where failureTriggerJob_id is null order by df.createdts,df.id LIMIT top OFFSET skip;
END IF;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `sp_getLoanAnswers`;
DELIMITER $$
CREATE PROCEDURE `sp_getLoanAnswers`(IN QueryResponseID varchar(100) CHARACTER SET UTF8 COLLATE utf8_general_ci)
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

DROP PROCEDURE IF EXISTS `sp_getQueryResponseApplicationList`;
DELIMITER $$
CREATE PROCEDURE `sp_getQueryResponseApplicationList`(IN id varchar(20000) CHARACTER SET UTF8 COLLATE utf8_general_ci,IN Status_id varchar(20000) CHARACTER SET UTF8 COLLATE utf8_general_ci)
BEGIN
SELECT 
    queryresp.id AS id,
    queryresp.QueryDefinition_id AS QueryDefinition_id,
    queryresp.OverallPercentageCompletion AS progress,
    queryresp.Status_id AS Status_id,
    queryresp.createdts AS createdTS,
    queryresp.lastmodifiedts AS modifiedTS,
    loantype.description AS loantype,
    queryresp.CoBorrower_id as CoBorrower_id, 
    (SELECT 
            CAST(CONCAT('[',
                            GROUP_CONCAT(JSON_OBJECT('id',
                                        questResp.id,
                                        'QuestionDefinition_id',
                                        questResp.QuestionDefinition_id,
                                        'QueryResponse_id',
                                        questResp.QueryResponse_id,
                                        'ResponseValue',
                                        questResp.ResponseValue,
                                        'Unit',
                                        questResp.Unit,
                                        'QuerySectionQuestion_id',
                                        questResp.QuerySectionQuestion_id)
                                SEPARATOR ','),
                            ']')
                    AS CHAR CHARSET UTF8MB4)
        FROM
            `questionresponse` questResp
        WHERE
            questResp.QueryResponse_id = queryresp.id) AS `QuestionResponse`
FROM
    `queryresponse` queryresp
        LEFT JOIN
    loanproduct loanproduct ON loanproduct.id = queryresp.LoanProduct_id
        LEFT JOIN
    loantype loantype ON loantype.id = loanproduct.LoanType_id
WHERE
    (queryresp.Customer_id = id or queryresp.CoBorrower_id= id)
        AND queryresp.Status_id = Status_id
        AND (queryresp.QueryDefinition_id = 'PERSONAL_APPLICATION'
        OR queryresp.QueryDefinition_id = 'CREDIT_CARD_APPLICATION'
        OR queryresp.QueryDefinition_id = 'VEHICLE_APPLICATION')
ORDER BY queryresp.lastmodifiedts DESC;
END$$
DELIMITER ;



DROP PROCEDURE IF EXISTS `sp_getQuestionOptions`;
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
