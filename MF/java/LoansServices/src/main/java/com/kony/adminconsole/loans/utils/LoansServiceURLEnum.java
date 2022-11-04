package com.kony.adminconsole.loans.utils;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.konylabs.middleware.api.processor.manager.FabricRequestManager;
import com.konylabs.middleware.controller.DataControllerRequest;

public enum LoansServiceURLEnum {
	
	CUSTOMER_QUERY_SECTION_STATUS_UPDATE("CustomerQuerySectionStatus.updateRecord"),
	CUSTOMER_QUERY_SECTION_STATUS_CREATE("CustomerQuerySectionStatus.createRecord"),
    QUESTION_RESPONSE_CREATE("QuestionResponse.createRecord"),
    QUERY_COBORROWER_CREATE("QueryCoborrower.createRecord"),
    QUERY_COBORROWER_UPDATE("QueryCoborrower.updateRecord"),
    QUERY_COBORROWER_GET("QueryCoborrower.getRecord"),
    QUERY_COBORROWER_DELETE("QueryCoborrower.deleteRecord"),
    QUERY_RESPONSE_CREATE("QueryResponse.createRecord"),
	QUERY_RESPONSE_GET("QueryResponse.getRecord"),
	QUERY_RESPONSE_GET_("QueryResponse.getRecord1"),
	QUERY_RESPONSE_UPDATE_TIMESTAMP("QueryResponse.updateTimeStamp"),
	LOANS_GET("Loans.getRecord"),
	LOANTYPE_GET("LoanType.getRecord"),
	CUSTOMER_GET("Customer.getRecord"),
	CUSTOMER_GETINFO("Customer.GetInfo"),
	USER_UPDATE("User.updateRecord"),
	USER_GET("User.readRecord"),
	CUSTOMER_UPDATE("Customer.updateRecord"),
	CUSTOMER_CREATE("Customer.CreateRecord"),
	CUSTOMERCOMMUNICATION_CREATE("CustomerCommunication.CreateRecord"),
	CUSTOMERCOMMUNICATION_UPDATE("CustomerCommunication.UpdateRecord"),
	CUSTOMERCOMMUNICATION_GET("CustomerCommunication.GetRecord"),
	CUSTOMERADDRESS_CREATE("CustomerAddress.CreateRecord"),
	CUSTOMERADDRESS_UPDATE("CustomerAddress.UpdateRecord"),
	CUSTOMERADDRESS_GET("CustomerAddress.GetRecord"),
	ADDRESS_CREATE("Address.CreateRecord"),
	ADDRESS_UPDATE("Address.UpdateRecord"),
	DISCLAIMER_GET("Disclaimer.getRecord"),
	QUESTION_RESPONSE_UPDATE("QuestionResponse.updateRecord"),
	QUESTION_RESPONSE_GET("QuestionResponse.getRecord"),
	QUESTION_RESPONSE_DELETECOBBOWERDATA("QuestionResponse.deleteCoApplicantData"),
	QUERY_RESPONSE_UPDATE("queryResponse.updateRecord"),
	QUESTION_RESPONSE_DELETE("QuestionResponse.deleteRecord"),
	GET_TOKEN_CUSTOMAPI_GET("GetAccessAPILogin"),
	LOANS_CUSTOMER_CREATE("LoanCustomer.createRecord"),
	QUERY_SECTION_QUESTION_GET("QuerySectionQuestion.getRecord"),
	QUESTION_OPTIONS_GET("QuestionOptions.getRecord"),
	LOAN_ANSWERS_GET("GetLoanAnswers.getRecord"),
	LOANPRODUCT_GET("LoanProduct.getRecord"),
	CUSTOMER_QUERY_SECTION_STATUS_GET("CustomerQuerySectionStatus.getRecord"),
	MESSAGING_SENDEMAIL("Messaging.sendEmail"),
	MESSAGING_SENDSMS("Messaging.sendSMS"),
	MESSAGING_CREATEKMS("Messaging.createKMS"),
	QUERY_SECTION_GET("QuerySection.getRecord"),
	CONFIGURATIONS_GET("LoansConfigurations.getRecord"),
	LOANTYPE_PING("LoanType.Ping"),
	APP_URL("APP_URL"), 
	VEHICLE_TYPE_OBJECT("VehicleTypes.object"),
	VEHICLE_MAKES_OBJECT("VehicleMakes.object"),
	VEHICLE_MODELS_OBJECT("VehicleModels.object"),
	NHTSAVEHICLEAPI_GETMAKESFORVEHICLETYPE("GetMakesForVehicleType"),
	NHTSAVEHICLEAPI_GETMODELSFORMAKEIDYEAR("GetModelsForMakeIdYear"),
	GET_APPLICATION_CUSTOMER("GetApplicationCustomer_Url"),
	NHTSAVEHICLEAPI_DECODEVINVALUES("DecodeVinValues");
    private static final Logger LOG = Logger.getLogger(LoansServiceURLEnum.class);
    private static final Properties PROPS = loadProps();
    private static final String SCHEMA_NAME_PLACE_HOLDER = "{schema_name}";

    private String serviceURLKey;

    private LoansServiceURLEnum(String serviceURLKey) {
        this.serviceURLKey = serviceURLKey;
    }

    private static Properties loadProps() {
        Properties properties = new Properties();
        try (InputStream serviceConfigInputStream =
                LoansServiceURLEnum.class.getClassLoader().getResourceAsStream("LoansServiceURLCollection.properties");) {
            properties.load(serviceConfigInputStream);
        } catch (Exception e) {
            LOG.error("Error occured while loading LoansServiceURLCollection.properties", e);
        }
        return properties;
    }

    private static String getValue(String key) {
        return PROPS.getProperty(key);
    }

    public static String getBaseURL(DataControllerRequest dataControllerRequest) {
        if (dataControllerRequest == null)
            return null;
        StringBuilder builder = new StringBuilder();
        builder.append(getValue("httpProtocol"));
        builder.append("://");
        String host = dataControllerRequest.getHeader("host");
        if (StringUtils.isBlank(host)) {
            host = dataControllerRequest.getHeader("Host");
        }
        LOG.debug("Resolved Host Header value: " + String.valueOf(host));
        builder.append(host);
        return builder.toString();
    }
    
    public static String getBaseURL(FabricRequestManager fabricRequestManager) {
        if (fabricRequestManager == null)
            return null;
        StringBuilder builder = new StringBuilder();
        builder.append(getValue("httpProtocol"));
        builder.append("://");
        String host = fabricRequestManager.getHeadersHandler().getHeader("host");
        if (StringUtils.isBlank(host)) {
            host = fabricRequestManager.getHeadersHandler().getHeader("Host");
        }
        LOG.debug("Resolved Host Header value: " + String.valueOf(host));
        builder.append(host);
        return builder.toString();
    }

    public String getServiceURL(DataControllerRequest dataControllerRequest) {
        String baseURL = getBaseURL(dataControllerRequest);
        if (StringUtils.isBlank(baseURL)) {
            return null;
        }
        StringBuilder builder = new StringBuilder(baseURL);
        builder.append(getValue(serviceURLKey));
        String serviceURL = builder.toString();
		if(serviceURL.contains(SCHEMA_NAME_PLACE_HOLDER)) {
			serviceURL = serviceURL.replace(SCHEMA_NAME_PLACE_HOLDER, getDatabaseSchemaName(dataControllerRequest));
		}
		return serviceURL;
    }
    
    public String getServiceURL(FabricRequestManager fabricRequestManager) {
        String baseURL = getBaseURL(fabricRequestManager);
        if (StringUtils.isBlank(baseURL)) {
            return null;
        }
        StringBuilder builder = new StringBuilder(baseURL);
        builder.append(getValue(serviceURLKey));
        String serviceURL = builder.toString();
		if(serviceURL.contains(SCHEMA_NAME_PLACE_HOLDER)) {
			serviceURL = serviceURL.replace(SCHEMA_NAME_PLACE_HOLDER, getDatabaseSchemaName(fabricRequestManager));
		}
		return serviceURL;
    }
    
    public String getServiceName(DataControllerRequest dataControllerRequest) {
		String serviceName = null;
		String serviceURL = getValue(serviceURLKey);
		serviceName = serviceURL.substring(serviceURL.indexOf("/", 1) + 1, serviceURL.lastIndexOf("/"));
		return serviceName;
	}
    
    public String getServiceName(FabricRequestManager fabricRequestManager) {
		String serviceName = null;
		String serviceURL = getValue(serviceURLKey);
		serviceName = serviceURL.substring(serviceURL.indexOf("/", 1) + 1, serviceURL.lastIndexOf("/"));
		return serviceName;
	}

	public String getOperationName(DataControllerRequest dataControllerRequest) {
		String operationName = null;
		String serviceURL = getValue(serviceURLKey);
		operationName = serviceURL.substring(serviceURL.lastIndexOf("/") + 1);
		if(operationName.contains(SCHEMA_NAME_PLACE_HOLDER)) {
			operationName = operationName.replace(SCHEMA_NAME_PLACE_HOLDER, getDatabaseSchemaName(dataControllerRequest));
		}
		return operationName;
	}
	
	public String getOperationName(FabricRequestManager fabricRequestManager) {
		String operationName = null;
		String serviceURL = getValue(serviceURLKey);
		operationName = serviceURL.substring(serviceURL.lastIndexOf("/") + 1);
		if(operationName.contains(SCHEMA_NAME_PLACE_HOLDER)) {
			operationName = operationName.replace(SCHEMA_NAME_PLACE_HOLDER, getDatabaseSchemaName(fabricRequestManager));
		}
		return operationName;
	}
	
	public String getDatabaseSchemaName(DataControllerRequest requestInstance) {
		return EnvironmentConfigurationsHandler.getValue("DBX_SCHEMA_NAME", requestInstance);
	}
    
	public String getDatabaseSchemaName(FabricRequestManager fabricRequestManager) {
		return EnvironmentConfigurationsHandler.getValue("DBX_SCHEMA_NAME", fabricRequestManager);
	}

}
