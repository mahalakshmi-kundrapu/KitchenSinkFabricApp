package com.kony.service.exception;

/**
 * Exception Class. Thrown on failure in fetching Authentication Token
 *
 * @author Aditya Mankal
 */
public class AuthenticationFailureException extends RuntimeException {

	private static final long serialVersionUID = -4105895002195296183L;

	public AuthenticationFailureException(String message) {
		super(message);
	}
}
