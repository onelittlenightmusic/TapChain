package org.tapchain.core;

import org.tapchain.editor.ITapChain;

public interface IRelease {
	boolean onRelease(ITapChain edit, IPoint pos);
}
