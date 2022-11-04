package com.kony.adminconsole.postprocessor;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.kony.adminconsole.commons.utils.FabricConstants;
import com.konylabs.middleware.common.DataPostProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * Postprocessor for the User Authentication JSON Service. <br>
 * Sets the httpStatusCode and the attribute {@link FabricConstants.IS_AUTHENTICATED_KEY} to true if the authentication
 * is successful
 * 
 * @author Aditya Mankal
 *
 */
public class UserAuthenticationPostProcessor implements DataPostProcessor2 {

    private static final String USERNAME_PARAM = "username";

    @Override
    public Object execute(Result result, DataControllerRequest request, DataControllerResponse response)
            throws Exception {

        // Read Inputs
        String username = result.getParamValueByName(USERNAME_PARAM);
        String backendErrorMessage = result.getParamValueByName(FabricConstants.BACKEND_ERROR_MESSAGE_KEY);
        String backendErrorCode = result.getParamValueByName(FabricConstants.BACKEND_ERROR_CODE_KEY);

        if (StringUtils.isNotBlank(backendErrorCode) || StringUtils.isNotBlank(backendErrorMessage)
                || StringUtils.isBlank(username)) {
            // Authentication Failure
            result.addParam(new Param(FabricConstants.HTTP_STATUS_CODE,
                    Integer.toString(HttpServletResponse.SC_UNAUTHORIZED), FabricConstants.INT));
        } else {
            // Authentication Success
            request.setAttribute(FabricConstants.IS_AUTHENTICATED_KEY, String.valueOf(true));
            result.addParam(new Param(FabricConstants.HTTP_STATUS_CODE, Integer.toString(HttpServletResponse.SC_OK),
                    FabricConstants.INT));
        }

        return result;
    }
}
