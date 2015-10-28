package org.tapchain.core;

import org.tapchain.editor.IActorTap;
import org.tapchain.editor.IEditor;

public interface IScrollHandler {
	public void onScroll(IEditor edit, IActorTap tap, IPoint pos, IPoint vp);
}
