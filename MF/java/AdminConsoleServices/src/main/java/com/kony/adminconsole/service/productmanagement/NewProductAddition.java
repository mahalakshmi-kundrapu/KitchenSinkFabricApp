package com.kony.adminconsole.service.productmanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class NewProductAddition implements JavaService2 {

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
        Param statusParam;
        String systemUser = "";
        String productCode = "";
        String productName = "";
        String productType = "";
        String productCharge = "";
        String productStatus = "";
        String productFeatures = "";
        String additionalInformation = "";

        productCode = requestInstance.getParameter("productCode");
        if (StringUtils.isBlank(productCode)) {
            return ErrorCodeEnum.ERR_20606.setErrorCode(result);
        }

        productName = requestInstance.getParameter("productName");
        if (StringUtils.isBlank(productName)) {
            return ErrorCodeEnum.ERR_20620.setErrorCode(result);
        }

        if (productName.length() > 200) {
            return ErrorCodeEnum.ERR_20621.setErrorCode(result);
        }

        productType = requestInstance.getParameter("productType");
        if (StringUtils.isBlank(productType)) {
            return ErrorCodeEnum.ERR_20622.setErrorCode(result);
        }

        productFeatures = requestInstance.getParameter("productFeatures");
        if (StringUtils.isNotBlank(productFeatures)) {
            if (productFeatures.trim().length() > 1500) {
                return ErrorCodeEnum.ERR_20623.setErrorCode(result);
            }
        }

        productCharge = requestInstance.getParameter("productCharge");
        if (StringUtils.isNotBlank(productCharge)) {
            if (productCharge.trim().length() > 1500) {
                return ErrorCodeEnum.ERR_20624.setErrorCode(result);
            }
        }

        additionalInformation = requestInstance.getParameter("additionalInformation");
        if (StringUtils.isNotBlank(additionalInformation.trim())) {
            if (additionalInformation.trim().length() > 1000) {
                return ErrorCodeEnum.ERR_20625.setErrorCode(result);
            }
        }

        productStatus = requestInstance.getParameter("productStatus");
        if (StringUtils.isBlank(productStatus)) {
            return ErrorCodeEnum.ERR_20625.setErrorCode(result);
        }

        if (requestInstance.getParameter("systemUser") != null) {
            systemUser = requestInstance.getParameter("systemUser");
        }

        try {

            String productId = CommonUtilities.getNewId().toString();
            JSONObject createResponseJSON = createTransactionFee(productId, productCode, productName, productType,
                    productFeatures, productCharge, additionalInformation, productStatus, systemUser, authToken,
                    requestInstance);
            if (createResponseJSON != null && createResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                // create call back to delete inserted record in role table
                return ErrorCodeEnum.ERR_20801.setErrorCode(result);
            }
        } catch (Exception e) {
            statusParam = new Param("Status", "Error", FabricConstants.STRING);
            result.addParam(statusParam);
        }
        return result;
    }

    private JSONObject createTransactionFee(String productId, String productCode, String productName,
            String productType, String productFeatures, String productCharge, String additionalInformation,
            String productStatus, String systemUser, String authToken, DataControllerRequest requestInstance) {
        JSONObject createResponseJSON = null;
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.clear();
        postParametersMap.put("id", productId);
        postParametersMap.put("Type_id", productType);
        postParametersMap.put("ProductCode", productCode);
        postParametersMap.put("Name", productName);
        postParametersMap.put("Status_id", productStatus);
        postParametersMap.put("ProductFeatures", productFeatures);
        postParametersMap.put("ProductCharge", productCharge);
        postParametersMap.put("AdditionalInformation", additionalInformation);
        postParametersMap.put("createdby", systemUser);
        postParametersMap.put("modifiedby", "NULL");
        postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
        postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
        postParametersMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());
        postParametersMap.put("softdeleteflag", "0");
        String createRoleResponse = Executor.invokeService(ServiceURLEnum.PRODUCT_CREATE, postParametersMap, null,
                requestInstance);
        createResponseJSON = CommonUtilities.getStringAsJSONObject(createRoleResponse);
        int getOpStatusCode = createResponseJSON.getInt(FabricConstants.OPSTATUS);
        if (getOpStatusCode != 0) {
            // rollback logic
            return createResponseJSON;
        }
        return createResponseJSON;
    }

}
