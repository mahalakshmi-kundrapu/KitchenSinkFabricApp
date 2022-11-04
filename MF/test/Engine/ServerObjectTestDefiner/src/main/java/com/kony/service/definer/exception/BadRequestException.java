package com.kony.service.definer.exception;

/**
 * Custom Exception thrown on a Bad Request
 *
 * @author Aditya Mankal
 */
public class BadRequestException extends RuntimeException {

	private static final long serialVersionUID = 8873443772057981412L;

	public BadRequestException(String message) {
		super(message);
	}
}
