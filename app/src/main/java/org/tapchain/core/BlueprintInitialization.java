package org.tapchain.core;

import java.io.Serializable;

public class BlueprintInitialization 
	implements IBlueprintInitialization, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2728411892052137465L;
	Object o;
	String t;
	public BlueprintInitialization(Object obj, String tag) {
		setObject(obj);
		setTag(tag);
	}
	
	public void setObject(Object obj) {
		o = obj;
	}
	
	public Object getObject() {
		return o;
	}
	
	public void setTag(String tag) {
		t = tag;
	}
	
	public String getTag() {
		return t;
	}

	@Override
	public void init(IPiece rtn) {
		((IValue)rtn)._valueSet(getObject());
	}
}
