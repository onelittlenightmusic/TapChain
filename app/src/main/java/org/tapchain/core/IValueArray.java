package org.tapchain.core;

import java.util.Collection;

public interface IValueArray<E> {
	Collection<E> _valueGetAll();
    E _valueGetLast();
	boolean _valueSet(E obj);
	E _valueGetNext();
}
