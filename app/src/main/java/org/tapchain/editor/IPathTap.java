package org.tapchain.editor;

import org.tapchain.core.Actor;
import org.tapchain.core.ActorManager;
import org.tapchain.core.IPath;
import org.tapchain.core.IPiece;
import org.tapchain.core.IPoint;
import org.tapchain.core.Actor.ControllableSignal;
import org.tapchain.core.ActorChain.IView;
import org.tapchain.editor.TapChainEditor.Tickable;

public interface IPathTap extends IView, IPiece<Actor>, Tickable<IPath>, ITap<IActorEditor, Actor, IPathTap> {
	public void setMyPath(IPath path);
	public void unsetMyPath();
	public IPath getMyPath();
	public ControllableSignal interrupt(ControllableSignal end);
	public IPoint getRecentPoint();
	public void setRecentPoint(IPoint newKey);
	public void setEditor(ActorManager manager);
	public IPoint getPoint(float beta);
}
