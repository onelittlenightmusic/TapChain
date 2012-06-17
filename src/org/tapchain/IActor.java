package org.tapchain;

import org.tapchain.Chain.ChainException;

public interface IActor {
	public boolean actorRun() throws ChainException, InterruptedException;
}