package org.tapchain;

import org.tapchain.AndroidActor.AndroidView;
import org.tapchain.core.Actor;
import org.tapchain.core.ActorManager;
import org.tapchain.core.Chain;
import org.tapchain.core.ClassEnvelope;
import org.tapchain.core.IActorBlueprint;
import org.tapchain.core.IActorSharedHandler;
import org.tapchain.core.IBlueprint;
import org.tapchain.core.IPiece;
import org.tapchain.core.IPoint;
import org.tapchain.core.IState;
import org.tapchain.core.IValue;
import org.tapchain.core.LinkType;
import org.tapchain.core.Packet;
import org.tapchain.core.TapLib;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.IActorTap;
import org.tapchain.editor.ITapControlInterface;

import java.util.Collection;
import java.util.HashMap;

public class ActorTap extends AndroidView implements IActorTap, ITapControlInterface {
	IActorSharedHandler eventHandler = null;
	IPoint gridSize = new WorldPoint(1, 1);
	IPoint recent = null;
	private Actor mytapchain = null;
	private HashMap<Object, IActorTap> tapSet = null;
	private IPoint minGridSize = new WorldPoint(1,1);

	public ActorTap() {
		super();
	}

	@Override
	public void setMyActor(Actor mytapchain) {
		this.mytapchain = mytapchain;
	}

	@Override
	public void unsetActor() {
		this.mytapchain = null;
	}

	@Override
	public Actor getActor() {
		return mytapchain;
	}

	@Override
	public int onTick(Actor p, Packet obj) {
		return 1;
	}

	@Override
	public void changeState(IState state) {
	}

	protected ActorTap setEventHandler(IActorSharedHandler eh) {
		eventHandler = eh;
		return this;
	}

	@Override
	public IActorSharedHandler getSharedHandler() {
		return eventHandler;
	}

	@Override
	public boolean hasEventHandler() {
		return eventHandler != null;
	}

	@Override
	public boolean setGridSize(IPoint gs) {
		gridSize.set(gs);
		return true;
	}

	@Override
	public IPoint getGridSize() {
		return gridSize;
	}
	
	@Override
	public IPoint getMinGridSize() {
		return minGridSize;
	}
	
	public void initMinGridSize(IPoint _gridSize) {
		minGridSize = _gridSize;
		setGridSize(minGridSize);
	}

	@Override
	public IActorTap getAccessoryTap(Object key) {
		if(tapSet == null)
			return null;
		return tapSet.get(key);
	}

	@Override
	public Collection<IActorTap> getAccessoryTaps() {
		if(tapSet == null)
			return null;
		return tapSet.values();
	}
	
	@Override
	public IActorTap setAccessoryTap(Object key, IActorTap tap) {
		if(tapSet == null)
			tapSet = new HashMap<Object, IActorTap>();
		return tapSet.put(key, tap);
	}

	@Override
	public IActorTap unsetAccessoryTap(Object key) {
		if(tapSet == null)
			return null;
		return tapSet.remove(key);
	}

    @Override
    public void ctrlStart() throws Chain.ChainException, InterruptedException {
        super.ctrlStart();
        TapLib.setTap(this);
    }

    @Override
	public void ctrlStop() {
		if(tapSet != null)
			for(IActorTap accessory: tapSet.values()) 
				accessory.end();
        TapLib.removeTap(this);
        super.ctrlStop();
	}

    @Override
	public boolean onPush(Actor t, Object obj) {
		if(eventHandler != null) {
			eventHandler.onPush(this, LinkType.PUSH, obj);
			return true;
		}
		return false;
	}

	@Override
	public boolean setMyActorValue(Object obj) {
		return false;
	}

	@Override
	public void commitMyActorValue() {
	}

	@Override
	public Object getMyActorValue() {
		if(getActor() instanceof IValue)
			return ((IValue) getActor())._valueGet();
		return null;
	}

	@Override
	public IPoint getRecentPoint() {
		return recent;
	}

	@Override
	public void setRecentPoint(IPoint p) {
		recent = p;
	}

	@Override
	public void postAdd(IPiece p, IActorTap rtn, IBlueprint b, IPoint pos) {
	}

    @Override
    public boolean isFamilyTo(IActorTap a) {
        Actor thisActor = getActor(), aActor = a.getActor();
        if(thisActor == null || aActor == null)
            return false;
        return checkParent(thisActor, aActor) || checkParent(aActor, thisActor);
    }

    boolean checkParent(Actor child, Actor parent) {
        ClassEnvelope clz = child.getLinkClassFromLib(LinkType.FROM_PARENT);
        if (clz == null) {
            return false;
        }
        return clz.isAssignableFrom(parent.getLinkClassFromLib(LinkType.TO_CHILD));
    }
}