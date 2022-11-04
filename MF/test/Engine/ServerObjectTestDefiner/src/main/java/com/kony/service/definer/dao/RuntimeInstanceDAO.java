package com.kony.service.definer.dao;

import java.util.List;

import com.kony.service.definer.dto.RuntimeInstance;

/**
 * DAO Interface of {@link RuntimeInstanceDAO}
 *
 * @author Aditya Mankal
 */
public interface RuntimeInstanceDAO {
	
	List<RuntimeInstance> getRuntimeInstances();
	
	RuntimeInstance getRuntimeInstance(String instanceName);
}
