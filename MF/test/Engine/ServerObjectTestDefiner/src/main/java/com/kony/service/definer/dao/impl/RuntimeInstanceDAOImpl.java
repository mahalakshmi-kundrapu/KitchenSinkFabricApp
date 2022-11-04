package com.kony.service.definer.dao.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omg.CORBA.Environment;

import com.kony.service.definer.dao.RuntimeInstanceDAO;
import com.kony.service.definer.dto.Configuration;
import com.kony.service.definer.dto.RuntimeInstance;
import com.kony.service.definer.exception.ResourceNotFoundException;

/**
 * JSON Storage DAO Implementation of {@link Environment}
 *
 * @author Aditya Mankal
 */
public class RuntimeInstanceDAOImpl implements RuntimeInstanceDAO {

	private static final Logger LOG = LogManager.getLogger(RuntimeInstanceDAOImpl.class);

	private static RuntimeInstanceDAOImpl runtimeInstanceDAOImpl;

	private RuntimeInstanceDAOImpl() {
		// Private Constructor
	}

	public static RuntimeInstanceDAOImpl getInstance() {
		if (runtimeInstanceDAOImpl == null) {
			runtimeInstanceDAOImpl = new RuntimeInstanceDAOImpl();
		}
		return runtimeInstanceDAOImpl;
	}

	@Override
	public List<RuntimeInstance> getRuntimeInstances() {
		Configuration configuration = ConfigurationDAOImpl.getInstance().getConfiguration();
		return configuration.getInstances();
	}

	@Override
	public RuntimeInstance getRuntimeInstance(String instanceName) {
		Configuration configuration = ConfigurationDAOImpl.getInstance().getConfiguration();
		List<RuntimeInstance> runtimeInstances = configuration.getInstances();
		for (RuntimeInstance runtimeInstance : runtimeInstances) {
			if (StringUtils.equals(instanceName, runtimeInstance.getName())) {
				return runtimeInstance;
			}
		}
		LOG.error("No Runtime Instance Found with the Name:" + instanceName);
		throw new ResourceNotFoundException("No Runtime Instance Found with the Name:" + instanceName);
	}

}
