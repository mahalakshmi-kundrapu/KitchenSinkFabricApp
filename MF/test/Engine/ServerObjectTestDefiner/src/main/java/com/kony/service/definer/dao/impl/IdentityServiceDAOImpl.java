package com.kony.service.definer.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kony.service.definer.dto.Application;
import com.kony.service.definer.dto.RuntimeInstance;
import com.kony.service.definer.exception.ResourceNotFoundException;
import com.kony.service.dto.IdentityService;

/**
 * JSON Storage DAO Implementation of {@link IdentityService}
 *
 * @author Aditya Mankal
 */
public class IdentityServiceDAOImpl implements com.kony.service.definer.dao.IdentityServiceDAO {

	private static final Logger LOG = LogManager.getLogger(IdentityServiceDAOImpl.class);

	private static IdentityServiceDAOImpl identityServiceDAOImpl;
	private final RuntimeInstance runtimeInstance;

	private IdentityServiceDAOImpl(RuntimeInstance runtimeInstance) {
		this.runtimeInstance = runtimeInstance;
		// Private Constructor
	}

	public static IdentityServiceDAOImpl getInstance(RuntimeInstance runtimeInstance) {
		if (identityServiceDAOImpl == null) {
			identityServiceDAOImpl = new IdentityServiceDAOImpl(runtimeInstance);
		}
		return identityServiceDAOImpl;
	}

	@Override
	public IdentityService getIdentityService(String providerName) {

		try {
			List<IdentityService> identityServicesList = new ArrayList<>();
			List<Application> applications = runtimeInstance.getApplications();
			for (Application application : applications) {
				identityServicesList.addAll(application.getIdentityServices());
			}
			for (IdentityService identityService : identityServicesList) {
				if (StringUtils.equalsIgnoreCase(identityService.getProviderName(), providerName)) {
					return identityService;
				}
			}
			throw new ResourceNotFoundException("Identity Service with the name" + providerName + " not found");
		} catch (Exception e) {
			LOG.error("Exception in fetching Identity Service. Exception", e);
			throw new RuntimeException("Exception in fetching Identity Service. Exception", e);
		}

	}

	@Override
	public List<IdentityService> getIdentityServices() {

		try {
			List<IdentityService> identityServicesList = new ArrayList<>();
			List<Application> applications = runtimeInstance.getApplications();
			for (Application application : applications) {
				identityServicesList.addAll(application.getIdentityServices());
			}
			return identityServicesList;
		} catch (Exception e) {
			LOG.error("Exception in constructing Identity Services List. Exception:", e);
			throw new RuntimeException("Exception in fetching Identity Service. Exception", e);
		}
	}

}
