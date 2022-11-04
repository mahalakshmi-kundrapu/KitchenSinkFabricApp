package com.kony.service.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * List of Identity Services - DTO - For marshaling operation only
 *
 * @author Aditya Mankal
 */
public class IdentityServices implements Serializable {

	private static final long serialVersionUID = 1545842740060243091L;
	private List<IdentityService> identityServices = new ArrayList<IdentityService>();

	/**
	 * 
	 */
	public IdentityServices() {
		super();
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
