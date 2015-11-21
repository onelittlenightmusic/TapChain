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

public interface IActorTap extends Tickable<Actor>, Pushable<Actor>, ITap, IPiece<Actor> {
	ControllableSignal interrupt(ControllableSignal end);
	Actor getActor();
	Object getMyActorValue();
	boolean setMyActorValue(Object obj);
	void commitMyActorValue();
	IPoint getGridSize();
	IPoint getMinGridSize();
	boolean setGridSize(IPoint add);
	IValue<IPoint> getSize();
	void changeState(IState state);
	IActorTap getAccessoryTap(Object key);
	Collection<IActorTap> getAccessoryTaps();
	IActorTap setAccessoryTap(Object key, IActorTap tap);
	IActorTap unsetAccessoryTap(Object key);
	IPoint getRecentPoint();
	void setRecentPoint(IPoint p);
    boolean isFamilyTo(IActorTap a);
}