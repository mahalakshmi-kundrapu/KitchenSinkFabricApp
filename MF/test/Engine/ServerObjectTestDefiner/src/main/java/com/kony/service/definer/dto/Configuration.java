package com.kony.service.definer.dto;

import java.util.List;

/**
 * DTO aggregating all Definer Inputs
 *
 * @author Aditya Mankal
 */
public class Configuration {

	private List<RuntimeInstance> instances;

	public Configuration() {
		super();
	}

	/**
	 * @param instances
	 */
	public Configuration(List<RuntimeInstance> instances) {
		super();
		this.instances = instances;
	}

	/**
	 * @return the instances
	 */
	public List<RuntimeInstance> getInstances() {
		return instances;
	}

	/**
	 * @param instances the instances to set
	 */
	public void setInstances(List<RuntimeInstance> instances) {
		this.instances = instances;
	}

}
