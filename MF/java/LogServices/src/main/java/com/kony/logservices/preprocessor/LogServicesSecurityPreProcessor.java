package com.kony.logservices.preprocessor;

import java.util.HashMap;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import com.kony.logservices.util.EnvironmentConfiguration;
import com.kony.logservices.util.ErrorCodeEnum;
import com.konylabs.middleware.common.DataPreProcessor2;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * <p>
 * This PreProcessor verifies the authenticity of the consumers of log services calls by validating the shared secret. A
 * call to any public operation of the Log Services is expected to have the shared secret as a request header value.
 * 
 * @author Venkateswara Rao Alla
 *
 */

public class LogServicesSecurityPreProcessor implements DataPreProcessor2 {

    private static final Logger LOG = Logger.getLogger(LogServicesSecurityPreProcessor.class);

    public static final String LOG_SERVICES_API_ACCESS_TOKEN_HEADER = "X-Kony-Log-Services-API-Access-Token";

    @SuppressWarnings("rawtypes")
    @Override
    public boolean execute(HashMap inputMap, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance, Result serviceResult) throws Exception {

        boolean isAuthorized = true;

        try {
            if (!EnvironmentConfiguration.AC_LOG_SERVICES_API_ACCESS_TOKEN.getValue(requestInstance)
                    .equals(requestInstance.getHeader(LOG_SERVICES_API_ACCESS_TOKEN_HEADER))) {
                serviceResult.addParam(new Param(FabricConstants.ERR_MSG, ErrorCodeEnum.ERR_20000.getMessage(),
                        FabricConstants.STRING));
                serviceResult.addParam(new Param(FabricConstants.OPSTATUS,
                        ErrorCodeEnum.ERR_20000.getErrorCodeAsString(), FabricConstants.INT));
                serviceResult.addParam(new Param(FabricConstants.HTTP_STATUS_CODE,
                        String.valueOf(HttpStatus.SC_UNAUTHORIZED), FabricConstants.INT));
                isAuthorized = false;
            }
        } catch (Exception e) {
            LOG.error("Error occured while authentication", e);
            ErrorCodeEnum.ERR_20002.setErrorCode(serviceResult);
            serviceResult.addParam(new Param(FabricConstants.HTTP_STATUS_CODE,
                    String.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR), FabricConstants.INT));
            isAuthorized = false;
        }
        return isAuthorized;
    }

}
