package org.tapchain.core;

import org.tapchain.editor.IActorTapView;
import org.tapchain.editor.ITapChain;

public interface IScrollHandler {
	public void onScroll(ITapChain edit, IActorTapView tap, IPoint pos, IPoint vp);
}
