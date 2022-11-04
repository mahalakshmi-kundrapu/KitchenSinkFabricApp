package com.kony.service.value.type;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
public class StringType extends ValueType {

	public static final String EQUALS = "equals";
	public static final String NOT_EQUALS = "notEquals";

	public static final String EQUALS_ANY = "equalsAny";
	public static final String EQUALS_ANY_IGNORE_CASE = "equalsAnyIgnoreCase";

	public static final String CONTAINS = "contains";
	public static final String NOT_CONTAINS = "notContains";

	public static final String HAS_LENGTH = "hasLength";

	public static final String EQUALS_IGNORE_CASE = "equalsIgnoreCase";
	public static final String NOT_EQUALS_IGNORE_CASE = "notEqualsIgnoreCase";

	public static final String CONTAINS_IGNORE_CASE = "containsIgnoreCase";
	public static final String NOT_CONTAINS_IGNORE_CASE = "notContainsIgnoreCase";

	private static final Class<?>[] STRING_TYPE_ARGS = { String.class, String.class };
	private static final Logger LOG = LogManager.getLogger(StringType.class);

	@Override
	public boolean compare(Object currActualValue, Object currExpectedValue, String currOperator)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		try {
			LOG.info("Actual Value:" + Objects.toString(currActualValue));
			LOG.info("Expected Value:" + Objects.toString(currExpectedValue));
			LOG.info("Operator:" + currOperator);
			Method currMethod = StringType.class.getDeclaredMethod(currOperator, STRING_TYPE_ARGS);
			boolean conditionResult = (boolean) currMethod.invoke(null, String.valueOf(currActualValue), String.valueOf(currExpectedValue));
			return conditionResult;
		} catch (Exception e) {
			LOG.error("Invalid Parameters passed. Exception:", e);
			return false;
		}
	}

	public static void main(String[] args) {
		String str1 = "abc";
		String str2 = "abc,def,ghi";
		System.out.println(equalsAny(str1, str2));
	}

	private static boolean equalsAny(String str1, String str2) {
		return StringUtils.equalsAny(str1, str2.split(","));
	}
	
	private static boolean equalsAnyIgnoreCase(String str1, String str2) {
		return StringUtils.equalsAnyIgnoreCase(str1, str2.split(","));
	}

	private static boolean hasLength(String str, String length) {
		return StringUtils.length(str) == Integer.parseInt(length);
	}

	private static boolean equals(String str1, String str2) {
		return StringUtils.equals(str1, str2);
	}

	private static boolean notEquals(String str1, String str2) {
		return !StringUtils.equals(str1, str2);
	}

	private static boolean equalsIgnoreCase(String str1, String str2) {
		return StringUtils.equalsIgnoreCase(str1, str2);
	}

	private static boolean notEqualsIgnoreCase(String str1, String str2) {
		return !StringUtils.equalsIgnoreCase(str1, str2);
	}

	private static boolean contains(String str1, String str2) {
		return StringUtils.contains(str1, str2);
	}

	private static boolean notContains(String str1, String str2) {
		return !StringUtils.contains(str1, str2);
	}

	private static boolean containsIgnoreCase(String str1, String str2) {
		return StringUtils.containsIgnoreCase(str1, str2);
	}

	private static boolean notContainsIgnoreCase(String str1, String str2) {
		return !StringUtils.containsIgnoreCase(str1, str2);
	}

}
