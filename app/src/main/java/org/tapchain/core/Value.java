package org.tapchain.core;

public class Value<T> implements IValue<T> {
	T val;
	public Value(T value) {
		_set(value);
	}
	@Override
	public boolean _set(T value) {
		val = value;
		return true;
	}

	@Override
	public T _get() {
		return val;
	}
	
}
