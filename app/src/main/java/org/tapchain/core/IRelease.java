package org.tapchain.core;

import org.tapchain.editor.IEditor;

public interface IRelease {
	public void onRelease(IEditor edit, IPoint pos);
}
