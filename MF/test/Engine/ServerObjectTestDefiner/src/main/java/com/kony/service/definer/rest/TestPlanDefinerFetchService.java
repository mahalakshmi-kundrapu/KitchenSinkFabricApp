package com.kony.service.definer.rest;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.kony.service.util.FileUtilities;
import com.kony.service.util.JSONUtilities;

/**
 * Class to return the TestPlanDefiner Page
 *
 * @author Aditya Mankal
 */
@Path("/TestPlanDefiner")
public class TestPlanDefinerFetchService {

	private static final String RESOURCE_NAME = "index.html";
	private static final Logger LOG = LogManager.getLogger(TestPlanDefinerFetchService.class);

	@GET
	@Produces(MediaType.TEXT_HTML)
	public InputStream getResource() {
		try (InputStream inputStream = FileUtilities.class.getClassLoader().getResourceAsStream(RESOURCE_NAME);) {
			return inputStream;
		} catch (Exception e) {
			LOG.error("Exception in Reading Resource Contents from Classpath. Resource name:" + RESOURCE_NAME, e);
			throw new RuntimeException("Exception in Reading Resource Contents from Classpath. Resource name:" + RESOURCE_NAME, e);
		}
	}

	@OPTIONS
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getSupportedOperations() {
		return JSONUtilities.getStringAsJSONObject("{\"operations\":[\"GET\"]}");
	}
}
