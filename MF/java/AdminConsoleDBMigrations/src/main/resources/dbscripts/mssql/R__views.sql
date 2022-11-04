GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[customerbasicinfo_view] AS select 
konyadminconsole.customer.Username AS Username,
konyadminconsole.customer.FirstName AS FirstName,
konyadminconsole.customer.MiddleName AS MiddleName,
konyadminconsole.customer.LastName AS LastName,
CONCAT(konyadminconsole.customer.FirstName,' ',konyadminconsole.customer.MiddleName,' ',konyadminconsole.customer.LastName) AS Name,
konyadminconsole.customer.Salutation AS Salutation,
konyadminconsole.customer.id AS Customer_id,
konyadminconsole.customer.Ssn AS SSN,
konyadminconsole.customer.createdts AS CustomerSince,
konyadminconsole.customer.Gender AS Gender,
konyadminconsole.customer.DateOfBirth AS DateOfBirth,
konyadminconsole.customer.Status_id AS CustomerStatus_id,
(select konyadminconsole.status.Description from status where (konyadminconsole.customer.Status_id = konyadminconsole.status.id)) AS CustomerStatus_name,
konyadminconsole.customer.MaritalStatus_id AS MaritalStatus_id,
(select konyadminconsole.status.Description from status where (konyadminconsole.customer.MaritalStatus_id = konyadminconsole.status.id)) AS MaritalStatus_name,
konyadminconsole.customer.SpouseName AS SpouseName,
konyadminconsole.customer.EmployementStatus_id AS EmployementStatus_id,
(select konyadminconsole.status.Description from status where (konyadminconsole.customer.EmployementStatus_id = konyadminconsole.status.id)) AS EmployementStatus_name,
(select CONCAT(konyadminconsole.customerflagstatus.Status_id,' ',',') from konyadminconsole.customerflagstatus where (customerflagstatus.Customer_id = customer.id)) AS CustomerFlag_ids,
(select CONCAT(konyadminconsole.status.Description,' ',',') from konyadminconsole.status where konyadminconsole.status.id in (select konyadminconsole.customerflagstatus.Status_id from konyadminconsole.customerflagstatus where (konyadminconsole.customerflagstatus.Customer_id = konyadminconsole.customer.id))) AS CustomerFlag,
konyadminconsole.customer.IsEnrolledForOlb AS IsEnrolledForOlb,
konyadminconsole.customer.IsStaffMember AS IsStaffMember,
konyadminconsole.customer.Location_id AS Branch_id,
konyadminconsole.location.Name AS Branch_name,
konyadminconsole.location.Code AS Branch_code,
konyadminconsole.customer.IsOlbAllowed AS IsOlbAllowed,
konyadminconsole.customer.IsAssistConsented AS IsAssistConsented from (konyadminconsole.customer left join konyadminconsole.location on((konyadminconsole.customer.Location_id = konyadminconsole.location.id)));

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[customerservice_communication_view] 
AS select 
konyadminconsole.customerservice.id AS Service_id,
konyadminconsole.customerservice.Name AS Service_Name,
konyadminconsole.customerservice.Status_id AS Service_Status_id,
konyadminconsole.customerservice.Description AS Service_Description,
konyadminconsole.customerservice.softdeleteflag AS Service_SoftDeleteFlag,
konyadminconsole.servicecommunication.id AS ServiceCommunication_id,
konyadminconsole.servicecommunication.Type_id AS ServiceCommunication_Typeid,
konyadminconsole.servicecommunication.Value AS ServiceCommunication_Value,
konyadminconsole.servicecommunication.Extension AS ServiceCommunication_Extension,
konyadminconsole.servicecommunication.Description AS ServiceCommunication_Description,
konyadminconsole.servicecommunication.Status_id AS ServiceCommunication_Status_id,
konyadminconsole.servicecommunication.Priority AS ServiceCommunication_Priority,
konyadminconsole.servicecommunication.createdby AS ServiceCommunication_createdby,
konyadminconsole.servicecommunication.modifiedby AS ServiceCommunication_modifiedby,
konyadminconsole.servicecommunication.createdts AS ServiceCommunication_createdts,
konyadminconsole.servicecommunication.lastmodifiedts AS ServiceCommunication_lastmodifiedts,
konyadminconsole.servicecommunication.synctimestamp AS ServiceCommunication_synctimestamp,
konyadminconsole.servicecommunication.softdeleteflag AS ServiceCommunication_SoftDeleteFlag from (konyadminconsole.servicecommunication left join konyadminconsole.customerservice on((konyadminconsole.servicecommunication.Service_id = konyadminconsole.customerservice.id))) ;

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[faqcategory_view] AS 
select 
  konyadminconsole.faqs.id AS id, 
  konyadminconsole.faqs.Status_id AS Status_id, 
  konyadminconsole.faqs.QuestionCode AS QuestionCode, 
  konyadminconsole.faqs.Question AS Question, 
  konyadminconsole.faqs.Channel_id AS Channel_id, 
  konyadminconsole.faqs.Answer AS Answer, 
  konyadminconsole.faqs.FaqCategory_Id AS CategoryId, 
  konyadminconsole.faqcategory.Name AS CategoryName 
from 
  (
    konyadminconsole.faqs 
    join konyadminconsole.faqcategory on(
      (
        konyadminconsole.faqcategory.id = konyadminconsole.faqs.FaqCategory_Id
      )
    )
  );

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[groups_view] AS 
select 
  konyadminconsole.membergroup.id AS Group_id, 
  konyadminconsole.membergroup.Description AS Group_Desc, 
  konyadminconsole.membergroup.Status_id AS Status_id, 
  konyadminconsole.membergroup.Name AS Group_Name, 
  (
    select 
      count(konyadminconsole.groupentitlement.Group_id) 
    from 
      konyadminconsole.groupentitlement 
    where 
      (
        konyadminconsole.groupentitlement.Group_id = konyadminconsole.membergroup.id
      )
  ) AS Entitlements_Count, 
  (
    select 
      count(konyadminconsole.customergroup.Customer_id) 
    from 
      konyadminconsole.customergroup 
    where 
      (
        konyadminconsole.customergroup.Group_id = konyadminconsole.membergroup.id
      )
  ) AS Customers_Count, (
    case membergroup.Status_id when 'SID_ACTIVE' then 'Active' else 'Inactive' end
  ) AS Status
from 
  konyadminconsole.membergroup;


GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[groupservices_view] AS 
select 
  konyadminconsole.groupentitlement.Group_id AS Group_id, 
  konyadminconsole.groupentitlement.Service_id AS Service_id, 
  konyadminconsole.service.Name AS Service_name, 
  konyadminconsole.service.Description AS Service_description, 
  konyadminconsole.service.Notes AS Service_notes, 
  konyadminconsole.service.Status_id AS Status_id, 
  (
    select 
      konyadminconsole.status.Description 
    from 
      konyadminconsole.status 
    where 
      (konyadminconsole.status.id = konyadminconsole.service.Status_id)
  ) AS Status_description, 
  konyadminconsole.groupentitlement.TransactionFee_id AS TransactionFee_id, 
  (
    select 
      konyadminconsole.transactionfee.Description 
    from 
      konyadminconsole.transactionfee 
    where 
      (
        konyadminconsole.transactionfee.id = konyadminconsole.groupentitlement.TransactionFee_id
      )
  ) AS TransactionFee_description, 
  konyadminconsole.groupentitlement.TransactionLimit_id AS TransactionLimit_id, 
  (
    select 
      konyadminconsole.transactionlimit.Description 
    from 
      konyadminconsole.transactionlimit 
    where 
      (
        konyadminconsole.transactionlimit.id = konyadminconsole.groupentitlement.TransactionLimit_id
      )
  ) AS TransactionLimit_description, 
  konyadminconsole.service.Type_id AS ServiceType_id, 
  (
    select 
      konyadminconsole.servicetype.Description 
    from 
      konyadminconsole.servicetype 
    where 
      (konyadminconsole.servicetype.id = konyadminconsole.service.Type_id)
  ) AS ServiceType_description, 
  konyadminconsole.service.Channel_id AS Channel_id, 
  (
    select 
      konyadminconsole.servicechannel.Description 
    from 
      konyadminconsole.servicechannel 
    where 
      (
        konyadminconsole.servicechannel.id = konyadminconsole.service.Channel_id
      )
  ) AS ChannelType_description, 
  konyadminconsole.service.MinTransferLimit AS MinTransferLimit, 
  konyadminconsole.service.MaxTransferLimit AS MaxTransferLimit, 
  konyadminconsole.service.DisplayName AS Display_Name, 
  konyadminconsole.service.DisplayDescription AS Display_Description 
from 
  (
    konyadminconsole.groupentitlement 
    left join konyadminconsole.service on(
      (
        konyadminconsole.groupentitlement.Service_id = konyadminconsole.service.id
      )
    )
  );


GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[internalusers_view] AS 
select 
  konyadminconsole.systemuser.id AS User_id, 
  konyadminconsole.systemuser.Status_id AS Status_id, 
  konyadminconsole.status.Description AS Status_Desc, 
  konyadminconsole.systemuser.FirstName AS FirstName, 
  konyadminconsole.systemuser.MiddleName AS MiddleName, 
  konyadminconsole.systemuser.LastName AS LastName, 
  konyadminconsole.systemuser.lastLogints AS lastLogints, 
  concat(
    konyadminconsole.systemuser.FirstName, ' ', konyadminconsole.systemuser.LastName
  ) AS Name, 
  konyadminconsole.systemuser.Username AS Username, 
  konyadminconsole.systemuser.Email AS Email, 
  (
    select 
      konyadminconsole.userrole.Role_id 
    from 
      konyadminconsole.userrole 
    where 
      (konyadminconsole.systemuser.id = konyadminconsole.userrole.User_id)
  ) AS Role_id, 
  (
    select 
      konyadminconsole.role.Description 
    from 
      konyadminconsole.role 
    where 
      konyadminconsole.role.id in (
        select 
          konyadminconsole.userrole.Role_id 
        from 
          konyadminconsole.userrole 
        where 
          (konyadminconsole.systemuser.id = konyadminconsole.userrole.User_id)
      )
  ) AS Role_Desc, 
  (
    select 
      konyadminconsole.role.Name 
    from 
      konyadminconsole.role 
    where 
      konyadminconsole.role.id in (
        select 
          konyadminconsole.userrole.Role_id 
        from 
          konyadminconsole.userrole 
        where 
          (konyadminconsole.systemuser.id = konyadminconsole.userrole.User_id)
      )
  ) AS Role_Name, 
  (
    (
      select 
        count(konyadminconsole.userpermission.Permission_id) 
      from 
        konyadminconsole.userpermission 
      where 
        (
          konyadminconsole.systemuser.id = konyadminconsole.userpermission.User_id
        )
    ) + (
      select 
        count(konyadminconsole.rolepermission.Permission_id) 
      from 
        konyadminconsole.rolepermission 
      where 
        konyadminconsole.rolepermission.Role_id in (
          select 
            konyadminconsole.userrole.Role_id 
          from 
            konyadminconsole.userrole 
          where 
            (konyadminconsole.systemuser.id = konyadminconsole.userrole.User_id)
        )
    )
  ) AS Permission_Count, 
  konyadminconsole.systemuser.lastmodifiedts AS lastmodifiedts, 
  konyadminconsole.systemuser.createdts AS createdts, 
  (
    select 
      concat(
        workaddress.AddressLine1, 
        ', ', 
        case when workaddress.AddressLine2 is null then '' else null end, 
        ', ', 
        (
          select 
            konyadminconsole.city.Name 
          from 
            konyadminconsole.city 
          where 
            (konyadminconsole.city.id = workaddress.City_id)
        ), 
        ', ', 
        (
          select 
            konyadminconsole.region.Name 
          from 
            konyadminconsole.region 
          where 
            (
              konyadminconsole.region.id = workaddress.Region_id
            )
        ), 
        ', ', 
        (
          select 
            konyadminconsole.country.Name 
          from 
            konyadminconsole.country 
          where 
            konyadminconsole.country.id in (
              select 
                konyadminconsole.city.Country_id 
              from 
                konyadminconsole.city 
              where 
                (konyadminconsole.city.id = workaddress.City_id)
            )
        ), 
        ', ', 
        workaddress.ZipCode
     )
  ) AS Work_Addr, 
  (
    select 
      concat(
        homeaddress.AddressLine1, 
        ', ', 
        case when homeaddress.AddressLine2 is null then '' else null end, 
        ', ', 
        (
          select 
            konyadminconsole.city.Name 
          from 
            konyadminconsole.city 
          where 
            (konyadminconsole.city.id = homeaddress.City_id)
        ), 
        ', ', 
        (
          select 
            konyadminconsole.region.Name 
          from 
            konyadminconsole.region 
          where 
            (
              konyadminconsole.region.id = homeaddress.Region_id
            )
        ), 
        ', ', 
        (
          select 
            konyadminconsole.country.Name 
          from 
            konyadminconsole.country 
          where 
            konyadminconsole.country.id in (
              select 
                konyadminconsole.city.Country_id 
              from 
                konyadminconsole.city 
              where 
                (konyadminconsole.city.id = homeaddress.City_id)
            )
        ), 
        ', ', 
        homeaddress.ZipCode
      )
  ) AS Home_Addr, 
  (
    select 
      case when homeaddress.id is null then '' else null end
  ) AS Home_AddressID, 
  (
    select 
      case when homeaddress.AddressLine1 is null then '' else null end
  ) AS Home_AddressLine1, 
  (
    select 
      case when homeaddress.AddressLine2 is null then '' else null end
 ) AS Home_AddressLine2, 
  (
    select 
      case when (
        select 
          city.Name 
        from 
          konyadminconsole.city 
        where 
          (konyadminconsole.city.id = homeaddress.City_id)
      ) is null then '' else null end
  ) AS Home_CityName, 
  (
    select 
      case when homeaddress.City_id is null then '' else null end
  ) AS Home_CityID, 
  (
    select 
      case when (
        select 
          konyadminconsole.region.Name 
        from 
          konyadminconsole.region 
        where 
          (
            konyadminconsole.region.id = homeaddress.Region_id
          )
      ) is null then '' else null end
  ) AS Home_StateName, 
  (
    select 
      case when homeaddress.Region_id is null then '' else null end
  ) AS Home_StateID, 
  (
    select 
      case when (
        select 
          konyadminconsole.country.Name 
        from 
          konyadminconsole.country 
        where 
          konyadminconsole.country.id in (
            select 
              konyadminconsole.city.Country_id 
            from 
              konyadminconsole.city 
            where 
              (konyadminconsole.city.id = homeaddress.City_id)
          )
      ) is null then '' else null end
  ) AS Home_CountryName, 
  (
    select 
      case when (
        select 
          konyadminconsole.country.id 
        from 
          konyadminconsole.country 
        where 
          konyadminconsole.country.id in (
            select 
              konyadminconsole.city.Country_id 
            from 
              konyadminconsole.city 
            where 
              (konyadminconsole.city.id = homeaddress.City_id)
          )
      ) is null then '' else null end
  ) AS Home_CountryID, 
  (
    select 
      case when homeaddress.ZipCode is null then '' else null end
  ) AS Home_Zipcode, 
  (
    select 
      case when workaddress.id is null then '' else null end
  ) AS Work_AddressID, 
  (
    select 
      case when workaddress.AddressLine1 is null then '' else null end
  ) AS Work_AddressLine1, 
  (
    select 
      case when workaddress.AddressLine2 is null then '' else null end
  ) AS Work_AddressLine2, 
  (
    select 
      case when (
        select 
          konyadminconsole.city.Name 
        from 
          konyadminconsole.city 
        where 
          (konyadminconsole.city.id = workaddress.City_id)
      ) is null then '' else null end
  ) AS Work_CityName, 
  (
    select 
      case when workaddress.City_id is null then '' else null end
  ) AS Work_CityID, 
  (
    select 
      case when (
        select 
          konyadminconsole.region.Name 
        from 
          konyadminconsole.region 
        where 
          (
            konyadminconsole.region.id = workaddress.Region_id
          )
      ) is null then '' else null end
  ) AS Work_StateName, 
  (
    select 
      case when workaddress.Region_id is null then '' else null end
  ) AS Work_StateID, 
  (
    select 
      case when (
        select 
          konyadminconsole.country.Name 
        from 
          konyadminconsole.country 
        where 
          konyadminconsole.country.id in (
            select 
              konyadminconsole.city.Country_id 
            from 
              konyadminconsole.city 
            where 
              (konyadminconsole.city.id = workaddress.City_id)
          )
      ) is null then '' else null end
  ) AS Work_CountryName, 
  (
    select 
      case when (
        select 
          konyadminconsole.country.id 
        from 
          konyadminconsole.country 
        where 
          konyadminconsole.country.id in (
            select 
              konyadminconsole.city.Country_id 
            from 
              konyadminconsole.city 
            where 
              (konyadminconsole.city.id = workaddress.City_id)
          )
      ) is null then '' else null end
  ) AS Work_CountryID, 
  (
    select 
      case when workaddress.ZipCode is null then '' else null end
  ) AS Work_Zipcode 
from 
  (
    (
      (
        konyadminconsole.systemuser 
        left join konyadminconsole.status on(
          (konyadminconsole.systemuser.Status_id = konyadminconsole.status.id)
        )
      ) 
      left join konyadminconsole.address homeaddress on(
        homeaddress.id in (
          select 
            useraddress.Address_id 
          from 
            useraddress 
          where 
            (
              (
                konyadminconsole.systemuser.id = useraddress.User_id
              ) 
              and (
                useraddress.Type_id = 'ADR_TYPE_HOME'
              )
            )
        )
      )
    ) 
    left join konyadminconsole.address workaddress on(
      workaddress.id in (
        select 
          useraddress.Address_id 
        from 
          useraddress 
        where 
          (
            (
              konyadminconsole.systemuser.id = useraddress.User_id
            ) 
            and (
              useraddress.Type_id = 'ADR_TYPE_WORK'
            )
          )
      )
    )
  );



GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[permissions_view] AS 
select 
  konyadminconsole.permission.id AS Permission_id, 
  konyadminconsole.permission.Type_id AS PermissionType_id, 
  konyadminconsole.permission.Name AS Permission_Name, 
  konyadminconsole.permission.Description AS Permission_Desc, 
  konyadminconsole.permission.Status_id AS Status_id, 
  konyadminconsole.status.Description AS Status_Desc, 
  (
    select 
      count(konyadminconsole.rolepermission.Role_id) 
    from 
      konyadminconsole.rolepermission 
    where 
      (
        konyadminconsole.rolepermission.Permission_id = konyadminconsole.permission.id
      )
  ) AS Role_Count, 
  (
    (
      select 
        count(konyadminconsole.userpermission.Permission_id) 
      from 
        konyadminconsole.userpermission 
      where 
        (
          konyadminconsole.userpermission.Permission_id = konyadminconsole.permission.id
        )
    ) + (
      select 
        count(konyadminconsole.userrole.User_id) 
      from 
        konyadminconsole.userrole 
      where 
        konyadminconsole.userrole.Role_id in (
          select 
            konyadminconsole.rolepermission.Role_id 
          from 
            konyadminconsole.rolepermission 
          where 
            (
             konyadminconsole.rolepermission.Permission_id = konyadminconsole.permission.id
            )
        )
    )
  ) AS Users_Count, (
    case konyadminconsole.permission.Status_id when 'SID_ACTIVE' then 'Active' else 'Inactive' end
  ) AS Status 
from 
  (
    konyadminconsole.permission 
    left join konyadminconsole.status on(
      (konyadminconsole.permission.Status_id = konyadminconsole.status.id)
    )
  );


GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[roles_view] AS 
select 
  konyadminconsole.role.id AS role_id, 
  konyadminconsole.role.Type_id AS roleType_id, 
  konyadminconsole.role.Name AS role_Name, 
  konyadminconsole.role.Description AS role_Desc, 
  konyadminconsole.role.Status_id AS Status_id, 
  konyadminconsole.status.Description AS Status_Desc, 
  (
    select 
      count(konyadminconsole.rolepermission.Role_id) 
    from 
      konyadminconsole.rolepermission 
    where 
      (konyadminconsole.rolepermission.Role_id = konyadminconsole.role.id)
  ) AS permission_Count, 
  (
    select 
      count(konyadminconsole.userrole.User_id) 
    from 
      konyadminconsole.userrole 
    where 
      konyadminconsole.userrole.Role_id in (
        select 
          konyadminconsole.rolepermission.Role_id 
        from 
          konyadminconsole.rolepermission 
        where 
          (konyadminconsole.rolepermission.Role_id = konyadminconsole.role.id)
      )
  ) AS Users_Count, (
    case konyadminconsole.role.Status_id when 'SID_ACTIVE' then 'Active' else 'Inactive' end
  ) AS Status  
from 
  (
    konyadminconsole.role 
    left join konyadminconsole.status on(
      (konyadminconsole.role.Status_id = konyadminconsole.status.id)
    )
  );

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[security_images_view] AS 
select 
  konyadminconsole.securityimage.id AS SecurityImage_id, 
  konyadminconsole.securityimage.Image AS SecurityImageBase64String, 
  konyadminconsole.securityimage.Status_id AS SecurityImage_Status, 
  (
    select 
      count(
        konyadminconsole.customersecurityimages.Customer_id
      ) 
    from 
      konyadminconsole.customersecurityimages 
    where 
      (
        konyadminconsole.customersecurityimages.Image_id = konyadminconsole.securityimage.id
      )
  ) AS UserCount, 
  konyadminconsole.securityimage.softdeleteflag AS softdeleteflag 
from 
  konyadminconsole.securityimage;


GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[security_questions_view] AS 
select 
  konyadminconsole.securityquestion.id AS SecurityQuestion_id, 
  konyadminconsole.securityquestion.Question AS SecurityQuestion, 
  konyadminconsole.securityquestion.Status_id AS SecurityQuestion_Status, 
  konyadminconsole.securityquestion.lastmodifiedts AS lastmodifiedts, 
  (
    select 
      count(
        konyadminconsole.customersecurityquestions.Customer_id
      ) 
    from 
      konyadminconsole.customersecurityquestions 
    where 
      (
        konyadminconsole.customersecurityquestions.SecurityQuestion_id = konyadminconsole.securityquestion.id
      )
  ) AS UserCount, 
  konyadminconsole.securityquestion.softdeleteflag AS softdeleteflag 
from 
  konyadminconsole.securityquestion;

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[applicant_view] (
   [Application_id], 
   [FirstName], 
   [MiddleName], 
   [LastName], 
   [DocumentsSubmitted], 
   [ContactNumber], 
   [EmailID], 
   [applicant_createdby], 
   [applicant_modifiedby], 
   [applicant_createdts], 
   [applicant_lastmodifiedts], 
   [Channel], 
   [Status], 
   [Product])
AS 
   SELECT 
      konyadminconsole.applicant.id AS Application_id, 
      konyadminconsole.applicant.FirstName AS FirstName, 
      konyadminconsole.applicant.MiddleName AS MiddleName, 
      konyadminconsole.applicant.LastName AS LastName, 
      konyadminconsole.applicant.DocumentsSubmitted AS DocumentsSubmitted, 
      konyadminconsole.applicant.ContactNumber AS ContactNumber, 
      konyadminconsole.applicant.EmailID AS EmailID, 
      konyadminconsole.applicant.createdby AS applicant_createdby, 
      konyadminconsole.applicant.modifiedby AS applicant_modifiedby, 
      konyadminconsole.applicant.createdts AS applicant_createdts, 
      konyadminconsole.applicant.lastmodifiedts AS applicant_lastmodifiedts, 
      konyadminconsole.applicant.Channel AS Channel, 
      konyadminconsole.applicant.Status AS Status, 
      konyadminconsole.applicant.Product AS Product
   FROM konyadminconsole.applicant

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[applicantnotes_view] (
   [id], 
   [Note], 
   [applicant_id], 
   [applicant_FirstName], 
   [applicant_MiddleName], 
   [applicant_LastName], 
   [applicant_Status], 
   [InternalUser_id], 
   [InternalUser_Username], 
   [InternalUser_FirstName], 
   [InternalUser_LastName], 
   [InternalUser_MiddleName], 
   [InternalUser_Email], 
   [createdts], 
   [synctimestamp], 
   [softdeleteflag])
AS 
   SELECT 
      konyadminconsole.applicantnote.id AS id, 
      konyadminconsole.applicantnote.Note AS Note, 
      konyadminconsole.applicantnote.Applicant_id AS applicant_id, 
      konyadminconsole.applicant.FirstName AS applicant_FirstName, 
      konyadminconsole.applicant.MiddleName AS applicant_MiddleName, 
      konyadminconsole.applicant.LastName AS applicant_LastName, 
      konyadminconsole.applicant.Status AS applicant_Status, 
      konyadminconsole.applicantnote.createdby AS InternalUser_id, 
      konyadminconsole.systemuser.Username AS InternalUser_Username, 
      konyadminconsole.systemuser.FirstName AS InternalUser_FirstName, 
      konyadminconsole.systemuser.LastName AS InternalUser_LastName, 
      konyadminconsole.systemuser.MiddleName AS InternalUser_MiddleName, 
      konyadminconsole.systemuser.Email AS InternalUser_Email, 
      konyadminconsole.applicantnote.createdts AS createdts, 
      konyadminconsole.applicantnote.synctimestamp AS synctimestamp, 
      konyadminconsole.applicantnote.softdeleteflag AS softdeleteflag
   FROM ((konyadminconsole.applicantnote 
      LEFT JOIN konyadminconsole.systemuser 
      ON ((konyadminconsole.applicantnote.createdby = konyadminconsole.systemuser.id))) 
      LEFT JOIN konyadminconsole.applicant 
      ON ((konyadminconsole.applicantnote.Applicant_id = konyadminconsole.applicant.id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[archivedcustomer_request_detailed_view] (
   [customerrequest_id], 
   [customerrequest_RequestCategory_id], 
   [customerrequest_lastupdatedbycustomer], 
   [requestcategory_Name], 
   [customerrequest_Customer_id],
   [customer_FirstName], 
   [customer_MiddleName], 
   [customer_Fullname], 
   [customer_LastName], 
   [customer_Username], 
   [customer_Salutation], 
   [customer_Gender], 
   [customer_DateOfBirth], 
   [customer_Status_id], 
   [customer_Ssn], 
   [customer_MaritalStatus_id], 
   [customer_SpouseName], 
   [customer_EmployementStatus_id], 
   [customer_IsEnrolledForOlb], 
   [customer_IsStaffMember], 
   [customer_Location_id], 
   [customer_PreferredContactMethod], 
   [customer_PreferredContactTime], 
   [customerrequest_Priority], 
   [customerrequest_Status_id], 
   [customerrequest_AssignedTo], 
   [customerrequest_RequestSubject], 
   [customerrequest_Accountid], 
   [customerrequest_createdby], 
   [customerrequest_modifiedby], 
   [customerrequest_createdts], 
   [customerrequest_lastmodifiedts], 
   [customerrequest_synctimestamp], 
   [customerrequest_softdeleteflag], 
   [requestmessage_id], 
   [requestmessage_RepliedBy], 
   [requestmessage_RepliedBy_Name], 
   [requestmessage_MessageDescription], 
   [requestmessage_ReplySequence], 
   [requestmessage_IsRead], 
   [requestmessage_createdby], 
   [requestmessage_modifiedby], 
   [requestmessage_createdts], 
   [requestmessage_lastmodifiedts], 
   [requestmessage_synctimestamp], 
   [requestmessage_softdeleteflag], 
   [messageattachment_id], 
   [messageattachment_AttachmentType_id], 
   [messageattachment_Media_id], 
   [messageattachment_createdby], 
   [messageattachment_modifiedby], 
   [messageattachment_createdts], 
   [messageattachment_lastmodifiedts], 
   [messageattachment_softdeleteflag], 
   [media_id], 
   [media_Name], 
   [media_Size], 
   [media_Type], 
   [media_Description], 
   [media_Url], 
   [media_createdby], 
   [media_modifiedby], 
   [media_lastmodifiedts], 
   [media_synctimestamp], 
   [media_softdeleteflag])
AS 
   SELECT 
      konyadminconsole.archivedcustomerrequest.id AS customerrequest_id, 
      konyadminconsole.archivedcustomerrequest.RequestCategory_id AS customerrequest_RequestCategory_id, 
      konyadminconsole.archivedcustomerrequest.lastupdatedbycustomer AS customerrequest_lastupdatedbycustomer, 
      konyadminconsole.requestcategory.Name AS requestcategory_Name, 
      konyadminconsole.archivedcustomerrequest.Customer_id AS customerrequest_Customer_id,
      konyadminconsole.customer.FirstName AS customer_FirstName, 
      konyadminconsole.customer.MiddleName AS customer_MiddleName, 
      konyadminconsole.customer.FirstName + customer.LastName AS customer_Fullname, 
      konyadminconsole.customer.LastName AS customer_LastName, 
      konyadminconsole.customer.Username AS customer_Username, 
      konyadminconsole.customer.Salutation AS customer_Salutation, 
      konyadminconsole.customer.Gender AS customer_Gender, 
      konyadminconsole.customer.DateOfBirth AS customer_DateOfBirth, 
      konyadminconsole.customer.Status_id AS customer_Status_id, 
      konyadminconsole.customer.Ssn AS customer_Ssn, 
      konyadminconsole.customer.MaritalStatus_id AS customer_MaritalStatus_id, 
      konyadminconsole.customer.SpouseName AS customer_SpouseName, 
      konyadminconsole.customer.EmployementStatus_id AS customer_EmployementStatus_id, 
      konyadminconsole.customer.IsEnrolledForOlb AS customer_IsEnrolledForOlb, 
      konyadminconsole.customer.IsStaffMember AS customer_IsStaffMember, 
      konyadminconsole.customer.Location_id AS customer_Location_id, 
      konyadminconsole.customer.PreferredContactMethod AS customer_PreferredContactMethod, 
      konyadminconsole.customer.PreferredContactTime AS customer_PreferredContactTime, 
      konyadminconsole.archivedcustomerrequest.Priority AS customerrequest_Priority, 
      konyadminconsole.archivedcustomerrequest.Status_id AS customerrequest_Status_id, 
      konyadminconsole.archivedcustomerrequest.AssignedTo AS customerrequest_AssignedTo, 
      konyadminconsole.archivedcustomerrequest.RequestSubject AS customerrequest_RequestSubject, 
      konyadminconsole.archivedcustomerrequest.Accountid AS customerrequest_Accountid, 
      konyadminconsole.archivedcustomerrequest.createdby AS customerrequest_createdby, 
      konyadminconsole.archivedcustomerrequest.modifiedby AS customerrequest_modifiedby, 
      konyadminconsole.archivedcustomerrequest.createdts AS customerrequest_createdts, 
      konyadminconsole.archivedcustomerrequest.lastmodifiedts AS customerrequest_lastmodifiedts, 
      konyadminconsole.archivedcustomerrequest.synctimestamp AS customerrequest_synctimestamp, 
      konyadminconsole.archivedcustomerrequest.softdeleteflag AS customerrequest_softdeleteflag, 
      konyadminconsole.archivedrequestmessage.id AS requestmessage_id, 
      konyadminconsole.archivedrequestmessage.RepliedBy AS requestmessage_RepliedBy, 
      konyadminconsole.archivedrequestmessage.RepliedBy_Name AS requestmessage_RepliedBy_Name, 
      konyadminconsole.archivedrequestmessage.MessageDescription AS requestmessage_MessageDescription, 
      konyadminconsole.archivedrequestmessage.ReplySequence AS requestmessage_ReplySequence, 
      konyadminconsole.archivedrequestmessage.IsRead AS requestmessage_IsRead, 
      konyadminconsole.archivedrequestmessage.createdby AS requestmessage_createdby, 
      konyadminconsole.archivedrequestmessage.modifiedby AS requestmessage_modifiedby, 
      konyadminconsole.archivedrequestmessage.createdts AS requestmessage_createdts, 
      konyadminconsole.archivedrequestmessage.lastmodifiedts AS requestmessage_lastmodifiedts, 
      konyadminconsole.archivedrequestmessage.synctimestamp AS requestmessage_synctimestamp, 
      konyadminconsole.archivedrequestmessage.softdeleteflag AS requestmessage_softdeleteflag, 
      konyadminconsole.archivedmessageattachment.id AS messageattachment_id, 
      konyadminconsole.archivedmessageattachment.AttachmentType_id AS messageattachment_AttachmentType_id, 
      konyadminconsole.archivedmessageattachment.Media_id AS messageattachment_Media_id, 
      konyadminconsole.archivedmessageattachment.createdby AS messageattachment_createdby, 
      konyadminconsole.archivedmessageattachment.modifiedby AS messageattachment_modifiedby, 
      konyadminconsole.archivedmessageattachment.createdts AS messageattachment_createdts, 
      konyadminconsole.archivedmessageattachment.lastmodifiedts AS messageattachment_lastmodifiedts, 
      konyadminconsole.archivedmessageattachment.softdeleteflag AS messageattachment_softdeleteflag, 
      konyadminconsole.archivedmedia.id AS media_id, 
      konyadminconsole.archivedmedia.Name AS media_Name, 
      konyadminconsole.archivedmedia.Size AS media_Size, 
      konyadminconsole.archivedmedia.Type AS media_Type, 
      konyadminconsole.archivedmedia.Description AS media_Description, 
      konyadminconsole.archivedmedia.Url AS media_Url, 
      konyadminconsole.archivedmedia.createdby AS media_createdby, 
      konyadminconsole.archivedmedia.modifiedby AS media_modifiedby, 
      konyadminconsole.archivedmedia.lastmodifiedts AS media_lastmodifiedts, 
      konyadminconsole.archivedmedia.synctimestamp AS media_synctimestamp, 
      konyadminconsole.archivedmedia.softdeleteflag AS media_softdeleteflag
   FROM (((((konyadminconsole.archivedcustomerrequest 
      LEFT JOIN konyadminconsole.archivedrequestmessage 
      ON ((konyadminconsole.archivedcustomerrequest.id = konyadminconsole.archivedrequestmessage.CustomerRequest_id))) 
      LEFT JOIN konyadminconsole.archivedmessageattachment 
      ON ((konyadminconsole.archivedrequestmessage.id = konyadminconsole.archivedmessageattachment.RequestMessage_id))) 
      LEFT JOIN konyadminconsole.archivedmedia 
      ON ((konyadminconsole.archivedmessageattachment.Media_id = konyadminconsole.archivedmedia.id))) 
      LEFT JOIN konyadminconsole.customer 
      ON ((konyadminconsole.archivedcustomerrequest.Customer_id = konyadminconsole.customer.id))) 
      LEFT JOIN konyadminconsole.requestcategory 
      ON ((konyadminconsole.archivedcustomerrequest.RequestCategory_id = konyadminconsole.requestcategory.id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[bankfortransfer_view] (
   [id], 
   [Bank_Code], 
   [Bank_Name], 
   [Logo], 
   [Status_id], 
   [Bank_Url], 
   [Address_id], 
   [AddressLine1], 
   [AddressLine2], 
   [City_id], 
   [City_Name], 
   [Region_id], 
   [Region_Name], 
   [Country_id], 
   [Country_Name], 
   [Routing_Code], 
   [Routing_Number], 
   [Service_id], 
   [Service_Name])
AS 
   SELECT 
      konyadminconsole.bankfortransfer.id AS id, 
      konyadminconsole.bankfortransfer.Code AS Bank_Code, 
      konyadminconsole.bankfortransfer.Name AS Bank_Name, 
      konyadminconsole.bankfortransfer.Logo AS Logo, 
      konyadminconsole.bankfortransfer.Status_id AS Status_id, 
      konyadminconsole.bankfortransfer.Url AS Bank_Url, 
      konyadminconsole.bankfortransfer.Address_id AS Address_id, 
      konyadminconsole.address.AddressLine1 AS AddressLine1, 
      konyadminconsole.address.AddressLine2 AS AddressLine2, 
      konyadminconsole.address.City_id AS City_id, 
      konyadminconsole.city.Name AS City_Name, 
      konyadminconsole.region.id AS Region_id, 
      konyadminconsole.region.Name AS Region_Name, 
      konyadminconsole.region.Country_id AS Country_id, 
      konyadminconsole.country.Name AS Country_Name, 
      konyadminconsole.bankservice.RoutingCode AS Routing_Code, 
      konyadminconsole.bankservice.RoutingNumber AS Routing_Number, 
      konyadminconsole.bankservice.Service_id AS Service_id, 
      konyadminconsole.service.Name AS Service_Name
   FROM ((((((konyadminconsole.bankfortransfer 
      LEFT JOIN konyadminconsole.address 
      ON ((konyadminconsole.bankfortransfer.Address_id = konyadminconsole.address.id))) 
      LEFT JOIN konyadminconsole.bankservice 
      ON ((konyadminconsole.bankfortransfer.id = konyadminconsole.bankservice.BankForTransfer_id))) 
      LEFT JOIN konyadminconsole.city 
      ON ((konyadminconsole.address.City_id = konyadminconsole.city.id))) 
      LEFT JOIN konyadminconsole.region 
      ON ((konyadminconsole.address.Region_id = konyadminconsole.region.id))) 
      LEFT JOIN konyadminconsole.country 
      ON ((konyadminconsole.region.Country_id = konyadminconsole.country.id))) 
      LEFT JOIN konyadminconsole.service 
      ON ((konyadminconsole.bankservice.Service_id = konyadminconsole.service.id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[customer_communication_view] (
   [customer_id], 
   [customer_FirstName], 
   [customer_MiddleName], 
   [customer_LastName], 
   [customer_Username], 
   [customer_Salutation], 
   [customer_Gender], 
   [customer_DateOfBirth], 
   [customer_Status_id], 
   [customer_Ssn], 
   [customer_MaritalStatus_id], 
   [customer_SpouseName], 
   [customer_EmployementStatus_id], 
   [customer_IsEnrolledForOlb], 
   [customer_IsStaffMember], 
   [customer_Location_id], 
   [customer_PreferredContactMethod], 
   [customer_PreferredContactTime], 
   [customercommunication_id], 
   [customercommunication_Type_id], 
   [customercommunication], 
   [customercommunication_Value], 
   [customercommunication_Extension], 
   [customercommunication_Description], 
   [customercommunication_createdby], 
   [customercommunication_modifiedby], 
   [customercommunication_createdts], 
   [customercommunication_lastmodifiedts], 
   [customercommunication_synctimestamp], 
   [customercommunication_softdeleteflag])
AS 
   SELECT 
      konyadminconsole.customer.id AS customer_id, 
      konyadminconsole.customer.FirstName AS customer_FirstName, 
      konyadminconsole.customer.MiddleName AS customer_MiddleName, 
      konyadminconsole.customer.LastName AS customer_LastName, 
      konyadminconsole.customer.Username AS customer_Username, 
      konyadminconsole.customer.Salutation AS customer_Salutation, 
      konyadminconsole.customer.Gender AS customer_Gender, 
      konyadminconsole.customer.DateOfBirth AS customer_DateOfBirth, 
      konyadminconsole.customer.Status_id AS customer_Status_id, 
      konyadminconsole.customer.Ssn AS customer_Ssn, 
      konyadminconsole.customer.MaritalStatus_id AS customer_MaritalStatus_id, 
      konyadminconsole.customer.SpouseName AS customer_SpouseName, 
      konyadminconsole.customer.EmployementStatus_id AS customer_EmployementStatus_id, 
      konyadminconsole.customer.IsEnrolledForOlb AS customer_IsEnrolledForOlb, 
      konyadminconsole.customer.IsStaffMember AS customer_IsStaffMember, 
      konyadminconsole.customer.Location_id AS customer_Location_id, 
      konyadminconsole.customer.PreferredContactMethod AS customer_PreferredContactMethod, 
      konyadminconsole.customer.PreferredContactTime AS customer_PreferredContactTime, 
      konyadminconsole.customercommunication.id AS customercommunication_id, 
      konyadminconsole.customercommunication.Type_id AS customercommunication_Type_id, 
      konyadminconsole.customercommunication.isPrimary AS customercommunication, 
      konyadminconsole.customercommunication.Value AS customercommunication_Value, 
      konyadminconsole.customercommunication.Extension AS customercommunication_Extension, 
      konyadminconsole.customercommunication.Description AS customercommunication_Description, 
      konyadminconsole.customercommunication.createdby AS customercommunication_createdby, 
      konyadminconsole.customercommunication.modifiedby AS customercommunication_modifiedby, 
      konyadminconsole.customercommunication.createdts AS customercommunication_createdts, 
      konyadminconsole.customercommunication.lastmodifiedts AS customercommunication_lastmodifiedts, 
      konyadminconsole.customercommunication.synctimestamp AS customercommunication_synctimestamp, 
      konyadminconsole.customercommunication.softdeleteflag AS customercommunication_softdeleteflag
   FROM (konyadminconsole.customer 
      LEFT JOIN konyadminconsole.customercommunication 
      ON ((konyadminconsole.customer.id = konyadminconsole.customercommunication.Customer_id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[branch_view] (
   [Branch_id], 
   [Branch_Typeid], 
   [Branch_Name], 
   [Branch_DisplayName], 
   [Branch_Description], 
   [Branch_Code], 
   [Branch_PhoneNumber], 
   [Branch_EmailId], 
   [Branch_Status_id], 
   [Branch_IsMainBranch], 
   [Branch_WorkSchedule_id], 
   [Branch_MainBranchCode], 
   [Branch_WebSiteUrl], 
   [City_id], 
   [City_Name], 
   [Address_id], 
   [Branch_Complete_Addr], 
   [Branch_createdby], 
   [Branch_modifiedby], 
   [Branch_createdts], 
   [Branch_lastmodifiedts], 
   [Branch_synctimestamp])
AS 
   SELECT 
      loc.id AS Branch_id, 
      ISNULL(loc.Type_id, N'') AS Branch_Typeid, 
      ISNULL(loc.Name, N'') AS Branch_Name, 
      ISNULL(loc.DisplayName, N'') AS Branch_DisplayName, 
      ISNULL(loc.Description, N'') AS Branch_Description, 
      ISNULL(loc.Code, N'') AS Branch_Code, 
      ISNULL(loc.PhoneNumber, N'') AS Branch_PhoneNumber, 
      ISNULL(loc.EmailId, N'') AS Branch_EmailId, 
      ISNULL(loc.Status_id, N'') AS Branch_Status_id, 
      ISNULL(CAST(loc.IsMainBranch AS varchar(50)), N'') AS Branch_IsMainBranch, 
      ISNULL(loc.WorkSchedule_id, N'') AS Branch_WorkSchedule_id, 
      ISNULL(loc.MainBranchCode, N'') AS Branch_MainBranchCode, 
      ISNULL(loc.WebSiteUrl, N'') AS Branch_WebSiteUrl, 
      konyadminconsole.address.City_id AS City_id, 
      konyadminconsole.city.Name AS City_Name, 
      loc.Address_id AS Address_id, 
      
         konyadminconsole.address.AddressLine1
          + 
         N', '
          + 
         ISNULL(konyadminconsole.address.AddressLine2, N'')
          + 
         N', '
          + 
         konyadminconsole.city.Name
          + 
         N', '
          + 
         konyadminconsole.region.Name
          + 
         N', '
          + 
         konyadminconsole.country.Name
          + 
         N', '
          + 
         ISNULL(konyadminconsole.address.ZipCode, N'') AS Branch_Complete_Addr, 
      ISNULL(loc.createdby, N'') AS Branch_createdby, 
      ISNULL(loc.modifiedby, N'') AS Branch_modifiedby, 
      ISNULL(loc.createdts, NULL) AS Branch_createdts, 
      ISNULL(loc.lastmodifiedts, NULL) AS Branch_lastmodifiedts, 
      ISNULL(loc.synctimestamp, NULL) AS Branch_synctimestamp
   FROM ((((konyadminconsole.location  AS loc 
      LEFT JOIN konyadminconsole.address 
      ON (((loc.Address_id = konyadminconsole.address.id) AND (loc.Type_id = 'Branch')))) 
      LEFT JOIN konyadminconsole.city 
      ON ((konyadminconsole.city.id = konyadminconsole.address.City_id))) 
      LEFT JOIN konyadminconsole.region 
      ON ((konyadminconsole.region.id = konyadminconsole.address.Region_id))) 
      LEFT JOIN konyadminconsole.country 
      ON ((konyadminconsole.city.Country_id = konyadminconsole.country.id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[customer_indirect_permissions_view] (
   [Customer_id], 
   [Group_id], 
   [Group_name], 
   [Group_desc], 
   [Service_id], 
   [Service_Name], 
   [Service_Description], 
   [Service_Status_id], 
   [Service_DisplayName], 
   [Service_DisplayDescription])
AS 
   SELECT 
      konyadminconsole.customergroup.Customer_id AS Customer_id, 
      konyadminconsole.membergroup.id AS Group_id, 
      konyadminconsole.membergroup.Name AS Group_name, 
      konyadminconsole.membergroup.Description AS Group_desc, 
      konyadminconsole.service.id AS Service_id, 
      konyadminconsole.service.Name AS Service_Name, 
      konyadminconsole.service.Description AS Service_Description, 
      konyadminconsole.service.Status_id AS Service_Status_id, 
      konyadminconsole.service.DisplayName AS Service_DisplayName, 
      konyadminconsole.service.DisplayDescription AS Service_DisplayDescription
   FROM (((konyadminconsole.customergroup 
      LEFT JOIN konyadminconsole.groupentitlement 
      ON ((konyadminconsole.customergroup.Group_id = konyadminconsole.groupentitlement.Group_id))) 
      LEFT JOIN konyadminconsole.membergroup 
      ON ((konyadminconsole.customergroup.Group_id = konyadminconsole.membergroup.id))) 
      LEFT JOIN konyadminconsole.service 
      ON ((konyadminconsole.groupentitlement.Service_id = konyadminconsole.service.id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[customer_request_category_count_view] (
  [requestcategory_id], [requestcategory_Name], 
  [request_count]
) AS 
SELECT 
  TOP (9223372036854775807) konyadminconsole.requestcategory.id AS requestcategory_id, 
  min(konyadminconsole.requestcategory.Name) AS requestcategory_Name, 
  count_big(konyadminconsole.customerrequest.id) AS request_count 
FROM 
  (
    konyadminconsole.requestcategory 
    LEFT JOIN konyadminconsole.customerrequest ON (
      (
        (
          konyadminconsole.customerrequest.RequestCategory_id = konyadminconsole.requestcategory.id
        ) 
        AND (
          konyadminconsole.customerrequest.Status_id = 'SID_OPEN'
        )
      )
    )
  ) 
GROUP BY 
  konyadminconsole.requestcategory.id 
ORDER BY 
  konyadminconsole.requestcategory.id

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[customer_request_csr_count_view] (
  [customerrequest_Status_id], [status_Description], 
  [customerrequest_assignedTo], [request_count]
) AS 
SELECT 
  TOP (9223372036854775807) konyadminconsole.customerrequest.Status_id AS customerrequest_Status_id, 
  min(konyadminconsole.status.Description) AS status_Description, 
  konyadminconsole.customerrequest.AssignedTo AS customerrequest_assignedTo, 
  count_big(konyadminconsole.customerrequest.id) AS request_count 
FROM 
  (
    konyadminconsole.customerrequest 
    LEFT JOIN konyadminconsole.status ON (
      (
        konyadminconsole.customerrequest.Status_id = konyadminconsole.status.id
      )
    )
  ) 
GROUP BY 
  konyadminconsole.customerrequest.Status_id, 
  konyadminconsole.customerrequest.AssignedTo 
ORDER BY 
  konyadminconsole.customerrequest.Status_id, 
  konyadminconsole.customerrequest.AssignedTo

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[customer_request_detailed_view] (
   [customerrequest_id], 
   [customerrequest_RequestCategory_id], 
   [customerrequest_lastupdatedbycustomer], 
   [requestcategory_Name], 
   [customerrequest_Customer_id],
   [customer_FirstName], 
   [customer_MiddleName], 
   [customer_Fullname], 
   [customerrequest_AssignedTo_Name], 
   [customer_LastName], 
   [customer_Username], 
   [customer_Salutation], 
   [customer_Gender], 
   [customer_DateOfBirth], 
   [customer_Status_id], 
   [customer_Ssn], 
   [customer_MaritalStatus_id], 
   [customer_SpouseName], 
   [customer_EmployementStatus_id], 
   [customer_IsEnrolledForOlb], 
   [customer_IsStaffMember], 
   [customer_Location_id], 
   [customer_PreferredContactMethod], 
   [customer_PreferredContactTime], 
   [customerrequest_Priority], 
   [customerrequest_Status_id], 
   [customerrequest_AssignedTo], 
   [customerrequest_RequestSubject], 
   [customerrequest_Accountid], 
   [customerrequest_createdby], 
   [customerrequest_modifiedby], 
   [customerrequest_createdts], 
   [customerrequest_lastmodifiedts], 
   [customerrequest_synctimestamp], 
   [customerrequest_softdeleteflag], 
   [requestmessage_id], 
   [requestmessage_RepliedBy], 
   [requestmessage_RepliedBy_Name], 
   [requestmessage_MessageDescription], 
   [requestmessage_ReplySequence], 
   [requestmessage_IsRead], 
   [requestmessage_createdby], 
   [requestmessage_modifiedby], 
   [requestmessage_createdts], 
   [requestmessage_lastmodifiedts], 
   [requestmessage_synctimestamp], 
   [requestmessage_softdeleteflag], 
   [messageattachment_id], 
   [messageattachment_AttachmentType_id], 
   [messageattachment_Media_id], 
   [messageattachment_createdby], 
   [messageattachment_modifiedby], 
   [messageattachment_createdts], 
   [messageattachment_lastmodifiedts], 
   [messageattachment_softdeleteflag], 
   [media_id], 
   [media_Name], 
   [media_Size], 
   [media_Type], 
   [media_Description], 
   [media_Url], 
   [media_createdby], 
   [media_modifiedby], 
   [media_lastmodifiedts], 
   [media_synctimestamp], 
   [media_softdeleteflag])
AS 
   SELECT 
      konyadminconsole.customerrequest.id AS customerrequest_id, 
      konyadminconsole.customerrequest.RequestCategory_id AS customerrequest_RequestCategory_id, 
      konyadminconsole.customerrequest.lastupdatedbycustomer AS customerrequest_lastupdatedbycustomer, 
      konyadminconsole.requestcategory.Name AS requestcategory_Name, 
      konyadminconsole.customerrequest.Customer_id AS customerrequest_Customer_id, 
      konyadminconsole.customer.FirstName AS customer_FirstName, 
      konyadminconsole.customer.MiddleName AS customer_MiddleName, 
      konyadminconsole.customer.FirstName + customer.LastName AS customer_Fullname, 
      konyadminconsole.systemuser.FirstName + systemuser.LastName AS customerrequest_AssignedTo_Name, 
      konyadminconsole.customer.LastName AS customer_LastName, 
      konyadminconsole.customer.Username AS customer_Username, 
      konyadminconsole.customer.Salutation AS customer_Salutation, 
      konyadminconsole.customer.Gender AS customer_Gender, 
      konyadminconsole.customer.DateOfBirth AS customer_DateOfBirth, 
      konyadminconsole.customer.Status_id AS customer_Status_id, 
      konyadminconsole.customer.Ssn AS customer_Ssn, 
      konyadminconsole.customer.MaritalStatus_id AS customer_MaritalStatus_id, 
      konyadminconsole.customer.SpouseName AS customer_SpouseName, 
      konyadminconsole.customer.EmployementStatus_id AS customer_EmployementStatus_id, 
      konyadminconsole.customer.IsEnrolledForOlb AS customer_IsEnrolledForOlb, 
      konyadminconsole.customer.IsStaffMember AS customer_IsStaffMember, 
      konyadminconsole.customer.Location_id AS customer_Location_id, 
      konyadminconsole.customer.PreferredContactMethod AS customer_PreferredContactMethod, 
      konyadminconsole.customer.PreferredContactTime AS customer_PreferredContactTime, 
      konyadminconsole.customerrequest.Priority AS customerrequest_Priority, 
      konyadminconsole.customerrequest.Status_id AS customerrequest_Status_id, 
      konyadminconsole.customerrequest.AssignedTo AS customerrequest_AssignedTo, 
      konyadminconsole.customerrequest.RequestSubject AS customerrequest_RequestSubject, 
      konyadminconsole.customerrequest.Accountid AS customerrequest_Accountid, 
      konyadminconsole.customerrequest.createdby AS customerrequest_createdby, 
      konyadminconsole.customerrequest.modifiedby AS customerrequest_modifiedby, 
      konyadminconsole.customerrequest.createdts AS customerrequest_createdts, 
      konyadminconsole.customerrequest.lastmodifiedts AS customerrequest_lastmodifiedts, 
      konyadminconsole.customerrequest.synctimestamp AS customerrequest_synctimestamp, 
      konyadminconsole.customerrequest.softdeleteflag AS customerrequest_softdeleteflag, 
      konyadminconsole.requestmessage.id AS requestmessage_id, 
      konyadminconsole.requestmessage.RepliedBy AS requestmessage_RepliedBy, 
      konyadminconsole.requestmessage.RepliedBy_Name AS requestmessage_RepliedBy_Name, 
      konyadminconsole.requestmessage.MessageDescription AS requestmessage_MessageDescription, 
      konyadminconsole.requestmessage.ReplySequence AS requestmessage_ReplySequence, 
      konyadminconsole.requestmessage.IsRead AS requestmessage_IsRead, 
      konyadminconsole.requestmessage.createdby AS requestmessage_createdby, 
      konyadminconsole.requestmessage.modifiedby AS requestmessage_modifiedby, 
      konyadminconsole.requestmessage.createdts AS requestmessage_createdts, 
      konyadminconsole.requestmessage.lastmodifiedts AS requestmessage_lastmodifiedts, 
      konyadminconsole.requestmessage.synctimestamp AS requestmessage_synctimestamp, 
      konyadminconsole.requestmessage.softdeleteflag AS requestmessage_softdeleteflag, 
      konyadminconsole.messageattachment.id AS messageattachment_id, 
      konyadminconsole.messageattachment.AttachmentType_id AS messageattachment_AttachmentType_id, 
      konyadminconsole.messageattachment.Media_id AS messageattachment_Media_id, 
      konyadminconsole.messageattachment.createdby AS messageattachment_createdby, 
      konyadminconsole.messageattachment.modifiedby AS messageattachment_modifiedby, 
      konyadminconsole.messageattachment.createdts AS messageattachment_createdts, 
      konyadminconsole.messageattachment.lastmodifiedts AS messageattachment_lastmodifiedts, 
      konyadminconsole.messageattachment.softdeleteflag AS messageattachment_softdeleteflag, 
      konyadminconsole.media.id AS media_id, 
      konyadminconsole.media.Name AS media_Name, 
      konyadminconsole.media.Size AS media_Size, 
      konyadminconsole.media.Type AS media_Type, 
      konyadminconsole.media.Description AS media_Description, 
      konyadminconsole.media.Url AS media_Url, 
      konyadminconsole.media.createdby AS media_createdby, 
      konyadminconsole.media.modifiedby AS media_modifiedby, 
      konyadminconsole.media.lastmodifiedts AS media_lastmodifiedts, 
      konyadminconsole.media.synctimestamp AS media_synctimestamp, 
      konyadminconsole.media.softdeleteflag AS media_softdeleteflag
   FROM ((((((konyadminconsole.customerrequest 
      LEFT JOIN konyadminconsole.requestmessage 
      ON ((konyadminconsole.customerrequest.id = konyadminconsole.requestmessage.CustomerRequest_id))) 
      LEFT JOIN konyadminconsole.messageattachment 
      ON ((konyadminconsole.requestmessage.id = konyadminconsole.messageattachment.RequestMessage_id))) 
      LEFT JOIN konyadminconsole.media 
      ON ((konyadminconsole.messageattachment.Media_id = konyadminconsole.media.id))) 
      LEFT JOIN konyadminconsole.customer 
      ON ((konyadminconsole.customerrequest.Customer_id = konyadminconsole.customer.id))) 
      LEFT JOIN konyadminconsole.requestcategory 
      ON ((konyadminconsole.customerrequest.RequestCategory_id = konyadminconsole.requestcategory.id))) 
      LEFT JOIN konyadminconsole.systemuser 
      ON ((konyadminconsole.customerrequest.AssignedTo = konyadminconsole.systemuser.id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[customer_request_status_count_view] ([Count], [Status_id])
AS 
   SELECT tbl$1.Count, tbl$1.Status_id
   FROM 
      (
         SELECT TOP (9223372036854775807) count_big(cr.id) AS Count, s1.id AS Status_id
         FROM (konyadminconsole.status  AS s1 
            LEFT JOIN konyadminconsole.customerrequest  AS cr 
            ON ((s1.id = cr.Status_id)))
         WHERE ((s1.Type_id = 'STID_CUSTOMERREQUEST') AND (s1.id <> 'SID_ARCHIVED'))
         GROUP BY s1.id
            ORDER BY s1.id
      )  AS tbl$1
    UNION
   SELECT TOP (9223372036854775807) count_big(acr.id) AS Count, s2.id AS Status_id
   FROM (konyadminconsole.status  AS s2 
      LEFT JOIN konyadminconsole.archivedcustomerrequest  AS acr 
      ON ((s2.id = acr.Status_id)))
   WHERE ((s2.Type_id = 'STID_CUSTOMERREQUEST') AND (s2.id = 'SID_ARCHIVED'))
   GROUP BY s2.id
      ORDER BY s2.id

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[customer_unread_message_count_view] ([customerID], [messageCount])
AS 
   SELECT TOP (9223372036854775807) konyadminconsole.customerrequest.Customer_id AS customerID, count_big(konyadminconsole.requestmessage.id) AS messageCount
   FROM (konyadminconsole.customerrequest 
      INNER JOIN konyadminconsole.requestmessage 
      ON ((konyadminconsole.requestmessage.CustomerRequest_id = konyadminconsole.customerrequest.id)))
   WHERE (konyadminconsole.requestmessage.IsRead = 'FALSE')
   GROUP BY konyadminconsole.customerrequest.Customer_id
      ORDER BY konyadminconsole.customerrequest.Customer_id

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[customerrequests_view] AS
 select konyadminconsole.customerrequest.id AS id, konyadminconsole.customerrequest.softdeleteflag AS softdeleteflag,
 konyadminconsole.customerrequest.Priority AS priority, konyadminconsole.customerrequest.createdts AS requestCreatedDate,
 max(konyadminconsole.requestmessage.createdts) AS recentMsgDate, konyadminconsole.requestcategory.Name AS requestcategory_id,
 konyadminconsole.customerrequest.Customer_id AS customer_id, konyadminconsole.customer.Username AS username, konyadminconsole.status.Description AS status_id,
  konyadminconsole.status.id AS statusIdentifier, konyadminconsole.customerrequest.RequestSubject AS requestsubject,
 konyadminconsole.customerrequest.Accountid AS accountid, count(konyadminconsole.requestmessage.id) AS totalmsgs,
 count((case when (konyadminconsole.requestmessage.IsRead = 'true') then 1 end)) AS readmsgs,
 count((case when (konyadminconsole.requestmessage.IsRead = 'false') then 1 end)) AS unreadmsgs,

 --substring_index(group_concat(requestmessage.MessageDescription order by requestmessage.createdts ASC,
 --requestmessage.id ASC separator '||'),'||',1)  AS [firstMessage],
 --group_concat(requestmessage.id separator ',') AS [msgids],

 count(konyadminconsole.messageattachment.id) AS [totalAttachments] 
 from (((((konyadminconsole.customerrequest left join konyadminconsole.requestmessage 
 on((konyadminconsole.customerrequest.id = konyadminconsole.requestmessage.CustomerRequest_id))) 
 left join konyadminconsole.requestcategory on((konyadminconsole.requestcategory.id = konyadminconsole.customerrequest.RequestCategory_id))) 
 left join konyadminconsole.customer on((konyadminconsole.customer.id = konyadminconsole.customerrequest.Customer_id))) 
 left join konyadminconsole.status on((konyadminconsole.status.id = konyadminconsole.customerrequest.Status_id))) 
 left join konyadminconsole.messageattachment on((konyadminconsole.messageattachment.RequestMessage_id= konyadminconsole.requestmessage.id))) 
 group by konyadminconsole.customerrequest.id , konyadminconsole.customerrequest.softdeleteflag,konyadminconsole.customerrequest.Priority,
 konyadminconsole.customerrequest.createdts,konyadminconsole.requestcategory.Name,konyadminconsole.customerrequest.Customer_id,konyadminconsole.customer.Username,
 konyadminconsole.status.Description,konyadminconsole.status.id,konyadminconsole.customerrequest.RequestSubject,konyadminconsole.customerrequest.Accountid
-- order by customerrequest.createdts desc,customerrequest.id;

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[customeraddress_view] (
   [CustomerId], 
   [Address_id], 
   [isPrimary], 
   [AddressType], 
   [AddressId], 
   [AddressLine1], 
   [AddressLine2], 
   [ZipCode], 
   [Region_id], 
   [City_id], 
   [Country_id], 
   [CityName], 
   [RegionName], 
   [RegionCode], 
   [CountryName], 
   [CountryCode])
AS 
   SELECT 
      c.id AS CustomerId, 
      ca.Address_id AS Address_id, 
      ca.isPrimary AS isPrimary, 
      ca.Type_id AS AddressType, 
      a.id AS AddressId, 
      a.AddressLine1 AS AddressLine1, 
      a.AddressLine2 AS AddressLine2, 
      a.ZipCode AS ZipCode, 
      a.Region_id AS Region_id, 
      a.City_id AS City_id, 
      cit.Country_id AS Country_id, 
      cit.Name AS CityName, 
      reg.Name AS RegionName, 
      reg.Code AS RegionCode, 
      coun.Name AS CountryName, 
      coun.Code AS CountryCode
   FROM (((((konyadminconsole.customeraddress  AS ca 
      LEFT JOIN konyadminconsole.customer  AS c 
      ON ((ca.Customer_id = c.id))) 
      LEFT JOIN konyadminconsole.address  AS a 
      ON ((a.id = ca.Address_id))) 
      LEFT JOIN konyadminconsole.region  AS reg 
      ON ((reg.id = a.Region_id))) 
      LEFT JOIN konyadminconsole.city  AS cit 
      ON ((cit.id = a.City_id))) 
      LEFT JOIN konyadminconsole.country  AS coun 
      ON ((coun.id = cit.Country_id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[customergroupinfo_view] (
   [Customer_id], 
   [Group_id], 
   [Group_name], 
   [Group_Desc], 
   [GroupStatus_id], 
   [GroupStatus_name], 
   [Group_createdby], 
   [Group_modifiedby], 
   [Group_createdts], 
   [Group_lastmodifiedts], 
   [Group_synctimestamp])
AS 
   SELECT 
      konyadminconsole.customergroup.Customer_id AS Customer_id, 
      konyadminconsole.customergroup.Group_id AS Group_id, 
      konyadminconsole.membergroup.Name AS Group_name, 
      konyadminconsole.membergroup.Description AS Group_Desc, 
      konyadminconsole.membergroup.Status_id AS GroupStatus_id, 
      
         (
            SELECT konyadminconsole.status.Description
            FROM konyadminconsole.status
            WHERE (konyadminconsole.membergroup.Status_id = konyadminconsole.status.id)
         ) AS GroupStatus_name, 
      konyadminconsole.membergroup.createdby AS Group_createdby, 
      konyadminconsole.membergroup.modifiedby AS Group_modifiedby, 
      konyadminconsole.membergroup.createdts AS Group_createdts, 
      konyadminconsole.membergroup.lastmodifiedts AS Group_lastmodifiedts, 
      konyadminconsole.membergroup.synctimestamp AS Group_synctimestamp
   FROM (konyadminconsole.customergroup 
      LEFT JOIN konyadminconsole.membergroup 
      ON ((konyadminconsole.customergroup.Group_id = konyadminconsole.membergroup.id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[customernotes_view] (
   [id], 
   [Note], 
   [Customer_id], 
   [Customer_FirstName], 
   [Customer_MiddleName], 
   [Customer_LastName], 
   [Customer_Username], 
   [Customer_Status_id], 
   [InternalUser_id], 
   [InternalUser_Username], 
   [InternalUser_FirstName], 
   [InternalUser_LastName], 
   [InternalUser_MiddleName], 
   [InternalUser_Email], 
   [createdts], 
   [synctimestamp], 
   [softdeleteflag])
AS 
   SELECT 
      konyadminconsole.customernote.id AS id, 
      konyadminconsole.customernote.Note AS Note, 
      konyadminconsole.customernote.Customer_id AS Customer_id, 
      konyadminconsole.customer.FirstName AS Customer_FirstName, 
      konyadminconsole.customer.MiddleName AS Customer_MiddleName, 
      konyadminconsole.customer.LastName AS Customer_LastName, 
      konyadminconsole.customer.Username AS Customer_Username, 
      konyadminconsole.customer.Status_id AS Customer_Status_id, 
      konyadminconsole.customernote.createdby AS InternalUser_id, 
      konyadminconsole.systemuser.Username AS InternalUser_Username, 
      konyadminconsole.systemuser.FirstName AS InternalUser_FirstName, 
      konyadminconsole.systemuser.LastName AS InternalUser_LastName, 
      konyadminconsole.systemuser.MiddleName AS InternalUser_MiddleName, 
      konyadminconsole.systemuser.Email AS InternalUser_Email, 
      konyadminconsole.customernote.createdts AS createdts, 
      konyadminconsole.customernote.synctimestamp AS synctimestamp, 
      konyadminconsole.customernote.softdeleteflag AS softdeleteflag
   FROM ((konyadminconsole.customernote 
      LEFT JOIN konyadminconsole.systemuser 
      ON ((konyadminconsole.customernote.createdby = konyadminconsole.systemuser.id))) 
      LEFT JOIN konyadminconsole.customer 
      ON ((konyadminconsole.customernote.Customer_id = konyadminconsole.customer.id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[customernotifications_view] (
   [customer_Id], 
   [isread], 
   [Name], 
   [Description], 
   [StartDate], 
   [ExpirationDate], 
   [Status_id], 
   [createdby], 
   [modifiedby], 
   [createdts], 
   [lastmodifiedts], 
   [synctimestamp], 
   [softdeleteflag])
AS 
    (
      SELECT 
         cn.Customer_id AS customer_Id, 
         cn.IsRead AS isread, 
         n.Name AS Name, 
         n.Description AS Description, 
         n.StartDate AS StartDate, 
         n.ExpirationDate AS ExpirationDate, 
         n.Status_id AS Status_id, 
         n.createdby AS createdby, 
         n.modifiedby AS modifiedby, 
         n.createdts AS createdts, 
         n.lastmodifiedts AS lastmodifiedts, 
         n.synctimestamp AS synctimestamp, 
         n.softdeleteflag AS softdeleteflag
      FROM (konyadminconsole.notification  AS n 
         INNER JOIN konyadminconsole.customernotification  AS cn 
         ON ((n.Id = cn.Notification_id)))
    )

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[customerpermissions_view] (
   [Customer_id], 
   [Service_id], 
   [Service_name], 
   [Service_description], 
   [Service_notes], 
   [Status_id], 
   [Status_description], 
   [TransactionFee_id], 
   [TransactionFee_description], 
   [TransactionLimit_id], 
   [TransactionLimit_description], 
   [ServiceType_id], 
   [ServiceType_description], 
   [Channel_id], 
   [ChannelType_description], 
   [MinTransferLimit], 
   [MaxTransferLimit], 
   [Display_Name], 
   [Display_Description])
AS 
   SELECT 
      konyadminconsole.customerentitlement.Customer_id AS Customer_id, 
      konyadminconsole.customerentitlement.Service_id AS Service_id, 
      konyadminconsole.service.Name AS Service_name, 
      konyadminconsole.service.Description AS Service_description, 
      konyadminconsole.service.Notes AS Service_notes, 
      konyadminconsole.service.Status_id AS Status_id, 
      konyadminconsole.status.Description AS Status_description, 
      konyadminconsole.customerentitlement.TransactionFee_id AS TransactionFee_id, 
      konyadminconsole.transactionfee.Description AS TransactionFee_description, 
      konyadminconsole.customerentitlement.TransactionLimit_id AS TransactionLimit_id, 
      konyadminconsole.transactionlimit.Description AS TransactionLimit_description, 
      konyadminconsole.service.Type_id AS ServiceType_id, 
      konyadminconsole.servicetype.Description AS ServiceType_description, 
      konyadminconsole.service.Channel_id AS Channel_id, 
      konyadminconsole.servicechannel.Description AS ChannelType_description, 
      konyadminconsole.service.MinTransferLimit AS MinTransferLimit, 
      konyadminconsole.service.MaxTransferLimit AS MaxTransferLimit, 
      konyadminconsole.service.DisplayName AS Display_Name, 
      konyadminconsole.service.DisplayDescription AS Display_Description
   FROM ((((((konyadminconsole.customerentitlement 
      LEFT JOIN konyadminconsole.service 
      ON ((konyadminconsole.customerentitlement.Service_id = konyadminconsole.service.id))) 
      LEFT JOIN konyadminconsole.status 
      ON ((konyadminconsole.service.Status_id = konyadminconsole.status.id))) 
      LEFT JOIN konyadminconsole.transactionfee 
      ON ((konyadminconsole.transactionfee.id = konyadminconsole.customerentitlement.TransactionFee_id))) 
      LEFT JOIN konyadminconsole.transactionlimit 
      ON ((konyadminconsole.transactionlimit.id = konyadminconsole.customerentitlement.TransactionLimit_id))) 
      LEFT JOIN konyadminconsole.servicetype 
      ON ((konyadminconsole.servicetype.id = konyadminconsole.service.Type_id))) 
      LEFT JOIN konyadminconsole.servicechannel 
      ON ((konyadminconsole.servicechannel.id = konyadminconsole.service.Channel_id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[customersecurityquestion_view] (
   [Customer_id], 
   [SecurityQuestion_id], 
   [CustomerAnswer], 
   [createdby], 
   [modifiedby], 
   [createdts], 
   [lastmodifiedts], 
   [Question], 
   [QuestionStatus_id], 
   [CustomerStatus_id])
AS 
   SELECT 
      konyadminconsole.customersecurityquestions.Customer_id AS Customer_id, 
      konyadminconsole.customersecurityquestions.SecurityQuestion_id AS SecurityQuestion_id, 
      konyadminconsole.customersecurityquestions.CustomerAnswer AS CustomerAnswer, 
      konyadminconsole.customersecurityquestions.createdby AS createdby, 
      konyadminconsole.customersecurityquestions.modifiedby AS modifiedby, 
      konyadminconsole.customersecurityquestions.createdts AS createdts, 
      konyadminconsole.customersecurityquestions.lastmodifiedts AS lastmodifiedts, 
      konyadminconsole.securityquestion.Question AS Question, 
      konyadminconsole.securityquestion.Status_id AS QuestionStatus_id, 
      konyadminconsole.customer.Status_id AS CustomerStatus_id
   FROM ((konyadminconsole.customersecurityquestions 
      INNER JOIN konyadminconsole.securityquestion 
      ON ((konyadminconsole.securityquestion.id = konyadminconsole.customersecurityquestions.SecurityQuestion_id))) 
      INNER JOIN konyadminconsole.customer 
      ON ((konyadminconsole.customer.id = konyadminconsole.customersecurityquestions.Customer_id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[internaluserdetails_view] (
   [id], 
   [Username], 
   [Email], 
   [Status_id], 
   [Password],
   [Code], 
   [FirstName], 
   [MiddleName], 
   [LastName], 
   [FailedCount], 
   [LastPasswordChangedts], 
   [ResetpasswordLink], 
   [ResetPasswordExpdts], 
   [lastLogints], 
   [createdby], 
   [createdts], 
   [modifiedby], 
   [lastmodifiedts], 
   [synctimestamp], 
   [softdeleteflag], 
   [Role_id], 
   [hasSuperAdminPrivilages], 
   [Role_Name], 
   [Role_Status_id])
AS 
   SELECT 
      konyadminconsole.systemuser.id AS id, 
      konyadminconsole.systemuser.Username AS Username, 
      konyadminconsole.systemuser.Email AS Email, 
      konyadminconsole.systemuser.Status_id AS Status_id, 
      konyadminconsole.systemuser.Password AS Password,
      konyadminconsole.systemuser.Code AS Code, 
      konyadminconsole.systemuser.FirstName AS FirstName, 
      konyadminconsole.systemuser.MiddleName AS MiddleName, 
      konyadminconsole.systemuser.LastName AS LastName, 
      konyadminconsole.systemuser.FailedCount AS FailedCount, 
      konyadminconsole.systemuser.LastPasswordChangedts AS LastPasswordChangedts, 
      konyadminconsole.systemuser.ResetpasswordLink AS ResetpasswordLink, 
      konyadminconsole.systemuser.ResetPasswordExpdts AS ResetPasswordExpdts, 
      konyadminconsole.systemuser.lastLogints AS lastLogints, 
      konyadminconsole.systemuser.createdby AS createdby, 
      konyadminconsole.systemuser.createdts AS createdts, 
      konyadminconsole.systemuser.modifiedby AS modifiedby, 
      konyadminconsole.systemuser.lastmodifiedts AS lastmodifiedts, 
      konyadminconsole.systemuser.synctimestamp AS synctimestamp, 
      konyadminconsole.systemuser.softdeleteflag AS softdeleteflag, 
      konyadminconsole.userrole.Role_id AS Role_id, 
      konyadminconsole.userrole.hasSuperAdminPrivilages AS hasSuperAdminPrivilages, 
      konyadminconsole.role.Name AS Role_Name, 
      konyadminconsole.role.Status_id AS Role_Status_id
   FROM ((konyadminconsole.systemuser 
      LEFT JOIN konyadminconsole.userrole 
      ON ((konyadminconsole.userrole.User_id = konyadminconsole.systemuser.id))) 
      LEFT JOIN konyadminconsole.role 
      ON ((konyadminconsole.userrole.Role_id = konyadminconsole.role.id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[locationservices_view] (
   [Location_id], 
   [Location_Name], 
   [Location_Display_Name], 
   [Location_Description], 
   [Location_Phone_Number], 
   [Location_EmailId], 
   [Location_Latitude], 
   [Location_Longitude], 
   [Location_Address_id], 
   [Location_IsMainBranch], 
   [Location_Status_id], 
   [Location_Type_id], 
   [Location_Code], 
   [Location_DeleteFlag], 
   [Location_WorkScheduleId], 
   [Service_id], 
   [Service_Type_id], 
   [Service_Status_id], 
   [Service_Name], 
   [Service_Description], 
   [Weekday_StartTime], 
   [Weekday_EndTime], 
   [Sunday_StartTime], 
   [Sunday_EndTime], 
   [Saturday_StartTime], 
   [Saturday_EndTime], 
   [ADDRESS])
AS 
   SELECT 
      konyadminconsole.location.id AS Location_id, 
      konyadminconsole.location.Name AS Location_Name, 
      konyadminconsole.location.DisplayName AS Location_Display_Name, 
      konyadminconsole.location.Description AS Location_Description, 
      konyadminconsole.location.PhoneNumber AS Location_Phone_Number, 
      konyadminconsole.location.EmailId AS Location_EmailId, 
      konyadminconsole.address.Latitude AS Location_Latitude, 
      konyadminconsole.address.Logitude AS Location_Longitude, 
      konyadminconsole.address.id AS Location_Address_id, 
      konyadminconsole.location.IsMainBranch AS Location_IsMainBranch, 
      konyadminconsole.location.Status_id AS Location_Status_id, 
      konyadminconsole.location.Type_id AS Location_Type_id, 
      konyadminconsole.location.Code AS Location_Code, 
      konyadminconsole.location.softdeleteflag AS Location_DeleteFlag, 
      konyadminconsole.location.WorkSchedule_id AS Location_WorkScheduleId, 
      konyadminconsole.service.id AS Service_id, 
      konyadminconsole.service.Type_id AS Service_Type_id, 
      konyadminconsole.service.Status_id AS Service_Status_id, 
      konyadminconsole.service.Name AS Service_Name, 
      konyadminconsole.service.Description AS Service_Description, 
      week_day.StartTime AS Weekday_StartTime, 
      week_day.EndTime AS Weekday_EndTime, 
      sunday.StartTime AS Sunday_StartTime, 
      sunday.EndTime AS Sunday_EndTime, 
      saturday.StartTime AS Saturday_StartTime, 
      saturday.EndTime AS Saturday_EndTime, 
      
         konyadminconsole.address.AddressLine1
          + 
         N', '
          + 
         
            (
               SELECT konyadminconsole.city.Name
               FROM konyadminconsole.city
               WHERE (konyadminconsole.city.id = konyadminconsole.address.City_id)
            )
          + 
         N', '
          + 
         
            (
               SELECT konyadminconsole.region.Name
               FROM konyadminconsole.region
               WHERE (konyadminconsole.region.id = konyadminconsole.address.Region_id)
            )
          + 
         N', '
          + 
         
            (
               SELECT konyadminconsole.country.Name
               FROM konyadminconsole.country
               WHERE konyadminconsole.country.id IN 
                  (
                     SELECT konyadminconsole.city.Country_id
                     FROM konyadminconsole.city
                     WHERE (konyadminconsole.city.id = konyadminconsole.address.City_id)
                  )
            )
          + 
         N', '
          + 
         konyadminconsole.address.ZipCode AS ADDRESS
   FROM ((((((konyadminconsole.location 
      LEFT JOIN konyadminconsole.locationservice 
      ON ((konyadminconsole.location.id = konyadminconsole.locationservice.Location_id))) 
      LEFT JOIN konyadminconsole.service 
      ON ((konyadminconsole.locationservice.Service_id = konyadminconsole.service.id))) 
      LEFT JOIN konyadminconsole.dayschedule  AS week_day 
      ON (((konyadminconsole.location.WorkSchedule_id = week_day.WorkSchedule_id) AND (week_day.WeekDayName = 'MONDAY')))) 
      LEFT JOIN konyadminconsole.dayschedule  AS sunday 
      ON (((konyadminconsole.location.WorkSchedule_id = sunday.WorkSchedule_id) AND (sunday.WeekDayName = 'SUNDAY')))) 
      LEFT JOIN konyadminconsole.dayschedule  AS saturday 
      ON (((konyadminconsole.location.WorkSchedule_id = saturday.WorkSchedule_id) AND (saturday.WeekDayName = 'SATURDAY')))) 
      INNER JOIN konyadminconsole.address 
      ON ((konyadminconsole.address.id = konyadminconsole.location.Address_id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[locationdetails_view] AS  
    SELECT DISTINCT  
        konyadminconsole.location.id AS locationId,  
        --(SELECT   
        --        GROUP_CONCAT(DISTINCT +(UPPER(LEFT(dayschedule.WeekDayName,  
        --                                    1)),  
        --                        LOWER(SUBSTRING(dayschedule.WeekDayName,  
        --                                    2, LEN(dayschedule.WeekDayName))),  
        --                        ':',  
        --                        SUBSTRING(dayschedule.StartTime,  
        --                            1,  
        --                            5),  
        --                        '-',  
        --                        SUBSTRING(dayschedule.EndTime,  
        --                            1,  
        --                            5))  
        --                SEPARATOR ' || ')  
        --    FROM  
        --        (dayschedule  
        --        JOIN location [l])  
        --    WHERE  
        --        ((dayschedule.WorkSchedule_id= [l.WorkSchedule_id])  
        --            AND ([l.id] = location.id))) AS [workingHours],  
        konyadminconsole.location.Name AS [informationTitle],  
        konyadminconsole.location.PhoneNumber AS [phone],  
        konyadminconsole.location.EmailId AS [email],  
        (CASE konyadminconsole.location.Status_id WHEN 'SID_ACTIVE' THEN 'OPEN' ELSE 'CLOSED' END) AS [status],  
        konyadminconsole.location.Type_id AS [type],  
        --(SELECT   
        --        GROUP_CONCAT(service.Name  
        --                ORDER BY service.Name ASC  
        --                SEPARATOR ' || ')  
        --    FROM  
        --        (service  
        --        JOIN locationservice)  
        --    WHERE  
        --        ((service.id = locationservice.Service_id)  
        --            AND (locationservice.Location_id = location.id))) AS [services],  
        (SELECT   
                konyadminconsole.city.Name  
            FROM  
                konyadminconsole.city  
            WHERE  
                (konyadminconsole.city.id = konyadminconsole.address.City_id)) AS [city],  
        (SELECT   
                konyadminconsole.region.Name  
            FROM  
                konyadminconsole.region  
            WHERE  
                (konyadminconsole.region.id = konyadminconsole.address.Region_id)) AS [region],  
        (SELECT   
                konyadminconsole.country.Name  
            FROM  
                konyadminconsole.country  
            WHERE  
                (konyadminconsole.country.id = (SELECT   
                        konyadminconsole.region.Country_id  
                      WHERE  
                        (konyadminconsole.region.id = konyadminconsole.address.Region_id)))) AS [country],  
        konyadminconsole.address.AddressLine1 AS [addressLine1],  
        konyadminconsole.address.AddressLine2 AS [addressLine2],  
        konyadminconsole.address.AddressLine3 AS [addressLine3],  
        konyadminconsole.address.ZipCode AS [zipCode],  
        konyadminconsole.address.Latitude AS [latitude],  
        konyadminconsole.address.Logitude AS [longitude]  
    FROM  
         konyadminconsole.address  
        JOIN konyadminconsole.location on konyadminconsole.address.id = konyadminconsole.location.Address_id  
        JOIN konyadminconsole.region on konyadminconsole.region.id = konyadminconsole.address.Region_id  
   -- ORDER BY location.id;  
  
--  
-- Final view structure for view locationservices_view  
--

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[requestmessages_view] (
   [TOTALATTACHMENTS], 
   [ATTACHMENTS], 
   [ID], 
   [CUSTOMERREQUEST_ID], 
   [MESSAGEDESCRIPTION], 
   [REPLYSEQUENCE], 
   [ISREAD], 
   [CREATEDBY], 
   [MODIFIEDBY], 
   [CREATEDTS], 
   [LASTMODIFIEDTS], 
   [SYNCTIMESTAMP], 
   [SOFTDELETEFLAG])
AS 
    (
      SELECT 
         
            (
               SELECT COUNT_BIG(konyadminconsole.MESSAGEATTACHMENT.ID)
               FROM konyadminconsole.MESSAGEATTACHMENT
               WHERE (konyadminconsole.MESSAGEATTACHMENT.REQUESTMESSAGE_ID = RM.ID)
            ) AS TOTALATTACHMENTS, 
         
          NULL AS ATTACHMENTS, 
			--ATT.ATTACHMENTS,
         RM.ID AS ID, 
         RM.CUSTOMERREQUEST_ID AS CUSTOMERREQUEST_ID, 
         RM.MESSAGEDESCRIPTION AS MESSAGEDESCRIPTION, 
         RM.REPLYSEQUENCE AS REPLYSEQUENCE, 
         RM.ISREAD AS ISREAD, 
         RM.CREATEDBY AS CREATEDBY, 
         RM.MODIFIEDBY AS MODIFIEDBY, 
         RM.CREATEDTS AS CREATEDTS, 
         RM.LASTMODIFIEDTS AS LASTMODIFIEDTS, 
         RM.SYNCTIMESTAMP AS SYNCTIMESTAMP, 
         RM.SOFTDELETEFLAG AS SOFTDELETEFLAG
      FROM konyadminconsole.REQUESTMESSAGE  AS RM
	  --CROSS APPLY(
   --            SELECT NULL AS ATTACHMENTS
   --            FROM (MEDIA  AS ME 
   --               INNER JOIN MESSAGEATTACHMENT  AS MA 
   --               ON ((MA.MEDIA_ID = ME.ID)))
   --            WHERE (MA.REQUESTMESSAGE_ID = RM.ID))AS ATT
    )


GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[outagemessage_view] (
   [id], 
   [Name], 
   [service_Status_id], 
   [Channel_id], 
   [Service_id], 
   [Status_id], 
   [MessageText], 
   [createdby], 
   [modifiedby], 
   [createdts], 
   [lastmodifiedts], 
   [synctimestamp], 
   [softdeleteflag])
AS 
   SELECT 
      konyadminconsole.outagemessage.id AS id, 
      konyadminconsole.service.Name AS Name, 
      konyadminconsole.service.Status_id AS service_Status_id, 
      konyadminconsole.outagemessage.Channel_id AS Channel_id, 
      konyadminconsole.outagemessage.Service_id AS Service_id, 
      konyadminconsole.outagemessage.Status_id AS Status_id, 
      konyadminconsole.outagemessage.MessageText AS MessageText, 
      konyadminconsole.outagemessage.createdby AS createdby, 
      konyadminconsole.outagemessage.modifiedby AS modifiedby, 
      konyadminconsole.outagemessage.createdts AS createdts, 
      konyadminconsole.outagemessage.lastmodifiedts AS lastmodifiedts, 
      konyadminconsole.outagemessage.synctimestamp AS synctimestamp, 
      konyadminconsole.outagemessage.softdeleteflag AS softdeleteflag
   FROM (konyadminconsole.outagemessage 
      LEFT JOIN konyadminconsole.service 
      ON ((konyadminconsole.outagemessage.Service_id = konyadminconsole.service.id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[overallpaymentlimits_view] (
   [TransactionGroupId], 
   [TransactionGroupName], 
   [TransactionGroupServiceId], 
   [ServiceId], 
   [ServiceName], 
   [Status_id], 
   [TransactionLimitId], 
   [PeriodicLimitId], 
   [Period_id], 
   [MaximumLimit], 
   [Currency], 
   [PeriodName], 
   [DayCount], 
   [Order])
AS 
   SELECT 
      konyadminconsole.transactiongroup.id AS TransactionGroupId, 
      konyadminconsole.transactiongroup.Name AS TransactionGroupName, 
      konyadminconsole.transactiongroupservice.id AS TransactionGroupServiceId, 
      konyadminconsole.transactiongroupservice.Service_id AS ServiceId, 
      konyadminconsole.service.Name AS ServiceName, 
      konyadminconsole.transactiongroup.Status_id AS Status_id, 
      konyadminconsole.periodiclimit.TransactionLimit_id AS TransactionLimitId, 
      konyadminconsole.periodiclimit.id AS PeriodicLimitId, 
      konyadminconsole.periodiclimit.Period_id AS Period_id, 
      konyadminconsole.periodiclimit.MaximumLimit AS MaximumLimit, 
      konyadminconsole.periodiclimit.Currency AS Currency, 
      konyadminconsole.period.Name AS PeriodName, 
      konyadminconsole.period.DayCount AS DayCount, 
      konyadminconsole.period.[Order] AS [Order]
   FROM ((((konyadminconsole.transactiongroup 
      INNER JOIN konyadminconsole.transactiongroupservice 
      ON ((konyadminconsole.transactiongroupservice.TransactionGroup_id = konyadminconsole.transactiongroup.id))) 
      INNER JOIN konyadminconsole.periodiclimit 
      ON ((konyadminconsole.periodiclimit.TransactionLimit_id = konyadminconsole.transactiongroup.TransactionLimit_id))) 
      INNER JOIN konyadminconsole.period 
      ON ((konyadminconsole.periodiclimit.Period_id = konyadminconsole.period.id))) 
      INNER JOIN konyadminconsole.service 
      ON ((konyadminconsole.service.id = konyadminconsole.transactiongroupservice.Service_id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[periodiclimitenduser_view] (
   [Customer_id], 
   [Service_id], 
   [TransactionFee_id], 
   [TransactionLimit_id], 
   [PeriodLimit_id], 
   [Period_id], 
   [Period_Name], 
   [MaximumLimit], 
   [Code], 
   [Currency])
AS 
   SELECT 
      konyadminconsole.customerentitlement.Customer_id AS Customer_id, 
      konyadminconsole.customerentitlement.Service_id AS Service_id, 
      konyadminconsole.customerentitlement.TransactionFee_id AS TransactionFee_id, 
      konyadminconsole.customerentitlement.TransactionLimit_id AS TransactionLimit_id, 
      konyadminconsole.periodiclimit.id AS PeriodLimit_id, 
      konyadminconsole.periodiclimit.Period_id AS Period_id, 
      konyadminconsole.period.Name AS Period_Name, 
      konyadminconsole.periodiclimit.MaximumLimit AS MaximumLimit, 
      konyadminconsole.periodiclimit.Code AS Code, 
      konyadminconsole.periodiclimit.Currency AS Currency
   FROM ((konyadminconsole.periodiclimit 
      INNER JOIN konyadminconsole.customerentitlement 
      ON ((konyadminconsole.customerentitlement.TransactionLimit_id = konyadminconsole.periodiclimit.TransactionLimit_id))) 
      INNER JOIN konyadminconsole.period 
      ON ((konyadminconsole.periodiclimit.Period_id = konyadminconsole.period.id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[periodiclimitservice_view] (
   [Service_id], 
   [TransactionFee_id], 
   [TransactionLimit_id], 
   [PeriodLimit_id], 
   [Period_id], 
   [Period_Name], 
   [MaximumLimit], 
   [Code], 
   [Currency])
AS 
   SELECT 
      konyadminconsole.service.id AS Service_id, 
      konyadminconsole.service.TransactionFee_id AS TransactionFee_id, 
      konyadminconsole.service.TransactionLimit_id AS TransactionLimit_id, 
      konyadminconsole.periodiclimit.id AS PeriodLimit_id, 
      konyadminconsole.periodiclimit.Period_id AS Period_id, 
      konyadminconsole.period.Name AS Period_Name, 
      konyadminconsole.periodiclimit.MaximumLimit AS MaximumLimit, 
      konyadminconsole.periodiclimit.Code AS Code, 
      konyadminconsole.periodiclimit.Currency AS Currency
   FROM ((konyadminconsole.periodiclimit 
      INNER JOIN konyadminconsole.service 
      ON ((konyadminconsole.service.TransactionLimit_id = konyadminconsole.periodiclimit.TransactionLimit_id))) 
      INNER JOIN konyadminconsole.period 
      ON ((konyadminconsole.periodiclimit.Period_id = konyadminconsole.period.id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[periodiclimitusergroup_view] (
   [Group_id], 
   [Service_id], 
   [TransactionFee_id], 
   [TransactionLimit_id], 
   [PeriodLimit_id], 
   [Period_id], 
   [Period_Name], 
   [MaximumLimit], 
   [Code], 
   [Currency])
AS 
   SELECT 
      konyadminconsole.groupentitlement.Group_id AS Group_id, 
      konyadminconsole.groupentitlement.Service_id AS Service_id, 
      konyadminconsole.groupentitlement.TransactionFee_id AS TransactionFee_id, 
      konyadminconsole.groupentitlement.TransactionLimit_id AS TransactionLimit_id, 
      konyadminconsole.periodiclimit.id AS PeriodLimit_id, 
      konyadminconsole.periodiclimit.Period_id AS Period_id, 
      konyadminconsole.period.Name AS Period_Name, 
      konyadminconsole.periodiclimit.MaximumLimit AS MaximumLimit, 
      konyadminconsole.periodiclimit.Code AS Code, 
      konyadminconsole.periodiclimit.Currency AS Currency
   FROM ((konyadminconsole.periodiclimit 
      INNER JOIN konyadminconsole.groupentitlement 
      ON ((konyadminconsole.groupentitlement.TransactionLimit_id = konyadminconsole.periodiclimit.TransactionLimit_id))) 
      INNER JOIN konyadminconsole.period 
      ON ((konyadminconsole.periodiclimit.Period_id = konyadminconsole.period.id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[permissionsallusers_view] (
   [User_id], 
   [Permission_id], 
   [Permission_Name], 
   [Permission_Status_id], 
   [Permission_Description], 
   [User_Status_id], 
   [UserName], 
   [Email], 
   [FirstName], 
   [MiddleName], 
   [LastName], 
   [createdby], 
   [updatedby], 
   [createdts], 
   [updatedts], 
   [isDirect], 
   [softdeleteflag])
AS 
   SELECT 
      konyadminconsole.userpermission.User_id AS User_id, 
      konyadminconsole.userpermission.Permission_id AS Permission_id, 
      konyadminconsole.permission.Name AS Permission_Name, 
      konyadminconsole.permission.Status_id AS Permission_Status_id, 
      konyadminconsole.permission.Description AS Permission_Description, 
      konyadminconsole.systemuser.Status_id AS User_Status_id, 
      konyadminconsole.systemuser.Username AS UserName, 
      konyadminconsole.systemuser.Email AS Email, 
      konyadminconsole.systemuser.FirstName AS FirstName, 
      konyadminconsole.systemuser.MiddleName AS MiddleName, 
      konyadminconsole.systemuser.LastName AS LastName, 
      konyadminconsole.systemuser.createdby AS createdby, 
      konyadminconsole.systemuser.modifiedby AS updatedby, 
      konyadminconsole.systemuser.createdts AS createdts, 
      konyadminconsole.systemuser.lastmodifiedts AS updatedts, 
      N'true' AS isDirect, 
      konyadminconsole.systemuser.softdeleteflag AS softdeleteflag
   FROM ((konyadminconsole.userpermission 
      INNER JOIN konyadminconsole.systemuser 
      ON ((konyadminconsole.userpermission.User_id = konyadminconsole.systemuser.id))) 
      INNER JOIN konyadminconsole.permission 
      ON ((konyadminconsole.userpermission.Permission_id = konyadminconsole.permission.id)))
    UNION
   SELECT 
      konyadminconsole.userrole.User_id AS User_id, 
      konyadminconsole.rolepermission.Permission_id AS Permission_id, 
      konyadminconsole.permission.Name AS Permission_Name, 
      konyadminconsole.permission.Status_id AS Permission_Status_id, 
      konyadminconsole.permission.Description AS Permission_Description, 
      konyadminconsole.systemuser.Status_id AS User_Status_id, 
      konyadminconsole.systemuser.Username AS UserName, 
      konyadminconsole.systemuser.Email AS Email, 
      konyadminconsole.systemuser.FirstName AS FirstName, 
      konyadminconsole.systemuser.MiddleName AS MiddleName, 
      konyadminconsole.systemuser.LastName AS LastName, 
      konyadminconsole.systemuser.createdby AS createdby, 
      konyadminconsole.systemuser.modifiedby AS updatedby, 
      konyadminconsole.systemuser.createdts AS createdts, 
      konyadminconsole.systemuser.lastmodifiedts AS updatedts, 
      N'false' AS isDirect, 
      konyadminconsole.systemuser.softdeleteflag AS softdeleteflag
   FROM (((konyadminconsole.rolepermission 
      INNER JOIN konyadminconsole.userrole 
      ON ((konyadminconsole.userrole.Role_id = konyadminconsole.rolepermission.Role_id))) 
      INNER JOIN konyadminconsole.systemuser 
      ON ((konyadminconsole.userrole.User_id = konyadminconsole.systemuser.id))) 
      INNER JOIN konyadminconsole.permission 
      ON ((konyadminconsole.rolepermission.Permission_id = konyadminconsole.permission.id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[productdetail_view] (
   [productId], 
   [Type_id], 
   [productTypeId], 
   [productName], 
   [Status_id], 
   [features], 
   [rates], 
   [info], 
   [productDescription], 
   [termsAndConditions], 
   [createdby], 
   [modifiedby], 
   [createdts], 
   [lastmodifiedts], 
   [synctimestamp], 
   [softdeleteflag], 
   [productType])
AS 
   SELECT 
      konyadminconsole.product.id AS productId, 
      konyadminconsole.product.Type_id AS Type_id, 
      konyadminconsole.product.ProductCode AS productTypeId, 
      konyadminconsole.product.Name AS productName, 
      konyadminconsole.product.Status_id AS Status_id, 
      konyadminconsole.product.ProductFeatures AS features, 
      konyadminconsole.product.ProductCharges AS rates, 
      konyadminconsole.product.AdditionalInformation AS info, 
      konyadminconsole.product.productDescription AS productDescription, 
      konyadminconsole.product.termsAndConditions AS termsAndConditions, 
      konyadminconsole.product.createdby AS createdby, 
      konyadminconsole.product.modifiedby AS modifiedby, 
      konyadminconsole.product.createdts AS createdts, 
      konyadminconsole.product.lastmodifiedts AS lastmodifiedts, 
      konyadminconsole.product.synctimestamp AS synctimestamp, 
      konyadminconsole.product.softdeleteflag AS softdeleteflag, 
      konyadminconsole.producttype.Name AS productType
   FROM (konyadminconsole.product 
      INNER JOIN konyadminconsole.producttype 
      ON ((konyadminconsole.product.Type_id = konyadminconsole.producttype.id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[rolepermission_view] (
   [Role_Name], 
   [Role_Description], 
   [Role_Status_id], 
   [Role_id], 
   [Permission_id], 
   [Permission_Type_id], 
   [Permission_Status_id], 
   [DataType_id], 
   [Permission_Name], 
   [Permission_Description], 
   [Permission_isComposite],
   [PermissionValue], 
   [Permission_createdby], 
   [Permission_modifiedby], 
   [Permission_createdts], 
   [Permission_lastmodifiedts], 
   [Permission_synctimestamp], 
   [Permission_softdeleteflag])
AS 
   SELECT 
      konyadminconsole.role.Name AS Role_Name, 
      konyadminconsole.role.Description AS Role_Description, 
      konyadminconsole.role.Status_id AS Role_Status_id, 
      konyadminconsole.rolepermission.Role_id AS Role_id, 
      konyadminconsole.permission.id AS Permission_id, 
      konyadminconsole.permission.Type_id AS Permission_Type_id, 
      konyadminconsole.permission.Status_id AS Permission_Status_id, 
      konyadminconsole.permission.DataType_id AS DataType_id, 
      konyadminconsole.permission.Name AS Permission_Name, 
      konyadminconsole.permission.Description AS Permission_Description,
	    konyadminconsole.permission.isComposite AS Permission_isComposite,
      konyadminconsole.permission.PermissionValue AS PermissionValue, 
      konyadminconsole.permission.createdby AS Permission_createdby, 
      konyadminconsole.permission.modifiedby AS Permission_modifiedby, 
      konyadminconsole.permission.createdts AS Permission_createdts, 
      konyadminconsole.permission.lastmodifiedts AS Permission_lastmodifiedts, 
      konyadminconsole.permission.synctimestamp AS Permission_synctimestamp, 
      konyadminconsole.permission.softdeleteflag AS Permission_softdeleteflag
   FROM ((konyadminconsole.rolepermission 
      LEFT JOIN konyadminconsole.permission 
      ON ((konyadminconsole.rolepermission.Permission_id = konyadminconsole.permission.id))) 
      INNER JOIN konyadminconsole.role 
      ON ((konyadminconsole.role.id = konyadminconsole.rolepermission.Role_id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[roleuser_view] (
   [User_id], 
   [Role_id], 
   [Status_id], 
   [Username], 
   [FirstName], 
   [MiddleName], 
   [LastName], 
   [Email], 
   [UpdatedBy], 
   [LastModifiedTimeStamp])
AS 
   SELECT 
      konyadminconsole.userrole.User_id AS User_id, 
      konyadminconsole.userrole.Role_id AS Role_id, 
      konyadminconsole.systemuser.Status_id AS Status_id, 
      konyadminconsole.systemuser.Username AS Username, 
      konyadminconsole.systemuser.FirstName AS FirstName, 
      konyadminconsole.systemuser.MiddleName AS MiddleName, 
      konyadminconsole.systemuser.LastName AS LastName, 
      konyadminconsole.systemuser.Email AS Email, 
      konyadminconsole.systemuser.modifiedby AS UpdatedBy, 
      konyadminconsole.systemuser.lastmodifiedts AS LastModifiedTimeStamp
   FROM (konyadminconsole.userrole 
      INNER JOIN konyadminconsole.systemuser 
      ON ((konyadminconsole.userrole.User_id = konyadminconsole.systemuser.id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[service_view] (
   [id], 
   [Type_id], 
   [Channel_id], 
   [Name], 
   [Description], 
   [DisplayName], 
   [DisplayDescription], 
   [Category_Id], 
   [Code], 
   [Status_id], 
   [Notes], 
   [MaxTransferLimit], 
   [MinTransferLimit], 
   [TransferDenominations], 
   [IsFutureTransaction], 
   [TransactionCharges], 
   [IsAuthorizationRequired], 
   [IsSMSAlertActivated], 
   [SMSCharges], 
   [IsBeneficiarySMSAlertActivated], 
   [BeneficiarySMSCharge], 
   [HasWeekendOperation], 
   [IsOutageMessageActive], 
   [IsAlertActive], 
   [IsTCActive], 
   [IsAgreementActive], 
   [IsCampaignActive], 
   [WorkSchedule_id], 
   [TransactionFee_id], 
   [TransactionLimit_id], 
   [createdby], 
   [modifiedby], 
   [createdts], 
   [lastmodifiedts], 
   [synctimestamp], 
   [softdeleteflag], 
   [Status], 
   [Category_Name], 
   [Channel], 
   [Type_Name], 
   [WorkSchedule_Desc])
AS 
   SELECT 
      konyadminconsole.service.id AS id, 
      konyadminconsole.service.Type_id AS Type_id, 
      konyadminconsole.service.Channel_id AS Channel_id, 
      konyadminconsole.service.Name AS Name, 
      konyadminconsole.service.Description AS Description, 
      konyadminconsole.service.DisplayName AS DisplayName, 
      konyadminconsole.service.DisplayDescription AS DisplayDescription, 
      konyadminconsole.service.Category_id AS Category_Id, 
      konyadminconsole.service.code AS Code, 
      konyadminconsole.service.Status_id AS Status_id, 
      konyadminconsole.service.Notes AS Notes, 
      konyadminconsole.service.MaxTransferLimit AS MaxTransferLimit, 
      konyadminconsole.service.MinTransferLimit AS MinTransferLimit, 
      konyadminconsole.service.TransferDenominations AS TransferDenominations, 
      konyadminconsole.service.IsFutureTransaction AS IsFutureTransaction, 
      konyadminconsole.service.TransactionCharges AS TransactionCharges, 
      konyadminconsole.service.IsAuthorizationRequired AS IsAuthorizationRequired, 
      konyadminconsole.service.IsSMSAlertActivated AS IsSMSAlertActivated, 
      konyadminconsole.service.SMSCharges AS SMSCharges, 
      konyadminconsole.service.IsBeneficiarySMSAlertActivated AS IsBeneficiarySMSAlertActivated, 
      konyadminconsole.service.BeneficiarySMSCharge AS BeneficiarySMSCharge, 
      konyadminconsole.service.HasWeekendOperation AS HasWeekendOperation, 
      konyadminconsole.service.IsOutageMessageActive AS IsOutageMessageActive, 
      konyadminconsole.service.IsAlertActive AS IsAlertActive, 
      konyadminconsole.service.IsTCActive AS IsTCActive, 
      konyadminconsole.service.IsAgreementActive AS IsAgreementActive, 
      konyadminconsole.service.IsCampaignActive AS IsCampaignActive, 
      konyadminconsole.service.WorkSchedule_id AS WorkSchedule_id, 
      konyadminconsole.service.TransactionFee_id AS TransactionFee_id, 
      konyadminconsole.service.TransactionLimit_id AS TransactionLimit_id, 
      konyadminconsole.service.createdby AS createdby, 
      konyadminconsole.service.modifiedby AS modifiedby, 
      konyadminconsole.service.createdts AS createdts, 
      konyadminconsole.service.lastmodifiedts AS lastmodifiedts, 
      konyadminconsole.service.synctimestamp AS synctimestamp, 
      konyadminconsole.service.softdeleteflag AS softdeleteflag, 
      konyadminconsole.status.Description AS Status, 
      konyadminconsole.category.Name AS Category_Name, 
      konyadminconsole.servicechannel.Description AS Channel, 
      konyadminconsole.servicetype.Description AS Type_Name, 
      konyadminconsole.workschedule.Description AS WorkSchedule_Desc
   FROM (((((konyadminconsole.service 
      LEFT JOIN konyadminconsole.servicechannel 
      ON ((konyadminconsole.service.Channel_id = konyadminconsole.servicechannel.id))) 
      LEFT JOIN konyadminconsole.category 
      ON ((konyadminconsole.service.Category_id = konyadminconsole.category.id))) 
      LEFT JOIN konyadminconsole.status 
      ON ((konyadminconsole.service.Status_id = konyadminconsole.status.id))) 
      LEFT JOIN konyadminconsole.servicetype 
      ON ((konyadminconsole.service.Type_id = konyadminconsole.servicetype.id))) 
      LEFT JOIN konyadminconsole.workschedule 
      ON ((konyadminconsole.service.WorkSchedule_id = konyadminconsole.workschedule.id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[systemuser_permissions_view] (
   [User_id], 
   [Permission_id], 
   [Permission_name], 
   [Permission_status])
AS 
   SELECT up.User_id AS User_id, up.Permission_id AS Permission_id, p.Name AS Permission_name, p.Status_id AS Permission_status
   FROM (konyadminconsole.userpermission  AS up 
      INNER JOIN konyadminconsole.permission  AS p 
      ON ((up.Permission_id = p.id)))
    UNION
   SELECT ur.User_id AS User_id, p.id AS Permission_id, p.Name AS Permission_name, p.Status_id AS Permission_status
   FROM (((konyadminconsole.role  AS r 
      INNER JOIN konyadminconsole.userrole  AS ur 
      ON ((r.id = ur.Role_id))) 
      INNER JOIN konyadminconsole.rolepermission  AS rp 
      ON ((ur.Role_id = rp.Role_id))) 
      INNER JOIN konyadminconsole.permission  AS p 
      ON ((rp.Permission_id = p.id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[systemuser_view] (
   [UserID], 
   [Username], 
   [FirstName], 
   [MiddleName], 
   [LastName], 
   [Email], 
   [Status_id], 
   [UpdatedBy], 
   [LastModifiedTimeStamp])
AS 
   SELECT 
      konyadminconsole.systemuser.id AS UserID, 
      konyadminconsole.systemuser.Username AS Username, 
      konyadminconsole.systemuser.FirstName AS FirstName, 
      konyadminconsole.systemuser.MiddleName AS MiddleName, 
      konyadminconsole.systemuser.LastName AS LastName, 
      konyadminconsole.systemuser.Email AS Email, 
      konyadminconsole.systemuser.Status_id AS Status_id, 
      konyadminconsole.systemuser.modifiedby AS UpdatedBy, 
      konyadminconsole.systemuser.lastmodifiedts AS LastModifiedTimeStamp
   FROM konyadminconsole.systemuser

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[transactionfeegroup_view] (
   [Group_id], 
   [Service_id], 
   [TransactionFee_id], 
   [MinimumTransactionValue], 
   [MaximumTransactionValue], 
   [Fees], 
   [transactionFeeSlab_id])
AS 
   SELECT 
      konyadminconsole.groupentitlement.Group_id AS Group_id, 
      konyadminconsole.groupentitlement.Service_id AS Service_id, 
      konyadminconsole.groupentitlement.TransactionFee_id AS TransactionFee_id, 
      konyadminconsole.transactionfeeslab.MinimumTransactionValue AS MinimumTransactionValue, 
      konyadminconsole.transactionfeeslab.MaximumTransactionValue AS MaximumTransactionValue, 
      konyadminconsole.transactionfeeslab.Fees AS Fees, 
      konyadminconsole.transactionfeeslab.id AS transactionFeeSlab_id
   FROM (konyadminconsole.transactionfeeslab 
      INNER JOIN konyadminconsole.groupentitlement 
      ON ((konyadminconsole.groupentitlement.TransactionFee_id = konyadminconsole.transactionfeeslab.TransactionFee_id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[transactionfeesenduser_view] (
   [customer_id], 
   [Service_id], 
   [TransactionFee_id], 
   [TransactionLimit_id], 
   [transactionfee_Description], 
   [MinimumTransactionValue], 
   [MaximumTransactionValue], 
   [Currency], 
   [Fees], 
   [transactionFeeSlab_id])
AS 
   SELECT 
      konyadminconsole.customerentitlement.Customer_id AS customer_id, 
      konyadminconsole.customerentitlement.Service_id AS Service_id, 
      konyadminconsole.customerentitlement.TransactionFee_id AS TransactionFee_id, 
      konyadminconsole.customerentitlement.TransactionLimit_id AS TransactionLimit_id, 
      konyadminconsole.transactionfee.Description AS transactionfee_Description, 
      konyadminconsole.transactionfeeslab.MinimumTransactionValue AS MinimumTransactionValue, 
      konyadminconsole.transactionfeeslab.MaximumTransactionValue AS MaximumTransactionValue, 
      konyadminconsole.transactionfeeslab.Currency AS Currency, 
      konyadminconsole.transactionfeeslab.Fees AS Fees, 
      konyadminconsole.transactionfeeslab.id AS transactionFeeSlab_id
   FROM ((konyadminconsole.transactionfee 
      INNER JOIN konyadminconsole.customerentitlement 
      ON ((konyadminconsole.customerentitlement.TransactionFee_id = konyadminconsole.transactionfee.id))) 
      INNER JOIN konyadminconsole.transactionfeeslab 
      ON ((konyadminconsole.transactionfeeslab.TransactionFee_id = konyadminconsole.transactionfee.id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[transactionfeeservice_view] (
   [Service_id], 
   [TransactionFee_id], 
   [TransactionLimit_id], 
   [MinimumTransactionValue], 
   [MaximumTransactionValue], 
   [Currency], 
   [Fees], 
   [transactionFeeSlab_id])
AS 
   SELECT 
      konyadminconsole.service.id AS Service_id, 
      konyadminconsole.service.TransactionFee_id AS TransactionFee_id, 
      konyadminconsole.service.TransactionLimit_id AS TransactionLimit_id, 
      konyadminconsole.transactionfeeslab.MinimumTransactionValue AS MinimumTransactionValue, 
      konyadminconsole.transactionfeeslab.MaximumTransactionValue AS MaximumTransactionValue, 
      konyadminconsole.transactionfeeslab.Currency AS Currency, 
      konyadminconsole.transactionfeeslab.Fees AS Fees, 
      konyadminconsole.transactionfeeslab.id AS transactionFeeSlab_id
   FROM (konyadminconsole.transactionfeeslab 
      INNER JOIN konyadminconsole.service 
      ON ((konyadminconsole.service.TransactionFee_id = konyadminconsole.transactionfeeslab.TransactionFee_id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[userdirectpermission_view] (
   [User_id], 
   [Permission_id], 
   [Permission_Name], 
   [Permission_Status_id], 
   [Permission_Description],
   [Permission_isComposite],   
   [User_Status_id], 
   [UserName], 
   [Email], 
   [FirstName], 
   [MiddleName], 
   [LastName], 
   [createdby], 
   [updatedby], 
   [createdts], 
   [updatedts], 
   [softdeleteflag])
AS 
   SELECT 
      konyadminconsole.userpermission.User_id AS User_id, 
      konyadminconsole.userpermission.Permission_id AS Permission_id, 
      konyadminconsole.permission.Name AS Permission_Name, 
      konyadminconsole.permission.Status_id AS Permission_Status_id, 
      konyadminconsole.permission.Description AS Permission_Description,
	    konyadminconsole.permission.isComposite AS Permission_isComposite,
      konyadminconsole.systemuser.Status_id AS User_Status_id, 
      konyadminconsole.systemuser.Username AS UserName, 
      konyadminconsole.systemuser.Email AS Email, 
      konyadminconsole.systemuser.FirstName AS FirstName, 
      konyadminconsole.systemuser.MiddleName AS MiddleName, 
      konyadminconsole.systemuser.LastName AS LastName, 
      konyadminconsole.systemuser.createdby AS createdby, 
      konyadminconsole.systemuser.modifiedby AS updatedby, 
      konyadminconsole.systemuser.createdts AS createdts, 
      konyadminconsole.systemuser.lastmodifiedts AS updatedts, 
      konyadminconsole.systemuser.softdeleteflag AS softdeleteflag
   FROM ((konyadminconsole.userpermission 
      INNER JOIN konyadminconsole.systemuser 
      ON ((konyadminconsole.userpermission.User_id = konyadminconsole.systemuser.id))) 
      INNER JOIN konyadminconsole.permission 
      ON ((konyadminconsole.userpermission.Permission_id = konyadminconsole.permission.id)))

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[userpermission_view] (
   [User_id], 
   [Role_id], 
   [Permission_id], 
   [Permission_Name], 
   [Permission_Desc],
   [Permission_isComposite])
AS 
   SELECT 
      konyadminconsole.userrole.User_id AS User_id, 
      konyadminconsole.userrole.Role_id AS Role_id, 
      konyadminconsole.rolepermission.Permission_id AS Permission_id, 
      konyadminconsole.permission.Name AS Permission_Name,
	    konyadminconsole.permission.Description AS Permissions_Desc,
      konyadminconsole.Permission.isComposite AS Permission_isComposite
   FROM (konyadminconsole.userrole 
      LEFT JOIN konyadminconsole.rolepermission 
      ON ((konyadminconsole.userrole.Role_id = konyadminconsole.rolepermission.Role_id))
	  LEFT JOIN konyadminconsole.permission ON ((konyadminconsole.permission.id = konyadminconsole.rolepermission.Permission_id))
	  )


GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO	 
CREATE VIEW [konyadminconsole].[card_request_notification_count_view] (
  customerId, cardNumber, reqType, requestcount) AS 
SELECT 
  cardaccountrequest_alias.Customer_id AS customerId, 
  cardaccountrequest_alias.CardAccountNumber AS cardNumber, 
  'REQUEST' AS reqType, 
  count (cardaccountrequest_alias.CardAccountNumber) AS requestcount 
FROM konyadminconsole.cardaccountrequest AS cardaccountrequest_alias
GROUP BY
  cardaccountrequest_alias.Customer_id, 
  cardaccountrequest_alias.CardAccountNumber
UNION 
SELECT 
  notificationcardinfo_alias.Customer_id AS customerId, 
  notificationcardinfo_alias.CardNumber AS cardNumber, 
  'NOTIFICATION' AS reqType, 
  count(notificationcardinfo_alias.CardNumber) AS requestcount 
FROM konyadminconsole.notificationcardinfo AS notificationcardinfo_alias 
GROUP BY notificationcardinfo_alias.Customer_id,notificationcardinfo_alias.CardNumber;

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO 
 CREATE VIEW [konyadminconsole].[customer_device_information_view] (
  Device_id, Customer_id, DeviceName, 
  LastLoginTime, LastUsedIp, Status_id, 
  Status_name, OperatingSystem, Channel_id, 
  Channel_Description, EnrollmentDate, 
  createdby, modifiedby, Registered_Date, 
  lastmodifiedts
) AS 
select 
  cd.id AS Device_id, 
  cd.Customer_id AS Customer_id, 
  cd.DeviceName AS DeviceName, 
  cd.LastLoginTime AS LastLoginTime, 
  cd.LastUsedIp AS LastUsedIp, 
  cd.Status_id AS Status_id, 
  s.Description AS Status_name, 
  cd.OperatingSystem AS OperatingSystem, 
  cd.Channel_id AS Channel_id, 
  sc.Description AS Channel_Description, 
  cd.EnrollmentDate AS EnrollmentDate, 
  cd.createdby AS createdby, 
  cd.modifiedby AS modifiedby, 
  sc.createdts AS Registered_Date, 
  cd.lastmodifiedts AS lastmodifiedts 
FROM [konyadminconsole].[customerdevice] AS cd 
  LEFT JOIN [konyadminconsole].[status] AS s ON (cd.Status_id = s.id) 
  LEFT JOIN [konyadminconsole].[servicechannel] AS sc ON (sc.id = cd.Channel_id);

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO 
 CREATE VIEW [konyadminconsole].[location_view] (
  id, Name, Code, Description, PhoneNumber, 
  Type_id, Status_id
) AS 
SELECT 
  DISTINCT konyadminconsole.location.id AS id, 
  konyadminconsole.location.Name AS Name, 
  konyadminconsole.location.Code AS Code, 
  konyadminconsole.location.Description AS Description, 
  konyadminconsole.location.PhoneNumber AS PhoneNumber, 
  konyadminconsole.location.Type_id AS Type_id, 
  (case konyadminconsole.location.Status_id when 'SID_ACTIVE' then 'Active' else 'Inactive' end) AS Status_id 
from konyadminconsole.location;

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO 
 CREATE VIEW [konyadminconsole].[region_details_view] (
  region_Id, region_Code, region_Name, 
  country_Id, country_Code, country_Name, 
  region_createdby, region_modifiedby, 
  region_createdts, region_lastmodifiedts
) AS 
SELECT 
  konyadminconsole.region.id AS region_Id, 
  konyadminconsole.region.Code AS region_Code, 
  konyadminconsole.region.Name AS region_Name, 
  konyadminconsole.region.Country_id AS country_Id, 
  konyadminconsole.country.Code AS country_Code, 
  konyadminconsole.country.Name AS country_Name, 
  konyadminconsole.region.createdby AS region_createdby, 
  konyadminconsole.region.modifiedby AS region_modifiedby, 
  konyadminconsole.region.createdts AS region_createdts, 
  konyadminconsole.region.lastmodifiedts AS region_lastmodifiedts 
FROM konyadminconsole.region left join konyadminconsole.country 
   ON (konyadminconsole.country.id = konyadminconsole.region.Country_id);


GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [konyadminconsole].[travelnotifications_view] AS 
SELECT 
  tn.id AS notificationId, 
  tn.PlannedDepartureDate AS startDate, 
  tn.PlannedReturnDate AS endDate, 
  tn.Destinations AS destinations, 
  tn.AdditionalNotes AS additionalNotes, 
  tn.Status_id AS Status_id, 
  tn.phonenumber AS contactNumber, 
  tn.createdts AS date,
  (select count(nci.CardNumber) 
		from konyadminconsole.notificationcardinfo nci 
		where nci.Notification_id = tn.id) as cardCount,
  (select  
		string_agg(concat(nci2.CardName, ' ', nci2.CardNumber) , ',') AS cardNumber  
		from konyadminconsole.notificationcardinfo nci2 
		where nci2.Notification_id = tn.id) as cardNumber,
  (select distinct nci3.Customer_id 
		from konyadminconsole.notificationcardinfo nci3 
		where nci3.Notification_id = tn.id) as customerId,
  status.Description AS status 
  FROM konyadminconsole.travelnotification tn 
		LEFT JOIN konyadminconsole.status status ON tn.Status_id = status.id;


