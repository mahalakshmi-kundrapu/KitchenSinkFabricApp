package com.kony.service.definer.rest;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.kony.service.definer.exception.ResourceNotFoundException;
import com.kony.service.util.FileUtilities;

/**
 * Class to handle return requested resource from classpath
 *
 * @author Aditya Mankal
 */
@Path("/Resource")
public class ResourceFetchService {

	@GET
	@Path("{filename}")
	public InputStream getResource(@PathParam("filename") String fileName) {
		try (InputStream inputStream = FileUtilities.class.getClassLoader()
				.getResourceAsStream(fileName);) {
			return inputStream;
		} catch (Exception e) {
			throw new ResourceNotFoundException("Resource with the name:" + fileName + " not found.");
		}

	}

	@GET
	@Path("site/{filename}")
	public InputStream getSiteResource(@PathParam("filename") String fileName) {
		try (InputStream inputStream = FileUtilities.class.getClassLoader()
				.getResourceAsStream("site/" + fileName);) {
			return inputStream;
		} catch (Exception e) {
			throw new ResourceNotFoundException("Resource with the name:" + fileName + " not found.");
		}
	}

}
