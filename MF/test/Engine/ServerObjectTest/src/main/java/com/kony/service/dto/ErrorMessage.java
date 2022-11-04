package com.kony.service.dto;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO of API Error Response Message
 *
 * @author Aditya Mankal
 */
@XmlRootElement
public class ErrorMessage {

	private String message;
	private int code;
	private String documentation;

	/**
	 * 
	 */
	public ErrorMessage() {
		super();
	}

	/**
	 * @param message
	 * @param code
	 * @param documentation
	 */
	public ErrorMessage(String message, int code, String documentation) {
		super();
		this.message = message;
		this.code = code;
		this.documentation = documentation;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * @return the documentation
	 */
	public String getDocumentation() {
		return documentation;
	}

	/**
	 * @param documentation the documentation to set
	 */
	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}

}
