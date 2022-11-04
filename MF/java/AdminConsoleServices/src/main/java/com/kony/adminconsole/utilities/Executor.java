package com.kony.adminconsole.utilities;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.dbp.core.fabric.extn.DBPServiceInvocationWrapper;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.konylabs.middleware.controller.DataControllerRequest;

/**
 * Class to execute adjacent Fabric Services via Inline Invocation
 * 
 * @author Venkateswara Rao Alla, Aditya Mankal
 *
 */
public class Executor {

    private static final Logger LOG = Logger.getLogger(Executor.class);

    /**
     * Method to execute an adjacent Fabric Service
     * 
     * @param serviceURLEnum
     * @param inputBodyMap
     * @param requestHeaders
     * @param request
     * @return Service Response as String
     */
    public static String invokeService(ServiceURLEnum serviceURLEnum, JSONObject inputBody,
            Map<String, String> requestHeaders, DataControllerRequest request) {
        try {
            if (inputBody != null) {
                InputStream inputStream = IOUtils.toInputStream(inputBody.toString());
                request.setAttribute(FabricConstants.PASS_THROUGH_HTTP_ENTITY, inputStream);
            }
            return DBPServiceInvocationWrapper.invokeServiceAndGetJSON(serviceURLEnum.getServiceName(request), null,
                    serviceURLEnum.getOperationName(request), null, convertMap(requestHeaders), request);
        } catch (Exception e) {
            LOG.error("Exception in invokeService", e);
            return null;
        }
    }

    /**
     * Method to execute an adjacent Fabric Service
     * 
     * @param serviceURLEnum
     * @param inputBodyMap
     * @param requestHeaders
     * @param request
     * @return Service Response as String
     */
    public static String invokeService(ServiceURLEnum serviceURLEnum, Map<String, String> inputBodyMap,
            Map<String, String> requestHeaders, DataControllerRequest request) {
        try {
            return DBPServiceInvocationWrapper.invokeServiceAndGetJSON(serviceURLEnum.getServiceName(request), null,
                    serviceURLEnum.getOperationName(request), convertMap(inputBodyMap), convertMap(requestHeaders),
                    request);

        } catch (Exception e) {
            LOG.error("Exception in invokeService", e);
            return null;
        }
    }

    /**
     * Method to execute an adjacent Fabric Service without DataControllerRequest
     * 
     * @param serviceURLEnum
     * @param inputBodyMap
     * @param requestHeaders
     * @param konyAuthToken
     * @return Service Response as String
     */
    public static String invokeService(ServiceURLEnum serviceURLEnum, Map<String, String> inputBodyMap,
            Map<String, String> requestHeaders, String konyAuthToken) {
        try {
            return DBPServiceInvocationWrapper.invokeServiceAndGetJSON(serviceURLEnum.getServiceName(), null,
                    serviceURLEnum.getOperationName(), convertMap(inputBodyMap), convertMap(requestHeaders),
                    konyAuthToken);

        } catch (Exception e) {
            LOG.error("Exception in invokeService", e);
            return null;
        }
    }

    /**
     * Method to execute an adjacent Fabric Service of type 'Pass-Through Response' and get the Response as Byte Array
     * 
     * @param serviceURLEnum
     * @param requestParameters
     * @param requestHeaders
     * @param dataControllerRequest
     * @return Service Response as Byte Array
     * @throws Exception
     */
    public static byte[] invokePassThroughServiceAndGetBytes(ServiceURLEnum serviceURLEnum,
            Map<String, String> requestParameters, Map<String, String> requestHeaders,
            DataControllerRequest dataControllerRequest) throws Exception {
        try {
            return DBPServiceInvocationWrapper.invokePassThroughServiceAndGetBytes(
                    serviceURLEnum.getServiceName(dataControllerRequest), null,
                    serviceURLEnum.getOperationName(dataControllerRequest), convertMap(requestParameters),
                    convertMap(requestHeaders), dataControllerRequest);
        } catch (Exception e) {
            LOG.error("Exception in invokePassThroughServiceAndGetBytes", e);
            return null;
        }
    }

    /**
     * Method to execute an adjacent Fabric Service of type 'Pass-Through Response' and get the Response as
     * BufferedHttpEntity
     * 
     * @param serviceURLEnum
     * @param requestParameters
     * @param requestHeaders
     * @param dataControllerRequest
     * @return Service Response as BufferedHttpEntity
     * @throws Exception
     */
    public static InputStream invokePassThroughServiceAndGetEntity(ServiceURLEnum serviceURLEnum,
            Map<String, String> requestParameters, Map<String, String> requestHeaders,
            DataControllerRequest dataControllerRequest) throws Exception {
        try {
            BufferedHttpEntity bufferedHttpEntity = DBPServiceInvocationWrapper.invokePassThroughServiceAndGetEntity(
                    serviceURLEnum.getServiceName(dataControllerRequest), null,
                    serviceURLEnum.getOperationName(dataControllerRequest), convertMap(requestParameters),
                    convertMap(requestHeaders), dataControllerRequest);
            if (bufferedHttpEntity != null) {
                return bufferedHttpEntity.getContent();
            }
        } catch (Exception e) {
            LOG.error("Exception in invokePassThroughServiceAndGetEntity", e);
        }
        return null;
    }

    /**
     * Method to execute an adjacent Fabric Service of type 'Pass-Through Response' and get the Response as String
     * 
     * @param serviceURLEnum
     * @param inputBodyMap
     * @param requestHeaders
     * @param request
     * @return Service Response as String
     */
    public static String invokePassThroughServiceAndGetString(ServiceURLEnum serviceURLEnum,
            Map<String, String> inputBodyMap, Map<String, String> requestHeaders, DataControllerRequest request) {
        try {
            return DBPServiceInvocationWrapper.invokePassThroughServiceAndGetString(
                    serviceURLEnum.getServiceName(request), null, serviceURLEnum.getOperationName(request),
                    convertMap(inputBodyMap), convertMap(requestHeaders), request);
        } catch (Exception e) {
            LOG.error("Exception in invokePassThroughServiceAndGetString", e);
            return null;
        }
    }

    private static Map<String, Object> convertMap(Map<String, String> sourceMap) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        if (sourceMap != null && !sourceMap.isEmpty()) {
            for (Entry<String, String> currRecord : sourceMap.entrySet()) {
                resultMap.put(currRecord.getKey(), Object.class.cast(currRecord.getValue()));
            }
        }
        return resultMap;
    }
}
