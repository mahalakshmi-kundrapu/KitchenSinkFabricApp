package com.kony.service.value.type;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
public class NumberType extends ValueType {

	public static final String EQUALS = "equals";
	public static final String NOT_EQUALS = "notEquals";

	public static final String EQUALS_ANY = "equalsAny";

	public static final String GREATER_THAN = "greaterThan";
	public static final String LESSER_THAN = "lesserThan";

	public static final String GREATER_THAN_OR_EQUALS_TO = "greaterThanOrEqualsTo";
	public static final String LESSER_THAN_OR_EQUALS_TO = "lesserThanOrEqualsTo";

	private static final Class<?>[] DOUBLE_TYPE_ARGS_REG = { double.class, double.class };

	private static final Class<?>[] DOUBLE_TYPE_ARGS_ARR = { double.class, double[].class };

	private static final Logger LOG = LogManager.getLogger(NumberType.class);

	private static final String ALLOWED_OPERATIONS_NAMES = String.join(", ", getAllowedOperations());

	public boolean compare(Object currActualValue, Object currExpectedValue, String currOperator)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		try {
			LOG.info("Actual Value:" + Objects.toString(currActualValue));
			LOG.info("Expected Value:" + Objects.toString(currExpectedValue));
			LOG.info("Operator:" + currOperator);

			Method currMethod = null;
			boolean conditionResult = false;

			// Fetch the method to be invoked
			if (StringUtils.equals(currOperator, EQUALS_ANY)) {
				double actualValue = Double.parseDouble(currActualValue.toString());
				String[] expectedValuesStr = currExpectedValue.toString().split(",");
				double[] expectedVales = new double[expectedValuesStr.length];

				for (int index = 0; index < expectedValuesStr.length; index++) {
					if (NumberUtils.isParsable(expectedValuesStr[index])) {
						expectedVales[index] = Double.parseDouble(expectedValuesStr[index]);
					}
				}
				currMethod = NumberType.class.getDeclaredMethod(currOperator, DOUBLE_TYPE_ARGS_ARR);
				conditionResult = (boolean) currMethod.invoke(null, Double.parseDouble(currActualValue.toString()), expectedVales);
			} else {
				currMethod = NumberType.class.getDeclaredMethod(currOperator, DOUBLE_TYPE_ARGS_REG);
				conditionResult = (boolean) currMethod.invoke(null, Double.parseDouble(currExpectedValue.toString()), Double.parseDouble(currActualValue.toString()));
			}

			return conditionResult;
		} catch (NoSuchMethodException e) {
			LOG.error("Invalid Operator. Allowed Operations:" + ALLOWED_OPERATIONS_NAMES);
			throw new NoSuchMethodException("Invalid Operator. Allowed Operations:" + ALLOWED_OPERATIONS_NAMES);
		} catch (Exception e) {
			LOG.error("Invalid Parameters passed. Exception:", e);
			return false;
		}

	}

	private static boolean equals(double num1, double num2) {
		return num1 == num2;
	}

	private static boolean equalsAny(double actualValue, double... expectedValues) {
		for (double expectedValue : expectedValues) {
			if (actualValue == expectedValue) {
				return true;
			}
		}
		return false;
	}

	private static boolean notEquals(double num1, double num2) {
		return num1 != num2;
	}

	private static boolean greaterThan(double num1, double num2) {
		return num1 > num2;
	}

	private static boolean lesserThan(double num1, double num2) {
		return num1 < num2;
	}

	private static boolean greaterThanOrEqualsTo(double num1, double num2) {
		return num1 >= num2;
	}

	private static boolean lesserThanOrEqualsTo(double num1, double num2) {
		return num1 <= num2;
	}

	public static List<String> getAllowedOperations() {
		List<String> allowedOperations = new ArrayList<>();
		List<Method> methodsList = Arrays.asList(NumberType.class.getDeclaredMethods());
		for (Method method : methodsList) {
			if (Arrays.equals(method.getParameterTypes(), DOUBLE_TYPE_ARGS_REG))
				allowedOperations.add(method.getName());
		}
		return allowedOperations;
	}

}
