package org.tapchain.core;


public class Packet<T> implements IPacket<T> {
	T obj = null;
	IPiece source = null;

	Packet(T _obj, IPiece _source) {
		obj = _obj;
		source = _source;
	}

	public T getObject() {
		return obj;
	}

	public IPiece getSource() {
		return source;
	}
}