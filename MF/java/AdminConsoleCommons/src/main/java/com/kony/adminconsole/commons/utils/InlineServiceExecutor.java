package com.kony.adminconsole.commons.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.log4j.Logger;

import com.kony.adminconsole.commons.handler.IServiceExecutable;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.konylabs.middleware.api.OperationData;
import com.konylabs.middleware.api.ServiceRequest;
import com.konylabs.middleware.api.ServiceRequestBuilder;
import com.konylabs.middleware.controller.DataControllerRequest;

/**
 * Utility class used to execute other integration services of same app using in line java method call approach
 * 
 * @author Venkateswara Rao Alla, Aditya Mankal
 *
 */

public class InlineServiceExecutor implements IServiceExecutable {

    public static final Logger LOG = Logger.getLogger(InlineServiceExecutor.class);

    protected InlineServiceExecutor() {
        // Protected Constructor
    }

    @Override
    public String invokeService(String serviceId, String operationId, Map<String, Object> requestParameters,
            Map<String, Object> requestHeaders, DataControllerRequest request) throws Exception {
        try {

            OperationData operationData = request.getServicesManager().getOperationDataBuilder()
                    .withServiceId(serviceId).withOperationId(operationId).build();

            ServiceRequestBuilder requestBuilder = request.getServicesManager().getRequestBuilder(operationData);
            requestBuilder.withDCRRequest(request);
            requestBuilder.withAttributes(getAttributes(request));
            String authToken = CommonUtilities.getAuthToken(request);
            if (StringUtils.isNotBlank(authToken)) {
                requestHeaders.put(HTTPOperations.X_KONY_AUTHORIZATION_HEADER, CommonUtilities.getAuthToken(request));
            }

            if (requestHeaders != null && !requestHeaders.isEmpty()) {
                requestBuilder.withHeaders(requestHeaders);
            }
            if (requestParameters != null && !requestParameters.isEmpty()) {
                requestBuilder.withInputs(requestParameters);
            }
            ServiceRequest serviceRequest = requestBuilder.build();
            return serviceRequest.invokeServiceAndGetJson();

        } catch (Exception e) {
            LOG.error("Failed to execute service as inline method call for service/operation:" + serviceId + "/"
                    + operationId, e);
            throw e;
        }
    }

    @Override
    public BufferedHttpEntity invokePassthroughService(String serviceId, String operationId,
            Map<String, Object> requestParameters, Map<String, Object> requestHeaders, DataControllerRequest request)
            throws Exception {
        try {

            OperationData operationData = request.getServicesManager().getOperationDataBuilder()
                    .withServiceId(serviceId).withOperationId(operationId).build();

            ServiceRequestBuilder requestBuilder = request.getServicesManager().getRequestBuilder(operationData);
            requestBuilder.withDCRRequest(request);
            requestBuilder.withAttributes(getAttributes(request));
            String authToken = CommonUtilities.getAuthToken(request);
            if (StringUtils.isNotBlank(authToken)) {
                requestHeaders.put(HTTPOperations.X_KONY_AUTHORIZATION_HEADER, CommonUtilities.getAuthToken(request));
            }

            if (requestHeaders != null && !requestHeaders.isEmpty()) {
                requestBuilder.withHeaders(requestHeaders);
            }
            if (requestParameters != null && !requestParameters.isEmpty()) {
                requestBuilder.withInputs(requestParameters);
            }
            ServiceRequest serviceRequest = requestBuilder.build();
            return serviceRequest.invokePassThroughServiceAndGetEntity();

        } catch (Exception e) {
            LOG.error("Failed to execute service as inline method call for service/operation:" + serviceId + "/"
                    + operationId, e);
            throw e;
        }
    }

    private Map<String, Object> getAttributes(DataControllerRequest request) {
        Map<String, Object> attributesMap = new HashMap<String, Object>();
        Iterator<String> attributeNames = request.getAttributeNames();
        String currAttributeName;
        while (attributeNames.hasNext()) {
            currAttributeName = attributeNames.next();
            attributesMap.put(currAttributeName, request.getAttribute(currAttributeName));
        }
        return attributesMap;
    }

}