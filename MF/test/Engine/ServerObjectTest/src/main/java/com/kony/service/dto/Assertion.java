package com.kony.service.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO of Assertion Attributes
 *
 * @author Aditya Mankal
 */
public class Assertion implements Serializable {

	private static final long serialVersionUID = 3132661617622323927L;

	private String id;
	private String path;
	private String dataType;
	private String operator;
	private Object value;
	private boolean isNullable;
	private boolean isValueAgnostic;
	private String variableName;

	/**
	 * 
	 */
	public Assertion() {
		super();
	}

	/**
	 * @return the variableName
	 */
	public String getVariableName() {
		return variableName;
	}

	/**
	 * @param variableName the variableName to set
	 */
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the dataType
	 */
	public String getDataType() {
		return dataType;
	}

	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the operator
	 */
	public String getOperator() {
		return operator;
	}

	/**
	 * @param operator the operator to set
	 */
	public void setOperator(String operator) {
		this.operator = operator;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * @return the isNullable
	 */
	@JsonProperty(value = "isNullable")
	public boolean isNullable() {
		return isNullable;
	}

	/**
	 * @param isNullable the isNullable to set
	 */
	@JsonProperty(value = "isNullable")
	public void setIsNullable(boolean isNullable) {
		this.isNullable = isNullable;
	}

	/**
	 * @return the isValueAgnostic
	 */
	@JsonProperty(value = "isValueAgnostic")
	public boolean isValueAgnostic() {
		return isValueAgnostic;
	}

	/**
	 * @param isValueAgnostic the isValueAgnostic to set
	 */
	@JsonProperty(value = "isValueAgnostic")
	public void setIsValueAgnostic(boolean isValueAgnostic) {
		this.isValueAgnostic = isValueAgnostic;
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public String toString() {
		return id;
	}

}
