package com.kony.service.dao.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kony.service.dao.ServiceDAO;
import com.kony.service.driver.TestExecutionCLI;
import com.kony.service.dto.Service;
import com.kony.service.util.FileUtilities;
import com.kony.service.util.JSONUtilities;

/**
 * DAO Implementation of {@link Service}
 *
 * @author Aditya Mankal
 */
public class ServiceDAOImpl implements ServiceDAO {

	private static final Logger LOG = LogManager.getLogger(ServiceDAOImpl.class);

	private static List<Service> services;
	private static ServiceDAOImpl serviceDAOImpl;

	private static final String SERVICE_FILE_NAME_PREFIX = "Service_";

	private ServiceDAOImpl() {
		services = initialiseServices();
	}

	public static ServiceDAOImpl getInstance() {

		if (serviceDAOImpl == null) {
			serviceDAOImpl = new ServiceDAOImpl();
		}
		return serviceDAOImpl;

	}

	private List<Service> initialiseServices() {
		try {
			File servicesDirectoryFilePointer = new File(TestExecutionCLI.TEST_PLANS_DIRECTORY_PATH);
			String currentServiceFileContents;
			List<Service> servicesList = new ArrayList<Service>();
			for (File currFile : servicesDirectoryFilePointer.listFiles()) {
				if (StringUtils.startsWith(currFile.getName(), SERVICE_FILE_NAME_PREFIX)) {
					currentServiceFileContents = FileUtilities.readFileContentsToString(currFile.getAbsolutePath());
					Service currentService = JSONUtilities.parse(currentServiceFileContents, Service.class);
					servicesList.add(currentService);
				}
			}
			return servicesList;
		} catch (Exception e) {
			LOG.error("Exception in constructing Services List. Exception:", e);
			return null;
		}
	}

	@Override
	public Service getService(String serviceName) {
		try {

			for (Service service : services) {
				if (StringUtils.equalsIgnoreCase(service.getName(), serviceName)) {
					return service;
				}
			}
			return null;
		} catch (Exception e) {
			LOG.error("Exception in fetching Service Instance. Exception", e);
			return null;
		}

	}

	@Override
	public List<Service> getServices() {
		return services;
	}

}
