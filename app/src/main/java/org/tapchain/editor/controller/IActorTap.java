package org.tapchain.editor.controller;

import java.util.Collection;

import org.tapchain.ActorTap;
import org.tapchain.ColorLib;
import org.tapchain.core.Actor;
import org.tapchain.core.Actor.ControllableSignal;
import org.tapchain.core.ActorManager;
import org.tapchain.core.IActorBlueprint;
import org.tapchain.core.IActorSharedHandler;
import org.tapchain.core.IBlueprint;
import org.tapchain.core.IGenericSharedHandler;
import org.tapchain.core.IPiece;
import org.tapchain.core.IPoint;
import org.tapchain.core.IState;
import org.tapchain.core.IValue;
import org.tapchain.editor.IActorEditor;
import org.tapchain.editor.ITap;
import org.tapchain.editor.TapChainEditor;
import org.tapchain.editor.TapChainEditor.Pushable;
import org.tapchain.editor.TapChainEditor.Tickable;
import org.tapchain.editor.view.IActorTapView;

public interface IActorTap extends TapChainEditor.Tickable<Actor> {
	public boolean hasEventHandler();
	public IActorSharedHandler getSharedHandler();
	public Actor getActor();
	public Object getMyActorValue();

	//	Factory f;
    //	int num;
    //	/** Set the method for copying this instance.
    //	 * @param factory
    //	 * @param n
    //	 */
    //	public void setCopyMethod(Factory factory, int n) {
    //		f = factory;
    //		num = n;
    //	}
    //
    //	public Factory getCopyFactory() {
    //		return f;
    //	}
    //
    //	public int getCopyNumber() {
    //		return num;
    //	}
    //
	boolean onPush(Actor t, Object obj);

	//	ActorManager editorManager;
	void onAdd(ActorManager maker);

	ActorTap end();

	public boolean setMyActorValue(Object obj);
	public void commitMyActorValue();

	IActorTapView getView();

	void setView(IActorTapView v);

	public void changeState(IState state);
	public IActorTap getAccessoryTap(Object key);
	public Collection<IActorTap> getAccessoryTaps();
	public IActorTap setAccessoryTap(Object key, IActorTap tap);
	public IActorTap unsetAccessoryTap(Object key);
	public void setActorBlueprint(IActorBlueprint b);
	public IActorBlueprint getActorBlueprint();
	public void postAdd(IPiece p, IActorTap rtn, IBlueprint b, IPoint pos);

    boolean isFamilyTo(IActorTap a);


	public IValue<IPoint> getSize();
	public IPoint getGridSize();
	public IPoint getMinGridSize();
	public boolean setGridSize(IPoint add);
	public IPoint getRecentPoint();
	public void setRecentPoint(IPoint p);
	void setColorCode(ColorLib.ColorCode red);

}