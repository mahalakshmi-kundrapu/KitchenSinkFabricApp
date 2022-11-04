package com.kony.adminconsole.service.authmodule;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.dto.AuthenticationResult;
import com.kony.adminconsole.dto.InternalUser;
import com.kony.adminconsole.utilities.AuthenticationResults;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to perform User Authentication
 * 
 * @author Aditya Mankal
 *
 */
public class UserAuthenticationService implements JavaService2 {

    private static final String USERNAME_PARAM_FIELD_NAME = "inputUsername";
    private static final String PASSWORD_PARAM_FIELD_NAME = "inputPassword";

    private static final String USERNAME_PARAM = "username";
    private static final String ERROR_CODE_PARAM = "errorCode";
    private static final String ERROR_MESSAGE_PARAM = "errorMessage";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        try {
            // Read Inputs
            String username = requestInstance.getParameter(USERNAME_PARAM_FIELD_NAME);
            String password = requestInstance.getParameter(PASSWORD_PARAM_FIELD_NAME);

            // Authenticate User
            AuthenticationResult authenticationResult = UserAuthentication.authenticateUser(username, password,
                    requestInstance);
            InternalUser userProfile = authenticationResult.getUserProfile();

            // Prepare Result
            Result result = new Result();

            if (authenticationResult.isAuthenticationSuccessful()) {
                // Add username to result
                result.addParam(new Param(USERNAME_PARAM, authenticationResult.getUserProfile().getUsername(),
                        FabricConstants.STRING));
            } else {
                JSONObject messageJSON = new JSONObject();
                // Add error message information -> Customised as per C360 client
                if (authenticationResult.getCode() == AuthenticationResults.INVALID_CREDENTIALS.getCode()
                        && userProfile != null) {
                    messageJSON.put("allowedLoginAttempts", UserAuthentication.MAX_ALLOWED_LOGIN_ATTEMPTS);
                    messageJSON.put("failedLoginAttempts", userProfile.getFailedLoginAttemptCount());
                }
                messageJSON.put("code", authenticationResult.getCode());
                messageJSON.put("message", authenticationResult.getMessage());
                result.addParam(new Param(ERROR_MESSAGE_PARAM, messageJSON.toString(), FabricConstants.STRING));
                result.addParam(new Param(ERROR_CODE_PARAM, Integer.toString(authenticationResult.getCode()),
                        FabricConstants.INT));
            }

            // Return Success Response
            return result;
        } catch (Exception e) {
            // Exception. Return Error Response
            Result errorResult = new Result();
            errorResult.addParam(
                    new Param(ERROR_MESSAGE_PARAM, ErrorCodeEnum.ERR_20001.getMessage(), FabricConstants.STRING));
            errorResult.addParam(new Param(ERROR_CODE_PARAM, Integer.toString(HttpStatus.SC_INTERNAL_SERVER_ERROR),
                    FabricConstants.INT));
            return errorResult;
        }
    }
}
