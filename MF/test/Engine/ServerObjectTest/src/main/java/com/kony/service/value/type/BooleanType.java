package com.kony.service.value.type;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
public class BooleanType extends ValueType {

	public static final String EQUALS = "equals";
	public static final String NOT_EQUALS = "notEquals";

	private static final Class<?>[] BOOLEAN_TYPE_ARGS = { boolean.class, boolean.class };
	private static final Logger LOG = LogManager.getLogger(BooleanType.class);

	public boolean compare(Object currActualValue, Object currExpectedValue, String currOperator)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		try {
			LOG.info("Actual Value:" + Objects.toString(currActualValue));
			LOG.info("Expected Value:" + Objects.toString(currExpectedValue));
			LOG.info("Operator:" + currOperator);
			Method currMethod = BooleanType.class.getDeclaredMethod(currOperator, BOOLEAN_TYPE_ARGS);
			boolean conditionResult = (boolean) currMethod.invoke(null, Boolean.parseBoolean(currExpectedValue.toString()), Boolean.parseBoolean(currActualValue.toString()));
			return conditionResult;
		} catch (Exception e) {
			LOG.error("Invalid Parameters passed. Exception:", e);
			return false;
		}

	}

	private static boolean equals(boolean val1, boolean val2) {
		return val1 == val2;
	}

	private static boolean notEquals(boolean val1, boolean val2) {
		return val1 != val2;
	}
}
