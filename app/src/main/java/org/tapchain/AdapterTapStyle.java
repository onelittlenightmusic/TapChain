package org.tapchain;

import org.tapchain.core.LinkType;
import org.tapchain.core.ClassEnvelope;
import org.tapchain.editor.IEditor;
import org.tapchain.core.IPoint;
import org.tapchain.core.IRelease;
import org.tapchain.core.IScrollHandler;
import org.tapchain.editor.IActorTap;

public class AdapterTapStyle extends OptionTapStyle implements IScrollHandler, IRelease {
	public AdapterTapStyle(IActorTap t) {
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
	public void onScroll(IEditor edit, IActorTap tap, IPoint pos, IPoint vp) {
		setCenter(pos);
	}
	
	@Override
	public void onRelease(IEditor edit, IPoint pos) {
		edit.checkAndAttach(this);
	}
}
