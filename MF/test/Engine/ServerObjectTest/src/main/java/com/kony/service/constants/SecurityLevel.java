package com.kony.service.constants;

/**
 * Constants class holding the Fabric Operation/Verb Security Level Names
 *
 * @author Aditya Mankal
 */
public enum SecurityLevel {

	AUTHENTICATED_APP_USER("AuthenticatedAppUser"),
	ANONYMOUS_APP_USER("AnonymousAppUser"),
	PUBLIC("Public");

	String securityLevel;

	SecurityLevel(String result) {
		this.securityLevel = result;
	}

	public String getValue() {
		return this.securityLevel;
	}

	@Override
	public String toString() {
		return this.securityLevel;
	}

}
