package org.tapchain.core;

public interface IActor {
	boolean actorRun(Actor act) throws ChainException, InterruptedException;
}