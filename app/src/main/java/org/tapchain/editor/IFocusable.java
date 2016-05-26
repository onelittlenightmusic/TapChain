package org.tapchain.editor;

import org.tapchain.core.LinkType;

public interface IFocusable extends IActorTapView {
	void focus(IFocusControl focusControl, LinkType al);
	void unfocus(IFocusControl focusControl);

}