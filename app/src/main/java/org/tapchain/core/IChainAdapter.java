package org.tapchain.core;

import java.util.Collection;

public interface IChainAdapter<T extends IPiece> {
	void adapterRun(Collection<T> obj);
}
