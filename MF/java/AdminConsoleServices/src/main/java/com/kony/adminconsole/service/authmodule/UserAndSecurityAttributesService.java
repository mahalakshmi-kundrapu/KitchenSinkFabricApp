
package com.kony.adminconsole.service.authmodule;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;

import com.dbp.core.constants.DBPConstants;
import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.dto.InternalUser;
import com.kony.adminconsole.dto.Permission;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.handler.InternalUserHandler;
import com.kony.adminconsole.handler.PermissionHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to fetch User Profile and Security Attributes
 * 
 * @author Aditya Mankal
 *
 */
public class UserAndSecurityAttributesService implements JavaService2 {

    private static final String USER_TYPE = "TYPE_ID_CUSTOMER360";

    private static final int SESSTION_TTL = -1;

    private static final String USERNAME_PARAM = "username";

    public static final String USER_ID_KEY = "user_id";
    public static final String USERNAME_KEY = "username";

    public static final String ROLE_ID_KEY = "roleId";
    public static final String USER_ROLE_KEY = "UserRole";

    public static final String EMAIL_KEY = "Email";

    public static final String FIRST_NAME_KEY = "FirstName";
    public static final String FIRST_NAME_KEY_ = "first_name";

    public static final String MIDDLE_NAME_KEY = "MiddleName";
    public static final String MIDDLE_NAME_KEY_ = "middle_name";

    public static final String LAST_NAME_KEY = "LastName";
    public static final String LAST_NAME_KEY_ = "LastName";

    public static final String CREATED_BY_KEY = "CreatedBy";
    public static final String MODIFIED_BY_KEY = "ModifiedBy";

    public static final String SYNC_TIME_STAMP_KEY = "SyncTimeStamp";
    public static final String LAST_MODIFIED_TIME_STAMP_KEY = "LastModifiedTimeStamp";

    public static final String SOFT_DELETE_FLAG_KEY = "SoftDeleteFlag";

    private static final Logger LOG = Logger.getLogger(UserAndSecurityAttributesService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        try {
            Result result = new Result();
            String username = requestInstance.getParameter(USERNAME_PARAM);
            boolean isAuthenticated = BooleanUtils
                    .toBoolean((String) requestInstance.getAttribute(FabricConstants.IS_AUTHENTICATED_KEY));

            if (StringUtils.isNotBlank(username) && isAuthenticated) {

                /* Fetch user profile */
                LOG.debug("Fetching user attributes");
                InternalUser userProfile = InternalUserHandler.getUserProfile(username, requestInstance);

                /* Fetch user attributes */
                LOG.debug("Constructing user attributes");
                Record userAttributes = getUserAttributes(userProfile, requestInstance);
                result.addRecord(userAttributes);

                /* Fetch security attributes */
                LOG.debug("Constructing security attributes");
                String userId = userProfile != null ? userProfile.getId() : StringUtils.EMPTY;
                Record securityAttributes = getSecurityAttributes(userId, requestInstance);
                result.addRecord(securityAttributes);
            }
            return result;

        } catch (ApplicationException e) {
            Result errorResult = new Result();
            LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
            e.getErrorCodeEnum().setErrorCode(errorResult);
            return errorResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20008.setErrorCode(errorResult);
            return errorResult;
        }
    }

    /**
     * Method to get the Security Attributes of a user
     * 
     * @param userId
     * @param requestInstance
     * @return
     * @throws ApplicationException
     * @throws IOException
     */
    public Record getSecurityAttributes(String userId, DataControllerRequest requestInstance)
            throws ApplicationException, IOException {

        Record securityAttributesRecord = new Record();
        securityAttributesRecord.setId(FabricConstants.SECURITY_ATTRIBUTES);

        if (StringUtils.isNotBlank(userId)) {
            List<Permission> permissionsList = PermissionHandler.getUserGrantedPermissions(userId, requestInstance);
            JSONArray permissionsArray = new JSONArray();
            if (permissionsList != null && !permissionsList.isEmpty()) {
                for (Permission permission : permissionsList) {
                    permissionsArray.put(permission.getName());
                }
            }
            securityAttributesRecord.addParam(new Param(DBPConstants.PERMISSIONS_IDENTITY_KEY,
                    permissionsArray.toString(), FabricConstants.STRING));
            securityAttributesRecord.addParam(
                    new Param(FabricConstants.SESSION_TTL, Integer.toString(SESSTION_TTL), FabricConstants.STRING));
            securityAttributesRecord.addParam(
                    new Param(FabricConstants.SESSION_TOKEN, UUID.randomUUID().toString(), FabricConstants.STRING));
        }

        return securityAttributesRecord;
    }

    /**
     * Method to get the user attributes
     * 
     * @param username
     * @param requestInstance
     * @return Record
     * @throws ApplicationException
     * @throws IOException
     */
    public Record getUserAttributes(InternalUser userProfile, DataControllerRequest requestInstance)
            throws ApplicationException, IOException {

        Record userAttributesRecord = new Record();
        userAttributesRecord.setId(FabricConstants.USER_ATTRIBUTES);
        if (userProfile != null) {
            /* set username */
            String username = StringUtils.isBlank(userProfile.getUsername()) ? StringUtils.EMPTY
                    : userProfile.getUsername();
            userAttributesRecord.addParam(new Param(USERNAME_KEY, username, FabricConstants.STRING));

            /* set userId */
            String userId = StringUtils.isBlank(userProfile.getId()) ? StringUtils.EMPTY : userProfile.getId();
            userAttributesRecord.addParam(new Param(USER_ID_KEY, userId, FabricConstants.STRING));

            /* set email address */
            String email = StringUtils.isBlank(userProfile.getEmail()) ? StringUtils.EMPTY : userProfile.getEmail();
            userAttributesRecord.addParam(new Param(EMAIL_KEY, email, FabricConstants.STRING));

            /* set role Id */
            String roleId = StringUtils.isBlank(userProfile.getRoleId()) ? StringUtils.EMPTY : userProfile.getRoleId();
            userAttributesRecord.addParam(new Param(ROLE_ID_KEY, roleId, FabricConstants.STRING));

            /* set role name */
            String roleName = StringUtils.isBlank(userProfile.getRoleName()) ? StringUtils.EMPTY
                    : userProfile.getRoleName();
            userAttributesRecord.addParam(new Param(USER_ROLE_KEY, roleName, FabricConstants.STRING));

            /* set first name */
            String firstName = StringUtils.isBlank(userProfile.getFirstName()) ? StringUtils.EMPTY
                    : userProfile.getFirstName();
            userAttributesRecord.addParam(new Param(FIRST_NAME_KEY, firstName, FabricConstants.STRING));
            userAttributesRecord.addParam(new Param(FIRST_NAME_KEY_, firstName, FabricConstants.STRING));

            /* set middle name */
            String middleName = StringUtils.isBlank(userProfile.getMiddleName()) ? StringUtils.EMPTY
                    : userProfile.getMiddleName();
            userAttributesRecord.addParam(new Param(MIDDLE_NAME_KEY, middleName, FabricConstants.STRING));
            userAttributesRecord.addParam(new Param(MIDDLE_NAME_KEY_, middleName, FabricConstants.STRING));

            /* set last name */
            String lastName = StringUtils.isBlank(userProfile.getLastName()) ? StringUtils.EMPTY
                    : userProfile.getLastName();
            userAttributesRecord.addParam(new Param(LAST_NAME_KEY, lastName, FabricConstants.STRING));
            userAttributesRecord.addParam(new Param(LAST_NAME_KEY_, lastName, FabricConstants.STRING));

            /* set sync timestamp */
            String syncTimeStamp = CommonUtilities.convertTimetoISO8601Format(userProfile.getSyncTime());
            userAttributesRecord.addParam(new Param(SYNC_TIME_STAMP_KEY, syncTimeStamp, FabricConstants.STRING));

            /* Set createdby */
            String createdBy = StringUtils.isBlank(userProfile.getCreatedby()) ? StringUtils.EMPTY
                    : userProfile.getCreatedby();
            userAttributesRecord.addParam(new Param(CREATED_BY_KEY, createdBy, FabricConstants.STRING));

            /* Set modifiedby */
            String modifiedby = StringUtils.isBlank(userProfile.getModifiedby()) ? StringUtils.EMPTY
                    : userProfile.getCreatedby();
            userAttributesRecord.addParam(new Param(MODIFIED_BY_KEY, modifiedby, FabricConstants.STRING));

            /* Set last modified timestamp */
            String lastModifiedTimeStamp = CommonUtilities
                    .convertTimetoISO8601Format(userProfile.getLastModifiedTime());
            userAttributesRecord
                    .addParam(new Param(LAST_MODIFIED_TIME_STAMP_KEY, lastModifiedTimeStamp, FabricConstants.STRING));

            /* Set soft delete flag */
            String softDeleteFlag = String.valueOf(userProfile.isSoftDeleteFlag());
            userAttributesRecord.addParam(new Param(SOFT_DELETE_FLAG_KEY, softDeleteFlag, FabricConstants.STRING));

            /* Set App name */
            userAttributesRecord
                    .addParam(new Param(DBPConstants.CUSTOMER_TYPE_ID_IDENTITY_KEY, USER_TYPE, FabricConstants.STRING));
        }
        return userAttributesRecord;
    }

}
