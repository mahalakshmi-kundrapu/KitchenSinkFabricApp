--
-- Table structure for table `action`
--

DROP TABLE IF EXISTS `action`;

CREATE TABLE `action` (
  `id` varchar(50) NOT NULL,
  `Description` varchar(50) DEFAULT NULL,
  `Code` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `actionprofile`
--

DROP TABLE IF EXISTS `actionprofile`;

CREATE TABLE `actionprofile` (
  `id` varchar(50) NOT NULL,
  `Description` varchar(50) DEFAULT NULL,
  `Code` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `decisionbatchrun`
--

DROP TABLE IF EXISTS `decisionbatchrun`;

CREATE TABLE `decisionbatchrun` (
  `job_id` varchar(50) NOT NULL,
  `completedRecordCounter` varchar(50) DEFAULT NULL,
  `successflag` tinyint(4) NOT NULL DEFAULT '0',
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



--
-- Table structure for table `decisionfailure`
--

DROP TABLE IF EXISTS `decisionfailure`;

CREATE TABLE `decisionfailure` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `job_id` varchar(50) DEFAULT NULL,
  `decision_id` varchar(50) DEFAULT NULL,
  `baseAttributeName` varchar(50) DEFAULT NULL,
  `baseAttributeValue` varchar(50) DEFAULT NULL,
  `resultAttributeName` varchar(50) DEFAULT NULL,
  `errmsg` varchar(500) DEFAULT NULL,
  `exception` varchar(500) DEFAULT NULL,
  `failureTriggerJob_id` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT NULL,
  `softdeleteflag` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=676 DEFAULT CHARSET=utf8;


--
-- Table structure for table `decisionresult`
--

DROP TABLE IF EXISTS `decisionresult`;

CREATE TABLE `decisionresult` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `job_id` varchar(50) DEFAULT NULL,
  `decision_id` varchar(50) DEFAULT NULL,
  `baseAttributeName` varchar(50) DEFAULT NULL,
  `baseAttributeValue` varchar(50) DEFAULT NULL,
  `resultAttributeName` varchar(50) DEFAULT NULL,
  `resultAttributeValue` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT NULL,
  `softdeleteflag` tinyint(4) DEFAULT NULL,
  `errmsg` varchar(500) DEFAULT NULL,
  `exception` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16018 DEFAULT CHARSET=utf8;


--
-- Table structure for table `loansconfigurations`
--

DROP TABLE IF EXISTS `loansconfigurations`;

CREATE TABLE `loansconfigurations` (
  `bundle_id` varchar(255) NOT NULL,
  `config_type` varchar(255) DEFAULT NULL,
  `config_key` varchar(255) DEFAULT NULL,
  `config_value` text,
  `description` varchar(255) DEFAULT NULL,
  `lastUpdatedTime` datetime(6) DEFAULT '2018-11-16 13:48:43.000000',
  PRIMARY KEY (`bundle_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



--
-- Table structure for table `querytype`
--

DROP TABLE IF EXISTS `querytype`;

CREATE TABLE `querytype` (
  `id` varchar(50) NOT NULL,
  `Description` varchar(100) DEFAULT NULL,
  `Code` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `querysectionquestion`
--

DROP TABLE IF EXISTS `querysectionquestion`;

CREATE TABLE `querysectionquestion` (
  `id` varchar(100) NOT NULL,
  `QueryDefinition_id` varchar(50) DEFAULT NULL,
  `QuerySection_id` varchar(50) DEFAULT NULL,
  `QuestionDefinition_id` varchar(100) DEFAULT NULL,
  `ParentQuerySectionQuestion_id` varchar(50) DEFAULT NULL,
  `ParentQuestionOptionValue` varchar(100) DEFAULT NULL,
  `Sequence` int(11) DEFAULT NULL,
  `IsRequired` tinyint(1) NOT NULL DEFAULT '0',
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  `abstractName` varchar(100) NOT NULL,
  `ParentQuerySectionQuestion_AbstractName` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `QuestionDefinitionID_FK_idx` (`QuestionDefinition_id`),
  KEY `FK_QuerySectionQuestion_QuestionDefinition_idx` (`QuestionDefinition_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `querydefinition`
--

DROP TABLE IF EXISTS `querydefinition`;

CREATE TABLE `querydefinition` (
  `id` varchar(50) NOT NULL,
  `QueryType_id` varchar(50) DEFAULT NULL,
  `Name` varchar(100) DEFAULT NULL,
  `Code` varchar(50) DEFAULT NULL,
  `Status_id` varchar(50) DEFAULT NULL,
  `StartDate` date DEFAULT NULL,
  `EndDate` date DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `optiongroup`
--

DROP TABLE IF EXISTS `optiongroup`;

CREATE TABLE `optiongroup` (
  `id` varchar(50) NOT NULL,
  `Name` varchar(100) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



--
-- Table structure for table `optionitem`
--

DROP TABLE IF EXISTS `optionitem`;

CREATE TABLE `optionitem` (
  `id` varchar(50) NOT NULL,
  `OptionGroup_id` varchar(50) DEFAULT NULL,
  `Label` varchar(100) DEFAULT NULL,
  `Code` varchar(50) DEFAULT NULL,
  `DefaultValue` varchar(50) DEFAULT NULL,
  `Sequence` int(11) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_OptionItem_OptionGroup_idx` (`OptionGroup_id`),
  CONSTRAINT `FK_OptionItem_OptionGroup` FOREIGN KEY (`OptionGroup_id`) REFERENCES `optiongroup` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `option`
--

DROP TABLE IF EXISTS `option`;

CREATE TABLE `option` (
  `id` varchar(50) NOT NULL,
  `Name` varchar(50) DEFAULT NULL,
  `Label` varchar(100) DEFAULT NULL,
  `Code` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT NULL,
  `lastmodifiedts` timestamp NULL DEFAULT NULL,
  `synctimestamp` timestamp NULL DEFAULT NULL,
  `softdeleteflag` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `loantype`
--

DROP TABLE IF EXISTS `loantype`;

CREATE TABLE `loantype` (
  `id` varchar(50) NOT NULL,
  `Description` varchar(50) DEFAULT NULL,
  `Code` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  `APRValue` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `loansconfigurationmasters`
--

DROP TABLE IF EXISTS `loansconfigurationmasters`;

CREATE TABLE `loansconfigurationmasters` (
  `bundle_id` varchar(255) NOT NULL,
  `app_id` varchar(255) DEFAULT NULL,
  `channels` varchar(255) DEFAULT NULL,
  `user_id` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `device_id` varchar(255) DEFAULT NULL,
  `app_version` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`bundle_id`),
  UNIQUE KEY `bundle_id_UNIQUE` (`bundle_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `disclaimer`
--

DROP TABLE IF EXISTS `disclaimer`;

CREATE TABLE `disclaimer` (
  `id` varchar(50) NOT NULL,
  `App_id` varchar(50) DEFAULT NULL,
  `ModuleName` varchar(50) DEFAULT NULL,
  `DisclaimerName` varchar(50) DEFAULT NULL,
  `DisclaimerText` text,
  `DisclaimerUrl` varchar(200) DEFAULT NULL,
  `ContentType` varchar(5) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=cp850;


--
-- Table structure for table `loanproduct`
--

DROP TABLE IF EXISTS `loanproduct`;

CREATE TABLE `loanproduct` (
  `id` varchar(50) NOT NULL,
  `LoanType_id` varchar(50) DEFAULT NULL,
  `MinLimitAmount` decimal(10,2) DEFAULT NULL,
  `MaxLimitAmount` decimal(10,2) DEFAULT NULL,
  `Name` varchar(100) DEFAULT NULL,
  `Description` varchar(100) DEFAULT NULL,
  `Code` varchar(50) DEFAULT NULL,
  `Image` text,
  `AtAGlance` text,
  `APR` varchar(50) DEFAULT NULL,
  `AnnualFee` decimal(10,2) DEFAULT NULL,
  `Rewards` text,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_LoanProduct_LoanType_idx` (`LoanType_id`),
  CONSTRAINT `FK_LoanProduct_LoanType` FOREIGN KEY (`LoanType_id`) REFERENCES `loantype` (`id`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `querysection`
--

DROP TABLE IF EXISTS `querysection`;

CREATE TABLE `querysection` (
  `id` varchar(50) NOT NULL,
  `Name` varchar(100) DEFAULT NULL,
  `QueryDefinition_id` varchar(50) DEFAULT NULL,
  `Parent_id` varchar(50) DEFAULT NULL,
  `ApplicantAllowedTo` varchar(50) DEFAULT NULL,
  `IndCoApplicantAllowedTo` varchar(50) DEFAULT NULL,
  `JointCoApplicantAllowedTo` varchar(50) DEFAULT NULL,
  `Sequence` int(11) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  `abstractname` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_QuerySection_QueryDefintion_idx` (`QueryDefinition_id`),
  CONSTRAINT `FK_QuerySection_QueryDefintion` FOREIGN KEY (`QueryDefinition_id`) REFERENCES `querydefinition` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `questiondefinition`
--

DROP TABLE IF EXISTS `questiondefinition`;

CREATE TABLE `questiondefinition` (
  `id` varchar(100) NOT NULL,
  `QueryDefinition_id` varchar(50) DEFAULT NULL,
  `DataType_id` varchar(50) DEFAULT NULL,
  `OptionGroup_id` varchar(50) DEFAULT NULL,
  `Unit` varchar(50) DEFAULT NULL,
  `Name` varchar(100) DEFAULT NULL,
  `Label` varchar(10000) DEFAULT NULL,
  `OtherLabel` varchar(10000) DEFAULT NULL,
  `IsRequired` tinyint(1) NOT NULL DEFAULT '0',
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_QuestionDefintion_QueryDefinition_idx` (`QueryDefinition_id`),
  KEY `FK_QuestionDefintion_OptionGroup_idx` (`OptionGroup_id`),
  CONSTRAINT `FK_QuestionDefintion_OptionGroup` FOREIGN KEY (`OptionGroup_id`) REFERENCES `optiongroup` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_QuestionDefintion_QueryDefinition` FOREIGN KEY (`QueryDefinition_id`) REFERENCES `querydefinition` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `prequalifypackage`
--

DROP TABLE IF EXISTS `prequalifypackage`;

CREATE TABLE `prequalifypackage` (
  `id` varchar(50) NOT NULL,
  `LoanType_id` varchar(50) DEFAULT NULL,
  `LoanProduct_id` varchar(50) DEFAULT NULL,
  `Name` varchar(50) DEFAULT NULL,
  `Code` varchar(50) DEFAULT NULL,
  `Description` varchar(100) DEFAULT NULL,
  `LoanAmount` varchar(100) DEFAULT NULL,
  `LoanTerms` varchar(50) DEFAULT NULL,
  `APR` varchar(50) DEFAULT NULL,
  `MonthlyPayment` varchar(50) DEFAULT NULL,
  `AnnualFee` varchar(50) DEFAULT NULL,
  `BenfitsandRewards` text,
  `TransferInformation` varchar(50) DEFAULT NULL,
  `PrequalifyCondition` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(4) DEFAULT NULL,
  `rate` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_PreQualifyPackage_LoanType_idx` (`LoanType_id`),
  KEY `FK_PreQualifyPackage_LoanProduct_idx` (`LoanProduct_id`),
  CONSTRAINT `FK_PreQualifyPackage_LoanProduct` FOREIGN KEY (`LoanProduct_id`) REFERENCES `loanproduct` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_PreQualifyPackage_LoanType` FOREIGN KEY (`LoanType_id`) REFERENCES `loantype` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `customerdpdata`
--

DROP TABLE IF EXISTS `customerdpdata`;

CREATE TABLE `customerdpdata` (
  `id` varchar(50) NOT NULL,
  `Customer_id` varchar(50) DEFAULT NULL,
  `CreditScore` varchar(50) DEFAULT NULL,
  `EmploymentType` varchar(50) DEFAULT NULL,
  `AnnualIncome` varchar(50) DEFAULT NULL,
  `AccountBalance` varchar(50) DEFAULT NULL,
  `Age` varchar(50) DEFAULT NULL,
  `DebtToIncomeRatio` varchar(50) DEFAULT NULL,
  `TimeofEmployment` varchar(50) DEFAULT NULL,
  `State` varchar(50) DEFAULT NULL,
  `PrequalifyScore` varchar(50) DEFAULT NULL,
  `ActionProfile_id` varchar(50) DEFAULT NULL,
  `Input1` varchar(50) DEFAULT NULL,
  `Input2` varchar(50) DEFAULT NULL,
  `Input3` varchar(50) DEFAULT NULL,
  `Input4` varchar(50) DEFAULT NULL,
  `Input5` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT NULL,
  `softdeleteflag` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_CutomerDpData_ActionProfile_idx` (`ActionProfile_id`),
  CONSTRAINT `FK_CutomerDpData_ActionProfile` FOREIGN KEY (`ActionProfile_id`) REFERENCES `actionprofile` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `customerprequalifypackage`
--

DROP TABLE IF EXISTS `customerprequalifypackage`;

CREATE TABLE `customerprequalifypackage` (
  `id` varchar(50) NOT NULL,
  `Customer_id` varchar(50) DEFAULT NULL,
  `PrequalifyPackage_id` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_CustomerPQP_PreQualifyP_idx` (`PrequalifyPackage_id`),
  CONSTRAINT `FK_CustomerPQP_PreQualifyP` FOREIGN KEY (`PrequalifyPackage_id`) REFERENCES `prequalifypackage` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `queryresponse`
--

DROP TABLE IF EXISTS `queryresponse`;

CREATE TABLE `queryresponse` (
  `id` varchar(50) NOT NULL,
  `QueryDefinition_id` varchar(50) DEFAULT NULL,
  `Customer_id` varchar(50) NOT NULL,
  `Is_Applicant` tinyint(1) DEFAULT NULL,
  `CoBorrower_id` varchar(50) DEFAULT NULL,
  `LoanProduct_id` varchar(50) DEFAULT NULL,
  `Status_id` varchar(50) DEFAULT NULL,
  `SubmitDate` date DEFAULT NULL,
  `ClosingDate` date DEFAULT NULL,
  `OverallPercentageCompletion` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_QueryResponse_QueryDefinition_idx` (`QueryDefinition_id`),
  KEY `FK_QueryResponse_LoanProduct_idx` (`LoanProduct_id`),
  CONSTRAINT `FK_QueryResponse_LoanProduct` FOREIGN KEY (`LoanProduct_id`) REFERENCES `loanproduct` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_QueryResponse_QueryDefinition` FOREIGN KEY (`QueryDefinition_id`) REFERENCES `querydefinition` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `customerquerysectionstatus`
--

DROP TABLE IF EXISTS `customerquerysectionstatus`;

CREATE TABLE `customerquerysectionstatus` (
  `id` varchar(50) NOT NULL,
  `User_id` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `QueryResponse_id` varchar(45) DEFAULT NULL,
  `QuerySection_id` varchar(45) DEFAULT NULL,
  `Status` varchar(45) DEFAULT NULL,
  `PercentageCompletion` varchar(45) DEFAULT NULL,
  `createdby` varchar(45) DEFAULT NULL,
  `modifiedby` varchar(45) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` varchar(45) DEFAULT NULL,
  `LastQuerySectionQuestion_id` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_CustomerQSS_QuesySection_idx` (`QuerySection_id`),
  CONSTRAINT `FK_CustomerQSS_QuesySection` FOREIGN KEY (`QuerySection_id`) REFERENCES `querysection` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `questionresponse`
--

DROP TABLE IF EXISTS `questionresponse`;

CREATE TABLE `questionresponse` (
  `id` varchar(50) NOT NULL,
  `QueryResponse_id` varchar(100) DEFAULT NULL,
  `QueryDefinition_id` varchar(50) DEFAULT NULL,
  `QuestionDefinition_id` varchar(50) DEFAULT NULL,
  `QuerySectionQuestion_id` varchar(100) DEFAULT NULL,
  `QuerySection_id` varchar(50) DEFAULT NULL,
  `ArrayIndex` int(11) DEFAULT NULL,
  `ResponseValue` text,
  `OptionItem_id` varchar(100) DEFAULT NULL,
  `Unit` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_QuestionResponse_QueryDefintion_idx` (`QueryDefinition_id`),
  KEY `FK_QuestionResponse_QuerySection_idx` (`QuerySection_id`),
  KEY `FK_QuestionResponse_QuerySectionQuestion_idx` (`QuerySectionQuestion_id`),
  CONSTRAINT `FK_QuestionResponse_QueryDefintion` FOREIGN KEY (`QueryDefinition_id`) REFERENCES `querydefinition` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_QuestionResponse_QuerySection` FOREIGN KEY (`QuerySection_id`) REFERENCES `querysection` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `querycoborrower`
--

DROP TABLE IF EXISTS `querycoborrower`;

CREATE TABLE `querycoborrower` (
  `id` varchar(50) NOT NULL,
  `QueryResponse_id` varchar(50) DEFAULT NULL,
  `CoBorrower_Type` varchar(50) DEFAULT NULL,
  `FirstName` varchar(50) DEFAULT NULL,
  `LastName` varchar(50) DEFAULT NULL,
  `PhoneNumber` varchar(20) DEFAULT NULL,
  `Email` varchar(50) DEFAULT NULL,
  `OTP` varchar(50) DEFAULT NULL,
  `OTPValidity` timestamp NULL DEFAULT NULL,
  `Is_CoBorrowerActive` tinyint(1) DEFAULT NULL,
  `Is_Verified` tinyint(1) DEFAULT NULL,
  `InvitationLink` varchar(200) DEFAULT NULL,
  `InvitationLinkValidity` timestamp NULL DEFAULT NULL,
  `InvitationLinkStatus` varchar(50) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT NULL,
  `softdeleteflag` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `optionitemresponse`
--

DROP TABLE IF EXISTS `optionitemresponse`;

CREATE TABLE `optionitemresponse` (
  `id` varchar(50) NOT NULL,
  `QuestionResponse_id` varchar(100) DEFAULT NULL,
  `OptionItem_id` varchar(50) DEFAULT NULL,
  `ItemValue` varchar(100) DEFAULT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_OptionItemResponse_OptionItem_idx` (`OptionItem_id`),
  CONSTRAINT `FK_OptionItemResponse_OptionItem` FOREIGN KEY (`OptionItem_id`) REFERENCES `optionitem` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `interestrate`
--

DROP TABLE IF EXISTS `interestrate`;
CREATE TABLE `interestrate` (
  `id` varchar(50) NOT NULL,
  `LoanProduct_id` varchar(50) DEFAULT NULL,
  `Name` varchar(100) NOT NULL,
  `RateValue` decimal(5,2) NOT NULL,
  `createdby` varchar(50) DEFAULT NULL,
  `modifiedby` varchar(50) DEFAULT NULL,
  `createdts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `lastmodifiedts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `synctimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `softdeleteflag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_InterestRate_LoanProduct_idx` (`LoanProduct_id`),
  CONSTRAINT `FK_InterestRate_LoanProduct` FOREIGN KEY (`LoanProduct_id`) REFERENCES `loanproduct` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP PROCEDURE IF EXISTS `sp_getAllApplications`;

DELIMITER ;;
CREATE PROCEDURE `sp_getAllApplications`(IN `User_id` varchar(20000))
BEGIN
SELECT 
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
                AND ((`questResp`.`QuerySectionQuestion_id` = 'VA_LOAN_AMOUNT'
                OR `questResp`.`QuerySectionQuestion_id` = 'VA_COBORROWER')
                OR (`questResp`.`QuerySectionQuestion_id` = 'CCA_CARDLIMIT')
                OR (`questResp`.`QuerySectionQuestion_id` = 'PA_LOANAMOUNT'
                OR `questResp`.`QuerySectionQuestion_id` = 'PA_COBORROWER'))) AS `QuestionResponse`
FROM
    `queryresponse` queryresp
        LEFT JOIN
    loanproduct loanProd ON queryresp.LoanProduct_id = loanProd.id
        LEFT JOIN
    querycoborrower querycoborrower ON queryresp.id = querycoborrower.queryresponse_id
WHERE
    `queryresp`.`createdts` IS NOT NULL
        AND `queryresp`.`lastmodifiedts` IS NOT NULL
        AND ((`queryresp`.`QueryDefinition_id` = 'PERSONAL_APPLICATION')
        OR (`queryresp`.`QueryDefinition_id` = 'VEHICLE_APPLICATION')
        OR (`queryresp`.`QueryDefinition_id` = 'CREDIT_CARD_APPLICATION'))
        AND (`queryresp`.`Customer_id` = User_id
        OR `queryresp`.`CoBorrower_id` = User_id)
ORDER BY StartDate DESC;
END ;;
DELIMITER ;

DROP PROCEDURE IF EXISTS `sp_getAPRs`;
DELIMITER ;;
CREATE PROCEDURE `sp_getAPRs`()
BEGIN

SELECT 

    loantype.id AS LoanType_id,

    loantype.APRValue AS APRValue,

	(select cast(concat('[',group_concat(json_object('LoanProduct_id',lp.id , 'APR',lp.APR, 'LoanType_id',lp.LoanType_id )separator ','),']') as char charset utf8mb4)from `loanproduct` lp where lp.loantype_id =loantype.id) AS LoanProduct

FROM

    loantype loantype;

END ;;
DELIMITER ;



DROP PROCEDURE IF EXISTS `sp_getCustomerPrequalifyPackage`;
DELIMITER ;;
CREATE PROCEDURE `sp_getCustomerPrequalifyPackage`(IN Customer_id varchar(50))
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

END ;;
DELIMITER ;



DROP PROCEDURE IF EXISTS `sp_getCustomerPrequalifyPackage`;
DELIMITER ;;
CREATE PROCEDURE `sp_getCustomerPrequalifyPackage`(IN Customer_id varchar(50))
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

END ;;
DELIMITER ;



DROP PROCEDURE IF EXISTS `sp_getDecisionFailureData`;
DELIMITER ;;
CREATE PROCEDURE `sp_getDecisionFailureData`(IN id_list varchar(50), IN job_id varchar(50), IN top INT, IN skip INT)
BEGIN
IF id_list IS NOT NULL THEN 
SELECT df.id,df.job_id,df.decision_id,df.failureTriggerJob_id,df.exception,cdd.Customer_id,cdd.CreditScore,cdd.EmploymentType,cdd.AnnualIncome,cdd.AccountBalance,cdd.Age,cdd.PrequalifyScore FROM decisionfailure df join customerdpdata cdd on baseAttributeValue=Customer_id where find_in_set(df.id , id_list) order by df.createdts,df.id LIMIT top OFFSET skip;
ELSEIF job_id IS NOT NULL THEN 
SELECT df.id,df.job_id,df.decision_id,cdd.Customer_id,df.exception,cdd.CreditScore,cdd.EmploymentType,cdd.AnnualIncome,cdd.AccountBalance,cdd.Age,cdd.PrequalifyScore FROM decisionfailure df join customerdpdata cdd on baseAttributeValue=Customer_id where df.job_id=job_id order by df.createdts,df.id LIMIT top OFFSET skip;
ELSE
SELECT df.id,df.job_id,df.decision_id,df.failureTriggerJob_id,df.exception,cdd.Customer_id,cdd.CreditScore,cdd.EmploymentType,cdd.AnnualIncome,cdd.AccountBalance,cdd.Age,cdd.PrequalifyScore FROM decisionfailure df join customerdpdata cdd on baseAttributeValue=Customer_id where failureTriggerJob_id is null order by df.createdts,df.id LIMIT top OFFSET skip;
END IF;
END ;;
DELIMITER ;



DROP PROCEDURE IF EXISTS `sp_getLoanAnswers`;
DELIMITER ;;
CREATE PROCEDURE `sp_getLoanAnswers`(IN QueryResponseID varchar(100))
BEGIN
SELECT 
    querySecQue.abstractName AS Question_FriendlyName,
    questionResp.id AS QuestionResponse_id,
    questionResp.ResponseValue AS Value,
    questionResp.OptionItem_id AS OptionItem_id
FROM
    questionresponse questionResp
        INNER JOIN
    querysectionquestion querySecQue ON querySecQue.id = questionResp.QuerySectionQuestion_id
WHERE
    questionResp.QueryResponse_id = QueryResponseID;
END ;;
DELIMITER ;




DROP PROCEDURE IF EXISTS `sp_getQueryAnswers`;

DELIMITER ;;
CREATE PROCEDURE `sp_getQueryAnswers`(IN id varchar(50))
BEGIN

SET @FirstPart = "SELECT 
	queryresp.id as id, 
	queryresp.QueryDefinition_id as QueryDefinition_id,
	queryresp.Customer_id as Customer_id,
	queryresp.LoanProduct_id as LoanProduct_id,
	queryresp.Status_id as Status_id,
	queryresp.SubmitDate as SubmitDate,
	queryresp.ClosingDate as ClosingDate,
	queryresp.OverallPercentageCompletion as OverallPercentageCompletion,
	queryresp.createdby as createdby,
	queryresp.modifiedby as modifiedby,
	queryresp.createdts as createdts,
	queryresp.lastmodifiedts as lastmodifiedts,
	queryresp.synctimestamp as synctimestamp,
	queryresp.softdeleteflag as softdeleteflag,
	(select cast(concat('[',group_concat(json_object(
	'id',questResp.id ,
	'QueryDefinition_id',questResp.QueryDefinition_id,
	'QueryResponse_id',questResp.QueryResponse_id,
	'QuestionDefinition_id',questResp.QuestionDefinition_id,
	'QuerySection_id',questResp.QuerySection_id,
	'ArrayIndex',questResp.ArrayIndex,
	'ResponseValue',questResp.ResponseValue,
	'Unit',questResp.Unit,
	'createdby',questResp.createdby,
	'modifiedby',questResp.modifiedby,
	'createdts',questResp.createdts,
	'lastmodifiedts',questResp.lastmodifiedts,
	'synctimestamp',questResp.synctimestamp,
	'softdeleteflag',questResp.softdeleteflag) separator ','),']') as char charset utf8mb4)
	FROM `questionresponse` questResp WHERE questResp.QueryResponse_id = queryresp.id) AS `QuestionResponse`,
	(select cast(concat('[',group_concat(json_object(
	'id',cqss.id ,
	'User_id',cqss.User_id,
	'QueryResponse_id',cqss.QueryResponse_id,
	'QuerySection_id',cqss.QuerySection_id,
	'Status',cqss.Status,
	'PercentageCompletion',cqss.PercentageCompletion,
    'LastQuerySectionQuestion_id',cqss.LastQuerySectionQuestion_id,
	'createdby',cqss.createdby,
	'modifiedby',cqss.modifiedby,
	'createdts',cqss.createdts,
	'lastmodifiedts',cqss.lastmodifiedts,
	'synctimestamp',cqss.synctimestamp,
	'softdeleteflag',cqss.softdeleteflag) separator ','),']') as char charset utf8mb4)
	FROM `customerquerysectionstatus` cqss WHERE cqss.QueryResponse_id = queryresp.id) AS `CustomerQuerySectionStatus`
	FROM `queryresponse` queryresp
WHERE queryresp.id = ";
SET @Query = CONCAT(@FirstPart, "'", id, "';");
PREPARE queryStatement FROM @Query;
EXECUTE queryStatement;
DEALLOCATE PREPARE queryStatement;
END ;;
DELIMITER ;



DROP PROCEDURE IF EXISTS `sp_getQueryDefinition`;


DELIMITER ;;
CREATE PROCEDURE `sp_getQueryDefinition`(IN id varchar(500), IN Parent_id varchar(500))
BEGIN
SET @FirstPart = "SELECT 
    `queryDef`.`id` AS QueryDefinition_id,
    `queryDef`.`Name` AS QueryDefinition_Name,
    `querySecQue`.`id` AS QuerySectionQuestion_id,
    `querySecQue`.`ParentQuerySectionQuestion_id` AS ParentQuerySectionQuestion_id,
    `querySecQue`.`ParentQuestionOptionValue` AS ParentQuestionOptionValue,
    `querySecQue`.`Sequence` AS QuerySectionQuestion_Sequence,
    `querySecQue`.`IsRequired` AS QuerySectionQuestion_IsRequired,
    `querySec`.`id` AS QuerySection_id,
    `querySec`.`Name` AS QuerySection_Name,
    `querySec`.`Sequence` AS QuerySection_Sequence,
	`querySec`.`Parent_id` AS QuerySection_Parent_id,
    `querySec`.`ApplicantAllowedTo` AS QuerySection_ApplicantAllowedTo,
    `querySec`.`IndCoApplicantAllowedTo` AS QuerySection_IndCoApplicantAllowedTo,
    `querySec`.`JointCoApplicantAllowedTo` AS QuerySection_JointCoApplicantAllowedTo,
	`questionDef`.`Name` AS QuestionDefinition_Name,
    `questionDef`.`Label` AS QuestionDefinition_Label,
    `questionDef`.`OtherLabel` AS QuestionDefinition_OtherLabel,
    `questionDef`.`id` AS QuestionDefinition_id,
    `optiongroup`.`Name` AS OptionGroup_Name,
    `optiongroup`.`id` AS OptionGroup_id,
    `optionitem`.`id` AS Optionitem_id,
    `optionitem`.`DefaultValue` AS Optionitem_DefaultValue,
    `optionitem`.`Sequence` AS Optionitem_Sequence
FROM
    querydefinition queryDef
        LEFT JOIN
    querysectionquestion querySecQue ON querySecQue.QueryDefinition_id = queryDef.id
        LEFT JOIN
    querysection querySec ON querySec.id = querySecQue.QuerySection_id
        LEFT JOIN
    questiondefinition questionDef ON querySecQue.QuestionDefinition_id = questionDef.id
        LEFT JOIN
    optiongroup optiongroup ON questionDef.OptionGroup_id = optiongroup.id
        LEFT JOIN
    optionitem optionitem ON optiongroup.id = optionitem.OptionGroup_id
WHERE
    queryDef.id = ";
SET @SecondPart = CONCAT(@FirstPart, "'", id, "'", " AND querySec.softdeleteflag = FALSE AND querySecQue.softdeleteflag = FALSE ");
IF Parent_id = 'COAPPLICANT' THEN
    SET @ThirdPart = CONCAT(@SecondPart, "AND querySec.Parent_id IN ('COAPPLICANT') ");
ELSE 
	IF Parent_id = 'APPLICANT' THEN
		SET @ThirdPart = CONCAT(@SecondPart, "AND querySec.Parent_id IN ('APPLICANT', 'GENERAL') ");
	ELSE 
		SET @ThirdPart = CONCAT(@SecondPart, "");
	END IF;
END IF;
SET @Query = CONCAT(@ThirdPart, "ORDER BY QuerySection_Sequence,QuerySectionQuestion_Sequence,Optionitem_Sequence");
PREPARE queryStatement FROM @Query;
EXECUTE queryStatement;
DEALLOCATE PREPARE queryStatement;
END ;;
DELIMITER ;




DROP PROCEDURE IF EXISTS `sp_getQueryResponse`;
DELIMITER ;;
CREATE PROCEDURE `sp_getQueryResponse`(IN id varchar(50))
BEGIN

SET @FirstPart = "SELECT 
queryresp.id as id, 
queryresp.QueryDefinition_id as QueryDefinition_id,
queryresp.User_id as User_id,
queryresp.LoanProduct_id as LoanProduct_id,
queryresp.Status_id as Status_id,
queryresp.SubmitDate as SubmitDate,
queryresp.ClosingDate as ClosingDate,
queryresp.OverallPercentageCompletion as OverallPercentageCompletion,
queryresp.createdby as createdby,
queryresp.modifiedby as modifiedby,
queryresp.createdts as createdts,
queryresp.lastmodifiedts as lastmodifiedts,
queryresp.synctimestamp as synctimestamp,
queryresp.softdeleteflag as softdeleteflag,
(select cast(concat('[',group_concat(json_object(
'id',cqss.id ,
'User_id',cqss.User_id,
'QueryResponse_id',cqss.QueryResponse_id,
'QuerySection_id',cqss.QuerySection_id,
'Status',cqss.Status,
'PercentageCompletion',cqss.PercentageCompletion,
'createdby',cqss.createdby,
'modifiedby',cqss.modifiedby,
'createdts',cqss.createdts,
'lastmodifiedts',cqss.lastmodifiedts,
'synctimestamp',cqss.synctimestamp,
'softdeleteflag',cqss.softdeleteflag) separator ','),']') as char charset utf8mb4)
FROM `customerquerysectionstatus` cqss WHERE cqss.QueryResponse_id = queryresp.id) AS `CustomerQuerySectionStatus`
FROM `queryresponse` queryresp
WHERE queryresp.id = ";
SET @Query = CONCAT(@FirstPart, "'", id, "';");
PREPARE queryStatement FROM @Query;
EXECUTE queryStatement;
DEALLOCATE PREPARE queryStatement;
END ;;
DELIMITER ;





DROP PROCEDURE IF EXISTS `sp_getQueryResponseApplicationList`;

DELIMITER ;;
CREATE PROCEDURE `sp_getQueryResponseApplicationList`(IN id varchar(20000),IN Status_id varchar(20000))
BEGIN
SELECT 
    queryresp.id AS id,
    queryresp.QueryDefinition_id AS QueryDefinition_id,
    queryresp.OverallPercentageCompletion AS progress,
    queryresp.Status_id AS Status_id,
    queryresp.createdts AS createdTS,
    queryresp.lastmodifiedts AS modifiedTS,
    loantype.description AS loantype,
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
                                        questResp.Unit)
                                SEPARATOR ','),
                            ']')
                    AS CHAR CHARSET UTF8MB4)
        FROM
            `questionresponse` questResp
        WHERE
            questResp.QueryResponse_id = queryresp.id
                AND (questResp.QuestionDefinition_id = 'QUESTION_LOAN_AMOUNT'
                OR questResp.QuestionDefinition_id = 'QUESTION_COAPPLICANT_FULL_NAME'
                OR questResp.QuestionDefinition_id = 'QUESTION_COAPPLICANT_EMAIL_ADDRESS'
                OR questResp.QuestionDefinition_id = 'QUESTION_COBORROWER'
                OR questResp.QuestionDefinition_id = 'QUESTION_COAPPLICANT_HOME_NUMBER'
                OR questResp.QuestionDefinition_id = 'QUESTION_COAPPLICANT_MOBILE_NUMBER'
                OR questResp.QuestionDefinition_id = 'QUESTION_COAPPLICANT_OFFICE_NUMBER'
                OR questResp.QuerySectionQuestion_id = 'CCA_CARDTYPE'
                OR questResp.QuerySectionQuestion_id = 'VA_LOAN_AMOUNT')) AS `QuestionResponse`
FROM
    `queryresponse` queryresp
        LEFT JOIN
    loanproduct loanproduct ON loanproduct.id = queryresp.LoanProduct_id
        LEFT JOIN
    loantype loantype ON loantype.id = loanproduct.LoanType_id
WHERE
    queryresp.User_id = id
        AND queryresp.Status_id = Status_id
        AND (queryresp.QueryDefinition_id = 'PERSONAL_APPLICATION'
        OR queryresp.QueryDefinition_id = 'CREDIT_CARD_APPLICATION'
        OR queryresp.QueryDefinition_id = 'VEHICLE_APPLICATION')
ORDER BY queryresp.lastmodifiedts DESC;
END ;;
DELIMITER ;




DROP PROCEDURE IF EXISTS `sp_getQuestionOptions`;


DELIMITER ;;
CREATE PROCEDURE `sp_getQuestionOptions`(

	IN `QueryDefinitionID` varchar(50)

)
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

      CAST(CONCAT('[', GROUP_CONCAT(JSON_OBJECT('Question_FriendlyName', querysectionquestion.abstractName, 'ParentQuestion_FriendlyName', querysectionquestion.ParentQuerySectionQuestion_AbstractName, 'ParentQuestion_Value', querysectionquestion.ParentQuestionOptionValue, 'OptionItems', ( 

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

) ) SEPARATOR ','), ']') AS CHAR CHARSET UTF8MB4) 

      FROM

        `querysectionquestion` querysectionquestion 

      WHERE

        querySec.id = querysectionquestion.QuerySection_id 

  ), '')

  AS `QuestionDefinition` 

FROM

  querysection querySec 

WHERE

  querySec.QueryDefinition_id = QueryDefinitionID AND querySec.softdeleteflag = FALSE

  ORDER BY querySec.Sequence;
END ;;
DELIMITER ;





DROP PROCEDURE IF EXISTS `sp_getQuestionResponse`;


DELIMITER ;;
CREATE PROCEDURE `sp_getQuestionResponse`(

	IN `QueryDefinition_id` VARCHAR(500)



,

	IN `User_id` VARCHAR(50)



)
BEGIN

 

SET @basequery = "SELECT 

    quesresp.id as id,

    quesresp.QueryResponse_id as QueryResponse_id,

    quesresp.QueryDefinition_id as QueryDefinition_id,

    quesresp.QuestionDefinition_id as QuestionDefinition_id,

    quesresp.QuerySection_id as QuerySection_id,

    quesresp.ArrayIndex as ArrayIndex,

    quesresp.ResponseValue as ResponseValue,

    quesresp.Unit as Unit,

    quesresp.createdby as createdby,

    quesresp.modifiedby as modifiedby,

    quesresp.createdts as createdts,

    quesresp.lastmodifiedts as lastmodifiedts,

    quesresp.synctimestamp as synctimestamp,

    quesresp.softdeleteflag as softdeleteflag    

    FROM questionresponse quesresp 

    WHERE QueryResponse_id = (SELECT id from queryresponse queryresp WHERE queryresp.QueryDefinition_id = ";

    

    SET @SubQueryFirst = "queryresp.Customer_id = ";

    SET @SubQuerySecond = "ORDER BY queryresp.lastmodifiedts DESC ";

SET @MainQuery = CONCAT(@basequery, "'", QueryDefinition_id, "'", " AND ", @SubQueryFirst, "'", User_id, "'", @SubQuerySecond, "limit 1)");

 

PREPARE queryStatement FROM @MainQuery;

EXECUTE queryStatement;

DEALLOCATE PREPARE queryStatement; 

 

END ;;
DELIMITER ;

DROP procedure IF EXISTS `sp_deleteCoApplicantData`;
 
DELIMITER ;;
CREATE PROCEDURE `sp_deleteCoApplicantData`(IN sectionsToDelete varchar(200), IN applicationID varchar(100))
BEGIN
SET @FirstPart = "update questionresponse set softdeleteflag =1 where QuerySectionQuestion_id in (select id from querysectionquestion where QuerySection_id IN (";
SET @Query = CONCAT(@FirstPart, sectionsToDelete, ")) AND questionresponse.QueryResponse_id = \'" ,applicationID, "\';");
PREPARE queryStatement FROM @Query;
EXECUTE queryStatement;
DEALLOCATE PREPARE queryStatement;
END;;
 
DELIMITER ;

ALTER TABLE `querycoborrower` ADD COLUMN `Key` VARCHAR(50) NULL;
ALTER TABLE `loanproduct` CHANGE COLUMN `Image` `Image` LONGTEXT NULL DEFAULT NULL ;
ALTER TABLE `questionresponse` ADD COLUMN `customsoftdeleteflag` INT(11) NULL AFTER `softdeleteflag`;

DROP procedure IF EXISTS `sp_deleteCoApplicantData`; 
DELIMITER $$
CREATE PROCEDURE `sp_deleteCoApplicantData`(IN sectionsToDelete varchar(200), IN applicationID varchar(100))
BEGIN
SET @FirstPart = "update questionresponse set customsoftdeleteflag =1 where QuerySectionQuestion_id in (select id from querysectionquestion where QuerySection_id IN (";
SET @Query = CONCAT(@FirstPart, sectionsToDelete, ")) AND questionresponse.QueryResponse_id = \'" ,applicationID, "\';");
PREPARE queryStatement FROM @Query;
EXECUTE queryStatement;
DEALLOCATE PREPARE queryStatement;
END$$
DELIMITER ;


--
-- Table structure for table `vehicletypes`
--

DROP TABLE IF EXISTS `vehicletypes`;
CREATE TABLE `vehicletypes` (
  `VehicleTypeId` int(11) NOT NULL,
  `ParentVehicleTypeName` varchar(50) DEFAULT NULL,
  `VehicleTypeName` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`VehicleTypeId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `vehiclemakes`
--

DROP TABLE IF EXISTS `vehiclemakes`;
CREATE TABLE `vehiclemakes` (
  `id` varchar(500) NOT NULL,
  `MakeId` int(11) DEFAULT NULL,
  `MakeName` varchar(500) DEFAULT NULL,
  `VehicleTypeId` int(11) DEFAULT NULL,
  `VehicleTypeName` varchar(500) DEFAULT NULL,
  `ParentVehicleTypeName` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `vehiclemodels`
--

DROP TABLE IF EXISTS `vehiclemodels`;
CREATE TABLE `vehiclemodels` (
  `id` varchar(500) NOT NULL,
  `Model_ID` int(11) DEFAULT NULL,
  `Model_Name` varchar(500) DEFAULT NULL,
  `VehicleTypeId` int(11) DEFAULT NULL,
  `VehicleTypeName` varchar(500) DEFAULT NULL,
  `ParentVehicleTypeName` varchar(500) DEFAULT NULL,
  `Make_ID` int(11) DEFAULT NULL,
  `Make_Name` varchar(500) DEFAULT NULL,
  `Year` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;