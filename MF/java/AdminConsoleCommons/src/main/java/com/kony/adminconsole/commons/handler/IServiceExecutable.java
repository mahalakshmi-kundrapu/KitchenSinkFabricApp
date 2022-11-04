package com.kony.adminconsole.commons.handler;

import java.util.Map;

import org.apache.http.entity.BufferedHttpEntity;

import com.konylabs.middleware.controller.DataControllerRequest;

/**
 * Interface for ServiceExecutors - Inline and HTTP
 * 
 * @author Venkateswara Rao Alla, Aditya Mankal
 *
 */
public interface IServiceExecutable {

    String invokeService(String serviceId, String operationId, Map<String, Object> postParameters,
            Map<String, Object> requestHeaders, DataControllerRequest request) throws Exception;

    BufferedHttpEntity invokePassthroughService(String serviceId, String operationId,
            Map<String, Object> postParameters, Map<String, Object> requestHeaders, DataControllerRequest request)
            throws Exception;

}
