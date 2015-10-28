package org.tapchain.core;

import org.tapchain.editor.IEditor;

public interface IScrollable {
	boolean onScrolled(IEditor edit, IPoint pos, IPoint vp);
}
