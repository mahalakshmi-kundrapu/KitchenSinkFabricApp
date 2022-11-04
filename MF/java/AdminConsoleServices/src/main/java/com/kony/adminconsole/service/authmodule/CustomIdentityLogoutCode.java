package com.kony.adminconsole.service.authmodule;

import org.apache.log4j.Logger;

import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

public class CustomIdentityLogoutCode implements JavaService2 {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(CustomIdentityLogoutCode.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result logoutResult = new Result();
        requestInstance.getSession().invalidate();

        // Success response
        return logoutResult;
    }
}