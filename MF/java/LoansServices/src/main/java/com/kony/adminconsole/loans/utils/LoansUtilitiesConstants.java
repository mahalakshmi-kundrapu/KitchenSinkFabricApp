package com.kony.adminconsole.loans.utils;

public final class LoansUtilitiesConstants {
	private LoansUtilitiesConstants(){}
	public static final String OP_STATUS = "opstatus";
	public static final String HTTP_STATUS_CODE = "httpStatusCode";
	public static final String STATUS_CODE = "StatusCode";
	public static final String HTTP_SUCCESS_CODE = "200";
	public static final String X_KONY_AUTHORIZATION = "X-Kony-Authorization";
	public static final String X_KONY_AUTHORIZATION_VALUE = "x-kony-authorization";
	public static final String HTTP_ERROR_CODE = "403";
	public static final String RECORD_COUNT = "recordCount";
	public static final String ZERO = "0";
	public static final String FILTER = "$filter";
	public static final String ORDERBY = "$orderby";
	public static final String TOP = "$top";
	public static final String SKIP = "$skip";
	public static final String SELECT = "$select";
	public static final String SORTBY = "sortBy";
	public static final String ORDER = "order";
	public static final String VALIDATION_ERROR = "errmsg";
	public static final String RESULT_MSG = "result";
	public static final String SUCCESS_MSG = "Successful";
	public static final String STRING_TYPE = "String";
	public static final String OTP = "otp";
	public static final String RESULT_ATTRIBUTE_KEY = "ResultOnException";
	public static final String EQUAL = " eq ";
	public static final String NOT_EQ = " ne ";
	public static final String AND = " and ";
	public static final String OR = " or ";
	public static final String GREATER_EQUAL = " ge ";
	public static final String LESS_EQUAL = " le ";
	public static final String LESS_THAN = " lt ";
	public static final String GREATER_THAN = " gt ";
	public static final String SOFT_DELETE_FLAG = "softdeleteflag";
	
	/**
	 * User constants
	 */
	public static final String EMAIL = "email";
	public static final String COUNTRY_CODE = "countryCode";
	public static final String PHONE_NUMBER = "phone";
	public static final String USER_NAME = "userName";
	public static final String USR_NAME = "username";
	public static final String PWD_FIELD = "password";
	public static final String OLD_PWD_FIELD = "oldpassword";
	public static final String FIRST_NAME = "userFirstName";
	public static final String LAST_NAME = "userLastName";
	public static final String DS_USER = "customer";
	public static final String SESSION_ID = "Session_id";
	public static final String ROLE = "role";
	public static final String DOB = "dateOfBirth";
	public static final String SSN = "ssn";
	public static final String U_ID = "id";
	public static final String IS_BILL_PAY_ACTIVE = "isBillPayActivated";
	public static final String IS_P2P_ACTIVE = "isP2PActivated";
	public static final String DEF_ACCNT_BILLPAY = "default_account_billPay";
	public static final String IS_ENROLLED = "isEnrolled";
	public static final String USR_ATTR = "user_attributes";
	public static final String PIN = "pin";
	public static final String IS_PIN_SET = "isPinSet";
	public static final String ARE_USR_ALERTS_ON = "areUserAlertsTurnedOn";
	public static final String USR_ALERTS_ON = "alertsTurnedOn";
	public static final String CUSTOMER_GET = "Customer.readRecord";
	/**
	 * loan application constants
	 */
	public static final String QUESTION_RESPONSE = "QuestionResponse";
	public static final String CUSTOMER_QUERY_QUESTION_STATUS = "CustomerQuerySectionStatus";
	public static final String QUERY_COBORROWER = "QueryCoBorrower";
	public static final String INDIVIDUALLY = "Individually";
	public static final String QUERY_DEFINITION_ID = "QueryDefinition_id";
	public static final String SELECT_FEILDS = "id, abstractName";
	public static final String QUERY_SECTION_STATUS = "querysectionquestion";
	public static final String ABSTRACT_NAME = "abstractName";
	public static final String QUERY_RESPONSE_ID = "QueryResponse_id";
	public static final String ERROR_CODE = "dbpErrCode";
	public static final String ERROR_MESSAGE = "dbpErrMsg";
	public static final String OBJECT_READ_ERROR_CODE = "33402";
	public static final String SUBMITTED_STATUS = "SUBMITTED";
	/**
	 * usersecurity constants
	 */
	public static final String USER_SECURITY_TABLE = "usersecurity";
	public static final String PROVIDER_TOKEN = "provider_token";
	public static final String SECURITY_ATTRIBUTES = "security_attributes";
	public static final String SESSION_TOKEN = "session_token";
	/**
	 * useralerts constants
	 */
	public static final String UA_USR_ID = "User_id";
	/**
	 * usernotification constants
	 */
	public static final String UNREAD_COUNT = "UnreadNotificationCount";
	public static final String UN_USR_ID = "user_id";
	public static final String IS_READ = "isRead";
	public static final String RECEIVED_DATE = "receivedDate";
	public static final String TBL_USERNOTIFICATION = "usernotification";
	public static final String NOTIFICATION_ID = "notification_id";
	public static final String UN_ID = "id";
	
	/**
	 * userpersonalinfo constants
	 */
	public static final String PI_USR_ID = "User_id";
	public static final String PERSONL_INFO = "userPersonalInfo";
	public static final String EMPLOYMENT_INFO = "userEmploymentInfo";
	public static final String FINANCIAL_INFO = "userFinancialInfo";
	public static final String SECURITY_QUEST = "userSecurityQuestions";
	public static final String EMP_DOC = "employementDoc";
	public static final String ADDRESS_DOC = "addressDoc";
	public static final String INCOME_DOC = "incomeDoc";
	public static final String SIG_IMG = "signatureImage";
	public static final String STATE = "state";
	public static final String COUNTRY = "country";
	public static final String ZIP_CODE = "zipcode";
	public static final String JOB_PROF = "jobProfile";
	public static final String EXPERIENCE = "experience";
	public static final String ANNUAL_INCOME = "annualIncome";
	public static final String SPOUSE_NAME = "spousename";
	public static final String CITY = "city";
	public static final String DEPENDENT_NOS = "noOfDependents";
	public static final String ASSETS = "assests";
	public static final String PI_DOB = "dateOfBirth";
	public static final String GENDER = "gender";
	public static final String PI_FIRST_NAME = "userfirstname";
	public static final String PI_LAST_NAME = "userlastname";
	public static final String MARITAL_STATUS = "maritalStatus";
	public static final String SPOUSE_F_NAME = "spouseFirstName";
	public static final String SPOUSE_L_NAME = "spouseLastName";
	public static final String ADDRESS1 = "addressLine1";
	public static final String ADDRESS2 = "addressLine2";
	public static final String COMAPNY = "company";
	public static final String MONTHLY_EXP = "montlyExpenditure";
	public static final String EMP_INFO = "employmentInfo";
	/**
	 * usersecurityquestions
	 */
	public static final String USR_SERCURITY_QUES = "userSecurityQuestions";
	public static final String USR_SECURITY_LIST = "usersecurityli";
	public static final String JSON_QUES_ID = "question_id";
	public static final String JSON_ANS = "answer";
	public static final String QUESTION = "question";
	public static final String ANSWER = "answer";
	/**
	 * userproducts
	 */
	public static final String USR_PRODUCTS = "userProducts";
	public static final String PRODUCT_ID = "Product_id";
	public static final String PRODUCT_LIST = "productLi";
	public static final String JSON_PRD_ID = "productId";
	
	
	/**
	 * messages
	 */
	public static final String CATEGORY_ID = "Category_id";
	public static final String SUB_CATEGORY_ID = "Subcategory_id";
	public static final String M_RECEIVED_DATE = "receivedDate";
	public static final String M_SOFT_DEL_DATE = "softdeletedDate";
	public static final String M_CREATED_DATE = "createdDate";
	public static final String M_SENT_DATE = "sentDate";
	
	public static final String [] VehicleTypes= {"Bus","Incomplete Vehicle","Low Speed Vehicle (LSV)","Multipurpose Passenger Vehicle (MPV)","Passenger Car","Truck","Motorcycle","Trailer","Off Road Vehicle"};
	
	/**
	 * Service name constants
	 */
	public static final String Generate_OTP_Service_Two = "CoApplicantRequestAndSendOTP";
	public static final String Generate_OTP_Service = "DBXRequestAndSendSMSRegistration";
	public static final String Verify_OTP_Service_Two = "verifyCoApplicantOTP";
	public static final String Verify_OTP_Service = "dbxRegistrationValidateOTP";
	public static final String NewCustomer_Registration_Service = "createDBXProspectRegistration";
	public static final String VerifyUserNameService = "verifyDBXUserNameForRegistration";
	public static final String VerifydbxUserName_service = "VerifyUserName";
	public static final String GetPhoneandSendSMS_service = "getCustomerCommunication";
	public static final String Validate_dbx_OTP_service = "VerifyOtpForForgotPassword";
	public static final String Reset_Password_service = "ResetPasswordNonMember";
	public static final String Registration_SMS_Tempalte_Name="LoansRegistrationMessage";
	/**
	 * EnvironemntVariables Constants
	 */
	public static final String DEMO_FLAG_KEY="LOANS_DEMOFLAG_VALUE";
	public static final String DEV_FLAG_KEY="LOANS_DEVFLAG_VALUE";
	public static final String OTP_Param_Name="Otp";
	public static final String ENCRYPTIONKEY="ENCRYPTION_KEY";
	public static final String DEMO_TWILIO_NUMBER = "LOANS_DEMO_TWILIO_NUMBER";
	public static final String DEMO_OTP_NUMBER="LOANS_OTP_VALIDATION_VALUE";
	
	/**
	 * CoApplicant Constants
	 */
	public static final String COAPPLICANTCOBORROWERTYPE="CoBorrower_Type";
	public static final String ABSTRACTCOAPPLICANTCOBORROWERTYPE="LoanCoborrower";
	public static final String COAPPLICANTEMAIL="Email";
	public static final String ABSTRACTCOAPPLICANTEMAIL="CoApplicantEmail";
	public static final String COAPPLICANTFIRSTNAME="FirstName";
	public static final String ABSTRACTCOAPPLICANTFIRSTNAME="CoApplicantFirstName";
	public static final String COAPPLICANTLASTNAME="LastName";
	public static final String ABSTRACTCOAPPLICANTLASTNAME="CoApplicantLastName";
	public static final String COAPPLICANTMOBILENUMBER="PhoneNumber";
	public static final String ABSTRACTCOAPPLICANTMOBILENUMBER="CoApplicantMobileNumber";
	public static final String COAPPLICANTINVITATIONLINK="InvitationLink";
	public static final String COAPPLICANTINVITATIONLINKVALIDITY="InvitationLinkValidity";
	
	/**
	 * User id session URL
	 */
	public static final String USER_SESSION_URL="/session/user_attributes?provider=DBXLoginLoans";
	public static final String MF_IDENTITY_URL="LOANSSERVICES_BASE_IDENTITY_URL";
	
	/**
	 * Forgot Password flow Constants
	 */
	public static final String SMS_SENT_SUCCESSFULLY = "10062";
	
	/**
	 * DBP Identity Constants
	 */
    public static final String DBP_IDENTITY="DBP.Identity";
    public static final String SHARED_SECRET="sharedSecret";
    public static final String LOANS_SHARED_SECRET="LOANS_SHARED_SECRET";
    public static final String AC_DBP_AUTH_URL="AC_DBP_AUTH_URL";
    public static final String DBP_APP_KEY = "x-kony-app-key";
    public static final String DBP_APP_SECRET = "x-kony-app-secret";
    public static final String DBP_REPORTING_PARAMS = "X-Kony-ReportingParams";
    public static final String AC_DBP_APP_KEY = "AC_DBP_APP_KEY";
    public static final String AC_DBP_APP_SECRET = "AC_DBP_APP_SECRET";
    public static final String AC_DBP_AUTH_REPORTING_PARAMS = "AC_DBP_AUTH_REPORTING_PARAMS";   
    public static final String CLAIMS_TOKEN = "claims_token";
    
}