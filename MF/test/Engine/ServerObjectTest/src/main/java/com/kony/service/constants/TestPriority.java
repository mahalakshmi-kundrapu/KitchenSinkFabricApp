package com.kony.service.constants;

import com.kony.service.dto.TestCase;

/**
 * Enum constants containing possible values of Test Priority for {@link TestCase}
 * 
 * @author Aditya Mankal
 *
 */
public enum TestPriority {

	P0("Priority 0"),
	P1("Priority 1"),
	P2("Priority 2"),
	P3("Priority 3");

	private String testPriority;

	TestPriority(String testPriority) {
		this.testPriority = testPriority;
	}

	@Override
	public String toString() {
		return this.testPriority;
	}
}
