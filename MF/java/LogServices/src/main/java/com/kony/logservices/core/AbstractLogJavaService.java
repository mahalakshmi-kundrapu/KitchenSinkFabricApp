package com.kony.logservices.core;

import com.kony.logservices.handler.LogDataSourceHandler;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;

public abstract class AbstractLogJavaService implements JavaService2 {

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            // keeping DCR in current thread context
            LogDataSourceHandler.setRequest(requestInstance);

            return execute(methodID, inputArray, requestInstance, responseInstance);
        } finally {
            // removing DCR from current thread context. This is important.
            LogDataSourceHandler.removeRequest();
        }
    }

    public abstract Object execute(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception;

}
