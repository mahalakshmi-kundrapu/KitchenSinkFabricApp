package com.kony.service.constants;

/**
 * Constants class holding the Test Case Result values
 *
 * @author Aditya Mankal
 */
public enum TestResult {

	PASS("Pass"),
	FAIL("Fail"),
	SKIP("Skip"),
	ERROR("Error");

	String result;

	TestResult(String result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return this.result;
	}

}
