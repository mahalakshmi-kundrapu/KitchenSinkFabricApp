package com.kony.adminconsole.service.kms;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.core.config.EnvironmentConfiguration;
import com.kony.adminconsole.dto.EmailHandlerBean;
import com.kony.adminconsole.dto.EmailServiceBean;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.handler.EmailHandler;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.EventEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ModuleNameEnum;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class EmailService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(EmailService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result processedResult = new Result();

            String baseURL = EnvironmentConfiguration.AC_HOST_URL.getValue(requestInstance);
            EmailServiceBean emailServiceBean = new EmailServiceBean();

            emailServiceBean.setEmailSubject(requestInstance.getParameter("emailSubject"));
            emailServiceBean.setRecipientEmailId(requestInstance.getParameter("recipientEmailId"));
            emailServiceBean.setCc(requestInstance.getParameter("cc"));
            emailServiceBean.setEmailType(requestInstance.getParameter("emailType"));
            if (requestInstance.getParameter("vizServerURL") != null
                    && requestInstance.getParameter("vizServerURL").contains("localhost")) {
                emailServiceBean.setVizServerURL(requestInstance.getParameter("vizServerURL"));
            } else {
                emailServiceBean.setVizServerURL(baseURL + "/apps/Customer360/#_frmErrorLogin");
            }
            emailServiceBean.setPasswordUUID(CommonUtilities.getNewId().toString());
            emailServiceBean.setAdditionalContext(
                    CommonUtilities.getStringAsJSONObject(requestInstance.getParameter("AdditionalContext")));

            EmailHandlerBean emailHandlerBeanInstance = new EmailHandlerBean();
            EmailHandler.getKMSAuthToken(emailHandlerBeanInstance, requestInstance);
            emailServiceBean.setClaimsToken(emailHandlerBeanInstance.getKMSClaimsToken());

            if (emailServiceBean.getEmailType().equals("createUser")
                    || emailServiceBean.getEmailType().equals("resetPassword")) {

                // ** Checking if email id exists in database **
                Map<String, String> headerParametersMap = new HashMap<String, String>();

                Map<String, String> postParametersMap = new HashMap<String, String>();
                postParametersMap.put(ODataQueryConstants.SELECT, "id, Username, FirstName, LastName");
                postParametersMap.put(ODataQueryConstants.FILTER,
                        "Email eq '" + emailServiceBean.getRecipientEmailId() + "'");

                String readSystemUserResponse = Executor.invokeService(ServiceURLEnum.INTERNALUSERDETAILS_VIEW_READ,
                        postParametersMap, headerParametersMap, requestInstance);
                JSONObject readSystemUserJSON = CommonUtilities.getStringAsJSONObject(readSystemUserResponse);

                if (readSystemUserJSON != null && readSystemUserJSON.has(FabricConstants.OPSTATUS)
                        && readSystemUserJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                    JSONObject userProfile = null;
                    JSONArray array = readSystemUserJSON.optJSONArray("internaluserdetails_view");
                    if (array != null && array.length() == 1) {
                        userProfile = array.optJSONObject(0);
                    }

                    if (userProfile != null) {
                        emailServiceBean.setId(userProfile.optString("id"));
                        emailServiceBean.setUsername(userProfile.optString("Username"));
                        emailServiceBean.setFirstName(userProfile.optString("FirstName"));
                        emailServiceBean.setLastName(userProfile.optString("LastName"));

                        // ** Updating UUID against email in database **
                        postParametersMap.clear();
                        postParametersMap.put("id", emailServiceBean.getId());
                        postParametersMap.put("ResetpasswordLink", emailServiceBean.getPasswordUUID());
                        postParametersMap.put("ResetPasswordExpdts", getTodaysTimestamp());

                        String updateSystemUserResponse = Executor.invokeService(ServiceURLEnum.SYSTEMUSER_UPDATE,
                                postParametersMap, headerParametersMap, requestInstance);
                        JSONObject updateSystemUserResponseJSON = CommonUtilities
                                .getStringAsJSONObject(updateSystemUserResponse);

                        if (updateSystemUserResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                            int updatedRecords = updateSystemUserResponseJSON.getInt("updatedRecords");

                            if (updatedRecords == 1) {

                                JSONObject sendKMSEmailJSON = createUserAndSendEmail(emailServiceBean, requestInstance);
                                processedResult.addParam(new Param("message", sendKMSEmailJSON.getString("message"),
                                        FabricConstants.STRING));
                                AuditHandler.auditAdminActivity(requestInstance,
                                        emailServiceBean.getEmailType().equals("createUser") ? ModuleNameEnum.USERS
                                                : ModuleNameEnum.LOGIN,
                                        EventEnum.COMMUNICATION, ActivityStatusEnum.SUCCESSFUL,
                                        "Successfully sent email. Email address: "
                                                + emailServiceBean.getRecipientEmailId());
                            } else {
                                AuditHandler.auditAdminActivity(requestInstance,
                                        emailServiceBean.getEmailType().equals("createUser") ? ModuleNameEnum.USERS
                                                : ModuleNameEnum.LOGIN,
                                        EventEnum.COMMUNICATION, ActivityStatusEnum.FAILED,
                                        "Failed to send email. Email address: "
                                                + emailServiceBean.getRecipientEmailId());
                                ErrorCodeEnum.ERR_21003.setErrorCode(processedResult);
                                return processedResult;
                            }
                        } else {
                            AuditHandler.auditAdminActivity(requestInstance,
                                    emailServiceBean.getEmailType().equals("createUser") ? ModuleNameEnum.USERS
                                            : ModuleNameEnum.LOGIN,
                                    EventEnum.COMMUNICATION, ActivityStatusEnum.FAILED,
                                    "Failed to send email. Email address: " + emailServiceBean.getRecipientEmailId());
                            ErrorCodeEnum.ERR_21003.setErrorCode(processedResult);
                            return processedResult;
                        }
                        processedResult.addParam(new Param("emailPresentInDB", "true", "boolean"));
                    } else {
                        processedResult.addParam(new Param("emailPresentInDB", "false", "boolean"));
                    }
                } else {
                    ErrorCodeEnum.ERR_21003.setErrorCode(processedResult);
                    return processedResult;
                }
            } else if (emailServiceBean.getEmailType().equals("EnrollCustomer")
                    || emailServiceBean.getEmailType().equals("InternalUserEdit")
                    || emailServiceBean.getEmailType().equals("InternalUserStatusChange")) {

                emailServiceBean.setFirstName(emailServiceBean.getAdditionalContext().getString("name"));
                emailServiceBean.setLastName(emailServiceBean.getAdditionalContext().getString("name"));
                emailServiceBean.setId(emailServiceBean.getAdditionalContext().getString("InternalUser_id"));

                JSONObject sendKMSEmailJSON = createUserAndSendEmail(emailServiceBean, requestInstance);
                processedResult
                        .addParam(new Param("message", sendKMSEmailJSON.getString("message"), FabricConstants.STRING));
            }

            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    private JSONObject createUserAndSendEmail(EmailServiceBean emailServiceBean,
            DataControllerRequest requestInstance) {

        Map<String, String> headers = new HashMap<>();
        headers.put("KMSAuthToken", emailServiceBean.getClaimsToken());

        // ** Creating the user in the subscriber list **
        Map<String, String> createKMSUserJSONObject = constructCreateKMSUser(emailServiceBean);

        String createKMSUserResponse = Executor.invokeService(ServiceURLEnum.EMAILSERVICE_ENROLLUSER,
                createKMSUserJSONObject, headers, requestInstance);
        LOG.debug("Create KMS User Response:" + createKMSUserResponse);

        // ** Sending the mail **
        Map<String, String> sendKMSEmailJSONObject = constructSendKMSEmail(emailServiceBean, requestInstance);
        String sendKMSEmailResponse = Executor.invokeService(ServiceURLEnum.EMAILSERVICE_SENDEMAIL,
                sendKMSEmailJSONObject, headers, requestInstance);
        JSONObject sendKMSEmailJSON = CommonUtilities.getStringAsJSONObject(sendKMSEmailResponse);

        ModuleNameEnum moduleName = null;
        switch (emailServiceBean.getEmailType()) {
            case "createUser":
                moduleName = ModuleNameEnum.USERS;
                break;
            case "resetPassword":
                moduleName = ModuleNameEnum.LOGIN;
                break;
            case "InternalUserEdit":
                moduleName = ModuleNameEnum.USERS;
                break;
            case "InternalUserStatusChange":
                moduleName = ModuleNameEnum.USERS;
                break;
            case "EnrollCustomer":
                moduleName = ModuleNameEnum.CUSTOMERS;
                break;
        }
        if (sendKMSEmailJSON != null && sendKMSEmailJSON.optString("message").contains("Request Queued")) {
            // SUCCESS :: Email
            AuditHandler.auditAdminActivity(requestInstance, moduleName, EventEnum.COMMUNICATION,
                    ActivityStatusEnum.SUCCESSFUL,
                    "Failed to send email. Email address: " + emailServiceBean.getRecipientEmailId());
        } else {
            // FAILURE :: Email
            AuditHandler.auditAdminActivity(requestInstance, moduleName, EventEnum.COMMUNICATION,
                    ActivityStatusEnum.FAILED,
                    "Failed to send email. Email address: " + emailServiceBean.getRecipientEmailId());
        }
        return sendKMSEmailJSON;
    }

    private static String getTodaysTimestamp() {
        // --OData query timestamp--
        DateFormat dfD = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat dfT = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        String dateString = dfD.format(date) + "T" + dfT.format(date);

        return dateString;
    }

    public String getEmailContent(EmailServiceBean emailServiceBean, DataControllerRequest requestInstance) {

        String content = StringUtils.EMPTY;
        String parameterString = emailServiceBean.getPasswordUUID() + "_-_" + emailServiceBean.getId() + "_-_"
                + emailServiceBean.getUsername();
        String resetPasswordLink = emailServiceBean.getVizServerURL() + "?qp="
                + CommonUtilities.encodeToBase64(parameterString);

        String templateSource = StringUtils.EMPTY;
        if (emailServiceBean.getEmailType().equals("createUser")) {
            templateSource = "emailTemplates/createUser.html";
        } else if (emailServiceBean.getEmailType().equals("resetPassword")) {
            templateSource = "emailTemplates/resetPassword.html";
        } else if (emailServiceBean.getEmailType().equals("EnrollCustomer")) {
            templateSource = "emailTemplates/enrollCustomer.html";
        } else if (emailServiceBean.getEmailType().equals("InternalUserEdit")) {
            templateSource = "emailTemplates/InternalUserEdit.html";
        } else if (emailServiceBean.getEmailType().equals("InternalUserStatusChange")) {
            templateSource = "emailTemplates/InternalUserStatusChange.html";
        }

        try {
            InputStream templateStream = this.getClass().getClassLoader().getResourceAsStream(templateSource);
            content = IOUtils.toString(templateStream, StandardCharsets.UTF_8);
            if (content != null) {
                if (emailServiceBean.getEmailType().equals("createUser")
                        || emailServiceBean.getEmailType().equals("resetPassword")) {
                    content = content.replaceAll("firstName", emailServiceBean.getFirstName());
                    content = content.replaceAll("resetPasswordLink", resetPasswordLink);
                } else if (emailServiceBean.getEmailType().equals("EnrollCustomer")) {
                    content = content.replaceAll("%customername%",
                            emailServiceBean.getAdditionalContext().getString("name"));
                    content = content.replaceAll("corebankingurl",
                            emailServiceBean.getAdditionalContext().getString("corebankingurl"));
                } else if (emailServiceBean.getEmailType().equals("InternalUserEdit")) {
                    content = content.replaceAll("%firstname%",
                            emailServiceBean.getAdditionalContext().getString("name"));
                    content = content.replaceAll("%username%",
                            emailServiceBean.getAdditionalContext().getString("username"));
                    content = content.replaceAll("%changedfields%",
                            emailServiceBean.getAdditionalContext().getString("ChangedFields"));
                } else if (emailServiceBean.getEmailType().equals("InternalUserStatusChange")) {
                    content = content.replaceAll("%firstname%",
                            emailServiceBean.getAdditionalContext().getString("name"));
                    content = content.replaceAll("%username%",
                            emailServiceBean.getAdditionalContext().getString("username"));
                    content = content.replaceAll("%status%",
                            emailServiceBean.getAdditionalContext().getString("status"));
                }

                String emailTemplateLogo = EnvironmentConfiguration.AC_EMAIL_TEMPLATE_LOGO_URL
                        .getValue(requestInstance);
                content = content.replaceAll("emailTemplateLogo", emailTemplateLogo);
            }
        } catch (Exception e) {
            LOG.error("Failed while executing get transaction logs", e);
        }

        return content;
    }

    public static Map<String, String> constructCreateKMSUser(EmailServiceBean emailServiceBean) {

        Map<String, String> map = new HashMap<>();
        map.put("firstName", emailServiceBean.getFirstName());
        map.put("lastName", emailServiceBean.getLastName());
        map.put("email", emailServiceBean.getRecipientEmailId());
        map.put("mobileNumber", "+919" + CommonUtilities.genPhoneNumber().substring(1));
        map.put("state", "Telangana");
        map.put("country", "India");
        return map;
    }

    public Map<String, String> constructSendKMSEmail(EmailServiceBean emailServiceBean,
            DataControllerRequest requestInstance) {

        Map<String, String> emailPayloadMap = new HashMap<>();
        emailPayloadMap.put("emailId", emailServiceBean.getRecipientEmailId());
        emailPayloadMap.put("subject", emailServiceBean.getEmailSubject());
        emailPayloadMap.put("content",
                CommonUtilities.encodeToBase64(getEmailContent(emailServiceBean, requestInstance)));

        return emailPayloadMap;
    }
}