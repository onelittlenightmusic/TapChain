package org.tapchain.core;

import org.tapchain.editor.IActorEditor;

public interface ISelectable {
	void onSelected(IActorEditor edit, IPoint pos);
}
