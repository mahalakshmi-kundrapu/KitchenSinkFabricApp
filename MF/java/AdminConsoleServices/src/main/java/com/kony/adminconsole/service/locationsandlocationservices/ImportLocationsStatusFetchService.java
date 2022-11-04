package com.kony.adminconsole.service.locationsandlocationservices;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.service.identitymanagement.PolicyFetchService;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to fetch status of currently executing Import Locations process from the back end
 * 
 * @author Mohit Khosla (KH2356)
 */

public class ImportLocationsStatusFetchService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(PolicyFetchService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();

        try {

            String yesterdaysDateTime = null;
            if (LocalDateTime.now().getSecond() == 0) {
                yesterdaysDateTime = LocalDateTime.now().plusSeconds(1).withNano(0).minusDays(1).toString();
            } else {
                yesterdaysDateTime = LocalDateTime.now().withNano(0).minusDays(1).toString();
            }

            // ** Reading from 'locationfile' table **
            Map<String, String> locationfileMap = new HashMap<>();
            locationfileMap.put(ODataQueryConstants.FILTER, "lastmodifiedts ge '" + yesterdaysDateTime + "'");
            locationfileMap.put(ODataQueryConstants.ORDER_BY, "lastmodifiedts desc");

            String readLocationFileResponse = Executor.invokeService(ServiceURLEnum.LOCATIONFILE_READ, locationfileMap,
                    null, requestInstance);
            JSONObject readLocationFileResponseJSON = CommonUtilities.getStringAsJSONObject(readLocationFileResponse);

            if (readLocationFileResponseJSON != null && readLocationFileResponseJSON.has(FabricConstants.OPSTATUS)
                    && readLocationFileResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readLocationFileResponseJSON.has("locationfile")) {

                JSONArray readLocationFileResponseJSONArray = readLocationFileResponseJSON.getJSONArray("locationfile");

                // -> Returning all campaigns <-
                Record locationfileRecord = new Record();
                locationfileRecord.setId("locationFile");

                if (readLocationFileResponseJSONArray.length() >= 1) {
                    JSONObject locationFileResponse = readLocationFileResponseJSONArray.getJSONObject(0);

                    String locationFileStatus = locationFileResponse.getString("locationfilestatus");
                    String successCount = locationFileResponse.optString("successcount");
                    String failureCount = locationFileResponse.optString("failurecount");

                    locationfileRecord
                            .addParam(new Param("locationFileStatus", locationFileStatus, FabricConstants.INT));
                    if (locationFileStatus.equals("1")) {
                        locationfileRecord.addParam(new Param("successCount", successCount, FabricConstants.INT));
                        locationfileRecord.addParam(new Param("failureCount", failureCount, FabricConstants.INT));

                        if (!StringUtils.isBlank(failureCount) && Integer.parseInt(failureCount) != 0) {
                            locationfileRecord.addParam(new Param("locationFileName",
                                    locationFileResponse.getString("id"), FabricConstants.STRING));
                        }
                    }
                }

                result.addRecord(locationfileRecord);
            } else {
                throw new ApplicationException(ErrorCodeEnum.ERR_20376);
            }
        } catch (ApplicationException ae) {
            ae.getErrorCodeEnum().setErrorCode(result);
            LOG.error("ApplicationException occured in ImportLocationsStatusFetchService JAVA service. Error: ", ae);
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            LOG.error("Exception occured in ImportLocationsStatusFetchService JAVA service. Error: ", e);
        }

        return result;
    }
}