package com.kony.adminconsole.service.alertmanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
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

public class AlertsGetService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(AlertsGetService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        try {
            Map<String, String> postParametersMap = new HashMap<String, String>();

            Result alertsCollectionResult = new Result();

            Dataset responseDataset = new Dataset();
            responseDataset.setId("response");

            String readVariableReferenceResponse = Executor.invokeService(ServiceURLEnum.VARIABLEREFERENCE_READ,
                    postParametersMap, null, requestInstance);

            // Variable reference
            Dataset vrDataset = new Dataset();
            vrDataset.setId("VariableReference");

            JSONObject readVariableReferenceResponseJSON = CommonUtilities
                    .getStringAsJSONObject(readVariableReferenceResponse);
            JSONArray listOfVariableReferences = null;
            if (readVariableReferenceResponseJSON != null
                    && readVariableReferenceResponseJSON.has(FabricConstants.OPSTATUS)
                    && readVariableReferenceResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readVariableReferenceResponseJSON.has("variablereference")) {
                listOfVariableReferences = readVariableReferenceResponseJSON.getJSONArray("variablereference");

                for (int vrIndexVar = 0; vrIndexVar < listOfVariableReferences.length(); ++vrIndexVar) {

                    JSONObject currVrJSONObject = listOfVariableReferences.getJSONObject(vrIndexVar);
                    Record currVrRecord = new Record();

                    for (String currKey : currVrJSONObject.keySet()) {
                        String currVal = currVrJSONObject.getString(currKey);
                        Param currValParam = new Param(currKey, currVal, FabricConstants.STRING);
                        currVrRecord.addParam(currValParam);
                    }
                    vrDataset.addRecord(currVrRecord);
                }
            } else {
                ErrorCodeEnum.ERR_20909.setErrorCode(alertsCollectionResult);
                return alertsCollectionResult;
            }
            postParametersMap.clear();

            // Alerts
            String readAlertResponse = Executor.invokeService(ServiceURLEnum.ALERT_READ, postParametersMap, null,
                    requestInstance);
            JSONObject readAlertResponseJSON = CommonUtilities.getStringAsJSONObject(readAlertResponse);
            JSONArray listOfAlerts = null;
            if (readAlertResponseJSON != null && readAlertResponseJSON.has(FabricConstants.OPSTATUS)
                    && readAlertResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readAlertResponseJSON.has("alert")) {
                listOfAlerts = readAlertResponseJSON.getJSONArray("alert");
            } else {
                ErrorCodeEnum.ERR_20910.setErrorCode(alertsCollectionResult);
                return alertsCollectionResult;
            }
            postParametersMap.clear();

            // Alert type
            String readAlertTypeResponse = Executor.invokeService(ServiceURLEnum.ALERTTYPE_READ, postParametersMap,
                    null, requestInstance);
            JSONObject readAlertTypeResponseJSON = CommonUtilities.getStringAsJSONObject(readAlertTypeResponse);

            if (readAlertTypeResponseJSON != null && readAlertTypeResponseJSON.has(FabricConstants.OPSTATUS)
                    && readAlertTypeResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readAlertTypeResponseJSON.has("alerttype")) {

                JSONArray listOfAlertTypes = readAlertTypeResponseJSON.getJSONArray("alerttype");
                for (int indexVar = 0; indexVar < listOfAlertTypes.length(); indexVar++) {

                    JSONObject currRecordJSONObject = listOfAlertTypes.getJSONObject(indexVar);
                    Record currAlertTypeRecord = new Record();

                    if (currRecordJSONObject.has("id")) {
                        currAlertTypeRecord.setId(currRecordJSONObject.getString("id"));
                    }
                    for (String currKey : currRecordJSONObject.keySet()) {
                        String currVal = currRecordJSONObject.getString(currKey);
                        Param currValParam = new Param(currKey, currVal, FabricConstants.STRING);
                        currAlertTypeRecord.addParam(currValParam);
                    }

                    Dataset alertsDataset = new Dataset();
                    alertsDataset.setId("Alerts");

                    int alertsIndexVar = 0;
                    while (alertsIndexVar < listOfAlerts.length()) {
                        JSONObject currAlertJSONObject = listOfAlerts.getJSONObject(alertsIndexVar);

                        if (currAlertJSONObject.has("id") && currAlertJSONObject.has("AlertType_id")
                                && currRecordJSONObject.get("id").equals(currAlertJSONObject.get("AlertType_id"))) {

                            Record currAlertRecord = new Record();
                            for (String currKey : currAlertJSONObject.keySet()) {
                                String currVal = currAlertJSONObject.getString(currKey);
                                Param currValParam = new Param(currKey, currVal, FabricConstants.STRING);
                                currAlertRecord.addParam(currValParam);
                            }

                            alertsDataset.addRecord(currAlertRecord);

                            listOfAlerts.remove(alertsIndexVar);
                        } else {
                            ++alertsIndexVar;
                        }
                    }

                    currAlertTypeRecord.addDataset(alertsDataset);
                    responseDataset.addRecord(currAlertTypeRecord);
                }

                alertsCollectionResult.addDataset(responseDataset);
                alertsCollectionResult.addDataset(vrDataset);
                return alertsCollectionResult;
            } else {
                ErrorCodeEnum.ERR_20911.setErrorCode(alertsCollectionResult);
                return alertsCollectionResult;
            }
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

}