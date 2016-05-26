package org.tapchain.editor;

import org.tapchain.core.Actor;
import org.tapchain.core.IPath;
import org.tapchain.core.IPiece;
import org.tapchain.core.IPoint;
import org.tapchain.core.Actor.ControllableSignal;
import org.tapchain.core.ActorChain.IView;

public interface IPathTapView extends IView, IPiece<Actor>, Tickable<IPath>, ITapView {
	void setMyPath(IPath path);
	void unsetMyPath();
	IPath getMyPath();
	ControllableSignal interrupt(ControllableSignal end);
	IPoint getRecentPoint();
	void setRecentPoint(IPoint newKey);
//	void setTapChain(ActorManager manager);
	IPoint getPoint(float beta);
}
