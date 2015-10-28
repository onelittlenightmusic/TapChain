package org.tapchain.core;

public interface ICollideRegister {
	public void registerCollideHandler(IActorCollideHandler s);
	public void unregisterCollideHandler(IActorCollideHandler effectValue);
}
