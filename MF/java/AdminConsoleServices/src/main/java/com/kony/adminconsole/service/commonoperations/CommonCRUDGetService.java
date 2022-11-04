package com.kony.adminconsole.service.commonoperations;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.handler.PaginationHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

/**
 * @author Aditya Mankal, Akhil Alahari
 * 
 * 
 *         Generic Service to retrieve records. Supports Pagination.
 * 
 */
public class CommonCRUDGetService implements JavaService2 {

    private static final String READ_OPERATION_SUFFIX = "_READ";
    private static final Logger LOG = Logger.getLogger(CommonCRUDGetService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
        String entityName = methodID.substring(4);
        return get(authToken, requestInstance, entityName);
    }

    private Result get(String authToken, DataControllerRequest requestInstance, String entityName) {

        ServiceURLEnum serviceURLEnum = null;
        try {
            serviceURLEnum = ServiceURLEnum.valueOf(entityName.toUpperCase() + READ_OPERATION_SUFFIX);
        } catch (IllegalArgumentException iae) {
            LOG.error(iae);
            Result processedResult = new Result();
            ErrorCodeEnum.ERR_20874.setErrorCode(processedResult);
            return processedResult;
        }
        LOG.debug("Resolved URL:" + serviceURLEnum.getServiceURL());
        Map<String, String> postParametersMap = new HashMap<String, String>();
        Map<String, String> headerParametersMap = new HashMap<String, String>();

        @SuppressWarnings("unchecked")
        Map<String, String> queryParamsMap = (Map<String, String>) requestInstance.getAttribute("queryparams");
        /*
         * Reading the Query Parameter Map for OData Query Inputs. The Post Body is later processed with a higher
         * precedence. Any common value is overwritten by that in the Post Body
         */
        if (queryParamsMap != null && !queryParamsMap.isEmpty()) {
            if (queryParamsMap.containsKey(ODataQueryConstants.SELECT))
                postParametersMap.put(ODataQueryConstants.SELECT, queryParamsMap.get(ODataQueryConstants.SELECT));
            if (queryParamsMap.containsKey(ODataQueryConstants.FILTER))
                postParametersMap.put(ODataQueryConstants.FILTER, queryParamsMap.get(ODataQueryConstants.FILTER));
            if (queryParamsMap.containsKey(ODataQueryConstants.ORDER_BY))
                postParametersMap.put(ODataQueryConstants.ORDER_BY, queryParamsMap.get(ODataQueryConstants.ORDER_BY));
            if (queryParamsMap.containsKey(ODataQueryConstants.TOP))
                postParametersMap.put(ODataQueryConstants.TOP, queryParamsMap.get(ODataQueryConstants.TOP));
            if (queryParamsMap.containsKey(ODataQueryConstants.SKIP))
                postParametersMap.put(ODataQueryConstants.SKIP, queryParamsMap.get(ODataQueryConstants.SKIP));
            if (queryParamsMap.containsKey(ODataQueryConstants.EXPAND))
                postParametersMap.put(ODataQueryConstants.EXPAND, queryParamsMap.get(ODataQueryConstants.EXPAND));
        }

        if (requestInstance.containsKeyInRequest(ODataQueryConstants.SELECT))
            postParametersMap.put(ODataQueryConstants.SELECT, requestInstance.getParameter(ODataQueryConstants.SELECT));
        if (requestInstance.containsKeyInRequest(ODataQueryConstants.FILTER))
            postParametersMap.put(ODataQueryConstants.FILTER, requestInstance.getParameter(ODataQueryConstants.FILTER));
        if (requestInstance.containsKeyInRequest(ODataQueryConstants.ORDER_BY))
            postParametersMap.put(ODataQueryConstants.ORDER_BY,
                    requestInstance.getParameter(ODataQueryConstants.ORDER_BY));
        if (requestInstance.containsKeyInRequest(ODataQueryConstants.TOP))
            postParametersMap.put(ODataQueryConstants.TOP, requestInstance.getParameter(ODataQueryConstants.TOP));
        if (requestInstance.containsKeyInRequest(ODataQueryConstants.SKIP))
            postParametersMap.put(ODataQueryConstants.SKIP, requestInstance.getParameter(ODataQueryConstants.SKIP));
        if (requestInstance.containsKeyInRequest(ODataQueryConstants.EXPAND))
            postParametersMap.put(ODataQueryConstants.EXPAND, requestInstance.getParameter(ODataQueryConstants.EXPAND));

        return CommonUtilities.getResultObjectFromJSONObject(PaginationHandler.getPaginatedData(serviceURLEnum,
                postParametersMap, headerParametersMap, requestInstance));
    }

}