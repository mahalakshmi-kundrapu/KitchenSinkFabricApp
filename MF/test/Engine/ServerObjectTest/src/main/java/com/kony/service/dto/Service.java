package com.kony.service.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Superset DTO of {@link TestCase}
 *
 * @author Aditya Mankal
 */
public class Service implements Serializable {

	private static final long serialVersionUID = 5553857380671586287L;
	
	private String name;
	private String url;
	private String moduleName;
	private String httpMethod;
	private String description;
	private List<TestCase> tests;
	private List<FieldDescription> inputFieldsDocument;
	private List<FieldDescription> outputFieldsDocument;
	private Map<String, String> inputFieldsDescription = new HashMap<>();
	private Map<String, String> outputFieldsDescription = new HashMap<>();

	/**
	 * @param name
	 * @param url
	 * @param moduleName
	 * @param httpMethod
	 * @param description
	 * @param tests
	 * @param inputFieldsDocument
	 * @param outputFieldsDocument
	 * @param inputFieldsDescription
	 * @param outputFieldsDescription
	 */
	public Service(String name, String url, String moduleName, String httpMethod, String description, List<TestCase> tests, List<FieldDescription> inputFieldsDocument,
			List<FieldDescription> outputFieldsDocument, Map<String, String> inputFieldsDescription, Map<String, String> outputFieldsDescription) {
		super();
		this.name = name;
		this.url = url;
		this.moduleName = moduleName;
		this.httpMethod = httpMethod;
		this.description = description;
		this.tests = tests;
		this.inputFieldsDocument = inputFieldsDocument;
		this.outputFieldsDocument = outputFieldsDocument;
		this.inputFieldsDescription = inputFieldsDescription;
		this.outputFieldsDescription = outputFieldsDescription;
	}

	/**
	 * 
	 */
	public Service() {
		super();
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
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the moduleName
	 */
	public String getModuleName() {
		return moduleName;
	}

	/**
	 * @param moduleName the moduleName to set
	 */
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	/**
	 * @return the httpMethod
	 */
	public String getHttpMethod() {
		return httpMethod;
	}

	/**
	 * @param httpMethod the httpMethod to set
	 */
	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
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
	 * @return the tests
	 */
	public List<TestCase> getTests() {
		return tests;
	}

	/**
	 * @param tests the tests to set
	 */
	public void setTests(List<TestCase> tests) {
		this.tests = tests;
	}

	/**
	 * @return the inputFieldsDocument
	 */
	public List<FieldDescription> getInputFieldsDocument() {
		return inputFieldsDocument;
	}

	/**
	 * @param inputFieldsDocument the inputFieldsDocument to set
	 */
	public void setInputFieldsDocument(List<FieldDescription> inputFieldsDocument) {
		this.inputFieldsDocument = inputFieldsDocument;
	}

	/**
	 * @return the outputFieldsDocument
	 */
	public List<FieldDescription> getOutputFieldsDocument() {
		return outputFieldsDocument;
	}

	/**
	 * @param outputFieldsDocument the outputFieldsDocument to set
	 */
	public void setOutputFieldsDocument(List<FieldDescription> outputFieldsDocument) {
		this.outputFieldsDocument = outputFieldsDocument;
	}

	/**
	 * @return the inputFieldsDescription
	 */
	public Map<String, String> getInputFieldsDescription() {
		return inputFieldsDescription;
	}

	/**
	 * @param inputFieldsDescription the inputFieldsDescription to set
	 */
	public void setInputFieldsDescription(Map<String, String> inputFieldsDescription) {
		this.inputFieldsDescription = inputFieldsDescription;
	}

	/**
	 * @return the outputFieldsDescription
	 */
	public Map<String, String> getOutputFieldsDescription() {
		return outputFieldsDescription;
	}

	/**
	 * @param outputFieldsDescription the outputFieldsDescription to set
	 */
	public void setOutputFieldsDescription(Map<String, String> outputFieldsDescription) {
		this.outputFieldsDescription = outputFieldsDescription;
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Service other = (Service) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
