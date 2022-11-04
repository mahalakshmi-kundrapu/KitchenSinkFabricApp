package com.kony.adminconsole.service.commonoperations;

import java.util.TimeZone;

import com.kony.adminconsole.commons.utils.FabricConstants;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class ServerTimeZoneOffset implements JavaService2 {

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        TimeZone timeZone = TimeZone.getDefault();
        long zoneOffsetInMillis = timeZone.getOffset(System.currentTimeMillis());
        int zoneOffsetInSeconds = (int) zoneOffsetInMillis / 1000;
        int zoneOffsetInMinutes = (int) zoneOffsetInSeconds / 60;

        Result result = new Result();
        result.addParam(new Param("ZoneID", timeZone.getID(), FabricConstants.STRING));
        result.addParam(new Param("ZoneOffsetInSeconds", String.valueOf(zoneOffsetInSeconds), FabricConstants.STRING));
        result.addParam(new Param("ZoneOffsetInMinutes", String.valueOf(zoneOffsetInMinutes), FabricConstants.STRING));
        return result;
    }
}
