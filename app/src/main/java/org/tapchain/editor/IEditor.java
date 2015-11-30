package org.tapchain.editor;

import java.util.Collection;
import java.util.List;

import org.tapchain.core.IActorSharedHandler;
import org.tapchain.core.LinkType;
import org.tapchain.core.ActorManager;
import org.tapchain.core.ClassEnvelope;
import org.tapchain.core.Factory;
import org.tapchain.core.IActionStyle;
import org.tapchain.core.IBlueprint;
import org.tapchain.core.IPiece;
import org.tapchain.core.IPoint;
import org.tapchain.core.IRelease;

public interface IEditor<ACTOR extends IPiece, VIEW extends ITap> {
	boolean checkAndAttach(VIEW selected, boolean onlyInclude);
    Collection<VIEW> getTaps();
    Collection<ACTOR> getActors();
    VIEW toTap(ACTOR actor);
	ACTOR toActor(VIEW view);
	ActorManager edit();
	ActorManager editTap();
	IActionStyle getInteract();
	Factory getFactory();
	void log(String...strings);
	void kickTapDraw(ITap startTap2);
	VIEW getCapturedActorTap();
	void setNextPos(IPoint nextConnectivity);
	void resetNextPos();
	List<IBlueprint<ACTOR>> highlightConnectables(LinkType reverse, IActorTap target, ClassEnvelope ce);
	boolean connect(ACTOR a1, LinkType al, ACTOR a2);
	void lockReleaseTap(IRelease t);
//	VIEW getLockedReleaseTap();
	void unhighlightConnectables();
    IActorSharedHandler getEventHandler();
}