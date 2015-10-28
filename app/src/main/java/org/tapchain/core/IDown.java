package org.tapchain.core;

import org.tapchain.editor.IEditor;

public interface IDown {
	boolean onDown(IEditor edit, IPoint pos);
}
