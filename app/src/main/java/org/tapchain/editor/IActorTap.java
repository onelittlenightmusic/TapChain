package org.tapchain.editor;

import java.util.Collection;

import org.tapchain.core.Actor;
import org.tapchain.core.Actor.ControllableSignal;
import org.tapchain.core.ActorChain.IView;
import org.tapchain.core.IActorBlueprint;
import org.tapchain.core.IBlueprint;
import org.tapchain.core.IPiece;
import org.tapchain.core.IPoint;
import org.tapchain.core.IState;
import org.tapchain.core.IValue;
import org.tapchain.core.Piece;
import org.tapchain.editor.TapChainEditor.Pushable;
import org.tapchain.editor.TapChainEditor.Tickable;

public interface IActorTap extends Tickable<Actor>, Pushable<Actor>, ITap<IActorEditor, Actor, IActorTap>, IPiece<Actor> {
	public ControllableSignal interrupt(ControllableSignal end);
	public Actor getActor();
	public Object getMyActorValue();
	public boolean setMyActorValue(Object obj);
	public void commitMyActorValue();
	public IPoint getGridSize();
	public IPoint getMinGridSize();
	public boolean setGridSize(IPoint add);
	public IValue<IPoint> getSize();
	public void changeState(IState state);
	public IActorTap getAccessoryTap(Object key);
	public Collection<IActorTap> getAccessoryTaps();
	public IActorTap setAccessoryTap(Object key, IActorTap tap);
	public IActorTap unsetAccessoryTap(Object key);
	public IPoint getRecentPoint();
	public void setRecentPoint(IPoint p);
	public void setActorBlueprint(IActorBlueprint b);
	public IActorBlueprint getActorBlueprint();
	public void postAdd(IPiece p, IActorTap rtn, IBlueprint b, IPoint pos);

    boolean isFamilyTo(IActorTap a);
}