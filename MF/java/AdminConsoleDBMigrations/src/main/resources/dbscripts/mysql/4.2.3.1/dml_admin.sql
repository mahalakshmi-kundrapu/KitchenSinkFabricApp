UPDATE IGNORE `eligibilitycriteria` SET `Description` = 'I live, work, worship or attend school at a community that Kony Credit Union serves.' WHERE (`id` = 'KONY_DBX_BANK_AREA_RESIDENT');
UPDATE IGNORE `eligibilitycriteria` SET `Description` = 'I am an employee, retiree or family member of an employee of a company that Kony DBX bank serves.' WHERE (`id` = 'KONY_DBX_BANK_CLIENT_ACQ');
UPDATE IGNORE `eligibilitycriteria` SET `Description` = 'I am a member of a membership organization Kony DBX bank serves.' WHERE (`id` = 'KONY_DBX_BANK_MEMBER');
UPDATE IGNORE `eligibilitycriteria` SET `Description` = 'I am related to a current Kony DBX bank member (Note: Eligible relationships include spouse, domestic partner, parent, grandparent, child, sibling, grandchild, step sibling or adopted children).' WHERE (`id` = 'KONY_DBX_BANK_MEMBER_ACQ');

UPDATE `faqs` SET `Answer`='The application supports the following file types: 1.txt 2.doc 3.docx 4.pdf' WHERE `id`='FAQ_ID19';

UPDATE `policycontent` SET `Content`='<ul><li>Minimum length of username: 8</li><li>Maximum Length of username: 64</li><li>Special characters are allowed (.,-,_,@,!,#,$)</li></ul>' WHERE `id`='POL1';
UPDATE `policycontent` SET `Content`='<ul><li>Minimum length of password: 8</li><li>Maximum Length of password: 64</li><li>Special characters are allowed (.,-,_,@,!,#,$)</li><li>Password must contain atleast one uppercase, one lowercase, one number</li><li>Password can have at max 4 consecutive character repetition</li></ul>' WHERE `id`='POL2';