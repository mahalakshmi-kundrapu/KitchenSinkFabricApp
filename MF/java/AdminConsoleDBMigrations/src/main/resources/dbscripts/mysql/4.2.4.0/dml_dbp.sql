UPDATE `application` SET `noOfDaysForRatingFromProfile`='30', `noOfDaysForRatingFromTransactions`='14', `noOfDaysForAnotherAttemptForRating`='90', `maxtimesFeedbackperversion`='3', `majorVersionsForFeedback`='[4.1, 4.2, 4.3]' WHERE `id`='2';

UPDATE `application` SET `bannerImageURL`='http://pmqa.konylabs.net/KonyWebBanking/banner_img.png', `desktopBannerImageURL`='https://retailbanking1.konycloud.com/dbimages/banner-add-mob.png', `mobileBannerImageURL`='https://retailbanking1.konycloud.com/dbimages/banner-ad-tab.png', `viewMoreDBXLink`='https://dbx.kony.com/products-and-solutions/consumer-lending' WHERE `id`='2';

-- password change
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Password Changed' WHERE `Id` = '2'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Password Changed' WHERE `Id` = '3'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Password Changed' WHERE `Id` = '4'; 

-- username update
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Username Changed' WHERE `Id` = '18'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Username Changed' WHERE `Id` = '19'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Username Changed' WHERE `Id` = '20'; 

-- Login success
UPDATE IGNORE `communicationtemplate` SET `Text` = 'You have successfully signed in to your online banking profile.' WHERE `Id` = '73'; 
UPDATE IGNORE `communicationtemplate` SET `Text` = 'You have successfully signed in to your online banking profile.', `Subject` = 'Sign In Success' WHERE `Id` = '74'; 
UPDATE IGNORE `communicationtemplate` SET `Text` = 'You have successfully signed in to your online banking profile.', `Subject` = 'Sign In Success' WHERE `Id` = '75'; 
UPDATE IGNORE `communicationtemplate` SET `Text` = '<p>Dear [#]FirstName[/#] [#]LastName[/#],</p><p>You have successfully signed in to your online banking profile.</p><p>Regards,<br />DBX Bank</p>.', `Subject` = 'Sign In Success' WHERE `Id` = '76'; 

-- login failure
UPDATE IGNORE `communicationtemplate` SET `Text` = 'You have made an invalid sign in attempt for your username [#]MaskedUsername[/#]  on [#]ServerDate[/#] [#]ServerTime[/#].' WHERE `Id` = '77'; 
UPDATE IGNORE `communicationtemplate` SET `Text` = 'You have made an invalid sign in attempt for your username [#]MaskedUsername[/#]  on [#]ServerDate[/#] [#]ServerTime[/#].', `Subject` = 'Sign In Failed' WHERE `Id` = '78'; 
UPDATE IGNORE `communicationtemplate` SET `Text` = 'You have made an invalid sign in attempt for your username [#]MaskedUsername[/#]  on [#]ServerDate[/#] [#]ServerTime[/#].', `Subject` = 'Sign In Failed' WHERE `Id` = '79'; 
UPDATE IGNORE `communicationtemplate` SET `Text` = '<p>Dear [#]FirstName[/#] [#]LastName[/#],</p><p>You have made an invalid sign in attempt for your username [#]MaskedUsername[/#]  on [#]ServerDate[/#] [#]ServerTime[/#].</p><p>Regards,<br />DBX Bank</p><p>&nbsp;</p>', `Subject` = 'Sign In Failed' WHERE `Id` = '80'; 

-- account locked email
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Account Locked' WHERE `Id` = '82'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Account Locked' WHERE `Id` = '83'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Account Locked' WHERE `Id` = '84'; 

-- primary phone change
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Phone Number Changed' WHERE `Id` = '6'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Phone Number Changed' WHERE `Id` = '7'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Phone Number Changed' WHERE `Id` = '8'; 

-- primary email change
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Email Changed' WHERE `Id` = '10'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Email Changed' WHERE `Id` = '11'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Email Changed' WHERE `Id` = '12'; 

-- primary address change
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Address Changed' WHERE `Id` = '14'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Address Changed' WHERE `Id` = '15'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Address Changed' WHERE `Id` = '16'; 

-- bill payee added
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Bill Payee Added' WHERE `Id` = '21'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Bill Payee Added' WHERE `Id` = '22'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Bill Payee Added' WHERE `Id` = '24'; 

-- P2P recepient added
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'P2P Recipient Added' WHERE `Id` = '26'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'P2P Recipient Added' WHERE `Id` = '27'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'P2P Recipient Added' WHERE `Id` = '28'; 

-- international wire recipient added
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'International Wire Recipient Added' WHERE `Id` = '30'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'International Wire Recipient Added' WHERE `Id` = '31'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'International Wire Recipient Added' WHERE `Id` = '32'; 

-- domestic wire recipient added
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Domestic Wire Recipient Added' WHERE `Id` = '34'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Domestic Wire Recipient Added' WHERE `Id` = '35'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Domestic Wire Recipient Added' WHERE `Id` = '36'; 

-- international recipient added
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'International Recipient Added' WHERE `Id` = '38'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'International Recipient Added' WHERE `Id` = '39'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'International Recipient Added' WHERE `Id` = '40'; 

-- Other Bank Recipient Added
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Other Bank Recipient Added' WHERE `Id` = '42'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Other Bank Recipient Added' WHERE `Id` = '43'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Other Bank Recipient Added' WHERE `Id` = '44'; 


-- Same Bank Recipeint Added
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Same Bank Recipient Added' WHERE `Id` = '46'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Same Bank Recipient Added' WHERE `Id` = '47'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Same Bank Recipient Added' WHERE `Id` = '48'; 


-- Onetime - Other Bank Transfer
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Transfer Made' WHERE `Id` = '50'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Transfer Made' WHERE `Id` = '51'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Transfer Made' WHERE `Id` = '52'; 

-- scheduled - other bank transfer
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Transfer Made' WHERE `Id` = '54'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Transfer Made' WHERE `Id` = '55'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Transfer Made' WHERE `Id` = '56'; 

-- Recurring - Other Bank Transfer
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Transfer Made' WHERE `Id` = '58'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Transfer Made' WHERE `Id` = '59'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Transfer Made' WHERE `Id` = '60'; 

-- One time - Own account Transfer
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Transfer Made' WHERE `Id` = '62'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Transfer Made' WHERE `Id` = '63'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Transfer Made' WHERE `Id` = '64'; 

-- Scheduled - Own account Transfer
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Transfer Made' WHERE `Id` = '66'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Transfer Made' WHERE `Id` = '67'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Transfer Made' WHERE `Id` = '68';

-- Recurring - Own account Transfer
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Transfer Made' WHERE `Id` = '70'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Transfer Made' WHERE `Id` = '71'; 
UPDATE IGNORE `communicationtemplate` SET `Subject` = 'Transfer Made' WHERE `Id` = '72';


-- dbxalerttype, alertsubtype name change
UPDATE IGNORE `dbxalerttype` SET `Name` = 'Sign In' WHERE `id` = 'LOGIN';
UPDATE IGNORE `dbxalerttypetext` SET `DisplayName` = 'Sign In Alerts', `Description` ='An alert is sent when a sign in attempt is made into the digital banking app.'  WHERE `AlertTypeId` = 'LOGIN';
UPDATE IGNORE `dbxalerttypetext` SET `Description` ='An alert is sent when the digital sign in username or password is changed'  WHERE `AlertTypeId` = 'CREDENTIAL_CHANGE';
UPDATE IGNORE `dbxalerttypetext` SET `DisplayName` = 'Primary Phone/Email/Address Change'  WHERE `AlertTypeId` = 'PROFILE_UPDATE IGNORE';

UPDATE IGNORE `alertsubtype` SET `Name` = 'Sign In Attempt', `Description` = 'Alert when a successful or failed sign in attempt is made' WHERE `id` = 'LOGIN_ATTEMPT';
UPDATE IGNORE `alertsubtype` SET `Description` = 'Alert when customer''s sign in password is changed' WHERE `id` = 'PASSWORD_CHANGE';
UPDATE IGNORE `alertsubtype` SET `Description` = 'Alert when customer''s sign in username is changed' WHERE `id` = 'USERNAME_CHANGE';
UPDATE IGNORE `alertsubtype` SET `Description` = 'Alert when customer exceeds maximum failed sign in attempts & the account gets locked' WHERE `id` = 'ACCOUNT_LOCKED';

UPDATE IGNORE `dbxalertcategorytext` SET `Description` = 'Security alerts are sent when any sensitive data of a customer is accessed/UPDATE IGNOREd. Certain high security alerts are sent as mandatory alerts like sign in attempt, username or password UPDATE IGNORE. For others, the customers can choose to activate or deactivate them' WHERE `AlertCategoryId` = 'ALERT_CAT_SECURITY';

UPDATE IGNORE `emailtemplates` SET `TemplateText`='</p><center><div style="0width: 95%;"><div><table border="\&quot;0\&quot;" width="\&quot;100%\&quot;" cellspacing="\&quot;0\&quot;" cellpadding="\&quot;0\&quot;"><tbody><tr><td style="0background-color: #284e77; font-size: 1px; line-height: 1px;" align="\&quot;center\&quot;" valign="\&quot;top\&quot;" bgcolor="\&quot;#284e77\&quot;" height="\&quot;10\&quot;">&nbsp;</td></tr></tbody></table></div></div><div style="0width: 95%;"><div><table border="\&quot;0\&quot;" width="\&quot;100%\&quot;" cellspacing="\&quot;0\&quot;" cellpadding="\&quot;0\&quot;"><tbody><tr><td style="0background-color: #ffffff;"><div><table width="\&quot;50%\&quot;"><tbody><tr><td width="\&quot;200\&quot;"> <img src="\&quot;emailTemplateLogo\&quot;" alt="\&quot;kony_logo\&quot;" width="\&quot;200\&quot;" /> </td></tr></tbody></table><br /><br />  Hi %firstName%, <br /><br /> Reset your password, and we\"ll get you on your way. <br /><br /> <div><table style="0table-layout: fixed;" border="\&quot;0\&quot;" width="\&quot;100%\&quot;" cellspacing="\&quot;0\&quot;" cellpadding="\&quot;0\&quot;"><tbody><tr><td align="\&quot;center\&quot;"><div>To change your account password, <a href="\&quot;%resetPasswordLink%\&quot;">click here</a> <br /> or paste the following link on your browser: <br /><br /> %resetPasswordLink%</div></td></tr></tbody></table></div> <br /><div align="\&quot;center\&quot;">The link will expire in 24 hours, so please use it right away.</div><br /> Regards, <br /> konyBank team  <br /><br /><br /><br /><br /><br /> This is a system generated mail. If you are not the named addressee please notify the sender immediately by e-mail at support@konybank.com and then delete the e-mail from your system. Although the company has taken reasonable precautions to ensure no viruses are present in this email, the company cannot accept responsibility for any loss or damage arising from the use of this email or attachments. Kony, Inc. www.kony.com  <br /><br /><div align="\&quot;center\&quot;">Copyright &copy; 2018 Kony DBX Customer 360. All rights reserved.</div> </div></td></tr></tbody></table></div></div></center><p>' WHERE `id`='1';

UPDATE IGNORE `emailtemplates` SET `TemplateText`='<p>&nbsp;</p><center><div style=\"width: 95%; height: 10px; margin: 50px 0px 0px 0px;\"><div><table border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\"><tbody><tr><td style=\"background-color: #284e77; font-size: 1px; line-height: 1px; -webkit-text-size-adjust: none;\" align=\"center\" valign=\"top\" bgcolor=\"#284e77\" height=\"10\">&nbsp;</td></tr></tbody></table></div></div><div style=\"width: 95%; margin: 0px 0px 50px 0px;\"><div style=\"display: inline-block; text-align: left;\"><table border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\"><tbody><tr><td style=\"background-color: #ffffff; -webkit-text-size-adjust: none;\"><div style=\"margin: 50px 20px 50px 20px; color: #333b44; font-size: 14px;\"><table style=\"width: 50%;\" width=\"50%\"><tbody><tr><td width=\"200\"><img style=\"text-align: right; width: 200px; border: 0;\" src=\"https://www.kony.com/assets/images/all/themes/kony/assets/images/logo.svg\" alt=\"kony_logo\" width=\"200\" /></td></tr></tbody></table><br /><br />Hi %userName%,<br /><br />You are invited to Digital Banking Channel. Please activate your account now.<br /> <br /><br />Username: %userName%<br /><br /> <br />You are required to input your Username in the link below.<br /><br /><div style=\"text-align: center; border-width: 1px; border-radius: 4px;\"><table style=\"table-layout: fixed;\" border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\"><tbody><tr><td style=\"color: #ffffff; background-color: #333b44; word-wrap: break-word; word-break: break-all; padding: 10px; line-height: 20px;\" align=\"center\"><div style=\"margin: 10px 10px 10px 10px; font-size: 13px;\">To activate your account and set a password,<a style=\"color: #11abeb;\" href=\"%resetPasswordLink%\">click here</a> <br /> or paste the following link on your browser: <br /><br /><a style=\"color: #11abeb;\">%resetPasswordLink%</a></div></td></tr></tbody></table></div><br /><div align=\"center\">The link will expire in 24 hours, so please use it right away.</div><br />Regards,<br />Kony Banking Team <br /><br /><br /><br /><span style=\"color: #999999; font-size: 12px;\">This is a system generated mail. If you are not the named addressee please notify the sender immediately by e-mail at support@konybank.com and then delete the e-mail from your system. Although the company has taken reasonable precautions to ensure no viruses are present in this email, the company cannot accept responsibility for any loss or damage arising from the use of this email or attachments. Kony, Inc. www.kony.com </span><br /><br /><div align=\"center\"><span style=\"color: #999999;\">Copyright &copy; 2018 Kony DBX. All rights reserved.</span></div></div></td></tr></tbody></table></div></div></center>' WHERE `id`='2';
