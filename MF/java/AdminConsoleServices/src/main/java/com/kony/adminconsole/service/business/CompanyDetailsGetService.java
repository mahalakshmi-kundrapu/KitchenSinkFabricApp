package com.kony.adminconsole.service.business;

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

public class CompanyDetailsGetService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CompanyDetailsGetService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        long startTime = System.currentTimeMillis();
        long svcEndTime = 0;
        long svcStartTime = 0;
        try {
            String searchType = "Search";
            String Name = null;
            if (requestInstance.getParameter("Name") != null) {
                Name = requestInstance.getParameter("Name");
            }
            String Email = null;
            if (requestInstance.getParameter("Email") != null) {
                Email = requestInstance.getParameter("Email");
            }
            String id = null;
            if (requestInstance.getParameter("id") != null) {
                id = requestInstance.getParameter("id");
            }
            svcStartTime = System.currentTimeMillis();
            JSONObject getCompanySearchresponse = DBPServices.getCompanySearch(searchType, Email, Name, id,
                    requestInstance);
            svcEndTime = System.currentTimeMillis();

            if (getCompanySearchresponse == null || !getCompanySearchresponse.has(FabricConstants.OPSTATUS)
                    || getCompanySearchresponse.getInt(FabricConstants.OPSTATUS) != 0) {
                ErrorCodeEnum.ERR_21018.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            } else {
                result.addParam(new Param("status", "Success", FabricConstants.STRING));
                result.addParam(new Param("opstatus", getCompanySearchresponse.get("opstatus").toString(),
                        FabricConstants.STRING));
                // Creating Dataset and adding to result
                if (getCompanySearchresponse.has("OrganisationDetails")) {
                    JSONArray readResponseJSONArray = getCompanySearchresponse.getJSONArray("OrganisationDetails");
                    Dataset dataSet = new Dataset();
                    dataSet.setId("OrganisationDetails");
                    for (int indexVar = 0; indexVar < readResponseJSONArray.length(); indexVar++) {
                        JSONObject currJSONObject = readResponseJSONArray.getJSONObject(indexVar);
                        Record currRecord = new Record();
                        if (currJSONObject.length() != 0) {
                            for (String currKey : currJSONObject.keySet()) {
                                if (currJSONObject.has(currKey)) {
                                    currRecord.addParam(new Param(currKey, currJSONObject.getString(currKey),
                                            FabricConstants.STRING));
                                }
                            }
                            dataSet.addRecord(currRecord);
                        }
                    }
                    result.addDataset(dataSet);
                }
                Dataset dataSet1 = new Dataset();
                dataSet1.setId("OrganisationOwnerDetails");
                if (getCompanySearchresponse.has("OrganisationOwnerDetails")) {
                    JSONArray readownerResponseJSONArray = getCompanySearchresponse
                            .getJSONArray("OrganisationOwnerDetails");
                    for (int indexVar = 0; indexVar < readownerResponseJSONArray.length(); indexVar++) {
                        JSONObject currJSONObject = readownerResponseJSONArray.getJSONObject(indexVar);
                        Record currRecord = new Record();
                        if (currJSONObject.length() != 0) {
                            for (String currKey : currJSONObject.keySet()) {
                                if (currJSONObject.has(currKey)) {
                                    currRecord.addParam(new Param(currKey, currJSONObject.getString(currKey),
                                            FabricConstants.STRING));
                                }
                            }
                            dataSet1.addRecord(currRecord);
                        }
                    }
                }
                result.addDataset(dataSet1);
            }
        } catch (Exception e) {
            LOG.error("Unexepected Error in get Company search", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
        }
        long endTime = System.currentTimeMillis();
        LOG.error("MF Time company details send rsp:" + (endTime - startTime) + "service time"
                + (svcEndTime - svcStartTime));
        return result;

    }

}