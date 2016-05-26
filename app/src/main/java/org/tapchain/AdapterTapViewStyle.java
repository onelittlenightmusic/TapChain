package org.tapchain;

import org.tapchain.core.LinkType;
import org.tapchain.core.ClassEnvelope;
import org.tapchain.editor.ITapChain;
import org.tapchain.core.IPoint;
import org.tapchain.core.IRelease;
import org.tapchain.core.IScrollHandler;
import org.tapchain.editor.IActorTapView;

public class AdapterTapViewStyle extends OptionTapViewStyle implements IScrollHandler, IRelease {
	public AdapterTapViewStyle(IActorTapView t) {
		super(t);
		registerHandler(this);
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 110974406008187568L;
	private LinkType ac;
	ClassEnvelope ce = null;

	public void setLink(LinkType ac) {
		this.ac = ac;
	}

	public LinkType getLink() {
		return ac;
	}

	public ClassEnvelope getClassEnvelope() {
		return ce;
	}

	@Override
	public void onScroll(ITapChain edit, IActorTapView tap, IPoint pos, IPoint vp) {
		setCenter(pos);
	}
	
	@Override
	public boolean onRelease(ITapChain edit, IPoint pos) {
		edit.checkAndConnect(this);
        return true;
	}
}
