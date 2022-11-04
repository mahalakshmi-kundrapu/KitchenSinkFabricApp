ALTER TABLE [konydbplog].[customeractivity] ADD 
[sessionId] NVARCHAR(100) NULL,  
[deviceId] NVARCHAR(100) NULL,  
[browser] NVARCHAR(50) NULL;
GO

ALTER TABLE [konydbplog].[customeractivity] ADD
  [customerId] NVARCHAR(50) NULL,
  [typeOfMFA] NVARCHAR(50) NULL,
  [payeeName] NVARCHAR(50) NULL,
  [accountNumber] NVARCHAR(50) NULL,
  [relationshipNumber] NVARCHAR(50) NULL,
  [phoneNumber] NVARCHAR(50) NULL,
  [email] NVARCHAR(50) NULL,
  [bankName] NVARCHAR(50) NULL,
  [maskedAccountNumber] NVARCHAR(50) NULL;
GO

ALTER TABLE [konydbplog].[transactionlog] ADD
  [internationalRoutingCode] NVARCHAR(50) NULL,
  [module] NVARCHAR(255) NULL,
  [customerId] NVARCHAR(50) NULL,
  [device] NVARCHAR(50) NULL,
  [operatingSystem] NVARCHAR(50) NULL,
  [deviceId] NVARCHAR(50) NULL,
  [ipress] NVARCHAR(50) NULL,
  [referenceNumber] NVARCHAR(50) NULL,
  [transactionDescription] NVARCHAR(1000) NULL,
  [errorCode] NVARCHAR(50) NULL,
  [recipientType] NVARCHAR(50) NULL,
  [recipientBankName] NVARCHAR(50) NULL,
  [recipientress] NVARCHAR(1000) NULL,
  [recipientBankress] NVARCHAR(1000) NULL,
  [checkNumber] NVARCHAR(50) NULL,
  [cashWithdrawalFor] NVARCHAR(50) NULL;
GO

UPDATE [konydbplog].[transactionlog] SET internationalRoutingCode=ibanCode
GO

ALTER TABLE [konydbplog].[transactionlog] DROP COLUMN ibanCode
GO

