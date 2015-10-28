package org.tapchain;

import java.util.HashMap;

import org.tapchain.core.Actor;
import org.tapchain.core.LinkType;
import org.tapchain.editor.IEditor;
import org.tapchain.core.IPoint;
import org.tapchain.core.IValue;
import org.tapchain.core.PointValue;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.IActorTap;
import org.tapchain.editor.IFocusControl;

public class MyFocusControl extends ActorTap implements IFocusControl {
	HashMap<IFocusable, LinkType> array = new HashMap<IFocusable, LinkType>();
	private static final long serialVersionUID = 1L;
	Actor spotTargetActor = null;
	IActorTap spotTap = null;
	LinkType selectedLinkType = null;
	IActorTap parent = null;
	
	public enum SpotGroupLayout {
		RIGHT, LEFT, TOP, BOTTOM
	}
	public MyFocusControl() {
		super();
	}
	

	@Override
	public void unfocusAll(IFocusable spotOption) {
		for(IFocusable s: array.keySet())
			if(s != spotOption)
				s.unfocus();
	}
	
	@Override
	public void clearAllFocusables() {
		for(IFocusable s: array.keySet())
			s.end();
		array.clear();
		spotTargetActor = null;
	}

	@Override
	public void setTargetActor(Actor a, IActorTap tap) {
		spotTargetActor = a;
		spotTap = tap;
	}
	
	public Actor getTargetActor() {
		return spotTargetActor;
	}
	
	public LinkType getFocusedLinkType() {
		return selectedLinkType;
	}
	public void setSpotActorLink(LinkType al) {
		selectedLinkType = al;
	}

	@Override
	public void init(IActorTap t) {
		getSize()._valueGet().clear();
		getSize()._valueGet().setOffset(t.getSize(), true);
		parent = t;
	}

	@Override
	public void addFocusable(IFocusable spot, LinkType al) {
		array.put(spot, al);
		IValue<IPoint> offset = null;
		switch(al) {
		case FROM_PARENT:
			offset = new PointValue(getSize()._valueGet(), new WorldPoint(0, 1));
			break;
		case TO_CHILD://TOP
			offset = new PointValue(getSize()._valueGet(), new WorldPoint(0, -1));
			break;
		default:
		}
		if(offset != null)
			((IActorTap)spot).getCenter().setOffset(offset, false);
	}
	
	public void save(IEditor edit) {
		for(IFocusable focusable : array.keySet()) {
			edit.editTap().add((Actor) focusable).save();
		}
	}
	
	public void large() {
		getSize()._valueGet().set(new WorldPoint(200f, 200f));
	}

	public void small() {
		getSize()._valueGet().set(new WorldPoint());
	}
}