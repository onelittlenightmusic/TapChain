package org.tapchain.core;

public class Value<T> implements IValue<T> {
	T val;
	public Value(T value) {
		_valueSet(value);
	}
	@Override
	public boolean _valueSet(T value) {
		val = value;
		return true;
	}

	@Override
	public T _valueGet() {
		return val;
	}
	
}
