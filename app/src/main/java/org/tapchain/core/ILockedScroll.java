package org.tapchain.core;

import org.tapchain.editor.ITapChain;
import org.tapchain.editor.ITapView;

public interface ILockedScroll {
	boolean onLockedScroll(ITapChain edit, ITapView selectedTap, IPoint wp);
}
