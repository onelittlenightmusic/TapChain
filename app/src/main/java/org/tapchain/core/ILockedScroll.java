package org.tapchain.core;

import org.tapchain.editor.ITapChain;
import org.tapchain.editor.ITap;

public interface ILockedScroll {
	boolean onLockedScroll(ITapChain edit, ITap selectedTap, IPoint wp);
}
