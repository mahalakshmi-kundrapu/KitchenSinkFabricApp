package com.kony.adminconsole.preprocessor;

import java.util.HashMap;

import com.konylabs.middleware.common.DataPreProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

/**
 * <p>
 * Pre-processor that returns services auth data received from identity as input param and sends back in response as is.
 * 
 * <p>
 * Enable pass-through response on service operation for using this pre-processor. This underlying service will never
 * gets executed.
 * 
 * @author Venkateswara Rao Alla
 *
 */

public class ServicesAuthDataPreProcessor implements DataPreProcessor2 {

    // Request Input Keys

    // API User Keys
    @SuppressWarnings("rawtypes")
    @Override
    public boolean execute(HashMap inputMap, DataControllerRequest request, DataControllerResponse response,
            Result result) throws Exception {

        return false;
    }

}
