package com.kony.service.definer.dto;

import java.util.List;

import com.kony.service.dto.IdentityService;

/**
 * DTO of Application
 *
 * @author Aditya Mankal
 */
public class Application {

	private String name;
	private String appKey;
	private String appSecret;
	private List<IdentityService> identityServices;

	/**
	 * 
	 */
	public Application() {
		super();
	}

	/**
	 * @param name
	 * @param appKey
	 * @param appSecret
	 * @param identityServices
	 */
	public Application(String name, String appKey, String appSecret, List<IdentityService> identityServices) {
		super();
		this.name = name;
		this.appKey = appKey;
		this.appSecret = appSecret;
		this.identityServices = identityServices;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the appKey
	 */
	public String getAppKey() {
		return appKey;
	}

	/**
	 * @param appKey the appKey to set
	 */
	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	/**
	 * @return the appSecret
	 */
	public String getAppSecret() {
		return appSecret;
	}

	/**
	 * @param appSecret the appSecret to set
	 */
	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}

	/**
	 * @return the identityServices
	 */
	public List<IdentityService> getIdentityServices() {
		return identityServices;
	}

	/**
	 * @param identityServices the identityServices to set
	 */
	public void setIdentityServices(List<IdentityService> identityServices) {
		this.identityServices = identityServices;
	}

}
