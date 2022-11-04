package com.kony.service.definer.exception;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.kony.service.constants.DocumentationURLs;
import com.kony.service.dto.ErrorMessage;

/**
 * Exception mapper for exception of type {Throwable}
 *
 * @author Aditya Mankal
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

	@Override
	public Response toResponse(Throwable exception) {
		ErrorMessage errorMessage = new ErrorMessage(exception.getMessage(), Status.INTERNAL_SERVER_ERROR.getStatusCode(), DocumentationURLs.DOCUMENTATION_HOME);
		Response response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(errorMessage).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
		return response;
	}

}
