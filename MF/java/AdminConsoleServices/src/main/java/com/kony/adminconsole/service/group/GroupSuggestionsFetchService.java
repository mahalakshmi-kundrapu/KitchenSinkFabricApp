package com.kony.adminconsole.service.group;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to fetch Group Suggestions
 *
 * @author Aditya Mankal
 * 
 */
public class GroupSuggestionsFetchService implements JavaService2 {

    private static final String GET_GROUP_SUGGESTIONS = "getGroupSuggestions";

    private static final int FETCH_RECORDS_LIMIT = 10;

    private static final Logger LOG = Logger.getLogger(GroupSuggestionsFetchService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        try {
            if (StringUtils.equalsIgnoreCase(methodID, GET_GROUP_SUGGESTIONS)) {
                String searchText = StringUtils.trim(requestInstance.getParameter("searchText"));
                return getGroupSuggestions(requestInstance, searchText);
            }
            return new Result();
        } catch (ApplicationException e) {
            Result errorResult = new Result();
            LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
            e.getErrorCodeEnum().setErrorCode(errorResult);
            return errorResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    /**
     * Method to get a list groups who's name starts with the search text
     * 
     * @param requestInstance
     * @param searchText
     * @return Result with two datasets i.e. Customers and Groups
     * @throws ApplicationException
     */
    private Result getGroupSuggestions(DataControllerRequest requestInstance, String searchText)
            throws ApplicationException {

        Result processedResult = new Result();

        if (StringUtils.isNotBlank(searchText)) {

            // Fetch Groups
            Map<String, String> queryMap = new HashMap<>();
            queryMap.clear();
            queryMap.put(ODataQueryConstants.ORDER_BY, "Name asc");
            queryMap.put(ODataQueryConstants.TOP, Integer.toString(FETCH_RECORDS_LIMIT));
            queryMap.put(ODataQueryConstants.FILTER, "startswith(Name,'" + searchText + "') eq true");
            String serviceResponse = Executor.invokeService(ServiceURLEnum.MEMBERGROUP_READ, queryMap, null,
                    requestInstance);
            JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
            if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                    || serviceResponseJSON.optInt(FabricConstants.OPSTATUS) != 0
                    || serviceResponseJSON.optJSONArray("membergroup") == null) {
                // Failed CRUD Operation
                LOG.error("Failed to read Member Groups data. Response:" + serviceResponse);
                throw new ApplicationException(ErrorCodeEnum.ERR_20404);
            }
            // Construct Groups Dataset
            JSONArray records = serviceResponseJSON.optJSONArray("membergroup");
            Dataset groupsDataset = CommonUtilities.constructDatasetFromJSONArray(records);
            groupsDataset.setId("groups");
            processedResult.addDataset(groupsDataset);

        }

        // Return result
        return processedResult;
    }

}
