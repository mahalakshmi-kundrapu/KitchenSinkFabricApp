package com.kony.service.exception;

/**
 * Exception Class. Thrown on Invalid HTTP Service Configuration
 *
 * @author Aditya Mankal
 */
public class InvalidServiceConfigurationException extends RuntimeException {

	private static final long serialVersionUID = -3875288556863758543L;

	public InvalidServiceConfigurationException(String message) {
		super(message);
	}
}
