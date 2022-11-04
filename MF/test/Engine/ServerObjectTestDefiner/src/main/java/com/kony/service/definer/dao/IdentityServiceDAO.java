package com.kony.service.definer.dao;

import java.util.List;

import com.kony.service.dto.IdentityService;

/**
 * DAO Interface of {@link IdentityService}
 *
 * @author Aditya Mankal
 */
public interface IdentityServiceDAO {

	IdentityService getIdentityService(String serviceName);

	List<IdentityService> getIdentityServices();
}
