package org.tapchain.editor;

import org.tapchain.IFocusable;
import org.tapchain.core.Actor;
import org.tapchain.core.LinkType;

/**
 * Created by hiro on 2015/05/04.
 */
public interface IFocusControl {
    public void unfocusAll(IFocusable spotOption);
    public void clearAllFocusables();
    public void addFocusable(IFocusable spot, LinkType al);
    public void large();

    public void small();
    public void init(IActorTap v);

    void setTargetActor(Actor actor, IActorTap spot);

    LinkType getLinkType();

    Actor getTargetActor();

    void save(IEditor edit);

    void setSpotActorLink(LinkType al);
}
