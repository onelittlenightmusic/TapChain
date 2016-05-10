package org.tapchain;

import android.app.Activity;

import org.tapchain.AndroidActor.AndroidView;
import org.tapchain.core.Actor;
import org.tapchain.core.ActorManager;
import org.tapchain.core.ChainException;
import org.tapchain.core.IPoint;
import org.tapchain.core.IState;
import org.tapchain.core.IValue;
import org.tapchain.core.Packet;
import org.tapchain.core.TapLib;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.IActorTap;
import org.tapchain.editor.ITapControlInterface;

import java.util.Collection;
import java.util.HashMap;

public class ActorTap extends AndroidView implements IActorTap, ITapControlInterface {
	IPoint gridSize = new WorldPoint(1, 1);
	IPoint recent = null;
	private Actor mytapchain = null;
	private HashMap<Object, IActorTap> tapSet = null;
	private IPoint minGridSize = new WorldPoint(1,1);

	public ActorTap() {
		super();
	}

    public ActorTap(Activity act) {
        super(act);
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
			tapSet = new HashMap<>();
		return tapSet.put(key, tap);
	}

	@Override
	public IActorTap unsetAccessoryTap(Object key) {
		if(tapSet == null)
			return null;
		return tapSet.remove(key);
	}

    @Override
    public void ctrlStart() throws ChainException, InterruptedException {
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
			return true;
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
			return ((IValue) getActor())._get();
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

}