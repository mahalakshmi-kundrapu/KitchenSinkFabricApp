package com.kony.service.value.type;

import java.lang.reflect.InvocationTargetException;

public abstract class ValueType {

	public abstract boolean compare(Object currActualValue, Object currExpectedValue, String currOperator)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;
}
