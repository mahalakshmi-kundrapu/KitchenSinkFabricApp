package com.kony.adminconsole.commons.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;

import com.kony.adminconsole.commons.utils.JSONUtils;
import com.konylabs.middleware.controller.DataControllerRequest;

/**
 * Utility used to parse JSON request paylod and unmarshall it to pojo
 * 
 * @author Venkateswara Rao Alla
 *
 */
public class JSONPayloadHandler {

    private static final Logger LOG = Logger.getLogger(JSONPayloadHandler.class);

    private static final String POST_METHOD = "POST";

    private JSONPayloadHandler() {
    }

    /**
     * Parses and unmarshalls the request payload to the provided class.
     * 
     * @param clazz
     * @param dataControllerRequest
     * @return
     * @throws IOException
     */
    @SuppressWarnings("deprecation")
    public static <T extends Serializable> T parseLog(Class<T> clazz, DataControllerRequest dataControllerRequest)
            throws IOException {
        if (!isJSONRequest(dataControllerRequest)) {
            LOG.debug("Request is not a JSON request");
            return null;
        }

        HttpServletRequest request = (HttpServletRequest) dataControllerRequest.getOriginalRequest();

        try (InputStream inputStream = request.getInputStream()) {
            String logJSON = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            return JSONUtils.parse(logJSON, clazz);
        }
    }

    /**
     * Determines whether the request content type is JSON or not
     * 
     * @param dataControllerRequest
     * @return
     */
    @SuppressWarnings("deprecation")
    public static boolean isJSONRequest(DataControllerRequest dataControllerRequest) {
        if (dataControllerRequest != null && dataControllerRequest.getOriginalRequest() != null) {
            HttpServletRequest request = (HttpServletRequest) dataControllerRequest.getOriginalRequest();
            // Check that we have a POST request
            if (!POST_METHOD.equalsIgnoreCase(request.getMethod())) {
                return false;
            }
            // Check that we have a JSON as payload content-type in the request
            if (!ContentType.APPLICATION_JSON.getMimeType().equalsIgnoreCase(request.getContentType())) {
                return false;
            }
            return true;
        }
        return false;
    }

}
