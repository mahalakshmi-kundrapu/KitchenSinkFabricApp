UPDATE `dashboardalerts` SET `Description`='The data center near NY was down for 10 mins on 16th May, 2018 which led to some transaction failures from the online and mobile banking applications.' WHERE `id`='DAID3';
UPDATE `dashboardalerts` SET `Description`='The data center near NY was down for 10 mins on 16th May, 2018 which led to some transaction failures from the online and mobile banking applications.' WHERE `id`='DAID2';

UPDATE `requestcategory` SET `Name`='Credit Cards' WHERE `id`='RCID_CREDITCARD';
UPDATE `requestcategory` SET `Name`='Wrong Debit/Credit charges' WHERE `id`='RCID_INCORRECTDEBITORCREDITCHARGES';

UPDATE `producttype` SET `Name`='Turbo Auto Loan' WHERE `id`='PRODUCT_TYPE5';

UPDATE `alerttype` SET `Description`='Mandatory alerts that are independent of customer alert subscriptions. Customer can’t switch off these alerts for security reasons' WHERE `id`='ALERT_TYPE_ID_1_SECURITY';
UPDATE `alerttype` SET `Description`='Alerts generated for deals and promotions based on customer alert subscription' WHERE `id`='ALERT_TYPE_ID_3_PROMOTIONAL';
UPDATE `alerttype` SET `Description`='Alerts generated for transaction related activities based on customer alert subscription' WHERE `id`='ALERT_TYPE_ID_2_TP';
UPDATE `alerttype` SET `Description`='Alerts generated for selected accounts based on customer alert subscription' WHERE `id`='ALERT_TYPE_ID_4_ACCOUNT';

ALTER TABLE `systemuser` 
DROP COLUMN `PasswordSalt`, 
DROP INDEX `IXFK_SystemUser_PasswordSalt`;

UPDATE `dashboardalerts` SET `Title` = 'Scheduled maintenance' where `Title` = 'Scheduled Maintenance';

UPDATE `privacypolicy` SET `Description`=' <p style=\"text-align:justify\"><i>At Kony Bank, the safeguarding of your account information is our top priority.  <span style=\"font-family:&#39;calibri&#39;;font-size:medium\">We strive to keep our members informed on current security issues, as well as providing the tools to help protect yourself.  Please check back periodically to view new information as it becomes available.</span></i></p><p><b><u>Your Account Security</u></b></p><p>We protect your online <b>security</b>.  Keeping financial and personal information about you secure and confidential is one of our most important responsibilities.  Our systems are protected, so information remains secure.</p><p>1. Computer virus protection detects and prevents computer viruses from entering our computer network systems.</p><p>2. Firewalls block unauthorized access by individuals or networks. Firewalls are just one way we protect our computer network systems that interact with the Internet.</p><p>3. Secure transmissions ensure information remains confidential. Kony Bank uses encryption technology such as Secure Socket Layer (SSL) on its Web sites to securely transmit information between you and the bank.</p><p>4. Send secure e-mail to almost any department in the bank through the Contact Us section.  Because an Internet e-mail response back to you may not be secure, we will not include confidential account information in an e-mail response.  In addition, you will never be asked for confidential information, such as passwords or PINs, through e-mail.  Besides e-mail, you can contact us by phone or visiting any branch.</p><p>5. Regular evaluations of our security features and continuous research of new advances in security technology ensure that your personal information is protected.</p>' WHERE `id`='PRIV_POL_ID1';

UPDATE `policies` SET `PolicyDescription`='<ul><li>Minimum Length- 8, Maximum Length- 24</li> <li>Can be alpha-numeric (a-z, A-Z, 0-9)</li> <li>No spaces or special characters allowed except period \'.\'</li> <li>Should Start with an alphabet or digit.</li> <li>Username is not case sensitive.</li> <li>Period \'.\' cannot be used more than once.</li></ul>' WHERE `id`='POLID1';
UPDATE `policies` SET `PolicyDescription`='<ul><li>Minimum Length- 8, Maximum Length- 24</li> <li>Should be a combination of alpha-numeric (a-z, A-Z, 0-9), special character is not mandatory</li> <li>Invalid Characters- &, %, <, >, +, ‘, \\, =, Pipe, Space</li> <li>Both fields should have the same value.</li> <li>It should not be 9 consecutive digits</li> <li>All characters should not be same. Ex: values like 11111111 should not be allowed.</li> <li>Values are case sensitive.</li></ul>' WHERE `id`='POLID2';

ALTER TABLE `archivedrequestmessage` ADD COLUMN `RepliedBy_id` VARCHAR(50) NULL AFTER `RepliedBy`;
update archivedrequestmessage set RepliedBy_id = (select id from systemuser 
	where concat(systemuser.FirstName,' ', ifnull(systemuser.MiddleName,''), ' ', systemuser.LastName) 
    = archivedrequestmessage.RepliedBy_Name limit 1) 
    where RepliedBy = 'ADMIN|CSR';
    
UPDATE `product` SET `ProductFeatures`='<p>As a cardholder, you will enjoy these features</p><ul><li>0% Introductory APR1 on all Purchases for six months</li><li>0% Introductory APR2 for twelve months on Balance Transfers</li><li>9.50% - 17.50% APR<sup>*</sup></li><li>Up to 2% rebate on the first $500,000 net spentA revolving “base” line of credit, plus a non-revolving “pay in full” line of credit valued at up to two times the base amount. Now that is a lot of buying power!</li></ul><p>Qualified purchases benefit from the security offered through Extended Warranty coverage that doubles the warranty time period and duplicates the coverage for up to one year.</p><p>Pay for the entire cost of a product and if within 60 days of the date of purchase you see either a printed ad or non-auction Internet ad for the same product (same model and model year) at a lower price, MasterCard<sup>® </sup>refunds the difference.Personal Identity Solution Services provides cardholders with access to a number of Identity Theft resolutions services.</p>' WHERE `id`='PRODUCT7';

UPDATE `service` SET `Description`='Fund Transfer between different eligible accounts of a member', `DisplayName`='KonyBankAccountsTransfer' WHERE `id`='SERVICE_ID_2';
UPDATE `service` SET `DisplayName`='OtherKonyAccountsTransfer' WHERE `id`='SERVICE_ID_1';
UPDATE `service` SET `Description`='Fund Transfer to other members of the Credit Union. Member to member Transfer' WHERE `id`='SERVICE_ID_1';