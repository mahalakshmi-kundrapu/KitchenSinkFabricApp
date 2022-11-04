package com.kony.adminconsole.preprocessor;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.konylabs.middleware.common.DataPreProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

public class SendEmailPreProcessor implements DataPreProcessor2 {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean execute(HashMap inputMap, DataControllerRequest request, DataControllerResponse response,
            Result result) throws Exception {

        String content = request.getParameter("content");
        if (StringUtils.isNotBlank(content)) {
            content = CommonUtilities.decodeFromBase64(content).replaceAll("\t", "");
            content = content.replaceAll("\r\n", "");
            content = content.replaceAll("\"", "'");
            inputMap.put("content", content);
        }
        return true;
    }

}
