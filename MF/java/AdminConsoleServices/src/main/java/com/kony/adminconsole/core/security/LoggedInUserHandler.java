package com.kony.adminconsole.core.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.service.authmodule.APICustomIdentityService;
import com.kony.adminconsole.service.authmodule.UserAndSecurityAttributesService;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.controller.DataControllerRequest;

/**
 * Handler class to Fetch Logged In User Profile
 * 
 * @author Aditya Mankal
 *
 */
public class LoggedInUserHandler {

    private static final Logger LOG = Logger.getLogger(LoggedInUserHandler.class);

    public static UserDetailsBean getUserDetails(DataControllerRequest requestInstance) throws ApplicationException {

        try {
            LOG.debug("Constructing Logged-In User DTO");

            // Read User Attributes
            Map<String, Object> userAttributes = requestInstance.getServicesManager().getIdentityHandler()
                    .getUserAttributes();

            String userId = userAttributes.get(UserAndSecurityAttributesService.USER_ID_KEY) != null
                    ? userAttributes.get(UserAndSecurityAttributesService.USER_ID_KEY).toString()
                    : StringUtils.EMPTY;

            String username = userAttributes.get(UserAndSecurityAttributesService.USERNAME_KEY) != null
                    ? userAttributes.get(UserAndSecurityAttributesService.USERNAME_KEY).toString()
                    : StringUtils.EMPTY;

            String emailId = userAttributes.get(UserAndSecurityAttributesService.EMAIL_KEY) != null
                    ? userAttributes.get(UserAndSecurityAttributesService.EMAIL_KEY).toString()
                    : StringUtils.EMPTY;

            String roleId = userAttributes.get(UserAndSecurityAttributesService.ROLE_ID_KEY) != null
                    ? userAttributes.get(UserAndSecurityAttributesService.ROLE_ID_KEY).toString()
                    : StringUtils.EMPTY;

            String roleName = userAttributes.get(UserAndSecurityAttributesService.USER_ROLE_KEY) != null
                    ? userAttributes.get(UserAndSecurityAttributesService.USER_ROLE_KEY).toString()
                    : StringUtils.EMPTY;

            String firstName = userAttributes.get(UserAndSecurityAttributesService.FIRST_NAME_KEY) != null
                    ? userAttributes.get(UserAndSecurityAttributesService.FIRST_NAME_KEY).toString()
                    : StringUtils.EMPTY;

            String middleName = userAttributes.get(UserAndSecurityAttributesService.MIDDLE_NAME_KEY) != null
                    ? userAttributes.get(UserAndSecurityAttributesService.MIDDLE_NAME_KEY).toString()
                    : StringUtils.EMPTY;

            String lastName = userAttributes.get(UserAndSecurityAttributesService.LAST_NAME_KEY) != null
                    ? userAttributes.get(UserAndSecurityAttributesService.LAST_NAME_KEY).toString()
                    : StringUtils.EMPTY;

            boolean isAPIUser = StringUtils.equals(userId, APICustomIdentityService.API_USER_ID);
            List<String> authData = new ArrayList<>();

            // Construct DTO
            UserDetailsBean userDetails = new UserDetailsBean(userId, username, emailId, firstName, middleName,
                    lastName, roleName, roleId, isAPIUser, authData);

            // Return DTO
            LOG.debug("Constructed Logged-In User DTO. Returning response");
            return userDetails;

        } catch (Exception e) {
            LOG.error("Exception in constructing logged-in user DTO. Exception:", e);
            throw new ApplicationException(ErrorCodeEnum.ERR_20008);
        }
    }
}
