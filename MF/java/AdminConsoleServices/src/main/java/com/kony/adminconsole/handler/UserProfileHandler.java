/**
 * 
 */
package com.kony.adminconsole.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.controller.DataControllerRequest;

/**
 * Handler class to retrieve User Profile Data.
 * 
 * @author Aditya Mankal
 * 
 */
public class UserProfileHandler {

    private UserDetailsBean userDetailsBean;

    public UserProfileHandler(String userId, DataControllerRequest requestInstance) {
        initialiseBean(userId, null, requestInstance);
    }

    public UserProfileHandler(DataControllerRequest requestInstance, String userName) {
        initialiseBean(null, userName, requestInstance);
    }

    public UserProfileHandler(String userId, String username, DataControllerRequest requestInstance) {
        initialiseBean(userId, username, requestInstance);
    }

    private void initialiseBean(String userId, String username, DataControllerRequest requestInstance) {
        Map<String, String> postParametersMap = new HashMap<>();
        if (StringUtils.isNotBlank(userId)) {
            postParametersMap.put(ODataQueryConstants.FILTER, "id eq'" + userId + "'");
        }
        if (StringUtils.isNotBlank(username)) {
            postParametersMap.put(ODataQueryConstants.FILTER, "Username eq '" + username + "'");
        }
        String readUserProfileResponse = Executor.invokeService(ServiceURLEnum.INTERNALUSERDETAILS_VIEW_READ,
                postParametersMap, null, requestInstance);
        JSONObject readUserProfileResponseJSON = CommonUtilities.getStringAsJSONObject(readUserProfileResponse);
        parseResponseJSON(readUserProfileResponseJSON);
    }

    private void parseResponseJSON(JSONObject readUserProfileResponseJSON) {
        if (readUserProfileResponseJSON != null && readUserProfileResponseJSON.has(FabricConstants.OPSTATUS)
                && readUserProfileResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            JSONArray array = readUserProfileResponseJSON.optJSONArray("internaluserdetails_view");
            if (array != null && array.length() > 0) {
                JSONObject userProfileJSON = array.optJSONObject(0);
                if (userProfileJSON != null) {
                    String userId = userProfileJSON.optString("id");
                    String username = userProfileJSON.optString("Username");
                    String userEmail = userProfileJSON.optString("Email");
                    String userFirstName = userProfileJSON.optString("FirstName");
                    String userMiddleName = userProfileJSON.optString("MiddleName");
                    String userLastName = userProfileJSON.optString("LastName");
                    String userRoleId = userProfileJSON.optString("Role_id");
                    String userRoleName = userProfileJSON.optString("Role_Name");
                    boolean isAPIUser = false;
                    userDetailsBean = new UserDetailsBean(userId, username, userEmail, userFirstName, userMiddleName,
                            userLastName, userRoleName, userRoleId, isAPIUser, null);
                }
            }
        }
    }

    public UserDetailsBean getDetailsBean() {
        return userDetailsBean;
    }

}
