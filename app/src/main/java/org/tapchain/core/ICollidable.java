package org.tapchain.core;


import java.util.Collection;

import org.tapchain.core.ActorChain.IView;
import org.tapchain.editor.ITapChain;

public interface ICollidable<ACTOR extends IPiece> {
	public void onCollideInternal(ITapChain edit, IView v1, Collection<ACTOR> obj, IPoint pos);
}
