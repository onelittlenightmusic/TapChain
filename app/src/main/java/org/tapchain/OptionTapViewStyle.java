package org.tapchain;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.tapchain.core.Actor;
import org.tapchain.core.ChainException;
import org.tapchain.editor.ITapChain;
import org.tapchain.core.IPoint;
import org.tapchain.core.IScrollHandler;
import org.tapchain.core.IRegister;
import org.tapchain.core.IScrollable;
import org.tapchain.editor.IActorTapView;

public class OptionTapViewStyle extends ActorTapView implements IScrollable, IRegister {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4678324567401425522L;
	private IActorTapView parentTap = null;
	private ConcurrentLinkedQueue<IScrollHandler> handlers = new ConcurrentLinkedQueue<IScrollHandler>();

	public OptionTapViewStyle(IActorTapView t) {
		super();
		parentTap = t;
		setMyActor(parentTap.getActor());
		try {
			((Actor)parentTap).addMember(this);
		} catch (ChainException e) {
			e.printStackTrace();
		}
	}
	public IActorTapView getParentTap() {
		return parentTap;
	}
	
	@Override
	public final boolean onScrolled(ITapChain edit, IPoint pos, IPoint vp) {
//		setCenter(pos);
		for(IScrollHandler s : handlers)
			s.onScroll(edit, this, pos, vp);
		return false;
	}
	
	@Override
	public void registerHandler(IScrollHandler s) {
		handlers.add(s);
	}
	@Override
	public void unregisterHandler(IScrollHandler effectValue) {
		handlers.remove(effectValue);
	}
}
