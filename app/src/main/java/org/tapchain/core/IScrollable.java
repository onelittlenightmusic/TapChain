package org.tapchain.core;


import org.tapchain.editor.TapChain;

public interface IScrollable {
	boolean onScrolled(TapChain edit, IPoint pos, IPoint vp);
}
