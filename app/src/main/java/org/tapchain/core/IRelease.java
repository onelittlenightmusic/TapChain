package org.tapchain.core;

import org.tapchain.editor.IEditor;

public interface IRelease {
	boolean onRelease(IEditor edit, IPoint pos);
}
