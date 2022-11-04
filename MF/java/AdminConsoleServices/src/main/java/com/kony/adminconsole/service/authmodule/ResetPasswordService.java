package com.kony.adminconsole.service.authmodule;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.crypto.BCrypt;
import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.handler.UserProfileHandler;
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

/**
 * Service to Reset Internal User Password
 * 
 * @author Aditya Mankal
 * 
 */
public class ResetPasswordService implements JavaService2 {

    private static final int PASSWORD_RESET_LINK_VALIDITY_IN_MINS = 24 * 60;// 24 hours * 60 minutes

    public static final Pattern PASSWORD_PATTERN = Pattern
            .compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*])(?=\\S+$).{8,16}$");
    private static final String RESET_PASSWORD_STATUS_CODE_KEY = "resetPasswordStatusCode";

    private static final Logger LOG = Logger.getLogger(ResetPasswordService.class);

    @Override
    public Object invoke(String methodId, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        try {
            Result processedResult = new Result();

            String userId = requestInstance.getParameter("UserID");
            String newPassword = requestInstance.getParameter("NewPassword");
            String resetPasswordURL = requestInstance.getParameter("ResetPasswordURL");
            String newConfirmPassword = requestInstance.getParameter("NewConfirmPassword");

            String username = null, userRole = null;
            UserProfileHandler userProfileHandlerInstance = new UserProfileHandler(userId, requestInstance);
            UserDetailsBean userDetails = userProfileHandlerInstance.getDetailsBean();

            if (userDetails != null) {
                username = userDetails.getUserName();
                userRole = userDetails.getRoleName();
            }

            if (StringUtils.isBlank(resetPasswordURL) || StringUtils.equalsIgnoreCase(resetPasswordURL, "NULL")) {
                // UnRecognized account OR Incorrect Password Reset Link
                AuditHandler.auditAdminActivity(requestInstance, username, userRole, ModuleNameEnum.LOGIN,
                        EventEnum.UPDATE, ActivityStatusEnum.FAILED, "Password reset failed.");
                LOG.debug("Unrecognised Account OR Incorrect Password Reset Link");
                throw new ApplicationException(ErrorCodeEnum.ERR_20931);
            }

            if (!newPassword.equals(newConfirmPassword)) {
                AuditHandler.auditAdminActivity(requestInstance, username, userRole, ModuleNameEnum.LOGIN,
                        EventEnum.UPDATE, ActivityStatusEnum.FAILED,
                        "New password and Confirm New Password values do not match.");
                LOG.debug("New Password and ConfirmNewPassword do not match. Password Change Denied");
                throw new ApplicationException(ErrorCodeEnum.ERR_20930);
            }

            if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
                AuditHandler.auditAdminActivity(requestInstance, username, userRole, ModuleNameEnum.LOGIN,
                        EventEnum.UPDATE, ActivityStatusEnum.FAILED, "Password does not meet validation criteria.");
                LOG.debug("Password does not meet the criteria.Password change denied");
                throw new ApplicationException(ErrorCodeEnum.ERR_20936);
            }

            // Prepare Input Map
            Map<String, String> inputMap = new HashMap<>();
            inputMap.put(ODataQueryConstants.FILTER,
                    "id eq '" + userId + "' and ResetpasswordLink eq '" + resetPasswordURL + "'");
            inputMap.put(ODataQueryConstants.SELECT, "FirstName,LastName,ResetPasswordExpdts");

            // Fetch Password Reset Link Information
            String serviceResponse = Executor.invokeService(ServiceURLEnum.INTERNALUSERDETAILS_VIEW_READ, inputMap,
                    null, requestInstance);
            JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);

            if (serviceResponseJSON != null && serviceResponseJSON.has(FabricConstants.OPSTATUS)
                    && serviceResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                // Successful Operation
                LOG.debug("Successful Read");

                JSONArray array = serviceResponseJSON.optJSONArray("internaluserdetails_view");
                if (array != null && array.length() == 1) {

                    // Valid Password Change Request. Proceed with further checks.
                    LOG.debug("Valid Password Change Request. Proceeding with further checks.");
                    JSONObject userProfileJSON = array.optJSONObject(0);

                    // Verify Password Reset Link Validity
                    long timeDiff = 0L;
                    if (userProfileJSON.has("ResetPasswordExpdts")) {
                        String passwordLinkCreatedTime = userProfileJSON.optString("ResetPasswordExpdts");
                        timeDiff = CommonUtilities.getTimeElapsedFromTimestampToNowInMinutes(passwordLinkCreatedTime,
                                "yyyy-MM-dd HH:mm:ss");
                    }
                    if (timeDiff > (PASSWORD_RESET_LINK_VALIDITY_IN_MINS)) {
                        // Expired Password Reset link
                        LOG.debug("Expired Password Reset Link");
                        inputMap.clear();
                        // Clearing the Password Reset Link from the database
                        inputMap.put("id", userId);
                        inputMap.put("ResetPasswordExpdts", "NULL");//
                        inputMap.put("ResetpasswordLink", "NULL");
                        Executor.invokeService(ServiceURLEnum.SYSTEMUSER_UPDATE, inputMap, null, requestInstance);
                        ErrorCodeEnum.ERR_20939.setErrorCode(processedResult);
                        processedResult.addParam(new Param("passwordResetLinkValidityInMinutes",
                                String.valueOf(PASSWORD_RESET_LINK_VALIDITY_IN_MINS), FabricConstants.INT));
                        return processedResult;
                    }

                    // Valid Password Reset Request. Update password
                    LOG.debug("Valid Password Reset Link");
                    String newPassword_Salt = BCrypt.gensalt(UserAuthentication.PASSWORD_HASH_ROUNDS);
                    String newPassword_hashed = BCrypt.hashpw(newPassword, newPassword_Salt);
                    inputMap.clear();
                    inputMap.put("id", userId);
                    inputMap.put("Password", newPassword_hashed);
                    inputMap.put("LastPasswordChangedts", CommonUtilities.getISOFormattedLocalTimestamp());
                    inputMap.put("ResetPasswordExpdts", "NULL");
                    inputMap.put("ResetpasswordLink", "NULL");
                    String updateProfileResponse = Executor.invokeService(ServiceURLEnum.SYSTEMUSER_UPDATE, inputMap,
                            null, requestInstance);
                    JSONObject updateProfileResponseJSON = CommonUtilities.getStringAsJSONObject(updateProfileResponse);
                    if (updateProfileResponseJSON != null && updateProfileResponseJSON.has(FabricConstants.OPSTATUS)
                            && updateProfileResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                        LOG.debug("Password reset successfully");
                        AuditHandler.auditAdminActivity(requestInstance, username, userRole, ModuleNameEnum.LOGIN,
                                EventEnum.UPDATE, ActivityStatusEnum.SUCCESSFUL, "Password reset successful.");
                        processedResult.addParam(new Param(RESET_PASSWORD_STATUS_CODE_KEY, "0", FabricConstants.INT));
                        return processedResult;
                    }

                    // Failed Kony Fabric Operation
                    LOG.error("Failed Operation. Response" + serviceResponse);
                    AuditHandler.auditAdminActivity(requestInstance, username, userRole, ModuleNameEnum.LOGIN,
                            EventEnum.UPDATE, ActivityStatusEnum.FAILED, "Password reset failed.");
                    throw new ApplicationException(ErrorCodeEnum.ERR_20981);

                } else {
                    // UnRecognized account OR Incorrect Password Reset Link
                    LOG.debug("Unrecognised Account OR Incorrect Password Reset Link");
                    AuditHandler.auditAdminActivity(requestInstance, username, userRole, ModuleNameEnum.LOGIN,
                            EventEnum.UPDATE, ActivityStatusEnum.FAILED, "Password reset failed.");
                    throw new ApplicationException(ErrorCodeEnum.ERR_20931);
                }
            } else {
                // Failed Kony Fabric Operation
                AuditHandler.auditAdminActivity(requestInstance, username, userRole, ModuleNameEnum.LOGIN,
                        EventEnum.UPDATE, ActivityStatusEnum.FAILED, "Password reset failed.");
                LOG.error("Failed Operation. Response" + serviceResponse);
                throw new ApplicationException(ErrorCodeEnum.ERR_20981);
            }
        } catch (ApplicationException e) {
            Result errorResult = new Result();
            LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
            e.getErrorCodeEnum().setErrorCode(errorResult);
            return errorResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }

    }

}