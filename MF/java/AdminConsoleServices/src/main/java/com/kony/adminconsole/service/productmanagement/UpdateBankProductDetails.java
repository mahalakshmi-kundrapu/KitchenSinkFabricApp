package com.kony.adminconsole.service.productmanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class UpdateBankProductDetails implements JavaService2 {

    private static final String MANAGE_STATUS_METHOD = "manageStatus";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result result = new Result();
        String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
        if (methodID.equalsIgnoreCase(MANAGE_STATUS_METHOD)) {
            String productId = "";
            String productStatus = "";
            String systemUser = "";
            if (requestInstance.getParameter("productId") != null) {
                productId = requestInstance.getParameter("productId");
                if (StringUtils.isBlank(productId)) {
                    return constructValidationFailResultObject("5055", "PRODUCT_ID_EMPTY");
                }
            } else {
                return constructValidationFailResultObject("5056", "MANDATORY_FIELD_PRODUCT_ID_MISSING");
            }
            if (requestInstance.getParameter("productStatus") != null) {
                productStatus = requestInstance.getParameter("productStatus");
                if (StringUtils.isBlank(productStatus)) {
                    return constructValidationFailResultObject("5053", "PRODUCT_STATUS_EMPTY");
                }
            } else {
                return constructValidationFailResultObject("5054", "PRODUCT_STATUS_MISSING");
            }
            if (requestInstance.getParameter("systemUser") != null) {
                systemUser = requestInstance.getParameter("systemUser");
            }
            manageStatus(productId, productStatus, systemUser, authToken, requestInstance);
        } else {
            Param statusParam;
            String productId = "";
            String productCode = "";
            String productName = "";
            String productType = "";
            String productFeatures = "";
            String productCharge = "";
            String additionalInformation = "";
            String productStatus = "";
            String systemUser = "";

            // mandatory
            if (requestInstance.getParameter("productId") != null) {
                productId = requestInstance.getParameter("productId");
                if (StringUtils.isBlank(productId)) {
                    return constructValidationFailResultObject("5055", "PRODUCT_ID_EMPTY");
                }
            } else {
                return constructValidationFailResultObject("5056", "MANDATORY_FIELD_PRODUCT_ID_MISSING");
            }

            // mandatory
            if (requestInstance.getParameter("productCode") != null) {
                productCode = requestInstance.getParameter("productCode");
                if (StringUtils.isBlank(productCode)) {
                    return constructValidationFailResultObject("5043", "PRODUCT_CODE_EMPTY");
                }
            } else {
                return constructValidationFailResultObject("5044", "MANDATORY_FIELD_PRODUCT_CODE_MISSING");
            }

            // mandatory
            if (requestInstance.getParameter("productName") != null) {
                productName = requestInstance.getParameter("productName");
                if (StringUtils.isBlank(productName)) {
                    return constructValidationFailResultObject("5045", "PRODUCT_NAME_EMPTY");
                }
                if (productName.length() > 200) {
                    return constructValidationFailResultObject("5049", "PRODUCT_NAME_SIZE_EXCEEDED");
                }
            } else {
                return constructValidationFailResultObject("5046", "MANDATORY_FIELD_PRODUCT_NAME_MISSING");
            }

            // mandatory
            if (requestInstance.getParameter("productType") != null) {
                productType = requestInstance.getParameter("productType");
                if (StringUtils.isBlank(productType)) {
                    return constructValidationFailResultObject("5047", "PRODUCT_TYPE_EMPTY");
                }
            } else {
                return constructValidationFailResultObject("5048", "MANDATORY_FIELD_PRODUCT_TYPE_MISSING");
            }

            if (requestInstance.getParameter("productFeatures") != null) {
                productFeatures = requestInstance.getParameter("productFeatures");
                if (StringUtils.isNotBlank(productFeatures)) {
                    if (productFeatures.trim().length() > 1500) {
                        return constructValidationFailResultObject("5050", "PRODUCT_FEATURES_SIZE_EXCEEDED");
                    }
                }
            }
            if (requestInstance.getParameter("productCharge") != null) {
                productCharge = requestInstance.getParameter("productCharge");
                if (StringUtils.isNotBlank(productCharge)) {
                    if (productCharge.trim().length() > 1500) {
                        return constructValidationFailResultObject("5051", "PRODUCT_CHARGES_SIZE_EXCEEDED");
                    }
                }
            }
            if (requestInstance.getParameter("additionalInformation") != null) {
                additionalInformation = requestInstance.getParameter("additionalInformation");
                if (StringUtils.isNotBlank(additionalInformation.trim())) {
                    if (additionalInformation.trim().length() > 1000) {
                        return constructValidationFailResultObject("5052", "ADDITIONAL_INFORMATION_SIZE_EXCEEDED");
                    }
                }
            }
            // mandatory
            if (requestInstance.getParameter("productStatus") != null) {
                productStatus = requestInstance.getParameter("productStatus");
                if (StringUtils.isBlank(productStatus)) {
                    return constructValidationFailResultObject("5053", "PRODUCT_STATUS_EMPTY");
                }
            } else {
                return constructValidationFailResultObject("5054", "PRODUCT_STATUS_MISSING");
            }
            if (requestInstance.getParameter("systemUser") != null) {
                systemUser = requestInstance.getParameter("systemUser");
            }

            try {

                JSONObject updateProductResponseJSON = updateProductDetails(productId, productCode, productName,
                        productType, productFeatures, productCharge, additionalInformation, productStatus, authToken,
                        systemUser, requestInstance);
                if (updateProductResponseJSON != null
                        && updateProductResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                    // create call back to delete inserted record in role table
                    return constructFailureResult(updateProductResponseJSON);
                }
            } catch (Exception e) {
                statusParam = new Param("Status", "EXCEPTION", FabricConstants.STRING);
                result.addParam(statusParam);
                return result;
            }
            statusParam = new Param("Status", "SUCCESSFUL", FabricConstants.STRING);
            result.addParam(statusParam);
        }
        return result;
    }

    private JSONObject manageStatus(String productId, String productStatus, String systemUser, String authToken,
            DataControllerRequest requestInstance) {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.clear();
        postParametersMap.put("id", productId);
        postParametersMap.put("Status_id", productStatus);
        postParametersMap.put("modifiedby", systemUser);
        String updateTransactionFeesResponse = Executor.invokeService(ServiceURLEnum.PRODUCT_UPDATE, postParametersMap,
                null, requestInstance);

        JSONObject updateResponseJSON = CommonUtilities.getStringAsJSONObject(updateTransactionFeesResponse);
        int getOpStatusCode = updateResponseJSON.getInt(FabricConstants.OPSTATUS);
        if (getOpStatusCode != 0) {
            // rollback logic
            return updateResponseJSON;
        }
        return updateResponseJSON;
    }

    private JSONObject updateProductDetails(String productId, String productCode, String productName,
            String productType, String productFeatures, String productCharge, String additionalInformation,
            String productStatus, String authToken, String systemUser, DataControllerRequest requestInstance) {
        JSONObject updateResponseJSON = null;
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.clear();
        postParametersMap.put("id", productId);
        postParametersMap.put("Type_id", productType);
        postParametersMap.put("ProductCode", productCode);
        postParametersMap.put("Name", productName);
        postParametersMap.put("Status_id", productStatus);
        postParametersMap.put("ProductFeatures", productFeatures);
        postParametersMap.put("ProductCharges", productCharge);
        postParametersMap.put("AdditionalInformation", additionalInformation);
        postParametersMap.put("modifiedby", systemUser);
        postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
        postParametersMap.put("softdeleteflag", "0");

        String updateTransactionFeesResponse = Executor.invokeService(ServiceURLEnum.PRODUCT_UPDATE, postParametersMap,
                null, requestInstance);

        updateResponseJSON = CommonUtilities.getStringAsJSONObject(updateTransactionFeesResponse);
        int getOpStatusCode = updateResponseJSON.getInt(FabricConstants.OPSTATUS);
        if (getOpStatusCode != 0) {
            // rollback logic
            return updateResponseJSON;
        }
        return updateResponseJSON;
    }

    private Result constructValidationFailResultObject(String errorCode, String errorMessage) {
        Result processedResult = new Result();
        Param errorCodeParam;
        Param errorMessageParam;
        errorCodeParam = new Param("errorCode", errorCode, FabricConstants.STRING);
        errorMessageParam = new Param("errorMessage", errorMessage, FabricConstants.STRING);
        processedResult.addParam(errorCodeParam);
        processedResult.addParam(errorMessageParam);
        return processedResult;
    }

    public Result constructFailureResult(JSONObject ResponseJSON) {
        Result Result = new Result();
        Param errCodeParam = new Param("backend_error_code", "" + ResponseJSON.getInt(FabricConstants.OPSTATUS),
                FabricConstants.INT);
        Param errMsgParam = new Param("backend_error_message", ResponseJSON.toString(), FabricConstants.STRING);
        Param serviceMessageParam = new Param("errmsg", ResponseJSON.toString(), FabricConstants.STRING);
        Param statusParam = new Param("Status", "Failure", FabricConstants.STRING);
        Result.addParam(errCodeParam);
        Result.addParam(errMsgParam);
        Result.addParam(serviceMessageParam);
        Result.addParam(statusParam);
        return Result;
    }
}
