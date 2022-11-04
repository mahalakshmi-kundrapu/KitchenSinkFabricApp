package com.kony.adminconsole.service.campaignmanagement;

import java.time.LocalDate;
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
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to fetch Campaign list / Campaign Priority list from the back end
 * 
 * @author Mohit Khosla (KH2356)
 */

public class CampaignsFetchService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CampaignsFetchService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();

        try {
            if (!methodID.contains("Priority")) {
                result = getCampaigns(methodID, requestInstance, result);
            } else {
                result = getCampaignPriorityList(methodID, requestInstance, result);
            }
        } catch (ApplicationException ae) {
            ae.getErrorCodeEnum().setErrorCode(result);
            LOG.error("ApplicationException occured in CampaignFetchService JAVA service. Error: ", ae);
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            LOG.error("Exception occured in CampaignFetchService JAVA service. Error: ", e);
        }

        return result;
    }

    Result getCampaigns(String methodID, DataControllerRequest requestInstance, Result result)
            throws ApplicationException {

        // ** Reading from 'campaign' table **
        Map<String, String> campaignMap = new HashMap<>();
        campaignMap.put(ODataQueryConstants.ORDER_BY, "end_datetime desc, name");

        String readCampaignResponse = Executor.invokeService(ServiceURLEnum.CAMPAIGN_READ, campaignMap, null,
                requestInstance);
        JSONObject readCampaignResponseJSON = CommonUtilities.getStringAsJSONObject(readCampaignResponse);

        if (readCampaignResponseJSON != null && readCampaignResponseJSON.has(FabricConstants.OPSTATUS)
                && readCampaignResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readCampaignResponseJSON.has("campaign")) {

            JSONArray readCampaignResponseJSONArray = readCampaignResponseJSON.getJSONArray("campaign");

            // -> Returning all campaigns <-
            Dataset campaignDataset = new Dataset();
            campaignDataset.setId("campaigns");

            for (int i = 0; i < readCampaignResponseJSONArray.length(); ++i) {

                JSONObject campaignResponse = readCampaignResponseJSONArray.getJSONObject(i);
                Record campaignRecord = new Record();

                campaignRecord.addParam(new Param("id", campaignResponse.getString("id"), FabricConstants.STRING));
                campaignRecord.addParam(new Param("name", campaignResponse.getString("name"), FabricConstants.STRING));
                campaignRecord
                        .addParam(new Param("priority", campaignResponse.getString("priority"), FabricConstants.INT));
                campaignRecord.addParam(
                        new Param("statusId", campaignResponse.getString("status_id"), FabricConstants.STRING));
                campaignRecord.addParam(new Param("startDateTime", campaignResponse.getString("start_datetime"),
                        FabricConstants.STRING));
                campaignRecord.addParam(
                        new Param("endDateTime", campaignResponse.getString("end_datetime"), FabricConstants.STRING));
                campaignRecord.addParam(
                        new Param("description", campaignResponse.getString("description"), FabricConstants.STRING));

                campaignDataset.addRecord(campaignRecord);
            }

            result.addDataset(campaignDataset);
        } else {
            throw new ApplicationException(ErrorCodeEnum.ERR_21701);
        }

        return result;
    }

    Result getCampaignPriorityList(String methodID, DataControllerRequest requestInstance, Result result)
            throws ApplicationException {

        // ** Reading from 'campaign' table **
        Map<String, String> campaignMap = new HashMap<>();
        campaignMap.put(ODataQueryConstants.FILTER,
                "status_id ne 'SID_TERMINATED' and end_datetime ge '" + LocalDate.now().toString() + "'");
        campaignMap.put(ODataQueryConstants.ORDER_BY, "priority");

        String readCampaignResponse = Executor.invokeService(ServiceURLEnum.CAMPAIGN_READ, campaignMap, null,
                requestInstance);
        JSONObject readCampaignResponseJSON = CommonUtilities.getStringAsJSONObject(readCampaignResponse);

        if (readCampaignResponseJSON != null && readCampaignResponseJSON.has(FabricConstants.OPSTATUS)
                && readCampaignResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readCampaignResponseJSON.has("campaign")) {

            JSONArray readCampaignResponseJSONArray = readCampaignResponseJSON.getJSONArray("campaign");

            // -> Returning all campaigns <-
            Dataset campaignDataset = new Dataset();
            campaignDataset.setId("campaignPriorityList");

            for (int i = 0; i < readCampaignResponseJSONArray.length(); ++i) {

                JSONObject campaignResponse = readCampaignResponseJSONArray.getJSONObject(i);
                Record campaignRecord = new Record();

                campaignRecord.addParam(new Param("name", campaignResponse.getString("name"), FabricConstants.STRING));
                campaignRecord
                        .addParam(new Param("priority", campaignResponse.getString("priority"), FabricConstants.INT));

                campaignDataset.addRecord(campaignRecord);
            }

            result.addDataset(campaignDataset);
        } else {
            throw new ApplicationException(ErrorCodeEnum.ERR_21719);
        }

        return result;
    }
}