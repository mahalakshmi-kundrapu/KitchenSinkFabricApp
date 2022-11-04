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
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to fetch Campaign customer roles from the back end
 * 
 * @author Mohit Khosla (KH2356)
 */

public class CampaignGroupsFetchService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CampaignGroupsFetchService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();

        try {
            result = getCampaignGroups(methodID, requestInstance, result);
        } catch (ApplicationException ae) {
            ae.getErrorCodeEnum().setErrorCode(result);
            LOG.error("ApplicationException occured in CampaignGroupsFetchService JAVA service. Error: ", ae);
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            LOG.error("Exception occured in CampaignGroupsFetchService JAVA service. Error: ", e);
        }

        return result;
    }

    Result getCampaignGroups(String methodID, DataControllerRequest requestInstance, Result result)
            throws ApplicationException {

        // ** Reading from 'membergroup' table **
        Map<String, String> groupMap = new HashMap<>();
        groupMap.put(ODataQueryConstants.FILTER, "Type_id eq 'TYPE_ID_CAMPAIGN'");

        String response = Executor.invokeService(ServiceURLEnum.MEMBERGROUP_READ, groupMap, null, requestInstance);
        JSONObject responseJSON = CommonUtilities.getStringAsJSONObject(response);

        if (responseJSON != null && responseJSON.has(FabricConstants.OPSTATUS)
                && responseJSON.getInt(FabricConstants.OPSTATUS) == 0 && responseJSON.has("membergroup")) {

            JSONArray responseJSONArray = responseJSON.getJSONArray("membergroup");

            // -> Returning all groups <-
            Dataset groupDataset = new Dataset();
            groupDataset.setId("groups");

            for (int i = 0; i < responseJSONArray.length(); ++i) {

                JSONObject groupResponse = responseJSONArray.getJSONObject(i);
                Record groupRecord = new Record();

                groupRecord.addParam(new Param("id", groupResponse.getString("id"), FabricConstants.STRING));
                groupRecord.addParam(new Param("name", groupResponse.getString("Name"), FabricConstants.STRING));
                groupRecord.addParam(
                        new Param("description", groupResponse.getString("Description"), FabricConstants.STRING));

                groupDataset.addRecord(groupRecord);
            }

            result.addDataset(groupDataset);
        } else {
            throw new ApplicationException(ErrorCodeEnum.ERR_21781);
        }

        return result;
    }
}