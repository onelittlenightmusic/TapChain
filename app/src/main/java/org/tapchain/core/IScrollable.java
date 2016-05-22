package org.tapchain.core;

import org.tapchain.editor.ITapChain;

public interface IScrollable {
	boolean onScrolled(ITapChain edit, IPoint pos, IPoint vp);
}
