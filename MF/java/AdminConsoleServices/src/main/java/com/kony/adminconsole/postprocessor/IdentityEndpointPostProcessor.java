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
 * Postprocessor for the login operation in the C360IdentityOrchService Orchestration Service. <br>
 * Sets the httpStatusCode based on the authentication result
 * 
 * @author Aditya Mankal
 *
 */
public class IdentityEndpointPostProcessor implements DataPostProcessor2 {

    @Override
    public Object execute(Result result, DataControllerRequest request, DataControllerResponse response)
            throws Exception {

        // Read Inputs
        String backendErrorMessage = result.getParamValueByName(FabricConstants.BACKEND_ERROR_MESSAGE_KEY);
        String backendErrorCode = result.getParamValueByName(FabricConstants.BACKEND_ERROR_CODE_KEY);

        if (StringUtils.isNotBlank(backendErrorCode) || StringUtils.isNotBlank(backendErrorMessage)) {
            // Authentication Failure
            result.addParam(new Param(FabricConstants.HTTP_STATUS_CODE,
                    Integer.toString(HttpServletResponse.SC_UNAUTHORIZED), FabricConstants.INT));
        } else {
            // Authentication Success
            result.addParam(new Param(FabricConstants.HTTP_STATUS_CODE, Integer.toString(HttpServletResponse.SC_OK),
                    FabricConstants.INT));
        }

        return result;
    }

}
