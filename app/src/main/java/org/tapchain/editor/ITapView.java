package org.tapchain.editor;

import org.tapchain.core.IGenericSharedHandler;
import org.tapchain.core.IPoint;
import org.tapchain.core.ActorChain.IView;
import org.tapchain.core.IPiece;

public interface ITapView extends IView {
	boolean contains(IPoint iPoint);
    void invalidate();
}
