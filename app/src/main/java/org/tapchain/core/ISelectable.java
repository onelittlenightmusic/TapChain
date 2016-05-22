package org.tapchain.core;

import org.tapchain.editor.ITapChain;

public interface ISelectable {
	void onSelected(ITapChain tapChain, IPoint pos);
}
