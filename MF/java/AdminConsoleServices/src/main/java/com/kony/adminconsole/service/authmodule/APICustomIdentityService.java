package com.kony.adminconsole.service.authmodule;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.dbp.core.constants.DBPConstants;
import com.kony.adminconsole.commons.crypto.EncryptionUtils;
import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.core.config.EnvironmentConfiguration;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Custom Identity service used by API consumers of Admin Console App
 * 
 * @author Venkateswara Rao Alla
 *
 */
public class APICustomIdentityService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(APICustomIdentityService.class);

    private static final String LOGIN_CALL_METHOD_ID = "login";

    private static final String API_ACCESS_TOKEN_KEY = "API_ACCESS_TOKEN";
    private static final String API_ACCESS_TOKEN_HEADER = "X-Kony-AC-API-Access-Token";

    public static final String API_ACCESS_PERMISSION = "API_ACCESS";

    public static final String API_USER_ID = "ADMIN_CONSOLE_API_USER";
    public static final String API_USER_TYPE = "C360_API_USER";

    private String encryptedAPIAccessToken = null;

    @Override
    public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
            DataControllerResponse response) throws Exception {
        Result result = new Result();
        if (StringUtils.equals(methodId, LOGIN_CALL_METHOD_ID)) {
            login(request, result);
        } else {
            logout(request, result);
        }
        return result;
    }

    private void login(DataControllerRequest request, Result result) {

        try {

            boolean proccedToAuthenticate = false;
            String accessTokenOnRequest = request.getHeader(API_ACCESS_TOKEN_HEADER);

            if (StringUtils.isNotBlank(accessTokenOnRequest)) {
                // fetch the encrypted access token and preserve it in this instance. This is to
                // avoid round trips to
                // database for each login call
                if (StringUtils.isBlank(this.encryptedAPIAccessToken)) {
                    // fetch the value from database and populates the respective instance variable
                    fetchConfiguredAccessToken(request);
                }

                // proceed to authenticate if encryptedAPIAccessToken variable is populated with
                // value properly
                if (StringUtils.isNotBlank(this.encryptedAPIAccessToken)) {
                    proccedToAuthenticate = true;
                }
            }

            if (proccedToAuthenticate
                    && accessTokenOnRequest.equals(EncryptionUtils.decrypt(this.encryptedAPIAccessToken,
                            EnvironmentConfiguration.AC_ENCRYPTION_KEY.getValue(request)))) {

                // user attributes
                Record userAttributes = new Record();
                userAttributes.setId(FabricConstants.USER_ATTRIBUTES);
                userAttributes.addParam(new Param("user_id", API_USER_ID, FabricConstants.STRING));
                userAttributes.addParam(
                        new Param(DBPConstants.CUSTOMER_TYPE_ID_IDENTITY_KEY, API_USER_TYPE, FabricConstants.STRING));
                result.addRecord(userAttributes);

                // security attributes
                Record securityAttributes = new Record();
                securityAttributes.setId(FabricConstants.SECURITY_ATTRIBUTES);

                // Add API Access Permission
                JSONArray permissionsArray = new JSONArray();
                permissionsArray.put(API_ACCESS_PERMISSION);
                securityAttributes.addParam(new Param(DBPConstants.PERMISSIONS_IDENTITY_KEY,
                        permissionsArray.toString(), FabricConstants.STRING));
                securityAttributes.addParam(
                        new Param(FabricConstants.SESSION_TOKEN, UUID.randomUUID().toString(), FabricConstants.STRING));
                securityAttributes.addParam(new Param(FabricConstants.SESSION_TTL, "-1", FabricConstants.INT));
                result.addRecord(securityAttributes);

                // HTTP status code
                result.addParam(new Param(FabricConstants.HTTP_STATUS_CODE, String.valueOf(HttpStatus.SC_OK),
                        FabricConstants.INT));
            } else {
                LOG.error("Provided access token is invalid");
                setAsBackendErrorCode(result, ErrorCodeEnum.ERR_21002, HttpStatus.SC_UNAUTHORIZED);
            }

        } catch (Exception e) {
            LOG.error("Error occured while authentication", e);
            setAsBackendErrorCode(result, ErrorCodeEnum.ERR_21001, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void logout(DataControllerRequest request, Result result) {
        try {
            request.getSession().invalidate();
            result.addParam(
                    new Param(FabricConstants.HTTP_STATUS_CODE, String.valueOf(HttpStatus.SC_OK), FabricConstants.INT));
        } catch (Exception e) {
            LOG.error("Error occured on logout", e);
        }
    }

    private synchronized void fetchConfiguredAccessToken(DataControllerRequest request) {
        if (StringUtils.isBlank(this.encryptedAPIAccessToken)) {
            Map<String, String> queryMap = new HashMap<>();
            queryMap.put(ODataQueryConstants.SELECT, "PropertyValue");
            queryMap.put(ODataQueryConstants.FILTER, "PropertyName eq '" + API_ACCESS_TOKEN_KEY + "'");
            String response = Executor.invokeService(ServiceURLEnum.SYSTEMCONFIGURATION_READ, queryMap, null, request);
            JSONObject jsonObject = CommonUtilities.getStringAsJSONObject(response);
            if (jsonObject != null) {
                JSONArray jsonArray = jsonObject.optJSONArray("systemconfiguration");
                if (jsonArray != null && jsonArray.length() > 0) {
                    this.encryptedAPIAccessToken = jsonArray.getJSONObject(0).getString("PropertyValue");
                }
            }
        }
    }

    public void setAsBackendErrorCode(Result result, ErrorCodeEnum errorCodeEnum, int httpStatus) {
        result.addParam(new Param(FabricConstants.BACKEND_ERROR_MESSAGE_KEY, errorCodeEnum.getMessage(),
                FabricConstants.STRING));
        result.addParam(new Param(FabricConstants.ERR_MSG, errorCodeEnum.getMessage(), FabricConstants.STRING));

        result.addParam(new Param(FabricConstants.BACKEND_ERROR_CODE_KEY, errorCodeEnum.getErrorCodeAsString(),
                FabricConstants.INT));
        result.addParam(new Param(FabricConstants.OPSTATUS, errorCodeEnum.getErrorCodeAsString(), FabricConstants.INT));

        result.addParam(new Param(FabricConstants.HTTP_STATUS_CODE, String.valueOf(httpStatus), FabricConstants.INT));
    }

}