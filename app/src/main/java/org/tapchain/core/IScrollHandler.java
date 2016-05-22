package org.tapchain.core;

import org.tapchain.editor.IActorTap;
import org.tapchain.editor.ITapChain;

public interface IScrollHandler {
	public void onScroll(ITapChain edit, IActorTap tap, IPoint pos, IPoint vp);
}
