package com.kony.adminconsole.commons.utils.http;

import java.io.File;
import java.io.IOException;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;

import com.kony.adminconsole.commons.dto.FileStreamHandlerBean;
import com.kony.adminconsole.commons.utils.CommonUtilities;

/**
 * Response handler consumes http response and returns as {@link File object} if
 * has content in response, otherwise returns null.
 * 
 * @author Prudhvi Akhil
 *
 */

public class FileStreamResponseHandler implements ResponseHandler<FileStreamHandlerBean> {

	@Override
	public FileStreamHandlerBean handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
		HttpEntity entity = response.getEntity();

		FileStreamHandlerBean fileStreamHandlerBean = new FileStreamHandlerBean();
		fileStreamHandlerBean.setFile(entity == null ? null : CommonUtilities.getInputStreamAsFile(entity.getContent()) );
		setFileName(response,fileStreamHandlerBean);
		
		return fileStreamHandlerBean;
	}

	private void setFileName(HttpResponse httpResponseInstance, FileStreamHandlerBean fileStreamHandlerBean) {
		if(httpResponseInstance.containsHeader("Content-Disposition")) {
			HeaderElement[] headerElements = httpResponseInstance.getFirstHeader("Content-Disposition").getElements();
			for (HeaderElement headerElement : headerElements) {
				if (headerElement.getParameterByName("filename") != null) {
					fileStreamHandlerBean.setFileName(headerElement.getParameterByName("filename").getValue());
				}
			}	
		}
	}

}