package com.kony.adminconsole.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.controller.DataControllerRequest;

public class StatusHandler {
    private static final Logger LOG = Logger.getLogger(StatusHandler.class);

    public String getStatusIdForGivenStatus(String systemUser, String status, String authToken,
            DataControllerRequest requestInstance) {
        String statusId = "";
        try {
            Map<String, String> postParametersMap = new HashMap<String, String>();
            postParametersMap.put(ODataQueryConstants.FILTER, "Description eq'" + status + "'");
            String readEndpointResponse = Executor.invokeService(ServiceURLEnum.STATUS_READ, postParametersMap, null,
                    requestInstance);
            JSONArray statusArray = (JSONArray) CommonUtilities.getStringAsJSONObject(readEndpointResponse)
                    .get("status");
            if (statusArray.length() != 0 || statusArray != null) {
                statusId = ((JSONObject) statusArray.get(0)).getString("id");
            }
        } catch (Exception e) {
            LOG.error("Exception in StatusHandler getStatusIdForGivenStatus", e);
        }

        return statusId;
    }
}
