CREATE TABLE [konydbplog].[adminactivity] (
	id VARCHAR(50) NOT NULL,
	event NVARCHAR(50),
	description NVARCHAR(1000),
	username NVARCHAR(50),
	userRole NVARCHAR(50),
	moduleName NVARCHAR(50),
	eventts datetime2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
	status NVARCHAR(50),
	createdBy NVARCHAR(50),
	createdOn datetime2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (id)
)
GO

CREATE TABLE [konydbplog].[admincustomeractivity] (
	id VARCHAR(50) NOT NULL,
	customerId NVARCHAR(50),
	adminName NVARCHAR(255),
	adminRole NVARCHAR(50),
	activityType NVARCHAR(50),
	description NVARCHAR(1000),
	eventts datetime2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
	status NVARCHAR(50),
	createdBy NVARCHAR(50),
	createdOn datetime2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (id)
)
GO

CREATE TABLE [konydbplog].[customeractivity] (
	id VARCHAR(50) NOT NULL,
	username NVARCHAR(50),
	moduleName NVARCHAR(100),
	activityType NVARCHAR(50),
	description NVARCHAR(1000),
	eventts datetime2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
	status NVARCHAR(50),
	channel NVARCHAR(50),
	ipAddress NVARCHAR(50),
	device NVARCHAR(50),
	operatingSystem NVARCHAR(50),
	referenceId NVARCHAR(50),
	errorCode NVARCHAR(50),
	createdBy NVARCHAR(50),
	createdOn datetime2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (id)
)
GO

CREATE TABLE [konydbplog].[transactionlog] (
	id VARCHAR(50) NOT NULL,
	transactionId NVARCHAR(50),
	username NVARCHAR(50),
	payeeName NVARCHAR(50),
	serviceName NVARCHAR(50),
	type NVARCHAR(50),
	fromAccount NVARCHAR(50),
	fromAccountType NVARCHAR(50),
	toAccount NVARCHAR(50),
	toAccountType NVARCHAR(50),
	amount decimal(20,2),
	currencyCode NVARCHAR(50),
	channel NVARCHAR(50),
	status NVARCHAR(50),
	description NVARCHAR(1000),
	routingNumber NVARCHAR(50),
	batchId NVARCHAR(45),
	transactionDate datetime2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
	fromMobileOrEmail NVARCHAR(50),
	toMobileOrEmail NVARCHAR(50),
	swiftCode NVARCHAR(50),
	ibanCode NVARCHAR(50),
	createdBy NVARCHAR(50),
	createdOn datetime2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (id)
)
GO

CREATE INDEX idx_systemuseractivity_eventts on konydbplog.adminactivity(eventts)
CREATE INDEX idx_systemuseractivity_modulename on konydbplog.adminactivity(moduleName)
CREATE INDEX idx_systemuseractivity_username on konydbplog.adminactivity(username)
CREATE INDEX idx_systemuseractivity_userrole on konydbplog.adminactivity(userRole)
CREATE INDEX idx_systemuseractivity_event on konydbplog.adminactivity(event)
CREATE INDEX idx_systemuseractivity_status on konydbplog.adminactivity(status)

CREATE INDEX idx_admincustomeractivity_adminname on konydbplog.admincustomeractivity(adminName)
CREATE INDEX idx_admincustomeractivity_activitytype on konydbplog.admincustomeractivity(activityType)
CREATE INDEX idx_admincustomeractivity_eventts on konydbplog.admincustomeractivity(eventts)
CREATE INDEX idx_admincustomeractivity_adminrole on konydbplog.admincustomeractivity(adminRole)
CREATE INDEX idx_admincustomeractivity_customerusername on konydbplog.admincustomeractivity(customerId)

CREATE INDEX idx_customeractivity_modulename on konydbplog.customeractivity(moduleName)
CREATE INDEX idx_customeractivity_eventts on konydbplog.customeractivity(eventts)
CREATE INDEX idx_customeractivity_activitytype on konydbplog.customeractivity(activityType)
CREATE INDEX idx_customeractivity_username on konydbplog.customeractivity(username)
CREATE INDEX idx_customeractivity_status on konydbplog.customeractivity(status)
CREATE INDEX idx_customeractivity_channel on konydbplog.customeractivity(channel)
CREATE INDEX idx_customeractivity_operatingsystem on konydbplog.customeractivity(operatingSystem)

CREATE INDEX idx_transactionlog_type on konydbplog.transactionlog(type)
CREATE INDEX idx_transactionlog_transactiondate on konydbplog.transactionlog(transactionDate)
CREATE INDEX idx_transactionlog_amount on konydbplog.transactionlog(amount)
CREATE INDEX idx_transactionlog_username on konydbplog.transactionlog(username)
CREATE INDEX idx_transactionlog_transactionid on konydbplog.transactionlog(transactionId)
CREATE INDEX idx_transactionlog_servicename on konydbplog.transactionlog(serviceName)
CREATE INDEX idx_transactionlog_fromaccounttype on konydbplog.transactionlog(fromAccountType)
CREATE INDEX idx_transactionlog_toaccounttype on konydbplog.transactionlog(toAccountType)
CREATE INDEX idx_transactionlog_currency on konydbplog.transactionlog(currencyCode)
CREATE INDEX idx_transactionlog_status on konydbplog.transactionlog(status)
CREATE INDEX idx_transactionlog_frommobileemail on konydbplog.transactionlog(fromMobileOrEmail)
CREATE INDEX idx_transactionlog_tomobileemail on konydbplog.transactionlog(toMobileOrEmail)
CREATE INDEX idx_transactionlog_payeename on konydbplog.transactionlog(payeeName)
GO

