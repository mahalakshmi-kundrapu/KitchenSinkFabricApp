package com.kony.adminconsole.service.poc;

import org.apache.log4j.Logger;

import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

/**
 * Super class of all DBX Service Classes
 * 
 * @author Aditya Mankal
 *
 */
public abstract class DBXService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(DBXService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) {

        try {
            // Invoke Service Implementation
            Object serviceResult = invokeService(methodID, inputArray, requestInstance, responseInstance);

            // Return Service Result
            return serviceResult;
        } catch (ApplicationException e) {
            // DBX Application Exception
            Result errorResult = new Result();
            LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
            e.getErrorCodeEnum().setErrorCode(errorResult);
            return errorResult;
        } catch (Exception e) {
            // Java Exception
            Result errorResult = new Result();
            LOG.debug("Runtime Exception. Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    protected abstract Object invokeService(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception;
}
