package com.kony.adminconsole.service.campaignmanagement;

import java.util.HashMap;
import java.util.Map;

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

/**
 * Service to fetch campaign settings from the back end
 * 
 * @author Mohit Khosla (KH2356)
 */

public class CampaignSettingsFetchService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CampaignSettingsFetchService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();

        try {
            result = getCampaignSettings(requestInstance, result);
        } catch (ApplicationException ae) {
            ae.getErrorCodeEnum().setErrorCode(result);
            LOG.error("ApplicationException occured in CampaignSettingsFetchService JAVA service. Error: ", ae);
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            LOG.error("Exception occured in CampaignSettingsFetchService JAVA service. Error: ", e);
        }

        return result;
    }

    Result getCampaignSettings(DataControllerRequest requestInstance, Result result) throws ApplicationException {

        // ** Reading from 'application' table **
        Map<String, String> campaignSettingsMap = new HashMap<>();
        campaignSettingsMap.put(ODataQueryConstants.SELECT, "showAdsPostLogin");
        campaignSettingsMap.put(ODataQueryConstants.FILTER, "id eq 2");

        String response = Executor.invokeService(ServiceURLEnum.APPLICATION_READ, campaignSettingsMap, null,
                requestInstance);
        JSONObject responseJSON = CommonUtilities.getStringAsJSONObject(response);

        if (responseJSON != null && responseJSON.has(FabricConstants.OPSTATUS)
                && responseJSON.getInt(FabricConstants.OPSTATUS) == 0 && responseJSON.has("application")) {

            JSONArray responseJSONArray = responseJSON.getJSONArray("application");
            JSONObject responseJSONObject = responseJSONArray.getJSONObject(0);

            result.addParam(new Param("showFullScreenAd", responseJSONObject.getString("showAdsPostLogin"),
                    FabricConstants.BOOLEAN));
        } else {
            throw new ApplicationException(ErrorCodeEnum.ERR_21752);
        }

        return result;
    }
}