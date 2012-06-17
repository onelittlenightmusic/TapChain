package org.tapchain;

import java.util.LinkedList;
import java.util.List;

import org.tapchain.ActorChain.TapChainAnimation;
import org.tapchain.ActorChain.IView;
import org.tapchain.Chain.ChainException;


public class ChainAnimation implements TapChainAnimation {
	List<TapChainAnimation> q = new LinkedList<TapChainAnimation>();
	SyncQueue<String> queue = new SyncQueue<String>();
	public ChainAnimation() {
	}
	public ChainAnimation addNext(TapChainAnimation ea) {
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
	
}
