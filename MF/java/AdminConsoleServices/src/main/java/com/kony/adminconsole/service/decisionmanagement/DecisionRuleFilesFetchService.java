package com.kony.adminconsole.service.decisionmanagement;

/*
* 
* @author Sai Krishna Aitha
*
*/
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.utilities.DBPServices;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

public class DecisionRuleFilesFetchService implements JavaService2 {

    public static final Logger LOG = Logger.getLogger(DecisionRuleFilesFetchService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        try {
            // Read Input
            String decisionId = requestInstance.getParameter("decisionId");
            String decisionName = requestInstance.getParameter("decisionName");
            // Validate Inputs
            if (StringUtils.isBlank(decisionName) && StringUtils.isBlank(decisionId)) {
                LOG.debug("Name and Id shouldn't be empty");
                ErrorCodeEnum.ERR_21581.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            if (StringUtils.isBlank(decisionId)) {
                decisionId = StringUtils.EMPTY;
            }
            // Fetching Files of Decision
            JSONObject getAllFilesofDecisionRuleResponse = DBPServices.getAllFilesforDecisionRule(decisionId,
                    decisionName, requestInstance);

            if (getAllFilesofDecisionRuleResponse == null
                    || !getAllFilesofDecisionRuleResponse.has(FabricConstants.OPSTATUS)
                    || getAllFilesofDecisionRuleResponse.getInt(FabricConstants.OPSTATUS) != 0) {
                LOG.debug("Failed to fetch Files of a decision:" + decisionName);
                ErrorCodeEnum.ERR_21577.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            JSONArray readResponseJSONArray = getAllFilesofDecisionRuleResponse.optJSONArray("rulesFileList");
            if (readResponseJSONArray == null) {
                LOG.debug("There are no files");
                ErrorCodeEnum.ERR_21582.setErrorCode(result);
                return result;
            }

            Dataset dataSet = new Dataset();
            dataSet.setId("rulesFileList");
            result.addDataset(dataSet);
            for (int indexVar = 0; indexVar < readResponseJSONArray.length(); indexVar++) {
                JSONObject currJSONObject = readResponseJSONArray.getJSONObject(indexVar);
                Record currRecord = new Record();
                if (currJSONObject.length() != 0) {
                    for (String currKey : currJSONObject.keySet()) {
                        if (currJSONObject.has(currKey)) {
                            currRecord.addParam(
                                    new Param(currKey, currJSONObject.getString(currKey), FabricConstants.STRING));
                        }
                    }
                    dataSet.addRecord(currRecord);
                }
            }
            return result;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

}
