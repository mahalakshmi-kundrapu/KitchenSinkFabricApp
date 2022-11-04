package com.kony.adminconsole.service.staticcontentmanagement.termsandconditions;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.kony.adminconsole.utilities.StatusEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to Retrieve TermsAndConditions
 *
 * @author Aditya Mankal
 * 
 */
public class TermsAndConditionsGetService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(TermsAndConditionsGetService.class);
    private static final String DEFAULT_LANGUAGE_CODE = "en-US";
    private static final String TANDC_ACTIVE_STATUS_ID = StatusEnum.SID_TANDC_ACTIVE.name();
    private static final String DEFAULT_TERM_AND_CONDITION_CODE = "DBX_Default_TnC";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result processedResult = new Result();
            Map<String, String> postParametersMap = new HashMap<String, String>();
            postParametersMap.put(ODataQueryConstants.FILTER, "Code eq '" + DEFAULT_TERM_AND_CONDITION_CODE + "'");
            postParametersMap.put(ODataQueryConstants.SELECT, "id");

            String readTermsAndConditionsResponse = Executor.invokeService(ServiceURLEnum.TERMANDCONDITION_READ,
                    postParametersMap, null, requestInstance);
            JSONObject readTermsAndConditionsResponseJSON = CommonUtilities
                    .getStringAsJSONObject(readTermsAndConditionsResponse);
            int opStatusCode = readTermsAndConditionsResponseJSON.getInt(FabricConstants.OPSTATUS);
            if (opStatusCode == 0) {
                JSONArray termsAndConditionsJSONArray = readTermsAndConditionsResponseJSON
                        .getJSONArray("termandcondition");
                Dataset termsAndConditionsDataSet = new Dataset();
                termsAndConditionsDataSet.setId("records");
                Record currRecord = new Record();
                JSONObject currTermsAndConditionsJSONObject = termsAndConditionsJSONArray.getJSONObject(0);
                String termAndConditonId = currTermsAndConditionsJSONObject.getString("id");
                String languageCode = DEFAULT_LANGUAGE_CODE;
                postParametersMap.clear();
                postParametersMap.put(ODataQueryConstants.FILTER,
                        "TermAndConditionId eq '" + termAndConditonId + "' and LanguageCode eq '" + languageCode
                                + "' and Status_id eq '" + TANDC_ACTIVE_STATUS_ID + "'");
                postParametersMap.put(ODataQueryConstants.SELECT, "Content");
                String readTermAndConditionTextResponse = Executor.invokeService(
                        ServiceURLEnum.TERMANDCONDITIONTEXT_READ, postParametersMap, null, requestInstance);
                JSONObject readTermAndConditionTextResponseJSON = CommonUtilities
                        .getStringAsJSONObject(readTermAndConditionTextResponse);
                if (readTermAndConditionTextResponseJSON != null
                        && readTermAndConditionTextResponseJSON.has(FabricConstants.OPSTATUS)
                        && readTermAndConditionTextResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                        && readTermAndConditionTextResponseJSON.has("termandconditiontext")) {
                    JSONArray readTermAndConditionTextJSONArray = readTermAndConditionTextResponseJSON
                            .optJSONArray("termandconditiontext");
                    if (readTermAndConditionTextJSONArray == null || readTermAndConditionTextJSONArray.length() < 1) {
                        LOG.error("Failed to get Terms and Conditions text");
                        ErrorCodeEnum.ERR_20265.setErrorCode(processedResult);
                        processedResult.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        return processedResult;
                    } else {
                        JSONObject currTermAndConditionTextRecord = readTermAndConditionTextJSONArray.getJSONObject(0);
                        Param descriptionParam = new Param("Description",
                                currTermAndConditionTextRecord.getString("Content"), FabricConstants.STRING);
                        Param contentId_Param = new Param("id", "TER_CON_ID1", FabricConstants.STRING);
                        Param contentStatusId_Param = new Param("Status_id", "SID_ACTIVE", FabricConstants.STRING);
                        currRecord.addParam(descriptionParam);
                        currRecord.addParam(contentId_Param);
                        currRecord.addParam(contentStatusId_Param);

                        termsAndConditionsDataSet.addRecord(currRecord);

                        processedResult.addDataset(termsAndConditionsDataSet);
                        return processedResult;
                    }
                } else {
                    LOG.error("Failed to get Terms and Conditions text");
                    ErrorCodeEnum.ERR_20265.setErrorCode(processedResult);
                    processedResult.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    return processedResult;
                }

            }
            ErrorCodeEnum.ERR_20264.setErrorCode(processedResult);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }

    }

}