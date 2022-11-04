/**
 * 
 */
package com.kony.adminconsole.handler;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.JSONUtils;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.dto.InternalUser;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.kony.adminconsole.utilities.StatusEnum;
import com.konylabs.middleware.controller.DataControllerRequest;

/**
 * 
 * Handler to perform operations on Internal User
 * 
 * @author Aditya Mankal
 */
public class InternalUserHandler {

    private static final Logger LOG = Logger.getLogger(InternalUserHandler.class);

    /**
     * Method to get the Role Id's of a list of a list of Internal Users
     * 
     * @param requestInstance
     * @param usersList
     * @return Map of Users and their Id of their respective role
     * @throws ApplicationException
     */
    public static Map<String, String> getUsersRoleMap(DataControllerRequest requestInstance, List<String> usersList)
            throws ApplicationException {

        Map<String, String> userRoleMap = new HashMap<>();
        if (usersList == null || usersList.isEmpty()) {
            return userRoleMap;
        }

        // Construct Filter Query
        StringBuffer filterQueryBuffer = new StringBuffer();
        for (String currUserId : usersList) {
            filterQueryBuffer.append("User_id eq '" + currUserId + "' or ");
        }
        filterQueryBuffer.trimToSize();
        String filterQuery = StringUtils.trim(
                CommonUtilities.replaceLastOccuranceOfString(filterQueryBuffer.toString(), "or", StringUtils.EMPTY));

        // Prepare Query Map
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put(ODataQueryConstants.FILTER, filterQuery);

        // Read User Role
        String serviceResponse = Executor.invokeService(ServiceURLEnum.USERROLE_READ, queryMap, null, requestInstance);
        JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0 || !serviceResponseJSON.has("userrole")) {
            LOG.error("Failed to read userrole records.Response:" + serviceResponse);
            throw new ApplicationException(ErrorCodeEnum.ERR_20548);
        }

        // Construct Result Map
        JSONObject currJSONObject;
        JSONArray userRoleArray = serviceResponseJSON.getJSONArray("userrole");
        for (Object currObject : userRoleArray) {
            if (currObject instanceof JSONObject) {
                currJSONObject = (JSONObject) currObject;
                if (currJSONObject.has("User_id") && currJSONObject.has("Role_id")) {
                    userRoleMap.put(currJSONObject.optString("User_id"), currJSONObject.optString("Role_id"));
                }
            }
        }

        // Return Result Map
        return userRoleMap;
    }

    /**
     * Method to suspend Internal User
     * 
     * @param userId
     * @param requestInstance
     * @throws ApplicationException
     */
    public static void suspendUserAccount(String userId, DataControllerRequest requestInstance)
            throws ApplicationException {

        if (StringUtils.isBlank(userId)) {
            return;
        }

        // Prepare Input Map
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("id", userId);
        inputMap.put("Status_id", StatusEnum.SID_SUSPENDED.name());

        // Update user profile status
        String serviceResponse = Executor.invokeService(ServiceURLEnum.SYSTEMUSER_UPDATE, inputMap, null,
                requestInstance);
        JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);

        // Check operation status
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
            throw new ApplicationException(ErrorCodeEnum.ERR_20003);
        }
    }

    /**
     * Method to set the Failed Login Attempt count of Internal user
     * 
     * @param userId
     * @param requestInstance
     * @throws ApplicationException
     */
    public static void setFailedLoginAttempCount(String userId, int count, DataControllerRequest requestInstance)
            throws ApplicationException {

        if (StringUtils.isBlank(userId)) {
            return;
        }

        // Prepare Input Map
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("id", userId);
        inputMap.put("FailedCount", Integer.toString(count));

        // Update failed login attempt count
        String serviceResponse = Executor.invokeService(ServiceURLEnum.SYSTEMUSER_UPDATE, inputMap, null,
                requestInstance);
        JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);

        // Check operation status
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
            throw new ApplicationException(ErrorCodeEnum.ERR_20004);
        }
    }

    /**
     * Method to get Internal User Profile with username
     * 
     * @param username
     * @param requestInstance
     * @return InternalUser if user exists with given username <br>
     *         null if no user exists with given username
     * @throws ApplicationException
     * @throws IOException
     */
    public static InternalUser getUserProfile(String username, DataControllerRequest requestInstance)
            throws ApplicationException, IOException {

        Map<String, String> queryMap = new HashMap<>();
        queryMap.put(ODataQueryConstants.FILTER, "Username eq '" + username + "'");

        // Fetch Internal User Profile
        String response = Executor.invokeService(ServiceURLEnum.INTERNALUSERDETAILS_VIEW_READ, queryMap, null,
                requestInstance);
        JSONObject responseJSON = CommonUtilities.getStringAsJSONObject(response);
        if (responseJSON == null || !responseJSON.has(FabricConstants.OPSTATUS)
                || responseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
            LOG.error("Fetch user-profile with username: Failed. Service Response:" + response);
            throw new ApplicationException(ErrorCodeEnum.ERR_20002);
        }

        // Parse response and construct InternalUser DTO
        JSONArray array = responseJSON.optJSONArray("internaluserdetails_view");
        if (array != null && array.optJSONObject(0) != null) {
            JSONObject userProfileJSON = array.optJSONObject(0);
            LOG.debug("Fetch user-profile with username: Success");
            return JSONUtils.parse(userProfileJSON.toString(), InternalUser.class);
        }
        LOG.debug("No Profile found with the given username. Returning 'NULL'");
        return null;
    }

    /**
     * Method to set the set the Last-Login Time of an Internal user
     * 
     * @param userId
     * @param loginDateTime
     * @param requestInstance
     * @throws ApplicationException
     */
    public static void setLastDateLoginTime(String userId, Date loginDateTime, DataControllerRequest requestInstance)
            throws ApplicationException {

        if (StringUtils.isBlank(userId) || loginDateTime == null) {
            LOG.error("Invalid Parameters");
            throw new ApplicationException(ErrorCodeEnum.ERR_20005);
        }

        // Prepare Input Map
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("id", userId);
        inputMap.put("lastLogints", CommonUtilities.convertTimetoISO8601Format(loginDateTime));

        // Update failed login attempt count
        String serviceResponse = Executor.invokeService(ServiceURLEnum.SYSTEMUSER_UPDATE, inputMap, null,
                requestInstance);
        JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);

        // Check operation status
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
            throw new ApplicationException(ErrorCodeEnum.ERR_20005);
        }
    }
}
