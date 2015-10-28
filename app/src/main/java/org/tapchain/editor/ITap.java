package org.tapchain.editor;

import org.tapchain.core.IGenericSharedHandler;
import org.tapchain.core.IPoint;
import org.tapchain.core.ActorChain.IView;
import org.tapchain.core.IPiece;

public interface ITap<EDITOR, ACTOR, VIEW> extends IView {
	public boolean hasEventHandler();
	public IGenericSharedHandler getSharedHandler();
	public boolean contains(IPoint iPoint);
}
