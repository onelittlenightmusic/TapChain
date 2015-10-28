package org.tapchain.core;

import java.util.LinkedList;
import java.util.List;

import org.tapchain.core.ActorChain.IView;


public class ChainAnimation /*implements IAnimation*/ {
	List<IView> q = new LinkedList<IView>();
	SyncQueue<String> queue = new SyncQueue<String>();
	public ChainAnimation() {
	}
	public ChainAnimation addNext(IView ea) {
		q.add(ea);
		return this;
	}
}
