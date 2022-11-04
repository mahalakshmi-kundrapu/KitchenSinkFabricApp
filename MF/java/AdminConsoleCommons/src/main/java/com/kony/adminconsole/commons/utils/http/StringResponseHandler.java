package com.kony.adminconsole.commons.utils.http;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

/**
 * Response handler consumes http response and returns as {@link String} if has content in response, otherwise returns null.
 * 
 * @author Venkateswara Rao Alla
 *
 */

public class StringResponseHandler implements ResponseHandler<String> {

    @Override
    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
        HttpEntity entity = response.getEntity();
        return entity == null ? null : EntityUtils.toString(entity, StandardCharsets.UTF_8);
    }

}