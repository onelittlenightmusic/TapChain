package org.tapchain;

import org.tapchain.core.LinkType;
import org.tapchain.core.IPiece;
import org.tapchain.editor.IActorTap;

public interface IFocusable extends IActorTap {
	public void focus(LinkType al);
	public void unfocus();

}