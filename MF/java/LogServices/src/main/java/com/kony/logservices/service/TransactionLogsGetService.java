package com.kony.logservices.service;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.JSONUtils;
import com.kony.logservices.core.AbstractLogJavaService;
import com.kony.logservices.dao.LogDAO;
import com.kony.logservices.dto.PaginationDTO;
import com.kony.logservices.dto.TransactionActivityDTO;
import com.kony.logservices.util.ErrorCodeEnum;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

public class TransactionLogsGetService extends AbstractLogJavaService {

    private static final Logger LOG = Logger.getLogger(TransactionLogsGetService.class);

    @Override
    public Object execute(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result result = new Result();
        try {
            String searchText = null;
            String typeId = null;
            String startDate = null;
            String endDate = null;
            Integer startAmount = null;
            Integer endAmount = null;
            int pageNumber = 1;
            int noOfRecords = 10;
            String sortBy = null;
            String sortDirection = null;
            String fromMobileEmail = null;
            String toMobileEmail = null;
            JSONArray fromAccountType = null;
            JSONArray toAccountType = null;
            JSONArray statusType = null;
            JSONArray type = null;
            JSONArray currencyType = null;
            ArrayList<String> fromAccountTypeArray = null;
            ArrayList<String> toAccountTypeArray = null;
            ArrayList<String> statusTypeArray = null;
            ArrayList<String> typeArray = null;
            ArrayList<String> currencyTypeArray = null;
            LOG.error("FILTER DATA:" + requestInstance.getParameter("FilterData"));

            JSONObject filterDataJSON = CommonUtilities
                    .getStringAsJSONObject(requestInstance.getParameter("FilterData"));
            if (filterDataJSON.has("ServiceName")) {
                typeId = filterDataJSON.getString("ServiceName");
            }
            if (filterDataJSON.has("SearchText")) {
                searchText = filterDataJSON.getString("SearchText");
            }
            if (filterDataJSON.has("StartDate")) {
                startDate = filterDataJSON.getString("StartDate");
            }
            if (filterDataJSON.has("EndDate")) {
                endDate = filterDataJSON.getString("EndDate");
            }
            if (filterDataJSON.has("StartAmount")) {
                startAmount = Integer.parseInt(filterDataJSON.getString("StartAmount"));
            }
            if (filterDataJSON.has("EndAmount")) {
                endAmount = Integer.parseInt(filterDataJSON.getString("EndAmount"));
            }
            if (filterDataJSON.has("PageNumber")) {
                pageNumber = filterDataJSON.getInt(("PageNumber"));
            }
            if (filterDataJSON.has("NoOfRecords")) {
                noOfRecords = filterDataJSON.getInt(("NoOfRecords"));
            }
            if (filterDataJSON.has("SortBy")) {
                sortBy = filterDataJSON.getString("SortBy");
            }
            if (filterDataJSON.has("SortDirection")) {
                sortDirection = filterDataJSON.getString("SortDirection");
            }
            if (filterDataJSON.has("FromMobileEmail")) {
                fromMobileEmail = filterDataJSON.getString("FromMobileEmail");
            }
            if (filterDataJSON.has("ToMobileEmail")) {
                toMobileEmail = filterDataJSON.getString("ToMobileEmail");
            }
            if (filterDataJSON.has("FromAccountType")) {
                fromAccountType = filterDataJSON.getJSONArray("FromAccountType");
                fromAccountTypeArray = new ArrayList<String>(fromAccountType.length());
                for (int i = 0; i < fromAccountType.length(); i++) {
                    fromAccountTypeArray.add(fromAccountType.getString(i));
                }
            }
            if (filterDataJSON.has("ToAccountType")) {
                toAccountType = filterDataJSON.getJSONArray("ToAccountType");
                toAccountTypeArray = new ArrayList<String>(toAccountType.length());
                for (int i = 0; i < toAccountType.length(); i++) {
                    toAccountTypeArray.add(toAccountType.getString(i));
                }
            }
            if (filterDataJSON.has("Status")) {
                statusType = filterDataJSON.getJSONArray("Status");
                statusTypeArray = new ArrayList<String>(statusType.length());
                for (int i = 0; i < statusType.length(); i++) {
                    statusTypeArray.add(statusType.getString(i));
                }
            }
            if (filterDataJSON.has("Type")) {
                type = filterDataJSON.getJSONArray("Type");
                typeArray = new ArrayList<String>(type.length());
                for (int i = 0; i < type.length(); i++) {
                    typeArray.add(type.getString(i));
                }
            }
            if (filterDataJSON.has("Currency")) {
                currencyType = filterDataJSON.getJSONArray("Currency");
                currencyTypeArray = new ArrayList<String>(currencyType.length());
                for (int i = 0; i < currencyType.length(); i++) {
                    currencyTypeArray.add(currencyType.getString(i));
                }
            }
            PaginationDTO<TransactionActivityDTO> paginatedTransactions = LogDAO.getPaginatedTransactions(searchText,
                    typeId, startDate, endDate, startAmount, endAmount, pageNumber, noOfRecords, sortBy, sortDirection,
                    fromAccountTypeArray, toAccountTypeArray, statusTypeArray, typeArray, currencyTypeArray,
                    fromMobileEmail, toMobileEmail);
            String resultJSON = JSONUtils.stringify(paginatedTransactions);

            JSONObject resultJSONObject = new JSONObject(resultJSON);
            return CommonUtilities.constructResultFromJSONObject(resultJSONObject);
        } catch (Exception e) {
            LOG.error("Failed while executing get transaction logs", e);
            ErrorCodeEnum.ERR_29003.setErrorCode(result);
        }
        return result;
    }

}
