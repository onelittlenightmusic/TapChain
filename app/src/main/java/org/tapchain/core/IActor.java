package org.tapchain.core;

import org.tapchain.core.Chain.ChainException;

public interface IActor {
	boolean actorRun(Actor act) throws ChainException, InterruptedException;
}