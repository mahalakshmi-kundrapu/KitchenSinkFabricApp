package com.kony.adminconsole.service.auditlogs;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
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

public class FilterManageService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(FilterManageService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {

            Result processedResult = new Result();
            Param operationStatusParam = new Param("operationStatusParam", "", FabricConstants.STRING);
            Param operationStatusCodeParam = new Param("operationStatusCodeParam", "", FabricConstants.STRING);

            processedResult.addParam(operationStatusParam);
            processedResult.addParam(operationStatusCodeParam);

            if (("createFilter").equalsIgnoreCase(methodID)) {
                Result createResult = createFilter(requestInstance);
                if (createResult != null) {
                    operationStatusParam.setValue(createResult.getParamByName("operationStatusParam").getValue());
                    operationStatusCodeParam
                            .setValue(createResult.getParamByName("operationStatusCodeParam").getValue());
                    processedResult.addRecord(createResult.getRecordById("operationRecord"));
                }
            } else if (("getFilters").equalsIgnoreCase(methodID)) {
                processedResult = getFilters(requestInstance);
            } else if (("deleteFilter").equalsIgnoreCase(methodID)) {
                Result deleteResult = deleteFilter(requestInstance);
                if (deleteResult != null) {
                    operationStatusParam.setValue(deleteResult.getParamByName("operationStatusParam").getValue());
                    operationStatusCodeParam
                            .setValue(deleteResult.getParamByName("operationStatusCodeParam").getValue());
                    processedResult.addRecord(deleteResult.getRecordById("operationRecord"));
                }
            }
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    private Result getFilters(DataControllerRequest requestInstance) throws ApplicationException {
        Result result = new Result();
        Map<String, String> postParametersMap = new HashMap<String, String>();
        requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
        UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
        String userId = userDetailsBeanInstance.getUserId();
        postParametersMap.put(ODataQueryConstants.FILTER, "User_id eq '" + userId + "'");
        String getViewDetailsResponse = Executor.invokeService(ServiceURLEnum.LOGVIEW_READ, postParametersMap, null,
                requestInstance);
        JSONObject getViewDetailsResponseJSON = CommonUtilities.getStringAsJSONObject(getViewDetailsResponse);
        int transactionalCount = 0;
        int adminconsoleCount = 0;
        int userActivityCount = 0;
        Dataset FiltersDataSet = new Dataset();
        FiltersDataSet.setId("FilterData");
        if (getViewDetailsResponseJSON != null && getViewDetailsResponseJSON.has(FabricConstants.OPSTATUS)
                && getViewDetailsResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            JSONArray listOfFilters = getViewDetailsResponseJSON.getJSONArray("logview");
            JSONObject currRecordJSONObject;
            for (int indexVar = 0; indexVar < listOfFilters.length(); indexVar++) {
                currRecordJSONObject = listOfFilters.getJSONObject(indexVar);
                Record currRecord = new Record();
                if (currRecordJSONObject.has("id")) {
                    Param id_Param = new Param("id", currRecordJSONObject.getString("id"));
                    currRecord.addParam(id_Param);
                }
                if (currRecordJSONObject.has("ViewName")) {
                    Param name_Param = new Param("Name", currRecordJSONObject.getString("ViewName"));
                    currRecord.addParam(name_Param);
                }
                if (currRecordJSONObject.has("Description")) {
                    Param desc_Param = new Param("Description", currRecordJSONObject.getString("Description"));
                    currRecord.addParam(desc_Param);
                } else {
                    Param desc_Param = new Param("Description", "");
                    currRecord.addParam(desc_Param);
                }
                if (currRecordJSONObject.has("LogType")) {
                    if (currRecordJSONObject.getString("LogType").equalsIgnoreCase("Transactional")) {
                        transactionalCount++;
                    } else if (currRecordJSONObject.getString("LogType").equalsIgnoreCase("Admin Console")) {
                        adminconsoleCount++;
                    } else if (currRecordJSONObject.getString("LogType").equalsIgnoreCase("Customer Activity")) {
                        userActivityCount++;
                    }
                    Param type_Param = new Param("LogType", currRecordJSONObject.getString("LogType"));
                    currRecord.addParam(type_Param);
                }
                if (currRecordJSONObject.has("createdts")) {
                    Param createdOn_Param = new Param("createdOn", currRecordJSONObject.getString("createdts"));
                    currRecord.addParam(createdOn_Param);
                }
                if (currRecordJSONObject.has("viewData")) {
                    Param ViewData_Param = new Param("ViewData", currRecordJSONObject.getString("viewData"));
                    currRecord.addParam(ViewData_Param);
                }
                FiltersDataSet.addRecord(currRecord);
            }
        }
        Param transactionalCount_Param = new Param("TransactionalCount", "" + transactionalCount);
        Param adminlogsCount_Param = new Param("AdmincConsoleCount", "" + adminconsoleCount);
        Param useractivitylogsCount_Param = new Param("CustomerActivityCount", "" + userActivityCount);
        result.addParam(transactionalCount_Param);
        result.addParam(adminlogsCount_Param);
        result.addParam(useractivitylogsCount_Param);
        result.addDataset(FiltersDataSet);
        return result;
    }

    private Result createFilter(DataControllerRequest requestInstance) throws ApplicationException {
        Result result = new Result();
        Map<String, String> postParametersMap = new HashMap<String, String>();
        requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
        UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
        String userId = userDetailsBeanInstance.getUserId();
        JSONObject filterDataJSON = CommonUtilities.getStringAsJSONObject(requestInstance.getParameter("FilterData"));
        String filterID = CommonUtilities.getNewId().toString();
        boolean isValidData = true;
        Record operationRecord = new Record();
        operationRecord.setId("operationRecord");
        Param operationStatusParam = new Param("operationStatusParam", "", FabricConstants.STRING);
        Param operationStatusCodeParam = new Param("operationStatusCodeParam", "", FabricConstants.STRING);

        StringBuffer errorMessageBuffer = new StringBuffer();
        errorMessageBuffer.append("ERROR:\n");

        postParametersMap.put("id", filterID);
        postParametersMap.put("createdby", userId);
        postParametersMap.put("User_id", userId);
        if (filterDataJSON.has("LogType"))
            postParametersMap.put("LogType", filterDataJSON.getString("LogType"));
        else {
            isValidData = false;
            errorMessageBuffer.append("\nType is a mandatory input for an create Request.");
        }
        if (filterDataJSON.has("ViewName"))
            postParametersMap.put("ViewName", filterDataJSON.getString("ViewName"));
        else {
            isValidData = false;
            errorMessageBuffer.append("\nViewName is a mandatory input for an create Request");
        }
        if (filterDataJSON.has("ViewData"))
            postParametersMap.put("viewData", filterDataJSON.get("ViewData").toString());
        else {
            isValidData = false;
            errorMessageBuffer.append("\nviewDataViewData is a mandatory input for an create Request");
        }
        if (filterDataJSON.has("Description"))
            postParametersMap.put("Description", filterDataJSON.getString("Description"));
        else
            postParametersMap.put("Description", "");
        if (isValidData) {
            String createFilterResponse = Executor.invokeService(ServiceURLEnum.LOGVIEW_CREATE, postParametersMap, null,
                    requestInstance);
            JSONObject createFilterResponseJSON = CommonUtilities.getStringAsJSONObject(createFilterResponse);
            if (createFilterResponseJSON != null && createFilterResponseJSON.has(FabricConstants.OPSTATUS)
                    && createFilterResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                operationStatusParam.setValue("Success");
                operationStatusCodeParam.setValue("0");
            } else {
                operationStatusParam.setValue("Failure");
                operationStatusCodeParam.setValue("-99");
            }
            operationRecord
                    .addParam(new Param("CreateAlert_" + filterID, createFilterResponse, FabricConstants.STRING));

        } else {
            operationRecord.addParam(new Param("", errorMessageBuffer.toString(), FabricConstants.STRING));
            operationStatusParam.setValue("Data Invalid");
            operationStatusCodeParam.setValue("-80");
        }
        result.addRecord(operationRecord);
        result.addParam(operationStatusParam);
        result.addParam(operationStatusCodeParam);

        return result;
    }

    private Result deleteFilter(DataControllerRequest requestInstance) {
        Result result = new Result();
        Map<String, String> postParametersMap = new HashMap<String, String>();
        requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
        Record operationRecord = new Record();
        operationRecord.setId("operationRecord");
        Param operationStatusParam = new Param("operationStatusParam", "", FabricConstants.STRING);
        Param operationStatusCodeParam = new Param("operationStatusCodeParam", "", FabricConstants.STRING);
        StringBuffer errorMessageBuffer = new StringBuffer();
        errorMessageBuffer.append("ERROR:\n");
        String viewId = requestInstance.getParameter("view_ID");

        if (viewId.startsWith("\"")) {
            viewId = viewId.substring(1, viewId.length());
        }
        if (viewId.endsWith("\"")) {
            viewId = viewId.substring(0, viewId.length() - 1);
        }
        if (viewId != null) {
            postParametersMap.clear();
            postParametersMap.put("id", viewId.trim());
            String deleteFilterResponse = Executor.invokeService(ServiceURLEnum.LOGVIEW_DELETE, postParametersMap, null,
                    requestInstance);
            JSONObject deleteFilterResponseJSON = CommonUtilities.getStringAsJSONObject(deleteFilterResponse);
            if (deleteFilterResponseJSON != null && deleteFilterResponseJSON.has(FabricConstants.OPSTATUS)
                    && deleteFilterResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                operationStatusParam.setValue("Success");
                operationStatusCodeParam.setValue("0");
            } else {
                errorMessageBuffer.append("Unable to delete row");
                operationRecord.addParam(new Param("", errorMessageBuffer.toString(), FabricConstants.STRING));
                operationStatusParam.setValue("Failure");
                operationStatusCodeParam.setValue("-99");
            }
        } else {
            errorMessageBuffer.append("Id cannot be null");
            operationRecord.addParam(new Param("", errorMessageBuffer.toString(), FabricConstants.STRING));
            operationStatusParam.setValue("Failure");
            operationStatusCodeParam.setValue("-99");
        }
        result.addRecord(operationRecord);
        result.addParam(operationStatusParam);
        result.addParam(operationStatusCodeParam);

        return result;
    }

}
