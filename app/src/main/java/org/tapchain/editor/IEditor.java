package org.tapchain.editor;

import java.util.Collection;
import java.util.List;

import org.tapchain.core.IGenericSharedHandler;
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
    //	public boolean checkAndDetach(ISystemPiece selected);
	public boolean checkAndAttach(VIEW selected, boolean onlyInclude);

    Collection<VIEW> getTaps();

    Collection<ACTOR> getActors();

    public VIEW toTap(ACTOR actor);
	public ACTOR toActor(VIEW view);
	public ActorManager edit();
	public ActorManager editTap();
	public IActionStyle getInteract();
	public Factory getFactory();
	public void log(String...strings);

	public void kickTapDraw(ITap startTap2);

	public VIEW getCapturedActorTap();
	public void setNextPos(IPoint nextConnectivity);
	public void resetNextPos();
	public List<IBlueprint<ACTOR>> highlightConnectables(LinkType reverse, ClassEnvelope ce);
	public void changePaletteToConnectables(LinkType ac,
			ClassEnvelope classEnvelope);
	public boolean connect(ACTOR a1, LinkType al, ACTOR a2);
	public void lockReleaseTap(IRelease t);
	public VIEW getLockedReleaseTap();
	void unhighlightConnectables();
}