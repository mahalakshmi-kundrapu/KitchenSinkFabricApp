package com.kony.adminconsole.service.productmanagement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.handler.MultipartPayloadHandler;
import com.kony.adminconsole.commons.handler.MultipartPayloadHandler.FormItem;
import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class ImportProductFromCSV implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(ImportProductFromCSV.class);

    private JSONObject parseCSVLintoJSON(String keys, String values) {
        String cols[] = keys.split(",");
        String record[] = values.split(",");
        JSONObject recordObj = new JSONObject();
        for (int i = 0; i < cols.length; i++) {
            recordObj.put(cols[i], record[i]);
        }
        return recordObj;
    }

    private JSONObject validateandConsolidateFields(JSONObject thisObj,
            Map<String, Map<String, String>> staticDataMapper, JSONObject mandatoryFields) throws Exception {
        for (String fieldKey : mandatoryFields.keySet()) {
            JSONObject thisField = mandatoryFields.getJSONObject(fieldKey);
            if (thisField.getBoolean("mandatory")) {
                if (!(thisObj.getString(fieldKey) != null && thisObj.getString(fieldKey).length() > 0)) {
                    thisObj.put("validationStatus", "invalid");
                    thisObj.put("validationMessage", "Mandatory Field: " + fieldKey + " is missing");
                    return thisObj;
                }
            }
            if (thisField.getBoolean("hasCharacterLimit")) {
                if ((thisObj.getString(fieldKey) != null && thisObj.getString(fieldKey).length() > 0) == true) {
                    if (thisObj.getString(fieldKey).length() > Integer
                            .parseInt(thisField.get("characterLimit").toString())) {
                        thisObj.put("validationStatus", "invalid");
                        thisObj.put("validationMessage",
                                thisField.get("characterLimit") + " Character Limit Exceeded for " + fieldKey + "("
                                        + thisObj.getString(fieldKey).length() + ")");
                        return thisObj;
                    }
                }
            }
            if (!thisField.getString("type").equalsIgnoreCase("input")) {
                if (staticDataMapper.get(fieldKey) != null) {
                    if (thisField.keySet().toString().indexOf("multi") > 0 && thisField.getBoolean("multi")) {
                        String fieldKeyId = "";
                        for (String thisMultiKey : thisObj.getString(fieldKey).split("\\|")) {
                            if (thisMultiKey.length() > 0) {
                                String thisId = staticDataMapper.get(fieldKey).get(thisMultiKey.trim());
                                if (thisId != null && thisId.length() > 0) {
                                    fieldKeyId += thisId + ",";
                                } else {
                                    thisObj.put("validationStatus", "invalid");
                                    thisObj.put("validationMessage",
                                            "Invalid Value " + thisMultiKey + " provided for Field : " + fieldKey);
                                    return thisObj;
                                }
                            }
                        }
                        if (fieldKeyId.length() > 0)
                            thisObj.put(fieldKey, fieldKeyId.substring(0, fieldKeyId.length() - 1));
                    } else {
                        String fieldKeyId = staticDataMapper.get(fieldKey).get(thisObj.getString(fieldKey));
                        if (fieldKeyId != null && fieldKeyId.length() > 0) {
                            thisObj.put(fieldKey, fieldKeyId);
                        } else {
                            thisObj.put("validationStatus", "invalid");
                            thisObj.put("validationMessage", "Invalid Value " + thisObj.getString(fieldKey)
                                    + " provided for Field : " + fieldKey);
                            return thisObj;
                        }
                    }
                }
            }
            thisObj.put("validationStatus", "valid");
            thisObj.put("validationMessage", "Validation Successful");
        }
        return thisObj;
    }

    private Map<String, String> callForDataMap(ServiceURLEnum url, String colsMapper, DataControllerRequest request)
            throws Exception {
        if (colsMapper == null || colsMapper.split(",").length != 2) {
            if (colsMapper == null) {
                throw new InvalidParameterException("Two Columns are Expected to return a Map. Provided 0"
                        + " columns. FYI: Key as first Column and Value as Second Column.");
            }
            throw new InvalidParameterException("Two Columns are Expected to return a Map. Provided "
                    + colsMapper.length() + " columns. FYI: Key as first Column and Value as Second Column.");

        }
        Map<String, String> result = new HashMap<>();
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(ODataQueryConstants.SELECT, colsMapper);
        String retStr = Executor.invokeService(url, paramMap, null, request);
        JSONObject response = CommonUtilities.getStringAsJSONObject(retStr);
        if (response == null || !response.has(FabricConstants.OPSTATUS)
                || response.getInt(FabricConstants.OPSTATUS) != 0) {
            retStr = "";
        } else {
            for (String key : response.keySet()) {
                if (!(key.equalsIgnoreCase(FabricConstants.OPSTATUS) || key.equalsIgnoreCase("httpStatusCode"))) {
                    JSONArray temp = (JSONArray) response.get(key);
                    if (temp.length() > 0) {
                        for (int i = 0; i < temp.length(); i++) {
                            JSONObject tempObj = (JSONObject) (temp.get(i));
                            result.put(tempObj.getString(colsMapper.split(",")[0].trim()),
                                    tempObj.getString(colsMapper.split(",")[1].trim()));
                        }
                    }
                }
            }
        }

        return result;
    }

    private boolean insertIntoProduct(JSONObject obj, DataControllerRequest request, ServiceURLEnum product_create,
            JSONObject csvservice_colMap) {
        Map<String, String> paramMap = new HashMap<>();
        JSONObject productColumns = (JSONObject) csvservice_colMap.get("product");
        for (String key : productColumns.keySet()) {
            paramMap.put(productColumns.getString(key), obj.getString(key));
        }
        String serviceResponse = Executor.invokeService(product_create, paramMap, null, request);
        JSONObject response = CommonUtilities.getStringAsJSONObject(serviceResponse);
        if (response != null && response.has(FabricConstants.OPSTATUS)
                && response.getInt(FabricConstants.OPSTATUS) == 0) {
            return true;
        }
        return false;
    }

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest request,
            DataControllerResponse response) throws Exception {
        // authToken = request.getHeader("X-Kony-Authorization");
        Result processedResult = new Result();
        List<FormItem> formItems = null;
        try {
            formItems = MultipartPayloadHandler.handleMultipart(request);
        } catch (FileSizeLimitExceededException fslee) {
            ErrorCodeEnum.ERR_20563.setErrorCode(processedResult);
            return processedResult;
        }

        JSONObject csvservice_colMap, mandatoryFields;
        Map<String, Map<String, String>> staticDataMapper;

        String importProduct_mandatoryFields =
                "{productid:{mandatory:TRUE,type:input,source:na,hasCharacterLimit:FALSE},"
                        + "producttype:{mandatory:TRUE,type:dynamic,source:producttype.read,hasCharacterLimit:FALSE},"
                        + "productname:{mandatory:TRUE,type:input,source:na,hasCharacterLimit:TRUE,characterLimit:200},"
                        + "productstatus:{mandatory:TRUE,type:input,source:na,hasCharacterLimit:FALSE},"
                        + "productfeatures:{mandatory:FALSE,type:input,source:na,hasCharacterLimit:TRUE,characterLimit:1500},"
                        + "productcharges:{mandatory:FALSE,type:input,source:na,hasCharacterLimit:TRUE,characterLimit:1500},"
                        + "additionalinformation:{mandatory:TRUE,type:input,source:na,hasCharacterLimit:TRUE,characterLimit:1000},"
                        + "createdby:{mandatory:FALSE,type:input,source:na,hasCharacterLimit:FALSE}}}";
        String importProduct_CSVServiceColMap =
                "{product:{productid:id,producttype:Type_id,productcode:ProductCode,productname:Name,productstatus:Status_id,productfeatures:ProductFeatures,productcharges:ProductCharges,additionalinformation:AdditionalInformation,createdby:createdby}}";

        // CSV Validators Config
        csvservice_colMap = new JSONObject(importProduct_CSVServiceColMap);
        mandatoryFields = new JSONObject(importProduct_mandatoryFields);
        // Validators and Mappers Content from DB
        staticDataMapper = new HashMap<>();
        Map<String, String> statusMap = new HashMap<String, String>();
        try {
            LOG.info("Service called : ImportProductsCSV_Service : Start");
            JSONArray jArr = new JSONArray();
            File file = null;
            LOG.info("FormItems : " + formItems);
            if (formItems != null) {
                for (FormItem fItem : formItems) {
                    if (fItem.getParamName().indexOf("X-Kony-Authorization") != -1) {
                        statusMap = callForDataMap(ServiceURLEnum.STATUS_READ, "Description,id", request);
                        staticDataMapper.put("producttype",
                                callForDataMap(ServiceURLEnum.PRODUCTTYPE_READ, "Name,id", request));
                        staticDataMapper.put("productstatus", statusMap);
                    }
                    if (fItem.isFile()) {
                        file = fItem.getFile();
                        if (file.exists() && file.isFile() && file.length() > 0) {

                            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                                String keys = br.readLine();
                                while (true) {
                                    String thisLine = br.readLine();
                                    if (thisLine == null) {
                                        break;
                                    }
                                    JSONObject obj = parseCSVLintoJSON(keys, thisLine);
                                    JSONObject rawObj = new JSONObject(obj.toString());
                                    try {
                                        obj = new JSONObject(
                                                validateandConsolidateFields(obj, staticDataMapper, mandatoryFields)
                                                        .toString());

                                        if (obj.getString("validationStatus").equalsIgnoreCase("valid")) {
                                            if (insertIntoProduct(obj, request, ServiceURLEnum.PRODUCT_CREATE,
                                                    csvservice_colMap)) {
                                                obj.put("productcreated_flag", true);
                                                rawObj.put("productcreated_flag", true);
                                            } else {
                                                obj.put("productcreated_flag", false);
                                                rawObj.put("productcreated_flag", false);
                                                rawObj.put("validationStatus", "invalid");
                                                rawObj.put("validationMessage", "The product already exists ");
                                            }
                                        } else {
                                            obj.put("productcreated_flag", false);
                                            rawObj.put("productcreated_flag", false);
                                            rawObj.put("validationStatus", obj.getString("validationStatus"));
                                            rawObj.put("validationMessage", obj.getString("validationMessage"));
                                        }
                                        jArr.put(rawObj);
                                    } catch (Exception excp) {
                                        rawObj.put("validationStatus", "invalid");
                                        rawObj.put("validationMessage", excp.getMessage());
                                        jArr.put(rawObj);
                                    }
                                }
                            } catch (Exception e) {
                                throw e;
                            }
                        }
                    }
                }
            }
            int created = 0;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject obj = (JSONObject) jArr.get(i);
                if (obj.keySet().toString().indexOf("productcreated_flag") > 0
                        && obj.getBoolean("productcreated_flag")) {
                    created++;
                }
            }
            processedResult.addParam(new Param("TotalImports", "" + jArr.length(), "int"));
            processedResult.addParam(new Param("CustomersCreated", "" + created, "int"));
            processedResult.addParam(new Param("importedCSV", "" + jArr.toString(), FabricConstants.STRING));
            LOG.info("Service called : ImportProduct_Service : End");
        } catch (Exception e) {
            processedResult.addParam(new Param("Error", e.getMessage(), FabricConstants.STRING));
            LOG.error("Failed while executing get transaction logs", e);
        }
        processedResult.addParam(new Param("Status", "Succesful", FabricConstants.STRING));
        return processedResult;
    }

}