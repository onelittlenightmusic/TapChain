package org.tapchain.core;

import org.tapchain.core.Chain.ChainException;

public interface IActor {
	public boolean actorRun() throws ChainException, InterruptedException;
}