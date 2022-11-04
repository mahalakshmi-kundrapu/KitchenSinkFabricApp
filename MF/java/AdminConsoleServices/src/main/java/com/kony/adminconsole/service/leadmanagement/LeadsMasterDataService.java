package com.kony.adminconsole.service.leadmanagement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.handler.LeadHandler;
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
 * Service to manage master data releated to Lead Management
 *
 * @author Aditya Mankal
 */
public class LeadsMasterDataService implements JavaService2 {

    private static final String GET_PRODUCTS_METHOD_ID = "getProducts";
    private static final String SET_SUPPORTED_PRODUCTS_METHOD_ID = "setSupportedProducts";
    private static final String GET_STATUS_COUNT_SUMMARY_METHOD_ID = "getStatusCountSummary";

    private static final Logger LOG = Logger.getLogger(LeadsMasterDataService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) {

        try {
            LOG.debug("Method Id: " + methodID);

            // Fetch Logged In User Info
            String loggedInUser = StringUtils.EMPTY;
            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            if (userDetailsBeanInstance != null) {
                loggedInUser = userDetailsBeanInstance.getUserId();
            }

            // Fetch supported products
            if (StringUtils.equals(methodID, GET_PRODUCTS_METHOD_ID)) {
                return getProducts(requestInstance);
            }

            // Get status count summary
            if (StringUtils.equals(methodID, GET_STATUS_COUNT_SUMMARY_METHOD_ID)) {
                return getStatusCountSummary(loggedInUser, requestInstance);
            }

            // Set supported products
            if (StringUtils.equals(methodID, SET_SUPPORTED_PRODUCTS_METHOD_ID)) {
                String supportedProducts = StringUtils.trim(requestInstance.getParameter("supportedProducts"));
                String unsupportedProducts = StringUtils.trim(requestInstance.getParameter("unSupportedProducts"));
                List<String> supportedProductsList = CommonUtilities.getStringifiedArrayAsList(supportedProducts);
                List<String> unSupportedProductsList = CommonUtilities.getStringifiedArrayAsList(unsupportedProducts);
                return setSupportedProducts(supportedProductsList, unSupportedProductsList, loggedInUser,
                        requestInstance);
            }

            return new Result();
        } catch (ApplicationException e) {
            Result errorResult = new Result();
            LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
            e.getErrorCodeEnum().setErrorCode(errorResult);
            return errorResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.error("Exception in Fetching Leads Master Data. Exception:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }

    }

    /**
     * Method to return the summary count of leads
     * 
     * @param loggedInUser
     * @param requestInstance
     * @return operation Result
     * @throws ApplicationException
     */
    private Result getStatusCountSummary(String loggedInUser, DataControllerRequest requestInstance)
            throws ApplicationException {

        Result operationResult = new Result();

        // Add Status Summary Information to Result
        Dataset leadsStatusCountSummary = LeadHandler.getLeadsStatusCountSummary(requestInstance);
        operationResult.addDataset(leadsStatusCountSummary);

        // Add count of leads assigned to logged-in user
        int assignedCount = LeadHandler.getLeadsAssignedCount(loggedInUser, requestInstance);
        operationResult.addParam(new Param("CSR_COUNT", String.valueOf(assignedCount), FabricConstants.INT));

        return operationResult;

    }

    /**
     * Method to get the products for which leads can be created
     * 
     * @param requestInstance
     * @return Supported Products
     * @throws ApplicationException
     */
    private Result getProducts(DataControllerRequest requestInstance) throws ApplicationException {

        Result result = new Result();

        // Prepare Input Map
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put(ODataQueryConstants.SELECT,
                "Type_id,productType,productId,productName,Status_id,isLeadSupported,otherproducttype_Name,otherproducttype_Description,otherproducttype_id");

        // Fetch Service Response
        String serviceResponse = Executor.invokeService(ServiceURLEnum.PRODUCTDETAIL_VIEW_READ, inputMap, null,
                requestInstance);
        JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                || !serviceResponseJSON.has("productdetail_view")) {
            LOG.error("Failed CRUD Operation");
            throw new ApplicationException(ErrorCodeEnum.ERR_21504);
        }
        LOG.debug("Successful CRUD Operation");
        JSONArray productsInfoArray = serviceResponseJSON.optJSONArray("productdetail_view");

        JSONObject currJSON = null;
        String currentProductTypeId;
        Map<String, Record> productTypeRecordsMap = new HashMap<>();

        // Parse response
        for (Object currObject : productsInfoArray) {
            if (currObject instanceof JSONObject) {
                currJSON = (JSONObject) currObject;

                currentProductTypeId = currJSON.optString("Type_id");

                Record currentProductTypeRecord = null;// Record of Product Type
                Dataset currentProductTypeProductsDataset = null;// Dataset of Products associated to the Product Type

                if (productTypeRecordsMap.containsKey(currentProductTypeId)) {
                    // Product Type that has been traversed
                    currentProductTypeRecord = productTypeRecordsMap.get(currentProductTypeId);
                    currentProductTypeProductsDataset = currentProductTypeRecord.getDatasetById("products");
                } else {
                    // Product Type that has not been traversed yet
                    currentProductTypeRecord = new Record();
                    currentProductTypeRecord.setId(currentProductTypeId);

                    currentProductTypeProductsDataset = new Dataset();
                    currentProductTypeProductsDataset.setId("products");
                    currentProductTypeRecord.addDataset(currentProductTypeProductsDataset);
                    productTypeRecordsMap.put(currentProductTypeId, currentProductTypeRecord);

                    // Add product type data
                    currentProductTypeRecord
                            .addParam(new Param("id", currJSON.optString("Type_id"), FabricConstants.STRING));
                    currentProductTypeRecord
                            .addParam(new Param("name", currJSON.optString("productType"), FabricConstants.STRING));
                }

                // Add the current product record to it's product type record
                Record currentProductRecord = new Record();
                currentProductRecord.addParam(new Param("id", currJSON.optString("productId"), FabricConstants.STRING));
                currentProductRecord
                        .addParam(new Param("name", currJSON.optString("productName"), FabricConstants.STRING));
                currentProductRecord
                        .addParam(new Param("statusId", currJSON.optString("Status_id"), FabricConstants.STRING));
                currentProductRecord.addParam(
                        new Param("isLeadSupported", currJSON.optString("isLeadSupported"), FabricConstants.STRING));
                currentProductRecord.addParam(new Param("otherProductTypeName",
                        currJSON.optString("otherproducttype_Name"), FabricConstants.STRING));
                currentProductRecord.addParam(new Param("otherProductTypeDescription",
                        currJSON.optString("otherproducttype_Description"), FabricConstants.STRING));
                currentProductRecord.addParam(new Param("otherProductTypeId", currJSON.optString("otherproducttype_id"),
                        FabricConstants.STRING));

                currentProductTypeProductsDataset.addRecord(currentProductRecord);

            }
        }
        Dataset resultDataset = new Dataset();
        resultDataset.addAllRecords(productTypeRecordsMap.values());
        resultDataset.setId("productTypes");

        // Add dataset to result
        result.addDataset(resultDataset);

        // Return result
        return result;
    }

    /**
     * Method to set the products supported for creating leads
     * 
     * @param supportedProductIds
     * @param unsupportedProductIds
     * @param loggedInUser
     * @param requestInstance
     * @return operation result
     * @throws ApplicationException
     */
    private Result setSupportedProducts(List<String> supportedProductIds, List<String> unsupportedProductIds,
            String loggedInUser, DataControllerRequest requestInstance) throws ApplicationException {

        Record record = new Record();
        record.setId("updatedSupportSettings");

        JSONObject serviceResponseJSON = null;
        String serviceResponse = StringUtils.EMPTY;
        Map<String, String> inputMap = new HashMap<>();

        // Remove unsupported products
        if (unsupportedProductIds != null && !unsupportedProductIds.isEmpty()) {
            inputMap.put("isLeadSupported", "0");
            for (String productId : unsupportedProductIds) {
                inputMap.put("id", productId);
                inputMap.put("updatedby", loggedInUser);
                inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
                serviceResponse = Executor.invokeService(ServiceURLEnum.PRODUCT_UPDATE, inputMap, null,
                        requestInstance);
                serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
                if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                        || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                    LOG.error("Failed CRUD Operation. Response:" + serviceResponse);
                    throw new ApplicationException(ErrorCodeEnum.ERR_21505);
                }
                record.addParam(new Param(productId, "0", FabricConstants.INT));
                LOG.debug("Successful CRUD Operation");
            }
        }
        inputMap.clear();

        // Set supported products
        if (supportedProductIds != null && !supportedProductIds.isEmpty()) {
            inputMap.put("isLeadSupported", "1");
            for (String productId : supportedProductIds) {
                inputMap.put("id", productId);
                inputMap.put("updatedby", loggedInUser);
                inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
                serviceResponse = Executor.invokeService(ServiceURLEnum.PRODUCT_UPDATE, inputMap, null,
                        requestInstance);
                serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
                if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                        || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                    LOG.error("Failed CRUD Operation. Response:" + serviceResponse);
                    throw new ApplicationException(ErrorCodeEnum.ERR_21505);
                }
                record.addParam(new Param(productId, "1", FabricConstants.INT));
                LOG.debug("Successful CRUD Operation");
            }
        }

        // Return result
        Result result = new Result();
        result.addRecord(record);
        return result;
    }

}
