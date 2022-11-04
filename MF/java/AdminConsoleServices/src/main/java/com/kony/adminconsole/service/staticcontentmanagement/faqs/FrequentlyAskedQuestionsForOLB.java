package com.kony.adminconsole.service.staticcontentmanagement.faqs;

import java.util.ArrayList;
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
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.kony.adminconsole.utilities.StatusEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

public class FrequentlyAskedQuestionsForOLB implements JavaService2 {

    private static final Logger logger = Logger.getLogger(FrequentlyAskedQuestionsForOLB.class);
    private static final String STATUS_ACTIVE = StatusEnum.SID_ACTIVE.name();

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result result = new Result();
        try {
            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            String categoryId = requestInstance.getParameter("categoryId");
            String categoryName = requestInstance.getParameter("categoryName");

            List<String> conditions = new ArrayList<String>();
            conditions.add("Status_id eq '" + STATUS_ACTIVE + "'");

            if (StringUtils.isNotBlank(categoryId)) {
                conditions.add("CategoryId eq '" + categoryId + "'");
            }
            if (StringUtils.isNotBlank(categoryName)) {
                conditions.add("CategoryName eq '" + categoryName + "'");
            }

            Map<String, String> postParametersMap = new HashMap<String, String>();
            postParametersMap.put(ODataQueryConstants.FILTER, StringUtils.join(conditions, " and "));
            postParametersMap.put(ODataQueryConstants.ORDER_BY, "CategoryName");

            String readFAQsResponse = Executor.invokeService(ServiceURLEnum.FAQCATEGORY_VIEW_READ, postParametersMap,
                    null, requestInstance);
            JSONObject readFAQsResponseJSON = CommonUtilities.getStringAsJSONObject(readFAQsResponse);
            if (readFAQsResponseJSON != null && readFAQsResponseJSON.has(FabricConstants.OPSTATUS)
                    && readFAQsResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                            & readFAQsResponseJSON.has("faqcategory_view")) {
                JSONArray FAQsJSONArray = readFAQsResponseJSON.getJSONArray("faqcategory_view");
                Record record = null;
                Dataset dataset = new Dataset("categories");
                String category, prevCategory = null;
                JSONObject temp = null;
                Dataset faqs = null;
                for (int indexVar = 0; indexVar < FAQsJSONArray.length(); indexVar++) {
                    temp = FAQsJSONArray.getJSONObject(indexVar);
                    category = temp.getString("CategoryName");
                    // FAQ record
                    Record faqRecord = new Record();
                    faqRecord.addParam(new Param("question", temp.getString("Question"), FabricConstants.STRING));
                    faqRecord.addParam(new Param("answer", temp.getString("Answer"), FabricConstants.STRING));
                    faqRecord.addParam(new Param("id", temp.getString("id"), FabricConstants.STRING));

                    if (!category.equalsIgnoreCase(prevCategory) && faqs != null) {
                        dataset.addRecord(record);
                        faqs = null;
                    }
                    faqs = (faqs == null ? new Dataset("faqs") : record.getDatasetById("faqs"));
                    faqs.addRecord(faqRecord);
                    record = new Record();
                    record.addParam(new Param("categoryName", category, FabricConstants.STRING));
                    record.addParam(new Param("categoryId", temp.getString("CategoryId"), FabricConstants.STRING));
                    record.addDataset(faqs);
                    prevCategory = category;

                    if (indexVar == FAQsJSONArray.length() - 1) {
                        dataset.addRecord(record);
                    }

                }

                if (FAQsJSONArray.length() == 0) {
                    dataset = constructCategoryNameAndId(categoryName, authToken, requestInstance);
                }

                result.addDataset(dataset);
                return result;
            }
            ErrorCodeEnum.ERR_20161.setErrorCode(result);
            return result;

        } catch (Exception e) {
            logger.error("Error in fetching category faqs");
            logger.error(e.toString());
            ErrorCodeEnum.ERR_20161.setErrorCode(result);
            return result;
        }
    }

    private Dataset constructCategoryNameAndId(String categoryName, String authToken,
            DataControllerRequest requestInstance) {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        if (StringUtils.isNotBlank(categoryName)) {
            postParametersMap.put(ODataQueryConstants.FILTER, "Name eq '" + categoryName + "'");
        }
        String readFAQCategories = Executor.invokeService(ServiceURLEnum.FAQS_READ, postParametersMap, null,
                requestInstance);
        JSONObject readFAQCategoriesJSON = CommonUtilities.getStringAsJSONObject(readFAQCategories);
        int opStatusCode = readFAQCategoriesJSON.getInt(FabricConstants.OPSTATUS);
        Dataset dataset = new Dataset("categories");
        if (opStatusCode == 0) {
            JSONArray FAQsCatArray = readFAQCategoriesJSON.getJSONArray("faqcategory");
            Dataset faqs = new Dataset("faqs");
            Record record = null;
            for (int indexVar = 0; indexVar < FAQsCatArray.length(); indexVar++) {
                record = new Record();
                record.addParam(new Param("categoryName", FAQsCatArray.getJSONObject(indexVar).optString("Name"),
                        FabricConstants.STRING));
                record.addParam(new Param("categoryId", FAQsCatArray.getJSONObject(indexVar).optString("id"),
                        FabricConstants.STRING));
                record.addDataset(faqs);
                dataset.addRecord(record);
            }
        }
        return dataset;
    }

}