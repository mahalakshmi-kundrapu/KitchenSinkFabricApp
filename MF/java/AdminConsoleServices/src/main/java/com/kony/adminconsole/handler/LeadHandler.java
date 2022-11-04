package com.kony.adminconsole.handler;

import java.util.HashMap;
import java.util.Map;

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
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.dataobject.Dataset;

/**
 * Handler to perform common operations on leads
 *
 * @author Aditya Mankal
 */
public class LeadHandler {

    private static final Logger LOG = Logger.getLogger(LeadHandler.class);

    /**
     * Method to get the count of leads per status as a Dataset
     * 
     * @param requestInstance
     * @return Summary Count Dataset
     * @throws ApplicationException
     */
    public static Dataset getLeadsStatusCountSummary(DataControllerRequest requestInstance)
            throws ApplicationException {

        // Execute Service
        String serviceResponse = Executor.invokeService(ServiceURLEnum.LEAD_STATUS_COUNT_VIEW_READ, new HashMap<>(),
                new HashMap<>(), requestInstance);
        JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                || !serviceResponseJSON.has("lead_status_count_view")) {
            LOG.error("Failed CRUD Operation");
            throw new ApplicationException(ErrorCodeEnum.ERR_21502);
        }
        LOG.debug("Successful CRUD Operation");

        // Construct result Dataset
        JSONArray resultsArray = serviceResponseJSON.optJSONArray("lead_status_count_view");
        Dataset summmaryDataset = CommonUtilities.constructDatasetFromJSONArray(resultsArray);
        summmaryDataset.setId("statusCount");
        return summmaryDataset;
    }

    /**
     * Method to fetch the count of leads assigned to a CSR
     * 
     * @param csrId
     * @param requestInstance
     * @return Count of Leads Assigned to a CSR
     * @throws ApplicationException
     */
    public static int getLeadsAssignedCount(String csrId, DataControllerRequest requestInstance)
            throws ApplicationException {

        // Prepare Input Map
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put(ODataQueryConstants.FILTER, "csrId eq '" + csrId + "'");

        // Execute Service
        String serviceResponse = Executor.invokeService(ServiceURLEnum.LEAD_CSR_COUNT_VIEW_READ, inputMap, null,
                requestInstance);
        JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                || !serviceResponseJSON.has("lead_csr_count_view")) {
            LOG.error("Failed CRUD Operation");
            throw new ApplicationException(ErrorCodeEnum.ERR_21503);
        }

        // Parse response and return count
        LOG.debug("Successful CRUD Operation");
        JSONArray resultsArray = serviceResponseJSON.optJSONArray("lead_csr_count_view");
        JSONObject currJSON = resultsArray.optJSONObject(0);
        if (currJSON != null) {
            return currJSON.optInt("count");
        }
        return 0;
    }

}
