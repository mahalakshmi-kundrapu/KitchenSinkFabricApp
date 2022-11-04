package com.kony.service.definer.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.omg.CORBA.Environment;

import com.kony.service.definer.dao.RuntimeInstanceDAO;
import com.kony.service.definer.dao.impl.RuntimeInstanceDAOImpl;
import com.kony.service.definer.dto.RuntimeInstance;
import com.kony.service.util.JSONUtilities;

/**
 * Class to handle Requests on resource {@link Environment}
 *
 * @author Aditya Mankal
 */
@Path("/Runtimes")
public class RuntimeFetchService {

	RuntimeInstanceDAO runtimeInstanceDAO = RuntimeInstanceDAOImpl.getInstance();

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRuntimeInstance(@QueryParam("instanceName") String instanceName) {
		if (StringUtils.isBlank(instanceName)) {
			// Return list of instance names
			List<String> runtimeInstanceNames = new ArrayList<>();
			List<RuntimeInstance> runtimeInstances = runtimeInstanceDAO.getRuntimeInstances();
			for (RuntimeInstance runtimeInstance : runtimeInstances) {
				if (!runtimeInstanceNames.contains(runtimeInstance.getName())) {
					runtimeInstanceNames.add(runtimeInstance.getName());
				}
			}
			Collections.sort(runtimeInstanceNames);
			return Response.status(HttpStatus.SC_OK).entity(runtimeInstanceNames).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
		} else {
			// Return the specific environment
			RuntimeInstance runtimeInstance = runtimeInstanceDAO.getRuntimeInstance(instanceName);
			return Response.status(HttpStatus.SC_OK).entity(runtimeInstance).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
		}
	}

	@OPTIONS
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getSupportedOperations() {
		return JSONUtilities.getStringAsJSONObject("{\"operations\":[\"GET\"]}");
	}
}
