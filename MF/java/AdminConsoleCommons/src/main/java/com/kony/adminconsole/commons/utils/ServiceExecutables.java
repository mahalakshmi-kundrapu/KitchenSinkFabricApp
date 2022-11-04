package com.kony.adminconsole.commons.utils;

import java.util.Map;

import org.apache.http.entity.BufferedHttpEntity;

import com.kony.adminconsole.commons.handler.IServiceExecutable;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.konylabs.middleware.controller.DataControllerRequest;

/**
 * ServiceExecutables - INLINE and HTTP
 * 
 * @author Venkateswara Rao Alla, Aditya Mankal
 *
 */
public enum ServiceExecutables {

    HTTP(new HTTPOperations()), INLINE(new InlineServiceExecutor());

    private IServiceExecutable serviceExecutable;

    private ServiceExecutables(IServiceExecutable serviceExecutable) {
        this.serviceExecutable = serviceExecutable;
    }

    public String invokeService(String serviceId, String operationId, Map<String, Object> postParameters,
            Map<String, Object> requestHeaders, DataControllerRequest request) throws Exception {
        return serviceExecutable.invokeService(serviceId, operationId, postParameters, requestHeaders, request);
    }

    public BufferedHttpEntity invokePassthroughService(String serviceId, String operationId,
            Map<String, Object> postParameters, Map<String, Object> requestHeaders, DataControllerRequest request)
            throws Exception {
        return serviceExecutable.invokePassthroughService(serviceId, operationId, postParameters, requestHeaders,
                request);
    }
}