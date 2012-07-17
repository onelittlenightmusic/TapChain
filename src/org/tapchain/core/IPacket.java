package org.tapchain.core;

import org.tapchain.core.Chain.IPiece;

public interface IPacket<T> {
	public T getObject();

	public IPiece getSource();
}