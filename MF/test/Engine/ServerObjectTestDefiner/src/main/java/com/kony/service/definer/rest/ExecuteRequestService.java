package com.kony.service.definer.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.kony.service.definer.dto.ServiceRequest;
import com.kony.service.definer.executor.RequestExecutor;
import com.kony.service.dto.RequestResponse;

/**
 * Class to handle Requests on resource {@link ServiceRequest}
 *
 * @author Aditya Mankal
 */
@Path("/Request")
public class ExecuteRequestService {

	@POST
	@Path("/executeRequest")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public RequestResponse executeRequest(ServiceRequest serviceRequest) {
		return RequestExecutor.executeRequest(serviceRequest);
	}

}
