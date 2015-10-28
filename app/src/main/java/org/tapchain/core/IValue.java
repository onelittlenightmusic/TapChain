package org.tapchain.core;

public interface IValue<T> {
	boolean _valueSet(T value);
	T _valueGet();
}