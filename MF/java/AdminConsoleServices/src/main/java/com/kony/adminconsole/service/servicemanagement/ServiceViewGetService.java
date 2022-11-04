package com.kony.adminconsole.service.servicemanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.service.limitsandfees.periodiclimitservice.PeriodLimitServiceView;
import com.kony.adminconsole.service.limitsandfees.transactionfeesservice.TransferFeeServiceView;
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

public class ServiceViewGetService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(ServiceViewGetService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result processedResult = new Result();
        try {
            requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            Map<String, String> postParametersMap = new HashMap<String, String>();
            postParametersMap.put(ODataQueryConstants.ORDER_BY, "Name");
            String readServicesResponse = Executor.invokeService(ServiceURLEnum.SERVICE_VIEW_READ, postParametersMap,
                    null, requestInstance);
            JSONObject readServicesResponseJSON = CommonUtilities.getStringAsJSONObject(readServicesResponse);

            if (readServicesResponseJSON != null && readServicesResponseJSON.has(FabricConstants.OPSTATUS)
                    && readServicesResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                JSONArray readServicesResponseJSONArray = readServicesResponseJSON.getJSONArray("service_view");
                Dataset ServicesDataSet = new Dataset();
                ServicesDataSet.setId("Services");
                for (int indexVar = 0; indexVar < readServicesResponseJSONArray.length(); indexVar++) {
                    JSONObject currServiceJSONObject = readServicesResponseJSONArray.getJSONObject(indexVar);
                    Record currRecord = new Record();
                    if (currServiceJSONObject.length() != 0) {
                        for (String currKey : currServiceJSONObject.keySet()) {
                            if (currServiceJSONObject.has(currKey)) {
                                currRecord.addParam(new Param(currKey, currServiceJSONObject.getString(currKey),
                                        FabricConstants.STRING));
                            } else {
                                currRecord.addParam(new Param(currKey, "N/A", FabricConstants.STRING));
                            }

                        }
                        requestInstance.addRequestParam_("serviceId", currServiceJSONObject.getString("id"));
                        TransferFeeServiceView transferFeeServiceView = new TransferFeeServiceView();
                        Object currServiceJSONObject1 = transferFeeServiceView.invoke(methodID, inputArray,
                                requestInstance, responseInstance);
                        Result currserviceResult = (Result) currServiceJSONObject1;
                        Dataset TansactionFeesDataSet = new Dataset();
                        TansactionFeesDataSet.setId("TransactionFees");
                        for (int index = 0; index < currserviceResult.getAllDatasets().get(0).getAllRecords()
                                .size(); index++) {
                            Record currTransferRecord = new Record();
                            Param Fee_Param = new Param("Fees", currserviceResult.getAllDatasets().get(0)
                                    .getRecord(index).getParam("Fees").getValue(), FabricConstants.STRING);
                            currTransferRecord.addParam(Fee_Param);
                            Param MinimumTransactionValue_Param = new Param(
                                    "MinimumTransactionValue", currserviceResult.getAllDatasets().get(0)
                                            .getRecord(index).getParam("MinimumTransactionValue").getValue(),
                                    FabricConstants.STRING);
                            currTransferRecord.addParam(MinimumTransactionValue_Param);
                            Param MaximumTransactionValue_Param = new Param(
                                    "MaximumTransactionValue", currserviceResult.getAllDatasets().get(0)
                                            .getRecord(index).getParam("MaximumTransactionValue").getValue(),
                                    FabricConstants.STRING);
                            currTransferRecord.addParam(MaximumTransactionValue_Param);

                            TansactionFeesDataSet.addRecord(currTransferRecord);
                        }
                        currRecord.addDataset(TansactionFeesDataSet);

                        PeriodLimitServiceView periodicLimitServiceView = new PeriodLimitServiceView();
                        Object periodicLimitServiceViewObject = periodicLimitServiceView.invoke(methodID, inputArray,
                                requestInstance, responseInstance);
                        Result periodicLimitServiceViewObjectResult = (Result) periodicLimitServiceViewObject;
                        Dataset PeriodicLimitsDataSet = new Dataset();
                        PeriodicLimitsDataSet.setId("PeriodicLimits");
                        for (int index = 0; index < periodicLimitServiceViewObjectResult.getAllDatasets().get(0)
                                .getAllRecords().size(); index++) {
                            Record currPeriodicRecord = new Record();
                            Param Fee_Param = new Param(
                                    "MaximumLimit", periodicLimitServiceViewObjectResult.getAllDatasets().get(0)
                                            .getRecord(index).getParam("MaximumLimit").getValue(),
                                    FabricConstants.STRING);
                            currPeriodicRecord.addParam(Fee_Param);
                            Param MinimumTransactionValue_Param = new Param(
                                    "Period_Name", periodicLimitServiceViewObjectResult.getAllDatasets().get(0)
                                            .getRecord(index).getParam("Period_Name").getValue(),
                                    FabricConstants.STRING);
                            currPeriodicRecord.addParam(MinimumTransactionValue_Param);

                            PeriodicLimitsDataSet.addRecord(currPeriodicRecord);
                        }
                        currRecord.addDataset(PeriodicLimitsDataSet);
                    }
                    ServicesDataSet.addRecord(currRecord);
                }
                processedResult.addDataset(ServicesDataSet);
                return processedResult;
            }
            ErrorCodeEnum.ERR_20384.setErrorCode(processedResult);
            return processedResult;
        } catch (Exception e) {
            LOG.error("Unexepected Error in get Company Customers", e);
            processedResult.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_20001.setErrorCode(processedResult);
            return processedResult;
        }
    }
}