-- >> CAMPAIGN MANAGEMENT <<
INSERT INTO `service_permission_mapper` (`id`, `service_name`, `object_name`, `operation`, `user_type`, `permissions`) VALUES ('ecd48fc8-c18b-4809-af2c-dffc3e690c82', 'CampaignManagementObjService', 'campaign', 'getCampaignSettings', 'TYPE_ID_CUSTOMER360', 'ViewCampaign');
INSERT INTO `service_permission_mapper` (`id`, `service_name`, `object_name`, `operation`, `user_type`, `permissions`) VALUES ('5976759c-5acb-4ca1-9764-a814ece4fccc', 'CampaignManagementObjService', 'campaign', 'updateCampaignSettings', 'TYPE_ID_CUSTOMER360', 'CreateUpdateCampaign');
INSERT INTO `service_permission_mapper` (`id`, `service_name`, `object_name`, `operation`, `user_type`, `permissions`) VALUES ('941e8002-ce0c-4aa8-8927-bdce5fc27b72', 'CampaignManagementObjService', 'campaign', 'checkIfCampaignExists', 'TYPE_ID_CUSTOMER360', 'ViewCampaign');
-- >> CAMPAIGN MANAGEMENT <<

INSERT IGNORE INTO `dbxalertcategory` (`id`, `Name`, `accountLevel`, `status_id`, `DisplaySequence`, `modifiedby`) VALUES ('ALERT_CAT_ACCOUNTS', 'Account', '1', 'SID_ACTIVE', '3', 'Kony User');

INSERT IGNORE INTO `dbxalertcategorytext` (`AlertCategoryId`, `LanguageCode`, `DisplayName`, `Description`) VALUES ('ALERT_CAT_ACCOUNTS', 'en-US', 'Account', 'Account alerts are sent');
UPDATE IGNORE `dbxalertcategorytext` SET `Description` = 'Security alerts are sent when any sensitive data of a customer is accessed/updated. Certain high security alerts are sent as mandatory alerts like sign in attempt, username or password update. For others, the customers can choose to activate or deactivate them' WHERE (`AlertCategoryId` = 'ALERT_CAT_SECURITY') and (`LanguageCode` = 'en-US');

INSERT INTO `alertcategorychannel` (`ChannelID`, `AlertCategoryId`, `createdby`) VALUES ('CH_EMAIL', 'ALERT_CAT_ACCOUNTS', 'Kony User');
INSERT INTO `alertcategorychannel` (`ChannelID`, `AlertCategoryId`, `createdby`) VALUES ('CH_NOTIFICATION_CENTER', 'ALERT_CAT_ACCOUNTS', 'Kony User');
INSERT INTO `alertcategorychannel` (`ChannelID`, `AlertCategoryId`, `createdby`) VALUES ('CH_PUSH_NOTIFICATION', 'ALERT_CAT_ACCOUNTS', 'Kony User');
INSERT INTO `alertcategorychannel` (`ChannelID`, `AlertCategoryId`, `createdby`) VALUES ('CH_SMS', 'ALERT_CAT_ACCOUNTS', 'Kony User');

INSERT INTO `dbxalerttype` (`id`, `Name`, `AlertCategoryId`, `AttributeId`, `AlertConditionId`, `Value1`, `Status_id`, `IsGlobal`, `DisplaySequence`) VALUES ('BALANCE', 'Balance', 'ALERT_CAT_ACCOUNTS', 'MINIMUM_BALANCE_THRESHOLD', 'EQUALS_TO', '50', 'SID_ACTIVE', '0', '1');
INSERT INTO `dbxalerttype` (`id`, `Name`, `AlertCategoryId`, `Status_id`, `IsGlobal`, `DisplaySequence`) VALUES ('PAYMENT_REMINDER', 'Payment Reminders', 'ALERT_CAT_ACCOUNTS', 'SID_ACTIVE', '0', '2');
INSERT INTO `dbxalerttype` (`id`, `Name`, `AlertCategoryId`, `Status_id`, `IsGlobal`, `DisplaySequence`) VALUES ('CHECK', 'Check', 'ALERT_CAT_ACCOUNTS', 'SID_ACTIVE', '0', '3');
INSERT INTO `dbxalerttype` (`id`, `Name`, `AlertCategoryId`, `AttributeId`, `AlertConditionId`, `Value1`, `Status_id`, `IsGlobal`, `DisplaySequence`) VALUES ('DEPOSIT_REMINDER', 'Deposit Reminder', 'ALERT_CAT_ACCOUNTS',  'DAYS_BEFORE_TO_REMIND', 'EQUALS_TO', '5', 'SID_ACTIVE', '0', '4');

INSERT INTO `alertattribute` (`id`, `LanguageCode`, `name`, `createdby`, `modifiedby`) VALUES ('MINIMUM_BALANCE_THRESHOLD', 'en-US', 'Minimum balance threshold', 'Kony User', 'Kony User');
INSERT INTO `alertattribute` (`id`, `LanguageCode`, `name`, `createdby`, `modifiedby`) VALUES ('FREQUENCY_OF_BALANCE_UPDATE', 'en-US', 'Frequency of balance update', 'Kony User', 'Kony User');
INSERT INTO `alertattribute` (`id`, `LanguageCode`, `name`, `createdby`, `modifiedby`) VALUES ('DAYS_BEFORE_TO_REMIND', 'en-US', 'Days before to remind', 'Kony User', 'Kony User');


INSERT INTO `alerttypeapp` (`AppId`, `AlertTypeId`, `createdby`) VALUES ('RETAIL_BANKING', 'BALANCE', 'Kony User');
INSERT INTO `alerttypeapp` (`AppId`, `AlertTypeId`, `createdby`) VALUES ('RETAIL_BANKING', 'PAYMENT_REMINDER', 'Kony User');
INSERT INTO `alerttypeapp` (`AppId`, `AlertTypeId`, `createdby`) VALUES ('RETAIL_BANKING', 'CHECK', 'Kony User');
INSERT INTO `alerttypeapp` (`AppId`, `AlertTypeId`, `createdby`) VALUES ('RETAIL_BANKING', 'DEPOSIT_REMINDER', 'Kony User');

INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_MICRO_BUSINESS', 'BALANCE', 'Kony User');
INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_RETAIL', 'BALANCE', 'Kony User');
INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_SMALL_BUSINESS', 'BALANCE', 'Kony User');
INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_MICRO_BUSINESS', 'CHECK', 'Kony User');
INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_RETAIL', 'CHECK', 'Kony User');
INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_SMALL_BUSINESS', 'CHECK', 'Kony User');
INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_MICRO_BUSINESS', 'DEPOSIT_REMINDER', 'Kony User');
INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_RETAIL', 'DEPOSIT_REMINDER', 'Kony User');
INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_SMALL_BUSINESS', 'DEPOSIT_REMINDER', 'Kony User');
INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_MICRO_BUSINESS', 'PAYMENT_REMINDER', 'Kony User');
INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_RETAIL', 'PAYMENT_REMINDER', 'Kony User');
INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_SMALL_BUSINESS', 'PAYMENT_REMINDER', 'Kony User');


INSERT INTO `dbxalerttypetext` (`AlertTypeId`, `LanguageCode`, `DisplayName`, `Description`) VALUES ('BALANCE', 'en-US', 'Balance Update', 'An alert is sent to the user incase of deposit / withdrawal from the account or if the balance is below the minimum balance amount');
INSERT INTO `dbxalerttypetext` (`AlertTypeId`, `LanguageCode`, `DisplayName`, `Description`) VALUES ('CHECK', 'en-US', 'Check Status', 'An alert is sent to the user if a check has been posted to the account or has been rejected');
INSERT INTO `dbxalerttypetext` (`AlertTypeId`, `LanguageCode`, `DisplayName`, `Description`) VALUES ('PAYMENT_REMINDER', 'en-US', 'Payment', 'An alert is sent to the user for overdue payments/ payment due reminder.');
INSERT INTO `dbxalerttypetext` (`AlertTypeId`, `LanguageCode`, `DisplayName`, `Description`) VALUES ('DEPOSIT_REMINDER', 'en-US', 'Deposit Reminder', 'Notify the users ____ days prior to the maturity date of my time deposit.');

INSERT INTO `alertsubtype` (`id`, `AlertTypeId`, `Name`, `Description`, `Status_id`) VALUES ('MINIMUM_BALANCE', 'BALANCE', 'Minimum Balance', 'Alert when account''s balance is below the defined value.', 'SID_ACTIVE');
INSERT INTO `alertsubtype` (`id`, `AlertTypeId`, `Name`, `Description`, `Status_id`) VALUES ('OVERDRAFT_ALERT', 'BALANCE', 'Overdraft Alerts', 'Alert when the account is overdrawn.', 'SID_ACTIVE');
INSERT INTO `alertsubtype` (`id`, `AlertTypeId`, `Name`, `Description`, `Status_id`) VALUES ('DEPOSITS', 'BALANCE', 'Deposits', 'When an amount is credited into the account.', 'SID_ACTIVE');
INSERT INTO `alertsubtype` (`id`, `AlertTypeId`, `Name`, `Description`, `Status_id`) VALUES ('WITHDRAWAL', 'BALANCE', 'Withdrawal', 'When an amount is debited into the account.', 'SID_ACTIVE');
INSERT INTO `alertsubtype` (`id`, `AlertTypeId`, `Name`, `Description`, `Status_id`) VALUES ('BALANCE_UPDATE', 'BALANCE', 'Balance Update', 'Notify me of my account balance every day/week/month/year', 'SID_ACTIVE');
INSERT INTO `alertsubtype` (`id`, `AlertTypeId`, `Name`, `Description`, `Status_id`) VALUES ('CHECK_STATUS', 'CHECK', 'Check Status', 'Check has been posted to my account or has been rejected', 'SID_ACTIVE');
INSERT INTO `alertsubtype` (`id`, `AlertTypeId`, `Name`, `Description`, `Status_id`) VALUES ('PAYMENT_DUE_DATE_REMINDER', 'PAYMENT_REMINDER', 'Payment Due Date Reminder', 'Notify the user ____ days prior to the due date of the payment.', 'SID_ACTIVE');
INSERT INTO `alertsubtype` (`id`, `AlertTypeId`, `Name`, `Description`, `Status_id`) VALUES ('PAYMENT_OVERDUE', 'PAYMENT_REMINDER', 'Payment Overdue', 'When the user''s payment is overdue.','SID_ACTIVE');
INSERT INTO `alertsubtype` (`id`, `AlertTypeId`, `Name`, `Description`, `Status_id`) VALUES ('DEPOSIT_MATURITY_REMINDER', 'DEPOSIT_REMINDER', 'Deposit Maturity Reminder', 'Notify the users ____ days prior to the maturity date of my time deposit.','SID_ACTIVE');


INSERT INTO `eventtype` (`id`, `Name`, `ActivityType`) VALUES ('BALANCE', 'Balance', 'CUSTOMER');
INSERT INTO `eventtype` (`id`, `Name`, `ActivityType`) VALUES ('CHECK', 'Check', 'CUSTOMER');
INSERT INTO `eventtype` (`id`, `Name`, `ActivityType`) VALUES ('DEPOSIT_REMINDER', 'Deposit Reminder', 'CUSTOMER');
INSERT INTO `eventtype` (`id`, `Name`, `ActivityType`) VALUES ('PAYMENT_REMINDER', 'Payment Reminders', 'CUSTOMER');

INSERT INTO `eventsubtype` (`id`, `eventtypeid`, `Name`, `Description`) VALUES ('MINIMUM_BALANCE', 'BALANCE', 'Minimum Balance', 'Alert when account''s balance is below the defined value.');
INSERT INTO `eventsubtype` (`id`, `eventtypeid`, `Name`, `Description`) VALUES ('OVERDRAFT_ALERT', 'BALANCE', 'Overdraft Alerts', 'Alert when the account is overdrawn.');
INSERT INTO `eventsubtype` (`id`, `eventtypeid`, `Name`, `Description`) VALUES ('DEPOSITS', 'BALANCE', 'Deposits', 'When an amount is credited into the account.');
INSERT INTO `eventsubtype` (`id`, `eventtypeid`, `Name`, `Description`) VALUES ('WITHDRAWAL', 'BALANCE', 'Withdrawal', 'When an amount is debited into the account.');
INSERT INTO `eventsubtype` (`id`, `eventtypeid`, `Name`, `Description`) VALUES ('BALANCE_UPDATE', 'BALANCE', 'Balance Update', 'Notify me of my account balance every day/week/month/year');
INSERT INTO `eventsubtype` (`id`, `eventtypeid`, `Name`, `Description`) VALUES ('CHECK_STATUS', 'CHECK', 'Check Status', 'Check has been posted to my account or has been rejected');
INSERT INTO `eventsubtype` (`id`, `eventtypeid`, `Name`, `Description`) VALUES ('PAYMENT_DUE_DATE_REMINDER', 'PAYMENT_REMINDER', 'Payment Due Date Reminder', 'Notify the user ____ days prior to the due date of the payment.');
INSERT INTO `eventsubtype` (`id`, `eventtypeid`, `Name`, `Description`) VALUES ('PAYMENT_OVERDUE', 'PAYMENT_REMINDER', 'Payment Overdue', 'When the user''s payment is overdue.');
INSERT INTO `eventsubtype` (`id`, `eventtypeid`, `Name`, `Description`) VALUES ('DEPOSIT_MATURITY_REMINDER', 'DEPOSIT_REMINDER', 'Deposit Maturity Reminder', 'Notify the users ____ days prior to the maturity date of my time deposit.');


INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`) VALUES ('110', 'en-US', 'MINIMUM_BALANCE', 'CH_SMS', 'SID_EVENT_SUCCESS', 'minimumbalance', 'Dear Customer, Your account balance for a/c [#]MaskedFromAccount[/#] is below the minimum balance. For any further queries you can call DBX bank.');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('111', 'en-US', 'MINIMUM_BALANCE', 'CH_PUSH_NOTIFICATION', 'SID_EVENT_SUCCESS', 'minimumbalance', 'Dear Customer, Your account balance for a/c [#]MaskedFromAccount[/#] is below the minimum balance. For any further queries you can call DBX bank.', 'Balance Update');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('112', 'en-US', 'MINIMUM_BALANCE', 'CH_NOTIFICATION_CENTER', 'SID_EVENT_SUCCESS', 'minimumbalance', 'Dear Customer, Your account balance for a/c [#]MaskedFromAccount[/#] is below the minimum balance. For any further queries you can call DBX bank.', 'Balance Update');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('113', 'en-US', 'MINIMUM_BALANCE', 'CH_EMAIL', 'SID_EVENT_SUCCESS', 'minimumbalance', 'Dear Customer, Your account balance for a/c [#]MaskedFromAccount[/#] is below the minimum balance. For any further queries you can call DBX bank.', 'Balance Update');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('114', 'en-US', 'OVERDRAFT_ALERT', 'CH_PUSH_NOTIFICATION', 'SID_EVENT_SUCCESS', 'overdraftalert', 'Dear Customer, Your account [#]MaskedFromAccount[/#] has insufficent funds for the requested transfer to avoid unarranged overdraft fee recharge today.', 'Overdraft Alert');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('115', 'en-US', 'OVERDRAFT_ALERT', 'CH_NOTIFICATION_CENTER', 'SID_EVENT_SUCCESS', 'overdraftalert', 'Dear Customer, Your account [#]MaskedFromAccount[/#] has insufficent funds for the requested transfer to avoid unarranged overdraft fee recharge today.', 'Overdraft Alert');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('116', 'en-US', 'OVERDRAFT_ALERT', 'CH_EMAIL', 'SID_EVENT_SUCCESS', 'overdraftalert', 'Dear Customer, Your account [#]MaskedFromAccount[/#] has insufficent funds for the requested transfer to avoid unarranged overdraft fee recharge today.', 'Overdraft Alert');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('117', 'en-US', 'OVERDRAFT_ALERT', 'CH_SMS', 'SID_EVENT_SUCCESS', 'overdraftalert', 'Dear Customer, Your account [#]MaskedFromAccount[/#] has insufficent funds for the requested transfer to avoid unarranged overdraft fee recharge today.', NULL);
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('118', 'en-US', 'DEPOSITS', 'CH_PUSH_NOTIFICATION', 'SID_EVENT_SUCCESS', 'deposits', 'Dear Customer, You have received a deposit from [#]MaskedFromAccount[/#] of amount [#]amount[/#] on [#]server date[/#] (Ref no: [#]RefNumber[/#])', 'Balance Update');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('119', 'en-US', 'DEPOSITS', 'CH_NOTIFICATION_CENTER', 'SID_EVENT_SUCCESS', 'deposits', 'Dear Customer, You have received a deposit from [#]MaskedFromAccount[/#] of amount [#]amount[/#] on [#]server date[/#] (Ref no: [#]RefNumber[/#])', 'Balance Update');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('120', 'en-US', 'DEPOSITS', 'CH_EMAIL', 'SID_EVENT_SUCCESS', 'deposits', 'Dear Customer, You have received a deposit from [#]MaskedFromAccount[/#] of amount [#]amount[/#] on [#]server date[/#] (Ref no: [#]RefNumber[/#])', 'Balance Update');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`) VALUES ('121', 'en-US', 'DEPOSITS', 'CH_SMS', 'SID_EVENT_SUCCESS', 'deposits', 'Dear Customer, You have received a deposit from [#]MaskedFromAccount[/#] of amount [#]amount[/#] on [#]server date[/#] (Ref no: [#]RefNumber[/#])');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('122', 'en-US', 'WITHDRAWAL', 'CH_PUSH_NOTIFICATION', 'SID_EVENT_SUCCESS', 'withdrawal', 'Dear Customer, You have made a transfer of $ [#]amount[/#] from your a/c [#]MaskedFromAccount[/#] to [#]RecipientNickName[/#] on [#]server date[/#] (Ref no: [#]RefNumber[/#]). If this transaction is not done by you, call DBX bank immediately', 'Balance Update');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('123', 'en-US', 'WITHDRAWAL', 'CH_EMAIL', 'SID_EVENT_SUCCESS', 'withdrawal', 'Dear Customer, You have made a transfer of $ [#]amount[/#] from your a/c [#]MaskedFromAccount[/#] to [#]RecipientNickName[/#] on [#]server date[/#] (Ref no: [#]RefNumber[/#]). If this transaction is not done by you, call DBX bank immediately', 'Balance Update');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('124', 'en-US', 'WITHDRAWAL', 'CH_NOTIFICATION_CENTER', 'SID_EVENT_SUCCESS', 'withdrawal', 'Dear Customer, You have made a transfer of $ [#]amount[/#] from your a/c [#]MaskedFromAccount[/#] to [#]RecipientNickName[/#] on [#]server date[/#] (Ref no: [#]RefNumber[/#]). If this transaction is not done by you, call DBX bank immediately', 'Balance Update');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`) VALUES ('125', 'en-US', 'WITHDRAWAL', 'CH_SMS', 'SID_EVENT_SUCCESS', 'withdrawal', 'Dear Customer, You have made a transfer of $ [#]amount[/#] from your a/c [#]MaskedFromAccount[/#] to [#]RecipientNickName[/#] on [#]server date[/#] (Ref no: [#]RefNumber[/#]). If this transaction is not done by you, call DBX bank immediately');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('126', 'en-US', 'BALANCE_UPDATE', 'CH_PUSH_NOTIFICATION', 'SID_EVENT_SUCCESS', 'balanceupdate', 'Dear Customer,  Balance for you’re a/c [#]MaskedFromAccount[/#] as of [#]server date[/#] is [#]amount[/#]. For any further queries contact DBX bank.', 'Balance Update');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('127', 'en-US', 'BALANCE_UPDATE', 'CH_EMAIL', 'SID_EVENT_SUCCESS', 'balanceupdate', 'Dear Customer,  Balance for you’re a/c [#]MaskedFromAccount[/#] as of [#]server date[/#] is [#]amount[/#]. For any further queries contact DBX bank.', 'Balance Update');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('128', 'en-US', 'BALANCE_UPDATE', 'CH_NOTIFICATION_CENTER', 'SID_EVENT_SUCCESS', 'balanceupdate', 'Dear Customer,  Balance for you’re a/c [#]MaskedFromAccount[/#] as of [#]server date[/#] is [#]amount[/#]. For any further queries contact DBX bank.', 'Balance Update');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`) VALUES ('129', 'en-US', 'BALANCE_UPDATE', 'CH_SMS', 'SID_EVENT_SUCCESS', 'balanceupdate', 'Dear Customer,  Balance for you’re a/c [#]MaskedFromAccount[/#] as of [#]server date[/#] is [#]amount[/#]. For any further queries contact DBX bank.');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('130', 'en-US', 'CHECK_STATUS', 'CH_PUSH_NOTIFICATION', 'SID_EVENT_SUCCESS', 'checkstatus', 'Dear Customer, Check number [#]CheckNumber[/#] for the a/c [#]MaskedFromAccount[/#] has been [#]SuccessOrFailed[/#]. For any further queries contact DBX bank.', 'Check Status');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('131', 'en-US', 'CHECK_STATUS', 'CH_EMAIL', 'SID_EVENT_SUCCESS', 'checkstatus', 'Dear Customer, Check number [#]CheckNumber[/#] for the a/c [#]MaskedFromAccount[/#] has been [#]SuccessOrFailed[/#]. For any further queries contact DBX bank.', 'Check Status');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('132', 'en-US', 'CHECK_STATUS', 'CH_NOTIFICATION_CENTER', 'SID_EVENT_SUCCESS', 'checkstatus', 'Dear Customer, Check number [#]CheckNumber[/#] for the a/c [#]MaskedFromAccount[/#] has been [#]SuccessOrFailed[/#]. For any further queries contact DBX bank.', 'Check Status');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`) VALUES ('133', 'en-US', 'CHECK_STATUS', 'CH_SMS', 'SID_EVENT_SUCCESS', 'checkstatus', 'Dear Customer, Check number [#]CheckNumber[/#] for the a/c [#]MaskedFromAccount[/#] has been [#]SuccessOrFailed[/#]. For any further queries contact DBX bank.');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('134', 'en-US', 'PAYMENT_DUE_DATE_REMINDER', 'CH_PUSH_NOTIFICATION', 'SID_EVENT_SUCCESS', 'paymentduedatereminder', 'Dear Customer, Payment for your [#]AccountType[/#] account [#]MaskedFromAccount[/#] of amount [#]amount[/#] is due on [#]server date[/#].', 'Payment Due');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('135', 'en-US', 'PAYMENT_DUE_DATE_REMINDER', 'CH_EMAIL', 'SID_EVENT_SUCCESS', 'paymentduedatereminder', 'Dear Customer, Payment for your [#]AccountType[/#] account [#]MaskedFromAccount[/#] of amount [#]amount[/#] is due on [#]server date[/#].', 'Payment Due');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('136', 'en-US', 'PAYMENT_DUE_DATE_REMINDER', 'CH_NOTIFICATION_CENTER', 'SID_EVENT_SUCCESS', 'paymentduedatereminder', 'Dear Customer, Payment for your [#]AccountType[/#] account [#]MaskedFromAccount[/#] of amount [#]amount[/#] is due on [#]server date[/#].', 'Payment Due');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`) VALUES ('137', 'en-US', 'PAYMENT_DUE_DATE_REMINDER', 'CH_SMS', 'SID_EVENT_SUCCESS', 'paymentduedatereminder', 'Dear Customer, Payment for your [#]AccountType[/#] account [#]MaskedFromAccount[/#] of amount [#]amount[/#] is due on [#]server date[/#].');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('138', 'en-US', 'PAYMENT_OVERDUE', 'CH_PUSH_NOTIFICATION', 'SID_EVENT_SUCCESS', 'paymentoverdue', 'Dear Customer, Payment for your [#]AccountType[/#] account [#]MaskedFromAccount[/#] of amount [#]amount[/#] is overdue. Please make the payment immediately.', 'Payment Overdue');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('139', 'en-US', 'PAYMENT_OVERDUE', 'CH_EMAIL', 'SID_EVENT_SUCCESS', 'paymentoverdue', 'Dear Customer, Payment for your [#]AccountType[/#] account [#]MaskedFromAccount[/#] of amount [#]amount[/#] is overdue. Please make the payment immediately.', 'Payment Overdue');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('140', 'en-US', 'PAYMENT_OVERDUE', 'CH_NOTIFICATION_CENTER', 'SID_EVENT_SUCCESS', 'paymentoverdue', 'Dear Customer, Payment for your [#]AccountType[/#] account [#]MaskedFromAccount[/#] of amount [#]amount[/#] is overdue. Please make the payment immediately.', 'Payment Overdue');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`) VALUES ('141', 'en-US', 'PAYMENT_OVERDUE', 'CH_SMS', 'SID_EVENT_SUCCESS', 'paymentoverdue', 'Dear Customer, Payment for your [#]AccountType[/#] account [#]MaskedFromAccount[/#] of amount [#]amount[/#] is overdue. Please make the payment immediately.');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('142', 'en-US', 'DEPOSIT_MATURITY_REMINDER', 'CH_PUSH_NOTIFICATION', 'SID_EVENT_SUCCESS', 'depositmaturityreminder', 'Dear Customer, Your time deposit [#]DepositNumber[/#] is due to mature on [#]server date[/#]  For any further queries contact DBX bank.', 'Deposit Maturity Reminder');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('143', 'en-US', 'DEPOSIT_MATURITY_REMINDER', 'CH_EMAIL', 'SID_EVENT_SUCCESS', 'depositmaturityreminder', 'Dear Customer, Your time deposit [#]DepositNumber[/#] is due to mature on [#]server date[/#]  For any further queries contact DBX bank.', 'Deposit Maturity Reminder');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('144', 'en-US', 'DEPOSIT_MATURITY_REMINDER', 'CH_NOTIFICATION_CENTER', 'SID_EVENT_SUCCESS', 'depositmaturityreminder', 'Dear Customer, Your time deposit [#]DepositNumber[/#] is due to mature on [#]server date[/#]  For any further queries contact DBX bank.', 'Deposit Maturity Reminder');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`) VALUES ('145', 'en-US', 'DEPOSIT_MATURITY_REMINDER', 'CH_SMS', 'SID_EVENT_SUCCESS', 'depositmaturityreminder', 'Dear Customer, Your time deposit [#]DepositNumber[/#] is due to mature on [#]server date[/#]  For any further queries contact DBX bank.');

INSERT INTO `alertcontentfields` (`Code`, `Name`, `DefaultValue`, `createdby`) VALUES ('CheckNumber', 'Check Number', 'xxxx6789', 'konyuser');
INSERT INTO `alertcontentfields` (`Code`, `Name`, `DefaultValue`, `createdby`) VALUES ('SuccessOrFailed', 'Successful or Failed', 'Successful', 'konyuser');
INSERT INTO `alertcontentfields` (`Code`, `Name`, `DefaultValue`, `createdby`) VALUES ('DepositNumber', 'Deposit Number', 'xxxx1234', 'konyuser');

INSERT INTO `alerttypeaccounttype` (`AccountTypeId`, `AlertTypeId`, `createdby`) VALUES ('1', 'BALANCE', 'Kony user');
INSERT INTO `alerttypeaccounttype` (`AccountTypeId`, `AlertTypeId`, `createdby`) VALUES ('2', 'BALANCE', 'Kony user');
INSERT INTO `alerttypeaccounttype` (`AccountTypeId`, `AlertTypeId`, `createdby`) VALUES ('6', 'PAYMENT_REMINDER', 'Kony user');
INSERT INTO `alerttypeaccounttype` (`AccountTypeId`, `AlertTypeId`, `createdby`) VALUES ('3', 'PAYMENT_REMINDER', 'Kony user');
INSERT INTO `alerttypeaccounttype` (`AccountTypeId`, `AlertTypeId`, `createdby`) VALUES ('1', 'CHECK', 'Kony user');
INSERT INTO `alerttypeaccounttype` (`AccountTypeId`, `AlertTypeId`, `createdby`) VALUES ('2', 'CHECK', 'Kony user');
INSERT INTO `alerttypeaccounttype` (`AccountTypeId`, `AlertTypeId`, `createdby`) VALUES ('4', 'DEPOSIT_REMINDER', 'Kony user');

UPDATE `cardaccountrequest` SET Channel_id='CH_ID_INT' WHERE Channel_id='CH_ID_ONLINE';

UPDATE `travelnotification` SET Channel_id='CH_ID_INT' WHERE Channel_id='CH_ID_ONLINE';

DELETE FROM `servicechannel` WHERE (`id` = 'CH_ID_ONLINE');

INSERT INTO `servicechannel` (`id`, `Description`, `createdby`, `modifiedby`) VALUES ('CH_ID_TABLET_INT', 'Tablet Web', 'KOny User', 'Kony Dev');

UPDATE `servicechannel` SET `Description` = 'Tablet Native' WHERE (`id` = 'CH_ID_TABLET');

INSERT INTO `statustype` (`id`, `Description`, `createdby`, `modifiedby`) VALUES ('STID_TANDCSTATUS', 'Terms and conditions status', 'Kony User', 'Kony Dev');
INSERT INTO `status` (`id`, `Type_id`, `Description`, `createdby`, `modifiedby`) VALUES ('SID_TANDC_ACTIVE', 'STID_TANDCSTATUS', 'Terms and conditions active status', 'Kony User', 'Kony Dev');
INSERT INTO `status` (`id`, `Type_id`, `Description`, `createdby`, `modifiedby`) VALUES ('SID_TANDC_DRAFT', 'STID_TANDCSTATUS', 'Terms and conditions draft status', 'Kony User', 'Kony Dev');
INSERT INTO `status` (`id`, `Type_id`, `Description`, `createdby`, `modifiedby`) VALUES ('SID_TANDC_ARCHIVED', 'STID_TANDCSTATUS', 'Terms and conditions archived status', 'Kony User', 'Kony Dev');

UPDATE `termandconditiontext` SET `Status_id` = 'SID_TANDC_ACTIVE' WHERE (`id` = '3434211');
UPDATE `termandconditiontext` SET `Status_id` = 'SID_TANDC_ACTIVE' WHERE (`id` = '3434214');

INSERT INTO `service_permission_mapper` (`id`, `service_name`, `object_name`, `operation`, `user_type`, `permissions`) VALUES ('6ffe6a93-fa88-4c3f-b8f2-58c50fd7d436', 'TermsAndConditionsObjService', 'termsandconditions', 'createTermsAndConditionsVersion', 'TYPE_ID_CUSTOMER360', 'UpdateAppContent');
INSERT INTO `service_permission_mapper` (`id`, `service_name`, `object_name`, `operation`, `user_type`, `permissions`) VALUES ('6sde6a93-sa88-4c3f-bas2-58c50fd7d436', 'TermsAndConditionsObjService', 'termsandconditions', 'deleteTermsAndConditionsVersion', 'TYPE_ID_CUSTOMER360', 'UpdateAppContent');
INSERT INTO `service_permission_mapper` (`id`, `service_name`, `object_name`, `operation`, `user_type`, `permissions`) VALUES ('ffcb0286-2abc-403e-bd6f-9ertgdsfvasw', 'AlertAndAlertTypes', 'alertType', 'getCustomerAccountAlertSettings', 'C360_API_USER', 'API_ACCESS');

DELETE FROM `outagemessage`;

INSERT INTO `statustype` (`id`, `Description`, `createdby`, `modifiedby`) VALUES ('STID_OUTAGE_MESSAGE', 'Outage Messages', 'Kony User', 'Kony Dev');

INSERT INTO `status` (`id`, `Type_id`, `Description`) VALUES ('SID_OUTAGE_SCHEDULED_ACTIVE_COMPLETED', 'STID_OUTAGE_MESSAGE', 'Scheduled Active Completed');
INSERT INTO `status` (`id`, `Type_id`, `Description`) VALUES ('SID_OUTAGE_TERMINATED', 'STID_OUTAGE_MESSAGE', 'Terminated');
INSERT INTO `status` (`id`, `Type_id`, `Description`) VALUES ('SID_OUTAGE_PAUSED', 'STID_OUTAGE_MESSAGE', 'Paused');

UPDATE `alertattribute` SET `type` = 'AMOUNT' WHERE (`id` = 'AMOUNT') and (`LanguageCode` = 'en-US');
UPDATE `alertattribute` SET `type` = 'DAYS' WHERE (`id` = 'DAYS_BEFORE_TO_REMIND') and (`LanguageCode` = 'en-US');
UPDATE `alertattribute` SET `type` = 'DAYS' WHERE (`id` = 'FREQUENCY_OF_BALANCE_UPDATE') and (`LanguageCode` = 'en-US');
UPDATE `alertattribute` SET `type` = 'AMOUNT' WHERE (`id` = 'MINIMUM_BALANCE_THRESHOLD') and (`LanguageCode` = 'en-US');

UPDATE `securityquestion` SET `Question`='What is the name of your favorite childhood friend?' WHERE `id`='SEC_QUES_ID_1';
UPDATE `securityquestion` SET `Question`='What is the name of your favorite uncle?' WHERE `id`='SEC_QUES_ID_10';
UPDATE `securityquestion` SET `Question`='What is the name of your favorite childhood teacher?' WHERE `id`='SEC_QUES_ID_4';
UPDATE `securityquestion` SET `Question`='What is your favorite shampoo brand?' WHERE `id`='SEC_QUES_ID_9';

INSERT INTO `securityquestion` (`id`, `Status_id`, `Question`, `createdby`, `modifiedby`) VALUES ('SEC_QUES_ID_20', 'SID_ACTIVE', 'What was your favorite food as a child?', 'Kony User', 'Kony Dev');
INSERT INTO `securityquestion` (`id`, `Status_id`, `Question`, `createdby`, `modifiedby`) VALUES ('SEC_QUES_ID_21', 'SID_ACTIVE', 'What was the name of the company where you had your first job?', 'Kony User', 'Kony Dev');
INSERT INTO `securityquestion` (`id`, `Status_id`, `Question`, `createdby`, `modifiedby`) VALUES ('SEC_QUES_ID_22', 'SID_ACTIVE', 'What is your favorite movie?', 'Kony User', 'Kony Dev');
INSERT INTO `securityquestion` (`id`, `Status_id`, `Question`, `createdby`, `modifiedby`) VALUES ('SEC_QUES_ID_23', 'SID_ACTIVE', 'What was the name of your elementary school?', 'Kony User', 'Kony Dev');
INSERT INTO `securityquestion` (`id`, `Status_id`, `Question`, `createdby`, `modifiedby`) VALUES ('SEC_QUES_ID_24', 'SID_ACTIVE', 'Where was your favorite vacation?', 'Kony User', 'Kony Dev');

-- Update for accounts alerts data
INSERT INTO `dbxalerttype` (`id`, `Name`, `AlertCategoryId`, `Status_id`, `DisplaySequence`) VALUES ('OVERDRAFT', 'Overdraft', 'ALERT_CAT_ACCOUNTS', 'SID_ACTIVE', '4');
INSERT INTO `dbxalerttypetext` (`AlertTypeId`, `LanguageCode`, `DisplayName`, `Description`) VALUES ('OVERDRAFT', 'en-US', 'Overdraft Alert', 'An alert is sent to the customer if the account is overdrawn.');
UPDATE `dbxalerttype` SET `IsGlobal` = '0' WHERE (`id` = 'OVERDRAFT');

INSERT INTO `alerttypeapp` (`AppId`, `AlertTypeId`, `createdby`) VALUES ('RETAIL_BANKING', 'OVERDRAFT', 'Dev');

INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_SMALL_BUSINESS', 'OVERDRAFT', 'Dev');
INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_RETAIL', 'OVERDRAFT', 'Dev');
INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_MICRO_BUSINESS', 'OVERDRAFT', 'Dev');

INSERT INTO `alerttypeaccounttype` (`AccountTypeId`, `AlertTypeId`, `createdby`) VALUES ('4', 'OVERDRAFT', 'Dev');
UPDATE `alertsubtype` SET `AlertTypeId` = 'OVERDRAFT' WHERE (`id` = 'OVERDRAFT_ALERT');

UPDATE `alertsubtype` SET `Name` = 'Deposit Maturity Alert', `Description` = 'Notify the customers prior to the maturity date of their time deposit.' WHERE (`id` = 'DEPOSIT_MATURITY_REMINDER');
UPDATE `dbxalerttypetext` SET `DisplayName` = 'Deposit Maturity', `Description` = 'Notify the customers days prior to the maturity date of their time deposit.' WHERE (`AlertTypeId` = 'DEPOSIT_REMINDER') and (`LanguageCode` = 'en-US');
UPDATE `dbxalerttype` SET `Name` = 'Deposit Maturity', `Value1` = '1', `DisplaySequence` = '8' WHERE (`id` = 'DEPOSIT_REMINDER');

INSERT INTO `dbxalerttype` (`id`, `Name`, `AlertCategoryId`, `Status_id`, `IsGlobal`, `DisplaySequence`) VALUES ('DEPOSIT_WITHDRAWAL', 'Deposit and Withdrawal', 'ALERT_CAT_ACCOUNTS', 'SID_ACTIVE', '0', '3');
INSERT INTO `dbxalerttypetext` (`AlertTypeId`, `LanguageCode`, `DisplayName`, `Description`) VALUES ('DEPOSIT_WITHDRAWAL', 'en-US', 'Deposit and Withdrawal Alerts', 'An alert is sent to the customer incase of deposit / withdrawal from the account.');
INSERT INTO `alerttypeapp` (`AppId`, `AlertTypeId`, `createdby`) VALUES ('RETAIL_BANKING', 'DEPOSIT_WITHDRAWAL', 'Dev');
INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_SMALL_BUSINESS', 'DEPOSIT_WITHDRAWAL', 'Dev');
INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_RETAIL', 'DEPOSIT_WITHDRAWAL', 'Dev');
INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_MICRO_BUSINESS', 'DEPOSIT_WITHDRAWAL', 'Dev');
INSERT INTO `alerttypeaccounttype` (`AccountTypeId`, `AlertTypeId`, `createdby`) VALUES ('4', 'DEPOSIT_WITHDRAWAL', 'Dev');

UPDATE `alertsubtype` SET `AlertTypeId` = 'DEPOSIT_WITHDRAWAL' WHERE (`id` = 'DEPOSITS');
UPDATE `alertsubtype` SET `AlertTypeId` = 'DEPOSIT_WITHDRAWAL' WHERE (`id` = 'WITHDRAWAL');

DELETE FROM `communicationtemplate` WHERE (`Id` = '126');
DELETE FROM `communicationtemplate` WHERE (`Id` = '127');
DELETE FROM `communicationtemplate` WHERE (`Id` = '128');
DELETE FROM `communicationtemplate` WHERE (`Id` = '129');
DELETE FROM `alertsubtype` WHERE (`id` = 'BALANCE_UPDATE');


DELETE FROM `alerttypeapp` WHERE (`AppId` = 'RETAIL_BANKING') and (`AlertTypeId` = 'PAYMENT_REMINDER');
DELETE FROM `alerttypecustomertype` WHERE (`CustomerTypeId` = 'TYPE_ID_MICRO_BUSINESS') and (`AlertTypeId` = 'PAYMENT_REMINDER');
DELETE FROM `alerttypecustomertype` WHERE (`CustomerTypeId` = 'TYPE_ID_RETAIL') and (`AlertTypeId` = 'PAYMENT_REMINDER');
DELETE FROM `alerttypecustomertype` WHERE (`CustomerTypeId` = 'TYPE_ID_SMALL_BUSINESS') and (`AlertTypeId` = 'PAYMENT_REMINDER');

DELETE FROM `alerttypeaccounttype` WHERE (`AccountTypeId` = '3') and (`AlertTypeId` = 'PAYMENT_REMINDER');
DELETE FROM `alerttypeaccounttype` WHERE (`AccountTypeId` = '6') and (`AlertTypeId` = 'PAYMENT_REMINDER');
DELETE FROM `dbxalerttypetext` WHERE (`AlertTypeId` = 'PAYMENT_REMINDER') and (`LanguageCode` = 'en-US');

DELETE FROM `communicationtemplate` WHERE (`Id` = '134');
DELETE FROM `communicationtemplate` WHERE (`Id` = '135');
DELETE FROM `communicationtemplate` WHERE (`Id` = '136');
DELETE FROM `communicationtemplate` WHERE (`Id` = '137');
DELETE FROM `communicationtemplate` WHERE (`Id` = '138');
DELETE FROM `communicationtemplate` WHERE (`Id` = '139');
DELETE FROM `communicationtemplate` WHERE (`Id` = '140');
DELETE FROM `communicationtemplate` WHERE (`Id` = '141');

DELETE FROM `alertsubtype` WHERE (`id` = 'PAYMENT_DUE_DATE_REMINDER');
DELETE FROM `alertsubtype` WHERE (`id` = 'PAYMENT_OVERDUE');

DELETE FROM `dbxalerttype` WHERE (`id` = 'PAYMENT_REMINDER');

INSERT INTO `dbxalerttype` (`id`, `Name`, `AlertCategoryId`, `Status_id`, `IsGlobal`, `DisplaySequence`) VALUES ('DAILY_BALANCE', 'Daily Balance', 'ALERT_CAT_ACCOUNTS', 'SID_ACTIVE', '0', '2');
INSERT INTO `dbxalerttype` (`id`, `Name`, `AlertCategoryId`, `Status_id`, `IsGlobal`, `DisplaySequence`) VALUES ('PAYMENT_OVERDUE', 'Payment Overdue', 'ALERT_CAT_ACCOUNTS', 'SID_ACTIVE', '0', '7');
INSERT INTO `dbxalerttype` (`id`, `Name`, `AlertCategoryId`, `AttributeId`, `AlertConditionId`, `Value1`, `Status_id`, `IsGlobal`, `DisplaySequence`) VALUES ('PAYMENT_DUE_DATE', 'Payment Due Date', 'ALERT_CAT_ACCOUNTS', 'DAYS_BEFORE_TO_REMIND', 'EQUALS_TO', '1', 'SID_ACTIVE', '0', '6');
UPDATE `dbxalerttype` SET `DisplaySequence` = '5' WHERE (`id` = 'CHECK');

INSERT INTO `dbxalerttypetext` (`AlertTypeId`, `LanguageCode`, `DisplayName`, `Description`) VALUES ('DAILY_BALANCE', 'en-US', 'Daily Balance Alert', 'An alert is sent to the customer with their account balance daily.');
INSERT INTO `alerttypeapp` (`AppId`, `AlertTypeId`, `createdby`) VALUES ('RETAIL_BANKING', 'DAILY_BALANCE', 'Dev');

INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_SMALL_BUSINESS', 'DAILY_BALANCE', 'Dev');
INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_RETAIL', 'DAILY_BALANCE', 'Dev');
INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_MICRO_BUSINESS', 'DAILY_BALANCE', 'Dev');

INSERT INTO `alerttypeaccounttype` (`AccountTypeId`, `AlertTypeId`, `createdby`) VALUES ('1', 'DAILY_BALANCE', 'Dev');
INSERT INTO `alerttypeaccounttype` (`AccountTypeId`, `AlertTypeId`, `createdby`) VALUES ('2', 'DAILY_BALANCE', 'Dev');
INSERT INTO `alertsubtype` (`id`, `AlertTypeId`, `Name`, `Description`, `Status_id`) VALUES ('DAILY_BALANCE', 'DAILY_BALANCE', 'Daily Balance Alert', 'Notify the customer of their account balance daily.', 'SID_ACTIVE');


INSERT INTO `dbxalerttypetext` (`AlertTypeId`, `LanguageCode`, `DisplayName`, `Description`) VALUES ('PAYMENT_OVERDUE', 'en-US', 'Payment Overdue Alert', 'An alert is sent to the customer for overdue payment.');
INSERT INTO `alerttypeapp` (`AppId`, `AlertTypeId`, `createdby`) VALUES ('RETAIL_BANKING', 'PAYMENT_OVERDUE', 'Dev');
INSERT INTO `alerttypeapp` (`AppId`, `AlertTypeId`, `createdby`) VALUES ('RETAIL_BANKING', 'PAYMENT_DUE_DATE', 'Dev');
INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_SMALL_BUSINESS', 'PAYMENT_OVERDUE', 'Dev');
INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_RETAIL', 'PAYMENT_OVERDUE', 'Dev');
INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_MICRO_BUSINESS', 'PAYMENT_OVERDUE', 'Dev');
INSERT INTO `alerttypeaccounttype` (`AccountTypeId`, `AlertTypeId`) VALUES ('3', 'PAYMENT_OVERDUE');
INSERT INTO `alerttypeaccounttype` (`AccountTypeId`, `AlertTypeId`) VALUES ('6', 'PAYMENT_OVERDUE');
INSERT INTO `alertsubtype` (`id`, `AlertTypeId`, `Name`, `Description`, `Status_id`) VALUES ('PAYMENT_OVERDUE', 'PAYMENT_OVERDUE', 'Payment Overdue', 'When the customer''s payment is overdue.', 'SID_ACTIVE');

INSERT INTO `dbxalerttypetext` (`AlertTypeId`, `LanguageCode`, `DisplayName`, `Description`) VALUES ('PAYMENT_DUE_DATE', 'en-US', 'Payment Due Date', 'An alert is sent to the customer days before the payment is due.');

INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_SMALL_BUSINESS', 'PAYMENT_DUE_DATE', 'Dev');
INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_RETAIL', 'PAYMENT_DUE_DATE', 'Dev');
INSERT INTO `alerttypecustomertype` (`CustomerTypeId`, `AlertTypeId`, `createdby`) VALUES ('TYPE_ID_MICRO_BUSINESS', 'PAYMENT_DUE_DATE', 'Dev');

INSERT INTO `alerttypeaccounttype` (`AccountTypeId`, `AlertTypeId`) VALUES ('3', 'PAYMENT_DUE_DATE');
INSERT INTO `alerttypeaccounttype` (`AccountTypeId`, `AlertTypeId`) VALUES ('6', 'PAYMENT_DUE_DATE');

INSERT INTO `alertsubtype` (`id`, `AlertTypeId`, `Name`, `Description`, `Status_id`) VALUES ('PAYMENT_DUE_DATE', 'PAYMENT_DUE_DATE', 'Payment Due Date Alert', 'Notify the customer prior to the due date of the payment.', 'SID_ACTIVE');

DELETE FROM `eventsubtype` WHERE (`id` = 'PAYMENT_DUE_DATE_REMINDER') and (`eventtypeid` = 'PAYMENT_REMINDER');
DELETE FROM `eventsubtype` WHERE (`id` = 'PAYMENT_OVERDUE') and (`eventtypeid` = 'PAYMENT_REMINDER');

DELETE FROM `eventtype` WHERE (`id` = 'PAYMENT_REMINDER');
INSERT INTO `eventtype` (`id`, `Name`, `ActivityType`) VALUES ('OVERDRAFT', 'Overdraft', 'CUSTOMER');
INSERT INTO `eventtype` (`id`, `Name`, `ActivityType`) VALUES ('DEPOSIT_WITHDRAWAL', 'Deposit and Withdrawal', 'CUSTOMER');
INSERT INTO `eventtype` (`id`, `Name`, `ActivityType`) VALUES ('DAILY_BALANCE', 'Daily Balance', 'CUSTOMER');
INSERT INTO `eventtype` (`id`, `Name`, `ActivityType`) VALUES ('PAYMENT_OVERDUE', 'Payment Overdue', 'CUSTOMER');
INSERT INTO `eventtype` (`id`, `Name`, `ActivityType`) VALUES ('PAYMENT_DUE_DATE', 'Payment Due Date', 'CUSTOMER');

UPDATE `eventsubtype` SET `eventtypeid` = 'OVERDRAFT' WHERE (`id` = 'OVERDRAFT_ALERT') and (`eventtypeid` = 'BALANCE');
UPDATE `eventsubtype` SET `eventtypeid` = 'DEPOSIT_WITHDRAWAL' WHERE (`id` = 'DEPOSITS') and (`eventtypeid` = 'BALANCE');
UPDATE `eventsubtype` SET `eventtypeid` = 'DEPOSIT_WITHDRAWAL' WHERE (`id` = 'WITHDRAWAL') and (`eventtypeid` = 'BALANCE');
DELETE FROM `eventsubtype` WHERE (`id` = 'BALANCE_UPDATE') and (`eventtypeid` = 'BALANCE');
INSERT INTO `eventsubtype` (`id`, `eventtypeid`, `Name`) VALUES ('DAILY_BALANCE', 'DAILY_BALANCE', 'Daily Balance');
INSERT INTO `eventsubtype` (`id`, `eventtypeid`, `Name`) VALUES ('PAYMENT_OVERDUE', 'PAYMENT_OVERDUE', 'Payment Overdue');
INSERT INTO `eventsubtype` (`id`, `eventtypeid`, `Name`) VALUES ('PAYMENT_DUE_DATE', 'PAYMENT_DUE_DATE', 'Payment Due Date');

INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('146', 'en-US', 'DAILY_BALANCE', 'CH_NOTIFICATION_CENTER', 'SID_EVENT_SUCCESS', 'Balance Update', 'Balance for your a/c <account no> as of <server date> is <amount>. For further queries contact DBX bank.', 'Balance Update');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('147', 'en-US', 'DAILY_BALANCE', 'CH_EMAIL', 'SID_EVENT_SUCCESS', 'Balance Update', 'Balance for your a/c <account no> as of <server date> is <amount>. For further queries contact DBX bank.', 'Balance Update');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('148', 'en-US', 'DAILY_BALANCE', 'CH_SMS', 'SID_EVENT_SUCCESS', 'Balance Update', 'Dear Customer,\nBalance for you’re a/c <account no> as of <server date> is <amount>. For further queries contact DBX bank.', 'Balance Update');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('149', 'en-US', 'DAILY_BALANCE', 'CH_PUSH_NOTIFICATION', 'SID_EVENT_SUCCESS', 'Balance Update', 'Balance for your a/c <account no> as of <server date> is <amount>. For further queries contact DBX bank.', 'Balance Update');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('150', 'en-US', 'PAYMENT_OVERDUE', 'CH_NOTIFICATION_CENTER', 'SID_EVENT_SUCCESS', 'Payment Overdue', 'Dear Customer,                                                                                      Payment for your <account type (credit card /loan> account <account number> of amount <amount> is overdue. Please make the payment immediately.', 'Payment Overdue');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('151', 'en-US', 'PAYMENT_OVERDUE', 'CH_EMAIL', 'SID_EVENT_SUCCESS', 'Payment Overdue', 'Dear Customer,                                                                                      Payment for your <account type (credit card /loan> account <account number> of amount <amount> is overdue. Please make the payment immediately.', 'Payment Overdue');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('152', 'en-US', 'PAYMENT_OVERDUE', 'CH_SMS', 'SID_EVENT_SUCCESS', 'Payment Overdue', 'Dear Customer,                                                                                           Payment for your <account type (credit card /loan> account <account number> of amount <amount> is overdue. Please make the payment immediately.', 'Payment Overdue');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('153', 'en-US', 'PAYMENT_OVERDUE', 'CH_PUSH_NOTIFICATION', 'SID_EVENT_SUCCESS', 'Payment Overdue', 'Dear Customer,                                                                                      Payment for your <account type (credit card /loan> account <account number> of amount <amount> is overdue. Please make the payment immediately.', 'Payment Overdue');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('154', 'en-US', 'PAYMENT_DUE_DATE', 'CH_NOTIFICATION_CENTER', 'SID_EVENT_SUCCESS', 'Payment Due', 'Dear Customer,                                                                                      Payment for your <account type (credit card /loan> account <account number> of amount <amount> is due on <date>.', 'Payment Due');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('155', 'en-US', 'PAYMENT_DUE_DATE', 'CH_EMAIL', 'SID_EVENT_SUCCESS', 'Payment Due', 'Dear Customer,                                                                                      Payment for your <account type (credit card /loan> account <account number> of amount <amount> is due on <date>.', 'Payment Due');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('156', 'en-US', 'PAYMENT_DUE_DATE', 'CH_SMS', 'SID_EVENT_SUCCESS', 'Payment Due', 'Dear Customer,                                                                                           Payment for your <account type (credit card /loan> account <account number> of amount <amount> is due on <date>.', 'Payment Due');
INSERT INTO `communicationtemplate` (`Id`, `LanguageCode`, `AlertSubTypeId`, `ChannelID`, `Status_id`, `Name`, `Text`, `Subject`) VALUES ('157', 'en-US', 'PAYMENT_DUE_DATE', 'CH_PUSH_NOTIFICATION', 'SID_EVENT_SUCCESS', 'Payment Due', 'Dear Customer,                                                                                      Payment for your <account type (credit card /loan> account <account number> of amount <amount> is due on <date>.', 'Payment Due');

UPDATE `dbxalertcategorytext` SET `Description` = 'Account alerts are sent to the  when an account related activity happens. The customer can choose to activate or deactivate these alerts' WHERE (`AlertCategoryId` = 'ALERT_CAT_ACCOUNTS') and (`LanguageCode` = 'en-US');
UPDATE `dbxalerttypetext` SET `DisplayName` = 'Notify me in case the account balance is below', `Description` = 'An alert is sent to the customer incase the account balance is below the minimum balance amount' WHERE (`AlertTypeId` = 'BALANCE') and (`LanguageCode` = 'en-US');
UPDATE `dbxalerttypetext` SET `DisplayName` = 'Notify me prior to when a payment is due. Number of days' WHERE (`AlertTypeId` = 'PAYMENT_DUE_DATE') and (`LanguageCode` = 'en-US');
UPDATE `dbxalerttypetext` SET `DisplayName` = 'Notify me prior to the maturity of my time deposit. Number of days' WHERE (`AlertTypeId` = 'DEPOSIT_REMINDER') and (`LanguageCode` = 'en-US');
UPDATE `dbxalerttype` SET `Name` = 'Minimum Balance' WHERE (`id` = 'BALANCE');
--End of account alerts data

INSERT INTO `termandcondition` (`id`, `Code`, `Title`, `Description`) VALUES ('1920004', 'Login_TnC', 'Login', 'Login');
INSERT INTO `termandcondition` (`id`, `Code`, `Title`, `Description`) VALUES ('1920005', 'Enroll_TnC', 'Enroll', 'Enroll');
INSERT INTO `termandcondition` (`id`, `Code`, `Title`, `Description`) VALUES ('1920006', 'Estatements_TnC', 'Enable E-Statements', 'Enable E-Statements');
INSERT INTO `termandcondition` (`id`, `Code`, `Title`, `Description`) VALUES ('1920007', 'P2P_Activation_TnC', 'P2P Activation', 'P2P Activation');
INSERT INTO `termandcondition` (`id`, `Code`, `Title`, `Description`) VALUES ('1920008', 'P2P_TnC', 'P2P Transfers', 'P2P Transfers');
INSERT INTO `termandcondition` (`id`, `Code`, `Title`, `Description`) VALUES ('1920009', 'BillPay_Activation_TnC', 'Bill Pay Activation', 'Bill Pay Activation');
INSERT INTO `termandcondition` (`id`, `Code`, `Title`, `Description`) VALUES ('1920010', 'BillPay_TnC', 'Bill Payments', 'Bill Payments');
INSERT INTO `termandcondition` (`id`, `Code`, `Title`, `Description`) VALUES ('1920011', 'WireTransfers_TnC', 'Wire Transfers', 'Wire Transfers');
INSERT INTO `termandcondition` (`id`, `Code`, `Title`, `Description`) VALUES ('1920012', 'International_WireTransfers_TnC', 'International Wire Transfers', 'International Wire Transfers');
INSERT INTO `termandcondition` (`id`, `Code`, `Title`, `Description`) VALUES ('1920013', 'International_Transfers_TnC', 'International Transfers', 'International Transfers');
INSERT INTO `termandcondition` (`id`, `Code`, `Title`, `Description`) VALUES ('1920014', 'SEPA_TnC', 'SEPA Transfers', 'SEPA Transfers');
INSERT INTO `termandcondition` (`id`, `Code`, `Title`, `Description`) VALUES ('1920015', 'StopPayment_TnC', 'Stop Check Payment', 'Stop Check Payment');
INSERT INTO `termandcondition` (`id`, `Code`, `Title`, `Description`) VALUES ('1920016', 'LockCard_TnC', 'Card Management - Lock Card', 'Card Management - Lock Card');
INSERT INTO `termandcondition` (`id`, `Code`, `Title`, `Description`) VALUES ('1920017', 'CancelCard_TnC', 'Card Management - Cancel Card', 'Card Management - Cancel Card');
INSERT INTO `termandcondition` (`id`, `Code`, `Title`, `Description`) VALUES ('1920018', 'Origination_TnC', 'Open New Account Post Login', 'Open New Account Post Login');
INSERT INTO `termandcondition` (`id`, `Code`, `Title`, `Description`) VALUES ('1920019', 'Onboarding_TnC', 'New User On-boarding', 'New User On-boarding');
INSERT INTO `termandcondition` (`id`, `Code`, `Title`, `Description`) VALUES ('1920020', 'RetailBanking_TnC', 'Footer Links, Hamburger Menu', 'Footer Links, Hamburger Menu');
UPDATE IGNORE `termandcondition` SET `Code` = 'DBX_Default_TnC' WHERE (`id` = '1920000');
UPDATE IGNORE `termandcondition` SET `Code` = 'C360_CustomerOnboarding_TnC' WHERE (`id` = '1920003');

INSERT INTO `termandconditiontext` (`id`, `TermAndConditionId`, `LanguageCode`, `Version_Id`,  `ContentType_id`, `ContentModifiedBy`, `Status_id`) VALUES ('3450004', '1920004', 'en-US', '1.0', 'TEXT', 'Kony User', 'SID_TANDC_ACTIVE');
INSERT INTO `termandconditiontext` (`id`, `TermAndConditionId`, `LanguageCode`, `Version_Id`,  `ContentType_id`, `ContentModifiedBy`, `Status_id`) VALUES ('3450005', '1920005', 'en-US', '1.0', 'TEXT', 'Kony User', 'SID_TANDC_ACTIVE');
INSERT INTO `termandconditiontext` (`id`, `TermAndConditionId`, `LanguageCode`, `Version_Id`,  `ContentType_id`, `ContentModifiedBy`, `Status_id`) VALUES ('3450006', '1920006', 'en-US', '1.0', 'TEXT', 'Kony User', 'SID_TANDC_ACTIVE');
INSERT INTO `termandconditiontext` (`id`, `TermAndConditionId`, `LanguageCode`, `Version_Id`,  `ContentType_id`, `ContentModifiedBy`, `Status_id`) VALUES ('3450007', '1920007', 'en-US', '1.0', 'TEXT', 'Kony User', 'SID_TANDC_ACTIVE');
INSERT INTO `termandconditiontext` (`id`, `TermAndConditionId`, `LanguageCode`, `Version_Id`,  `ContentType_id`, `ContentModifiedBy`, `Status_id`) VALUES ('3450008', '1920008', 'en-US', '1.0', 'TEXT', 'Kony User', 'SID_TANDC_ACTIVE');
INSERT INTO `termandconditiontext` (`id`, `TermAndConditionId`, `LanguageCode`, `Version_Id`,  `ContentType_id`, `ContentModifiedBy`, `Status_id`) VALUES ('3450009', '1920009', 'en-US', '1.0', 'TEXT', 'Kony User', 'SID_TANDC_ACTIVE');
INSERT INTO `termandconditiontext` (`id`, `TermAndConditionId`, `LanguageCode`, `Version_Id`,  `ContentType_id`, `ContentModifiedBy`, `Status_id`) VALUES ('3450010', '1920010', 'en-US', '1.0', 'TEXT', 'Kony User', 'SID_TANDC_ACTIVE');
INSERT INTO `termandconditiontext` (`id`, `TermAndConditionId`, `LanguageCode`, `Version_Id`,  `ContentType_id`, `ContentModifiedBy`, `Status_id`) VALUES ('3450011', '1920011', 'en-US', '1.0', 'TEXT', 'Kony User', 'SID_TANDC_ACTIVE');
INSERT INTO `termandconditiontext` (`id`, `TermAndConditionId`, `LanguageCode`, `Version_Id`,  `ContentType_id`, `ContentModifiedBy`, `Status_id`) VALUES ('3450012', '1920012', 'en-US', '1.0', 'TEXT', 'Kony User', 'SID_TANDC_ACTIVE');
INSERT INTO `termandconditiontext` (`id`, `TermAndConditionId`, `LanguageCode`, `Version_Id`,  `ContentType_id`, `ContentModifiedBy`, `Status_id`) VALUES ('3450013', '1920013', 'en-US', '1.0', 'TEXT', 'Kony User', 'SID_TANDC_ACTIVE');
INSERT INTO `termandconditiontext` (`id`, `TermAndConditionId`, `LanguageCode`, `Version_Id`,  `ContentType_id`, `ContentModifiedBy`, `Status_id`) VALUES ('3450014', '1920014', 'en-US', '1.0', 'TEXT', 'Kony User', 'SID_TANDC_ACTIVE');
INSERT INTO `termandconditiontext` (`id`, `TermAndConditionId`, `LanguageCode`, `Version_Id`,  `ContentType_id`, `ContentModifiedBy`, `Status_id`) VALUES ('3450015', '1920015', 'en-US', '1.0', 'TEXT', 'Kony User', 'SID_TANDC_ACTIVE');
INSERT INTO `termandconditiontext` (`id`, `TermAndConditionId`, `LanguageCode`, `Version_Id`,  `ContentType_id`, `ContentModifiedBy`, `Status_id`) VALUES ('3450016', '1920016', 'en-US', '1.0', 'TEXT', 'Kony User', 'SID_TANDC_ACTIVE');
INSERT INTO `termandconditiontext` (`id`, `TermAndConditionId`, `LanguageCode`, `Version_Id`,  `ContentType_id`, `ContentModifiedBy`, `Status_id`) VALUES ('3450017', '1920017', 'en-US', '1.0', 'TEXT', 'Kony User', 'SID_TANDC_ACTIVE');
INSERT INTO `termandconditiontext` (`id`, `TermAndConditionId`, `LanguageCode`, `Version_Id`,  `ContentType_id`, `ContentModifiedBy`, `Status_id`) VALUES ('3450018', '1920018', 'en-US', '1.0', 'TEXT', 'Kony User', 'SID_TANDC_ACTIVE');
INSERT INTO `termandconditiontext` (`id`, `TermAndConditionId`, `LanguageCode`, `Version_Id`,  `ContentType_id`, `ContentModifiedBy`, `Status_id`) VALUES ('3450019', '1920019', 'en-US', '1.0', 'TEXT', 'Kony User', 'SID_TANDC_ACTIVE');
INSERT INTO `termandconditiontext` (`id`, `TermAndConditionId`, `LanguageCode`, `Version_Id`,  `ContentType_id`, `ContentModifiedBy`, `Status_id`) VALUES ('3450020', '1920020', 'en-US', '1.0', 'TEXT', 'Kony User', 'SID_TANDC_ACTIVE');

INSERT INTO `termandcondition` (`id`, `Code`, `Title`, `Description`) VALUES ('1920021', 'Commons_TnC', 'Footer Links', 'Footer Links');
UPDATE `termandcondition` SET `Title` = 'Hamburger Menu', `Description` = 'Hamburger Menu' WHERE (`id` = '1920020');
INSERT INTO `termandconditiontext` (`id`, `TermAndConditionId`, `LanguageCode`, `Version_Id`,  `ContentType_id`, `ContentModifiedBy`, `Status_id`) VALUES ('3450021', '1920021', 'en-US', '1.0', 'TEXT', 'Kony User', 'SID_TANDC_ACTIVE');

INSERT INTO `configurationbundles` (`bundle_id`, `bundle_name`, `app_id`, `createdby`, `modifiedby`) VALUES ('C360_CONFIG_BUNDLE', 'C360', 'C360', 'Dev', 'dev');
INSERT INTO `configurations` (`configuration_id`, `bundle_id`, `config_type`, `config_key`, `description`, `config_value`, `target`, `isPreLoginConfiguration`, `createdby`, `modifiedby`) VALUES ('3bca9ff8-74e7-4e93-9db1-876543ecvb', 'C360_CONFIG_BUNDLE', 'PREFERENCE', 'CONCURRENT_ACTIVE_OUTAGE_MESSAGES', 'Ensures that not more than n outage messages should be active for an application at a time', '2', 'SERVER', '0', 'Dev', 'Dev');

UPDATE `product` SET `productDescription`='* APR = Annual Percentage Rate. Rates, terms, and conditions subject to change. Credit subject to approval. + Rate includes 0.25% reduction for automatic payment from a Infinity Bank Savings or Checking Account. Other rates are available. NOTE: A convenience fee of $10.00 may be assessed for payments made by phone.' WHERE `id`='PRODUCT14';
UPDATE `product` SET `AdditionalInformation`='<p><sup>*</sup> APR = Annual Percentage Rate. Rates, terms, and conditions subject to change. Credit subject to approval. + Rate includes 0.25% reduction for automatic payment from a Infinity Bank Savings or Checking Account. Other rates are available. NOTE: A convenience fee of $10.00 may be assessed for payments made by phone.</p> ' WHERE `id`='PRODUCT14';

-- Update for alertattribute
INSERT INTO `alertattribute` (`id`, `LanguageCode`, `name`, `type`, `createdby`, `modifiedby`) VALUES ('FREQUENCY', 'en-US', 'Frequency', 'DAYS', 'Dev', 'Dev');
UPDATE dbxalerttype SET AttributeId='FREQUENCY' WHERE AttributeId IN ('DAYS_BEFORE_TO_REMIND','FREQUENCY_OF_BALANCE_UPDATE');
DELETE FROM `alertattribute` WHERE (`id` = 'FREQUENCY_OF_BALANCE_UPDATE') AND (`LanguageCode` = 'en-US');
DELETE FROM `alertattribute` WHERE (`id` = 'DAYS_BEFORE_TO_REMIND') AND (`LanguageCode` = 'en-US');
UPDATE dbxalerttype SET AttributeId='AMOUNT' WHERE AttributeId IN ('MINIMUM_BALANCE_THRESHOLD');
DELETE FROM `alertattribute` WHERE (`id` = 'MINIMUM_BALANCE_THRESHOLD') AND (`LanguageCode` = 'en-US');

UPDATE `policycontent` SET `Content` = '<ul class=\"\"><li class=\"\">The username should contain at least 8 characters and up to 64 characters</li><li class=\"\">Must contain at least one special character, only the following are allowed: .-_@!#$</li><li class=\"\">Password must contain at least one uppercase, one lowercase, and one number</li><li class=\"\">Password can have up to 4 consecutive repeated characters</li></ul>' WHERE (`id` = 'POL2');

UPDATE IGNORE `eligibilitycriteria` SET `Description` = 'I live, work, worship or attend school at a community that Infinity DBX Bank serves' WHERE (`id` = '2146f199-44ab-49ee-ae2b-93813791e132');
UPDATE IGNORE `eligibilitycriteria` SET `Description` = 'I am a member of an organization that Infinity DBX Bank serves' WHERE (`id` = '3e5542a9-ac52-4e47-b22a-0417a29f95fb');
UPDATE IGNORE `eligibilitycriteria` SET `Description` = 'I am an employee, retiree or family member of an employee of a company that Infinity DBX Bank serves' WHERE (`id` = '875a5ba2-f938-4a5a-acfa-08e19aad2c12');
UPDATE IGNORE `eligibilitycriteria` SET `Description` = 'I am related to a current Infinity DBX Bank member (Note: Eligible relationships include spouse, domestic partner, parent, grandparent, child, sibling, grandchild, step sibling or adopted children)' WHERE (`id` = 'b6d7d60c-352c-4956-a7c8-541e65916e4d');

UPDATE `dbxalerttypetext` SET `Description` = 'An alert is sent incase the payment is overdue.' WHERE (`AlertTypeId` = 'PAYMENT_OVERDUE') and (`LanguageCode` = 'en-US');
UPDATE `dbxalerttypetext` SET `Description` = 'An alert is sent before the payment is due. Number of days.' WHERE (`AlertTypeId` = 'PAYMENT_DUE_DATE') and (`LanguageCode` = 'en-US');
UPDATE `dbxalerttypetext` SET `Description` = 'An alert is sent if a check has been posted to the account or has been rejected.' WHERE (`AlertTypeId` = 'CHECK') and (`LanguageCode` = 'en-US');
UPDATE `dbxalerttypetext` SET `Description` = 'An alert is sent if the account is overdrawn.' WHERE (`AlertTypeId` = 'OVERDRAFT') and (`LanguageCode` = 'en-US');
UPDATE `dbxalerttypetext` SET `Description` = 'An alert is sent days prior to the maturity date of the time deposit. Number of days.' WHERE (`AlertTypeId` = 'DEPOSIT_REMINDER') and (`LanguageCode` = 'en-US');

UPDATE `servicechannel` SET `Description` = 'Desktop Web' WHERE (`id` = 'CH_ID_INT');

INSERT INTO `service_permission_mapper` (`id`, `service_name`, `object_name`, `operation`, `user_type`, `permissions`) VALUES ('ffa0d49b-16b8-4896-891e-a5f4b32d2334', 'AlertAndAlertTypes', 'alertType', 'getAccountTypes', 'TYPE_ID_CUSTOMER360', 'ViewAppContent');
INSERT INTO `service_permission_mapper` (`id`, `service_name`, `object_name`, `operation`, `user_type`, `permissions`) VALUES ('ffcb0286-2abc-403e-bd6f-9ertgadadadf', 'AlertAndAlertTypes', 'alertType', 'getCustomerAccountAlertSettings', 'TYPE_ID_CUSTOMER360', 'ViewAlerts');
INSERT INTO `service_permission_mapper` (`id`, `service_name`, `object_name`, `operation`, `user_type`, `permissions`) VALUES ('cd7947a1-81b2-4d4f-8760-6ca90b3sdf44f', 'StaticContentObjService', 'outageMessage', 'bulkUpdateOutageMessage', 'TYPE_ID_CUSTOMER360', 'ModifyAppContentStatus,UpdateAppContent,CreateAppContent');

INSERT INTO `termandconditionapp` (`id`, `TermAndConditionId`, `AppId`, `createdby`) VALUES ('2312334', '1920004', 'RETAIL_BANKING', 'Kony user');
INSERT INTO `termandconditionapp` (`id`, `TermAndConditionId`, `AppId`, `createdby`) VALUES ('2312335', '1920005', 'RETAIL_BANKING', 'Kony user');
INSERT INTO `termandconditionapp` (`id`, `TermAndConditionId`, `AppId`, `createdby`) VALUES ('2312336', '1920006', 'RETAIL_BANKING', 'Kony user');
INSERT INTO `termandconditionapp` (`id`, `TermAndConditionId`, `AppId`, `createdby`) VALUES ('2312337', '1920007', 'RETAIL_BANKING', 'Kony user');
INSERT INTO `termandconditionapp` (`id`, `TermAndConditionId`, `AppId`, `createdby`) VALUES ('2312338', '1920008', 'RETAIL_BANKING', 'Kony user');
INSERT INTO `termandconditionapp` (`id`, `TermAndConditionId`, `AppId`, `createdby`) VALUES ('2312339', '1920009', 'RETAIL_BANKING', 'Kony user');
INSERT INTO `termandconditionapp` (`id`, `TermAndConditionId`, `AppId`, `createdby`) VALUES ('2312340', '1920010', 'RETAIL_BANKING', 'Kony user');
INSERT INTO `termandconditionapp` (`id`, `TermAndConditionId`, `AppId`, `createdby`) VALUES ('2312341', '1920011', 'RETAIL_BANKING', 'Kony user');
INSERT INTO `termandconditionapp` (`id`, `TermAndConditionId`, `AppId`, `createdby`) VALUES ('2312342', '1920012', 'RETAIL_BANKING', 'Kony user');
INSERT INTO `termandconditionapp` (`id`, `TermAndConditionId`, `AppId`, `createdby`) VALUES ('2312343', '1920013', 'RETAIL_BANKING', 'Kony user');
INSERT INTO `termandconditionapp` (`id`, `TermAndConditionId`, `AppId`, `createdby`) VALUES ('2312344', '1920014', 'RETAIL_BANKING', 'Kony user');
INSERT INTO `termandconditionapp` (`id`, `TermAndConditionId`, `AppId`, `createdby`) VALUES ('2312345', '1920015', 'RETAIL_BANKING', 'Kony user');
INSERT INTO `termandconditionapp` (`id`, `TermAndConditionId`, `AppId`, `createdby`) VALUES ('2312346', '1920016', 'RETAIL_BANKING', 'Kony user');
INSERT INTO `termandconditionapp` (`id`, `TermAndConditionId`, `AppId`, `createdby`) VALUES ('2312347', '1920017', 'RETAIL_BANKING', 'Kony user');
INSERT INTO `termandconditionapp` (`id`, `TermAndConditionId`, `AppId`, `createdby`) VALUES ('2312348', '1920018', 'RETAIL_BANKING', 'Kony user');
INSERT INTO `termandconditionapp` (`id`, `TermAndConditionId`, `AppId`, `createdby`) VALUES ('2312350', '1920020', 'RETAIL_BANKING', 'Kony user');
INSERT INTO `termandconditionapp` (`id`, `TermAndConditionId`, `AppId`, `createdby`) VALUES ('2312351', '1920021', 'RETAIL_BANKING', 'Kony user');

UPDATE `dbxalerttypetext` SET `DisplayName` = 'Minimum Balance Alert' WHERE (`AlertTypeId` = 'BALANCE') and (`LanguageCode` = 'en-US');
UPDATE `dbxalerttypetext` SET `DisplayName` = 'Payment Due Date Alert' WHERE (`AlertTypeId` = 'PAYMENT_DUE_DATE') and (`LanguageCode` = 'en-US');
UPDATE `dbxalerttypetext` SET `DisplayName` = 'Deposit and Withdrawal Alert' WHERE (`AlertTypeId` = 'DEPOSIT_WITHDRAWAL') and (`LanguageCode` = 'en-US');
UPDATE `dbxalerttypetext` SET `DisplayName` = 'Deposit Maturity Alert' WHERE (`AlertTypeId` = 'DEPOSIT_REMINDER') and (`LanguageCode` = 'en-US');

UPDATE `termandcondition` SET `Code` = 'Common_TnC' WHERE (`id` = '1920021');

INSERT INTO `service_permission_mapper` (`id`, `service_name`, `object_name`, `operation`, `user_type`, `permissions`) VALUES ('04039f1c-e142-4661-bc99-7654345345433', 'MasterDataObjService', 'countrycode', 'getCountryCodes', 'C360_API_USER', 'API_ACCESS');
