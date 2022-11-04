package com.kony.service.constants;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.StringUtils;

import com.kony.service.value.type.ArrayType;
import com.kony.service.value.type.BooleanType;
import com.kony.service.value.type.NumberType;
import com.kony.service.value.type.StringType;
import com.kony.service.value.type.ValueType;

/**
 * Enumeration holding the Data types. Also provides compare method on each datatype.
 *
 * @author Aditya Mankal
 */
public enum Datatype {

	STRING("string") {
		public boolean compare(Object currActualValue, Object currExpectedValue, String currOperator)
				throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			ValueType valueType = new StringType();
			return valueType.compare(currActualValue, currExpectedValue, currOperator);
		}
	},
	NUMBER("number") {
		public boolean compare(Object currActualValue, Object currExpectedValue, String currOperator)
				throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			ValueType valueType = new NumberType();
			return valueType.compare(currActualValue, currExpectedValue, currOperator);
		}
	},
	BOOLEAN("boolean") {
		public boolean compare(Object currActualValue, Object currExpectedValue, String currOperator)
				throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			ValueType valueType = new BooleanType();
			return valueType.compare(currActualValue, currExpectedValue, currOperator);
		}
	},
	NULL("null") {
		public boolean compare(Object currActualValue, Object currExpectedValue, String currOperator)
				throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			return currActualValue == null;
		}
	},
	OBJECT("object") {
		public boolean compare(Object currActualValue, Object currExpectedValue, String currOperator)
				throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			ValueType valueType = new StringType();
			return valueType.compare(currActualValue, currExpectedValue, currOperator);
		}
	},
	ARRAY("array") {
		public boolean compare(Object currActualValue, Object currExpectedValue, String currOperator)
				throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			ValueType valueType = new ArrayType();
			return valueType.compare(currActualValue, currExpectedValue, currOperator);
		}
	};

	private final String dataType;
	ValueType valueType;

	Datatype(String dataType) {
		this.dataType = dataType;
	}

	public abstract boolean compare(Object currActualValue, Object currExpectedValue, String currOperator)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	@Override
	public String toString() {
		return this.dataType;
	}

	public static boolean isRecognisedDataType(String dataType) {
		Datatype[] dataTypes = Datatype.values();
		Datatype currDataType;
		for (int indexVar = 0; indexVar < dataTypes.length; indexVar++) {
			currDataType = dataTypes[indexVar];
			if (StringUtils.equals(currDataType.dataType, dataType)) {
				return true;
			}
		}
		return false;
	}
}
