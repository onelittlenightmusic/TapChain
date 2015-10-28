package org.tapchain.core;

public interface IRegister {
	public void registerHandler(IScrollHandler s);
	public void unregisterHandler(IScrollHandler effectValue);
}
