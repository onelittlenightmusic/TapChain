package org.tapchain;

import java.util.HashMap;

import org.tapchain.core.Actor;
import org.tapchain.core.ActorManager;
import org.tapchain.core.LinkType;
import org.tapchain.editor.IFocusControl;
import org.tapchain.editor.IFocusable;
import org.tapchain.editor.TapChain;
import org.tapchain.editor.TapManager;

public class MyFocusControl extends ActorTapView implements IFocusControl {
	HashMap<IFocusable, LinkType> array = new HashMap<IFocusable, LinkType>();
	private static final long serialVersionUID = 1L;
	Actor spotTargetActor = null;
	LinkType selectedLinkType = null;
    TapChain tapChain;

	public enum SpotGroupLayout {
		RIGHT, LEFT, TOP, BOTTOM
	}
	public MyFocusControl(TapChain t) {
		super();
        tapChain = t;
	}
	

	@Override
	public void unfocusAll(IFocusable spotOption) {
		for(IFocusable s: array.keySet())
			if(s != spotOption)
				s.unfocus(this);
        setSpotActorLink(null);
    }
	
	@Override
	public void clearAllFocusables() {
		for(IFocusable s: array.keySet())
			s.end();
		array.clear();
		spotTargetActor = null;
	}

	@Override
	public void setTargetActor(Actor a) {
		spotTargetActor = a;
	}
	
	public Actor getTargetActor() {
		return spotTargetActor;
	}
	
	public LinkType getLinkType() {
		return selectedLinkType;
	}
	public void setSpotActorLink(LinkType al) {
		selectedLinkType = al;
	}

	@Override
	public void addFocusable(IFocusable spot, LinkType al) {
		array.put(spot, al);
	}
	
	public void save() {
        ActorManager manager = new TapManager(tapChain).editTap();
		for(IFocusable focusable : array.keySet()) {
			manager.add((Actor) focusable).save();
		}
	}
}