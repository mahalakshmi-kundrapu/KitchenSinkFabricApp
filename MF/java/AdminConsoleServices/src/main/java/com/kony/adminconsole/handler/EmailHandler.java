package com.kony.adminconsole.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.dto.EmailHandlerBean;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.controller.DataControllerRequest;

public class EmailHandler {
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(EmailHandler.class);

    private static final String INPUT_SUBJECT = "subject";
    private static final String INPUT_BODY = "body";
    private static final String INPUT_FIRSTNAME = "recepientFirstname";
    private static final String INPUT_LASTNAME = "recepientLastname";
    private static final String INPUT_PHONENUMBER = "phonenumber";
    private static final String INPUT_COUNTRY = "phonenumber";

    public static EmailHandlerBean initializeEmailDTO(DataControllerRequest requestInstance) {
        EmailHandlerBean emailDTOInstance = new EmailHandlerBean();
        emailDTOInstance.setSubject(requestInstance.getParameter(INPUT_SUBJECT));
        emailDTOInstance.setBody(requestInstance.getParameter(INPUT_BODY));
        emailDTOInstance.setFirstName(requestInstance.getParameter(INPUT_FIRSTNAME));
        emailDTOInstance.setLastName(requestInstance.getParameter(INPUT_LASTNAME));
        emailDTOInstance.setPhoneNumber(requestInstance.getParameter(INPUT_PHONENUMBER));
        emailDTOInstance.setCountry(requestInstance.getParameter(INPUT_COUNTRY));
        return emailDTOInstance;
    }

    public static void getKMSAuthToken(EmailHandlerBean emailDTOInstance, DataControllerRequest requestInstance) {
        JSONObject response = CommonUtilities.getStringAsJSONObject(Executor
                .invokeService(ServiceURLEnum.AUTHKMSSERVICE_AUTHENTICATE, new HashMap<>(), null, requestInstance));
        if (response != null && response.has(FabricConstants.OPSTATUS) && response.getInt(FabricConstants.OPSTATUS) == 0
                && response.has("KMSAuthToken")) {
            emailDTOInstance.setKMSClaimsToken(String.valueOf(response.getString("KMSAuthToken")));
        }
    }

    public static JSONObject sendEmailToSingleRecipient(EmailHandlerBean emailDTOInstance,
            DataControllerRequest requestInstance) {

        if (StringUtils.isBlank(emailDTOInstance.getKMSClaimsToken())) {
            getKMSAuthToken(emailDTOInstance, requestInstance);
        }

        Map<String, String> emailPayloadMap = new HashMap<>();
        emailPayloadMap.put("emailId", emailDTOInstance.getRecipientEmailId());
        emailPayloadMap.put("subject", emailDTOInstance.getSubject());
        emailPayloadMap.put("content", CommonUtilities.encodeToBase64(emailDTOInstance.getBody()));
        Map<String, String> headers = new HashMap<>();
        headers.put("KMSAuthToken", emailDTOInstance.getKMSClaimsToken());
        String sendKMSEmailResponse = Executor.invokeService(ServiceURLEnum.EMAILSERVICE_SENDEMAIL, emailPayloadMap,
                headers, requestInstance);
        return CommonUtilities.getStringAsJSONObject(sendKMSEmailResponse);
    }

    public static JSONObject enrolKMSUser(EmailHandlerBean emailDTOInstance, DataControllerRequest requestInstance) {

        if (StringUtils.isBlank(emailDTOInstance.getKMSClaimsToken()))
            getKMSAuthToken(emailDTOInstance, requestInstance);

        if (StringUtils.isBlank(emailDTOInstance.getPhoneNumber()))
            emailDTOInstance.setPhoneNumber("+91" + CommonUtilities.genPhoneNumber());
        if (StringUtils.isBlank(emailDTOInstance.getCountry()))
            emailDTOInstance.setCountry("India");

        Map<String, String> enrolKMSUserJSONObject = new HashMap<>();
        enrolKMSUserJSONObject.put("firstName", emailDTOInstance.getFirstName());
        enrolKMSUserJSONObject.put("lastName", emailDTOInstance.getLastName());
        enrolKMSUserJSONObject.put("email", emailDTOInstance.getRecipientEmailId());
        enrolKMSUserJSONObject.put("mobileNumber", emailDTOInstance.getPhoneNumber());
        enrolKMSUserJSONObject.put("country", emailDTOInstance.getCountry());
        enrolKMSUserJSONObject.put("state", emailDTOInstance.getState());
        enrolKMSUserJSONObject.put("smsSubscription", "true");
        enrolKMSUserJSONObject.put("emailSubscription", "true");

        Map<String, String> headers = new HashMap<>();
        headers.put("KMSAuthToken", emailDTOInstance.getKMSClaimsToken());

        String enrolKMSEmailResponse = Executor.invokeService(ServiceURLEnum.EMAILSERVICE_ENROLLUSER,
                enrolKMSUserJSONObject, headers, requestInstance);
        return CommonUtilities.getStringAsJSONObject(enrolKMSEmailResponse);
    }

    public static JSONObject invokeSendEmailObjectService(DataControllerRequest dataControllerRequest, String authToken,
            String recipientEmailId, String cc, String subject, String emailType, JSONObject additionalContext) {

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("emailSubject", subject);
        postParametersMap.put("senderEmailId", "retailbankingdemos@kony.com");
        postParametersMap.put("recipientEmailId", recipientEmailId);
        postParametersMap.put("cc", cc);
        postParametersMap.put("emailType", emailType);
        if (additionalContext != null) {
            postParametersMap.put("AdditionalContext", additionalContext.toString());
            if (additionalContext.has("vizServerURL")) {
                postParametersMap.put("vizServerURL", additionalContext.optString("vizServerURL"));
            }
        }

        String EndpointResponse = Executor.invokeService(ServiceURLEnum.EMAILKMSJAVASERVICE_SENDEMAIL,
                postParametersMap, null, dataControllerRequest);
        return CommonUtilities.getStringAsJSONObject(EndpointResponse);

    }
}
