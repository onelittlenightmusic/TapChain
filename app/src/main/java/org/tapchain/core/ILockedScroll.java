package org.tapchain.core;

import org.tapchain.editor.IEditor;
import org.tapchain.editor.ITap;

public interface ILockedScroll {
	boolean onLockedScroll(IEditor edit, ITap selectedTap, IPoint wp);
}
