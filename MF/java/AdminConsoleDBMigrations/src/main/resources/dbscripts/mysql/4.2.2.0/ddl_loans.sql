DROP PROCEDURE IF EXISTS `sp_update_tabledata`;
DELIMITER $$
CREATE PROCEDURE `sp_update_tabledata`(IN tablename varchar(255),IN primarykeyfield varchar(255),IN primarykeyvalue varchar(255),IN fieldname varchar(255),IN fieldvalue varchar(255))
BEGIN
Set @MainQuery = CONCAT("update ",tablename," set ",fieldname,"=",fieldvalue," where ",primarykeyfield,"=","'",primarykeyvalue,"'");
PREPARE queryStatement FROM @MainQuery;
EXECUTE queryStatement;
DEALLOCATE PREPARE queryStatement;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `sp_getAllApplications`;
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