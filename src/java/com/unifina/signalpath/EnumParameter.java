package com.unifina.signalpath;

import java.util.ArrayList;
import java.util.List;

public class EnumParameter<T extends Enum<T>> extends Parameter<T> {

	private final T[] enumValues;
	private final Class<T> enumClass;

	public EnumParameter(AbstractSignalPathModule owner, String name, T[] enumValues) {
		super(owner, name, enumValues[0], "String");
		this.enumValues = enumValues;
		this.enumClass = enumValues[0].getDeclaringClass();
	}

	@Override
	public T parseValue(String s) {
		return T.valueOf(enumClass, s.toUpperCase());
	}

	@Override
	public Object formatValue(T value) {
		return value.name();
	}

	@Override
	public void receive(Object value) {
		if (value instanceof String) {
			value = parseValue(value.toString());
		}
		super.receive(value);
	}

	@Override
	protected T handlePulledObject(Object o) {
		if (o instanceof String) {
			o = parseValue(o.toString());
		}
		return super.handlePulledObject(o);
	}

	@Override
	protected List<PossibleValue> getPossibleValues() {
		List<PossibleValue> possibleValues = new ArrayList<>();
		for (T value : enumValues) {
			possibleValues.add(new PossibleValue(value.toString(), value.name()));
		}
		return possibleValues;
	}

	// Base class implementation causes class cast exception because of complicated generic type
	// signature. Couldn't find a version that handles this type signature so overloading for now.
	@Override
	public Class<T> getTypeClass() {
		return enumClass;
	}
}
