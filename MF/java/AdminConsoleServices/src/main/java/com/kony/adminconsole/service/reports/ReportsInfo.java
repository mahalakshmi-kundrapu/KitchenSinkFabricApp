package com.kony.adminconsole.service.reports;

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
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * @author Mohit Khosla
 *
 */
public class ReportsInfo implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(ReportsInfo.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        try {
            Result processedResult = new Result();

            // Getting CSR names
            Map<String, String> systemuserTableMap = new HashMap<String, String>();
            systemuserTableMap.put(ODataQueryConstants.SELECT, "UserID, FirstName, LastName");

            String readSystemUserResponse = Executor.invokeService(ServiceURLEnum.SYSTEMUSER_VIEW_READ,
                    systemuserTableMap, null, requestInstance);
            JSONObject readSystemUserResponseJSON = CommonUtilities.getStringAsJSONObject(readSystemUserResponse);
            int opStatusCode = readSystemUserResponseJSON.getInt(FabricConstants.OPSTATUS);
            if (opStatusCode == 0) {
                JSONArray systemuserJSONArray = readSystemUserResponseJSON.getJSONArray("systemuser_view");
                Dataset systemuserDataSet = new Dataset();
                systemuserDataSet.setId("csrNames");

                for (int indexVar = 0; indexVar < systemuserJSONArray.length(); indexVar++) {
                    JSONObject currRecordJSONObject = systemuserJSONArray.getJSONObject(indexVar);
                    Record csrNamesRecord = new Record();
                    csrNamesRecord.addParam(
                            new Param("id", currRecordJSONObject.getString("UserID"), FabricConstants.STRING));
                    csrNamesRecord.addParam(new Param("name", currRecordJSONObject.getString("FirstName") + " "
                            + currRecordJSONObject.getString("LastName"), FabricConstants.STRING));
                    systemuserDataSet.addRecord(csrNamesRecord);
                }
                processedResult.addDataset(systemuserDataSet);
            } else {
                processedResult.addParam(new Param("errorInfo", "Failed to fetch CSR Names", FabricConstants.STRING));
                ErrorCodeEnum.ERR_20441.setErrorCode(processedResult);
                return processedResult;
            }

            // Getting categories
            Map<String, String> requestcategoryTableMap = new HashMap<String, String>();
            requestcategoryTableMap.put(ODataQueryConstants.SELECT, "id, Name");

            String readRequestCategoryResponse = Executor.invokeService(ServiceURLEnum.REQUESTCATEGORY_READ,
                    requestcategoryTableMap, null, requestInstance);
            JSONObject readRequestCategoryResponseJSON = CommonUtilities
                    .getStringAsJSONObject(readRequestCategoryResponse);
            opStatusCode = readRequestCategoryResponseJSON.getInt(FabricConstants.OPSTATUS);
            if (opStatusCode == 0) {
                JSONArray requestcategoryJSONArray = readRequestCategoryResponseJSON.getJSONArray("requestcategory");
                Dataset categoryDataSet = new Dataset();
                categoryDataSet.setId("category");

                for (int indexVar = 0; indexVar < requestcategoryJSONArray.length(); indexVar++) {
                    JSONObject currRecordJSONObject = requestcategoryJSONArray.getJSONObject(indexVar);
                    Record categoryRecord = new Record();
                    categoryRecord
                            .addParam(new Param("id", currRecordJSONObject.getString("id"), FabricConstants.STRING));
                    categoryRecord.addParam(
                            new Param("name", currRecordJSONObject.getString("Name"), FabricConstants.STRING));
                    categoryDataSet.addRecord(categoryRecord);
                }
                processedResult.addDataset(categoryDataSet);
            } else {
                processedResult
                        .addParam(new Param("errorInfo", "Failed to fetch Request Categories", FabricConstants.STRING));
                ErrorCodeEnum.ERR_20441.setErrorCode(processedResult);
                return processedResult;
            }

            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

}