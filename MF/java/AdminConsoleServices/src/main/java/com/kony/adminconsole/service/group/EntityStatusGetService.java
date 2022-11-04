package com.kony.adminconsole.service.group;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class EntityStatusGetService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(EntityStatusGetService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result processedResult = new Result();

        try {
            Map<String, String> queryMap = new HashMap<>();
            queryMap.put(ODataQueryConstants.FILTER, "status eq '0'");
            queryMap.put(ODataQueryConstants.SELECT, "Entity_id");

            String serviceResponse = Executor.invokeService(ServiceURLEnum.ENTITYSTATUS_READ, queryMap, null,
                    requestInstance);
            JSONObject statusReadResponse = CommonUtilities.getStringAsJSONObject(serviceResponse);
            if (statusReadResponse == null || !statusReadResponse.has(FabricConstants.OPSTATUS)
                    || statusReadResponse.getInt(FabricConstants.OPSTATUS) != 0
                    || statusReadResponse.optJSONArray("entitystatus") == null) {
                LOG.error("Failed to read entity status");
                throw new ApplicationException(ErrorCodeEnum.ERR_20431);
            }
            JSONArray entityStatusArray = statusReadResponse.optJSONArray("entitystatus");
            JSONArray entityIds = new JSONArray();
            JSONObject currJSON;
            for (Object currObject : entityStatusArray) {
                if (currObject instanceof JSONObject) {
                    currJSON = (JSONObject) currObject;
                    if (StringUtils.isNotBlank(currJSON.optString("Entity_id"))) {
                        entityIds.put(currJSON.optString("Entity_id"));
                    }
                }
            }
            Param entityIdsParam = new Param("entityIds", entityIds.toString(), FabricConstants.STRING);
            processedResult.addParam(entityIdsParam);
            return processedResult;
        } catch (ApplicationException e) {
            LOG.error("");
            e.getErrorCodeEnum().setErrorCode(processedResult);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

}
