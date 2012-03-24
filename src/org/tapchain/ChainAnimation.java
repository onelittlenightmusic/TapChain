package org.tapchain;

import java.util.LinkedList;
import java.util.List;

import org.tapchain.AnimationChain.TapChainAnimation;
import org.tapchain.AnimationChain.TapChainViewI;
import org.tapchain.Chain.ChainException;


import android.graphics.Canvas;


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
	public boolean effect_run() throws ChainException, InterruptedException {
		if(!q.get(0).effect_run()) {
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
	public int getDuration() {
		return 0;
	}
	@Override
	public TapChainViewI setCenter(WorldPoint point) {
		return null;
	}
	
}
