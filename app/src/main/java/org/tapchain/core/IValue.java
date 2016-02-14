package org.tapchain.core;

public interface IValue<T> {
	boolean _set(T value);
	T _get();
}