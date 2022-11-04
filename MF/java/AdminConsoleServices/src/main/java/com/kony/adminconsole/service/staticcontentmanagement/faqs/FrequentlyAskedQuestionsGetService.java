package com.kony.adminconsole.service.staticcontentmanagement.faqs;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.handler.PaginationHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to retrieve the FAQs
 *
 * @author Aditya Mankal
 * 
 */
public class FrequentlyAskedQuestionsGetService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(FrequentlyAskedQuestionsGetService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            String categoryId = requestInstance.getParameter("categoryId");
            Map<String, String> postParametersMap = new HashMap<String, String>();

            PaginationHandler.setOffset(requestInstance, postParametersMap);

            if (StringUtils.isNotBlank(categoryId)) {
                postParametersMap.put(ODataQueryConstants.FILTER, "CategoryId eq '" + categoryId + "'");
            }

            JSONObject readFAQsResponseJSON = PaginationHandler.getPaginatedData(ServiceURLEnum.FAQCATEGORY_VIEW_READ,
                    postParametersMap, null, requestInstance);
            Result processedResult = new Result();
            if (readFAQsResponseJSON != null && readFAQsResponseJSON.has(FabricConstants.OPSTATUS)
                    && readFAQsResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readFAQsResponseJSON.has("faqcategory_view")) {
                LOG.debug("Fetch FAQs Status:Successful");
                PaginationHandler.addPaginationMetadataToResultObject(processedResult, readFAQsResponseJSON);
                JSONArray FAQsJSONArray = readFAQsResponseJSON.getJSONArray("faqcategory_view");
                Dataset faqDataSet = new Dataset();
                faqDataSet.setId("records");
                for (int indexVar = 0; indexVar < FAQsJSONArray.length(); indexVar++) {
                    Record currRecord = new Record();
                    JSONObject currFAQObject = FAQsJSONArray.getJSONObject(indexVar);
                    for (String currKey : currFAQObject.keySet()) {
                        Param currValParam = new Param(currKey, currFAQObject.optString(currKey),
                                FabricConstants.STRING);
                        currRecord.addParam(currValParam);
                    }
                    faqDataSet.addRecord(currRecord);
                }
                processedResult.addDataset(faqDataSet);
                return processedResult;
            }
            LOG.error("Fetch FAQs Status:Failed");
            ErrorCodeEnum.ERR_20161.setErrorCode(processedResult);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

}