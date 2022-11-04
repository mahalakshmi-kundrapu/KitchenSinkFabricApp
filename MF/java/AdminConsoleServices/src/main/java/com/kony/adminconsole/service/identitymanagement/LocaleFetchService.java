package com.kony.adminconsole.service.identitymanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.exception.ApplicationException;
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
 * Service to fetch password lockout settings from the back end
 * 
 * @author Mohit Khosla (KH2356)
 */

public class LocaleFetchService implements JavaService2 {

    public static final String LOCALE = "locale";

    private static final Logger LOG = Logger.getLogger(LocaleFetchService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();

        try {

            // ** Reading from 'locale' table **
            Map<String, String> localeTableMap = new HashMap<>();

            String readLocaleResponse = Executor.invokeService(ServiceURLEnum.LOCALE_READ, localeTableMap, null,
                    requestInstance);
            JSONObject readLocaleResponseJSON = CommonUtilities.getStringAsJSONObject(readLocaleResponse);

            if (readLocaleResponseJSON != null && readLocaleResponseJSON.has(FabricConstants.OPSTATUS)
                    && readLocaleResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readLocaleResponseJSON.has(LOCALE)) {

                JSONArray readLocaleResponseJSONArray = readLocaleResponseJSON.getJSONArray(LOCALE);

                Dataset localeDataset = new Dataset();
                localeDataset.setId(LOCALE);

                for (int i = 0; i < readLocaleResponseJSONArray.length(); ++i) {

                    JSONObject localeResponse = readLocaleResponseJSONArray.getJSONObject(i);
                    Record localeRecord = new Record();

                    if (StringUtils.isNotBlank(localeResponse.optString("Code"))
                            && StringUtils.isNotBlank(localeResponse.optString("Language"))) {

                        localeRecord
                                .addParam(new Param("code", localeResponse.getString("Code"), FabricConstants.STRING));
                        localeRecord.addParam(
                                new Param("language", localeResponse.getString("Language"), FabricConstants.STRING));
                    } else {
                        throw new ApplicationException(ErrorCodeEnum.ERR_21171);
                    }

                    localeDataset.addRecord(localeRecord);
                }

                result.addDataset(localeDataset);
            } else {
                ErrorCodeEnum.ERR_21171.setErrorCode(result);
            }
        } catch (ApplicationException ae) {
            ae.getErrorCodeEnum().setErrorCode(result);
            LOG.error("ApplicationException occured in LocaleFetchService. Error: ", ae);
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            LOG.error("Exception occured in LocaleFetchService. Error: ", e);
        }

        return result;
    }
}
