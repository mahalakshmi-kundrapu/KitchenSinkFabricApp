package com.kony.service.definer.exception;

/**
 * Custom Exception thrown when the requested Resource is not found
 *
 * @author Aditya Mankal
 */
public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 8076550594561793992L;

	public ResourceNotFoundException(String message) {
		super(message);
	}

}
