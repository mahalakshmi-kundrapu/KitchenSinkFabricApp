package com.kony.service.identity;

import com.kony.service.dto.IdentityResponse;
import com.kony.service.exception.AuthenticationFailureException;

/**
 * Interface for Identity Managers
 * 
 * @author Aditya Mankal
 *
 */
public interface IdentityManager {

	public abstract IdentityResponse executeIdentityRequest() throws AuthenticationFailureException;
}
