UPDATE konyadminconsole.dashboardalerts SET [Description]='The data center near NY was down for 10 mins on 16th May, 2018 which led to some transaction failures from the online and mobile banking applications.' WHERE [id]='DAID3';
UPDATE konyadminconsole.dashboardalerts SET [Description]='The data center near NY was down for 10 mins on 16th May, 2018 which led to some transaction failures from the online and mobile banking applications.' WHERE [id]='DAID2';

UPDATE konyadminconsole.requestcategory SET [Name]='Credit Cards' WHERE [id]='RCID_CREDITCARD';
UPDATE konyadminconsole.requestcategory SET [Name]='Wrong Debit/Credit charges' WHERE [id]='RCID_INCORRECTDEBITORCREDITCHARGES';

UPDATE konyadminconsole.producttype SET [Name]='Turbo Auto Loan' WHERE [id]='PRODUCT_TYPE5';

UPDATE konyadminconsole.alerttype SET [Description]='Mandatory alerts that are independent of customer alert subscriptions. Customer can’t switch off these alerts for security reasons' WHERE [id]='ALERT_TYPE_ID_1_SECURITY';
UPDATE konyadminconsole.alerttype SET [Description]='Alerts generated for deals and promotions based on customer alert subscription' WHERE [id]='ALERT_TYPE_ID_3_PROMOTIONAL';
UPDATE konyadminconsole.alerttype SET [Description]='Alerts generated for transaction related activities based on customer alert subscription' WHERE [id]='ALERT_TYPE_ID_2_TP';
UPDATE konyadminconsole.alerttype SET [Description]='Alerts generated for selected accounts based on customer alert subscription' WHERE [id]='ALERT_TYPE_ID_4_ACCOUNT';

ALTER TABLE konyadminconsole.systemuser DROP COLUMN [PasswordSalt];

UPDATE konyadminconsole.dashboardalerts SET [Title]='Scheduled maintenance' where [Title]='Scheduled Maintenance';

UPDATE konyadminconsole.privacypolicy SET [Description]=' <p style="text-align:justify"><i>At Kony Bank, the safeguarding of your account information is our top priority.  <span style="font-family:&#39;calibri&#39;;font-size:medium">We strive to keep our members informed on current security issues, as well as providing the tools to help protect yourself.  Please check back periodically to view new information as it becomes available.</span></i></p><p><b><u>Your Account Security</u></b></p><p>We protect your online <b>security</b>.  Keeping financial and personal information about you secure and confidential is one of our most important responsibilities.  Our systems are protected, so information remains secure.</p><p>1. Computer virus protection detects and prevents computer viruses from entering our computer network systems.</p><p>2. Firewalls block unauthorized access by individuals or networks. Firewalls are just one way we protect our computer network systems that interact with the Internet.</p><p>3. Secure transmissions ensure information remains confidential. Kony Bank uses encryption technology such as Secure Socket Layer (SSL) on its Web sites to securely transmit information between you and the bank.</p><p>4. Send secure e-mail to almost any department in the bank through the Contact Us section.  Because an Internet e-mail response back to you may not be secure, we will not include confidential account information in an e-mail response.  In addition, you will never be asked for confidential information, such as passwords or PINs, through e-mail.  Besides e-mail, you can contact us by phone or visiting any branch.</p><p>5. Regular evaluations of our security features and continuous research of new advances in security technology ensure that your personal information is protected.</p>' WHERE [id]='PRIV_POL_ID1';

UPDATE konyadminconsole.policies SET [PolicyDescription]='<ul><li>Minimum Length- 8, Maximum Length- 24</li> <li>Can be alpha-numeric (a-z, A-Z, 0-9)</li> <li>No spaces or special characters allowed except period ''.''</li> <li>Should Start with an alphabet or digit.</li> <li>Username is not case sensitive.</li> <li>Period ''.'' cannot be used more than once.</li></ul>' WHERE [id]='POLID1';
UPDATE konyadminconsole.policies SET [PolicyDescription]='<ul><li>Minimum Length- 8, Maximum Length- 24</li> <li>Should be a combination of alpha-numeric (a-z, A-Z, 0-9), special character is not mandatory</li> <li>Invalid Characters- &, %, <, >, +, ‘, \, =, Pipe, Space</li> <li>Both fields should have the same value.</li> <li>It should not be 9 consecutive digits</li> <li>All characters should not be same. Ex: values like 11111111 should not be allowed.</li> <li>Values are case sensitive.</li></ul>' WHERE [id]='POLID2';

ALTER TABLE konyadminconsole.archivedrequestmessage ADD [RepliedBy_id] NVARCHAR(50) NULL;

update konyadminconsole.archivedrequestmessage set RepliedBy_Name = (select TOP 1 id from konyadminconsole.systemuser 
where concat(konyadminconsole.systemuser.FirstName,' ', case when konyadminconsole.systemuser.MiddleName is null then '' else null end, ' ', konyadminconsole.systemuser.LastName) 
= konyadminconsole.archivedrequestmessage.RepliedBy_Name) where RepliedBy = 'ADMIN|CSR';
