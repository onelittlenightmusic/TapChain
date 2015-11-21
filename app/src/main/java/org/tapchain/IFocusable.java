package org.tapchain;

import org.tapchain.core.LinkType;
import org.tapchain.core.IPiece;
import org.tapchain.editor.IActorTap;
import org.tapchain.editor.IEditor;
import org.tapchain.editor.IFocusControl;

public interface IFocusable extends IActorTap {
	void focus(IFocusControl focusControl, LinkType al);
	void unfocus(IFocusControl focusControl);

}