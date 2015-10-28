package org.tapchain.core;

import org.tapchain.editor.IEditor;

public interface ISelectable {
	public void onSelected(IEditor edit, IPoint pos);
}
