package org.tapchain.core;

import java.util.concurrent.ConcurrentSkipListSet;

public class LinkBooleanSet extends ConcurrentSkipListSet<LinkType> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5027017289304512517L;
	static LinkBooleanSet PULL = new LinkBooleanSet(LinkType.PULL);
	static LinkBooleanSet PUSH = new LinkBooleanSet(LinkType.PUSH);
	static LinkBooleanSet PARENT = new LinkBooleanSet(LinkType.FROM_PARENT);
	static LinkBooleanSet APPEARANCE = new LinkBooleanSet(LinkType.TO_CHILD);
	static LinkBooleanSet NULL = new LinkBooleanSet();
	
	public LinkBooleanSet() {
		super();
	}
	
	public LinkBooleanSet(LinkType al) {
		this();
		setTrue(al);
	}

	public void set(LinkType ac, boolean add) {
		if(add) {
			setTrue(ac);
		} else {
			setFalse(ac);
		}
	}
	
	public void setTrue(LinkType ac) {
		add(ac);
	}
	
	public void setFalse(LinkType ac) {
		remove(ac);
	}
	
	public boolean isTrue(LinkType ac) {
		return contains(ac);
	}
	
	public boolean hasAnyConnect() {
		return !isEmpty();
	}

}