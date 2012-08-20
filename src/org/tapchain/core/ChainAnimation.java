package org.tapchain.core;

import java.util.LinkedList;
import java.util.List;

import org.tapchain.core.ActorChain.IAnimation;
import org.tapchain.core.ActorChain.IView;
import org.tapchain.core.Chain.ChainException;


public class ChainAnimation implements IAnimation {
	List<IAnimation> q = new LinkedList<IAnimation>();
	SyncQueue<String> queue = new SyncQueue<String>();
	public ChainAnimation() {
	}
	public ChainAnimation addNext(IAnimation ea) {
		q.add(ea);
		return this;
	}
	@Override
	public boolean actorRun() throws ChainException, InterruptedException {
		if(!q.get(0).actorRun()) {
			q.remove(0);
		}
		return !q.isEmpty();
	}

	@Override
	public boolean view_impl(Object canvas) {
		q.get(0).view_impl(canvas);
		return false;
	}
	@Override
	public IView setCenter(WorldPoint point) {
		return null;
	}
	@Override
	public IView setAlpha(int i) {
		return null;
	}
	
}
