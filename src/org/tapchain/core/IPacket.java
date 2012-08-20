package org.tapchain.core;


public interface IPacket<T> {
	public T getObject();

	public IPiece getSource();
}