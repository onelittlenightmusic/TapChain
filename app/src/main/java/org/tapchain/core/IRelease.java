package org.tapchain.core;

import org.tapchain.editor.IEditor;

public interface IRelease {
	public void onRelease(IPoint pos, IEditor edit);
}
