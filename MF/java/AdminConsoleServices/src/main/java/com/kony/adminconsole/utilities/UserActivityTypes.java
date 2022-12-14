package com.kony.adminconsole.utilities;

public enum UserActivityTypes {
    ROLE_CREATE, ROLE_UPDATE, ROLE_DELETE, PERMISSION_ASSIGN_TO_CUSTOMER, PERMISSION_REMOVE_FROM_CUSTOMER,
    GROUP_ASSIGN_TO_CUSTOMER, GROUP_REMOVE_FROM_CUSTOMER, PERMISSION_ASSIGN_TO_USER, PERMISSION_REMOVE_FROM_USER,
    PERMISSION_ASSIGN_TO_ROLE, PERMISSION_REMOVE_FROM_ROLE, PERMISSION_CREATE, PERMISSION_UPDATE, PERMISSION_DELETE,
    USER_CREATE, USER_UPDATE, USER_DELETE, USER_SUCCESSFUL_LOGIN, USER_FAILED_LOGIN, USER_PASSWORD_CHANGE,
    USER_PASSWORD_RESET, USER_ACCOUNT_SUSPENDED, PASSSWORD_CHANGE_SUCCESSFUL, PASSSWORD_CHANGE_FAILED,
    PASSSWORD_RESET_SUCCESSFUL, SECURITY_QUESTION_ADD_SUCCESSFUL, SECURITY_QUESTION_ADD_FAILED,
    SECURITY_QUESTION_DELETE_SUCCESSFUL, SECURITY_QUESTION_DELETE_FAILED, SECURITY_QUESTION_UPDATE_SUCCESSFUL,
    SECURITY_QUESTION_UPDATE_FAILED, USER_STATUS_CHANGE_SUCCESSFUL, USER_STATUS_CHANGE_FAILED, USER_EDIT_SUCCESSFUL,
    USER_EDIT_FAILED, SECURITY_IMAGE_ADD_SUCCESSFUL, SECURITY_IMAGE_ADD_FAILED, SECURITY_IMAGE_DELETE_SUCCESSFUL,
    SECURITY_IMAGE_DELETE_FAILED, SECURITY_IMAGE_UPDATE_SUCCESSFUL, SECURITY_IMAGE_UPDATE_FAILED,
    ROLE_CREATE_SUCCESSFUL, ROLE_CREATE_FAILED, ASSIGN_PERMISSIONS_TO_ROLE_SUCCESSFUL,
    ASSIGN_PERMISSIONS_TO_ROLE_FAILED, ASSIGN_USERS_TO_ROLE_SUCCESSFUL, ASSIGN_USERS_TO_ROLE_FAILED,
    CREATE_ROLE_WITH_PERMISSIONS_AND_USERS_SUCCESSFUL, CREATE_ROLE_WITH_PERMISSIONS_AND_USERS_FAILED,
    CUSTOMER_CONTACT_INFO_EDIT_SUCCESSFUL, CUSTOMER_CONTACT_INFO_EDIT_FAILED, CUSTOMER_ASSIGN_PERMISSIONS_SUCCESSFUL,
    CUSTOMER_ASSIGN_PERMISSIONS_FAILED, CUSTOMER_ASSIGN_GROUP_TO_CUSTOMER_SUCCESSFUL,
    CUSTOMER_ASSIGN_GROUP_TO_CUSTOMER_FAILED, ROLE_EDIT_SUCCESSFUL, ROLE_EDIT_FAILED,
    ROLE_ASSIGN_PERMISSIONS_TO_ROLE_SUCCESSFUL, ROLE_ASSIGN_PERMISSIONS_TO_ROLE_FAILED,
    ROLE_ASSIGN_USERS_TO_ROLE_SUCCESSFUL, ROLE_ASSIGN_USERS_TO_ROLE_FAILED, ROLE_WITH_PERMISSIONS_AND_USERS_SUCCESSFUL,
    ROLE_WITH_PERMISSIONS_AND_USERS_FAILED, ROLE_DELETE_PERMISSION_SUCCESSFUL, ROLE_EDIT_PERMISSION_FAILED,
    USER_REMOVE_ROLE_SUCCESSFUL, USER_REMOVE_ROLE_FAILED, UPDATE_SYSTEMCONFIG_PROP_SUCCESSFUL,
    UPDATE_SYSTEMCONFIG_PROP_FAILED, FAQ_CREATE_SUCCESSFUL, FAQ_CREATE_FAILED, FAQ_EDIT_SUCCESSFUL, FAQ_EDIT_FAILED,
    FAQ_DELETE_SUCCESSFUL, FAQ_DELETE_FAILED, PRIVACYPOLICY_CREATE_SUCCESSFUL, PRIVACYPOLICY_CREATE_FAILED,
    PRIVACYPOLICY_DELETE_SUCCESSFUL, PRIVACYPOLICY_DELETE_FAILED, PRIVACYPOLICY_EDIT_SUCCESSFUL,
    PRIVACYPOLICY_EDIT_FAILED, TERMSANDCONDITIONS_CREATE_SUCCESSFUL, TERMSANDCONDITIONS_CREATE_FAILED,
    TERMSANDCONDITIONS_DELETE_SUCCESSFUL, TERMSANDCONDITIONS_DELETE_FAILED, TERMSANDCONDITIONS_EDIT_SUCCESSFUL,
    TERMSANDCONDITIONS_EDIT_FAILED, USER_ASSIGN_ROLE, USER_REMOVE_ROLE, EMAIL_SEND_SUCCESSFUL, EMAIL_SEND_FAILED,
    SERVICE_CREATE_SUCCESSFUL, SERVICE_UPDATE_SUCCESSFUL, SERVICE_DELETE_SUCCESSFUL, SERVICE_CREATE_FAILED,
    SERVICE_UPDATE_FAILED, SERVICE_DELETE_FAILED, CUSTOMER_SERVICE_CREATE_SUCCESSFUL,
    CUSTOMER_SERVICE_UPDATE_SUCCESSFUL, CUSTOMER_SERVICE_DELETE_SUCCESSFUL, CUSTOMER_SERVICE_CREATE_FAILED,
    CUSTOMER_SERVICE_UPDATE_FAILED, CUSTOMER_SERVICE_DELETE_FAILED, CUSTOMER_SERVICE_COMM_CREATE_SUCESSFUL,
    CUSTOMER_SERVICE_COMM_UPDATE_SUCESSFUL, CUSTOMER_SERVICE_COMM_DELETE_SUCESSFUL, CUSTOMER_SERVICE_COMM_CREATE_FAILED,
    CUSTOMER_SERVICE_COMM_UPDATE_FAILED, CUSTOMER_SERVICE_COMM_DELETE_FAILED, CREATE_WORK_SCHEDULE_SUCCESSFUL,
    CREATE_WORK_SCHEDULE_FAILED, CUSTOMER_ENTITLEMENT_CREATE_SUCCESS, CUSTOMER_ENTITLEMENT_CREATE_FAILED,
    PERIODIC_LIMIT_CREATE_SUCCESS, PERIODIC_LIMIT_CREATE_FAILED, USERGROUP_PERIODS_CREATE_SUCCESSFUL,
    USERGROUP_PERIODS_CREATE_FAILED, TRANSACTION_LIMIT_USERGROUP_CREATE_SUCCESSFUL,
    TRANSACTION_LIMIT_USERGROUP_CREATE_FAILED, PERIODIC_LIMIT_USERGROUP_CREATE_SUCCESSFUL,
    PERIODIC_LIMIT_USERGROUP_CREATE_FAILED, PERIOD_USERGROUP_CREATE_FAILED, PERIOD_USERGROUP_CREATE_SUCCESSFUL,
    GROUP_ENTITLEMENT_USERGROUP_CREATE_SUCCESSFUL, GROUP_ENTITLEMENT_USERGROUP_CREATE_FAILED,
    PERIODIC_LIMIT_USERGROUP_UPDATE_SUCCESSFUL, PERIODIC_LIMIT_USERGROUP_UPDATE_FAILED,
    USERGROUP_PERIODS_UPDATE_SUCCESSFUL, USERGROUP_PERIODS_UPDATE_FAILED, PERIODIC_LIMIT_UPDATE_SUCCESS,
    PERIODIC_LIMIT_UPDATE_FAILED, PERIODIC_LIMIT_DELETE_SUCCESS, PERIODIC_LIMIT_DELETE_FAILED,
    TRANSACTION_LIMIT_CREATE_SUCCESS, TRANSACTION_LIMIT_CREATE_FAILED, CUSTOMER_UPDATE_PHONE_NUMBER_FAILED,
    CUSTOMER_UPDATE_PHONE_NUMBER_SUCCESS, CUSTOMER_CREATE_PHONE_NUMBER_FAILED, CUSTOMER_CREATE_PHONE_NUMBER_SUCCESSFUL,
    CUSTOMER_UPDATE_EMAIL_FAILED, CUSTOMER_UPDATE_EMAIL_SUCCESS, CUSTOMER_CREATE_EMAIL_FAILED,
    CUSTOMER_CREATE_EMAIL_SUCCESS, CUSTOMER_CREATE_NOTE_SUCCESSFUL, CUSTOMER_CREATE_NOTE_FAILED,
    LOCATION_CREATE_FROM_CSV_SUCCESSFUL, LOCATION_CREATE_FROM_CSV_FAILED, SERVICE_UPDATE_PERIODLIMIT_SUCCESS,
    SERVICE_UPDATE_PERIODLIMIT_FAILED, CUSTOMER_EDIT_BASIC_INFO_FAILED, CUSTOMER_EDIT_BASIC_INFO_SUCCESS,
    CITY_READ_FROM_LOCATION_CSV_SUCCESSFUL, CITY_READ_FROM_LOCATION_CSV_FAILED,
    REGION_READ_FROM_LOCATION_CSV_SUCCESSFUL, REGION_READ_FROM_LOCATION_CSV_FAILED,
    COUNTRY_READ_FROM_LOCATION_CSV_SUCCESSFUL, COUNTRY_READ_FROM_LOCATION_CSV_FAILED,
    SERVICE_READ_FROM_LOCATION_CSV_SUCCESSFUL, SERVICE_READ_FROM_LOCATION_CSV_FAILED,
    ADDRESS_CREATE_FROM_LOCATION_CSV_SUCCESSFUL, ADDRESS_CREATE_FROM_LOCATION_CSV_FAILED,
    WORKSCHEDULE_CREATE_FROM_LOCATION_CSV_SUCCESSFUL, WORKSCHEDULE_CREATE_FROM_LOCATION_CSV_FAILED,
    DAYSCHEDULE_CREATE_FROM_LOCATION_CSV_SUCCESSFUL, DAYSCHEDULE_CREATE_FROM_LOCATION_CSV_FAILED,
    LOCATION_CREATE_FROM_LOCATION_CSV_SUCCESSFUL, LOCATION_CREATE_FROM_LOCATION_CSV_FAILED,
    LOCATIONSERVICE_CREATE_FROM_LOCATION_CSV_SUCCESSFUL, LOCATIONSERVICE_CREATE_FROM_LOCATION_CSV_FAILED,
    LOCATIONFILE_CREATE_FROM_LOCATION_CSV_SUCCESSFUL, LOCATIONFILE_CREATE_FROM_LOCATION_CSV_FAILED,
    LOCATIONFILEMETA_CREATE_FROM_LOCATION_CSV_SUCCESSFUL, LOCATIONFILEMETA_CREATE_FROM_LOCATION_CSV_FAILED,
    LOCATIONFILE_READ_FROM_LOCATION_CSV_SUCCESSFUL, LOCATIONFILE_READ_FROM_LOCATION_CSV_FAILED,
    LOCATIONFILEMETA_READ_FROM_LOCATION_CSV_SUCCESSFUL, LOGVIEW_CREATE_SUCCESSFUL, LOGVIEW_CREATE_FAILED,
    LOGVIEW_DELETE_SUCCESSFUL, LOGVIEW_DELETE_FAILED, WORKSCHEDULE_CREATE_SUCESSFUL, WORKSCHEDULE_CREATE_FAILED,
    WORKSCHEDULE_UPDATE_SUCESSFUL, WORKSCHEDULE_UPDATE_FAILED, LOCATION_CREATE_SUCCESSFUL, LOCATION_CREATE_FAILED,
    LOCATION_UPDATE_SUCCESSFUL, LOCATION_UPDATE_FAILED, ADDRESS_CREATE_SUCCESSFUL, ADDRESS_CREATE_FAILED,
    ADDRESS_UPDATE_SUCCESSFUL, ADDRESS_UPDATE_FAILED, SERVICE_ASSIGN_TO_LOCATION_SUCCESSFUL,
    SERVICE_ASSIGN_TO_LOCATION_FAILED, SERVICE_REMOVE_FROM_LOCATION_SUCCESSFUL, SERVICE_REMOVE_FROM_LOCATION_FAILED,
    TRANSACTION_FEE_SLAB_CREATE_FAILED, TRANSACTION_FEE_SLAB_CREATE_SUCCESS, TRANSACTION_FEE_CREATE_SUCCESS,
    TRANSACTION_FEE_CREATE_FAILED, PERIOD_CREATE_FAILED, PERIOD_CREATE_SUCCESSFUL, PERIOD_UPDATE_FAILED,
    PERIOD_UPDATE_SUCCESS, PERIOD_GET_FAILED, PERIOD_GET_SUCCESS, TRANSFER_FEE_SERVICE_ADD_FAILED,
    TRANSFER_FEE_SERVICE_ADD_SUCCESS, LOCATIONFILEMETA_READ_FROM_LOCATION_CSV_FAILED, CUSTOMER_DELETE_ADDRESS_FAILED,
    CUSTOMER_DELETE_ADDRESS_SUCCESS, CUSTOMER_DELETE_COMMUNICATION_FAILED, CUSTOMER_DELETE_COMMUNICATION_SUCCESS,
    GROUP_CREATE_SUCCESSFUL, GROUP_CREATE_FAILED, GROUP_UPDATE_FAILED, GROUP_UPDATE_SUCCESSFUL,
    GROUPENTITLEMENT_CREATE_SUCCESSFUL, GROUPENTITLEMENT_CREATE_FAILED, GROUPENTITLEMENT_DELETE_SUCCESSFUL,
    GROUPENTITLEMENT_DELETE_FAILED, CUSTOMERSGROUP_CREATE_SUCCESSFUL, CUSTOMERSGROUP_CREATE_FAILED,
    TRANSACTION_FEE_GROUP_DELETE_FAILED, TRANSACTION_FEE_GROUP_DELETE_SUCCESS, TRANSACTION_FEE_GROUP_UPDATE_FAILED,
    TRANSACTION_FEE_GROUP_UPDATE_SUCCESS, TRANSACTIONGROUPSERVICE_CREATE_FAILED, TRANSACTIONGROUPSERVICE_CREATE_SUCCESS,
    TRANSACTIONGROUP_CREATE_FAILED, TRANSACTIONGROUP_CREATE_SUCCESS, TRANSACTIONGROUP_UPDATE_FAILED,
    TRANSACTIONGROUP_UPDATE_SUCCESS, TRANSACTION_FEES_CREATE_FAILED, TRANSACTION_FEES_CREATE_SUCCESSFUL,
    TRANSACTION_FEESSLAB_CREATE_FAILED, TRANSACTION_FEESSLAB_CREATE_SUCCESSFUL, TRANSACTION_FEESSLAB_UPDATE_SUCCESSFUL,
    TRANSACTION_FEESSLAB_UPDATE_FAILED, TRANSACTION_FEESSLAB_DELETE_SUCCESSFUL, PERIODIC_DELETE_SUCCESS,
    PERIODIC_DELETE_FAILED, TRANSACTION_FEESSLAB_DELETE_FAILED, PRODUCT_CREATE_SUCCESS, PRODUCT_CREATE_FAILED,
    PRODUCT_UPDATE_FAILED, PRODUCT_UPDATE_SUCCESS, PRODUCT_PARTIAL_IMPORT_SUCCESS, PRODUCT_IMPORT_SUCCESS,
    PRODUCT_IMPORT_FAILED, CUSTOMERFILE_READ_FROM_CUSTOMER_CSV_FAILED, CUSTOMERFILE_READ_FROM_CUSTOMER_CSV_SUCCESSFUL,
    APPLICANT_CREATE_NOTE_FAILED, APPLICANT_CREATE_NOTE_SUCCESSFUL, POLICY_UPDATE_SUCCESSFUL, POLICY_UPDATE_FAILED,
    PERMISSION_STATUS_UPDATE_FAILED, PERMISSION_STATUS_UPDATE_SUCCESS, ALERTTYPE_CREATE_SUCCESSFUL,
    ALERTTYPE_UPDATE_SUCCESSFUL, ALERTTYPE_DELETE_SUCCESSFUL, ALERTTYPE_CREATE_FAILED, ALERTTYPE_UPDATE_FAILED,
    ALERTTYPE_DELETE_FAILED, ALERT_CREATE_SUCCESSFUL, ALERT_UPDATE_SUCCESSFUL, ALERT_DELETE_SUCCESSFUL,
    ALERT_CREATE_FAILED, ALERT_UPDATE_FAILED, ALERT_DELETE_FAILED, MESSAGETEMPLATE_CREATE_SUCCESSFUL,
    MESSAGETEMPLATE_UPDATE_SUCCESSFUL, MESSAGETEMPLATE_DELETE_SUCCESSFUL, MESSAGETEMPLATE_CREATE_FAILED,
    MESSAGETEMPLATE_UPDATE_FAILED, MESSAGETEMPLATE_DELETE_FAILED, CUSTOMER_SECURITY_QUESTION_CREATE_SUCCESSFUL,
    CUSTOMER_SECURITY_QUESTION_CREATE_FAILED, CUSTOMER_SECURITY_QUESTION_UPDATE_SUCCESSFUL,
    CUSTOMER_SECURITY_QUESTION_UPDATE_FAILED, CUSTOMER_SECURITY_QUESTION_VERIFY_SUCCESSFUL,
    CUSTOMER_SECURITY_QUESTION_VERIFY_FAILED, CUSTOMER_SECURITY_QUESTION_DELETE_SUCCESSFUL,
    CUSTOMER_SECURITY_QUESTION_DELETE_FAILED, APPLICANT_CREATE_FAILED, APPLICANT_CREATE_SUCCESSFUL,
    CUSTOMER_ALERT_ENTITLEMENT_CREATE_SUCCESSFUL, CUSTOMER_ALERT_ENTITLEMENT_CREATE_FAILED,
    CUSTOMER_ALERT_ENTITLEMENT_UPDATE_SUCCESSFUL, CUSTOMER_ALERT_ENTITLEMENT_UPDATE_FAILED,
    CUSTOMER_ALERT_ENTITLEMENT_DELETE_SUCCESSFUL, CUSTOMER_ALERT_ENTITLEMENT_DELETE_FAILED;

}