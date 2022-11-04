package com.kony.adminconsole.service.customermanagement;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * DMSCustomerCreation service is used to create DMS user
 * 
 * @author Alahari Prudhvi Akhil (KH2346)
 * 
 */
public class DMSCustomerCreation implements JavaService2 {

    public static final String DEFAULT_GROUP = "DEFAULT_GROUP";
    public static final String DEFAULT_EUROPE_GROUP = "DEFAULT_EUROPE_GROUP";
    public static final String EUROPE_COUNTRY_CODE = "COUNTRY_ID5";
    private static final Logger LOG = Logger.getLogger(DMSCustomerCreation.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        String username = requestInstance.getParameter("username");
        try {
            Result processedResult = new Result();
            String group = requestInstance.getParameter("group");

            if (StringUtils.isBlank(username)) {
                ErrorCodeEnum.ERR_20705.setErrorCode(processedResult);
                processedResult.addParam(new Param("Status", "Failed", FabricConstants.STRING));
                return processedResult;
            }

            HashMap<String, String> postParametersMap = new HashMap<>();
            postParametersMap.put("_username", username);
            if (StringUtils.isBlank(group)) {
                postParametersMap.put("_group", DEFAULT_GROUP);
            } else {
                postParametersMap.put("_group", group);
            }

            if (!(StringUtils.isBlank(requestInstance.getParameter("countryCode")))) {
                if (requestInstance.getParameter("countryCode").equalsIgnoreCase(EUROPE_COUNTRY_CODE)) {
                    postParametersMap.put("_group", DEFAULT_EUROPE_GROUP);
                }
            }

            String response = Executor.invokeService(ServiceURLEnum.DMS_CREATE_USER_PROC, postParametersMap, null,
                    requestInstance);

            JSONObject createResponse = CommonUtilities.getStringAsJSONObject(response);
            if (createResponse != null && createResponse.has(FabricConstants.OPSTATUS)
                    && createResponse.getInt(FabricConstants.OPSTATUS) == 0) {
                processedResult.addParam(new Param("Status", "Success", FabricConstants.STRING));
            } else {
                ErrorCodeEnum.ERR_20001.setErrorCode(processedResult);
            }

            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

}