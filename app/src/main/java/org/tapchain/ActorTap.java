package org.tapchain;

import java.util.Collection;
import java.util.HashMap;

import org.tapchain.AndroidActor.AndroidView;
import org.tapchain.core.Actor;
import org.tapchain.core.IActorSharedHandler;
import org.tapchain.core.LinkType;
import org.tapchain.core.ActorManager;
import org.tapchain.core.ClassEnvelope;
import org.tapchain.core.IActorBlueprint;
import org.tapchain.core.IBlueprint;
import org.tapchain.core.IPiece;
import org.tapchain.core.IPoint;
import org.tapchain.core.IState;
import org.tapchain.core.IValue;
import org.tapchain.core.TapLib;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.ITapControlInterface;
import org.tapchain.editor.IActorTap;

public class ActorTap extends AndroidView implements IActorTap, ITapControlInterface {
	IActorSharedHandler event = null;
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
	public int onTick(Actor p, Object obj) {
		return 1;
	}

	@Override
	public void changeState(IState state) {
	}

	protected ActorTap setEventHandler(IActorSharedHandler eh) {
		event = eh;
		return this;
	}

	@Override
	public IActorSharedHandler getSharedHandler() {
		return event;
	}

	@Override
	public boolean hasEventHandler() {
		return event != null;
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
	public void view_end() {
		if(tapSet != null)
			for(IActorTap accessory: tapSet.values()) 
				accessory.end();
	}
	
	@Override
	public boolean onPush(Actor t, Object obj) {
		if(event != null) {
			event.onPush(this, LinkType.PUSH, obj);
			return true;
		}
		return false;
	}

	@Override
	public void onAdd(ActorManager maker) {
		TapLib.setTap(this);
	}
	
	@Override
	public ActorTap end() {
		super.end();
		TapLib.removeTap(this);
		return this;
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
	public void setActorBlueprint(IActorBlueprint b) {
	}

	@Override
	public IActorBlueprint getActorBlueprint() {
		return null;
	}

	@Override
	public void postAdd(IPiece p, IActorTap rtn, IBlueprint b, IPoint pos) {
	}

    @Override
    public boolean isFamilyTo(IActorTap a) {
        if(a instanceof ActorTap) {
            ClassEnvelope clz;
            IActorBlueprint ab = getActorBlueprint(), ab2 = a.getActorBlueprint();
            if(ab != null && ab2 != null) {
                clz = ab.getConnectClass(LinkType.FROM_PARENT);
                if (clz != null) {
                    if (clz.isAssignableFrom(ab2.getConnectClass(LinkType.TO_CHILD))) {
                        return true;
                    }
                }
                clz = ab2.getConnectClass(LinkType.FROM_PARENT);
                if (clz != null) {
                    if (clz.isAssignableFrom(ab.getConnectClass(LinkType.TO_CHILD))) {
                        return true;
                    }
                }
            }

        }
        return false;
    }
}