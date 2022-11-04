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
 * Service to fetch default campaign details (description & specification) from the back end
 * 
 * @author Mohit Khosla (KH2356)
 */

public class DefaultCampaignFetchService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(DefaultCampaignFetchService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();

        try {
            result = getDefaultCampaignDescription(requestInstance, result);
            result = getDefaultCampaignSpecifications(requestInstance, result);
        } catch (ApplicationException ae) {
            ae.getErrorCodeEnum().setErrorCode(result);
            LOG.error("ApplicationException occured in DefaultCampaignFetchService JAVA service. Error: ", ae);
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            LOG.error("Exception occured in DefaultCampaignFetchService JAVA service. Error: ", e);
        }

        return result;
    }

    Result getDefaultCampaignDescription(DataControllerRequest requestInstance, Result result)
            throws ApplicationException {

        // ** Reading from 'defaultcampaign' table **
        Map<String, String> defaultCampaignMap = new HashMap<>();

        String readDefaultCampaignResponse = Executor.invokeService(ServiceURLEnum.DEFAULTCAMPAIGN_READ,
                defaultCampaignMap, null, requestInstance);
        JSONObject readDefaultCampaignResponseJSON = CommonUtilities.getStringAsJSONObject(readDefaultCampaignResponse);

        if (readDefaultCampaignResponseJSON != null && readDefaultCampaignResponseJSON.has(FabricConstants.OPSTATUS)
                && readDefaultCampaignResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readDefaultCampaignResponseJSON.has("defaultcampaign")) {

            JSONArray readDefaultCampaignResponseJSONArray = readDefaultCampaignResponseJSON
                    .getJSONArray("defaultcampaign");

            JSONObject defaultCampaignResponse = readDefaultCampaignResponseJSONArray.getJSONObject(0);

            result.addParam(
                    new Param("description", defaultCampaignResponse.getString("description"), FabricConstants.STRING));
        } else {
            throw new ApplicationException(ErrorCodeEnum.ERR_21746);
        }

        return result;
    }

    Result getDefaultCampaignSpecifications(DataControllerRequest requestInstance, Result result)
            throws ApplicationException {

        // ** Reading from 'defaultcampaignspecification_get_c360_proc' stored procedure
        // **
        Map<String, String> postParametersMap = new HashMap<String, String>();

        String readCampaignSpecificationResponse = Executor.invokeService(
                ServiceURLEnum.CAMPAIGN_C360_DEFAULT_SPECIFICATION_GET_PROC, postParametersMap, null, requestInstance);
        JSONObject readCampaignSpecificationResponseJSON = CommonUtilities
                .getStringAsJSONObject(readCampaignSpecificationResponse);

        if (readCampaignSpecificationResponseJSON != null
                && readCampaignSpecificationResponseJSON.has(FabricConstants.OPSTATUS)
                && readCampaignSpecificationResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readCampaignSpecificationResponseJSON.has("records")) {

            JSONArray readCampaignSpecificationResponseJSONArray = readCampaignSpecificationResponseJSON
                    .getJSONArray("records");

            // -> Returning campaign specifications by arranging them within each channel &
            // location <-
            Record defaultCampaignSpecificationsRecord = new Record();
            defaultCampaignSpecificationsRecord.setId("specifications");

            for (int i = 0; i < readCampaignSpecificationResponseJSONArray.length(); ++i) {

                JSONObject response = readCampaignSpecificationResponseJSONArray.getJSONObject(i);

                String imageIndex = response.getString("image_index");
                String imageURL = response.optString("image_url");
                String destinationURL = response.optString("destination_url");
                String imageResolution = response.getString("image_resolution");
                String imageScale = response.getString("image_scale");

                Record campaignRecord = new Record();
                campaignRecord.addParam(new Param("imageIndex", imageIndex, FabricConstants.STRING));
                campaignRecord.addParam(new Param("imageURL", imageURL, FabricConstants.STRING));
                campaignRecord.addParam(new Param("destinationURL", destinationURL, FabricConstants.STRING));
                campaignRecord.addParam(new Param("imageResolution", imageResolution, FabricConstants.STRING));
                campaignRecord.addParam(new Param("imageScale", imageScale, FabricConstants.STRING));

                String channel = response.getString("channel");
                String screen = response.getString("screen");

                if (defaultCampaignSpecificationsRecord.getRecordById(channel) == null) {

                    Record channelRecord = new Record();
                    channelRecord.setId(channel);

                    Dataset screenDataset = new Dataset();
                    screenDataset.setId(screen);
                    screenDataset.addRecord(campaignRecord);

                    channelRecord.addDataset(screenDataset);
                    defaultCampaignSpecificationsRecord.addRecord(channelRecord);
                } else {
                    Record channelRecord = defaultCampaignSpecificationsRecord.getRecordById(channel);

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

                    defaultCampaignSpecificationsRecord.addRecord(channelRecord);
                }
            }

            result.addRecord(defaultCampaignSpecificationsRecord);
        } else {
            throw new ApplicationException(ErrorCodeEnum.ERR_21748);
        }

        return result;
    }
}