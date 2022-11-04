package com.kony.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO of Field Description
 *
 * @author Aditya Mankal
 */
public class FieldDescription {

	private String name;
	private String description;
	private boolean isMandatory;
	private String range;

	/**
	 * 
	 */
	public FieldDescription() {
		super();
	}

	/**
	 * @param name
	 * @param description
	 * @param isMandatory
	 * @param range
	 */
	public FieldDescription(String name, String description, boolean isMandatory, String range) {
		super();
		this.name = name;
		this.description = description;
		this.isMandatory = isMandatory;
		this.range = range;
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
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the isMandatory
	 */
	@JsonProperty(value = "isMandatory")
	public boolean isMandatory() {
		return isMandatory;
	}

	/**
	 * @param isMandatory the isMandatory to set
	 */
	public void setIsMandatory(boolean isMandatory) {
		this.isMandatory = isMandatory;
	}

	/**
	 * @return the range
	 */
	public String getRange() {
		return range;
	}

	/**
	 * @param range the range to set
	 */
	public void setRange(String range) {
		this.range = range;
	}

}
