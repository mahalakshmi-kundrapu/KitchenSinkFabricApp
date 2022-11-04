package com.kony.adminconsole.service.campaignmanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
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
 * Service to fetch Campaign specifications & groups pertaining to a Campaign from the back end
 * 
 * @author Mohit Khosla (KH2356)
 */

public class CampaignSpecificationsAndGroupsFetchService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CampaignSpecificationsAndGroupsFetchService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();

        try {
            String campaignId = requestInstance.getParameter("campaignId");
            if (campaignId == null) {
                throw new ApplicationException(ErrorCodeEnum.ERR_21709);
            }

            result = getCampaignSpecifications(requestInstance, campaignId, result);
            result = getCampaignGroups(requestInstance, campaignId, result);
        } catch (ApplicationException ae) {
            ae.getErrorCodeEnum().setErrorCode(result);
            LOG.error(
                    "ApplicationException occured in CampaignSpecificationsAndGroupsFetchService JAVA service. Error: ",
                    ae);
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            LOG.error("Exception occured in CampaignSpecificationsAndGroupsFetchService JAVA service. Error: ", e);
        }

        return result;
    }

    Result getCampaignSpecifications(DataControllerRequest requestInstance, String campaignId, Result result)
            throws ApplicationException {

        // ** Reading from 'campaign_c360_specification_get_proc' stored procedure **
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("_campaignId", campaignId);

        String readCampaignSpecificationResponse = Executor.invokeService(
                ServiceURLEnum.CAMPAIGN_C360_SPECIFICATION_GET_PROC, postParametersMap, null, requestInstance);
        JSONObject readCampaignSpecificationResponseJSON = CommonUtilities
                .getStringAsJSONObject(readCampaignSpecificationResponse);

        if (readCampaignSpecificationResponseJSON != null
                && readCampaignSpecificationResponseJSON.has(FabricConstants.OPSTATUS)
                && readCampaignSpecificationResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readCampaignSpecificationResponseJSON.has("records")) {

            JSONArray readCampaignSpecificationResponseJSONArray = readCampaignSpecificationResponseJSON
                    .getJSONArray("records");

            // -> Returning Campaign specifications by arranging them within each channel &
            // location <-
            Record campaignSpecificationsRecord = new Record();
            campaignSpecificationsRecord.setId("campaignSpecifications");

            for (int i = 0; i < readCampaignSpecificationResponseJSONArray.length(); ++i) {

                JSONObject campaignURLResponse = readCampaignSpecificationResponseJSONArray.getJSONObject(i);

                String imageURL = campaignURLResponse.getString("image_url");
                String destinationURL = campaignURLResponse.optString("destination_url");
                String imageResolution = campaignURLResponse.getString("image_resolution");
                String imageScale = campaignURLResponse.getString("image_scale");

                Record campaignRecord = new Record();
                campaignRecord.addParam(new Param("imageURL", imageURL, FabricConstants.STRING));
                campaignRecord.addParam(new Param("destinationURL", destinationURL, FabricConstants.STRING));
                campaignRecord.addParam(new Param("imageResolution", imageResolution, FabricConstants.STRING));
                campaignRecord.addParam(new Param("imageScale", imageScale, FabricConstants.STRING));

                String channel = campaignURLResponse.getString("channel");
                String screen = campaignURLResponse.getString("screen");

                if (campaignSpecificationsRecord.getRecordById(channel) == null) {

                    Record channelRecord = new Record();
                    channelRecord.setId(channel);

                    Dataset screenDataset = new Dataset();
                    screenDataset.setId(screen);
                    screenDataset.addRecord(campaignRecord);

                    channelRecord.addDataset(screenDataset);
                    campaignSpecificationsRecord.addRecord(channelRecord);
                } else {
                    Record channelRecord = campaignSpecificationsRecord.getRecordById(channel);

                    if (channelRecord.getDatasetById(screen) == null) {

                        Dataset screenDataset = new Dataset();
                        screenDataset.setId(screen);
                        screenDataset.addRecord(campaignRecord);

                        channelRecord.addDataset(screenDataset);
                    } else {

                        Dataset screenDataset = channelRecord.getDatasetById(screen);
                        screenDataset.addRecord(campaignRecord);

                        channelRecord.addDataset(screenDataset);
                    }

                    campaignSpecificationsRecord.addRecord(channelRecord);
                }
            }

            result.addRecord(campaignSpecificationsRecord);

        } else {
            throw new ApplicationException(ErrorCodeEnum.ERR_21705);
        }

        return result;
    }

    Result getCampaignGroups(DataControllerRequest requestInstance, String campaignId, Result result)
            throws ApplicationException {

        // ** Reading from 'campaigngroup_membergroup_read' stored procedure **
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("_campaignId", campaignId);

        String readResponse = Executor.invokeService(ServiceURLEnum.CAMPAIGN_C360_GROUP_GET_PROC, postParametersMap,
                null, requestInstance);
        JSONObject readResponseJSON = CommonUtilities.getStringAsJSONObject(readResponse);

        if (readResponseJSON != null && readResponseJSON.has(FabricConstants.OPSTATUS)
                && readResponseJSON.getInt(FabricConstants.OPSTATUS) == 0 && readResponseJSON.has("records")) {

            Dataset campaigngroupDataset = new Dataset();
            campaigngroupDataset.setId("campaignGroups");

            JSONArray readResponseJSONArray = readResponseJSON.optJSONArray("records");
            for (int i = 0; i < readResponseJSONArray.length(); ++i) {

                JSONObject readResponseJSONObject = readResponseJSONArray.getJSONObject(i);
                Record campaigngroupRecord = new Record();

                campaigngroupRecord.addParam(
                        new Param("groupId", readResponseJSONObject.getString("group_id"), FabricConstants.STRING));
                campaigngroupRecord.addParam(
                        new Param("groupName", readResponseJSONObject.getString("group_name"), FabricConstants.STRING));
                campaigngroupRecord.addParam(
                        new Param("groupDesc", readResponseJSONObject.getString("group_desc"), FabricConstants.STRING));

                JSONArray attributesJSONArray = CommonUtilities
                        .getStringAsJSONArray(readResponseJSONObject.optString("attributes"));
                Dataset attributesDataset = attributesJSONArray != null
                        ? CommonUtilities.constructDatasetFromJSONArray(attributesJSONArray)
                        : new Dataset();
                attributesDataset.setId("groupAttributes");
                campaigngroupRecord.addDataset(attributesDataset);

                campaigngroupRecord.addParam(new Param("groupCustomerCount",
                        !(readResponseJSONObject.optString("customer_count").equals(""))
                                ? readResponseJSONObject.getString("customer_count")
                                : "0",
                        FabricConstants.INT));

                campaigngroupDataset.addRecord(campaigngroupRecord);
            }

            result.addDataset(campaigngroupDataset);
        } else {
            throw new ApplicationException(ErrorCodeEnum.ERR_21721);
        }

        return result;
    }
}