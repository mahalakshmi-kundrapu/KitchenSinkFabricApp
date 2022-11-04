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
 * Service to check if a Campaign exists at the back end
 * 
 * @author Mohit Khosla (KH2356)
 */

public class CampaignExistsCheckService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CampaignExistsCheckService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();

        try {
            String campaignName = requestInstance.getParameter("name");
            if (campaignName == null) {
                throw new ApplicationException(ErrorCodeEnum.ERR_21710);
            }

            result = checkIfCampaignExists(campaignName, requestInstance, result);
        } catch (ApplicationException ae) {
            ae.getErrorCodeEnum().setErrorCode(result);
            LOG.error("ApplicationException occured in CampaignExistsCheckService JAVA service. Error: ", ae);
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            LOG.error("Exception occured in CampaignExistsCheckService JAVA service. Error: ", e);
        }

        return result;
    }

    Result checkIfCampaignExists(String campaignName, DataControllerRequest requestInstance, Result result)
            throws ApplicationException {

        // ** Reading from 'campaign' table **
        Map<String, String> campaignMap = new HashMap<>();
        campaignMap.put(ODataQueryConstants.FILTER, "name eq '" + campaignName + "'");

        String readCampaignResponse = Executor.invokeService(ServiceURLEnum.CAMPAIGN_READ, campaignMap, null,
                requestInstance);
        JSONObject readCampaignResponseJSON = CommonUtilities.getStringAsJSONObject(readCampaignResponse);

        if (readCampaignResponseJSON != null && readCampaignResponseJSON.has(FabricConstants.OPSTATUS)
                && readCampaignResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readCampaignResponseJSON.has("campaign")) {

            JSONArray readCampaignResponseJSONArray = readCampaignResponseJSON.getJSONArray("campaign");

            boolean campaignExists = false;

            if (readCampaignResponseJSONArray.length() > 1) {
                throw new ApplicationException(ErrorCodeEnum.ERR_21701);
            } else if (readCampaignResponseJSONArray.length() == 1) {
                campaignExists = true;
            }

            result.addParam(new Param("campaignExists", "" + campaignExists, FabricConstants.BOOLEAN));
        } else {
            throw new ApplicationException(ErrorCodeEnum.ERR_21701);
        }

        return result;
    }
}