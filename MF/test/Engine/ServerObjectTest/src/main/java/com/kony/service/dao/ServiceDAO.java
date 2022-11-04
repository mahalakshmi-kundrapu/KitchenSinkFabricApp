package com.kony.service.dao;

import java.util.List;

import com.kony.service.dto.Service;

/**
 * DAO Interface of {@link Service}
 *
 * @author Aditya Mankal
 */
public interface ServiceDAO {

	Service getService(String serviceName);

	List<Service> getServices();
}
