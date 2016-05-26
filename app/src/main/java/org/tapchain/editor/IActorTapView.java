package org.tapchain.editor;

import java.util.Collection;

import org.tapchain.core.Actor;
import org.tapchain.core.IPiece;
import org.tapchain.core.IPoint;
import org.tapchain.core.IState;
import org.tapchain.core.IValue;

public interface IActorTapView extends Tickable<Actor>, Pushable<Actor>, ITapView, IPiece<Actor> {
	Actor getActor();
	Object getMyActorValue();
	boolean setMyActorValue(Object obj);
	void commitMyActorValue();
	IPoint getGridSize();
	IPoint getMinGridSize();
	boolean setGridSize(IPoint add);
	IValue<IPoint> getSize();
	void changeState(IState state);
	IActorTapView getAccessoryTap(Object key);
	Collection<IActorTapView> getAccessoryTaps();
	IActorTapView setAccessoryTap(Object key, IActorTapView tap);
	IActorTapView unsetAccessoryTap(Object key);
	IPoint getRecentPoint();
	void setRecentPoint(IPoint p);
}