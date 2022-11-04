package com.kony.adminconsole.loans.service.messaging;

public interface MessagingConstants {
	
	// URLS
	public static final String CLOUD_LOGIN = "loginCloud";
	public static final String PUSH_NOTIFICATION_STATUS = "PushNotification.Status";
	public static final String SMS_STATUS = "SMS.Status";
	
	// configuration
	public static final String CACHETIMEEXPIRY = "CacheTimeExpiry";
	
	// filenames
	public static final String CONFIGURATION_PROPERTIES = "Configuration.properties";
	
	// custom entitlements
	public static final String IS_EMAIL_ACTIVE = "IsEmailActive";
	public static final String IS_SMS_ACTIVE = "IsSmsActive";
	public static final String IS_PUSH_ACTIVE = "IsPushActive";
	public static final String CUSTOMER_ID = "Customer_id";
	public static final String ALERT_ID = "Alert_id";
	public static final String SOFTDELETEFLAG = "softdeleteflag";
	
	// customer
	public static final String ID = "id";
	public static final String FIRSTNAME = "FirstName";
	public static final String MIDDLENAME = "MiddleName";
	public static final String LASTNAME = "LastName";
	public static final String SALUTATION = "Salutation";
	public static final String EMAIL = "Email";
	public static final String PHONE = "Phone";
	public static final String PREFERRED_LANGUAGE = "PreferredLanguage";
	public static final String PSW_MODIFIED_TIMESTAMP = "PasswordModifiedTimeStamp";
	
	// templates
	public static final String TEMPLATE_ID = "template_Id";
	public static final String TEMPLATENAME = "TemplateName";
	public static final String TEMPLATETEXT = "TemplateText";
	public static final String SUBJECT = "Subject";
	public static final String ALERTCHANNEL = "AlertChannel";
	public static final String ALERTLANGUAGECODE = "AlertLanguageCode";
	
	// alert
	public static final String ALERTTYPE_ID = "AlertType_id";
	
	// alert logs
	public static final String STATUS = "Status";
	public static final String MESSAGE = "Message";
	public static final String ALERT_STATUS_ID = "AlertStatusId";
	public static final String LASTUPDATEDTIMESTAMP = "LastUpdatedTimeStamp";
	public static final String CREATEDTIMESTAMP = "CreatedTimeStamp";
	public static final String LOG_ID = "Log_id";
	
	// constructed response params
	public static final String EMAIL_KEY = "email";
	public static final String SMS_KEY = "sms";
	public static final String PUSH_KEY = "push";
	
	// request params
	public static final String CUSTOMER_ID_KEY = "customer_id";
	public static final String ALERT_ID_KEY = "alert_id";
	
	// odata
	public static final String FILTER = "$filter";
	
	// relational operators
	public static final String EQ = " eq ";
	public static final String NE = " ne ";
	public static final String AND = " and ";
	
	// misc
	public static final String EMPTYSTRING = "";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String NOT_AVAILABLE = "NA";
	public static final String SLASH = "/";
	
	// alert status
	public static final String PUSHED = "Pushed";
	public static final String REJECTED = "Rejected";
	public static final String FAILED = "Failed";
	public static final String SUBMITTED = "Submitted";
	public static final String MESSAGE_SENT = "Message Sent";
	
	// login
	public static final String USERID = "userid";
	public static final String PASSWORD = "password";
	public static final String CLAIMS_TOKEN = "claims_token";
	public static final String VALUE = "value";
	public static final String X_KONY_AUTHORIZATION = "X-Kony-Authorization";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String APPLICATION_JSON = "application/json";
	
	// date formats
	public static final String UTC_DATE_FORMAT = "yyyy-MM-dd'T'hh:mm:ss";
	public static final String ONLY_DATE_FORMAT = "yyyy-MM-dd'T'hh:mm:ss";
	
	// type of url
	public static final String KMS = "KMS";

	// push notification status params
	public static final String MESSAGES = "messages";
	public static final String STATUS_KEY = "status";
	public static final String STATUSMESSAGE_KEY = "statusMessage";
	public static final String REQUEST_ID = "requestId";
	public static final String IS_CUSTOMER = "isCustomer";
	public static final String USERFIRSTNAME = "Userfirstname";
	public static final String USERLASTNAME = "Userlastname";
	public static final String PHONE_KEY = "phone";
	
	//Messaging ErrorMessages
	public static final String TWILIO_NULL_STATUS_MESSAGE = "Message Sending Failed.";
	public static final String TWILIO_NULL_Sid_MESSAGE = "No id Reveived from Server.";
	public static final String TWILIO_SUCCESS_MESSAGE="queued";
}
