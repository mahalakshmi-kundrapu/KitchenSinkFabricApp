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
 * Service to fetch list of attributes from the back end
 * 
 * @author Mohit Khosla (KH2356)
 */

public class AttributesFetchService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(AttributesFetchService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();

        try {
            result = getAttributes(methodID, requestInstance, result);
        } catch (ApplicationException ae) {
            ae.getErrorCodeEnum().setErrorCode(result);
            LOG.error("ApplicationException occured in AttributesFetchService JAVA service. Error: ", ae);
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            LOG.error("Exception occured in AttributesFetchService JAVA service. Error: ", e);
        }

        return result;
    }

    Result getAttributes(String methodID, DataControllerRequest requestInstance, Result result)
            throws ApplicationException {

        // ** Reading from 'attribute' table **
        Map<String, String> attributeMap = new HashMap<>();

        String response = Executor.invokeService(ServiceURLEnum.ATTRIBUTE_READ, attributeMap, null, requestInstance);
        JSONObject responseJSON = CommonUtilities.getStringAsJSONObject(response);

        if (responseJSON != null && responseJSON.has(FabricConstants.OPSTATUS)
                && responseJSON.getInt(FabricConstants.OPSTATUS) == 0 && responseJSON.has("attribute")) {

            JSONArray responseJSONArray = responseJSON.getJSONArray("attribute");

            // -> Returning all attributes <-
            Dataset attributeDataset = new Dataset();
            attributeDataset.setId("attributes");

            for (int i = 0; i < responseJSONArray.length(); ++i) {

                JSONObject attributeResponse = responseJSONArray.getJSONObject(i);
                Record attributeRecord = new Record();

                attributeRecord.addParam(new Param("id", attributeResponse.getString("id"), FabricConstants.STRING));
                attributeRecord
                        .addParam(new Param("name", attributeResponse.getString("name"), FabricConstants.STRING));
                attributeRecord.addParam(
                        new Param("type", attributeResponse.getString("attributetype"), FabricConstants.STRING));
                attributeRecord
                        .addParam(new Param("range", attributeResponse.optString("range"), FabricConstants.STRING));
                attributeRecord.addParam(
                        new Param("helpText", attributeResponse.optString("helptext"), FabricConstants.STRING));

                JSONArray criteriasJSONArray = CommonUtilities
                        .getStringAsJSONArray(attributeResponse.optString("criterias"));
                Dataset criteriasDataset = criteriasJSONArray != null
                        ? CommonUtilities.constructDatasetFromJSONArray(criteriasJSONArray)
                        : new Dataset();
                criteriasDataset.setId("criterias");
                attributeRecord.addDataset(criteriasDataset);

                JSONArray optionsJSONArray = CommonUtilities
                        .getStringAsJSONArray(attributeResponse.optString("options"));
                Dataset optionsDataset = optionsJSONArray != null
                        ? CommonUtilities.constructDatasetFromJSONArray(optionsJSONArray)
                        : new Dataset();
                optionsDataset.setId("options");
                attributeRecord.addDataset(optionsDataset);

                attributeDataset.addRecord(attributeRecord);
            }

            result.addDataset(attributeDataset);
        } else {
            throw new ApplicationException(ErrorCodeEnum.ERR_21761);
        }

        return result;
    }
}