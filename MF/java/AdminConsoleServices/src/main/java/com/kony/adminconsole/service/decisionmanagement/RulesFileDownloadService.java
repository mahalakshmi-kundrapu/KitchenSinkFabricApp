package com.kony.adminconsole.service.decisionmanagement;

/*
* 
* @author Alahari Prudhvi Akhil
*
*/

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.log4j.Logger;

import com.kony.adminconsole.commons.dto.FileStreamHandlerBean;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.utilities.DBPServices;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

public class RulesFileDownloadService implements JavaService2 {

    public static final Logger LOG = Logger.getLogger(RulesFileDownloadService.class);

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result processedResult = new Result();
        try {
            Map<String, String> queryParamsMap = (Map<String, String>) requestInstance.getAttribute("queryparams");
            String decisionName = queryParamsMap.get("decisionName");
            String decisionId = queryParamsMap.get("decisionId");
            String version = queryParamsMap.get("version");
            FileStreamHandlerBean fileStreamHandlerBean = DBPServices.downloadRuleFile(decisionName, version,
                    decisionId, requestInstance);
            Map<String, String> customHeaders = new HashMap<String, String>();
            if (StringUtils.isBlank(fileStreamHandlerBean.getFileName())) {
                customHeaders.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
            } else {
                customHeaders.put(HttpHeaders.CONTENT_TYPE, "text/plain; charset=utf-8");
                customHeaders.put("Content-Disposition",
                        "attachment; filename=\"" + fileStreamHandlerBean.getFileName() + "\"");
            }

            responseInstance.setAttribute(FabricConstants.CHUNKED_RESULTS_IN_JSON,
                    new BufferedHttpEntity(new FileEntity(fileStreamHandlerBean.getFile())));
            responseInstance.getHeaders().putAll(customHeaders);
            return processedResult;

        } catch (Exception e) {
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(processedResult);
        }
        return processedResult;
    }

}
