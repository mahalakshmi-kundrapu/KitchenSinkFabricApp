package com.kony.adminconsole.service.limitsandfees;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;

public class ValidateTransactionFeeLimits {

    private static final BigDecimal ZERO_CHECK = new BigDecimal("0");

    public Dataset validateFeeLimits(Dataset ds, JSONArray transactionFeeLimits) {
        Dataset dataset = new Dataset("violations");

        for (int i = 0; i < transactionFeeLimits.length(); i++) {

            JSONObject temp = transactionFeeLimits.getJSONObject(i);
            BigDecimal minLimit = null, maxLimit = null;
            Dataset errors = new Dataset("errors");

            if (StringUtils.isBlank(temp.optString("minAmount"))) {
                Record record = new Record();
                record.addParam(new Param(FabricConstants.ERR_MSG, ErrorCodeEnum.ERR_20597.getMessage(),
                        FabricConstants.STRING));
                errors.addRecord(record);
            }
            if (StringUtils.isBlank(temp.optString("maxAmount"))) {
                Record record = new Record();
                record.addParam(new Param(FabricConstants.ERR_MSG, ErrorCodeEnum.ERR_20598.getMessage(),
                        FabricConstants.STRING));
                errors.addRecord(record);
            }
            if (StringUtils.isBlank(temp.optString("fees"))) {
                Record record = new Record();
                record.addParam(new Param(FabricConstants.ERR_MSG, ErrorCodeEnum.ERR_20587.getMessage(),
                        FabricConstants.STRING));
                errors.addRecord(record);
            }

            if (errors.getAllRecords().isEmpty()) {
                // min, max and fee are defined. Check for mandatory validations
                minLimit = temp.getBigDecimal("minAmount");
                maxLimit = temp.getBigDecimal("maxAmount");
                // min limit cannot be zero or less
                if (minLimit.compareTo(ZERO_CHECK) <= 0) {
                    Record record = new Record();
                    record.addParam(new Param(FabricConstants.ERR_MSG, ErrorCodeEnum.ERR_20599.getMessage(),
                            FabricConstants.STRING));
                    errors.addRecord(record);
                }
                // max limit cannot be zero or less
                if (maxLimit.compareTo(ZERO_CHECK) <= 0) {
                    Record record = new Record();
                    record.addParam(new Param(FabricConstants.ERR_MSG, ErrorCodeEnum.ERR_20600.getMessage(),
                            FabricConstants.STRING));
                    errors.addRecord(record);
                }
                // min limit cannot be greater or equal to max
                if (minLimit.compareTo(maxLimit) >= 0) {
                    Record record = new Record();
                    record.addParam(new Param(FabricConstants.ERR_MSG, ErrorCodeEnum.ERR_20586.getMessage(),
                            FabricConstants.STRING));
                    errors.addRecord(record);
                }
            }
            if (!errors.getAllRecords().isEmpty()) { // error exists , add these errors to along with min max limits
                Record record = new Record();
                record.addParam(new Param("minAmount", temp.optString("minAmount"), FabricConstants.STRING));
                record.addParam(new Param("maxAmount", temp.optString("maxAmount"), FabricConstants.STRING));
                record.addDataset(errors);
                dataset.addRecord(record);
            }
        }
        if (!dataset.getAllRecords().isEmpty()) {
            return dataset;
        }
        dataset = validateOverlapLimitsWithinInput(transactionFeeLimits);
        if (!dataset.getAllRecords().isEmpty()) {
            return dataset;
        }
        dataset = validateOverlapLimitsWithExistingLimits(ds, transactionFeeLimits);
        return dataset;
    }

    /**
     * This method checks for overlapping limits with data in DB i.e already defined limits
     * 
     * @param ds
     * @param transactionFeeLimits
     * @return dataset
     */
    private Dataset validateOverlapLimitsWithExistingLimits(Dataset ds, JSONArray transactionFeeLimits) {
        Dataset dataset = new Dataset("violations");
        for (int i = 0; i < transactionFeeLimits.length(); i++) {
            Dataset errors = new Dataset("errors");
            JSONObject temp = transactionFeeLimits.getJSONObject(i);
            BigDecimal minLimit = temp.getBigDecimal("minAmount");
            BigDecimal maxLimit = temp.getBigDecimal("maxAmount");
            for (Record row : ds.getAllRecords()) {
                if (!row.getParam("transactionFeeSlab_id").getValue()
                        .equalsIgnoreCase(temp.optString("transactionFeesSlabId"))) {
                    BigDecimal minimumValue = new BigDecimal(row.getParam("MinimumTransactionValue").getValue());
                    BigDecimal maximumValue = new BigDecimal(row.getParam("MaximumTransactionValue").getValue());
                    if (minLimit.compareTo(minimumValue) >= 0 && minLimit.compareTo(maximumValue) <= 0) {
                        Record record = new Record();
                        record.addParam(new Param(FabricConstants.ERR_MSG, ErrorCodeEnum.ERR_20588.getMessage(),
                                FabricConstants.STRING));
                        errors.addRecord(record);
                        break;
                    } else if (maxLimit.compareTo(minimumValue) >= 0 && maxLimit.compareTo(maximumValue) <= 0) {
                        Record record = new Record();
                        record.addParam(new Param(FabricConstants.ERR_MSG, ErrorCodeEnum.ERR_20588.getMessage(),
                                FabricConstants.STRING));
                        errors.addRecord(record);
                        break;
                    }
                }
            }
            if (!errors.getAllRecords().isEmpty()) {
                Record record = new Record();
                record.addParam(new Param("minAmount", temp.getString("minAmount"), FabricConstants.STRING));
                record.addParam(new Param("maxAmount", temp.getString("maxAmount"), FabricConstants.STRING));
                record.addDataset(errors);
                dataset.addRecord(record);
            }
        }
        return dataset;
    }

    /**
     * This method checks for overlapping limits within input JSON
     * 
     * @param transactionFeeLimits
     * @return dataset
     */
    private Dataset validateOverlapLimitsWithinInput(JSONArray transactionFeeLimits) {
        Dataset dataset = new Dataset("violations");
        for (int i = 0; i < transactionFeeLimits.length(); i++) {
            Dataset errors = new Dataset("errors");
            JSONObject temp = transactionFeeLimits.getJSONObject(i);
            BigDecimal minLimit = temp.getBigDecimal("minAmount");
            BigDecimal maxLimit = temp.getBigDecimal("maxAmount");
            for (int j = 0; j < transactionFeeLimits.length(); j++) {
                if (i != j) {
                    JSONObject nextJSON = transactionFeeLimits.getJSONObject(j);
                    BigDecimal minimumValue = nextJSON.getBigDecimal("minAmount");
                    BigDecimal maximumValue = nextJSON.getBigDecimal("maxAmount");
                    if (minLimit.compareTo(minimumValue) >= 0 && minLimit.compareTo(maximumValue) <= 0) {
                        Record record = new Record();
                        record.addParam(new Param(FabricConstants.ERR_MSG, ErrorCodeEnum.ERR_20588.getMessage(),
                                FabricConstants.STRING));
                        errors.addRecord(record);
                        break;
                    } else if (maxLimit.compareTo(minimumValue) >= 0 && maxLimit.compareTo(maximumValue) <= 0) {
                        Record record = new Record();
                        record.addParam(new Param(FabricConstants.ERR_MSG, ErrorCodeEnum.ERR_20588.getMessage(),
                                FabricConstants.STRING));
                        errors.addRecord(record);
                        break;
                    }
                }
            }
            if (!errors.getAllRecords().isEmpty()) {
                Record record = new Record();
                record.addParam(new Param("minAmount", temp.getString("minAmount"), FabricConstants.STRING));
                record.addParam(new Param("maxAmount", temp.getString("maxAmount"), FabricConstants.STRING));
                record.addDataset(errors);
                dataset.addRecord(record);
            }
        }
        return dataset;
    }

}