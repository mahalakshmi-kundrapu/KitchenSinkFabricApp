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
 * Service to fetch list of Campaign resolutions for all channels & screens from the backend (Channel, screen,
 * resolution info is part of the master-data. There is no provision to add more entries from the front-end)
 * 
 * @author Mohit Khosla (KH2356)
 */

public class ChannelsAndScreensInformationFetchService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(ChannelsAndScreensInformationFetchService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();

        try {
            Map<String, String> campaignCounts = getCampaignCount(requestInstance);

            // ** Reading from 'campaignplaceholder' table **
            Map<String, String> campaignResolutionMap = new HashMap<>();

            String readCampaignPlaceholderResponse = Executor.invokeService(ServiceURLEnum.CAMPAIGNPLACEHOLDER_READ,
                    campaignResolutionMap, null, requestInstance);
            JSONObject readCampaignPlaceholderResponseJSON = CommonUtilities
                    .getStringAsJSONObject(readCampaignPlaceholderResponse);

            if (readCampaignPlaceholderResponseJSON != null
                    && readCampaignPlaceholderResponseJSON.has(FabricConstants.OPSTATUS)
                    && readCampaignPlaceholderResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readCampaignPlaceholderResponseJSON.has("campaignplaceholder")) {

                JSONArray readCampaignPlaceholderResponseJSONArray = readCampaignPlaceholderResponseJSON
                        .getJSONArray("campaignplaceholder");

                // -> Returning the list of channels, screens within each channel, and the
                // resolutions available in each screen <-
                Map<String, Map<String, Dataset>> screenMap = new HashMap<String, Map<String, Dataset>>();

                for (int i = 0; i < readCampaignPlaceholderResponseJSONArray.length(); ++i) {

                    JSONObject readCampaignPlaceholderResponseJSONObject = readCampaignPlaceholderResponseJSONArray
                            .getJSONObject(i);

                    String screen = readCampaignPlaceholderResponseJSONObject.getString("screen");
                    String channel = readCampaignPlaceholderResponseJSONObject.getString("channel");
                    String imageResolution = readCampaignPlaceholderResponseJSONObject.getString("image_resolution");
                    String imageScale = readCampaignPlaceholderResponseJSONObject.getString("image_scale");
                    String imageSize = readCampaignPlaceholderResponseJSONObject.getString("image_size");

                    String campaignCountKey = channel + "_" + screen + "_" + imageResolution;
                    String campaignCount = campaignCounts.get(campaignCountKey) != null
                            ? campaignCounts.get(campaignCountKey)
                            : "0";

                    Record campaignRecord = new Record();
                    campaignRecord.addParam(new Param("campaignCount", campaignCount, FabricConstants.INT));
                    campaignRecord.addParam(new Param("imageResolution", imageResolution, FabricConstants.STRING));
                    campaignRecord.addParam(new Param("imageScale", imageScale, FabricConstants.STRING));
                    campaignRecord.addParam(new Param("imageSize", imageSize, FabricConstants.STRING));

                    Dataset resolutionDataset = null;

                    if (screenMap.get(screen) == null) {

                        resolutionDataset = new Dataset();
                        resolutionDataset.setId("resolutions");
                        resolutionDataset.addRecord(campaignRecord);

                        Map<String, Dataset> channelMap = new HashMap<String, Dataset>();
                        channelMap.put(channel, resolutionDataset);
                        screenMap.put(screen, channelMap);
                    } else {
                        Map<String, Dataset> channelMap = screenMap.get(screen);

                        if (channelMap.get(channel) == null) {
                            resolutionDataset = new Dataset();
                            resolutionDataset.setId("resolutions");
                        } else {
                            resolutionDataset = channelMap.get(channel);
                        }
                        resolutionDataset.addRecord(campaignRecord);

                        channelMap.put(channel, resolutionDataset);
                        screenMap.put(screen, channelMap);
                    }
                }

                Dataset channelsAndScreensDataset = new Dataset();
                channelsAndScreensDataset.setId("screens");

                for (Map.Entry<String, Map<String, Dataset>> screen : screenMap.entrySet()) {

                    Record screenRecord = new Record();
                    screenRecord.addParam(new Param("screenId", screen.getKey()));

                    Dataset channelDataset = new Dataset();
                    channelDataset.setId("channels");

                    Map<String, Dataset> channelMap = screen.getValue();

                    for (Map.Entry<String, Dataset> channel : channelMap.entrySet()) {

                        Record channelRecord = new Record();
                        channelRecord.addParam(new Param("channelId", channel.getKey()));

                        channelRecord.addDataset(channel.getValue());
                        channelDataset.addRecord(channelRecord);
                    }

                    screenRecord.addDataset(channelDataset);
                    channelsAndScreensDataset.addRecord(screenRecord);
                }

                result.addDataset(channelsAndScreensDataset);
            } else {
                throw new ApplicationException(ErrorCodeEnum.ERR_21720);
            }
        } catch (ApplicationException ae) {
            ae.getErrorCodeEnum().setErrorCode(result);
            LOG.error("ApplicationException occured in ChannelsAndScreensInformationFetchService JAVA service. Error: ",
                    ae);
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            LOG.error("Exception occured in ChannelsAndScreensInformationFetchService JAVA service. Error: ", e);
        }

        return result;
    }

    public Map<String, String> getCampaignCount(DataControllerRequest requestInstance) throws ApplicationException {

        Map<String, String> campaignCounts = new HashMap<String, String>();
        Map<String, String> procMap = new HashMap<>();

        String readResponse = Executor.invokeService(ServiceURLEnum.CAMPAIGN_C360_COUNT_PROC, procMap, null,
                requestInstance);
        JSONObject readResponseJSON = CommonUtilities.getStringAsJSONObject(readResponse);

        if (readResponseJSON != null && readResponseJSON.has(FabricConstants.OPSTATUS)
                && readResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {

            JSONArray readResponseJSONArray = readResponseJSON.getJSONArray("records");

            for (int i = 0; i < readResponseJSONArray.length(); ++i) {

                JSONObject readResponseJSONObject = readResponseJSONArray.getJSONObject(i);

                String campaignCountKey = readResponseJSONObject.getString("channel") + "_"
                        + readResponseJSONObject.getString("screen") + "_"
                        + readResponseJSONObject.getString("image_resolution");
                String campaignCount = readResponseJSONObject.optString("campaign_count");

                campaignCounts.put(campaignCountKey, campaignCount);
            }
        } else {
            throw new ApplicationException(ErrorCodeEnum.ERR_21754);
        }

        return campaignCounts;
    }
}