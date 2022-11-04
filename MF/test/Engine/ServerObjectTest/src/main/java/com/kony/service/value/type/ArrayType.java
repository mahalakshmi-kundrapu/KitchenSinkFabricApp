package com.kony.service.value.type;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
public class ArrayType extends ValueType {

	public static final String HAS_N_ELEMENTS = "hasNElements";
	public static final String HAS_ATLEAST_N_ELEMENTS = "hasAtleastNElements";
	public static final String HAS_ATMOST_N_ELEMENTS = "hasAtmostNElements";

	public static final String HAS_ELEMENT = "hasElement";
	public static final String NOT_HAS_ELEMENT = "notHasElement";

	private static final Class<?>[] OBJECT_TYPE_ARGS = { Object.class, Object.class };
	private static final Logger LOG = LogManager.getLogger(ArrayType.class);

	public boolean compare(Object currActualValue, Object currExpectedValue, String currOperator)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		try {
			LOG.info("Actual Value:" + Objects.toString(currActualValue));
			LOG.info("Expected Value:" + Objects.toString(currExpectedValue));
			LOG.info("Operator:" + currOperator);
			Method currMethod = ArrayType.class.getDeclaredMethod(currOperator, OBJECT_TYPE_ARGS);
			boolean conditionResult = (boolean) currMethod.invoke(null, currExpectedValue, currActualValue);
			return conditionResult;
		} catch (Exception e) {
			LOG.error("Invalid Parameters passed. Exception:", e);
			return false;
		}
	}

	private static boolean hasNElements(Object[] array, Object expectedLength) {
		return array.length == (int) expectedLength;
	}

	private static boolean hasAtleastNElements(Object[] array, Object expectedLength) {
		return array.length >= (int) expectedLength;
	}

	private static boolean hasAtmostNElements(Object[] array, Object expectedLength) {
		return array.length <= (int) expectedLength;
	}

	private static boolean hasElement(Object[] array, Object expectedElement) {
		return ArrayUtils.contains(array, expectedElement);
	}

	private static boolean notHasElement(Object[] array, Object expectedElement) {
		return !ArrayUtils.contains(array, expectedElement);
	}

}
